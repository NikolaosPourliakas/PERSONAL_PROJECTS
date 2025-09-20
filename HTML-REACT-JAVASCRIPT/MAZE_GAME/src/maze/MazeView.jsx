/* File: 
			/maze/MazeView.jsx
   Purpose: 
			Main React component for rendering the maze game, including maze grid, 
			counters (moves/time/best), start/restart buttons, and arrow navigation.
			Handles state management and timer logic.
*/

import React, { useState, useEffect, useRef } from "react";
import { setupMaze, renderMazeCells } from "./mazeEngine";
import { ArrowButtons, handleKeyboard } from "./movement";
import "../styles/index.css";

/* Renders maze container, top panel with buttons and counters,
   and the maze grid. Handles player state, movement, and timing */
   
export default function MazeView({ size = 41 }) {
	
	// --- State variables ---
	
	const [mazeData, setMazeData] = useState({ maze: [], start: [1,1], end: [size-2, size-2] });
	const [player, setPlayer] = useState({ x: 1, y: 1 });
	const [moves, setMoves] = useState(0);
	const [time, setTime] = useState(0);
	const [bestMoves, setBestMoves] = useState(null);
	const [bestTime, setBestTime] = useState(null);
	const [endReached, setEndReached] = useState(false);

	const [newHover, setNewHover] = useState(false);
	const [restartHover, setRestartHover] = useState(false);

	const timerRef = useRef(null);
	const playerRef = useRef(player);

	// --- Initialize maze on size change ---
	
	useEffect(() => {
		const newMazeData = setupMaze(size);
		setMazeData(newMazeData);
		setPlayer({ x: newMazeData.start[1], y: newMazeData.start[0] });
		setMoves(0); setTime(0); setEndReached(false);
		setBestMoves(null); setBestTime(null);
		setNewHover(false); setRestartHover(false);
	}, [size]);

	// --- Keep playerRef updated ---
	
	useEffect(() => { playerRef.current = player; }, [player]);

	// --- Timer for time counter ---
	
	useEffect(() => {
		if (!endReached) {
			timerRef.current = setInterval(() => setTime(t => t + 1), 1000);
			return () => clearInterval(timerRef.current);
		}
	}, [endReached]);

	// --- Keyboard movement handler ---
	
	useEffect(() => {
		const handleKey = (e) => {
			if (endReached) return;
			const newPlayer = handleKeyboard(e, playerRef.current, mazeData.maze);
			if (newPlayer.x !== playerRef.current.x || newPlayer.y !== playerRef.current.y) {
				setPlayer(newPlayer);
				setMoves(m => m + 1);
			}
		};
		window.addEventListener("keydown", handleKey);
		return () => window.removeEventListener("keydown", handleKey);
	}, [mazeData.maze, endReached]);

	// --- Check if end reached and update best scores ---
	
	useEffect(() => {
		if (player.y === mazeData.end[0] && player.x === mazeData.end[1]) {
			setEndReached(true);
			clearInterval(timerRef.current);
			if (bestMoves === null || moves < bestMoves) setBestMoves(moves);
			if (bestTime === null || time < bestTime) setBestTime(time);

			setRestartHover(true);
			const timer = setTimeout(() => { setRestartHover(false); setNewHover(true); }, 5000);
			return () => clearTimeout(timer);
		}
	}, [player, mazeData.end, moves, time]);

	// --- Button handlers ---
	
	const handleRestart = () => {
		setPlayer({ x: mazeData.start[1], y: mazeData.start[0] });
		setMoves(0); setTime(0); setEndReached(false);
		setRestartHover(false); setNewHover(false);
	};

	const handleNewMaze = () => {
		const newMazeData = setupMaze(size);
		setMazeData(newMazeData);
		setPlayer({ x: newMazeData.start[1], y: newMazeData.start[0] });
		setMoves(0); setTime(0); setEndReached(false);
		setBestMoves(null); setBestTime(null);
		setNewHover(false); setRestartHover(false);
	};

	// --- Render MazeView ---
	
	return (
		<div className="maze-container">
		
			<div className="top-panel">
			
				<button className={`start-button newMaze ${newHover ? "hover" : ""}`} onClick={handleNewMaze}>NEW</button>
				
				<div className="counters-box">
				
					<div className="counter-line">
						<div className="counter-box">Moves: {moves}</div>
						{bestMoves !== null && <div className="counter-box best-box">Best: {bestMoves}</div>}
					</div>
					
					<div className="counter-line">
						<div className="counter-box">Time: {time}s</div>
						{bestTime !== null && <div className="counter-box best-box">Best: {bestTime}s</div>}
					</div>
					
				</div>
				
				<button className={`start-button restart ${restartHover ? "hover" : ""}`} onClick={handleRestart}>RESTART</button>
				
				<ArrowButtons player={player} setPlayer={(p)=>{ setPlayer(p); setMoves(m=>m+1); }} maze={mazeData.maze}/>
			
			</div>
			
			<div className="maze" style={{ "--maze-size": size }}>
				{renderMazeCells(mazeData.maze, player)}
			</div>
			
		</div>
	);
}
