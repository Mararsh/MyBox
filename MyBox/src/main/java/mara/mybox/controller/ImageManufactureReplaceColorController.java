package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageScope;
import mara.mybox.image.FxmlImageTools;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureReplaceColorController extends ImageManufactureController {

    protected int replaceColorScopeType, distance;
    protected ImageScope replaceColorScope;

    @FXML
    protected ToggleGroup replaceScopeGroup;
    @FXML
    protected ToolBar replaceColorBar;
    @FXML
    protected ColorPicker newColorPicker, scopeColorPicker;
    @FXML
    protected Button transForScopeButton, transForNewButton, replaceColorOkButton;
    @FXML
    protected RadioButton replaceColorSetting;
    @FXML
    protected HBox originalBox;
    @FXML
    private Label replaceColorLabel, distanceLabel;
    @FXML
    private TextField distanceInput;

    public static class ReplaceColorScopeType {

        public static int Color = 0;
        public static int Hue = 1;
        public static int Settings = 2;
    }

    public ImageManufactureReplaceColorController() {
    }

    @Override
    protected void initializeNext2() {
        try {
            initCommon();
            initReplaceColorTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();

            isSettingValues = true;
            if (CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                transForScopeButton.setDisable(true);
                transForNewButton.setDisable(true);

            } else {
                transForScopeButton.setDisable(false);
                transForNewButton.setDisable(false);
            }

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initReplaceColorTab() {
        try {

            Tooltip tips = new Tooltip(getMessage("ClickForReplaceColor"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(replaceColorBar, tips);

            replaceColorScope = new ImageScope();
            replaceColorScope.setOperationType(ImageScope.OperationType.ReplaceColor);
            replaceColorScope.setAllColors(false);
            replaceColorScope.setAreaScopeType(ImageScope.AreaScopeType.AllArea);

            replaceScopeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkReplaceColorScope();
                }
            });

            scopeColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> ov,
                        Color oldValue, Color newValue) {
                    setScopeColor(newValue);
                }
            });
            checkReplaceColorScope();

            distanceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkDistance();
                }
            });
            distanceInput.setText("20");

            replaceColorOkButton.disableProperty().bind(
                    distanceInput.styleProperty().isEqualTo(badStyle)
                            .or(Bindings.isEmpty(distanceInput.textProperty()))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkReplaceColorScope() {
        try {
            RadioButton selected = (RadioButton) replaceScopeGroup.getSelectedToggle();
            if (AppVaribles.getMessage("Hue").equals(selected.getText())) {
                replaceColorScopeType = ReplaceColorScopeType.Hue;
                setScopeColor(scopeColorPicker.getValue());
                originalBox.setDisable(false);
                replaceColorLabel.setVisible(true);
                distanceLabel.setText(getMessage("HueDistance"));
                showScopeCheck.setDisable(true);
                scopePaneValid = false;
                super.setScopePane();

            } else if (AppVaribles.getMessage("Settings").equals(selected.getText())) {
                originalBox.setDisable(true);
                replaceColorLabel.setVisible(false);
                distanceLabel.setText("");

            } else if (AppVaribles.getMessage("Color").equals(selected.getText())) {
                replaceColorScopeType = ReplaceColorScopeType.Color;
                setScopeColor(scopeColorPicker.getValue());
                originalBox.setDisable(false);
                replaceColorLabel.setVisible(true);
                distanceLabel.setText(getMessage("ColorDistance"));
                showScopeCheck.setDisable(true);
                scopePaneValid = false;
                super.setScopePane();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkDistance() {
        try {
            distance = Integer.valueOf(distanceInput.getText());
            distanceInput.setStyle(null);
            if (distance >= 0 && distance <= 255) {
                distanceInput.setStyle(null);
                replaceColorScope.setColorDistance(distance);
                replaceColorScope.setHueDistance(distance);
            } else {
                distanceInput.setStyle(badStyle);
                distance = 0;
            }
        } catch (Exception e) {
            distanceInput.setStyle(badStyle);
            distance = 0;
        }
    }

    private void setScopeColor(Color color) {
        try {
            if (replaceColorScopeType == ReplaceColorScopeType.Settings) {
                return;
            }
            replaceColorScope.setAllColors(false);
            replaceColorScope.setAreaScopeType(ImageScope.AreaScopeType.AllArea);
            if (replaceColorScopeType == ReplaceColorScopeType.Color) {
                replaceColorScope.setMatchColor(true);
            } else if (replaceColorScopeType == ReplaceColorScopeType.Hue) {
                replaceColorScope.setMatchColor(false);
            } else {
                return;
            }
            replaceColorScope.setColorExcluded(false);
            List<Color> colors = new ArrayList<>();
            colors.add(color);
            replaceColorScope.setColors(colors);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void setScopeForReplaceColor() {
        replaceColorScopeType = ReplaceColorScopeType.Settings;
        setScope(replaceColorScope);
    }

    @Override
    protected void setScopePane() {
        try {
            showScopeCheck.setDisable(false);
            values.setCurrentScope(replaceColorScope);
            scopePaneValid = (replaceColorScopeType == ReplaceColorScopeType.Settings);
            super.setScopePane();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void scopeDetermined(ImageScope imageScope) {
        values.setCurrentScope(imageScope);
        replaceColorScope = imageScope;
        setScopePane();
    }

    @FXML
    public void transparentForNew() {
        newColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void whiteForNew() {
        newColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void blackForNew() {
        newColorPicker.setValue(Color.BLACK);
    }

    @FXML
    public void transparentForScope() {
        scopeColorPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    public void whiteForScope() {
        scopeColorPicker.setValue(Color.WHITE);
    }

    @FXML
    public void blackForScope() {
        scopeColorPicker.setValue(Color.BLACK);
    }

    @FXML
    @Override
    public void clickImage(MouseEvent event) {
        if (values.getCurrentImage() == null) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }
        imageView.setCursor(Cursor.HAND);

        int x = (int) Math.round(event.getX() * values.getCurrentImage().getWidth() / imageView.getBoundsInLocal().getWidth());
        int y = (int) Math.round(event.getY() * values.getCurrentImage().getHeight() / imageView.getBoundsInLocal().getHeight());
        PixelReader pixelReader = values.getCurrentImage().getPixelReader();
        Color color = pixelReader.getColor(x, y);

        if (event.getButton() == MouseButton.PRIMARY) {
            scopeColorPicker.setValue(color);

        } else if (event.getButton() == MouseButton.SECONDARY) {
            newColorPicker.setValue(color);
        }

    }

    @FXML
    public void replaceColorAction() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage = FxmlImageTools.replaceColors(values.getCurrentImage(), newColorPicker.getValue(), replaceColorScope);
                recordImageHistory(ImageOperationType.Replace_Color, newImage);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        values.setUndoImage(values.getCurrentImage());
                        values.setCurrentImage(newImage);
                        imageView.setImage(newImage);
                        setImageChanged(true);
                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
