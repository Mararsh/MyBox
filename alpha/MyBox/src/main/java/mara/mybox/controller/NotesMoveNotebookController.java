package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import mara.mybox.db.data.Notebook;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-3-9
 * @License Apache License Version 2.0
 */
public class NotesMoveNotebookController extends ControlNotebookSelector {

    protected Notebook sourceBook;

    @FXML
    protected Label sourceLabel;

    public NotesMoveNotebookController() {
        baseTitle = Languages.message("MoveNode");
    }

    public void setValues(NotesController notesController, Notebook sourceBook, String name) {
        this.sourceBook = sourceBook;
        ignoreNode = sourceBook;
        sourceLabel.setText(Languages.message("NotebookMoved") + ":\n" + name);
        setCaller(notesController);
    }

    @Override
    public Notebook getIgnoreNode() {
        return sourceBook;
    }

    @FXML
    @Override
    public void okAction() {
        if (sourceBook == null || sourceBook.isRoot()) {
            return;
        }
        TreeItem<Notebook> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        Notebook targetBook = selectedItem.getValue();
        if (targetBook == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }

            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    sourceBook.setOwner(targetBook.getNbid());
                    tableNotebook.updateData(sourceBook);
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (notesController == null || !notesController.getMyStage().isShowing()) {
                        notesController = NotesController.oneOpen();
                    } else {
                        notesController.bookChanged(sourceBook);
                        notesController.bookChanged(targetBook);
                    }
                    notesController.notebooksController.loadTree(targetBook);
                    notesController.popSuccessful();
                    closeStage();
                }
            };
            handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

}
