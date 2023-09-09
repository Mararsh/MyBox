package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.db.data.InfoNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-2-28
 * @License Apache License Version 2.0
 */
public class NotesController extends TreeManageController {

    @FXML
    protected NoteEditor editorController;

    public NotesController() {
        baseTitle = Languages.message("Notes");
        TipsLabelKey = "NotesComments";
        category = InfoNode.Notebook;
        nameMsg = message("Title");
        valueMsg = message("Html");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initControls() {
        try {
            editorController.setParameters(this);

            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void editNode(InfoNode node) {
        if (!checkBeforeNextAction()) {
            return;
        }
        editorController.editNote(node);
    }

    @FXML
    @Override
    protected void recoverNode() {
        editorController.recoverNote();
    }

    @Override
    public boolean isNodeChanged() {
        return editorController.fileChanged || nodeController.nodeChanged;
    }

    @Override
    public void nodeSaved() {
        super.nodeSaved();
        editorController.updateFileStatus(false);
    }

    @Override
    public void newNodeSaved() {
        super.newNodeSaved();
        editorController.updateFileStatus(false);
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (editorController.keyEventsFilter(event)) {
            return true;
        }
        return super.keyEventsFilter(event);
    }

    /*
        static methods
     */
    public static NotesController oneOpen() {
        NotesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof NotesController) {
                try {
                    controller = (NotesController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (NotesController) WindowTools.openStage(Fxmls.NotesFxml);
        }
        controller.requestMouse();
        return controller;
    }

}
