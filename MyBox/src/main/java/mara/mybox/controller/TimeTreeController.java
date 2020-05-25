package mara.mybox.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

    protected List<Date> times;

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

    protected void loadTree(List<Date> times) {
        this.times = times;
        loadTree();
    }

    @Override
    public void loadTree() {
        treeView.setRoot(null);
        if (times == null || times.isEmpty()) {
            return;
        }
        try {
            CheckBoxTreeItem<ConditionNode> allItem = new CheckBoxTreeItem(
                    ConditionNode.create(message("AllTime"))
                            .setTitle(message("AllTime"))
                            .setCondition("")
            );
            allItem.setExpanded(true);
            treeView.setRoot(allItem);

            for (Date time : times) {
                String timeString = DateTools.datetimeToString(time);
                addTime(timeString);
            }

            treeView.setSelection();
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void addTime(String timeString) {
        String year = timeString.substring(0, 4);
        CheckBoxTreeItem<ConditionNode> yearItem = null;
        CheckBoxTreeItem<ConditionNode> root = (CheckBoxTreeItem<ConditionNode>) treeView.getRoot();
        if (!root.isLeaf()) {
            for (TreeItem<ConditionNode> rootChild : root.getChildren()) {
                if (year.equals(rootChild.getValue().getTitle())) {
                    yearItem = (CheckBoxTreeItem<ConditionNode>) rootChild;
                    break;
                }
            }
        }
        if (yearItem == null) {
            yearItem = new CheckBoxTreeItem(
                    ConditionNode.create(year)
                            .setTitle(year)
                            .setCondition(" time BETWEEN '" + year
                                    + "-01-01 00:00:00' AND '" + year + "-12-31 23:59:59'")
            );
            root.getChildren().add(yearItem);
        }
        yearItem.setExpanded(true);

        String month = timeString.substring(0, 7);
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
            monthItem = new CheckBoxTreeItem(
                    ConditionNode.create(month)
                            .setTitle(month)
                            .setCondition(" time BETWEEN '" + start
                                    + "' AND '" + endDay + " 23:59:59'")
            );
            yearItem.getChildren().add(monthItem);
        }
        monthItem.setExpanded(treeView.getExpandedNodes().contains(month));

        String day = timeString.substring(0, 10);
        CheckBoxTreeItem<ConditionNode> dayItem = null;
        if (!monthItem.isLeaf()) {
            for (TreeItem<ConditionNode> monthChild : monthItem.getChildren()) {
                if (month.equals(monthChild.getValue().getTitle())) {
                    dayItem = (CheckBoxTreeItem<ConditionNode>) monthChild;
                    break;
                }
            }
        }
        if (dayItem == null) {
            dayItem = new CheckBoxTreeItem(
                    ConditionNode.create(day)
                            .setTitle(day)
                            .setCondition(" time BETWEEN '" + day
                                    + " 00:00:00' AND '" + day + " 23:59:59'")
            );
            dayItem.setExpanded(false);
            monthItem.getChildren().add(dayItem);
        }
        dayItem.setExpanded(treeView.getExpandedNodes().contains(day));

        CheckBoxTreeItem<ConditionNode> timeItem = null;
        if (!dayItem.isLeaf()) {
            for (TreeItem<ConditionNode> dayChild : dayItem.getChildren()) {
                if (timeString.equals(dayChild.getValue().getTitle())) {
                    timeItem = (CheckBoxTreeItem<ConditionNode>) dayChild;
                    break;
                }
            }
        }
        if (timeItem == null) {
            timeItem = new CheckBoxTreeItem(
                    ConditionNode.create(timeString)
                            .setTitle(timeString)
                            .setCondition(" time='" + timeString + "' ")
            );
            dayItem.getChildren().add(timeItem);
        }
    }

}
