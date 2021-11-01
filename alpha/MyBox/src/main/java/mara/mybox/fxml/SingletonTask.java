package mara.mybox.fxml;

import mara.mybox.controller.BaseController;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class SingletonTask<Void> extends BaseTask<Void> {

    BaseController controller;

    public SingletonTask(BaseController controller) {
        this.controller = controller;
    }

    @Override
    protected void whenSucceeded() {
        if (controller != null) {
            controller.popSuccessful();
        }
    }

    @Override
    protected void whenFailed() {
        if (isCancelled()) {
            return;
        }
        if (controller != null) {
            if (error != null) {
                controller.popError(error);
            } else {
                controller.popFailed();
            }
        }
    }

    @Override
    protected void finalAction() {
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

}
