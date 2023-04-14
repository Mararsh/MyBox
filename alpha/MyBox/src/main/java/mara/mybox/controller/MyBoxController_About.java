package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_About extends MyBoxController_Settings {

    @FXML
    public void popAboutMenu(Event event) {
        if (UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true)) {
            showAboutMenu(event);
        }
    }

    @FXML
    protected void showAboutMenu(Event event) {
        MenuItem ReadMe = new MenuItem(Languages.message("ReadMe"));
        ReadMe.setOnAction((ActionEvent event1) -> {
            HelpTools.readMe(myController);
        });

        MenuItem FunctionsList = new MenuItem(Languages.message("FunctionsList"));
        FunctionsList.setOnAction((ActionEvent event1) -> {
            openStage(Fxmls.FunctionsListFxml);
        });

        MenuItem Shortcuts = new MenuItem(Languages.message("Shortcuts"));
        Shortcuts.setOnAction((ActionEvent event1) -> {
            openStage(Fxmls.ShortcutsFxml);
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(ReadMe, FunctionsList, Shortcuts));

        items.add(new SeparatorMenuItem());

        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean("MyBoxHomeMenuPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("MyBoxHomeMenuPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);

        popCenterMenu(aboutBox, items);

    }

}
