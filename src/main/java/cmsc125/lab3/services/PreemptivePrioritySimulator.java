package cmsc125.lab3.services;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import cmsc125.lab3.models.ProcessModel;

/**
 * FCFSSimulator
 * This simulates the first come first served cpu scheduling algorithm
 */
public class PreemptivePrioritySimulator extends BaseSimulator {

    public PreemptivePrioritySimulator(List<ProcessModel> processes) {
        // We initialize the base PriorityQueue with a multi-level Comparator:
        // 1. Primary sort: Priority (Ascending, e.g., 1 > 5)
        // 2. Secondary sort (Tie-breaker): Arrival Time (FIFO)
        super(processes, Comparator
                .comparingInt(ProcessModel::getPriority)
                .thenComparingInt(ProcessModel::getArrivalTime));
    }

    @Override
    public boolean executeStep() {
        // 1. Exit condition: check if simulation is complete
        if (arrivalQueue.isEmpty() && processQueue.isEmpty() && currentRunningProcess == null) {
            return false;
        }

        boolean higherPriorityArrived = false;

        // 2. ARRIVALS: Process all arrivals for the current tick
        while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <= currentTime) {
            ProcessModel arrived = arrivalQueue.poll();
            processQueue.add(arrived);


            // CHECK: If this newcomer has a strictly better priority than what's running
            if (currentRunningProcess != null && arrived.getPriority() < currentRunningProcess.getPriority()) {
                higherPriorityArrived = true;
            }
        }

        // 3. PREEMPTION: The "Enforcer"
        // If a higher priority process arrived, move current back to queue and clear CPU
        if (higherPriorityArrived && currentRunningProcess != null) {
            processQueue.add(currentRunningProcess);
            currentRunningProcess = null;
        }

        // 4. SELECTION: Pick the best process currently in the pool
        if (currentRunningProcess == null && !processQueue.isEmpty()) {
            currentRunningProcess = processQueue.poll();
        }

        // 5. EXECUTION
        if (currentRunningProcess != null) {
            this.activeProcessId = currentRunningProcess.getProcessId();
            
            // Standard decrement of remaining time
            currentRunningProcess.setRemainingTime(currentRunningProcess.getRemainingTime() - 1);

            // Check if the process finished during this unit of work
            if (currentRunningProcess.getRemainingTime() <= 0) {
                currentRunningProcess.setCompletionTime(currentTime + 1);
                
                // Ensure base class methods like calculateStats are accessible
                calculateStats(currentRunningProcess);
                
                currentRunningProcess = null;
            }
        } else {
            this.activeProcessId = "IDLE";
        }

        // Increment global simulation clock
        currentTime++;
        return true;
    }
}
