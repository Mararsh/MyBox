package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
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
import javafx.scene.layout.HBox;
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
public class ControlCoordinatePicker extends BaseMapController {

    protected List<GeographyCode> geographyCodes;
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
    protected HBox queryBox;
    @FXML
    protected Button queryButton, clearCodeButton;
    @FXML
    protected CheckBox wrapCheck;

    public void setParameter(boolean multiple) {
        try {

            setButtons();

            locateGroup.selectedToggleProperty().addListener(
                    (ObservableValue<? extends Toggle> ov, Toggle oldv, Toggle newv) -> {
                        checkLocateMethod();
                    });
            checkLocateMethod();

            multipleCheck.setVisible(multiple);
            if (multiple) {
                multipleCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
                            UserConfig.setBoolean(baseName + "MultiplePoints", newv);
                        });
                multipleCheck.setSelected(UserConfig.getBoolean(baseName + "MultiplePoints", true));
            }

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "Wrap", false));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    UserConfig.setBoolean(baseName + "Wrap", wrapCheck.isSelected());
                    dataArea.setWrapText(wrapCheck.isSelected());
                }
            });
            dataArea.setWrapText(wrapCheck.isSelected());

            initMap();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void setButtons() {
        boolean none = geographyCode == null;
        saveButton.setDisable(none);
        clearCodeButton.setDisable(none);
    }

    protected void checkLocateMethod() {
        if (isSettingValues) {
            return;
        }
        if (coordinateRadio.isSelected()) {
            NodeStyleTools.setTooltip(locateInput, message("InputCoordinateComments"));
            queryBox.setVisible(true);
            locateInput.setText("117.0983,36.25551");
        } else if (addressRadio.isSelected()) {
            NodeStyleTools.setTooltip(locateInput, message("MapAddressComments"));
            queryBox.setVisible(true);
            locateInput.setText(Languages.isChinese() ? "苏州"
                    : (isGaoDeMap() ? "巴黎" : "Paris"));
        } else {
            NodeStyleTools.setTooltip(locateInput, message("PickCoordinateComments"));
            queryBox.setVisible(false);
        }
    }

    public void loadCoordinate(double longitude, double latitude) {
        isSettingValues = true;
        clickMapRadio.setSelected(true);
        isSettingValues = false;
        showCoordinate(longitude, latitude);
    }

    @Override
    public void mapLoaded() {
        super.mapLoaded();
        if (needQeury) {
            needQeury = false;
            queryAction();
        }
    }

    @Override
    protected void mouseClick(double longitude, double latitude) {
        if (clickMapRadio.isSelected()) {
            showCoordinate(longitude, latitude);
        }
    }

    public void showCoordinate(double longitude, double latitude) {
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
                        code = GeographyCodeTools.geoCode(coordinateSystem, longitude, latitude, true);
                    } catch (Exception e) {
                        MyBoxLog.error(e);
                        return false;
                    }
                } else {
                    code = GeographyCodeTools.geoCode(coordinateSystem, value);
                }
                if (code == null) {
                    return false;
                }
                geographyCode = code;
                return geographyCode != null;
            }

            @Override
            protected void whenSucceeded() {
                if (geographyCodes == null
                        || multipleCheck == null || !multipleCheck.isSelected()) {
                    geographyCodes = new ArrayList<>();
                }
                geographyCodes.add(geographyCode);
                drawCodes(geographyCodes, 1);
                dataArea.setText(nodeTable.text(geographyCode));
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
        clearCodes();
        geographyCodes = new ArrayList<>();
        clearCodeAction();
    }

    @FXML
    @Override
    public void saveAction() {  // ###############
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
        alert.setContentText(message("Saved"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        ButtonType buttonEdit = new ButtonType(message("Edit"));
        ButtonType buttonClose = new ButtonType(message("Close"));
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

    public static ControlCoordinatePicker load(double longitude, double latitude) {
        try {
            ControlCoordinatePicker controller = (ControlCoordinatePicker) WindowTools.openStage(Fxmls.LocationInMapFxml);
            controller.loadCoordinate(longitude, latitude);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
