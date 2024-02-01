package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
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
public class NotesController extends InfoTreeManageController {

    @FXML
    protected NoteEditor editorController;

    public NotesController() {
        baseTitle = Languages.message("Notes");
        TipsLabelKey = "NotesTips";
        category = InfoNode.Notebook;
        nameMsg = message("Title");
        valueMsg = message("Html");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            editor = editorController;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
