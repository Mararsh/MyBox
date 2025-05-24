package mara.mybox.controller;

import javafx.fxml.FXML;
import mara.mybox.db.data.DataNode;
import mara.mybox.db.table.TableNodeImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.image.data.ImageScope;
import mara.mybox.image.tools.ImageScopeTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2024-12-5
 * @License Apache License Version 2.0
 */
public class DataSelectImageScopeController extends BaseDataSelectController {

    protected ControlSelectPixels pixelsController;

    public void setParameters(ControlSelectPixels controller) {
        try {
            if (controller == null) {
                close();
                return;
            }
            pixelsController = controller;

            initDataTree(new TableNodeImageScope(), null);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        DataNode node = selectedNode();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private ImageScope scope;

            @Override
            protected boolean handle() {
                scope = ImageScopeTools.fromDataNode(this, myController, nodeTable.query(node));
                return scope != null;
            }

            @Override
            protected void whenSucceeded() {
                pixelsController.handleController.applyScope(scope);
                close();
            }
        };
        start(task);
    }

    /*
        static methods
     */
    public static DataSelectImageScopeController open(ControlSelectPixels parent) {
        DataSelectImageScopeController controller
                = (DataSelectImageScopeController) WindowTools.childStage(parent, Fxmls.DataSelectImageScopeFxml);
        controller.setParameters(parent);
        controller.requestMouse();
        return controller;
    }

}
