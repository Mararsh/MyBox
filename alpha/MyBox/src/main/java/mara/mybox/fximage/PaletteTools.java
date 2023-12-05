package mara.mybox.fximage;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.ColorPalettePopupController;
import mara.mybox.controller.ControlColorPaletteSelector;
import mara.mybox.controller.ControlColorSet;
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
import mara.mybox.value.AppVariables;
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

    public static List<MenuItem> paletteExamplesMenu(BaseController parent) {
        try {
            List<MenuItem> menus = new ArrayList<>();

            MenuItem menu;

            menu = new MenuItem(defaultPaletteName(AppVariables.CurrentLangName));
            menu.setOnAction((ActionEvent e) -> {
                importPalette(parent, defaultPaletteName(AppVariables.CurrentLangName));
            });
            menus.add(menu);

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
        if (parent == null || paletteName == null) {
            return;
        }
        if (parent.getTask() != null) {
            parent.getTask().cancel();
        }
        SingletonTask task = new SingletonTask<Void>(parent) {
            @Override
            protected boolean handle() {
                List<ColorData> colors;
                String fileLang = Languages.embedFileLang();
                if (defaultPaletteName(AppVariables.CurrentLangName).equals(paletteName)) {
                    colors = defaultColors(AppVariables.CurrentLangName);

                } else if (message("WebCommonColors").equals(paletteName)) {
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

                } else if ((message("ArtHuesWheel") + "-" + message("Colors12")).equals(paletteName)) {
                    File file = FxFileTools.getInternalFile("/data/examples/ColorsRYB12_" + fileLang + ".csv",
                            "data", "ColorsRYB12_" + fileLang + ".csv", true);
                    colors = ColorDataTools.readCSV(file, true);

                } else if ((message("ArtHuesWheel") + "-" + message("Colors360")).equals(paletteName)) {
                    colors = artHuesWheel(AppVariables.CurrentLangName, 1);

                } else if ((message("OpticalHuesWheel") + "-" + message("Colors12")).equals(paletteName)) {
                    colors = opticalHuesWheel(AppVariables.CurrentLangName, 30);

                } else if ((message("OpticalHuesWheel") + "-" + message("Colors24")).equals(paletteName)) {
                    colors = opticalHuesWheel(AppVariables.CurrentLangName, 15);

                } else if ((message("OpticalHuesWheel") + "-" + message("Colors360")).equals(paletteName)) {
                    colors = opticalHuesWheel(AppVariables.CurrentLangName, 1);

                } else if ((message("GrayScale")).equals(paletteName)) {
                    colors = greyScales(AppVariables.CurrentLangName);

                } else {
                    File file = FxFileTools.getInternalFile("/data/examples/ColorsRYB24_" + fileLang + ".csv",
                            "data", "ColorsRYB24_" + fileLang + ".csv", true);
                    colors = ColorDataTools.readCSV(file, true);

                }
                if (colors == null || colors.isEmpty()) {
                    return false;
                }
                colors.addAll(speicalColors(AppVariables.CurrentLangName));
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

    public static List<ColorData> opticalHuesWheel(String lang, int step) {
        try {
            List<ColorData> colors = new ArrayList<>();
            for (int hue = 0; hue < 360; hue += step) {
                Color color = Color.hsb(hue, 1f, 1f);
                ColorData data = new ColorData(color).calculate();
                data.setColorName(message(lang, "Hue") + ":" + Math.round(data.getColor().getHue()));
                colors.add(data);
            }
            return colors;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<ColorData> artHuesWheel(String lang, int step) {
        try {
            List<ColorData> colors = new ArrayList<>();
            for (int angle = 0; angle < 360; angle += step) {
                java.awt.Color color = ColorConvertTools.ryb2rgb(angle);
                ColorData data = new ColorData(color.getRGB()).calculate();
                data.setColorName(message(lang, "RYBAngle") + ":" + angle + " "
                        + message(lang, "Hue") + ":" + Math.round(data.getColor().getHue()));
                colors.add(data);
            }
            return colors;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<ColorData> greyScales(String lang) {
        try {
            List<ColorData> colors = new ArrayList<>();
            for (int v = 0; v < 256; v++) {
                Color color = Color.gray(v / 255d);
                ColorData data = new ColorData(color).calculate();
                data.setColorName(message(lang, "Gray") + ":" + Math.round(color.getRed() * 255));
                colors.add(data);
            }
            return colors;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static List<ColorData> speicalColors(String lang) {
        try {
            List<ColorData> colors = new ArrayList<>();
            colors.add(new ColorData(FxColorTools.color2rgba(Color.WHITE), message(lang, "White")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.LIGHTGREY), message(lang, "LightGrey")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.GREY), message(lang, "Grey")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.BLACK), message(lang, "Black")).calculate());
            colors.add(new ColorData(0, message(lang, "Transparent")).calculate());
            return colors;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String defaultPaletteName(String lang) {
        return message(lang, "DefaultPalette");
    }

    public static List<ColorData> defaultColors(String lang) {
        try {
            List<ColorData> colors = new ArrayList<>();
            colors.add(new ColorData(FxColorTools.color2rgba(Color.RED), message(lang, "Red")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.ORANGE), message(lang, "Orange")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.YELLOW), message(lang, "Yellow")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.GREENYELLOW), message(lang, "GreenYellow")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.GREEN), message(lang, "Green")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.LIGHTSEAGREEN), message(lang, "SeaGreen")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.DODGERBLUE), message(lang, "Blue")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.BLUE), message(lang, "MediumBlue")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.PURPLE), message(lang, "Purple")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.PINK), message(lang, "Pink")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.DEEPSKYBLUE), message(lang, "SkyBlue")).calculate());
            colors.add(new ColorData(FxColorTools.color2rgba(Color.GOLD), message(lang, "GoldColor")).calculate());
            return colors;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static ColorPaletteName defaultPalette(String lang, Connection conn) {
        try {
            if (conn == null) {
                return null;
            }
            boolean ac = conn.getAutoCommit();
            conn.setAutoCommit(true);
            ColorPaletteName palette = new TableColorPaletteName().findAndCreate(conn, defaultPaletteName(lang));
            if (palette == null) {
                conn.setAutoCommit(ac);
                return null;
            }
            conn.setAutoCommit(false);
            long paletteid = palette.getCpnid();
            TableColorPalette tableColorPalette = new TableColorPalette();
            if (tableColorPalette.size(conn, paletteid) == 0) {
                List<ColorData> colors = defaultColors(lang);
                colors.addAll(speicalColors(lang));
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
        if (parent instanceof ControlColorPaletteSelector) {
            ControlColorPaletteSelector controller = (ControlColorPaletteSelector) parent;
            UserConfig.setString(controller.getBaseName() + "Palette", paletteName);
            controller.loadPalettes();
            parent.popSuccessful();
        } else if (parent instanceof ControlColorSet) {
            ControlColorSet controller = (ControlColorSet) parent;
            controller.showColorPalette();
        }

    }

}
