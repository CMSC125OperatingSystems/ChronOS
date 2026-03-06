package cmsc125.lab3.services;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import cmsc125.lab3.models.ProcessModel;

/**
 * FCFSSimulator
 * This simulates the first come first served cpu scheduling algorithm
 */
public class FCFSSimulator extends BaseSimulator{
    public FCFSSimulator(List<ProcessModel> startingProcesses) {
        super(startingProcesses, 
            Comparator.comparingInt(ProcessModel::getArrivalTime));
        // Orders processes by who arrived first
    }

}
