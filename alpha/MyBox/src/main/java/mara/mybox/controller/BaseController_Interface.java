package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import javafx.stage.Stage;
import javafx.stage.Window;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppPaths;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
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
//                sourceFileInput.setText(UserConfig.getString(interfaceName + "SourceFile", null));
            }

            if (sourcePathInput != null) {
                sourcePathInput.setText(UserConfig.getString(interfaceName + "SourcePath", AppPaths.getGeneratedPath()));
                sourcePathInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> o, String ov, String nv) {
                        checkSourcetPathInput();
                    }
                });
            }

            if (targetPrefixInput != null) {
                targetPrefixInput.setText(UserConfig.getString(interfaceName + "TargetPrefix", "mm"));
                targetPrefixInput.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> o, String ov, String nv) {
                        if (nv != null && !nv.isBlank()) {
                            UserConfig.setString(interfaceName + "TargetPrefix", nv);
                        }
                    }
                });
            }

            if (targetFileController != null) {
                targetFileController.type(TargetFileType).parent((BaseController) this);
            }

            if (targetPathController != null) {
                targetPathController.type(TargetPathType).parent((BaseController) this);
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
            if (saveAsGroup != null && saveLoadRadio != null) {
                String v = UserConfig.getString(interfaceName + "SaveAsType", BaseController.SaveAsType.Edit.name());
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
                    case Edit:
                        if (saveEditRadio != null) {
                            saveEditRadio.setSelected(true);
                        } else if (saveLoadRadio != null) {
                            saveLoadRadio.setSelected(true);
                        }
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
                        } else if (saveEditRadio != null && saveEditRadio.isSelected()) {
                            saveAsType = BaseController.SaveAsType.Edit;
                        } else {
                            saveAsType = BaseController.SaveAsType.Open;
                        }
                        UserConfig.setString(interfaceName + "SaveAsType", saveAsType.name());
                    }
                });
            }

            dpi = UserConfig.getInt(interfaceName + "DPI", 96);
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

            if (openSourceButton != null) {
                openSourceButton.setDisable(true);
            }

            initMainArea();

            if (onTopCheck != null) {
                onTopCheck.setSelected(UserConfig.getBoolean(interfaceName + "AlwaysTop", false));
                onTopCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        if (isSettingValues || myStage == null) {
                            return;
                        }
                        UserConfig.setBoolean(interfaceName + "AlwaysTop", onTopCheck.isSelected());
                        setAlwaysTop(onTopCheck.isSelected(), true);
                    }
                });
            }
            if (closeAfterCheck != null) {
                closeAfterCheck.setSelected(UserConfig.getBoolean(interfaceName + "CloseAfterOperation", false));
                closeAfterCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                        UserConfig.setBoolean(interfaceName + "CloseAfterOperation", closeAfterCheck.isSelected());
                    }
                });
            }

            if (miaoCheck != null) {
                miaoCheck.setSelected(UserConfig.getBoolean(interfaceName + "Miao", true));
                miaoCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(interfaceName + "Miao", miaoCheck.isSelected());
                    }
                });

            }
            if (openCheck != null) {
                openCheck.setSelected(UserConfig.getBoolean(interfaceName + "OpenTargetPath", true));
                openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        UserConfig.setBoolean(interfaceName + "OpenTargetPath", openCheck.isSelected());
                    }
                });
            }

            if (tipsView != null) {
                tipsView.setPickOnBounds(true);
                tipsView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        popTips();
                    }
                });
            }

            if (rightTipsView != null) {
                rightTipsView.setPickOnBounds(true);
                rightTipsView.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        popTips();
                    }
                });
            }

            initLeftPaneControl();
            initRightPaneControl();

            initNodes(thisPane);
            initSplitPanes();
            setControlsStyle();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initMainArea() {
        if (toolbar == null || toolbarCheck == null || mainAreaBox == null) {
            return;
        }
        toolbarCheck.setSelected(UserConfig.getBoolean(interfaceName + "Toolbar", true));
        toolbarCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                UserConfig.setBoolean(interfaceName + "Toolbar", toolbarCheck.isSelected());
                checkToolbar();
            }
        });
        checkToolbar();
    }

    public void checkToolbar() {
        if (toolbar == null || toolbarCheck == null || mainAreaBox == null) {
            return;
        }
        if (toolbarCheck.isSelected()) {
            if (!mainAreaBox.getChildren().contains(toolbar)) {
                mainAreaBox.getChildren().add(0, toolbar);
            }
        } else {
            if (mainAreaBox.getChildren().contains(toolbar)) {
                mainAreaBox.getChildren().remove(toolbar);
            }
        }
        refreshStyle(mainAreaBox);
    }

    public void initLeftPaneControl() {
        if (splitPane != null && leftPane != null && leftPaneControl != null) {
            leftPaneControl.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    controlLeftPane();
                }
            });
            leftPaneControl.setPickOnBounds(true);
            leftPane.setHvalue(0);
            leftPane.setVvalue(0);
        }
    }

    public void initRightPaneControl() {
        if (splitPane != null && rightPane != null && rightPaneControl != null) {
            rightPaneControl.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    controlRightPane();
                }
            });
            rightPaneControl.setPickOnBounds(true);
            rightPane.setHvalue(0);
            rightPane.setVvalue(0);
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
                    MenuTextEditController.textMenu(myController, node, event);
                }
            });
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkDPI() {
        try {
            int v = Integer.parseInt(dpiSelector.getValue());
            if (v > 0) {
                dpi = v;
                UserConfig.setInt(interfaceName + "DPI", dpi);
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
    public boolean afterSceneLoaded() {
        try {
            getMyScene();
            getMyStage();
            isTopPane = true;

            if (endForAutoTestingWhenSceneLoaded()) {
                return false;
            }

            myStage.setMinWidth(minSize);
            myStage.setMinHeight(20);

            refreshStyle();

            if (this instanceof LoadingController) {
                return true;
            }

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

            toFront();

            if (leftPane != null || rightPane != null) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            if (leftPane != null) {
                                leftPane.setHvalue(0);
                                leftPane.setVvalue(0);
                            }

                            if (rightPane != null) {
                                rightPane.setHvalue(0);
                                rightPane.setVvalue(0);
                            }
                        });
                        Platform.requestNextPulse();
                    }
                }, 1000);
            }

            if (onTopCheck != null) {
                isSettingValues = true;
                onTopCheck.setSelected(myStage.isAlwaysOnTop());
                isSettingValues = false;
            }
            return true;

        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean endForAutoTestingWhenSceneLoaded() {
        if (AppVariables.autoTestingController == null) {
            return false;
        }
        myStage.setIconified(true);
        close();
        AppVariables.autoTestingController.sceneLoaded();
        return true;
    }

    public String interfaceKeysPrefix() {
        return "Interface_" + interfaceName;
    }

    public void setStageStatus() {
        try {
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
            MyBoxLog.error(e);
        }
    }

    public void setMinWidth(int minWidth) {
        try {
            if (getMyStage() == null) {
                return;
            }
            int w = (int) myStage.getWidth();
            if (w < minWidth) {
                myStage.setWidth(minWidth);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setInterfaceStyle(String style) {
        try {
            if (thisPane == null || style == null) {
                return;
            }
            thisPane.getStylesheets().clear();
            if (!AppValues.MyBoxStyle.equals(style)) {
                thisPane.getStylesheets().add(BaseController.class.getResource(style).toExternalForm());
            }
            thisPane.getStylesheets().add(BaseController.class.getResource(AppValues.MyBoxStyle).toExternalForm());
        } catch (Exception e) {
//            MyBoxLog.error(e);
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
            MyBoxLog.error(e);
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
                    });
                    Platform.requestNextPulse();
                }
            }, 500);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setAlwaysTop(boolean onTop, boolean info) {
        try {
            myStage = getMyStage();
            if (myStage == null || !myStage.isShowing()) {
                return;
            }
            myStage.setAlwaysOnTop(onTop);
            if (info) {
                popInformation(onTop ? message("AlwayOnTop") : message("DisableAlwayOnTop"));
            }
            if (onTopCheck != null) {
                isSettingValues = true;
                onTopCheck.setSelected(onTop);
                isSettingValues = false;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setIconified(boolean set) {
        try {
            myStage = getMyStage();
            Window owner = myStage.getOwner();
            if (owner != null) {
                if (owner instanceof Stage) {
                    ((Stage) owner).setIconified(set);
                }
            } else {
                myStage.setIconified(set);
            }
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }
    }

    public void requestMouse() {
        try {
            if (getMyStage() == null || this instanceof MyBoxLogViewerController) {
                return;
            }
            Platform.runLater(() -> {
                myStage.toFront();
                myStage.requestFocus();
                LocateTools.mouseCenter(myStage);
                closePopup();
            });
            Platform.requestNextPulse();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // Do not call "refreshStyle" in this method, or else endless loop happens
    public void setControlsStyle() {
        try {
            if (TipsLabelKey != null) {
                if (tipsLabel != null) {
                    NodeStyleTools.setTooltip(tipsLabel, new Tooltip(message(TipsLabelKey)));
                }

                if (tipsView != null) {
                    NodeStyleTools.setTooltip(tipsView, new Tooltip(message(TipsLabelKey)));
                }

                if (rightTipsView != null) {
                    NodeStyleTools.setTooltip(rightTipsView, new Tooltip(message(TipsLabelKey)));
                }
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

        } catch (Exception e) {
            MyBoxLog.debug(e);
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
            MyBoxLog.error(e);
            return myController;
        }
    }

    public boolean setSceneFontSize(int size) {
        try {
            if (thisPane == null) {
                return false;
            }
            UserConfig.setSceneFontSize(size);
            thisPane.setStyle("-fx-font-size: " + size + "px;");
            if (parentController != null && parentController != this) {
                parentController.setSceneFontSize(size);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
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
            MyBoxLog.error(e);
            return null;
        }
    }

    public BaseController openScene(String newFxml) {
        try {
            if (AppVariables.closeCurrentWhenOpenTool) {
                return loadScene(newFxml);
            } else {
                return openStage(newFxml);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public BaseController loadScene(String newFxml) {
        try {
            if (!leavingScene()) {
                return null;
            }
            return WindowTools.replaceStage(getStage(), newFxml);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public BaseController openStage(String newFxml) {
        return WindowTools.openStage(getStage(), newFxml);
    }

    public BaseController childStage(String newFxml) {
        return WindowTools.childStage(myController, newFxml);
    }

    public BaseController referredTopStage(String newFxml) {
        return WindowTools.referredTopStage(myController, newFxml);
    }

    public BaseController topStage(String newFxml) {
        return WindowTools.topStage(myController, newFxml);
    }

    public void updateStageTitle(File file) {
        try {
            if (getMyStage() == null) {
                return;
            }
            String title = getBaseTitle();
            if (file != null) {
                title += " - " + file.getAbsolutePath();
            }
            setTitle(title);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    /*
        close fxml
     */
    public boolean leavingScene() {
        try {
            if (!checkBeforeNextAction(thisPane)) {
                return false;
            }
            if (needStageVisitHistory()) {
                VisitHistoryTools.visitMenu(baseTitle, myFxml);
            }
            leaveScene();
            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    public boolean needStageVisitHistory() {
        return AppVariables.autoTestingController == null
                && isIndependantStage();
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
        try {
            cleanNode(thisPane);
            cleanWindow();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
            MyBoxLog.debug(e);
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
            MyBoxLog.debug(e);
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

    public boolean isRunning() {
        return getMyStage() != null && getMyStage().isShowing();
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
                rightPaneCheck.setSelected(UserConfig.getBoolean(interfaceName + "DisplayRightPane", true));
                checkRightPane();
                rightPaneCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                            if (isSettingValues) {
                                return;
                            }
                            UserConfig.setBoolean(interfaceName + "DisplayRightPane", rightPaneCheck.isSelected());
                            checkRightPane();
                        });

            }

            if (leftPaneCheck != null) {
                leftPaneCheck.setSelected(UserConfig.getBoolean(interfaceName + "DisplayLeftPane", true));
                checkLeftPane();
                leftPaneCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> o, Boolean ov, Boolean nv) {
                        if (isSettingValues) {
                            return;
                        }
                        UserConfig.setBoolean(interfaceName + "DisplayLeftPane", leftPaneCheck.isSelected());
                        checkLeftPane();
                    }
                });
            }
            if (!checkRightPaneHide()) {
                setSplitDividerPositions();
                refreshStyle(splitPane);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkLeftPane() {
        try {
            if (isSettingValues || splitPane == null || leftPane == null
                    || leftPaneCheck == null) {
                return;
            }
            if (leftPaneCheck.isSelected()) {
                if (leftPaneControl != null) {
                    leftPaneControl.setVisible(true);
                }
                showLeftPane();
            } else {
                hideLeftPane();
                if (leftPaneControl != null) {
                    leftPaneControl.setVisible(false);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkRightPane() {
        try {
            if (isSettingValues || splitPane == null || rightPane == null
                    || rightPaneCheck == null) {
                return;
            }
            if (rightPaneCheck.isSelected()) {
                if (rightPaneControl != null) {
                    rightPaneControl.setVisible(true);
                }
                showRightPane();
            } else {
                hideRightPane();
                if (rightPaneControl != null) {
                    rightPaneControl.setVisible(false);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkRightPaneHide() {
        try {
            if (isSettingValues || splitPane == null || rightPane == null
                    || (rightPaneControl != null && !rightPaneControl.isVisible())
                    || !splitPane.getItems().contains(rightPane)
                    || splitPane.getItems().size() == 1) {
                return false;
            }
            if (!UserConfig.getBoolean(interfaceName + "ShowRightControl", true)) {
                hideRightPane();
            }
            setSplitDividerPositions();
            refreshStyle(splitPane);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
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
                double v = UserConfig.getDouble(interfaceName + "LeftPanePosition", defaultv);
                splitPane.setDividerPosition(0, v);
                leftDividerListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!isSettingValues && newValue != null) {
                        UserConfig.setDouble(interfaceName + "LeftPanePosition", newValue.doubleValue());
                    }
                };
                splitPane.getDividers().get(0).positionProperty().addListener(leftDividerListener);
            }
            if (splitPane.getItems().contains(rightPane)) {
                int index = splitPane.getDividers().size() - 1;
                double defaultv = index > 0 ? 0.85 : 0.65;
                double v = UserConfig.getDouble(interfaceName + "RightPanePosition", defaultv);
                splitPane.setDividerPosition(index, v);
                rightDividerListener = (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (!isSettingValues && newValue != null) {
                        UserConfig.setDouble(interfaceName + "RightPanePosition", newValue.doubleValue());
                    }
                };
                splitPane.getDividers().get(index).positionProperty().addListener(rightDividerListener);
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void controlLeftPane() {
        if (isSettingValues || splitPane == null || leftPane == null
                || (leftPaneControl != null && !leftPaneControl.isVisible())) {
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
                || (leftPaneControl != null && !leftPaneControl.isVisible())
                || !splitPane.getItems().contains(leftPane)
                || splitPane.getItems().size() == 1) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().remove(leftPane);
        isSettingValues = false;
        setSplitDividerPositions();
        refreshStyle(splitPane);
        UserConfig.setBoolean(interfaceName + "ShowLeftControl", false);
        if (leftPaneControl != null) {
            StyleTools.setIconName(leftPaneControl, "iconDoubleRight.png");
        }
    }

    public void showLeftPane() {
        if (isSettingValues || splitPane == null || leftPane == null
                || (leftPaneControl != null && !leftPaneControl.isVisible())
                || splitPane.getItems().contains(leftPane)) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().add(0, leftPane);
        isSettingValues = false;
        setSplitDividerPositions();
        refreshStyle(splitPane);
        UserConfig.setBoolean(interfaceName + "ShowLeftControl", true);
        if (leftPaneControl != null) {
            StyleTools.setIconName(leftPaneControl, "iconDoubleLeft.png");
        }
        leftPane.setHvalue(0);
        leftPane.setVvalue(0);
    }

    @FXML
    public void controlRightPane() {
        if (isSettingValues || splitPane == null || rightPane == null
                || (rightPaneControl != null && !rightPaneControl.isVisible())) {
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
                || (rightPaneControl != null && !rightPaneControl.isVisible())
                || !splitPane.getItems().contains(rightPane)
                || splitPane.getItems().size() == 1) {
            return;
        }
        isSettingValues = true;
        splitPane.getItems().remove(rightPane);
        isSettingValues = false;
        setSplitDividerPositions();
        refreshStyle(splitPane);
        UserConfig.setBoolean(interfaceName + "ShowRightControl", false);
        if (rightPaneControl != null) {
            StyleTools.setIconName(rightPaneControl, "iconDoubleLeft.png");
        }
    }

    public void showRightPane() {
        try {
            if (isSettingValues || splitPane == null || rightPane == null
                    || (rightPaneControl != null && !rightPaneControl.isVisible())
                    || splitPane.getItems().contains(rightPane)) {
                return;
            }
            isSettingValues = true;
            splitPane.getItems().add(rightPane);
            isSettingValues = false;
            setSplitDividerPositions();
            refreshStyle(splitPane);
            UserConfig.setBoolean(interfaceName + "ShowRightControl", true);
            if (rightPaneControl != null) {
                StyleTools.setIconName(rightPaneControl, "iconDoubleRight.png");
            }
            rightPane.setHvalue(0);
            rightPane.setVvalue(0);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
