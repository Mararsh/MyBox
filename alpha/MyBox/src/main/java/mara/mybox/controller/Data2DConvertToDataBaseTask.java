package mara.mybox.controller;

import java.sql.Connection;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.SoundTools;

/**
 * @Author Mara
 * @CreateDate 2022-2-13
 * @License Apache License Version 2.0
 */
public class Data2DConvertToDataBaseTask extends Data2DTableCreateController {

    protected Data2DConvertToDataBaseController convertController;

    public void setParameters(Data2DConvertToDataBaseController convertController) {
        try {
            this.convertController = convertController;

            attributesController = convertController.attributesController;
            startButton = convertController.startButton;
            tabPane = convertController.tabPane;
            logsTab = convertController.logsTab;
            attributesTab = convertController.attributesTab;
            attributesBox = convertController.attributesBox;
            optionsBox = convertController.attributesBox;
            zeroNonnumericRadio = convertController.zeroNonnumericRadio;
            blankNonnumericRadio = convertController.blankNonnumericRadio;

            attributesController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        return true;
    }

    public boolean checkAttributes() {
        return super.checkOptions();
    }

    @Override
    public void beforeTask() {
        try {
            super.beforeTask();

            convertController.dataVBox.setDisable(true);
            convertController.filterVBox.setDisable(true);

            if (zeroNonnumericRadio != null && zeroNonnumericRadio.isSelected()) {
                invalidAs = InvalidAs.Zero;
            } else {
                invalidAs = InvalidAs.Blank;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try (Connection conn = DerbyBase.getConnection()) {
            attributesController.columnIndices = convertController.checkedColsIndices;
            if (!attributesController.createTable(currentTask, conn)) {
                return false;
            }
            if (convertController.importCheck.isSelected()) {
                attributesController.data2D.startTask(currentTask, convertController.filterController.filter);
                attributesController.task = currentTask;
                if (convertController.isAllPages() && convertController.data2D.isMutiplePages()) {
                    attributesController.importAllData(currentTask, conn, convertController.invalidAs);
                } else {
                    attributesController.importData(conn, convertController.filteredRowsIndices(), convertController.invalidAs);
                }
                attributesController.data2D.stopFilter();
            }
            return true;
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }

    }

    @Override
    public void afterSuccess() {
        try {
            SoundTools.miao3();
            Data2DManufactureController.openDef(attributesController.dataTable);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void afterTask() {
        try {
            super.afterTask();
            convertController.dataVBox.setDisable(false);
            convertController.filterVBox.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
