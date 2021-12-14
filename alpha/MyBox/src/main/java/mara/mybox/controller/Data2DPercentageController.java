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
    protected CheckBox valuesCheck;

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController, true, true, false);

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
    public boolean hanldeData() {
        try {
            handleFile = null;
            if (!prepareRows()) {
                return false;
            }
            if (selectController.isAllData() && data2D.isMutiplePages()) {
                return handleFile();
            } else {
                return handleRows();
            }
        } catch (Exception e) {
            outError(e.toString());
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public boolean prepareRows() {
        try {
            if (selectedColumns == null || selectedColumns.isEmpty()) {
                outError(message("SelectToHandle"));
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
            for (Data2DColumn column : selectedColumns) {
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
            outError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean handleRows() {
        try {
            if (checkedRowsIndices == null || checkedRowsIndices.isEmpty()) {
                outError(message("SelectToHandle"));
                return false;
            }
            int colsLen = checkedColsIndices.size();
            double[] sum = new double[colsLen];
            for (int r : checkedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                for (int c = 0; c < colsLen; c++) {
                    sum[c] += data2D.doubleValue(tableRow.get(checkedColsIndices.get(c) + 1));
                }
            }
            handledData = new ArrayList<>();
            int scale = data2D.getScale();
            for (int r : checkedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                List<String> row = new ArrayList<>();
                row.add((r + 1) + "");
                for (int c = 0; c < colsLen; c++) {
                    double d = data2D.doubleValue(tableRow.get(checkedColsIndices.get(c) + 1));
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
            outError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean handleFile() {
        handleFile = data2D.percentage(handledNames, checkedRowsIndices, valuesCheck.isSelected());
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
            outError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean outputExternal() {
        if (selectController.isAllData() && data2D.isMutiplePages()) {
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
