package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.db.table.TableNote;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public class NotesTreeController extends BaseDataTreeManageController {

    @FXML
    protected NoteNodeController noteController;

    public NotesTreeController() {
        baseTitle = Languages.message("Notes");
        TipsLabelKey = "NotesTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            dataTable = new TableNote();
            nodeController = noteController;
            treeController = treeManageController;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static methods
     */
    public static NotesTreeController oneOpen() {
        NotesTreeController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof NotesTreeController) {
                try {
                    controller = (NotesTreeController) object;
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (NotesTreeController) WindowTools.openStage(Fxmls.NotesTreeFxml);
        }
        controller.requestMouse();
        return controller;
    }

}
