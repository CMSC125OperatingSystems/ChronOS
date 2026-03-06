package cmsc125.lab3;

import cmsc125.lab3.models.ProcessModel;
import cmsc125.lab3.services.PreemptivePrioritySimulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PreemptivePrioritySimulatorTest {

    private List<ProcessModel> processes;
    private PreemptivePrioritySimulator simulator;

    @BeforeEach
    void setUp() {
        processes = new ArrayList<>();
    }

    @Test
    @DisplayName("Preemptive Priority: Basic Interruption Check")
    void testBasicPreemption() {
        // P1 starts at T=0 with Priority 3
        processes.add(new ProcessModel("P1", 10, 0, 3));
        // P2 arrives at T=2 with Priority 1 (Better)
        processes.add(new ProcessModel("P2", 2, 2, 1));

        simulator = new PreemptivePrioritySimulator(processes, true);

        // Run until T=2
        for (int i = 0; i < 2; i++) simulator.executeStep();
        assertEquals("P1", simulator.getActiveProcessId());

        // At T=2, P2 arrives and should preempt P1
        simulator.executeStep();
        assertEquals("P2", simulator.getActiveProcessId(), "P2 should preempt P1 at T=2");
    }

    @Test
    @DisplayName("Preemptive Priority: Tie-breaking with Arrival Time")
    void testPriorityTieBreaking() {
        // Two processes with same priority (2)
        processes.add(new ProcessModel("P1", 5, 0, 2));
        processes.add(new ProcessModel("P2", 5, 1, 2));

        simulator = new PreemptivePrioritySimulator(processes, true);

        // P1 should run first and NOT be preempted by P2 because priorities are equal
        for (int i = 0; i < 3; i++) {
            simulator.executeStep();
            assertEquals("P1", simulator.getActiveProcessId(), "P1 should keep CPU; P2 has same priority but arrived later");
        }
    }

    @Test
    @DisplayName("Preemptive Priority: Multiple Preemptions (Textbook Case)")
    void testComplexPreemption() {
        // Based on common OS lab data: P(ID, Burst, Arrival, Priority)
        processes.add(new ProcessModel("P0", 8, 0, 3));
        processes.add(new ProcessModel("P1", 4, 1, 1)); // Should preempt P0
        processes.add(new ProcessModel("P2", 2, 2, 2)); // Should stay in queue (1 < 2)

        simulator = new PreemptivePrioritySimulator(processes, true);

        // T=0: P0 starts
        simulator.executeStep();
        assertEquals("P0", simulator.getActiveProcessId());

        // T=1: P1 arrives and preempts P0
        simulator.executeStep();
        assertEquals("P1", simulator.getActiveProcessId(), "P1 (Pri 1) must preempt P0 (Pri 3)");

        // T=2: P2 arrives, but P1 is still better
        simulator.executeStep();
        assertEquals("P1", simulator.getActiveProcessId(), "P1 should continue; P2 (Pri 2) is lower priority");
    }

    @Test
    @DisplayName("Preemptive Priority: Example 2 Validation (P1-P5)")
    void testPreemptivePriorityExample2() {
        processes.add(new ProcessModel("P1", 8, 0, 4));
        processes.add(new ProcessModel("P2", 4, 3, 3));
        processes.add(new ProcessModel("P3", 5, 4, 1));
        processes.add(new ProcessModel("P4", 3, 6, 2));
        processes.add(new ProcessModel("P5", 2, 10, 2));

        // FIXED: Passing "true" to indicate Lower Number = Highest Priority
        simulator = new PreemptivePrioritySimulator(processes, true);

        while (simulator.executeStep());

        ProcessModel p1 = processes.stream().filter(p -> p.getProcessId().equals("P1")).findFirst().get();
        ProcessModel p2 = processes.stream().filter(p -> p.getProcessId().equals("P2")).findFirst().get();
        ProcessModel p3 = processes.stream().filter(p -> p.getProcessId().equals("P3")).findFirst().get();
        ProcessModel p4 = processes.stream().filter(p -> p.getProcessId().equals("P4")).findFirst().get();
        ProcessModel p5 = processes.stream().filter(p -> p.getProcessId().equals("P5")).findFirst().get();

        assertAll("Preemptive Priority Example 2 Results",
            () -> assertEquals(22, p1.getCompletionTime()),
            () -> assertEquals(17, p2.getCompletionTime()),
            () -> assertEquals(9, p3.getCompletionTime()),
            () -> assertEquals(12, p4.getCompletionTime()),
            () -> assertEquals(14, p5.getCompletionTime())
        );
    }
}
