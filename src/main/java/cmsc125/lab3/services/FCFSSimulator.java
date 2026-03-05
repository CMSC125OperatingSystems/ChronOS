package cmsc125.lab3.services;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import cmsc125.lab3.models.ProcessModel;

/**
 * FCFSSimulator
 * This simulates the first come first served cpu scheduling algorithm
 */
public class FCFSSimulator extends Simulator {
    PriorityQueue<ProcessModel> processQueue;

    public FCFSSimulator(List<ProcessModel> startingProcesses) {
        super(startingProcesses);
        // Orders processes by who arrived first
        processQueue = new PriorityQueue<>(
            Comparator.comparingInt(ProcessModel::getArrivalTime)
        );
        processQueue.addAll(startingProcesses);
    }

    public boolean executeStep() {
        // Stop if no processes are left
        if (processQueue.isEmpty()) {
            return false; 
        }

        ProcessModel currentProcess = processQueue.peek();

        // Check if the process has actually arrived yet
        if (currentProcess.getArrivalTime() <= currentTime) {
            // Execute 1 unit of work
            currentProcess.setRemainingTime(currentProcess.getRemainingTime() - 1);
            
            // Record the current process ID so the GUI can draw it
            this.activeProcessId = currentProcess.getProcessId(); 

            // Check if finished
            if (currentProcess.getRemainingTime() <= 0) {
                processQueue.poll(); // Remove from queue
                currentProcess.setCompletionTime(currentTime + 1); // +1 because tick finishes at end of unit
                calculateStats(currentProcess);
            }
        } else {
            // CPU is IDLE because the next process hasn't arrived
            this.activeProcessId = "IDLE";
        }

        currentTime++;
        return true;
    }

    private void calculateStats(ProcessModel p) {
        p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime());
        p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
    }
}
