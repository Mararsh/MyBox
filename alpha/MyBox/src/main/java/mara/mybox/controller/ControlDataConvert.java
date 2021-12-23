package mara.mybox.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
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
import mara.mybox.data.Data2D;
import mara.mybox.data.DataClipboard;
import mara.mybox.data.PaginatedPdfTable;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.tools.JsonTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.HtmlStyles;
import static mara.mybox.value.Languages.message;
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

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public class ControlDataConvert extends BaseController {

    protected BaseTaskController parent;
    protected List<String> names;
    protected List<Data2DColumn> columns;
    protected boolean firstRow, skip;
    protected File csvFile, textFile, xmlFile, jsonFile, htmlFile, pdfFile, xlsxFile, dataClipboardFile;
    protected CSVPrinter csvPrinter, dataClipboardPrinter;
    protected BufferedWriter textWriter, htmlWriter, xmlWriter, jsonWriter;
    protected XSSFWorkbook xssfBook;
    protected XSSFSheet xssfSheet;
    protected String indent = "    ", filePrefix, textDelimiter;
    protected List<Integer> columnWidths;
    protected PaginatedPdfTable pdfTable;
    protected List<List<String>> pageRows;
    protected int maxLines, fileIndex, fileRowIndex, dataRowIndex;

    @FXML
    protected FlowPane formatsPane;
    @FXML
    protected ComboBox<String> maxLinesSelector;
    @FXML
    protected CheckBox csvCheck, textsCheck, pdfCheck, htmlCheck, xmlCheck, jsonCheck, excelCheck,
            dataClipboardCheck, rowNumberCheck, excelWithNamesCheck;
    @FXML
    protected TextArea cssArea;
    @FXML
    protected TextField widthList;
    @FXML
    protected ControlCsvOptions csvWriteController;
    @FXML
    protected ControlTextOptions textWriteOptionsController;
    @FXML
    protected ControlPdfWriteOptions pdfOptionsController;

    public ControlDataConvert() {
        baseTitle = message("dataConvert");
    }

    /*
        init
     */
    public void setControls(BaseTaskController parent) {
        this.parent = parent;
        baseName = parent.baseName + baseName;

        initChecks();
        initCSV();
        initExcel();
        initTexts();
        initPDF();
        initHtml();
        initOthers();
    }

    private void initChecks() {
        csvCheck.setSelected(UserConfig.getBoolean(baseName + "CSV", true));
        csvCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "CSV", csvCheck.isSelected());
            }
        });

        textsCheck.setSelected(UserConfig.getBoolean(baseName + "Text", true));
        textsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "Text", textsCheck.isSelected());
            }
        });

        jsonCheck.setSelected(UserConfig.getBoolean(baseName + "Json", false));
        jsonCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "Json", jsonCheck.isSelected());
            }
        });

        xmlCheck.setSelected(UserConfig.getBoolean(baseName + "Xml", false));
        xmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "Xml", xmlCheck.isSelected());
            }
        });

        excelCheck.setSelected(UserConfig.getBoolean(baseName + "Xlsx", false));
        excelCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "Xlsx", excelCheck.isSelected());
            }
        });

        htmlCheck.setSelected(UserConfig.getBoolean(baseName + "Html", false));
        htmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "Html", htmlCheck.isSelected());
            }
        });

        pdfCheck.setSelected(UserConfig.getBoolean(baseName + "PDF", false));
        pdfCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "PDF", pdfCheck.isSelected());
            }
        });

        dataClipboardCheck.setSelected(UserConfig.getBoolean(baseName + "DataClipboard", false));
        dataClipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(baseName + "DataClipboard", dataClipboardCheck.isSelected());
            }
        });
    }

    private void initCSV() {
        csvWriteController.setControls(baseName + "Write");
    }

    private void initExcel() {
        excelWithNamesCheck.setSelected(UserConfig.getBoolean(baseName + "ExcelTargetWithNames", true));
        excelWithNamesCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
            if (!isSettingValues) {
                UserConfig.setBoolean(baseName + "ExcelTargetWithNames", newValue);
            }
        });
    }

    private void initTexts() {
        textWriteOptionsController.setControls(baseName + "Write", false);
    }

    private void initHtml() {
        cssArea.setText(UserConfig.getString(baseName + "Css", HtmlStyles.BaseStyle));
    }

    private void initPDF() {
        if (pdfOptionsController != null) {
            pdfOptionsController.set(baseName, false);
        }
        pdfOptionsController.pixSizeRadio.setDisable(true);
        pdfOptionsController.standardSizeRadio.fire();
    }

    private void initOthers() {
        try {
            rowNumberCheck.setSelected(UserConfig.getBoolean(baseName + "TargetWithRowNumber", false));
            rowNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "TargetWithRowNumber", rowNumberCheck.isSelected());
                }
            });

            maxLines = -1;
            maxLinesSelector.getItems().addAll(Arrays.asList(message("NotSplit"),
                    "1000", "500", "200", "300", "800", "2000", "3000", "5000", "8000"
            ));
            maxLinesSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        if (newValue == null || newValue.isEmpty()) {
                            return;
                        }
                        UserConfig.setString(baseName + "ExportMaxLines", newValue);
                        if (message("NotSplit").equals(newValue)) {
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
            maxLinesSelector.getSelectionModel().select(
                    UserConfig.getString(baseName + "ExportMaxLines", message("NotSplit")));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        run task
     */
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
            fileRowIndex = dataRowIndex = 0;
            targetPath = parent.targetPath;

            if (pdfCheck.isSelected()) {
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
            if (htmlCheck.isSelected()) {
                UserConfig.setString(baseName + "Css", cssArea.getText());
            }
            if (csvCheck.isSelected() && csvWriteController.delimiterInput.getStyle().equals(UserConfig.badStyle())) {
                return false;
            }
            if (textsCheck.isSelected()
                    && textWriteOptionsController.delimiterController.delimiterInput.getStyle().equals(UserConfig.badStyle())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean setExport(File path, List<Data2DColumn> cols, String prefix, boolean skip) {
        targetPath = path;
        columns = cols;
        names = new ArrayList<>();
        for (Data2DColumn c : columns) {
            names.add(c.getName());
        }
        return setParameters(prefix, skip);
    }

    public boolean setParameters(File path, List<String> cols, String prefix, boolean skip) {
        targetPath = path;
        names = cols;
        return setParameters(prefix, skip);
    }

    public boolean setParameters(String prefix, boolean skip) {
        filePrefix = prefix;
        this.skip = skip;
        fileIndex = 1;
        fileRowIndex = dataRowIndex = 0;
        if (rowNumberCheck.isSelected() && columns != null) {
            columns.add(0, new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
        }
        if (rowNumberCheck.isSelected() && names != null) {
            names.add(0, message("RowNumber"));
        }
        if (columns == null || columns.isEmpty()) {
            columns = Data2DColumn.toColumns(names);
        }
        return openWriters();
    }

    private void initWriters() {
        csvPrinter = null;
        textWriter = null;
        htmlWriter = null;
        xmlWriter = null;
        jsonWriter = null;
        xssfSheet = null;
        dataClipboardPrinter = null;

        csvFile = null;
        textFile = null;
        htmlFile = null;
        xmlFile = null;
        xlsxFile = null;
        jsonFile = null;
        pdfFile = null;
        dataClipboardFile = null;

        firstRow = true;
    }

    private boolean openWriters() {
        initWriters();
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
            if (csvCheck.isSelected()) {
                csvFile = parent.makeTargetFile(currentPrefix, ".csv", targetPath);
                if (csvFile != null) {
                    updateLogs(message("Writing") + " " + csvFile.getAbsolutePath());
                    CSVFormat csvFormat = CSVFormat.DEFAULT
                            .withDelimiter(csvWriteController.delimiter).withTrim().withNullString("");
                    csvPrinter = new CSVPrinter(new FileWriter(csvFile, csvWriteController.charset), csvFormat);
                    if (csvWriteController.withNamesCheck.isSelected()) {
                        csvPrinter.printRecord(names);
                    }
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (textsCheck.isSelected()) {
                textFile = parent.makeTargetFile(currentPrefix, ".txt", targetPath);
                if (textFile != null) {
                    updateLogs(message("Writing") + " " + textFile.getAbsolutePath());
                    textWriter = new BufferedWriter(new FileWriter(textFile, textWriteOptionsController.charset));
                    textDelimiter = TextTools.delimiterValue(textWriteOptionsController.delimiterName);
                    if (textWriteOptionsController.withNamesCheck.isSelected()) {
                        TextFileTools.writeLine(textWriter, names, textDelimiter);
                    }
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (htmlCheck.isSelected()) {
                htmlFile = parent.makeTargetFile(currentPrefix, ".html", targetPath);
                if (htmlFile != null) {
                    updateLogs(message("Writing") + " " + htmlFile.getAbsolutePath());
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
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (xmlCheck.isSelected()) {
                xmlFile = parent.makeTargetFile(currentPrefix, ".xml", targetPath);
                if (xmlFile != null) {
                    updateLogs(message("Writing") + " " + xmlFile.getAbsolutePath());
                    xmlWriter = new BufferedWriter(new FileWriter(xmlFile, csvWriteController.charset));
                    StringBuilder s = new StringBuilder();
                    s.append("<?xml version=\"1.0\" encoding=\"")
                            .append(csvWriteController.charset.name()).append("\"?>\n")
                            .append("<Data>\n");
                    xmlWriter.write(s.toString());
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (jsonCheck.isSelected()) {
                jsonFile = parent.makeTargetFile(currentPrefix, ".json", targetPath);
                if (jsonFile != null) {
                    updateLogs(message("Writing") + " " + jsonFile.getAbsolutePath());
                    jsonWriter = new BufferedWriter(new FileWriter(jsonFile, Charset.forName("utf-8")));
                    StringBuilder s = new StringBuilder();
                    s.append("{\"Data\": [\n");
                    jsonWriter.write(s.toString());
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (pdfCheck.isSelected() && pdfTable != null) {
                pdfFile = parent.makeTargetFile(currentPrefix, ".pdf", targetPath);
                if (pdfFile != null) {
                    updateLogs(message("Writing") + " " + pdfFile.getAbsolutePath());
                    pdfTable.setColumns(names).createDoc(pdfFile);
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (excelCheck.isSelected()) {
                xlsxFile = parent.makeTargetFile(currentPrefix, ".xlsx", targetPath);
                if (xlsxFile != null) {
                    updateLogs(message("Writing") + " " + xlsxFile.getAbsolutePath());
                    xssfBook = new XSSFWorkbook();
                    xssfSheet = xssfBook.createSheet("sheet1");
                    xssfSheet.setDefaultColumnWidth(20);
                    if (excelWithNamesCheck.isSelected()) {
                        XSSFRow titleRow = xssfSheet.createRow(0);
                        XSSFCellStyle horizontalCenter = xssfBook.createCellStyle();
                        horizontalCenter.setAlignment(HorizontalAlignment.CENTER);
                        for (int i = 0; i < names.size(); i++) {
                            XSSFCell cell = titleRow.createCell(i);
                            cell.setCellValue(names.get(i));
                            cell.setCellStyle(horizontalCenter);
                        }
                    }
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (dataClipboardCheck.isSelected()) {
                dataClipboardFile = DataClipboard.newFile();
                if (dataClipboardFile != null) {
                    updateLogs(message("Writing") + " " + dataClipboardFile.getAbsolutePath());
                    CSVFormat csvFormat = CSVFormat.DEFAULT
                            .withDelimiter(',').withTrim().withNullString("");
                    dataClipboardPrinter = new CSVPrinter(new FileWriter(dataClipboardFile, Charset.forName("UTF-8")), csvFormat);
                    dataClipboardPrinter.printRecord(names);
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            return true;
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
    }

    public void writeRow(List<String> row) {
        try {
            if (row == null) {
                return;
            }

            if (maxLines > 0 && fileRowIndex >= maxLines) {
                closeWriters();
                fileIndex++;
                fileRowIndex = 0;
                openWriters();
            }

            dataRowIndex++;
            if (rowNumberCheck.isSelected()) {
                row.add(0, dataRowIndex + "");
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
                            .append(JsonTools.replaceSpecialChars(value)).append("\"");
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
                XSSFRow sheetRow = xssfSheet.createRow(fileRowIndex + 1);
                for (int i = 0; i < row.size(); i++) {
                    XSSFCell cell = sheetRow.createCell(i);
                    cell.setCellValue(row.get(i));
                }
            }

            if (dataClipboardPrinter != null) {
                dataClipboardPrinter.printRecord(row);
            }

            fileRowIndex++;

        } catch (Exception e) {
            updateLogs(e.toString());
        }
    }

    public void closeWriters() {
        try ( Connection conn = DerbyBase.getConnection()) {
            if (csvPrinter != null && csvFile != null) {
                csvPrinter.flush();
                csvPrinter.close();
                parent.targetFileGenerated(csvFile, VisitHistory.FileType.CSV);
                csvPrinter = null;
                Data2D d = Data2D.create(Data2DDefinition.Type.CSV).setTask(task);
                d.setFile(csvFile)
                        .setCharset(csvWriteController.charset)
                        .setDelimiter(csvWriteController.delimiter + "")
                        .setHasHeader(csvWriteController.withNamesCheck.isSelected())
                        .setDataName(csvFile.getName())
                        .setColsNumber(columns.size())
                        .setRowsNumber(dataRowIndex);
                Data2D.save(conn, d, columns);
                conn.commit();
            }

            if (textWriter != null && textFile != null) {
                textWriter.flush();
                textWriter.close();
                parent.targetFileGenerated(textFile, VisitHistory.FileType.Text);
                textWriter = null;
                Data2D d = Data2D.create(Data2DDefinition.Type.Texts).setTask(task);
                d.setFile(textFile)
                        .setCharset(textWriteOptionsController.charset)
                        .setDelimiter(textDelimiter)
                        .setHasHeader(textWriteOptionsController.withNamesCheck.isSelected())
                        .setDataName(textFile.getName())
                        .setColsNumber(columns.size())
                        .setRowsNumber(dataRowIndex);
                Data2D.save(conn, d, columns);
                conn.commit();
            }

            if (htmlWriter != null && htmlFile != null) {
                htmlWriter.write(StringTable.tableSuffix(new StringTable(names)));
                htmlWriter.write(indent + "<BODY>\n</HTML>\n");
                htmlWriter.flush();
                htmlWriter.close();
                parent.targetFileGenerated(htmlFile, VisitHistory.FileType.Html);
                htmlWriter = null;
            }

            if (xmlWriter != null && xmlFile != null) {
                xmlWriter.write("</Data>\n");
                xmlWriter.flush();
                xmlWriter.close();
                parent.targetFileGenerated(xmlFile, VisitHistory.FileType.Xml);
                xmlWriter = null;
            }

            if (jsonWriter != null && jsonFile != null) {
                jsonWriter.write("\n]}\n");
                jsonWriter.flush();
                jsonWriter.close();
                parent.targetFileGenerated(jsonFile, VisitHistory.FileType.Text);
                jsonWriter = null;
            }

            if (pdfFile != null && pdfTable != null) {
                if (pageRows != null && !pageRows.isEmpty()) {
                    pdfTable.writePage(pageRows);
                    pageRows = null;
                }
                pdfTable.closeDoc();
                parent.targetFileGenerated(pdfFile, VisitHistory.FileType.PDF);
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
                parent.targetFileGenerated(xlsxFile, VisitHistory.FileType.Excel);
                xssfBook = null;
                Data2D d = Data2D.create(Data2DDefinition.Type.Excel).setTask(task);
                d.setFile(xlsxFile).setSheet("sheet1")
                        .setHasHeader(excelWithNamesCheck.isSelected())
                        .setDataName(xlsxFile.getName())
                        .setColsNumber(columns.size())
                        .setRowsNumber(dataRowIndex);
                Data2D.save(conn, d, columns);
                conn.commit();
            }

            if (dataClipboardPrinter != null && dataClipboardFile != null) {
                dataClipboardPrinter.flush();
                dataClipboardPrinter.close();
                parent.targetFileGenerated(dataClipboardFile, VisitHistory.FileType.CSV);
                dataClipboardPrinter = null;
                Data2D d = Data2D.create(Data2DDefinition.Type.MyBoxClipboard).setTask(task);
                d.setFile(dataClipboardFile)
                        .setCharset(Charset.forName("UTF-8"))
                        .setDelimiter(",")
                        .setHasHeader(true)
                        .setDataName(dataRowIndex + "x" + columns.size())
                        .setColsNumber(columns.size())
                        .setRowsNumber(dataRowIndex);
                Data2D.save(conn, d, columns);
                DataClipboardController.update();
                conn.commit();
            }
            conn.close();
        } catch (Exception e) {
            updateLogs(e.toString());
            MyBoxLog.console(e.toString());
        }
    }

    public void updateLogs(String logs) {
        parent.updateLogs(logs);
    }

    public void openFiles() {
        if (csvFile != null && csvFile.exists()) {
            DataFileCSVController.open(csvFile, csvWriteController.charset,
                    csvWriteController.withNamesCheck.isSelected(), csvWriteController.delimiter);
        }
        if (xlsxFile != null && xlsxFile.exists()) {
            DataFileExcelController.open(xlsxFile, excelWithNamesCheck.isSelected());
        }
        if (textFile != null && textFile.exists()) {
            DataFileTextController.open(textFile, textWriteOptionsController.charset,
                    textWriteOptionsController.withNamesCheck.isSelected(), textWriteOptionsController.delimiterName);
        }
        if (pdfFile != null && pdfFile.exists()) {
            PdfViewController.open(pdfFile);
        }
        if (htmlFile != null && htmlFile.exists()) {
            WebBrowserController.oneOpen(htmlFile);
        }
        if (dataClipboardFile != null && dataClipboardFile.exists()) {
            DataClipboardController.oneOpen();
        }
        if (xmlFile != null && xmlFile.exists()) {
            browse(xmlFile);
        }
        if (jsonFile != null && jsonFile.exists()) {
            browse(jsonFile);
        }

    }

}
