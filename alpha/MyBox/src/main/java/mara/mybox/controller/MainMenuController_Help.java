package mara.mybox.controller;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Help extends MainMenuController_Development {

    @FXML
    protected void Overview(ActionEvent event) {
        String lang = Languages.getLangName();
        File file = FxFileTools.getInternalFile("/doc/" + lang + "/MyBox-Overview-" + lang + ".pdf",
                "doc", "MyBox-Overview-" + lang + ".pdf");
        if (file != null && file.exists()) {
            PopTools.browseURI(this, file.toURI());
        }
    }

    @FXML
    protected void Shortcuts(ActionEvent event) {
        openStage(Fxmls.ShortcutsFxml);
    }

    @FXML
    protected void FunctionsList(ActionEvent event) {
        openStage(Fxmls.FunctionsListFxml);
    }

    @FXML
    public void documents(ActionEvent event) {
        String lang = Languages.getLangName();
        File file = FxFileTools.getInternalFile("/doc/" + lang + "/MyBox-Documents-" + lang + ".html",
                "doc", "MyBox-Documents-" + lang + ".html");
        if (file != null && file.exists()) {
            PopTools.browseURI(this, file.toURI());
        }
    }

    @FXML
    public void readme(ActionEvent event) {
        HelpTools.readMe(myController);
    }

    @FXML
    protected void showAbout(ActionEvent event) {
        HelpTools.about();
    }

    @FXML
    protected void stories(ActionEvent event) {
        HelpTools.imageStories(parentController);
    }
}
