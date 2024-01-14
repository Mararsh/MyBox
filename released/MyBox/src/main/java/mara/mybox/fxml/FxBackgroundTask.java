package mara.mybox.fxml;

import mara.mybox.controller.BaseController;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class FxBackgroundTask<Void> extends FxTask<Void> {

    public FxBackgroundTask(BaseController controller) {
        this.controller = controller;
    }

    @Override
    protected void taskQuit() {
        if (controller != null && controller.getBackgroundTask() == self) {
            controller.setBackgroundTask(null);
        }
        super.taskQuit();
    }

}
