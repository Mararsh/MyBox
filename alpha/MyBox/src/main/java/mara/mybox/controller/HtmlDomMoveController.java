package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
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
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                return move();
            }

            @Override
            protected void whenSucceeded() {
                if (count > 0) {
                    closeStage();
                    editor.domController.refreshAction();
                    editor.domController.focus(editorItem);
                    editor.domChanged(true);
                }
                editor.popInformation(message("Moved") + ": " + count);
            }
        };
        start(task);
    }

    public boolean move() {
        try {
            count = 0;

            targetLocation = targetController.hierarchyNumber(targetItem);
            if (targetLocation == null) {
                error = message("SelectNodeCopyInto");
                return false;
            }
            MyBoxLog.console(targetLocation);
            editorItem = editor.domController.find(targetLocation);
            if (editorItem == null) {
                error = message("SelectNodeCopyInto");
                return false;
            }

            List<TreeItem<HtmlNode>> selected = sourceController.selected();
            Element editElement = editorItem.getValue().getElement();
            for (TreeItem<HtmlNode> item : selected) {
                String hierarchyNumber = sourceController.hierarchyNumber(item);
                if (hierarchyNumber.contains(targetLocation)) {
                    continue;
                }
                Element selectedElement = item.getValue().getElement();
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
