package mara.mybox.controller;

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
import mara.mybox.data.PaginatedPdfTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.reader.Data2DExport;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.style.HtmlStyles;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public class ControlDataConvert extends BaseController {

    protected BaseTaskController parent;
    protected int maxLines;

    @FXML
    protected FlowPane formatsPane;
    @FXML
    protected ComboBox<String> maxLinesSelector;
    @FXML
    protected CheckBox csvCheck, textsCheck, pdfCheck, htmlCheck, xmlCheck, jsonCheck, excelCheck,
            myBoxClipboardCheck, rowNumberCheck, excelWithNamesCheck, formatValuesCheck;
    @FXML
    protected TextArea cssArea;
    @FXML
    protected TextField widthList;
    @FXML
    protected ControlTextOptions csvWriteController;
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
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "CSV", csvCheck.isSelected());
            }
        });

        textsCheck.setSelected(UserConfig.getBoolean(baseName + "Text", true));
        textsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Text", textsCheck.isSelected());
            }
        });

        jsonCheck.setSelected(UserConfig.getBoolean(baseName + "Json", false));
        jsonCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Json", jsonCheck.isSelected());
            }
        });

        xmlCheck.setSelected(UserConfig.getBoolean(baseName + "Xml", false));
        xmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Xml", xmlCheck.isSelected());
            }
        });

        excelCheck.setSelected(UserConfig.getBoolean(baseName + "Xlsx", false));
        excelCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Xlsx", excelCheck.isSelected());
            }
        });

        htmlCheck.setSelected(UserConfig.getBoolean(baseName + "Html", false));
        htmlCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "Html", htmlCheck.isSelected());
            }
        });

        pdfCheck.setSelected(UserConfig.getBoolean(baseName + "PDF", false));
        pdfCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "PDF", pdfCheck.isSelected());
            }
        });

        myBoxClipboardCheck.setSelected(UserConfig.getBoolean(baseName + "DataClipboard", false));
        myBoxClipboardCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "DataClipboard", myBoxClipboardCheck.isSelected());
            }
        });
    }

    private void initCSV() {
        csvWriteController.setControls(baseName + "CSVWrite", false, false);
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
        textWriteOptionsController.setControls(baseName + "TextWrite", false, true);
    }

    private void initHtml() {
        cssArea.setText(UserConfig.getString(baseName + "Css", HtmlStyles.DefaultStyle));
    }

    private void initPDF() {
        if (pdfOptionsController != null) {
            pdfOptionsController.set(baseName, false);
        }
        pdfOptionsController.pixSizeRadio.setDisable(true);
        pdfOptionsController.standardSizeRadio.setSelected(true);
    }

    private void initOthers() {
        try {
            rowNumberCheck.setSelected(UserConfig.getBoolean(baseName + "TargetWithRowNumber", false));
            rowNumberCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "TargetWithRowNumber", rowNumberCheck.isSelected());
                }
            });

            maxLines = -1;
            maxLinesSelector.getItems().addAll(Arrays.asList(message("NotSplit"),
                    "1000", "500", "200", "300", "800", "2000", "3000", "5000", "8000"
            ));
            maxLinesSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        if (isSettingValues || newValue == null || newValue.isEmpty()) {
                            return;
                        }
                        UserConfig.setString(baseName + "ExportMaxLines", newValue);
                        if (message("NotSplit").equals(newValue)) {
                            maxLines = -1;
                            ValidationTools.setEditorNormal(maxLinesSelector);
                            return;
                        }
                        try {
                            int v = Integer.parseInt(newValue);
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
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void selectAllAction() {
        csvCheck.setSelected(true);
        textsCheck.setSelected(true);
        pdfCheck.setSelected(true);
        htmlCheck.setSelected(true);
        xmlCheck.setSelected(true);
        jsonCheck.setSelected(true);
        excelCheck.setSelected(true);
        myBoxClipboardCheck.setSelected(true);
    }

    @FXML
    @Override
    public void selectNoneAction() {
        csvCheck.setSelected(false);
        textsCheck.setSelected(false);
        pdfCheck.setSelected(false);
        htmlCheck.setSelected(false);
        xmlCheck.setSelected(false);
        jsonCheck.setSelected(false);
        excelCheck.setSelected(false);
        myBoxClipboardCheck.setSelected(false);
    }

    /*
        run task
     */
    public boolean checkParameters() {
        try {
            if (csvCheck.isSelected()) {
                if (csvWriteController.delimiterController.delimiterInput.getStyle().equals(UserConfig.badStyle())) {
                    return false;
                }
            }
            if (textsCheck.isSelected()) {
                if (textWriteOptionsController.delimiterController.delimiterInput.getStyle().equals(UserConfig.badStyle())) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public Data2DExport pickParameters(Data2D data2D) {
        try {
            Data2DExport export = data2D != null
                    ? Data2DExport.create(data2D) : new Data2DExport();
            export.initParameters();
            if (parent != null) {
                export.setTargetPath(parent.targetPath);
            }
            export.setRowNumber(rowNumberCheck.isSelected());
            export.setFormatValues(formatValuesCheck.isSelected());
            export.setCsv(csvCheck.isSelected());
            export.setTexts(textsCheck.isSelected());
            export.setExcel(excelCheck.isSelected());
            export.setHtml(htmlCheck.isSelected());
            export.setJson(jsonCheck.isSelected());
            export.setXml(xmlCheck.isSelected());
            export.setPdf(pdfCheck.isSelected());
            export.setMyBoxClipboard(myBoxClipboardCheck.isSelected());
            if (csvCheck.isSelected()) {
                if (csvWriteController.delimiterController.delimiterInput.getStyle().equals(UserConfig.badStyle())) {
                    return null;
                }
                export.setCvsCharset(csvWriteController.getCharset());
                export.setCsvDelimiter(csvWriteController.getDelimiterName());
                export.setCsvWithNames(csvWriteController.withName());
            }
            if (textsCheck.isSelected()) {
                if (textWriteOptionsController.delimiterController.delimiterInput.getStyle().equals(UserConfig.badStyle())) {
                    return null;
                }
                export.setCvsCharset(textWriteOptionsController.getCharset());
                export.setCsvDelimiter(textWriteOptionsController.getDelimiterName());
                export.setCsvWithNames(textWriteOptionsController.withName());
            }
            if (excelCheck.isSelected()) {
                export.setExcelWithNames(excelWithNamesCheck.isSelected());
            }
            if (pdfCheck.isSelected()) {
                List<Integer> columnWidths = new ArrayList<>();
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
                export.setPdfTable(PaginatedPdfTable.create()
                        .setPageSize(new PDRectangle(pdfOptionsController.pageWidth, pdfOptionsController.pageHeight))
                        .setTtf(pdfOptionsController.getTtfFile())
                        .setFontSize(pdfOptionsController.fontSize)
                        .setMargin(pdfOptionsController.marginSize)
                        .setColumnWidths(columnWidths)
                        .setDefaultZoom(pdfOptionsController.zoom)
                        .setHeader(pdfOptionsController.getHeader())
                        .setShowPageNumber(pdfOptionsController.showPageNumber));
            }
            if (htmlCheck.isSelected()) {
                UserConfig.setString(baseName + "Css", cssArea.getText());
            }
            return export;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
