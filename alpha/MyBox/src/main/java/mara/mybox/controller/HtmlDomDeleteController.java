package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2023-3-6
 * @License Apache License Version 2.0
 */
public class HtmlDomDeleteController extends BaseChildController {

    protected BaseHtmlFormat editor;
    protected TreeTableView<HtmlNode> sourceTree;
    protected BaseHtmlTreeController manageController;
    protected int count;

    @FXML
    protected ControlHtmlDomSource sourceController;

    public HtmlDomDeleteController() {
        baseTitle = message("DeleteNodes");
    }

    public void setParamters(BaseHtmlFormat editor, TreeItem<HtmlNode> sourceItem) {
        try {
            this.editor = editor;
            if (invalidTarget()) {
                return;
            }
            manageController = editor.domController;
            Element root = manageController.treeView.getRoot().getValue().getElement();
            sourceController.load(root, sourceItem);
            sourceController.setLabel(message("Select"));
            sourceTree = sourceController.treeView;

        } catch (Exception e) {
            MyBoxLog.error(e);
            closeStage();
        }
    }

    public boolean invalidTarget() {
        if (editor == null || editor.getMyStage() == null || !editor.getMyStage().isShowing()) {
            popError(message("Invalid"));
            closeStage();
            return true;
        }
        return false;
    }

    public boolean checkParameters() {
        return !invalidTarget();
    }

    @FXML
    @Override
    public void okAction() {
        if (!checkParameters()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return delete();
            }

            @Override
            protected void whenSucceeded() {
                if (count > 0) {
                    closeStage();
                    manageController.refreshAction();
                    editor.domChanged(true);
                }
                editor.popInformation(message("Deleted") + ": " + count);
            }
        };
        start(task);
    }

    protected boolean delete() {
        try {
            count = 0;
            List<TreeItem<HtmlNode>> sourcesItems = sourceController.selectedItems();
            for (TreeItem<HtmlNode> sourceItem : sourcesItems) {
                String sourceNumber = sourceController.hierarchyNumber(sourceItem);
                TreeItem<HtmlNode> manageItem = manageController.findSequenceNumber(sourceNumber);
                Element selectedElement = manageItem.getValue().getElement();
                selectedElement.remove();
                count++;
            }
            return true;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }


    /*
        static methods
     */
    public static HtmlDomDeleteController open(BaseHtmlFormat editor, TreeItem<HtmlNode> sourceItem) {
        if (editor == null) {
            return null;
        }
        HtmlDomDeleteController controller = (HtmlDomDeleteController) WindowTools.childStage(
                editor, Fxmls.HtmlDomDeleteFxml);
        if (controller != null) {
            controller.setParamters(editor, sourceItem);
            controller.requestMouse();
        }
        return controller;
    }

}
