package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.db.data.TreeLeaf;
import mara.mybox.db.data.TreeNode;
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
        category = TreeNode.Notebook;
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
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void editAction() {
        if (!checkBeforeNextAction()) {
            return;
        }
        editorController.editNote(tableView.getSelectionModel().getSelectedItem());
    }

    @FXML
    @Override
    protected void addLeaf() {
        if (!checkBeforeNextAction()) {
            return;
        }
        editorController.addNote();
    }

    @FXML
    @Override
    protected void copyLeaf() {
        editorController.copyNote();
    }

    @FXML
    @Override
    protected void recoverLeaf() {
        editorController.recoverNote();
    }

    @Override
    public TreeLeaf pickCurrentLeaf() {
        TreeLeaf leaf = super.pickCurrentLeaf();
        if (leaf == null) {
            return null;
        }
        leaf.setValue(editorController.currentHtml(true));
        return leaf;
    }

    @Override
    public boolean leafChanged() {
        return editorController.fileChanged || leafController.leafChanged;
    }

    @Override
    public void afterSaved() {
        editorController.updateFileStatus(false);
        super.afterSaved();
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
