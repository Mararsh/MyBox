package mara.mybox.controller;

import java.sql.Connection;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
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

            attributesController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        return convertController.checkOptions() && super.checkOptions();
    }

    @Override
    public void beforeTask() {
        try {
            convertController.dataVBox.setDisable(true);
            convertController.attributesBox.setDisable(true);
            convertController.filterVBox.setDisable(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean doTask() {
        try ( Connection conn = DerbyBase.getConnection()) {
            convertController.data2D.startFilterService(task);
            attributesController.columnIndices = convertController.checkedColsIndices;
            if (!attributesController.createTable(conn)) {
                return false;
            }
            if (convertController.importCheck.isSelected()) {
                if (convertController.isAllPages() && convertController.data2D.isMutiplePages()) {
                    attributesController.task = task;
                    attributesController.importAllData(conn);
                } else {
                    attributesController.importData(conn, convertController.checkedRowsIndices);
                }
            }
            convertController.data2D.stopFilterService();
            return true;
        } catch (Exception e) {
            updateLogs(e.toString());
            convertController.data2D.stopFilterService();
            return false;
        }

    }

    @Override
    public void afterSuccess() {
        try {
            SoundTools.miao3();
            DataTablesController c = DataTablesController.open(attributesController.dataTable);
            c.refreshAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterTask() {
        try {
            convertController.dataVBox.setDisable(false);
            convertController.filterVBox.setDisable(false);
            convertController.attributesBox.setDisable(false);
            convertController.data2D.stopFilterService();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
