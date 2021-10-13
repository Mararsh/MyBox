package mara.mybox.controller;

import java.sql.Connection;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import mara.mybox.db.data.Notebook;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-3-1
 * @License Apache License Version 2.0
 */
public class ControlNotebookSelector extends BaseNodeSelector<Notebook> {

    protected NotesController notesController;
    protected TableNotebook tableNotebook;
    protected TableNote tableNote;

    public ControlNotebookSelector() {
        baseTitle = Languages.message("Notebook");
    }

    public void setParameters(NotesController notesController) {
        this.notesController = notesController;
        this.tableNotebook = notesController.tableNotebook;
        this.tableNote = notesController.tableNote;
        super.setParent(notesController, true);
    }

    public void setCaller(NotesController notesController) {
        this.notesController = notesController;
        this.tableNotebook = notesController.tableNotebook;
        this.tableNote = notesController.tableNote;
        super.setParent(null, false);
        cloneTree(notesController.notebooksController.treeView, treeView, getIgnoreNode());
    }

    @Override
    public String name(Notebook node) {
        return node.getName();
    }

    @Override
    public String display(Notebook node) {
        return node.getName();
    }

    @Override
    public String tooltip(Notebook node) {
        if (node.getDescription() != null && !node.getDescription().isBlank()) {
            return node.getName() + "\n" + node.getDescription();
        } else {
            return null;
        }
    }

    @Override
    public long id(Notebook node) {
        return node.getNbid();
    }

    @Override
    public Notebook dummy() {
        return new Notebook();
    }

    @Override
    public boolean isDummy(Notebook node) {
        return node.getName() == null;
    }

    @Override
    public Notebook root(Connection conn) {
        Notebook root = tableNotebook.checkRoot(conn);
        root.setName(Languages.message("Notebook"));
        return root;
    }

    @Override
    public int size(Connection conn, Notebook root) {
        return tableNotebook.size(conn);
    }

    @Override
    public List<Notebook> children(Connection conn, Notebook node) {
        return tableNotebook.children(conn, node.getNbid());
    }

    @Override
    public List<Notebook> ancestor(Connection conn, Notebook node) {
        return tableNotebook.ancestor(conn, node.getNbid());
    }

    @Override
    public Notebook createNode(Notebook targetNode, String name) {
        Notebook newBook = new Notebook(targetNode.getNbid(), name);
        newBook = tableNotebook.insertData(newBook);
        return newBook;
    }

    @Override
    protected void delete(Connection conn, Notebook node) {
        tableNotebook.deleteData(conn, node);
    }

    @Override
    protected void clearTree(Connection conn, Notebook node) {
        tableNotebook.clear(conn);
    }

    @Override
    protected Notebook rename(Notebook node, String name) {
        node.setName(name);
        return tableNotebook.updateData(node);
    }

    @Override
    protected void nodeChanged(Notebook node) {
        super.nodeChanged(node);
        notesController.bookChanged(node);
    }

    @Override
    protected void copyNode(Boolean onlyContents) {
        TreeItem<Notebook> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || isRoot(selectedItem.getValue())) {
            return;
        }
        String chainName = chainName(selectedItem);
        NotesCopyNotebookController controller = (NotesCopyNotebookController) WindowTools.openStage(Fxmls.NotesCopyNotebookFxml);
        controller.setValues(notesController, selectedItem.getValue(), chainName, onlyContents);
    }

    @FXML
    @Override
    protected void moveNode() {
        TreeItem<Notebook> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue().isRoot()) {
            return;
        }
        String chainName = chainName(selectedItem);
        NotesMoveNotebookController controller = (NotesMoveNotebookController) WindowTools.openStage(Fxmls.NotesMoveNotebookFxml);
        controller.setValues(notesController, selectedItem.getValue(), chainName);
    }

    @FXML
    @Override
    protected void exportNode() {
        NotesExportController exportController = (NotesExportController) WindowTools.openStage(Fxmls.NotesExportFxml);
        exportController.setParameters(notesController);
    }

    @FXML
    protected void importExamples() {
        NotesImportController controller = (NotesImportController) WindowTools.openStage(Fxmls.NotesImportFxml);
        controller.importExamples(notesController);
    }

    @FXML
    protected void importFiles() {
        NotesImportController c = (NotesImportController) WindowTools.openStage(Fxmls.NotesImportFxml);
        c.notesController = notesController;
    }

}
