package mara.mybox.fxml.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import static mara.mybox.fxml.style.NodeStyleTools.attributeTextStyle;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2025-12-9
 * @License Apache License Version 2.0
 */
public class MenuTools {

    public static List<MenuItem> toolsMenu(BaseController controller) {
        Menu documentMenu = new Menu(message("Document"));
        documentMenu.getItems().addAll(DocumentToolsMenu.menusList(controller));

        Menu imageMenu = new Menu(message("Image"));
        imageMenu.getItems().addAll(ImageToolsMenu.menusList(controller));

        Menu fileMenu = new Menu(message("File"));
        fileMenu.getItems().addAll(FileToolsMenu.menusList(controller));

        Menu networkMenu = new Menu(message("Network"));
        networkMenu.getItems().addAll(NetworkToolsMenu.menusList(controller));

        Menu dataMenu = new Menu(message("Data"));
        dataMenu.getItems().addAll(DataToolsMenu.menusList(controller));

        Menu mediaMenu = new Menu(message("Media"));
        mediaMenu.getItems().addAll(MediaToolsMenu.menusList(controller));

        MenuItem mainMenu = new MenuItem(message("MainPageShortcut"));
        mainMenu.setOnAction((ActionEvent event) -> {
            controller.openStage(Fxmls.MyboxFxml);
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(documentMenu, imageMenu, fileMenu,
                networkMenu, dataMenu, mediaMenu, new SeparatorMenuItem(),
                mainMenu));

        return items;
    }

    public static CheckMenuItem popCheckMenu(String name) {
        if (name == null) {
            return null;
        }
        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"),
                StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(name + "MenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean(name + "MenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        return popItem;
    }

    public static boolean isPopMenu(String name) {
        return isPopMenu(name, true);
    }

    public static boolean isPopMenu(String name, boolean init) {
        if (name == null) {
            return false;
        }
        return UserConfig.getBoolean(name + "MenuPopWhenMouseHovering", init);
    }

    public static List<MenuItem> initMenu(String name) {
        return initMenu(name, true);
    }

    public static List<MenuItem> initMenu(String name, boolean fix) {
        List<MenuItem> items = new ArrayList<>();
        if (name == null || name.isBlank()) {
            return items;
        }

        MenuItem menu = new MenuItem(fix ? StringTools.menuPrefix(name) : name);
        menu.setStyle(attributeTextStyle());
        items.add(menu);
        items.add(new SeparatorMenuItem());

        return items;
    }

}
