package jakobo;

import com.opencsv.bean.CsvBindByName;

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

    public NumberOfChecklistsResultAccumulator findPigeonsInCity(String cityId, String cityName, int numberOfAttempts) {
        final List<String> pigeonsInCity = pigeons.getPigeonsInCity(cityId).collect(Collectors.toList());
        final NumberOfChecklistsResultAccumulator result = new NumberOfChecklistsResultAccumulator(cityId, cityName, pigeonsInCity.size(), checklists.getChecklistsInCity(cityId).count());

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

    public static class NumberOfChecklistsResultAccumulator {
        private final String cityId;
        private final String cityName;
        private final int totalPigeonsInCity;
        private final long totalChecklistsInCity;

        private final List<Integer> numberOfChecklistsRequiredToFindPigeons = new ArrayList<>();

        public NumberOfChecklistsResultAccumulator(String cityId, String cityName, int totalPigeonsInCity, long totalChecklistsInCity) {
            this.cityId = cityId;
            this.cityName = cityName;
            this.totalPigeonsInCity = totalPigeonsInCity;
            this.totalChecklistsInCity = totalChecklistsInCity;
        }

        public NumberOfChecklistsResultAccumulator addResult(int numberOfChecklistsRequired) {
            numberOfChecklistsRequiredToFindPigeons.add(numberOfChecklistsRequired);
            return this;
        }

        public NumberOfChecklistsResult result() {
            return new NumberOfChecklistsResult(
                    cityId,
                    cityName,
                    totalPigeonsInCity,
                    totalChecklistsInCity,
                    numberOfChecklistsRequiredToFindPigeons.size(),
                    numberOfChecklistsRequiredToFindPigeons.stream().mapToInt(i -> i).average().getAsDouble(),
                    numberOfChecklistsRequiredToFindPigeons.stream().mapToInt(i -> i).max().getAsInt(),
                    numberOfChecklistsRequiredToFindPigeons.stream().mapToInt(i -> i).min().getAsInt());
        }
    }

    public static class NumberOfChecklistsResult {
        @CsvBindByName(column = "city_id")
        private final String cityId;
        @CsvBindByName(column = "city_name")
        private final String cityName;
        @CsvBindByName(column = "total_pigeons_in_city")
        private final int totalPigeonsInCity;
        @CsvBindByName(column = "total_checklists_in_city")
        private final long totalChecklistsInCity;
        @CsvBindByName(column = "number_of_attempts")
        private final int numberOfRuns;
        @CsvBindByName(column = "average_number_of_checklists_required")
        private final double averageChecklistsRequired;
        @CsvBindByName(column = "max_number_of_checklists_required")
        private final int maxChecklistsRequired;
        @CsvBindByName(column = "min_number_of_checklists_required")
        private final int minChecklistsRequired;

        public NumberOfChecklistsResult(String cityId, String cityName, int totalPigeonsInCity, long totalChecklistsInCity, int numberOfRuns, double averageChecklistsRequired, int maxChecklistsRequired, int minChecklistsRequired) {
            this.cityId = cityId;
            this.cityName = cityName;
            this.totalPigeonsInCity = totalPigeonsInCity;
            this.totalChecklistsInCity = totalChecklistsInCity;
            this.numberOfRuns = numberOfRuns;
            this.averageChecklistsRequired = averageChecklistsRequired;
            this.maxChecklistsRequired = maxChecklistsRequired;
            this.minChecklistsRequired = minChecklistsRequired;
        }

        @Override
        public String toString() {
            return cityName + " (" + cityId + ")" + ":\n" +
                    "-----------------------------------------\n" +
                    "total pigeons: " + totalPigeonsInCity + "\n" +
                    "total checklists: " + totalChecklistsInCity + "\n" +
                    "runs: " + numberOfRuns + "\n" +
                    "average checklists required: " + averageChecklistsRequired + "\n" +
                    "max checklists required: " + maxChecklistsRequired + "\n"+
                    "min checklists required: " + minChecklistsRequired + "\n" +
                    "-----------------------------------------\n";
        }
    }

    public static void main(String[] args) {
        final NumberOfChecklists numberOfChecklists = new NumberOfChecklists(Pigeons.pigeons(), Checklists.checklists());
        System.out.println(numberOfChecklists.findPigeonsInCity("1786", "Manchester", 5).result());
    }
}
