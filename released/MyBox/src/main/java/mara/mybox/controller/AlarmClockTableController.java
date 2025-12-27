package mara.mybox.controller;

import java.sql.Connection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.AlarmClock;
import mara.mybox.db.table.TableAlarmClock;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-7-13
 * @Description
 * @License Apache License Version 2.0
 */
public class AlarmClockTableController extends BaseController {

    protected AlarmClockController alarmClockController;
    protected ObservableList<AlarmClock> tableData = FXCollections.observableArrayList();
    protected TableAlarmClock tableAlarmClock;

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
            tableAlarmClock = new TableAlarmClock();

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

            alarmClocksView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    ObservableList<AlarmClock> selected = alarmClocksView.getSelectionModel().getSelectedItems();
                    if (selected != null && !selected.isEmpty()) {
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
                        if (selected != null && !selected.isEmpty()) {
                            alarmClockController.edit(selected.get(0));
                        }
                    }
                }
            });

            refreshAction();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void refreshAction() {
        tableData.clear();
        tableData.addAll(tableAlarmClock.readAll());
    }

    @FXML
    @Override
    public void clearAction() {
        if (!PopTools.askSure(getTitle(), Languages.message("SureClearAlarmClocks"))) {
            return;
        }
        tableData.clear();
        deleteButton.setDisable(true);
        editButton.setDisable(true);
        activeButton.setDisable(true);
        inactiveButton.setDisable(true);
        tableAlarmClock.clearData();
    }

    @FXML
    @Override
    public void deleteAction() {
        ObservableList<AlarmClock> selected = alarmClocksView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            popError(message("SelectToHandle"));
            return;
        }
        tableAlarmClock.deleteData(selected);
        tableData.removeAll(selected);
    }

    @FXML
    public void editAction() {
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
        try (Connection conn = DerbyBase.getConnection()) {
            for (AlarmClock alarm : selected) {
                alarm.setIsActive(true);
                alarm.calculateNextTime();
                alarm.addInSchedule();
            }
            tableAlarmClock.updateList(conn, selected);
            alarmClocksView.refresh();
            activeButton.setDisable(true);
            inactiveButton.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void inactiveAction(ActionEvent event) {
        ObservableList<AlarmClock> selected = alarmClocksView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        try (Connection conn = DerbyBase.getConnection()) {
            for (AlarmClock alarm : selected) {
                alarm.setIsActive(false);
                alarm.setNextTime(null);
                alarm.removeFromSchedule();
            }
            tableAlarmClock.updateList(conn, selected);
            alarmClocksView.refresh();
            inactiveButton.setDisable(true);
            activeButton.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public AlarmClockController getAlarmClockController() {
        return alarmClockController;
    }

    public void setAlarmClockController(AlarmClockController alarmClockController) {
        this.alarmClockController = alarmClockController;
    }

    public void saveAlarm(AlarmClock alarm) {
//        AlarmClock.setExtraValues(alarm);
//        AlarmClock.writeAlarmClock(alarm);
//        AlarmClock.scheduleAlarmClock(alarm);
//        if (isNew) {
//            tableData.add(alarm);
//        } else {
//            int i = AlarmClock.findAlarmIndex(tableData, alarm.getKey());
//            if (i >= 0) {
//                tableData.set(i, alarm);


////                alarmClocksView.refresh();
//            } else {
//                tableData.add(alarm);
//            }
//        }
    }

}
