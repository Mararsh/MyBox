package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAlphaAddBatchController extends ImageManufactureBatchController {

    private float opacityValue;
    private boolean useOpacityValue;
    private BufferedImage alphaImage;
    private AlphaBlendMode blendMode;

    public static enum AlphaBlendMode {
        Set, KeepOriginal, Plus
    }

    @FXML
    private ToggleGroup alphaGroup, alphaAddGroup;
    @FXML
    private HBox alphaFileBox;
    @FXML
    private RadioButton opacityRadio;
    @FXML
    private ComboBox<String> opacityBox;

    public ImageAlphaAddBatchController() {
        baseTitle = AppVariables.message("ImageAlphaAdd");

        sourceExtensionFilter = CommonFxValues.AlphaImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
                    .or(opacityBox.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();

            alphaGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkOpacityType();
                }
            });
            checkOpacityType();

            opacityBox.getItems().addAll(Arrays.asList("50", "10", "60", "80", "100", "90", "20", "30"));
            opacityBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkOpacity();
                }
            });
            opacityBox.getSelectionModel().select(0);

            alphaAddGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkOpacityAdd();
                }
            });
            checkOpacityAdd();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void checkOpacityType() {
        alphaFileBox.setDisable(true);
        sourceFileInput.setStyle(null);
        opacityBox.setDisable(true);
        FxmlControl.setEditorNormal(opacityBox);

        useOpacityValue = opacityRadio.isSelected();
        if (useOpacityValue) {
            opacityBox.setDisable(false);
            checkOpacity();

        } else {
            alphaFileBox.setDisable(false);
            checkSourceFileInput();

        }
    }

    private void checkOpacity() {
        try {
            int v = Integer.valueOf(opacityBox.getValue());
            if (v >= 0 && v <= 100) {
                opacityValue = v / 100f;
                FxmlControl.setEditorNormal(opacityBox);
            } else {
                FxmlControl.setEditorBadStyle(opacityBox);
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(opacityBox);
        }
    }

    private void checkOpacityAdd() {
        String selected = ((RadioButton) alphaAddGroup.getSelectedToggle()).getText();
        if (message("Plus").equals(selected)) {
            blendMode = AlphaBlendMode.Plus;
        } else if (message("Keep").equals(selected)) {
            blendMode = AlphaBlendMode.KeepOriginal;
        } else {
            blendMode = AlphaBlendMode.Set;
        }
    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }
        if (!useOpacityValue) {
            alphaImage = ImageFileReaders.readImage(sourceFile);
        }
        return true;
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            if (source.getColorModel().hasAlpha() && blendMode == AlphaBlendMode.KeepOriginal) {
                errorString = message("NeedNotHandle");
                return null;
            }
            BufferedImage target;
            if (useOpacityValue) {
                target = ImageManufacture.addAlpha(source, opacityValue, blendMode == AlphaBlendMode.Plus);
            } else {
                target = ImageManufacture.addAlpha(source, alphaImage, blendMode == AlphaBlendMode.Plus);
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

}
