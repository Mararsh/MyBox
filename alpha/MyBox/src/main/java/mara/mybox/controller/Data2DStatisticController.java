package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-12
 * @License Apache License Version 2.0
 */
public class Data2DStatisticController extends Data2DHandleController {

    protected List<String> countRow, summationRow, meanRow, varianceRow, skewnessRow,
            maximumRow, minimumRow, modeRow, medianRow;
    protected List<String> handledNames;

    @FXML
    protected CheckBox countCheck, summationCheck, meanCheck, varianceCheck, skewnessCheck,
            maximumCheck, minimumCheck, modeCheck, medianCheck;
    @FXML
    protected Label memoryNoticeLabel;
    @FXML
    protected VBox operationBox;

    @Override
    public void initControls() {
        try {
            super.initControls();

            countCheck.setSelected(UserConfig.getBoolean(baseName + "Count", true));
            countCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Count", countCheck.isSelected());
                }
            });

            summationCheck.setSelected(UserConfig.getBoolean(baseName + "Summation", true));
            summationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Summation", summationCheck.isSelected());
                }
            });

            meanCheck.setSelected(UserConfig.getBoolean(baseName + "Mean", true));
            meanCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Mean", meanCheck.isSelected());
                }
            });

            varianceCheck.setSelected(UserConfig.getBoolean(baseName + "Variance", false));
            varianceCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Variance", varianceCheck.isSelected());
                }
            });

            skewnessCheck.setSelected(UserConfig.getBoolean(baseName + "Skewness", false));
            skewnessCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Skewness", skewnessCheck.isSelected());
                }
            });

            maximumCheck.setSelected(UserConfig.getBoolean(baseName + "Maximum", false));
            maximumCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Maximum", maximumCheck.isSelected());
                }
            });

            minimumCheck.setSelected(UserConfig.getBoolean(baseName + "Minimum", false));
            minimumCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Minimum", minimumCheck.isSelected());
                }
            });

            modeCheck.setSelected(UserConfig.getBoolean(baseName + "Mode", false));
            modeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Mode", modeCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            medianCheck.setSelected(UserConfig.getBoolean(baseName + "Median", false));
            medianCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Median", medianCheck.isSelected());
                    checkMemoryLabel();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        checkMemoryLabel();
        return ok;
    }

    public void checkMemoryLabel() {
        if (!data2D.isTable() && sourceController.allPages()
                && (modeCheck.isSelected() || medianCheck.isSelected())) {
            if (!operationBox.getChildren().contains(memoryNoticeLabel)) {
                operationBox.getChildren().add(memoryNoticeLabel);
            }
        } else {
            if (operationBox.getChildren().contains(memoryNoticeLabel)) {
                operationBox.getChildren().remove(memoryNoticeLabel);
            }
        }
    }

    @FXML
    @Override
    public void selectAllAction() {
        countCheck.setSelected(true);
        summationCheck.setSelected(true);
        meanCheck.setSelected(true);
        varianceCheck.setSelected(true);
        skewnessCheck.setSelected(true);
        maximumCheck.setSelected(true);
        minimumCheck.setSelected(true);
        modeCheck.setSelected(true);
        medianCheck.setSelected(true);
    }

    @FXML
    @Override
    public void selectNoneAction() {
        countCheck.setSelected(false);
        summationCheck.setSelected(false);
        meanCheck.setSelected(false);
        varianceCheck.setSelected(false);
        skewnessCheck.setSelected(false);
        maximumCheck.setSelected(false);
        minimumCheck.setSelected(false);
        modeCheck.setSelected(false);
        medianCheck.setSelected(false);
    }

    @FXML
    @Override
    public void okAction() {
        if ((sourceController.allPages() && !tableController.checkBeforeLoadingTableData())
                || !checkOptions() || !prepareRows()) {
            return;
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    if (sourceController.allPages()) {
                        if (modeCheck.isSelected() || medianCheck.isSelected()) {
                            if (data2D instanceof DataTable) {
                                return statisticAllInTable();
                            } else {
                                return statisticRows(data2D.allRows(sourceController.checkedColsIndices(), false));
                            }
                        } else {
                            return statisticAllWithoutModeMedian();
                        }
                    } else {
                        return statisticRows(sourceController.selectedData(false));
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (targetController.inTable()) {
                    updateTable();
                } else {
                    outputExternal();
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.setTask(null);
                task = null;
                if (targetController != null) {
                    targetController.refreshControls();
                }
            }

        };
        start(task);
    }

    public boolean prepareRows() {
        try {
            List<String> names = sourceController.checkedColsNames();
            if (names == null || names.isEmpty()) {
                popError(message("SelectToHandle"));
                return false;
            }
            String cName = message("Calculation");
            while (names.contains(cName)) {
                cName += "m";
            }
            handledNames = new ArrayList<>();
            handledNames.add(cName);
            handledNames.addAll(names);

            handledColumns = new ArrayList<>();
            handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.String));
            for (String name : names) {
                handledColumns.add(new Data2DColumn(name, ColumnDefinition.ColumnType.Double));
            }

            handledData = new ArrayList<>();
            countRow = null;
            if (countCheck.isSelected()) {
                countRow = new ArrayList<>();
                countRow.add(message("Count"));
                handledData.add(countRow);
            }
            summationRow = null;
            if (summationCheck.isSelected()) {
                summationRow = new ArrayList<>();
                summationRow.add(message("Summation"));
                handledData.add(summationRow);
            }
            meanRow = null;
            if (meanCheck.isSelected()) {
                meanRow = new ArrayList<>();
                meanRow.add(message("Mean"));
                handledData.add(meanRow);
            }
            varianceRow = null;
            if (varianceCheck.isSelected()) {
                varianceRow = new ArrayList<>();
                varianceRow.add(message("Variance"));
                handledData.add(varianceRow);
            }
            skewnessRow = null;
            if (skewnessCheck.isSelected()) {
                skewnessRow = new ArrayList<>();
                skewnessRow.add(message("Skewness"));
                handledData.add(skewnessRow);
            }
            maximumRow = null;
            if (maximumCheck.isSelected()) {
                maximumRow = new ArrayList<>();
                maximumRow.add(message("Maximum"));
                handledData.add(maximumRow);
            }
            minimumRow = null;
            if (minimumCheck.isSelected()) {
                minimumRow = new ArrayList<>();
                minimumRow.add(message("Minimum"));
                handledData.add(minimumRow);
            }
            modeRow = null;
            if (modeCheck.isSelected()) {
                modeRow = new ArrayList<>();
                modeRow.add(message("Mode"));
                handledData.add(modeRow);
            }
            medianRow = null;
            if (medianCheck.isSelected()) {
                medianRow = new ArrayList<>();
                medianRow.add(message("Median"));
                handledData.add(medianRow);
            }
            if (handledData.size() < 2) {
                popError(message("SelectToHandle"));
                return false;
            }

            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean statisticRows(List<List<String>> rows) {
        try {
            if (rows == null || rows.isEmpty()) {
                if (task != null) {
                    task.setError(message("SelectToHandle"));
                }
                return false;
            }
            int rowsNumber = rows.size();
            int colsNumber = rows.get(0).size();
            int scale = data2D.getScale();
            for (int c = 0; c < colsNumber; c++) {
                double[] colData = new double[rowsNumber];
                for (int r = 0; r < rowsNumber; r++) {
                    colData[r] = data2D.doubleValue(rows.get(r).get(c));
                }
                DoubleStatistic statistic = new DoubleStatistic(colData, modeCheck.isSelected(), medianCheck.isSelected());
                if (countRow != null) {
                    countRow.add(StringTools.format(statistic.getCount()));
                }
                if (summationRow != null) {
                    summationRow.add(DoubleTools.format(statistic.getSum(), scale));
                }
                if (meanRow != null) {
                    meanRow.add(DoubleTools.format(statistic.getMean(), scale));
                }
                if (varianceRow != null) {
                    varianceRow.add(DoubleTools.format(statistic.getVariance(), scale));
                }
                if (skewnessRow != null) {
                    skewnessRow.add(DoubleTools.format(statistic.getSkewness(), scale));
                }
                if (maximumRow != null) {
                    maximumRow.add(DoubleTools.format(statistic.getMaximum(), scale));
                }
                if (minimumRow != null) {
                    minimumRow.add(DoubleTools.format(statistic.getMinimum(), scale));
                }
                if (modeRow != null) {
                    if (statistic.getMode() == 0) {
                        String[] colStrings = new String[rowsNumber];
                        for (int r = 0; r < rowsNumber; r++) {
                            colStrings[r] = rows.get(r).get(c);
                        }
                        Object mode = mode(colStrings);
                        modeRow.add(mode + "");
                    } else {
                        modeRow.add(DoubleTools.format(statistic.getMode(), scale));
                    }
                }
                if (medianRow != null) {
                    medianRow.add(DoubleTools.format(statistic.getMedian(), scale));
                }
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

    public static Object mode(Object[] values) {
        Object mode = null;
        try {
            if (values == null || values.length == 0) {
                return mode;
            }
            Map<Object, Integer> number = new HashMap<>();
            for (Object value : values) {
                if (number.containsKey(value)) {
                    number.put(value, number.get(value) + 1);
                } else {
                    number.put(value, 1);
                }
            }
            double num = 0;
            for (Object value : number.keySet()) {
                if (num < number.get(value)) {
                    mode = value;
                }
            }
        } catch (Exception e) {
        }
        return mode;
    }

    public boolean statisticAllWithoutModeMedian() {
        DoubleStatistic[] statisticData = data2D.statisticData(sourceController.checkedColsIndices);
        if (statisticData == null) {
            return false;
        }
        int scale = data2D.getScale();
        for (DoubleStatistic sData : statisticData) {
            if (countRow != null) {
                countRow.add(StringTools.format(sData.count));
            }
            if (summationRow != null) {
                summationRow.add(DoubleTools.format(sData.sum, scale));
            }
            if (meanRow != null) {
                meanRow.add(DoubleTools.format(sData.mean, scale));
            }
            if (varianceRow != null) {
                varianceRow.add(DoubleTools.format(sData.variance, scale));
            }
            if (skewnessRow != null) {
                skewnessRow.add(DoubleTools.format(sData.skewness, scale));
            }
            if (maximumRow != null) {
                maximumRow.add(DoubleTools.format(sData.maximum, scale));
            }
            if (minimumRow != null) {
                minimumRow.add(DoubleTools.format(sData.minimum, scale));
            }
        }
        return true;
    }

    public boolean statisticAllInTable() {
        if (!statisticAllWithoutModeMedian()) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            List<Integer> cols = sourceController.checkedColsIndices();
            DataTable dataTable = (DataTable) data2D;
            int scale = dataTable.getScale();
            for (int c : cols) {
                Data2DColumn column = data2D.getColumns().get(c);
                if (modeRow != null) {
                    Object mode = dataTable.mode(conn, column.getColumnName());
                    if (column.isNumberType()) {
                        modeRow.add(DoubleTools.format(Double.valueOf(mode + ""), scale));
                    } else {
                        modeRow.add(column.toString(mode));
                    }
                }
                if (medianRow != null) {
                    Object median = dataTable.median(conn, column);
                    if (column.isNumberType()) {
                        medianRow.add(DoubleTools.format(Double.valueOf(median + ""), scale));
                    } else {
                        medianRow.add(column.toString(median));
                    }
                }
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

    /*
        static
     */
    public static Data2DStatisticController open(ControlData2DEditTable tableController) {
        try {
            Data2DStatisticController controller = (Data2DStatisticController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DStatisticFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
