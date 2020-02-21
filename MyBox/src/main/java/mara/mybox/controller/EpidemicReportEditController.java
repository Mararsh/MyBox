package mara.mybox.controller;

import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import mara.mybox.data.BaseTask;
import mara.mybox.data.EpidemicReport;
import mara.mybox.data.GeographyCode;
import mara.mybox.db.TableEpidemicReport;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-2-2
 * @License Apache License Version 2.0
 */
public class EpidemicReportEditController extends LocationBaseController {

    protected EpidemicReportsController parent;
    protected int confirmed, suspected, dead, healed;
    protected Date time;
    protected long dataid = -1;

    @FXML
    protected TextField dataidInput, confirmedInput, suspectedInput, healedInput, deadInput, timeInput, labelInput;
    @FXML
    protected TextArea commentsArea;
    @FXML
    protected ComboBox<String> datasetSelector;
    @FXML
    protected Button locationButton;

    public EpidemicReportEditController() {
        baseTitle = AppVariables.message("EpidemicReport");

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            datasetSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkDataset();
                    });

            confirmedInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkConfirmed();
                    });

            suspectedInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkSuspected();
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
            FxmlControl.setTooltip(timeInput, message("LocationDataTimeComments"));

            saveButton.disableProperty().bind(datasetSelector.getEditor().styleProperty().isEqualTo(badStyle)
                    .or(longitudeInput.styleProperty().isEqualTo(badStyle))
                    .or(latitudeInput.styleProperty().isEqualTo(badStyle))
                    .or(confirmedInput.styleProperty().isEqualTo(badStyle))
                    .or(suspectedInput.styleProperty().isEqualTo(badStyle))
                    .or(healedInput.styleProperty().isEqualTo(badStyle))
                    .or(deadInput.styleProperty().isEqualTo(badStyle))
                    .or(timeInput.styleProperty().isEqualTo(badStyle))
            );

            loadDatasets();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void loadDatasets() {
        synchronized (this) {
            BaseTask datasetTask = new BaseTask<Void>() {

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
                    if (v != null) {
                        datasetSelector.setValue(v);
                    }
                    isSettingValues = false;
                }
            };
            openHandlingStage(datasetTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(datasetTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void checkDataset() {
        if (isSettingValues) {
            return;
        }
        try {
            String value = datasetSelector.getValue();
            if (value == null || value.trim().isBlank()) {
                datasetSelector.getEditor().setStyle(badStyle);
            } else {
                datasetSelector.getEditor().setStyle(null);
            }
            loadDatasets();
        } catch (Exception e) {
            datasetSelector.getEditor().setStyle(badStyle);
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
                confirmedInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            confirmedInput.setStyle(badStyle);
        }
    }

    protected void checkSuspected() {
        try {
            String value = suspectedInput.getText().trim();
            if (value.isBlank()) {
                suspected = 0;
                suspectedInput.setStyle(null);
                return;
            }
            int v = Integer.valueOf(value);
            if (v >= 0) {
                suspected = v;
                suspectedInput.setStyle(null);
            } else {
                suspectedInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            suspectedInput.setStyle(badStyle);
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
                healedInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            healedInput.setStyle(badStyle);
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
                deadInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            deadInput.setStyle(badStyle);
        }
    }

    protected void checkTime() {
        try {
            String value = timeInput.getText().trim();
            if (value.isBlank()) {
                time = new Date();
                timeInput.setStyle(null);
                return;
            }
            Date v = DateTools.stringToDatetime(value);
            if (v != null) {
                time = v;
                timeInput.setStyle(null);
            } else {
                timeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            timeInput.setStyle(badStyle);
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(locationButton, message("CoordinateOnMap"));
    }

    public void loadReport(EpidemicReport report) {
        try {
            if (report == null) {
                return;
            }
            isSettingValues = true;
            dataid = report.getDataid();
            dataidInput.setText(dataid + "");
            datasetSelector.setValue(report.getDataSet());
            if (report.getDataLabel() != null) {
                labelInput.setText(report.getDataLabel());
            }
            if (report.getCountry() != null) {
                countrySelector.setValue(report.getCountry());
            }
            if (report.getProvince() != null) {
                provinceSelector.setValue(report.getProvince());
            }
            if (report.getCity() != null) {
                citySelector.setValue(report.getCity());
            }
            confirmedInput.setText(report.getConfirmed() + "");
            suspectedInput.setText(report.getSuspected() + "");
            healedInput.setText(report.getHealed() + "");
            deadInput.setText(report.getDead() + "");
            if (report.getLongitude() > Double.MIN_VALUE) {
                longitudeInput.setText(report.getLongitude() + "");
            }
            if (report.getLatitude() > Double.MIN_VALUE) {
                latitudeInput.setText(report.getLatitude() + "");
            }

            timeInput.setText(DateTools.datetimeToString(report.getTime()));
            if (report.getComments() != null) {
                commentsArea.setText(report.getComments());
            }
            isSettingValues = false;
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (task != null) {
            return;
        }
        task = new SingletonTask<Void>() {

            @Override
            protected boolean handle() {
                EpidemicReport report = new EpidemicReport();
                report.setDataid(dataid);
                String dataset = datasetSelector.getValue().trim();
                report.setDataSet(dataset);
                report.setDataLabel(labelInput.getText().trim());
                report.setCountry(countrySelector.getValue());
                report.setProvince(provinceSelector.getValue());
                report.setCity(citySelector.getValue());
                report.setConfirmed(confirmed);
                report.setSuspected(suspected);
                report.setHealed(healed);
                report.setDead(dead);
                report.setLongitude(longtitude);
                report.setLatitude(latitude);
                report.setComments(commentsArea.getText().trim());
                report.setTime(time.getTime());

                if (countrySelector.getValue() != null
                        && !message("China").equals(countrySelector.getValue())
                        && longtitude >= -180 && longtitude <= 180
                        && latitude >= -90 && latitude <= 90) {
                    GeographyCode code = new GeographyCode();
                    code.setAddress(countrySelector.getValue());
                    code.setCountry(countrySelector.getValue());
                    code.setLongitude(longtitude);
                    code.setLatitude(latitude);
                    code.setLevel(message("Country"));
                    TableGeographyCode.write(code);
                };

                TableEpidemicReport.write(report);

                return true;
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                if (parent != null) {
                    parent.loadTree();
                }

                if (saveCloseCheck.isSelected()) {
                    closeStage();
                    if (parent != null) {
                        parent.getMyStage().toFront();
                    }
                }
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
