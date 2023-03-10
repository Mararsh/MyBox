package mara.mybox.fximage;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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

    public static void popPaletteExamplesMenu(BaseController parent, Event event) {
        try {
            ContextMenu pMenu = parent.getPopMenu();
            if (pMenu != null && pMenu.isShowing()) {
                pMenu.hide();
            }
            ContextMenu popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            popMenu.getItems().addAll(paletteExamplesMenu(parent));

            popMenu.getItems().add(new SeparatorMenuItem());

            MenuItem menu = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent e) -> {
                popMenu.hide();
            });
            popMenu.getItems().add(menu);

            parent.setPopMenu(popMenu);
            LocateTools.locateEvent(event, popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static List<MenuItem> paletteExamplesMenu(BaseController parent) {
        try {
            List<MenuItem> menus = new ArrayList<>();

            MenuItem menu = new MenuItem(message("ArtHuesWheel24"));
            menu.setOnAction((ActionEvent e) -> {
                String lang = Languages.getLangName();
                File file = FxFileTools.getInternalFile("/data/examples/ColorsRYB24_" + lang + ".csv",
                        "data", "ColorsRYB24_" + lang + ".csv");
                importFile(parent, file, message("ArtHuesWheel24"), true);
            });
            menus.add(menu);

            menu = new MenuItem(message("OpticalHuesWheel24"));
            menu.setOnAction((ActionEvent e) -> {
                importData(parent, opticalHuesWheel(15), message("OpticalHuesWheel24"));
            });
            menus.add(menu);

            menu = new MenuItem(message("WebCommonColors"));
            menu.setOnAction((ActionEvent e) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ColorsWeb.csv", "data", "ColorsWeb.csv");
                importFile(parent, file, message("WebCommonColors"), true);
            });
            menus.add(menu);

            menu = new MenuItem(message("ChineseTraditionalColors"));
            menu.setOnAction((ActionEvent e) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ColorsChinese.csv", "data", "ColorsChinese.csv");
                importFile(parent, file, message("ChineseTraditionalColors"), true);
            });
            menus.add(menu);

            menu = new MenuItem(message("JapaneseTraditionalColors"));
            menu.setOnAction((ActionEvent e) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ColorsJapanese.csv", "data", "ColorsJapanese.csv");
                importFile(parent, file, message("JapaneseTraditionalColors"), true);
            });
            menus.add(menu);

            menu = new MenuItem(message("HexaColors"));
            menu.setOnAction((ActionEvent e) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ColorsColorhexa.csv", "data", "ColorsColorhexa.csv");
                importFile(parent, file, message("HexaColors"), true);
            });
            menus.add(menu);

            menu = new MenuItem(message("OpticalHuesWheel360"));
            menu.setOnAction((ActionEvent e) -> {
                importData(parent, opticalHuesWheel(1), message("OpticalHuesWheel360"));
            });
            menus.add(menu);

            menu = new MenuItem(message("ArtPaints"));
            menu.setOnAction((ActionEvent e) -> {
                File file = FxFileTools.getInternalFile("/data/examples/ColorsArtPaints.csv", "data", "ColorsArtPaints.csv");
                importFile(parent, file, message("ArtPaints"), true);
            });
            menus.add(menu);

            menu = new MenuItem(message("MyBoxColors"));
            menu.setOnAction((ActionEvent e) -> {
                List<ColorData> colors = new ArrayList<>();
                for (StyleColor style : StyleData.StyleColor.values()) {
                    colors.add(new ColorData(color(style, true).getRGB(), message("MyBoxColor" + style.name() + "Dark")));
                    colors.add(new ColorData(color(style, false).getRGB(), message("MyBoxColor" + style.name() + "Light")));
                }
                colors.add(new ColorData(FxColorTools.color2rgba(Color.WHITE), message("White")));
                colors.add(new ColorData(FxColorTools.color2rgba(Color.BLACK), message("Black")));
                colors.add(new ColorData(0, message("Transparent")));
                importData(parent, colors, message("MyBoxColors"));
            });
            menus.add(menu);

            menu = new MenuItem(message("GrayScale"));
            menu.setOnAction((ActionEvent e) -> {
                importData(parent, greyScales(), message("GrayScale"));
            });
            menus.add(menu);

            return menus;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void importData(BaseController parent, List<ColorData> colors, String name) {
        if (parent == null || (parent.getTask() != null && !parent.getTask().isQuit())
                || colors == null || colors.isEmpty()) {
            return;
        }
        SingletonTask task = new SingletonTask<Void>(parent) {
            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    ColorPaletteName palette = new TableColorPaletteName().findAndCreate(conn, name);
                    if (palette == null) {
                        return false;
                    }
                    new TableColor().writeData(conn, colors, false);
                    new TableColorPalette().write(conn, palette.getCpnid(), colors, true);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                afterPaletteChanged(parent, name);
            }
        };
        parent.setTask(task);
        parent.start(task);
    }

    public static void importFile(BaseController parent,
            File file, String paletteName, boolean reOrder) {
        if (parent == null || (parent.getTask() != null && !parent.getTask().isQuit())
                || file == null || !file.exists()) {
            return;
        }
        SingletonTask task = new SingletonTask<Void>(parent) {
            List<ColorData> colors;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    ColorPaletteName palette = new TableColorPaletteName().findAndCreate(conn, paletteName);
                    if (palette == null) {
                        return false;
                    }
                    colors = ColorDataTools.readCSV(file, reOrder);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
                return colors != null && !colors.isEmpty();
            }

            @Override
            protected void whenSucceeded() {
                importData(parent, colors, paletteName);
            }
        };
        parent.setTask(task);
        parent.start(task);
    }

    public static List<ColorData> opticalHuesWheel(int step) {
        try {
            List<ColorData> colors = new ArrayList<>();
            for (int hue = 0; hue < 360; hue += step) {
                ColorData color = new ColorData(FxColorTools.hsb2rgba(hue, 1f, 1f), message("Hue") + ": " + hue);
                colors.add(color);
            }
            colors.add(new ColorData(FxColorTools.color2rgba(Color.WHITE), message("White")));
            colors.add(new ColorData(FxColorTools.color2rgba(Color.BLACK), message("Black")));
            colors.add(new ColorData(0, message("Transparent")));
            return colors;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<ColorData> greyScales() {
        try {
            List<ColorData> colors = new ArrayList<>();
            for (int b = 100; b >= 0; b--) {
                ColorData color = new ColorData(FxColorTools.hsb2rgba(0, 0f, b / 100f), message("Brightness") + ": " + b);
                colors.add(color);
            }
            return colors;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String defaultPaletteName() {
        return message("ArtHuesWheel24");
    }

    public static ColorPaletteName defaultPalette(Connection conn) {
        try {
            if (conn == null) {
                return null;
            }
            boolean ac = conn.getAutoCommit();
            conn.setAutoCommit(true);
            ColorPaletteName palette = new TableColorPaletteName().findAndCreate(conn, defaultPaletteName());
            if (palette == null) {
                conn.setAutoCommit(ac);
                return null;
            }
            conn.setAutoCommit(false);
            long paletteid = palette.getCpnid();
            TableColorPalette tableColorPalette = new TableColorPalette();
            if (tableColorPalette.size(conn, paletteid) == 0) {
                String lang = Languages.getLangName();
                File file = FxFileTools.getInternalFile("/data/examples/ColorsRYB24_" + lang + ".csv",
                        "data", "ColorsRYB24_" + lang + ".csv");
                List<ColorData> colors = ColorDataTools.readCSV(file, true);
                if (colors == null) {
                    return null;
                }
                colors.add(new ColorData(FxColorTools.color2rgba(Color.WHITE), message("White")));
                colors.add(new ColorData(FxColorTools.color2rgba(Color.BLACK), message("Black")));
                colors.add(new ColorData(0, message("Transparent")));
                new TableColor().writeData(conn, colors, false);
                tableColorPalette.write(conn, palette.getCpnid(), colors, true);
                conn.commit();
            }
            conn.setAutoCommit(ac);
            return palette;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
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
