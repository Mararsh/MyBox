package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.reader.Data2DExport;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public class ControlDataExport extends BaseDataConvertController {

    protected BaseTaskController taskController;
    protected int maxLines;

    @FXML
    protected FlowPane formatsPane;
    @FXML
    protected ComboBox<String> maxLinesSelector;
    @FXML
    protected CheckBox csvCheck, textsCheck, pdfCheck, htmlCheck, xmlCheck, jsonCheck, excelCheck,
            myBoxClipboardCheck, rowNumberCheck, formatValuesCheck;

    public void setParameters(BaseTaskController parent) {
        this.taskController = parent;
        initControls(taskController.baseName + "_" + baseName);
        initChecks();
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

    public Data2DExport pickParameters(Data2D data2D) {
        try {
            if (data2D == null) {
                return null;
            }
            Data2DExport export = Data2DExport.create(data2D);
            export.initParameters();
            if (taskController != null) {
                export.setTargetPath(taskController.targetPath);
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
                if (!pickCSV(export)) {
                    return null;
                }
            }
            if (textsCheck.isSelected()) {
                if (!pickText(export)) {
                    return null;
                }
            }
            if (excelCheck.isSelected()) {
                if (!pickExcel(export)) {
                    return null;
                }
            }
            if (pdfCheck.isSelected()) {
                if (!pickPDF(export)) {
                    return null;
                }
            }
            if (htmlCheck.isSelected()) {
                if (!pickHtml(export)) {
                    return null;
                }
            }
            return export;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
