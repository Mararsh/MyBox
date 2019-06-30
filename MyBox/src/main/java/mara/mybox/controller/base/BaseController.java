package mara.mybox.controller.base;

import java.awt.Desktop;
import mara.mybox.fxml.FxmlStage;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
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
import mara.mybox.controller.HtmlEditorController;
import mara.mybox.controller.LoadingController;
import mara.mybox.controller.MainMenuController;
import mara.mybox.controller.OperationController;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.db.DerbyBase;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.data.VisitHistory;
import static mara.mybox.value.AppVaribles.env;
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

    public String TipsLabelKey, LastPathKey, targetPathKey, sourcePathKey, defaultPathKey, SaveAsOptionsKey;
    public int SourceFileType, SourcePathType, TargetFileType, TargetPathType, AddFileType, AddPathType,
            operationType;
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
    public SaveAsType saveAsType;

    public enum SaveAsType {
        Load, Open, None
    }

    @FXML
    public Pane thisPane, mainMenu, operationBar;
    @FXML
    public MainMenuController mainMenuController;
    @FXML
    public TextField sourceFileInput, sourcePathInput, targetPathInput, targetPrefixInput, targetFileInput, statusLabel;
    @FXML
    public OperationController operationBarController;
    @FXML
    public Button selectSourceButton, createButton, copyButton, pasteButton,
            deleteButton, saveButton, infoButton, metaButton, selectAllButton,
            okButton, startButton, firstButton, lastButton, previousButton, nextButton, goButton, previewButton,
            cropButton, saveAsButton, recoverButton, renameButton, tipsButton, viewButton, popButton, refButton,
            undoButton, redoButton, transparentButton, whiteButton, blackButton;
    @FXML
    public VBox paraBox;
    @FXML
    public Label bottomLabel, tipsLabel;
    @FXML
    public ImageView tipsView, linksView;
    @FXML
    protected ChoiceBox saveAsOptionsBox;

    public BaseController() {
        baseTitle = AppVaribles.getMessage("AppTitle");

        SourceFileType = 0;
        SourcePathType = 0;
        TargetPathType = 0;
        TargetFileType = 0;
        AddFileType = 0;
        AddPathType = 0;
        operationType = 0;

        LastPathKey = "LastPathKey";
        targetPathKey = "targetPath";
        sourcePathKey = "sourcePath";
        defaultPathKey = null;
        SaveAsOptionsKey = "SaveAsOptionsKey";

        fileExtensionFilter = CommonValues.AllExtensionFilter;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
//
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

            initControls();
            initializeNext();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void afterStageShown() {
        getMyStage();
    }

    public void afterSceneLoaded() {
        getMyScene();
        getMyStage();

        final String prefix;
        if (baseName.contains("ImageManufacture") && !baseName.contains("ImageManufactureBatch")) {
            prefix = "Interface_" + "ImageManufacture";
        } else {
            prefix = "Interface_" + baseName;
        }

        if (AppVaribles.restoreStagesSize) {

            final int minSize = 400;

            myStage.setFullScreen(AppVaribles.getUserConfigBoolean(prefix + "FullScreen", false));
            FxmlControl.setMaximized(myStage, AppVaribles.getUserConfigBoolean(prefix + "Maximized", false));

            if (!myStage.isFullScreen() && !myStage.isMaximized()) {
                myStage.sizeToScene();
                int v = AppVaribles.getUserConfigInt(prefix + "StageWidth", -1);
                if (v > 0) {
                    if (v < Math.min(minSize, myStage.getWidth())) {
                        v = 400;
                    }
                    myStage.setWidth(v);
                }
                v = AppVaribles.getUserConfigInt(prefix + "StageHeight", -1);
                if (v > 0) {
                    if (v < Math.min(minSize, myStage.getHeight())) {
                        v = 400;
                    }
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
                    if (!myStage.isFullScreen() && !myStage.isMaximized() && myStage.getWidth() > minSize) {
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
                    if (!myStage.isFullScreen() && !myStage.isMaximized() && myStage.getHeight() > minSize) {
                        AppVaribles.setUserConfigInt(prefix + "StageHeight", (int) myStage.getHeight());
                    }
                }
            });

        }

        Parent root = myScene.getRoot();
        root.requestFocus();
        refreshStyle(root);

        myStage.requestFocus();

    }

    public void refreshStyle(Parent node) {
        node.applyCss();
        node.layout();
        applyStyle(node);
    }

    public void applyStyle(Node node) {
        if (node == null) {
            return;
        }
        ControlStyle.setStyle(node);
        if (node instanceof Parent) {
            for (Node c : ((Parent) node).getChildrenUnmodifiable()) {
                applyStyle(c);
            }
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
                        checkSourceFileInput();
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

            if (tipsLabel != null && TipsLabelKey != null) {
                FxmlControl.setTooltip(tipsLabel, new Tooltip(getMessage(TipsLabelKey)));
            }

            if (tipsView != null && TipsLabelKey != null) {
                FxmlControl.setTooltip(tipsView, new Tooltip(getMessage(TipsLabelKey)));
            }

            try {
                String vv = AppVaribles.getUserConfigValue(SaveAsOptionsKey, SaveAsType.Load + "");
                if ((SaveAsType.Load + "").equals(vv)) {
                    saveAsType = SaveAsType.Load;

                } else if ((SaveAsType.Open + "").equals(vv)) {
                    saveAsType = SaveAsType.Open;

                } else if ((SaveAsType.None + "").equals(vv)) {
                    saveAsType = SaveAsType.None;
                }

            } catch (Exception e) {
//                logger.error(e.toString());
                saveAsType = SaveAsType.Load;
            }

            if (saveAsOptionsBox != null) {
                List<String> optionsList = Arrays.asList(getMessage("LoadAfterSaveAs"),
                        getMessage("OpenAfterSaveAs"), getMessage("JustSaveAs"));
                saveAsOptionsBox.getItems().addAll(optionsList);
                saveAsOptionsBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                        checkSaveAsOption();
                    }
                });
                if (null != saveAsType) {
                    switch (saveAsType) {
                        case Load:
                            saveAsOptionsBox.getSelectionModel().select(0);
                            break;
                        case Open:
                            saveAsOptionsBox.getSelectionModel().select(1);
                            break;
                        case None:
                            saveAsOptionsBox.getSelectionModel().select(2);
                            break;
                        default:
                            break;
                    }
                }

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkSaveAsOption() {
        switch (saveAsOptionsBox.getSelectionModel().getSelectedIndex()) {
            case 0:
                AppVaribles.setUserConfigValue(SaveAsOptionsKey, SaveAsType.Load + "");
                saveAsType = SaveAsType.Load;
                break;
            case 1:
                AppVaribles.setUserConfigValue(SaveAsOptionsKey, SaveAsType.Open + "");
                saveAsType = SaveAsType.Open;
                break;
            case 2:
                AppVaribles.setUserConfigValue(SaveAsOptionsKey, SaveAsType.None + "");
                saveAsType = SaveAsType.None;
                break;
            default:
                break;
        }
    }

    public void checkSourceFileInput() {
        String v = sourceFileInput.getText();
        if (v == null || v.isEmpty()) {
            sourceFileInput.setStyle(badStyle);
            return;
        }
        final File file = new File(v);
        if (!file.exists()) {
            sourceFileInput.setStyle(badStyle);
            return;
        }
        sourceFileInput.setStyle(null);
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
            switch (event.getCode()) {
                case HOME:
                    if (firstButton != null && !firstButton.isDisabled()) {
                        firstAction();
                    }
                    return;
                case END:
                    if (lastButton != null && !lastButton.isDisabled()) {
                        lastAction();
                    }
                    return;
            }
            String key = event.getText();
            if (key != null) {
                switch (key) {
                    case "n":
                    case "N":
                        if (createButton != null && !createButton.isDisabled()) {
                            createAction();
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
                    case "v":
                    case "V":
                        if (pasteButton != null && !pasteButton.isDisabled()) {
                            pasteAction();
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
                    case "x":
                    case "X":
                        if (cropButton != null && !cropButton.isDisabled()) {
                            cropAction();
                        }
                        break;
                    case "r":
                    case "R":
                        if (recoverButton != null && !recoverButton.isDisabled()) {
                            recoverAction();
                        }
                        break;
                    case "-":
                        setSceneFontSize(AppVaribles.sceneFontSize - 1);
                        break;
                    case "=":
                        setSceneFontSize(AppVaribles.sceneFontSize + 1);
                        break;
                }

            }

        } else {
            if (null != event.getCode()) {
                switch (event.getCode()) {
                    case DELETE:
                        if (deleteButton != null && !deleteButton.isDisabled()) {
                            deleteAction();
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
//
    }

    public void setInterfaceStyle(Scene scene, String style) {
        try {
            if (scene != null && style != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(env.getResource(style).toExternalForm());
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
                thisPane.getStylesheets().add(env.getResource(style).toExternalForm());
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
        try {
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
            c.getMyStage().requestFocus();
            return c;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
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
            if (!checkBeforeNextAction()) {
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
        if (!checkBeforeNextAction()) {
            return;
        }

        selectSourceFileDo(file);
    }

    public void selectSourceFileDo(File file) {
        recordFileOpened(file);
        if (sourceFileInput != null) {
            sourceFileInput.setText(file.getAbsolutePath());
        } else {
            sourceFileChanged(file);
        }
    }

    public void sourceFileChanged(final File file) {
        sourceFile = file;

    }

    public void recordFileOpened(String file) {
        recordFileOpened(new File(file));
    }

    public void recordFileOpened(final File file) {
        if (file == null) {
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

    public void recordFileOpened(final File file, int pathType, int fileType) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            String path = file.getPath();
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(pathType, path);
        } else {
            String path = file.getParent();
            String fname = file.getAbsolutePath();
            AppVaribles.setUserConfigValue(LastPathKey, path);
            VisitHistory.readPath(pathType, path);
            VisitHistory.readFile(fileType, fname);
        }

    }

    public void recordFileWritten(String file) {
        recordFileWritten(new File(file));
    }

    public void recordFileWritten(final File file) {
        recordFileWritten(file, targetPathKey, TargetPathType, TargetFileType);
    }

    public void recordFileWritten(final File file,
            String targetPathKey, int TargetPathType, int TargetFileType) {
        if (file == null) {
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
        if (file == null) {
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

    public void openTarget(ActionEvent event) {

    }

    @FXML
    public void addFilesAction(ActionEvent event) {

    }

    public void addFile(File file) {

    }

    @FXML
    public void insertFilesAction(ActionEvent event) {

    }

    public void insertFile(File file) {

    }

    public void addDirectory(File directory) {

    }

    public void insertDirectory(File directory) {

    }

    @FXML
    public void saveAsAction() {

    }

    @FXML
    public void popSourceFile(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        int fileNumber = AppVaribles.fileRecentNumber * 2 / 3 + 1;
        List<VisitHistory> his;
        if (operationType == VisitHistory.OperationType.Alpha) {
            his = VisitHistory.getRecentAlphaImages(fileNumber);
        } else {
            his = VisitHistory.getRecentFile(SourceFileType, fileNumber);
        }
        if (his != null && !his.isEmpty()) {
            MenuItem imenu = new MenuItem(getMessage("RecentAccessedFiles"));
            imenu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(imenu);
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
        }

        int pathNumber = AppVaribles.fileRecentNumber / 3 + 1;
        his = VisitHistory.getRecentPath(SourcePathType, pathNumber);
        List<String> paths = new ArrayList();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        if (defaultPathKey != null
                && !paths.contains(defaultPathKey)) {
            paths.add(defaultPathKey);
        }
        if (!paths.isEmpty()) {
            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem imenu = new MenuItem(getMessage("RecentAccessedDirectories"));
            imenu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(imenu);
            for (String path : paths) {
                MenuItem menu = new MenuItem(path);
                final String p = path;
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        AppVaribles.setUserConfigValue(sourcePathKey, p);
                        selectSourceFile(event);
                    }
                });
                popMenu.getItems().add(menu);
            }
        }

        if (popMenu.getItems().isEmpty()) {
            popMenu = null;
            return;
        }

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(getMessage("MenuClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popMenu.hide();
                popMenu = null;
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void popFileAdd(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        if (AddFileType <= 0) {
            AddFileType = SourceFileType;
        }
        int fileNumber = AppVaribles.fileRecentNumber * 2 / 3 + 1;
        List<VisitHistory> his;
        if (operationType == VisitHistory.OperationType.Alpha) {
            his = VisitHistory.getRecentAlphaImages(fileNumber);
        } else {
            his = VisitHistory.getRecentFile(AddFileType, fileNumber);
        }

        if (his != null && !his.isEmpty()) {
            MenuItem imenu = new MenuItem(getMessage("RecentAccessedFiles"));
            imenu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(imenu);
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
        }

        int pathNumber = AppVaribles.fileRecentNumber / 3 + 1;
        his = VisitHistory.getRecentPath(SourcePathType, pathNumber);
        List<String> paths = new ArrayList();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        if (defaultPathKey != null
                && !paths.contains(defaultPathKey)) {
            paths.add(defaultPathKey);
        }
        if (!paths.isEmpty()) {
            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem imenu = new MenuItem(getMessage("RecentAccessedDirectories"));
            imenu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(imenu);
            for (String path : paths) {
                MenuItem menu = new MenuItem(path);
                final String p = path;
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        AppVaribles.setUserConfigValue(sourcePathKey, p);
                        addFilesAction(event);
                    }
                });
                popMenu.getItems().add(menu);
            }
        }

        if (popMenu.getItems().isEmpty()) {
            popMenu = null;
            return;
        }

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(getMessage("MenuClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popMenu.hide();
                popMenu = null;
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void popFileInsert(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);

        if (AddFileType <= 0) {
            AddFileType = SourceFileType;
        }
        int fileNumber = AppVaribles.fileRecentNumber * 2 / 3 + 1;
        List<VisitHistory> his;
        if (operationType == VisitHistory.OperationType.Alpha) {
            his = VisitHistory.getRecentAlphaImages(fileNumber);
        } else {
            his = VisitHistory.getRecentFile(AddFileType, fileNumber);
        }

        if (his != null && !his.isEmpty()) {
            MenuItem imenu = new MenuItem(getMessage("RecentAccessedFiles"));
            imenu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(imenu);
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
        }

        int pathNumber = AppVaribles.fileRecentNumber / 3 + 1;
        his = VisitHistory.getRecentPath(SourcePathType, pathNumber);
        List<String> paths = new ArrayList();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        if (defaultPathKey != null
                && !paths.contains(defaultPathKey)) {
            paths.add(defaultPathKey);
        }
        if (!paths.isEmpty()) {
            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem imenu = new MenuItem(getMessage("RecentAccessedDirectories"));
            imenu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(imenu);
            for (String path : paths) {
                MenuItem menu = new MenuItem(path);
                final String p = path;
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        AppVaribles.setUserConfigValue(sourcePathKey, p);
                        insertFilesAction(event);
                    }
                });
                popMenu.getItems().add(menu);
            }
        }

        if (popMenu.getItems().isEmpty()) {
            popMenu = null;
            return;
        }

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(getMessage("MenuClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popMenu.hide();
                popMenu = null;
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

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
        MenuItem imenu = new MenuItem(getMessage("RecentAccessedDirectories"));
        imenu.setStyle("-fx-text-fill: #2e598a;");
        popMenu.getItems().add(imenu);
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

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(getMessage("MenuClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popMenu.hide();
                popMenu = null;
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

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
        MenuItem imenu = new MenuItem(getMessage("RecentAccessedDirectories"));
        imenu.setStyle("-fx-text-fill: #2e598a;");
        popMenu.getItems().add(imenu);
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

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(getMessage("MenuClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popMenu.hide();
                popMenu = null;
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void popSourcePath(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        List<VisitHistory> his = VisitHistory.getRecentPath(SourcePathType);
        List<String> paths = new ArrayList();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        if (defaultPathKey != null
                && !paths.contains(defaultPathKey)) {
            paths.add(defaultPathKey);
        }
        if (paths.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        MenuItem imenu = new MenuItem(getMessage("RecentAccessedDirectories"));
        imenu.setStyle("-fx-text-fill: #2e598a;");
        popMenu.getItems().add(imenu);
        for (String path : paths) {
            MenuItem menu = new MenuItem(path);
            final File p = new File(path);
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    selectSourcePath(p);
                }
            });
            popMenu.getItems().add(menu);
        }

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(getMessage("MenuClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popMenu.hide();
                popMenu = null;
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void popTargetPath(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        List<VisitHistory> his = VisitHistory.getRecentPath(TargetPathType);
        List<String> paths = new ArrayList();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        if (defaultPathKey != null
                && !paths.contains(defaultPathKey)) {
            paths.add(defaultPathKey);
        }
        if (paths.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        MenuItem imenu = new MenuItem(getMessage("RecentAccessedDirectories"));
        imenu.setStyle("-fx-text-fill: #2e598a;");
        popMenu.getItems().add(imenu);
        for (String path : paths) {
            MenuItem menu = new MenuItem(path);
            final File p = new File(path);
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    selectTargetPath(p);
                }
            });
            popMenu.getItems().add(menu);
        }

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(getMessage("MenuClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popMenu.hide();
                popMenu = null;
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void popTargetFile(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        List<VisitHistory> his = VisitHistory.getRecentPath(TargetPathType);
        List<String> paths = new ArrayList();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        if (defaultPathKey != null
                && !paths.contains(defaultPathKey)) {
            paths.add(defaultPathKey);
        }
        if (paths.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        MenuItem imenu = new MenuItem(getMessage("RecentAccessedDirectories"));
        imenu.setStyle("-fx-text-fill: #2e598a;");
        popMenu.getItems().add(imenu);
        MenuItem menu;
        for (String path : paths) {
            menu = new MenuItem(path);
            final File p = new File(path);
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    selectTargetFileFromPath(p);
                }
            });
            popMenu.getItems().add(menu);
        }

        popMenu.getItems().add(new SeparatorMenuItem());
        menu = new MenuItem(getMessage("MenuClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popMenu.hide();
                popMenu = null;
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void popSaveAs(MouseEvent event) { //
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        List<VisitHistory> his = VisitHistory.getRecentPath(TargetPathType);
        List<String> paths = new ArrayList();
        if (his != null) {
            for (VisitHistory h : his) {
                String pathname = h.getResourceValue();
                paths.add(pathname);
            }
        }
        if (defaultPathKey != null
                && !paths.contains(defaultPathKey)) {
            paths.add(defaultPathKey);
        }

        if (paths.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
        MenuItem imenu = new MenuItem(getMessage("RecentAccessedDirectories"));
        imenu.setStyle("-fx-text-fill: #2e598a;");
        popMenu.getItems().add(imenu);
        MenuItem menu;
        for (String path : paths) {
            menu = new MenuItem(path);
            final String p = path;
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    AppVaribles.setUserConfigValue(targetPathKey, p);
                    saveAsAction();
                }
            });
            popMenu.getItems().add(menu);
        }

        popMenu.getItems().add(new SeparatorMenuItem());
        menu = new MenuItem(getMessage("MenuClose"));
        menu.setStyle("-fx-text-fill: #2e598a;");
        menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                popMenu.hide();
                popMenu = null;
            }
        });
        popMenu.getItems().add(menu);

        FxmlControl.locateBelow((Region) event.getSource(), popMenu);

    }

    @FXML
    public void link(ActionEvent event) {
        try {
            Hyperlink link = (Hyperlink) event.getSource();
            browseURI(new URI(link.getText()));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void okAction() {

    }

    @FXML
    public void startAction() {

    }

    @FXML
    public void createAction() {

    }

    @FXML
    public void copyAction() {

    }

    @FXML
    public void pasteAction() {

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

    public void view(File file) {
        FxmlStage.openTarget(null, file.getAbsolutePath());
    }

    public void view(String file) {
        FxmlStage.openTarget(null, file);
    }

    public boolean browseURI(URI uri) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    try {
                        desktop.browse(uri);
                        return true;
                    } catch (Exception ioe) {
                        view(uri.toString());
                    }
                }
            } else {
                view(uri.toString());
            }

        } catch (Exception e) {
            if (uri != null) {
                view(uri.toString());
            }
        }
        popError(getMessage("DesktopNotSupportBrowse"));
        return false;
    }

    @FXML
    public void openUserPath(ActionEvent event) {
        try {
            browseURI(new File(AppDataRoot).toURI());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void developerGuide(ActionEvent event) {
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
            FxmlControl.getUserFile("/docs/mybox.css", "mybox.css", !updated);
            File mybox_help = FxmlControl.getUserFile("/docs/mybox_help_" + lang + ".html", "mybox_help_" + lang + ".html", !updated);
            FxmlControl.getUserFile("/docs/mybox_help_nav_" + lang + ".html", "mybox_help_nav_" + lang + ".html", !updated);
            FxmlControl.getUserFile("/docs/mybox_help_main_" + lang + ".html", "mybox_help_main_" + lang + ".html", !updated);
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
            File jvm_help = FxmlControl.getUserFile("/docs/mybox_help_main_" + lang + ".html", "mybox_help_main_" + lang + ".html", false);
            if (jvm_help != null) {
                URI uri = jvm_help.toURI();
                browseURI(new URI(uri.getScheme(), uri.getHost(), uri.getPath(), "#Memory"));
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
            return FxmlStage.openScene(getMyStage(), newFxml);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public BaseController openStage(String newFxml) {
        return openStage(newFxml, false);
    }

    public BaseController openStage(String newFxml, boolean isOwned) {
        return FxmlStage.openStage(myStage, newFxml, isOwned);
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
            if (!checkBeforeNextAction()) {
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

    public boolean checkBeforeNextAction() {
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
            final LoadingController controller
                    = (LoadingController) FxmlStage.openLoadingStage(myStage, block, info);
            return controller;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    public LoadingController openHandlingStage(final Task<?> task, Modality block) {
        return openHandlingStage(task, block, null);
    }

    public LoadingController openHandlingStage(final Task<?> task, Modality block, String info) {
        try {
            final LoadingController controller
                    = (LoadingController) FxmlStage.openLoadingStage(myStage, block, info);

            controller.init(task);
            controller.parentController = myController;

            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    controller.closeStage();
                }
            });
            task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    popInformation(AppVaribles.getMessage("Canceled"));
                    controller.closeStage();
                }
            });
            task.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    popError(AppVaribles.getMessage("Error"));
                    controller.closeStage();
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
