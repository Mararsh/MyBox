package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.objects.ImageInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-11-30
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageFramesViewerController extends ImageBaseController {

    protected SimpleBooleanProperty changed;
    protected boolean isSettingValues;

    protected ObservableList<ImageInformation> sourceImages = FXCollections.observableArrayList();

    @FXML
    protected Button extractButton, infoButton, metaButton, viewButton, editButton;
    @FXML
    protected TableView<ImageInformation> sourceTable;
    @FXML
    protected TableColumn<ImageInformation, Image> imageColumn;
    @FXML
    protected TableColumn<ImageInformation, String> fileColumn, sizeColumn, typeColumn;
    @FXML
    protected TableColumn<ImageInformation, Integer> indexColumn;
    @FXML
    protected VBox sourcesBox;
    @FXML
    protected Label sourcesLabel;

    public ImageFramesViewerController() {
        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("tif/tiff/gif", "*.tif", "*.tiff", "*.gif"));
                add(new FileChooser.ExtensionFilter("tif", "*.tif", "*.tiff"));
                add(new FileChooser.ExtensionFilter("gif", "*.gif"));
            }
        };
    }

    @Override
    protected void initializeNext() {
        try {
            initSourceSection();
            sourcesBox.setDisable(true);
            editButton.setDisable(true);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initSourceSection() {
        try {
            changed = new SimpleBooleanProperty(false);
            sourcesLabel.setText("");

            fileColumn.setCellValueFactory(new PropertyValueFactory<ImageInformation, String>("filename"));
            typeColumn.setCellValueFactory(new PropertyValueFactory<ImageInformation, String>("colorSpace"));
            indexColumn.setCellValueFactory(new PropertyValueFactory<ImageInformation, Integer>("index"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<ImageInformation, String>("sizeString"));
            sizeColumn.setCellFactory(new Callback<TableColumn<ImageInformation, String>, TableCell<ImageInformation, String>>() {
                @Override
                public TableCell<ImageInformation, String> call(TableColumn<ImageInformation, String> param) {
                    TableCell<ImageInformation, String> cell = new TableCell<ImageInformation, String>() {
                        final Text text = new Text();

                        @Override
                        protected void updateItem(final String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                text.setText(item);
                                if (item.contains("*")) {
                                    text.setFill(Color.RED);
                                }
                                setGraphic(text);
                            }
                        }
                    };
                    return cell;
                }
            });
            imageColumn.setCellValueFactory(new PropertyValueFactory<ImageInformation, Image>("image"));
            imageColumn.setCellFactory(new Callback<TableColumn<ImageInformation, Image>, TableCell<ImageInformation, Image>>() {
                @Override
                public TableCell<ImageInformation, Image> call(TableColumn<ImageInformation, Image> param) {
                    final ImageView imageview = new ImageView();
                    imageview.setPreserveRatio(true);
                    imageview.setFitWidth(100);
                    imageview.setFitHeight(100);
                    TableCell<ImageInformation, Image> cell = new TableCell<ImageInformation, Image>() {
                        @Override
                        protected void updateItem(final Image item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                imageview.setImage(item);
                                setGraphic(imageview);
                            }
                        }
                    };
                    return cell;
                }
            });

            sourceTable.setItems(sourceImages);
            sourceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            sourceTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    if (!isSettingValues) {
                        checkTableSelected();
                    }
                }
            });
            sourceTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        viewAction();
                    }
                }
            });
            checkTableSelected();

            sourceImages.addListener(new ListChangeListener<ImageInformation>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends ImageInformation> change) {
                    if (!isSettingValues) {
                        setImageChanged(true);
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void checkTableSelected() {
        int selected = sourceTable.getSelectionModel().getSelectedIndex();
        boolean none = (selected < 0);
        infoButton.setDisable(none);
        metaButton.setDisable(none);
        viewButton.setDisable(none);
        extractButton.setDisable(none);
        if (none) {
            bottomLabel.setText("");
        } else {
            bottomLabel.setText(getMessage("DoubleClickToView"));
        }
    }

    @FXML
    protected void openAction(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigValue(sourcePathKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(sourcePathKey, file.getParent());
            openFile(file);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void openFile(final File file) {
        try {
            sourceImages.clear();

            sourceFile = file;
            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
            sourcesBox.setDisable(false);
            editButton.setDisable(false);

            List<File> files = new ArrayList<>();
            files.add(file);
            addFiles(files, 0);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void addFiles(final List<File> files, final int index) {
        if (files == null || files.isEmpty()) {
            return;
        }
        task = new Task<Void>() {
            private List<ImageInformation> infos;
            private String ret;
            private boolean hasSampled;

            @Override
            protected Void call() throws Exception {
                infos = new ArrayList<>();
                ret = "";
                hasSampled = false;
                for (File file : files) {
                    final String fileName = file.getPath();
                    ImageFileInformation finfo = ImageFileReaders.readImageFileMetaData(fileName);
                    String format = finfo.getImageFormat();
                    if ("raw".equals(format)) {
                        continue;
                    }
                    List<BufferedImage> bufferImages = ImageFileReaders.readFrames(format, fileName, finfo.getImagesInformation());
                    if (bufferImages == null || bufferImages.isEmpty()) {
                        ret = "FailedReadFile";
                        break;
                    }
                    for (int i = 0; i < bufferImages.size(); i++) {
                        ImageInformation minfo = finfo.getImagesInformation().get(i);
                        if (minfo.isIsSampled()) {
                            hasSampled = true;
                        }
                        image = SwingFXUtils.toFXImage(bufferImages.get(i), null);
                        minfo.setImage(image);
                        infos.add(minfo);
                    }
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ret.isEmpty()) {
                            if (hasSampled) {
                                alertWarning(AppVaribles.getMessage("ImageSampled"));
                                bottomLabel.setText(AppVaribles.getMessage("ImageSampled"));
                            }
                            isSettingValues = true;
                            if (index < 0 || index >= sourceImages.size()) {
                                sourceImages.addAll(infos);
                            } else {
                                sourceImages.addAll(index, infos);
                            }
                            sourceTable.refresh();
                            isSettingValues = false;
                            setImageChanged(true);
                        } else {
                            popError(AppVaribles.getMessage(ret));
                        }

                    }
                });
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    protected void setImageChanged(boolean c) {
        changed.setValue(c);
        long pixels = 0;
        for (ImageInformation m : sourceImages) {
            pixels += m.getWidth() * m.getHeight();
        }
        sourcesLabel.setText(getMessage("TotalImages") + ":" + sourceImages.size() + "  "
                + getMessage("TotalPixels") + ":" + ValueTools.formatData(pixels));
    }

    protected boolean hasSampled() {
        for (ImageInformation info : sourceImages) {
            if (info.isIsSampled()) {
                return true;
            }
        }
        return false;
    }

    @FXML
    protected void showInfo() {
        showImageInformation(sourceTable.getSelectionModel().getSelectedItem());
    }

    @FXML
    protected void showMeta() {
        showImageMetaData(sourceTable.getSelectionModel().getSelectedItem());
    }

    @FXML
    protected void viewAction() {
        ImageInformation info = sourceTable.getSelectionModel().getSelectedItem();
        if (info == null) {
            return;
        }
        showImageView(info);
    }

    @FXML
    protected void selectAll() {
        isSettingValues = true;
        sourceTable.getSelectionModel().selectAll();
        isSettingValues = false;
        checkTableSelected();
    }

    @FXML
    protected void unselectAll() {
        isSettingValues = true;
        sourceTable.getSelectionModel().clearSelection();
        isSettingValues = false;
        checkTableSelected();
    }

    @FXML
    protected void editAction() {
        try {
            String format = FileTools.getFileSuffix(sourceFile.getAbsolutePath()).toLowerCase();
            if (format.contains("tif")) {
                final ImageTiffEditerController controller
                        = (ImageTiffEditerController) openStage(CommonValues.ImageTiffEditerFxml, false, true);
                controller.setBaseTitle(AppVaribles.getMessage("ImageTiffEditer"));
                controller.loadFile(sourceFile, sourceImages);

            } else if (format.contains("gif")) {
                final ImageGifEditerController controller
                        = (ImageGifEditerController) openStage(CommonValues.ImageGifEditerFxml, false, true);
                controller.setBaseTitle(AppVaribles.getMessage("ImageGifEditer"));
                controller.loadFile(sourceFile, sourceImages);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void extractAction() {
        try {
            if (sourceFile == null || sourceImages.isEmpty()) {
                return;
            }
            final List<ImageInformation> selectedImages = new ArrayList<>();
            selectedImages.addAll(sourceTable.getSelectionModel().getSelectedItems());
            if (selectedImages.isEmpty()) {
                return;
            }

            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigValue(targetPathKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(CommonValues.ImageExtensionFilter);
            fileChooser.setTitle(getMessage("FilePrefixInput"));
            final File targetFile = fileChooser.showSaveDialog(getMyStage());
            if (targetFile == null) {
                return;
            }
            AppVaribles.setUserConfigValue(targetPathKey, targetFile.getParent());

            task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String format = FileTools.getFileSuffix(targetFile.getAbsolutePath()).toLowerCase();
                    String filePrefix = FileTools.getFilePrefix(targetFile.getAbsolutePath());
                    String filename;
                    int digit = (selectedImages.size() + "").length();
                    final List<String> filenames = new ArrayList<>();
                    for (int i = 0; i < selectedImages.size(); i++) {
                        filename = filePrefix + "-" + ValueTools.fillNumber(i, digit) + "." + format;
                        BufferedImage bufferedImage = ImageFileReaders.getBufferedImage(selectedImages.get(i));
                        if (bufferedImage == null) {
                            continue;
                        }
                        ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                        filenames.add(filename);
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            multipleFilesGenerated(filenames);
                        }
                    });
                    return null;
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
