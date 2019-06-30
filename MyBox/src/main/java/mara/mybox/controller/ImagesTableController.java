package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import mara.mybox.controller.base.TableController;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.StringTools;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-11-28
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesTableController extends TableController<ImageInformation> {

    public boolean isOpenning;
    public SimpleBooleanProperty changed, hasSampled;
    public Image image;

    public ImagesTableController() {

        fileExtensionFilter = CommonValues.ImageExtensionFilter;
    }

    @Override
    public void initializeNext() {
        try {
            initTable();
            changed = new SimpleBooleanProperty(false);
            hasSampled = new SimpleBooleanProperty(false);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void dataChanged() {
        if (!isSettingValues) {
            setImageChanged(true);
        }
    }

    public void setImageChanged(boolean c) {
        changed.setValue(c);
        if (changed.getValue()) {
            if (targetFile != null) {
                getMyStage().setTitle(getBaseTitle() + "  " + targetFile.getAbsolutePath() + " *");
            }
        } else {
            if (targetFile != null) {
                getMyStage().setTitle(getBaseTitle() + "  " + targetFile.getAbsolutePath());
            }
        }
        long pixels = 0;
        for (ImageInformation m : tableData) {

            pixels += m.getWidth() * m.getHeight();
        }
        tableLabel.setText(getMessage("TotalImages") + ":" + tableData.size() + "  "
                + getMessage("TotalPixels") + ":" + StringTools.formatData(pixels));

        hasSampled.set(hasSampled());

    }

    public boolean hasSampled() {
        for (ImageInformation info : tableData) {
            if (info.isIsSampled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addFiles(final int index, final List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        recordFileAdded(files.get(0));
        task = new Task<Void>() {
            private List<ImageInformation> infos;
            private String ret;
            private boolean hasSampled;
            private boolean ok;

            @Override
            public Void call() throws Exception {
                infos = new ArrayList<>();
                ret = "";
                hasSampled = false;
                for (File file : files) {
                    if (task == null || task.isCancelled()) {
                        return null;
                    }
                    final String fileName = file.getPath();
                    ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(fileName);
                    String format = finfo.getImageFormat();
                    if ("raw".equals(format)) {
                        continue;
                    }
                    if (!tableView.getColumns().contains(imageColumn)) {
                        for (int i = 0; i < finfo.getNumberOfImages(); i++) {
                            ImageInformation minfo = finfo.getImagesInformation().get(i);
                            if (minfo.isIsSampled()) {
                                hasSampled = true;
                            }
                            infos.add(minfo);

                        }

                    } else {
                        List<BufferedImage> bufferImages
                                = ImageFileReaders.readFrames(format, fileName, finfo.getImagesInformation());
                        if (bufferImages == null || bufferImages.isEmpty()) {
                            ret = "FailedReadFile";
                            break;
                        }
                        for (int i = 0; i < bufferImages.size(); i++) {
                            if (task == null || task.isCancelled()) {
                                return null;
                            }
                            ImageInformation minfo = finfo.getImagesInformation().get(i);
                            if (minfo.isIsSampled()) {
                                hasSampled = true;
                            }
                            image = SwingFXUtils.toFXImage(bufferImages.get(i), null);
                            minfo.setImage(image);
                            infos.add(minfo);
                        }
                    }
                }

                ok = true;
                return null;
            }

            @Override
            public void succeeded() {
                super.succeeded();
                if (ok) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ret.isEmpty()) {
                                isSettingValues = true;
                                if (index < 0 || index >= tableData.size()) {
                                    tableData.addAll(infos);
                                } else {
                                    tableData.addAll(index, infos);
                                }
                                tableView.refresh();
                                isSettingValues = false;
                                setImageChanged(!isOpenning);
                                isOpenning = false;
                                if (hasSampled) {
                                    bottomLabel.setText(AppVaribles.getMessage("ImageSampled"));
                                    alertWarning(AppVaribles.getMessage("ImageSampled"));
                                }
                            } else {
                                popError(AppVaribles.getMessage(ret));
                            }

                        }
                    });
                }
            }

        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public ImageInformation getData(File directory) {
        ImageInformation d = new ImageInformation(directory);
        return d;
    }

    @FXML
    @Override
    public void viewFileAction() {
        try {
            ImageInformation info = tableView.getSelectionModel().getSelectedItem();
            if (info == null) {
                return;
            }
            final ImageViewerController controller = FxmlStage.openImageViewer( null);
            if (controller != null) {
                controller.loadImage(info);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    public void infoAction() {
        ImageInformation info = tableView.getSelectionModel().getSelectedItem();
        if (info == null) {
            return;
        }
        FxmlStage.openImageInformation( null, info);
    }

    @FXML
    public void metaAction() {
        ImageInformation info = tableView.getSelectionModel().getSelectedItem();
        if (info == null) {
            return;
        }
        FxmlStage.openImageMetaData( null, info);
    }

}
