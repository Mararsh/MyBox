/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import mara.mybox.controller.base.ImageAttributesBaseController;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.fxml.FxmlControl;

/**
 * @Author Mara
 * @CreateDate 2018-6-15
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfConvertAttributesController extends ImageAttributesBaseController {

    private final String PdfConvertDensityKey, PdfConvertDensityInputKey;

    @FXML
    public HBox pdfConvertAttributesPane;
    @FXML
    public ToggleGroup DensityGroup;
    @FXML
    public TextField densityInput;
    @FXML
    public Pane pdfConvertAttributes;

    public PdfConvertAttributesController() {
        PdfConvertDensityKey = "PdfConvertDensityKey";
        PdfConvertDensityInputKey = "PdfConvertDensityInputKey";
    }

    @Override
    public void initializeNext2() {

        DensityGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkDensity();
            }
        });
        FxmlControl.setRadioSelected(DensityGroup, AppVaribles.getUserConfigValue(PdfConvertDensityKey, "72dpi"));
        checkDensity();
        densityInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkDensity();
            }
        });
        densityInput.setText(AppVaribles.getUserConfigValue(PdfConvertDensityInputKey, null));
        FxmlControl.setNonnegativeValidation(densityInput);

    }

    private void checkDensity() {
        try {
            RadioButton selected = (RadioButton) DensityGroup.getSelectedToggle();
            String s = selected.getText();
            densityInput.setStyle(null);
            int inputValue;
            try {
                inputValue = Integer.parseInt(densityInput.getText());
                if (inputValue > 0) {
                    AppVaribles.setUserConfigValue(PdfConvertDensityInputKey, inputValue + "");
                } else {
                    inputValue = -1;
                }
            } catch (Exception e) {
                inputValue = -1;
            }
            if (getMessage("InputValue").equals(s)) {
                if (inputValue > 0) {
                    imageAttributes.setDensity(inputValue);
                    AppVaribles.setUserConfigValue(PdfConvertDensityKey, s);
                } else {
                    densityInput.setStyle(FxmlControl.badStyle);
                }

            } else {
                imageAttributes.setDensity(Integer.parseInt(s.substring(0, s.length() - 3)));
                AppVaribles.setUserConfigValue(PdfConvertDensityKey, s);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

}
