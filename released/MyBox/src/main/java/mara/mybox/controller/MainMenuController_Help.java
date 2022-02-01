package mara.mybox.controller;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Help extends MainMenuController_Development {

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
        openStage(Fxmls.DocumentsFxml);
    }

    @FXML
    public void readme(ActionEvent event) {
        MarkdownEditorController c = (MarkdownEditorController) openStage(Fxmls.MarkdownEditorFxml);
        String lang = Languages.isChinese() ? "zh" : "en";
        File file = FxFileTools.getInternalFile("/doc/" + lang + "/README.md", "doc", "README-" + lang + ".md");
        c.sourceFileChanged(file);
    }

    @FXML
    protected void showAbout(ActionEvent event) {
        ControllerTools.about();
    }
}
