package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.operate.Data2DExport;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.JsonWriter;
import mara.mybox.data2d.writer.MyBoxClipboardWriter;
import mara.mybox.data2d.writer.XmlWriter;
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
            Data2DExport export;
            if (data2D == null) {
                export = new Data2DExport();
            } else {
                export = Data2DExport.create(data2D);
            }
            export.setIncludeRowNumber(rowNumberCheck.isSelected());
            export.setFormatValues(formatValuesCheck.isSelected());
            if (csvCheck.isSelected()) {
                Data2DWriter writer = pickCSVWriter();
                if (writer != null) {
                    export.addWriter(writer);
                } else {
                    return null;
                }
            }
            if (textsCheck.isSelected()) {
                Data2DWriter writer = pickTextWriter();
                if (writer != null) {
                    export.addWriter(writer);
                } else {
                    return null;
                }
            }
            if (excelCheck.isSelected()) {
                Data2DWriter writer = pickExcelWriter();
                if (writer != null) {
                    export.addWriter(writer);
                } else {
                    return null;
                }
            }
            if (pdfCheck.isSelected()) {
                Data2DWriter writer = pickPDFWriter();
                if (writer != null) {
                    export.addWriter(writer);
                } else {
                    return null;
                }
            }
            if (htmlCheck.isSelected()) {
                Data2DWriter writer = pickHtmlWriter();
                if (writer != null) {
                    export.addWriter(writer);
                } else {
                    return null;
                }
            }
            if (jsonCheck.isSelected()) {
                Data2DWriter writer = new JsonWriter();
                export.addWriter(writer);
            }
            if (xmlCheck.isSelected()) {
                Data2DWriter writer = new XmlWriter();
                export.addWriter(writer);
            }
            if (myBoxClipboardCheck.isSelected()) {
                Data2DWriter writer = new MyBoxClipboardWriter();
                export.addWriter(writer);
            }
            return export;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
