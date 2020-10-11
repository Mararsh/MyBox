package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import mara.mybox.data.ColorData;
import mara.mybox.db.TableColorData;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-09-04
 * @License Apache License Version 2.0
 */
public class ColorSetController extends BaseController {

    protected String thisName;
    protected Object data;
    protected Color defaultColor;

    @FXML
    protected Rectangle rect;
    @FXML
    protected Button colorButton;

    public ColorSetController() {
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
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    AppVariables.setUserConfigValue(thisName, ((Color) newValue).toString());
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
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
    public void popColorPalette(MouseEvent event) {
        synchronized (this) {
            if (task != null || (popup != null && popup.isShowing())) {
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
                    load(event, colors);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    public void load(MouseEvent event, List<ColorData> colors) {
        try {
            popup = getPopup();

            FXMLLoader fxmlLoader = new FXMLLoader(
                    FxmlStage.class.getResource(CommonValues.ColorPalettePopupFxml), AppVariables.currentBundle);
            Pane pane = fxmlLoader.load();
            ColorPalettePopupController controller = (ColorPalettePopupController) fxmlLoader.getController();
            controller.load(this, colors);
            popup.getContent().add(pane);

            popup.show(colorButton, event.getSceneX(), event.getSceneY());
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

}
