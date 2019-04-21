package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;

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
            values.setRefFile(file);
            recordFileOpened(file);

            loadReferenceImage();

        } catch (Exception e) {
//            logger.error(e.toString());
        }
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
