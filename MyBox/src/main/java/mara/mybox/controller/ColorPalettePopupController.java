package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-09-04
 * @License Apache License Version 2.0
 */
public class ColorPalettePopupController extends BaseController {

    protected TableColorPaletteName tableColorPaletteName;
    protected TableColorPalette tableColorPalette;
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

    public ColorPalettePopupController() {
        baseTitle = AppVariables.message("ColorPalette");

    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        setController.keyEventsHandler(event);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            tableColorPaletteName = new TableColorPaletteName();
            tableColorPalette = new TableColorPalette();
            rectSize = AppVariables.iconSize * 0.8;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void load(ColorSet parent) {
        try {
            thisPane.setStyle(" -fx-background-color: white;");
            FxmlControl.refreshStyle(thisPane);
            FxmlControl.setTooltip(cancelButton, message("PopupClose"));

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
            task = new SingletonTask<Void>() {

                protected List<ColorData> colors;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        ColorPaletteName defaultPalette = tableColorPaletteName.defaultPalette(conn);
                        String paletteName = AppVariables.getUserConfigValue(baseName + "Palette", defaultPalette.getName());
                        currentPalette = tableColorPaletteName.find(conn, paletteName);
                        if (currentPalette == null) {
                            currentPalette = defaultPalette;
                        }
                        if (currentPalette == null) {
                            return false;
                        }
                        paletteName = currentPalette.getName();
                        AppVariables.setUserConfigValue(baseName + "Palette", paletteName);
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
                        try {
                            addColor(data, false);
                        } catch (Exception e) {
                            MyBoxLog.error(e.toString());
                        }
                    }
                    label.setText(currentPalette.getName() + ": " + colorsPane.getChildren().size());
                    isSettingValues = false;
                }

                @Override
                protected void finalAction() {
                    thisPane.setDisable(false);
                }

            };
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
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
    protected void popPaletteMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            if (palettes != null) {
                for (ColorPaletteName palette : palettes) {
                    menu = new MenuItem(palette.getName());
                    menu.setOnAction((ActionEvent menuItemEvent) -> {
                        AppVariables.setUserConfigValue(baseName + "Palette", palette.getName());
                        loadColors();
                    });
                    items.add(menu);
                }
            }

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(message("ManageColors"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                ColorsManageController.oneOpen(null);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem checkMenu = new CheckMenuItem(message("PopColorSetWhenMousePassing"));
            checkMenu.setSelected(AppVariables.getUserConfigBoolean("PopColorSetWhenMousePassing", true));
            checkMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVariables.setUserConfigValue("PopColorSetWhenMousePassing", checkMenu.isSelected());
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
            FxmlControl.locateCenter((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void cancelAction() {
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
