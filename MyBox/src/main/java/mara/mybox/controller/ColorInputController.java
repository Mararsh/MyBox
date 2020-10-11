package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-09-01
 * @License Apache License Version 2.0
 */
public class ColorInputController extends BaseController {

    @FXML
    protected TextArea valuesArea;
    @FXML
    protected Button examplesButton;

    public ColorInputController() {
        baseTitle = AppVariables.message("InputColors");
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            FxmlControl.removeTooltip(examplesButton);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    public void popExamples(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            List<String> values = new ArrayList<>();
            values.addAll(Arrays.asList(
                    "orange", "pink", "lightblue", "wheat",
                    "0xff668840", "0xff6688", "#ff6688", "#f68",
                    "rgb(255,102,136)", "rgb(100%,50%,50%)",
                    "rgba(255,102,136,0.25)", "rgba(255,50%,50%,0.25)",
                    "hsl(240,100%,100%)", "hsla(120,0%,0%,0.25)"
            ));

            MenuItem menu;
            for (String value : values) {
                menu = new MenuItem(value);
                menu.setOnAction((ActionEvent event) -> {
                    valuesArea.appendText(value + "\n");
                });
                popMenu.getItems().add(menu);
            }

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (parentController == null) {
            return;
        }
        final ColorImportController pController = (ColorImportController) parentController;
        pController.inputColors(valuesArea);
        if (saveCloseCheck.isSelected()) {
            closeStage();
        }
    }

    @FXML
    @Override
    public void clearAction() {
        valuesArea.clear();
    }

}
