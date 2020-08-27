package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.data.CoordinateSystem;
import mara.mybox.data.Dataset;
import mara.mybox.data.Direction;
import mara.mybox.data.Direction.Name;
import mara.mybox.data.GeographyCode;
import mara.mybox.data.Location;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.TableLocationData;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import static mara.mybox.value.AppVariables.tableMessage;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationDataEditController extends BaseController {

    protected LocationDataController parent;
    protected double longitude, latitude, altitude, precision, speed, dataValue, dataSize;
    protected int direction;
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
    protected ComboBox<String> directionSelector, datasetSelector, coordinateSystemSelector;
    @FXML
    protected Button locationButton;

    public LocationDataEditController() {
        baseTitle = AppVariables.message("LocationData");

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
            startTime = endTime = Long.MIN_VALUE;
            longitude = latitude = -200;
            dataset = null;
            datasetSelector.getEditor().setStyle(badStyle);

            datasetSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkDataset();
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
                coordinateSystemSelector.getItems().add(message(item.name()));
            }
            coordinateSystemSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        if (newValue == null || newValue.isEmpty()) {
                            return;
                        }
                        coordinateSystem = new CoordinateSystem(newValue);
                        AppVariables.setUserConfigValue("GeographyCodeCoordinateSystem", newValue);
                    });
            coordinateSystemSelector.getSelectionModel().select(
                    AppVariables.getUserConfigValue("GeographyCodeCoordinateSystem", message(CoordinateSystem.defaultValue().name())));

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
            for (Name name : Direction.Name.values()) {
                directionSelector.getItems().add(message(name.name()) + " " + Direction.angle(name));
            }
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

            startTimeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkStartTime();
                    });
            FxmlControl.setTooltip(startTimeInput, message("EraComments"));

            endTimeInput.textProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        checkEndTime();
                    });
            FxmlControl.setTooltip(endTimeInput, message("EraComments"));

            saveButton.disableProperty().bind(datasetSelector.getEditor().styleProperty().isEqualTo(badStyle)
                    .or(longitudeInput.styleProperty().isEqualTo(badStyle))
                    .or(latitudeInput.styleProperty().isEqualTo(badStyle))
                    .or(altitudeInput.styleProperty().isEqualTo(badStyle))
                    .or(precisionInput.styleProperty().isEqualTo(badStyle))
                    .or(speedInput.styleProperty().isEqualTo(badStyle))
                    .or(valueInput.styleProperty().isEqualTo(badStyle))
                    .or(sizeInput.styleProperty().isEqualTo(badStyle))
                    .or(directionSelector.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(startTimeInput.styleProperty().isEqualTo(badStyle))
                    .or(endTimeInput.styleProperty().isEqualTo(badStyle))
                    .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkDataset() {
        dataset = null;
        datasetSelector.getEditor().setStyle(badStyle);
        try {
            if (parent != null && parent.datasets != null && !parent.datasets.isEmpty()) {
                String value = datasetSelector.getValue();
                for (Dataset d : parent.datasets) {
                    if (value.equals(d.getDataSet())) {
                        dataset = d;
                        datasetSelector.getEditor().setStyle(null);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            datasetSelector.getEditor().setStyle(badStyle);
        }
    }

    protected void checklongitude() {
        try {
            double v = Double.valueOf(longitudeInput.getText().trim());
            if (v >= -180 && v <= 180) {
                longitude = v;
                longitudeInput.setStyle(null);
            } else {
                longitudeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            longitudeInput.setStyle(badStyle);
        }
    }

    protected void checkLatitude() {
        try {
            double v = Double.valueOf(latitudeInput.getText().trim());
            if (v >= -90 && v <= 90) {
                latitude = v;
                latitudeInput.setStyle(null);
            } else {
                latitudeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            latitudeInput.setStyle(badStyle);
        }
    }

    @FXML
    public void locationAction() {
        try {
            LocationInMapController controller = (LocationInMapController) openStage(CommonValues.LocationInMapFxml);
            controller.consumer = this;
            controller.setCoordinate(longitude, latitude);
            controller.getMyStage().toFront();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void setGeographyCode(GeographyCode code) {
        try {
            if (code == null || !code.validCoordinate()) {
                return;
            }
            longitudeInput.setText(code.getLongitude() + "");
            latitudeInput.setText(code.getLatitude() + "");
            addressInput.setText(code.getFullName());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkAltitude() {
        try {
            String value = altitudeInput.getText().trim();
            if (value.isBlank()) {
                altitude = Double.MAX_VALUE;
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
                precision = Double.MAX_VALUE;
                precisionInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            precision = v;
            precisionInput.setStyle(null);
        } catch (Exception e) {
            precisionInput.setStyle(badStyle);
        }
    }

    protected void checkSpeed() {
        try {
            String value = speedInput.getText().trim();
            if (value.isBlank()) {
                speed = Double.MAX_VALUE;
                speedInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            if (v >= 0) {
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
                dataValue = Double.MAX_VALUE;
                valueInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            dataValue = v;
            valueInput.setStyle(null);
        } catch (Exception e) {
            valueInput.setStyle(badStyle);
        }
    }

    protected void checkSize() {
        try {
            String value = sizeInput.getText().trim();
            if (value.isBlank()) {
                dataSize = Double.MAX_VALUE;
                sizeInput.setStyle(null);
                return;
            }
            double v = Double.valueOf(value);
            dataSize = v;
            sizeInput.setStyle(null);
        } catch (Exception e) {
            sizeInput.setStyle(badStyle);
        }
    }

    protected void checkStartTime() {
        try {
            String value = startTimeInput.getText().trim();
            if (value.isBlank()) {
                startTime = Long.MIN_VALUE;
                startTimeInput.setStyle(null);
                return;
            }
            Date v = DateTools.encodeEra(value);
            if (v != null) {
                startTime = v.getTime();
                startTimeInput.setStyle(null);
            } else {
                startTimeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            startTimeInput.setStyle(badStyle);
        }
    }

    protected void checkEndTime() {
        try {
            String value = endTimeInput.getText().trim();
            if (value.isBlank()) {
                endTime = Long.MIN_VALUE;
                endTimeInput.setStyle(null);
                return;
            }
            Date v = DateTools.encodeEra(value);
            if (v != null) {
                endTime = v.getTime();
                endTimeInput.setStyle(null);
            } else {
                endTimeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            endTimeInput.setStyle(badStyle);
        }
    }

    @Override
    public void afterSceneLoaded() {
        super.afterSceneLoaded();
        FxmlControl.setTooltip(locationButton, message("CoordinateOnMap"));
    }

    public void initEditor(LocationDataController parent, Location location) {
        this.parent = parent;
        if (parent.datasets != null && !parent.datasets.isEmpty()) {
            for (Dataset d : parent.datasets) {
                datasetSelector.getItems().add(d.getDataSet());
            }
            datasetSelector.getSelectionModel().select(0);
        }
        loadedLocationData = location;
        loadLocation();
    }

    public void loadLocation() {
        try {
            if (loadedLocationData == null) {
                clearData();
                return;
            }
            dataid = loadedLocationData.getId();
            dataidInput.setText(dataid + "");
            datasetSelector.setValue(loadedLocationData.getDataset().getDataSet());
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
            if (loadedLocationData.getAltitude() != Double.MAX_VALUE) {
                altitudeInput.setText(loadedLocationData.getAltitude() + "");
            } else {
                altitudeInput.setText("");
            }
            if (loadedLocationData.getPrecision() != Double.MAX_VALUE) {
                precisionInput.setText(loadedLocationData.getPrecision() + "");
            } else {
                precisionInput.setText("");
            }
            if (loadedLocationData.getSpeed() != Double.MAX_VALUE) {
                speedInput.setText(loadedLocationData.getSpeed() + "");
            } else {
                speedInput.setText("");
            }
            coordinateSystemSelector.getSelectionModel().select(
                    message(loadedLocationData.getCoordinateSystem().name()));
            if (loadedLocationData.getStartTime() != Long.MIN_VALUE) {
                startTimeInput.setText(DateTools.textEra(loadedLocationData.getStartEra()));
            } else {
                startTimeInput.setText("");
            }
            if (loadedLocationData.getEndTime() != Long.MIN_VALUE) {
                endTimeInput.setText(DateTools.textEra(loadedLocationData.getEndEra()));
            } else {
                endTimeInput.setText("");
            }
            if (loadedLocationData.getDataSize() != Double.MAX_VALUE) {
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
            directionSelector.setValue(loadedLocationData.getDirection() + "");
            if (loadedLocationData.getImage() != null) {
                sourceFileInput.setText(loadedLocationData.getImage());
            } else {
                sourceFileInput.setText("");
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void datasetAction() {
        DatasetController controller
                = (DatasetController) openStage(CommonValues.DatasetFxml);
        controller.parent = this;
        controller.load(tableMessage("Location_data"));
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
            logger.error(e.toString());
        }
    }

    @FXML
    public void popStartExample(MouseEvent mouseEvent) {
        popExample(startTimeInput, mouseEvent);
    }

    @FXML
    public void popEndExample(MouseEvent mouseEvent) {
        popExample(endTimeInput, mouseEvent);
    }

    public void popExample(TextField input, MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            List<String> values = new ArrayList<>();
            values.addAll(Arrays.asList(
                    "2020-07-10 10:10:10", "960-01-23", "581",
                    "-2020-07-10 10:10:10", "-960-01-23", "-581"
            ));
            if (AppVariables.isChinese()) {
                values.addAll(Arrays.asList(
                        "公元960", "公元960-01-23", "公元2020-07-10 10:10:10",
                        "公元前202", "公元前770-12-11", "公元前1046-03-10 10:10:10"
                ));
            }
            values.addAll(Arrays.asList(
                    "202 BC", "770-12-11 BC", "1046-03-10 10:10:10 BC",
                    "581 AD", "960-01-23 AD", "2020-07-10 10:10:10 AD"
            ));

            MenuItem menu;
            for (String value : values) {
                menu = new MenuItem(value);
                menu.setOnAction((ActionEvent event) -> {
                    input.setText(value);
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("MenuClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
                popMenu = null;
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void saveAction() {
        try {
            Location location = new Location();
            if (dataidInput.getText() == null || dataidInput.getText().isBlank()) {
                location.setId(-1);
            } else {
                location.setId(Long.valueOf(dataidInput.getText()));
            }
            location.setDataset(dataset);
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
                location.setImage(file.getAbsolutePath());
            }
            if (!commentsArea.getText().trim().isBlank()) {
                location.setComments(commentsArea.getText().trim());
            }
            if (new TableLocationData().writeData(location) != null) {
                popSuccessful();
            } else {
                popFailed();
            }

            if (parent != null) {
                parent.refreshAction();
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

    @FXML
    @Override
    public void recoverAction() {
        loadLocation();
    }

    @FXML
    @Override
    public void copyAction() {
        dataidInput.clear();
        labelInput.setText(labelInput.getText() + " - " + message("Copy"));
    }

}
