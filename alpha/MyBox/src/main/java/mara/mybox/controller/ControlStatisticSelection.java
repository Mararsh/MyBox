package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import mara.mybox.calculation.DescriptiveStatistic;
import mara.mybox.calculation.DescriptiveStatistic.StatisticType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-12
 * @License Apache License Version 2.0
 */
public class ControlStatisticSelection extends BaseController {

    protected BaseData2DHandleController handleController;
    protected DescriptiveStatistic calculation;

    @FXML
    protected CheckBox countCheck, summationCheck, meanCheck, geometricMeanCheck, sumOfSquaresCheck,
            populationVarianceCheck, sampleVarianceCheck, populationStandardDeviationCheck, sampleStandardDeviationCheck, skewnessCheck,
            maximumCheck, minimumCheck, medianCheck, upperQuartileCheck, lowerQuartileCheck,
            UpperMildOutlierLineCheck, UpperExtremeOutlierLineCheck, LowerMildOutlierLineCheck, LowerExtremeOutlierLineCheck,
            modeCheck;

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

    public void setParameters(BaseData2DHandleController handleController) {
        try {
            this.handleController = handleController;
            baseName = handleController.baseName;

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
                }
            });

            medianCheck.setSelected(UserConfig.getBoolean(baseName + "Median", false));
            medianCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Median", medianCheck.isSelected());
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

            UpperMildOutlierLineCheck.setSelected(UserConfig.getBoolean(baseName + "UpperMildOutlierLine", true));
            UpperMildOutlierLineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "UpperMildOutlierLine", UpperMildOutlierLineCheck.isSelected());
                }
            });

            UpperExtremeOutlierLineCheck.setSelected(UserConfig.getBoolean(baseName + "UpperExtremeOutlierLine", true));
            UpperExtremeOutlierLineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "UpperExtremeOutlierLine", UpperExtremeOutlierLineCheck.isSelected());
                }
            });
            LowerMildOutlierLineCheck.setSelected(UserConfig.getBoolean(baseName + "LowerMildOutlierLine", true));
            LowerMildOutlierLineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "LowerMildOutlierLine", LowerMildOutlierLineCheck.isSelected());
                }
            });
            LowerExtremeOutlierLineCheck.setSelected(UserConfig.getBoolean(baseName + "LowerExtremeOutlierLine", true));
            LowerExtremeOutlierLineCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "LowerExtremeOutlierLine", LowerExtremeOutlierLineCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void mustCount() {
        countCheck.setSelected(true);
        countCheck.setDisable(true);
    }

    public DescriptiveStatistic pickValues() {
        try {
            calculation = new DescriptiveStatistic();
            if (countCheck.isSelected()) {
                calculation.add(StatisticType.Count);
            }
            if (summationCheck.isSelected()) {
                calculation.add(StatisticType.Sum);
            }
            if (meanCheck.isSelected()) {
                calculation.add(StatisticType.Mean);
            }
            if (geometricMeanCheck.isSelected()) {
                calculation.add(StatisticType.GeometricMean);
            }
            if (sumOfSquaresCheck.isSelected()) {
                calculation.add(StatisticType.SumOfSquares);
            }
            if (populationStandardDeviationCheck.isSelected()) {
                calculation.add(StatisticType.PopulationStandardDeviation);
            }
            if (populationVarianceCheck.isSelected()) {
                calculation.add(StatisticType.PopulationVariance);
            }
            if (sampleStandardDeviationCheck.isSelected()) {
                calculation.add(StatisticType.SampleStandardDeviation);
            }
            if (sampleVarianceCheck.isSelected()) {
                calculation.add(StatisticType.SampleVariance);
            }
            if (skewnessCheck.isSelected()) {
                calculation.add(StatisticType.Skewness);
            }
            if (medianCheck.isSelected()) {
                calculation.add(StatisticType.Median);
            }
            if (maximumCheck.isSelected()) {
                calculation.add(StatisticType.MaximumQ4);
            }
            if (minimumCheck.isSelected()) {
                calculation.add(StatisticType.MinimumQ0);
            }
            if (upperQuartileCheck.isSelected()) {
                calculation.add(StatisticType.UpperQuartile);
            }
            if (lowerQuartileCheck.isSelected()) {
                calculation.add(StatisticType.LowerQuartile);
            }
            if (UpperExtremeOutlierLineCheck.isSelected()) {
                calculation.add(StatisticType.UpperExtremeOutlierLine);
            }
            if (UpperMildOutlierLineCheck.isSelected()) {
                calculation.add(StatisticType.UpperMildOutlierLine);
            }
            if (LowerExtremeOutlierLineCheck.isSelected()) {
                calculation.add(StatisticType.LowerExtremeOutlierLine);
            }
            if (LowerMildOutlierLineCheck.isSelected()) {
                calculation.add(StatisticType.LowerMildOutlierLine);
            }
            if (modeCheck.isSelected()) {
                calculation.add(StatisticType.Mode);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            calculation = null;
        }
        return calculation;
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
    }

    @FXML
    @Override
    public void selectNoneAction() {
        isSettingValues = true;
        if (!countCheck.isDisabled()) {
            countCheck.setSelected(false);
        }
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
    }

}
