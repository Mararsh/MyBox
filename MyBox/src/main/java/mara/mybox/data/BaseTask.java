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

    protected long startTime, endTime, cost;
    protected boolean ok;
    protected String error;

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
            if (!handle() || isCancelled()) {
                return null;
            }
        } catch (Exception e) {
            error = e.toString();
        }
        return null;
    }

    protected boolean initValues() {
        startTime = new Date().getTime();
        return true;
    }

    protected boolean handle() {
        return true;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        cost = new Date().getTime() - startTime;
        taskQuit();
        Platform.runLater(() -> {
            if (ok) {
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
        endTime = new Date().getTime();
    }

    /*
        get/set
     */
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
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

}
