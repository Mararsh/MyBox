package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.NumberTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-13
 * @License Apache License Version 2.0
 */
public class Data2DPercentageController extends BaseData2DTargetsController {

    protected File handleFile;

    @FXML
    protected ToggleGroup negativeGroup;
    @FXML
    protected RadioButton negativeSkipRadio, negativeZeroRadio, negativeAbsRadio;

    public Data2DPercentageController() {
        baseTitle = message("ValuePercentage");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            String toNegative = UserConfig.getString(baseName + "ToNegative", "skip");
            if ("zero".equals(toNegative)) {
                negativeZeroRadio.setSelected(true);
            } else if ("abs".equals(toNegative)) {
                negativeAbsRadio.setSelected(true);
            } else {
                negativeSkipRadio.setSelected(true);
            }
            negativeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (negativeZeroRadio.isSelected()) {
                        UserConfig.setString(baseName + "ToNegative", "zero");
                    } else if (negativeAbsRadio.isSelected()) {
                        UserConfig.setString(baseName + "ToNegative", "abs");
                    } else {
                        UserConfig.setString(baseName + "ToNegative", "skip");
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            outputColumns = data2D.makePercentageColumns(checkedColsIndices, otherColsIndices, objectType);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean handleRows() {
        try {
            filteredRowsIndices = filteredRowsIndices();
            if (filteredRowsIndices == null || filteredRowsIndices.isEmpty()) {
                if (task != null) {
                    task.setError(message("NoData"));
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
            return false;
        }
    }

    public boolean dataByColumns() {
        try {
            List<Integer> colIndices = checkedColsIndices;
            int colsLen = colIndices.size();
            double[] sum = new double[colsLen];
            for (int r : filteredRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                for (int c = 0; c < colsLen; c++) {
                    double d = DoubleTools.toDouble(tableRow.get(colIndices.get(c) + 1), invalidAs);
                    if (DoubleTools.invalidDouble(d)) {
                    } else if (d < 0) {
                        if (negativeAbsRadio.isSelected()) {
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
                row.add(NumberTools.format(sum[c], scale));
            }
            int otherColsNumber = otherColsIndices != null ? otherColsIndices.size() : 0;
            if (otherColsNumber > 0) {
                for (int c = 0; c < otherColsNumber; c++) {
                    row.add(null);
                }
            }
            outputData.add(row);
            for (int r : filteredRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                row = new ArrayList<>();
                row.add(message("Row") + (r + 1));
                for (int c = 0; c < colsLen; c++) {
                    String s = tableRow.get(colIndices.get(c) + 1);
                    double d = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(d)) {
                        row.add(Double.NaN + "");
                    } else if (sum[c] == 0) {
                        row.add("0");
                    } else {
                        if (d < 0) {
                            if (negativeSkipRadio.isSelected()) {
                                row.add(Double.NaN + "");
                                continue;
                            } else if (negativeAbsRadio.isSelected()) {
                                d = Math.abs(d);
                            } else {
                                d = 0;
                            }
                        }
                        row.add(DoubleTools.percentage(d, sum[c], scale));
                    }
                }
                if (otherColsNumber > 0) {
                    for (int c = 0; c < otherColsNumber; c++) {
                        row.add(tableRow.get(otherColsIndices.get(c) + 1));
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
            List<Integer> colIndices = checkedColsIndices;
            outputData = new ArrayList<>();
            int otherColsNumber = otherColsIndices != null ? otherColsIndices.size() : 0;
            for (int r : filteredRowsIndices) {
                double sum = 0d;
                List<String> row = new ArrayList<>();
                row.add(message("Row") + (r + 1));
                List<String> tableRow = tableController.tableData.get(r);
                for (int c : colIndices) {
                    double d = DoubleTools.toDouble(tableRow.get(c + 1), invalidAs);
                    if (DoubleTools.invalidDouble(d)) {
                    } else if (d < 0) {
                        if (negativeAbsRadio.isSelected()) {
                            sum += Math.abs(d);
                        }
                    } else if (d > 0) {
                        sum += d;
                    }
                }
                row.add(NumberTools.format(sum, scale));
                for (int c : colIndices) {
                    String s = tableRow.get(c + 1);
                    double d = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(d)) {
                        row.add(Double.NaN + "");
                    } else if (sum == 0) {
                        row.add("0");
                    } else {
                        if (d < 0) {
                            if (negativeSkipRadio.isSelected()) {
                                row.add(Double.NaN + "");
                                continue;
                            } else if (negativeAbsRadio.isSelected()) {
                                d = Math.abs(d);
                            } else {
                                d = 0;
                            }
                        }
                        row.add(DoubleTools.percentage(d, sum, scale));
                    }
                }
                if (otherColsNumber > 0) {
                    for (int c = 0; c < otherColsNumber; c++) {
                        row.add(tableRow.get(otherColsIndices.get(c) + 1));
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
            List<Integer> colIndices = checkedColsIndices;
            double sum = 0d;
            for (int r : filteredRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                for (int c : colIndices) {
                    double d = DoubleTools.toDouble(tableRow.get(c + 1), invalidAs);
                    if (DoubleTools.invalidDouble(d)) {
                    } else if (d < 0) {
                        if (negativeAbsRadio.isSelected()) {
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
            row.add(NumberTools.format(sum, scale));
            for (int c : colIndices) {
                row.add(null);
            }
            int otherColsNumber = otherColsIndices != null ? otherColsIndices.size() : 0;
            if (otherColsNumber > 0) {
                for (int c = 0; c < otherColsNumber; c++) {
                    row.add(null);
                }
            }
            outputData.add(row);
            for (int r : filteredRowsIndices) {
                List<String> tableRow = tableController.tableData.get(r);
                row = new ArrayList<>();
                row.add(message("Row") + (r + 1) + "");
                for (int c : colIndices) {
                    String s = tableRow.get(c + 1);
                    double d = DoubleTools.toDouble(s, invalidAs);
                    if (DoubleTools.invalidDouble(d)) {
                        row.add(Double.NaN + "");
                    } else if (sum == 0) {
                        row.add("0");
                    } else {
                        if (d < 0) {
                            if (negativeSkipRadio.isSelected()) {
                                row.add(Double.NaN + "");
                                continue;
                            } else if (negativeAbsRadio.isSelected()) {
                                d = Math.abs(d);
                            } else {
                                d = 0;
                            }
                        }
                        row.add(DoubleTools.percentage(d, sum, scale));
                    }
                }
                if (otherColsNumber > 0) {
                    for (int c = 0; c < otherColsNumber; c++) {
                        row.add(tableRow.get(otherColsIndices.get(c) + 1));
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
        String toNegative;
        if (negativeSkipRadio.isSelected()) {
            toNegative = "skip";
        } else if (negativeAbsRadio.isSelected()) {
            toNegative = "abs";
        } else {
            toNegative = "zero";
        }
        String name = targetController.name();
        switch (objectType) {
            case Rows:
                return data2D.percentageRows(name, checkedColsIndices, otherColsIndices,
                        scale, toNegative, invalidAs);
            case All:
                return data2D.percentageAll(name, checkedColsIndices, otherColsIndices,
                        scale, toNegative, invalidAs);
            default:
                return data2D.percentageColumns(name, checkedColsIndices, otherColsIndices,
                        scale, toNegative, invalidAs);
        }
    }

    /*
        static
     */
    public static Data2DPercentageController open(ControlData2DLoad tableController) {
        try {
            Data2DPercentageController controller = (Data2DPercentageController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DPercentageFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
