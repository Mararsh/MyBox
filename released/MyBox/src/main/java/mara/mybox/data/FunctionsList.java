package mara.mybox.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-4
 * @License Apache License Version 2.0
 */
public class FunctionsList {

    protected static final int MaxLevel = 3;
    protected MenuBar menuBar;
    protected boolean withLink;
    protected StringTable table;
    protected String goImageFile, lang;
    protected Map<String, MenuItem> map;

    public FunctionsList(MenuBar menuBar, boolean withLink, String lang) {
        this.menuBar = menuBar;
        this.withLink = withLink;
        this.lang = lang;
    }

    public StringTable make() {
        try {
            if (menuBar == null) {
                return null;
            }
            if (withLink) {
                goImageFile = AppVariables.MyboxDataPath + "/icons/iconGo.png";
                BufferedImage srcImage = SwingFXUtils.fromFXImage(StyleTools.getIconImage("iconGo.png"), null);
                ImageFileWriters.writeImageFile(null, srcImage, "png", goImageFile);
                goImageFile = new File(goImageFile).toURI().toString();
            }
            List<String> names = new ArrayList<>();
            for (int i = 1; i <= MaxLevel; i++) {
                names.add(message(lang, "Level") + " " + i);
            }
            if (withLink) {
                names.add(message(lang, "Go"));
            }
            table = new StringTable(names, message(lang, "FunctionsList"));
            map = new HashMap<>();
            List<Menu> menus = menuBar.getMenus();
            for (Menu menu : menus) {
                menu(menu, 0);
            }
            return table;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void menu(Menu menu, int level) {
        makeRow(menu, level);
        for (MenuItem menuItem : menu.getItems()) {
            if (menuItem instanceof Menu) {
                menu((Menu) menuItem, level + 1);
            } else {
                makeRow(menuItem, level + 1);
            }
        }
    }

    public void makeRow(MenuItem menu, int level) {
        String name = menu.getText();
        if (name == null || name.isBlank()) {
            return;
        }
        List<String> row = new ArrayList<>();
        for (int i = 0; i < MaxLevel; i++) {
            row.add("");
        }
        if (withLink) {
            String link;
            if (menu.getOnAction() != null) {
                link = "<a><img src=\"" + goImageFile + "\" "
                        + "onclick=\"alert('" + name + "')\" "
                        + "alt=\"" + message(lang, "Go") + "\"></a>";
                map.put(name, menu);
            } else {
                link = "";
            }
            row.add(link);
        }
        row.set(level, name);
        table.add(row);
    }

    /*
        get
     */
    public static int getMaxLevel() {
        return MaxLevel;
    }

    public StringTable getTable() {
        return table;
    }

    public String getGoImageFile() {
        return goImageFile;
    }

    public Map<String, MenuItem> getMap() {
        return map;
    }

}
