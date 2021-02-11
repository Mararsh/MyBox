package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.db.data.BaseDataTools;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.LocationTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationInMapController extends GeographyCodeMapController {

    protected GeographyCode geographyCode;
    protected boolean loading = false;

    @FXML
    protected TextField locateInput;
    @FXML
    protected TextArea dataArea;
    @FXML
    protected ToggleGroup locateGroup;
    @FXML
    protected RadioButton clickMapRadio, addressRadio, coordinateRadio;
    @FXML
    protected CheckBox multipleCheck;
    @FXML
    protected Button clearCodeButton;

    public LocationInMapController() {
        baseTitle = AppVariables.message("LocationInMap");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            mapOptionsController.optionsBox.getChildren().removeAll(mapOptionsController.dataBox);

            setButtons();

            locateGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                        checkLocateMethod();
                    });
            checkLocateMethod();

            multipleCheck.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
                        AppVariables.setUserConfigValue(baseName + "MultiplePoints", newv);
                    });
            multipleCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "MultiplePoints", true));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkLocateMethod() {
        if (isSettingValues) {
            return;
        }
        if (coordinateRadio.isSelected()) {
            FxmlControl.setTooltip(locateInput, message("InputCoordinateComments"));
            locateInput.setText("117.0983,36.25551");
            locateInput.setEditable(true);
            startButton.setDisable(false);
        } else if (addressRadio.isSelected()) {
            FxmlControl.setTooltip(locateInput, message("MapAddressComments"));
            locateInput.setText(AppVariables.isChinese() ? "拙政园"
                    : (mapOptionsController.mapName == ControlMapOptions.MapName.TianDiTu ? "Paris" : "巴黎"));
            locateInput.setEditable(true);
            startButton.setDisable(false);
        } else {
            FxmlControl.setTooltip(locateInput, message("PickCoordinateComments"));
            locateInput.setStyle(null);
            locateInput.setText("");
            locateInput.setEditable(false);
            startButton.setDisable(true);
        }
    }

    protected void setButtons() {
        boolean none = geographyCode == null;
        saveButton.setDisable(none);
        clearCodeButton.setDisable(none);
    }

    public void loadCoordinate(BaseController parent, double longitude, double latitude) {
        parentController = parent;
        clickMapRadio.fire();
        loading = true;
        getMyStage().setAlwaysOnTop(true);
        setCoordinate(longitude, latitude);
    }

    @Override
    protected void mapClicked(double longitude, double latitude) {
        if (!isSettingValues && clickMapRadio.isSelected()) {
            setCoordinate(longitude, latitude);
        }
    }

    @Override
    public void setCoordinate(double longitude, double latitude) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (!LocationTools.validCoordinate(longitude, latitude)) {
            loading = false;
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (timer == null || !mapOptionsController.mapLoaded) {
                        return;
                    }
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    locateInput.setText(longitude + "," + latitude);
                    startAction();
                });
            }
        }, 0, 100);
    }

    @FXML
    @Override
    public void startAction() {
        clearCodeAction();
        String value = locateInput.getText().trim();
        if (value.isBlank()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    GeographyCode code;
                    if (!addressRadio.isSelected()) {
                        try {
                            String[] values = value.split(",");
                            double longitude = DoubleTools.scale6(Double.valueOf(values[0].trim()));
                            double latitude = DoubleTools.scale6(Double.valueOf(values[1].trim()));
                            code = GeographyCodeTools.geoCode(mapOptionsController.coordinateSystem,
                                    longitude, latitude, true);
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                            return false;
                        }
                    } else {
                        code = GeographyCodeTools.geoCode(mapOptionsController.coordinateSystem, value);
                    }
                    if (code == null) {
                        return false;
                    }
                    geographyCode = code;
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (parentController != null && !loading) {
                        parentController.setGeographyCode(geographyCode);
                        parentController.getMyStage().requestFocus();
                        closeStage();
                        return;
                    }
                    loading = false;
                    dataArea.setText(BaseDataTools.displayData(geoTable, geographyCode, null, false));
                    setButtons();
                    try {
                        if (geographyCodes == null) {
                            geographyCodes = new ArrayList<>();
                        } else if (!multipleCheck.isSelected()) {
                            geographyCodes.clear();
                        }
                        geographyCodes.add((GeographyCode) geographyCode.clone());
                        drawPoints();
                    } catch (Exception e) {
                    }

                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    public void clearCodeAction() {
        geographyCode = null;
        dataArea.clear();
        setButtons();
    }

    @FXML
    @Override
    public void clearAction() {
        if (mapOptionsController.mapLoaded) {
            webEngine.executeScript("clearMap();");
        }
        geographyCodes.clear();
        clearCodeAction();
    }

    @FXML
    @Override
    public void saveAction() {
        if (geographyCode == null) {
            return;
        }
        GeographyCode code = GeographyCodeTools.encode(geographyCode);
        if (code != null) {
            geographyCode = code;
        } else {
            if (!TableGeographyCode.write(geographyCode)) {
                popFailed();
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVariables.message("Saved"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonEdit = new ButtonType(AppVariables.message("Edit"));
        ButtonType buttonClose = new ButtonType(AppVariables.message("Close"));
        alert.getButtonTypes().setAll(buttonEdit, buttonClose);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != buttonEdit) {
            return;
        }
        GeographyCodeEditController controller
                = (GeographyCodeEditController) openStage(CommonValues.GeographyCodeEditFxml);
        controller.setGeographyCode(geographyCode);
        controller.getMyStage().requestFocus();

    }

}
