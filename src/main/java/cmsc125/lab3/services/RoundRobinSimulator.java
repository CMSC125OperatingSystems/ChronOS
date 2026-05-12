package cmsc125.lab3.services;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.*;

import cmsc125.lab3.models.ProcessModel;

/**
 * FCFSSimulator
 * This simulates the first come first served cpu scheduling algorithm
 */

public class RoundRobinSimulator extends BaseSimulator {
    private final int quantum;
    private int quantumCounter = 0;
    
    // OVERRIDE: Use a LinkedList for true FIFO behavior
    // PriorityQueue was sorting by ArrivalTime (0 < 2), causing P1 to jump ahead of P2.
    private final Queue<ProcessModel> fifoQueue = new LinkedList<>();

    public RoundRobinSimulator(List<ProcessModel> processes, int quantum) {
        super(processes, null); // We don't need the base comparator anymore
        this.quantum = quantum;
    }

    @Override
    public boolean executeStep() {
        if (arrivalQueue.isEmpty() && fifoQueue.isEmpty() && currentRunningProcess == null) return false;

        // Check Arrivals: Add all processes arriving at 'currentTime' to the Queue
        while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <= currentTime) {
            ProcessModel arrived = arrivalQueue.poll();
            fifoQueue.add(arrived);
        }

        // Check Quantum / Completion
        if (currentRunningProcess != null) {
            if (quantumCounter >= quantum) {
                fifoQueue.add(currentRunningProcess);
                currentRunningProcess = null;
                quantumCounter = 0;
            }
        }

        // Context Switch: Load next process if CPU free
        if (currentRunningProcess == null && !fifoQueue.isEmpty()) {
            currentRunningProcess = fifoQueue.poll();
            quantumCounter = 0;
        }

        // Execution
        if (currentRunningProcess != null) {
            this.activeProcessId = currentRunningProcess.getProcessId();
            currentRunningProcess.setRemainingTime(currentRunningProcess.getRemainingTime() - 1);
            quantumCounter++;

            // Check if finished
            if (currentRunningProcess.getRemainingTime() <= 0) {
                currentRunningProcess.setCompletionTime(currentTime + 1);
                calculateStats(currentRunningProcess);
                currentRunningProcess = null; // CPU free for next tick
                quantumCounter = 0;
            }
        } else this.activeProcessId = "IDLE";

        currentTime++;
        return true;
    }
}