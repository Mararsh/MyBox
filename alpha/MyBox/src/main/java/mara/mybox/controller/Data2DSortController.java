package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.data.DataFileCSV;
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
public class Data2DSortController extends Data2DHandleController {

    protected int orderCol;

    @FXML
    protected ComboBox<String> colSelector;
    @FXML
    protected CheckBox descendCheck;
    @FXML
    protected Label memoryNoticeLabel;

    @Override
    public void setParameters(ControlData2DEditTable tableController) {
        super.setParameters(tableController);
        refreshControls();
    }

    public void refreshControls() {
        try {
            String selectedCol = colSelector.getSelectionModel().getSelectedItem();
            colSelector.getItems().setAll(tableController.data2D.columnNames());
            if (selectedCol != null) {
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
        targetController.setNotInTable(allPages());
        memoryNoticeLabel.setVisible(allPages());
        orderCol = data2D.colOrder(colSelector.getSelectionModel().getSelectedItem());
        if (orderCol < 0) {
            popError(message("SelectToHandle"));
            okButton.setDisable(true);
            return false;
        }
        return super.checkOptions();
    }

    @Override
    public boolean handleRows() {
        try {
            List<List<String>> selectRows = new ArrayList<>();
            int size = tableController.tableData.size();
            for (int row : tableController.checkedRowsIndices) {
                if (row < 0 || row >= size) {
                    continue;
                }
                List<String> tableRow = tableController.tableData.get(row);
                selectRows.add(tableRow);
            }
            Data2DColumn column = data2D.getColumns().get(orderCol);
            int index = orderCol + 1;
            boolean desc = descendCheck.isSelected();
            Collections.sort(selectRows, new Comparator<List<String>>() {
                @Override
                public int compare(List<String> r1, List<String> r2) {
                    int c = column.compare(r1.get(index), r2.get(index));
                    return desc ? -c : c;
                }
            });

            handledData = new ArrayList<>();
            if (showColNames()) {
                List<String> names = tableController.checkedColsNames();
                if (rowNumberCheck != null && rowNumberCheck.isSelected()) {
                    names.add(0, message("SourceRowNumber"));
                }
                handledData.add(0, names);
            }
            for (List<String> row : selectRows) {
                List<String> newRow = new ArrayList<>();
                if (rowNumberCheck.isSelected()) {
                    newRow.add(row.get(0));
                }
                for (int col : tableController.checkedColsIndices) {
                    int cc = col + 1;
                    if (cc < 0 || cc >= row.size()) {
                        continue;
                    }
                    newRow.add(row.get(cc));
                }
                handledData.add(newRow);
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
        return data2D.copy(tableController.checkedColsIndices, rowNumberCheck.isSelected(), colNameCheck.isSelected());
    }

    /*
        static
     */
    public static Data2DSortController open(ControlData2DEditTable tableController) {
        try {
            Data2DSortController controller = (Data2DSortController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSortFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
