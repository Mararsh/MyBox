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
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
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
public class Data2DPercentageController extends BaseData2DTaskTargetsController {

    protected File handleFile;

    @FXML
    protected ToggleGroup negativeGroup;
    @FXML
    protected RadioButton negativeSkipRadio, negativeZeroRadio, negativeAbsRadio;

    public Data2DPercentageController() {
        baseTitle = message("ValuePercentage");
    }

    @Override
    public void initOptions() {
        try {
            super.initOptions();

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
    public boolean checkOptions() {
        try {
            if (!super.checkOptions()) {
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
            List<Integer> filteredRowsIndices = sourceController.filteredRowsIndices;
            if (filteredRowsIndices == null || filteredRowsIndices.isEmpty()) {
                if (task != null) {
                    task.setError(message("NoData"));
                }
                return false;
            }
            switch (objectType) {
                case Rows:
                    return dataByRows(filteredRowsIndices);
                case All:
                    return dataByAll(filteredRowsIndices);
                default:
                    return dataByColumns(filteredRowsIndices);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public boolean dataByColumns(List<Integer> filteredRowsIndices) {
        try {
            List<Integer> colIndices = checkedColsIndices;
            int colsLen = colIndices.size();
            double[] sum = new double[colsLen];
            for (int r : filteredRowsIndices) {
                List<String> tableRow = sourceController.tableData.get(r);
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
                List<String> tableRow = sourceController.tableData.get(r);
                row = new ArrayList<>();
                row.add("" + (r + 1));
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

    public boolean dataByRows(List<Integer> filteredRowsIndices) {
        try {
            List<Integer> colIndices = checkedColsIndices;
            outputData = new ArrayList<>();
            int otherColsNumber = otherColsIndices != null ? otherColsIndices.size() : 0;
            for (int r : filteredRowsIndices) {
                double sum = 0d;
                List<String> row = new ArrayList<>();
                row.add("" + (r + 1));
                List<String> tableRow = sourceController.tableData.get(r);
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

    public boolean dataByAll(List<Integer> filteredRowsIndices) {
        try {
            List<Integer> colIndices = checkedColsIndices;
            double sum = 0d;
            for (int r : filteredRowsIndices) {
                List<String> tableRow = sourceController.tableData.get(r);
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
                List<String> tableRow = sourceController.tableData.get(r);
                row = new ArrayList<>();
                row.add("" + (r + 1));
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
    public boolean handleAllData(FxTask currentTask, Data2DWriter writer) {
        String toNegative;
        if (negativeSkipRadio.isSelected()) {
            toNegative = "skip";
        } else if (negativeAbsRadio.isSelected()) {
            toNegative = "abs";
        } else {
            toNegative = "zero";
        }
        switch (objectType) {
            case Rows:
                return data2D.percentageRows(currentTask, writer,
                        checkedColsIndices, otherColsIndices,
                        scale, toNegative, invalidAs);
            case All:
                return data2D.percentageAll(currentTask, writer,
                        checkedColsIndices, otherColsIndices,
                        scale, toNegative, invalidAs);
            default:
                return data2D.percentageColumns(currentTask, writer,
                        checkedColsIndices, otherColsIndices,
                        scale, toNegative, invalidAs);
        }
    }

    /*
        static
     */
    public static Data2DPercentageController open(BaseData2DLoadController tableController) {
        try {
            Data2DPercentageController controller = (Data2DPercentageController) WindowTools.referredStage(
                    tableController, Fxmls.Data2DPercentageFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
