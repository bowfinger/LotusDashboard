package lotusbidashboard.data.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lotusbidashboard.Sales;

/**
 *
 * @author Jamie Deville & John Harvey
 *
 * Class to support importing and exporting of Sales objects from Observable
 * List to CSV/XLS.
 *
 * Uses reflection to generate and test for headers
 */
public class ImportExportParser {

    private String delimiter = ",";
    private FileWriter writer;
    private String header = "";

    public ImportExportParser() {
        //get fields from object
        Field[] fields = Sales.class.getDeclaredFields();

        List<String> headings = new ArrayList<>();
        for (Field field : fields) {
            if (!field.getName().contains("_") && field.getName() != null) {
                headings.add(field.getName());
            }
        }
        setHeader(String.join(delimiter, headings));
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getHeader() {
        return header;
    }

    private void setHeader(String header) {
        this.header = header;
    }

    public void toCsv(File file, ObservableList<Sales> salesList) throws IOException {
        writer = new FileWriter(file);

        writer.append(header);
        writer.append("\n");

        for (Sales sale : salesList) {
            writer.append(Integer.toString(sale.getYear()));
            writer.append(getDelimiter());
            writer.append(Integer.toString(sale.getQTR()));
            writer.append(getDelimiter());
            writer.append(sale.getRegion());
            writer.append(getDelimiter());
            writer.append(sale.getVehicle());
            writer.append(getDelimiter());
            writer.append(Integer.toString(sale.getQuantity()));
            writer.append("\n");
        }
        writer.flush();
        writer.close();
    }

    public List<Sales> fromCsv(File file) throws IOException {
        RandomAccessFile raf = null;
        String line;

        if (Files.probeContentType(file.toPath()).equalsIgnoreCase("xls")) {
            setDelimiter("\t");
        }

        ObservableList<Sales> list = FXCollections.observableArrayList();
        try {
            //random access file with read-only permissions
            raf = new RandomAccessFile(file, "r");

            //check for header, if not present reset reader
            String headerRead = raf.readLine();
            if (!headerRead.equalsIgnoreCase(getHeader())
                    || !containsAny(headerRead, getHeader().split(delimiter))) {
                raf.seek(0);
            }

            while ((line = raf.readLine()) != null) {
                String[] log = line.split(getDelimiter());
                Sales sale = new Sales();
                sale.setYear(Integer.parseInt(log[0]));
                sale.setQTR(Integer.parseInt(log[1]));
                sale.setRegion(log[2]);
                sale.setVehicle(log[3]);
                sale.setQuantity(Integer.parseInt(log[4]));
                list.add(sale);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Fail: " + e);
        } catch (IOException e) {
            System.out.println("Fail: " + e);
        } finally {
            if (raf != null) {
                raf.close();
            }
        }

        System.out.println("Successfully Imported " + list.size() + " Items");
        return list;
    }

    //utility method to check if first row is header
    public static boolean containsAny(String str, String[] words) {
        boolean bResult = false; 
        List<String> list = Arrays.asList(words);
        for (String word : list) {
            boolean bFound = str.contains(word);
            if (bFound) {
                bResult = bFound;
                break;
            }
        }
        return bResult;
    }
}
