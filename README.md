# Fire_Simulation
Mobile Agents fire Simulation, We created a framework in this project that allows mobile
agents—small programs—to switch between servers. Every agent has a task to do and is able
to visit another server if necessary. The agents use messages to communicate with servers
and other agents. We used sockets to connect various machines and threads to run multiple
jobs simultaneously. We learn more about how programs can cooperate over a network thanks
to this effort. It also demonstrates how to use concurrency to manage multiple concurrent
tasks. The objective is to gain a basic understanding of distributed systems. We used 
sockets to connect various machines and threads to run multiple jobs simultaneously. We
learn more about how programs can cooperate over a network thanks to this effort. It also 
demonstrates how to use concurrency to manage multiple concurrent tasks. The objective is 
to gain a basic understanding of distributed systems.

Project Structure
ForestFireSimulation/
├── src/
│   ├── firesimulation/
│   │   ├── Main.java                                  // Entry point and GUI setup
│   │   ├── MobileAgent.java                          // Agent logic with smarter movement
│   │   ├── SensorNode.java                          // Sensor logic with fire spread
│   │   ├── SimulationModel.java                    // Core simulation model
│   │   ├── SimulationPanel.java                   // GUI panel for visualization
│   │   └── LogPanel.java                         // Log and stats display
├── example_config/sample.txt                    // Default configuration file
├── firesimulation.jar                          // Output in JAR format
├── Docs/
│  ├── ObjectOrientedDesignDiagram.pdf        // Object design diagram
│
│                     
└── README.md                               // This file

src/firesimulation/: Contains all source files under a single package.
example_config/sample.txt: Sample configuration for initializing the simulation.
docs/: Optional documentation in HTML format.
firesimulation.jar: Executable JAR file.

## How to run this game?

Install JavaFX in your computer.

Open all the classes in intellij.

Run the Main.java class to play this game.

The GUI version and the console version will run at the same time.

```
Interact with the GUI:

Controls: Use "Start", "Pause", "Reset", and "Load Config" buttons to
manage the simulation.
Speed: Adjust the slider to change the simulation pace (1x to 10x).
Navigation: Zoom with the mouse wheel, pan by dragging, and hover over
nodes for tooltips.
Log Filter: Enter an Agent ID in the log panel to filter events.
Config File Format:
The simulation reads a text file (e.g., sample.txt) to initialize the network. Lines can appear in any order:

node x y: Creates a sensor at coordinates (x, y).
edge x1 y1 x2 y2: Connects sensors at (x1, y1) and (x2, y2) for communication.
station x y: Sets the base station at (x, y) where the initial agent starts. Only the first station line is used; additional ones are ignored.
fire x y: Specifies the initial fire location at (x, y). Only the last fire line is used; earlier ones are overwritten.
Example sample.txt:


Example sample.txt: 
node 0 0
node 3 0
node 5 0
node 7 0
node 8 1
node 1 1
node 2 2
node 4 1
node 6 1
node 7 2

edge 0 0 3 0
edge 1 1 0 0
edge 1 1 2 2
edge 2 2 3 0
edge 2 2 4 1
edge 3 0 5 0
edge 5 0 4 1
edge 5 0 6 1
edge 5 0 7 0
edge 4 1 6 1
edge 6 1 7 2
edge 7 2 7 0
edge 7 0 8 1
edge 8 1 7 2

station 0 0
fire 8 1

