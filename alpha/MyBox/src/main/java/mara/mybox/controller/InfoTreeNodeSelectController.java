package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import mara.mybox.db.data.InfoNode;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-9-26
 * @License Apache License Version 2.0
 */
public class InfoTreeNodeSelectController extends BaseInfoTreeHandleController {

    protected SimpleBooleanProperty notify;

    public InfoTreeNodeSelectController() {
        baseTitle = message("SelectNode");
    }

    public void setParameters(String categroy) {
        this.manager = null;
        handlerController.setParameters(this, categroy);
        notify = new SimpleBooleanProperty();
    }

    @FXML
    @Override
    public void okAction() {
        InfoNode node = selected();
        if (node == null) {
            popError(message("SelectToHandle"));
            return;
        }
        notify.set(!notify.get());
    }

    @FXML
    public void dataAction() {
        InfoTreeManageController c = InfoNode.openManager(handlerController.category);
        c.getMyStage().setAlwaysOnTop(true);
    }

    public SimpleBooleanProperty getNotify() {
        return notify;
    }

    public void setNotify(SimpleBooleanProperty notify) {
        this.notify = notify;
    }

    @Override
    public void cleanPane() {
        try {
            notify = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static methods
     */
    public static InfoTreeNodeSelectController open(BaseController parent, String categroy) {
        InfoTreeNodeSelectController controller = (InfoTreeNodeSelectController) WindowTools.childStage(
                parent, Fxmls.InfoTreeNodeSelectFxml);
        controller.setParameters(categroy);
        controller.requestMouse();
        return controller;
    }

}
