package mara.mybox.data;

import java.util.Date;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * @Author Mara
 * @CreateDate 2019-12-18
 * @License Apache License Version 2.0
 */
public class BaseTask<P> extends Task<P> {

    protected BaseTask self;
    protected Date startTime, endTime;
    protected long cost;
    protected boolean ok, quit;
    protected String error;

    public BaseTask() {
        startTime = new Date();
        ok = quit = false;
        self = this;
    }

    public static BaseTask create() {
        BaseTask task = new BaseTask();
        return task;
    }

    @Override
    protected P call() {
        ok = true;
        if (!initValues()) {
            return null;
        }
        try {
            ok = handle();
        } catch (Exception e) {
            error = e.toString();
            ok = false;
        }
        return null;
    }

    protected boolean initValues() {
        startTime = new Date();
        return true;
    }

    protected boolean handle() {
        return true;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        cost = new Date().getTime() - startTime.getTime();
        taskQuit();
        Platform.runLater(() -> {
            if (isCancelled()) {
                whenCanceled();
            } else if (ok) {
                whenSucceeded();
            } else {
                whenFailed();
            }
            finalAction();
        });
    }

    protected void whenSucceeded() {
    }

    protected void whenFailed() {

    }

    protected void whenCanceled() {

    }

    protected void finalAction() {

    }

    @Override
    protected void failed() {
        super.failed();
        whenFailed();
        taskQuit();
        finalAction();
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        whenCanceled();
        taskQuit();
        finalAction();
    }

    protected void taskQuit() {
        endTime = new Date();
        self = null;
        quit = true;
    }

    /*
        get/set
     */
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public BaseTask getSelf() {
        return self;
    }

    public void setSelf(BaseTask self) {
        this.self = self;
    }

    public boolean isQuit() {
        return quit;
    }

    public void setQuit(boolean quit) {
        this.quit = quit;
    }

}
