package cmsc125.lab3;

import org.junit.jupiter.api.Test;

import cmsc125.lab3.models.ProcessModel;
import cmsc125.lab3.services.FCFSSimulator;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FCFSSimulatorTest {

    @Test
    void testFCFSExecutionAndStats() {
        List<ProcessModel> processes = new ArrayList<>();
        // P1 arrives at 0, Burst 3
        // P2 arrives at 1, Burst 2
        processes.add(new ProcessModel("P1", 3, 0, 1));
        processes.add(new ProcessModel("P2", 2, 1, 2));

        FCFSSimulator sim = new FCFSSimulator(processes);

        // Step 1-3: P1 should be running
        assertTrue(sim.executeStep()); // Tick 0
        assertTrue(sim.executeStep()); // Tick 1
        assertTrue(sim.executeStep()); // Tick 2
        
        // At Tick 3, P1 should be finished. 
        // Completion = 3, Turnaround = 3-0=3, Waiting = 3-3=0
        ProcessModel p1 = processes.get(0);
        assertEquals(0, p1.getRemainingTime());
        assertEquals(3, p1.getCompletionTime());
        assertEquals(3, p1.getTurnaroundTime());
        assertEquals(0, p1.getWaitingTime());

        // Step 4-5: P2 should be running
        assertTrue(sim.executeStep()); // Tick 3
        assertTrue(sim.executeStep()); // Tick 4
        
        // P2 Completion = 5, Turnaround = 5-1=4, Waiting = 4-2=2
        ProcessModel p2 = processes.get(1);
        assertEquals(5, p2.getCompletionTime());
        assertEquals(4, p2.getTurnaroundTime());
        assertEquals(2, p2.getWaitingTime());

        // Next step should return false as queue is empty
        assertFalse(sim.executeStep());
    }

    @Test
    void testIdleTime() {
        List<ProcessModel> processes = new ArrayList<>();
        // P1 arrives late at time 2
        processes.add(new ProcessModel("P1", 1, 2, 1));
        
        FCFSSimulator sim = new FCFSSimulator(processes);
        
        sim.executeStep(); // Tick 0: Idle
        assertEquals("IDLE", sim.getActiveProcessId());
        
        sim.executeStep(); // Tick 1: Idle
        assertEquals("IDLE", sim.getActiveProcessId());
        
        sim.executeStep(); // Tick 2: P1 starts
        assertEquals("P1", sim.getActiveProcessId());
    }
}
