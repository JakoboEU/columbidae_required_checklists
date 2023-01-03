package jakobo;

import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class TestAllCities {

    public static void main(String[] args) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        final Checklists checklists = Checklists.checklists();
        final NumberOfChecklists numberOfChecklists = new NumberOfChecklists(Pigeons.pigeons(), checklists);
        final int numberOfRuns = 100;

        final Writer writer = new FileWriter("number_of_checklists_required_to_find_all_cities__" + numberOfRuns + "_runs.csv");
        StatefulBeanToCsv beanToCsv = new StatefulBeanToCsvBuilder(writer).build();
        beanToCsv.write(
                checklists.getAllCities().map(
                        city -> numberOfChecklists.findPigeonsInCity(city.getCityId(), city.getCityName(), numberOfRuns).result()
                )
        );
        writer.close();
    }
}
