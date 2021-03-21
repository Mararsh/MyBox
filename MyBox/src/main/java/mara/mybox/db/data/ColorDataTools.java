package mara.mybox.db.data;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableColorData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-12-13
 * @License Apache License Version 2.0
 */
public class ColorDataTools {

    public static List<ColorData> predefined(String type) {
        switch (type) {
            case "mybox":
                String lang = AppVariables.isChinese() ? "zh" : "en";
                return readCSV(FxmlControl.getInternalFile("/data/db/ColorsMyBox_" + lang + ".csv", "data", "ColorsMyBox_" + lang + ".csv", false));
            case "chinese":
                return readCSV(FxmlControl.getInternalFile("/data/db/ColorsChinese.csv", "data", "ColorsChinese.csv", false));
            case "japanese":
                return readCSV(FxmlControl.getInternalFile("/data/db/ColorsJapanese.csv", "data", "ColorsJapanese.csv", false));
            case "colorhexa":
                return readCSV(FxmlControl.getInternalFile("/data/db/ColorsColorhexa.csv", "data", "ColorsColorhexa.csv", false));
            default:
                return readCSV(FxmlControl.getInternalFile("/data/db/ColorsWeb.csv", "data", "ColorsWeb.csv", false));
        }
    }

    public static void printHeader(CSVPrinter printer) {
        try {
            if (printer == null) {
                return;
            }
            printer.printRecord("name", "rgba", "rgb", "value", "SRGB", "HSB",
                    "AdobeRGB", "AppleRGB", "EciRGB", "SRGBLinear", "AdobeRGBLinear", "AppleRGBLinear",
                    "CalculatedCMYK", "EciCMYK", "AdobeCMYK", "XYZ", "CieLab", "Lchab", "CieLuv", "Lchuv", "PaletteIndex");
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static void exportCSV(File file) {
        try ( Connection conn = DerbyBase.getConnection();
                 CSVPrinter printer = new CSVPrinter(new FileWriter(file, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            conn.setReadOnly(true);
            String sql = " SELECT * FROM Color_Data";
            printHeader(printer);
            try (final Statement statement = conn.createStatement();
                    final ResultSet results = statement.executeQuery(sql)) {
                List<String> row = new ArrayList<>();
                while (results.next()) {
                    ColorData data = TableColorData.read(results); //
                    printRow(printer, row, data);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static void exportCSV(List<ColorData> dataList, File file) {
        try (final CSVPrinter printer = new CSVPrinter(new FileWriter(file, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            printHeader(printer);
            List<String> row = new ArrayList<>();
            for (ColorData data : dataList) {
                printRow(printer, row, data);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static void printRow(CSVPrinter printer, List<String> row, ColorData data) {
        try {
            if (printer == null || row == null || data == null) {
                return;
            }
            row.clear();
            row.add(data.getColorName());
            row.add(data.getRgba());
            row.add(data.getRgb());
            row.add(data.getColorValue() + "");
            row.add(data.getSrgb());
            row.add(data.getHsb());
            row.add(data.getAdobeRGB());
            row.add(data.getAppleRGB());
            row.add(data.getEciRGB());
            row.add(data.getSRGBLinear());
            row.add(data.getAdobeRGBLinear());
            row.add(data.getAppleRGBLinear());
            row.add(data.getCalculatedCMYK());
            row.add(data.getEciCMYK());
            row.add(data.getAdobeCMYK());
            row.add(data.getXyz());
            row.add(data.getCieLab());
            row.add(data.getLchab());
            row.add(data.getCieLuv());
            row.add(data.getLchuv());
            row.add((long) data.getPaletteIndex() + "");
            printer.printRecord(row);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public static List<ColorData> readCSV(File file) {
        List<ColorData> data = new ArrayList();
        try (final CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',').withTrim().withNullString(""))) {
            List<String> names = parser.getHeaderNames();
            if (names == null || (!names.contains("rgba") && !names.contains("rgb"))) {
                return null;
            }
            for (CSVRecord record : parser) {
                try {
                    ColorData item = new ColorData();
                    if (names.contains("name")) {
                        item.setColorName(record.get("name"));
                    }
                    if (names.contains("rgba")) {
                        item.setWeb(record.get("rgba"));
                    }
                    if (names.contains("rgb")) {
                        item.setRgb(record.get("rgb"));
                    }
                    if (names.contains("PaletteIndex")) {
                        item.setPaletteIndex(Double.parseDouble(record.get("PaletteIndex")));
                    }
                    try {
                        item.setColorValue(Integer.parseInt(record.get("value")));
                        item.setSrgb(record.get("SRGB"));
                        item.setHsb(record.get("HSB"));
                        item.setAdobeRGB(record.get("AdobeRGB"));
                        item.setAppleRGB(record.get("AppleRGB"));
                        item.setEciRGB(record.get("EciRGB"));
                        item.setSRGBLinear(record.get("SRGBLinear"));
                        item.setAdobeRGBLinear(record.get("AdobeRGBLinear"));
                        item.setAppleRGBLinear(record.get("AppleRGBLinear"));
                        item.setCalculatedCMYK(record.get("CalculatedCMYK"));
                        item.setEciCMYK(record.get("EciCMYK"));
                        item.setAdobeCMYK(record.get("AdobeCMYK"));
                        item.setXyz(record.get("XYZ"));
                        item.setCieLab(record.get("CieLab"));
                        item.setLchab(record.get("Lchab"));
                        item.setCieLuv(record.get("CieLuv"));
                        item.setLchuv(record.get("Lchuv"));
                    } catch (Exception e) {
                        item.calculate();
                    }
                    data.add(item);
                } catch (Exception e) {
                    MyBoxLog.debug(e.toString());
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return data;
    }

}
