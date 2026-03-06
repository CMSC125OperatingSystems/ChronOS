package cmsc125.lab3.services;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import cmsc125.lab3.models.ProcessModel;
import cmsc125.lab3.models.ProcessModel;
import java.util.Comparator;
import java.util.List;

/**
 * FCFSSimulator
 * This simulates the first come first served cpu scheduling algorithm
 */


public class SRTSimulator extends BaseSimulator {

    public SRTSimulator(List<ProcessModel> processes) {
        // Comparator: 
        // 1. Primary: Shortest Remaining Time
        // 2. Secondary: Arrival Time (Tie-breaker)
        super(processes, Comparator
            .comparingInt(ProcessModel::getBurstTime)
            .thenComparingInt(ProcessModel::getArrivalTime));
    }

    @Override
    public boolean executeStep() {
        // 1. Exit Condition: Simulation ends when all pools are empty
        if (arrivalQueue.isEmpty() && processQueue.isEmpty() && currentRunningProcess == null) {
            return false;
        }

        boolean shorterJobArrived = false;

        // 2. Arrivals: Move processes from arrivalQueue to processQueue
        while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <= currentTime) {
            ProcessModel arrived = arrivalQueue.poll();
            processQueue.add(arrived);


            // PREEMPTION CHECK: Is newcomer strictly shorter than current process's remaining time?
            if (currentRunningProcess != null && arrived.getRemainingTime() < currentRunningProcess.getRemainingTime()) {
                shorterJobArrived = true;
            }
        }

        // 3. Preemption: Force a context switch if a better candidate arrived
        if (shorterJobArrived && currentRunningProcess != null) {
            processQueue.add(currentRunningProcess);
            currentRunningProcess = null;
        }

        // 4. Selection: Pick the process with the least remaining time
        if (currentRunningProcess == null && !processQueue.isEmpty()) {
            currentRunningProcess = processQueue.poll();
        }

        // 5. Execution: Process one unit of time
        if (currentRunningProcess != null) {
            this.activeProcessId = currentRunningProcess.getProcessId();

            // Decrement remaining time
            currentRunningProcess.setRemainingTime(currentRunningProcess.getRemainingTime() - 1);

            // Check if finished
            if (currentRunningProcess.getRemainingTime() <= 0) {
                currentRunningProcess.setCompletionTime(currentTime + 1);
                calculateStats(currentRunningProcess); // Triggers TAT and WT updates
                currentRunningProcess = null;
            }
        } else {
            this.activeProcessId = "IDLE";
        }

        // 6. Clock increment
        currentTime++;
        return true;
    }
}
