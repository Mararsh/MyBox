package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-8
 * @License Apache License Version 2.0
 */
public class Data2DExportController extends BaseTaskController {

    protected ControlData2DEditTable tableController;
    protected String filePrefix;
    protected List<Data2DColumn> selectedColumns;
    protected List<Integer> selectedColumnsIndices, selectedRowsIndices;

    @FXML
    protected ToggleGroup rowGroup;
    @FXML
    protected RadioButton allRowsRadio;
    @FXML
    protected VBox dataVBox, formatVBox, targetVBox;
    @FXML
    protected ControlDataConvert convertController;
    @FXML
    protected CheckBox openCheck;
    @FXML
    protected Label dataLabel, infoLabel;

    public Data2DExportController() {
        baseTitle = Languages.message("Export");
    }

    @Override
    public void setStageStatus() {
        setAsPop(baseName);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            convertController.setControls(this);

            openCheck.setSelected(UserConfig.getBoolean(baseName + "OpenGenerated", false));
            openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "OpenGenerated", openCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            getMyStage().setTitle(tableController.getBaseTitle());

            rowGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    checkOptions();
                }
            });

            tableController.selectNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });
            tableController.statusNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });
            checkSource();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean checkSource() {
        try {
            dataLabel.setText(tableController.data2D.displayName());
            infoLabel.setText("");

            selectedColumnsIndices = tableController.checkedColsIndices();
            selectedColumns = tableController.checkedCols();
            if (selectedColumnsIndices == null || selectedColumnsIndices.isEmpty()
                    || selectedColumns == null || selectedColumns.isEmpty()) {
                infoLabel.setText(message("SelectToHandle"));
                startButton.setDisable(true);
                return false;
            }

            if (!allRowsRadio.isSelected()) {
                selectedRowsIndices = tableController.checkedRowsIndices(false);
                if (selectedRowsIndices == null || selectedRowsIndices.isEmpty()) {
                    infoLabel.setText(message("SelectToHandle"));
                    startButton.setDisable(true);
                    return false;
                }
            } else {
                selectedRowsIndices = null;
            }
            startButton.setDisable(false);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            infoLabel.setText("");
            if (!checkSource()) {
                return false;
            }
            if (allRowsRadio.isSelected() && tableController.data2D.isMutiplePages()
                    && tableController.data2D.isTableChanged()) {
                infoLabel.setText(message("NeedSaveBeforeAction"));
                startButton.setDisable(true);
                return false;
            }
            targetPath = targetPathController.file;
            if (targetPath == null) {
                infoLabel.setText(message("InvalidParameters"));
                startButton.setDisable(true);
                return false;
            }
            if (!tableController.data2D.hasData()) {
                infoLabel.setText(message("NoData"));
                startButton.setDisable(true);
                return false;
            }

            startButton.setDisable(false);
            filePrefix = tableController.data2D.getDataName();
            if (filePrefix == null || filePrefix.isBlank()) {
                filePrefix = DateTools.nowFileString();
            }
            return convertController.initParameters();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    protected void beforeTask() {
        try {
            dataVBox.setDisable(true);
            formatVBox.setDisable(true);
            targetVBox.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected boolean doTask() {
        try {
            convertController.setExport(targetPath, selectedColumns, filePrefix, targetPathController.isSkip());

            if (!allRowsRadio.isSelected() || !tableController.data2D.isMutiplePages()) {
                selectedRowsIndices = tableController.checkedRowsIndices(allRowsRadio.isSelected());
                for (Integer row : selectedRowsIndices) {
                    List<String> dataRow = tableController.tableData.get(row);
                    List<String> exportRow = new ArrayList<>();
                    for (Integer col : selectedColumnsIndices) {
                        exportRow.add(dataRow.get(col + 1));
                    }
                    convertController.writeRow(exportRow);
                }

            } else {
                tableController.data2D.setTask(task);
                tableController.data2D.export(convertController, selectedColumnsIndices);
                tableController.data2D.setTask(null);
            }

            convertController.closeWriters();
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    protected void afterSuccess() {
        try {
            SoundTools.miao3();
            if (openCheck.isSelected()) {
                new Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            convertController.openFiles();
                        });
                    }

                }, 1000);

            }
            if (targetPath != null && targetPath.exists()) {
                browseURI(targetPath.toURI());
                recordFileOpened(targetPath);
            } else {
                popInformation(message("NoFileGenerated"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected void afterTask() {
        try {
            dataVBox.setDisable(false);
            formatVBox.setDisable(false);
            targetVBox.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        convertController.closeWriters();
    }

    @Override
    public void cancelAction() {
        cancelTask();
        close();
    }

    /*
        source
     */
    @FXML
    public void selectAllRows() {
        tableController.isSettingValues = true;
        tableController.allRowsCheck.setSelected(false);
        tableController.isSettingValues = false;
        tableController.allRowsCheck.setSelected(true);
    }

    @FXML
    public void selectNoneRows() {
        tableController.isSettingValues = true;
        tableController.allRowsCheck.setSelected(true);
        tableController.isSettingValues = false;
        tableController.allRowsCheck.setSelected(false);
    }

    @FXML
    public void selectAllCols() {
        tableController.isSettingValues = true;
        tableController.columnsCheck.setSelected(false);
        tableController.isSettingValues = false;
        tableController.columnsCheck.setSelected(true);
    }

    @FXML
    public void selectNoneCols() {
        tableController.isSettingValues = true;
        tableController.columnsCheck.setSelected(true);
        tableController.isSettingValues = false;
        tableController.columnsCheck.setSelected(false);
    }


    /*
        static
     */
    public static Data2DExportController open(ControlData2DEditTable tableController) {
        try {
            Data2DExportController controller = (Data2DExportController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DExportFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
