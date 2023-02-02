package mara.mybox.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebEvent;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-5-14
 * @License Apache License Version 2.0
 */
public class FunctionsListController extends ControlWebView {

    protected StringTable table;
    protected String goImage;
    protected Map<String, MenuItem> map;

    public FunctionsListController() {
        baseTitle = Languages.message("FunctionsList");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            goImage = FxFileTools.getInternalFile("/" + StyleTools.getIconPath() + "iconGo.png",
                    "icons", "iconGo.png", true).toURI().toString();
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

            table = new StringTable(Languages.message("FunctionsList"));
            map = new HashMap<>();
            List<Menu> menus = mainMenuController.menuBar.getMenus();
            for (Menu menu : menus) {
                menu(menu, 1);
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
        String indent = "";
        for (int i = 0; i < level * 4; i++) {
            indent += "&nbsp;&nbsp;";
        }
        String link;
        if (menu.getOnAction() != null) {
            link = "<a><img src=\"" + goImage + "\" onclick=\"alert('" + name + "')\" alt=\"" + Languages.message("Go") + "\"></a>";
            map.put(name, menu);
        } else {
            link = "";
        }
        table.newNameValueRow(indent + name, link);

    }

}
