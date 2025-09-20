/* File: 
			/src/App.jsx
   Purpose:
			Main app component rendering MazeView and importing consolidated CSS. */

import React from "react";
import MazeView from "./maze/MazeView";
import "./styles/index.css";

export default function App() {
    return (
        <div className="app">
            <MazeView size={41} />
        </div>
    );
}
