package mara.mybox.controller;

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
import mara.mybox.db.table.TableColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-09-04
 * @License Apache License Version 2.0
 */
public class ControlColorSet extends BaseController {

    protected TableColor tableColor;
    protected String thisName;
    protected Object data;
    protected Color defaultColor;

    @FXML
    protected Rectangle rect;
    @FXML
    protected Button colorButton;

    public ControlColorSet() {
        baseTitle = "ColorImport";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            tableColor = new TableColor();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public ControlColorSet init(BaseController parent, String name) {
        return init(parent, name, Color.TRANSPARENT);
    }

    public ControlColorSet init(BaseController parent, String name, Color defaultColor) {
        try {
            if (parent == null) {
                return this;
            }
            if (name == null) {
                name = parent.baseName + "Color";
            }
            parentController = parent;
            thisName = name;
            this.defaultColor = defaultColor;

            asSaved();

            rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> v, Paint ov, Paint nv) {
                    if (isSettingValues) {
                        return;
                    }
                    UserConfig.setString(thisName, ((Color) nv).toString());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return this;
    }

    public void hideRect() {
        thisPane.getChildren().remove(rect);
    }

    public void setColor(Color color) {
        rect.setFill(color);
        NodeStyleTools.setTooltip(rect, FxColorTools.colorNameDisplay(tableColor, color));
    }

    public void initColor(Color color) {
        isSettingValues = true;
        setColor(color);
        isSettingValues = false;
    }

    public Color color() {
        return (Color) rect.getFill();
    }

    public java.awt.Color awtColor() {
        return FxColorTools.toAwtColor(color());
    }

    public String rgb() {
        return FxColorTools.color2rgb(color());
    }

    public String rgba() {
        return FxColorTools.color2rgba(color());
    }

    public String css() {
        return FxColorTools.color2css(color());
    }

    public Color saved() {
        return Color.web(UserConfig.getString(thisName, FxColorTools.color2rgba(defaultColor)));
    }

    public void asSaved() {
        isSettingValues = true;
        setColor(saved());
        isSettingValues = false;
    }

    @FXML
    public void openColorPalette(ActionEvent event) {
        showColorPalette();
    }

    @FXML
    public void popColorPalette(MouseEvent event) {
        if (UserConfig.getBoolean("PopColorSetWhenMouseHovering", true)) {
            showColorPalette();
        }
    }

    public void showColorPalette() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    WindowTools.class.getResource(Fxmls.ColorPalettePopupFxml), AppVariables.currentBundle);
            Pane pane = fxmlLoader.load();
            ColorPalettePopupController controller = (ColorPalettePopupController) fxmlLoader.getController();
            controller.load(this, rect);

            popup = makePopup();
            popup.getContent().add(pane);
            LocateTools.locateCenter(colorButton, popup);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

}