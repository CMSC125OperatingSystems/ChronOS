package cmsc125.lab3.services;
import java.util.*;
import cmsc125.lab3.models.ProcessModel;

public class SchedulerAlgorithms {

    // FCFS: Simplest implementation
    public List<ProcessModel> calculateFCFS(List<ProcessModel> processes) {
        processes.sort(Comparator.comparingInt(ProcessModel::getArrivalTime));
        int currentTime = 0;

        for (ProcessModel p : processes) {
            if (currentTime < p.getArrivalTime()) {
                currentTime = p.getArrivalTime();
            }
            p.setWaitingTime(currentTime - p.getArrivalTime());
            currentTime += p.getBurstTime();
            p.setTurnaroundTime(p.getWaitingTime() + p.getBurstTime());
        }
        return processes;
    }

    // Round Robin: Requires the Quantum Time input from your GUI
    public void calculateRoundRobin(List<ProcessModel> processes, int quantum) {
        Queue<ProcessModel> readyQueue = new LinkedList<>();
        int currentTime = 0;
        int completed = 0;
        int n = processes.size();

        // Sort by arrival first to populate initial queue
        processes.sort(Comparator.comparingInt(ProcessModel::getArrivalTime));
        
        // This logic would run inside a loop until all processes are finished
        // Note: For the Gantt chart, you'd record 'currentTime' at each step
    }
}
