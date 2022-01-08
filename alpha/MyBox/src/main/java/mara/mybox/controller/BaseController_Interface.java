package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.StyleTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.isTesting;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-27
 * @License Apache License Version 2.0
 */
public abstract class BaseController_Interface extends BaseController_Files {

    protected final int minSize = 200;
    protected ChangeListener<Number> leftDividerListener, rightDividerListener;


    /*
        open fxml
     */
    public void initBaseControls() {
        try {
            setInterfaceStyle(UserConfig.getStyle());
            setSceneFontSize(AppVariables.sceneFontSize);
            if (thisPane != null) {
                thisPane.setStyle("-fx-font-size: " + AppVariables.sceneFontSize + "px;");
            }

            if (mainMenuController != null) {
                mainMenuController.SourceFileType = getSourceFileType();
                mainMenuController.sourceExtensionFilter = sourceExtensionFilter;
                mainMenuController.targetExtensionFilter = targetExtensionFilter;
                mainMenuController.SourcePathType = SourcePathType;
                mainMenuController.TargetPathType = TargetPathType;
                mainMenuController.TargetFileType = TargetFileType;
                mainMenuController.AddFileType = AddFileType;
                mainMenuController.AddPathType = AddPathType;
            }

            if (sourceFileInput != null) {
                sourceFileInput.textProperty().addListener(
                        (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                            checkSourceFileInput();
                        });
//                sourceFileInput.setText(UserConfig.getString(baseName + "SourceFile", null));
            }

            if (sourcePathInput != null) {
                sourcePathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        checkSourcetPathInput();
                    }
                });
                sourcePathInput.setText(UserConfig.getString(baseName + "SourcePath", AppPaths.getGeneratedPath()));
            }

            if (targetPrefixInput != null) {
                targetPrefixInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        if (newValue != null && !newValue.isBlank()) {
                            UserConfig.setString(baseName + "TargetPrefix", newValue);
                        }
                    }
                });
                targetPrefixInput.setText(UserConfig.getString(baseName + "TargetPrefix", "mm"));
            }

            if (targetFileController != null) {
                targetFileController.notify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (targetFileController.valid.get()) {
                            targetFile = targetFileController.file;
                        }
                    }
                });
                targetFileController.baseName(baseName).savedName(baseName + "TargetFile").type(TargetFileType).init();
            }

            if (targetPathController != null) {
                targetPathController.notify.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (targetPathController.valid.get()) {
                            targetPath = targetPathController.file;
                        }
                    }
                });
                targetPathController.baseName(baseName).savedName(baseName + "TargetPath").type(TargetPathType).init();
            }

            if (operationBarController != null) {
                operationBarController.parentController = myController;
                if (operationBarController.openTargetButton != null) {
                    if (targetFileController != null) {
                        operationBarController.openTargetButton.disableProperty().bind(Bindings.isEmpty(targetFileController.fileInput.textProperty())
                                .or(targetFileController.fileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                        );
                    } else if (targetPathController != null) {
                        operationBarController.openTargetButton.disableProperty().bind(Bindings.isEmpty(targetPathController.fileInput.textProperty())
                                .or(targetPathController.fileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                        );
                    }
                }
            }

            saveAsType = BaseController.SaveAsType.Open;
            if (saveAsGroup != null && saveOpenRadio != null) {
                String v = UserConfig.getString(baseName + "SaveAsType", BaseController.SaveAsType.Open.name());
                for (BaseController.SaveAsType s : BaseController.SaveAsType.values()) {
                    if (v.equals(s.name())) {
                        saveAsType = s;
                        break;
                    }
                }
                if (saveAsType == null || (saveLoadRadio == null && saveAsType == BaseController.SaveAsType.Load)) {
                    saveAsType = BaseController.SaveAsType.Open;
                }
                switch (saveAsType) {
                    case Load:
                        saveLoadRadio.setSelected(true);
                        break;
                    case Open:
                        saveOpenRadio.setSelected(true);
                        break;
                    case None:
                        saveJustRadio.setSelected(true);
                        break;
                    default:
                        break;
                }
                saveAsGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                        if (saveOpenRadio.isSelected()) {
                            saveAsType = BaseController.SaveAsType.Open;
                        } else if (saveJustRadio.isSelected()) {
                            saveAsType = BaseController.SaveAsType.None;
                        } else if (saveLoadRadio != null && saveLoadRadio.isSelected()) {
                            saveAsType = BaseController.SaveAsType.Load;
                        } else {
                            saveAsType = BaseController.SaveAsType.Open;
                        }
                        UserConfig.setString(baseName + "SaveAsType", saveAsType.name());
                    }
                });
            }

            if (topCheck != null) {
                topCheck.setSelected(UserConfig.getBoolean(baseName + "Top", true));
                topCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (!isSettingValues) {
                            UserConfig.setBoolean(baseName + "Top", newValue);
                        }
                        checkAlwaysTop();
                    }
                });

            }

            if (saveCloseCheck != null) {
                saveCloseCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov,
                            Boolean oldVal, Boolean newVal) {
                        UserConfig.setBoolean(baseName + "SaveClose", saveCloseCheck.isSelected());
                    }
                });
                saveCloseCheck.setSelected(UserConfig.getBoolean(baseName + "SaveClose", false));
            }

            dpi = UserConfig.getInt(baseName + "DPI", 96);
            if (dpiSelector != null) {
                List<String> dpiValues = new ArrayList();
                dpiValues.addAll(Arrays.asList("96", "72", "300", "160", "240", "120", "600", "400"));
                String sValue = (int) NodeTools.screenResolution() + "";
                if (dpiValues.contains(sValue)) {
                    dpiValues.remove(sValue);
                }
                dpiValues.add(0, sValue);
                sValue = (int) NodeTools.screenDpi() + "";
                if (dpiValues.contains(sValue)) {
                    dpiValues.remove(sValue);
                }
                dpiValues.add(sValue);
                dpiSelector.getItems().addAll(dpiValues);
                dpiSelector.setValue(dpi + "");
                dpiSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            checkDPI();
                        });
            }

            if (splitPane != null && leftPane != null && leftPaneControl != null) {
                leftPaneControl.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (UserConfig.getBoolean("MousePassControlPanes", true)) {
                            controlLeftPane();
                        }
                    }
                });
                leftPaneControl.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        controlLeftPane();
                    }
                });
                leftPaneControl.setPickOnBounds(UserConfig.getBoolean("ControlSplitPanesSensitive", false));
                leftPane.setHvalue(0);
            }

            if (splitPane != null && rightPane != null && rightPaneControl != null) {
                rightPaneControl.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (UserConfig.getBoolean("MousePassControlPanes", true)) {
                            controlRightPane();
                        }
                    }
                });
                rightPaneControl.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        controlRightPane();
                    }
                });
                rightPaneControl.setPickOnBounds(UserConfig.getBoolean("ControlSplitPanesSensitive", false));
                rightPane.setHvalue(0);
            }

            if (tabPane != null) {
                tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue ov, Tab oldValue, Tab newValue) {
                        MenuController.closeAll();
                    }
                });
            }

            initNodes(thisPane);
            initSplitPanes();
            setControlsStyle();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    private void initNodes(Node node) {
        if (node == null) {
            return;
        }
//        MyBoxLog.console(this.getClass() + "  " + node.getClass() + "  " + node.getId());
        Object o = node.getUserData();
        // Controls in embedded fxmls have been initialized by themselves
        if (o != null && o instanceof BaseController && node != thisPane) {
            return;
        }
        if (node instanceof TextInputControl) {
            makeEditContextMenu(node);
        } else if (node instanceof ComboBox) {
            ComboBox cb = (ComboBox) node;
            if (cb.isEditable()) {
                makeEditContextMenu(cb, cb.getEditor());
            }
        } else if (node instanceof SplitPane) {
            for (Node child : ((SplitPane) node).getItems()) {
                initNodes(child);
            }
        } else if (node instanceof ScrollPane) {
            initNodes(((ScrollPane) node).getContent());
        } else if (node instanceof TitledPane) {
            initNodes(((TitledPane) node).getContent());
        } else if (node instanceof TabPane) {
            for (Tab tab : ((TabPane) node).getTabs()) {
                initNodes(tab.getContent());
            }
        } else if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                initNodes(child);
            }
        }
    }

    public void makeEditContextMenu(Node node) {
        makeEditContextMenu(node, node);
    }

    public void makeEditContextMenu(Node node, Node textInput) {
        try {
            textInput.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    MenuTextEditController.open(myController, node, event);
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkDPI() {
        try {
            int v = Integer.parseInt(dpiSelector.getValue());
            if (v > 0) {
                dpi = v;
                UserConfig.setInt(baseName + "DPI", dpi);
                dpiSelector.getEditor().setStyle(null);
            } else {
                dpiSelector.getEditor().setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            dpiSelector.getEditor().setStyle(UserConfig.badStyle());
        }
    }

    public void initControls() {

    }

    /*
        show scene
     */
    // This is called automatically after TOP scene is loaded.
    // Notice embedded fxml will NOT call this automatically.
    public void afterSceneLoaded() {
        try {
            getMyScene();
            getMyStage();

            myStage.setMinWidth(minSize);
            myStage.setMinHeight(minSize);

            setStageStatus();

            Rectangle2D screen = NodeTools.getScreen();
            if (myStage.getHeight() > screen.getHeight()) {
                myStage.setHeight(screen.getHeight());
            }
            if (myStage.getWidth() > screen.getWidth()) {
                myStage.setWidth(screen.getWidth());
            }
            if (myStage.getX() < 0) {
                myStage.setX(0);
            }
            if (myStage.getY() < 0) {
                myStage.setY(0);
            }

            refreshStyle();
            toFront();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public String interfaceKeysPrefix() {
        return "Interface_" + interfaceName + (isPop ? "_Pop" : "");
    }

    public void setStageStatus() {
        try {
            isPop = false;
            if (AppVariables.recordWindowsSizeLocation) {
                String prefix = interfaceKeysPrefix();
                if (UserConfig.getBoolean(prefix + "FullScreen", false)) {
                    myStage.setFullScreen(true);

                } else if (UserConfig.getBoolean(prefix + "Maximized", false)) {
                    NodeTools.setMaximized(myStage, true);

                } else {
                    int mw = UserConfig.getInt(prefix + "StageWidth", -1);
                    int mh = UserConfig.getInt(prefix + "StageHeight", -1);
                    int mx = UserConfig.getInt(prefix + "StageX", -1);
                    int my = UserConfig.getInt(prefix + "StageY", -1);
                    if (mw > minSize && mh > minSize) {
                        myStage.setWidth(mw);
                        myStage.setHeight(mh);
                    }
                    if (mx >= 0 && my >= 0) {
                        myStage.setX(mx);
                        myStage.setY(my);
                    }
                }

                myStage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                        UserConfig.setBoolean(prefix + "FullScreen", myStage.isFullScreen());
                    }
                });
                myStage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                        UserConfig.setBoolean(prefix + "Maximized", myStage.isMaximized());
                    }
                });

            } else {
                myStage.sizeToScene();
                myStage.centerOnScreen();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setAsPop(String name) {
        try {
            isPop = true;
            this.interfaceName = name;
            String prefix = interfaceKeysPrefix();
            int mw = UserConfig.getInt(prefix + "StageWidth", Math.min(600, (int) myStage.getWidth()));
            int mh = UserConfig.getInt(prefix + "StageHeight", Math.min(500, (int) myStage.getHeight()));
            int mx = UserConfig.getInt(prefix + "StageX", (int) myStage.getX());
            int my = UserConfig.getInt(prefix + "StageY", (int) myStage.getY());
            if (mw > minSize && mh > minSize) {
                myStage.setWidth(mw);
                myStage.setHeight(mh);
            }
            if (mx >= 0 && my >= 0) {
                myStage.setX(mx);
                myStage.setY(my);
            }
            if (topCheck != null) {
                topCheck.setVisible(true);
                checkAlwaysTop();
            } else {
                myStage.setAlwaysOnTop(true);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setInterfaceStyle(Scene scene, String style) {
        try {
            if (scene != null && style != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(BaseController.class.getResource(style).toExternalForm());
            }
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    public void setInterfaceStyle(String style) {
        try {
            if (thisPane != null && style != null) {
                thisPane.getStylesheets().clear();
                if (!AppValues.MyBoxStyle.equals(style)) {
                    thisPane.getStylesheets().add(BaseController.class.getResource(style).toExternalForm());
                }
                thisPane.getStylesheets().add(BaseController.class.getResource(AppValues.MyBoxStyle).toExternalForm());
            }
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    public void refreshStyle() {
        if (getMyScene() != null) {
            refreshStyle(myScene.getRoot());
        } else if (thisPane != null) {
            refreshStyle(thisPane);
        }
    }

    public void refreshStyle(Parent node) {
        try {
            NodeStyleTools.refreshStyle(node);
            setControlsStyle();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void toFront() {
        try {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        getMyWindow().requestFocus();
                        getMyStage().setIconified(false);
                        myStage.toFront();
                        if (selectFileButton != null) {
                            selectFileButton.requestFocus();
                        } else if (tipsView != null) {
                            tipsView.requestFocus();
                        } else {
                            thisPane.requestFocus();
                        }
                        if (getMyWindow() instanceof Popup) {
                            LocateTools.mouseCenter(myStage);
                        }
                        if (leftPane != null) {
                            leftPane.setHvalue(0);
                        }
                        checkAlwaysTop();
                    });
                }
            }, 500);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkAlwaysTop() {
        if (topCheck == null || !topCheck.isVisible() || topCheck.isDisabled()
                || getMyStage() == null) {
            return;
        }
        myStage.setAlwaysOnTop(topCheck.isSelected());
        if (topCheck.isSelected()) {
            popWarn(message("AlwaysTopWarning"));
            FadeTransition fade = new FadeTransition(Duration.millis(300));
            fade.setFromValue(1.0);
            fade.setToValue(0f);
            fade.setCycleCount(4);
            fade.setAutoReverse(true);
            fade.setNode(topCheck);
            fade.play();
        }
    }

    // Do not call "refreshStyle" in this method, or else endless loop happens
    public void setControlsStyle() {
        try {
            if (leftPaneControl != null) {
                NodeStyleTools.setTooltip(leftPaneControl, new Tooltip("F4"));
            }
            if (rightPaneControl != null) {
                NodeStyleTools.setTooltip(rightPaneControl, new Tooltip("F5"));
            }
            if (tipsLabel != null && TipsLabelKey != null) {
                NodeStyleTools.setTooltip(tipsLabel, new Tooltip(message(TipsLabelKey)));
            }
            if (tipsView != null && TipsLabelKey != null) {
                NodeStyleTools.setTooltip(tipsView, new Tooltip(message(TipsLabelKey)));
            }
            if (rightTipsView != null && TipsLabelKey != null) {
                NodeStyleTools.setTooltip(rightTipsView, new Tooltip(message(TipsLabelKey)));
            }

            if (copyButton == null) {
                if (copyToSystemClipboardButton != null) {
                    NodeStyleTools.setTooltip(copyToSystemClipboardButton, new Tooltip(message("CopyToSystemClipboard") + "\nCTRL+c / ALT+c"));
                } else if (copyToMyBoxClipboardButton != null) {
                    NodeStyleTools.setTooltip(copyToMyBoxClipboardButton, new Tooltip(message("CopyToMyBoxClipboard") + "\nCTRL+c / ALT+c"));
                }
            }

            if (pasteButton == null) {
                if (pasteContentInSystemClipboardButton != null) {
                    NodeStyleTools.setTooltip(pasteContentInSystemClipboardButton, new Tooltip(message("PasteContentInSystemClipboard") + "\nCTRL+v / ALT+v"));
                } else if (loadContentInSystemClipboardButton != null) {
                    NodeStyleTools.setTooltip(loadContentInSystemClipboardButton, new Tooltip(message("LoadContentInSystemClipboard") + "\nCTRL+v / ALT+v"));
                }
            }

            if (panesMenuButton != null) {
                StyleTools.setIconTooltips(panesMenuButton, "iconPanes.png", message("Panes"));
            }

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public BaseController reload() {
        try {
            if (!checkBeforeNextAction()) {
                return myController;
            }
            if (getMyStage() == null || myFxml == null) {
                return refreshInterfaceAndFile();
            }
            BaseController p = parentController;
            File file = sourceFile;
            BaseController b = loadScene(myFxml);
            if (b == null) {
                return myController;
            }
            if (file != null) {
                b.selectSourceFileDo(file);
            }
            if (p != null) {
                p = p.reload();
                if (p != null) {
                    b.setParentController((BaseController) p);
                    b.setParentFxml(p.getMyFxml());
                }
            }
            b.toFront();
            return b;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return myController;
        }
    }

    public boolean setSceneFontSize(int size) {
        if (thisPane == null) {
            return false;
        }
        UserConfig.setSceneFontSize(size);
        thisPane.setStyle("-fx-font-size: " + size + "px;");
        if (parentController != null && parentController != this) {
            parentController.setSceneFontSize(size);
        }
        return true;
    }

    public boolean setIconSize(int size) {
        if (thisPane == null) {
            return false;
        }
        UserConfig.setIconSize(size);
        if (parentController != null && parentController != this) {
            parentController.setIconSize(size);
        }
        refreshInterface();
        return true;
    }

    public BaseController refreshInterfaceAndFile() {
        refreshInterface();
        if (checkBeforeNextAction()) {
            selectSourceFileDo(sourceFile);
        }
        return myController;
    }

    public BaseController refreshInterface() {
        try {
            if (thisPane != null) {
                thisPane.setStyle("-fx-font-size: " + AppVariables.sceneFontSize + "px;");
            }
            refreshStyle();
            if (parentController != null && parentController != this) {
                parentController.refreshInterface();
            }
            return myController;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public Window getOwner() {
        if (getMyWindow() instanceof Popup) {
            return ((Popup) myWindow).getOwnerWindow();
        } else {
            return getMyStage();
        }
    }

    public BaseController loadScene(String newFxml) {
        try {
            if (!leavingScene()) {
                return null;
            }
            return WindowTools.openScene(getOwner(), newFxml);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public BaseController openStage(String newFxml) {
        return WindowTools.openStage(getOwner(), newFxml);
    }

    public BaseController openChildStage(String newFxml, boolean isModal) {
        return WindowTools.openChildStage(getOwner(), newFxml, isModal);
    }


    /*
        close fxml
     */
    public boolean leavingScene() {
        try {
            if (!checkBeforeNextAction(thisPane)) {
                return false;
            }
            if (!isTesting && getMyStage() != null && mainMenuController != null
                    && !isPop && myStage.getOwner() == null) {
                VisitHistoryTools.visitMenu(baseTitle, myFxml);
            }
            leaveScene();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

    public static boolean checkBeforeNextAction(Node node) {
        if (node == null) {
            return true;
        }
        Object o = node.getUserData();
        if (o != null && o instanceof BaseController) {
            BaseController c = (BaseController) o;
            if (!c.checkBeforeNextAction()) {
                return false;
            }
        }
        if (node instanceof SplitPane) {
            for (Node child : ((SplitPane) node).getItems()) {
                if (!checkBeforeNextAction(child)) {
                    return false;
                }
            }
        } else if (node instanceof ScrollPane) {
            if (!checkBeforeNextAction(((ScrollPane) node).getContent())) {
                return false;
            }
        } else if (node instanceof TitledPane) {
            if (!checkBeforeNextAction(((TitledPane) node).getContent())) {
                return false;
            }
        } else if (node instanceof TabPane) {
            for (Tab tab : ((TabPane) node).getTabs()) {
                if (!checkBeforeNextAction(tab.getContent())) {
                    return false;
                }
            }
        } else if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                if (!checkBeforeNextAction(child)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void leaveScene() {
        cleanNode(thisPane);
        cleanWindow();
    }

    public static void cleanNode(Node node) {
        if (node == null) {
            return;
        }
        if (node instanceof SplitPane) {
            for (Node child : ((SplitPane) node).getItems()) {
                cleanNode(child);
            }
        } else if (node instanceof ScrollPane) {
            cleanNode(((ScrollPane) node).getContent());
        } else if (node instanceof TitledPane) {
            cleanNode(((TitledPane) node).getContent());
        } else if (node instanceof TabPane) {
            for (Tab tab : ((TabPane) node).getTabs()) {
                cleanNode(tab.getContent());
            }
        } else if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                cleanNode(child);
            }
        }
        Object o = node.getUserData();
        if (o != null && o instanceof BaseController) {
            BaseController c = (BaseController) o;
            c.cleanPane();
        }
        node.setUserData(null);
    }

    public void cleanPane() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (task != null) {
                task.cancel();
                task = null;
            }
            if (backgroundTask != null) {
                backgroundTask.cancel();
                backgroundTask = null;
            }
            closePopup();
            leftDividerListener = null;
            rightDividerListener = null;
            mainMenuController = null;
            parentFxml = null;
            parentController = null;
            myController = null;
            myFxml = null;
            if (thisPane != null) {
                thisPane.setUserData(null);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public void cleanWindow() {
        try {
            if (myScene != null) {
                myScene.setUserData(null);
                myScene = null;
            }
            myStage = getMyStage();
            if (myStage != null) {
                myStage.setUserData(null);
                final String prefix = interfaceKeysPrefix();
                UserConfig.setInt(prefix + "StageX", (int) myStage.getX());
                UserConfig.setInt(prefix + "StageY", (int) myStage.getY());
                UserConfig.setInt(prefix + "StageWidth", (int) myStage.getWidth());
                UserConfig.setInt(prefix + "StageHeight", (int) myStage.getHeight());
                myStage = null;
            }

            myWindow = null;
            System.gc();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }

    }

    public boolean close() {
        if (leavingScene()) {
            WindowTools.closeWindow(getMyWindow());
            return true;
        } else {
            return false;
        }
    }

    public boolean closeStage() {
        return close();
    }


    /*
        split panes
     */
    public void initSplitPanes() {
        try {
            if (splitPane == null || splitPane.getDividers().isEmpty()) {
                return;
            }
            if (rightPaneCheck != null) {
                rightPaneCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayRightPane", true));
                rightPaneCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                            if (isSettingValues) {
                                return;
                            }
                            UserConfig.setBoolean(baseName + "DisplayRightPane", rightPaneCheck.isSelected());
                            checkRightPane();
                        });
                checkRightPane();
            }
            if (leftPaneCheck != null) {
                leftPaneCheck.setSelected(UserConfig.getBoolean(baseName + "DisplayLeftPane", true));
                checkLeftPane();
                leftPaneCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                        UserConfig.setBoolean(baseName + "DisplayLeftPane", leftPaneCheck.isSelected());
                        checkLeftPane();
                    }
                });
            }
            if (!checkRightPaneHide()) {
                setSplitDividerPositions();
                refreshStyle(splitPane);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkLeftPane() {
        try {
            if (isSettingValues || splitPane == null || leftPane == null
                    || leftPaneCheck == null || leftPaneControl == null) {
                return;
            }
            if (leftPaneCheck.isSelected()) {
                leftPaneControl.setVisible(true);
                showLeftPane();
            } else {
                hideLeftPane();
                leftPaneControl.setVisible(false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkRightPane() {
        try {
            if (isSettingValues || splitPane == null || rightPane == null
                    || rightPaneCheck == null || rightPaneControl == null) {
                return;
            }
            if (rightPaneCheck.isSelected()) {
                rightPaneControl.setVisible(true);
                showRightPane();
            } else {
                hideRightPane();
                rightPaneControl.setVisible(false);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public boolean checkRightPaneHide() {
        try {
            if (isSettingValues || splitPane == null || rightPane == null
                    || rightPaneControl == null || !rightPaneControl.isVisible()
                    || !splitPane.getItems().contains(rightPane)
                    || splitPane.getItems().size() == 1) {
                return false;
            }
            if (!UserConfig.getBoolean(baseName + "ShowRightControl", true)) {
                hideRightPane();
            }
            setSplitDividerPositions();
            refreshStyle(splitPane);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return false;
        }
    }

    public void setSplitDividerPositions() {
        try {
            if (isSettingValues || splitPane == null) {
                return;
            }
            int dividersSize = splitPane.getDividers().size();
            if (dividersSize < 1) {
                return;
            }
            isSettingValues = true;
            try {
                splitPane.getDividers().get(0).positionProperty().removeListener(leftDividerListener);
                leftDividerListener = null;
            } catch (Exception e) {
            }
            try {
                splitPane.getDividers().get(dividersSize - 1).positionProperty().removeListener(rightDividerListener);
                rightDividerListener = null;
            } catch (Exception e) {
            }
            if (splitPane.getItems().contains(leftPane)) {
                double defaultv = dividersSize == 1 ? 0.35 : 0.15;
                try {
                    String v = UserConfig.getString(baseName + "LeftPanePosition", defaultv + "");
                    splitPane.setDividerPosition(0, Double.parseDouble(v));
                } catch (Exception e) {
                    splitPane.setDividerPosition(0, defaultv);
                }
                leftDividerListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!isSettingValues) {
                        UserConfig.setString(baseName + "LeftPanePosition", newValue.doubleValue() + "");
                    }
                };
                splitPane.getDividers().get(0).positionProperty().addListener(leftDividerListener);
            }
            if (splitPane.getItems().contains(rightPane)) {
                int index = splitPane.getDividers().size() - 1;
                double defaultv = index > 0 ? 0.85 : 0.65;
                try {
                    String v = UserConfig.getString(baseName + "RightPanePosition", defaultv + "");
                    splitPane.setDividerPosition(index, Double.parseDouble(v));
                } catch (Exception e) {
                    splitPane.setDividerPosition(index, defaultv);
                }
                rightDividerListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!isSettingValues) {
                        UserConfig.setString(baseName + "RightPanePosition", newValue.doubleValue() + "");
                    }
                };
                splitPane.getDividers().get(index).positionProperty().addListener(rightDividerListener);
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void controlLeftPane() {
        if (isSettingValues || splitPane == null || leftPane == null
                || leftPaneControl == null || !leftPaneControl.isVisible()) {
            return;
        }
        if (splitPane.getItems().contains(leftPane)) {
            hideLeftPane();
        } else {
            showLeftPane();
        }
    }

    public void hideLeftPane() {
        if (isSettingValues || splitPane == null || leftPane == null
                || leftPaneControl == null || !leftPaneControl.isVisible()
                || !splitPane.getItems().contains(leftPane)
                || splitPane.getItems().size() == 1) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().remove(leftPane);
        isSettingValues = false;
        setSplitDividerPositions();
        refreshStyle(splitPane);
        UserConfig.setBoolean(baseName + "ShowLeftControl", false);
        StyleTools.setIconName(leftPaneControl, "iconDoubleRight.png");
    }

    public void showLeftPane() {
        if (isSettingValues || splitPane == null || leftPane == null
                || leftPaneControl == null || !leftPaneControl.isVisible()
                || splitPane.getItems().contains(leftPane)) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().add(0, leftPane);
        isSettingValues = false;
        setSplitDividerPositions();
        refreshStyle(splitPane);
        UserConfig.setBoolean(baseName + "ShowLeftControl", true);
        StyleTools.setIconName(leftPaneControl, "iconDoubleLeft.png");
    }

    @FXML
    public void controlRightPane() {
        if (isSettingValues || splitPane == null || rightPane == null
                || rightPaneControl == null || !rightPaneControl.isVisible()) {
            return;
        }
        if (splitPane.getItems().contains(rightPane)) {
            hideRightPane();
        } else {
            showRightPane();
        }
    }

    public void hideRightPane() {
        if (isSettingValues || splitPane == null || rightPane == null
                || rightPaneControl == null || !rightPaneControl.isVisible()
                || !splitPane.getItems().contains(rightPane)
                || splitPane.getItems().size() == 1) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().remove(rightPane);
        isSettingValues = false;
        setSplitDividerPositions();
        refreshStyle(splitPane);
        UserConfig.setBoolean(baseName + "ShowRightControl", false);
        StyleTools.setIconName(rightPaneControl, "iconDoubleLeft.png");
    }

    public void showRightPane() {
        try {
            if (isSettingValues || splitPane == null || rightPane == null
                    || rightPaneControl == null || !rightPaneControl.isVisible()
                    || splitPane.getItems().contains(rightPane)) {
                return;
            }
            isSettingValues = true;
            splitPane.getItems().add(rightPane);
            isSettingValues = false;
            setSplitDividerPositions();
            refreshStyle(splitPane);
            UserConfig.setBoolean(baseName + "ShowRightControl", true);
            StyleTools.setIconName(rightPaneControl, "iconDoubleRight.png");
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
