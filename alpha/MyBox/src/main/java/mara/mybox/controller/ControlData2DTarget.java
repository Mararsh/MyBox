package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.CSV;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Excel;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.HTML;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.JSON;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.MyBoxClipboard;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.PDF;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Text;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.XML;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTmpTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-11
 * @License Apache License Version 2.0
 */
public class ControlData2DTarget extends BaseController {
    
    protected BaseData2DLoadController tableController;
    protected TargetType format;
    protected boolean notInTable;
    protected ChangeListener<Boolean> tableStatusListener;
    protected SimpleBooleanProperty formatNotify;
    
    @FXML
    protected ToggleGroup targetGroup;
    @FXML
    protected RadioButton csvRadio, excelRadio, textsRadio, matrixRadio, databaseRadio,
            jsonRadio, xmlRadio, htmlRadio, pdfRadio,
            systemClipboardRadio, myBoxClipboardRadio, replaceRadio, insertRadio, appendRadio;
    @FXML
    protected ComboBox<String> rowSelector, colSelector;
    @FXML
    protected VBox externalBox, externalDefBox, fileBox, inTableBox;
    @FXML
    protected HBox dataNameBox, locationBox;
    @FXML
    protected TextField nameInput;
    
    public void setParameters(BaseController parent, BaseData2DLoadController controller) {
        try {
            baseName = parent.baseName + "_" + baseName;
            tableController = controller;
            formatNotify = new SimpleBooleanProperty(false);
            
            checkControls();
            targetGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkTarget();
                }
            });
            
            if (tableController != null) {
                refreshControls();
                tableStatusListener = new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        refreshControls();
                    }
                };
                tableController.statusNotify.addListener(tableStatusListener);
            }
            initTarget();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }
    
    public void checkControls() {
        if (inTableBox != null) {
            if (notInTable) {
                if (thisPane.getChildren().contains(inTableBox)) {
                    thisPane.getChildren().remove(inTableBox);
                }
            } else {
                if (!thisPane.getChildren().contains(inTableBox)) {
                    thisPane.getChildren().add(2, inTableBox);
                }
            }
        }
        if (inTable()) {
            if (!inTableBox.getChildren().contains(locationBox)) {
                inTableBox.getChildren().add(locationBox);
            }
            if (externalBox != null) {
                if (externalBox.getChildren().contains(externalDefBox)) {
                    externalBox.getChildren().remove(externalDefBox);
                }
            }
        } else {
            if (inTableBox != null && inTableBox.getChildren().contains(locationBox)) {
                inTableBox.getChildren().remove(locationBox);
            }
            if (externalBox != null) {
                if (externalBox.getChildren().contains(externalDefBox)) {
                    externalBox.getChildren().remove(externalDefBox);
                }
            }
            if (matrixRadio.isSelected()
                    || systemClipboardRadio.isSelected()
                    || myBoxClipboardRadio.isSelected()
                    || databaseRadio.isSelected()) {
                if (externalDefBox.getChildren().contains(fileBox)) {
                    externalDefBox.getChildren().remove(fileBox);
                }
            } else {
                if (!externalDefBox.getChildren().contains(fileBox)) {
                    externalDefBox.getChildren().add(fileBox);
                }
            }
        }
    }
    
    public void initTarget() {
        try {
            format = TargetType.valueOf(UserConfig.getString(baseName + "DataTarget", "CSV"));
            isSettingValues = true;
            if (format == null) {
                csvRadio.setSelected(true);
            } else {
                switch (format) {
                    case CSV:
                        csvRadio.setSelected(true);
                        break;
                    case Excel:
                        excelRadio.setSelected(true);
                        break;
                    case Text:
                        textsRadio.setSelected(true);
                        break;
                    case Matrix:
                        matrixRadio.setSelected(true);
                        break;
                    case SystemClipboard:
                        systemClipboardRadio.setSelected(true);
                        break;
                    case MyBoxClipboard:
                        myBoxClipboardRadio.setSelected(true);
                        break;
                    case DatabaseTable:
                        databaseRadio.setSelected(true);
                        break;
                    case JSON:
                        jsonRadio.setSelected(true);
                        break;
                    case XML:
                        xmlRadio.setSelected(true);
                        break;
                    case HTML:
                        htmlRadio.setSelected(true);
                        break;
                    case PDF:
                        pdfRadio.setSelected(true);
                        break;
                    case Append:
                        if (notInTable && inTableBox != null) {
                            appendRadio.setSelected(true);
                        } else {
                            csvRadio.setSelected(true);
                        }
                        break;
                    case Insert:
                        if (notInTable && inTableBox != null) {
                            insertRadio.setSelected(true);
                        } else {
                            csvRadio.setSelected(true);
                        }
                        break;
                    case Replace:
                        if (notInTable && inTableBox != null) {
                            replaceRadio.setSelected(true);
                        } else {
                            csvRadio.setSelected(true);
                        }
                        break;
                    default:
                        csvRadio.setSelected(true);
                }
            }
            isSettingValues = false;
            checkTarget();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }
    
    public TargetType checkTarget() {
        try {
            format = TargetType.CSV;
            if (isSettingValues || tableController == null || tableController.data2D == null) {
                return format;
            }
            String name = name();
            if (name == null || name.isBlank()) {
                name = tableController.data2D.getDataName();
            }
            if (name == null || name.isBlank()) {
                name = tableController.getTitle();
            }
            if (csvRadio.isSelected()) {
                format = TargetType.CSV;
                targetFileController.setFile(FileType.CSV,
                        baseName + "TargetType" + FileType.CSV, name, "csv");
            } else if (excelRadio.isSelected()) {
                format = TargetType.Excel;
                targetFileController.setFile(FileType.Excel,
                        baseName + "TargetType" + FileType.Excel, name, "xlsx");
            } else if (textsRadio.isSelected()) {
                format = TargetType.Text;
                targetFileController.setFile(FileType.Text,
                        baseName + "TargetType" + FileType.Text, name, "txt");
            } else if (matrixRadio.isSelected()) {
                format = TargetType.Matrix;
            } else if (systemClipboardRadio.isSelected()) {
                format = TargetType.SystemClipboard;
            } else if (myBoxClipboardRadio.isSelected()) {
                format = TargetType.MyBoxClipboard;
            } else if (databaseRadio.isSelected()) {
                format = TargetType.DatabaseTable;
            } else if (jsonRadio.isSelected()) {
                format = TargetType.JSON;
                targetFileController.setFile(FileType.JSON,
                        baseName + "TargetType" + FileType.JSON, name, "json");
            } else if (xmlRadio.isSelected()) {
                format = TargetType.XML;
                targetFileController.setFile(FileType.XML,
                        baseName + "TargetType" + FileType.XML, name, "xml");
            } else if (htmlRadio.isSelected()) {
                format = TargetType.HTML;
                targetFileController.setFile(FileType.Html,
                        baseName + "TargetType" + FileType.Html, name, "html");
            } else if (pdfRadio.isSelected()) {
                format = TargetType.PDF;
                targetFileController.setFile(FileType.PDF,
                        baseName + "TargetType" + FileType.PDF, name, "pdf");
            } else if (inTableBox != null) {
                if (replaceRadio.isSelected()) {
                    if (!notInTable) {
                        format = TargetType.Replace;
                    } else {
                        format = TargetType.CSV;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                csvRadio.setSelected(true);
                            }
                        });
                    }
                } else if (insertRadio.isSelected()) {
                    if (!notInTable) {
                        format = TargetType.Insert;
                    } else {
                        format = TargetType.CSV;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                csvRadio.setSelected(true);
                            }
                        });
                    }
                } else if (appendRadio.isSelected()) {
                    if (!notInTable) {
                        format = TargetType.Append;
                    } else {
                        format = TargetType.CSV;
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                csvRadio.setSelected(true);
                            }
                        });
                    }
                }
            }
            checkControls();
            UserConfig.setString(baseName + "DataTarget", format.name());
            formatNotify.set(!formatNotify.get());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return format;
    }
    
    public synchronized void refreshControls() {
        try {
            if (tableController == null || tableController.data2D == null) {
                return;
            }
            nameInput.setText(tableController.data2D.dataName());
            if (rowSelector == null) {
                return;
            }
            int thisSelect = rowSelector.getSelectionModel().getSelectedIndex();
            List<String> rows = new ArrayList<>();
            if (tableController.tableData != null) {
                for (long i = 0; i < tableController.tableData.size(); i++) {
                    rows.add("" + (i + 1));
                }
            }
            rowSelector.getItems().setAll(rows);
            rowSelector.getSelectionModel().select(thisSelect >= 0 ? thisSelect : 0);
            
            String selectedCol = colSelector.getSelectionModel().getSelectedItem();
            if (tableController.data2D.getColumns() != null) {
                List<String> names = tableController.data2D.columnNames();
                for (Data2DColumn column : tableController.data2D.getColumns()) {
                    if (!column.isId()) {
                        names.add(column.getColumnName());
                    }
                }
                colSelector.getItems().setAll(names);
                if (selectedCol != null) {
                    colSelector.setValue(selectedCol);
                } else {
                    colSelector.getSelectionModel().select(0);
                }
            } else {
                colSelector.getItems().clear();
            }
            
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }
    
    public void setNotInTable(boolean notInTable) {
        this.notInTable = notInTable;
        checkTarget();
    }
    
    public boolean inTable() {
        return !notInTable && inTableBox != null
                && (insertRadio.isSelected() || appendRadio.isSelected() || replaceRadio.isSelected());
    }
    
    public boolean validateTarget() {
        try {
            if (format == null) {
                return false;
            }
            switch (format) {
                case CSV:
                case Excel:
                case Text:
                case JSON:
                case XML:
                case HTML:
                case PDF:
                    File file = file();
                    if (file == null) {
                        popError(message("InvalidParameter") + ": " + message("FileName"));
                        return false;
                    } else {
                        return true;
                    }
//                case Matrix:
//                case DatabaseTable:
//                    if (name() == null) {
//                        popError(message("InvalidParameter") + ": " + message("DataName"));
//                        return false;
//                    } else {
//                        return true;
//                    }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }
    
    public TargetType format() {
        return format;
    }
    
    public String name() {
        String name = nameInput.getText();
        return name != null && !name.isBlank() ? name.trim() : null;
    }
    
    public File file() {
        if (targetFileController == null) {
            return FileTmpTools.getTempFile();
        } else {
            return targetFileController.makeTargetFile();
        }
    }
    
    public int row() {
        if (!inTable()) {
            return -1;
        }
        return rowSelector.getSelectionModel().getSelectedIndex();
    }
    
    public int col() {
        if (!inTable()) {
            return -1;
        }
        return tableController.data2D.colOrder(colSelector.getSelectionModel().getSelectedItem());
    }
    
    public Data2DWriter pickWriter() {
        try {
            if (format == null) {
                return null;
            }
            Data2DWriter writer = Data2DWriter.getWriter(format);
            if (writer != null) {
                writer.setController(this)
                        .setDataName(name())
                        .setTargetFile(file());
            }
            return writer;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
    
    @Override
    public void cleanPane() {
        try {
            if (tableController != null) {
                tableController.statusNotify.removeListener(tableStatusListener);
            }
            tableStatusListener = null;
            tableController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }
    
}
