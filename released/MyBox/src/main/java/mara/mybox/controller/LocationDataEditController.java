package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import mara.mybox.data.CoordinateSystem;
import mara.mybox.db.data.Dataset;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.data.Location;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableLocationData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.cell.ListDatasetCell;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationDataEditController extends BaseController {

    protected TableLocationData tableLocationData;
    protected double longitude, latitude, altitude, precision, speed, dataValue, dataSize;
    protected short direction;
    protected long startTime, endTime;
    protected long dataid = -1;
    protected CoordinateSystem coordinateSystem;
    protected Dataset dataset;
    protected Location loadedLocationData;

    @FXML
    protected TextField dataidInput, labelInput, addressInput, longitudeInput, latitudeInput,
            altitudeInput, precisionInput, speedInput, startTimeInput, endTimeInput, valueInput, sizeInput;
    @FXML
    protected TextArea commentsArea;
    @FXML
    protected ComboBox<Dataset> datasetSelector;
    @FXML
    protected ComboBox<String> directionSelector, coordinateSystemSelector;
    @FXML
    protected Button locationButton;

    public LocationDataEditController() {
        baseTitle = Languages.message("LocationData");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Image);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            startTime = endTime = AppValues.InvalidLong;
            longitude = latitude = -200;
            dataset = null;
            datasetSelector.getEditor().setStyle(UserConfig.badStyle());
            direction = AppValues.InvalidShort;
            tableLocationData = new TableLocationData();

            datasetSelector.setButtonCell(new ListDatasetCell());
            datasetSelector.setCellFactory(new Callback<ListView<Dataset>, ListCell<Dataset>>() {
                @Override
                public ListCell<Dataset> call(ListView<Dataset> p) {
                    return new ListDatasetCell();
                }
            });
            datasetSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends Dataset> ov, Dataset oldv, Dataset newv) -> {
                        dataset = newv;
                    });

            longitudeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checklongitude();
                    });
            checklongitude();
            latitudeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkLatitude();
                    });
            checkLatitude();

            altitudeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkAltitude();
                    });
            checkAltitude();

            precisionInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkPrecision();
                    });
            checkPrecision();

            for (CoordinateSystem.Value item : CoordinateSystem.Value.values()) {
                coordinateSystemSelector.getItems().add(Languages.message(item.name()));
            }
            coordinateSystemSelector.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                if (newValue == null || newValue.isEmpty()) {
                    return;
                }
                coordinateSystem = new CoordinateSystem(newValue);
                UserConfig.setString("GeographyCodeCoordinateSystem", newValue);
            });
            coordinateSystemSelector.getSelectionModel().select(UserConfig.getString("GeographyCodeCoordinateSystem", Languages.message(CoordinateSystem.defaultValue().name())));

            speedInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkSpeed();
                    });
            checkSpeed();

            valueInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkValue();
                    });
            checkValue();

            sizeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkSize();
                    });
            checkSize();

            directionSelector.getItems().addAll(Arrays.asList(Languages.message("NotSetting"),
                    Languages.message("East") + " " + 90, Languages.message("West") + " " + 270,
                    Languages.message("North") + " " + 0, Languages.message("South") + " " + 180,
                    Languages.message("EastNorth") + " " + 45, Languages.message("WestNorth") + " " + 315,
                    Languages.message("EastSouth") + " " + 135, Languages.message("WestSouth") + " " + 225
            ));
            directionSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            if (newValue == null || newValue.isBlank() || Languages.message("NotSetting").equals(newValue)) {
                                direction = AppValues.InvalidShort;
                                return;
                            }
                            String value = newValue;
                            int pos = newValue.indexOf(" ");
                            if (pos >= 0) {
                                value = value.substring(pos);
                            }
                            short v = Short.valueOf(value.trim());
                            if (v >= 0 && v <= 360) {
                                direction = v;
                                directionSelector.getEditor().setStyle(null);
                            } else {
                                directionSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            directionSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            startTimeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkStartTime();
                    });

            endTimeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkEndTime();
                    });

            saveButton.disableProperty().bind(
                    longitudeInput.styleProperty().isEqualTo(UserConfig.badStyle())
                            .or(latitudeInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(altitudeInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(precisionInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(speedInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(valueInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(sizeInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(directionSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(startTimeInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(endTimeInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(sourceFileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

            datasetSelector.requestFocus();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(endTimeInput, Languages.message("EraComments"));
            NodeStyleTools.setTooltip(startTimeInput, Languages.message("EraComments"));
            NodeStyleTools.setTooltip(locationButton, Languages.message("CoordinateOnMap"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    protected void checklongitude() {
        try {
            double v = Double.valueOf(longitudeInput.getText().trim());
            if (v >= -180 && v <= 180) {
                longitude = v;
                longitudeInput.setStyle(null);
            } else {
                longitudeInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            longitudeInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkLatitude() {
        try {
            double v = Double.valueOf(latitudeInput.getText().trim());
            if (v >= -90 && v <= 90) {
                latitude = v;
                latitudeInput.setStyle(null);
            } else {
                latitudeInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            latitudeInput.setStyle(UserConfig.badStyle());
        }
    }

    @FXML
    public void locationAction(ActionEvent event) {
        try {
            LocationInMapController controller = (LocationInMapController) openStage(Fxmls.LocationInMapFxml);
            controller.loadCoordinate(this, longitude, latitude);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setGeographyCode(GeographyCode code) {
        try {
            if (code == null || !GeographyCodeTools.validCoordinate(code)) {
                return;
            }
            longitudeInput.setText(code.getLongitude() + "");
            latitudeInput.setText(code.getLatitude() + "");
            addressInput.setText(code.getFullName());
            labelInput.setText(code.getName());

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkAltitude() {
        try {
            String value = altitudeInput.getText().trim();
            if (value.isBlank()) {
                altitude = AppValues.InvalidDouble;
                altitudeInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            altitude = v;
            altitudeInput.setStyle(null);
        } catch (Exception e) {
            altitudeInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkPrecision() {
        try {
            String value = precisionInput.getText().trim();
            if (value.isBlank()) {
                precision = AppValues.InvalidDouble;
                precisionInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            precision = v;
            precisionInput.setStyle(null);
        } catch (Exception e) {
            precisionInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkSpeed() {
        try {
            String value = speedInput.getText().trim();
            if (value.isBlank()) {
                speed = AppValues.InvalidDouble;
                speedInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            if (v >= 0) {
                speed = v;
                speedInput.setStyle(null);
            } else {
                speedInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            speedInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkValue() {
        try {
            String value = valueInput.getText().trim();
            if (value.isBlank()) {
                dataValue = AppValues.InvalidDouble;
                valueInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            dataValue = v;
            valueInput.setStyle(null);
        } catch (Exception e) {
            valueInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkSize() {
        try {
            String value = sizeInput.getText().trim();
            if (value.isBlank()) {
                dataSize = AppValues.InvalidDouble;
                sizeInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            dataSize = v;
            sizeInput.setStyle(null);
        } catch (Exception e) {
            sizeInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkStartTime() {
        try {
            String value = startTimeInput.getText().trim();
            if (value.isBlank()) {
                startTime = AppValues.InvalidLong;
                startTimeInput.setStyle(null);
                return;
            }
            Date v = DateTools.encodeEra(value);
            if (v != null) {
                startTime = v.getTime();
                startTimeInput.setStyle(null);
            } else {
                startTimeInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            startTimeInput.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkEndTime() {
        try {
            String value = endTimeInput.getText().trim();
            if (value.isBlank()) {
                endTime = AppValues.InvalidLong;
                endTimeInput.setStyle(null);
                return;
            }
            Date v = DateTools.encodeEra(value);
            if (v != null) {
                endTime = v.getTime();
                endTimeInput.setStyle(null);
            } else {
                endTimeInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            endTimeInput.setStyle(UserConfig.badStyle());
        }
    }

    public void initEditor(BaseController parentController, Location location) {
        if (parentController == null || !(parentController instanceof LocationDataController)) {
            return;
        }
        this.parentController = parentController;
        loadedLocationData = location;
        loadDatasets();
    }

    public void loadDatasets() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            datasetSelector.getItems().clear();
            task = new SingletonTask<Void>(this) {
                protected List<Dataset> datasets;

                @Override
                protected boolean handle() {
                    datasets = tableLocationData.datasets();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (datasets == null || datasets.isEmpty()) {
                        alertError(Languages.message("MissDataset"));
                        closeStage();
                    } else {
                        datasetSelector.getItems().addAll(datasets);
                        datasetSelector.getSelectionModel().select(0);
                        loadLocation();
                    }
                }
            };
            start(task);
        }
    }

    public void loadLocation() {
        try {
            if (loadedLocationData == null || loadedLocationData.getDataset() == null) {
                clearData();
                return;
            }
            dataid = loadedLocationData.getLdid();
            dataidInput.setText(dataid + "");
            dataset = loadedLocationData.getDataset();
            for (Dataset d : datasetSelector.getItems()) {
                if (d.getDataSet().equals(dataset.getDataSet())) {
                    datasetSelector.getSelectionModel().select(d);
                    break;
                }
            }
            if (loadedLocationData.getAddress() != null) {
                addressInput.setText(loadedLocationData.getAddress());
            } else {
                addressInput.setText("");
            }
            if (loadedLocationData.getLongitude() >= -180 && loadedLocationData.getLongitude() <= 180) {
                longitudeInput.setText(loadedLocationData.getLongitude() + "");
            } else {
                longitudeInput.setText("");
            }
            if (loadedLocationData.getLatitude() >= -90 && loadedLocationData.getLatitude() <= 90) {
                latitudeInput.setText(loadedLocationData.getLatitude() + "");
            } else {
                latitudeInput.setText("");
            }
            if (loadedLocationData.getAltitude() != AppValues.InvalidDouble) {
                altitudeInput.setText(loadedLocationData.getAltitude() + "");
            } else {
                altitudeInput.setText("");
            }
            if (loadedLocationData.getPrecision() != AppValues.InvalidDouble) {
                precisionInput.setText(loadedLocationData.getPrecision() + "");
            } else {
                precisionInput.setText("");
            }
            if (loadedLocationData.getSpeed() != AppValues.InvalidDouble) {
                speedInput.setText(loadedLocationData.getSpeed() + "");
            } else {
                speedInput.setText("");
            }
            coordinateSystemSelector.getSelectionModel().select(Languages.message(loadedLocationData.getCoordinateSystem().name()));
            if (loadedLocationData.getStartTime() != AppValues.InvalidLong) {
                startTimeInput.setText(DateTools.textEra(loadedLocationData.getStartEra()));
            } else {
                startTimeInput.setText("");
            }
            if (loadedLocationData.getEndTime() != AppValues.InvalidLong) {
                endTimeInput.setText(DateTools.textEra(loadedLocationData.getEndEra()));
            } else {
                endTimeInput.setText("");
            }
            if (loadedLocationData.getDataSize() != AppValues.InvalidDouble) {
                sizeInput.setText(loadedLocationData.getDataSize() + "");
            } else {
                sizeInput.setText("");
            }
            if (loadedLocationData.getComments() != null) {
                commentsArea.setText(loadedLocationData.getComments());
            } else {
                commentsArea.setText("");
            }
            if (loadedLocationData.getLabel() != null) {
                labelInput.setText(loadedLocationData.getLabel());
            } else {
                labelInput.setText("");
            }
            short v = loadedLocationData.getDirection();
            if (v != AppValues.InvalidShort) {
                directionSelector.setValue(v + "");
            } else {
                directionSelector.setValue(Languages.message("NotSetting"));
            }
            if (loadedLocationData.getImage() != null) {
                sourceFileInput.setText(loadedLocationData.getImage().getAbsolutePath());
            } else {
                sourceFileInput.setText("");
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void datasetAction(ActionEvent event) {
        DatasetController controller
                = (DatasetController) openStage(Fxmls.DatasetFxml);
        controller.load(this, Languages.tableMessage("Location_data"));
    }

    public void clearData() {
        try {
            dataidInput.clear();
            labelInput.clear();
            addressInput.clear();
            longitudeInput.clear();
            latitudeInput.clear();
            altitudeInput.clear();
            precisionInput.clear();
            speedInput.clear();
            startTimeInput.clear();
            endTimeInput.clear();
            valueInput.clear();
            sizeInput.clear();
            sourceFileInput.clear();
            commentsArea.clear();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popStartExample(MouseEvent mouseEvent) {
        popMenu = PopTools.popEraExample(popMenu, startTimeInput, mouseEvent);
    }

    @FXML
    public void popEndExample(MouseEvent mouseEvent) {
        popMenu = PopTools.popEraExample(popMenu, endTimeInput, mouseEvent);
    }

    @FXML
    @Override
    public void saveAction() {
        try {
            Location location = new Location();
            if (dataidInput.getText() == null || dataidInput.getText().isBlank()) {
                location.setLdid(-1);
            } else {
                location.setLdid(Long.valueOf(dataidInput.getText()));
            }
            location.setDataset(dataset);
            location.setDatasetid(dataset.getId());
            location.setLabel(labelInput.getText().trim());
            location.setAddress(addressInput.getText().trim());
            location.setLongitude(longitude);
            location.setLatitude(latitude);
            location.setAltitude(altitude);
            location.setPrecision(precision);
            location.setSpeed(speed);
            location.setDirection(direction);
            location.setCoordinateSystem(coordinateSystem);
            location.setDataValue(dataValue);
            location.setDataSize(dataSize);
            location.setStartTime(startTime);
            location.setEndTime(endTime);
            File file = new File(sourceFileInput.getText().trim());
            if (file.exists()) {
                location.setImageName(file.getAbsolutePath());
            }
            if (!commentsArea.getText().trim().isBlank()) {
                location.setComments(commentsArea.getText().trim());
            }
            if (new TableLocationData().writeData(location) == null) {
                popFailed();
                return;
            }

            if (parentController != null) {
                ((LocationDataController) parentController).refreshAction();
                parentController.getMyStage().requestFocus();
            } else {
                LocationDataController controller = (LocationDataController) openStage(Fxmls.LocationDataFxml);
                controller.getMyStage().requestFocus();
            }

            closeStage();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        clearData();
        loadLocation();
    }

    @FXML
    @Override
    public void copyAction() {
        dataidInput.clear();
        labelInput.setText(labelInput.getText() + " - " + Languages.message("Copy"));
    }

    @FXML
    public void refreshAction() {
        loadDatasets();
    }

}
