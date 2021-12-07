package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import mara.mybox.bufferedimage.ImageInformation;
import mara.mybox.bufferedimage.TransformTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.ImageViewTools;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2021-8-21
 * @License Apache License Version 2.0
 */
public abstract class ImagesBrowserController_Action extends ImagesBrowserController_Load {

    public void zoomIn(int index) {
        try {
            if (index >= imageViewList.size()) {
                return;
            }
            ImageView iView = imageViewList.get(index);
            ImageViewTools.zoomIn((ScrollPane) iView.getUserData(), iView, 5, 5);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void zoomOut(int index) {
        try {
            if (index >= imageViewList.size()) {
                return;
            }
            ImageView iView = imageViewList.get(index);
            ImageViewTools.zoomOut((ScrollPane) iView.getUserData(), iView, 5, 5);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void paneSize(int index) {
        try {
            if (index >= imageViewList.size()) {
                return;
            }
            ImageView iView = imageViewList.get(index);
            ImageViewTools.paneSize((ScrollPane) iView.getUserData(), iView);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void imageSize(int index) {
        try {
            if (index >= imageViewList.size()) {
                return;
            }
            ImageView iView = imageViewList.get(index);
            ImageViewTools.imageSize((ScrollPane) iView.getUserData(), iView);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void info(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation imageInfo = tableData.get(index);
            if (imageInfo != null) {
                ControllerTools.showImageInformation(imageInfo);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void meta(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation imageInfo = tableData.get(index);
            if (imageInfo != null) {
                ControllerTools.showImageMetaData(imageInfo);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void view(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation imageInfo = tableData.get(index);
            if (imageInfo != null) {
                File file = imageInfo.getImageFileInformation().getFile();
                ControllerTools.openImageViewer(null, file);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void delete(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
                if (!PopTools.askSure(getMyStage().getTitle(), Languages.message("SureDelete"))) {
                    return;
                }
            }
            ImageInformation info = tableData.get(index);
            File file = info.getImageFileInformation().getFile();
            if (FileDeleteTools.delete(file)) {
                imageFileList.remove(file);
            }
            popSuccessful();
            makeImagesNevigator(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void selectAllImages() {
        if (displayMode == DisplayMode.ThumbnailsList || displayMode == DisplayMode.FilesList) {
            tableView.getSelectionModel().selectAll();
        } else {
            selectedIndexes.clear();
            for (int i = 0; i < imageBoxList.size(); i++) {
                selectedIndexes.add(i);
                VBox vbox = imageBoxList.get(i);
                vbox.setStyle("-fx-background-color:dodgerblue;-fx-text-fill:white;");
            }
        }
    }

    @FXML
    public void selectNoneImages() {
        if (displayMode == DisplayMode.ThumbnailsList || displayMode == DisplayMode.FilesList) {
            tableView.getSelectionModel().clearSelection();
        } else {
            selectedIndexes.clear();
            for (int i = 0; i < imageBoxList.size(); i++) {
                VBox vbox = imageBoxList.get(i);
                vbox.setStyle(null);
            }
        }
    }

    @FXML
    public void rotateRightImages() {
        rotateImages(90);
    }

    @FXML
    public void rotateLeftImages() {
        rotateImages(270);
    }

    @FXML
    public void turnOverImages() {
        rotateImages(180);
    }

    public void rotateImages(int rotateAngle) {
        if (saveRotationCheck.isSelected()) {
            saveRotation(selectedIndexes, rotateAngle);
        } else {
            rotateImages(selectedIndexes, rotateAngle);
        }
    }

    public void rotateImages(int index, int rotateAngle) {
        List<Integer> indexs = new ArrayList<>();
        indexs.add(index);
        if (saveRotationCheck.isSelected()) {
            saveRotation(indexs, rotateAngle);
        } else {
            rotateImages(indexs, rotateAngle);
        }
    }

    public void rotateImages(List<Integer> indexs, int rotateAngle) {
        switch (displayMode) {
            case FilesList:
                break;
            case ThumbnailsList:
                if (indexs == null || indexs.isEmpty()) {
                    for (int i = 0; i < tableData.size(); ++i) {
                        ImageInformation info = tableData.get(i);
                        if (info.isIsMultipleFrames()) {
                            continue;
                        }
                        info.setThumbnailRotation(info.getThumbnailRotation() + rotateAngle);
                        tableData.set(i, info);
                    }
                } else {
                    for (int i = 0; i < indexs.size(); ++i) {
                        int index = indexs.get(i);
                        ImageInformation info = tableData.get(index);
                        if (info.isIsMultipleFrames()) {
                            continue;
                        }
                        info.setThumbnailRotation(info.getThumbnailRotation() + rotateAngle);
                        tableData.set(index, info);
                    }
                }
                tableView.refresh();
                if (indexs != null) {
                    for (int i = 0; i < indexs.size(); ++i) {
                        tableView.getSelectionModel().select(indexs.get(i));
                    }
                }
                break;
            case ImagesGrid:
                if (indexs == null || indexs.isEmpty()) {
                    for (int i = 0; i < imageViewList.size(); ++i) {
                        ImageView iView = imageViewList.get(i);
                        iView.setRotate(iView.getRotate() + rotateAngle);
                    }
                } else {
                    for (int i = 0; i < indexs.size(); ++i) {
                        int index = indexs.get(i);
                        ImageView iView = imageViewList.get(index);
                        iView.setRotate(iView.getRotate() + rotateAngle);
                    }
                }
                break;
        }
    }

    public void saveRotation(List<Integer> indexs, int rotateAngle) {
        if (!saveRotationCheck.isSelected()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                private int handled = 0;
                private boolean hasMultipleFrames = false;

                @Override
                protected boolean handle() {
                    if (indexs == null || indexs.isEmpty()) {
                        for (int i = 0; i < tableData.size(); ++i) {
                            ImageInformation info = tableData.get(i);
                            if (info.isIsMultipleFrames()) {
                                hasMultipleFrames = true;
                                continue;
                            }
                            ImageInformation newInfo = saveRotation(info, rotateAngle);
                            if (newInfo == null) {
                                continue;
                            }
                            if (displayMode == DisplayMode.ImagesGrid) {
                                newInfo.loadThumbnail(thumbWidth);
                            } else if (displayMode == DisplayMode.ThumbnailsList) {
                                newInfo.loadThumbnail();
                            }
                            tableData.set(i, newInfo);
                            handled++;
                        }
                    } else {
                        for (int i = 0; i < indexs.size(); ++i) {
                            int index = indexs.get(i);
                            ImageInformation info = tableData.get(index);
                            if (info.isIsMultipleFrames()) {
                                hasMultipleFrames = true;
                                continue;
                            }
                            ImageInformation newInfo = saveRotation(info, rotateAngle);
                            if (newInfo == null) {
                                continue;
                            }
                            if (displayMode == DisplayMode.ImagesGrid) {
                                newInfo.loadThumbnail(thumbWidth);
                            } else if (displayMode == DisplayMode.ThumbnailsList) {
                                newInfo.loadThumbnail();
                            }
                            tableData.set(index, newInfo);
                            handled++;
                        }
                    }
                    return true;
                }

                private ImageInformation saveRotation(ImageInformation info, int rotateAngle) {
                    if (info == null || info.getImageFileInformation() == null || info.isIsMultipleFrames()) {
                        return null;
                    }
                    try {
                        File file = info.getImageFileInformation().getFile();
                        BufferedImage bufferedImage = ImageInformation.readBufferedImage(info);
                        bufferedImage = TransformTools.rotateImage(bufferedImage, rotateAngle);
                        ImageFileWriters.writeImageFile(bufferedImage, file);
                        ImageInformation newInfo = loadInfo(file);
                        return newInfo;
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                        return null;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    if (hasMultipleFrames) {
                        popError(Languages.message("CanNotHandleMultipleFrames"));
                    }
                    if (handled == 0) {
                        return;
                    }
                    if (displayMode == DisplayMode.ImagesGrid) {
                        if (indexs == null || indexs.isEmpty()) {
                            for (int i = 0; i < tableData.size(); ++i) {
                                ImageView iView = imageViewList.get(i);
                                iView.setImage(tableData.get(i).getThumbnail());
                            }
                        } else {
                            for (int i = 0; i < indexs.size(); ++i) {
                                int index = indexs.get(i);
                                ImageView iView = imageViewList.get(index);
                                iView.setImage(tableData.get(index).getThumbnail());
                            }

                        }
                    } else {
                        tableView.refresh();
                        if (indexs != null) {
                            for (int i = 0; i < indexs.size(); ++i) {
                                tableView.getSelectionModel().select(indexs.get(i));
                            }
                        }
                    }
                    popSaved();
                    if (indexs == null || indexs.isEmpty() || indexs.contains(currentIndex)) {
                        imageInformation.setThumbnail(null);
                        loadImageFile(imageInformation.getFile(), loadWidth, 0);
                    }
                }

            };
            start(task);
        }
    }

    @FXML
    @Override
    public void nextAction() {
        if (nextFiles != null) {
            previousFiles = imageFileList;
            imageFileList.clear();
            imageFileList.addAll(nextFiles);
            makeImagesNevigator(false);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (previousFiles != null) {
            nextFiles = imageFileList;
            imageFileList.clear();
            imageFileList.addAll(previousFiles);
            makeImagesNevigator(false);
        }
    }

    @Override
    public void fileRenamed(File newFile) {
        fileRenamed(currentIndex, newFile);
    }

    public void rename(int index) {
        try {
            if (index >= tableData.size()) {
                return;
            }
            ImageInformation info = tableData.get(index);
            if (info == null) {
                return;
            }
            File file = info.getImageFileInformation().getFile();
            FileRenameController controller = (FileRenameController) WindowTools.openStage(Fxmls.FileRenameFxml);
            controller.getMyStage().setOnHiding((WindowEvent event) -> {
                File newFile = controller.getNewFile();
                Platform.runLater(() -> {
                    fileRenamed(index, newFile);
                });
            });
            controller.set(file);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void fileRenamed(int index, File newFile) {
        try {
            if (newFile == null) {
                return;
            }
            ImageInformation info = tableData.get(index);
            if (info == null) {
                return;
            }
            File file = info.getImageFileInformation().getFile();
            changeFile(info, newFile);
            tableData.set(index, info);
            imageFileList.set(index, newFile);
            if (displayMode == DisplayMode.ImagesGrid) {
                imageTitleList.get(index).setText(newFile.getName());
            } else if (displayMode == DisplayMode.FilesList || displayMode == DisplayMode.ThumbnailsList) {
                tableView.refresh();
            }
            if (index == currentIndex) {
                super.fileRenamed(newFile);
                filenameLabel.setText(newFile.getAbsolutePath());
            } else {
                recordFileOpened(newFile);
                popInformation(MessageFormat.format(Languages.message("FileRenamed"), file.getAbsolutePath(), newFile.getAbsolutePath()));
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void deleteAction() {
        try {
            if (selectedIndexes == null || selectedIndexes.isEmpty()) {
                return;
            }
            if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
                if (!PopTools.askSure(getMyStage().getTitle(), Languages.message("SureDelete"))) {
                    return;
                }
            }
            int count = 0;
            for (int index : selectedIndexes) {
                ImageInformation info = tableData.get(index);
                File file = info.getImageFileInformation().getFile();
                if (FileDeleteTools.delete(file)) {
                    imageFileList.remove(file);
                    count++;
                }
            }
            popInformation(Languages.message("TotalDeletedFiles") + ": " + count);
            makeImagesNevigator(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void filesListAction(ActionEvent event) {
        colsnumBox.getSelectionModel().select(Languages.message("FilesList"));
    }

    @FXML
    protected void thumbsListAction(ActionEvent event) {
        colsnumBox.getSelectionModel().select(Languages.message("ThumbnailsList"));
    }

    @FXML
    protected void gridAction(ActionEvent event) {
        colsnumBox.getSelectionModel().select("" + (colsNum > 0 ? colsNum : 3));
    }

    @FXML
    public void zoomOutAll() {
        if (displayMode != ImagesBrowserController_Load.DisplayMode.ImagesGrid) {
            return;
        }
        if (selectedIndexes == null || selectedIndexes.isEmpty()) {
            for (int i = 0; i < imageViewList.size(); ++i) {
                zoomOut(i);
            }
        } else {
            for (int i = 0; i < selectedIndexes.size(); ++i) {
                zoomOut(selectedIndexes.get(i));
            }
        }
    }

    @FXML
    public void zoomInAll() {
        if (displayMode != ImagesBrowserController_Load.DisplayMode.ImagesGrid) {
            return;
        }
        if (selectedIndexes == null || selectedIndexes.isEmpty()) {
            for (int i = 0; i < imageViewList.size(); ++i) {
                zoomIn(i);
            }
        } else {
            for (int i = 0; i < selectedIndexes.size(); ++i) {
                zoomIn(selectedIndexes.get(i));
            }
        }
    }

    @FXML
    public void loadedSizeAll() {
        if (displayMode != ImagesBrowserController_Load.DisplayMode.ImagesGrid) {
            return;
        }
        if (selectedIndexes == null || selectedIndexes.isEmpty()) {
            for (int i = 0; i < imageViewList.size(); ++i) {
                imageSize(i);
            }
        } else {
            for (int i = 0; i < selectedIndexes.size(); ++i) {
                imageSize(selectedIndexes.get(i));
            }
        }
    }

    @FXML
    public void paneSizeAll() {
        if (displayMode != ImagesBrowserController_Load.DisplayMode.ImagesGrid) {
            return;
        }
        if (selectedIndexes == null || selectedIndexes.isEmpty()) {
            for (int i = 0; i < imageViewList.size(); ++i) {
                paneSize(i);
            }
        } else {
            for (int i = 0; i < selectedIndexes.size(); ++i) {
                paneSize(selectedIndexes.get(i));
            }
        }
    }

}
