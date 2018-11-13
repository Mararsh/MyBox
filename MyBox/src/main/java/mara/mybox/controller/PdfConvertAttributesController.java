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
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.fxml.FxmlTools;

/**
 * @Author Mara
 * @CreateDate 2018-6-15
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfConvertAttributesController extends ImageAttributesBaseController {

    private final String PdfConvertDensityKey, PdfConvertDensityInputKey;

    @FXML
    protected HBox pdfConvertAttributesPane;
    @FXML
    protected ToggleGroup DensityGroup;
    @FXML
    protected TextField densityInput;

    public PdfConvertAttributesController() {
        PdfConvertDensityKey = "PdfConvertDensityKey";
        PdfConvertDensityInputKey = "PdfConvertDensityInputKey";
    }

    @Override
    protected void initializeNext2() {

        DensityGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkDensity();
            }
        });
        FxmlTools.setRadioSelected(DensityGroup, AppVaribles.getConfigValue(PdfConvertDensityKey, "72dpi"));
        checkDensity();
        densityInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkDensity();
            }
        });
        densityInput.setText(AppVaribles.getConfigValue(PdfConvertDensityInputKey, null));
        FxmlTools.setNonnegativeValidation(densityInput);

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
                    AppVaribles.setConfigValue(PdfConvertDensityInputKey, inputValue + "");
                } else {
                    inputValue = -1;
                }
            } catch (Exception e) {
                inputValue = -1;
            }
            if (getMessage("InputValue").equals(s)) {
                if (inputValue > 0) {
                    attributes.setDensity(inputValue);
                    AppVaribles.setConfigValue(PdfConvertDensityKey, s);
                } else {
                    densityInput.setStyle(FxmlTools.badStyle);
                }

            } else {
                attributes.setDensity(Integer.parseInt(s.substring(0, s.length() - 3)));
                AppVaribles.setConfigValue(PdfConvertDensityKey, s);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public HBox getPdfConvertAttributesPane() {
        return pdfConvertAttributesPane;
    }

    public void setPdfConvertAttributesPane(HBox pdfConvertAttributesPane) {
        this.pdfConvertAttributesPane = pdfConvertAttributesPane;
    }

    public ToggleGroup getDensityGroup() {
        return DensityGroup;
    }

    public void setDensityGroup(ToggleGroup DensityGroup) {
        this.DensityGroup = DensityGroup;
    }

    public TextField getDensityInput() {
        return densityInput;
    }

    public void setDensityInput(TextField densityInput) {
        this.densityInput = densityInput;
    }

}
