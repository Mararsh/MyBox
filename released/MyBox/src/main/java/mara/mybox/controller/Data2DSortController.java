package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.data2d.Data2D_Attributes.InvalidAs;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-25
 * @License Apache License Version 2.0
 */
public class Data2DSortController extends BaseData2DHandleController {

    protected int maxData = -1;
    protected List<String> orders;

    @FXML
    protected ControlSelection columnsController;
    @FXML
    protected TextField maxInput;

    public Data2DSortController() {
        baseTitle = message("Sort");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            maxData = UserConfig.getInt(baseName + "MaxDataNumber", -1);
            if (maxData > 0) {
                maxInput.setText(maxData + "");
            }
            maxInput.setStyle(null);
            maxInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    String maxs = maxInput.getText();
                    if (maxs == null || maxs.isBlank()) {
                        maxData = -1;
                        maxInput.setStyle(null);
                        UserConfig.setLong(baseName + "MaxDataNumber", -1);
                    } else {
                        try {
                            maxData = Integer.valueOf(maxs);
                            maxInput.setStyle(null);
                            UserConfig.setLong(baseName + "MaxDataNumber", maxData);
                        } catch (Exception e) {
                            maxInput.setStyle(UserConfig.badStyle());
                        }
                    }
                }
            });

            columnsController.setParameters(this, message("Column"), message("DataSortLabel"));
            columnsController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();
            if (!data2D.isValid()) {
                columnsController.loadNames(null);
                return;
            }
            List<String> names = new ArrayList<>();
            for (Data2DColumn column : data2D.columns) {
                String name = column.getColumnName();
                names.add(name + "-" + message("Descending"));
                names.add(name + "-" + message("Ascending"));
            }
            columnsController.loadNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            orders = columnsController.selectedNames();
            if (orders == null || orders.isEmpty()) {
                outOptionsError(message("SelectToHandle") + ": " + message("Order"));
                tabPane.getSelectionModel().select(optionsTab);
                return false;
            }
            List<String> colsNames = new ArrayList<>();
            for (String name : checkedColsNames) {
                if (!colsNames.contains(name)) {
                    colsNames.add(name);
                }
            }
            for (String order : orders) {
                String name;
                if (order.endsWith("-" + message("Ascending"))) {
                    name = order.substring(0, order.length() - ("-" + message("Ascending")).length());
                } else {
                    name = order.substring(0, order.length() - ("-" + message("Descending")).length());
                }
                if (!colsNames.contains(name)) {
                    colsNames.add(name);
                }
            }
            checkedColsIndices = new ArrayList<>();
            checkedColumns = new ArrayList<>();
            for (String name : colsNames) {
                checkedColsIndices.add(data2D.colOrder(name));
                checkedColumns.add(data2D.columnByName(name));
            }
            checkedColsNames = colsNames;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean handleRows() {
        try {
            outputData = filtered(checkedColsIndices, showRowNumber());
            if (outputData == null || outputData.isEmpty()) {
                return false;
            }
            DataTable tmpTable = data2D.toTmpTable(task, checkedColsIndices, outputData, showRowNumber(), false, InvalidAs.Blank);
            if (tmpTable == null) {
                return false;
            }
            DataFileCSV csvData = tmpTable.sort(targetController.name(), task,
                    orders, maxData, colNameCheck.isSelected());
            tmpTable.drop();
            if (csvData == null) {
                return false;
            }
            outputData = csvData.allRows(false);
            if (showColNames()) {
                outputData.add(0, checkedColsNames);
            }
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
    public DataFileCSV generatedFile() {
        try {
            DataTable tmpTable = data2D.toTmpTable(task, checkedColsIndices, showRowNumber(), false, InvalidAs.Blank);
            if (tmpTable == null) {
                return null;
            }
            DataFileCSV csvData = tmpTable.sort(targetController.name(), task,
                    orders, maxData, colNameCheck.isSelected());
            tmpTable.drop();
            return csvData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        static
     */
    public static Data2DSortController open(ControlData2DEditTable tableController) {
        try {
            Data2DSortController controller = (Data2DSortController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSortFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
