package jakobo;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

public class Pigeons {

    private final Map<String, List<String>> pigeonsByChecklist;
    private final Map<String,List<String>> pigeonsByCity;

    private Pigeons(Stream<PigeonRow> pigeonData) {
        Map<String, List<String>> pigeonsByChecklist = new HashMap<>();
        Map<String,List<String>> pigeonsByCity = new HashMap<>();

        pigeonData.forEach(pigeonRow -> {
            append(pigeonsByCity, pigeonRow.cityId, pigeonRow.name);
            append(pigeonsByChecklist, pigeonRow.checklistId, pigeonRow.name);
        });

        this.pigeonsByChecklist = Collections.unmodifiableMap(pigeonsByChecklist);
        this.pigeonsByCity = Collections.unmodifiableMap(pigeonsByCity);
    }

    public Stream<String> getPigeonsInCity(String cityId) {
        return pigeonsByCity.get(cityId).stream().distinct();
    }

    public Map<String,Long> getPigeonsInCityChecklistOccurrence(String cityId) {
        return pigeonsByCity.get(cityId).stream().collect(Collectors.groupingByConcurrent(name -> name, Collectors.counting()));
    }

    public Stream<String> getPigeonsInCityOnAtLeastXChecklists(String cityId, int minimumNumberOfChecklists) {
        return getPigeonsInCityChecklistOccurrence(cityId)
                .entrySet()
                .stream()
                .filter(e -> e.getValue() >= minimumNumberOfChecklists)
                .map(e -> e.getKey());
    }

    public Stream<String> getPigeonsOnChecklist(String checklistId) {
        return Optional.ofNullable(pigeonsByChecklist.get(checklistId)).orElse(Collections.EMPTY_LIST).stream().distinct();
    }

    public static Pigeons pigeons() {
        return new Pigeons(
                concat(concat(loadFromFile(1), loadFromFile(2)), loadFromFile(3)));
    }

    private static Stream<PigeonRow> loadFromFile(final int part) {
        final Reader fileInput = new InputStreamReader(Pigeons.class.getResourceAsStream("/bigquery_export__pigeons_on_checklists__part" + part + ".csv"));
        return new CsvToBeanBuilder<PigeonRow>(fileInput)
                                .withType(PigeonRow.class)
                                .build()
                                .parse()
                                .stream();
    }

    private static void append(Map<String,List<String>> mapToAppend, String key, String value) {
        if (!mapToAppend.containsKey(key)) {
            mapToAppend.put(key, new ArrayList<>());
        }

        mapToAppend.get(key).add(value);
    }

    public static class PigeonRow {
        @CsvBindByName(column = "species_name")
        private String name;
        @CsvBindByName(column = "checklist_id")
        private String checklistId;
        @CsvBindByName(column = "city_id")
        private String cityId;
    }

    public static void main(String[] args) {
        final Pigeons pigeons = Pigeons.pigeons();

        System.out.println("Manchester");
        System.out.println("----------------------");
        pigeons.getPigeonsInCity("1786").forEach(System.out::println);

        System.out.println("----------------------");
        pigeons.getPigeonsInCityChecklistOccurrence("1786").entrySet().forEach(entry -> System.out.println(entry.getKey() + " on " + entry.getValue() + " checklists."));

        System.out.println("----------------------");
        pigeons.getPigeonsInCityOnAtLeastXChecklists("1786", 1000).forEach(System.out::println);
    }
}
