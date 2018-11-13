package mara.mybox.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.CommonValues;
import mara.mybox.fxml.FxmlReplaceColorTools;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.ImageScope;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureReplaceColorController extends ImageManufactureController {

    @FXML
    protected ToolBar replaceColorBar;
    @FXML
    protected ColorPicker newColorPicker;
    @FXML
    protected Button transForNewButton, okButton;

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
            values.getScope().setOperationType(ImageScope.OperationType.ReplaceColor);

            isSettingValues = true;
            if (CommonValues.NoAlphaImages.contains(values.getImageInfo().getImageFormat())) {
                transForNewButton.setDisable(true);

            } else {
                transForNewButton.setDisable(false);
            }

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initReplaceColorTab() {
        try {
            scopeColorString = getMessage("ReplaceColorClickForColor");
            scopeAllString = getMessage("ColorLabel");
            promptLabel.setText(scopeAllString);

            Tooltip tips = new Tooltip(getMessage("CTRL+a"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(okButton, tips);

            okButton.disableProperty().bind(
                    scopeLeftXInput.styleProperty().isEqualTo(badStyle)
                            .or(scopeLeftYInput.styleProperty().isEqualTo(badStyle))
                            .or(scopeRightXInput.styleProperty().isEqualTo(badStyle))
                            .or(scopeRightYInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
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

    @Override
    public void clickImageForAll(MouseEvent event, Color color) {
        newColorPicker.setValue(color);
    }

    @Override
    public void clickImageForColor(MouseEvent event, Color color) {
        if (event.getButton() == MouseButton.PRIMARY) {
            scope.addColor(color);
            indicateColor();

        } else if (event.getButton() == MouseButton.SECONDARY) {
            newColorPicker.setValue(color);
        }
    }

    @Override
    protected void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        String key = event.getText();
        if (key == null || key.isEmpty()) {
            return;
        }
        if (event.isControlDown()) {
            switch (key) {
                case "a":
                case "A":
                    replaceColorAction();
                    break;
            }
        }
    }

    @FXML
    public void replaceColorAction() {
        if (scope == null || (task != null && task.isRunning())) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                final Image newImage;
                switch (scope.getScopeType()) {
                    case Matting:
                        newImage = FxmlReplaceColorTools.replaceColorsMatting(values.getCurrentImage(),
                                newColorPicker.getValue(), scope.getPoints(), scope.getColorDistance());
                        break;
                    case Rectangle:
                        newImage = FxmlReplaceColorTools.replaceColorsRectangle(values.getCurrentImage(),
                                newColorPicker.getValue(), scope.getRectangle());
                        break;
                    case Circle:
                        newImage = FxmlReplaceColorTools.replaceColorsCircle(values.getCurrentImage(),
                                newColorPicker.getValue(), scope.getCircle());
                        break;
                    default:
                        newImage = FxmlReplaceColorTools.replaceColors(values.getCurrentImage(),
                                newColorPicker.getValue(), scope);
                        break;
                }
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
