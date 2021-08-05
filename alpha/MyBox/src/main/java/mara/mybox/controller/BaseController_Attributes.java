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
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.data.BaseTask;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-27
 * @License Apache License Version 2.0
 */
public abstract class BaseController_Attributes {

    protected BaseController parentController, myController;
    protected SingletonTask<Void> task, backgroundTask;
    protected int SourceFileType = -1, SourcePathType, TargetFileType, TargetPathType, AddFileType, AddPathType,
            operationType, dpi;
    protected List<FileChooser.ExtensionFilter> sourceExtensionFilter, targetExtensionFilter;
    protected String myFxml, parentFxml, currentStatus, baseTitle, baseName, TipsLabelKey;
    protected Stage myStage;
    protected Scene myScene;
    protected Window myWindow;
    protected Alert loadingAlert;
    protected Timer popupTimer, timer;
    protected Popup popup;
    protected ContextMenu popMenu;
    protected String targetFileSuffix, targetNameAppend;
    protected boolean isSettingValues, isPop;
    protected File sourceFile, sourcePath, targetPath, targetFile;
    protected SaveAsType saveAsType;
    protected TargetExistType targetExistType;

    protected enum SaveAsType {
        Load, Open, None
    }

    public static enum TargetExistType {
        Rename, Replace, Skip
    }

    @FXML
    protected Pane thisPane, mainMenu, operationBar;
    @FXML
    protected MainMenuController mainMenuController;
    @FXML
    protected TextField sourceFileInput, sourcePathInput, targetAppendInput,
            targetPathInput, targetPrefixInput, targetFileInput, statusLabel;
    @FXML
    protected OperationController operationBarController;
    @FXML
    protected Button selectFileButton, okButton, startButton, goButton, previewButton, playButton, stopButton,
            editButton, deleteButton, saveButton, cropButton, saveAsButton, undoButton, redoButton,
            clearButton, createButton, cancelButton, addButton, recoverButton, viewButton, popButton,
            copyButton, copyToSystemClipboardButton, copyToMyBoxClipboardButton,
            pasteButton, pasteContentInSystemClipboardButton, loadContentInSystemClipboardButton,
            myBoxClipboardButton, systemClipboardButton, selectButton,
            selectAllButton, selectNoneButton, renameButton, tipsButton, setButton, allButton, refButton,
            firstButton, lastButton, previousButton, nextButton, pageFirstButton, pageLastButton, pagePreviousButton, pageNextButton,
            infoButton, metaButton, transparentButton, whiteButton, blackButton, withdrawButton;
    @FXML
    protected VBox paraBox;
    @FXML
    protected Label bottomLabel, tipsLabel;
    @FXML
    protected ImageView tipsView, rightTipsView, linksView, leftPaneControl, rightPaneControl;
    @FXML
    protected CheckBox topCheck, saveCloseCheck, closeRightPaneCheck;
    @FXML
    protected ToggleGroup saveAsGroup, targetExistGroup, fileTypeGroup;
    @FXML
    protected RadioButton saveLoadRadio, saveOpenRadio, saveJustRadio,
            targetReplaceRadio, targetRenameRadio, targetSkipRadio;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected ScrollPane leftPane, rightPane;
    @FXML
    protected ComboBox<String> dpiSelector;

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
        if (baseTitle == null && myStage != null) {
            baseTitle = myStage.getTitle();
            if (baseTitle == null) {
                baseTitle = Languages.message("AppTitle");
            }
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

    public Scene getMyScene() {
        if (myScene == null) {
            if (myStage != null) {
                myScene = myStage.getScene();
            } else if (thisPane != null) {
                myScene = thisPane.getScene();
            }
        }
        return myScene;
    }

    public Window getMyWindow() {
        if (myWindow == null) {
            if (myStage != null) {
                myWindow = myStage;
            } else if (getMyScene() != null) {
                myWindow = myScene.getWindow();
            }
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

    public boolean isIsPop() {
        return isPop;
    }

    public void setIsPop(boolean isPop) {
        this.isPop = isPop;
    }

    /*
        task
     */
    public class SingletonTask<Void> extends BaseTask<Void> {

        @Override
        protected void whenSucceeded() {
            popSuccessful();
        }

        @Override
        protected void whenFailed() {
            if (error != null) {
                popError(error);
            } else {
                popFailed();
            }
        }

    };

    public SingletonTask<Void> getTask() {
        return task;
    }

    public void setTask(SingletonTask<Void> task) {
        this.task = task;
    }

    public void taskCanceled(Task task) {

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
        popText(text, duration, UserConfig.getPopTextbgColor(), UserConfig.getPopInfoColor(), size, null);
    }

    public void popInformation(String text, int duration) {
        popInformation(text, duration, UserConfig.getPopTextSize());
    }

    public void popInformation(String text, Region attach) {
        popText(text, UserConfig.getPopTextDuration(), UserConfig.getPopTextbgColor(), UserConfig.getPopInfoColor(), UserConfig.getPopTextSize(), attach);
    }

    public void popInformation(String text) {
        popInformation(text, UserConfig.getPopTextDuration(), UserConfig.getPopTextSize());
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
        popText(text, duration, UserConfig.getPopTextbgColor(), UserConfig.getPopErrorColor(), size, null);
    }

    public void popError(String text) {
        popError(text, UserConfig.getPopTextDuration(), UserConfig.getPopTextSize());
    }

    public void popFailed() {
        popError(Languages.message("Failed"));
    }

    public void popWarn(String text, int duration, String size) {
        popText(text, duration, UserConfig.getPopTextbgColor(), UserConfig.getPopWarnColor(), size, null);
    }

    public void popWarn(String text, int duration) {
        popWarn(text, duration, UserConfig.getPopTextSize());
    }

    public void popWarn(String text) {
        popWarn(text, UserConfig.getPopTextDuration(), UserConfig.getPopTextSize());
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
