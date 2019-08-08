package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.color.Illuminant;
import mara.mybox.color.Illuminant.IlluminantType;
import mara.mybox.color.Illuminant.Observer;
import mara.mybox.tools.DoubleTools;
import static mara.mybox.value.AppVaribles.message;
import static mara.mybox.value.AppVaribles.message;

/**
 * @Author Mara
 * @CreateDate 2019-6-2
 * @License Apache License Version 2.0
 */
public class WhitePointController extends XYZController {

    @FXML
    private ComboBox<String> wpSelector;
    @FXML
    private ToggleGroup sourceGroup;
    @FXML
    private RadioButton standardRadio, inputRadio;

    public WhitePointController() {
    }

    @Override
    public void initializeNext() {
        try {

            sourceGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkValues();
                }
            });

            List<String> names = Illuminant.names();
            wpSelector.getItems().addAll(names);
            wpSelector.setVisibleRowCount(15);
            wpSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov, String oldv, String newv) {
                    checkSourceIlluminant();
                }
            });

            super.initializeNext();

        } catch (Exception e) {

        }
    }

    @Override
    public void checkValues() {
        try {
            if (valueType == ValueType.Tristimulus) {
                standardRadio.setDisable(true);
                wpSelector.setDisable(true);
                inputRadio.setSelected(true);
            } else {
                wpSelector.setDisable(false);
                standardRadio.setDisable(false);
            }
            RadioButton selected = (RadioButton) sourceGroup.getSelectedToggle();
            if (message("StandardIlluminant").equals(selected.getText())) {
                xInput.setDisable(true);
                yInput.setDisable(true);
                zInput.setDisable(true);
                checkSourceIlluminant();
            } else if (message("Input").equals(selected.getText())) {
                xInput.setDisable(false);
                yInput.setDisable(valueType == ValueType.Relative);
                zInput.setDisable(valueType == ValueType.Normalized);
                checkInputs();
            }
        } catch (Exception e) {
            checkInputs();
        }
    }

    private void checkSourceIlluminant() {
        String from = wpSelector.getSelectionModel().getSelectedItem();
        if (from == null) {
            return;
        }
        IlluminantType fromType = Illuminant.type(from.substring(0, from.length() - 10));
        Observer fromObserver = from.endsWith("1931") ? Observer.CIE1931 : Observer.CIE1964;
        Illuminant illuminant = new Illuminant(fromType, fromObserver);
        isSettingValues = true;
        if (null != valueType) {
            switch (valueType) {
                case Normalized:
                    xInput.setText(DoubleTools.scale(illuminant.normalizedX, scale) + "");
                    yInput.setText(DoubleTools.scale(illuminant.normalizedY, scale) + "");
                    zInput.setText(DoubleTools.scale(illuminant.normalizedZ, scale) + "");
                    break;
                case Relative:
                    xInput.setText(DoubleTools.scale(illuminant.relativeX, scale) + "");
                    yInput.setText(DoubleTools.scale(illuminant.relativeY, scale) + "");
                    zInput.setText(DoubleTools.scale(illuminant.relativeZ, scale) + "");
                    break;
                case Tristimulus:
                    xInput.setText(DoubleTools.scale(illuminant.X, scale) + "");
                    yInput.setText(DoubleTools.scale(illuminant.Y, scale) + "");
                    zInput.setText(DoubleTools.scale(illuminant.Z, scale) + "");
                    break;
                default:
                    break;
            }
        }
        isSettingValues = false;
        checkInputs();
    }

}
