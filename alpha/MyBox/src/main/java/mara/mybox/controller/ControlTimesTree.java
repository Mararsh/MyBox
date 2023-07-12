package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ConditionNode;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.tools.DateTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.TimeFormats;

/**
 * @Author Mara
 * @CreateDate 2022-04-10
 * @License Apache License Version 2.0
 */
public class ControlTimesTree extends ControlConditionTree {

    protected String tableName, fieldName, baseCondition;
    protected LoadingController loading;

    @FXML
    protected FlowPane buttonsPane;
    @FXML
    protected Button refreshNodesButton, queryNodesButton;

    public ControlTimesTree() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            List<String> s = new ArrayList();
            s.add(message("AllTime"));
            treeView.setSelectedTitles(s);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParent(BaseController parent, String baseCondition, String tableName, String fieldName) {
        setParent(parent);
        this.tableName = tableName;
        this.fieldName = fieldName;
        this.baseCondition = baseCondition;
    }

    @Override
    public void loadTree() {
        loadYears();
    }

    public void loadYears() {
        if (task != null) {
            task.cancel();
        }
        clearTree();
        CheckBoxTreeItem<ConditionNode> root = new CheckBoxTreeItem(
                ConditionNode.create(message("AllTime"))
                        .setTitle(message("AllTime"))
                        .setCondition("")
        );
        root.setExpanded(true);
        treeView.setRoot(root);
        task = new SingletonCurrentTask<Void>(this) {
            private List<String> years;

            @Override
            protected boolean handle() {
                years = new ArrayList<>();
                String sql = "SELECT DISTINCT YEAR(" + fieldName + ") AS dvalue FROM " + tableName
                        + (baseCondition != null && !baseCondition.isBlank() ? " WHERE " + baseCondition : "")
                        + " ORDER BY dvalue DESC";
                try (Connection conn = DerbyBase.getConnection();
                        PreparedStatement query = conn.prepareStatement(sql);
                        ResultSet results = query.executeQuery()) {
                    while (results.next()) {
                        String d = results.getString("dvalue");
                        if (d != null) {
                            String name;
                            if (d.length() < 4) {
                                name = "0".repeat(4 - d.length()) + d;
                            } else {
                                name = d;
                            }
                            years.add(name);
                        } else {
                            years.add(null);
                        }
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                for (String year : years) {
                    String name = year == null ? message("Null") : year;
                    CheckBoxTreeItem<ConditionNode> yearItem = new CheckBoxTreeItem(
                            ConditionNode.create(name)
                                    .setTitle(name)
                                    .setCondition(year == null ? fieldName + " IS NULL"
                                            : condition(name + "-01-01 00:00:00", year + "-12-31 23:59:59"))
                    );
                    root.getChildren().add(yearItem);
                    yearItem.setExpanded(false);
                    if (year == null) {
                        continue;
                    }
                    TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                    yearItem.getChildren().add(dummyItem);
                    yearItem.expandedProperty().addListener(
                            (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                if (newVal && !yearItem.isLeaf() && !loaded(yearItem)) {
                                    loadMonths(yearItem);
                                }
                            });
                }
            }

        };
        start(task, thisPane);
    }

    protected void loadMonths(CheckBoxTreeItem<ConditionNode> yearItem) {
        if (yearItem == null) {
            return;
        }
        yearItem.getChildren().clear();
        String year = yearItem.getValue().getTitle();
        if (year == null) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private List<String> months;

            @Override
            protected boolean handle() {
                months = new ArrayList<>();
                String sql = "SELECT DISTINCT MONTH(" + fieldName + ") AS dvalue FROM " + tableName
                        + " WHERE " + (baseCondition != null && !baseCondition.isBlank() ? baseCondition + " AND " : "")
                        + yearItem.getValue().getCondition()
                        + " ORDER BY dvalue DESC";
                try (Connection conn = DerbyBase.getConnection();
                        PreparedStatement query = conn.prepareStatement(sql);
                        ResultSet results = query.executeQuery()) {
                    while (results.next()) {
                        String d = results.getString("dvalue");
                        if (d.length() < 2) {
                            d = "0" + d;
                        }
                        months.add(year + "-" + d);
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                for (String month : months) {
                    String start = month + "-01 00:00:00";
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(DateTools.encodeDate(start));
                    calendar.add(Calendar.MONTH, 1);
                    calendar.add(Calendar.DATE, -1);
                    String end = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()) + " 23:59:59";
                    CheckBoxTreeItem<ConditionNode> monthItem = new CheckBoxTreeItem(
                            ConditionNode.create(month)
                                    .setTitle(month)
                                    .setCondition(condition(start, end))
                    );
                    yearItem.getChildren().add(monthItem);
                    monthItem.setExpanded(false);

                    TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                    monthItem.getChildren().add(dummyItem);
                    monthItem.expandedProperty().addListener(
                            (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                if (newVal && !monthItem.isLeaf() && !loaded(monthItem)) {
                                    loadDays(monthItem);
                                }
                            });
                }
            }

            @Override
            protected void finalAction() {
                thisPane.setDisable(false);
            }

        };
        start(task, false);
    }

    protected void loadDays(CheckBoxTreeItem<ConditionNode> monthItem) {
        if (monthItem == null) {
            return;
        }
        monthItem.getChildren().clear();
        String month = monthItem.getValue().getTitle();
        if (month == null) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private List<String> days;

            @Override
            protected boolean handle() {
                days = new ArrayList<>();
                String sql = "SELECT DISTINCT DAY(" + fieldName + ") AS dvalue FROM " + tableName
                        + " WHERE " + (baseCondition != null && !baseCondition.isBlank() ? baseCondition + " AND " : "")
                        + monthItem.getValue().getCondition()
                        + " ORDER BY dvalue DESC";
                try (Connection conn = DerbyBase.getConnection();
                        PreparedStatement query = conn.prepareStatement(sql);
                        ResultSet results = query.executeQuery()) {
                    while (results.next()) {
                        String d = results.getString("dvalue");
                        if (d.length() < 2) {
                            d = "0" + d;
                        }
                        days.add(month + "-" + d);
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                for (String day : days) {
                    CheckBoxTreeItem<ConditionNode> dayItem = new CheckBoxTreeItem(
                            ConditionNode.create(day)
                                    .setTitle(day)
                                    .setCondition(condition(day + " 00:00:00", day + " 23:59:59"))
                    );
                    monthItem.getChildren().add(dayItem);
                    dayItem.setExpanded(false);

                    TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                    dayItem.getChildren().add(dummyItem);
                    dayItem.expandedProperty().addListener(
                            (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                if (newVal && !dayItem.isLeaf() && !loaded(dayItem)) {
                                    loadHours(dayItem);
                                }
                            });
                }
            }

            @Override
            protected void finalAction() {
                thisPane.setDisable(false);
            }

        };
        start(task, false);
    }

    protected void loadHours(CheckBoxTreeItem<ConditionNode> dayItem) {
        if (dayItem == null) {
            return;
        }
        dayItem.getChildren().clear();
        String day = dayItem.getValue().getTitle();
        if (day == null) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private List<String> hours;

            @Override
            protected boolean handle() {
                hours = new ArrayList<>();
                String sql = "SELECT DISTINCT HOUR(" + fieldName + ") AS dvalue FROM " + tableName
                        + " WHERE " + (baseCondition != null && !baseCondition.isBlank() ? baseCondition + " AND " : "")
                        + dayItem.getValue().getCondition()
                        + " ORDER BY dvalue DESC";
                try (Connection conn = DerbyBase.getConnection();
                        PreparedStatement query = conn.prepareStatement(sql);
                        ResultSet results = query.executeQuery()) {
                    while (results.next()) {
                        String d = results.getString("dvalue");
                        if (d.length() < 2) {
                            d = "0" + d;
                        }
                        hours.add(day + " " + d);
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                for (String hour : hours) {
                    CheckBoxTreeItem<ConditionNode> hourItem = new CheckBoxTreeItem(
                            ConditionNode.create(hour)
                                    .setTitle(hour)
                                    .setCondition(condition(hour + ":00:00", hour + ":59:59"))
                    );
                    dayItem.getChildren().add(hourItem);
                    hourItem.setExpanded(false);

                    TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                    hourItem.getChildren().add(dummyItem);
                    hourItem.expandedProperty().addListener(
                            (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                if (newVal && !hourItem.isLeaf() && !loaded(hourItem)) {
                                    loadMinutes(hourItem);
                                }
                            });
                }
            }

            @Override
            protected void finalAction() {
                thisPane.setDisable(false);
            }

        };
        start(task, false);
    }

    protected void loadMinutes(CheckBoxTreeItem<ConditionNode> hourItem) {
        if (hourItem == null) {
            return;
        }
        hourItem.getChildren().clear();
        String hour = hourItem.getValue().getTitle();
        if (hour == null) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private List<String> minutes;

            @Override
            protected boolean handle() {
                minutes = new ArrayList<>();
                String sql = "SELECT DISTINCT MINUTE(" + fieldName + ") AS dvalue FROM " + tableName
                        + " WHERE " + (baseCondition != null && !baseCondition.isBlank() ? baseCondition + " AND " : "")
                        + hourItem.getValue().getCondition()
                        + " ORDER BY dvalue DESC";
                try (Connection conn = DerbyBase.getConnection();
                        PreparedStatement query = conn.prepareStatement(sql);
                        ResultSet results = query.executeQuery()) {
                    while (results.next()) {
                        String d = results.getString("dvalue");
                        if (d.length() < 2) {
                            d = "0" + d;
                        }
                        minutes.add(hour + ":" + d);
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                for (String minute : minutes) {
                    CheckBoxTreeItem<ConditionNode> minuteItem = new CheckBoxTreeItem(
                            ConditionNode.create(minute)
                                    .setTitle(minute)
                                    .setCondition(condition(minute + ":00", minute + ":59"))
                    );
                    hourItem.getChildren().add(minuteItem);
                    minuteItem.setExpanded(false);

                    TreeItem<ConditionNode> dummyItem = new TreeItem("Loading");
                    minuteItem.getChildren().add(dummyItem);
                    minuteItem.expandedProperty().addListener(
                            (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                if (newVal && !minuteItem.isLeaf() && !loaded(minuteItem)) {
                                    loadTimes(minuteItem);
                                }
                            });
                }
            }

            @Override
            protected void finalAction() {
                thisPane.setDisable(false);
            }

        };
        start(task, false);
    }

    protected void loadTimes(CheckBoxTreeItem<ConditionNode> minuteItem) {
        if (minuteItem == null) {
            return;
        }
        minuteItem.getChildren().clear();
        String minute = minuteItem.getValue().getTitle();
        if (minute == null) {
            return;
        }
        task = new SingletonCurrentTask<Void>(this) {
            private List<Date> times;

            @Override
            protected boolean handle() {
                times = new ArrayList<>();
                String sql = "SELECT " + fieldName + " FROM " + tableName
                        + " WHERE " + (baseCondition != null && !baseCondition.isBlank() ? baseCondition + " AND " : "")
                        + minuteItem.getValue().getCondition()
                        + " ORDER BY " + fieldName + " DESC";
                try (Connection conn = DerbyBase.getConnection();
                        PreparedStatement query = conn.prepareStatement(sql);
                        ResultSet results = query.executeQuery()) {
                    while (results.next()) {
                        times.add(results.getTimestamp(fieldName));
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                for (Date time : times) {
                    String timeString = DateTools.datetimeToString(time, TimeFormats.DatetimeMs);
                    CheckBoxTreeItem<ConditionNode> timeItem = new CheckBoxTreeItem(
                            ConditionNode.create(timeString)
                                    .setTitle(timeString)
                                    .setCondition(condition(time))
                    );
                    minuteItem.getChildren().add(timeItem);
                    timeItem.setExpanded(false);
                }
            }

            @Override
            protected void finalAction() {
                thisPane.setDisable(false);
            }

        };
        start(task, false);
    }

    protected boolean loaded(TreeItem item) {
        if (item == null || item.isLeaf()) {
            return true;
        }
        Object child = item.getChildren().get(0);
        return child instanceof CheckBoxTreeItem;
    }

    protected String condition(String start, String end) {
        return " " + fieldName + " BETWEEN '" + start + "' AND '" + end + "'";
    }

    protected String condition(Date time) {
        return " " + fieldName + "='" + time + "'";
    }

}
