package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Optional;
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
import javafx.stage.Stage;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.LocationTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationInMapController extends GeographyCodeMapController {

    protected GeographyCode geographyCode;
    protected boolean needQeury;

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
    protected Button queryButton, clearCodeButton;

    public LocationInMapController() {
        baseTitle = message("LocationInMap");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            setButtons();

            locateGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                        checkLocateMethod();
                    });
            checkLocateMethod();

            if (multipleCheck != null) {
                multipleCheck.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
                    UserConfig.setBoolean(baseName + "MultiplePoints", newv);
                });
                multipleCheck.setSelected(UserConfig.getBoolean(baseName + "MultiplePoints", true));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void checkLocateMethod() {
        if (isSettingValues) {
            return;
        }
        if (coordinateRadio.isSelected()) {
            NodeStyleTools.setTooltip(locateInput, Languages.message("InputCoordinateComments"));
            locateInput.setText("117.0983,36.25551");
            locateInput.setEditable(true);
            queryButton.setDisable(false);
        } else if (addressRadio.isSelected()) {
            NodeStyleTools.setTooltip(locateInput, Languages.message("MapAddressComments"));
            locateInput.setText(Languages.isChinese() ? "拙政园"
                    : (mapOptions.isGaoDeMap() ? "巴黎" : "Paris"));
            locateInput.setEditable(true);
            queryButton.setDisable(false);
        } else {
            NodeStyleTools.setTooltip(locateInput, Languages.message("PickCoordinateComments"));
            locateInput.setStyle(null);
            locateInput.setText("");
            locateInput.setEditable(false);
            queryButton.setDisable(true);
        }
    }

    protected void setButtons() {
        boolean none = geographyCode == null;
        saveButton.setDisable(none);
        clearCodeButton.setDisable(none);
    }

    @Override
    public void mapLoaded() {
        super.mapLoaded();
        if (needQeury) {
            needQeury = false;
            queryAction();
        }
    }

    public void loadCoordinate(double longitude, double latitude) {
        clickMapRadio.setSelected(true);
        setCoordinate(longitude, latitude);
    }

    @Override
    protected void mapClicked(double longitude, double latitude) {
        if (clickMapRadio.isSelected()) {
            setCoordinate(longitude, latitude);
        }
    }

    public void setCoordinate(double longitude, double latitude) {
        if (!LocationTools.validCoordinate(longitude, latitude)) {
            return;
        }
        locateInput.setText(longitude + "," + latitude);
        if (mapLoaded) {
            queryAction();
        } else {
            needQeury = true;
        }
    }

    @FXML
    public void queryAction() {
        if (!mapLoaded) {
            return;
        }
        clearCodeAction();
        String value = locateInput.getText().trim();
        if (value.isBlank()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                GeographyCode code;
                if (!addressRadio.isSelected()) {
                    try {
                        String[] values = value.split(",");
                        double longitude = DoubleTools.scale6(Double.parseDouble(values[0].trim()));
                        double latitude = DoubleTools.scale6(Double.parseDouble(values[1].trim()));
                        code = GeographyCodeTools.geoCode(mapOptions.getCoordinateSystem(),
                                longitude, latitude, true);
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                        return false;
                    }
                } else {
                    code = GeographyCodeTools.geoCode(mapOptions.getCoordinateSystem(), value);
                }
                if (code == null) {
                    return false;
                }
                geographyCode = code;
                return true;
            }

            @Override
            protected void whenSucceeded() {
                if (geographyCodes == null
                        || multipleCheck == null || !multipleCheck.isSelected()) {
                    geographyCodes = new ArrayList<>();
                }
                geographyCodes.add(geographyCode);
                drawPoints();
                dataArea.setText(geoTable.text(geographyCode));
                setButtons();
            }

        };
        start(task);
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
        clearMap();
        geographyCodes.clear();
        clearCodeAction();
    }

    @FXML
    @Override
    public void saveAction() {
        if (geographyCode == null) {
            return;
        }
        GeographyCode code = GeographyCodeTools.encode(null, geographyCode);
        if (code != null) {
            geographyCode = code;
        } else {
            if (!TableGeographyCode.write(geographyCode)) {
                popFailed();
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(Languages.message("Saved"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonEdit = new ButtonType(Languages.message("Edit"));
        ButtonType buttonClose = new ButtonType(Languages.message("Close"));
        alert.getButtonTypes().setAll(buttonEdit, buttonClose);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();

        Optional<ButtonType> result = alert.showAndWait();
        if (result == null || result.get() != buttonEdit) {
            return;
        }
        GeographyCodeEditController controller
                = (GeographyCodeEditController) openStage(Fxmls.GeographyCodeEditFxml);
        controller.setGeographyCode(geographyCode);
        controller.getMyStage().requestFocus();

    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public boolean keyESC() {
        close();
        return false;
    }

    public static LocationInMapController load(double longitude, double latitude) {
        try {
            LocationInMapController controller = (LocationInMapController) WindowTools.openStage(Fxmls.LocationInMapFxml);
            controller.loadCoordinate(longitude, latitude);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
