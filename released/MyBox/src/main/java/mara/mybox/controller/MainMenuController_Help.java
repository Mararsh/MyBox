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
    public void readme(ActionEvent event) {
        HelpTools.readMe(myController);
    }

    @FXML
    protected void StoriesOfImages(ActionEvent event) {
        HelpTools.imageStories(parentController);
    }

    @FXML
    protected void InterfaceTips(ActionEvent event) {
        openHtml(HelpTools.interfaceTips());
    }

    @FXML
    protected void AboutData2D(ActionEvent event) {
        openHtml(HelpTools.aboutData2D());
    }

    @FXML
    protected void AboutRowExpression(ActionEvent event) {
        openHtml(HelpTools.aboutRowExpression());
    }

    @FXML
    protected void AboutGroupingRows(ActionEvent event) {
        openHtml(HelpTools.aboutGroupingRows());
    }

    @FXML
    protected void AboutDataAnalysis(ActionEvent event) {
        openHtml(HelpTools.aboutDataAnalysis());
    }

    @FXML
    protected void AboutCoordinateSystem(ActionEvent event) {
        openHtml(HelpTools.aboutCoordinateSystem());
    }

    @FXML
    protected void AboutColor(ActionEvent event) {
        openHtml(HelpTools.aboutColor());
    }

    @FXML
    protected void AboutMedia(ActionEvent event) {
        openHtml(HelpTools.aboutMedia());
    }

    @FXML
    protected void SomeLinks(ActionEvent event) {
        openHtml(HelpTools.usefulLinks());
    }

}
