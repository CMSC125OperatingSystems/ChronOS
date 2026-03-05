package cmsc125.lab3.services;
import java.util.List;

import cmsc125.lab3.models.ProcessModel;

/**
 * Simulator
 * this serves as the parent template for all the scheduling algorithms
 */
public abstract class Simulator {
    List<ProcessModel> processes;
    int currentTime;
    int completedProcesses;
    int totalProcesses;
    String activeProcessId;


	public Simulator (List<ProcessModel> startingProcesses) {
        processes = startingProcesses;
        currentTime = 0;
        completedProcesses = 0;
        totalProcesses = processes.size();
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
