package mara.mybox.controller.base;

import mara.mybox.fxml.FxmlStage;
import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import mara.mybox.controller.HtmlEditorController;
import mara.mybox.controller.LoadingController;
import mara.mybox.controller.MainMenuController;
import mara.mybox.controller.OperationController;
import mara.mybox.db.DerbyBase;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.data.VisitHistory;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.CommonValues.AppDataRoot;

/**
 * @Author Mara
 * @CreateDate 2018-6-4 17:50:43
 * @Description
 * @License Apache License Version 2.0
 */
public class BaseController implements Initializable {

    public String TipsLabelKey, LastPathKey, targetPathKey, sourcePathKey;
    public int SourceFileType, SourcePathType, TargetFileType, TargetPathType, AddFileType, AddPathType;
    public List<FileChooser.ExtensionFilter> fileExtensionFilter;
    public String myFxml, parentFxml, currentStatus, baseTitle, baseName, loadFxml;
    public Stage myStage;
    public Scene myScene;
    public Alert loadingAlert;
    public Task<Void> task, backgroundTask;
    public BaseController parentController, myController;
    public Timer popupTimer, timer;
    public Popup popup;
    public ContextMenu popMenu;

    public boolean isSettingValues;
    public File sourceFile, targetPath, targetFile;

    @FXML
    public Pane thisPane, mainMenu, operationBar;
    @FXML
    public MainMenuController mainMenuController;
    @FXML
    public TextField sourceFileInput, sourcePathInput, targetPathInput, targetPrefixInput, targetFileInput, statusLabel;
    @FXML
    public OperationController operationBarController;
    @FXML
    public Button selectSourceButton, newButton, copyButton, deleteButton, saveButton, infoButton, selectAllButton,
            okButton, startButton, firstButton, lastButton, previousButton, nextButton, previewButton;
    @FXML
    public VBox paraBox;
    @FXML
    public Label bottomLabel, tipsLabel;

    public BaseController() {
        baseTitle = AppVaribles.getMessage("AppTitle");

        SourceFileType = 0;
        SourcePathType = 0;
        TargetPathType = 0;
        TargetFileType = 0;
        AddFileType = 0;
        AddPathType = 0;

        LastPathKey = "LastPathKey";
        targetPathKey = "targetPath";
        sourcePathKey = "sourcePath";

        fileExtensionFilter = CommonValues.AllExtensionFilter;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            myFxml = FxmlControl.getFxmlPath(url.getPath());
            myController = this;
            baseName = FileTools.getFilePrefix(myFxml);
            if (mainMenuController != null) {
                mainMenuController.parentFxml = myFxml;
                mainMenuController.parentController = this;
            }
            AppVaribles.alarmClockController = null;

            setInterfaceStyle(AppVaribles.getStyle());
            setSceneFontSize(AppVaribles.sceneFontSize);
            if (thisPane != null) {
                thisPane.setStyle("-fx-font-size: " + AppVaribles.sceneFontSize + "px;");
                thisPane.setOnKeyReleased(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        keyEventsHandler(event);
                    }
                });

            }

            setTips();
            initControls();

            initializeNext();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void afterStageShown() {
        getMyStage();
    }

    public void initStage() {
        getMyScene();
        final String prefix;
        if (baseName.contains("ImageManufacture") && !baseName.contains("ImageManufactureBatch")) {
            prefix = "Interface_" + "ImageManufacture";
        } else {
            prefix = "Interface_" + baseName;
        }

        if (AppVaribles.restoreStagesSize) {

            myStage.setFullScreen(AppVaribles.getUserConfigBoolean(prefix + "FullScreen", false));
            FxmlControl.setMaximized(myStage, AppVaribles.getUserConfigBoolean(prefix + "Maximized", false));

            if (!myStage.isFullScreen() && !myStage.isMaximized()) {
                myStage.sizeToScene();
                int v = AppVaribles.getUserConfigInt(prefix + "StageWidth", -1);
                if (v > 0) {
                    myStage.setWidth(v);
                }
                v = AppVaribles.getUserConfigInt(prefix + "StageHeight", -1);
                if (v > 0) {
                    myStage.setHeight(v);
                }
                myStage.centerOnScreen();
            }

            myScene.widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    AppVaribles.setUserConfigValue(prefix + "FullScreen", myStage.isFullScreen());
                    AppVaribles.setUserConfigValue(prefix + "Maximized", myStage.isMaximized());
//                AppVaribles.setUserConfigValue(baseName + "Iconified", myStage.isIconified());
                    if (!myStage.isFullScreen() && !myStage.isMaximized()) {
                        AppVaribles.setUserConfigInt(prefix + "StageWidth", (int) myStage.getWidth());
                    }
                }
            });
            myScene.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                    AppVaribles.setUserConfigValue(prefix + "FullScreen", myStage.isFullScreen());
                    AppVaribles.setUserConfigValue(prefix + "Maximized", myStage.isMaximized());
//                AppVaribles.setUserConfigValue(baseName + "Iconified", myStage.isIconified());
                    if (!myStage.isFullScreen() && !myStage.isMaximized()) {
                        AppVaribles.setUserConfigInt(prefix + "StageHeight", (int) myStage.getHeight());
                    }
                }
            });

        }

        thisPane.requestFocus();

    }

    public void setTips() {

        if (tipsLabel != null && TipsLabelKey != null) {
            FxmlControl.quickTooltip(tipsLabel, new Tooltip(getMessage(TipsLabelKey)));
        }

        if (okButton != null) {
            FxmlControl.quickTooltip(okButton, new Tooltip("ENTER / F1 / CTRL+g"));
        }

        if (newButton != null) {
            FxmlControl.quickTooltip(newButton, new Tooltip("CTRL+n"));
        }

        if (deleteButton != null) {
            FxmlControl.quickTooltip(deleteButton, new Tooltip("CTRL+d / DELETE"));
        }

        if (copyButton != null) {
            FxmlControl.quickTooltip(copyButton, new Tooltip("CTRL+c"));
        }

        if (saveButton != null) {
            FxmlControl.quickTooltip(saveButton, new Tooltip("F2 / CTRL+s"));

        }

        if (infoButton != null) {
            FxmlControl.quickTooltip(infoButton, new Tooltip("CTRL+i"));
        }

        if (selectAllButton != null) {
            FxmlControl.quickTooltip(selectAllButton, new Tooltip("CTRL+a"));
        }

        if (nextButton != null) {
            FxmlControl.quickTooltip(nextButton, new Tooltip("PAGE DOWN"));
        }
        if (previousButton != null) {
            FxmlControl.quickTooltip(previousButton, new Tooltip("PAGE UP"));
        }
        if (firstButton != null) {
            FxmlControl.quickTooltip(firstButton, new Tooltip("HOME"));
        }
        if (lastButton != null) {
            FxmlControl.quickTooltip(lastButton, new Tooltip("END"));
        }
    }

    public void initControls() {
        try {

            if (mainMenuController != null) {
                mainMenuController.fileExtensionFilter = fileExtensionFilter;
                mainMenuController.sourcePathKey = sourcePathKey;
                mainMenuController.sourcePathKey = sourcePathKey;
                mainMenuController.SourceFileType = SourceFileType;
                mainMenuController.SourcePathType = SourcePathType;
                mainMenuController.TargetPathType = TargetPathType;
                mainMenuController.TargetFileType = TargetFileType;
                mainMenuController.AddFileType = AddFileType;
                mainMenuController.AddPathType = AddPathType;
                mainMenuController.targetPathKey = targetPathKey;
                mainMenuController.LastPathKey = LastPathKey;
            }

            if (sourceFileInput != null) {
                sourceFileInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (newValue == null || newValue.isEmpty()) {
                            sourceFileInput.setStyle(badStyle);
                            return;
                        }
                        final File file = new File(newValue);
                        if (!file.exists()) {
                            sourceFileInput.setStyle(badStyle);
                            return;
                        }
                        sourceFileInput.setStyle(null);
                        sourceFile = file;
                        sourceFileChanged(file);
                        if (parentController != null) {
                            parentController.sourceFileChanged(file);
                        }
                        if (file.isDirectory()) {
                            AppVaribles.setUserConfigValue(sourcePathKey, file.getPath());
                        } else {
                            AppVaribles.setUserConfigValue(sourcePathKey, file.getParent());
                            if (targetPrefixInput != null) {
                                targetPrefixInput.setText(FileTools.getFilePrefix(file.getName()));
                            }
                        }
                    }
                });
            }

            if (sourcePathInput != null) {
                sourcePathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        final File file = new File(newValue);
                        if (!file.exists() || !file.isDirectory()) {
                            sourcePathInput.setStyle(badStyle);
                            return;
                        }
                        sourcePathInput.setStyle(null);
                        recordFileOpened(file);
                    }
                });
                File sfile = AppVaribles.getUserConfigPath(sourcePathKey);
                if (sfile != null) {
                    sourcePathInput.setText(sfile.getAbsolutePath());
                }
            }

            if (targetFileInput != null) {
                targetFileInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        checkTargetFileInput();
                    }
                });
            }

            if (targetPathInput != null) {
                targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        checkTargetPathInput();
                    }
                });
                File tfile = AppVaribles.getUserConfigPath(targetPathKey);
                if (tfile != null) {
                    targetPathInput.setText(tfile.getAbsolutePath());
                }
            }

            if (operationBarController != null) {
                operationBarController.parentController = this;
                if (operationBarController.openTargetButton != null) {
                    if (targetFileInput != null) {
                        operationBarController.openTargetButton.disableProperty().bind(Bindings.isEmpty(targetFileInput.textProperty())
                                .or(targetFileInput.styleProperty().isEqualTo(badStyle))
                        );
                    } else if (targetPathInput != null) {
                        operationBarController.openTargetButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                                .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                        );
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkTargetPathInput() {
        try {
            final File file = new File(targetPathInput.getText());
            if (!file.exists() || !file.isDirectory()) {
                targetPathInput.setStyle(badStyle);
                return;
            }
            targetPath = file;
            targetPathInput.setStyle(null);
            AppVaribles.setUserConfigValue(targetPathKey, file.getPath());
            recordFileWritten(file);
        } catch (Exception e) {
        }
    }

    public void checkTargetFileInput() {
        try {
            String input = targetFileInput.getText();
            targetFile = new File(input);
            targetFileInput.setStyle(null);
            AppVaribles.setUserConfigValue(targetPathKey, targetFile.getParent());
        } catch (Exception e) {
            targetFile = null;
            targetFileInput.setStyle(badStyle);
        }
    }

    public void keyEventsHandler(KeyEvent event) {
        if (event.isControlDown()) {
            String key = event.getText();
            if (key == null || key.isEmpty()) {
                return;
            }
            switch (key) {
                case "n":
                case "N":
                    if (newButton != null && !newButton.isDisabled()) {
                        newAction();
                    }
                    break;
                case "g":
                case "G":
                    if (okButton != null && !okButton.isDisabled()) {
                        okAction();
                    }
                    break;
                case "c":
                case "C":
                    if (copyButton != null && !copyButton.isDisabled()) {
                        copyAction();
                    }
                    break;
                case "s":
                case "S":
                    if (saveButton != null && !saveButton.isDisabled()) {
                        saveAction();
                    }
                    break;
                case "i":
                case "I":
                    if (infoButton != null && !infoButton.isDisabled()) {
                        infoAction();
                    }
                    break;
                case "d":
                case "D":
                    if (deleteButton != null && !deleteButton.isDisabled()) {
                        deleteAction();
                    }
                    break;
                case "a":
                case "A":
                    if (selectAllButton != null && !selectAllButton.isDisabled()) {
                        selectAllAction();
                    }
                    break;
                case "m":
                case "M":
                    if (mainMenuController != null) {
                        mainMenuController.getShowCommentsCheck().setSelected(!mainMenuController.getShowCommentsCheck().isSelected());
                        mainMenuController.showCommentsAction();
                    }
                    break;
                case "-":
                    setSceneFontSize(AppVaribles.sceneFontSize - 1);
                    break;
                case "=":
                    setSceneFontSize(AppVaribles.sceneFontSize + 1);
                    break;
            }

        } else {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case DELETE:
                        if (deleteButton != null && !deleteButton.isDisabled()) {
                            deleteAction();
                        }
                        break;
                    case HOME:
                        if (firstButton != null && !firstButton.isDisabled()) {
                            firstAction();
                        }
                        break;
                    case END:
                        if (lastButton != null && !lastButton.isDisabled()) {
                            lastAction();
                        }
                        break;
                    case PAGE_UP:
                        if (previousButton != null && !previousButton.isDisabled()) {
                            previousAction();
                        }
                        break;
                    case PAGE_DOWN:
                        if (nextButton != null && !nextButton.isDisabled()) {
                            nextAction();
                        }
                        break;
                    case F1:
                        if (okButton != null && !okButton.isDisabled()) {
                            okAction();
                        }
                        break;
                    case F2:
                        if (saveButton != null && !saveButton.isDisabled()) {
                            saveAction();
                        }
                        break;
                    case F4:
                        closeStage();
                        break;
                    case F5:
                        refresh();
                        break;
                }
            }
        }
    }

    public void initializeNext() {

    }

    public void setInterfaceStyle(Scene scene, String style) {
        try {
            if (scene != null && style != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource(style).toExternalForm());
//                thisPane.getStylesheets().add(getClass().getResource(CommonValues.MyBoxStyle).toExternalForm());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void setInterfaceStyle(String style) {
        try {
            if (thisPane != null && style != null) {
                thisPane.getStylesheets().clear();
                thisPane.getStylesheets().add(getClass().getResource(style).toExternalForm());
//                thisPane.getStylesheets().add(getClass().getResource(CommonValues.MyBoxStyle).toExternalForm());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public boolean setSceneFontSize(int size) {
        if (thisPane == null) {
            return false;
        }
        AppVaribles.setSceneFontSize(size);
        thisPane.setStyle("-fx-font-size: " + size + "px;");
        if (parentController != null) {
            parentController.setSceneFontSize(size);
        }
        return true;
    }

    public BaseController refresh() {
        return refreshBase();
    }

    public BaseController refreshBase() {
        String title = myStage.getTitle();
        BaseController c, p = parentController;
        c = loadScene(myFxml);
        if (c == null) {
            return null;
        }
        myStage.setTitle(title);
        if (p != null) {
            c.parentFxml = myFxml;
            c.parentController = p;
            p.refresh();
        }
        return c;
    }

    @FXML
    public ContextMenu getRecentMenu(boolean addClose) {
        final ContextMenu recentMenu = new ContextMenu();
        List<VisitHistory> his = VisitHistory.getRecentMenu();
        if (his == null || his.isEmpty()) {
            return recentMenu;
        }

        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            final String fxml = h.getDataMore();
            MenuItem menu = new MenuItem(getMessage(fname));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    loadScene(fxml);
                }
            });
            recentMenu.getItems().add(menu);
        }

        return recentMenu;

    }

    @FXML
    public void selectSourceFile(ActionEvent event) {
        try {
            if (!checkSavingForNextAction()) {
                return;
            }
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            File file = fileChooser.showOpenDialog(myStage);
            if (file == null || !file.exists()) {
                return;
            }

            selectSourceFileDo(file);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void selectSourceFile(File file) {
        if (!checkSavingForNextAction()) {
            return;
        }
        selectSourceFileDo(file);
    }

    public void selectSourceFileDo(File file) {
        sourceFile = file;
        if (sourceFileInput != null) {
            sourceFileInput.setText(sourceFile.getAbsolutePath());
        }
        recordFileOpened(file);
        sourceFileChanged(file);

    }

    public void sourceFileChanged(final File file) {

    }

    public void recordFileOpened(String file) {
        recordFileOpened(new File(file));
    }

    public void recordFileOpened(final File file) {
        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            String path = file.getPath();
            AppVaribles.setUserConfigValue(sourcePathKey, path);
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(SourcePathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVaribles.setUserConfigValue(sourcePathKey, path);
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(SourcePathType, path);
            VisitHistory.readFile(SourceFileType, fname);
        }

    }

    public void recordFileWritten(String file) {
        recordFileWritten(new File(file));
    }

    public void recordFileWritten(final File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            String path = file.getPath();
            AppVaribles.setUserConfigValue(targetPathKey, path);
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.writePath(TargetPathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVaribles.setUserConfigValue(targetPathKey, path);
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.writePath(TargetPathType, path);
            VisitHistory.writeFile(TargetFileType, fname);
        }
    }

    public void recordFileAdded(String file) {
        recordFileOpened(new File(file));
    }

    public void recordFileAdded(final File file) {
        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            String path = file.getPath();
            AppVaribles.setUserConfigValue(sourcePathKey, path);
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(SourcePathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVaribles.setUserConfigValue(sourcePathKey, path);
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(SourcePathType, path);
            VisitHistory.readFile(AddFileType, fname);
        }

    }

    @FXML
    public void selectTargetPath(ActionEvent event) {
        if (targetPathInput == null) {
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVaribles.getUserConfigPath(targetPathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(myStage);
            if (directory == null) {
                return;
            }
            selectTargetPath(directory);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void selectTargetPath(File directory) {
        targetPathInput.setText(directory.getPath());

        recordFileWritten(directory);
        targetPathChanged();
    }

    public void targetPathChanged() {

    }

    @FXML
    public void selectTargetFile(ActionEvent event) {
        File path = AppVaribles.getUserConfigPath(targetPathKey);
        selectTargetFileFromPath(path);
    }

    public void selectTargetFileFromPath(File path) {
        try {
            final FileChooser fileChooser = new FileChooser();
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            selectTargetFile(file);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void selectTargetFile(File file) {
        try {
            if (file == null) {
                return;
            }
            targetFile = file;
            recordFileWritten(file);

            if (targetFileInput != null) {
                targetFileInput.setText(targetFile.getAbsolutePath());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void openTarget(ActionEvent event) {

    }

    @FXML
    public void popSourceFile(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        List<VisitHistory> his = VisitHistory.getRecentFile(SourceFileType);
        if (his == null || his.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        popMenu.setAutoHide(true);
        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            MenuItem menu = new MenuItem(fname);
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    selectSourceFile(new File(fname));
                }
            });
            popMenu.getItems().add(menu);
        }
        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void popFileAdd(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        if (AddFileType <= 0) {
            AddFileType = SourceFileType;
        }
        List<VisitHistory> his = VisitHistory.getRecentFile(AddFileType);
        if (his == null || his.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            MenuItem menu = new MenuItem(fname);
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    addFile(new File(fname));
                }
            });
            popMenu.getItems().add(menu);
        }
        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    public void addFile(File file) {

    }

    @FXML
    public void popFileInsert(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        if (AddFileType <= 0) {
            AddFileType = SourceFileType;
        }
        List<VisitHistory> his = VisitHistory.getRecentFile(AddFileType);
        if (his == null || his.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            MenuItem menu = new MenuItem(fname);
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertFile(new File(fname));
                }
            });
            popMenu.getItems().add(menu);
        }

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    public void insertFile(File file) {

    }

    @FXML
    public void popDirectoryAdd(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        if (AddPathType <= 0) {
            AddPathType = SourcePathType;
        }
        List<VisitHistory> his = VisitHistory.getRecentPath(AddPathType);
        if (his == null || his.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            MenuItem menu = new MenuItem(fname);
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    addDirectory(new File(fname));
                }
            });
            popMenu.getItems().add(menu);
        }

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    public void addDirectory(File directory) {

    }

    @FXML
    public void popDirectoryInsert(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        if (AddPathType <= 0) {
            AddPathType = SourcePathType;
        }
        List<VisitHistory> his = VisitHistory.getRecentPath(AddPathType);
        if (his == null || his.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            MenuItem menu = new MenuItem(fname);
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    insertDirectory(new File(fname));
                }
            });
            popMenu.getItems().add(menu);
        }

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    public void insertDirectory(File directory) {

    }

    @FXML
    public void popSourcePath(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        List<VisitHistory> his = VisitHistory.getRecentPath(SourcePathType);
        if (his == null || his.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            MenuItem menu = new MenuItem(fname);
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    selectSourcePath(new File(fname));
                }
            });
            popMenu.getItems().add(menu);
        }

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void selectSourcePath(ActionEvent event) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            selectSourcePath(directory);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void selectSourcePath(File directory) {
        if (sourcePathInput != null) {
            sourcePathInput.setText(directory.getPath());
        }
        recordFileWritten(directory);
    }

    @FXML
    public void popTargetPath(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        List<VisitHistory> his = VisitHistory.getRecentPath(TargetPathType);
        if (his == null || his.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            MenuItem menu = new MenuItem(fname);
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    selectTargetPath(new File(fname));
                }
            });
            popMenu.getItems().add(menu);
        }
        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void popTargetFile(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        List<VisitHistory> his = VisitHistory.getRecentPath(TargetPathType);
        if (his == null || his.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        MenuItem menu;
        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            menu = new MenuItem(fname);
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    selectTargetFileFromPath(new File(fname));
                }
            });
            popMenu.getItems().add(menu);
        }

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void okAction() {

    }

    @FXML
    public void startAction() {

    }

    @FXML
    public void newAction() {

    }

    @FXML
    public void copyAction() {

    }

    @FXML
    public void saveAction() {

    }

    @FXML
    public void deleteAction() {

    }

    @FXML
    public void infoAction() {

    }

    @FXML
    public void selectAllAction() {

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
    public void clearSettings(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVaribles.getMessage("SureClear"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        DerbyBase.clearData();
        cleanAppPath();
        AppVaribles.initAppVaribles();
        popInformation(AppVaribles.getMessage("Successful"));
    }

    public void cleanAppPath() {
        try {
            File userPath = new File(AppDataRoot);
            if (userPath.exists()) {
                File[] files = userPath.listFiles();
                for (File f : files) {
                    if (f.isFile()) {
                        f.delete();
                    } else if (!CommonValues.AppDataPaths.contains(f)) {
                        FileTools.deleteDir(f);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void clearUserTempFiles() {
        try {
            File tempPath = AppVaribles.getUserTempPath();
            if (tempPath.exists()) {
                FileTools.deleteDir(tempPath);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void clearSystemTempFiles() {
        try {
            if (CommonValues.AppTempPath.exists()) {
                FileTools.deleteDir(CommonValues.AppTempPath);
            } else {
                CommonValues.AppTempPath.mkdirs();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void openUserPath(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(new File(AppDataRoot).toURI());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void showHelp(ActionEvent event) {
        try {
            File help = checkHelps();
            if (help != null) {
                HtmlEditorController controller
                        = (HtmlEditorController) openStage(CommonValues.HtmlEditorFxml);
                controller.switchBroswerTab();
                controller.loadHtml(help);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void showHelpExternal(ActionEvent event) {
        try {
            File help = checkHelps();
            if (help != null) {
                Desktop.getDesktop().browse(help.toURI());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public File checkHelps() {
        try {
            String lang = AppVaribles.getLanguage();
            String latest = AppVaribles.getSystemConfigValue("HelpsVersion", "");
            boolean updated = latest.equals(CommonValues.AppVersion);
            if (!updated) {
                logger.debug("Updating Helps " + CommonValues.AppVersion);
                clearHelps();
                AppVaribles.setSystemConfigValue("HelpsVersion", CommonValues.AppVersion);
            }
            FxmlControl.getUserFile(getClass(),
                    "/docs/mybox.css", "mybox.css", !updated);
            File mybox_help = FxmlControl.getUserFile(getClass(),
                    "/docs/mybox_help_" + lang + ".html", "mybox_help_" + lang + ".html", !updated);
            FxmlControl.getUserFile(getClass(),
                    "/docs/mybox_help_nav_" + lang + ".html", "mybox_help_nav_" + lang + ".html", !updated);
            FxmlControl.getUserFile(getClass(),
                    "/docs/mybox_help_main_" + lang + ".html", "mybox_help_main_" + lang + ".html", !updated);
            return mybox_help;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public void clearHelps() {
        try {
            File file = new File(AppDataRoot);
            if (file.exists()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    if (f.getAbsolutePath().endsWith(".html")) {
                        f.delete();
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void showHelpJVM() {
        try {
            checkHelps();
            String lang = AppVaribles.getLanguage();
            File jvm_help = FxmlControl.getUserFile(getClass(),
                    "/docs/mybox_help_main_" + lang + ".html", "mybox_help_main_" + lang + ".html", false);
            if (jvm_help != null) {
                URI uri = jvm_help.toURI();
                Desktop.getDesktop().browse(new URI(uri.getScheme(), uri.getHost(), uri.getPath(), "#Memory"));
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public BaseController loadScene(String newFxml) {
        try {
            if (!leavingScene()) {
                return null;
            }
            return FxmlStage.openScene(getClass(), getMyStage(), newFxml);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public BaseController openStage(String newFxml) {
        return openStage(newFxml, false);
    }

    public BaseController openStage(String newFxml, boolean isOwned) {
        return FxmlStage.openStage(getClass(), myStage, newFxml, isOwned);
    }

    public boolean closeStage() {
        if (leavingScene()) {
            FxmlStage.closeStage(myStage);
            return true;
        } else {
            return false;
        }
    }

    public boolean leavingScene() {
        try {
            if (!checkSavingForNextAction()) {
                return false;
            }

            if (mainMenuController != null) {
                mainMenuController.stopMemoryMonitorTimer();
                mainMenuController.stopCpuMonitorTimer();
            }

            hidePopup();
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (task != null && task.isRunning()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(myStage.getTitle());
                alert.setContentText(AppVaribles.getMessage("TaskRunning"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK && task != null) {
                    task.cancel();
                    task = null;
                } else {
                    return false;
                }
            }

            if (backgroundTask != null && backgroundTask.isRunning()) {
                backgroundTask.cancel();
                backgroundTask = null;
            }

//            System.gc();
            return true;
        } catch (Exception e) {
            logger.debug(e.toString());
            return false;
        }

    }

    public boolean checkSavingForNextAction() {
        return true;
    }

    public void alertError(String information) {
        FxmlStage.alertError(myStage, information);
    }

    public void alertWarning(String information) {
        FxmlStage.alertError(myStage, information);
    }

    public void alertInformation(String information) {
        FxmlStage.alertInformation(myStage, information);
    }

    public void popInformation(String text) {
        popInformation(text, AppVaribles.getCommentsDelay());
    }

    public void popInformation(String text, int delay) {
        try {
            if (popup != null) {
                popup.hide();
            }
            popup = getPopup();
            Label popupLabel = new Label(text);
            popupLabel.setStyle("-fx-background-color:black;"
                    + " -fx-text-fill: white;"
                    + " -fx-font-size: 1em;"
                    + " -fx-padding: 10px;"
                    + " -fx-background-radius: 6;");
            popup.setAutoFix(true);
            popup.getContent().add(popupLabel);

            if (delay <= 0) {
                popup.setAutoHide(true);
            } else {
                if (popupTimer != null) {
                    popupTimer.cancel();
                }
                popupTimer = getPopupTimer();
                popupTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (popup != null) {
                                    popup.hide();
                                }
                                popupTimer.cancel();
                            }
                        });
                    }
                }, delay);
            }

            popup.show(myStage);

        } catch (Exception e) {

        }
    }

    public void popError(String text) {
        popError(text, AppVaribles.getCommentsDelay());
    }

    public void popError(String text, int delay) {
        try {
            popup = getPopup();
            Label popupLabel = new Label(text);
            popupLabel.setStyle("-fx-background-color:white;"
                    + " -fx-text-fill: red;"
                    + " -fx-font-size: 1em;"
                    + " -fx-padding: 10px;"
                    + " -fx-background-radius: 6;");
            popup.setAutoFix(true);
            popup.getContent().add(popupLabel);

            if (delay <= 0) {
                popup.setAutoHide(true);
            } else {
                popupTimer = getPopupTimer();
                popupTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (popup != null) {
                                    popup.hide();
                                }
                                popupTimer.cancel();
                            }
                        });
                    }
                }, delay);
            }

            popup.show(myStage);
        } catch (Exception e) {

        }

    }

    public void hidePopup() {
        if (popup != null) {
            popup.hide();
        }
        if (popupTimer != null) {
            popupTimer.cancel();
        }
    }

    public Stage getMyStage() {
        if (myStage == null) {
            if (thisPane != null) {
                myScene = thisPane.getScene();
                if (myScene != null) {
                    myStage = (Stage) myScene.getWindow();
                }
            }
        }
        return myStage;
    }

    public LoadingController openHandlingStage(Modality block) {
        return openHandlingStage(block, null);
    }

    public LoadingController openHandlingStage(Modality block, String info) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.LoadingFxml), AppVaribles.currentBundle);
            Pane pane = fxmlLoader.load();
            LoadingController controller = fxmlLoader.getController();
            final Stage loadingStage = new Stage();

            loadingStage.initModality(block);
//            loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.initStyle(StageStyle.TRANSPARENT);
            loadingStage.initOwner(myStage);
            loadingStage.setScene(new Scene(pane));
            loadingStage.show();

            if (info != null) {
                controller.setInfo(info);
            }
            controller.myStage = loadingStage;

            return controller;

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
//        try {
//            LoadingController controller = (LoadingController) FxmlStage.openNewStage(getClass(),
//                    myStage, CommonValues.LoadingFxml, true, block);
//            controller.myStage.initStyle(StageStyle.TRANSPARENT);
//            if (info != null) {
//                controller.setInfo(info);
//            }
//            return controller;
//        } catch (Exception e) {
//            logger.error(e.toString());
//            return null;
//        }
    }

    public LoadingController openHandlingStage(final Task<?> task, Modality block) {
        return openHandlingStage(task, block, null);
    }

    public LoadingController openHandlingStage(final Task<?> task, Modality block, String info) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.LoadingFxml), AppVaribles.currentBundle);
            Pane pane = fxmlLoader.load();
            LoadingController controller = fxmlLoader.getController();
            controller.init(task);
            controller.parentController = myController;

            final Stage loadingStage = new Stage();
            loadingStage.initModality(block);
//            loadingStage.initStyle(StageStyle.UNDECORATED);
            loadingStage.initStyle(StageStyle.TRANSPARENT);
            loadingStage.initOwner(myStage);
            loadingStage.setScene(new Scene(pane));
            loadingStage.show();

            if (info != null) {
                controller.setInfo(info);
            }
            controller.myStage = loadingStage;

            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    loadingStage.close();
                }
            });
            task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    popInformation(AppVaribles.getMessage("Canceled"));
                    loadingStage.close();
                }
            });
            task.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    popError(AppVaribles.getMessage("Error"));
                    loadingStage.close();
                }
            });
            return controller;

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public void taskCanceled(Task task) {

    }

    public String getBaseTitle() {
        if (baseTitle == null && myStage != null) {
            baseTitle = myStage.getTitle();
            if (baseTitle == null) {
                baseTitle = AppVaribles.getMessage("AppTitle");
            }
        }
        return baseTitle;
    }

    public Timer getPopupTimer() {
        if (popupTimer != null) {
            popupTimer.cancel();

        }
        popupTimer = new Timer();
        return popupTimer;
    }

    public Popup getPopup() {
        if (popup != null) {
            popup.hide();
        }
        popup = new Popup();
        popup.setAutoHide(true);
        return popup;
    }

    public Scene getMyScene() {
        if (myScene == null) {
            if (thisPane != null) {
                myScene = thisPane.getScene();
            } else if (myStage != null) {
                myScene = myStage.getScene();
            }
        }
        return myScene;
    }

    public void setMaskStroke() {

    }

    public void drawMaskRulerX() {

    }

    public void drawMaskRulerY() {

    }

}
