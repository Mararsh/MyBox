package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import mara.mybox.db.data.Notebook;
import mara.mybox.db.table.TableNote;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlStage;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

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
        baseTitle = message("Notebook");
    }

    public void setParent(NotesController notesController) {
        this.notesController = notesController;
        this.tableNotebook = notesController.tableNotebook;
        this.tableNote = notesController.tableNote;
        super.setParent(notesController, true);
        if (importButton != null) {
            FxmlControl.removeTooltip(importButton);
        }
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
        root.setName(message("Notebook"));
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
        NotesCopyNotebookController controller = (NotesCopyNotebookController) FxmlStage.openStage(CommonValues.NotesCopyNotebookFxml);
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
        NotesMoveNotebookController controller = (NotesMoveNotebookController) FxmlStage.openStage(CommonValues.NotesMoveNotebookFxml);
        controller.setValues(notesController, selectedItem.getValue(), chainName);
    }

    @FXML
    @Override
    protected void exportNode() {
        NotesExportController exportController = (NotesExportController) FxmlStage.openStage(CommonValues.NotesExportFxml);
        exportController.setValues(notesController);
    }

    @FXML
    @Override
    protected void popImportMenu(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu = new MenuItem(message("ImportFiles"));
            menu.setOnAction((ActionEvent event) -> {
                NotesImportController c = (NotesImportController) FxmlStage.openStage(CommonValues.NotesImportFxml);
                c.notesController = notesController;
            });
            items.add(menu);

            menu = new MenuItem(message("ImportExamples"));
            menu.setOnAction((ActionEvent event) -> {
                importExamples();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                if (popMenu != null && popMenu.isShowing()) {
                    popMenu.hide();
                }
                popMenu = null;
            });
            items.add(menu);

            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);
            popMenu.getItems().addAll(items);
            FxmlControl.locateCenter((Region) mouseEvent.getSource(), popMenu);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void importExamples() {
        NotesImportController controller = (NotesImportController) FxmlStage.openStage(CommonValues.NotesImportFxml);
        controller.importExamples(notesController);
    }

}
