package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-10-12
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureFileController extends ImageManufactureController {

    final protected String ImageSaveConfirmKey;

    public ImageManufactureFileController() {

        ImageSaveConfirmKey = "ImageSaveConfirmKey";
    }

    @Override
    public void initializeNext2() {
        try {
            initCommon();
            initFileTab();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initFileTab() {
        try {

            saveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setUserConfigValue(ImageSaveConfirmKey, saveCheck.isSelected());
                    values.setIsConfirmBeforeSave(saveCheck.isSelected());
                }
            });
            saveCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageSaveConfirmKey));
            values.setIsConfirmBeforeSave(saveCheck.isSelected());

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
            tabPane.getSelectionModel().select(fileTab);

            infoButton.setDisable(imageInformation == null);
            metaButton.setDisable(imageInformation == null);
            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @Override
    public void afterInfoLoaded() {
        super.afterInfoLoaded();
        saveCheck.setDisable(true);
    }

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();
            saveCheck.setDisable(false);

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

}
