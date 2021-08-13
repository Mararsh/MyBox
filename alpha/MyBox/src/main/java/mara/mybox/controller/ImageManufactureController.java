package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2019-8-12
 * @License Apache License Version 2.0
 */
public class ImageManufactureController extends ImageManufactureController_Actions {

    public ImageManufactureController() {
        baseTitle = Languages.message("ImageManufacture");
        TipsLabelKey = "ImageManufactureTips";
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            imageLoaded = new SimpleBooleanProperty(false);
            historyIndex = -1;
            imageHistoriesPath = AppVariables.getImageHisPath();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initCreatePane();
            initHistoriesTab();
            initBackupsTab();
            initEditBar();

            mainBox.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));
            rightPane.disableProperty().bind(Bindings.isNull(imageView.imageProperty()));

            operationsController.setParameters(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void initBackupsTab() {
        try {
            backupController.setControls(this, baseName);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            imageLoaded.set(true);
            imageChanged = false;
            resetImagePane();

            scopeController.setParameters(this);
            scopeSavedController.setParameters(this);
            operationsController.resetOperationPanes();

            recordImageHistory(ImageOperation.Load, image);
            updateLabel(Languages.message("Loaded"));

//            autoSize();
            backupController.loadBackups(sourceFile);

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

}
