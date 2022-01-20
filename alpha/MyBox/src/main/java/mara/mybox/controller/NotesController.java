package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNoteTag;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.db.table.TableTag;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-2-28
 * @License Apache License Version 2.0
 *
 * NotesController < NotesController_Notes < NotesController_Tags <
 * NotesController_Notebooks < NotesController_Base < BaseDataTableController
 *
 */
public class NotesController extends NotesController_Notes {

    public NotesController() {
        baseTitle = Languages.message("Notes");
        TipsLabelKey = "NotesComments";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void setTableDefinition() {
        tableNotebook = new TableNotebook();
        tableNote = new TableNote();
        tableTag = new TableTag();
        tableNoteTag = new TableNoteTag();
        tableDefinition = tableNote;
        notebooksController.selectedNode = null;

    }

    @Override
    public void initControls() {
        try {
            initNotebooks();
            initTimes();
            initTags();
            initNotes();

            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            if (tableNotebook.size() < 2
                    && PopTools.askSure(this,getBaseTitle(), Languages.message("ImportExamples"))) {
                notebooksController.importExamples();
            } else {
                loadBooks();
            }
            refreshTags();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            return noteEditorController.keyEventsFilter(event);
        }
        return true;
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
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (NotesController) WindowTools.openStage(Fxmls.NotesFxml);
        }
        return controller;
    }

}
