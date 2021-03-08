package mara.mybox.controller;

import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mara.mybox.db.data.ColorData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.getUserConfigBoolean;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-09-04
 * @License Apache License Version 2.0
 */
public class ColorPalettePopupController extends BaseController {

    protected ColorSet setController;
    protected Rectangle clickedRect, enteredRect;
    protected DropShadow shadowEffect;
    protected double rectSize;

    @FXML
    protected HBox barBox;
    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected FlowPane colorsPane;
    @FXML
    protected Label label;
    @FXML
    protected Button closeButton;
    @FXML
    protected CheckBox popColorSetCheck;

    public ColorPalettePopupController() {
        baseTitle = AppVariables.message("ColorPalette");
        rectSize = AppVariables.iconSize * 0.8;
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        setController.keyEventsHandler(event);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            popColorSetCheck.setSelected(getUserConfigBoolean("PopColorSetWhenMousePassing", true));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    protected void popColorSet() {
        AppVariables.setUserConfigValue("PopColorSetWhenMousePassing", popColorSetCheck.isSelected());
    }

    public void load(ColorSet parent, List<ColorData> colors) {
        try {
            thisPane.setStyle(" -fx-background-color: white;");
            FxmlControl.refreshStyle(thisPane);
            FxmlControl.setTooltip(closeButton, message("PopupClose"));

            this.setController = parent;
            shadowEffect = new DropShadow();
            isSettingValues = true;
            colorsPane.getChildren().clear();
            for (ColorData data : colors) {
                try {
                    addColor(data, false);
                } catch (Exception e) {
                    MyBoxLog.error(e.toString());
                }
            }
            label.setText(message("Count") + ": " + colorsPane.getChildren().size());
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected Rectangle addColor(ColorData data, boolean ahead) {
        try {
            if (data == null) {
                return null;
            }
            Rectangle rect = new Rectangle(rectSize, rectSize);
            rect.setUserData(data);
            FxmlControl.setTooltip(rect, new Tooltip(data.display()));
            Color color = data.getColor();
            rect.setFill(color);
            rect.setStroke(Color.BLACK);
            rect.setOnMouseClicked((MouseEvent event) -> {
                Platform.runLater(() -> {
                    if (isSettingValues || setController == null || setController.rect == null) {
                        return;
                    }
                    try {
                        setController.rect.setFill(color);
                        setController.rect.setUserData(data);
                        FxmlControl.setTooltip(setController.rect, data.display());
                        setController.hidePopup();
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }

                });
            });
            rect.setOnMouseEntered((MouseEvent event) -> {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Rectangle rect = (Rectangle) event.getSource();
                        if (isSettingValues || rect.equals(enteredRect) || rect.equals(clickedRect)) {
                            return;
                        }
                        isSettingValues = true;
                        if (enteredRect != null && !enteredRect.equals(clickedRect)) {
                            enteredRect.setEffect(null);
                            enteredRect.setWidth(rectSize);
                            enteredRect.setHeight(rectSize);
                        }
                        rect.setEffect(shadowEffect);
                        rect.setWidth(rectSize * 1.4);
                        rect.setHeight(rectSize * 1.4);
                        enteredRect = rect;
                        isSettingValues = false;
                    }
                });
            });

            if (ahead) {
                colorsPane.getChildren().add(0, rect);
            } else {
                colorsPane.getChildren().add(rect);
            }
            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    public void managePalette() {
        ColorPaletteManageController.oneOpen();
    }

    @FXML
    public void dataAction() {
        ColorsManageController.oneOpen();
    }

    @FXML
    @Override
    public void closeAction() {
        setController.hidePopup();
    }

    @FXML
    public void exitPane() {
        if (enteredRect != null && !enteredRect.equals(clickedRect)) {
            enteredRect.setEffect(null);
            enteredRect.setWidth(rectSize);
            enteredRect.setHeight(rectSize);
            enteredRect = null;
        }
    }

}
