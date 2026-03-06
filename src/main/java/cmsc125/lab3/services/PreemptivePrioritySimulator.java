package cmsc125.lab3.services;

import cmsc125.lab3.models.ProcessModel;
import java.util.Comparator;
import java.util.List;

public class PreemptivePrioritySimulator extends BaseSimulator {
    private final boolean isLowerBetter;

    public PreemptivePrioritySimulator(List<ProcessModel> processes, boolean isLowerBetter) {
        super(processes, (p1, p2) -> {
            int pCompare = isLowerBetter
                ? Integer.compare(p1.getPriority(), p2.getPriority())
                : Integer.compare(p2.getPriority(), p1.getPriority());
            if (pCompare != 0) return pCompare;
            return Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
        });
        this.isLowerBetter = isLowerBetter;
    }

    @Override
    public boolean executeStep() {
        if (arrivalQueue.isEmpty() && processQueue.isEmpty() && currentRunningProcess == null) return false;

        boolean higherPriorityArrived = false;

        while (!arrivalQueue.isEmpty() && arrivalQueue.peek().getArrivalTime() <= currentTime) {
            ProcessModel arrived = arrivalQueue.poll();
            processQueue.add(arrived);

            if (currentRunningProcess != null) {
                boolean isBetter = isLowerBetter
                    ? arrived.getPriority() < currentRunningProcess.getPriority()
                    : arrived.getPriority() > currentRunningProcess.getPriority();

                if (isBetter) higherPriorityArrived = true;
            }
        }

        if (higherPriorityArrived && currentRunningProcess != null) {
            processQueue.add(currentRunningProcess);
            currentRunningProcess = null;
        }

        if (currentRunningProcess == null && !processQueue.isEmpty()) {
            currentRunningProcess = processQueue.poll();
        }

        if (currentRunningProcess != null) {
            this.activeProcessId = currentRunningProcess.getProcessId();
            currentRunningProcess.setRemainingTime(currentRunningProcess.getRemainingTime() - 1);

            if (currentRunningProcess.getRemainingTime() <= 0) {
                currentRunningProcess.setCompletionTime(currentTime + 1);
                calculateStats(currentRunningProcess);
                currentRunningProcess = null;
            }
        } else {
            this.activeProcessId = "IDLE";
        }

        currentTime++;
        return true;
    }
}