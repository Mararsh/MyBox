package mara.mybox.controller;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.table.TableUserConf;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

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
            MyBoxLog.error(e.toString());
        }
    }

    public void openLink(String address) {
        if (address == null || address.isBlank()) {
            return;
        }
        WebBrowserController.oneOpen(address, true);
    }

    public void openLink(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        WebBrowserController.oneOpen(file);
    }

    @FXML
    public void openDataPath(ActionEvent event) {
        try {
            browseURI(new File(AppVariables.MyboxDataPath).toURI());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void derbyHelp() {
        openLink("http://db.apache.org/derby/docs/10.15/ref/index.html");
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

        } else if (this instanceof ControlData2D) {
            DataClipboardPopController.open((ControlData2D) this);

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

    public void clearUserSettings() {
        if (!PopTools.askSure(getBaseTitle(), Languages.message("ClearPersonalSettings"), Languages.message("SureClear"))) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(myController) {

                @Override
                protected boolean handle() {
                    try {
                        new TableUserConf().clear();
                        AppVariables.initAppVaribles();
                        return true;
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
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
    }

    public void view(File file) {
        ControllerTools.openTarget(null, file.getAbsolutePath());
    }

    public void view(String file) {
        ControllerTools.openTarget(null, file);
    }

    public void browse(String url) {
        try {
            browseURI(new URI(url));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void browseURI(URI uri) {
        PopTools.browseURI(uri);
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
                task.setOnSucceeded((WorkerStateEvent event) -> {
                    controller.closeStage();
                });
                task.setOnCancelled((WorkerStateEvent event) -> {
                    popInformation(Languages.message("Canceled"));
                    controller.closeStage();
                });
                task.setOnFailed((WorkerStateEvent event) -> {
                    popError(Languages.message("Error"));
                    controller.closeStage();
                });
            }
            return controller;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
        }
        if (task instanceof SingletonTask) {
            SingletonTask sTask = (SingletonTask) task;
            sTask.setController(myController);
            sTask.setSelf(sTask);
        }
        Thread thread = new Thread(task);
        thread.setDaemon(false);
        thread.start();
        return controller;
    }

    public void multipleFilesGenerated(final List<String> fileNames) {
        try {
            if (fileNames == null || fileNames.isEmpty()) {
                return;
            }
            String path = new File(fileNames.get(0)).getParent();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(getMyStage().getTitle());
            String info = MessageFormat.format(Languages.message("GeneratedFilesResult"),
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
            ButtonType buttonOpen = new ButtonType(Languages.message("OpenTargetPath"));
            ButtonType buttonBrowse = new ButtonType(Languages.message("Browse"));
            ButtonType buttonBrowseNew = new ButtonType(Languages.message("BrowseInNew"));
            ButtonType buttonClose = new ButtonType(Languages.message("Close"));
            alert.getButtonTypes().setAll(buttonBrowseNew, buttonBrowse, buttonOpen, buttonClose);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            stage.sizeToScene();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonOpen) {
                browseURI(new File(path).toURI());
                recordFileOpened(path);
            } else if (result.get() == buttonBrowse) {
                final ImagesBrowserController controller = ControllerTools.openImagesBrowser(getMyStage());
                if (controller != null) {
                    controller.loadFiles(fileNames);
                }
            } else if (result.get() == buttonBrowseNew) {
                final ImagesBrowserController controller = ControllerTools.openImagesBrowser(null);
                if (controller != null) {
                    controller.loadFiles(fileNames);
                }
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public void dataChanged() {

    }

    // pick coordinate from outside
    public void setCoordinate(double longitude, double latitude) {
    }

    // pick GeographyCode from outside
    public void setGeographyCode(GeographyCode code) {
    }

}
