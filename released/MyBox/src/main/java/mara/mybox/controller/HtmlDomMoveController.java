package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2023-2-18
 * @License Apache License Version 2.0
 */
public class HtmlDomMoveController extends HtmlDomCopyController {

    public HtmlDomMoveController() {
        baseTitle = message("MoveNodes");
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
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                return move();
            }

            @Override
            protected void whenSucceeded() {
                if (count > 0) {
                    closeStage();
                    editor.domController.refreshAction();
                    editor.domController.focusItem(editorItem);
                    editor.domChanged(true);
                }
                editor.popInformation(message("Moved") + ": " + count);
            }
        };
        start(task);
    }

    public static boolean isEqualOrSubNode(String node1, String node2) {
        if (node1 == null || node1.isBlank() || node2 == null || node2.isBlank()) {
            return false;
        }
        String name1 = node1 + ".";
        String name2 = node2 + ".";
        return name1.equals(name2) || name1.startsWith(name2);
    }

    public boolean move() {
        try {
            count = 0;

            String targetNumber = targetController.hierarchyNumber(targetItem);
            if (targetNumber == null) {
                error = message("SelectNodeCopyInto");
                return false;
            }
            editorItem = manageController.findSequenceNumber(targetNumber);
            if (editorItem == null) {
                error = message("SelectNodeCopyInto");
                return false;
            }
            List<TreeItem<HtmlNode>> sourcesItems = sourceController.selectedItems();
            Element editElement = editorItem.getValue().getElement();
            List<TreeItem<HtmlNode>> manageItems = new ArrayList<>();
            for (TreeItem<HtmlNode> sourceItem : sourcesItems) {
                String sourceNumber = sourceController.hierarchyNumber(sourceItem);
                if (isEqualOrSubNode(targetNumber, sourceNumber)) {
                    continue;
                }
                TreeItem<HtmlNode> manageItem = manageController.findSequenceNumber(sourceNumber);
                manageItems.add(manageItem);
            }
            for (TreeItem<HtmlNode> manageItem : manageItems) {
                Element selectedElement = manageItem.getValue().getElement();
                if (targetController.beforeRadio.isSelected()) {
                    editElement.before(selectedElement);

                } else if (targetController.afterRadio.isSelected()) {
                    editElement.after(selectedElement);
                    editElement = selectedElement;

                } else {
                    editElement.appendChild(selectedElement);

                }
                count++;
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
    public static HtmlDomMoveController open(ControlHtmlEditor editor, TreeItem<HtmlNode> sourceItem) {
        if (editor == null) {
            return null;
        }
        HtmlDomMoveController controller = (HtmlDomMoveController) WindowTools.openChildStage(
                editor.getMyWindow(), Fxmls.HtmlDomMoveFxml);
        if (controller != null) {
            controller.setParamters(editor, sourceItem);
            controller.requestMouse();
        }
        return controller;
    }

}
