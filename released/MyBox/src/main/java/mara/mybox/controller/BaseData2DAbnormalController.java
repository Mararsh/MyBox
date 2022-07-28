package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import mara.mybox.db.data.Data2DStyle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.RowFilter;

/**
 * @Author Mara
 * @CreateDate 2022-7-8
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DAbnormalController extends BaseController {

    protected ControlData2DEditTable tableController;
    protected Data2DStyle currentStyle;
    protected ChangeListener<Boolean> tableStatusListener;
    protected RowFilter rowFilter;

    @FXML
    protected ControlData2DAbnormalList listController;
    @FXML
    protected Label idLabel;

    @Override
    public void initValues() {
        try {
            super.initValues();

            rightPaneControl = listController.rightPaneControl;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(ControlData2DEditTable tableController) {
        try {
            this.tableController = tableController;
            listController.setParameters(this);

            tableStatusListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    sourceChanged();
                }
            };
            tableController.statusNotify.addListener(tableStatusListener);

            sourceChanged();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void sourceChanged() {
        try {
            if (tableController == null) {
                return;
            }
            getMyStage().setTitle(baseTitle + " - " + tableController.data2D.displayName());

            listController.sourceChanged();

            loadStyle(currentStyle);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadNull() {
        currentStyle = new Data2DStyle();
    }

    public void loadStyle(Data2DStyle style) {
        if (style == null) {
            loadNull();
            return;
        }
        currentStyle = style;
    }

    public void reloadDataPage() {
        if (tableController == null || !tableController.checkBeforeNextAction()) {
            return;
        }
        tableController.dataController.goPage();
        tableController.requestMouse();
    }

    @FXML
    public void dataAction() {
        Data2DMarkAbnormalController.open(tableController);
    }

    @FXML
    @Override
    public void cancelAction() {
        close();
    }

    @Override
    public void cleanPane() {
        try {
            tableController.statusNotify.removeListener(tableStatusListener);
            tableStatusListener = null;
            tableController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
