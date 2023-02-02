package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.PaletteTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-09-04
 * @License Apache License Version 2.0
 */
public class ColorPalettePopupController extends BaseChildController {

    protected Rectangle parentRect;
    protected TableColorPaletteName tableColorPaletteName;
    protected TableColorPalette tableColorPalette;
    protected TableColor tableColor;
    protected List<ColorPaletteName> palettes;
    protected ColorPaletteName currentPalette;
    protected Rectangle clickedRect, enteredRect;
    protected DropShadow shadowEffect;
    protected double rectSize;
    protected SimpleBooleanProperty setNotify;

    @FXML
    protected HBox barBox;
    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected FlowPane colorsPane;
    @FXML
    protected Label label;
    @FXML
    protected Button paletteButton;

    public ColorPalettePopupController() {
        baseTitle = message("ColorPalette");
        setNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (parentController != null) {
                return parentController.keyEventsFilter(event);
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableColorPaletteName = new TableColorPaletteName();
            tableColorPalette = new TableColorPalette();
            tableColor = new TableColor();
            tableColorPalette.setTableColor(tableColor);
            rectSize = AppVariables.iconSize * 0.8;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(cancelButton, message("PopupClose"));
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    public void load(BaseController parent, Rectangle rect) {
        try {
            thisPane.setStyle(" -fx-background-color: white;");
            refreshStyle(thisPane);

            this.parentController = parent;
            parentRect = rect;
            shadowEffect = new DropShadow();
            loadColors();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadColors() {
        synchronized (this) {
            if ((task != null && !task.isQuit())) {
                return;
            }
            thisPane.setDisable(true);
            task = new SingletonTask<Void>(this) {

                protected List<ColorData> colors;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        ColorPaletteName defaultPalette = tableColorPaletteName.defaultPalette(conn);
                        if (defaultPalette == null) {
                            return false;
                        }
                        String paletteName = UserConfig.getString("ColorPalettePopupPalette", defaultPalette.getName());
                        currentPalette = tableColorPaletteName.find(conn, paletteName);
                        if (currentPalette == null) {
                            currentPalette = defaultPalette;
                        }
                        if (currentPalette == null) {
                            return false;
                        }
                        paletteName = currentPalette.getName();
                        UserConfig.setString("ColorPalettePopupPalette", paletteName);
                        colors = tableColorPalette.colors(conn, currentPalette.getCpnid());
                        palettes = tableColorPaletteName.readAll(conn);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return colors != null;
                }

                @Override
                protected void whenSucceeded() {
                    isSettingValues = true;
                    colorsPane.getChildren().clear();
                    for (ColorData data : colors) {
                        Rectangle rect = makeColorRect(data);
                        if (rect != null) {
                            colorsPane.getChildren().add(rect);
                        }
                    }
                    label.setText(currentPalette.getName() + ": " + colorsPane.getChildren().size());
                    isSettingValues = false;
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                colorsPane.applyCss();
                                colorsPane.layout();
                            });
                        }
                    }, 600);
                }

                @Override
                protected void whenFailed() {
                }

                @Override
                protected void finalAction() {
                    task = null;
                    thisPane.setDisable(false);
                }

            };
            start(task, false);
        }

    }

    public Rectangle makeColorRect(ColorData data) {
        try {
            if (data == null) {
                return null;
            }
            Rectangle rect = new Rectangle(rectSize, rectSize);
            rect.setUserData(data);
            NodeStyleTools.setTooltip(rect, new Tooltip(data.display()));
            Color color = data.getColor();
            rect.setFill(color);
            rect.setStroke(Color.BLACK);
            rect.setOnMouseClicked((MouseEvent event) -> {
                Platform.runLater(() -> {
                    takeColor(data);
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
            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    public void takeColor(ColorData data) {
        if (isSettingValues || data == null
                || parentController == null || parentRect == null) {
            return;
        }
        try {
            Color color = data.getColor();
            parentRect.setFill(color);
            parentRect.setUserData(data);
            NodeStyleTools.setTooltip(parentRect, data.display());
            parentController.closePopup();
            setNotify.set(!setNotify.get());
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void popPaletteMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            if (palettes != null) {
                ToggleGroup pgroup = new ToggleGroup();
                String currentName = UserConfig.getString(baseName + "Palette", null);
                for (ColorPaletteName palette : palettes) {
                    String name = palette.getName();
                    RadioMenuItem rmenu = new RadioMenuItem(name);
                    rmenu.setOnAction((ActionEvent menuItemEvent) -> {
                        UserConfig.setString(baseName + "Palette", name);
                        loadColors();
                    });
                    rmenu.setToggleGroup(pgroup);
                    rmenu.setSelected(name.equals(currentName));
                    items.add(rmenu);
                }
            }

            items.add(new SeparatorMenuItem());

            MenuItem menu = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                popMenu = null;
            });
            items.add(menu);

            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.getItems().addAll(items);

            LocateTools.locateMouse(mouseEvent, popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popDataMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("ManageColors"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                ColorsManageController.oneOpen();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
            items.addAll(PaletteTools.paletteExamplesMenu(parentController == null ? myController : parentController,
                    tableColorPaletteName, tableColorPalette, tableColor));

            items.add(new SeparatorMenuItem());

            CheckMenuItem checkMenu = new CheckMenuItem(message("PopColorSetWhenMouseHovering"));
            checkMenu.setSelected(UserConfig.getBoolean("PopColorSetWhenMouseHovering", true));
            checkMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("PopColorSetWhenMouseHovering", checkMenu.isSelected());
                }
            });
            items.add(checkMenu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                popMenu = null;
            });
            items.add(menu);

            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.getItems().addAll(items);

            LocateTools.locateMouse(mouseEvent, popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void inputAction() {
        ColorPaletteInputController.open(this);
    }

    public void addColor(ColorData colorData) {
        if (colorData == null) {
            popError(message("InvalidParameters") + ": " + message("Color"));
            return;
        }
        SingletonTask addTask = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                return tableColorPalette.findAndCreate(currentPalette.getCpnid(), colorData, false) != null;
            }

            @Override
            protected void whenSucceeded() {
                PaletteTools.afterPaletteChanged(parentController, currentPalette.getName());
            }
        };
        start(addTask, false);
    }

    @FXML
    @Override
    public void closePopup() {
        if (parentController != null) {
            parentController.closePopup();
        }
        super.closePopup();
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

    public SimpleBooleanProperty getSetNotify() {
        return setNotify;
    }

    @Override
    public void cleanPane() {
        try {
            setNotify = null;
            parentRect = null;
            tableColorPaletteName = null;
            tableColorPalette = null;
            tableColor = null;
            palettes = null;
            currentPalette = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static ColorPalettePopupController open(BaseController parent, Rectangle rect) {
        try {
            ColorPalettePopupController controller = (ColorPalettePopupController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ColorPalettePopupFxml, true);
            controller.load(parent, rect);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
