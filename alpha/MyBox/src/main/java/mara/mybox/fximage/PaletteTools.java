package mara.mybox.fximage;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import mara.mybox.bufferedimage.ColorConvertTools;
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
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.StyleData;
import mara.mybox.fxml.style.StyleData.StyleColor;
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
            List<MenuItem> items = new ArrayList<>();
            items.addAll(paletteExamplesMenu(parent));
            parent.popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public static List<MenuItem> paletteExamplesMenu(BaseController parent) {
        try {
            List<MenuItem> menus = new ArrayList<>();

            MenuItem menu;

            menu = new MenuItem(message("WebCommonColors"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("WebCommonColors"));
            });
            menus.add(menu);

            menu = new MenuItem(message("ChineseTraditionalColors"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("ChineseTraditionalColors"));
            });
            menus.add(menu);

            menu = new MenuItem(message("JapaneseTraditionalColors"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("JapaneseTraditionalColors"));
            });
            menus.add(menu);

            menu = new MenuItem(message("HexaColors"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("HexaColors"));
            });
            menus.add(menu);

            menu = new MenuItem(message("ArtHuesWheel") + "-" + message("Colors12"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("ArtHuesWheel") + "-" + message("Colors12"));
            });
            menus.add(menu);

            menu = new MenuItem(message("ArtHuesWheel") + "-" + message("Colors24"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("ArtHuesWheel") + "-" + message("Colors24"));
            });
            menus.add(menu);

            menu = new MenuItem(message("ArtHuesWheel") + "-" + message("Colors360"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("ArtHuesWheel") + "-" + message("Colors360"));
            });
            menus.add(menu);

            menu = new MenuItem(message("OpticalHuesWheel") + "-" + message("Colors12"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("OpticalHuesWheel") + "-" + message("Colors12"));
            });
            menus.add(menu);

            menu = new MenuItem(message("OpticalHuesWheel") + "-" + message("Colors24"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("OpticalHuesWheel") + "-" + message("Colors24"));
            });
            menus.add(menu);

            menu = new MenuItem(message("OpticalHuesWheel") + "-" + message("Colors360"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("OpticalHuesWheel") + "-" + message("Colors360"));
            });
            menus.add(menu);

            menu = new MenuItem(message("ArtPaints"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("ArtPaints"));
            });
            menus.add(menu);

            menu = new MenuItem(message("MyBoxColors"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("MyBoxColors"));
            });
            menus.add(menu);

            menu = new MenuItem(message("GrayScale"));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, message("GrayScale"));
            });
            menus.add(menu);

            return menus;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void importPalette(BaseController parent, String paletteName) {
        if (parent == null || (parent.getTask() != null && !parent.getTask().isQuit())
                || paletteName == null) {
            return;
        }
        SingletonTask task = new SingletonTask<Void>(parent) {
            @Override
            protected boolean handle() {
                List<ColorData> colors;
                if (message("WebCommonColors").equals(paletteName)) {
                    File file = FxFileTools.getInternalFile("/data/examples/ColorsWeb.csv",
                            "data", "ColorsWeb.csv", true);
                    colors = ColorDataTools.readCSV(file, true);

                } else if (message("ArtPaints").equals(paletteName)) {
                    File file = FxFileTools.getInternalFile("/data/examples/ColorsArtPaints.csv",
                            "data", "ColorsArtPaints.csv", true);
                    colors = ColorDataTools.readCSV(file, true);

                } else if (message("ChineseTraditionalColors").equals(paletteName)) {
                    File file = FxFileTools.getInternalFile("/data/examples/ColorsChinese.csv",
                            "data", "ColorsChinese.csv", true);
                    colors = ColorDataTools.readCSV(file, true);

                } else if (message("JapaneseTraditionalColors").equals(paletteName)) {
                    File file = FxFileTools.getInternalFile("/data/examples/ColorsJapanese.csv",
                            "data", "ColorsJapanese.csv", true);
                    colors = ColorDataTools.readCSV(file, true);

                } else if (message("HexaColors").equals(paletteName)) {
                    File file = FxFileTools.getInternalFile("/data/examples/ColorsColorhexa.csv",
                            "data", "ColorsColorhexa.csv", true);
                    colors = ColorDataTools.readCSV(file, true);

                } else if (message("MyBoxColors").equals(paletteName)) {
                    colors = new ArrayList<>();
                    for (StyleColor style : StyleData.StyleColor.values()) {
                        colors.add(new ColorData(color(style, true).getRGB(), message("MyBoxColor" + style.name() + "Dark")));
                        colors.add(new ColorData(color(style, false).getRGB(), message("MyBoxColor" + style.name() + "Light")));
                    }
                    colors.addAll(speicalColors());

                } else if ((message("ArtHuesWheel") + "-" + message("Colors12")).equals(paletteName)) {
                    String lang = Languages.getLangName();
                    File file = FxFileTools.getInternalFile("/data/examples/ColorsRYB12_" + lang + ".csv",
                            "data", "ColorsRYB12_" + lang + ".csv", true);
                    colors = ColorDataTools.readCSV(file, true);

                } else if ((message("ArtHuesWheel") + "-" + message("Colors360")).equals(paletteName)) {
                    colors = artHuesWheel(1);

                } else if ((message("OpticalHuesWheel") + "-" + message("Colors12")).equals(paletteName)) {
                    colors = opticalHuesWheel(30);

                } else if ((message("OpticalHuesWheel") + "-" + message("Colors24")).equals(paletteName)) {
                    colors = opticalHuesWheel(15);

                } else if ((message("OpticalHuesWheel") + "-" + message("Colors360")).equals(paletteName)) {
                    colors = opticalHuesWheel(1);

                } else if (message("GrayScale").equals(paletteName)) {
                    colors = greyScales();

                } else {
                    String lang = Languages.getLangName();
                    File file = FxFileTools.getInternalFile("/data/examples/ColorsRYB24_" + lang + ".csv",
                            "data", "ColorsRYB24_" + lang + ".csv", true);
                    colors = ColorDataTools.readCSV(file, true);

                }
                if (colors == null || colors.isEmpty()) {
                    return false;
                }
                try (Connection conn = DerbyBase.getConnection()) {
                    ColorPaletteName palette = new TableColorPaletteName().findAndCreate(conn, paletteName);
                    if (palette == null) {
                        return false;
                    }
                    TableColorPalette tableColorPalette = new TableColorPalette();
                    tableColorPalette.clear(conn, palette.getCpnid());
                    tableColorPalette.write(conn, palette.getCpnid(), colors, true, false);
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

    public static void importFile(BaseController parent,
            File file, String paletteName, boolean reOrder) {
        if (parent == null || (parent.getTask() != null && !parent.getTask().isQuit())
                || file == null || !file.exists()) {
            return;
        }
        SingletonTask task = new SingletonTask<Void>(parent) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    List<ColorData> colors = ColorDataTools.readCSV(file, reOrder);
                    if (colors == null || colors.isEmpty()) {
                        return false;
                    }
                    if (paletteName == null || paletteName.isBlank()) {
                        new TableColor().writeData(conn, colors, true);
                    } else {
                        ColorPaletteName palette = new TableColorPaletteName().findAndCreate(conn, paletteName);
                        if (palette == null) {
                            return false;
                        }
                        new TableColorPalette().write(conn, palette.getCpnid(), colors, true, false);
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

    public static List<ColorData> opticalHuesWheel(int step) {
        try {
            List<ColorData> colors = new ArrayList<>();
            for (int hue = 0; hue < 360; hue += step) {
                Color color = Color.hsb(hue, 1f, 1f);
                ColorData data = new ColorData(color).calculate();
                data.setColorName(message("Hue") + ":" + Math.round(data.getColor().getHue()));
                colors.add(data);
            }
            return colors;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<ColorData> artHuesWheel(int step) {
        try {
            List<ColorData> colors = new ArrayList<>();
            for (int angle = 0; angle < 360; angle += step) {
                java.awt.Color color = ColorConvertTools.ryb2rgb(angle);
                ColorData data = new ColorData(color.getRGB()).calculate();
                data.setColorName(message("RYBAngle") + ":" + angle + " "
                        + message("Hue") + ":" + Math.round(data.getColor().getHue()));
                colors.add(data);
            }
            return colors;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<ColorData> greyScales() {
        try {
            List<ColorData> colors = new ArrayList<>();
            for (int v = 255; v >= 0; v--) {
                float c = v / 255f;
                Color color = new Color(c, c, c, 1);
                ColorData data = new ColorData(color).calculate();
                data.setColorName(message("Brightness") + ":" + Math.round(color.getBrightness() * 255));
                colors.add(data);
            }
            return colors;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<ColorData> speicalColors() {
        try {
            List<ColorData> colors = new ArrayList<>();
            colors.add(new ColorData(FxColorTools.color2rgba(Color.WHITE), message("White")));
            colors.add(new ColorData(FxColorTools.color2rgba(Color.BLACK), message("Black")));
            colors.add(new ColorData(FxColorTools.color2rgba(Color.GRAY), message("Gray")));
            colors.add(new ColorData(0, message("Transparent")));
            return colors;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String defaultPaletteName() {
        return message("ArtHuesWheel") + "-" + message("Colors24");
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
                tableColorPalette.write(conn, palette.getCpnid(), colors, true, false);
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
        if (paletteName == null || paletteName.isBlank()) {
            return;
        }
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
