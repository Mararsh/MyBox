package mara.mybox.fxml.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.Data2DManufactureController;
import mara.mybox.controller.DataTreeController;
import mara.mybox.controller.MyBoxDocumentsController;
import mara.mybox.controller.TextPopController;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.example.SoftwareTesting;
import mara.mybox.db.table.BaseTableTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2025-12-9
 * @License Apache License Version 2.0
 */
public class DevelopmentMenu {

    public static List<MenuItem> menusList(BaseController controller) {
        MenuItem MyBoxProperties = new MenuItem(message("MyBoxProperties"));
        MyBoxProperties.setOnAction((ActionEvent event) -> {
            controller.openStage(Fxmls.MyBoxPropertiesFxml);
        });

        MenuItem MyBoxLogs = new MenuItem(message("MyBoxLogs"));
        MyBoxLogs.setOnAction((ActionEvent event) -> {
            controller.openStage(Fxmls.MyBoxLogsFxml);
        });

        MenuItem RunSystemCommand = new MenuItem(message("RunSystemCommand"));
        RunSystemCommand.setOnAction((ActionEvent event) -> {
            controller.openStage(Fxmls.RunSystemCommandFxml);
        });

        MenuItem JConsole = new MenuItem(message("JConsole"));
        JConsole.setOnAction((ActionEvent event) -> {
            try {
                String cmd = System.getProperty("java.home") + File.separator + "bin" + File.separator + "jconsole";
                if (SystemTools.isWindows()) {
                    cmd += ".exe";
                }
                new ProcessBuilder(cmd).start();
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
        });

        MenuItem MacroCommands = new MenuItem(message("MacroCommands"));
        MacroCommands.setOnAction((ActionEvent event) -> {
            DataTreeController.macroCommands(controller, false);
        });

        MenuItem MyBoxTables = new MenuItem(message("MyBoxTables"));
        MyBoxTables.setOnAction((ActionEvent event) -> {
            controller.openStage(Fxmls.MyBoxTablesFxml);
        });

        MenuItem ManageLanguages = new MenuItem(message("ManageLanguages"));
        ManageLanguages.setOnAction((ActionEvent event) -> {
            controller.openStage(Fxmls.MyBoxLanguagesFxml);
        });

        MenuItem MakeIcons = new MenuItem(message("MakeIcons"));
        MakeIcons.setOnAction((ActionEvent event) -> {
            controller.openStage(Fxmls.MyBoxIconsFxml);
        });

        MenuItem MakeDocuments = new MenuItem(message("MakeDocuments"));
        MakeDocuments.setOnAction((ActionEvent event) -> {
            MyBoxDocumentsController.open();
        });

        MenuItem AutoTesting = new MenuItem(message("AutoTesting"));
        AutoTesting.setOnAction((ActionEvent event) -> {
            controller.openStage(Fxmls.AutoTestingCasesFxml);
        });

        MenuItem AllTableNames = new MenuItem(message("AllTableNames"));
        AllTableNames.setOnAction((ActionEvent event) -> {
            TextPopController.loadText(BaseTableTools.allTableNames());
        });

        MenuItem MyBoxBaseVerificationList = new MenuItem(message("MyBoxBaseVerificationList"));
        MyBoxBaseVerificationList.setOnAction((ActionEvent event) -> {
            DataFileCSV data = SoftwareTesting.MyBoxBaseVerificationList(
                    controller, Languages.getLangName(), false);
            Data2DManufactureController.openDef(data);
        });

        MenuItem MessageAuthor = new MenuItem(message("MessageAuthor"));
        MessageAuthor.setOnAction((ActionEvent event) -> {
            controller.openStage(Fxmls.MessageAuthorFxml);
        });

        List<MenuItem> items = new ArrayList<>();
        items.addAll(Arrays.asList(MyBoxProperties, MyBoxLogs, new SeparatorMenuItem(),
                RunSystemCommand, JConsole, new SeparatorMenuItem(),
                MacroCommands, MyBoxTables, AllTableNames, new SeparatorMenuItem(),
                ManageLanguages, MakeIcons, MakeDocuments,
                AutoTesting, MyBoxBaseVerificationList));

        return items;
    }

}
