package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableImageCell;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonImageValues;

/**
 * @Author Mara
 * @CreateDate 2019-4-28
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesTableController extends TableController<ImageInformation> {

    protected boolean isOpenning;
    protected SimpleBooleanProperty hasSampled;
    protected Image image;

    @FXML
    protected TableColumn<ImageInformation, String> colorSpaceColumn, pixelsColumn;
    @FXML
    protected TableColumn<ImageInformation, Image> imageColumn;
    @FXML
    protected TableColumn<ImageInformation, Integer> indexColumn;
    @FXML
    protected CheckBox tableThumbCheck;

    public ImagesTableController() {
        SourceFileType = VisitHistory.FileType.Image;
        SourcePathType = VisitHistory.FileType.Image;
        TargetPathType = VisitHistory.FileType.Image;
        TargetFileType = VisitHistory.FileType.Image;
        AddFileType = VisitHistory.FileType.Image;
        AddPathType = VisitHistory.FileType.Image;

        targetPathKey = "ImageFilePath";
        sourcePathKey = "ImageFilePath";
        sourceExtensionFilter = CommonImageValues.ImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initTable() {
        super.initTable();
        hasSampled = new SimpleBooleanProperty(false);

        if (tableThumbCheck != null && imageColumn != null) {
            tableThumbCheck.setSelected(AppVariables.getUserConfigBoolean("ImageTableThumbnail", true));
            tableThumbCheck.selectedProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    if (tableThumbCheck.isSelected()) {
                        if (!tableView.getColumns().contains(imageColumn)) {
                            tableView.getColumns().add(0, imageColumn);
                        }
                    } else {
                        if (tableView.getColumns().contains(imageColumn)) {
                            tableView.getColumns().remove(imageColumn);
                        }
                    }
                    AppVariables.setUserConfigValue("ImageTableThumbnail", tableThumbCheck.isSelected());
                }
            });
        }
    }

    @Override
    public void initColumns() {
        try {
            super.initColumns();

            if (imageColumn != null) {
                imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
                imageColumn.setCellFactory(new TableImageCell());
            }

            if (pixelsColumn != null) {
                pixelsColumn.setCellValueFactory(new PropertyValueFactory<>("pixelsString"));
                pixelsColumn.setCellFactory(new Callback<TableColumn<ImageInformation, String>, TableCell<ImageInformation, String>>() {
                    @Override
                    public TableCell<ImageInformation, String> call(TableColumn<ImageInformation, String> param) {
                        TableCell<ImageInformation, String> cell = new TableCell<ImageInformation, String>() {
                            final Text text = new Text();

                            @Override
                            public void updateItem(final String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                    setGraphic(null);
                                    return;
                                }
                                text.setText(item);
                                ImageInformation info = tableData.get(getIndex());
                                if (info.isIsSampled()) {
                                    text.setFill(Color.RED);
                                }
                                setGraphic(text);
                            }
                        };
                        return cell;
                    }
                });
            }

            if (colorSpaceColumn != null) {
                colorSpaceColumn.setCellValueFactory(new PropertyValueFactory<>("colorSpace"));
            }

            if (indexColumn != null) {
                indexColumn.setCellValueFactory(new PropertyValueFactory<>("index"));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void tableChanged() {
        super.tableChanged();
        if (tableLabel != null) {
            long pixels = 0;
            for (ImageInformation m : tableData) {
                pixels += m.getWidth() * m.getHeight();
            }
            tableLabel.setText(MessageFormat.format(message("TotalFilesNumberSize"),
                    totalFilesNumber, FileTools.showFileSize(totalFilesSize)) + "  "
                    + message("TotalPixels") + ": " + StringTools.formatData(pixels) + "    "
                    + message("DoubleClickToView"));
        }
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
    protected ImageInformation create(File file) {
        ImageInformation d = new ImageInformation(file);
        return d;
    }

    @Override
    public void addFiles(final int index, final List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        recordFileAdded(files.get(0));
        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<ImageInformation> infos;
                private boolean hasSampled;

                @Override
                protected boolean handle() {
                    infos = new ArrayList<>();
                    error = null;
                    hasSampled = false;
                    for (File file : files) {
                        if (task == null || isCancelled()) {
                            return false;
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
                                error = "FailedReadFile";
                                break;
                            }
                            for (int i = 0; i < bufferImages.size(); i++) {
                                if (task == null || isCancelled()) {
                                    return false;
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

                    return error == null;
                }

                @Override
                protected void whenSucceeded() {
                    if (index < 0 || index >= tableData.size()) {
                        tableData.addAll(infos);
                    } else {
                        tableData.addAll(index, infos);
                    }
                    tableView.refresh();
                    isOpenning = false;
                    if (hasSampled) {
                        alertWarning(AppVariables.message("ImageSampled"));
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    @Override
    public void viewFileAction() {
        try {
            ImageInformation info = tableView.getSelectionModel().getSelectedItem();
            if (info == null) {
                return;
            }
            final ImageViewerController controller = FxmlStage.openImageViewer(null);
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
            info = tableData.get(0);
        }
        FxmlStage.openImageInformation(null, info);
    }

    @FXML
    public void metaAction() {
        ImageInformation info = tableView.getSelectionModel().getSelectedItem();
        if (info == null) {
            info = tableData.get(0);
        }
        FxmlStage.openImageMetaData(null, info);
    }

}
