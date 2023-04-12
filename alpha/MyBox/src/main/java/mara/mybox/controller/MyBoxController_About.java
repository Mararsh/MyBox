package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import mara.mybox.fxml.HelpTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MyBoxController_About extends MyBoxController_Settings {

    @FXML
    protected void showAboutMenu(MouseEvent event) {
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

        popCenterMenu(aboutBox, items);

    }

}
