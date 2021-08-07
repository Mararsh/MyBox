package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.TreeNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.WebFavorite;
import mara.mybox.db.table.TableTree;
import mara.mybox.db.table.TableWebFavorite;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TableImageFileCell;
import mara.mybox.imagefile.ImageFileReaders;

import mara.mybox.tools.IconTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-4-29
 * @License Apache License Version 2.0
 */
public class WebFavoritesController extends BaseDataTableController<WebFavorite> {

    protected TableTree tableTree;
    protected TableWebFavorite tableWebFavorite;
    protected TreeNode treeNode;
    protected String queryLabel;
    protected WebFavorite currentAddress;
    protected TreeNode nodeOfCurrentAddress;

    @FXML
    protected ControlWebFavoriateNodes treeController;
    @FXML
    protected TableColumn<WebFavorite, Long> faidColumn;
    @FXML
    protected TableColumn<WebFavorite, String> titleColumn, iconColumn, addressColumn;
    @FXML
    protected VBox conditionBox;
    @FXML
    protected CheckBox subCheck;
    @FXML
    protected FlowPane namesPane;
    @FXML
    protected TextField idInput, titleInput, addressInput;
    @FXML
    protected Label addressLabel;
    @FXML
    protected ControlFileSelecter iconController;
    @FXML
    protected ImageView iconView;

    public WebFavoritesController() {
        baseTitle = Languages.message("WebFavorites");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void setTableDefinition() {
        tableTree = new TableTree();
        tableWebFavorite = new TableWebFavorite();
        tableDefinition = tableWebFavorite;
    }

    @Override
    protected void initColumns() {
        try {
            faidColumn.setCellValueFactory(new PropertyValueFactory<>("faid"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            iconColumn.setCellValueFactory(new PropertyValueFactory<>("icon"));
            iconColumn.setCellFactory(new TableImageFileCell(20));
            addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            treeController.setParent(this, true);
            super.initControls();

            treeController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    loadAddress(treeController.selectedNode);
                }
            });

            treeController.changedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    addressChanged(treeController.changedNode);
                }
            });

            subCheck.setSelected(UserConfig.getBoolean(baseName + "IncludeSub", false));
            subCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    if (treeController.selectedNode != null) {
                        loadTableData();
                    }
                }
            });

            iconController.isDirectory(false).isSource(true).mustExist(true).permitNull(true)
                    .name(baseName + "Icon", false).type(VisitHistory.FileType.Image);
            iconController.notify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldTab, Boolean newTab) {
                    updateIcon(iconController.text());
                }
            });

            goButton.disableProperty().bind(Bindings.isEmpty(addressInput.textProperty()));

            hideRightPane();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }


    /*
        tree
     */
    protected void loadTree(TreeNode node) {
        if (tableWebFavorite.size() < 1
                && PopTools.askSure(getBaseTitle(), Languages.message("ImportExamples"))) {
            treeController.importExamples();
        } else {
            treeController.loadTree(node);
        }
    }

    protected void clearQuery() {
        treeController.selectedNode = null;
        queryConditions = null;
        queryLabel = null;
        tableData.clear();
        conditionBox.getChildren().clear();
        namesPane.getChildren().clear();
        currentPageStart = 1;
    }

    protected void loadAddress(TreeNode node) {
        clearQuery();
        treeController.selectedNode = node;
        if (node != null) {
            queryConditions = " owner=" + node.getNodeid();
            loadTableData();
        }
    }

    protected void addressChanged(TreeNode node) {
        if (node == null) {
            return;
        }
        makeConditionPane();
        if (nodeOfCurrentAddress != null && node.getNodeid() == nodeOfCurrentAddress.getNodeid()) {
            nodeOfCurrentAddress = node;
            updateNodeOfCurrentAddress();
        }
        if (treeController.selectedNode != null && treeController.selectedNode.getNodeid() == node.getNodeid()) {
            treeController.selectedNode = node;
            makeConditionPane();
        }
    }


    /*
        addresses list
     */
    @Override
    public void postLoadedTableData() {
        super.postLoadedTableData();
        makeConditionPane();
    }

    public void makeConditionPane() {
        conditionBox.getChildren().clear();
        if (treeController.selectedNode == null) {
            conditionBox.applyCss();
            return;
        }
        synchronized (this) {
            SingletonTask bookTask = new SingletonTask<Void>() {
                private List<TreeNode> ancestor;

                @Override
                protected boolean handle() {
                    ancestor = tableTree.ancestor(treeController.selectedNode.getNodeid());
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    List<Node> nodes = new ArrayList<>();
                    if (ancestor != null) {
                        for (TreeNode node : ancestor) {
                            Hyperlink link = new Hyperlink(node.getTitle());
                            link.setWrapText(true);
                            link.setMinHeight(Region.USE_PREF_SIZE);
                            link.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    loadAddress(node);
                                }
                            });
                            nodes.add(link);
                            nodes.add(new Label(">"));
                        }
                    }
                    Label label = new Label(treeController.selectedNode.getTitle());
                    label.setWrapText(true);
                    label.setMinHeight(Region.USE_PREF_SIZE);
                    nodes.add(label);
                    namesPane.getChildren().setAll(nodes);
                    conditionBox.getChildren().setAll(namesPane, subCheck);
                    conditionBox.applyCss();
                }
            };
            bookTask.setSelf(bookTask);
            Thread thread = new Thread(bookTask);
            thread.setDaemon(false);
            thread.start();
        }

    }

    @Override
    public int readDataSize() {
        if (treeController.selectedNode != null && subCheck.isSelected()) {
            return TableWebFavorite.withSubSize(tableTree, treeController.selectedNode.getNodeid());

        } else if (queryConditions != null) {
            return tableWebFavorite.conditionSize(queryConditions);

        } else {
            return 0;
        }

    }

    @Override
    public List<WebFavorite> readPageData() {
        if (treeController.selectedNode != null && subCheck.isSelected()) {
            return tableWebFavorite.withSub(tableTree, treeController.selectedNode.getNodeid(), currentPageStart - 1, currentPageSize);

        } else if (queryConditions != null) {
            return tableWebFavorite.queryConditions(queryConditions, currentPageStart - 1, currentPageSize);

        } else {
            return null;
        }
    }

    @Override
    protected int clearData() {
        if (queryConditions != null) {
            return tableWebFavorite.deleteCondition(queryConditions);

        } else {
            return -1;
        }
    }

    @Override
    protected List<MenuItem> makeTableContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            menu = new MenuItem(Languages.message("Delete"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                deleteAction();
            });
            menu.setDisable(deleteButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(Languages.message("Copy"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                copyAction();
            });
            menu.setDisable(copyButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(Languages.message("Move"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                moveAction();
            });
            menu.setDisable(moveDataButton.isDisabled());
            items.add(menu);

            menu = new MenuItem(Languages.message("Add"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                addAction(null);
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            menu = new MenuItem(Languages.message("Clear"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                clearAction();
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            if (pageNextButton != null && pageNextButton.isVisible() && !pageNextButton.isDisabled()) {
                menu = new MenuItem(Languages.message("NextPage"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pageNextAction();
                });
                items.add(menu);
            }

            if (pagePreviousButton != null && pagePreviousButton.isVisible() && !pagePreviousButton.isDisabled()) {
                menu = new MenuItem(Languages.message("PreviousPage"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pagePreviousAction();
                });
                items.add(menu);
            }

            menu = new MenuItem(Languages.message("Refresh"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                refreshAction();
            });
            items.add(menu);

            return items;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public void itemClicked() {
        editAction(null);
    }

    @Override
    public void itemDoubleClicked() {
        currentAddress = tableView.getSelectionModel().getSelectedItem();
        if (currentAddress != null) {
            WebBrowserController.oneOpen(currentAddress.getAddress());
        }
    }

    @FXML
    @Override
    public void goAction() {
        WebBrowserController.oneOpen(addressInput.getText());
    }

    @Override
    protected int checkSelected() {
        if (isSettingValues) {
            return -1;
        }
        int selection = super.checkSelected();
        deleteButton.setDisable(selection == 0);
        copyButton.setDisable(selection == 0);
        moveDataButton.setDisable(selection == 0);
        selectedLabel.setText(Languages.message("Selected") + ": " + selection);
        return selection;
    }

    @FXML
    @Override
    public void addAction(ActionEvent event) {
        nodeOfCurrentAddress = treeController.selectedNode;
        editAddress(null);
    }

    @FXML
    @Override
    public void editAction(ActionEvent event) {
        editAddress(tableView.getSelectionModel().getSelectedItem());
    }

    @FXML
    @Override
    public void copyAction() {
        WebFavoritesCopyController.oneOpen(this);
    }

    @FXML
    protected void moveAction() {
        WebFavoritesMoveController.oneOpen(this);
    }

    /*
        edit address
     */
    @FXML
    protected void addAddress() {
        editAddress(null);
    }

    protected void editAddress(WebFavorite address) {
        synchronized (this) {
            currentAddress = address;
            if (address != null) {
                idInput.setText(address.getFaid() + "");
                titleInput.setText(address.getTitle());
                addressInput.setText(address.getAddress());
                iconController.input(address.getIcon());
            } else {
                idInput.setText("");
                titleInput.setText("");
                addressInput.setText("");
                iconController.input("");
            }
            updateNodeOfCurrentAddress();
            showRightPane();
        }
    }

    protected void updateIcon(String icon) {
        try {
            iconView.setImage(null);
            if (icon != null) {
                File file = new File(icon);
                if (file.exists()) {
                    BufferedImage image = ImageFileReaders.readImage(file);
                    if (image != null) {
                        iconView.setImage(SwingFXUtils.toFXImage(image, null));
                    }
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    protected void updateNodeOfCurrentAddress() {
        synchronized (this) {
            SingletonTask updateTask = new SingletonTask<Void>() {
                private String chainName;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        if (currentAddress != null) {
                            if (nodeOfCurrentAddress == null || nodeOfCurrentAddress.getNodeid() != currentAddress.getOwner()) {
                                nodeOfCurrentAddress = tableTree.find(conn, currentAddress.getOwner());
                            }
                        }
                        if (nodeOfCurrentAddress == null) {
                            nodeOfCurrentAddress = treeController.root(conn);
                        }
                        if (nodeOfCurrentAddress == null) {
                            return false;
                        }
                        chainName = treeController.chainName(conn, nodeOfCurrentAddress);
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    addressLabel.setText(chainName);
                }
            };
            updateTask.setSelf(updateTask);
            Thread thread = new Thread(updateTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    protected void copyAddress() {
        idInput.setText("");
        titleInput.appendText(" " + Languages.message("Copy"));
        currentAddress = null;
    }

    @FXML
    protected void recoverAddress() {
        editAddress(currentAddress);
    }

    @FXML
    protected void downloadIcon() {
        synchronized (this) {
            String address;
            try {
                URL url = new URL(addressInput.getText());
                address = url.toString();
            } catch (Exception e) {
                popError(Languages.message("InvalidData"));
                return;
            }
            SingletonTask updateTask = new SingletonTask<Void>() {
                private File iconFile;

                @Override
                protected boolean handle() {
                    iconFile = IconTools.readIcon(address, true);
                    return iconFile != null && iconFile.exists();
                }

                @Override
                protected void whenSucceeded() {
                    iconController.input(iconFile.getAbsolutePath());
                }
            };
            updateTask.setSelf(updateTask);
            handling(updateTask);
            Thread thread = new Thread(updateTask);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void saveAction() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            String title = titleInput.getText();
            if (title == null || title.isBlank()) {
                popError(Languages.message("InvalidData") + ": " + Languages.message("Title"));
                return;
            }
            String address = addressInput.getText();
            try {
                URL url = new URL(address);
            } catch (Exception e) {
                popError(Languages.message("InvalidData") + ": " + Languages.message("Address"));
                return;
            }
            task = new SingletonTask<Void>() {
                private WebFavorite data;

                @Override
                protected boolean handle() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        data = new WebFavorite();
                        data.setTitle(title);
                        data.setAddress(address);
                        File icon = iconController.file;
                        if (icon != null && icon.exists()) {
                            data.setIcon(icon.getAbsolutePath());
                        }
                        if (currentAddress != null) {
                            data.setFaid(currentAddress.getFaid());
                            data.setOwner(currentAddress.getOwner());
                            currentAddress = tableWebFavorite.updateData(conn, data);
                        } else {
                            if (nodeOfCurrentAddress == null) {
                                nodeOfCurrentAddress = treeController.root(conn);
                            }
                            data.setOwner(nodeOfCurrentAddress.getNodeid());
                            currentAddress = tableWebFavorite.insertData(conn, data);
                        }
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return currentAddress != null;
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    editAddress(currentAddress);
                    if (treeController.selectedNode != null
                            && currentAddress.getOwner() == treeController.selectedNode.getNodeid()) {
                        refreshAction();
                    }
                }

            };
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    /*
        static methods
     */
    public static WebFavoritesController oneOpen() {
        WebFavoritesController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof WebFavoritesController) {
                try {
                    controller = (WebFavoritesController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (WebFavoritesController) WindowTools.openStage(Fxmls.WebFavoritesFxml);
        }
        return controller;
    }

    public static WebFavoritesController oneOpen(TreeNode node) {
        WebFavoritesController controller = oneOpen();
        if (controller != null) {
            controller.loadTree(node);
        }
        return controller;
    }

}
