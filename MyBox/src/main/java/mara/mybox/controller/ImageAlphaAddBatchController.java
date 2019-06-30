package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import mara.mybox.controller.base.ImageManufactureBatchController;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageAlphaAddBatchController extends ImageManufactureBatchController {

    private float opacityValue;
    private boolean useOpacityValue;
    private BufferedImage alphaImage;
    private AlphaBlendMode blendMode;

    public static enum AlphaBlendMode {
        Set, KeepOriginal, Plus
    }

    @FXML
    private ToggleGroup alphaGroup, alphaAddGroup;
    @FXML
    private HBox alphaFileBox;
    @FXML
    private RadioButton opacityRadio;
    @FXML
    private ComboBox<String> opacityBox;

    public ImageAlphaAddBatchController() {
        baseTitle = AppVaribles.getMessage("ImageAlphaAdd");

        fileExtensionFilter = CommonValues.AlphaImageExtensionFilter;

    }

    @Override
    public void initializeNext2() {
        try {
            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(Bindings.isEmpty(targetPathInput.textProperty())
                    .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                    .or(sourceFileInput.styleProperty().isEqualTo(badStyle))
                    .or(opacityBox.getEditor().styleProperty().isEqualTo(badStyle))
                    .or(Bindings.isEmpty(tableView.getItems()))
            );

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void initOptionsSection() {
        try {
            super.initOptionsSection();

            alphaGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkOpacityType();
                }
            });
            checkOpacityType();

            opacityBox.getItems().addAll(Arrays.asList("50", "10", "60", "80", "100", "90", "20", "30"));
            opacityBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkOpacity();
                }
            });
            opacityBox.getSelectionModel().select(0);

            alphaAddGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkOpacityAdd();
                }
            });
            checkOpacityAdd();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkOpacityType() {
        alphaFileBox.setDisable(true);
        sourceFileInput.setStyle(null);
        opacityBox.setDisable(true);
        FxmlControl.setEditorNormal(opacityBox);

        useOpacityValue = opacityRadio.isSelected();
        if (useOpacityValue) {
            opacityBox.setDisable(false);
            checkOpacity();

        } else {
            alphaFileBox.setDisable(false);
            checkSourceFileInput();

        }
    }

    private void checkOpacity() {
        try {
            int v = Integer.valueOf(opacityBox.getValue());
            if (v >= 0 && v <= 100) {
                opacityValue = v / 100f;
                FxmlControl.setEditorNormal(opacityBox);
            } else {
                FxmlControl.setEditorBadStyle(opacityBox);
            }
        } catch (Exception e) {
            FxmlControl.setEditorBadStyle(opacityBox);
        }
    }

    private void checkOpacityAdd() {
        String selected = ((RadioButton) alphaAddGroup.getSelectedToggle()).getText();
        if (getMessage("Plus").equals(selected)) {
            blendMode = AlphaBlendMode.Plus;
        } else if (getMessage("Keep").equals(selected)) {
            blendMode = AlphaBlendMode.KeepOriginal;
        } else {
            blendMode = AlphaBlendMode.Set;
        }
    }

    @FXML
    public void popAlphaFile(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        int fileNumber = AppVaribles.fileRecentNumber * 2 / 3 + 1;
        List<VisitHistory> his;
        his = VisitHistory.getRecentAlphaImages(fileNumber);
        if (his == null || his.isEmpty()) {
            return;
        }
        popMenu = new ContextMenu();
        popMenu.setAutoHide(true);
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

        int pathNumber = AppVaribles.fileRecentNumber / 3 + 1;
        his = VisitHistory.getRecentPath(SourcePathType, pathNumber);
        if (his != null) {
            popMenu.getItems().add(new SeparatorMenuItem());
            MenuItem dmenu = new MenuItem(getMessage("RecentAccessedDirectories"));
            dmenu.setStyle("-fx-text-fill: #2e598a;");
            popMenu.getItems().add(dmenu);
            for (VisitHistory h : his) {
                final String pathname = h.getResourceValue();
                MenuItem menu = new MenuItem(pathname);
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        AppVaribles.setUserConfigValue(sourcePathKey, pathname);
                        selectSourceFile(event);
                    }
                });
                popMenu.getItems().add(menu);
            }
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

    @Override
    public void makeMoreParameters() {
        super.makeMoreParameters();
        if (!useOpacityValue) {
            alphaImage = ImageFileReaders.readImage(sourceFile);
        }
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            if (source.getColorModel().hasAlpha() && blendMode == AlphaBlendMode.KeepOriginal) {
                errorString = getMessage("NeedNotHandle");
                return null;
            }
            BufferedImage target;
            if (useOpacityValue) {
                target = ImageManufacture.addAlpha(source, opacityValue, blendMode == AlphaBlendMode.Plus);
            } else {
                target = ImageManufacture.addAlpha(source, alphaImage, blendMode == AlphaBlendMode.Plus);
            }
            return target;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

}
