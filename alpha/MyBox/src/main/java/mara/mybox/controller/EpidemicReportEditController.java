package mara.mybox.controller;

import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import mara.mybox.db.data.EpidemicReport;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class EpidemicReportEditController extends GeographyCodeUserController {

    protected EpidemicReportsController reportsController;
    protected long confirmed, dead, healed;
    protected long epid, time;
    protected EpidemicReport report;

    @FXML
    protected TextField epidInput, confirmedInput, healedInput, deadInput,
            locationInput, timeInput, labelInput;
    @FXML
    protected TextArea commentsArea;
    @FXML
    protected ComboBox<String> datasetSelector;
    @FXML
    protected RadioButton inputtedRadio, predefinedRadio, filledRadio, statisticRadio;

    public EpidemicReportEditController() {
        baseTitle = Languages.message("EpidemicReport");
        TipsLabelKey = "EpidemicReportEditComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            selectedCode = null;
            epid = -1;
            time = -1;

            confirmedInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkConfirmed();
                    });

            healedInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkHealed();
                    });

            deadInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkDead();
                    });

            timeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkTime();
                    });
            timeInput.setText(DateTools.nowString());

            saveButton.disableProperty().bind(timeInput.styleProperty().isEqualTo(NodeStyleTools.badStyle)
                    .or(confirmedInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                    .or(healedInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                    .or(deadInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            loadDatasets();
            locationController.loadTree(this);
            confirmedInput.requestFocus();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(timeInput, Languages.message("TimeComments"));
            NodeStyleTools.setTooltip(epidInput, Languages.message("AssignedByMyBox"));
            NodeStyleTools.setTooltip(locationInput, Languages.message("ClickNodePickValue"));
            NodeStyleTools.setTooltip(clearButton, Languages.message("Reset"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void loadDatasets() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<String> datasets;

                @Override
                protected boolean handle() {
                    datasets = TableEpidemicReport.datasets();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    String v = datasetSelector.getValue();
                    datasetSelector.getItems().clear();
                    datasetSelector.getItems().addAll(datasets);
                    if (v != null && !v.isBlank()) {
                        datasetSelector.setValue(v);
                    } else if (datasets.size() > 0) {
                        datasetSelector.getSelectionModel().select(0);
                    }
                    isSettingValues = false;
                }
            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    protected void checkConfirmed() {
        try {
            String value = confirmedInput.getText().trim();
            if (value.isBlank()) {
                confirmed = 0;
                confirmedInput.setStyle(null);
                return;
            }
            int v = Integer.valueOf(value);
            if (v >= 0) {
                confirmed = v;
                confirmedInput.setStyle(null);
            } else {
                confirmedInput.setStyle(NodeStyleTools.badStyle);
            }
        } catch (Exception e) {
            confirmedInput.setStyle(NodeStyleTools.badStyle);
        }
    }

    protected void checkHealed() {
        try {
            String value = healedInput.getText().trim();
            if (value.isBlank()) {
                healed = 0;
                healedInput.setStyle(null);
                return;
            }
            int v = Integer.valueOf(value);
            if (v >= 0) {
                healed = v;
                healedInput.setStyle(null);
            } else {
                healedInput.setStyle(NodeStyleTools.badStyle);
            }
        } catch (Exception e) {
            healedInput.setStyle(NodeStyleTools.badStyle);
        }
    }

    protected void checkDead() {
        try {
            String value = deadInput.getText().trim();
            if (value.isBlank()) {
                dead = 0;
                deadInput.setStyle(null);
                return;
            }
            int v = Integer.valueOf(value);
            if (v >= 0) {
                dead = v;
                deadInput.setStyle(null);
            } else {
                deadInput.setStyle(NodeStyleTools.badStyle);
            }
        } catch (Exception e) {
            deadInput.setStyle(NodeStyleTools.badStyle);
        }
    }

    protected void checkTime() {
        try {
            String value = timeInput.getText().trim();
            if (value.isBlank()) {
                time = new Date().getTime();
                timeInput.setStyle(null);
                return;
            }
            Date v = DateTools.stringToDatetime(value);
            if (v != null) {
                time = v.getTime();
                timeInput.setStyle(null);
            } else {
                timeInput.setStyle(NodeStyleTools.badStyle);
            }
        } catch (Exception e) {
            timeInput.setStyle(NodeStyleTools.badStyle);
        }
    }

    @Override
    public void codeSelected(GeographyCode code) {
        try {
            if (code == null) {
                return;
            }
            selectedCode = code;
            locationInput.setText(selectedCode.getFullName());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadReport(EpidemicReport report) {
        this.report = report;
        loadReport();
    }

    public void loadReport() {
        try {
            if (report == null) {
                return;
            }
            isSettingValues = true;
            epid = report.getEpid();
            selectedCode = report.getLocation();
            time = report.getTime();
            confirmed = report.getConfirmed();
            dead = report.getDead();
            healed = report.getHealed();

            epidInput.setText(epid + "");
            datasetSelector.setValue(report.getDataSet());
            switch (report.getSource()) {
                case 1:
                    predefinedRadio.setSelected(true);
                    break;
                case 2:
                    inputtedRadio.setSelected(true);
                    break;
                case 3:
                    filledRadio.setSelected(true);
                    break;
                case 4:
                    statisticRadio.setSelected(true);
                    break;
                default:
                    inputtedRadio.setSelected(true);
                    break;
            }
            confirmedInput.setText(confirmed + "");
            healedInput.setText(healed + "");
            deadInput.setText(dead + "");
            timeInput.setText(DateTools.datetimeToString(time));
            if (selectedCode != null) {
                locationInput.setText(selectedCode.getFullName());
            } else {
                locationInput.setText("");
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void dataAction() {
        openStage(Fxmls.GeographyCodeFxml);
    }

    @FXML
    @Override
    public void clearAction() {
        try {
            if (report != null) {
                loadReport();
                return;
            }
            isSettingValues = true;
            epid = -1;
            selectedCode = null;
            time = new Date().getTime();
            confirmed = 0;
            dead = 0;
            healed = 0;

            epidInput.setText("");
            datasetSelector.setValue("");
            inputtedRadio.setSelected(true);
            confirmedInput.setText("");
            healedInput.setText("");
            deadInput.setText("");
            timeInput.setText(DateTools.datetimeToString(time));
            locationInput.setText("");
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (selectedCode == null) {
            popError(Languages.message("MissLocation"));
            return;
        }
        if (time <= 0) {
            popError(Languages.message("MissTime"));
            return;
        }
        String dataset = datasetSelector.getValue().trim();
        if (dataset.isBlank()) {
            popError(Languages.message("MissDataset"));
            return;
        }
        if (confirmed <= 0 && healed <= 0 && dead <= 0) {
            popError(Languages.message("ValuesShouldNotZero"));
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonTask<Void>() {

            @Override
            protected boolean handle() {
                EpidemicReport report = new EpidemicReport();
                report.setEpid(epid);
                report.setDataSet(dataset);
                report.setLocation(selectedCode);
                report.setConfirmed(confirmed);
                report.setHealed(healed);
                report.setDead(dead);
                report.setTime(time);
                if (predefinedRadio.isSelected()) {
                    report.setSource((short) 1);
                } else if (inputtedRadio.isSelected()) {
                    report.setSource((short) 2);
                } else if (filledRadio.isSelected()) {
                    report.setSource((short) 3);
                } else if (statisticRadio.isSelected()) {
                    report.setSource((short) 4);
                } else {
                    report.setSource((short) 2);
                }
                return TableEpidemicReport.write(report);
            }

            @Override
            protected void whenSucceeded() {
                if (reportsController != null) {
                    reportsController.loadTrees(true);
                }

                if (saveCloseCheck.isSelected()) {
                    closeStage();
                    if (reportsController != null) {
                        reportsController.getMyStage().requestFocus();
                    }
                } else {
                    popInformation(Languages.message("Written"));
                }
            }
        };
        handling(task);
        task.setSelf(task);
        Thread thread = new Thread(task);
        thread.setDaemon(false);
        thread.start();
    }

}
