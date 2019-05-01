package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import java.io.File;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import mara.mybox.data.ControlStyle;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureRefController extends ImageManufactureController {

    final protected String ImageReferenceDisplayKey;

    @FXML
    protected ToolBar refBar;
    @FXML
    protected CheckBox refSyncCheck;
    @FXML
    protected Button originalImageButton;

    public ImageManufactureRefController() {
        ImageReferenceDisplayKey = "ImageReferenceDisplay";
    }

    @Override
    public void initializeNext2() {
        try {
            initCommon();
            initReferenceTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void initInterface() {
        try {
            if (values == null || values.getImage() == null) {
                return;
            }
            super.initInterface();

            isSettingValues = true;
            tabPane.getSelectionModel().select(refTab);

            refSyncCheck.setSelected(values.isRefSync());
            refBar.setDisable(!values.isShowRef());

            isSettingValues = false;
            ControlStyle.setStyle(selectSourceButton);
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initReferenceTab() {
        try {

            refSyncCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    values.setRefSync(refSyncCheck.isSelected());
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void checkReferencePane() {
        super.checkReferencePane();
        refBar.setDisable(!values.isShowRef());
    }

    @FXML
    public void selectReference() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            referenceSelected(file);

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    public void referenceSelected(final File file) {
        try {
            if (file == null) {
                return;
            }
            values.setRefFile(file);
            recordFileOpened(file);

            loadReferenceImage();

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    public void popRefFile(MouseEvent event) {
        if (popMenu != null && popMenu.isShowing()) {
            popMenu.hide();
            popMenu = null;
        }
        int fileNumber = AppVaribles.fileRecentNumber * 2 / 3 + 1;
        List<VisitHistory> his = VisitHistory.getRecentFile(SourceFileType, fileNumber);
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
                    referenceSelected(new File(fname));
                }
            });
            popMenu.getItems().add(menu);
        }

        int pathNumber = AppVaribles.fileRecentNumber / 3 + 1;
        his = VisitHistory.getRecentPath(SourcePathType, pathNumber);
        if (his != null) {
            popMenu.getItems().add(new SeparatorMenuItem());
            for (VisitHistory h : his) {
                final String pathname = h.getResourceValue();
                MenuItem menu = new MenuItem(pathname);
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        AppVaribles.setUserConfigValue(sourcePathKey, pathname);
                        selectReference();
                    }
                });
                popMenu.getItems().add(menu);
            }
        }

        popMenu.getItems().add(new SeparatorMenuItem());
        MenuItem menu = new MenuItem(getMessage("MenuClose"));
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
    public void originalImage() {
        values.setRefFile(values.getSourceFile());
        loadReferenceImage();
    }

    @FXML
    public void popRefInformation() {
        showImageInformation(values.getRefInfo());
    }

    @FXML
    public void popRefMeta() {
        showImageMetaData(values.getRefInfo());
    }

}
