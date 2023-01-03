package jakobo;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
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
        return pigeonsByCity.get(cityId).stream();
    }

    public Stream<String> getPigeonsOnChecklist(String checklistId) {
        return Optional.ofNullable(pigeonsByChecklist.get(checklistId)).orElse(Collections.EMPTY_LIST).stream();
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

        if (!mapToAppend.get(key).contains(value)) {
            mapToAppend.get(key).add(value);
        }
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
        pigeons.getPigeonsInCity("1786").forEach(System.out::println);
    }
}
