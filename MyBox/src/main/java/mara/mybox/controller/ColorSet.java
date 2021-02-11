package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.table.TableColorData;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-09-04
 * @License Apache License Version 2.0
 */
public class ColorSet extends BaseController {

    protected String thisName;
    protected Object data;
    protected Color defaultColor;

    @FXML
    protected Rectangle rect;
    @FXML
    protected Button colorButton;

    public ColorSet() {
        baseTitle = "ColorImport";
    }

    public void init(BaseController parent, String name) {
        init(parent, name, Color.TRANSPARENT);
    }

    public void init(BaseController parent, String name, Color defaultColor) {
        try {
            if (parent == null) {
                return;
            }
            if (name == null) {
                name = parent.baseName + "Color";
            }
            parentController = parent;
            thisName = name;
            this.defaultColor = defaultColor;

            resetRect();

            rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable, Paint oldValue, Paint newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.setUserConfigValue(thisName, ((Color) newValue).toString());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void hideRect() {
        thisPane.getChildren().remove(rect);
    }

    public void setColor(Color color) {
        rect.setFill(color);
    }

    public Color color() {
        return (Color) rect.getFill();
    }

    public String rgb() {
        return FxmlColor.color2rgb(color());
    }

    public String rgba() {
        return FxmlColor.color2rgba(color());
    }

    public void resetRect() {
        Color color = Color.web(AppVariables.getUserConfigValue(thisName, FxmlColor.color2rgba(defaultColor)));
        rect.setFill(color);
        FxmlControl.setTooltip(rect, FxmlColor.colorNameDisplay(color));
    }

    @FXML
    public void openColorPalette(ActionEvent event) {
        showColorPalette();
    }

    @FXML
    public void popColorPalette(MouseEvent event) {
        if (AppVariables.getUserConfigBoolean("PopColorSet", true)) {
            showColorPalette();
        }
    }

    public void showColorPalette() {
        synchronized (this) {
            if ((task != null && !task.isQuit())
                    || (popup != null && popup.isShowing())) {
                return;
            }

            task = new SingletonTask<Void>() {

                protected List<ColorData> colors;

                @Override
                protected boolean handle() {
                    colors = TableColorData.readPalette();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    load(colors);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    private void load(List<ColorData> colors) {
        try {
            popup = getPopup();

            FXMLLoader fxmlLoader = new FXMLLoader(
                    FxmlStage.class.getResource(CommonValues.ColorPalettePopupFxml), AppVariables.currentBundle);
            Pane pane = fxmlLoader.load();
            ColorPalettePopupController controller = (ColorPalettePopupController) fxmlLoader.getController();
            controller.load(this, colors);
            popup.getContent().add(pane);

            FxmlControl.locateCenter(colorButton, popup);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

}
