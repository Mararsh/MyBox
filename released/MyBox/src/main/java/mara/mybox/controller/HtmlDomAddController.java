package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
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

    protected BaseHtmlFormat editor;
    protected TreeItem<HtmlNode> targetItem;

    @FXML
    protected ControlHtmlDomNode nodeController;
    @FXML
    protected Label hierarchyLabel;

    public HtmlDomAddController() {
        baseTitle = message("AddNode");
    }

    public void setParamters(BaseHtmlFormat editor, TreeItem<HtmlNode> targetItem) {
        try {
            if (targetItem == null) {
                popError(message("SelectToHandle"));
                close();
                return;
            }
            this.editor = editor;
            this.targetItem = targetItem;

            nodeController.load(null);

            hierarchyLabel.setText(editor.domController.label(targetItem));

        } catch (Exception e) {
            MyBoxLog.error(e);
            closeStage();
        }
    }

    @FXML
    @Override
    public void okAction() {
        try {
            Element element = nodeController.pickValues();
            if (element == null) {
                popError(message("Invalid"));
                return;
            }
            closeStage();
            if (targetItem == null) {
                editor.popError(message("InvalidParameters"));
                return;
            }
            editor.domController.addElement(targetItem, element);
            editor.domChanged(true);
            editor.popInformation(message("Created"));
        } catch (Exception e) {
            MyBoxLog.error(e);
            closeStage();
        }
    }


    /*
        static methods
     */
    public static HtmlDomAddController open(BaseHtmlFormat editor, TreeItem<HtmlNode> targetItem) {
        if (editor == null) {
            return null;
        }
        HtmlDomAddController controller = (HtmlDomAddController) WindowTools.childStage(
                editor, Fxmls.HtmlDomAddFxml);
        if (controller != null) {
            controller.setParamters(editor, targetItem);
            controller.requestMouse();
        }
        return controller;
    }

}
