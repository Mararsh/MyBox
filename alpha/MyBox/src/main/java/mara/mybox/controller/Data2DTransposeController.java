package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-9
 * @License Apache License Version 2.0
 */
public class Data2DTransposeController extends BaseData2DHandleController {

    @FXML
    protected CheckBox firstCheck;

    public Data2DTransposeController() {
        baseTitle = message("Transpose");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            firstCheck.setSelected(UserConfig.getBoolean(baseName + "FirstAsNames", false));
            firstCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                if (isSettingValues) {
                    return;
                }
                UserConfig.setBoolean(baseName + "FirstAsNames", firstCheck.isSelected());
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean handleRows() {
        try {
            outputData = selectedData(showRowNumber());
            if (outputData == null) {
                return false;
            }
            if (showColNames()) {
                List<String> names = checkedColsNames;
                if (showRowNumber()) {
                    names.add(0, message("SourceRowNumber"));
                }
                outputData.add(0, names);
            }
            int rowsNumber = outputData.size(), columnsNumber = outputData.get(0).size();

            outputColumns = null;
            if (firstCheck.isSelected()) {
                outputColumns = new ArrayList<>();
                if (showRowNumber()) {
                    outputColumns.add(new Data2DColumn(message("RowNumber"), ColumnDefinition.ColumnType.String));
                }
                List<String> names = new ArrayList<>();
                for (int c = 0; c < rowsNumber; ++c) {
                    String name = outputData.get(c).get(0);
                    if (name == null || name.isBlank()) {
                        name = message("Columns") + (c + 1);
                    }
                    while (names.contains(name)) {
                        name += "m";
                    }
                    names.add(name);
                    outputColumns.add(new Data2DColumn(name, ColumnDefinition.ColumnType.String));
                }
            }

            List<List<String>> transposed = new ArrayList<>();
            for (int r = 0; r < columnsNumber; ++r) {
                List<String> row = new ArrayList<>();
                for (int c = 0; c < rowsNumber; ++c) {
                    row.add(outputData.get(c).get(r));
                }
                transposed.add(row);
            }
            outputData = transposed;

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
            DataTable tmpTable = data2D.toTmpTable(task, checkedColsIndices, showRowNumber());
            if (tmpTable == null) {
                return null;
            }
            DataFileCSV csvData = tmpTable.transpose(task, showColNames(), firstCheck.isSelected());
            tmpTable.deleteTable();
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
    public static Data2DTransposeController open(ControlData2DEditTable tableController) {
        try {
            Data2DTransposeController controller = (Data2DTransposeController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DTransposeFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
