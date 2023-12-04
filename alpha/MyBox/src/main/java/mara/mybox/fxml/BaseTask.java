package mara.mybox.fxml;

import java.util.Date;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import mara.mybox.tools.DateTools;

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
    protected Node disableNode;

    public BaseTask() {
        error = null;
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
        ok = false;
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
        if (disableNode != null) {
            disableNode.setDisable(true);
        }
        return true;
    }

    protected boolean handle() {
        return true;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        taskQuit();
        Platform.runLater(() -> {
            if (isCancelled()) {
                return;
            }
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

    @Override
    protected void failed() {
        super.failed();
        taskQuit();
        Platform.runLater(() -> {
            whenFailed();
            finalAction();
        });

    }

    @Override
    protected void cancelled() {
        super.cancelled();
        taskQuit();
        Platform.runLater(() -> {
            whenCanceled();
            finalAction();
        });
    }

    protected void taskQuit() {
        endTime = new Date();
        if (startTime != null) {
            cost = endTime.getTime() - startTime.getTime();
        }
        self = null;
        quit = true;
    }

    protected void finalAction() {
        if (disableNode != null) {
            disableNode.setDisable(false);
        }
    }

    public String duration() {
        return DateTools.datetimeMsDuration(new Date().getTime() - startTime.getTime());
    }

    public boolean isWorking() {
        return !quit && !isCancelled() && !isDone();
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

    public Node getDisableNode() {
        return disableNode;
    }

    public void setDisableNode(Node disableNode) {
        this.disableNode = disableNode;
    }

}
