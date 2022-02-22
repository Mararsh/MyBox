package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import mara.mybox.data.Data2D;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2022-2-21
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DController extends BaseController {

    protected Data2D.Type type;
    protected Data2D data2D;

    @FXML
    protected ControlData2DList listController;
    @FXML
    protected ControlData2D dataController;
    @FXML
    protected ControlData2DLoad loadController;
    @FXML
    protected Label nameLabel;

    public BaseData2DController() {
        TipsLabelKey = "Data2DTips";
        type = Data2DDefinition.Type.Texts;
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            initData();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void initData() {
        try {
            setDataType(type);

            if (listController != null) {
                listController.setParameters(this);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    // subclass should call this
    public void setDataType(Data2D.Type type) {
        try {
            if (dataController != null) {
                dataController.setDataType(this, type);
                data2D = dataController.data2D;
                loadController = dataController.editController.tableController;

            } else if (loadController != null) {
                data2D = Data2D.create(type);
                loadController.setData(data2D);

            }

            if (loadController != null) {
                loadController.dataLabel = nameLabel;
                loadController.baseTitle = baseTitle;
            }
            checkButtons();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (dataController != null) {
                dataController.savedNotify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                        refreshAction();
                    }
                });
            }

            if (listController != null) {
                rightPaneControl = listController.rightPaneControl;
                initRightPaneControl();

            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkButtons() {
        if (saveButton != null) {
            saveButton.setDisable(data2D == null || !data2D.isValid());
        }
        if (recoverButton != null) {
            recoverButton.setDisable(data2D == null || data2D.isTmpData());
        }
    }

    public void loadDef(Data2DDefinition def) {
        if (def == null || loadController == null || !checkBeforeNextAction()) {
            return;
        }
        loadController.loadDef(def);
        data2D = loadController.data2D;
        checkButtons();
    }

    @FXML
    @Override
    public void createAction() {
        if (dataController == null) {
            return;
        }
        dataController.create();
    }

    @FXML
    @Override
    public void recoverAction() {
        if (dataController == null) {
            return;
        }
        dataController.recoverFile();
    }

    @FXML
    public void refreshAction() {
        if (listController == null) {
            return;
        }
        listController.refreshAction();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        if (dataController == null) {
            return;
        }
        dataController.loadContentInSystemClipboard();
    }

    @FXML
    public void editAction() {
        Data2DDefinition.open(data2D);
    }

    @FXML
    @Override
    public void saveAction() {
        if (dataController == null) {
            return;
        }
        dataController.save();
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (dataController != null) {
            return dataController.checkBeforeNextAction();
        } else {
            return true;
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (dataController != null) {
                return dataController.keyEventsFilter(event);
            } else if (loadController != null) {
                return loadController.keyEventsFilter(event);
            }
        }
        return true;
    }

    @Override
    public void myBoxClipBoard() {
        if (dataController != null) {
            dataController.myBoxClipBoard();
        } else if (loadController != null) {
            loadController.myBoxClipBoard();
        }

    }

    @Override
    public void cleanPane() {
        try {
            dataController = null;
            loadController = null;
            data2D = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
