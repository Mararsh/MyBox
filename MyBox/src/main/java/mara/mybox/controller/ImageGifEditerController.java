package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.image.ImageConvertTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.imagefile.ImageGifFile;
import static mara.mybox.objects.AppVaribles.getMessage;

/**
 * @Author Mara
 * @CreateDate 2018-11-16
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageGifEditerController extends ImageViewerController {

    private File targetFile;
    protected int currentIndex, interval, width, height;
    private boolean isSettingValues, keepSize;
    protected SimpleBooleanProperty changed;

    protected ObservableList<ImageFileInformation> sourceImages = FXCollections.observableArrayList();

    @FXML
    private SplitPane splitPane;
    @FXML
    private Button saveButton, deleteButton, saveAsButton, stopButton;
    @FXML
    private TableView<ImageFileInformation> sourceTable;
    @FXML
    private TableColumn<ImageFileInformation, Image> imageColumn;
    @FXML
    private TableColumn<ImageFileInformation, String> fileColumn, sizeColumn;
    @FXML
    private HBox toolBox;
    @FXML
    protected ComboBox<String> intervalCBox;
    @FXML
    protected ToggleGroup sizeGroup;
    @FXML
    protected TextField widthInput, heightInput;
    @FXML
    private CheckBox viewCheck, loopCheck;

    public ImageGifEditerController() {
        fileExtensionFilter = new ArrayList() {
            {
                add(new FileChooser.ExtensionFilter("*.gif", "*.GIF"));
            }
        };
    }

    @Override
    protected void initializeNext() {
        try {
            initSourceSection();
            initOptionsSection();
            splitPane.setDisable(true);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initSourceSection() {
        try {
            changed = new SimpleBooleanProperty(false);

            fileColumn.setCellValueFactory(new PropertyValueFactory<ImageFileInformation, String>("filename"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<ImageFileInformation, String>("pixels"));
            imageColumn.setCellValueFactory(new PropertyValueFactory<ImageFileInformation, Image>("image"));
            imageColumn.setCellFactory(new Callback<TableColumn<ImageFileInformation, Image>, TableCell<ImageFileInformation, Image>>() {
                @Override
                public TableCell<ImageFileInformation, Image> call(TableColumn<ImageFileInformation, Image> param) {
                    final ImageView imageview = new ImageView();
                    imageview.setPreserveRatio(true);
                    imageview.setFitWidth(100);
                    imageview.setFitHeight(100);
                    TableCell<ImageFileInformation, Image> cell = new TableCell<ImageFileInformation, Image>() {
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
            checkTableSelected();

            sourceImages.addListener(new ListChangeListener<ImageFileInformation>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends ImageFileInformation> change) {
                    if (!isSettingValues) {
                        setImageChanged(true);
                    }
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkTableSelected() {
        ObservableList<ImageFileInformation> selected = sourceTable.getSelectionModel().getSelectedItems();
        if (selected != null && selected.size() > 0) {
            upButton.setDisable(false);
            downButton.setDisable(false);
            deleteButton.setDisable(false);
        } else {
            upButton.setDisable(true);
            downButton.setDisable(true);
            deleteButton.setDisable(true);
        }
    }

    private void initOptionsSection() {
        try {
            imageView.setPreserveRatio(true);

            interval = 500;
            List<String> values = Arrays.asList("500", "300", "1000", "2000", "3000", "5000", "10000");
            intervalCBox.getItems().addAll(values);
            intervalCBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v > 0) {
                            interval = v;
                            intervalCBox.getEditor().setStyle(null);
                            if (!isSettingValues && targetFile != null) {
                                setImageChanged(true);
                            }
                        } else {

                            intervalCBox.getEditor().setStyle(badStyle);
                        }
                    } catch (Exception e) {
                        intervalCBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            intervalCBox.getSelectionModel().select(0);

            sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkSizeType();
                }
            });
            checkSizeType();

            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkSize();
                }
            });

            heightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkSize();
                }
            });

            toolBox.disableProperty().bind(
                    Bindings.isEmpty(sourceImages)
            );
            saveButton.disableProperty().bind(
                    Bindings.isEmpty(sourceImages)
                            //                    Bindings.not(changed)
                            .or(widthInput.styleProperty().isEqualTo(badStyle))
                            .or(heightInput.styleProperty().isEqualTo(badStyle))
            );

            saveAsButton.disableProperty().bind(
                    saveButton.disableProperty()
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkSizeType() {
        RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
        if (getMessage("KeepImagesSize").equals(selected.getText())) {
            keepSize = true;
            widthInput.setStyle(null);
            heightInput.setStyle(null);
        } else if (getMessage("AllSetAs").equals(selected.getText())) {
            keepSize = false;
            checkSize();
        }
    }

    private void checkSize() {
        try {
            int v = Integer.valueOf(widthInput.getText());
            if (v > 0) {
                width = v;
                widthInput.setStyle(null);
            } else {
                widthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            widthInput.setStyle(badStyle);
        }

        try {
            int v = Integer.valueOf(heightInput.getText());
            if (v > 0) {
                height = v;
                heightInput.setStyle(null);
            } else {
                heightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            heightInput.setStyle(badStyle);
        }

    }

    @FXML
    private void createAction(ActionEvent event) {
        try {
            if (!checkSaving()) {
                return;
            }
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigValue(targetPathKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(targetPathKey, file.getParent());
            targetFile = file;
            splitPane.setDisable(false);
            getMyStage().setTitle(AppVaribles.getMessage("ImageGifEditer") + "  "
                    + targetFile.getAbsolutePath());

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    private void openAction(ActionEvent event) {
        try {
            if (!checkSaving()) {
                return;
            }
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigValue(targetPathKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(targetPathKey, file.getParent());
            openFile(file);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void openFile(final File file) {
        try {
            targetFile = file;
            splitPane.setDisable(false);
            getMyStage().setTitle(AppVaribles.getMessage("ImageGifEditer") + "  "
                    + targetFile.getAbsolutePath());

            final String fileName = targetFile.getPath();
            task = new Task<Void>() {
                private List<ImageFileInformation> infos;

                @Override
                protected Void call() throws Exception {
                    infos = new ArrayList<>();
                    List<BufferedImage> bimages = ImageGifFile.readGifFile(fileName);
                    if (bimages != null) {
                        ImageFileInformation info;
                        for (int i = 0; i < bimages.size(); i++) {
                            info = new ImageFileInformation();
                            info.setFilename(AppVaribles.getMessage("Index") + " " + i);
                            info.setPixels(bimages.get(i).getWidth() + "x" + bimages.get(i).getHeight());
                            Image m = SwingFXUtils.toFXImage(bimages.get(i), null);
                            info.setImage(m);
                            info.setBufferedImage(bimages.get(i));
                            infos.add(info);
                        }
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            isSettingValues = true;
                            setImageChanged(false);
                            sourceImages.clear();
                            sourceImages.addAll(infos);
                            sourceTable.refresh();
                            isSettingValues = false;
                            showGif();
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

    private boolean checkSaving() {
        if (changed.getValue() && !sourceImages.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("ImageChanged"));
            ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("Save"));
            ButtonType buttonNotSave = new ButtonType(AppVaribles.getMessage("NotSave"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSave, buttonNotSave, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                saveAction();
                return true;
            } else if (result.get() == buttonNotSave) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void setImageChanged(boolean c) {
        changed.setValue(c);
        if (changed.getValue()) {
            if (targetFile != null) {
                getMyStage().setTitle(AppVaribles.getMessage("ImageGifEditer") + "  "
                        + targetFile.getAbsolutePath() + " *");
            }
            showGif();
        } else {
            if (targetFile != null) {
                getMyStage().setTitle(AppVaribles.getMessage("ImageGifEditer") + "  "
                        + targetFile.getAbsolutePath());
            }
        }
    }

    @FXML
    private void saveAction() {
        save(targetFile);
        setImageChanged(false);
    }

    @FXML
    private void saveAsAction() {
        try {
            if (!checkSaving()) {
                return;
            }
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigValue(targetPathKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(targetPathKey, file.getParent());
            save(file);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    private void save(final File file) {
        if (file == null || sourceImages.isEmpty()) {
            return;
        }
        try {
            task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        List<BufferedImage> images = new ArrayList<>();
                        for (ImageFileInformation info : sourceImages) {
                            BufferedImage image = info.getBufferedImage();
                            if (keepSize) {
                                images.add(image);
                            } else {
                                images.add(ImageConvertTools.scaleImage(image, width, height));
                            }
                        }
                        boolean ok = ImageGifFile.writeGifImagesFile(images, file, interval, loopCheck.isSelected());
                        if (!ok) {
                            return null;
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if (viewCheck.isSelected()) {
                                    try {
                                        final ImageGifViewerController controller
                                                = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml, false, true);
                                        controller.loadImage(file.getAbsolutePath());
                                    } catch (Exception e) {
                                        logger.error(e.toString());
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
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

    @FXML
    private void addAction(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = new File(AppVaribles.getUserConfigValue(sourcePathKey, CommonValues.UserFilePath));
            if (!defaultPath.isDirectory()) {
                defaultPath = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(defaultPath);
            fileChooser.getExtensionFilters().addAll(CommonValues.ImageExtensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            String path = files.get(0).getParent();
            AppVaribles.setUserConfigValue(LastPathKey, path);
            AppVaribles.setUserConfigValue(sourcePathKey, path);
            addFiles(files);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void addFiles(final List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        task = new Task<Void>() {
            private List<ImageFileInformation> infos;

            @Override
            protected Void call() throws Exception {
                infos = new ArrayList<>();
                for (File file : files) {
                    final String fileName = file.getPath();
                    ImageFileInformation info = ImageFileReaders.readImageMetaData(fileName);
                    BufferedImage bufferImage;
                    String format = FileTools.getFileSuffix(fileName).toLowerCase();
                    if (!"raw".equals(format)) {
                        bufferImage = ImageIO.read(file);
                        Image image = SwingFXUtils.toFXImage(bufferImage, null);
                        info.setImage(image);
                        info.setBufferedImage(bufferImage);
                        infos.add(info);
                    }
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        isSettingValues = true;
                        sourceImages.addAll(infos);
                        sourceTable.refresh();
                        isSettingValues = false;
                        setImageChanged(true);
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

    @FXML
    private void deleteAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < 0 || index > sourceImages.size() - 1) {
                continue;
            }
            sourceImages.remove(index);
        }
        sourceTable.refresh();
    }

    @FXML
    private void clearAction(ActionEvent event) {
        sourceImages.clear();
        sourceTable.refresh();
    }

    @FXML
    private void upAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index == 0) {
                continue;
            }
            ImageFileInformation info = sourceImages.get(index);
            sourceImages.set(index, sourceImages.get(index - 1));
            sourceImages.set(index - 1, info);
        }
        for (Integer index : selected) {
            if (index > 0) {
                sourceTable.getSelectionModel().select(index - 1);
            }
        }
        sourceTable.refresh();
    }

    @FXML
    private void downAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index == sourceImages.size() - 1) {
                continue;
            }
            ImageFileInformation info = sourceImages.get(index);
            sourceImages.set(index, sourceImages.get(index + 1));
            sourceImages.set(index + 1, info);
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < sourceImages.size() - 1) {
                sourceTable.getSelectionModel().select(index + 1);
            }
        }
        sourceTable.refresh();
    }

    @FXML
    public void stopAction() {
        try {
            if (stopButton.getText().equals(AppVaribles.getMessage("Stop"))) {
                if (timer != null) {
                    timer.cancel();
                }
                stopButton.setText(AppVaribles.getMessage("Start"));
            } else if (stopButton.getText().equals(AppVaribles.getMessage("Start"))) {
                showGif();
                stopButton.setText(AppVaribles.getMessage("Stop"));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void showGif() {
        imageView.setImage(null);
        bottomLabel.setText("");
        if (timer != null) {
            timer.cancel();
            stopButton.setText(AppVaribles.getMessage("Start"));
        }
        if (sourceImages.isEmpty()) {
            return;
        }
        int x = 0, y = 0;
        for (ImageFileInformation info : sourceImages) {
            x += info.getImage().getWidth();
            y += info.getImage().getHeight();
        }
        bottomLabel.setText(AppVaribles.getMessage("TotalSize") + ": " + x + "x" + y);
        currentIndex = 0;
        image = sourceImages.get(currentIndex).getImage();
        imageView.setFitHeight(scrollPane.getHeight() - 5);
        imageView.setFitWidth(scrollPane.getWidth() - 1);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (sourceImages.isEmpty()) {
                            timer.cancel();
                            stopButton.setText(AppVaribles.getMessage("Start"));
                            return;
                        }
                        if (currentIndex > sourceImages.size() - 1) {
                            if (!loopCheck.isSelected()) {
                                timer.cancel();
                                stopButton.setText(AppVaribles.getMessage("Start"));
                                return;
                            }
                            currentIndex -= sourceImages.size();
                        } else if (currentIndex < 0) {
                            currentIndex += sourceImages.size();
                        }
                        imageView.setImage(sourceImages.get(currentIndex).getImage());
                        currentIndex++;
                    }
                });
            }
        }, 0, interval);
    }

}
