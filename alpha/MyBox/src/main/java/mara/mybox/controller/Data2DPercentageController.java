package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data.DataFileCSV;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-13
 * @License Apache License Version 2.0
 */
public class Data2DPercentageController extends Data2DHandleController {

    protected File handleFile;
    protected List<String> handledNames;

    @FXML
    protected CheckBox valuesCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            valuesCheck.setSelected(UserConfig.getBoolean(baseName + "WithDataValues", false));
            valuesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WithDataValues", valuesCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        targetController.setNotInTable(allPages());
        ok = ok && prepareRows();
        okButton.setDisable(!ok);
        return ok;
    }

    public boolean prepareRows() {
        try {
            List<Data2DColumn> cols = tableController.checkedCols();
            if (cols == null || cols.isEmpty()) {
                return false;
            }
            handledNames = new ArrayList<>();
            handledColumns = new ArrayList<>();
            String cName = message("SourceRowNumber");
            while (handledNames.contains(cName)) {
                cName += "m";
            }
            handledNames.add(cName);
            handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Integer));
            for (Data2DColumn column : cols) {
                if (valuesCheck.isSelected()) {
                    handledColumns.add(column.cloneAll());
                    handledNames.add(column.getName());
                }
                cName = column.getName() + "%";
                while (handledNames.contains(cName)) {
                    cName += "m";
                }
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
                handledNames.add(cName);
            }
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean handleRows() {
        try {
            if (tableController.checkedRowsIndices == null || tableController.checkedRowsIndices.isEmpty()) {
                if (task != null) {
                    task.setError(message("SelectToHandle"));
                }
                return false;
            }
            int colsLen = tableController.checkedColsIndices.size();
            double[] sum = new double[colsLen];
            for (int r : tableController.checkedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                for (int c = 0; c < colsLen; c++) {
                    sum[c] += data2D.doubleValue(tableRow.get(tableController.checkedColsIndices.get(c) + 1));
                }
            }
            handledData = new ArrayList<>();
            handledData.add(0, handledNames);
            int scale = data2D.getScale();
            List<String> row = new ArrayList<>();
            row.add(message("Count"));
            for (int c = 0; c < colsLen; c++) {
                if (valuesCheck.isSelected()) {
                    row.add("");
                }
                row.add(DoubleTools.format(sum[c], scale));
            }
            handledData.add(row);
            for (int r : tableController.checkedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                row = new ArrayList<>();
                row.add((r + 1) + "");
                for (int c = 0; c < colsLen; c++) {
                    double d = data2D.doubleValue(tableRow.get(tableController.checkedColsIndices.get(c) + 1));
                    if (valuesCheck.isSelected()) {
                        row.add(DoubleTools.format(d, scale));
                    }
                    if (sum[c] == 0) {
                        row.add("0");
                    } else {
                        row.add(DoubleTools.percentage(d, sum[c]));
                    }
                }
                handledData.add(row);
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public DataFileCSV generatedFile() {
        return data2D.percentage(handledNames, tableController.checkedColsIndices, valuesCheck.isSelected());
    }

    /*
        static
     */
    public static Data2DPercentageController open(ControlData2DEditTable tableController) {
        try {
            Data2DPercentageController controller = (Data2DPercentageController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DPercentageFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
