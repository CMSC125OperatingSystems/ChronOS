
package cmsc125.lab3.services;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import cmsc125.lab3.models.ProcessModel;

/**
 * Simulator
 * this serves as the parent template for all the scheduling algorithms
 */
public abstract class BaseSimulator {
    List<ProcessModel> processes;
    PriorityQueue<ProcessModel> arrivalQueue;
    PriorityQueue<ProcessModel> processQueue;
    ProcessModel currentRunningProcess;
    int currentTime;
    int completedProcesses;
    int totalProcesses;
    String activeProcessId;


	public BaseSimulator (List<ProcessModel> startingProcesses, Comparator<ProcessModel> comparator) {
        processes = startingProcesses;
        currentTime = 0;
        completedProcesses = 0;
        totalProcesses = processes.size();
        arrivalQueue = new PriorityQueue<>(
            Comparator.comparingInt(ProcessModel::getArrivalTime)
            .thenComparingInt(p -> Integer.parseInt(p.getProcessId().substring(1)))
        );
        arrivalQueue.addAll(startingProcesses);
        if (comparator != null) {
            processQueue = new PriorityQueue<>(comparator
                .thenComparingInt(p -> Integer.parseInt(p.getProcessId().substring(1)))
            );
        }
        else {
            processQueue = new PriorityQueue<>();
        }
    }

    public boolean executeStep() {
        // 1. Exit condition: Are all processes finished?
        // We check both queues and the current running process
        if (arrivalQueue.isEmpty() && processQueue.isEmpty() && currentRunningProcess == null) {
            return false;
        }

        // 2. The Handover: Move arrived processes from Pool to Ready Queue
        // We use a while loop because multiple processes can arrive at the same time (e.g., at Tick 0)
        while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <= currentTime) {
            processQueue.add(arrivalQueue.poll());
        }

        // 3. Selection: If the CPU is free, pick the "best" process based on your algorithm's comparator
        if (currentRunningProcess == null && !processQueue.isEmpty()) {
            currentRunningProcess = processQueue.poll();
        }

        // 4. Execution Logic
        if (currentRunningProcess != null) {
            this.activeProcessId = currentRunningProcess.getProcessId();
            currentRunningProcess.setRemainingTime(currentRunningProcess.getRemainingTime() - 1);

            // Check if finished
            if (currentRunningProcess.getRemainingTime() <= 0) {
                currentRunningProcess.setCompletionTime(currentTime + 1);
                calculateStats(currentRunningProcess);
                currentRunningProcess = null; // System is now free for a new process next tick
            }
        } else {
            this.activeProcessId = "IDLE";
        }

        currentTime++;
        return true;
    }

    public void calculateStats(ProcessModel p) {
        p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime());
        p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
    }

    public int getCurrentTime() { return currentTime; }
	public void setCurrentTime(int t) { this.currentTime = t; }
	public int getCompletedProcesses() { return completedProcesses; }
	public void setCompletedProcesses(int completedProcesses) { this.completedProcesses = completedProcesses; }
	public int getTotalProcesses() { return totalProcesses; }
	public void setTotalProcesses(int totalProcesses) { this.totalProcesses = totalProcesses; }
	public String getActiveProcessId() { return activeProcessId; }
	public void setActiveProcessId(String activeProcessId) { this.activeProcessId = activeProcessId; }
}
