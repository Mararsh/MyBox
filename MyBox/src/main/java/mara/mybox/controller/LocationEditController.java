package mara.mybox.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import mara.mybox.data.Location;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.TableLocation;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationEditController extends LocationBaseController {

    protected LocationsDataController parent;
    protected double longtitude, latitude, altitude, precision, speed, dataValue, dataSize;
    protected int direction, coordinateSystem;
    protected Date dataTime;
    protected long dataid = -1;

    @FXML
    protected TextField dataidInput, addressInput, longitudeInput, latitudeInput,
            altitudeInput, precisionInput, speedInput, timeInput, valueInput, sizeInput, labelInput;
    @FXML
    protected TextArea commentsArea;
    @FXML
    protected ComboBox<String> directionSelector, datasetSelector;
    @FXML
    protected Button locationButton;
    @FXML
    protected CheckBox timeBCCheck;

    public LocationEditController() {
        baseTitle = AppVariables.message("Location");

        SourceFileType = VisitHistory.FileType.Image;
        SourcePathType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Image;
        TargetFileType = VisitHistory.FileType.Image;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        targetPathKey = "ImageFilePath";
        sourcePathKey = "ImageFilePath";

        sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            datasetSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkDataset();
                    });

            checkLongtitude();
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

            directionSelector.getItems().addAll(Arrays.asList(
                    message("East") + " " + 90, message("West") + " " + 270,
                    message("North") + " " + 0, message("South") + " " + 180,
                    message("EastNorth") + " " + 45, message("WestNorth") + " " + 315,
                    message("EastSouth") + " " + 135, message("WestSouth") + " " + 225
            ));
            directionSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            String value = newValue;
                            int pos = newValue.indexOf(" ");
                            if (pos >= 0) {
                                value = value.substring(pos);
                            }
                            int v = Integer.valueOf(value.trim());
                            if (v >= 0 && v <= 360) {
                                direction = v;
                                directionSelector.getEditor().setStyle(null);
                            } else {
                                directionSelector.getEditor().setStyle(badStyle);
                            }
                        } catch (Exception e) {
                            directionSelector.getEditor().setStyle(badStyle);
                        }
                    });
            directionSelector.getSelectionModel().select(0);

            timeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkTime();
                    });
            timeInput.setText(DateTools.nowString());
            FxmlControl.setTooltip(timeInput, message("LocationDataTimeComments"));

            saveButton.disableProperty().bind(datasetSelector.getEditor().styleProperty().isEqualTo(badStyle)
                    .or(longitudeInput.styleProperty().isEqualTo(badStyle))
                    .or(latitudeInput.styleProperty().isEqualTo(badStyle))
                    .or(altitudeInput.styleProperty().isEqualTo(badStyle))
                    .or(precisionInput.styleProperty().isEqualTo(badStyle))
                    .or(speedInput.styleProperty().isEqualTo(badStyle))
                    .or(valueInput.styleProperty().isEqualTo(badStyle))
                    .or(sizeInput.styleProperty().isEqualTo(badStyle))
                    .or(directionSelector.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(timeInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkDataset() {
        try {
            String value = datasetSelector.getValue();
            if (value == null || value.trim().isBlank()) {
                datasetSelector.getEditor().setStyle(badStyle);
            } else {
                datasetSelector.getEditor().setStyle(null);
            }
        } catch (Exception e) {
            datasetSelector.getEditor().setStyle(badStyle);
        }
    }

    protected void checkAltitude() {
        try {
            String value = altitudeInput.getText().trim();
            if (value.isBlank()) {
                altitude = Double.MIN_VALUE;
                altitudeInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            altitude = v;
            altitudeInput.setStyle(null);
        } catch (Exception e) {
            altitudeInput.setStyle(badStyle);
        }
    }

    protected void checkPrecision() {
        try {
            String value = precisionInput.getText().trim();
            if (value.isBlank()) {
                precision = Double.MIN_VALUE;
                precisionInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            if (v > 0) {
                precision = v;
                precisionInput.setStyle(null);
            } else {
                precisionInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            precisionInput.setStyle(badStyle);
        }
    }

    protected void checkSpeed() {
        try {
            String value = speedInput.getText().trim();
            if (value.isBlank()) {
                speed = Double.MIN_VALUE;
                speedInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            if (v > 0) {
                speed = v;
                speedInput.setStyle(null);
            } else {
                speedInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            speedInput.setStyle(badStyle);
        }
    }

    protected void checkValue() {
        try {
            String value = valueInput.getText().trim();
            if (value.isBlank()) {
                dataValue = Double.MIN_VALUE;
                valueInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            if (v > 0) {
                dataValue = v;
                valueInput.setStyle(null);
            } else {
                valueInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            valueInput.setStyle(badStyle);
        }
    }

    protected void checkSize() {
        try {
            String value = sizeInput.getText().trim();
            if (value.isBlank()) {
                dataSize = Double.MIN_VALUE;
                sizeInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            if (v > 0) {
                dataSize = v;
                sizeInput.setStyle(null);
            } else {
                sizeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            sizeInput.setStyle(badStyle);
        }
    }

    protected void checkTime() {
        try {
            String value = timeInput.getText().trim();
            if (value.isBlank()) {
                dataTime = new Date();
                timeInput.setStyle(null);
                return;
            }
            Date v = DateTools.stringToDatetime(value);
            if (v != null) {
                dataTime = v;
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

    protected void loadDataSets(String dataset) {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<String> datasets;

                @Override
                protected boolean handle() {
                    datasets = TableLocation.datasets();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    datasetSelector.getItems().addAll(datasets);
                    isSettingValues = false;
                    if (dataset != null && datasets.contains(dataset)) {
                        datasetSelector.getSelectionModel().select(dataset);
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void loadLocation(Location location) {
        try {
            loadDataSets(null);
            if (location == null) {
                return;
            }
            dataid = location.getDataid();
            dataidInput.setText(dataid + "");
            datasetSelector.setValue(location.getDataSet());
            if (location.getAddress() != null) {
                addressInput.setText(location.getAddress());
            }
            if (location.getLongitude() > Double.MIN_VALUE) {
                longitudeInput.setText(location.getLongitude() + "");
            }
            if (location.getLatitude() > Double.MIN_VALUE) {
                latitudeInput.setText(location.getLatitude() + "");
            }
            if (location.getAltitude() > Double.MIN_VALUE) {
                altitudeInput.setText(location.getAltitude() + "");
            }
            if (location.getPrecision() > Double.MIN_VALUE) {
                precisionInput.setText(location.getPrecision() + "");
            }
            if (location.getSpeed() > Double.MIN_VALUE) {
                speedInput.setText(location.getSpeed() + "");
            }
            timeInput.setText(DateTools.datetimeToString(location.getDataTime()));
            timeBCCheck.setSelected(location.isTimeBC());
            if (location.getDataValue() > Double.MIN_VALUE) {
                valueInput.setText(location.getDataValue() + "");
            }
            if (location.getDataSize() > Double.MIN_VALUE) {
                sizeInput.setText(location.getDataSize() + "");
            }
            if (location.getComments() != null) {
                commentsArea.setText(location.getComments());
            }
            if (location.getDataLabel() != null) {
                labelInput.setText(location.getDataLabel());
            }
            directionSelector.setValue(location.getDirection() + "");
            if (location.getImageLocation() != null) {
                sourceFileInput.setText(location.getImageLocation());
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        try {
            Location location = new Location();
            location.setDataid(dataid);
            String dataset = datasetSelector.getValue().trim();
            location.setDataSet(dataset);
            location.setAddress(addressInput.getText().trim());
            location.setLongitude(longtitude);
            location.setLatitude(latitude);
            location.setAltitude(altitude);
            location.setPrecision(precision);
            location.setSpeed(speed);
            location.setDirection(direction);
            location.setComments(commentsArea.getText().trim());
            location.setImageLocation(sourceFileInput.getText().trim());
            location.setDataValue(dataValue);
            location.setDataSize(dataSize);
            location.setDataTime(dataTime.getTime());
            location.setTimeBC(timeBCCheck.isSelected());
            location.setDataLabel(labelInput.getText().trim());
            if (TableLocation.write(location)) {
                popSuccessful();
            } else {
                popFailed();
            }

            if (parent != null) {
                parent.loadDataSets();
            }

            if (saveCloseCheck.isSelected()) {
                closeStage();
                if (parent != null) {
                    parent.getMyStage().toFront();
                }
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
