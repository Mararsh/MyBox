package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Note;
import mara.mybox.db.data.Notebook;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-29
 * @License Apache License Version 2.0
 */
public class NotesCopyNotebookController extends ControlNotebookSelector {

    protected Notebook sourceBook;
    protected boolean onlyContents;

    @FXML
    protected Label sourceLabel;

    public NotesCopyNotebookController() {
        baseTitle = message("Notebook");
    }

    public void setValues(NotesController notesController, Notebook sourceBook, String name, boolean onlyContents) {
        this.sourceBook = sourceBook;
        this.onlyContents = onlyContents;
        sourceLabel.setText(message("NotebookCopyed") + ":\n" + name);
        setValues(notesController);
    }

    @Override
    public Notebook getIgnoreBook() {
        return sourceBook;
    }

    @FXML
    @Override
    public void okAction() {
        if (notesController == null || sourceBook == null || sourceBook.isRoot()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            TreeItem<Notebook> targetNode = treeView.getSelectionModel().getSelectedItem();
            if (targetNode == null) {
                alertError(message("SelectNotebook"));
                return;
            }
            Notebook targetBook = targetNode.getValue();
            if (targetBook == null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (!onlyContents) {
                            Notebook newBook = new Notebook(targetBook.getNbid(), sourceBook.getName())
                                    .setDescription(sourceBook.getDescription());
                            newBook = tableNotebook.insertData(conn, newBook);
                            if (newBook == null) {
                                return false;
                            }
                            ok = copyContents(conn, sourceBook, newBook);
                        } else {
                            ok = copyContents(conn, sourceBook, targetBook);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return ok;
                }

                protected boolean copyContents(Connection conn, Notebook sourceBook, Notebook targetBook) {
                    if (conn == null || sourceBook == null || targetBook == null) {
                        return false;
                    }
                    try {
                        long sourceid = sourceBook.getNbid();
                        long targetid = targetBook.getNbid();
                        List<Note> notes = tableNote.notes(conn, sourceid);
                        if (notes != null) {
                            conn.setAutoCommit(false);
                            for (Note note : notes) {
                                Note newNote = new Note(targetid, note.getTitle(), note.getHtml(), null);
                                tableNote.insertData(conn, newNote);
                            }
                            conn.commit();
                        }
                        conn.setAutoCommit(true);
                        List<Notebook> children = tableNotebook.children(conn, sourceid);
                        if (children != null) {
                            for (Notebook child : children) {
                                Notebook newBook = new Notebook(targetid, child.getName()).setDescription(child.getDescription());
                                newBook = tableNotebook.insertData(conn, newBook);
                                if (newBook == null) {
                                    continue;
                                }
                                copyContents(conn, child, newBook);
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (notesController == null || !notesController.getMyStage().isShowing()) {
                        notesController = NotesController.oneOpen();
                    } else {
                        notesController.refreshBooks();
                        notesController.refreshTimes();
                        notesController.bookChanged(targetBook);
                        if (notesController.selectedBook != null && notesController.selectedBook.getNbid() == targetBook.getNbid()) {
                            notesController.loadTableData();
                        }
                    }
                    notesController.popSuccessful();
                    closeStage();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
