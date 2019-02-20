package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.data.ImageFileInformation;
import mara.mybox.data.ImageInformation;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-11-28
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageSourcesController extends ImageViewerController {

    protected File targetFile;
    protected boolean isOpenning;
    protected SimpleBooleanProperty changed, hasSampled;

    protected ObservableList<ImageInformation> sourceImages = FXCollections.observableArrayList();

    @FXML
    protected Button saveAsButton, insertButton, viewButton, clearButton;
    @FXML
    protected TableView<ImageInformation> sourceTable;
    @FXML
    protected TableColumn<ImageInformation, Image> imageColumn;
    @FXML
    protected TableColumn<ImageInformation, String> fileColumn, sizeColumn, typeColumn;
    @FXML
    protected TableColumn<ImageInformation, Integer> indexColumn;
    @FXML
    protected VBox optionsBox, sourcesBox;
    @FXML
    protected CheckBox viewCheck;
    @FXML
    protected Label sourcesLabel;

    public ImageSourcesController() {
        fileExtensionFilter = CommonValues.ImageExtensionFilter;
    }

    @Override
    protected void initializeNext() {
        try {
            initSourceSection();
            initOptionsSection();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initSourceSection() {
        try {
            changed = new SimpleBooleanProperty(false);
            hasSampled = new SimpleBooleanProperty(false);
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
        insertButton.setDisable(none);
        upButton.setDisable(none);
        downButton.setDisable(none);
        deleteButton.setDisable(none);
        if (infoButton != null) {
            infoButton.setDisable(none);
        }
        if (metaButton != null) {
            metaButton.setDisable(none);
        }
        if (viewButton != null) {
            viewButton.setDisable(none);
        }
        if (none) {
            bottomLabel.setText("");
        } else {
            bottomLabel.setText(getMessage("DoubleClickToView"));
        }
    }

    protected void initOptionsSection() {
        try {

            optionsBox.setDisable(true);
            sourcesBox.setDisable(true);

            saveButton.disableProperty().bind(
                    Bindings.isEmpty(sourceImages)
            );

            saveAsButton.disableProperty().bind(
                    saveButton.disableProperty()
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void createAction(ActionEvent event) {
        try {
            if (!checkSaving()) {
                return;
            }
            sourceImages.clear();
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(sourcePathKey, file.getParent());
            targetFile = file;
            optionsBox.setDisable(false);
            sourcesBox.setDisable(false);
            getMyStage().setTitle(getBaseTitle() + "  " + targetFile.getAbsolutePath());

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    protected void openAction(ActionEvent event) {
        try {
            if (!checkSaving()) {
                return;
            }
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(sourcePathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
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
            if (!checkSaving()) {
                return;
            }
            sourceImages.clear();

            targetFile = file;
            optionsBox.setDisable(false);
            sourcesBox.setDisable(false);
            getMyStage().setTitle(getBaseTitle() + "  " + targetFile.getAbsolutePath());

            List<File> files = new ArrayList<>();
            files.add(file);
            isOpenning = true;
            addFiles(files, 0);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected boolean checkSaving() {
        if (changed.getValue() && !sourceImages.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("ImageChanged"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
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

    protected void setImageChanged(boolean c) {
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
        for (ImageInformation m : sourceImages) {
            pixels += m.getWidth() * m.getHeight();
        }
        sourcesLabel.setText(getMessage("TotalImages") + ":" + sourceImages.size() + "  "
                + getMessage("TotalPixels") + ":" + ValueTools.formatData(pixels));

        hasSampled.set(hasSampled());

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
    @Override
    public void saveAction() {
        if (targetFile == null) {
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
            targetFile = file;
        }
        saveFile(targetFile);
    }

    @FXML
    protected void saveAsAction() {
        try {
            if (!checkSaving()) {
                return;
            }
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
            saveFile(file);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void saveFile(final File outFile) {
        if (outFile == null || sourceImages.isEmpty()) {
            return;
        }
        if (hasSampled()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("SureSampled"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVaribles.getMessage("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSure) {
                saveFileDo(outFile);
            }
        } else {
            saveFileDo(outFile);
        }
    }

    protected void saveFileDo(final File outFile) {

    }

    @FXML
    protected void addAction(ActionEvent event) {
        addAction(sourceImages.size());
    }

    @FXML
    protected void insertAction(ActionEvent event) {
        int index = sourceTable.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            addAction(index);
        } else {
            insertButton.setDisable(true);
        }
    }

    protected void addAction(int index) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = AppVaribles.getUserConfigPath(sourcePathKey);
            if (defaultPath.exists()) {
                fileChooser.setInitialDirectory(defaultPath);
            }
            fileChooser.getExtensionFilters().addAll(CommonValues.ImageExtensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            String path = files.get(0).getParent();
            AppVaribles.setUserConfigValue(LastPathKey, path);
            AppVaribles.setUserConfigValue(sourcePathKey, path);
            addFiles(files, index);

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
                    if (task.isCancelled()) {
                        return null;
                    }
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
                        if (task.isCancelled()) {
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
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ret.isEmpty()) {
                            isSettingValues = true;
                            if (index < 0 || index >= sourceImages.size()) {
                                sourceImages.addAll(infos);
                            } else {
                                sourceImages.addAll(index, infos);
                            }
                            sourceTable.refresh();
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
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    protected void loadFile(final File file, List<ImageInformation> infos) {
        try {
            targetFile = file;
            optionsBox.setDisable(false);
            sourcesBox.setDisable(false);
            getMyStage().setTitle(getBaseTitle() + "  " + targetFile.getAbsolutePath());

            isSettingValues = true;
            sourceImages.clear();
            sourceImages.addAll(infos);
            sourceTable.refresh();
            isSettingValues = false;
            checkTableSelected();
            setImageChanged(false);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void deleteAction() {
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
    protected void clearAction(ActionEvent event) {
        sourceImages.clear();
        sourceTable.refresh();
    }

    @FXML
    protected void upAction(ActionEvent event) {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index == 0) {
                continue;
            }
            ImageInformation info = sourceImages.get(index);
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
    protected void downAction(ActionEvent event) {
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
            ImageInformation info = sourceImages.get(index);
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
    @Override
    public void infoAction() {
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
        openImageViewer(info);
    }

}
