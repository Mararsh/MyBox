package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.data2d.reader.DataTableGroup;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-1-19
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DChartController extends BaseData2DHandleController {

    protected String selectedCategory, selectedValue;
    protected DataTableGroup group;
    protected int framesNumber, groupid;

    @FXML
    protected ComboBox<String> categoryColumnSelector, valueColumnSelector;
    @FXML
    protected Label noticeLabel;
    @FXML
    protected CheckBox displayAllCheck;
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

            if (playController != null) {
                playController.setParameters(this);
                playController.frameStartNodify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        loadFrame(playController.currentIndex);
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();
            makeOptions();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            MyBoxLog.error(e.toString());
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
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public String chartTitle() {
        return baseTitle;
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
        if (task != null) {
            task.cancel();
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
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
                task = null;
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

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void redrawChart() {
        drawChart();
    }

    @FXML
    @Override
    public void refreshAction() {
        okAction();
    }

    /*
        group
     */
    protected void startGroup() {
        if (task != null) {
            task.cancel();
        }
        playController.clear();
        groupDataController.loadNull();
        group = null;
        framesNumber = -1;
        task = new SingletonTask<Void>(this) {

            List<String> groupLabels;

            @Override
            protected boolean handle() {
                try {
                    outputColumns = data2D.makeColumns(dataColsIndices, showRowNumber());
                    List<String> dataNames = new ArrayList<>();
                    for (Data2DColumn c : outputColumns) {
                        dataNames.add(c.getColumnName());
                    }
                    List<String> sortNames = sortNames();
                    if (sortNames != null) {
                        for (String name : sortNames) {
                            if (!dataNames.contains(name)) {
                                dataNames.add(name);
                            }
                        }
                    }
                    group = groupData(DataTableGroup.TargetType.Table,
                            dataNames, orders, maxData, scale);
                    group.run();
                    List<String> values = group.getParameterValues();
                    framesNumber = values.size();
                    groupLabels = new ArrayList<>();
                    for (int i = 0; i < framesNumber; i++) {
                        groupLabels.add(values.get(i));
                    }
                    return framesNumber > 0;
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
                task = null;
                if (ok) {
                    groupDataController.loadData(group.getTargetData().cloneAll());
                    playController.play(groupLabels);
                }
            }

        };
        start(task);
    }

    protected boolean handleGroups() {
        return framesNumber > 0;
    }

    public boolean initFrame() {
        return outputData != null && !outputData.isEmpty();
    }

    public synchronized void loadFrame(int index) {
        if (group == null || framesNumber <= 0 || index < 0 || index > framesNumber) {
            playController.clear();
            return;
        }
        groupid = index + 1;  // groupid is 1-based
        if (backgroundTask != null) {
            backgroundTask.cancel();
        }
        backgroundTask = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return makeFrameData();
            }

            @Override
            protected void whenFailed() {
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                backgroundTask = null;
                if (ok) {
                    drawFrame();
                }
            }

        };
        start(backgroundTask, false);
    }

    protected boolean makeFrameData() {
        outputData = group.groupData(backgroundTask, groupid, outputColumns);
        return initFrame();
    }

    public void drawFrame() {
    }

    @Override
    public void cleanPane() {
        try {
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
