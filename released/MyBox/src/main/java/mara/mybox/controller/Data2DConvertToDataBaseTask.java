package mara.mybox.controller;

import java.sql.Connection;
import mara.mybox.data2d.Data2D_Attributes;
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
        return true;
    }

    public boolean checkAttributes() {
        return super.checkOptions();
    }

    @Override
    public void beforeTask() {
        try {
            convertController.dataVBox.setDisable(true);
            convertController.attributesBox.setDisable(true);
            convertController.filterVBox.setDisable(true);
            convertController.optionsBox.setDisable(true);

            if (zeroNonnumericRadio != null && zeroNonnumericRadio.isSelected()) {
                invalidAs = Data2D_Attributes.InvalidAs.Zero;
            } else {
                invalidAs = Data2D_Attributes.InvalidAs.Blank;
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean doTask() {
        try ( Connection conn = DerbyBase.getConnection()) {
            attributesController.columnIndices = convertController.checkedColsIndices;
            if (!attributesController.createTable(conn)) {
                return false;
            }
            if (convertController.importCheck.isSelected()) {
                attributesController.data2D.startTask(task, convertController.filterController.filter);
                attributesController.task = task;
                if (convertController.isAllPages() && convertController.data2D.isMutiplePages()) {
                    attributesController.importAllData(conn, convertController.invalidAs);
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
            convertController.optionsBox.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
