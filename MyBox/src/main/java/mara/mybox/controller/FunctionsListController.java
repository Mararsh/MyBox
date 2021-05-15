package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-5-14
 * @License Apache License Version 2.0
 */
public class FunctionsListController extends BaseController {

    protected int index;

    @FXML
    protected GridPane gridPane;

    public FunctionsListController() {
        baseTitle = message("FunctionsList");
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();
            display();

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public void display() {
        try {
            index = 0;
            List<Menu> menus = mainMenuController.menuBar.getMenus();
            for (Menu menu : menus) {
                menu(menu, 1);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            indent += "  ";
        }
        Label label = new Label(indent + name);
        label.setWrapText(true);
        VBox.setVgrow(label, Priority.NEVER);
        HBox.setHgrow(label, Priority.ALWAYS);

        if (menu.getOnAction() != null) {
            Button button = new Button();
            ImageView view = new ImageView(ControlStyle.getIcon("iconGo.png"));
            view.setFitWidth(AppVariables.iconSize);
            view.setFitHeight(AppVariables.iconSize);
            button.setGraphic(view);
            FxmlControl.setTooltip(button, new Tooltip(message("Go")));

            button.setOnMouseClicked((MouseEvent event) -> {
                menu.fire();
            });
            gridPane.addRow(index++, label, button);
        } else {
            gridPane.addRow(index++, label);
        }

    }

}
