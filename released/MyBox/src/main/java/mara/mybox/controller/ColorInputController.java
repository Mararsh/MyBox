package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-09-01
 * @License Apache License Version 2.0
 */
public class ColorInputController extends BaseController {

    protected ColorsManageController colorsManager;

    @FXML
    protected TextArea valuesArea;
    @FXML
    protected Button examplesButton;
    @FXML
    protected ColorPicker colorPicker;

    public ColorInputController() {
        baseTitle = Languages.message("InputColors");
    }

    public void setParameters(ColorsManageController colorsManager) {
        try {
            this.colorsManager = colorsManager;
            NodeStyleTools.removeTooltip(examplesButton);

            colorPicker.valueProperty().addListener((ObservableValue<? extends Color> ov, Color oldVal, Color newVal) -> {
                if (isSettingValues || newVal == null) {
                    return;
                }
                valuesArea.appendText(FxColorTools.color2rgba(newVal) + "\n");
            });

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void popExamples(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            List<String> values = new ArrayList<>();
            values.addAll(Arrays.asList(
                    "orange", "pink", "lightblue", "wheat",
                    "0xff668840", "0xff6688", "#ff6688", "#f68",
                    "rgb(255,102,136)", "rgb(100%,50%,50%)",
                    "rgba(255,102,136,0.25)", "rgba(255,50%,50%,0.25)",
                    "hsl(240,100%,100%)", "hsla(120,0%,0%,0.25)"
            ));

            MenuItem menu;
            for (String value : values) {
                menu = new MenuItem(value);
                menu.setOnAction((ActionEvent event) -> {
                    valuesArea.appendText(value + "\n");
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(Languages.message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    String[] values = valuesArea.getText().split("\n");
                    if (values == null || values.length == 0) {
                        return true;
                    }
                    try ( Connection conn = DerbyBase.getConnection();) {
                        TableColor tableColor = null;
                        TableColorPalette tableColorPalette = null;
                        long paletteid = -1;
                        if (colorsManager != null) {
                            tableColor = colorsManager.colorsController.tableColor;
                            if (!colorsManager.colorsController.isAllColors()) {
                                paletteid = colorsManager.colorsController.currentPalette.getCpnid();
                            }
                            tableColorPalette = colorsManager.colorsController.tableColorPalette;
                        }
                        if (tableColor == null) {
                            tableColor = new TableColor();
                        }
                        if (tableColorPalette == null) {
                            tableColorPalette = new TableColorPalette();
                        }
                        conn.setAutoCommit(false);
                        for (String value : values) {
                            value = value.trim();
                            ColorData color = new ColorData(value).calculate();
                            if (color.getSrgb() == null) {
                                continue;
                            }
                            if (!value.startsWith("#") && !value.startsWith("0x")
                                    && !value.startsWith("rgb") && !value.startsWith("hsl")) {
                                color.setColorName(value);
                            }
                            tableColor.write(conn, color, true);
                            if (paletteid >= 0) {
                                tableColorPalette.findAndCreate(conn, paletteid, color, false);
                            }
                        }
                        conn.commit();
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (colorsManager == null || !colorsManager.getMyStage().isShowing()) {
                        colorsManager = ColorsManageController.oneOpen();
                    } else {
                        colorsManager.colorsController.refreshPalette();
                    }
                    closeStage();
                }

            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void clearAction() {
        valuesArea.clear();
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

    /*
        static methods
     */
    public static ColorInputController oneOpen(ColorsManageController parent) {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof ColorInputController) {
                ((ColorInputController) object).close();
            }
        }
        ColorInputController controller = (ColorInputController) WindowTools.openChildStage(parent.getMyStage(), Fxmls.ColorInputFxml, false);
        controller.setParameters(parent);
        return controller;
    }
}
