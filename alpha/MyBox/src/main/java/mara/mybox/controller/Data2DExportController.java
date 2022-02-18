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

    protected ControlData2DEditTable editController;
    protected String filePrefix;
    protected List<Data2DColumn> selectedColumns;
    protected List<Integer> selectedColumnsIndices, selectedRowsIndices;

    @FXML
    protected ControlData2DSource sourceController;
    @FXML
    protected VBox dataVBox, formatVBox, targetVBox;
    @FXML
    protected ControlDataConvert convertController;
    @FXML
    protected CheckBox openCheck;
    @FXML
    protected Label infoLabel;

    public Data2DExportController() {
        baseTitle = Languages.message("Export");
    }

    @Override
    public void setStageStatus() {
        setAsNormal();
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

    public void setParameters(ControlData2DEditTable editController) {
        try {
            this.editController = editController;
            getMyStage().setTitle(editController.getBaseTitle());

            sourceController.setParameters(this, editController);

            sourceController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });
            sourceController.selectNotify.addListener(new ChangeListener<Boolean>() {
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
            getMyStage().setTitle(editController.getTitle());
            infoLabel.setText("");

            selectedColumnsIndices = sourceController.checkedColsIndices();
            selectedColumns = sourceController.checkedCols();
            if (selectedColumnsIndices == null || selectedColumnsIndices.isEmpty()
                    || selectedColumns == null || selectedColumns.isEmpty()) {
                infoLabel.setText(message("SelectToHandle"));
                startButton.setDisable(true);
                return false;
            }

            if (!sourceController.allPages()) {
                selectedRowsIndices = sourceController.checkedRowsIndices();
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
            targetPath = targetPathController.file;
            if (targetPath == null) {
                infoLabel.setText(message("InvalidParameters"));
                startButton.setDisable(true);
                return false;
            }
            if (!editController.data2D.hasData()) {
                infoLabel.setText(message("NoData"));
                startButton.setDisable(true);
                return false;
            }

            startButton.setDisable(false);
            filePrefix = editController.data2D.getDataName();
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
    public void beforeTask() {
        try {
            dataVBox.setDisable(true);
            formatVBox.setDisable(true);
            targetVBox.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean doTask() {
        try {
            convertController.setExport(targetPath, selectedColumns, filePrefix, targetPathController.isSkip());

            if (!sourceController.allPages() || !editController.data2D.isMutiplePages()) {
                selectedRowsIndices = sourceController.checkedRowsIndices();
                for (Integer row : selectedRowsIndices) {
                    List<String> dataRow = editController.tableData.get(row);
                    List<String> exportRow = new ArrayList<>();
                    for (Integer col : selectedColumnsIndices) {
                        exportRow.add(dataRow.get(col + 1));
                    }
                    convertController.writeRow(exportRow);
                }

            } else {
                editController.data2D.setTask(task);
                editController.data2D.export(convertController, selectedColumnsIndices);
                editController.data2D.setTask(null);
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
    public void afterSuccess() {
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
    public void afterTask() {
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
