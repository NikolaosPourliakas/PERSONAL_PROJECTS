/* File: 
			/maze/mazeEngine.js
   Purpose: 
			Contains functions to generate the maze layout and render the maze cells as JSX 
*/

/* Creates random maze, defines start and end positions, 
   carves paths recursively using a randomized DFS algorithm */
   
export function setupMaze(size = 41) {
	
	// Initialize maze full of walls
	
	const maze = Array.from({ length: size }, () => Array(size).fill("wall"));

	/* Randomly shuffles an array (used to randomize directions) */
	
	function shuffleArray(arr) {
		
		const a = [...arr];
		for (let i = a.length - 1; i > 0; i--) {
			const j = Math.floor(Math.random() * (i + 1));
			[a[i], a[j]] = [a[j], a[i]];
		}
		return a;
	}

	/* Recursively carves empty paths in the maze */
	
	function carve(r, c) {
		
		maze[r][c] = "empty";
		const dirs = shuffleArray([[0, 2], [0, -2], [2, 0], [-2, 0]]);
		for (const [dr, dc] of dirs) {
			const nr = r + dr, nc = c + dc;
			if (nr > 0 && nr < size - 1 && nc > 0 && nc < size - 1 && maze[nr][nc] === "wall") {
				maze[r + dr / 2][c + dc / 2] = "empty";
				carve(nr, nc);
			}
		}
	}

	// Select random starting cell for carving
	
	const startR = 1 + 2 * Math.floor(Math.random() * ((size - 2) / 2));
	const startC = 1 + 2 * Math.floor(Math.random() * ((size - 2) / 2));
	carve(startR, startC);

	// Define corners for start/end
	
	const corners = [[1,1],[1,size-2],[size-2,1],[size-2,size-2]];
	const startCorner = corners[Math.floor(Math.random()*4)];
	let endCorner;
	if (startCorner[0] === 1 && startCorner[1] === 1) endCorner = [size-2,size-2];
	else if (startCorner[0] === 1 && startCorner[1] === size-2) endCorner = [size-2,1];
	else if (startCorner[0] === size-2 && startCorner[1] === 1) endCorner = [1,size-2];
	else endCorner = [1,1];

	// Mark start and end cells
	
	maze[startCorner[0]][startCorner[1]] = "start";
	maze[endCorner[0]][endCorner[1]] = "end";

	return { maze, start: startCorner, end: endCorner };
}

/* Renders the maze as a JSX grid of divs 
with appropriate classes for wall, empty, start, end, and player */

export function renderMazeCells(maze, player) {
	
	return maze.flatMap((row, r) =>
		row.map((cell, c) => {
			let className = "maze-cell";
			if (cell) className += ` ${cell}`;
			if (player && r === player.y && c === player.x) className += " player";
			return <div key={`${r}-${c}`} className={className} />;
		})
	);
}
