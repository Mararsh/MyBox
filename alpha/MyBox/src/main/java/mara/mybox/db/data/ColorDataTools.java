package mara.mybox.db.data;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileTools;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-12-13
 * @License Apache License Version 2.0
 */
public class ColorDataTools {

    public static void printHeader(CSVPrinter printer, boolean orderNumber) {
        try {
            if (printer == null) {
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList("name", "rgba", "rgb", "SRGB", "HSB", "rybAngle",
                    "AdobeRGB", "AppleRGB", "EciRGB", "SRGBLinear", "AdobeRGBLinear", "AppleRGBLinear",
                    "CalculatedCMYK", "EciCMYK", "AdobeCMYK", "XYZ", "CieLab", "Lchab", "CieLuv", "Lchuv",
                    "invert", "complementray", "value"));
            if (orderNumber) {
                names.add("PaletteIndex");
            }
            printer.printRecord(names);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static void exportCSV(TableColor tableColor, File file) {
        try (Connection conn = DerbyBase.getConnection();
                CSVPrinter printer = CsvTools.csvPrinter(file)) {
            conn.setReadOnly(true);
            printHeader(printer, false);
            String sql = " SELECT * FROM Color ORDER BY color_value";
            try (PreparedStatement statement = conn.prepareStatement(sql);
                    ResultSet results = statement.executeQuery()) {
                List<String> row = new ArrayList<>();
                while (results.next()) {
                    ColorData data = tableColor.readData(results);
                    printRow(printer, row, data, false);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static void exportCSV(TableColorPalette tableColorPalette, File file, ColorPaletteName palette) {
        try (Connection conn = DerbyBase.getConnection();
                CSVPrinter printer = CsvTools.csvPrinter(file)) {
            conn.setReadOnly(true);
            printHeader(printer, true);
            String sql = "SELECT * FROM Color_Palette_View WHERE paletteid=" + palette.getCpnid() + " ORDER BY order_number";
            try (PreparedStatement statement = conn.prepareStatement(sql);
                    ResultSet results = statement.executeQuery()) {
                List<String> row = new ArrayList<>();
                while (results.next()) {
                    ColorPalette data = tableColorPalette.readData(results);
                    ColorData color = data.getData();
                    color.setColorName(data.getName());
                    color.setOrderNumner(data.getOrderNumber());
                    color.setPaletteid(data.getCpid());
                    printRow(printer, row, color, true);
                }
            } catch (Exception e) {
                MyBoxLog.error(e, sql);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static void exportCSV(List<ColorData> dataList, File file, boolean orderNumber) {
        try (final CSVPrinter printer = CsvTools.csvPrinter(file)) {
            printHeader(printer, orderNumber);
            List<String> row = new ArrayList<>();
            for (ColorData data : dataList) {
                printRow(printer, row, data, orderNumber);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static void printRow(CSVPrinter printer, List<String> row, ColorData data, boolean orderNumber) {
        try {
            if (printer == null || row == null || data == null) {
                return;
            }
            if (data.needConvert()) {
                data.convert();
            }
            row.clear();
            row.add(data.getColorName());
            row.add(data.getRgba());
            row.add(data.getRgb());
            row.add(data.getSrgb());
            row.add(data.getHsb());
            row.add(data.getRybAngle());
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
            row.add(data.getInvertRGB());
            row.add(data.getComplementaryRGB());
            row.add(data.getColorValue() + "");
            if (orderNumber) {
                row.add(data.getOrderNumner() + "");
            }
            printer.printRecord(row);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public static List<ColorData> readCSV(FxTask task, File file, boolean reOrder) {
        List<ColorData> data = new ArrayList();
        File validFile = FileTools.removeBOM(task, file);
        if (validFile == null || (task != null && !task.isWorking())) {
            return null;
        }
        try (CSVParser parser = CSVParser.parse(validFile, StandardCharsets.UTF_8, CsvTools.csvFormat())) {
            List<String> names = parser.getHeaderNames();
            if (names == null || (!names.contains("rgba") && !names.contains("rgb"))) {
                return null;
            }
            int index = 0;
            for (CSVRecord record : parser) {
                if (task != null && !task.isWorking()) {
                    parser.close();
                    return null;
                }
                try {
                    ColorData item = new ColorData();
                    if (names.contains("rgba")) {
                        item.setWeb(record.get("rgba"));
                    }
                    if (names.contains("rgb")) {
                        item.setRgb(record.get("rgb"));
                    }
                    if (names.contains("name")) {
                        item.setColorName(record.get("name"));
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
                    index++;
                    if (!reOrder && names.contains("PaletteIndex")) {
                        item.setOrderNumner(Float.parseFloat(record.get("PaletteIndex")));
                    } else {
                        item.setOrderNumner(index);
                    }
                    data.add(item);
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                }
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return data;
    }

}
