package jakobo;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class NumberOfChecklists {
    private final Pigeons pigeons;
    private final Checklists checklists;

    private static Collector<?, ?, ?> SHUFFLED_LIST = Collectors.collectingAndThen(
            Collectors.toCollection(ArrayList::new),
            list -> {
                Collections.shuffle(list);
                return list;
            }
    );

    public NumberOfChecklists(Pigeons pigeons, Checklists checklists) {
        this.pigeons = pigeons;
        this.checklists = checklists;
    }

    public NumberOfChecklistsResult findPigeonsInCity(String cityId, String cityName, int numberOfAttempts) {
        final List<String> pigeonsInCity = pigeons.getPigeonsInCity(cityId).collect(Collectors.toList());
        final NumberOfChecklistsResult result = new NumberOfChecklistsResult(cityId, cityName, pigeonsInCity.size());

        for (int i = 0; i < numberOfAttempts; i++) {
            result.addResult(findPigeons(pigeonsInCity, checklists.getChecklistsInCity(cityId).collect(shuffledList())));
        }

        return result;
    }

    private int findPigeons(List<String> pigeonsToFind, List<String> randomOrderedChecklists) {
        int checklistCount = 0;
        final List<String> remainingPigeons = new ArrayList<>(pigeonsToFind);
        final ListIterator<String> randomChecklists = randomOrderedChecklists.listIterator();

        while(!remainingPigeons.isEmpty()) {
            final String nextChecklist = randomChecklists.next();
            remainingPigeons.removeAll(pigeons.getPigeonsOnChecklist(nextChecklist).collect(Collectors.toList()));
            checklistCount++;
        }

        return checklistCount;
    }

    private static <T> Collector<T, ?, List<T>> shuffledList() {
        return (Collector<T, ?, List<T>>) SHUFFLED_LIST;
    }

    public static class NumberOfChecklistsResult {
        private final String cityId;
        private final String cityName;
        private final int totalPigeonsInCity;

        private final List<Integer> numberOfChecklistsRequiredToFindPigeons = new ArrayList<>();

        public NumberOfChecklistsResult(String cityId, String cityName, int totalPigeonsInCity) {
            this.cityId = cityId;
            this.cityName = cityName;
            this.totalPigeonsInCity = totalPigeonsInCity;
        }

        public NumberOfChecklistsResult addResult(int numberOfChecklistsRequired) {
            numberOfChecklistsRequiredToFindPigeons.add(numberOfChecklistsRequired);
            return this;
        }

        @Override
        public String toString() {
            return cityName + " (" + cityId + ")" + ":\n" +
                    "-----------------------------------------\n" +
                    "total pigeons: " + totalPigeonsInCity + "\n" +
                    "runs: " + numberOfChecklistsRequiredToFindPigeons.size() + "\n" +
                    "average checklists required: " + numberOfChecklistsRequiredToFindPigeons.stream().mapToInt(i -> i).average().getAsDouble() + "\n" +
                    "max checklists required: " + numberOfChecklistsRequiredToFindPigeons.stream().mapToInt(i -> i).max().getAsInt() + "\n"+
                    "min checklists required: " + numberOfChecklistsRequiredToFindPigeons.stream().mapToInt(i -> i).min().getAsInt() + "\n" +
                    "-----------------------------------------\n";
        }
    }

    public static void main(String[] args) {
        final NumberOfChecklists numberOfChecklists = new NumberOfChecklists(Pigeons.pigeons(), Checklists.checklists());
        System.out.println(numberOfChecklists.findPigeonsInCity("1786", "Manchester", 5));
    }
}
