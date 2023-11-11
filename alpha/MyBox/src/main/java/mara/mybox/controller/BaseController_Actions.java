package mara.mybox.controller;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;
import javafx.scene.robot.Robot;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-27
 * @License Apache License Version 2.0
 */
public abstract class BaseController_Actions extends BaseController_Interface {

    @FXML
    public void link(ActionEvent event) {
        try {
            Hyperlink link = (Hyperlink) event.getSource();
            openLink(link.getText());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void openLink(String address) {
        if (address == null || address.isBlank()) {
            return;
        }
        WebBrowserController.openAddress(address, true);
    }

    public void openHtml(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        WebBrowserController.openFile(file);
    }

    @FXML
    public void openDataPath(ActionEvent event) {
        try {
            browseURI(new File(AppVariables.MyboxDataPath).toURI());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void derbyHelp() {
        openLink(HelpTools.derbyLink());
    }

    @FXML
    public void okAction() {

    }

    @FXML
    public void startAction() {

    }

    @FXML
    public void playAction() {

    }

    @FXML
    public void goAction() {

    }

    @FXML
    public void stopAction() {

    }

    @FXML
    public void createAction() {

    }

    @FXML
    public void addAction() {

    }

    @FXML
    public void addRowsAction() {

    }

    @FXML
    public void deleteRowsAction() {

    }

    @FXML
    public void copyAction() {

    }

    @FXML
    public void copyToSystemClipboard() {

    }

    @FXML
    public void copyToMyBoxClipboard() {

    }

    @FXML
    public void systemClipBoard() {
        if (this instanceof BaseImageController) {
            ImageInSystemClipboardController.oneOpen();
        } else {
            TextInSystemClipboardController.oneOpen();
        }
    }

    @FXML
    public void myBoxClipBoard() {
        if (this instanceof BaseImageController) {
            ImageInMyBoxClipboardController.oneOpen();

        } else if (this instanceof ControlData2DEditTable) {
            Data2DPasteContentInMyBoxClipboardController.open((ControlData2DEditTable) this);

        } else {
            TextInMyBoxClipboardController.oneOpen();
        }
    }

    @FXML
    public void pasteAction() {

    }

    @FXML
    public void pasteContentInSystemClipboard() {

    }

    @FXML
    public void loadContentInSystemClipboard() {

    }

    @FXML
    public void saveAction() {

    }

    @FXML
    public void deleteAction() {

    }

    @FXML
    public void cropAction() {

    }

    @FXML
    public void recoverAction() {

    }

    @FXML
    public void redoAction() {

    }

    @FXML
    public void undoAction() {

    }

    @FXML
    public void allAction() {

    }

    @FXML
    public void clearAction() {

    }

    @FXML
    public void findAction() {

    }

    @FXML
    public void replaceAction() {

    }

    @FXML
    public void cancelAction() {

    }

    @FXML
    public void closeAction() {
        closeStage();
    }

    @FXML
    public void openSourcePath() {
        if (sourceFile != null && sourceFile.exists()) {
            browse(sourceFile.getParentFile());
        } else {
            popError(message("NoFileOpened"));
        }
    }

    @FXML
    public void infoAction() {

    }

    @FXML
    public void setAction() {

    }

    @FXML
    public void selectAllAction() {

    }

    @FXML
    public void selectNoneAction() {

    }

    @FXML
    public void selectAction() {

    }

    @FXML
    public void nextAction() {

    }

    @FXML
    public void previousAction() {

    }

    @FXML
    public void firstAction() {

    }

    @FXML
    public void lastAction() {

    }

    @FXML
    public void pageNextAction() {

    }

    @FXML
    public void pagePreviousAction() {

    }

    @FXML
    public void pageFirstAction() {

    }

    @FXML
    public void pageLastAction() {

    }

    @FXML
    public void refreshAction() {
        if (sourceFile == null) {
            return;
        }
        sourceFileChanged(sourceFile);
    }

    @FXML
    public boolean popAction() {
        return false;
    }

    @FXML
    public boolean menuAction() {
        return false;
    }

    @FXML
    public boolean synchronizeAction() {
        return false;
    }

    @FXML
    public void withdrawAction() {

    }

    @FXML
    public void mybox(ActionEvent event) {
        openStage(Fxmls.MyboxFxml);
    }

    @FXML
    public void systemMethod() {
        if (sourceFile != null && sourceFile.exists()) {
            browse(sourceFile);
        }
    }

    public void clearUserSettings() {
        if (!PopTools.askSure(getTitle(), message("ClearPersonalSettings"), message("SureClear"))) {
            return;
        }
        if (task != null && !task.isQuit()) {
            return;
        }
        task = new SingletonCurrentTask<Void>(myController) {

            @Override
            protected boolean handle() {
                try {
                    UserConfig.clear();
                    return true;
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                reload();
                popSuccessful();
            }
        };
        start(task);
    }

    @FXML
    public void clearExpiredData() {
        ClearExpiredDataController.open(false);
    }

    public void view(File file) {
        if (file != null) {
            ControllerTools.openTarget(file.getAbsolutePath());
        }
    }

    public void browse(File file) {
        try {
            browseURI(file.toURI());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void browse(String url) {
        try {
            browseURI(new URI(url));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void browseURI(URI uri) {
        PopTools.browseURI(myController, uri);
    }

    public LoadingController handling() {
        return handling(null, Modality.WINDOW_MODAL, null);
    }

    public LoadingController handling(String info) {
        return handling(null, Modality.WINDOW_MODAL, info);
    }

    public LoadingController handling(Task<?> task) {
        return handling(task, Modality.WINDOW_MODAL, null);
    }

    public LoadingController handling(Task<?> task, String info) {
        return handling(task, Modality.WINDOW_MODAL, info);
    }

    public LoadingController handling(Task<?> task, Modality block, String info) {
        try {
            LoadingController controller = (LoadingController) WindowTools.handling(getMyWindow(), Fxmls.LoadingFxml);
            controller.init(task);
            if (info != null) {
                controller.setInfo(info);
            }
            controller.parentController = myController;

            if (task != null) {
                if (task instanceof SingletonTask) {
                    SingletonTask sTask = (SingletonTask) task;
                    sTask.setController(myController);
                    sTask.setSelf(sTask);
                    sTask.setLoading(controller);
                }
                task.setOnSucceeded((WorkerStateEvent event) -> {
                    controller.closeStage();
                });
                task.setOnCancelled((WorkerStateEvent event) -> {
                    popInformation(message("Canceled"));
                    controller.closeStage();
                });
                task.setOnFailed((WorkerStateEvent event) -> {
                    popError(message("Error"));
                    controller.closeStage();
                });
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public LoadingController start(Task<?> task) {
        return start(task, true, null);
    }

    public LoadingController start(Task<?> task, String info) {
        return start(task, true, info);
    }

    public LoadingController start(Task<?> task, boolean handling) {
        return start(task, handling, null);
    }

    public LoadingController start(Task<?> task, boolean handling, String info) {
        LoadingController controller = null;
        if (handling) {
            controller = handling(task, info);
        } else if (task instanceof SingletonTask) {
            SingletonTask sTask = (SingletonTask) task;
            sTask.setController(myController);
            sTask.setSelf(sTask);
            sTask.setLoading(controller);
        }
        Thread thread = new Thread(task);
        thread.setDaemon(false);
        thread.start();
        return controller;
    }

    public void start(Task<?> task, Node node) {
        if (task instanceof SingletonTask) {
            SingletonTask sTask = (SingletonTask) task;
            sTask.setController(myController);
            sTask.setSelf(sTask);
            if (node != null) {
                sTask.setDisableNode(node);
            }
        }
        Thread thread = new Thread(task);
        thread.setDaemon(false);
        thread.start();
    }

    public void multipleFilesGenerated(final List<String> fileNames) {
        try {
            if (fileNames == null || fileNames.isEmpty()) {
                return;
            }
            String path = new File(fileNames.get(0)).getParent();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(getMyStage().getTitle());
            String info = MessageFormat.format(message("GeneratedFilesResult"),
                    fileNames.size(), "\"" + path + "\"");
            int num = fileNames.size();
            if (num > 10) {
                num = 10;
            }
            for (int i = 0; i < num; ++i) {
                info += "\n    " + fileNames.get(i);
            }
            if (fileNames.size() > num) {
                info += "\n    ......";
            }
            alert.setContentText(info);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonOpen = new ButtonType(message("OpenTargetPath"));
            ButtonType buttonBrowse = new ButtonType(message("Browse"));
            ButtonType buttonClose = new ButtonType(message("Close"));
            alert.getButtonTypes().setAll(buttonBrowse, buttonOpen, buttonClose);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            stage.sizeToScene();

            Optional<ButtonType> result = alert.showAndWait();
            if (result == null || !result.isPresent()) {
                return;
            }
            if (result.get() == buttonOpen) {
                browseURI(new File(path).toURI());
                recordFileOpened(path);
            } else if (result.get() == buttonBrowse) {
                final ImagesBrowserController controller = ImagesBrowserController.open();
                if (controller != null) {
                    controller.loadFiles(fileNames);
                }
            }

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }

    }

    @FXML
    protected void popHtmlHelps(Event event) {
        if (UserConfig.getBoolean("HtmlHelpsPopWhenMouseHovering", false)) {
            showHtmlHelps(event);
        }
    }

    @FXML
    protected void showHtmlHelps(Event event) {
        popEventMenu(event, HelpTools.htmlHelps(true));
    }

    public void popEventMenu(Event event, List<MenuItem> menuItems) {
        if (event == null || menuItems == null || menuItems.isEmpty()) {
            return;
        }
        popNodeMenu((Node) event.getSource(), menuItems);
    }

    public void popNodeMenu(Node node, List<MenuItem> menuItems) {
        if (node == null || menuItems == null || menuItems.isEmpty()) {
            return;
        }
        Robot robot = new Robot();
        popMenu(node, menuItems, robot.getMouseX() - 20, robot.getMouseY() + 10);
    }

    public void popCenterMenu(Node node, List<MenuItem> menuItems) {
        if (node == null || menuItems == null || menuItems.isEmpty()) {
            return;
        }
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        popMenu(node, menuItems,
                bounds.getMinX() + bounds.getWidth() / 2,
                bounds.getMinY() + bounds.getHeight() / 2);
    }

    public void popMenu(Node node, List<MenuItem> menuItems, double x, double y) {
        if (node == null || menuItems == null || menuItems.isEmpty()) {
            return;
        }
        List<MenuItem> items = new ArrayList<>();
        items.addAll(menuItems);

        MenuItem menu = new MenuItem(message("PopupClose"), StyleTools.getIconImageView("iconCancel.png"));
//        menu.setStyle("-fx-text-fill: #2e598a;");
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

        popMenu.show(node, x, y);
    }

}
