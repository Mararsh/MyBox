package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
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
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-09-04
 * @License Apache License Version 2.0
 */
public class ColorPalettePopupController extends BaseController {

    protected TableColorPaletteName tableColorPaletteName;
    protected TableColorPalette tableColorPalette;
    protected TableColor tableColor;
    protected List<ColorPaletteName> palettes;
    protected ColorPaletteName currentPalette;
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
    protected Button paletteButton;

    public ColorPalettePopupController() {
        baseTitle = message("ColorPalette");
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        return setController.keyEventsFilter(event);
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

    public void load(ColorSet parent) {
        try {
            thisPane.setStyle(" -fx-background-color: white;");
            refreshStyle(thisPane);

            this.setController = parent;
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
                        String paletteName = UserConfig.getString(baseName + "Palette", defaultPalette.getName());
                        currentPalette = tableColorPaletteName.find(conn, paletteName);
                        if (currentPalette == null) {
                            currentPalette = defaultPalette;
                        }
                        if (currentPalette == null) {
                            return false;
                        }
                        paletteName = currentPalette.getName();
                        UserConfig.setString(baseName + "Palette", paletteName);
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

    protected Rectangle makeColorRect(ColorData data) {
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
                    if (isSettingValues || setController == null || setController.rect == null) {
                        return;
                    }
                    try {
                        setController.rect.setFill(color);
                        setController.rect.setUserData(data);
                        NodeStyleTools.setTooltip(setController.rect, data.display());
                        setController.closePopup();
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
            return rect;
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    protected void popPaletteMenu() {
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

            MenuItem menu = new MenuItem(message("ManageColors"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                ColorsManageController.oneOpen();
            });
            items.add(menu);

            Menu exmaplesMenu = new Menu(message("ExamplePalettes"));
            exmaplesMenu.getItems().addAll(PaletteTools.paletteExamplesMenu(setController, tableColorPaletteName, tableColorPalette, tableColor));
            items.add(exmaplesMenu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem checkMenu = new CheckMenuItem(message("PopColorSetWhenMousePassing"));
            checkMenu.setSelected(UserConfig.getBoolean("PopColorSetWhenMousePassing", true));
            checkMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("PopColorSetWhenMousePassing", checkMenu.isSelected());
                }
            });
            items.add(checkMenu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("PopupClose"));
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
            LocateTools.locateCenter(paletteButton, popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void closePopup() {
        setController.closePopup();
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
