package mara.mybox.controller;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.FileEditInformation.Edit_Type;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-12-09
 * @License Apache License Version 2.0
 *
 * BaseFileEditorController < BaseFileEditorController_Left <
 * BaseFileEditorController_Actions < BaseFileEditorController_File <
 * BaseFileEditorController_Main < BaseFileEditorController_Pair <
 * BaseFileEditorController_Base
 */
public abstract class BaseFileEditorController extends BaseFileEditorController_Left {

    public BaseFileEditorController() {
        baseTitle = message("FileEditer");
    }

    public BaseFileEditorController(Edit_Type editType) {
        baseTitle = message("FileEditer");
        if (null != editType) {
            switch (editType) {
                case Text:
                    setTextType();
                    break;
                case Markdown:
                    setMarkdownType();
                    break;
                case Bytes:
                    setBytesType();
                    break;
                default:
                    break;
            }
        }
    }

    public final void setTextType() {
        editType = Edit_Type.Text;
        defaultPageSize = 200;

        setFileType(VisitHistory.FileType.Text);
    }

    public final void setBytesType() {
        editType = Edit_Type.Bytes;
        defaultPageSize = 50000;

        setFileType(VisitHistory.FileType.All);
    }

    public final void setMarkdownType() {
        editType = Edit_Type.Markdown;
        defaultPageSize = 200;

        setFileType(VisitHistory.FileType.Markdown);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            if (findReplaceController != null) {
                findReplaceController.setEditor(this);
            }
            initPage(null);

            initFormatTab();
            initSaveTab();
            initBackupsTab();
            initSaveAsTab();
            initLocateTab();
            initFilterTab();
            initFindTab();
            initMainBox();
            initPairBox();
            initPageBar();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            if (findReplaceController != null) {
                if (sourceInformation != null && sourceInformation.getEditType() == Edit_Type.Bytes) {
                    NodeStyleTools.setTooltip(findReplaceController.tipsView, new Tooltip(message("FindReplaceBytesTips")));
                } else {
                    NodeStyleTools.setTooltip(findReplaceController.tipsView, new Tooltip(message("FindReplaceTextsTips")));
                }
            }

            if (charsetSelector != null) {
                NodeStyleTools.setTooltip(charsetSelector, new Tooltip(message("EncodeComments")));
            }
            if (targetBomCheck != null) {
                NodeStyleTools.setTooltip(targetBomCheck, new Tooltip(message("BOMcomments")));
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean controlAltFilter(KeyEvent event) {
        if (event.getCode() == null) {
            return false;
        }
        if (findReplaceController != null && findPane != null) {
            switch (event.getCode()) {
                case DIGIT1:
                case DIGIT2:
                case F:
                case H:
                case W:
                    if (leftPaneControl != null) {
                        showLeftPane();
                    }
                    if (findPane.isExpanded()) {
                        findReplaceController.keyEventsFilter(event);
                    } else {
                        findPane.setExpanded(true);
                        findReplaceController.findArea.requestFocus();
                    }
                    return true;
            }
        }
        return super.controlAltFilter(event);
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (isPop || fileChanged == null || !fileChanged.getValue()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("NeedSaveBeforeAction"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(message("Save"));
            ButtonType buttonNotSave = new ButtonType(message("NotSave"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent()) {
                return false;
            }
            if (result.get() == buttonSave) {
                saveAction();
                return false;
            } else if (result.get() == buttonNotSave) {
                if (fileChanged != null) {
                    fileChanged.set(false);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void cleanPane() {
        try {
            if (autoSaveTimer != null) {
                autoSaveTimer.cancel();
                autoSaveTimer = null;
            }

        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
