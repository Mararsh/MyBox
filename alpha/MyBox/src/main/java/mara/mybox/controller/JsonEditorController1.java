package mara.mybox.controller;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import mara.mybox.data.HtmlNode;
import mara.mybox.data.JsonDomNode;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.cell.TreeTableHierachyCell;
import mara.mybox.fxml.cell.TreeTableTextTrimCell;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.jsoup.nodes.Element;

/**
 * @Author Mara
 * @CreateDate 2018-7-31
 * @License Apache License Version 2.0
 */
public class JsonEditorController1 extends BaseController {
    
    protected boolean fileChanged;
    protected String title;
    
    @FXML
    protected Tab domTab, backupTab;
    @FXML
    protected TreeTableView<JsonDomNode> domTree;
    @FXML
    protected TreeTableColumn<JsonDomNode, String> hierarchyColumn, nameColumn, valueColumn;
    @FXML
    protected ControlFileBackup backupController;
    
    public JsonEditorController1() {
        baseTitle = message("JsonEditor");
    }
    
    @Override
    public void initValues() {
        try {
            super.initValues();

//            domController.setEditor(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.JSON);
    }
    
    @Override
    public void initControls() {
        try {
            super.initControls();
            
            hierarchyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
            hierarchyColumn.setCellFactory(new TreeTableHierachyCell());
            nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
            nameColumn.setCellFactory(new TreeTableTextTrimCell());
            valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));
            valueColumn.setCellFactory(new TreeTableTextTrimCell());
            
            domTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            domTree.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (popMenu != null && popMenu.isShowing()) {
                        popMenu.hide();
                    }
                    TreeItem<JsonDomNode> item = selected();
                    if (event.getButton() == MouseButton.SECONDARY) {
                        popNodeMenu(domTree, makeFunctionsMenu(item));
                    } else {
                        treeClicked(event, item);
                    }
                }
            });
            
            backupController.setParameters(this, baseName);
            
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }
    
    @Override
    public void sourceFileChanged(final File file) {
        sourceFile = file;
        fileChanged = false;
        loadDom(file);
        if (backupController != null) {
            backupController.loadBackups(sourceFile);
        }
    }
    
    public TreeItem<JsonDomNode> loadDom(File jsonFile) {
        if (jsonFile == null) {
            clearAction();
            return null;
        }
        try (JsonParser jParser = new JsonFactory().createParser(jsonFile)) {
            while (jParser.nextToken() != JsonToken.END_OBJECT) {
                String name = jParser.getCurrentName();
                Object value = jParser.getCurrentValue();
                MyBoxLog.console(name + " " + value);
            }
            
            while (jParser.nextToken() != JsonToken.END_OBJECT) {
                String name = jParser.getCurrentName();
                Object value = jParser.getCurrentValue();
                MyBoxLog.console(name + " " + value);
            }
            return domTree.getRoot();
            
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }
    
    @FXML
    @Override
    public void refreshAction() {
        fileChanged = false;
        
    }

    /*
        file
     */
    @FXML
    @Override
    public void saveAction() {
        if (task != null && !task.isQuit()) {
            return;
        }
        if (sourceFile == null) {
            targetFile = chooseSaveFile();
        } else {
            targetFile = sourceFile;
        }
        if (targetFile == null) {
            return;
        }
//        String html = currentJSON(true);
//        if (html == null || html.isBlank()) {
//            popError(message("NoData"));
//            return;
//        }
        task = new SingletonTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
//                    File tmpFile = HtmlWriteTools.writeHtml(html);
//                    if (tmpFile == null || !tmpFile.exists()) {
//                        return false;
//                    }
                    if (sourceFile != null && backupController != null && backupController.needBackup()) {
                        backupController.addBackup(task, sourceFile);
                    }
//                    return FileTools.rename(tmpFile, targetFile);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }
            
            @Override
            protected void whenSucceeded() {
                popSaved();
                recordFileWritten(targetFile);
                fileChanged = false;
                sourceFileChanged(targetFile);
            }
        };
        start(task);
    }
    
    @FXML
    @Override
    public void createAction() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            clearAction();
            getMyStage().setTitle(getBaseTitle());
            sourceFile = null;
            fileChanged = false;
            if (backupController != null) {
                backupController.loadBackups(null);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }
    
    public void updateStageTitle() {
        if (getMyStage() == null) {
            return;
        }
        if (fileChanged) {
            myStage.setTitle(myStage.getTitle() + " *");
        }
    }
    
    protected void updateFileStatus(boolean changed) {
        fileChanged = changed;
        updateStageTitle();
    }

    /*
        dom
     */
    public TreeItem<JsonDomNode> selected() {
        TreeItem<JsonDomNode> item = domTree.getSelectionModel().getSelectedItem();
        return validItem(item);
    }
    
    public TreeItem<JsonDomNode> validItem(TreeItem<JsonDomNode> item) {
        TreeItem<JsonDomNode> validItem = item;
        if (validItem == null) {
            validItem = domTree.getRoot();
        }
        if (validItem == null) {
            return null;
        }
        JsonDomNode node = validItem.getValue();
//        if (node == null || node.getElement() == null) {
//            return null;
//        }
        return validItem;
    }
    
    public String hierarchyNumber(TreeItem<JsonDomNode> item) {
        if (item == null) {
            return "";
        }
        TreeItem<JsonDomNode> parent = item.getParent();
        if (parent == null) {
            return "";
        }
        String p = hierarchyNumber(parent);
        return (p == null || p.isBlank() ? "" : p + ".") + (parent.getChildren().indexOf(item) + 1);
    }
    
    public String tag(TreeItem<JsonDomNode> item) {
        try {
            return item.getValue().getName();
        } catch (Exception e) {
            return null;
        }
    }
    
    public String label(TreeItem<JsonDomNode> item) {
        if (item == null) {
            return "";
        }
        return hierarchyNumber(item) + " " + tag(item);
    }
    
    public void treeClicked(MouseEvent event, TreeItem<JsonDomNode> item) {
        
    }
    
    @FXML
    public void popFunctionsMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "DomFunctionsPopWhenMouseHovering", true)) {
            showFunctionsMenu(event);
        }
    }
    
    @FXML
    public void showFunctionsMenu(Event event) {
        List<MenuItem> items = makeFunctionsMenu(selected());
        
        CheckMenuItem popItem = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        popItem.setSelected(UserConfig.getBoolean(baseName + "DomFunctionsPopWhenMouseHovering", true));
        popItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                UserConfig.setBoolean(baseName + "DomFunctionsPopWhenMouseHovering", popItem.isSelected());
            }
        });
        items.add(popItem);
        
        if (event == null) {
            popNodeMenu(domTree, items);
        } else {
            popEventMenu(event, items);
        }
    }
    
    public List<MenuItem> makeFunctionsMenu(TreeItem<JsonDomNode> inItem) {
        TreeItem<JsonDomNode> item = validItem(inItem);
        if (item == null) {
            return null;
        }
        List<MenuItem> items = new ArrayList<>();
        MenuItem menuItem = new MenuItem(StringTools.menuPrefix(label(item)));
        menuItem.setStyle("-fx-text-fill: #2e598a;");
        items.add(menuItem);
        items.add(new SeparatorMenuItem());
        
        menuItem = new MenuItem(message("Unfold"), StyleTools.getIconImageView("iconPlus.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
//            setExpanded(item, true);
        });
        items.add(menuItem);
        
        menuItem = new MenuItem(message("Fold"), StyleTools.getIconImageView("iconMinus.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
//            setExpanded(item, false);
        });
        items.add(menuItem);
        
        menuItem = new MenuItem(message("Refresh"), StyleTools.getIconImageView("iconRefresh.png"));
        menuItem.setOnAction((ActionEvent menuItemEvent) -> {
//            updateTreeItem(item);
        });
        items.add(menuItem);
        
        items.add(new SeparatorMenuItem());

//        items.addAll(viewMenu(item));
//
//        List<MenuItem> more = moreMenu(item);
//        if (more != null) {
//            items.addAll(more);
//
//        }
        return items;
    }
    
    @FXML
    @Override
    public void clearAction() {
        domTree.setRoot(null);
    }

    /*
        edit
     */
    public void editNode(TreeItem<HtmlNode> item) {
//        currentItem = item;
//        if (currentItem == null) {
//            clearNode();
//            return;
//        }
//        Element element = currentItem.getValue().getElement();
//        nodeController.load(element);
//        setCodes();
//        tabPane.setDisable(false);
    }
    
    @FXML
    public void recoverNode() {
//        nodeController.recover();
    }
    
    @FXML
    public void okNode() {
//        if (currentItem == null) {
//            return;
//        }
//        okNode(nodeController.pickValues());
    }
    
    public void okNode(Element element) {
//        if (currentItem == null || element == null) {
//            return;
//        }
//        updateTreeItem(currentItem, element);
//        editNode(currentItem);
//        htmlEditor.domChanged(true);
//        htmlEditor.popInformation(message("UpdateSuccessfully"));
    }

    /*
        panes
     */
    @Override
    public boolean checkBeforeNextAction() {
        if (isPop || !fileChanged) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(message("FileChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSave = new ButtonType(message("Save"));
            ButtonType buttonNotSave = new ButtonType(message("NotSave"));
            ButtonType buttonCancel = new ButtonType(message("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent()) {
                return false;
            }
            if (result.get() == buttonSave) {
                saveAction();
                return false;
            } else {
                return result.get() == buttonNotSave;
            }
        }
    }

    /*
        static
     */
    public static JsonEditorController1 load(File file) {
        try {
            JsonEditorController1 controller = (JsonEditorController1) WindowTools.openStage(Fxmls.JsonEditorFxml);
            controller.sourceFileChanged(file);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }
    
}
