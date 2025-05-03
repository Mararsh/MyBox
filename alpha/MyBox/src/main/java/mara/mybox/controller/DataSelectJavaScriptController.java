package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextInputControl;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableNodeJavaScript;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-12-5
 * @License Apache License Version 2.0
 */
public class DataSelectJavaScriptController extends BaseDataSelectController {

    protected TextInputControl scriptInput;

    public void setParameters(TextInputControl input) {
        try {
            if (input == null) {
                close();
                return;
            }
            scriptInput = input;

            initDataTree(new TableNodeJavaScript(), null);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        DataNode node = treeController.selectedValue();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private DataNode savedNode;

            @Override
            protected boolean handle() {
                savedNode = nodeTable.query(node);
                return savedNode != null;
            }

            @Override
            protected void whenSucceeded() {
                scriptInput.replaceText(scriptInput.getSelection(),
                        savedNode.getStringValue("script"));
                close();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static DataSelectJavaScriptController open(BaseController parent, TextInputControl input) {
        DataSelectJavaScriptController controller
                = (DataSelectJavaScriptController) WindowTools.childStage(parent, Fxmls.DataSelectJavaScriptFxml);
        controller.setParameters(input);
        controller.requestMouse();
        return controller;
    }

}
