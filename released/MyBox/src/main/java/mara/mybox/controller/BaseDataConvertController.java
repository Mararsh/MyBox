package mara.mybox.controller;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.data.PaginatedPdfTable;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Append;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.CSV;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.DatabaseTable;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Excel;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.HTML;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Insert;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.JSON;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Matrix;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.MyBoxClipboard;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.PDF;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Replace;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Text;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.XML;
import mara.mybox.data2d.DataMatrix;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.DataFileCSVWriter;
import mara.mybox.data2d.writer.DataFileExcelWriter;
import mara.mybox.data2d.writer.DataFileTextWriter;
import mara.mybox.data2d.writer.DataMatrixWriter;
import mara.mybox.data2d.writer.DataTableWriter;
import mara.mybox.data2d.writer.HtmlWriter;
import mara.mybox.data2d.writer.JsonWriter;
import mara.mybox.data2d.writer.MyBoxClipboardWriter;
import mara.mybox.data2d.writer.PdfWriter;
import mara.mybox.data2d.writer.SystemClipboardWriter;
import mara.mybox.data2d.writer.XmlWriter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public class BaseDataConvertController extends BaseTaskController {

    @FXML
    protected TextArea cssArea;
    @FXML
    protected TextField widthList;
    @FXML
    protected ControlTextOptions csvWriteController;
    @FXML
    protected CheckBox excelWithNamesCheck, currentSheetOnlyCheck;
    @FXML
    protected ControlTextOptions textWriteOptionsController;
    @FXML
    protected ControlPdfWriteOptions pdfOptionsController;
    @FXML
    protected ControlMatrixOptions matrixOptionsController;

    public void initControls(String name) {
        baseName = name;
        initCSV();
        initExcel();
        initTexts();
        initMatrix();
        initPDF();
        initHtml();
    }

    private void initCSV() {
        if (csvWriteController != null) {
            csvWriteController.setControls(baseName + "CSVWrite", false, false);
        }
    }

    private void initExcel() {
        if (excelWithNamesCheck != null) {
            excelWithNamesCheck.setSelected(UserConfig.getBoolean(baseName + "ExcelTargetWithNames", true));
        }
        if (currentSheetOnlyCheck != null) {
            currentSheetOnlyCheck.setSelected(UserConfig.getBoolean(baseName + "ExcelCurrentSheetOnly", false));
        }
    }

    private void initTexts() {
        if (textWriteOptionsController != null) {
            textWriteOptionsController.setControls(baseName + "TextWrite", false, true);
        }
    }

    private void initMatrix() {
        if (matrixOptionsController != null) {
            matrixOptionsController.setParameters(baseName);
        }
    }

    private void initHtml() {
        if (cssArea != null) {
            cssArea.setText(UserConfig.getString(baseName + "Css", HtmlStyles.TableStyle));
        }
    }

    private void initPDF() {
        if (pdfOptionsController != null) {
            pdfOptionsController.set(baseName, false);
            pdfOptionsController.pixSizeRadio.setDisable(true);
            pdfOptionsController.standardSizeRadio.setSelected(true);
        }
    }

    public DataFileCSVWriter pickCSVWriter() {
        try {
            DataFileCSVWriter writer = new DataFileCSVWriter();
            if (csvWriteController != null) {
                if (csvWriteController.invalidDelimiter()) {
                    popError(message("InvalidParameter") + ": " + message("Delimiter"));
                    return null;
                }
                writer.setCharset(csvWriteController.getCharset())
                        .setDelimiter(csvWriteController.getDelimiterName())
                        .setWriteHeader(csvWriteController.withName());
            }

            return writer;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataFileExcelWriter pickExcelWriter() {
        try {
            DataFileExcelWriter writer = new DataFileExcelWriter();
            if (excelWithNamesCheck != null) {
                UserConfig.setBoolean(baseName + "ExcelTargetWithNames", excelWithNamesCheck.isSelected());
                writer.setWriteHeader(excelWithNamesCheck.isSelected());
            }
            if (currentSheetOnlyCheck != null) {
                UserConfig.setBoolean(baseName + "ExcelCurrentSheetOnly", currentSheetOnlyCheck.isSelected());
                writer.setCurrentSheetOnly(currentSheetOnlyCheck.isSelected());
            }

            return writer;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataFileTextWriter pickTextWriter() {
        try {
            DataFileTextWriter writer = new DataFileTextWriter();
            if (textWriteOptionsController != null) {
                if (textWriteOptionsController.invalidDelimiter()) {
                    popError(message("InvalidParameter") + ": " + message("Delimiter"));
                    return null;
                }
                writer.setCharset(textWriteOptionsController.getCharset())
                        .setDelimiter(textWriteOptionsController.getDelimiterName())
                        .setWriteHeader(textWriteOptionsController.withName());
            }

            return writer;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public DataMatrixWriter pickMatrixWriter() {
        try {
            DataMatrixWriter writer = new DataMatrixWriter();
            String type = matrixOptionsController != null
                    ? matrixOptionsController.pickType() : "Double";
            writer.setDataType(type)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(DataMatrix.MatrixDelimiter)
                    .setWriteHeader(false);
            return writer;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public PdfWriter pickPDFWriter() {
        try {
            PdfWriter writer = new PdfWriter();
            if (pdfOptionsController != null) {
                if (!pdfOptionsController.pickValues()) {
                    return null;
                }
                List<Integer> columnWidths = new ArrayList<>();
                String w = widthList.getText();
                if (w != null && !w.isBlank()) {
                    String[] values = w.split(",");
                    for (String value : values) {
                        try {
                            int v = Integer.parseInt(value.trim());
                            if (v > 0) {
                                columnWidths.add(v);
                            }
                        } catch (Exception e) {
                        }
                    }
                }

                writer.setPdfTable(PaginatedPdfTable.create()
                        .setPageSize(new PDRectangle(pdfOptionsController.pageWidth, pdfOptionsController.pageHeight))
                        .setTtf(pdfOptionsController.getTtfFile())
                        .setFontSize(pdfOptionsController.fontSize)
                        .setMargin(pdfOptionsController.marginSize)
                        .setColumnWidths(columnWidths)
                        .setDefaultZoom(pdfOptionsController.zoom)
                        .setHeader(pdfOptionsController.getHeader())
                        .setFooter(pdfOptionsController.getFooter())
                        .setShowPageNumber(pdfOptionsController.showPageNumber));
            }

            return writer;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public HtmlWriter pickHtmlWriter() {
        try {
            HtmlWriter writer = new HtmlWriter();
            if (cssArea != null) {
                String css = cssArea.getText();
                UserConfig.setString(baseName + "Css", css);
                writer.setCss(css);
            }

            return writer;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public Data2DWriter pickWriter(TargetType format) {
        try {
            if (format == null) {
                return null;
            }
            Data2DWriter writer = null;
            switch (format) {
                case CSV:
                    writer = pickCSVWriter();
                    break;
                case Excel:
                    writer = pickExcelWriter();
                    break;
                case Text:
                    writer = pickTextWriter();
                    break;
                case Matrix:
                    writer = pickMatrixWriter();
                    break;
                case DatabaseTable:
                    writer = new DataTableWriter();
                    break;
                case MyBoxClipboard:
                    writer = new MyBoxClipboardWriter();
                    break;
                case SystemClipboard:
                    writer = new SystemClipboardWriter();
                    break;
                case HTML:
                    writer = pickHtmlWriter();
                    break;
                case PDF:
                    writer = pickPDFWriter();
                    break;
                case JSON:
                    writer = new JsonWriter();
                    break;
                case XML:
                    writer = new XmlWriter();
                    break;
                case Replace:
                case Insert:
                case Append:
                    writer = new SystemClipboardWriter();
                    break;
            }
            if (writer != null) {

            }
            return writer;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
