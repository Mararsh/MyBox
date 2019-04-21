package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import javafx.fxml.FXML;
import javafx.scene.control.ToolBar;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-10-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBrowseController extends ImageManufactureController {

    final protected String ImageSortTypeKey;

    @FXML
    protected ToolBar navBar;

    public ImageManufactureBrowseController() {
        ImageSortTypeKey = "ImageSortType";
    }

    @Override
    public void initializeNext2() {
        try {
            initCommon();
            initBrowseTab();
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
            tabPane.getSelectionModel().select(browseTab);

            if (sourceFile != null && navBox != null) {
                navBox.setDisable(false);
                makeImageNevigator();
            }

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initBrowseTab() {
        try {

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void nextAction() {
        if (!checkSavingForNextAction()) {
            return;
        }
        if (nextFile != null) {
            setImageChanged(false);
            loadImage(nextFile.getAbsoluteFile());
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (!checkSavingForNextAction()) {
            return;
        }
        if (previousFile != null) {
            setImageChanged(false);
            loadImage(previousFile.getAbsoluteFile());
        }
    }

}
