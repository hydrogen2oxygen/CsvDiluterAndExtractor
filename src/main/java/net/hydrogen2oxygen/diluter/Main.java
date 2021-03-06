package net.hydrogen2oxygen.diluter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

public class Main {

   private static final String UTF_8 = "UTF-8";

   public static void main(String[] args) throws Exception {

      if (args.length != 6) {
         System.err.println("syntax:\n inputFile diluterColumns[commaseparated integers] uniqueFile duplicateFile templateFile extractFile");
      }

      String inputFile = args[0];
      String diluterColumns = args[1];
      String uniqueFile = args[2];
      String duplicateFile = args[3];
      String templateFile = args[4];
      String extractFile = args[5];

      Reader in = new FileReader(inputFile);
      CSVParser records = CSVFormat.EXCEL.parse(in);

      String lastRecord = null;
      Integer[] columnsForDilutionIdentifiersArray = integerArrayFromStringArray(diluterColumns.split(","));
      List<Integer> columnsForDilutionIdentifiers = Arrays.asList(columnsForDilutionIdentifiersArray);
      CSVRecord headerRecord = null;
      List<CSVRecord> uniqueRecords = new ArrayList<>();
      List<CSVRecord> duplicateRecords = new ArrayList<>();
      boolean ignoreHeaderLine = true;

      int x = 0;

      for (CSVRecord record : records) {

         x++;

         if (ignoreHeaderLine && x == 1) {
            headerRecord = record;
            continue;
         }

         if (lastRecord != null && checkForDuplicates(record, lastRecord, columnsForDilutionIdentifiers)) {
            duplicateRecords.add(record);
            continue;
         }

         System.out.println(record.toString());

         uniqueRecords.add(record);

         lastRecord = generateLastRecord(record, columnsForDilutionIdentifiers);

      }

      in.close();

      writeCsv(headerRecord, uniqueRecords, uniqueFile);
      writeCsv(headerRecord, duplicateRecords, duplicateFile);

      String template = FileUtils.readFileToString(new File(templateFile), UTF_8);
      StringBuilder extract = new StringBuilder();

      for (CSVRecord record : duplicateRecords) {

         String line = template;

         for (int i = 0; i < record.size(); i++) {
            line = line.replaceAll("#" + i + "#", record.get(i));
         }

         extract.append(line + "\n");
      }

      FileUtils.writeStringToFile(new File(extractFile), extract.toString(), UTF_8);
   }

   private static Integer[] integerArrayFromStringArray(String[] strIntegers) {

      Integer[] integers = new Integer[strIntegers.length];

      int i=0;

      for (String strInteger : strIntegers) {

         // minus one, because the user will input 1 for column 0
         integers[i] = Integer.parseInt(strInteger.trim()) - 1;

         i++;
      }

      return integers;
   }

   public static void writeCsv(CSVRecord headerRecord, List<CSVRecord> records, String outputFile)
         throws IOException {

      CSVFormat csvFileFormat = CSVFormat.EXCEL;
      FileWriter fileWriter = new FileWriter(outputFile);
      CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

      if (headerRecord != null) {
         csvFilePrinter.printRecord(headerRecord);
      }

      csvFilePrinter.printRecords(records);

      fileWriter.flush();
      fileWriter.close();
      csvFilePrinter.close();
   }

   private static boolean checkForDuplicates(CSVRecord record, String lastRecord,
         List<Integer> columnsForDilutionIdentifiers) {

      return lastRecord.equals(generateLastRecord(record, columnsForDilutionIdentifiers));
   }

   private static String generateLastRecord(CSVRecord record, List<Integer> columnsForDilutionIdentifiers) {

      StringBuilder lastRecord = new StringBuilder();

      for (int x = 0; x < record.size(); x++) {

         if (columnsForDilutionIdentifiers.contains(x)) {
            continue;
         }

         lastRecord.append(record.get(x));
         lastRecord.append(",");
      }

      return lastRecord.toString();
   }
}