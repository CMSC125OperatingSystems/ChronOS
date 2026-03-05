package cmsc125.lab3.services;

import java.util.Random;

import cmsc125.lab3.models.ProcessModel;

import java.util.ArrayList;
import java.util.List;

public class GenerateRandomProcesses {
    private static final Random rand = new Random();

    public static List<ProcessModel> generateRandom(int count) {
        List<ProcessModel> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(new ProcessModel(
                "P" + i,
                rand.nextInt(30) + 1, // Burst 1-30
                rand.nextInt(31),     // Arrival 0-30
                rand.nextInt(20) + 1  // Priority 1-20
            ));
        }
        return list;
    }
}
