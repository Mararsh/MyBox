package mara.mybox.fxml;

import javafx.scene.Node;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.LoadingController;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class FxTask<Void> extends BaseTask<Void> {

    protected BaseController controller;
    protected LoadingController loading;
    protected Node disableNode;

    public FxTask() {
    }

    public FxTask(BaseController controller) {
        this.controller = controller;
    }

    @Override
    protected boolean initValues() {
        if (!super.initValues()) {
            return false;
        }
        if (disableNode != null) {
            disableNode.setDisable(true);
        }
        return true;
    }

    public void setInfo(String info) {
        if (info == null || info.isBlank()) {
            return;
        }
        if (loading != null) {
            loading.setInfo(info);
        } else if (controller != null) {
            controller.setInfo(info);
        } else {
            MyBoxLog.console(info);
        }
    }

    @Override
    public void setError(String error) {
        this.error = error;
        if (error == null || error.isBlank()) {
            return;
        }
        if (loading != null) {
            loading.setInfo(error);
        } else if (controller != null) {
            controller.setError(error);
        } else {
            MyBoxLog.error(error);
        }
    }

    public String getInfo() {
        if (loading == null) {
            return null;
        }
        return loading.getInfo();
    }

    @Override
    protected void whenSucceeded() {
        if (isCancelled()) {
            setInfo(message("Cancelled"));
            return;
        }
        if (controller != null) {
            controller.displayInfo(message("Successful"));
        }
    }

    @Override
    protected void whenCanceled() {
        setInfo(message("Cancelled"));
    }

    @Override
    protected void whenFailed() {
        if (isCancelled()) {
            setInfo(message("Cancelled"));
            return;
        }
        if (controller != null) {
            MyBoxLog.console(controller.getClass());
            if (error != null) {
                MyBoxLog.debug(controller.getTitle() + ": " + error);
                if (error.equals(message("Failed"))) {
                    controller.displayError(error);
                } else {
                    if (error.contains("java.sql.SQLDataException: 22003 : [0] DOUBLE")) {
                        error = error + "\n\n" + message("DataOverflow");
                    }
                    controller.alertError(error);
                }
            } else {
                controller.popFailed();
            }
        }
    }

    @Override
    protected void finalAction() {
        super.finalAction();
        controller = null;
        loading = null;
        if (disableNode != null) {
            disableNode.setDisable(false);
        }
    }

    /*
        get/set
     */
    public BaseController getController() {
        return controller;
    }

    public void setController(BaseController controller) {
        this.controller = controller;
    }

    public LoadingController getLoading() {
        return loading;
    }

    public void setLoading(LoadingController loading) {
        this.loading = loading;
    }

    public Node getDisableNode() {
        return disableNode;
    }

    public void setDisableNode(Node disableNode) {
        this.disableNode = disableNode;
    }

}
