package cmsc125.lab3;
import org.junit.jupiter.api.Test;
import cmsc125.lab3.models.ProcessModel;
import static org.junit.jupiter.api.Assertions.*;

class ProcessModelTest {
    @Test
    void testProcessInitialization() {
        // Requirements: Burst 1-30, Arrival 0-30, Priority 1-20
        ProcessModel p = new ProcessModel("P1", 10, 5, 1);
        
        assertEquals("P1", p.getProcessId());
        assertEquals(10, p.getBurstTime());
        assertEquals(10, p.getRemainingTime()); // Remaining should initially match burst
        assertEquals(5, p.getArrivalTime());
    }
}
