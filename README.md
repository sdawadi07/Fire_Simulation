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

# Project Structure

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

## Testing with Other Configurations:

By default, the simulation loads the configuration from example_config/sample.txt as specified
in the Main.java file. If you want to test the simulation using a different configuration file,
you will need to manually update the file path in the Main.java class. The current 
implementation does not support command-line arguments or file pickers. To change the file,
locate the section in Main.java where the configuration is loaded and replace the path with
your custom file path. This allows for testing with various sensor network layouts and fire 
scenarios.

## Features:

Sensor Network: Modeled as a planar graph with sensors (SensorNode) as concurrent threads:

States: Blue (NORMAL), Yellow (NEAR_FIRE), Red (ON_FIRE).
Red sensors send a one-time message to neighbors and cannot host agents.

Fire Spread: Begins at the last config-specified fire location, spreading to neighbors after
3 seconds (adjusted by speed factor) with a 30% probability.

Mobile Agents: Independent threads (MobileAgent) that:
Start at the base station.
Perform a random walk, prioritizing yellow nodes over blue ones for smarter monitoring.
Clone on yellow nodes to unoccupied blue or yellow neighbors.
Die when their sensor turns red.
Limited to one agent per sensor.

Base Station: Logs all agent creations and destructions with unique IDs, locations, and 
reasons, displayed in the GUI.

## GUI:

Simulation Panel: Visualizes the network with edges, node states (blue, yellow, red), agents
(white "A"), and base station (black "B").

Log Panel: Shows real-time agent event logs with filtering by Agent ID, plus stats: Total Agents,
Active Agents, Burned 
Sensors, and Active Areas.
Tooltips: Hover over nodes to see location, state, and agent ID (if present).
Controls: Start, pause, reset, load config, and speed adjustment.
Design Choices
Concurrency:

Used ExecutorService for managing sensor and agent threads, ensuring efficient resource use.
Employed ReentrantLock for sensor state and agent movement, with ordered locking in MobileAgent.
randomWalk() (based on 
object hash codes) to prevent deadlocks.
Avoided global resources; agent IDs are generated via a synchronized nextAgentId in SimulationModel.
Single Fire Start:

Adhered to the assignment baseline by supporting a single fire start, using the last fire line in the config file for 
simplicity and clarity.
Smarter Movement:

Agents prioritize yellow (NEAR_FIRE) nodes over blue (NORMAL) ones in randomWalk(), enhancing fire monitoring efficiency
beyond the basic random walk requirement.
GUI Enhancements:

Implemented hover tooltips in SimulationPanel for node details, improving usability over click-based dialogs.
Centralized stats (Active Agents, Burned Sensors) in LogPanel with Total Agents and Active Areas, updated every 1 second
for clear monitoring.
Fire Spread Logic:

Yellow nodes transition to red after 3 seconds near a burning neighbor with a 30% chance, reset each 
cycle if not 
ignited, balancing observability and realism.
Known Limitations
Single Fire Start: Only the last fire line is used; multiple fire starts are not supported, aligning
with the assignment’s
minimum requirement.
Single Base Station: Only the first station line is recognized; extras are ignored to simplify
initialization.
Stat Update Frequency: Stats in LogPanel update every 1 second, slower than the 50ms GUI refresh, which may lag slightly
behind real-time events.
Agent Destruction: Logged accurately, but GUI updates may not reflect all destructions instantly due to timing differences.
No Tests: Unit tests are not included in this submission.
Output Description

GUI Window:
Left: Network graph with colored nodes (blue, yellow, red), edges, agents (white "A"), and base station (black "B").
Right: Log panel with a table of events (Time, Agent ID, Location, Reason) and filter controls, plus a stats section 
(Total Agents, Active Agents, Burned Sensors, Active Areas).

Bottom: Control panel with buttons (Start, Pause, Reset, Load Config) and speed slider.

Console: Debugging output for agent movements, fire spread, and simulation state changes