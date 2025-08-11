package fireSimulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a mobile agent that traverses the sensor network to monitor fire spread.
 * Agents prioritize movement to yellow (NEAR_FIRE) nodes and clone themselves when on yellow nodes.
 */

public class MobileAgent implements Runnable {
    private final int id;
    private SensorNode currentNode;
    private final SimulationModel model;
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final Random random = new Random();

    /**
     * Constructs a new mobile agent.
     * @param id The unique identifier for this agent.
     * @param startingNode The initial sensor node where the agent begins.
     * @param model The simulation model managing the network.
     */
    public MobileAgent(int id, SensorNode startingNode, SimulationModel model) {
        this.id = id;
        this.currentNode = startingNode;
        this.model = model;
    }

    /**
     * Runs the agent's main loop, performing movement or cloning based on node state.
     */
    @Override
    public void run() {
        currentNode.sendLogToBaseStation(id, currentNode.getLocation(), "Agent created");

        while (active.get() && model.isSimulationActive()) {
            if (currentNode.getState() == SensorNode.State.NEAR_FIRE) {
                createCopiesOnNeighbors();
            } else {
                randomWalk();
            }

            try {
                Thread.sleep(1000 / model.getSpeedFactor());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Performs a random walk
     * Prioritizing yellow (NEAR_FIRE) nodes over blue (NORMAL) ones.
     * Uses ordered locking to prevent deadlocks during movement.
     */
    private void randomWalk() {
        List<SensorNode> yellowNeighbors = new ArrayList<>();
        List<SensorNode> blueNeighbors = new ArrayList<>();

        for (SensorNode neighbor : currentNode.getNeighbors()) {
            if (neighbor.getState() != SensorNode.State.ON_FIRE && !neighbor.hasAgent()) {
                if (neighbor.getState() == SensorNode.State.NEAR_FIRE) {
                    yellowNeighbors.add(neighbor);
                } else if (neighbor.getState() == SensorNode.State.NORMAL) {
                    blueNeighbors.add(neighbor);
                }
            }
        }

        SensorNode nextNode = null;
        if (!yellowNeighbors.isEmpty()) {
            nextNode = yellowNeighbors.get(random.nextInt(yellowNeighbors.size()));
            System.out.println("Agent " + id + " prioritizing yellow node at " + nextNode.getLocation());
        } else if (!blueNeighbors.isEmpty()) {
            nextNode = blueNeighbors.get(random.nextInt(blueNeighbors.size()));
            System.out.println("Agent " + id + " moving to blue node at " + nextNode.getLocation());
        }

        if (nextNode != null) {
            SensorNode lockFirst = currentNode;
            SensorNode lockSecond = nextNode;

            if (System.identityHashCode(currentNode) > System.identityHashCode(nextNode)) {
                lockFirst = nextNode;
                lockSecond = currentNode;
            }

            boolean firstLocked = false;
            boolean secondLocked = false;
            try {
                firstLocked = lockFirst.getAgentLock().tryLock();
                if (firstLocked) {
                    secondLocked = lockSecond.getAgentLock().tryLock();
                }

                if (firstLocked && secondLocked) {
                    if (nextNode.getState() != SensorNode.State.ON_FIRE &&
                            nextNode.getCurrentAgentUnsafe() == null &&
                            currentNode.getCurrentAgentUnsafe() == this) {
                        nextNode.setCurrentAgentUnsafe(this);
                        currentNode.setCurrentAgentUnsafe(null);
                        currentNode = nextNode;
                        System.out.println("Agent " + id + " successfully moved to " + currentNode.getLocation());
                    } else {
                        System.out.println("Agent " + id + " move to " + nextNode.getLocation() + " aborted (conditions changed).");
                    }
                } else {
                    System.out.println("Agent " + id + " failed to acquire locks for move to " + nextNode.getLocation());
                }
            } finally {

                if (secondLocked) lockSecond.getAgentLock().unlock();
                if (firstLocked) lockFirst.getAgentLock().unlock();
            }
        } else {
            System.out.println("Agent " + id + " at " + currentNode.getLocation() + " has no available neighbors to move to.");
        }
    }

    /**
     * Creates copies of this agent on unoccupied neighboring nodes when on a yellow node.
     */
    private void createCopiesOnNeighbors() {
        for (SensorNode neighbor : currentNode.getNeighbors()) {
            if (neighbor.getState() == SensorNode.State.ON_FIRE || neighbor.hasAgent()) {
                continue;
            }

            MobileAgent newAgent = new MobileAgent(model.getNextAgentId(), neighbor, model);
            if (neighbor.setAgent(newAgent)) {
                String reason = "Cloned from Agent " + id + " (near fire)";
                neighbor.sendLogToBaseStation(newAgent.getId(), neighbor.getLocation(), reason);
                model.submitAgentTask(newAgent);
            }
        }
    }
    /**
     * Destroys this agent, stopping its execution.
     */
    public void destroy() {
        active.set(false);
    }
    /**
     * Gets the unique identifier of this agent.
     * @return The agent's ID.
     */
    public int getId() {
        return id;
    }
}