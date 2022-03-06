package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import static mara.mybox.fxml.PopTools.addButtonsPane;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-4
 * @License Apache License Version 2.0
 */
public class RunJdkCommandController extends RunSystemCommandController {

    public RunJdkCommandController() {
        baseTitle = message("RunJdkCommand");
    }

    @Override
    public String example() {
        return "java -version";
    }

    @Override
    public String makeCmd() {
        return System.getProperty("java.home") + File.separator + "bin"
                + File.separator + cmdController.value();
    }

    @FXML
    protected void popExamplesMenu(MouseEvent mouseEvent) {
        PopTools.popSqlExamples(this, cmdController.selector.getEditor(), mouseEvent);

        try {
            TextField input = cmdController.selector.getEditor();
            MenuController controller = MenuController.open(this, cmdController.selector.getEditor(),
                    mouseEvent.getScreenX(), mouseEvent.getScreenY());

            List<Node> topButtons = new ArrayList<>();
            Button clearButton = new Button(message("Clear"));
            clearButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    input.clear();
                }
            });
            topButtons.add(clearButton);
            controller.addFlowPane(topButtons);

            addButtonsPane(controller, input, Arrays.asList(
                    "SELECT * FROM ", " WHERE ", " ORDER BY ", " DESC ", " ASC ",
                    " OFFSET <start> ROWS FETCH NEXT <size> ROWS ONLY"
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " , ", " (   ) ", " >= ", " > ", " <= ", " < ", " != "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " AND ", " OR ", " NOT ", " IS NULL ", " IS NOT NULL "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " LIKE 'a%' ", " LIKE 'a_' ", " BETWEEN <value1> AND <value2>"
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " IN ( <value1>, <value2> ) ", " IN (SELECT FROM <table> WHERE <condition>) "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " EXISTS (SELECT FROM <table> WHERE <condition>) "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " DATE('1998-02-26') ", " TIMESTAMP('1962-09-23 03:23:34.234') "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " COUNT ", " AVG ", " MAX ", " MIN ", " SUM ", " GROUP BY ", " HAVING "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    " JOIN ", " INNER JOIN ", " LEFT OUTER JOIN ", " RIGHT OUTER JOIN ", " CROSS JOIN "
            ));
            addButtonsPane(controller, input, Arrays.asList(
                    "INSERT INTO <table> (column1, column2) VALUES (value1, value2)",
                    "UPDATE <table> SET <column1>=<value1>, <column2>=<value2> WHERE ",
                    "DELETE FROM <table> WHERE ", "TRUNCATE TABLE "
            ));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void popHelpMenu(MouseEvent mouseEvent) {
        try {
            MenuController controller = MenuController.open(this, cmdController.selector,
                    mouseEvent.getScreenX(), mouseEvent.getScreenY());
            controller.addNode(new Label(message("HelpMe")));

            List<String> cmds = new ArrayList<>();
            cmds.addAll(Arrays.asList(
                    "keytool", "jshell", "jrunscript", "jstack", "jcmd",
                    "jinfo"
            ));
            List<Node> valueButtons = new ArrayList<>();
            for (String cmd : cmds) {
                Button button = new Button(cmd);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        RunCommandController.open(myController,
                                System.getProperty("java.home") + File.separator + "bin"
                                + File.separator + cmd + " --help");
                    }
                });
                valueButtons.add(button);
            }
            controller.addFlowPane(valueButtons);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
