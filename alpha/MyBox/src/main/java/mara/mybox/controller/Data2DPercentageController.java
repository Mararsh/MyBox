package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data2d.DataFileCSV;
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
public class Data2DPercentageController extends BaseData2DHandleController {

    protected File handleFile;
    protected List<String> handledNames;

    @FXML
    protected CheckBox valuesCheck;
    @FXML
    protected ToggleGroup negativeGroup;
    @FXML
    protected RadioButton zeroRadio, absRadio;

    public Data2DPercentageController() {
        baseTitle = message("ValuePercentage");
    }

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

            if (UserConfig.getBoolean(baseName + "NegativeAsZero", true)) {
                zeroRadio.fire();
            } else {
                absRadio.fire();
            }
            negativeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    UserConfig.setBoolean(baseName + "NegativeAsZero", zeroRadio.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        targetController.setNotInTable(sourceController.allPages());
        return super.checkOptions();
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (!checkOptions() || !prepare()) {
                return;
            }
            if (sourceController.allPages()) {
                handleAllTask();
            } else {
                handleRowsTask();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public boolean prepare() {
        try {
            switch (objectType) {
                case Rows:
                    return prepareByRows();
                case All:
                    return prepareByColumns(message("PercentageInAll"));
                default:
                    return prepareByColumns(message("PercentageInColumn"));
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean prepareByColumns(String suffix) {
        try {
            List<Data2DColumn> cols = sourceController.checkedCols();
            if (cols == null || cols.isEmpty()) {
                return false;
            }
            handledNames = new ArrayList<>();
            outputColumns = new ArrayList<>();
            String cName = message("SourceRowNumber");
            while (handledNames.contains(cName)) {
                cName += "m";
            }
            handledNames.add(cName);
            outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.String));
            for (Data2DColumn column : cols) {
                if (valuesCheck.isSelected()) {
                    outputColumns.add(column.cloneAll());
                    handledNames.add(column.getColumnName());
                }
                cName = column.getColumnName() + "_" + suffix;
                while (handledNames.contains(cName)) {
                    cName += "m";
                }
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
                handledNames.add(cName);
            }
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean prepareByRows() {
        try {
            List<Data2DColumn> cols = sourceController.checkedCols();
            if (cols == null || cols.isEmpty()) {
                return false;
            }
            handledNames = new ArrayList<>();
            outputColumns = new ArrayList<>();

            String cName = message("SourceRowNumber");
            while (handledNames.contains(cName)) {
                cName += "m";
            }
            handledNames.add(cName);
            outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.String));

            cName = message("Row") + "-" + message("Summation");
            while (handledNames.contains(cName)) {
                cName += "m";
            }
            handledNames.add(cName);
            outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));

            for (Data2DColumn column : cols) {
                if (valuesCheck.isSelected()) {
                    outputColumns.add(column.cloneAll());
                    handledNames.add(column.getColumnName());
                }
                cName = column.getColumnName() + "_" + message("PercentageInRow");
                while (handledNames.contains(cName)) {
                    cName += "m";
                }
                outputColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
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
            if (sourceController.checkedRowsIndices == null || sourceController.checkedRowsIndices.isEmpty()) {
                if (task != null) {
                    task.setError(message("SelectToHandle"));
                }
                return false;
            }
            switch (objectType) {
                case Rows:
                    return dataByRows();
                case All:
                    return dataByAll();
                default:
                    return dataByColumns();
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean dataByColumns() {
        try {
            List<Integer> colIndices = sourceController.checkedColsIndices;
            int colsLen = colIndices.size();
            double[] sum = new double[colsLen];
            for (int r : sourceController.checkedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                for (int c = 0; c < colsLen; c++) {
                    double d = data2D.doubleValue(tableRow.get(colIndices.get(c) + 1));
                    if (d < 0) {
                        if (!zeroRadio.isSelected()) {
                            sum[c] += Math.abs(d);
                        }
                    } else if (d > 0) {
                        sum[c] += d;
                    }
                }
            }
            outputData = new ArrayList<>();
            List<String> row = new ArrayList<>();
            row.add(message("Column") + "-" + message("Summation"));
            for (int c = 0; c < colsLen; c++) {
                row.add(DoubleTools.format(sum[c], scale));
                if (valuesCheck.isSelected()) {
                    row.add("100");
                }
            }
            outputData.add(row);
            for (int r : sourceController.checkedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                row = new ArrayList<>();
                row.add(message("Row") + (r + 1));
                for (int c = 0; c < colsLen; c++) {
                    double d = data2D.doubleValue(tableRow.get(colIndices.get(c) + 1));
                    if (valuesCheck.isSelected()) {
                        row.add(DoubleTools.format(d, scale));
                    }
                    if (sum[c] == 0) {
                        row.add("0");
                    } else {
                        if (d < 0) {
                            if (!zeroRadio.isSelected()) {
                                d = Math.abs(d);
                            } else {
                                d = 0;
                            }
                        }
                        row.add(DoubleTools.percentage(d, sum[c], scale));
                    }
                }
                outputData.add(row);
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

    public boolean dataByRows() {
        try {
            List<Integer> colIndices = sourceController.checkedColsIndices;
            outputData = new ArrayList<>();
            for (int r : sourceController.checkedRowsIndices) {
                double sum = 0d;
                List<String> row = new ArrayList<>();
                row.add(message("Row") + (r + 1));
                List<String> tableRow = tableController.tableData.get(r);
                for (int c : colIndices) {
                    double d = data2D.doubleValue(tableRow.get(c + 1));
                    if (d < 0) {
                        if (!zeroRadio.isSelected()) {
                            sum += Math.abs(d);
                        }
                    } else if (d > 0) {
                        sum += d;
                    }
                }
                row.add(DoubleTools.format(sum, scale));
                for (int c : colIndices) {
                    double d = data2D.doubleValue(tableRow.get(c + 1));
                    if (valuesCheck.isSelected()) {
                        row.add(DoubleTools.format(d, scale));
                    }
                    if (sum == 0) {
                        row.add("0");
                    } else {
                        if (d < 0) {
                            if (!zeroRadio.isSelected()) {
                                d = Math.abs(d);
                            } else {
                                d = 0;
                            }
                        }
                        row.add(DoubleTools.percentage(d, sum, scale));
                    }
                }
                outputData.add(row);
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

    public boolean dataByAll() {
        try {
            List<Integer> colIndices = sourceController.checkedColsIndices;
            double sum = 0d;
            for (int r : sourceController.checkedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                for (int c : colIndices) {
                    double d = data2D.doubleValue(tableRow.get(c + 1));
                    if (d < 0) {
                        if (!zeroRadio.isSelected()) {
                            sum += Math.abs(d);
                        }
                    } else if (d > 0) {
                        sum += d;
                    }
                }
            }
            outputData = new ArrayList<>();
            List<String> row = new ArrayList<>();
            row.add(message("All") + "-" + message("Summation"));
            row.add(DoubleTools.format(sum, scale));
            if (valuesCheck.isSelected()) {
                row.add("100");
            }
            for (int c : colIndices) {
                row.add(null);
                if (valuesCheck.isSelected()) {
                    row.add(null);
                }
            }
            outputData.add(row);
            for (int r : sourceController.checkedRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                row = new ArrayList<>();
                row.add(message("Row") + (r + 1) + "");
                for (int c : colIndices) {
                    double d = data2D.doubleValue(tableRow.get(c + 1));
                    if (valuesCheck.isSelected()) {
                        row.add(DoubleTools.format(d, scale));
                    }
                    if (sum == 0) {
                        row.add("0");
                    } else {
                        if (d < 0) {
                            if (!zeroRadio.isSelected()) {
                                d = Math.abs(d);
                            } else {
                                d = 0;
                            }
                        }
                        row.add(DoubleTools.percentage(d, sum, scale));
                    }
                }
                outputData.add(row);
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
        switch (objectType) {
            case Rows:
                return data2D.percentageRows(handledNames, sourceController.checkedColsIndices,
                        valuesCheck.isSelected(), !zeroRadio.isSelected(), scale);
            case All:
                return data2D.percentageAll(handledNames, sourceController.checkedColsIndices,
                        valuesCheck.isSelected(), !zeroRadio.isSelected(), scale);
            default:
                return data2D.percentageColumns(handledNames, sourceController.checkedColsIndices,
                        valuesCheck.isSelected(), !zeroRadio.isSelected(), scale);
        }
    }

    /*
        static
     */
    public static Data2DPercentageController open(ControlData2DEditTable tableController) {
        try {
            Data2DPercentageController controller = (Data2DPercentageController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DPercentageFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
