package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageClipboard;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageScope;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FileTools.FileSortMode;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends BaseImageShapesController {

    protected ImageScope scope;
    protected int currentAngle = 0, rotateAngle = 90;
    protected File nextFile, previousFile;
    protected FileSortMode sortMode;

    @FXML
    protected TitledPane filePane, viewPane, saveAsPane, editPane, browsePane;
    @FXML
    protected VBox contentBox, fileBox, saveAsBox, saveFormatsBox;
    @FXML
    protected HBox operationBox;
    @FXML
    protected FlowPane frameSelectorPane, saveFramesPane, formatsPane1, formatsPane2, formatsPane3, formatsPane4;
    @FXML
    protected CheckBox selectAreaCheck, deleteConfirmCheck, saveConfirmCheck;
    @FXML
    protected ToggleGroup sortGroup, framesSaveGroup;
    @FXML
    protected ComboBox<String> loadWidthBox, frameSelector;
    @FXML
    protected Label framesLabel;
    @FXML
    protected RadioButton saveAllFramesRadio, gifRadio, tifRadio, pngRadio;

    public ImageViewerController() {
        baseTitle = message("ImageViewer");
        TipsLabelKey = "ImageViewerTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            initFilePane();
            initViewPane();
            initSaveAsPane();
            initEditPane();
            initBrowsePane();
            initOperationBox();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initFilePane() {
        try {
            if (fileBox != null && imageView != null) {
                fileBox.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }
            if (saveButton != null && imageView != null) {
                saveButton.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            }

            loadWidth = defaultLoadWidth;
            if (loadWidthBox != null) {
                List<String> values = Arrays.asList(message("OriginalSize"),
                        "512", "1024", "256", "128", "2048", "100", "80", "4096");
                loadWidthBox.getItems().addAll(values);
                loadWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (message("OriginalSize").equals(newValue)) {
                            loadWidth = -1;
                        } else {
                            try {
                                loadWidth = Integer.valueOf(newValue);
                                FxmlControl.setEditorNormal(loadWidthBox);
                            } catch (Exception e) {
                                FxmlControl.setEditorBadStyle(loadWidthBox);
                                return;
                            }
                        }
                        AppVariables.setUserConfigInt(baseName + "LoadWidth", loadWidth);
                        setLoadWidth();
                    }
                });

                isSettingValues = true;
                int v = AppVariables.getUserConfigInt(baseName + "LoadWidth", defaultLoadWidth);
                if (v <= 0) {
                    loadWidthBox.getSelectionModel().select(0);
                } else {
                    loadWidthBox.getSelectionModel().select(v + "");
                }
                isSettingValues = false;
                FxmlControl.setTooltip(loadWidthBox, new Tooltip(AppVariables.message("ImageLoadWidthCommnets")));
            }

            if (deleteConfirmCheck != null) {
                deleteConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "ConfirmDelete", deleteConfirmCheck.isSelected());
                    }
                });
                deleteConfirmCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ConfirmDelete", true));
            }

            if (saveConfirmCheck != null) {
                saveConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "ConfirmSave", saveConfirmCheck.isSelected());
                    }
                });
                saveConfirmCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ConfirmSave", true));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initViewPane() {
        try {
            if (viewPane != null) {
                if (imageView != null) {
                    viewPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                viewPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "ViewPane", viewPane.isExpanded());
                });
                viewPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "ViewPane", false));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    protected List<MenuItem> makeImageContextMenu() {
        try {
            List<MenuItem> items = new ArrayList<>();
            MenuItem menu;

            List<MenuItem> subItems = new ArrayList<>();
            if (popButton != null && popButton.isVisible() && !popButton.isDisabled()) {
                menu = new MenuItem(message("Pop") + "  CTRL+p");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    popAction();
                });
                subItems.add(menu);
            }

            if (pickColorCheck != null && pickColorCheck.isVisible() && !pickColorCheck.isDisabled()) {
                CheckMenuItem checkMenu = new CheckMenuItem(message("PickColor"));
                checkMenu.setOnAction((ActionEvent menuItemEvent) -> {
                    if (isSettingValues) {
                        return;
                    }
                    checkMenu.setSelected(!pickColorCheck.isSelected());
                    pickColorCheck.setSelected(!pickColorCheck.isSelected());
                });
                isSettingValues = true;
                checkMenu.setSelected(pickColorCheck.isSelected());
                isSettingValues = false;
                subItems.add(checkMenu);
            }

            if (!needNotCoordinates) {
                CheckMenuItem checkMenu = new CheckMenuItem(message("Coordinate"));
                checkMenu.setOnAction((ActionEvent menuItemEvent) -> {
                    if (isSettingValues) {
                        return;
                    }
                    checkMenu.setSelected(!AppVariables.getUserConfigBoolean(baseName + "PopCooridnate", false));
                    if (coordinateCheck != null) {
                        coordinateCheck.setSelected(!coordinateCheck.isSelected());
                    } else {
                        AppVariables.setUserConfigValue(baseName + "PopCooridnate", checkMenu.isSelected());
                        checkCoordinate();
                    }
                });
                isSettingValues = true;
                checkMenu.setSelected(coordinateCheck != null ? coordinateCheck.isSelected()
                        : AppVariables.getUserConfigBoolean(baseName + "PopCooridnate", false));
                isSettingValues = false;
                subItems.add(checkMenu);
            }

            if (!needNotRulers) {
                CheckMenuItem checkMenuX = new CheckMenuItem(message("RulerX"));
                checkMenuX.setOnAction((ActionEvent menuItemEvent) -> {
                    if (isSettingValues) {
                        return;
                    }
                    checkMenuX.setSelected(!AppVariables.getUserConfigBoolean(baseName + "RulerX", false));
                    if (rulerXCheck != null) {
                        rulerXCheck.setSelected(!rulerXCheck.isSelected());
                    } else {
                        AppVariables.setUserConfigValue(baseName + "RulerX", checkMenuX.isSelected());
                        checkRulerX();
                    }
                });
                isSettingValues = true;
                checkMenuX.setSelected(rulerXCheck != null ? rulerXCheck.isSelected()
                        : AppVariables.getUserConfigBoolean(baseName + "RulerX", false));
                isSettingValues = false;
                subItems.add(checkMenuX);

                CheckMenuItem checkMenuY = new CheckMenuItem(message("RulerY"));
                checkMenuY.setOnAction((ActionEvent menuItemEvent) -> {
                    if (isSettingValues) {
                        return;
                    }
                    checkMenuY.setSelected(!AppVariables.getUserConfigBoolean(baseName + "RulerY", false));
                    if (rulerYCheck != null) {
                        rulerYCheck.setSelected(!rulerYCheck.isSelected());
                    } else {
                        AppVariables.setUserConfigValue(baseName + "RulerY", checkMenuY.isSelected());
                        checkRulerY();
                    }
                });
                isSettingValues = true;
                checkMenuY.setSelected(rulerYCheck != null ? rulerYCheck.isSelected()
                        : AppVariables.getUserConfigBoolean(baseName + "RulerY", false));
                isSettingValues = false;
                subItems.add(checkMenuY);
            }

            if (!subItems.isEmpty()) {
                items.addAll(subItems);
                items.add(new SeparatorMenuItem());
            }

            subItems = new ArrayList<>();

            if (selectAllButton != null && selectAllButton.isVisible() && !selectAllButton.isDisabled()) {
                menu = new MenuItem(message("SelectAll") + "  CTRL+a");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    selectAllAction();
                });
                subItems.add(menu);
            }

            if (selectAreaCheck != null && selectAreaCheck.isVisible() && !selectAreaCheck.isDisabled()) {
                CheckMenuItem checkMenu = new CheckMenuItem(message("SelectArea"));
                checkMenu.setOnAction((ActionEvent menuItemEvent) -> {
                    if (isSettingValues) {
                        return;
                    }
                    checkMenu.setSelected(!selectAreaCheck.isSelected());
                    selectAreaCheck.setSelected(!selectAreaCheck.isSelected());
                });
                isSettingValues = true;
                checkMenu.setSelected(selectAreaCheck.isSelected());
                isSettingValues = false;
                subItems.add(checkMenu);
            }

            if (copyButton != null && copyButton.isVisible() && !copyButton.isDisabled()) {
                menu = new MenuItem(message("Copy") + "  CTRL+c");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    copyAction();
                });
                subItems.add(menu);
            }

            if (pasteButton != null && pasteButton.isVisible() && !pasteButton.isDisabled()) {
                menu = new MenuItem(message("Paste") + "  CTRL+v");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    pasteAction();
                });
                subItems.add(menu);
            }

            if (cropButton != null && cropButton.isVisible() && !cropButton.isDisabled()) {
                menu = new MenuItem(message("Crop") + "  CTRL+x");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    cropAction();
                });
                subItems.add(menu);
            }

            if (rotateLeftButton != null && rotateLeftButton.isVisible() && !rotateLeftButton.isDisabled()) {
                menu = new MenuItem(message("RotateLeft"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    rotateLeft();
                });
                subItems.add(menu);
            }

            if (rotateRightButton != null && rotateRightButton.isVisible() && !rotateRightButton.isDisabled()) {
                menu = new MenuItem(message("RotateRight"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    rotateRight();
                });
                subItems.add(menu);
            }

            if (undoButton != null && undoButton.isVisible() && !undoButton.isDisabled()) {
                menu = new MenuItem(message("Undo") + "  CTRL+z");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    undoAction();
                });
                subItems.add(menu);
            }

            if (redoButton != null && redoButton.isVisible() && !redoButton.isDisabled()) {
                menu = new MenuItem(message("Redo") + "  F3CTRL+y");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    redoAction();
                });
                subItems.add(menu);
            }

            if (recoverButton != null && recoverButton.isVisible() && !recoverButton.isDisabled()) {
                menu = new MenuItem(message("Recover") + "  F3 / CTRL+r");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    recoverAction();
                });
                subItems.add(menu);
            }

            if (saveButton != null && saveButton.isVisible() && !saveButton.isDisabled()) {
                menu = new MenuItem(message("Save") + "  F2 / CTRL+s");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    saveAction();
                });
                subItems.add(menu);
            }

            if (saveAsButton != null && saveAsButton.isVisible() && !saveAsButton.isDisabled()) {
                menu = new MenuItem(message("SaveAs") + "  F11");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    saveAsAction();
                });
                subItems.add(menu);
            }

            if (renameButton != null && renameButton.isVisible() && !renameButton.isDisabled()) {
                menu = new MenuItem(message("Rename"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    renameAction();
                });
                subItems.add(menu);
            }

            if (deleteButton != null && deleteButton.isVisible() && !deleteButton.isDisabled()) {
                menu = new MenuItem(message("Delete"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    deleteAction();
                });
                subItems.add(menu);
            }

            if (!subItems.isEmpty()) {
                if (subItems.size() > 2) {
                    Menu subMenu = new Menu(message("Edit"));
                    subMenu.getItems().addAll(subItems);
                    items.add(subMenu);
                } else {
                    items.addAll(subItems);
                }
                items.add(new SeparatorMenuItem());
            }

            if (imageInformation != null) {
                menu = new MenuItem(message("Information"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                items.add(menu);

                menu = new MenuItem(message("MetaData"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    popMetaData();
                });
                items.add(menu);

                items.add(new SeparatorMenuItem());
            }

            subItems = new ArrayList<>();
            if (previousButton != null && previousButton.isVisible() && !previousButton.isDisabled()) {
                menu = new MenuItem(message("Previous") + "  PAGE UP");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    previousAction();
                });
                subItems.add(menu);
            }
            if (nextButton != null && nextButton.isVisible() && !nextButton.isDisabled()) {
                menu = new MenuItem(message("Next") + "  PAGE DOWN");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    nextAction();
                });
                subItems.add(menu);
            }

            if (firstButton != null && firstButton.isVisible() && !firstButton.isDisabled()) {
                menu = new MenuItem(message("First") + "  ALT+HOME");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    firstAction();
                });
                subItems.add(menu);
            }

            if (lastButton != null && lastButton.isVisible() && !lastButton.isDisabled()) {
                menu = new MenuItem(message("Last") + "  ALT+END");
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    lastAction();
                });
                subItems.add(menu);
            }

            if (!subItems.isEmpty()) {
                if (subItems.size() > 2) {
                    Menu subMenu = new Menu(message("Navigate"));
                    subMenu.getItems().addAll(subItems);
                    items.add(subMenu);
                } else {
                    items.addAll(subItems);
                }

            }

            List<MenuItem> superItems = super.makeImageContextMenu();
            if (!superItems.isEmpty()) {
                superItems.addAll(items);
            }

            return superItems;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    protected void initSaveAsPane() {
        try {
            if (saveAsPane != null) {
                if (imageView != null) {
                    saveAsPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                saveAsPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "SaveAsPane", saveAsPane.isExpanded());
                });
                saveAsPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "SaveAsPane", false));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initBrowsePane() {
        try {
            if (browsePane != null) {
                if (imageView != null) {
                    browsePane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                browsePane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "BrowsePane", browsePane.isExpanded());
                });
                browsePane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "BrowsePane", false));
            }

            if (previousButton != null) {
                previousButton.setDisable(sourceFile == null);
            }
            if (nextButton != null) {
                nextButton.setDisable(sourceFile == null);
            }

            String saveMode = AppVariables.getUserConfigValue(baseName + "SortMode",
                    FileTools.FileSortMode.NameAsc.name());
            sortMode = FileTools.sortMode(saveMode);
            if (sortGroup != null) {
                sortGroup.selectedToggleProperty().addListener(
                        (ObservableValue<? extends Toggle> ov, Toggle oldValue, Toggle newValue) -> {
                            if (newValue == null || isSettingValues) {
                                return;
                            }
                            String selected = ((RadioButton) newValue).getText();
                            for (FileSortMode mode : FileSortMode.values()) {
                                if (message(mode.name()).equals(selected)) {
                                    sortMode = mode;
                                    break;
                                }
                            }
                            AppVariables.setUserConfigValue(baseName + "SortMode", sortMode.name());
                            makeImageNevigator();
                        });
                for (Toggle toggle : sortGroup.getToggles()) {
                    RadioButton button = (RadioButton) toggle;
                    if (button.getText().equals(message(saveMode))) {
                        isSettingValues = true;
                        button.fire();
                        isSettingValues = false;
                    }
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initEditPane() {
        try {
            if (editPane != null) {
                editPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                editPane.expandedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    AppVariables.setUserConfigValue(baseName + "EditPane", editPane.isExpanded());
                });
                editPane.setExpanded(AppVariables.getUserConfigBoolean(baseName + "EditPane", false));
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initOperationBox() {
        try {
            if (imageView != null) {
                if (operationBox != null) {
                    operationBox.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
                }
                if (leftPaneControl != null) {
                    leftPaneControl.visibleProperty().bind(Bindings.isNotNull(imageView.imageProperty()));
                }
                if (rightPaneControl != null) {
                    rightPaneControl.visibleProperty().bind(Bindings.isNotNull(imageView.imageProperty()));
                }
            }

            if (selectAreaCheck != null) {
                selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        AppVariables.setUserConfigValue(baseName + "SelectArea", selectAreaCheck.isSelected());
                        checkSelect();
                    }
                });
                selectAreaCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "SelectArea", false));
                checkSelect();
                FxmlControl.setTooltip(selectAreaCheck, new Tooltip("CTRL+t"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    public void controlAltHandler(KeyEvent event) {
        if (event.getCode() == null) {
            return;
        }
        switch (event.getCode()) {
            case T:
                if (selectAreaCheck != null) {
                    selectAreaCheck.setSelected(!selectAreaCheck.isSelected());
                }
                return;
        }
        super.controlAltHandler(event);
    }

    protected void checkSaveAs() {

    }

    protected void checkSelect() {
        if (cropButton != null) {
            cropButton.setDisable(!selectAreaCheck.isSelected());
        }
        if (selectAllButton != null) {
            selectAllButton.setDisable(!selectAreaCheck.isSelected());
        }

        if (selectAreaCheck != null) {
            initMaskRectangleLine(selectAreaCheck.isSelected());
        }
        updateLabelsTitle();
    }

    protected void setLoadWidth() {
        if (isSettingValues) {
            return;
        }
        if (sourceFile != null) {
            loadImage(sourceFile, loadWidth);
        } else if (imageView.getImage() != null) {
            loadImage(imageView.getImage(), loadWidth);
        } else if (image != null) {
            loadImage(image, loadWidth);
        }
        if (imageInformation != null) {
            setImageChanged(imageInformation.isIsScaled());
        } else {
            setImageChanged(false);
        }
    }

    @Override
    public void afterInfoLoaded() {
        if (infoButton != null) {
            infoButton.setDisable(imageInformation == null);
        }
        if (metaButton != null) {
            metaButton.setDisable(imageInformation == null);
        }
        if (deleteButton != null) {
            deleteButton.setDisable(sourceFile == null);
        }
        if (renameButton != null) {
            renameButton.setDisable(sourceFile == null);
        }
        if (deleteConfirmCheck != null) {
            deleteConfirmCheck.setDisable(sourceFile == null);
        }
        if (previousButton != null) {
            previousButton.setDisable(sourceFile == null);
        }
        if (nextButton != null) {
            nextButton.setDisable(sourceFile == null);
        }

    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded()) {
                return false;
            }
            afterInfoLoaded();
            if (image == null || imageView == null) {
                return false;
            }
            if (sampledView != null) {
                if (imageInformation != null && imageInformation.isIsSampled()) {
                    FxmlControl.setTooltip(sampledView, imageInformation.sampleInformation(image));
                    sampledView.setVisible(true);
                } else {
                    sampledView.setVisible(false);
                }
            }

            imageView.setPreserveRatio(true);
            imageView.setImage(image);

            if (fileBox != null && frameSelectorPane != null) {
                if (framesNumber <= 1) {
                    if (fileBox.getChildren().contains(frameSelectorPane)) {
                        fileBox.getChildren().remove(frameSelectorPane);
                    }
                    if (saveAsBox.getChildren().contains(saveFramesPane)) {
                        saveAsBox.getChildren().remove(saveFramesPane);
                    }

                } else {
                    if (!fileBox.getChildren().contains(frameSelectorPane)) {
                        fileBox.getChildren().add(0, frameSelectorPane);
                    }
                    if (!saveAsBox.getChildren().contains(saveFramesPane)) {
                        saveAsBox.getChildren().add(0, saveFramesPane);
                    }
                }
                saveAllFramesRadio.fire();
                saveAllFramesSelected();
                framesLabel.setText("/" + framesNumber);
                List<String> frames = new ArrayList<>();
                for (int i = 1; i <= framesNumber; i++) {
                    frames.add(i + "");
                }
                isSettingValues = true;
                frameSelector.getItems().setAll(frames);
                frameSelector.setValue((frameIndex + 1) + "");
                isSettingValues = false;
            }

            if (sourceFile != null && nextButton != null) {
                makeImageNevigator();
            }
            fitSize();
            setMaskStroke();

            if (selectAreaCheck != null) {
                checkSelect();
            }

            if (imageInformation == null) {
                setImageChanged(true);
            } else {
                setImageChanged(imageInformation.isIsScaled());
            }
            refinePane();
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            imageView.setImage(null);
            alertInformation(AppVariables.message("NotSupported"));
            return false;
        }
    }

    public void makeImageNevigator() {
        makeImageNevigator(sourceFile);
    }

    public void makeImageNevigator(File currentfile) {
        try {
            if (currentfile == null) {
                previousFile = null;
                previousButton.setDisable(true);
                nextFile = null;
                nextButton.setDisable(true);
                return;
            }
            File path = currentfile.getParentFile();
            List<File> pathFiles = new ArrayList<>();
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && FileTools.isSupportedImage(file)) {
                        pathFiles.add(file);
                    }
                }
                FileTools.sortFiles(pathFiles, sortMode);

                for (int i = 0; i < pathFiles.size(); ++i) {
                    if (pathFiles.get(i).getAbsoluteFile().equals(currentfile.getAbsoluteFile())) {
                        if (i < pathFiles.size() - 1) {
                            nextFile = pathFiles.get(i + 1);
                            nextButton.setDisable(false);
                        } else {
                            nextFile = null;
                            nextButton.setDisable(true);
                        }
                        if (i > 0) {
                            previousFile = pathFiles.get(i - 1);
                            previousButton.setDisable(false);
                        } else {
                            previousFile = null;
                            previousButton.setDisable(true);
                        }
                        return;
                    }
                }
            }
            previousFile = null;
            previousButton.setDisable(true);
            nextFile = null;
            nextButton.setDisable(true);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void loadFrame() {
        try {
            if (frameSelector == null) {
                return;
            }
            int v = Integer.parseInt(frameSelector.getValue());
            if (v < 1 || v > framesNumber) {
                frameSelector.getEditor().setStyle(badStyle);
            } else {
                frameSelector.getEditor().setStyle(null);
                loadFrame(v - 1);
            }
        } catch (Exception e) {
            frameSelector.getEditor().setStyle(badStyle);
        }
    }

    @FXML
    public void viewFrames() {
        loadMultipleFramesImage(sourceFile);
    }

    @FXML
    public void saveAllFramesSelected() {
        if (sourceFile != null && framesNumber > 1) {
            saveFormatsBox.getChildren().clear();
            saveFormatsBox.getChildren().addAll(formatsPane2);
            if ("gif".equalsIgnoreCase(FileTools.getFileSuffix(sourceFile))) {
                gifRadio.fire();
            } else {
                tifRadio.fire();
            }
        } else {
            saveFormatsBox.getChildren().clear();
            saveFormatsBox.getChildren().addAll(formatsPane1, formatsPane2, formatsPane3, formatsPane4);
            pngRadio.fire();
        }
    }

    @FXML
    public void saveCurrentFramesSelected() {
        saveFormatsBox.getChildren().clear();
        saveFormatsBox.getChildren().addAll(formatsPane1, formatsPane2, formatsPane3, formatsPane4);
    }

    @FXML
    @Override
    public void infoAction() {
        if (imageInformation == null) {
            return;
        }
        FxmlStage.showImageInformation(imageInformation);
    }

    @FXML
    @Override
    public void nextAction() {
        if (nextFile != null) {
            loadImage(nextFile.getAbsoluteFile(), loadWidth, 0);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (previousFile != null) {
            loadImage(previousFile.getAbsoluteFile(), loadWidth, 0);
        }
    }

    @FXML
    public void viewImageAction() {
        FxmlStage.openImageViewer(null, sourceFile);
    }

    @FXML
    public void popMetaData() {
        FxmlStage.showImageMetaData(imageInformation);
    }

    @FXML
    public void moveRight() {
        FxmlControl.setScrollPane(scrollPane, -40, scrollPane.getVvalue());
    }

    @FXML
    public void moveLeft() {
        FxmlControl.setScrollPane(scrollPane, 40, scrollPane.getVvalue());
    }

    @FXML
    public void moveUp() {
        FxmlControl.setScrollPane(scrollPane, scrollPane.getHvalue(), 40);
    }

    @FXML
    public void moveDown() {
        FxmlControl.setScrollPane(scrollPane, scrollPane.getHvalue(), -40);
    }

    @FXML
    public void rotateRight() {
        rotate(90);
    }

    @FXML
    public void rotateLeft() {
        rotate(270);
    }

    @FXML
    public void turnOver() {
        rotate(180);
    }

    public void rotate(final int rotateAngle) {
        if (imageView.getImage() == null) {
            return;
        }
        currentAngle = rotateAngle;
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.rotateImage(imageView.getImage(), rotateAngle);
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    imageView.setImage(newImage);
                    checkSelect();
                    setImageChanged(true);
                    refinePane();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void selectAllAction() {
        if (imageView.getImage() == null
                || maskRectangleLine == null || !maskRectangleLine.isVisible()) {
            return;
        }
        maskRectangleData = new DoubleRectangle(0, 0,
                getImageWidth() - 1, getImageHeight() - 1);

        drawMaskRectangleLineAsData();
    }

    @FXML
    @Override
    public void cropAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        try {
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private Image areaImage;

                    @Override
                    protected boolean handle() {
                        areaImage = cropImage();
                        if (areaImage == null) {
                            areaImage = imageView.getImage();
                        }
                        return areaImage != null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        imageView.setImage(areaImage);
                        setImageChanged(true);
                        resetMaskControls();
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    protected Image cropImage() {
        Image inImage = imageView.getImage();

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            if (maskRectangleData.getSmallX() == 0
                    && maskRectangleData.getSmallY() == 0
                    && maskRectangleData.getBigX() == (int) inImage.getWidth() - 1
                    && maskRectangleData.getBigY() == (int) inImage.getHeight() - 1) {
                return null;
            }
            return FxmlImageManufacture.cropOutsideFx(inImage, maskRectangleData, Color.WHITE);

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            return FxmlImageManufacture.cropOutsideFx(inImage, maskCircleData, Color.WHITE);

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            return FxmlImageManufacture.cropOutsideFx(inImage, maskEllipseData, Color.WHITE);

        } else if (maskPolygonLine != null && maskPolygonLine.isVisible()) {
            return FxmlImageManufacture.cropOutsideFx(inImage, maskPolygonData, Color.WHITE);

        } else {
            return null;
        }

    }

    @FXML
    @Override
    public void recoverAction() {
        if (imageView == null) {
            return;
        }
        boolean sizeChanged = getImageWidth() != image.getWidth()
                || getImageHeight() != image.getHeight();
        imageView.setImage(image);
        if (sizeChanged) {
            resetMaskControls();
        }
        setImageChanged(false);
        popInformation(message("Recovered"));
    }

    @FXML
    @Override
    public void copyAction() {
        if (imageView == null || imageView.getImage() == null || copyButton == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image areaImage;

                @Override
                protected boolean handle() {
                    areaImage = cropImage();
                    if (areaImage == null) {
                        areaImage = imageView.getImage();
                    }
                    return ImageClipboard.add(areaImage) != null;
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(AppVariables.message("ImageSelectionInClipBoard"));
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (imageView == null) {
            return;
        }
        if (sourceFile == null) {
            saveAsAction();
            return;
        }

        try {
            String ask = null;
            if (imageInformation != null && imageInformation.isIsScaled()) {
                ask = message("SureSaveScaled");
            } else if (saveConfirmCheck != null && saveConfirmCheck.isSelected()) {
                ask = message("SureOverrideFile");
            }
            if (ask != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getMyStage().getTitle());
                alert.setContentText(ask);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                ButtonType buttonSave = new ButtonType(AppVariables.message("Save"));
                ButtonType buttonSaveAs = new ButtonType(AppVariables.message("SaveAs"));
                ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
                alert.getButtonTypes().setAll(buttonSave, buttonSaveAs, buttonCancel);
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonCancel) {
                    return;
                } else if (result.get() == buttonSaveAs) {
                    saveAsAction();
                    return;
                }
            }

            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    private String filename;
                    private Image selected;

                    @Override
                    protected boolean handle() {
                        String format = FileTools.getFileSuffix(sourceFile.getName());
                        selected = cropImage();
                        if (selected == null) {
                            selected = imageView.getImage();
                        }

                        final BufferedImage bufferedImage = FxmlImageManufacture.bufferedImage(selected);
                        if (bufferedImage == null || task == null || isCancelled()) {
                            return false;
                        }
                        filename = sourceFile.getAbsolutePath();
                        if (framesNumber > 1) {
                            error = ImageFileWriters.writeFrame(sourceFile, frameIndex, bufferedImage);
                            ok = error == null;
                        } else {
                            ok = ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                        }
                        if (!ok || task == null || isCancelled()) {
                            return false;
                        }
                        ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(filename);
                        if (finfo == null || finfo.getImageInformation() == null) {
                            return false;
                        }
                        imageInformation = finfo.getImageInformation();
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        image = selected;
                        imageView.setImage(image);
                        popInformation(filename + "   " + AppVariables.message("Saved"));
                        setImageChanged(false);
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public String saveAsPrefix() {
        String name = "";
        if (sourceFile != null) {
            name = FileTools.getFilePrefix(sourceFile.getName());
        }
        if (fileTypeGroup != null) {
            name += "." + ((RadioButton) fileTypeGroup.getSelectedToggle()).getText();
        }
        return name;
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (imageView == null) {
            return;
        }
        try {
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    saveAsPrefix(), CommonFxValues.ImageExtensionFilter);
            if (file == null) {
                return;
            }
            recordFileWritten(file);

            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        Image selected = cropImage();
                        if (selected == null) {
                            selected = imageView.getImage();
                        }
                        String format = FileTools.getFileSuffix(file.getName());
                        final BufferedImage bufferedImage = FxmlImageManufacture.bufferedImage(selected);
                        if (task == null || isCancelled()) {
                            return false;
                        }
                        if (framesNumber > 1 && saveAllFramesRadio.isSelected()) {
                            error = ImageFileWriters.writeFrame(sourceFile, frameIndex, bufferedImage, file, format);
                            return error == null;
                        } else {
                            return ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                        }
                    }

                    @Override
                    protected void whenSucceeded() {
                        popInformation(AppVariables.message("Saved"));
                        if (sourceFile == null || saveAsType == SaveAsType.Load) {
                            loadImage(file);

                        } else if (saveAsType == SaveAsType.Open) {
                            FxmlStage.openImageViewer(file);
                        }
                    }

                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    @Override
    public void deleteAction() {
        if (deleteFile(sourceFile)) {
            sourceFile = null;
            image = null;
            imageView.setImage(null);
            if (nextFile != null) {
                nextAction();
            } else if (previousFile != null) {
                previousAction();
            } else {
                if (previousButton != null) {
                    previousButton.setDisable(true);
                }
                if (nextButton != null) {
                    nextButton.setDisable(true);
                }
            }
        }
    }

    public boolean deleteFile(File sfile) {
        if (sfile == null) {
            return false;
        }
        if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
            if (!FxmlControl.askSure(getMyStage().getTitle(), message("SureDelete"))) {
                return false;
            }
        }
        if (FileTools.delete(sfile)) {
            popSuccessful();
            return true;
        } else {
            popFailed();
            return false;
        }
    }

    public void changeFile(ImageInformation info, File file) {
        if (info == null || file == null) {
            return;
        }
        ImageFileInformation finfo = info.getImageFileInformation();
        if (finfo != null) {
            finfo.setFile(file);
            finfo.setFileName(file.getAbsolutePath());
        }
        info.setFileName(file.getAbsolutePath());
        info.setFile(file);
    }

    @FXML
    public void renameAction() {
        try {
            if (imageChanged) {
                saveAction();
            }
            if (sourceFile == null) {
                return;
            }
            FileRenameController controller = (FileRenameController) FxmlStage.openStage(CommonValues.FileRenameFxml);
            controller.getMyStage().setOnHiding((WindowEvent event) -> {
                File newFile = controller.getNewFile();
                Platform.runLater(() -> {
                    fileRenamed(newFile);
                });
            });
            controller.set(sourceFile);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    public void fileRenamed(File newFile) {
        try {
            if (newFile == null) {
                return;
            }
            popSuccessful();
            sourceFile = newFile;
            recordFileOpened(sourceFile);
            changeFile(imageInformation, newFile);
            updateLabelsTitle();
            makeImageNevigator();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            popError(e.toString());
        }
    }

    @FXML
    protected void manufactureAction() {
        ImageManufactureController controller = (ImageManufactureController) FxmlStage.openStage(CommonValues.ImageManufactureFxml);
        operation(controller);
    }

    @FXML
    public void browseAction() {
        ImagesBrowserController controller = FxmlStage.openImagesBrowser(null);
        if (sourceFile != null) {
            controller.loadImages(sourceFile.getParentFile(), 9);
        }
    }

    @FXML
    @Override
    public void popAction() {
        ImageViewerController controller = (ImageViewerController) FxmlStage.openStage(CommonValues.ImagePopupFxml);
        operation(controller);
    }

    @FXML
    public void statisticAction() {
        ImageAnalyseController controller = (ImageAnalyseController) FxmlStage.openStage(CommonValues.ImageAnalyseFxml);
        operation(controller);
    }

    @FXML
    public void ocrAction() {
        ImageOCRController controller = (ImageOCRController) FxmlStage.openStage(CommonValues.ImageOCRFxml);
        operation(controller);
    }

    @FXML
    public void splitAction() {
        ImageSplitController controller = (ImageSplitController) FxmlStage.openStage(CommonValues.ImageSplitFxml);
        operation(controller);
    }

    public void sampleAction() {
        ImageSampleController controller = (ImageSampleController) FxmlStage.openStage(CommonValues.ImageSampleFxml);
        operation(controller);
    }

    public void operation(BaseImageController controller) {
        if (imageView == null || imageView.getImage() == null || controller == null) {
            return;
        }
        if (maskRectangleLine == null || !maskRectangleLine.isVisible()) {
            if (imageChanged || (imageInformation != null && imageInformation.isIsScaled())) {
                controller.loadImage(image);
            } else {
                controller.loadImage(sourceFile, imageInformation, image);
            }
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image areaImage;

                @Override
                protected boolean handle() {
                    areaImage = cropImage();
                    if (areaImage == null) {
                        areaImage = imageView.getImage();
                    }
                    return areaImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    controller.loadImage(areaImage);
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void convertAction() {
        ImageConverterBatchController controller = (ImageConverterBatchController) FxmlStage.openStage(CommonValues.ImageConverterBatchFxml);
        if (sourceFile != null) {
            controller.tableController.addFile(sourceFile);
        }
    }

    @FXML
    public void settings() {
        SettingsController controller = (SettingsController) openStage(CommonValues.SettingsFxml);
        controller.setParentController(this);
        controller.parentFxml = myFxml;
        controller.tabPane.getSelectionModel().select(controller.imageTab);
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {
        try {
            if (image == null) {
                return;
            }
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("Manufacture"));
            menu.setOnAction((ActionEvent event) -> {
                manufactureAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Statistic"));
            menu.setOnAction((ActionEvent event) -> {
                statisticAction();

            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("OCR"));
            menu.setOnAction((ActionEvent event) -> {
                ocrAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Split"));
            menu.setOnAction((ActionEvent event) -> {
                splitAction();
            });
            popMenu.getItems().add(menu);

            menu = new MenuItem(message("Sample"));
            menu.setOnAction((ActionEvent event) -> {
                sampleAction();

            });
            popMenu.getItems().add(menu);

            if (sourceFile != null) {
                menu = new MenuItem(message("Convert"));
                menu.setOnAction((ActionEvent event) -> {
                    convertAction();
                });
                popMenu.getItems().add(menu);
            }

            if (imageInformation != null) {
                popMenu.getItems().add(new SeparatorMenuItem());

                menu = new MenuItem(message("Information"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    infoAction();
                });
                popMenu.getItems().add(menu);

                menu = new MenuItem(message("MetaData"));
                menu.setOnAction((ActionEvent menuItemEvent) -> {
                    popMetaData();
                });
                popMenu.getItems().add(menu);
                popMenu.getItems().add(new SeparatorMenuItem());
            }

            if (sourceFile != null) {
                menu = new MenuItem(message("Browse"));
                menu.setOnAction((ActionEvent event) -> {
                    browseAction();
                });
                popMenu.getItems().add(menu);
            }

            menu = new MenuItem(message("Settings"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                settings();

            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            FxmlControl.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
