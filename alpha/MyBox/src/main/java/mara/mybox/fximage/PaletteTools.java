package mara.mybox.fximage;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ColorPalettePopupController;
import mara.mybox.controller.ColorSet;
import mara.mybox.controller.ControlColors;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorDataTools;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.StyleData;
import mara.mybox.fxml.style.StyleData.StyleColor;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Colors.color;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-22
 * @License Apache License Version 2.0
 */
public class PaletteTools {

    public static void popPaletteExamplesMenu(BaseController parent, MouseEvent mouseEvent,
            TableColorPaletteName tableColorPaletteName, TableColorPalette tableColorPalette, TableColor tableColor) {
        try {
            ContextMenu pMenu = parent.getPopMenu();
            if (pMenu != null && pMenu.isShowing()) {
                pMenu.hide();
            }
            ContextMenu popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            popMenu.getItems().addAll(paletteExamplesMenu(parent, tableColorPaletteName, tableColorPalette, tableColor));

            popMenu.getItems().add(new SeparatorMenuItem());

            MenuItem menu = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                popMenu.hide();
            });
            popMenu.getItems().add(menu);

            parent.setPopMenu(popMenu);
            LocateTools.locateMouse(mouseEvent, popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static List<MenuItem> paletteExamplesMenu(BaseController parent,
            TableColorPaletteName tableColorPaletteName, TableColorPalette tableColorPalette, TableColor tableColor) {
        try {
            List<MenuItem> menus = new ArrayList<>();

            MenuItem menu;
            menu = new MenuItem(message("WebCommonColors"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ColorsWeb.csv", "data", "ColorsWeb.csv");
                importPalette(parent, tableColorPaletteName, tableColorPalette, tableColor,
                        file, message("WebCommonColors"), true);
            });
            menus.add(menu);

            menu = new MenuItem(message("ArtHues24"));
            menu.setOnAction((ActionEvent event) -> {
                String lang = Languages.getLangName();
                File file = FxFileTools.getInternalFile("/data/examples/ColorsRYB24_" + lang + ".csv", "data", "ColorsRYB24_" + lang + ".csv");
                importPalette(parent, tableColorPaletteName, tableColorPalette, tableColor,
                        file, message("ArtHues24"), true);
            });
            menus.add(menu);

            menu = new MenuItem(message("ChineseTraditionalColors"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ColorsChinese.csv", "data", "ColorsChinese.csv");
                importPalette(parent, tableColorPaletteName, tableColorPalette, tableColor,
                        file, message("ChineseTraditionalColors"), true);
            });
            menus.add(menu);

            menu = new MenuItem(message("JapaneseTraditionalColors"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ColorsJapanese.csv", "data", "ColorsJapanese.csv");
                importPalette(parent, tableColorPaletteName, tableColorPalette, tableColor,
                        file, message("JapaneseTraditionalColors"), true);
            });
            menus.add(menu);

            menu = new MenuItem(message("HexaColors"));
            menu.setOnAction((ActionEvent event) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ColorsColorhexa.csv", "data", "ColorsColorhexa.csv");
                importPalette(parent, tableColorPaletteName, tableColorPalette, tableColor,
                        file, message("HexaColors"), true);
            });
            menus.add(menu);

            menu = new MenuItem(message("MyBoxColors"));
            menu.setOnAction((ActionEvent event) -> {
                importMyBoxColors(parent, tableColorPaletteName, tableColorPalette, tableColor);
            });
            menus.add(menu);

            return menus;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public synchronized static void importMyBoxColors(BaseController parent,
            TableColorPaletteName tableColorPaletteName, TableColorPalette tableColorPalette, TableColor tableColor) {
        if (parent == null || (parent.getTask() != null && !parent.getTask().isQuit())) {
            return;
        }
        SingletonTask task = new SingletonTask<Void>(parent) {
            @Override
            protected boolean handle() {
                List<ColorData> data = new ArrayList<>();
                for (StyleColor style : StyleData.StyleColor.values()) {
                    data.add(new ColorData(color(style, true).getRGB(), message("MyBoxColor" + style.name() + "Dark")));
                    data.add(new ColorData(color(style, false).getRGB(), message("MyBoxColor" + style.name() + "Light")));
                }
                data.add(new ColorData(FxColorTools.color2rgba(Color.BLACK), message("Black")));
                data.add(new ColorData(FxColorTools.color2rgba(Color.WHITE), message("White")));
                data.add(new ColorData(0, message("Transparent")));
                try (Connection conn = DerbyBase.getConnection()) {
                    tableColor.writeData(conn, data, false);
                    ColorPaletteName palette = tableColorPaletteName.findAndCreate(conn, message("MyBoxColors"));
                    tableColorPalette.write(conn, palette.getCpnid(), data, true);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                afterPaletteChanged(parent, message("MyBoxColors"));
            }
        };
        parent.setTask(task);
        parent.start(task);
    }

    public synchronized static void importPalette(BaseController parent,
            TableColorPaletteName tableColorPaletteName, TableColorPalette tableColorPalette, TableColor tableColor,
            File file, String paletteName, boolean reOrder) {
        if (parent == null || (parent.getTask() != null && !parent.getTask().isQuit())
                || file == null || !file.exists()) {
            return;
        }
        SingletonTask task = new SingletonTask<Void>(parent) {
            @Override
            protected boolean handle() {
                List<ColorData> data = ColorDataTools.readCSV(file, reOrder);
                if (data == null) {
                    return false;
                }
                data.add(new ColorData(FxColorTools.color2rgba(Color.BLACK), message("Black")));
                data.add(new ColorData(FxColorTools.color2rgba(Color.WHITE), message("White")));
                data.add(new ColorData(0, message("Transparent")));
                try (Connection conn = DerbyBase.getConnection()) {
                    tableColor.writeData(conn, data, false);
                    if (paletteName != null && !paletteName.isBlank()) {
                        ColorPaletteName palette = tableColorPaletteName.findAndCreate(conn, paletteName);
                        tableColorPalette.write(conn, palette.getCpnid(), data, true);
                    }
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                afterPaletteChanged(parent, paletteName);
            }
        };
        parent.setTask(task);
        parent.start(task);
    }

    public static void afterPaletteChanged(BaseController parent, String paletteName) {
        UserConfig.setString("ColorPalettePopupPalette", paletteName);
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof ColorPalettePopupController) {
                try {
                    ColorPalettePopupController controller = (ColorPalettePopupController) object;
                    controller.loadColors();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (parent == null) {
            return;
        }
        if (parent instanceof ControlColors) {
            ControlColors controller = (ControlColors) parent;
            UserConfig.setString(controller.getBaseName() + "Palette", paletteName);
            controller.refreshPalettes();
            parent.popSuccessful();
        } else if (parent instanceof ColorSet) {
            ColorSet controller = (ColorSet) parent;
            controller.showColorPalette();
        }

    }

}
