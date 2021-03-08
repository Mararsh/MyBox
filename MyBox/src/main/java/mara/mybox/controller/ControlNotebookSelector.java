package mara.mybox.controller;

import java.sql.Connection;
import java.sql.DriverManager;
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
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
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
    protected Notebook root;
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

        root = tableNotebook.checkRoot();
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

    protected void loadTree() {
        loadTree(tableNotebook.size() <= 20);
    }

    protected void loadTree(boolean expand) {
        treeView.setRoot(null);
        if (tableNotebook == null) {
            tableNotebook = new TableNotebook();
        }
        if (root == null) {
            return;
        }
        root.setName(message("Notebook"));
        TreeItem<Notebook> rootNode = new TreeItem(root);
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
                    try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
                        expandChildren(conn, node);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                protected void expandChildren(Connection conn, TreeItem<Notebook> parent) {
                    if (conn == null || parent == null) {
                        return;
                    }
                    Notebook parentBook = parent.getValue();
                    if (parentBook == null) {
                        return;
                    }
                    List<Notebook> childrenBooks = tableNotebook.children(parentBook.getNbid());
                    for (Notebook childBook : childrenBooks) {
                        TreeItem<Notebook> childNode = new TreeItem(childBook);
                        expandChildren(conn, childNode);
                        childNode.setExpanded(true);
                        parent.getChildren().add(childNode);
                    }
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

}
