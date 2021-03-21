package mara.mybox.controller;

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
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
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
    protected boolean firstRow, toCsv, toHtml, toXml, toJson, toXlsx, toPdf;
    protected File csvFile, xmlFile, jsonFile, htmlFile, pdfFile, xlsxFile;
    protected CSVPrinter csvPrinter;
    protected FileWriter htmlWriter, xmlWriter, jsonWriter;
    protected XSSFWorkbook xssfBook;
    protected XSSFSheet xssfSheet;
    protected String indent = "    ", filePrefix;
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
    protected CheckBox csvCheck, pdfCheck, htmlCheck, xmlCheck, jsonCheck, xlsxCheck;
    @FXML
    protected TextArea cssArea;
    @FXML
    protected TextField widthList;
    @FXML
    protected VBox columnsWidthBox, styleBox;

    public ControlDataConvert() {
        baseTitle = AppVariables.message("dataConvert");

    }

    public void setControls(BaseTaskController parent, ControlPdfWriteOptions pdfOptionsController) {
        setControls(parent, pdfOptionsController, true, true, true, true, true, true);
    }

    public void setControls(BaseTaskController parent, ControlPdfWriteOptions pdfOptionsController,
            boolean toCsv, boolean toJson, boolean toXml, boolean toXlsx, boolean toHtml, boolean toPdf) {
        try {
            this.parent = parent;
            baseName = parent.baseName;
            this.pdfOptionsController = pdfOptionsController;
            if (pdfOptionsController != null) {
                pdfOptionsController.set(baseName, false);
            }
            this.toCsv = toCsv;
            this.toJson = toJson;
            this.toXml = toXml;
            this.toXlsx = toXlsx;
            this.toHtml = toHtml;
            this.toPdf = toPdf;
            formatsPane.getChildren().clear();
            csvWriteController.setControls(baseName + "Write");
            if (toCsv) {
                formatsPane.getChildren().add(csvCheck);
                csvCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "CSV", true));
                csvCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "CSV", csvCheck.isSelected());
                    }
                });
            }
            if (toJson) {
                formatsPane.getChildren().add(jsonCheck);
                jsonCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Json", false));
                jsonCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "Json", jsonCheck.isSelected());
                    }
                });

            }
            if (toXml) {
                formatsPane.getChildren().add(xmlCheck);
                xmlCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Xml", false));
                xmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "Xml", xmlCheck.isSelected());
                    }
                });
            }
            if (toXlsx) {
                formatsPane.getChildren().add(xlsxCheck);
                xlsxCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Xlsx", false));
                xlsxCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "Xlsx", xlsxCheck.isSelected());
                    }
                });
            }
            if (toHtml) {
                formatsPane.getChildren().add(htmlCheck);
                cssArea.setText(AppVariables.getUserConfigValue(baseName + "Css", HtmlTools.BaseStyle));
                htmlCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "Html", false));
                htmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "Html", htmlCheck.isSelected());
                    }
                });
            } else {
                thisPane.getChildren().remove(styleBox);
            }
            if (toPdf) {
                formatsPane.getChildren().add(pdfCheck);
                pdfCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "PDF", false));
                pdfCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "PDF", pdfCheck.isSelected());
                    }
                });
            } else {
                thisPane.getChildren().remove(columnsWidthBox);
            }
            FxmlControl.refreshStyle(thisPane);

            maxLines = -1;
            maxLinesSelector.getItems().addAll(Arrays.asList(
                    message("NotSplit"), "1000", "500", "200", "300", "800", "2000", "3000", "5000", "8000"
            ));
            maxLinesSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        if (newValue == null || newValue.isEmpty()) {
                            return;
                        }
                        AppVariables.setUserConfigValue(baseName + "ExportMaxLines", newValue);
                        if (message("NotSplit").equals(newValue)) {
                            maxLines = -1;
                            FxmlControl.setEditorNormal(maxLinesSelector);
                            return;
                        }
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                maxLines = v;
                                FxmlControl.setEditorNormal(maxLinesSelector);
                            } else {
                                FxmlControl.setEditorBadStyle(maxLinesSelector);
                            }
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(maxLinesSelector);
                        }
                    });
            maxLinesSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue(baseName + "ExportMaxLines", message("NotSplit")));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean initParameters() {
        try {
            if (parent == null) {
                return false;
            }
            names = null;
            csvPrinter = null;
            htmlWriter = null;
            xmlWriter = null;
            jsonWriter = null;
            xssfSheet = null;
            columnWidths = null;
            pdfTable = null;
            firstRow = true;
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
                AppVariables.setUserConfigValue(baseName + "Css", cssArea.getText());
            }
            if (toCsv && csvCheck.isSelected() && csvWriteController.delimiterInput.getStyle().equals(badStyle)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    protected boolean openWriters(String prefix) {
        filePrefix = prefix;
        fileIndex = 1;
        rowsIndex = 0;
        return openWriters();
    }

    protected boolean openWriters() {
        csvPrinter = null;
        htmlWriter = null;
        xmlWriter = null;
        jsonWriter = null;
        xssfSheet = null;
        firstRow = true;
        if (csvWriteController.charset == null) {
            csvWriteController.charset = Charset.forName("utf-8");
        }
        if (targetPath == null || filePrefix == null || names == null) {
            updateLogs(message("InvalidParameters"));
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
                    updateLogs(message("Writing") + " " + csvFile.getAbsolutePath());
                    CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader()
                            .withDelimiter(csvWriteController.delimiter).withTrim().withNullString("");
                    csvPrinter = new CSVPrinter(new FileWriter(csvFile, csvWriteController.charset), csvFormat);
                    csvPrinter.printRecord(names);
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (toHtml && htmlCheck.isSelected()) {
                htmlFile = makeTargetFile(currentPrefix, ".html", targetPath);
                if (htmlFile != null) {
                    updateLogs(message("Writing") + " " + htmlFile.getAbsolutePath());
                    htmlWriter = new FileWriter(htmlFile, csvWriteController.charset);
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
                    updateLogs(message("Skipped"));
                }
            }
            if (toXml && xmlCheck.isSelected()) {
                xmlFile = makeTargetFile(currentPrefix, ".xml", targetPath);
                if (xmlFile != null) {
                    updateLogs(message("Writing") + " " + xmlFile.getAbsolutePath());
                    xmlWriter = new FileWriter(xmlFile, csvWriteController.charset);
                    StringBuilder s = new StringBuilder();
                    s.append("<?xml version=\"1.0\" encoding=\"")
                            .append(csvWriteController.charset.name()).append("\"?>\n")
                            .append("<Data>\n");
                    xmlWriter.write(s.toString());
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (toJson && jsonCheck.isSelected()) {
                jsonFile = makeTargetFile(currentPrefix, ".json", targetPath);
                if (jsonFile != null) {
                    updateLogs(message("Writing") + " " + jsonFile.getAbsolutePath());
                    jsonWriter = new FileWriter(jsonFile, Charset.forName("utf-8"));
                    StringBuilder s = new StringBuilder();
                    s.append("{\"Data\": [\n");
                    jsonWriter.write(s.toString());
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (toPdf && pdfTable != null) {
                pdfFile = makeTargetFile(currentPrefix, ".pdf", targetPath);
                if (pdfFile != null) {
                    updateLogs(message("Writing") + " " + pdfFile.getAbsolutePath());
                    pdfTable.setColumns(names).createDoc(pdfFile);
                } else if (targetExistType == TargetExistType.Skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (toXlsx && xlsxCheck.isSelected()) {
                xlsxFile = makeTargetFile(currentPrefix, ".xlsx", targetPath);
                if (xlsxFile != null) {
                    updateLogs(message("Writing") + " " + xlsxFile.getAbsolutePath());
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
                    updateLogs(message("Skipped"));
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
            if (maxLines > 0 && rowsIndex >= maxLines) {
                closeWriters();
                fileIndex++;
                rowsIndex = 0;
                openWriters();
            }

            if (csvPrinter != null) {
                csvPrinter.printRecord(row);
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
                            .append(row.get(i))
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
                            .append(row.get(i)).append("\"");
                }
                s.append(indent).append("\n").append(indent).append("}");
                jsonWriter.write(s.toString());
            }

            if (pdfTable != null) {
                if (pageRows == null) {
                    pageRows = new ArrayList<>();
                }
                if (pageRows.size() < pdfTable.getRowsPerPage()) {
                    pageRows.add(row);
                } else {
                    pdfTable.writePage(pageRows);
                    pageRows.clear();
                }
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
            if (csvPrinter != null) {
                csvPrinter.flush();
                csvPrinter.close();
                targetFileGenerated(csvFile);
            }
            if (htmlWriter != null) {
                htmlWriter.write(StringTable.tableSuffix(new StringTable(names)));
                htmlWriter.write(indent + "<BODY>\n</HTML>\n");
                htmlWriter.flush();
                htmlWriter.close();
                targetFileGenerated(htmlFile);
            }
            if (xmlWriter != null) {
                xmlWriter.write("</Data>\n");
                xmlWriter.flush();
                xmlWriter.close();
                targetFileGenerated(xmlFile);
            }
            if (jsonWriter != null) {
                jsonWriter.write("\n]}\n");
                jsonWriter.flush();
                jsonWriter.close();
                targetFileGenerated(jsonFile);
            }
            if (pdfTable != null) {
                if (pageRows != null && !pageRows.isEmpty()) {
                    pdfTable.writePage(pageRows);
                    pageRows.clear();
                }
                pdfTable.closeDoc();
                targetFileGenerated(pdfFile);
            }
            if (xssfBook != null && xssfSheet != null) {
                for (int i = 0; i < names.size(); i++) {
                    xssfSheet.autoSizeColumn(i);
                }
                try ( FileOutputStream fileOut = new FileOutputStream(xlsxFile)) {
                    xssfBook.write(fileOut);
                }
                xssfBook.close();
                targetFileGenerated(xlsxFile);
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
