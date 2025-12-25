package mara.mybox.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.menu.DevelopmentMenu;
import mara.mybox.fxml.menu.MenuTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-4
 * @License Apache License Version 2.0
 */
public class FunctionsList {

    public static final int MaxLevel = 4;
    protected BaseController controller;
    protected List<MenuItem> menus;
    protected boolean withLink;
    protected int index;
    protected StringTable table;
    protected String goImageFile, lang;
    protected Map<String, MenuItem> map;

    public FunctionsList(BaseController controller, boolean withLink, String lang) {
        this.controller = controller;
        this.withLink = withLink;
        this.lang = lang;
    }

    public StringTable make() {
        try {
            if (withLink) {
                goImageFile = AppVariables.MyboxDataPath + "/icons/iconGo.png";
                BufferedImage srcImage = SwingFXUtils.fromFXImage(StyleTools.getIconImage("iconGo.png"), null);
                ImageFileWriters.writeImageFile(null, srcImage, "png", goImageFile);
                goImageFile = new File(goImageFile).toURI().toString();
            }
            index = 0;
            List<String> names = new ArrayList<>();
            names.add(message(lang, "Index"));
            names.add(message(lang, "HierarchyNumber"));
            for (int i = 1; i <= MaxLevel; i++) {
                names.add(message(lang, "Level") + " " + i);
            }
            if (withLink) {
                names.add(message(lang, "Go"));
            }
            table = new StringTable(names, message(lang, "FunctionsList"));
            map = new HashMap<>();
            menus = MenuTools.toolsMenu(controller);
            Menu devMenu = new Menu(message("Development"));
            devMenu.getItems().addAll(DevelopmentMenu.menusList(controller));
            menus.add(devMenu);
            int number = 0;
            for (MenuItem menu : menus) {
                menu(menu, 0, ++number + "");
            }
            return table;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public void menu(MenuItem menu, int level, String number) {
        try {
            makeRow(menu, level, number);
            if (menu instanceof Menu) {
                int childIndex = 0;
                for (MenuItem menuItem : ((Menu) menu).getItems()) {
                    String childNumber = number + "." + ++childIndex;
                    menu(menuItem, level + 1, childNumber);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void makeRow(MenuItem menu, int level, String number) {
        try {
            String name = menu.getText();
            if (name == null || name.isBlank()) {
                return;
            }
            List<String> row = new ArrayList<>();
            row.add(++index + "");
            row.add(number);
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
            row.set(level + 2, name);
            table.add(row);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
