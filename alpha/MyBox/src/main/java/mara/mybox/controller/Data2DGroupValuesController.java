package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-10
 * @License Apache License Version 2.0
 */
public class Data2DGroupValuesController extends BaseData2DHandleController {

    protected List<String> groups, calculations;

    @FXML
    protected ControlSelection groupController, calculationController;

    public Data2DGroupValuesController() {
        baseTitle = message("GroupByValues");
        TipsLabelKey = "GroupByValuesTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            noColumnSelection(true);

            groupController.setParameters(this, message("Group"));
            groupController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkOptions();
                }
            });

            calculationController.setParameters(this, message("Calculation"));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();
            if (!data2D.isValid()) {
                groupController.loadNames(null);
                calculationController.loadNames(null);
                return;
            }
            List<String> gnames = new ArrayList<>();
            List<String> cnames = new ArrayList<>();
            cnames.add(message("Count"));
            for (Data2DColumn column : data2D.columns) {
                String name = column.getColumnName();
                gnames.add(name);
                cnames.add(name + "-" + message("Mean"));
                cnames.add(name + "-" + message("Summation"));
                cnames.add(name + "-" + message("Maximum"));
                cnames.add(name + "-" + message("Minimum"));
                cnames.add(name + "-" + message("PopulationVariance"));
                cnames.add(name + "-" + message("SampleVariance"));
                cnames.add(name + "-" + message("PopulationStandardDeviation"));
                cnames.add(name + "-" + message("SampleStandardDeviation"));
            }
            groupController.loadNames(gnames);
            calculationController.loadNames(cnames);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            if (isSettingValues) {
                return true;
            }
            boolean ok = super.checkOptions();
            groups = groupController.selectedNames();
            if (groups == null || groups.isEmpty()) {
                infoLabel.setText(message("SelectToHandle"));
                okButton.setDisable(true);
                return false;
            }

            return ok;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean initData() {
        List<String> colsNames = new ArrayList<>();
        colsNames.addAll(groups);
        calculations = calculationController.selectedNames();
        if (calculations != null) {
            for (String name : calculations) {
                if (name.endsWith("-" + message("Mean"))) {
                    name = name.substring(0, name.length() - ("-" + message("Mean")).length());
                } else if (name.endsWith("-" + message("Summation"))) {
                    name = name.substring(0, name.length() - ("-" + message("Summation")).length());
                } else if (name.endsWith("-" + message("Maximum"))) {
                    name = name.substring(0, name.length() - ("-" + message("Maximum")).length());
                } else if (name.endsWith("-" + message("Minimum"))) {
                    name = name.substring(0, name.length() - ("-" + message("Minimum")).length());
                } else if (name.endsWith("-" + message("PopulationVariance"))) {
                    name = name.substring(0, name.length() - ("-" + message("PopulationVariance")).length());
                } else if (name.endsWith("-" + message("SampleVariance"))) {
                    name = name.substring(0, name.length() - ("-" + message("SampleVariance")).length());
                } else if (name.endsWith("-" + message("PopulationStandardDeviation"))) {
                    name = name.substring(0, name.length() - ("-" + message("PopulationStandardDeviation")).length());
                } else if (name.endsWith("-" + message("SampleStandardDeviation"))) {
                    name = name.substring(0, name.length() - ("-" + message("SampleStandardDeviation")).length());
                } else {
                    continue;
                }
                if (!colsNames.contains(name)) {
                    colsNames.add(name);
                }
            }
        }
        checkedColsIndices = new ArrayList<>();
        checkedColumns = new ArrayList<>();
        for (String name : colsNames) {
            checkedColsIndices.add(data2D.colOrder(name));
            checkedColumns.add(data2D.columnByName(name));
        }
        checkedColsNames = colsNames;
        return true;
    }

    @Override
    public boolean handleRows() {
        try {
            outputData = filtered(checkedColsIndices, showRowNumber());
            if (outputData == null || outputData.isEmpty()) {
                return false;
            }
            DataTable tmpTable = data2D.toTmpTable(task, checkedColsIndices, outputData, showRowNumber(), false);
            if (tmpTable == null) {
                return false;
            }
            DataFileCSV csvData = tmpTable.groupValues(targetController.name(), task, groups, calculations);
            tmpTable.drop();
            if (csvData == null) {
                return false;
            }
            outputColumns = csvData.columns;
            outputData = csvData.allRows(false);
            if (showColNames()) {
                outputData.add(0, csvData.columnNames());
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

    @Override
    public DataFileCSV generatedFile() {
        try {
            DataTable tmpTable = data2D.toTmpTable(task, checkedColsIndices, showRowNumber(), false);
            if (tmpTable == null) {
                return null;
            }
            DataFileCSV csvData = tmpTable.groupValues(targetController.name(), task, groups, calculations);
            tmpTable.drop();
            return csvData;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return null;
        }
    }


    /*
        static
     */
    public static Data2DGroupValuesController open(ControlData2DEditTable tableController) {
        try {
            Data2DGroupValuesController controller = (Data2DGroupValuesController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DGroupValuesFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
