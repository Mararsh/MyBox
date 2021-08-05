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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.bufferedimage.AlphaTools;
import mara.mybox.bufferedimage.ImageAttributes;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAlphaAddBatchController extends BaseImageManufactureBatchController {

    private float opacityValue;
    private boolean useOpacityValue;
    private BufferedImage alphaImage;
    private AlphaBlendMode blendMode;

    public static enum AlphaBlendMode {
        Set, KeepOriginal, Plus
    }

    @FXML
    protected ToggleGroup alphaGroup, alphaAddGroup;
    @FXML
    protected HBox alphaFileBox;
    @FXML
    protected RadioButton opacityRadio, tifRadio;
    @FXML
    protected ComboBox<String> opacityBox;

    public ImageAlphaAddBatchController() {
        baseTitle = Languages.message("ImageAlphaAdd");

        sourceExtensionFilter = FileFilters.AlphaImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                    .or(sourceFileInput.styleProperty().isEqualTo(NodeStyleTools.badStyle))
                    .or(opacityBox.getEditor().styleProperty().isEqualTo(NodeStyleTools.badStyle))
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
        ValidationTools.setEditorNormal(opacityBox);

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
                ValidationTools.setEditorNormal(opacityBox);
            } else {
                ValidationTools.setEditorBadStyle(opacityBox);
            }
        } catch (Exception e) {
            ValidationTools.setEditorBadStyle(opacityBox);
        }
    }

    private void checkOpacityAdd() {
        String selected = ((RadioButton) alphaAddGroup.getSelectedToggle()).getText();
        if (Languages.message("Plus").equals(selected)) {
            blendMode = AlphaBlendMode.Plus;
        } else if (Languages.message("Keep").equals(selected)) {
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
        if (tifRadio.isSelected()) {
            targetFileSuffix = "tif";
        } else {
            targetFileSuffix = "png";
        }
        attributes = new ImageAttributes(targetFileSuffix);
        return true;
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            if (source.getColorModel().hasAlpha() && blendMode == AlphaBlendMode.KeepOriginal) {
                errorString = Languages.message("NeedNotHandle");
                return null;
            }
            BufferedImage target;
            if (useOpacityValue) {
                target = AlphaTools.addAlpha(source, opacityValue, blendMode == AlphaBlendMode.Plus);
            } else {
                target = AlphaTools.addAlpha(source, alphaImage, blendMode == AlphaBlendMode.Plus);
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }

    }

}
