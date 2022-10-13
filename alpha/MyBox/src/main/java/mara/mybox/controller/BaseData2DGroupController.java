package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import mara.mybox.data2d.DataFilter;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-8-10
 * @License Apache License Version 2.0
 */
public class BaseData2DGroupController extends Data2DChartXYController {

    protected String groupName;
    protected List<String> groupNames;
    protected List<DataFilter> groupConditions;
    protected double groupInterval, groupNumber;

    @FXML
    protected Tab groupTab;
    @FXML
    protected ControlData2DGroup groupController;

    public BaseData2DGroupController() {
        baseTitle = message("GroupStatistic");
        TipsLabelKey = "GroupEqualTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            groupController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();

            groupController.refreshControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterRefreshControls() {
    }

    @Override
    public boolean initData() {
        try {
            groupName = null;
            groupNames = null;
            groupConditions = null;
            groupInterval = Double.NaN;
            groupNumber = Double.NaN;

            boolean valid = false;
            if (groupByValues()) {
                groupNames = getGroupColumns();
                valid = groupNames != null && !groupNames.isEmpty();

            } else if (groupByConditions()) {
                groupConditions = getFilters();
                valid = groupConditions != null && !groupConditions.isEmpty();

            } else if (groupByInterval()) {
                groupName = getColumn();
                groupInterval = getGroupInterval();
                valid = groupName != null && !groupName.isBlank()
                        && !DoubleTools.invalidDouble(groupInterval);

            } else if (groupByNumber()) {
                groupName = getColumn();
                groupNumber = getGroupNumber();
                valid = groupName != null && !groupName.isBlank()
                        && groupNumber > 0;

            }

            if (!valid) {
                outOptionsError(message("SelectToHandle") + ": " + message("GroupBy"));
                tabPane.getSelectionModel().select(groupTab);
                return false;
            }

            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean groupByValues() {
        return groupController.valuesRadio.isSelected();
    }

    public List<String> getGroupColumns() {
        if (!groupByValues()) {
            return null;
        }
        return groupController.columnsController.selectedNames();
    }

    public boolean groupByInterval() {
        return groupController.intervalRadio.isSelected();
    }

    public String getColumn() {
        if (!groupByInterval() && !groupByNumber()) {
            return null;
        }
        return groupController.columnSelector.getValue();
    }

    public double getGroupInterval() {
        if (!groupByInterval()) {
            return Double.NaN;
        }
        double v;
        try {
            v = Double.valueOf(groupController.intervalInput.getText());
        } catch (Exception e) {
            v = Double.NaN;
        }
        return v;
    }

    public boolean groupByNumber() {
        return groupController.numberRadio.isSelected();
    }

    public int getGroupNumber() {
        if (!groupByNumber()) {
            return -1;
        }
        int v;
        try {
            v = Integer.valueOf(groupController.numberInput.getText());
        } catch (Exception e) {
            v = -1;
        }
        return v;
    }

    public boolean groupByConditions() {
        return groupController.conditionsRadio.isSelected();
    }

    public List<DataFilter> getFilters() {
        if (!groupByConditions()) {
            return null;
        }
        return groupController.tableData;
    }

}
