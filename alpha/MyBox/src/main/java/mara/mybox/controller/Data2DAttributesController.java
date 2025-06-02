package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.FileBackup;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-16
 * @License Apache License Version 2.0
 */
public class Data2DAttributesController extends BaseChildController {

    protected Data2DManufactureController dataController;
    protected TableData2DDefinition tableData2DDefinition;
    protected TableData2DColumn tableData2DColumn;
    protected Data2D data2D;
    protected ObservableList<List<String>> tableData;
    protected String dataName;
    protected short scale;
    protected int maxRandom;
    protected boolean attributesChanged, columnsChanged;

    @FXML
    protected ControlData2DColumns columnsController;
    @FXML
    protected TextArea descInput;
    @FXML
    protected TextField idInput, timeInput, dataTypeInput, dataNameInput;
    @FXML
    protected ComboBox<String> scaleSelector, randomSelector;
    @FXML
    protected WebView webView;
    @FXML
    protected Tab attributesTab, columnsTab;

    public Data2DAttributesController() {
        baseTitle = message("DataDefinition");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            scale = (short) UserConfig.getInt(baseName + "Scale", 2);
            if (scale < 0) {
                scale = 2;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.setValue(scale + "");

            maxRandom = UserConfig.getInt(baseName + "MaxRandom", 100000);
            if (maxRandom < 0) {
                maxRandom = 100000;
            }
            randomSelector.getItems().addAll(Arrays.asList("1", "100", "10", "1000", "10000", "1000000", "10000000"));
            randomSelector.setValue(maxRandom + "");

            dataNameInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    attributesChanged(true);
                }
            });

            descInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    attributesChanged(true);
                }
            });

            scaleSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    attributesChanged(true);
                }
            });

            randomSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    attributesChanged(true);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean isInvalid() {
        return dataController == null
                || !dataController.isShowing()
                || dataController.invalidData();
    }

    protected void setParameters(Data2DManufactureController controller) {
        try {
            dataController = controller;
            if (isInvalid()) {
                close();
                return;
            }
            loadValues();

            dataController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                    loadValues();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadValues() {
        try {
            if (isInvalid()) {
                close();
                return;
            }
            tableData2DDefinition = dataController.tableData2DDefinition;
            tableData2DColumn = dataController.tableData2DColumn;
            tableData = dataController.tableData;
            data2D = dataController.data2D.cloneAll();
            columnsController.setParameters(this);

            isSettingValues = true;
            if (idInput != null) {
                idInput.setText(data2D.getDataID() >= 0 ? data2D.getDataID() + "" : message("NewData"));
            }
            if (timeInput != null) {
                timeInput.setText(DateTools.datetimeToString(data2D.getModifyTime()));
            }
            if (dataTypeInput != null) {
                dataTypeInput.setText(data2D.getTypeName());
            }
            dataNameInput.setText(data2D.getDataName());
            scaleSelector.setValue(data2D.getScale() + "");
            randomSelector.setValue(data2D.getMaxRandom() + "");
            descInput.setText(data2D.getComments());
            if (webView != null) {
                webView.getEngine().loadContent(HtmlWriteTools.table(data2D.pageInfo()));
            }
            attributesChanged(false);
            columnsChanged(false);
            isSettingValues = false;
            checkStatus();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void attributesChanged(boolean changed) {
        attributesChanged = changed;
        checkStatus();
    }

    public void columnsChanged(boolean changed) {
        columnsChanged = changed;
        checkStatus();
    }

    public void checkStatus() {
        if (isSettingValues) {
            return;
        }
        if (attributesChanged) {
            attributesTab.setText(message("Attributes") + "*");
        } else {
            attributesTab.setText(message("Attributes"));
        }
        if (columnsChanged) {
            columnsTab.setText(message("Columns") + "*");
        } else {
            columnsTab.setText(message("Columns"));
        }
        String title = baseTitle + " - " + data2D.displayName();
        if (columnsChanged || attributesChanged) {
            title += " *";
        }
        setTitle(title);
    }

    public Data2D pickValues() {
        if (isInvalid()) {
            close();
            return null;
        }
        String name = dataNameInput.getText();
//        if (name == null || name.isBlank()) {
//            popError(message("InvalidParameter") + ": " + message("DataName"));
//            return false;
//        }
        dataName = name;

        int v = -1;
        try {
            v = Integer.parseInt(scaleSelector.getValue());
        } catch (Exception e) {
        }
        if (v >= 0 && v <= 15) {
            scale = (short) v;
        } else {
            popError(message("InvalidParameter") + ": " + message("DecimalScale"));
            return null;
        }

        v = -1;
        try {
            v = Integer.parseInt(randomSelector.getValue());
        } catch (Exception e) {
        }
        if (v > 0) {
            maxRandom = v;
        } else {
            popError(message("InvalidParameter") + ": " + message("MaxRandom"));
            return null;
        }

        List<Data2DColumn> columns = columnsController.pickColumns();
        if (columns == null || columns.isEmpty()) {
            popError(message("DataColumnsShouldNotEmpty"));
            return null;
        }

        data2D.setDataName(dataName);
        data2D.setScale(scale);
        data2D.setMaxRandom(maxRandom);
        data2D.setComments(descInput.getText());
        data2D.setModifyTime(new Date());
        data2D.setColumns(columns);
        return data2D;
    }

    @FXML
    @Override
    public void okAction() {
        Data2D attributes = pickValues();
        if (attributes == null) {
            return;
        }
        if (dataController.data2D != null && attributes.isMutiplePages()) {
            handleMutiplePages(attributes);
        } else {
            handlePage(attributes);
        }
    }

    public void handlePage(Data2D attributes) {
        List<List<String>> pageData = new ArrayList<>();
        for (List<String> rowValues : tableData) {
            List<String> newRow = new ArrayList<>();
            newRow.add(rowValues.get(0));
            for (Data2DColumn column : attributes.getColumns()) {
                int dataIndex = column.getIndex() + 1;
                if (dataIndex <= 0 || dataIndex >= rowValues.size()) {
                    newRow.add(null);
                } else {
                    newRow.add(rowValues.get(dataIndex));
                }
            }
            pageData.add(newRow);
        }
        int colIndex = 0;
        for (Data2DColumn column : attributes.getColumns()) {
            column.setIndex(colIndex++);
        }
        if (dataController.data2D == null) {
            dataController.data2D = attributes;
        } else {
            dataController.data2D.cloneData(attributes);
        }
        dataController.makeColumns();
        dataController.updateTable(pageData);
        dataController.tableChanged(true);
        dataController.popInformation(message("Changed"));
        if (closeAfterCheck.isSelected()) {
            close();
        }
    }

    public void handleMutiplePages(Data2D attributes) {
        if (!PopTools.askSure(getTitle(), message("SureChangeDataAttributes"))) {
            return;
        }
        Data2D sourceData = dataController.data2D.cloneAll();
        sourceFile = sourceData.getFile();
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private boolean needBackup = false;
            private FileBackup backup;

            @Override
            protected boolean handle() {
                try {
                    needBackup = sourceData.needBackup();
                    if (needBackup) {
                        backup = addBackup(this, sourceFile);
                    }
                    sourceData.startTask(this, null);
                    return sourceData.saveAttributes(this, attributes) >= 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                dataController.data2D = attributes;
                dataController.popInformation(message("Saved"));
                dataController.notifySaved();
                dataController.readData(false);
                if (needBackup) {
                    if (backup != null && backup.getBackup() != null) {
                        dataController.popInformation(message("SavedAndBacked"));
                        FileBackupController.updateList(sourceFile);
                    } else {
                        dataController.popError(message("FailBackup"));
                    }
                }
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                sourceData.stopTask();
            }
        };
        start(task);
    }

    @FXML
    @Override
    public void recoverAction() {
        loadValues();
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (tab == columnsTab) {
            if (columnsController.keyEventsFilter(event)) {
                return true;
            }
        }
        return super.keyEventsFilter(event);
    }

    /*
        static
     */
    public static Data2DAttributesController edit(Data2DManufactureController tableController) {
        try {
            Data2DAttributesController controller = (Data2DAttributesController) WindowTools.childStage(
                    tableController, Fxmls.Data2DAttributesFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
