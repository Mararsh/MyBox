package mara.mybox.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import mara.mybox.fxml.ConditionNode;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-04-19
 * @License Apache License Version 2.0
 */
public class TimeTreeController extends ConditionTreeController {

    protected String fieldName;
    protected LoadingController loading;
    protected List<Date> times;
    protected boolean isEra;

    public TimeTreeController() {
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();
            List<String> s = new ArrayList();
            s.add(message("AllTime"));
            treeView.setSelectedTitles(s);
        } catch (Exception e) {
            logger.error(e.toString());
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
        for (Date time : times) {
            String timeString = DateTools.datetimeToString(time);
            String year = timeString.substring(0, 4);
            CheckBoxTreeItem<ConditionNode> yearItem = null;
            if (!root.isLeaf()) {
                for (TreeItem<ConditionNode> rootChild : root.getChildren()) {
                    if (year.equals(rootChild.getValue().getTitle())) {
                        yearItem = (CheckBoxTreeItem<ConditionNode>) rootChild;
                        break;
                    }
                }
            }
            if (yearItem == null) {
                CheckBoxTreeItem<ConditionNode> newYearItem = new CheckBoxTreeItem(
                        ConditionNode.create(year)
                                .setTitle(year)
                                .setCondition(condition(year + "-01-01 00:00:00", year + "-12-31 23:59:59"))
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
        for (Date time : times) {
            String timeString = DateTools.datetimeToString(time);
            String month = timeString.substring(0, 7);
            if (!month.startsWith(year)) {
                continue;
            }
            CheckBoxTreeItem<ConditionNode> monthItem = null;
            if (!yearItem.isLeaf()) {
                for (TreeItem<ConditionNode> yearChild : yearItem.getChildren()) {
                    if (month.equals(yearChild.getValue().getTitle())) {
                        monthItem = (CheckBoxTreeItem<ConditionNode>) yearChild;
                        break;
                    }
                }
            }
            if (monthItem == null) {
                String start = month + "-01 00:00:00";
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(DateTools.stringToDatetime(start));
                calendar.add(Calendar.MONTH, 1);
                calendar.add(Calendar.DATE, -1);
                String endDay = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                CheckBoxTreeItem<ConditionNode> newMonthItem = new CheckBoxTreeItem(
                        ConditionNode.create(month)
                                .setTitle(month)
                                .setCondition(condition(start, endDay + " 23:59:59"))
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
        for (Date time : times) {
            String timeString = DateTools.datetimeToString(time);
            String day = timeString.substring(0, 10);
            if (!day.startsWith(month)) {
                continue;
            }
            CheckBoxTreeItem<ConditionNode> dayItem = null;
            if (!monthItem.isLeaf()) {
                for (TreeItem<ConditionNode> monthChild : monthItem.getChildren()) {
                    if (day.equals(monthChild.getValue().getTitle())) {
                        dayItem = (CheckBoxTreeItem<ConditionNode>) monthChild;
                        break;
                    }
                }
            }
            if (dayItem == null) {
                CheckBoxTreeItem<ConditionNode> newDayItem = new CheckBoxTreeItem(
                        ConditionNode.create(day)
                                .setTitle(day)
                                .setCondition(condition(day + " 00:00:00", day + " 23:59:59"))
                );
                monthItem.getChildren().add(newDayItem);
                newDayItem.setExpanded(false);

                TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                newDayItem.getChildren().add(dummyItem);
                newDayItem.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                            if (newVal && !loaded(newDayItem)) {
                                if (times.size() < 1000) {
                                    loadTimes(newDayItem);
                                } else {
                                    loadHours(newDayItem);
                                }
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
        for (Date time : times) {
            String timeString = DateTools.datetimeToString(time);
            String hour = timeString.substring(0, 13);
            if (!hour.startsWith(day)) {
                continue;
            }
            CheckBoxTreeItem<ConditionNode> hourItem = null;
            if (!dayItem.isLeaf()) {
                for (TreeItem<ConditionNode> dayChild : dayItem.getChildren()) {
                    if (hour.equals(dayChild.getValue().getTitle())) {
                        hourItem = (CheckBoxTreeItem<ConditionNode>) dayChild;
                        break;
                    }
                }
            }
            if (hourItem == null) {
                CheckBoxTreeItem<ConditionNode> newHourItem = new CheckBoxTreeItem(
                        ConditionNode.create(hour)
                                .setTitle(hour)
                                .setCondition(condition(hour + ":00:00", hour + ":59:59"))
                );
                dayItem.getChildren().add(newHourItem);
                newHourItem.setExpanded(false);

                TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                newHourItem.getChildren().add(dummyItem);
                newHourItem.expandedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                            if (newVal && !loaded(newHourItem)) {
                                loadTimes(newHourItem);
                            }
                        });
            }
        }
    }

    protected void loadTimes(CheckBoxTreeItem<ConditionNode> parentItem) {
        if (parentItem == null || times == null || times.isEmpty()) {
            return;
        }
        parentItem.getChildren().clear();
        String hour = parentItem.getValue().getTitle();
        for (Date time : times) {
            String timeString = DateTools.datetimeToString(time);
            if (!timeString.startsWith(hour)) {
                continue;
            }
            CheckBoxTreeItem<ConditionNode> timeItem = null;
            if (!parentItem.isLeaf()) {
                for (TreeItem<ConditionNode> node : parentItem.getChildren()) {
                    if (timeString.equals(node.getValue().getTitle())) {
                        timeItem = (CheckBoxTreeItem<ConditionNode>) node;
                        break;
                    }
                }
            }
            if (timeItem == null) {
                timeItem = new CheckBoxTreeItem(
                        ConditionNode.create(timeString)
                                .setTitle(timeString)
                                .setCondition(condition(timeString))
                );
                parentItem.getChildren().add(timeItem);
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
            long startTime = DateTools.stringToDatetime(start).getTime();
            long endTime = DateTools.stringToDatetime(end).getTime();
            return " " + fieldName + " BETWEEN " + startTime + " AND " + endTime;
        }
    }

    protected String condition(String time) {
        if (!isEra) {
            return " " + fieldName + "= '" + time + "'";
        } else {
            long timeValue = DateTools.stringToDatetime(time).getTime();
            return " " + fieldName + "=" + timeValue;
        }
    }

}
