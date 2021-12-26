package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.data.DataFileCSV;
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
public class Data2DSortController extends Data2DHandleController {

    protected int orderCol;
    protected List<Integer> colsIndices;
    protected List<String> colsNames;

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
            infoLabel.setText(message("SelectToHandle"));
            okButton.setDisable(true);
            return false;
        }
        return super.checkOptions();
    }

    public List<Integer> adjustedCols() {
        try {
            colsNames = tableController.checkedColsNames();
            String orderName = data2D.colName(orderCol);
            if (colsNames.contains(orderName)) {
                colsNames.remove(orderName);
            }
            colsNames.add(0, orderName);

            colsIndices = new ArrayList<>();
            handledColumns = new ArrayList<>();
            for (String name : colsNames) {
                int col = data2D.colOrder(name);
                colsIndices.add(col);
                handledColumns.add(data2D.column(col));
            }
            if (showRowNumber()) {
                colsNames.add(0, message("SourceRowNumber"));
                handledColumns.add(0, new Data2DColumn(message("SourceRowNumber"), ColumnDefinition.ColumnType.Long));
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
            handledData = tableController.selectedData(
                    tableController.checkedRowsIndices(all()), adjustedCols(), showRowNumber());
            sort(handledData);
            if (showRowNumber()) {
                handledData.add(0, colsNames);
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
            int index = showRowNumber() ? 1 : 0;
            boolean desc = descendCheck.isSelected();
            Collections.sort(data, new Comparator<List<String>>() {
                @Override
                public int compare(List<String> r1, List<String> r2) {
                    int c = column.compare(r1.get(index), r2.get(index));
                    return desc ? -c : c;
                }
            });
            handledData = data;
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
            List<List<String>> rows = data2D.allRows(adjustedCols(), showRowNumber());
            if (!sort(rows)) {
                return null;
            }
            MyBoxLog.debug(colsNames);
            DataFileCSV dataFileCSV = new DataFileCSV();
            File file = dataFileCSV.tmpFile(colsNames, rows);
            dataFileCSV.setFile(file).setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",").setHasHeader(true)
                    .setColsNumber(colsNames.size()).setRowsNumber(rows.size());
            return dataFileCSV;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
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
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
