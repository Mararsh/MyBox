package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import mara.mybox.controller.base.ImageManufactureController;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureRefController extends ImageManufactureController {

    final protected String ImageReferenceDisplayKey;

    @FXML
    protected HBox refButtonsBox;
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
            refButtonsBox.setDisable(!showRefCheck.isSelected());

            isSettingValues = false;

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
        refButtonsBox.setDisable(!showRefCheck.isSelected());
    }

    @FXML
    public void selectReference() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(sourceExtensionFilter);
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
        if ( AppVaribles.fileRecentNumber <= 0 ) return;   new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return recentSourceFiles();
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectReference();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    handleSelect();
                    return;
                }
                referenceSelected(file);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

        }.pop();
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
