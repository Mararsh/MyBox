package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.fxml.FxmlImageTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.image.ImageMarginsTools;

/**
 * @Author Mara
 * @CreateDate 2018-9-26
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchAddMarginsController extends ImageManufactureBatchController {

    private int addMarginWidth;

    @FXML
    private ComboBox<String> addMarginBox;
    @FXML
    private ColorPicker addMarginsColorPicker;
    @FXML
    private Button transForAddMarginsButton;
    @FXML
    private CheckBox addMarginsTopCheck, addMarginsBottomCheck, addMarginsLeftCheck, addMarginsRightCheck;

    public ImageManufactureBatchAddMarginsController() {

    }

    @Override
    protected void initializeNext2() {
        try {

            operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(sourceFilesInformation))
                    .or(addMarginBox.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(addMarginsTopCheck.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    protected void initOptionsSection() {
        try {
            super.initOptionsSection();

            addMarginBox.getItems().addAll(Arrays.asList("5", "10", "2", "15", "20", "30", "1"));
            addMarginBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        addMarginWidth = Integer.valueOf(newValue);
                        if (addMarginWidth > 0) {
                            addMarginBox.getEditor().setStyle(null);
                        } else {
                            addMarginWidth = 0;
                            addMarginBox.getEditor().setStyle(badStyle);
                        }

                    } catch (Exception e) {
                        addMarginWidth = 0;
                        addMarginBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            addMarginBox.getSelectionModel().select(0);

            addMarginsTopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkMargins();
                }
            });

            addMarginsBottomCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkMargins();
                }
            });

            addMarginsLeftCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkMargins();
                }
            });

            addMarginsRightCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkMargins();
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private boolean checkMargins() {
        if (!addMarginsTopCheck.isSelected()
                && !addMarginsBottomCheck.isSelected()
                && !addMarginsLeftCheck.isSelected()
                && !addMarginsRightCheck.isSelected()) {
            addMarginsTopCheck.setStyle(badStyle);
            popError(AppVaribles.getMessage("NothingHandled"));
            return false;
        } else {
            addMarginsTopCheck.setStyle(null);
            return true;
        }

    }

    @FXML
    public void addMarginsTransparentAction() {
        addMarginsColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void addMarginsBlackAction() {
        addMarginsColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void addMarginsWhiteAction() {
        addMarginsColorPicker.setValue(Color.WHITE);
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            if (!checkMargins()) {
                return null;
            }
            BufferedImage target = ImageMarginsTools.addMargins(source,
                    FxmlImageTools.colorConvert(addMarginsColorPicker.getValue()), addMarginWidth,
                    addMarginsTopCheck.isSelected(), addMarginsBottomCheck.isSelected(),
                    addMarginsLeftCheck.isSelected(), addMarginsRightCheck.isSelected());

            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }
}
