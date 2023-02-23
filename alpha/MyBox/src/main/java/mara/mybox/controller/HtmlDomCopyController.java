package mara.mybox.controller;

import javafx.collections.ObservableList;
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
    protected Element targetElement;
    protected int targetIndex, count;
    protected TreeTableView<HtmlNode> sourceTree, targetTree;

    @FXML
    protected ControlHtmlDomSource sourceController;
    @FXML
    protected ControlHtmlDomTarget targetController;
    @FXML
    protected RadioButton nodeAndDescendantsRadio, descendantsRadio, nodeRadio;

    public HtmlDomCopyController() {
        baseTitle = message("CopyNodes");
    }

    public void setParamters(ControlHtmlEditor editor, int rowIndex) {
        try {
            this.editor = editor;
            if (invalidTarget()) {
                return;
            }
            Element root = editor.domController.domTree.getRoot().getValue().getElement();
            sourceController.load(root, rowIndex);
            targetController.load(root);

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
            targetIndex = targetTree.getSelectionModel().getSelectedIndex();
            if (targetIndex < 0) {
                popError(message("SelectNodeCopyInto"));
                return false;
            }
            targetElement = targetTree.getTreeItem(targetIndex).getValue().getElement();
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
                try {
                    return checkCopy(sourceTree.getRoot());
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                if (count > 0) {
                    editor.domController.load(targetTree.getRoot().getValue().getElement().html());
                    editor.domChanged(true);
                    editor.domController.domTree.scrollTo(targetIndex);
                    closeStage();
                }
                editor.popInformation(message("Copied") + ": " + count);
            }
        };
        start(task);
    }

    public boolean checkCopy(TreeItem<HtmlNode> sourceItem) {
        try {
            if (task == null || task.isCancelled()) {
                return true;
            }
            HtmlNode node = sourceItem.getValue();
            if (node.getSelected().get()) {
                Element sourceElement = node.getElement();
                String html;
                if (nodeAndDescendantsRadio.isSelected()) {
                    html = sourceElement.outerHtml();

                } else if (descendantsRadio.isSelected()) {
                    html = sourceElement.html();

                } else {
                    html = sourceElement.shallowClone().html();
                }
                if (targetController.beforeRadio.isSelected()) {
                    targetElement.before(html);

                } else if (targetController.afterRadio.isSelected()) {
                    targetElement.after(html);

                } else {
                    targetElement.append(html);

                }
                count++;
            }
            ObservableList<TreeItem<HtmlNode>> children = sourceItem.getChildren();
            if (children != null) {
                for (TreeItem<HtmlNode> child : children) {
                    checkCopy(child);
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    /*
        static methods
     */
    public static HtmlDomCopyController open(ControlHtmlEditor editor, int rowIndex) {
        if (editor == null) {
            return null;
        }
        HtmlDomCopyController controller = (HtmlDomCopyController) WindowTools.openChildStage(
                editor.getMyWindow(), Fxmls.HtmlDomCopyFxml);
        if (controller != null) {
            controller.setParamters(editor, rowIndex);
            controller.requestMouse();
        }
        return controller;
    }

}
