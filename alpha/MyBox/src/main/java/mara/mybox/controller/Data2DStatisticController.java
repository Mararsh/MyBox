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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.data.StatisticSelection;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
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

    protected List<String> countRow, summationRow, meanRow, geometricMeanRow, sumOfSquaresRow,
            populationVarianceRow, sampleVarianceRow, populationStandardDeviationRow, sampleStandardDeviationRow, skewnessRow,
            maximumRow, minimumRow, medianRow, upperQuartileRow, lowerQuartileRow, modeRow;
    protected List<String> handledNames;
    protected StatisticSelection selections;

    @FXML
    protected CheckBox countCheck, summationCheck, meanCheck, geometricMeanCheck, sumOfSquaresCheck,
            populationVarianceCheck, sampleVarianceCheck, populationStandardDeviationCheck, sampleStandardDeviationCheck, skewnessCheck,
            maximumCheck, minimumCheck, medianCheck, upperQuartileCheck, lowerQuartileCheck, modeCheck;

    @FXML
    protected Label memoryNoticeLabel;
    @FXML
    protected VBox operationBox;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(summationCheck, new Tooltip("sum(V1, V2, ..., Vn) \n= V1 + V2 + ... + Vn \n= Σ(Vi)"));
            NodeStyleTools.setTooltip(meanCheck, new Tooltip("mean(V1, V2, ..., Vn) \n= (V1 + V2 + ... + Vn) / n \n= Σ(Vi) / n"));
            NodeStyleTools.setTooltip(geometricMeanCheck, new Tooltip("geometricMean(V1, V2, ..., Vn) \n= (V1 * V2 * ... * Vn)^(1/n)"));
            NodeStyleTools.setTooltip(sumOfSquaresCheck, new Tooltip("sumOfSquares(V1, V2, ..., Vn) \n= V1^2 + V2^2 + ... + Vn^2 \n= Σ(Vi^2)"));
            NodeStyleTools.setTooltip(populationVarianceCheck, new Tooltip("populationVariance(V1, V2, ..., Vn) \n= Σ((Vi-mean)^2) / n"));
            NodeStyleTools.setTooltip(sampleVarianceCheck, new Tooltip("sampleVariance(V1, V2, ..., Vn) \n= Σ((Vi-mean)^2) / (n-1)"));
            NodeStyleTools.setTooltip(populationStandardDeviationCheck, new Tooltip("populationStandardDeviation(V1, V2, ..., Vn) \n= √(populationVariance) \n= populationVariance^(1/2)"));
            NodeStyleTools.setTooltip(sampleStandardDeviationCheck, new Tooltip("sampleStandardDeviation(V1, V2, ..., Vn) \n= √(sampleVariance) \n= sampleVariance^(1/2)"));
            NodeStyleTools.setTooltip(medianCheck, new Tooltip("50%"));
            NodeStyleTools.setTooltip(upperQuartileCheck, new Tooltip("25%"));
            NodeStyleTools.setTooltip(lowerQuartileCheck, new Tooltip("75%"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

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

            geometricMeanCheck.setSelected(UserConfig.getBoolean(baseName + "GeometricMean", false));
            geometricMeanCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "GeometricMean", geometricMeanCheck.isSelected());
                }
            });

            sumOfSquaresCheck.setSelected(UserConfig.getBoolean(baseName + "SumOfSquares", false));
            sumOfSquaresCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "SumOfSquares", sumOfSquaresCheck.isSelected());
                }
            });

            populationVarianceCheck.setSelected(UserConfig.getBoolean(baseName + "PopulationVariance", false));
            populationVarianceCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "PopulationVariance", populationVarianceCheck.isSelected());
                }
            });

            sampleVarianceCheck.setSelected(UserConfig.getBoolean(baseName + "SampleVariance", false));
            sampleVarianceCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "SampleVariance", sampleVarianceCheck.isSelected());
                }
            });

            populationStandardDeviationCheck.setSelected(UserConfig.getBoolean(baseName + "PopulationStandardDeviation", false));
            populationStandardDeviationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "PopulationStandardDeviation", populationStandardDeviationCheck.isSelected());
                }
            });

            sampleStandardDeviationCheck.setSelected(UserConfig.getBoolean(baseName + "SampleStandardDeviation", false));
            sampleStandardDeviationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "SampleStandardDeviation", sampleStandardDeviationCheck.isSelected());
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

            upperQuartileCheck.setSelected(UserConfig.getBoolean(baseName + "UpperQuartile", true));
            upperQuartileCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "UpperQuartile", upperQuartileCheck.isSelected());
                }
            });

            lowerQuartileCheck.setSelected(UserConfig.getBoolean(baseName + "LowerQuartile", true));
            lowerQuartileCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "LowerQuartile", lowerQuartileCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        selections = new StatisticSelection()
                .setCount(countCheck.isSelected())
                .setSum(summationCheck.isSelected())
                .setMean(meanCheck.isSelected())
                .setGeometricMean(geometricMeanCheck.isSelected())
                .setSumSquares(sumOfSquaresCheck.isSelected())
                .setPopulationStandardDeviation(populationStandardDeviationCheck.isSelected())
                .setPopulationVariance(populationVarianceCheck.isSelected())
                .setSampleStandardDeviation(sampleStandardDeviationCheck.isSelected())
                .setSampleVariance(sampleVarianceCheck.isSelected())
                .setSkewness(skewnessCheck.isSelected())
                .setMedian(medianCheck.isSelected())
                .setMaximum(maximumCheck.isSelected())
                .setMinimum(minimumCheck.isSelected())
                .setUpperQuartile(upperQuartileCheck.isSelected())
                .setLowerQuartile(lowerQuartileCheck.isSelected())
                .setMode(modeCheck.isSelected());
        checkMemoryLabel();
        return ok;
    }

    public void checkMemoryLabel() {
        if (!data2D.isTable() && sourceController.allPages() && selections.needStored()) {
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
        sumOfSquaresCheck.setSelected(true);
        skewnessCheck.setSelected(true);
        maximumCheck.setSelected(true);
        minimumCheck.setSelected(true);
        geometricMeanCheck.setSelected(true);
        populationVarianceCheck.setSelected(true);
        sampleVarianceCheck.setSelected(true);
        populationStandardDeviationCheck.setSelected(true);
        sampleStandardDeviationCheck.setSelected(true);
        modeCheck.setSelected(true);
        medianCheck.setSelected(true);
        upperQuartileCheck.setSelected(true);
        lowerQuartileCheck.setSelected(true);
    }

    @FXML
    @Override
    public void selectNoneAction() {
        countCheck.setSelected(false);
        summationCheck.setSelected(false);
        meanCheck.setSelected(false);
        sumOfSquaresCheck.setSelected(false);
        skewnessCheck.setSelected(false);
        maximumCheck.setSelected(false);
        minimumCheck.setSelected(false);
        modeCheck.setSelected(false);
        medianCheck.setSelected(false);
        geometricMeanCheck.setSelected(false);
        populationVarianceCheck.setSelected(false);
        sampleVarianceCheck.setSelected(false);
        populationStandardDeviationCheck.setSelected(false);
        sampleStandardDeviationCheck.setSelected(false);
        upperQuartileCheck.setSelected(false);
        lowerQuartileCheck.setSelected(false);
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
                        if (selections.needStored()) {
                            if (data2D instanceof DataTable) {
                                return statisticAllInTable();
                            } else {
                                return statisticRows(data2D.allRows(sourceController.checkedColsIndices(), false));
                            }
                        } else {
                            return statisticAllWithoutStored();
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

            geometricMeanRow = null;
            if (geometricMeanCheck.isSelected()) {
                geometricMeanRow = new ArrayList<>();
                geometricMeanRow.add(message("GeometricMean"));
                handledData.add(geometricMeanRow);
            }

            sumOfSquaresRow = null;
            if (sumOfSquaresCheck.isSelected()) {
                sumOfSquaresRow = new ArrayList<>();
                sumOfSquaresRow.add(message("SumOfSquares"));
                handledData.add(sumOfSquaresRow);
            }

            populationVarianceRow = null;
            if (populationVarianceCheck.isSelected()) {
                populationVarianceRow = new ArrayList<>();
                populationVarianceRow.add(message("PopulationVariance"));
                handledData.add(populationVarianceRow);
            }

            sampleVarianceRow = null;
            if (sampleVarianceCheck.isSelected()) {
                sampleVarianceRow = new ArrayList<>();
                sampleVarianceRow.add(message("SampleVariance"));
                handledData.add(sampleVarianceRow);
            }

            populationStandardDeviationRow = null;
            if (populationStandardDeviationCheck.isSelected()) {
                populationStandardDeviationRow = new ArrayList<>();
                populationStandardDeviationRow.add(message("PopulationStandardDeviation"));
                handledData.add(populationStandardDeviationRow);
            }

            sampleStandardDeviationRow = null;
            if (sampleStandardDeviationCheck.isSelected()) {
                sampleStandardDeviationRow = new ArrayList<>();
                sampleStandardDeviationRow.add(message("SampleStandardDeviation"));
                handledData.add(sampleStandardDeviationRow);
            }

            skewnessRow = null;
            if (skewnessCheck.isSelected()) {
                skewnessRow = new ArrayList<>();
                skewnessRow.add(message("Skewness"));
                handledData.add(skewnessRow);
            }

            minimumRow = null;
            if (minimumCheck.isSelected()) {
                minimumRow = new ArrayList<>();
                minimumRow.add(message("MinimumQ0"));
                handledData.add(minimumRow);
            }

            upperQuartileRow = null;
            if (upperQuartileCheck.isSelected()) {
                upperQuartileRow = new ArrayList<>();
                upperQuartileRow.add(message("UpperQuartile"));
                handledData.add(upperQuartileRow);
            }

            medianRow = null;
            if (medianCheck.isSelected()) {
                medianRow = new ArrayList<>();
                medianRow.add(message("Median"));
                handledData.add(medianRow);
            }

            lowerQuartileRow = null;
            if (lowerQuartileCheck.isSelected()) {
                lowerQuartileRow = new ArrayList<>();
                lowerQuartileRow.add(message("LowerQuartile"));
                handledData.add(lowerQuartileRow);
            }

            maximumRow = null;
            if (maximumCheck.isSelected()) {
                maximumRow = new ArrayList<>();
                maximumRow.add(message("MaximumQ4"));
                handledData.add(maximumRow);
            }

            modeRow = null;
            if (modeCheck.isSelected()) {
                modeRow = new ArrayList<>();
                modeRow.add(message("Mode"));
                handledData.add(modeRow);
            }

            if (handledData.size() < 1) {
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

    public boolean writeRows(DoubleStatistic statistic, int scale) {
        if (statistic == null) {
            return false;
        }
        if (countRow != null) {
            countRow.add(StringTools.format(statistic.getCount()));
        }
        if (summationRow != null) {
            summationRow.add(DoubleTools.format(statistic.getSum(), scale));
        }
        if (meanRow != null) {
            meanRow.add(DoubleTools.format(statistic.getMean(), scale));
        }
        if (maximumRow != null) {
            maximumRow.add(DoubleTools.format(statistic.getMaximum(), scale));
        }
        if (minimumRow != null) {
            minimumRow.add(DoubleTools.format(statistic.getMinimum(), scale));
        }
        if (geometricMeanRow != null) {
            geometricMeanRow.add(DoubleTools.format(statistic.getGeometricMean(), scale));
        }
        if (sumOfSquaresRow != null) {
            sumOfSquaresRow.add(DoubleTools.format(statistic.getSumSquares(), scale));
        }
        if (populationVarianceRow != null) {
            populationVarianceRow.add(DoubleTools.format(statistic.getPopulationVariance(), scale));
        }
        if (sampleVarianceRow != null) {
            sampleVarianceRow.add(DoubleTools.format(statistic.getSampleVariance(), scale));
        }
        if (populationStandardDeviationRow != null) {
            populationStandardDeviationRow.add(DoubleTools.format(statistic.getPopulationStandardDeviation(), scale));
        }
        if (sampleStandardDeviationRow != null) {
            sampleStandardDeviationRow.add(DoubleTools.format(statistic.getSampleStandardDeviation(), scale));
        }
        if (skewnessRow != null) {
            skewnessRow.add(DoubleTools.format(statistic.getSkewness(), scale));
        }
        return true;
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
                DoubleStatistic statistic = new DoubleStatistic(colData, selections);
                writeRows(statistic, scale);
                if (medianRow != null) {
                    medianRow.add(DoubleTools.format(statistic.getMedian(), scale));
                }
                if (upperQuartileRow != null) {
                    upperQuartileRow.add(DoubleTools.format(statistic.getUpperQuartile(), scale));
                }
                if (lowerQuartileRow != null) {
                    lowerQuartileRow.add(DoubleTools.format(statistic.getLowerQuartile(), scale));
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

    public boolean statisticAllWithoutStored() {
        DoubleStatistic[] statisticData = data2D.statisticData(sourceController.checkedColsIndices, selections);
        if (statisticData == null) {
            return false;
        }
        int scale = data2D.getScale();
        for (DoubleStatistic statistic : statisticData) {
            writeRows(statistic, scale);
        }
        return true;
    }

    public boolean statisticAllInTable() {
        if (!statisticAllWithoutStored() || !(data2D instanceof DataTable)) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            List<Integer> cols = sourceController.checkedColsIndices();
            DataTable dataTable = (DataTable) data2D;
            int scale = dataTable.getScale();
            for (int c : cols) {
                Data2DColumn column = data2D.getColumns().get(c);
                if (medianRow != null) {
                    Object median = dataTable.percentile(conn, column, 50);
                    if (column.isNumberType()) {
                        medianRow.add(DoubleTools.format(Double.valueOf(median + ""), scale));
                    } else {
                        medianRow.add(column.toString(median));
                    }
                }
                if (upperQuartileRow != null) {
                    Object o = dataTable.percentile(conn, column, 25);
                    if (column.isNumberType()) {
                        upperQuartileRow.add(DoubleTools.format(Double.valueOf(o + ""), scale));
                    } else {
                        upperQuartileRow.add(column.toString(o));
                    }
                }
                if (lowerQuartileRow != null) {
                    Object o = dataTable.percentile(conn, column, 75);
                    if (column.isNumberType()) {
                        lowerQuartileRow.add(DoubleTools.format(Double.valueOf(o + ""), scale));
                    } else {
                        lowerQuartileRow.add(column.toString(o));
                    }
                }
                if (modeRow != null) {
                    Object mode = dataTable.mode(conn, column.getColumnName());
                    if (column.isNumberType()) {
                        modeRow.add(DoubleTools.format(Double.valueOf(mode + ""), scale));
                    } else {
                        modeRow.add(column.toString(mode));
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
