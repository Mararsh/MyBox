package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2023-2-18
 * @License Apache License Version 2.0
 */
public class HtmlDomCopyController extends BaseChildController {

    protected ControlHtmlEditor editor;
    protected BaseHtmlDomTreeController manageController;
    protected Element targetElement;
    protected int count;
    protected TreeTableView<HtmlNode> sourceTree, targetTree, editorTree;
    protected TreeItem<HtmlNode> targetItem, editorItem;
    protected Element editorElement;

    @FXML
    protected ControlHtmlDomSource sourceController;
    @FXML
    protected ControlHtmlDomTarget targetController;
    @FXML
    protected RadioButton nodeAndDescendantsRadio, descendantsRadio, nodeRadio;

    public HtmlDomCopyController() {
        baseTitle = message("CopyNodes");
    }

    public void setParamters(ControlHtmlEditor editor, TreeItem<HtmlNode> sourceItem) {
        try {
            this.editor = editor;
            if (invalidTarget()) {
                return;
            }
            manageController = editor.domController;
            editorTree = manageController.domTree;
            Element root = editorTree.getRoot().getValue().getElement();
            sourceController.load(root, sourceItem);
            targetController.loadElement(root);

            sourceTree = sourceController.domTree;
            targetTree = targetController.domTree;

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
        try {
            if (invalidTarget()) {
                return false;
            }
            targetItem = targetTree.getSelectionModel().getSelectedItem();
            if (targetItem == null) {
                popError(message("SelectNodeCopyInto"));
                return false;
            }
            targetElement = targetItem.getValue().getElement();
            count = 0;
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
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
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return makeValues();
            }

            @Override
            protected void whenSucceeded() {
                if (count > 0) {
                    closeStage();
                    manageController.updateTreeItem(editorItem, editorElement);
                    editor.domChanged(true);
                }
                editor.popInformation(message("Copied") + ": " + count);
            }
        };
        start(task);
    }

    protected boolean makeValues() {
        try {
            count = 0;
            editorElement = null;
            String copyiedHtml = null;

            String targetLocation = targetController.hierarchyNumber(targetItem);
            if (targetLocation == null) {
                error = message("SelectNodeCopyInto");
                return false;
            }
            editorItem = manageController.find(targetLocation);
            if (editorItem == null) {
                error = message("SelectNodeCopyInto");
                return false;
            }

            List<TreeItem<HtmlNode>> selected = sourceController.selected();
            for (TreeItem<HtmlNode> item : selected) {
                Element sourceElement = item.getValue().getElement();
                String html;
                if (nodeAndDescendantsRadio.isSelected()) {
                    html = sourceElement.outerHtml();

                } else if (descendantsRadio.isSelected()) {
                    html = sourceElement.html();

                } else {
                    html = sourceElement.shallowClone().html();
                }
                if (copyiedHtml == null) {
                    copyiedHtml = html;
                } else {
                    copyiedHtml += "\n" + html;
                }
                count++;
            }
            if (copyiedHtml == null || copyiedHtml.isBlank()) {
                return false;
            }
            TreeItem<HtmlNode> eParent = editorItem.getParent();
            if (targetController.beforeRadio.isSelected()) {
                targetElement.before(copyiedHtml);
                editorElement = targetElement.parent();
                editorItem = eParent;

            } else if (targetController.afterRadio.isSelected()) {
                targetElement.after(copyiedHtml);
                editorElement = targetElement.parent();
                editorItem = eParent;

            } else {
                targetElement.append(copyiedHtml);
                editorElement = targetElement;

            }
            return editorElement != null;
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }


    /*
        static methods
     */
    public static HtmlDomCopyController open(ControlHtmlEditor editor, TreeItem<HtmlNode> sourceItem) {
        if (editor == null) {
            return null;
        }
        HtmlDomCopyController controller = (HtmlDomCopyController) WindowTools.openChildStage(
                editor.getMyWindow(), Fxmls.HtmlDomCopyFxml);
        if (controller != null) {
            controller.setParamters(editor, sourceItem);
            controller.requestMouse();
        }
        return controller;
    }

}
