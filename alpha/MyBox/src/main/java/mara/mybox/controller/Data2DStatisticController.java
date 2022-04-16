package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.data.StatisticOptions;
import mara.mybox.data.StatisticOptions.StatisticObject;
import mara.mybox.data2d.DataFileCSV;
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
    protected StatisticOptions options;
    protected int scale;

    @FXML
    protected CheckBox countCheck, summationCheck, meanCheck, geometricMeanCheck, sumOfSquaresCheck,
            populationVarianceCheck, sampleVarianceCheck, populationStandardDeviationCheck, sampleStandardDeviationCheck, skewnessCheck,
            maximumCheck, minimumCheck, medianCheck, upperQuartileCheck, lowerQuartileCheck, modeCheck;
    @FXML
    protected RadioButton columnsRadio, rowsRadio, allRadio;
    @FXML
    protected Label memoryNoticeLabel;
    @FXML
    protected VBox operationBox;
    @FXML
    protected ComboBox<String> scaleSelector;

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

            scale = (short) UserConfig.getInt(baseName + "Scale", 2);
            if (scale < 0) {
                scale = 2;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.setValue(scale + "");
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(scaleSelector.getValue());
                            if (v >= 0 && v <= 15) {
                                scale = (short) v;
                                UserConfig.setInt(baseName + "Scale", v);
                                scaleSelector.getEditor().setStyle(null);
                            } else {
                                scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        options = new StatisticOptions()
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
                .setMode(modeCheck.isSelected())
                .setScale(scale);
        if (rowsRadio.isSelected()) {
            options.setStatisticObject(StatisticObject.Rows);
        } else if (allRadio.isSelected()) {
            options.setStatisticObject(StatisticObject.All);
        } else {
            options.setStatisticObject(StatisticObject.Columns);
        }
        checkMemoryLabel();
        return ok;
    }

    public void checkMemoryLabel() {
        if (sourceController.allPages() && options.needStored() && (!data2D.isTable() || allRadio.isSelected())) {
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
        try {
            if ((sourceController.allPages() && !tableController.checkBeforeLoadingTableData())
                    || !checkOptions() || !prepare()) {
                return;
            }
            if (sourceController.allPages()) {
                if (rowsRadio.isSelected()) {
                    handleAllTask();
                } else if (allRadio.isSelected()) {
                    handleAllByAllTask();
                } else {
                    handleAllByColumnsTask();
                }
            } else {
                handleRowsTask();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public boolean prepare() {
        try {
            switch (options.statisticObject) {
                case Rows:
                    return prepareRows();
                case All:
                    return prepareColumns(Arrays.asList(message("All")));
                default:
                    return prepareColumns(sourceController.checkedColsNames());
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean prepareColumns(List<String> names) {
        try {

            if (names == null || names.isEmpty()) {
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

    public boolean prepareRows() {
        try {
            handledNames = new ArrayList<>();
            handledColumns = new ArrayList<>();

            String cName = message("Row");
            handledNames.add(cName);
            handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.String));

            if (countCheck.isSelected()) {
                cName = message("Count");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (summationCheck.isSelected()) {
                cName = message("Summation");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (meanCheck.isSelected()) {
                cName = message("Mean");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (geometricMeanCheck.isSelected()) {
                cName = message("GeometricMean");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (sumOfSquaresCheck.isSelected()) {
                cName = message("SumOfSquares");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (populationVarianceCheck.isSelected()) {
                cName = message("PopulationVariance");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (sampleVarianceCheck.isSelected()) {
                cName = message("SampleVariance");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (populationStandardDeviationCheck.isSelected()) {
                cName = message("PopulationStandardDeviation");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (sampleStandardDeviationCheck.isSelected()) {
                cName = message("SampleStandardDeviation");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (skewnessCheck.isSelected()) {
                cName = message("Skewness");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (minimumCheck.isSelected()) {
                cName = message("MinimumQ0");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (upperQuartileCheck.isSelected()) {
                cName = message("UpperQuartile");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (medianCheck.isSelected()) {
                cName = message("Median");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (lowerQuartileCheck.isSelected()) {
                cName = message("LowerQuartile");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (maximumCheck.isSelected()) {
                cName = message("MaximumQ4");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (modeCheck.isSelected()) {
                cName = message("Mode");
                handledNames.add(cName);
                handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.Double));
            }

            if (handledNames.size() < 2) {
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

    @Override
    public boolean handleRows() {
        return statisticData(sourceController.selectedData(true));
    }

    public boolean statisticData(List<List<String>> rows) {
        try {
            if (rows == null || rows.isEmpty()) {
                if (task != null) {
                    task.setError(message("SelectToHandle"));
                }
                return false;
            }
            switch (options.statisticObject) {
                case Rows:
                    return statisticByRows(rows);
                case All:
                    return statisticByAll(rows);
                default:
                    return statisticByColumns(rows);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean statisticByColumns(List<List<String>> rows) {
        try {
            if (rows == null || rows.isEmpty()) {
                return false;
            }
            int rowsNumber = rows.size();
            int colsNumber = rows.get(0).size();
            for (int c = 1; c < colsNumber; c++) {
                String[] colData = new String[rowsNumber];
                for (int r = 0; r < rowsNumber; r++) {
                    colData[r] = rows.get(r).get(c);
                }
                DoubleStatistic statistic = new DoubleStatistic(colData, options);
                statisticByColumnsWrite(statistic);
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

    public boolean statisticByColumnsWrite(DoubleStatistic statistic) {
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
            try {
                modeRow.add(DoubleTools.format((double) statistic.getMode(), scale));
            } catch (Exception e) {
                modeRow.add(statistic.getMode().toString());
            }
        }
        return true;
    }

    public boolean statisticByRows(List<List<String>> rows) {
        try {
            if (rows == null || rows.isEmpty()) {
                return false;
            }
            handledData = new ArrayList<>();
            int rowsNumber = rows.size();
            for (int r = 0; r < rowsNumber; r++) {
                List<String> rowStatistic = new ArrayList<>();
                List<String> row = rows.get(r);
                rowStatistic.add(message("Row") + " " + row.get(0));
                int colsNumber = row.size();
                String[] rowData = new String[colsNumber - 1];
                for (int c = 1; c < colsNumber; c++) {
                    rowData[c - 1] = row.get(c);
                }
                DoubleStatistic statistic = new DoubleStatistic(rowData, options);
                rowStatistic.addAll(statistic.toStringList());
                handledData.add(rowStatistic);
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

    public boolean statisticByAll(List<List<String>> rows) {
        try {
            if (rows == null || rows.isEmpty()) {
                return false;
            }
            int rowsNumber = rows.size();
            int colsNumber = rows.get(0).size();
            String[] allData = new String[rowsNumber * (colsNumber - 1)];
            int index = 0;
            for (int r = 0; r < rowsNumber; r++) {
                for (int c = 1; c < colsNumber; c++) {
                    allData[index++] = rows.get(r).get(c);
                }
            }
            DoubleStatistic statistic = new DoubleStatistic(allData, options);
            statisticByColumnsWrite(statistic);
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public void handleAllByColumnsTask() {
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    if (options.needStored()) {
                        if (data2D instanceof DataTable) {
                            return statisticAllByColumnsInDataTable();
                        } else {
                            return statisticData(data2D.allRows(sourceController.checkedColsIndices(), true));
                        }
                    } else {
                        return statisticAllByColumnsWithoutStored();
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

    public boolean statisticAllByColumnsWithoutStored() {
        DoubleStatistic[] statisticData = data2D.statisticByColumns(sourceController.checkedColsIndices, options);
        if (statisticData == null) {
            return false;
        }
        for (DoubleStatistic statistic : statisticData) {
            statisticByColumnsWrite(statistic);
        }
        return true;
    }

    public boolean statisticAllByColumnsInDataTable() {
        if (!statisticAllByColumnsWithoutStored() || !(data2D instanceof DataTable)) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            List<Integer> cols = sourceController.checkedColsIndices();
            DataTable dataTable = (DataTable) data2D;
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

    public void handleAllByAllTask() {
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    if (options.needStored()) {
                        return statisticData(data2D.allRows(sourceController.checkedColsIndices(), true));
                    } else {
                        return statisticAllByColumnsWithoutStored();
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

    public boolean statisticAllByAllWithoutStored() {
        DoubleStatistic statisticData = data2D.statisticByAll(sourceController.checkedColsIndices, options);
        if (statisticData == null) {
            return false;
        }
        statisticByColumnsWrite(statisticData);
        return true;
    }

    @Override
    public DataFileCSV generatedFile() {
        return data2D.statisticByRows(handledNames, sourceController.checkedColsIndices(), options);
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
