package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-25
 * @License Apache License Version 2.0
 */
public class Data2DSortController extends BaseData2DHandleController {

    protected int orderCol;
    protected List<Integer> colsIndices;
    protected List<String> colsNames;
    protected String orderName;

    @FXML
    protected ComboBox<String> colSelector;
    @FXML
    protected CheckBox descendCheck;
    @FXML
    protected Label memoryNoticeLabel;

    public Data2DSortController() {
        baseTitle = message("Sort");
    }

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController);

            colSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
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

            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                colSelector.getItems().clear();
                return;
            }
            String selectedCol = colSelector.getSelectionModel().getSelectedItem();
            colSelector.getItems().setAll(names);
            if (selectedCol != null && names.contains(selectedCol)) {
                colSelector.setValue(selectedCol);
            } else {
                colSelector.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        targetController.setNotInTable(isAllPages());
        memoryNoticeLabel.setVisible(!data2D.isTable() && isAllPages());
        orderCol = data2D.colOrder(colSelector.getSelectionModel().getSelectedItem());
        colsIndices = checkedColsIndices;
        if (colsIndices == null || colsIndices.isEmpty() || orderCol < 0) {
            infoLabel.setText(message("SelectToHandle"));
            okButton.setDisable(true);
            return false;
        }
        orderName = data2D.colName(orderCol);
        return ok;
    }

    public List<Integer> adjustedCols() {
        try {
            colsNames = checkedColsNames;
            if (!colsIndices.contains(orderCol)) {
                colsIndices.add(orderCol);
                colsNames.add(orderName);
            }
            outputColumns = new ArrayList<>();
            for (int col : colsIndices) {
                outputColumns.add(data2D.column(col));
            }
            if (showRowNumber()) {
                colsNames.add(0, message("SourceRowNumber"));
                outputColumns.add(0, new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
            }
            return colsIndices;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public boolean handleRows() {
        try {
            outputData = selectedData(adjustedCols(), showRowNumber());
            sort(outputData);
            if (showRowNumber()) {
                outputData.add(0, colsNames);
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

    public boolean sort(List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                return false;
            }
            Data2DColumn column = data2D.getColumns().get(orderCol);
            int index = colsNames.indexOf(orderName);
            boolean desc = descendCheck.isSelected();
            Collections.sort(data, new Comparator<List<String>>() {
                @Override
                public int compare(List<String> r1, List<String> r2) {
                    int c = column.compare(r1.get(index), r2.get(index));
                    return desc ? -c : c;
                }
            });
            outputData = data;
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
        if (data2D instanceof DataTable) {
            return ((DataTable) data2D).sort(colsIndices, orderName, descendCheck.isSelected(), showRowNumber());
        } else {
            try {
                List<List<String>> data = data2D.allRows(adjustedCols(), showRowNumber());
                if (!sort(data)) {
                    return null;
                }
                return DataFileCSV.save(task, outputColumns, data);
            } catch (Exception e) {
                if (task != null) {
                    task.setError(e.toString());
                }
                MyBoxLog.error(e.toString());
                return null;
            }
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
