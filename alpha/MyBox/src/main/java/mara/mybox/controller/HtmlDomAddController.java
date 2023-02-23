package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.data.HtmlNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2023-2-18
 * @License Apache License Version 2.0
 */
public class HtmlDomAddController extends BaseChildController {

    protected ControlHtmlEditor editor;
    protected int targetIndex;

    @FXML
    protected ControlHtmlDomNode nodeController;

    public HtmlDomAddController() {
        baseTitle = message("AddNode");
    }

    public void setParamters(ControlHtmlEditor editor, int rowIndex) {
        try {
            this.editor = editor;
            targetIndex = rowIndex;

            nodeController.load(null);

        } catch (Exception e) {
            MyBoxLog.error(e);
            closeStage();
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            Element e = nodeController.pickValues();
            if (e == null) {
                popError(message("Invalid"));
                return;
            }
            closeStage();
            TreeItem<HtmlNode> targetItem = editor.domController.domTree.getTreeItem(targetIndex);
            if (targetItem == null) {
                editor.popError(message("InvalidParameters"));
                return;
            }
            targetItem.getValue().getElement().appendChild(e);
            editor.domController.createTreeNode(targetItem, -1, e);
            editor.domController.domTree.scrollTo(targetIndex);
            editor.popInformation(message("Created"));
        } catch (Exception e) {
            MyBoxLog.error(e);
            closeStage();
        }
    }


    /*
        static methods
     */
    public static HtmlDomAddController open(ControlHtmlEditor editor, int rowIndex) {
        if (editor == null) {
            return null;
        }
        HtmlDomAddController controller = (HtmlDomAddController) WindowTools.openChildStage(
                editor.getMyWindow(), Fxmls.HtmlDomAddFxml);
        if (controller != null) {
            controller.setParamters(editor, rowIndex);
            controller.requestMouse();
        }
        return controller;
    }

}
