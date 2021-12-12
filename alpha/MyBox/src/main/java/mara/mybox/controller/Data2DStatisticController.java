package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-12
 * @License Apache License Version 2.0
 */
public class Data2DStatisticController extends Data2DOperationController {

    protected int scale;

    @FXML
    protected CheckBox countCheck, summationCheck, meanCheck, varianceCheck, skewnessCheck,
            maximumCheck, minimumCheck, modeCheck, medianCheck, percentageCheck;
    @FXML
    protected Label noNumberLabel;

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            super.setParameters(tableController, true, true);

            scale = tableController.data2D.getScale();
            setColumns();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setColumns() {
        try {
            List<String> numberColumnNames = tableController.data2D.numberColumnNames();
            if (numberColumnNames == null || numberColumnNames.isEmpty()) {
                noNumberLabel.setVisible(true);
                okButton.setDisable(true);
                selectController.colsListController.clear();
            } else {
                noNumberLabel.setVisible(false);
                okButton.setDisable(false);
                List<String> selectedCols = selectController.colsListController.checkedValues();
                selectController.colsListController.setValues(numberColumnNames);
                selectController.colsListController.checkValues(selectedCols);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean hanldeData() {
        try {
            if (sourceAll) {
                // to do
                return true;
            }
            statisticTable();
            return true;
        } catch (Exception e) {
            popError(e.toString());
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public double doubleValue(String v) {
        try {
            if (v == null || v.isBlank()) {
                return 0;
            }
            return Double.valueOf(v.replaceAll(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    // All as double to make things simple. 
    // To improve performance, this should be counting according to columns' types.
    public boolean statisticTable() {
        try {
            if (selectedData == null || selectedData.isEmpty()) {
                popError(message("NoData"));
                return false;
            }
            handledNames = new ArrayList<>();
            handledNames.add("m-" + message("Calculation") + "-m");
            handledNames.addAll(selectedNames);

            handledData = new ArrayList<>();
            List<String> countRow = null;
            if (countCheck.isSelected()) {
                countRow = new ArrayList<>();
                countRow.add(message("Count"));
                handledData.add(countRow);
            }
            List<String> summationRow = null;
            if (summationCheck.isSelected()) {
                summationRow = new ArrayList<>();
                summationRow.add(message("Summation"));
                handledData.add(summationRow);
            }
            List<String> meanRow = null;
            if (meanCheck.isSelected()) {
                meanRow = new ArrayList<>();
                meanRow.add(message("Mean"));
                handledData.add(meanRow);
            }
            List<String> varianceRow = null;
            if (varianceCheck.isSelected()) {
                varianceRow = new ArrayList<>();
                varianceRow.add(message("Variance"));
                handledData.add(varianceRow);
            }
            List<String> skewnessRow = null;
            if (skewnessCheck.isSelected()) {
                skewnessRow = new ArrayList<>();
                skewnessRow.add(message("Skewness"));
                handledData.add(skewnessRow);
            }
            List<String> maximumRow = null;
            if (maximumCheck.isSelected()) {
                maximumRow = new ArrayList<>();
                maximumRow.add(message("Maximum"));
                handledData.add(maximumRow);
            }
            List<String> minimumRow = null;
            if (minimumCheck.isSelected()) {
                minimumRow = new ArrayList<>();
                minimumRow.add(message("Minimum"));
                handledData.add(minimumRow);
            }
            List<String> modeRow = null;
            if (modeCheck.isSelected()) {
                modeRow = new ArrayList<>();
                modeRow.add(message("Mode"));
                handledData.add(modeRow);
            }
            List<String> medianRow = null;
            if (medianCheck.isSelected()) {
                medianRow = new ArrayList<>();
                medianRow.add(message("Median"));
                handledData.add(medianRow);
            }
            if (handledData.isEmpty()) {
                popError(message("SelectToHandle"));
                return false;
            }

            int rowsNumber = selectedData.size();
            int columnsNumber = selectedData.get(0).size();
            for (int c = 0; c < columnsNumber; c++) {
                double[] colData = new double[rowsNumber];
                for (int r = 0; r < rowsNumber; r++) {
                    colData[r] = doubleValue(selectedData.get(r).get(c));
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

    @Override
    public void refreshControls() {
        super.refreshControls();
        setColumns();
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
