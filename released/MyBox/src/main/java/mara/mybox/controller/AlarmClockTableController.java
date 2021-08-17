package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import mara.mybox.db.data.AlarmClock;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-7-13
 * @Description
 * @License Apache License Version 2.0
 */
public class AlarmClockTableController extends BaseController {

    protected AlarmClockController alarmClockController;
    protected ObservableList<AlarmClock> tableData = FXCollections.observableArrayList();

    @FXML
    protected HBox alarmClocksPane;
    @FXML
    protected Button activeButton, inactiveButton;
    @FXML
    protected TableView<AlarmClock> alarmClocksView;
    @FXML
    protected TableColumn<AlarmClock, String> statusColumn, descriptionColumn, repeatColumn;
    @FXML
    protected TableColumn<AlarmClock, String> nextTimeColumn, soundColumn, lastTimeColumn, startTimeColumn;

    @Override
    public void initControls() {
        try {
            super.initControls();
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            repeatColumn.setCellValueFactory(new PropertyValueFactory<>("repeat"));
            nextTimeColumn.setCellValueFactory(new PropertyValueFactory<>("next"));
            soundColumn.setCellValueFactory(new PropertyValueFactory<>("sound"));
            lastTimeColumn.setCellValueFactory(new PropertyValueFactory<>("last"));
            startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("start"));

            alarmClocksView.setItems(tableData);
            alarmClocksView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            tableData.addAll(AlarmClock.readAlarmClocks());

            tableData.addListener(new ListChangeListener() {
                @Override
                public void onChanged(ListChangeListener.Change change) {
                    if (tableData.size() > 0) {
                        clearButton.setDisable(false);
                    } else {
                        clearButton.setDisable(true);
                    }
                }
            });
            if (tableData.size() > 0) {
                clearButton.setDisable(false);
            } else {
                clearButton.setDisable(true);
            }

            alarmClocksView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    ObservableList<AlarmClock> selected = alarmClocksView.getSelectionModel().getSelectedItems();
                    if (selected != null && selected.size() > 0) {
                        editButton.setDisable(false);
                        deleteButton.setDisable(false);
                        activeButton.setDisable(true);
                        inactiveButton.setDisable(true);
                        for (AlarmClock alarm : selected) {
                            if (alarm.isIsActive()) {
                                inactiveButton.setDisable(false);
                            } else {
                                activeButton.setDisable(false);
                            }
                        }
                    } else {
                        editButton.setDisable(true);
                        deleteButton.setDisable(true);
                        activeButton.setDisable(true);
                        inactiveButton.setDisable(true);
                    }
                }
            });

            alarmClocksView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        ObservableList<AlarmClock> selected = alarmClocksView.getSelectionModel().getSelectedItems();
                        if (selected != null && selected.size() > 0) {
                            alarmClockController.edit(selected.get(0));
                        }
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void clearAction() {
        if (!PopTools.askSure(myStage.getTitle(), Languages.message("SureClearAlarmClocks"))) {
            return;
        }

        tableData.clear();
        deleteButton.setDisable(true);
        editButton.setDisable(true);
        activeButton.setDisable(true);
        inactiveButton.setDisable(true);
        AlarmClock.clearAllAlarmClocks();
    }

    @FXML
    @Override
    public void deleteAction() {
        ObservableList<AlarmClock> selected = alarmClocksView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        AlarmClock.deleteAlarmClocks(selected);
        tableData.removeAll(selected);
    }

    @FXML
    public void editAction(ActionEvent event) {
        ObservableList<AlarmClock> selected = alarmClocksView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        alarmClockController.edit(selected.get(0));
    }

    @FXML
    public void activeAction(ActionEvent event) {
        ObservableList<AlarmClock> selected = alarmClocksView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (AlarmClock alarm : selected) {
            alarm.setIsActive(true);
            alarm.setStatus(Languages.message("Active"));
            AlarmClock.calculateNextTime(alarm);
            alarm.setNext(DateTools.datetimeToString(alarm.getNextTime()));
            AlarmClock.scheduleAlarmClock(alarm);

        }
        AlarmClock.writeAlarmClocks(selected);
        alarmClocksView.refresh();
        activeButton.setDisable(true);
        inactiveButton.setDisable(false);

    }

    @FXML
    public void inactiveAction(ActionEvent event) {
        ObservableList<AlarmClock> selected = alarmClocksView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        for (AlarmClock alarm : selected) {
            alarm.setIsActive(false);
            alarm.setStatus(Languages.message("Inactive"));
            alarm.setNextTime(-1);
            alarm.setNext("");
            AlarmClock.scheduleAlarmClock(alarm);
        }
        AlarmClock.writeAlarmClocks(selected);
        alarmClocksView.refresh();
        inactiveButton.setDisable(true);
        activeButton.setDisable(false);
    }

    @FXML
    public void refreshAction() {
        tableData.clear();
        tableData.addAll(AlarmClock.readAlarmClocks());

        if (tableData.size() > 0) {
            clearButton.setDisable(false);
        } else {
            clearButton.setDisable(true);
        }

    }

    public AlarmClockController getAlarmClockController() {
        return alarmClockController;
    }

    public void setAlarmClockController(AlarmClockController alarmClockController) {
        this.alarmClockController = alarmClockController;
    }

    public void saveAlarm(AlarmClock alarm, boolean isNew) {
        AlarmClock.setExtraValues(alarm);
        AlarmClock.writeAlarmClock(alarm);
        AlarmClock.scheduleAlarmClock(alarm);
        if (isNew) {
            tableData.add(alarm);
        } else {
            int i = AlarmClock.findAlarmIndex(tableData, alarm.getKey());
            if (i >= 0) {
                tableData.set(i, alarm);
//                alarmClocksView.refresh();
            } else {
                tableData.add(alarm);
            }
        }
    }

}
