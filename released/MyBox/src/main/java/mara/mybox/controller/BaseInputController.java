package mara.mybox.controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-10-2
 * @License Apache License Version 2.0
 */
public abstract class BaseInputController extends BaseChildController {

    protected SimpleBooleanProperty notify;

    @FXML
    protected Label titleLabel, commentsLabel;

    public void setParameters(BaseController parent, String title) {
        try {
            parentController = parent;
            if (parent != null) {
                baseName = parent.baseName + "_" + baseName;
                getMyStage().setTitle(parent.getMyStage().getTitle());
            }
            getMyStage().centerOnScreen();
            if (titleLabel != null && title != null) {
                titleLabel.setText(title);
            }

            notify = new SimpleBooleanProperty();

        } catch (Exception e) {
            MyBoxLog.debug(e);

        }
    }

    public void setTitleLabel(String title) {
        if (titleLabel != null) {
            titleLabel.setText(title);
        }
    }

    public void setCommentsLabel(String comments) {
        if (commentsLabel != null) {
            commentsLabel.setText(comments);
        }
    }

    public String getInputString() {
        return null;
    }

    public boolean checkInput() {
        return true;
    }

    @FXML
    @Override
    public void okAction() {
        if (checkInput()) {
            notify.set(!notify.get());
        }
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

}
