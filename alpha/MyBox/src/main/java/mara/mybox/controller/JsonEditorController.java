package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import mara.mybox.data.JsonTreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class JsonEditorController extends BaseDomEditorController {

    @FXML
    protected ControlJsonTree domController;
    @FXML
    protected VBox treeBox;

    public JsonEditorController() {
        baseTitle = message("JsonEditor");
        TipsLabelKey = "JsonEditorTips";
        typeName = "JSON";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.JSON);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            domController.jsonEditor = this;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public String makeBlank() {
        return "{}";
    }

    @Override
    public void openSavedFile(File file) {
        JsonEditorController.open(file);
    }

    @Override
    public void loadDom(String json, boolean updated) {
        if (!tabPane.getTabs().contains(domTab)) {
            return;
        }
        domController.makeTree(json);
        domChanged(updated);
    }

    @Override
    public String textsByDom() {
        return domController.jsonFormatString();
    }

    public void updateNode(TreeItem<JsonTreeNode> item) {
        domChanged(true);
    }

    @Override
    public void clearDom() {
        domController.clearTree();
        domChanged(true);
    }

    @FXML
    @Override
    protected void options() {
        JsonOptionsController.open();
    }

    @Override
    public void domMenuAction() {
        domController.showFunctionsMenu(null);
    }

    @Override
    protected List<MenuItem> helpMenus(Event event) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menuItem = new MenuItem(message("JsonTutorial") + " - " + message("English"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.jsonEnLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("JsonTutorial") + " - " + message("Chinese"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.jsonZhLink(), true);
                }
            });
            items.add(menuItem);

            menuItem = new MenuItem(message("JsonSpecification"));
            menuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    WebBrowserController.openAddress(HelpTools.jsonSpecification(), true);
                }
            });
            items.add(menuItem);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    @Override
    protected void exampleAction() {
        File example = HelpTools.jsonExample(Languages.embedFileLang());
        if (example != null && example.exists()) {
            loadTexts(TextFileTools.readTexts(null, example, Charset.forName("utf-8")));
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (treeBox.isFocused() || treeBox.isFocusWithin()) {
            if (domController.keyEventsFilter(event)) {
                return true;
            }
        }
        if (super.keyEventsFilter(event)) {
            return true;
        }
        return domController.keyEventsFilter(event);
    }

    /*
        static
     */
    public static JsonEditorController load(String json) {
        try {
            JsonEditorController controller = (JsonEditorController) WindowTools.openStage(Fxmls.JsonEditorFxml);
            controller.writePanes(json);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static JsonEditorController open(File file) {
        try {
            JsonEditorController controller = (JsonEditorController) WindowTools.openStage(Fxmls.JsonEditorFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
