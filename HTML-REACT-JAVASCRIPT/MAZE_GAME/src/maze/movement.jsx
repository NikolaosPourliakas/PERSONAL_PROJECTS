/* File: 
			/maze/MazeMovement.js
   Purpose: 
			Handles player movement via arrow buttons and keyboard,
			and renders the arrow button controls for the maze.
*/

/* Renders four arrow buttons in a cross layout to move the player */

import React from "react";

export function ArrowButtons({ player, setPlayer, maze }) {
	
	// Handle a click on an arrow button
	
	const handleArrowClick = (key) => {
		const newPlayer = handleKeyboard({ key }, player, maze);
		if (newPlayer.x !== player.x || newPlayer.y !== player.y) setPlayer(newPlayer);
	};

	return (
	
		<div className="arrow-buttons-frame">
		
			<button className="arrow-button up" onClick={() => 
			handleArrowClick("ArrowUp")}>↑</button>
			
			<button className="arrow-button down" onClick={() => 
			handleArrowClick("ArrowDown")}>↓</button>
			
			<button className="arrow-button left" onClick={() => 
			handleArrowClick("ArrowLeft")}>←</button>
			
			<button className="arrow-button right" onClick={() => 
			handleArrowClick("ArrowRight")}>→</button>
			
		</div>
	);
}

/* Handles player movement via keyboard arrows, 
moving until hitting a wall or intersection */

export function handleKeyboard(e, player, maze) {
	
	if (!player) return player;

	const dirMap = {
		ArrowUp: { x: 0, y: -1 },
		ArrowDown: { x: 0, y: 1 },
		ArrowLeft: { x: -1, y: 0 },
		ArrowRight: { x: 1, y: 0 },
	};

	const dir = dirMap[e.key];
	if (!dir) return player;

	let { x, y } = player;
	const size = maze.length;

	while (true) {
		const nx = x + dir.x;
		const ny = y + dir.y;
		if (nx < 0 || nx >= size || ny < 0 || ny >= size || maze[ny][nx] === "wall") break;
		x = nx;
		y = ny;
		// Stop at intersections
		if (isIntersection(maze, x, y)) break;
	}

	return { x, y };
}

/* Determines if a cell is an intersection in the maze 
   (stops automatic movement if true) */
   
function isIntersection(maze, x, y) {
	
	const dirs = [
		{ dx: 0, dy: 1 }, { dx: 0, dy: -1 }, { dx: 1, dy: 0 }, { dx: -1, dy: 0 }
	];
	let emptyNeighbors = 0;
	for (const {dx, dy} of dirs) {
		const nx = x + dx;
		const ny = y + dy;
		if (nx >= 0 && nx < maze.length && ny >= 0 && ny < maze.length) {
			if (maze[ny][nx] !== "wall") emptyNeighbors++;
		}
	}
	return emptyNeighbors !== 2;
}
