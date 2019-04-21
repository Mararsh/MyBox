package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import mara.mybox.fxml.ImageManufacture;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-10-12
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureFileController extends ImageManufactureController {

    final protected String ImageSaveAsKey, ImageSaveConfirmKey;

    @FXML
    protected ToolBar saveAsBar;
    @FXML
    protected ChoiceBox saveAsOptions;

    public static class SaveAsType {

        public static int Load = 0;
        public static int Open = 1;
        public static int None = 2;
    }

    public ImageManufactureFileController() {
        ImageSaveAsKey = "ImageSaveAsKey";
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

            List<String> optionsList = Arrays.asList(getMessage("LoadAfterSaveAs"),
                    getMessage("OpenAfterSaveAs"), getMessage("JustSaveAs"));
            saveAsOptions.getItems().addAll(optionsList);
            saveAsOptions.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue ov, Number oldValue, Number newValue) {
                    switch (newValue.intValue()) {
                        case 0:
                            AppVaribles.setUserConfigValue(ImageSaveAsKey, SaveAsType.Load + "");
                            values.setSaveAsType(SaveAsType.Load);
                            break;
                        case 1:
                            AppVaribles.setUserConfigValue(ImageSaveAsKey, SaveAsType.Open + "");
                            values.setSaveAsType(SaveAsType.Open);
                            break;
                        case 2:
                            AppVaribles.setUserConfigValue(ImageSaveAsKey, SaveAsType.None + "");
                            values.setSaveAsType(SaveAsType.None);
                            break;
                        default:
                            break;
                    }
                }
            });
            try {
                int vv = AppVaribles.getUserConfigInt(ImageSaveAsKey, SaveAsType.Load);
                values.setSaveAsType(vv);
                if (vv == SaveAsType.Load) {
                    saveAsOptions.getSelectionModel().select(0);
                } else if (vv == SaveAsType.Open) {
                    saveAsOptions.getSelectionModel().select(1);
                } else if (vv == SaveAsType.None) {
                    saveAsOptions.getSelectionModel().select(2);
                }
            } catch (Exception e) {
                logger.error(e.toString());
            }

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

    @FXML
    @Override
    public void saveAsAction() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(targetPathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(targetPathKey, file.getParent());

            task = new Task<Void>() {
                private boolean ok;

                @Override
                protected Void call() throws Exception {
                    String format = FileTools.getFileSuffix(file.getName());
                    final BufferedImage bufferedImage = ImageManufacture.getBufferedImage(imageView.getImage());
                    if (task == null || task.isCancelled()) {
                        return null;
                    }
                    ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());

                    ok = true;
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    if (ok) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (values.getSourceFile() == null
                                        || values.getSaveAsType() == ImageManufactureFileController.SaveAsType.Load) {
                                    sourceFileChanged(file);

                                } else if (values.getSaveAsType() == ImageManufactureFileController.SaveAsType.Open) {
                                    openImageManufacture(file.getAbsolutePath());
                                }
                                popInformation(AppVaribles.getMessage("Successful"));
                            }
                        });
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
