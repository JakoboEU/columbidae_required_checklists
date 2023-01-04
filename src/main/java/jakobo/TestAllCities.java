package jakobo;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.stream.Stream;

public class TestAllCities {

    private static void writeFile(String filename, Stream<NumberOfChecklists.NumberOfChecklistsResult> results) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        final Writer writer = new FileWriter(filename);
        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
        beanToCsv.write(results);
        writer.close();
    }

    public static void main(String[] args) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        final Checklists checklists = Checklists.checklists();
        final NumberOfChecklists numberOfChecklists = new NumberOfChecklists(Pigeons.pigeons(), checklists);
        final int numberOfRuns = 1000;

        writeFile("number_of_checklists_required_to_find_all_pigeons__" + numberOfRuns + "_runs.csv",
            checklists.getAllCities().map(
                    city -> numberOfChecklists.findPigeonsInCity(city.getCityId(), city.getCityName(), numberOfRuns, 0.0).result()
            ));

        writeFile("number_of_checklists_required_to_find_pigeons_on_min_5-perc_checklists__" + numberOfRuns + "_runs.csv",
                checklists.getAllCities().map(
                        city -> numberOfChecklists.findPigeonsInCity(city.getCityId(), city.getCityName(), numberOfRuns, 0.05).result()
                ));

        writeFile("number_of_checklists_required_to_find_pigeons_on_min_10-perc_checklists__" + numberOfRuns + "_runs.csv",
                checklists.getAllCities().map(
                        city -> numberOfChecklists.findPigeonsInCity(city.getCityId(), city.getCityName(), numberOfRuns, 0.1).result()
                ));
    }
}
