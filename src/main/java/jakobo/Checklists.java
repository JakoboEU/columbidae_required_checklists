package jakobo;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

public class Checklists {

    private final Map<CityId, List<ChecklistRow>> cityToChecklists;

    private Checklists(Stream<ChecklistRow> checklistData) {
        this.cityToChecklists = checklistData.collect(Collectors.groupingBy(row -> new CityId(row.cityId, row.cityName)));
    }

    public static Checklists checklists() {
        return new Checklists(concat(loadFromFile(1), loadFromFile(2)));
    }

    public Stream<String> getChecklistsInCity(String cityId) {
        return this.cityToChecklists.get(new CityId(cityId)).stream().map(ChecklistRow::getChecklistId);
    }

    public Stream<CityId> getAllCities() {
        return this.cityToChecklists.keySet().stream();
    }

    private static Stream<ChecklistRow> loadFromFile(final int part) {
        final Reader fileInput = new InputStreamReader(Pigeons.class.getResourceAsStream("/bigquery_export__city_checklists__part" + part + ".csv"));
        return new CsvToBeanBuilder<ChecklistRow>(fileInput)
                .withType(ChecklistRow.class)
                .build()
                .parse()
                .stream();
    }

    public static class CityId {
        private final String cityId;
        private final String cityName;

        public CityId(String cityId, String cityName) {
            this.cityId = cityId;
            this.cityName = cityName;
        }

        public CityId(String cityId) {
            this(cityId, null);
        }

        public String getCityId() {
            return cityId;
        }

        public String getCityName() {
            return cityName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final CityId cityId1 = (CityId) o;
            return Objects.equals(cityId, cityId1.cityId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cityId);
        }
    }

    public static class ChecklistRow {
        @CsvBindByName(column = "city_id")
        private String cityId;
        @CsvBindByName(column = "city_name")
        private String cityName;
        @CsvBindByName(column = "checklist_id")
        private String checklistId;

        public String getChecklistId() {
            return checklistId;
        }
    }

    public static void main(String[] args) {
        final Checklists checklists = Checklists.checklists();
        checklists.getChecklistsInCity("1786").forEach(System.out::println);
    }
}
