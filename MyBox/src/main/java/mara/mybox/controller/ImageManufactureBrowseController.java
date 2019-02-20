package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToolBar;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.value.AppVaribles.getMessage;

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
    protected void initializeNext2() {
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

            if (sourceFile != null && navBox != null) {
                navBox.setDisable(false);
                checkImageNevigator();
            }

            isSettingValues = false;
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    protected void initBrowseTab() {
        try {
            sortGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkImageNevigator();
                    RadioButton selected = (RadioButton) sortGroup.getSelectedToggle();
                    AppVaribles.setUserConfigValue(ImageSortTypeKey, selected.getText());
                }
            });
            FxmlTools.setRadioSelected(sortGroup, AppVaribles.getUserConfigValue(ImageSortTypeKey, getMessage("FileName")));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void nextAction() {
        if (!checkSavingBeforeExit()) {
            return;
        }
        if (nextFile != null) {
            setImageChanged(false);
            loadImage(nextFile.getAbsoluteFile(), false);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (!checkSavingBeforeExit()) {
            return;
        }
        if (previousFile != null) {
            setImageChanged(false);
            loadImage(previousFile.getAbsoluteFile(), false);
        }
    }

}
