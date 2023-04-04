package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEvent;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-5-14
 * @License Apache License Version 2.0
 */
public class FunctionsListController extends ControlWebView {

    protected StringTable table;
    protected String goImageFile;
    protected Map<String, MenuItem> map;

    public FunctionsListController() {
        baseTitle = message("FunctionsList");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            goImageFile = AppVariables.MyboxDataPath + "/icons/iconGo.png";
            BufferedImage srcImage = SwingFXUtils.fromFXImage(StyleTools.getIconImage("iconGo.png"), null);
            ImageFileWriters.writeImageFile(srcImage, "png", goImageFile);

            goImageFile = new File(goImageFile).toURI().toString();

            webView.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
                @Override
                public void handle(WebEvent<String> ev) {
                    String name = ev.getData();
                    MenuItem menu = map.get(name);
                    if (menu != null) {
                        menu.fire();
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Level") + " 1", message("Level") + " 2",
                    message("Level") + " 3", message("Go")));
            table = new StringTable(names, message("FunctionsList"));
            map = new HashMap<>();
            List<Menu> menus = mainMenuController.menuBar.getMenus();
            for (Menu menu : menus) {
                menu(menu, 0);
            }
            webView.getEngine().loadContent(table.html());

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
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
        String link;
        if (menu.getOnAction() != null) {
            link = "<a><img src=\"" + goImageFile + "\" onclick=\"alert('" + name + "')\" alt=\"" + message("Go") + "\"></a>";
            map.put(name, menu);
        } else {
            link = "";
        }
        List<String> row = new ArrayList<>();
        row.addAll(Arrays.asList("", "", "", link));
        row.set(level, name);
        table.add(row);

    }

}
