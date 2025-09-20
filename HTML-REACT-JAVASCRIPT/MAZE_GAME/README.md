# Maze Game - React Version

## Overview
A simple maze game built with React. The player navigates a randomly generated maze using arrow buttons or keyboard arrows. Tracks moves, time, and best scores.

---

## Features
- Dynamic maze generation
- Arrow button and keyboard controls
- Move counter and timer
- Best scores tracking
- Start new maze / Restart current maze
- Fully responsive layout

---

## Folder Structure

```
MyMazeGame/          ← root repo folder
│
├── package.json
├── package-lock.json
├── vite.config.js
├── README.md
├── .gitignore
│
├── public/
│   └── favicon.ico
│
└── src/
    ├── index.js
    ├── App.jsx
    │
    ├── maze/
    │   ├── MazeView.jsx
    │   ├── mazeEngine.js
    │   └── movement.js
    │
    └── styles/
        ├── index.css
        ├── Layout.css
        ├── Buttons.css
        ├── Maze.css
        └── Arrow.css
```

---

## Installation

1. Clone the repo:
```bash
git clone https://github.com/YOUR_USERNAME/MyMazeGame.git
cd MyMazeGame
```

2. Install dependencies:
```bash
npm install
```

3. Run the development server:
```bash
npm run dev
```

4. Open your browser at `http://localhost:5173` (default Vite port).

---

## Usage

- Use the **arrow buttons** or **keyboard arrows** to navigate the maze.
- Click **NEW** to generate a new maze.
- Click **RESTART** to restart the current maze.
- Move counter, timer, and best scores update automatically.

---

## Notes

- All CSS is imported via `src/styles/index.css`.
- The `MazeView.jsx` handles maze rendering, player state, and movement logic.
- The `mazeEngine.js` file handles maze generation.
- The `movement.js` file handles keyboard and arrow button navigation.

---

## Dependencies

- React
- Vite

---

## License

This project is open-source and free to use.
