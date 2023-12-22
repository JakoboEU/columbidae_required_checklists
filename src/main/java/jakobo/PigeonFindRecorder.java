package jakobo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PigeonFindRecorder {

    private final Map<String,PigeonRecord> pigeonsToFind;

    private AtomicInteger checklistsViewed = new AtomicInteger();

    PigeonFindRecorder(Set<String> pigeonsToFind, double requiredPercentageOfChecklistsToAppear) {
        this.pigeonsToFind = pigeonsToFind.stream().collect(Collectors.toMap(name ->  name, name -> new PigeonRecord(requiredPercentageOfChecklistsToAppear)));
    }

    void recordPigeons(List<String> pigeonsFound) {
        int currentChecklistsViewed = checklistsViewed.incrementAndGet();
        pigeonsFound.stream()
                .filter(name -> pigeonsToFind.containsKey(name))
                .forEach(name -> pigeonsToFind.get(name).markFound(currentChecklistsViewed));
    }

    boolean allAppearOnAtLeastXPercentOfChecklists() {
        return this.pigeonsToFind
                .values().stream()
                .allMatch(record -> record.appearsOnAtLeastXPercentOfChecklists());
    }

    private static class PigeonRecord {
        private final double requiredPercentageOfChecklistsToAppear;
        private int numberOfTimesFound;
        private double percentageOfChecklists;

        public PigeonRecord(double requiredPercentageOfChecklistsToAppear) {
            this.requiredPercentageOfChecklistsToAppear = requiredPercentageOfChecklistsToAppear;
        }

        void markFound(int totalChecklistsViewed) {
            numberOfTimesFound++;
            percentageOfChecklists = (double) numberOfTimesFound / (double) totalChecklistsViewed;
        }

        boolean appearsOnAtLeastXPercentOfChecklists() {
            return percentageOfChecklists >= requiredPercentageOfChecklistsToAppear;
        }
    }
}
