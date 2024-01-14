package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import mara.mybox.data2d.reader.DataTableGroup;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DChartController extends BaseData2DHandleController {

    protected String selectedCategory, selectedValue, groupParameters;
    protected DataTableGroup group;
    protected int chartMaxData, framesNumber;
    protected long groupid;
    protected Thread frameThread;
    protected Connection conn;
    protected List<List<String>> chartData;
    protected List<String> groupLabels;

    @FXML
    protected ComboBox<String> categoryColumnSelector, valueColumnSelector;
    @FXML
    protected Label noticeLabel;
    @FXML
    protected CheckBox displayAllCheck;
    @FXML
    protected TextField chartMaxInput;
    @FXML
    protected ControlData2DResults groupDataController;
    @FXML
    protected ControlPlay playController;

    @Override
    public void initControls() {
        try {
            super.initControls();

            initDataTab();

            if (displayAllCheck != null) {
                displayAllCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayAll", true));
                displayAllCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) -> {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setBoolean(baseName + "DisplayAll", displayAllCheck.isSelected());
                    noticeMemory();
                });

                displayAllCheck.visibleProperty().bind(allPagesRadio.selectedProperty());
            }

            chartMaxData = UserConfig.getInt(baseName + "ChartMaxData", 100);
            if (chartMaxInput != null) {
                chartMaxInput.setText(chartMaxData + "");
            }

            if (playController != null) {
                frameThread = new Thread() {
                    @Override
                    public void run() {
                        loadFrame(playController.currentIndex);
                    }
                };
                playController.setParameters(this, frameThread, snapNode());

                playController.stopped.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        try {
                            if (conn != null) {
                                conn.close();
                                conn = null;
                            }
                        } catch (Exception ex) {
                        }
                    }
                });

            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initDataTab() {
        try {
            if (categoryColumnSelector != null) {
                categoryColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkOptions();
                    }
                });
            }

            if (valueColumnSelector != null) {
                valueColumnSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        checkOptions();
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();
            makeOptions();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeOptions() {
        try {
            List<String> names = tableController.data2D.columnNames();
            if (names == null || names.isEmpty()) {
                return;
            }
            isSettingValues = true;
            if (categoryColumnSelector != null) {
                selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
                categoryColumnSelector.getItems().setAll(names);
                if (selectedCategory != null && names.contains(selectedCategory)) {
                    categoryColumnSelector.setValue(selectedCategory);
                } else {
                    categoryColumnSelector.getSelectionModel().select(0);
                }
            }
            if (valueColumnSelector != null) {
                selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
                valueColumnSelector.getItems().setAll(names);
                if (selectedValue != null && names.contains(selectedValue)) {
                    valueColumnSelector.setValue(selectedValue);
                } else {
                    valueColumnSelector.getSelectionModel().select(names.size() > 1 ? 1 : 0);
                }
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (isSettingValues) {
            return true;
        }
        boolean ok = super.checkOptions();
        noticeMemory();
        return ok;
    }

    public void noticeMemory() {
        if (noticeLabel == null) {
            return;
        }
        noticeLabel.setVisible(isAllPages()
                && (displayAllCheck == null || displayAllCheck.isSelected()));
    }

    @Override
    public boolean initData() {
        try {
            if (!super.initData()) {
                return false;
            }
            if (categoryColumnSelector != null) {
                selectedCategory = categoryColumnSelector.getSelectionModel().getSelectedItem();
            }
            if (valueColumnSelector != null) {
                selectedValue = valueColumnSelector.getSelectionModel().getSelectedItem();
            }

            group = null;
            framesNumber = -1;
            groupid = -1;
            groupParameters = null;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public String chartTitle() {
        if (groupController != null) {
            return groupChartTitle();
        } else {
            return baseChartTitle();
        }
    }

    public String baseChartTitle() {
        return baseTitle;
    }

    public String groupChartTitle() {
        if (group == null) {
            return baseChartTitle();
        }
        return baseChartTitle() + (this instanceof BaseData2DChartHtmlController ? "<BR>" : "\n")
                + group.getIdColName() + groupid + " - " + groupParameters;
    }

    public String categoryName() {
        return selectedCategory;
    }

    @Override
    protected void startOperation() {
        if (groupController != null) {
            startGroup();
        } else {
            startNoGroup();
        }
    }

    /*
        no group
     */
    protected void startNoGroup() {
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(this, filterController.filter);
                    readData();
                    data2D.stopFilter();
                    return outputData != null && !outputData.isEmpty();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                if (ok) {
                    outputData();
                }
            }

        };
        start(task);
    }

    public void readData() {
        try {
            boolean showRowNumber = showRowNumber();
            outputData = sortedData(dataColsIndices, showRowNumber);
            if (outputData == null || scaleSelector == null) {
                return;
            }
            outputColumns = data2D.makeColumns(dataColsIndices, showRowNumber);
            boolean needScale = false;
            for (Data2DColumn c : outputColumns) {
                if (c.needScale()) {
                    needScale = true;
                    break;
                }
            }
            if (!needScale) {
                return;
            }
            List<List<String>> scaled = new ArrayList<>();
            for (List<String> row : outputData) {
                List<String> srow = new ArrayList<>();
                for (int i = 0; i < outputColumns.size(); i++) {
                    String s = row.get(i);
                    if (s == null || !outputColumns.get(i).needScale()) {
                        srow.add(s);
                    } else {
                        srow.add(DoubleTools.scaleString(s, invalidAs, scale));
                    }
                }
                scaled.add(srow);
            }
            outputData = scaled;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
        }
    }

    public void outputData() {
        drawChart();
    }

    public void drawChart() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            chartMax();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void redrawChart() {
        drawChart();
    }

    public boolean checkMax() {
        if (chartMaxInput != null) {
            try {
                chartMaxData = Integer.parseInt(chartMaxInput.getText());
                UserConfig.setInt(baseName + "ChartMaxData", chartMaxData);
                return true;
            } catch (Exception ex) {
                popError(message("Invalid") + ": " + message("Maximum"));
                return false;
            }
        }
        return true;
    }

    @FXML
    @Override
    public void refreshAction() {
        if (!checkMax()) {
            return;
        }
        okAction();
    }

    public List<List<String>> chartMax() {
        try {
            if (outputData == null || outputData.isEmpty()) {
                popError(message("NoData"));
                return null;
            }
            if (chartMaxData > 0 && chartMaxData < outputData.size()) {
                chartData = outputData.subList(0, chartMaxData);
            } else {
                chartData = outputData;
            }
            return chartData;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void goMaxAction() {
        if (!checkMax()) {
            return;
        }
        drawChart();
    }

    /*
        group
     */
    protected void startGroup() {
        if (task != null && !task.isQuit()) {
            return;
        }
        playController.clear();
        groupDataController.loadNull();
        group = null;
        framesNumber = -1;
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.setTask(this);
                    List<Integer> cols = dataColsIndices;
                    List<String> sortNames = sortNames();
                    if (sortNames != null) {
                        for (String name : sortNames()) {
                            int col = data2D.colOrder(name);
                            if (!cols.contains(col)) {
                                cols.add(col);
                            }
                        }
                    }
                    outputColumns = data2D.makeColumns(cols, showRowNumber());
                    group = groupData(DataTableGroup.TargetType.Table,
                            cols, showRowNumber(), maxData, scale);
                    if (!group.run()) {
                        return false;
                    }
                    framesNumber = (int) group.groupsNumber();
                    return initGroups();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                if (ok) {
                    loadChartData();
                    playController.play(framesNumber);
                }
            }

        };
        start(task);
    }

    protected boolean initGroups() {
        return framesNumber > 0;
    }

    protected void loadChartData() {
        if (group.getTargetData() != null) {
            groupDataController.loadData(group.getTargetData().cloneAll());
        }
    }

    public boolean initFrame() {
        return outputData != null && !outputData.isEmpty();
    }

    public void loadFrame(int index) {
        if (group == null || framesNumber <= 0 || index < 0 || index > framesNumber) {
            playController.clear();
            return;
        }
        groupid = index + 1;  // groupid is 1-based
        if (makeFrameData()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if (groupLabels != null) {
                        playController.setList(groupLabels);
                    }
                    drawFrame();
                }
            });
        }
    }

    protected boolean makeFrameData() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DerbyBase.getConnection();
            }
            outputData = group.groupData(conn, groupid, outputColumns);
            groupParameters = group.parameterValue(conn, groupid);
            if (!playController.selectCurrentFrame()) {
                groupLabels = group.getParameterLabels(conn, playController.currentRange());
            } else {
                groupLabels = null;
            }
            return initFrame();
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
            return false;
        }
    }

    public void drawFrame() {
    }

    public Node snapNode() {
        return null;
    }

    @Override
    public void cleanPane() {
        try {
            if (conn != null) {
                conn.close();
            }
            if (playController != null) {
                playController.clear();
            }
            if (groupDataController != null) {
                groupDataController.loadData(null);
            }
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
