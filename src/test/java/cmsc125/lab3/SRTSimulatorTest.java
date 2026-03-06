package cmsc125.lab3;

import cmsc125.lab3.models.ProcessModel;
import cmsc125.lab3.services.SRTSimulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SRTSimulatorTest { // FIXED CLASS NAME

    private List<ProcessModel> processes;
    private SRTSimulator simulator;

    @BeforeEach
    void setUp() {
        processes = new ArrayList<>();
    }

    @Test
    @DisplayName("SRT: Verification of Immediate Preemption")
    void testImmediatePreemption() {
        // P1 starts at T=0 with Burst 10.
        processes.add(new ProcessModel("P1", 10, 0, 0)); 
        // P2 arrives at T=1 with Burst 2. 
        // P1's remaining time is 9. Since 2 < 9, P2 MUST preempt.
        processes.add(new ProcessModel("P2", 2, 1, 0)); 
        
        simulator = new SRTSimulator(processes);

        // T=0: P1 starts
        simulator.executeStep();
        assertEquals("P1", simulator.getActiveProcessId());

        // T=1: P2 arrives and preempts P1
        simulator.executeStep();
        assertEquals("P2", simulator.getActiveProcessId(), "SRT MUST preempt P1 for P2 at T=1");
    }

    @Test
    @DisplayName("SRT: Equal Remaining Time (No Preemption)")
    void testEqualRemainingTime() {
        // P1 has 5ms remaining at T=2. P2 arrives at T=2 with 5ms burst.
        // Standard SRT should NOT preempt if the newcomer is not STRICTLY shorter.
        processes.add(new ProcessModel("P1", 7, 0, 0)); 
        processes.add(new ProcessModel("P2", 5, 2, 0)); 
        
        simulator = new SRTSimulator(processes);

        // Run to T=2
        simulator.executeStep(); // T=0
        simulator.executeStep(); // T=1
        assertEquals("P1", simulator.getActiveProcessId());

        // At T=2, P1 has 5 left, P2 arrives with 5.
        simulator.executeStep();
        assertEquals("P1", simulator.getActiveProcessId(), "Should not preempt if remaining times are equal");
    }

    @Test
    @DisplayName("SRT: Late Arrival Preemption")
    void testLateArrivalPreemption() {
        // P1 is almost finished when a tiny job arrives.
        processes.add(new ProcessModel("P1", 10, 0, 0)); 
        processes.add(new ProcessModel("P2", 1, 8, 0)); 
        
        simulator = new SRTSimulator(processes);

        // Run until T=8. P1 has 2ms remaining.
        for (int i = 0; i < 8; i++) simulator.executeStep();
        assertEquals("P1", simulator.getActiveProcessId());

        // At T=8, P2 (1ms) is shorter than P1 (2ms).
        simulator.executeStep();
        assertEquals("P2", simulator.getActiveProcessId(), "P2 should preempt P1 even near the end of P1's burst");
    }

    @Test
    @DisplayName("SRT: Simultaneous Arrivals at T=0")
    void testSimultaneousArrivals() {
        // Multiple processes arrive at the same time. Should pick the shortest immediately.
        processes.add(new ProcessModel("P1", 10, 0, 0)); 
        processes.add(new ProcessModel("P2", 2, 0, 0)); 
        processes.add(new ProcessModel("P3", 5, 0, 0)); 
        
        simulator = new SRTSimulator(processes);

        // T=0: Should pick P2 (shortest)
        simulator.executeStep();
        assertEquals("P2", simulator.getActiveProcessId(), "Should pick P2 as the shortest initial arrival");
        
        // After P2 finishes (T=2), should pick P3 (5ms) over P1 (10ms)
        simulator.executeStep(); // T=1 (P2 running)
        simulator.executeStep(); // T=2 (P2 finishes, P3 selected)
        assertEquals("P3", simulator.getActiveProcessId());
    }

    @Test
    @DisplayName("SRT: Textbook Example 2 Validation (P0-P3)")
    void testSRTTextbookValidation() {
        processes.add(new ProcessModel("P0", 7, 0, 0));
        processes.add(new ProcessModel("P1", 4, 2, 0));
        processes.add(new ProcessModel("P2", 1, 4, 0));
        processes.add(new ProcessModel("P3", 4, 5, 0));

        simulator = new SRTSimulator(processes);
        while (simulator.executeStep());

        ProcessModel p0 = processes.get(0);
        ProcessModel p1 = processes.get(1);
        ProcessModel p2 = processes.get(2);
        ProcessModel p3 = processes.get(3);

        assertAll("SRT Textbook Results",
            () -> assertEquals(16, p0.getCompletionTime(), "P0 Completion Time"),
            () -> assertEquals(7, p1.getCompletionTime(), "P1 Completion Time"),
            () -> assertEquals(5, p2.getCompletionTime(), "P2 Completion Time"),
            () -> assertEquals(11, p3.getCompletionTime(), "P3 Completion Time")
        );
    }
}
