package cmsc125.lab3.services;

import cmsc125.lab3.models.ProcessModel;
import java.util.List;

public class PrioritySimulator extends BaseSimulator {

    // Non-Preemptive variation with priority configuration flag
    public PrioritySimulator(List<ProcessModel> startingProcesses, boolean isLowerBetter) {
        super(startingProcesses, (p1, p2) -> {
            int priorityCompare = isLowerBetter
                ? Integer.compare(p1.getPriority(), p2.getPriority())
                : Integer.compare(p2.getPriority(), p1.getPriority());

            if (priorityCompare != 0) return priorityCompare;

            int arrivalCompare = Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
            if (arrivalCompare != 0) return arrivalCompare;

            return p1.getProcessId().compareTo(p2.getProcessId());
        });
    }
}