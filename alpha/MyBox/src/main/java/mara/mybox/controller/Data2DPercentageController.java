package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import mara.mybox.data.Data2D;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
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
public class Data2DPercentageController extends Data2DOperationController {

    protected File handleFile;

    @FXML
    protected Label noNumberLabel;
    @FXML
    protected CheckBox valuesCheck;

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController, true, false);

            setColumns();

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

    public void setColumns() {
        try {
            List<String> numberColumnNames = data2D.numberColumnNames();
            if (numberColumnNames == null || numberColumnNames.isEmpty()) {
                noNumberLabel.setVisible(true);
                okButton.setDisable(true);
                selectController.colsListController.clear();
            } else {
                noNumberLabel.setVisible(false);
                okButton.setDisable(false);
                List<String> selectedCols = selectController.colsListController.checkedValues();
                selectController.colsListController.setValues(numberColumnNames);
                if (selectedCols != null && !selectedCols.isEmpty()) {
                    selectController.colsListController.checkValues(selectedCols);
                } else {
                    selectController.colsListController.checkAll();
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        super.refreshControls();
        setColumns();
    }

    @Override
    public boolean hanldeData() {
        try {
            handleFile = null;
            if (!prepareRows()) {
                return false;
            }
            if (sourceAll && data2D.isMutiplePages()) {
                return handleFile();
            } else {
                return handleRows();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }

    }

    public boolean prepareRows() {
        try {
            if (selectedNames == null || selectedNames.isEmpty()) {
                popError(message("SelectToHandle"));
                return false;
            }
            handledNames = new ArrayList<>();
            for (String name : selectedNames) {
                if (valuesCheck.isSelected()) {
                    handledNames.add(name);
                }
                handledNames.add("m-%" + name + "-m");
            }

            handledColumns = new ArrayList<>();
            for (Data2DColumn column : selectedColumns) {
                if (valuesCheck.isSelected()) {
                    handledColumns.add(column);
                }
                handledColumns.add(new Data2DColumn("m-%" + column.getName() + "-m",
                        ColumnDefinition.ColumnType.Double));
            }

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    // All as double to make things simple. 
    // To improve performance, this should be counting according to columns' types.
    public boolean handleRows() {
        try {
            if (selectedData == null || selectedData.isEmpty()) {
                popError(message("SelectToHandle"));
                return false;
            }

            int rowsNumber = selectedData.size();
            int columnsNumber = selectedNames.size();
            double[] sum = new double[rowsNumber];
            for (int c = 0; c < columnsNumber; c++) {
                for (int r = 0; r < rowsNumber; r++) {
                    sum[c] += data2D.doubleValue(selectedData.get(r).get(c));
                }
            }
            handledData = new ArrayList<>();
            int scale = data2D.getScale();
            for (int r = 0; r < rowsNumber; r++) {
                List<String> row = new ArrayList<>();
                for (int c = 0; c < columnsNumber; c++) {
                    double d = data2D.doubleValue(selectedData.get(r).get(c));
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
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean handleFile() {
        try {
            handleFile = data2D.percentage(handledNames, selectedRowsIndices, valuesCheck.isSelected());
            if (handleFile == null || !handleFile.exists()) {
                return false;
            }
            try ( Connection conn = DerbyBase.getConnection()) {
                Data2DDefinition def = Data2D.create(Data2DDefinition.Type.CSV)
                        .setFile(handleFile).setHasHeader(true)
                        .setDelimiter(",").setCharset(Charset.forName("UTF-8"));
                def = tableController.tableData2DDefinition.insertData(conn, def);
                conn.commit();
                for (int i = 0; i < handledColumns.size(); i++) {
                    Data2DColumn column = handledColumns.get(i);
                    column.setIndex(i);
                }
                tableController.tableData2DColumn.save(conn, def.getD2did(), handledColumns);
            } catch (Exception e) {
                MyBoxLog.error(e);
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean outputExternal() {
        if (sourceAll && data2D.isMutiplePages()) {
            DataFileCSVController.open(handleFile, Charset.forName("UTF-8"), true, ',');

        } else {
            DataFileCSVController.open(handledColumns, handledData);
        }
        return true;
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
