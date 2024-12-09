package mara.mybox.controller;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.TableFileBackup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-27
 * @License Apache License Version 2.0
 */
public abstract class BaseController_Attributes {

    protected BaseController parentController, myController;
    protected FxTask<Void> task, backgroundTask;
    protected int SourceFileType = -1, SourcePathType, TargetFileType, TargetPathType, AddFileType, AddPathType,
            operationType, dpi;
    protected List<FileChooser.ExtensionFilter> sourceExtensionFilter, targetExtensionFilter;
    protected String myFxml, parentFxml, currentStatus, baseTitle, baseName, interfaceName, TipsLabelKey;
    protected Stage myStage;
    protected Scene myScene;
    protected Window myWindow;
    protected Alert loadingAlert;
    protected Timer popupTimer, timer;
    protected Popup popup;
    protected ContextMenu popMenu;
    protected String error, targetFileSuffix;
    protected boolean isSettingValues;
    protected File sourceFile, sourcePath, targetPath, targetFile;
    protected StageType stageType;
    protected SaveAsType saveAsType;
    protected TableFileBackup tableFileBackup;

    public static enum StageType {
        Normal, Branch, Child, Pop, Popup, OneOpen
    }

    public static enum SaveAsType {
        Load, Open, Edit, None
    }

    @FXML
    protected Pane thisPane, mainMenu, operationBar;
    @FXML
    protected MainMenuController mainMenuController;
    @FXML
    protected TextField sourceFileInput, sourcePathInput, targetPrefixInput, statusInput;
    @FXML
    protected OperationController operationBarController;
    @FXML
    protected ControlTargetPath targetPathController;
    @FXML
    protected ControlTargetFile targetFileController;
    @FXML
    protected Button selectFileButton, okButton, startButton, goButton, previewButton, playButton, stopButton,
            editButton, deleteButton, saveButton, cropButton, saveAsButton, undoButton, redoButton,
            clearButton, createButton, cancelButton, addButton, recoverButton, viewButton, popButton,
            copyButton, copyToSystemClipboardButton, copyToMyBoxClipboardButton,
            addRowsButton, deleteRowsButton, selectButton, selectAllButton, selectNoneButton,
            pasteButton, pasteContentInSystemClipboardButton, loadContentInSystemClipboardButton,
            myBoxClipboardButton, systemClipboardButton, operationsButton,
            renameButton, tipsButton, setButton, allButton, menuButton, synchronizeButton,
            firstButton, lastButton, previousButton, nextButton,
            pageFirstButton, pageLastButton, pagePreviousButton, pageNextButton,
            infoButton, metaButton, openSourceButton,
            transparentButton, whiteButton, blackButton, withdrawButton;
    @FXML
    protected VBox paraBox, mainAreaBox;
    @FXML
    protected HBox toolbar;
    @FXML
    protected Label bottomLabel, tipsLabel;
    @FXML
    protected ImageView tipsView, rightTipsView, linksView, leftPaneControl, rightPaneControl;
    @FXML
    protected CheckBox rightPaneCheck, leftPaneCheck, toolbarCheck, onTopCheck, closeAfterCheck;
    @FXML
    protected ToggleGroup saveAsGroup, fileTypeGroup;
    @FXML
    protected RadioButton saveLoadRadio, saveOpenRadio, saveEditRadio, saveJustRadio;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected ScrollPane leftPane, rightPane;
    @FXML
    protected ComboBox<String> dpiSelector;
    @FXML
    protected TabPane tabPane;

    public void setFileType() {
        setFileType(FileType.All);
    }

    public void setFileType(int fileType) {
        setSourceFileType(fileType);
        setTargetFileType(fileType);
    }

    public void setFileType(int sourceType, int targetType) {
        setSourceFileType(sourceType);
        setTargetFileType(targetType);
    }

    public void setSourceFileType(int sourceType) {
        SourceFileType = sourceType;
        SourcePathType = sourceType;
        AddFileType = sourceType;
        AddPathType = sourceType;
        sourceExtensionFilter = VisitHistoryTools.getExtensionFilter(sourceType);
    }

    public void setTargetFileType(int targetType) {
        TargetPathType = targetType;
        TargetFileType = targetType;
        targetExtensionFilter = VisitHistoryTools.getExtensionFilter(targetType);
    }

    public String getBaseTitle() {
        if (baseTitle == null && getMyStage() != null) {
            baseTitle = myStage.getTitle();
        }
        if (baseTitle == null) {
            baseTitle = Languages.message("AppTitle");
        }
        return baseTitle;
    }

    public String getRootBaseTitle() {
        if (getMyStage() != null && myStage.getUserData() != null) {
            return ((BaseController) myStage.getUserData()).getBaseTitle();
        }
        return baseTitle;
    }

    public String getTitle() {
        if (getMyStage() != null) {
            return myStage.getTitle();
        } else {
            return getBaseTitle();
        }
    }

    public void setTitle(String title) {
        if (getMyStage() != null) {
            myStage.setTitle(title);
        }
    }

    public Scene getMyScene() {
        if (myStage != null) {
            myScene = myStage.getScene();
        } else if (thisPane != null) {
            myScene = thisPane.getScene();
        }
        return myScene;
    }

    public Window getMyWindow() {
        if (myStage != null) {
            myWindow = myStage;
        } else if (getMyScene() != null) {
            myWindow = myScene.getWindow();
        }
        return myWindow;
    }

    public Stage getMyStage() {
        if (myStage == null) {
            Window window = getMyWindow();
            if (window != null && window instanceof Stage) {
                myStage = (Stage) window;
                if (myStage.getUserData() == null) {
                    myStage.setUserData(this);
                }
            }
        }
        return myStage;
    }

    public Window getStage() {
        if (getMyWindow() instanceof Popup) {
            return ((Popup) myWindow).getOwnerWindow();
        } else {
            return getMyStage();
        }
    }

    public MenuBar getMainMenu() {
        if (mainMenuController != null) {
            return mainMenuController.menuBar;
        } else {
            return null;
        }
    }

    public boolean isIndependantStage() {
        return getMyStage() != null
                && mainMenuController != null
                && myStage.getOwner() == null
                && stageType != StageType.Branch
                && stageType != StageType.Child
                && stageType != StageType.Pop
                && stageType != StageType.Popup;
    }

    public boolean isPopup() {
        if (stageType == StageType.Pop
                || stageType == StageType.Popup) {
            return true;
        }
        Window win = getMyWindow();
        return win != null && (win instanceof Popup);
    }

    public boolean isChild() {
        if (getMyStage() != null) {
            return myStage.getOwner() != null;
        } else {
            return false;
        }
    }

    public Window owner() {
        if (getMyStage() != null) {
            return myStage.getOwner();
        } else {
            return null;
        }
    }

    public boolean isShowing() {
        if (getMyStage() != null) {
            return myStage.isShowing();
        } else {
            return false;
        }
    }

    @FXML
    public void popTips() {
        String tips = null;
        if (tipsView != null) {
            tips = NodeStyleTools.getTips(tipsView);
        } else if (rightTipsView != null) {
            tips = NodeStyleTools.getTips(rightTipsView);
        }
        if (tips != null && !tips.isBlank()) {
            TextPopController.loadText(tips);
        }
    }

    /*
        get/set
     */
    public void setMyStage(Stage myStage) {
        this.myStage = myStage;
    }

    public void setMyScene(Scene myScene) {
        this.myScene = myScene;
    }

    public Pane getThisPane() {
        return thisPane;
    }

    public String getBaseName() {
        return baseName;
    }

    public String getMyFxml() {
        return myFxml;
    }

    public int getSourceFileType() {
        if (SourceFileType < 0) {
            setFileType();
        }
        return SourceFileType;
    }

    public int getSourcePathType() {
        return SourcePathType;
    }

    public int getTargetFileType() {
        return TargetFileType;
    }

    public int getTargetPathType() {
        return TargetPathType;
    }

    public int getAddFileType() {
        return AddFileType;
    }

    public int getAddPathType() {
        return AddPathType;
    }

    public List<FileChooser.ExtensionFilter> getSourceExtensionFilter() {
        if (sourceExtensionFilter == null) {
            setFileType();
        }
        return sourceExtensionFilter;
    }

    public void setSourceExtensionFilter(List<FileChooser.ExtensionFilter> sourceExtensionFilter) {
        this.sourceExtensionFilter = sourceExtensionFilter;
    }

    public void setParentFxml(String parentFxml) {
        this.parentFxml = parentFxml;
    }

    public BaseController_Attributes getParentController() {
        return parentController;
    }

    public void setParentController(BaseController parentController) {
        this.parentController = parentController;
    }

    public MainMenuController getMainMenuController() {
        return mainMenuController;
    }

    public ContextMenu getPopMenu() {
        return popMenu;
    }

    public void setPopMenu(ContextMenu popMenu) {
        this.popMenu = popMenu;
    }

    public void setPopup(Popup popup) {
        this.popup = popup;
    }

    public Popup getPopup() {
        return popup;
    }

    public Label getBottomLabel() {
        return bottomLabel;
    }

    public void setBottomLabel(Label bottomLabel) {
        this.bottomLabel = bottomLabel;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setMyWindow(Window myWindow) {
        this.myWindow = myWindow;
    }

    public boolean isIsSettingValues() {
        return isSettingValues;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public StageType getStageType() {
        return stageType;
    }

    public void setStageType(StageType stageType) {
        this.stageType = stageType;
    }

    /*
        task
     */
    public boolean taskWorking() {
        return task != null && task.isWorking();
    }

    public boolean taskQuit() {
        return task != null && !task.isWorking();
    }

    public FxTask<Void> getTask() {
        return task;
    }

    public void setTask(FxTask<Void> task) {
        this.task = task;
    }

    public void taskCanceled(Task task) {

    }

    public FxTask<Void> getBackgroundTask() {
        return backgroundTask;
    }

    public void setBackgroundTask(FxTask<Void> backgroundTask) {
        this.backgroundTask = backgroundTask;
    }

    /*
        popup
     */
    public void alertError(String information) {
        PopTools.alertError(myController, information);
    }

    public void alertWarning(String information) {
        PopTools.alertError(myController, information);
    }

    public void alertInformation(String information) {
        PopTools.alertInformation(myController, information);
    }

    public Popup makePopup() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
        popup = new Popup();
        popup.setAutoHide(true);
        return popup;
    }

    public void popText(String text, int duration, String bgcolor, String color, String size, Region attach) {
        try {
            if (popup != null) {
                popup.hide();
            }
            popup = makePopup();
            popup.setAutoFix(true);
            Label popupLabel = new Label(text);
            popupLabel.setStyle("-fx-background-color:" + bgcolor + ";"
                    + " -fx-text-fill: " + color + ";"
                    + " -fx-font-size: " + size + ";"
                    + " -fx-padding: 10px;"
                    + " -fx-background-radius: 6;");
            popup.setAutoFix(true);
            popup.getContent().add(popupLabel);
            popupLabel.setWrapText(true);
            popupLabel.setMinHeight(Region.USE_PREF_SIZE);
            popupLabel.applyCss();

            if (duration > 0) {
                if (popupTimer != null) {
                    popupTimer.cancel();
                }
                popupTimer = getPopupTimer();
                popupTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            closePopup();
                        });
                    }
                }, duration);
            }
            if (attach != null) {
                LocateTools.locateUp(attach, popup);
            } else if (thisPane != null) {
                LocateTools.locateCenter(thisPane, popup);
            } else {
                popup.show(getMyWindow());
            }
        } catch (Exception e) {

        }
    }

    public void popInformation(String text, int duration, String size) {
        popText(text, duration, UserConfig.textBgColor(), UserConfig.infoColor(), size, null);
    }

    public void popInformation(String text, int duration) {
        popInformation(text, duration, UserConfig.textSize());
    }

    public void popInformation(String text, Region attach) {
        popText(text, UserConfig.textDuration(), UserConfig.textBgColor(), UserConfig.infoColor(), UserConfig.textSize(), attach);
    }

    public void popInformation(String text) {
        popInformation(text, UserConfig.textDuration(), UserConfig.textSize());
    }

    public void popSuccessful() {
        popInformation(Languages.message("Successful"));
    }

    public void popSaved() {
        popInformation(Languages.message("Saved"));
    }

    public void popDone() {
        popInformation(Languages.message("Done"));
    }

    public void popError(String text, int duration, String size) {
        popText(text, duration, UserConfig.textBgColor(), UserConfig.errorColor(), size, null);
    }

    public void popError(String text) {
//        MyBoxLog.debug(text);
        popError(text, UserConfig.textDuration(), UserConfig.textSize());
    }

    public void popFailed() {
        popError(Languages.message("Failed"));
    }

    public void popWarn(String text, int duration, String size) {
        popText(text, duration, UserConfig.textBgColor(), UserConfig.warnColor(), size, null);
    }

    public void popWarn(String text, int duration) {
        popWarn(text, duration, UserConfig.textSize());
    }

    public void popWarn(String text) {
        popWarn(text, UserConfig.textDuration(), UserConfig.textSize());
    }

    public void handleInfo(String text, boolean pop) {
        if (this instanceof BaseLogsController) {
            ((BaseLogsController) this).updateLogs(text);
        } else if (task != null && task.isWorking()) {
            task.setInfo(text);
        } else if (pop) {
            popInformation(text);
        } else {
            MyBoxLog.console(text);
        }
    }

    public void handleError(String text, boolean pop) {
        if (this instanceof BaseLogsController) {
            ((BaseLogsController) this).updateLogs(text, true, true);
        } else if (task != null && task.isWorking()) {
            task.setError(text);
        } else if (pop) {
            popError(text);
        } else {
            MyBoxLog.error(text);
        }
    }

    public void setInfo(String text) {
        handleInfo(text, false);
    }

    public void displayInfo(String text) {
        handleInfo(text, true);
    }

    public void setError(String text) {
        handleError(text, false);
    }

    public void displayError(String text) {
        handleError(text, true);
    }

    @FXML
    public void closePopup() {
        if (popupTimer != null) {
            popupTimer.cancel();
            popupTimer = null;
        }
        if (popMenu != null) {
            popMenu.setUserData(null);
            popMenu.hide();
            popMenu = null;
        }
        if (popup != null) {
            popup.setUserData(null);
            popup.hide();
            popup = null;
        }
    }

    public Timer getPopupTimer() {
        if (popupTimer != null) {
            popupTimer.cancel();

        }
        popupTimer = new Timer();
        return popupTimer;
    }

}
