package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Modality;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Notebook;
import static mara.mybox.db.data.Notebook.NotebooksSeparater;
import mara.mybox.db.table.TableNotebook;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-1
 * @License Apache License Version 2.0
 */
public class ControlNotebookSelector extends BaseController {

    protected TableNotebook tableNotebook;
    protected Notebook rootBook, ignoreBook;
    protected SimpleBooleanProperty selected;
    protected boolean expandAll;

    @FXML
    protected TreeView<Notebook> treeView;

    public ControlNotebookSelector() {
        baseTitle = message("Notebook");
    }

    public void setValues(BaseController parent, TableNotebook tableNotebook) {
        parentController = parent;
        this.tableNotebook = tableNotebook;
        this.baseName = parent.baseName;

        rootBook = tableNotebook.checkRoot();
        treeView.setCellFactory(p -> new TreeCell<Notebook>() {
            @Override
            public void updateItem(Notebook item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(item.getName());
                if (item.getDescription() != null && !item.getDescription().isBlank()) {
                    FxmlControl.setTooltip(this, item.getName() + "\n" + item.getDescription());
                }
            }
        });
    }

    public void loadTree() {
        loadTree(tableNotebook.size() <= 20);
    }

    public void loadTree(boolean expand) {
        treeView.setRoot(null);
        if (tableNotebook == null) {
            setValues(null, new TableNotebook());
        }
        if (rootBook == null) {
            return;
        }
        rootBook.setName(message("Notebook"));
        TreeItem<Notebook> rootNode = new TreeItem(rootBook);
        rootNode.setExpanded(true);
        treeView.setRoot(rootNode);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        if (expand) {
            expandChildren(rootNode);
        } else {
            loadChildren(rootNode);
        }
    }

    protected void expandChildren(TreeItem<Notebook> node) {
        if (node == null) {
            return;
        }
        node.getChildren().clear();
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        expandChildren(conn, node);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    treeView.refresh();
//                    treeView.getSelectionModel().select(treeView.getRoot());
                }

            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected void expandChildren(Connection conn, TreeItem<Notebook> node) {
        if (conn == null || node == null) {
            return;
        }
        Notebook nodeBook = node.getValue();
        if (nodeBook == null) {
            return;
        }
        List<Notebook> childrenBooks = tableNotebook.children(nodeBook.getNbid());
        for (Notebook childBook : childrenBooks) {
            if (ignoreBook != null && childBook.getNbid() == ignoreBook.getNbid()) {
                continue;
            }
            TreeItem<Notebook> childNode = new TreeItem(childBook);
            expandChildren(conn, childNode);
            childNode.setExpanded(true);
            node.getChildren().add(childNode);
        }
    }

    protected void loadChildren(TreeItem<Notebook> node) {
        if (node == null) {
            return;
        }
        node.getChildren().clear();
        Notebook notebook = node.getValue();
        if (notebook == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                private List<Notebook> books;

                @Override
                protected boolean handle() {
                    books = tableNotebook.children(notebook.getNbid());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (books == null) {
                        return;
                    }
                    Notebook dummy = new Notebook();
                    for (Notebook book : books) {
                        if (ignoreBook != null && book.getNbid() == ignoreBook.getNbid()) {
                            continue;
                        }
                        TreeItem<Notebook> child = new TreeItem(book);
                        node.getChildren().add(child);
                        child.setExpanded(false);
                        child.expandedProperty().addListener(
                                (ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) -> {
                                    if (newVal && !child.isLeaf() && !loaded(child)) {
                                        loadChildren(child);
                                    }
                                });
                        TreeItem<Notebook> dummyItem = new TreeItem(dummy);
                        child.getChildren().add(dummyItem);
                    }
                    treeView.refresh();
                }

                protected boolean loaded(TreeItem<Notebook> item) {
                    if (item == null || item.isLeaf()) {
                        return true;
                    }
                    try {
                        TreeItem<Notebook> child = (TreeItem<Notebook>) (item.getChildren().get(0));
                        return child.getValue().getName() != null;
                    } catch (Exception e) {
                        return true;
                    }
                }

            };
            if (parentController != null) {
                parentController.openHandlingStage(task, Modality.WINDOW_MODAL);
            } else {
                openHandlingStage(task, Modality.WINDOW_MODAL);
            }
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /*
        static methods
     */
    public static List<TreeItem<Notebook>> ancestor(TreeItem<Notebook> node) {
        if (node == null) {
            return null;
        }
        List<TreeItem<Notebook>> ancestor = null;
        TreeItem<Notebook> parent = node.getParent();
        if (parent != null) {
            ancestor = ancestor(parent);
            if (ancestor == null) {
                ancestor = new ArrayList<>();
            }
            ancestor.add(parent);
        }
        return ancestor;
    }

    public static String nodeName(TreeItem<Notebook> node) {
        if (node == null) {
            return null;
        }
        String chainName = "";
        List<TreeItem<Notebook>> ancestor = ancestor(node);
        if (ancestor != null) {
            for (TreeItem<Notebook> a : ancestor) {
                chainName += a.getValue().getName() + NotebooksSeparater;
            }
        }
        chainName += node.getValue().getName();
        return chainName;
    }

    public static void cloneTree(TreeView<Notebook> sourceTreeView, TreeView<Notebook> targetTreeView) {
        if (sourceTreeView == null || targetTreeView == null) {
            return;
        }
        TreeItem<Notebook> sourceRoot = sourceTreeView.getRoot();
        if (sourceRoot == null) {
            return;
        }
        TreeItem<Notebook> targetRoot = new TreeItem(sourceRoot.getValue());
        targetTreeView.setRoot(targetRoot);
        targetRoot.setExpanded(sourceRoot.isExpanded());
        cloneNode(sourceRoot, targetRoot);
        TreeItem<Notebook> selected = sourceTreeView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            select(targetTreeView, selected.getValue());
        }
    }

    public static void cloneNode(TreeItem<Notebook> sourceNode, TreeItem<Notebook> targetNode) {
        if (sourceNode == null || targetNode == null) {
            return;
        }
        List<TreeItem<Notebook>> sourceChildren = sourceNode.getChildren();
        if (sourceChildren == null) {
            return;
        }
        for (TreeItem<Notebook> sourceChild : sourceChildren) {
            TreeItem<Notebook> targetChild = new TreeItem<>(sourceChild.getValue());
            targetChild.setExpanded(sourceChild.isExpanded());
            targetNode.getChildren().add(targetChild);
            cloneNode(sourceChild, targetChild);
        }
    }

    public static void select(TreeView<Notebook> treeView, Notebook notebook) {
        if (treeView == null || notebook == null) {
            return;
        }
        treeView.getSelectionModel().select(find(treeView.getRoot(), notebook.getNbid()));
    }

    public static void select(TreeView<Notebook> treeView, long id) {
        if (treeView == null || id < 1) {
            return;
        }
        treeView.getSelectionModel().select(find(treeView.getRoot(), id));
    }

    public static TreeItem<Notebook> find(TreeItem<Notebook> node, long id) {
        if (node == null || id < 1) {
            return null;
        }
        if (node.getValue().getNbid() == id) {
            return node;
        }
        List<TreeItem<Notebook>> children = node.getChildren();
        if (children == null) {
            return null;
        }
        for (TreeItem<Notebook> child : children) {
            TreeItem<Notebook> find = find(child, id);
            if (find != null) {
                return find;
            }
        }
        return null;
    }

}
