package mara.mybox.fxml;

import mara.mybox.controller.BaseController;
import mara.mybox.controller.LoadingController;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class SingletonTask<Void> extends BaseTask<Void> {

    protected BaseController controller;
    protected LoadingController loading;

    public SingletonTask(BaseController controller) {
        this.controller = controller;
    }

    public void setInfo(String info) {
        if (info == null || info.isBlank()) {
            return;
        }
        if (loading != null) {
            loading.setInfo(info);
        }
//        MyBoxLog.console(info);
    }

    @Override
    public void setError(String error) {
        this.error = error;
        setInfo(error);
    }

    @Override
    protected void whenSucceeded() {
        if (controller != null) {
            controller.popSuccessful();
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
            if (error != null) {
                if (error.equals(message("Failed"))) {
                    controller.popError(error);
                } else {
                    if (error.contains("java.sql.SQLDataException: 22003 : [0] DOUBLE")) {
                        error = error + "\n\n" + message("DataOverflow");
                    }
                    controller.alertError(error);
                }
                MyBoxLog.debug(controller.getTitle() + ": " + error);
            } else {
                controller.popFailed();
            }
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

}
