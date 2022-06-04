package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DescriptiveStatistic.StatisticObject;
import mara.mybox.calculation.DoubleStatistic;
import mara.mybox.data2d.Data2D_Operations.ObjectType;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-12
 * @License Apache License Version 2.0
 */
public class Data2DStatisticController extends BaseData2DHandleController {

    protected DescriptiveStatistic calculation;
    protected int categorysCol;
    protected String selectedCategory;

    @FXML
    protected CheckBox countCheck, summationCheck, meanCheck, geometricMeanCheck, sumOfSquaresCheck,
            populationVarianceCheck, sampleVarianceCheck, populationStandardDeviationCheck, sampleStandardDeviationCheck, skewnessCheck,
            maximumCheck, minimumCheck, medianCheck, upperQuartileCheck, lowerQuartileCheck,
            UpperMildOutlierLineCheck, UpperExtremeOutlierLineCheck, LowerMildOutlierLineCheck, LowerExtremeOutlierLineCheck,
            modeCheck;
    @FXML
    protected Label memoryNoticeLabel;
    @FXML
    protected VBox operationBox, dataOptionsBox;
    @FXML
    protected FlowPane categoryColumnsPane;
    @FXML
    protected ComboBox<String> categoryColumnSelector;

    public Data2DStatisticController() {
        baseTitle = message("DescriptiveStatistics");
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(summationCheck, new Tooltip("sum(X1, X2, ..., Xn) \n= X1 + X2 + ... + Xn \n= Σ(Xi)"));
            NodeStyleTools.setTooltip(meanCheck, new Tooltip("mean(X1, X2, ..., Xn) \n= (X1 + X2 + ... + Xn) / n \n= Σ(Xi) / n"));
            NodeStyleTools.setTooltip(geometricMeanCheck, new Tooltip("geometricMean(X1, X2, ..., Xn) \n= (X1 * X2 * ... * Xn)^(1/n)"));
            NodeStyleTools.setTooltip(sumOfSquaresCheck, new Tooltip("sumOfSquares(X1, X2, ..., Xn) \n= X1^2 + X2^2 + ... + Xn^2 \n= Σ(Xi^2)"));
            NodeStyleTools.setTooltip(populationVarianceCheck, new Tooltip("populationVariance(X1, X2, ..., Xn) \n= Σ((Xi-mean)^2) / n"));
            NodeStyleTools.setTooltip(sampleVarianceCheck, new Tooltip("sampleVariance(X1, X2, ..., Xn) \n= Σ((Xi-mean)^2) / (n-1)"));
            NodeStyleTools.setTooltip(populationStandardDeviationCheck, new Tooltip("populationStandardDeviation(X1, X2, ..., Xn) \n= √(populationVariance) \n= populationVariance^(1/2)"));
            NodeStyleTools.setTooltip(sampleStandardDeviationCheck, new Tooltip("sampleStandardDeviation(X1, X2, ..., Xn) \n= √(sampleVariance) \n= sampleVariance^(1/2)"));
            NodeStyleTools.setTooltip(medianCheck, new Tooltip("50%"));
            NodeStyleTools.setTooltip(upperQuartileCheck, new Tooltip("25%"));
            NodeStyleTools.setTooltip(lowerQuartileCheck, new Tooltip("75%"));
            NodeStyleTools.setTooltip(UpperMildOutlierLineCheck, new Tooltip("Q3 + 1.5 * ( Q3 - Q1 )"));
            NodeStyleTools.setTooltip(UpperExtremeOutlierLineCheck, new Tooltip("Q3 + 3 * ( Q3 - Q1 )"));
            NodeStyleTools.setTooltip(LowerMildOutlierLineCheck, new Tooltip("Q1 - 1.5 * ( Q3 - Q1 )"));
            NodeStyleTools.setTooltip(LowerExtremeOutlierLineCheck, new Tooltip("Q1 - 3 * ( Q3 - Q1 )"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            categoryColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkOptions();
                }
            });

            countCheck.setSelected(UserConfig.getBoolean(baseName + "Count", true));
            countCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Count", countCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            summationCheck.setSelected(UserConfig.getBoolean(baseName + "Summation", true));
            summationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Summation", summationCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            meanCheck.setSelected(UserConfig.getBoolean(baseName + "Mean", true));
            meanCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Mean", meanCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            geometricMeanCheck.setSelected(UserConfig.getBoolean(baseName + "GeometricMean", false));
            geometricMeanCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "GeometricMean", geometricMeanCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            sumOfSquaresCheck.setSelected(UserConfig.getBoolean(baseName + "SumOfSquares", false));
            sumOfSquaresCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "SumOfSquares", sumOfSquaresCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            populationVarianceCheck.setSelected(UserConfig.getBoolean(baseName + "PopulationVariance", false));
            populationVarianceCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "PopulationVariance", populationVarianceCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            sampleVarianceCheck.setSelected(UserConfig.getBoolean(baseName + "SampleVariance", false));
            sampleVarianceCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "SampleVariance", sampleVarianceCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            populationStandardDeviationCheck.setSelected(UserConfig.getBoolean(baseName + "PopulationStandardDeviation", false));
            populationStandardDeviationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "PopulationStandardDeviation", populationStandardDeviationCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            sampleStandardDeviationCheck.setSelected(UserConfig.getBoolean(baseName + "SampleStandardDeviation", false));
            sampleStandardDeviationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "SampleStandardDeviation", sampleStandardDeviationCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            skewnessCheck.setSelected(UserConfig.getBoolean(baseName + "Skewness", false));
            skewnessCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Skewness", skewnessCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            maximumCheck.setSelected(UserConfig.getBoolean(baseName + "Maximum", false));
            maximumCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Maximum", maximumCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            minimumCheck.setSelected(UserConfig.getBoolean(baseName + "Minimum", false));
            minimumCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Minimum", minimumCheck.isSelected());
                    checkMemoryLabel();
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
                    checkMemoryLabel();
                }
            });

            lowerQuartileCheck.setSelected(UserConfig.getBoolean(baseName + "LowerQuartile", true));
            lowerQuartileCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "LowerQuartile", lowerQuartileCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            UpperMildOutlierLineCheck.setSelected(UserConfig.getBoolean(baseName + "UpperMildOutlierLine", true));
            UpperMildOutlierLineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "UpperMildOutlierLine", UpperMildOutlierLineCheck.isSelected());
                    checkMemoryLabel();
                }
            });

            UpperExtremeOutlierLineCheck.setSelected(UserConfig.getBoolean(baseName + "UpperExtremeOutlierLine", true));
            UpperExtremeOutlierLineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "UpperExtremeOutlierLine", UpperExtremeOutlierLineCheck.isSelected());
                    checkMemoryLabel();
                }
            });
            LowerMildOutlierLineCheck.setSelected(UserConfig.getBoolean(baseName + "LowerMildOutlierLine", true));
            LowerMildOutlierLineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "LowerMildOutlierLine", LowerMildOutlierLineCheck.isSelected());
                    checkMemoryLabel();
                }
            });
            LowerExtremeOutlierLineCheck.setSelected(UserConfig.getBoolean(baseName + "LowerExtremeOutlierLine", true));
            LowerExtremeOutlierLineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "LowerExtremeOutlierLine", LowerExtremeOutlierLineCheck.isSelected());
                    checkMemoryLabel();
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
    public void refreshControls() {
        try {
            super.refreshControls();

            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                return;
            }
            isSettingValues = true;

            selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
            names.add(0, message("RowNumber"));
            categoryColumnSelector.getItems().setAll(names);
            if (selectedCategory != null && names.contains(selectedCategory)) {
                categoryColumnSelector.setValue(selectedCategory);
            } else {
                categoryColumnSelector.getSelectionModel().select(0);
            }
            isSettingValues = false;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void objectChanged() {
        super.objectChanged();
        if (rowsRadio.isSelected()) {
            if (!dataOptionsBox.getChildren().contains(categoryColumnsPane)) {
                dataOptionsBox.getChildren().add(1, categoryColumnsPane);
            }
        } else {
            if (dataOptionsBox.getChildren().contains(categoryColumnsPane)) {
                dataOptionsBox.getChildren().remove(categoryColumnsPane);
            }
        }
        checkMemoryLabel();
    }

    @Override
    public boolean checkOptions() {
        boolean ok = super.checkOptions();
        objectChanged();

        categorysCol = -1;
        if (rowsRadio.isSelected()) {
            selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
            if (selectedCategory != null && categoryColumnSelector.getSelectionModel().getSelectedIndex() != 0) {
                categorysCol = data2D.colOrder(selectedCategory);
            }
        }
        calculation = new DescriptiveStatistic()
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
                .setUpperExtremeOutlierLine(UpperExtremeOutlierLineCheck.isSelected())
                .setUpperMildOutlierLine(UpperMildOutlierLineCheck.isSelected())
                .setLowerExtremeOutlierLine(LowerExtremeOutlierLineCheck.isSelected())
                .setLowerMildOutlierLine(LowerMildOutlierLineCheck.isSelected())
                .setMode(modeCheck.isSelected())
                .setScale(scale);
        switch (objectType) {
            case Rows:
                calculation.setStatisticObject(StatisticObject.Rows);
                break;
            case All:
                calculation.setStatisticObject(StatisticObject.All);
                break;
            default:
                calculation.setStatisticObject(StatisticObject.Columns);
                break;
        }
        calculation.setHandleController(this).setData2D(data2D)
                .setColsIndices(checkedColsIndices)
                .setColsNames(checkedColsNames)
                .setCategoryName(categorysCol >= 0 ? selectedCategory : null);
        checkMemoryLabel();
        return ok;
    }

    public void checkMemoryLabel() {
        if (isSettingValues) {
            return;
        }
        if (isAllPages() && objectType != ObjectType.Rows
                && (medianCheck.isSelected() || upperQuartileCheck.isSelected()
                || lowerQuartileCheck.isSelected() || modeCheck.isSelected()
                || UpperExtremeOutlierLineCheck.isSelected() || UpperMildOutlierLineCheck.isSelected()
                || LowerExtremeOutlierLineCheck.isSelected() || LowerMildOutlierLineCheck.isSelected())
                && (!data2D.isTable() || allRadio.isSelected())) {
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
        isSettingValues = true;
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
        UpperMildOutlierLineCheck.setSelected(true);
        UpperExtremeOutlierLineCheck.setSelected(true);
        LowerMildOutlierLineCheck.setSelected(true);
        LowerExtremeOutlierLineCheck.setSelected(true);
        isSettingValues = false;
        checkMemoryLabel();
    }

    @FXML
    @Override
    public void selectNoneAction() {
        isSettingValues = true;
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
        UpperMildOutlierLineCheck.setSelected(false);
        UpperExtremeOutlierLineCheck.setSelected(false);
        LowerMildOutlierLineCheck.setSelected(false);
        LowerExtremeOutlierLineCheck.setSelected(false);
        isSettingValues = false;
        checkMemoryLabel();
    }

    @FXML
    @Override
    public void okAction() {
        try {
            if (!checkOptions() || !calculation.prepare()) {
                return;
            }
            if (isAllPages()) {
                switch (objectType) {
                    case Rows:
                        handleAllTask();
                        break;
                    case All:
                        handleAllByAllTask();
                        break;
                    default:
                        handleAllByColumnsTask();
                        break;
                }
            } else {
                handleRowsTask();
            }
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean handleRows() {
        List<Integer> colsIndices = checkedColsIndices;
        if (rowsRadio.isSelected() && categorysCol >= 0) {
            colsIndices.add(0, categorysCol);
        }
        if (!calculation.statisticData(selectedData(colsIndices, rowsRadio.isSelected() && categorysCol < 0))) {
            return false;
        }
        outputColumns = calculation.getOutputColumns();
        outputData = calculation.getOutputData();
        return true;
    }

    public void handleAllByColumnsTask() {
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    calculation.setTask(task);
                    if (calculation.needStored()) {
                        if (data2D instanceof DataTable) {
                            return calculation.statisticAllByColumnsInDataTable();
                        } else {
                            return calculation.statisticData(data2D.allRows(checkedColsIndices, false));
                        }
                    } else {
                        return calculation.statisticAllByColumnsWithoutStored();
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                outputColumns = calculation.getOutputColumns();
                outputData = calculation.getOutputData();
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
                calculation.setTask(null);
                task = null;
                if (targetController != null) {
                    targetController.refreshControls();
                }
            }

        };
        start(task);
    }

    public void handleAllByAllTask() {
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(task);
                    calculation.setTask(task);
                    if (calculation.needStored()) {
                        return calculation.statisticData(data2D.allRows(checkedColsIndices, false));
                    } else {
                        DoubleStatistic statisticData = data2D.statisticByAll(checkedColsIndices, calculation);
                        if (statisticData == null) {
                            return false;
                        }
                        calculation.statisticByColumnsWrite(statisticData);
                        return true;
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                outputColumns = calculation.getOutputColumns();
                outputData = calculation.getOutputData();
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
                calculation.setTask(null);
                task = null;
                if (targetController != null) {
                    targetController.refreshControls();
                }
            }

        };
        start(task);
    }

    @Override
    public DataFileCSV generatedFile() {
        List<Integer> colsIndices = checkedColsIndices;
        if (rowsRadio.isSelected() && categorysCol >= 0) {
            colsIndices.add(0, categorysCol);
        }
        return data2D.statisticByRows(calculation.getOutputNames(), colsIndices, calculation);
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
