package cmsc125.lab3.services;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

import cmsc125.lab3.models.ProcessModel;

public class RoundRobinSimulator extends BaseSimulator {
    private final int quantum;
    private int quantumCounter = 0;
    private final Queue<ProcessModel> fifoQueue = new LinkedList<>();

    public RoundRobinSimulator(List<ProcessModel> processes, int quantum) {
        super(processes, null);
        this.quantum = quantum;
    }

    @Override
    public boolean executeStep() {
        if (arrivalQueue.isEmpty() && fifoQueue.isEmpty() && currentRunningProcess == null) return false;

        while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <= currentTime) {
            ProcessModel arrived = arrivalQueue.poll();
            fifoQueue.add(arrived);
        }

        if (currentRunningProcess != null) {
            if (quantumCounter >= quantum) {
                fifoQueue.add(currentRunningProcess);
                currentRunningProcess = null;
                quantumCounter = 0;
            }
        }

        if (currentRunningProcess == null && !fifoQueue.isEmpty()) {
            currentRunningProcess = fifoQueue.poll();
            quantumCounter = 0;
        }

        if (currentRunningProcess != null) {
            this.activeProcessId = currentRunningProcess.getProcessId();
            currentRunningProcess.setRemainingTime(currentRunningProcess.getRemainingTime() - 1);
            quantumCounter++;

            if (currentRunningProcess.getRemainingTime() <= 0) {
                currentRunningProcess.setCompletionTime(currentTime + 1);
                calculateStats(currentRunningProcess);
                currentRunningProcess = null;
                quantumCounter = 0;
            }
        } else this.activeProcessId = "IDLE";

        currentTime++;
        return true;
    }
}