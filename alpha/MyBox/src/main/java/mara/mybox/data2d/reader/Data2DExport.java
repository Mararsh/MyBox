package mara.mybox.data2d.reader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseTaskController;
import mara.mybox.controller.Data2DManufactureController;
import mara.mybox.controller.DataInMyBoxClipboardController;
import mara.mybox.controller.JsonEditorController;
import mara.mybox.controller.PdfViewController;
import mara.mybox.controller.WebBrowserController;
import mara.mybox.controller.XmlEditorController;
import mara.mybox.data.PaginatedPdfTable;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2DTools;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.CSV;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.DatabaseTable;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Excel;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.HTML;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.JSON;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Matrix;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.MyBoxClipboard;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.PDF;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.SystemClipboard;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Text;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.XML;
import mara.mybox.data2d.Data2D_Edit;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.JsonTools;
import mara.mybox.tools.TextFileTools;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2022-2-25
 * @License Apache License Version 2.0
 */
public class Data2DExport extends Data2DOperator {

    protected BaseTaskController taskController;
    protected List<String> names;
    protected List<Data2DColumn> columns;
    protected boolean firstRow, rowNumber, skip,
            csv, texts, excel, myBoxClipboard, systemClipboard, matrix, table, pdf, html, xml, json,
            csvWithNames, textWithNames, excelWithNames;
    protected File csvFile, textFile, xmlFile, jsonFile, htmlFile, pdfFile, xlsxFile,
            myBoxClipboardFile, systemClipboardFile;
    protected Charset cvsCharset, textCharset;
    protected CSVPrinter dataClipboardPrinter;
    protected BufferedWriter textWriter, htmlWriter, xmlWriter, jsonWriter;
    protected XSSFWorkbook xssfBook;
    protected XSSFSheet xssfSheet;
    protected TargetType format;
    protected String indent = "    ", dataName, filePrefix,
            textDelimiter, csvDelimiter, css, sheetName;
    protected PaginatedPdfTable pdfTable;
    protected List<List<String>> pageRows;
    protected int maxLines, fileIndex, fileRowIndex, dataRowIndex;
    protected File targetPath, targetFile;
    protected Data2D csvData, excelData, textData, myBoxClipboadData, matrixData;

    public Data2DExport() {
        firstRow = csvWithNames = textWithNames = excelWithNames = true;
        rowNumber = formatValues = false;
        maxLines = -1;
    }

    public static Data2DExport create(Data2D_Edit data) {
        Data2DExport op = new Data2DExport();
        return op.setData(data) ? op : null;
    }

    @Override
    public boolean checkParameters() {
        return cols != null && !cols.isEmpty();
    }

    @Override
    public void handleRow() {
        try {
            List<String> row = new ArrayList<>();
            for (int col : cols) {
                if (col >= 0 && col < sourceRow.size()) {
                    row.add(sourceRow.get(col));
                } else {
                    row.add(null);
                }
            }
            if (row.isEmpty()) {
                return;
            }
            writeRow(row);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean end() {
        closeWriters();
        return true;
    }

    /*
        run task
     */
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
        myBoxClipboardFile = null;
        systemClipboardFile = null;

        csvData = null;
        excelData = null;
        textData = null;
        myBoxClipboadData = null;
        matrixData = null;

        firstRow = true;
        if (cvsCharset == null) {
            cvsCharset = Charset.forName("UTF-8");
        }
        if (csvDelimiter == null) {
            csvDelimiter = ",";
        }
        if (textCharset == null) {
            textCharset = Charset.forName("UTF-8");
        }
        if (textDelimiter == null) {
            textDelimiter = ",";
        }
        if (sheetName == null) {
            sheetName = "sheet1";
        }
        if (pdf && pdfTable == null) {
            pdfTable = PaginatedPdfTable.create();
        }
    }

    public boolean initParameters() {
        try {
            initWriters();

            format = null;
            names = null;
            pdfTable = null;
            initFiles();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public Data2DExport initParameters(TargetType inFormat) {
        initParameters();
        format = inFormat;
        if (format == null) {
            return this;
        }
        switch (format) {
            case CSV:
                setCsv(true);
                break;
            case Excel:
                setExcel(true);
                break;
            case Text:
                setTexts(true);
                break;
            case Matrix:
                setMatrix(true);
                break;
            case SystemClipboard:
                setSystemClipboard(true);
                break;
            case MyBoxClipboard:
                setMyBoxClipboard(true);
                break;
            case DatabaseTable:
                setTable(true);
                break;
            case JSON:
                setJson(true);
                break;
            case XML:
                setXml(true);
                break;
            case HTML:
                setHtml(true);
                break;
            case PDF:
                setPdf(true);
                break;
        }
        return this;
    }

    public boolean initFiles() {
        try {
            filePrefix = null;
            fileIndex = 1;
            fileRowIndex = dataRowIndex = 0;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean initPath(File path, List<Data2DColumn> cols, String prefix, boolean skip) {
        targetPath = path;
        columns = cols;
        names = new ArrayList<>();
        for (Data2DColumn c : columns) {
            names.add(c.getColumnName());
        }
        return openWriters(prefix, skip);
    }

    public boolean initFiles(File path, List<String> cols, String prefix, boolean skip) {
        targetPath = path;
        names = cols;
        return openWriters(prefix, skip);
    }

    public boolean openWriters(String prefix, boolean skip) {
        initFiles();
        filePrefix = prefix;
        this.skip = skip;
        if (rowNumber && columns != null) {
            columns.add(0, new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
        }
        if (rowNumber && names != null) {
            names.add(0, message("RowNumber"));
        }
        if (columns == null || columns.isEmpty()) {
            columns = Data2DTools.toColumns(names);
        }
        return openWriters();
    }

    public File makeTargetFile(String prefix, String suffix) {
        return targetFile != null ? targetFile
                : taskController.makeTargetFile(prefix, "." + suffix, targetPath);
    }

    public boolean openWriters() {
        try {
            initWriters();
            if (targetFile == null) {
                if (targetPath == null || filePrefix == null || names == null) {
                    updateLogs(message("InvalidParameters"));
                    return false;
                }
            }
            String currentPrefix = filePrefix;
            if (maxLines > 0) {
                currentPrefix += "_" + fileIndex;
            }
            if (csv || matrix || table) {
                csvFile = makeTargetFile(currentPrefix, "csv");
                if (csvFile != null) {
                    updateLogs(message("Writing") + " " + csvFile.getAbsolutePath());
                    csvPrinter = new CSVPrinter(new FileWriter(csvFile, cvsCharset), CsvTools.csvFormat(csvDelimiter));
                    if (csvWithNames) {
                        csvPrinter.printRecord(names);
                    }
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (texts) {
                textFile = makeTargetFile(currentPrefix, "txt");
                if (textFile != null) {
                    updateLogs(message("Writing") + " " + textFile.getAbsolutePath());
                    textWriter = new BufferedWriter(new FileWriter(textFile, textCharset));
                    if (textWithNames) {
                        TextFileTools.writeLine(task, textWriter, names, textDelimiter);
                    }
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (excel) {
                xlsxFile = makeTargetFile(currentPrefix, "xlsx");
                if (xlsxFile != null) {
                    updateLogs(message("Writing") + " " + xlsxFile.getAbsolutePath());
                    xssfBook = new XSSFWorkbook();
                    xssfSheet = xssfBook.createSheet(sheetName);
                    xssfSheet.setDefaultColumnWidth(20);
                    if (excelWithNames) {
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
            if (myBoxClipboard) {
                myBoxClipboardFile = DataClipboard.newFile();
                if (myBoxClipboardFile != null) {
                    updateLogs(message("Writing") + " " + myBoxClipboardFile.getAbsolutePath());
                    dataClipboardPrinter = new CSVPrinter(new FileWriter(myBoxClipboardFile,
                            Charset.forName("UTF-8")), CsvTools.csvFormat());
                    dataClipboardPrinter.printRecord(names);
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (html) {
                htmlFile = makeTargetFile(currentPrefix, "html");
                if (htmlFile != null) {
                    updateLogs(message("Writing") + " " + htmlFile.getAbsolutePath());
                    htmlWriter = new BufferedWriter(new FileWriter(htmlFile, Charset.forName("utf-8")));
                    StringBuilder s = new StringBuilder();
                    s.append("<!DOCTYPE html><HTML>\n").
                            append(indent).append("<HEAD>\n").
                            append(indent).append(indent).
                            append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
                    if (css != null && !css.isBlank()) {
                        s.append(indent).append(indent).append("<style type=\"text/css\">\n");
                        s.append(indent).append(indent).append(indent).append(css).append("\n");
                        s.append(indent).append(indent).append("</style>\n");
                    }
                    s.append(indent).append("</HEAD>\n").append(indent).append("<BODY>\n");
                    s.append(StringTable.tablePrefix(new StringTable(names)));
                    htmlWriter.write(s.toString());
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (xml) {
                xmlFile = makeTargetFile(currentPrefix, "xml");
                if (xmlFile != null) {
                    updateLogs(message("Writing") + " " + xmlFile.getAbsolutePath());
                    xmlWriter = new BufferedWriter(new FileWriter(xmlFile, Charset.forName("UTF-8")));
                    StringBuilder s = new StringBuilder();
                    s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                            .append("<Data>\n");
                    xmlWriter.write(s.toString());
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (json) {
                jsonFile = makeTargetFile(currentPrefix, "json");
                if (jsonFile != null) {
                    updateLogs(message("Writing") + " " + jsonFile.getAbsolutePath());
                    jsonWriter = new BufferedWriter(new FileWriter(jsonFile, Charset.forName("UTF-8")));
                    StringBuilder s = new StringBuilder();
                    s.append("{\"Data\": [\n");
                    jsonWriter.write(s.toString());
                } else if (skip) {
                    updateLogs(message("Skipped"));
                }
            }
            if (pdf && pdfTable != null) {
                pdfFile = makeTargetFile(currentPrefix, "pdf");
                if (pdfFile != null) {
                    updateLogs(message("Writing") + " " + pdfFile.getAbsolutePath());
                    pdfTable.setColumns(names).createDoc(pdfFile);
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

    public void writeRow(List<String> inRow) {
        try {
            if (inRow == null) {
                return;
            }

            if (maxLines > 0 && fileRowIndex >= maxLines) {
                closeWriters();
                fileIndex++;
                fileRowIndex = 0;
                Data2DExport.this.openWriters();
            }

            dataRowIndex++;
            if (rowNumber) {
                inRow.add(0, dataRowIndex + "");
            }
            List<String> row = inRow;
            if (formatValues) {
                row = new ArrayList<>();
                for (int i = 0; i < columns.size(); i++) {
                    String v = inRow.get(i);
                    if (v != null) {
                        v = columns.get(i).format(v);
                    }
                    row.add(v);
                }
            }

            if (csvPrinter != null) {
                csvPrinter.printRecord(row);
            }

            if (textWriter != null) {
                TextFileTools.writeLine(task, textWriter, row, textDelimiter);
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
                            .append("<![CDATA[").append(value).append("]]>")
                            .append("</Col>").append("\n");
                }
                s.append(indent).append("</Row>").append("\n");
                xmlWriter.write(s.toString());
            }
            if (jsonWriter != null) {
                StringBuilder s = new StringBuilder();
                if (firstRow) {
                    firstRow = false;
                } else {
                    s.append(",\n");
                }
                s.append(indent).append("{").append("\n");
                boolean firstData = true;
                for (int i = 0; i < names.size(); i++) {
                    String value = row.get(i);
                    if (value == null) {
                        continue;
                    }
                    if (!firstData) {
                        s.append(",\n");
                    } else {
                        firstData = false;
                    }
                    s.append(indent).append(indent)
                            .append("\"").append(names.get(i)).append("\": ")
                            .append(JsonTools.encode(value));
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
        try (Connection conn = DerbyBase.getConnection()) {
            if (csvPrinter != null && csvFile != null) {
                csvPrinter.flush();
                csvPrinter.close();
                if (taskController != null) {
                    taskController.targetFileGenerated(csvFile, VisitHistory.FileType.CSV);
                }
                csvPrinter = null;
                csvData = Data2D.create(Data2DDefinition.DataType.CSV);
                csvData.setTask(task).setFile(csvFile)
                        .setCharset(cvsCharset)
                        .setDelimiter(csvDelimiter)
                        .setHasHeader(csvWithNames)
                        .setDataName(dataName)
                        .setColsNumber(columns.size())
                        .setRowsNumber(dataRowIndex);
                Data2D.saveAttributes(conn, csvData, columns);
                conn.commit();

                if (matrix) {
                    matrixData = DataMatrix.toMatrix(task, conn, csvData, dataName);
                }
                if (systemClipboard) {
                    systemClipboardFile = csvFile;
                }

            }

            if (textWriter != null && textFile != null) {
                textWriter.flush();
                textWriter.close();
                if (taskController != null) {
                    taskController.targetFileGenerated(textFile, VisitHistory.FileType.Text);
                }
                textWriter = null;
                textData = Data2D.create(Data2DDefinition.DataType.Texts);
                textData.setTask(task).setFile(textFile)
                        .setCharset(textCharset)
                        .setDelimiter(textDelimiter)
                        .setHasHeader(textWithNames)
                        .setDataName(dataName)
                        .setColsNumber(columns.size())
                        .setRowsNumber(dataRowIndex);
                Data2D.saveAttributes(conn, textData, columns);
                conn.commit();
            }

            if (xssfBook != null && xssfSheet != null && xlsxFile != null) {
                for (int i = 0; i < names.size(); i++) {
                    xssfSheet.autoSizeColumn(i);
                }
                try (FileOutputStream fileOut = new FileOutputStream(xlsxFile)) {
                    xssfBook.write(fileOut);
                }
                xssfBook.close();
                if (taskController != null) {
                    taskController.targetFileGenerated(xlsxFile, VisitHistory.FileType.Excel);
                }
                xssfBook = null;
                excelData = Data2D.create(Data2DDefinition.DataType.Excel);
                excelData.setTask(task).setFile(xlsxFile).setSheet(sheetName)
                        .setHasHeader(excelWithNames)
                        .setDataName(dataName)
                        .setColsNumber(columns.size())
                        .setRowsNumber(dataRowIndex);
                Data2D.saveAttributes(conn, excelData, columns);
                conn.commit();
            }

            if (dataClipboardPrinter != null && myBoxClipboardFile != null) {
                dataClipboardPrinter.flush();
                dataClipboardPrinter.close();
                if (taskController != null) {
                    taskController.targetFileGenerated(myBoxClipboardFile, VisitHistory.FileType.CSV);
                }
                dataClipboardPrinter = null;
                myBoxClipboadData = Data2D.create(Data2DDefinition.DataType.MyBoxClipboard);
                myBoxClipboadData.setTask(task).setFile(myBoxClipboardFile)
                        .setCharset(Charset.forName("UTF-8"))
                        .setDelimiter(",")
                        .setHasHeader(true)
                        .setDataName(dataName)
                        .setColsNumber(columns.size())
                        .setRowsNumber(dataRowIndex);
                Data2D.saveAttributes(conn, myBoxClipboadData, columns);
                DataInMyBoxClipboardController.update();
                conn.commit();
            }

            if (htmlWriter != null && htmlFile != null) {
                htmlWriter.write(StringTable.tableSuffix(new StringTable(names)));
                htmlWriter.write(indent + "<BODY>\n</HTML>\n");
                htmlWriter.flush();
                htmlWriter.close();
                if (taskController != null) {
                    taskController.targetFileGenerated(htmlFile, VisitHistory.FileType.Html);
                }
                htmlWriter = null;
            }

            if (xmlWriter != null && xmlFile != null) {
                xmlWriter.write("</Data>\n");
                xmlWriter.flush();
                xmlWriter.close();
                if (taskController != null) {
                    taskController.targetFileGenerated(xmlFile, VisitHistory.FileType.XML);
                }
                xmlWriter = null;
            }

            if (jsonWriter != null && jsonFile != null) {
                jsonWriter.write("\n]}\n");
                jsonWriter.flush();
                jsonWriter.close();
                if (taskController != null) {
                    taskController.targetFileGenerated(jsonFile, VisitHistory.FileType.JSON);
                }
                jsonWriter = null;
            }

            if (pdfFile != null && pdfTable != null) {
                if (pageRows != null && !pageRows.isEmpty()) {
                    pdfTable.writePage(pageRows);
                    pageRows = null;
                }
                pdfTable.closeDoc();
                if (taskController != null) {
                    taskController.targetFileGenerated(pdfFile, VisitHistory.FileType.PDF);
                }
                pdfTable = null;
            }

            conn.close();
        } catch (Exception e) {
            updateLogs(e.toString());
            MyBoxLog.console(e.toString());
        }
    }

    public void updateLogs(String logs) {
        if (taskController != null) {
            taskController.updateLogs(logs);
        } else if (task != null) {
            task.setInfo(logs);
        }
    }

    public void openResults(BaseController controller) {
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                Platform.runLater(() -> {

                    if (excelData != null) {
                        Data2DManufactureController.openDef(excelData);
                    }
                    if (textData != null) {
                        Data2DManufactureController.openDef(textData);
                    }
                    if (myBoxClipboadData != null) {
                        DataInMyBoxClipboardController.open(myBoxClipboadData);
                    }
                    if (systemClipboardFile != null) {
                        TextClipboardTools.copyToSystemClipboard(controller,
                                TextFileTools.readTexts(null, systemClipboardFile));
                    }
                    if (matrixData != null) {
                        Data2DManufactureController.openDef(matrixData);
                    }
                    if (pdfFile != null && pdfFile.exists()) {
                        PdfViewController.open(pdfFile);
                    }
                    if (htmlFile != null && htmlFile.exists()) {
                        WebBrowserController.openFile(htmlFile);
                    }
                    if (xmlFile != null && xmlFile.exists()) {
                        XmlEditorController.open(xmlFile);
                    }
                    if (jsonFile != null && jsonFile.exists()) {
                        JsonEditorController.open(jsonFile);
                    }
                    if (csvData != null) {
                        if (csv) {
                            Data2DManufactureController.openDef(csvData);
                        } else {
                            csvData.drop();
                        }
                    }

                    initParameters();

                });
            }

        }, 1000);
    }

    /*
        set
     */
    public BaseTaskController getTaskController() {
        return taskController;
    }

    public void setTaskController(BaseTaskController taskController) {
        this.taskController = taskController;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<Data2DColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<Data2DColumn> columns) {
        this.columns = columns;
    }

    public boolean isFirstRow() {
        return firstRow;
    }

    public void setFirstRow(boolean firstRow) {
        this.firstRow = firstRow;
    }

    public boolean isRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(boolean rowNumber) {
        this.rowNumber = rowNumber;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean isCsv() {
        return csv;
    }

    public void setCsv(boolean csv) {
        this.csv = csv;
    }

    public boolean isTexts() {
        return texts;
    }

    public void setTexts(boolean texts) {
        this.texts = texts;
    }

    public boolean isMatrix() {
        return matrix;
    }

    public void setMatrix(boolean matrix) {
        this.matrix = matrix;
    }

    public boolean isTable() {
        return table;
    }

    public void setTable(boolean table) {
        this.table = table;
    }

    public boolean isPdf() {
        return pdf;
    }

    public void setPdf(boolean pdf) {
        this.pdf = pdf;
    }

    public boolean isSystemClipboard() {
        return systemClipboard;
    }

    public void setSystemClipboard(boolean systemClipboard) {
        this.systemClipboard = systemClipboard;
    }

    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

    public boolean isXml() {
        return xml;
    }

    public void setXml(boolean xml) {
        this.xml = xml;
    }

    public boolean isJson() {
        return json;
    }

    public void setJson(boolean json) {
        this.json = json;
    }

    public boolean isExcel() {
        return excel;
    }

    public void setExcel(boolean excel) {
        this.excel = excel;
    }

    public boolean isMyBoxClipboard() {
        return myBoxClipboard;
    }

    public void setMyBoxClipboard(boolean myBoxClipboard) {
        this.myBoxClipboard = myBoxClipboard;
    }

    public boolean isCsvWithNames() {
        return csvWithNames;
    }

    public void setCsvWithNames(boolean csvWithNames) {
        this.csvWithNames = csvWithNames;
    }

    public boolean isTextWithNames() {
        return textWithNames;
    }

    public void setTextWithNames(boolean textWithNames) {
        this.textWithNames = textWithNames;
    }

    public boolean isExcelWithNames() {
        return excelWithNames;
    }

    public void setExcelWithNames(boolean excelWithNames) {
        this.excelWithNames = excelWithNames;
    }

    public Charset getCvsCharset() {
        return cvsCharset;
    }

    public void setCvsCharset(Charset cvsCharset) {
        this.cvsCharset = cvsCharset;
    }

    public Charset getTextCharset() {
        return textCharset;
    }

    public void setTextCharset(Charset textCharset) {
        this.textCharset = textCharset;
    }

    public String getIndent() {
        return indent;
    }

    public void setIndent(String indent) {
        this.indent = indent;
    }

    public String getFilePrefix() {
        return filePrefix;
    }

    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    public String getTextDelimiter() {
        return textDelimiter;
    }

    public void setTextDelimiter(String textDelimiter) {
        this.textDelimiter = textDelimiter;
    }

    public String getCsvDelimiter() {
        return csvDelimiter;
    }

    public void setCsvDelimiter(String csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
    }

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public PaginatedPdfTable getPdfTable() {
        return pdfTable;
    }

    public void setPdfTable(PaginatedPdfTable pdfTable) {
        this.pdfTable = pdfTable;
    }

    public int getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    public File getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(File targetPath) {
        this.targetPath = targetPath;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(File targetFile) {
        this.targetFile = targetFile;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public Data2D getCsvData() {
        return csvData;
    }

}
