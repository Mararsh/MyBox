package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Window;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-5-21
 * @License Apache License Version 2.0
 */
public class Data2DSpliceController extends BaseData2DController {

    @FXML
    protected ControlData2DSource dataAController, dataBController;
    @FXML
    protected RadioButton horizontalRadio, aRadio, bRadio, longerRadio, shorterRadio;
    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected Label numberLabel;
    @FXML
    protected ToggleGroup directionGroup;

    public Data2DSpliceController() {
        baseTitle = message("SpliceData");
        TipsLabelKey = "DataManageTips";
    }

    @Override
    public void setDataType(Data2D.Type type) {
        try {
            this.type = type;
            dataAController.setData(Data2D.create(type));
            dataBController.setData(Data2D.create(type));

            tableData2DDefinition = dataAController.tableData2DDefinition;
            data2D = dataAController.data2D;

            checkButtons();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            directionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (horizontalRadio.isSelected()) {
                        numberLabel.setText(message("RowsNumber"));
                    } else {
                        numberLabel.setText(message("ColumnsNumber"));
                    }
                }
            });

            targetController.setParameters(this, null);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            private DataFileCSV csvA, csvB;

            @Override
            protected boolean handle() {
                try {
                    dataAController.data2D.setTask(this);
                    if (dataAController.allPages()) {
                        csvA = dataAController.data2D.copy(dataAController.checkedColsIndices, false, true);
                    } else {
                        csvA = DataFileCSV.save(task, dataAController.checkedCols(), dataAController.selectedData(false));
                    }
                    dataAController.data2D.setTask(null);
                    if (csvA == null) {
                        return false;
                    }

                    dataBController.data2D.setTask(this);
                    if (dataBController.allPages()) {
                        csvB = dataBController.data2D.copy(dataBController.checkedColsIndices, false, true);
                    } else {
                        csvB = DataFileCSV.save(task, dataBController.checkedCols(), dataBController.selectedData(false));
                    }
                    dataBController.data2D.setTask(null);
                    if (csvB == null) {
                        return false;
                    }
                    if (horizontalRadio.isSelected()) {

                    }
                    return csvB != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popDone();

            }

            @Override
            protected void finalAction() {
                super.finalAction();
                dataAController.data2D.setTask(null);
                dataBController.data2D.setTask(null);
                task = null;
            }

        };
        start(task);
    }

//    protected DataFileCSV spliceVertically(DataFileCSV csvA, DataFileCSV csvB) {
//        try {
//            if (csvA == null || csvB == null);
//
//            targetController.setParameters(this, null);
//
//        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
//        }
//    }

    /*
        static
     */
    public static Data2DSpliceController oneOpen() {
        Data2DSpliceController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof Data2DSpliceController) {
                try {
                    controller = (Data2DSpliceController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (Data2DSpliceController) WindowTools.openStage(Fxmls.Data2DManageFxml);
        }
        controller.requestMouse();
        return controller;
    }

    public static Data2DSpliceController open(Data2DDefinition def) {
        Data2DSpliceController controller = oneOpen();
        controller.loadDef(def);
        return controller;
    }

    public static void updateList() {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof Data2DSpliceController) {
                try {
                    Data2DSpliceController controller = (Data2DSpliceController) object;
                    controller.refreshAction();
                    break;
                } catch (Exception e) {
                }
            }
        }
    }

}
