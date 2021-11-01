package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.BaseTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @param <P> Data
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public abstract class BaseDataTableController<P> extends BaseTableViewController<P> {

    protected BaseTable tableDefinition;

    @FXML
    protected Button examplesButton, refreshButton, resetButton,
            importButton, exportButton, chartsButton, queryButton, moveDataButton;
    @FXML
    protected Label queryConditionsLabel;

    public BaseDataTableController() {
        tableName = "";
        TipsLabelKey = "TableTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            setTableDefinition();
            if (tableDefinition != null) {
                tableName = tableDefinition.getTableName();
                idColumn = tableDefinition.getIdColumn();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // define tableDefinition here
    public void setTableDefinition() {
    }

    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        setPagination();
        if (queryConditionsLabel != null) {
            queryConditionsLabel.setText(queryConditionsString);
        }
    }

    @Override
    public List<P> readPageData() {
        if (tableDefinition != null) {
            return tableDefinition.queryConditions(queryConditions, startRowOfCurrentPage, pageSize);
        } else {
            return null;
        }
    }

    @Override
    public int readDataSize() {
        if (tableDefinition != null) {
            if (queryConditions != null) {
                return tableDefinition.conditionSize(queryConditions);
            } else {
                return tableDefinition.size();
            }
        } else {
            return 0;
        }
    }

    @Override
    public List<P> readData() {
        if (tableDefinition != null) {
            if (queryConditions != null) {
                return tableDefinition.query(queryConditions);
            } else {
                return tableDefinition.readAll();
            }
        } else {
            return null;
        }
    }

    @Override
    protected int deleteData(List<P> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        if (tableDefinition != null) {
            return tableDefinition.deleteData(data);
        }
        return 0;
    }

    @Override
    protected int clearData() {
        if (tableDefinition != null) {
            return tableDefinition.deleteCondition(queryConditions);
        } else {
            return 0;
        }
    }

    @FXML
    protected void importAction() {
        File file = FxFileTools.selectFile(this);
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    importData(file);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    refreshAction();
                }
            };
            start(task);
        }
    }

    protected void importData(File file) {
        DerbyBase.importData(tableName, file.getAbsolutePath(), false);
    }

    @FXML
    protected void exportAction() {
        final File file = chooseSaveFile(UserConfig.getPath(baseName + "TargetPath"),
                Languages.message(tableName) + ".txt", FileFilters.AllExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file);
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    DerbyBase.exportData(tableName, file.getAbsolutePath());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    ControllerTools.openTextEditer(null, file);
                }
            };
            start(task);
        }
    }

    @FXML
    protected void analyseAction() {

    }

}
