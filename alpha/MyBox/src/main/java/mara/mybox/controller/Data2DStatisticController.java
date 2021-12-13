package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
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
public class Data2DStatisticController extends Data2DOperationController {

    protected List<String> countRow, summationRow, meanRow, varianceRow, skewnessRow,
            maximumRow, minimumRow, modeRow, medianRow;

    @FXML
    protected CheckBox countCheck, summationCheck, meanCheck, varianceCheck, skewnessCheck,
            maximumCheck, minimumCheck, modeCheck, medianCheck;
    @FXML
    protected Label noNumberLabel, memoryNoticeLabel;
    @FXML
    protected VBox operationBox;

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController, true, false);

            setColumns();

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

            checkMemoryLabel();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkMemoryLabel() {
        if (modeCheck.isSelected() || medianCheck.isSelected()) {
            if (!operationBox.getChildren().contains(memoryNoticeLabel)) {
                operationBox.getChildren().add(memoryNoticeLabel);
            }
        } else {
            if (operationBox.getChildren().contains(memoryNoticeLabel)) {
                operationBox.getChildren().remove(memoryNoticeLabel);
            }
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

    @Override
    public boolean hanldeData() {
        try {
            if (sourceAll && data2D.isMutiplePages()) {
                if (modeCheck.isSelected() || medianCheck.isSelected()) {
                    selectedData = data2D.allRows(selectedColumnsIndices);
                    return statisticRows();
                } else {
                    return statisticFile();
                }
            } else {
                return statisticRows();
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

            String cName = "m-" + message("Calculation") + "-m";
            handledNames = new ArrayList<>();
            handledNames.add(cName);
            handledNames.addAll(selectedNames);

            handledColumns = new ArrayList<>();
            handledColumns.add(new Data2DColumn(cName, ColumnDefinition.ColumnType.String));
            for (String name : selectedNames) {
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
            if (handledData.isEmpty()) {
                popError(message("SelectToHandle"));
                return false;
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    // All as double to make things simple. 
    // To improve performance, this should be counting according to columns' types.
    public boolean statisticRows() {
        try {
            if (selectedData == null || selectedData.isEmpty()) {
                popError(message("SelectToHandle"));
                return false;
            }
            if (!prepareRows()) {
                return false;
            }
            int rowsNumber = selectedData.size();
            int columnsNumber = selectedColumnsIndices.size();
            int scale = data2D.getScale();
            for (int c = 0; c < columnsNumber; c++) {
                double[] colData = new double[rowsNumber];
                for (int r = 0; r < rowsNumber; r++) {
                    colData[r] = data2D.doubleValue(selectedData.get(r).get(c));
                }
                DoubleStatistic statistic = new DoubleStatistic(colData);
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
                    modeRow.add(DoubleTools.format(statistic.getMode(), scale));
                }
                if (medianRow != null) {
                    medianRow.add(DoubleTools.format(statistic.getMedian(), scale));
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean statisticFile() {
        DoubleStatistic[] statisticData = data2D.statisticData(selectedColumnsIndices);
        if (statisticData == null || !prepareRows()) {
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

    /*
        static
     */
    public static Data2DStatisticController open(ControlData2DEditTable tableController) {
        try {
            Data2DStatisticController controller = (Data2DStatisticController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DStatisticFxml, false);
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
