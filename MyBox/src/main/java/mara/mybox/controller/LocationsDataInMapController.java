package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import mara.mybox.data.BaseTask;
import mara.mybox.data.Location;
import mara.mybox.db.TableLocation;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-1-24
 * @License Apache License Version 2.0
 */
public class LocationsDataInMapController extends LocationMapBaseController {

    protected LocationEditController editor;
    protected String dataSet;

    @FXML
    protected ComboBox<String> datasetSelector, intervalSelector;
    @FXML
    protected ToggleGroup displayGroup, orderGroup;
    @FXML
    protected RadioButton transitionRadio, distirbutionRadio,
            orderLabelRadio, orderAddressRadio, orderValueRadio, orderSizeRadio, orderTimeRadio;
    @FXML
    protected VBox displayBox, transitionBox;
    @FXML
    protected CheckBox orderDescCheck, linkCheck;
    @FXML
    protected Button displayButton;
    @FXML
    protected Label titleLabel;

    public LocationsDataInMapController() {
        baseTitle = AppVariables.message("LocationsDataInMap");

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            loadDataNames();
            datasetSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                        try {
                            String value = datasetSelector.getValue();
                            if (value == null || value.trim().isBlank()) {
                                displayButton.setDisable(true);
                            } else {
                                dataSet = value;
                                displayButton.setDisable(false);
                            }
                        } catch (Exception e) {
                            displayButton.setDisable(true);
                        }
                    });

            displayGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                        checkDisplay();
                    });
            checkDisplay();

            fitViewCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
                        AppVariables.setUserConfigValue("LocationInMapFitView", newv);
                    });
            fitViewCheck.setSelected(AppVariables.getUserConfigBoolean("LocationInMapFitView", true));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkDisplay() {
        if (distirbutionRadio.isSelected()) {
            if (displayBox.getChildren().contains(transitionBox)) {
                displayBox.getChildren().remove(transitionBox);
            }
        } else {
            if (!displayBox.getChildren().contains(transitionBox)) {
                displayBox.getChildren().add(3, transitionBox);
            }
        }
    }

    protected void loadDataNames() {
        synchronized (this) {
            BaseTask namesTask = new BaseTask<Void>() {

                private List<String> names;

                @Override
                protected boolean handle() {
                    Location.ChinaEarlyCultures();
                    names = TableLocation.datasets();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    datasetSelector.getItems().addAll(names);
                    isSettingValues = false;
                }
            };
            openHandlingStage(namesTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(namesTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public String markerImage(Location location) {
        String path = "/" + ControlStyle.getIconPath();
        if (markerCircleRadio.isSelected()) {
            return FxmlControl.getInternalFile(path + "iconCircle.png", "map",
                    AppVariables.ControlColor.name() + "Circle.png").getAbsolutePath();

        } else if (markerImageRadio.isSelected()) {
            if (sourceFile != null && sourceFile.exists()) {
                return sourceFile.getAbsolutePath();
            }

        } else if (markerDataImageRadio.isSelected()) {
            if (location != null && location.getImageLocation() != null) {
                return location.getImageLocation();
            }
        }
        return FxmlControl.getInternalFile(path + "iconLocation.png", "map",
                AppVariables.ControlColor.name() + "Point.png").getAbsolutePath();
    }

    public String markerLabel(Location location) {
        if (textNoneRadio.isSelected()) {
            return "";

        } else if (textCoordinateRadio.isSelected()) {
            return location.getLongitude() + "," + location.getLatitude();

        } else if (textAddressRadio.isSelected()) {
            if (location != null && location.getAddress() != null) {
                return location.getAddress();
            } else {
                return "";
            }

        } else if (textLabelRadio.isSelected()) {
            if (location != null && location.getDataLabel() != null) {
                return StringTools.replaceAll(location.getDataLabel(), "\n", "</br>");
            } else {
                return "";
            }

        } else if (textValueRadio.isSelected()) {
            if (location != null && location.getDataValue() > Double.MIN_VALUE) {
                return location.getDataValue() + "";
            } else {
                return "";
            }

        } else if (textSizeRadio.isSelected()) {
            if (location != null && location.getDataSize() > Double.MIN_VALUE) {
                return location.getDataSize() + "";
            } else {
                return "";
            }

        } else if (textTimeRadio.isSelected()) {
            if (location != null && location.getDataTime() >= 0) {
                String s = DateTools.datetimeToString(location.getDataTime());
                if (location.isTimeBC()) {
                    s = message("BC") + s;
                }
                return s;
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public String markerInfo(Location location) {
        String s = "";
        if (popLabelCheck.isSelected() && location.getDataLabel() != null) {
            s += location.getDataLabel() + "<hr>";
        }
        if (popAddressCheck.isSelected() && location.getDataLabel() != null) {
            s += location.getDataLabel() + "</br>";
        }
        if (popCoordinateCheck.isSelected()) {
            s += location.getLongitude() + "," + location.getLatitude() + "</br>";
        }
        if (popValueCheck.isSelected() && location.getDataValue() > Double.MIN_VALUE) {
            s += message("Data") + ": " + location.getDataValue() + "</br>";
        }
        if (popSizeCheck.isSelected() && location.getDataSize() > Double.MIN_VALUE) {
            s += message("Size") + ": " + location.getDataSize() + "</br>";
        }
        if (popTimeCheck.isSelected() && location.getDataTime() > 0) {
            s += DateTools.datetimeToString(location.getDataTime()) + "</br>";
        }
        if (popCommentsCheck.isSelected() && location.getComments() != null) {
            s += location.getDataValue() + "</br>";
        }
        if (popImageCheck.isSelected() && location.getImageLocation() != null) {
            s += "<img src=\"file:///" + location.getImageLocation() + "\" width=100>";
        }
        return StringTools.replaceAll(s, "\n", "</br>");
    }

    @FXML
    public void displayAction() {
        webEngine.executeScript("clearMap();");
        String name = datasetSelector.getValue();
        if (name == null || name.isBlank()) {
            return;
        }
        titleLabel.setText(name);
        if (distirbutionRadio.isSelected()) {
            displayDistibution(name);
        } else {
            displayTransition(name);
        }
    }

    public void displayDistibution(String name) {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<Location> locations;

                @Override
                protected boolean handle() {
                    locations = TableLocation.read(name);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (locations == null || locations.isEmpty()) {
                        return;
                    }
                    String image;
                    for (Location location : locations) {
                        image = markerImage(location);
                        if (image != null) {
                            image = StringTools.replaceAll(markerImage(location), "\\", "/");
                        }
                        mapZoom = fitViewCheck.isSelected() ? 0 : 11;
                        webEngine.executeScript("addMarker(" + +location.getLongitude() + "," + location.getLatitude()
                                + ", " + markerSize + ", '" + markerLabel(location) + "', '" + markerInfo(location)
                                + "', '" + image + "', " + mapZoom + ", true);");

                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    public void displayTransition(String name) {
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<Location> locations;

                @Override
                protected boolean handle() {
                    String order = "data_label";
                    if (orderAddressRadio.isSelected()) {
                        order = "address";
                    } else if (orderValueRadio.isSelected()) {
                        order = "data_value";
                    } else if (orderSizeRadio.isSelected()) {
                        order = "data_size";
                    } else if (orderTimeRadio.isSelected()) {
                        order = "data_time";
                    }
                    locations = TableLocation.readOrder(name, order, orderDescCheck.isSelected());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (locations == null || locations.isEmpty()) {
                        return;
                    }
                    String image;
                    for (Location location : locations) {
                        image = markerImage(location);
                        if (image != null) {
                            image = StringTools.replaceAll(markerImage(location), "\\", "/");
                        }
                        webEngine.executeScript("addMarker(" + +location.getLongitude() + "," + location.getLatitude()
                                + ", " + markerSize + ", '" + markerLabel(location) + "', '" + markerInfo(location)
                                + "', '" + image + "', " + fitViewCheck.isSelected() + ", true);");

                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @Override
    protected void mapLoaded() {
        mapLoaded = true;
        rightPane.setDisable(false);
        checkLanguage();
        if (dataSet != null) {
            displayAction();
        }
    }

    public void display(String name) {
        dataSet = name;
        datasetSelector.getSelectionModel().select(name);
        if (mapLoaded) {
            displayAction();
        }
    }

    @FXML
    public void exampleAction() {

    }

    @FXML
    public void footAction() {

    }
}
