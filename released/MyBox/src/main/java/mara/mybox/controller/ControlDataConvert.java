package mara.mybox.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.HtmlStyles;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import thridparty.PaginatedPdfTable;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public class ControlDataConvert extends BaseController {

    protected BaseTaskController parent;
    protected List<String> names;
    protected boolean firstRow, toCsv, toText, toHtml, toXml, toJson, toXlsx, toPdf;
    protected File csvFile, textFile, xmlFile, jsonFile, htmlFile, pdfFile, xlsxFile;
    protected CSVPrinter csvPrinter;
    protected BufferedWriter textWriter, htmlWriter, xmlWriter, jsonWriter;
    protected XSSFWorkbook xssfBook;
    protected XSSFSheet xssfSheet;
    protected String indent = "    ", filePrefix, textDelimiter;
    protected List<Integer> columnWidths;
    protected PaginatedPdfTable pdfTable;
    protected List<List<String>> pageRows;
    protected ControlPdfWriteOptions pdfOptionsController;
    protected int maxLines, fileIndex, rowsIndex;

    @FXML
    protected FlowPane formatsPane;
    @FXML
    protected ControlCsvOptions csvWriteController;
    @FXML
    protected ComboBox<String> maxLinesSelector;
    @FXML
    protected CheckBox csvCheck, textCheck, pdfCheck, htmlCheck, xmlCheck, jsonCheck, xlsxCheck;
    @FXML
    protected TextArea cssArea;
    @FXML
    protected TextField widthList;
    @FXML
    protected VBox columnsWidthBox, styleBox;
    @FXML
    protected ControlTextOptions textWriteOptionsController;

    public ControlDataConvert() {
        baseTitle = Languages.message("dataConvert");

    }

    public void setControls(BaseTaskController parent, ControlPdfWriteOptions pdfOptionsController) {
        setControls(parent, pdfOptionsController, true, true, true, true, true, true, true);
    }

    public void setControls(BaseTaskController parent, ControlPdfWriteOptions pdfOptionsController,
            boolean toCsv, boolean toText, boolean toJson, boolean toXml, boolean toXlsx, boolean toHtml, boolean toPdf) {
        try {
            this.parent = parent;
            baseName = parent.baseName;

            this.pdfOptionsController = pdfOptionsController;
            if (pdfOptionsController != null) {
                pdfOptionsController.set(baseName, false);
            }
            this.toCsv = toCsv;
            this.toText = toText;
            this.toJson = toJson;
            this.toXml = toXml;
            this.toXlsx = toXlsx;
            this.toHtml = toHtml;
            this.toPdf = toPdf;
            formatsPane.getChildren().clear();
            csvWriteController.setControls(baseName + "Write");
            textWriteOptionsController.setControls(baseName + "Write", false);

            if (toCsv) {
                formatsPane.getChildren().add(csvCheck);
                csvCheck.setSelected(UserConfig.getBoolean(baseName + "CSV", true));
                csvCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "CSV", csvCheck.isSelected());
                    }
                });
            }

            if (toText) {
                formatsPane.getChildren().add(textCheck);
                textCheck.setSelected(UserConfig.getBoolean(baseName + "Text", true));
                textCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "Text", textCheck.isSelected());
                    }
                });
            }

            if (toJson) {
                formatsPane.getChildren().add(jsonCheck);
                jsonCheck.setSelected(UserConfig.getBoolean(baseName + "Json", false));
                jsonCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "Json", jsonCheck.isSelected());
                    }
                });

            }
            if (toXml) {
                formatsPane.getChildren().add(xmlCheck);
                xmlCheck.setSelected(UserConfig.getBoolean(baseName + "Xml", false));
                xmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "Xml", xmlCheck.isSelected());
                    }
                });
            }
            if (toXlsx) {
                formatsPane.getChildren().add(xlsxCheck);
                xlsxCheck.setSelected(UserConfig.getBoolean(baseName + "Xlsx", false));
                xlsxCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "Xlsx", xlsxCheck.isSelected());
                    }
                });
            }
            if (toHtml) {
                formatsPane.getChildren().add(htmlCheck);
                cssArea.setText(UserConfig.getString(baseName + "Css", HtmlStyles.BaseStyle));
                htmlCheck.setSelected(UserConfig.getBoolean(baseName + "Html", false));
                htmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "Html", htmlCheck.isSelected());
                    }
                });
            } else {
                thisPane.getChildren().remove(styleBox);
            }
            if (toPdf) {
                formatsPane.getChildren().add(pdfCheck);
                pdfCheck.setSelected(UserConfig.getBoolean(baseName + "PDF", false));
                pdfCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(baseName + "PDF", pdfCheck.isSelected());
                    }
                });
            } else {
                thisPane.getChildren().remove(columnsWidthBox);
            }
            refreshStyle(thisPane);

            maxLines = -1;
            maxLinesSelector.getItems().addAll(Arrays.asList(Languages.message("NotSplit"), "1000", "500", "200", "300", "800", "2000", "3000", "5000", "8000"
            ));
            maxLinesSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                if (newValue == null || newValue.isEmpty()) {
                    return;
                }
                UserConfig.setString(baseName + "ExportMaxLines", newValue);
                if (Languages.message("NotSplit").equals(newValue)) {
                    maxLines = -1;
                    ValidationTools.setEditorNormal(maxLinesSelector);
                    return;
                }
                try {
                    int v = Integer.valueOf(newValue);
                    if (v > 0) {
                        maxLines = v;
                        ValidationTools.setEditorNormal(maxLinesSelector);
                    } else {
                        ValidationTools.setEditorBadStyle(maxLinesSelector);
                    }
                } catch (Exception e) {
                    ValidationTools.setEditorBadStyle(maxLinesSelector);
                }
            });
            maxLinesSelector.getSelectionModel().select(UserConfig.getString(baseName + "ExportMaxLines", Languages.message("NotSplit")));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean initParameters() {
        try {
            if (parent == null) {
                return false;
            }
            initWriters();

            names = null;
            columnWidths = null;
            pdfTable = null;
            filePrefix = null;
            fileIndex = 1;
            rowsIndex = 0;
            targetPath = parent.targetPath;
            targetExistType = parent.targetExistType;

            if (pdfOptionsController != null && toPdf && pdfCheck.isSelected()) {
                columnWidths = new ArrayList<>();
                String[] values = widthList.getText().split(",");
                for (String value : values) {
                    try {
                        int v = Integer.parseInt(value.trim());
                        if (v > 0) {
                            columnWidths.add(v);
                        }
                    } catch (Exception e) {
                    }
                }
                pdfTable = PaginatedPdfTable.create()
                        .setPageSize(new PDRectangle(pdfOptionsController.pageWidth, pdfOptionsController.pageHeight))
                        .setTtf(pdfOptionsController.getTtfFile())
                        .setFontSize(pdfOptionsController.fontSize)
                        .setMargin(pdfOptionsController.marginSize)
                        .setColumnWidths(columnWidths)
                        .setDefaultZoom(pdfOptionsController.zoom)
                        .setHeader(pdfOptionsController.getHeader())
                        .setShowPageNumber(pdfOptionsController.showPageNumber);
            }
            if (toHtml && htmlCheck.isSelected()) {
                UserConfig.setString(baseName + "Css", cssArea.getText());
            }
            if (toCsv && csvCheck.isSelected() && csvWriteController.delimiterInput.getStyle().equals(NodeStyleTools.badStyle)) {
                return false;
            }
            if (toText && textCheck.isSelected()
                    && textWriteOptionsController.delimiterController.delimiterInput.getStyle().equals(NodeStyleTools.badStyle)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    protected void initWriters() {
        csvPrinter = null;
        textWriter = null;
        htmlWriter = null;
        xmlWriter = null;
        jsonWriter = null;
        xssfSheet = null;

        csvFile = null;
        textFile = null;
        htmlFile = null;
        xmlFile = null;
        xlsxFile = null;
        jsonFile = null;
        pdfFile = null;

        firstRow = true;
    }

    protected boolean openWriters(String prefix) {
        filePrefix = prefix;
        fileIndex = 1;
        rowsIndex = 0;
        return openWriters();
    }

    protected boolean openWriters() {
        initWriters();
        if (csvWriteController.charset == null) {
            csvWriteController.charset = Charset.forName("utf-8");
        }
        if (targetPath == null || filePrefix == null || names == null) {
            updateLogs(Languages.message("InvalidParameters"));
            return false;
        }
        String currentPrefix = filePrefix;
        if (maxLines > 0) {
            currentPrefix += "_" + fileIndex;
        }
        try {
            if (toCsv && csvCheck.isSelected()) {
                csvFile = makeTargetFile(currentPrefix, ".csv", targetPath);
                if (csvFile != null) {
                    updateLogs(Languages.message("Writing") + " " + csvFile.getAbsolutePath());
                    CSVFormat csvFormat = CSVFormat.DEFAULT
                            .withDelimiter(csvWriteController.delimiter).withTrim().withNullString("");
                    csvPrinter = new CSVPrinter(new FileWriter(csvFile, csvWriteController.charset), csvFormat);
                    if (csvWriteController.withNamesCheck.isSelected()) {
                        csvPrinter.printRecord(names);
                    }
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(Languages.message("Skipped"));
                }
            }
            if (toText && textCheck.isSelected()) {
                textFile = makeTargetFile(currentPrefix, ".txt", targetPath);
                if (textFile != null) {
                    updateLogs(Languages.message("Writing") + " " + textFile.getAbsolutePath());
                    textWriter = new BufferedWriter(new FileWriter(textFile, textWriteOptionsController.charset));
                    textDelimiter = TextTools.delimiterValue(textWriteOptionsController.delimiterName);
                    if (textWriteOptionsController.withNamesCheck.isSelected()) {
                        TextFileTools.writeLine(textWriter, names, textDelimiter);
                    }
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(Languages.message("Skipped"));
                }
            }
            if (toHtml && htmlCheck.isSelected()) {
                htmlFile = makeTargetFile(currentPrefix, ".html", targetPath);
                if (htmlFile != null) {
                    updateLogs(Languages.message("Writing") + " " + htmlFile.getAbsolutePath());
                    htmlWriter = new BufferedWriter(new FileWriter(htmlFile, csvWriteController.charset));
                    StringBuilder s = new StringBuilder();
                    s.append("<!DOCTYPE html><HTML>\n").
                            append(indent).append("<HEAD>\n").
                            append(indent).append(indent).
                            append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=").append(csvWriteController.charset.name())
                            .append("\" />\n");
                    s.append(indent).append(indent).append("<style type=\"text/css\">\n");
                    s.append(indent).append(indent).append(indent).append(cssArea.getText().trim()).append("\n");
                    s.append(indent).append(indent).append("</style>\n");
                    s.append(indent).append("</HEAD>\n").append(indent).append("<BODY>\n");
                    s.append(StringTable.tablePrefix(new StringTable(names)));
                    htmlWriter.write(s.toString());
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(Languages.message("Skipped"));
                }
            }
            if (toXml && xmlCheck.isSelected()) {
                xmlFile = makeTargetFile(currentPrefix, ".xml", targetPath);
                if (xmlFile != null) {
                    updateLogs(Languages.message("Writing") + " " + xmlFile.getAbsolutePath());
                    xmlWriter = new BufferedWriter(new FileWriter(xmlFile, csvWriteController.charset));
                    StringBuilder s = new StringBuilder();
                    s.append("<?xml version=\"1.0\" encoding=\"")
                            .append(csvWriteController.charset.name()).append("\"?>\n")
                            .append("<Data>\n");
                    xmlWriter.write(s.toString());
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(Languages.message("Skipped"));
                }
            }
            if (toJson && jsonCheck.isSelected()) {
                jsonFile = makeTargetFile(currentPrefix, ".json", targetPath);
                if (jsonFile != null) {
                    updateLogs(Languages.message("Writing") + " " + jsonFile.getAbsolutePath());
                    jsonWriter = new BufferedWriter(new FileWriter(jsonFile, Charset.forName("utf-8")));
                    StringBuilder s = new StringBuilder();
                    s.append("{\"Data\": [\n");
                    jsonWriter.write(s.toString());
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(Languages.message("Skipped"));
                }
            }
            if (toPdf && pdfCheck.isSelected() && pdfTable != null) {
                pdfFile = makeTargetFile(currentPrefix, ".pdf", targetPath);
                if (pdfFile != null) {
                    updateLogs(Languages.message("Writing") + " " + pdfFile.getAbsolutePath());
                    pdfTable.setColumns(names).createDoc(pdfFile);
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(Languages.message("Skipped"));
                }
            }
            if (toXlsx && xlsxCheck.isSelected()) {
                xlsxFile = makeTargetFile(currentPrefix, ".xlsx", targetPath);
                if (xlsxFile != null) {
                    updateLogs(Languages.message("Writing") + " " + xlsxFile.getAbsolutePath());
                    xssfBook = new XSSFWorkbook();
                    xssfSheet = xssfBook.createSheet("sheet1");
                    xssfSheet.setDefaultColumnWidth(20);
                    XSSFRow titleRow = xssfSheet.createRow(0);
                    XSSFCellStyle horizontalCenter = xssfBook.createCellStyle();
                    horizontalCenter.setAlignment(HorizontalAlignment.CENTER);
                    for (int i = 0; i < names.size(); i++) {
                        XSSFCell cell = titleRow.createCell(i);
                        cell.setCellValue(names.get(i));
                        cell.setCellStyle(horizontalCenter);
                    }
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(Languages.message("Skipped"));
                }
            }
            return true;
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
    }

    protected void writeRow(List<String> row) {
        try {
            if (row == null) {
                return;
            }
            if (maxLines > 0 && rowsIndex >= maxLines) {
                closeWriters();
                fileIndex++;
                rowsIndex = 0;
                openWriters();
            }

            if (csvPrinter != null) {
                csvPrinter.printRecord(row);
            }

            if (textWriter != null) {
                TextFileTools.writeLine(textWriter, row, textDelimiter);
            }

            if (htmlWriter != null) {
                htmlWriter.write(StringTable.tableRow(row));
            }
            if (xmlWriter != null) {
                StringBuilder s = new StringBuilder();
                s.append(indent).append("<Row>").append("\n");
                for (int i = 0; i < names.size(); i++) {
                    String value = row.get(i);
                    if (value == null || value.isBlank()) {
                        continue;
                    }
                    s.append(indent).append(indent)
                            .append("<Col name=\"").append(names.get(i)).append("\" >")
                            .append(value)
                            .append("</Col>").append("\n");
                }
                s.append(indent).append("</Row>").append("\n");
                xmlWriter.write(s.toString());
            }
            if (jsonWriter != null) {
                StringBuilder s = new StringBuilder();
                if (!firstRow) {
                    s.append(",\n");
                } else {
                    firstRow = false;
                }
                s.append(indent).append("{").append("\n");
                boolean firstData = true;
                for (int i = 0; i < names.size(); i++) {
                    String value = row.get(i);
                    if (value == null || value.isBlank()) {
                        continue;
                    }
                    if (!firstData) {
                        s.append(",\n");
                    } else {
                        firstData = false;
                    }
                    s.append(indent).append(indent)
                            .append("\"").append(names.get(i)).append("\": \"")
                            .append(value).append("\"");
                }
                s.append(indent).append("\n").append(indent).append("}");
                jsonWriter.write(s.toString());
            }

            if (pdfFile != null) {
                if (pageRows == null) {
                    pageRows = new ArrayList<>();
                }
                if (pageRows.size() >= pdfTable.getRowsPerPage()) {
                    pdfTable.writePage(pageRows);
                    pageRows = new ArrayList<>();
                }
                pageRows.add(row);
            }

            if (xssfSheet != null) {
                XSSFRow sheetRow = xssfSheet.createRow(rowsIndex + 1);
                for (int i = 0; i < row.size(); i++) {
                    XSSFCell cell = sheetRow.createCell(i);
                    cell.setCellValue(row.get(i));
                }
            }

            rowsIndex++;
        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    protected void closeWriters() {
        try {
            if (csvPrinter != null && csvFile != null) {
                csvPrinter.flush();
                csvPrinter.close();
                targetFileGenerated(csvFile);
                csvPrinter = null;
            }

            if (textWriter != null && textFile != null) {
                textWriter.flush();
                textWriter.close();
                targetFileGenerated(textFile);
                textWriter = null;
            }

            if (htmlWriter != null && htmlFile != null) {
                htmlWriter.write(StringTable.tableSuffix(new StringTable(names)));
                htmlWriter.write(indent + "<BODY>\n</HTML>\n");
                htmlWriter.flush();
                htmlWriter.close();
                targetFileGenerated(htmlFile);
                htmlWriter = null;
            }

            if (xmlWriter != null && xmlFile != null) {
                xmlWriter.write("</Data>\n");
                xmlWriter.flush();
                xmlWriter.close();
                targetFileGenerated(xmlFile);
                xmlWriter = null;
            }

            if (jsonWriter != null && jsonFile != null) {
                jsonWriter.write("\n]}\n");
                jsonWriter.flush();
                jsonWriter.close();
                targetFileGenerated(jsonFile);
                jsonWriter = null;
            }

            if (pdfFile != null && pdfTable != null) {
                if (pageRows != null && !pageRows.isEmpty()) {
                    pdfTable.writePage(pageRows);
                    pageRows = null;
                }
                pdfTable.closeDoc();
                targetFileGenerated(pdfFile);
                pdfTable = null;
            }

            if (xssfBook != null && xssfSheet != null && xlsxFile != null) {
                for (int i = 0; i < names.size(); i++) {
                    xssfSheet.autoSizeColumn(i);
                }
                try ( FileOutputStream fileOut = new FileOutputStream(xlsxFile)) {
                    xssfBook.write(fileOut);
                }
                xssfBook.close();
                targetFileGenerated(xlsxFile);
                xssfBook = null;
            }

        } catch (Exception e) {
            updateLogs(e.toString());
            MyBoxLog.console(e.toString());
        }
    }

    protected void updateLogs(String logs) {
        parent.updateLogs(logs);
    }

    protected void targetFileGenerated(File file) {
        parent.targetFileGenerated(file);
    }

}
