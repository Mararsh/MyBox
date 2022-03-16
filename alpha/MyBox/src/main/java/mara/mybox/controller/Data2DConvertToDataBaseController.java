package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-2-13
 * @License Apache License Version 2.0
 */
public class Data2DConvertToDataBaseController extends Data2DTableCreateController {

    protected List<Integer> selectedRowsIndices, selectedColumnsIndices;

    @FXML
    protected Tab dataTab;
    @FXML
    protected ControlData2DSource sourceController;
    @FXML
    protected VBox dataVBox;
    @FXML
    protected CheckBox importCheck;
    @FXML
    protected Label dataLabel, infoLabel;

    public Data2DConvertToDataBaseController() {
        TipsLabelKey = message("SqlIdentifierComments");
    }

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController);

            attributesController.nameInput.setText(tableController.data2D.shortName());

            sourceController.setParameters(this, tableController);
            sourceController.setLabel(message("SelectRowsColumnsToHanlde"));

            sourceController.loadedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkSource();
                }
            });
            sourceController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkSource();
                }
            });
            checkSource();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean checkSource() {
        try {
            getMyStage().setTitle(tableController.getTitle());
            infoLabel.setText("");

            selectedColumnsIndices = sourceController.checkedColsIndices();
            if (selectedColumnsIndices == null || selectedColumnsIndices.isEmpty()) {
                infoLabel.setText(message("SelectToHandle") + ": " + message("Columns"));
                tabPane.getSelectionModel().select(dataTab);
                return false;
            }
            attributesController.setColumns(selectedColumnsIndices);

            if (!sourceController.allPages()) {
                selectedRowsIndices = sourceController.checkedRowsIndices();
                if (selectedRowsIndices == null || selectedRowsIndices.isEmpty()) {
                    infoLabel.setText(message("SelectToHandle") + ": " + message("Rows"));
                    tabPane.getSelectionModel().select(dataTab);
                    return false;
                }
            } else {
                selectedRowsIndices = null;
            }
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
            if (!tableController.data2D.hasData()) {
                infoLabel.setText(message("NoData"));
                tabPane.getSelectionModel().select(dataTab);
                return false;
            }
            return super.checkOptions();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    @Override
    public void beforeTask() {
        try {
            dataVBox.setDisable(true);
            attributesBox.setDisable(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean doTask() {
        try ( Connection conn = DerbyBase.getConnection()) {
            if (!attributesController.createTable(conn)) {
                return false;
            }
            if (importCheck.isSelected()) {
                if (sourceController.allPages() && tableController.data2D.isMutiplePages()) {
                    attributesController.importAllData(conn);
                } else {
                    attributesController.importData(conn, selectedRowsIndices);
                }
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
            dataVBox.setDisable(false);
            attributesBox.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static
     */
    public static Data2DConvertToDataBaseController open(ControlData2DEditTable tableController) {
        try {
            Data2DConvertToDataBaseController controller = (Data2DConvertToDataBaseController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DConvertToDatabaseFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
