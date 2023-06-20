package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-13
 * @License Apache License Version 2.0
 */
public class ControlSvgTree extends ControlXmlTree {

    protected SvgEditorController editorController;

    @FXML
    protected ControlSvgNodeEdit svgNodeController;

    @Override
    public void initValues() {
        try {
            super.initValues();

            nodeController = svgNodeController;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    protected void popHelps(Event event) {
        if (UserConfig.getBoolean("SvgHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

    @FXML
    @Override
    protected void showHelps(Event event) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menuItem = new MenuItem(message("SvgTutorial") + " - " + message("English"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.svgEnLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("SvgTutorial") + " - " + message("Chinese"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.svgZhLink(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            menuItem = new MenuItem(message("SvgSpecification"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.svgSpecification(), true);
                }
            });
            items.add(menuItem);

            items.add(new SeparatorMenuItem());

            items.addAll(xmlHelps(event));

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("SvgHelpsPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("SvgHelpsPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
