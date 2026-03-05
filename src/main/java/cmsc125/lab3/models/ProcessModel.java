package cmsc125.lab3.models;

public class ProcessModel {

    private String processId;
	private int burstTime;
    private int arrivalTime;
    private int priority;
    
	// Calculated fields
    private int completionTime;
	private int waitingTime;
	private int turnaroundTime;
	private int remainingTime; // Vital for Preemptive algorithms & Round Robin

    public ProcessModel(String id, int burst, int arrival, int priority) {
        this.processId = id;
        this.burstTime = burst;
        this.arrivalTime = arrival;
        this.priority = priority;
        this.remainingTime = burst;
    }

    public int getWaitingTime() { return waitingTime; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public String getProcessId() { return processId; }
    public int getBurstTime() { return burstTime; }
    public int getPriority() { return priority; }
    public int getRemainingTime() { return remainingTime; }
    public int getArrivalTime() { return arrivalTime; }
    public int getCompletionTime() { return completionTime; }
	public void setCompletionTime(int t) { this.completionTime = t; }
    public void subtractRemainingTime(int t) { this.remainingTime -= t; }
    public void setRemainingTime(int t) { this.remainingTime = t; }
	public void setWaitingTime(int t) { this.waitingTime = t; }
	public void setTurnaroundTime(int t) { this.turnaroundTime = t; }
}
