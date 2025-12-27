package mara.mybox.fxml.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-12-9
 * @License Apache License Version 2.0
 */
public class HelpMenu {

    public static List<MenuItem> menusList(BaseController controller) {
        MenuItem Overview = new MenuItem(message("Overview"));
        Overview.setOnAction((ActionEvent event) -> {
            String lang = Languages.embedFileLang();
            File file = FxFileTools.getInternalFile("/doc/" + lang + "/MyBox-Overview-" + lang + ".pdf",
                    "doc", "MyBox-Overview-" + lang + ".pdf");
            if (file != null && file.exists()) {
                PopTools.browseURI(controller, file.toURI());
            }
        });

        MenuItem Shortcuts = new MenuItem(message("Shortcuts"));
        Shortcuts.setOnAction((ActionEvent event) -> {
            controller.openStage(Fxmls.ShortcutsFxml);
        });

        MenuItem FunctionsList = new MenuItem(message("FunctionsList"));
        FunctionsList.setOnAction((ActionEvent event) -> {
            controller.openStage(Fxmls.FunctionsListFxml);
        });

        MenuItem InterfaceTips = new MenuItem(message("InterfaceTips"));
        InterfaceTips.setOnAction((ActionEvent event) -> {
            controller.openHtml(HelpTools.makeInterfaceTips(AppVariables.CurrentLangName));
        });

        MenuItem AboutTreeInformation = new MenuItem(message("AboutTreeInformation"));
        AboutTreeInformation.setOnAction((ActionEvent event) -> {
            controller.openHtml(HelpTools.aboutTreeInformation());
        });

        MenuItem AboutImageScope = new MenuItem(message("AboutImageScope"));
        AboutImageScope.setOnAction((ActionEvent event) -> {
            controller.openHtml(HelpTools.aboutImageScope());
        });

        MenuItem AboutData2D = new MenuItem(message("AboutData2D"));
        AboutData2D.setOnAction((ActionEvent event) -> {
            controller.openHtml(HelpTools.aboutData2D());
        });

        MenuItem AboutRowExpression = new MenuItem(message("AboutRowExpression"));
        AboutRowExpression.setOnAction((ActionEvent event) -> {
            controller.openHtml(HelpTools.aboutRowExpression());
        });

        MenuItem AboutGroupingRows = new MenuItem(message("AboutGroupingRows"));
        AboutGroupingRows.setOnAction((ActionEvent event) -> {
            controller.openHtml(HelpTools.aboutGroupingRows());
        });

        MenuItem AboutDataAnalysis = new MenuItem(message("AboutDataAnalysis"));
        AboutDataAnalysis.setOnAction((ActionEvent event) -> {
            controller.openHtml(HelpTools.aboutDataAnalysis());
        });

        MenuItem AboutCoordinateSystem = new MenuItem(message("AboutCoordinateSystem"));
        AboutCoordinateSystem.setOnAction((ActionEvent event) -> {
            controller.openHtml(HelpTools.aboutCoordinateSystem());
        });

        MenuItem AboutColor = new MenuItem(message("AboutColor"));
        AboutColor.setOnAction((ActionEvent event) -> {
            controller.openHtml(HelpTools.aboutColor());
        });

        MenuItem AboutMedia = new MenuItem(message("AboutMedia"));
        AboutMedia.setOnAction((ActionEvent event) -> {
            controller.openHtml(HelpTools.aboutMedia());
        });

        MenuItem AboutMacro = new MenuItem(message("AboutMacro"));
        AboutMacro.setOnAction((ActionEvent event) -> {
            controller.openHtml(HelpTools.aboutMacro());
        });

        MenuItem SomeLinks = new MenuItem(message("SomeLinks"));
        SomeLinks.setOnAction((ActionEvent event) -> {
            controller.openHtml(HelpTools.usefulLinks(AppVariables.CurrentLangName));
        });

        MenuItem imagesStories = new MenuItem(message("StoriesOfImages"));
        imagesStories.setOnAction((ActionEvent event) -> {
            HelpTools.imageStories(controller);
        });

        MenuItem ReadMe = new MenuItem(message("ReadMe"));
        ReadMe.setOnAction((ActionEvent event) -> {
            HelpTools.readMe(controller);
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(Overview, Shortcuts, FunctionsList, new SeparatorMenuItem(),
                InterfaceTips, AboutTreeInformation, AboutImageScope,
                AboutData2D, AboutRowExpression, AboutGroupingRows, AboutDataAnalysis,
                AboutCoordinateSystem, AboutColor, AboutMedia, AboutMacro,
                SomeLinks, imagesStories, ReadMe));

        return items;
    }

}
