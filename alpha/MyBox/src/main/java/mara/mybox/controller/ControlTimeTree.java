package mara.mybox.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.FlowPane;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ConditionNode;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.TimeFormats;

/**
 * @Author Mara
 * @CreateDate 2020-04-19
 * @License Apache License Version 2.0
 */
public class ControlTimeTree extends ControlConditionTree {

    protected String fieldName;
    protected LoadingController loading;
    protected List<Date> times;
    protected boolean isEra;

    @FXML
    protected FlowPane buttonsPane;
    @FXML
    protected Button refreshNodesButton, queryNodesButton;

    public ControlTimeTree() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> s = new ArrayList();
            s.add(message("AllTime"));
            treeView.setSelectedTitles(s);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParent(BaseController parent, boolean noQueryButton) {
        setParent(parent);
        if (noQueryButton) {
            buttonsPane.getChildren().removeAll(refreshNodesButton, queryNodesButton);
        }
    }

    protected void loadTree(String fieldName, List<Date> times, boolean isEra) {
        this.fieldName = fieldName;
        this.times = times;
        this.isEra = isEra;
        loadTree();
        selectAllAction();
    }

    @Override
    public void loadTree() {
        CheckBoxTreeItem<ConditionNode> allItem = new CheckBoxTreeItem(
                ConditionNode.create(message("AllTime"))
                        .setTitle(message("AllTime"))
                        .setCondition("")
        );
        allItem.setExpanded(true);
        treeView.setRoot(allItem);
        loadYears();
    }

    protected void loadYears() {
        if (times == null || times.isEmpty()) {
            return;
        }
        CheckBoxTreeItem<ConditionNode> root = (CheckBoxTreeItem<ConditionNode>) treeView.getRoot();
        root.getChildren().clear();
        boolean isBC = false;
        String year, yearEra;
        for (Date time : times) {
            String timeString = DateTools.datetimeToString(time);
            year = timeString.substring(0, 4);
            yearEra = year;
            if (isEra) {
                isBC = DateTools.isBC(time.getTime());
                if (isBC) {
                    yearEra += " BC";
                }
            }
            CheckBoxTreeItem<ConditionNode> yearItem = null;
            if (!root.isLeaf()) {
                for (TreeItem<ConditionNode> rootChild : root.getChildren()) {
                    if (yearEra.equals(rootChild.getValue().getTitle())) {
                        yearItem = (CheckBoxTreeItem<ConditionNode>) rootChild;
                        break;
                    }
                }
            }
            if (yearItem == null) {
                String start = year + "-01-01 00:00:00" + (isBC ? " BC" : "");
                String end = year + "-12-31 23:59:59" + (isBC ? " BC" : "");
                CheckBoxTreeItem<ConditionNode> newYearItem = new CheckBoxTreeItem(
                        ConditionNode.create(yearEra)
                                .setTitle(yearEra)
                                .setCondition(condition(start, end))
                );
                root.getChildren().add(newYearItem);
                newYearItem.setExpanded(false);

                TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                newYearItem.getChildren().add(dummyItem);
                newYearItem.setExpanded(false);
                newYearItem.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                            if (newVal && !newYearItem.isLeaf() && !loaded(newYearItem)) {
                                loadMonths(newYearItem);
                            }
                        });
            }

        }
    }

    protected void loadMonths(CheckBoxTreeItem<ConditionNode> yearItem) {
        if (yearItem == null || times == null || times.isEmpty()) {
            return;
        }
        yearItem.getChildren().clear();
        String year = yearItem.getValue().getTitle();
        if (year.length() < 4) {
            return;
        }
        String yearTime = year.substring(0, 4);
        String month, monthEra;
        boolean isBC = false;
        for (Date time : times) {
            String timeString = DateTools.datetimeToString(time);
            month = timeString.substring(0, 7);
            if (!month.startsWith(yearTime)) {
                continue;
            }
            monthEra = month;
            if (isEra) {
                isBC = DateTools.isBC(time.getTime());
                if (isBC) {
                    if (!year.endsWith(" BC")) {
                        continue;
                    }
                    monthEra += " BC";
                }
            }
            CheckBoxTreeItem<ConditionNode> monthItem = null;
            if (!yearItem.isLeaf()) {
                for (TreeItem<ConditionNode> yearChild : yearItem.getChildren()) {
                    if (monthEra.equals(yearChild.getValue().getTitle())) {
                        monthItem = (CheckBoxTreeItem<ConditionNode>) yearChild;
                        break;
                    }
                }
            }
            if (monthItem == null) {
                String start = month + "-01 00:00:00" + (isBC ? " BC" : "");
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(DateTools.encodeDate(start));
                calendar.add(Calendar.MONTH, 1);
                calendar.add(Calendar.DATE, -1);
                String end = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()) + " 23:59:59" + (isBC ? " BC" : "");
                CheckBoxTreeItem<ConditionNode> newMonthItem = new CheckBoxTreeItem(
                        ConditionNode.create(monthEra)
                                .setTitle(monthEra)
                                .setCondition(condition(start, end))
                );
                yearItem.getChildren().add(newMonthItem);
                newMonthItem.setExpanded(false);

                TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                newMonthItem.getChildren().add(dummyItem);
                newMonthItem.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                            if (newVal && !newMonthItem.isLeaf() && !loaded(newMonthItem)) {
                                loadDays(newMonthItem);
                            }
                        });
            }
        }
    }

    protected void loadDays(CheckBoxTreeItem<ConditionNode> monthItem) {
        if (monthItem == null || times == null || times.isEmpty()) {
            return;
        }
        monthItem.getChildren().clear();
        String month = monthItem.getValue().getTitle();
        if (month.length() < 7) {
            return;
        }
        String monthTime = month.substring(0, 7);
        String day, dayEra;
        boolean isBC = false;
        for (Date time : times) {
            String timeString = DateTools.datetimeToString(time);
            day = timeString.substring(0, 10);
            if (!day.startsWith(monthTime)) {
                continue;
            }
            dayEra = day;
            if (isEra) {
                isBC = DateTools.isBC(time.getTime());
                if (isBC) {
                    if (!month.endsWith(" BC")) {
                        continue;
                    }
                    dayEra += " BC";
                }
            }
            CheckBoxTreeItem<ConditionNode> dayItem = null;
            if (!monthItem.isLeaf()) {
                for (TreeItem<ConditionNode> monthChild : monthItem.getChildren()) {
                    if (dayEra.equals(monthChild.getValue().getTitle())) {
                        dayItem = (CheckBoxTreeItem<ConditionNode>) monthChild;
                        break;
                    }
                }
            }
            if (dayItem == null) {
                String start = day + " 00:00:00" + (isBC ? " BC" : "");
                String end = day + " 23:59:59" + (isBC ? " BC" : "");
                CheckBoxTreeItem<ConditionNode> newDayItem = new CheckBoxTreeItem(
                        ConditionNode.create(dayEra)
                                .setTitle(dayEra)
                                .setCondition(condition(start, end))
                );
                monthItem.getChildren().add(newDayItem);
                newDayItem.setExpanded(false);

                TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                newDayItem.getChildren().add(dummyItem);
                newDayItem.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                            if (newVal && !loaded(newDayItem)) {
//                                if (times.size() < 1000) {
//                                    loadTimes(newDayItem, false);
//                                } else {
//                                    loadHours(newDayItem);
//                                }
                                loadHours(newDayItem);
                            }
                        });
            }
        }
    }

    protected void loadHours(CheckBoxTreeItem<ConditionNode> dayItem) {
        if (dayItem == null || times == null || times.isEmpty()) {
            return;
        }
        dayItem.getChildren().clear();
        String day = dayItem.getValue().getTitle();
        if (day.length() < 10) {
            return;
        }
        String dayTime = day.substring(0, 10);
        String hour, hourEra;
        boolean isBC = false;
        for (Date time : times) {
            String timeString = DateTools.datetimeToString(time);
            hour = timeString.substring(0, 13);
            if (!hour.startsWith(dayTime)) {
                continue;
            }
            hourEra = hour;
            if (isEra) {
                isBC = DateTools.isBC(time.getTime());
                if (isBC) {
                    if (!day.endsWith(" BC")) {
                        continue;
                    }
                    hourEra += " BC";
                }
            }
            CheckBoxTreeItem<ConditionNode> hourItem = null;
            if (!dayItem.isLeaf()) {
                for (TreeItem<ConditionNode> dayChild : dayItem.getChildren()) {
                    if (hourEra.equals(dayChild.getValue().getTitle())) {
                        hourItem = (CheckBoxTreeItem<ConditionNode>) dayChild;
                        break;
                    }
                }
            }
            if (hourItem == null) {
                String start = hour + ":00:00" + (isBC ? " BC" : "");
                String end = hour + ":59:59" + (isBC ? " BC" : "");
                CheckBoxTreeItem<ConditionNode> newHourItem = new CheckBoxTreeItem(
                        ConditionNode.create(hourEra)
                                .setTitle(hourEra)
                                .setCondition(condition(start, end))
                );
                dayItem.getChildren().add(newHourItem);
                newHourItem.setExpanded(false);

                TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                newHourItem.getChildren().add(dummyItem);
                newHourItem.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                            if (newVal && !loaded(newHourItem)) {
                                loadMinutes(newHourItem);
                            }
                        });
            }
        }
    }

    protected void loadMinutes(CheckBoxTreeItem<ConditionNode> hourItem) {
        if (hourItem == null || times == null || times.isEmpty()) {
            return;
        }
        hourItem.getChildren().clear();
        String hour = hourItem.getValue().getTitle();
        if (hour.length() < 13) {
            return;
        }
        String hourTime = hour.substring(0, 13);
        String minute, minuteEra;
        boolean isBC = false;
        for (Date time : times) {
            String timeString = DateTools.datetimeToString(time);
            minute = timeString.substring(0, 16);
            if (!minute.startsWith(hourTime)) {
                continue;
            }
            minuteEra = minute;
            if (isEra) {
                isBC = DateTools.isBC(time.getTime());
                if (isBC) {
                    if (!minute.endsWith(" BC")) {
                        continue;
                    }
                    minuteEra += " BC";
                }
            }
            CheckBoxTreeItem<ConditionNode> minuteItem = null;
            if (!hourItem.isLeaf()) {
                for (TreeItem<ConditionNode> hourChild : hourItem.getChildren()) {
                    if (minuteEra.equals(hourChild.getValue().getTitle())) {
                        minuteItem = (CheckBoxTreeItem<ConditionNode>) hourChild;
                        break;
                    }
                }
            }
            if (minuteItem == null) {
                String start = minute + ":00" + (isBC ? " BC" : "");
                String end = minute + ":59" + (isBC ? " BC" : "");
                CheckBoxTreeItem<ConditionNode> newMinuteItem = new CheckBoxTreeItem(
                        ConditionNode.create(minuteEra)
                                .setTitle(minuteEra)
                                .setCondition(condition(start, end))
                );
                hourItem.getChildren().add(newMinuteItem);
                newMinuteItem.setExpanded(false);

                TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                newMinuteItem.getChildren().add(dummyItem);
                newMinuteItem.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                            if (newVal && !loaded(newMinuteItem)) {
                                loadTimes(newMinuteItem);
                            }
                        });
            }
        }
    }

    protected void loadTimes(CheckBoxTreeItem<ConditionNode> minuteItem) {
        if (minuteItem == null || times == null || times.isEmpty()) {
            return;
        }
        minuteItem.getChildren().clear();
        String minute = minuteItem.getValue().getTitle();
        String minuteTime = minute.substring(0, 16);
        String timeEra;
        boolean isBC;
        for (Date time : times) {
            String timeString = DateTools.datetimeToString(time, TimeFormats.DatetimeMs);
            if (!timeString.startsWith(minuteTime)) {
                continue;
            }
            timeEra = timeString;
            if (isEra) {
                isBC = DateTools.isBC(time.getTime());
                if (isBC) {
                    if (!minute.endsWith(" BC")) {
                        continue;
                    }
                    timeEra += " BC";
                }
            }
            CheckBoxTreeItem<ConditionNode> timeItem = null;
            if (!minuteItem.isLeaf()) {
                for (TreeItem<ConditionNode> node : minuteItem.getChildren()) {
                    if (timeEra.equals(node.getValue().getTitle())) {
                        timeItem = (CheckBoxTreeItem<ConditionNode>) node;
                        break;
                    }
                }
            }
            if (timeItem == null) {
                timeItem = new CheckBoxTreeItem(
                        ConditionNode.create(timeEra)
                                .setTitle(timeEra)
                                .setCondition(condition(time))
                );
                minuteItem.getChildren().add(timeItem);
            }
        }
    }

    protected boolean loaded(TreeItem item) {
        if (item == null || item.isLeaf()) {
            return true;
        }
        Object child = item.getChildren().get(0);
        return child instanceof CheckBoxTreeItem;
    }

    protected String condition(String start, String end) {
        if (!isEra) {
            return " " + fieldName + " BETWEEN '" + start + "' AND '" + end + "'";
        } else {
            long startTime = DateTools.encodeEra(start).getTime();
            long endTime = DateTools.encodeEra(end).getTime();
            return " " + fieldName + " BETWEEN " + startTime + " AND " + endTime;
        }
    }

    protected String condition(Date time) {
        if (isEra) {
            return " " + fieldName + "=" + time.getTime();
        } else {
            return " " + fieldName + "='" + time + "'";
        }
    }

}
