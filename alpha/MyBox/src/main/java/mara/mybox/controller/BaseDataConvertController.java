package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.data.PaginatedPdfTable;
import mara.mybox.data2d.operate.Data2DExport;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.HtmlStyles;
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
    protected CheckBox excelWithNamesCheck;
    @FXML
    protected ControlTextOptions textWriteOptionsController;
    @FXML
    protected ControlPdfWriteOptions pdfOptionsController;

    public void initControls(String name) {
        baseName = name + "_" + baseName;
        initCSV();
        initExcel();
        initTexts();
        initPDF();
        initHtml();
    }

    private void initCSV() {
        csvWriteController.setControls(baseName + "CSVWrite", false, false);
    }

    private void initExcel() {
        excelWithNamesCheck.setSelected(UserConfig.getBoolean(baseName + "ExcelTargetWithNames", true));
    }

    private void initTexts() {
        textWriteOptionsController.setControls(baseName + "TextWrite", false, true);
    }

    private void initHtml() {
        cssArea.setText(UserConfig.getString(baseName + "Css", HtmlStyles.TableStyle));
    }

    private void initPDF() {
        if (pdfOptionsController != null) {
            pdfOptionsController.set(baseName, false);
        }
        pdfOptionsController.pixSizeRadio.setDisable(true);
        pdfOptionsController.standardSizeRadio.setSelected(true);
    }

    public boolean pickCSV(Data2DExport export) {
        try {
            if (export == null) {
                return false;
            }
            if (csvWriteController.delimiterController.delimiterInput.getStyle().equals(UserConfig.badStyle())) {
                return false;
            }
            export.setCvsCharset(csvWriteController.getCharset());
            export.setCsvDelimiter(csvWriteController.getDelimiterName());
            export.setCsvWithNames(csvWriteController.withName());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickExcel(Data2DExport export) {
        try {
            if (export == null) {
                return false;
            }
            UserConfig.setBoolean(baseName + "ExcelTargetWithNames", excelWithNamesCheck.isSelected());
            export.setExcelWithNames(excelWithNamesCheck.isSelected());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickText(Data2DExport export) {
        try {
            if (export == null) {
                return false;
            }
            if (textWriteOptionsController.delimiterController.delimiterInput.getStyle().equals(UserConfig.badStyle())) {
                return false;
            }
            export.setCvsCharset(textWriteOptionsController.getCharset());
            export.setCsvDelimiter(textWriteOptionsController.getDelimiterName());
            export.setCsvWithNames(textWriteOptionsController.withName());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickPDF(Data2DExport export) {
        try {
            if (export == null) {
                return false;
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
            export.setPdfTable(PaginatedPdfTable.create()
                    .setPageSize(new PDRectangle(pdfOptionsController.pageWidth, pdfOptionsController.pageHeight))
                    .setTtf(pdfOptionsController.getTtfFile())
                    .setFontSize(pdfOptionsController.fontSize)
                    .setMargin(pdfOptionsController.marginSize)
                    .setColumnWidths(columnWidths)
                    .setDefaultZoom(pdfOptionsController.zoom)
                    .setHeader(pdfOptionsController.getHeader())
                    .setShowPageNumber(pdfOptionsController.showPageNumber));
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickHtml(Data2DExport export) {
        try {
            if (export == null) {
                return false;
            }
            String css = cssArea.getText();
            UserConfig.setString(baseName + "Css", css);
            export.setCss(css);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
