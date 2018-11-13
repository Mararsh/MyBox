package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageConvertTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageCombine;
import mara.mybox.objects.ImageCombine.ArrayType;
import mara.mybox.objects.ImageCombine.CombineSizeType;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.tools.FileTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.fxml.FxmlImageTools;

/**
 * @Author Mara
 * @CreateDate 2018-8-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesCombineController extends ImageViewerController {

    protected String ImageCombineArrayTypeKey, ImageCombineCombineSizeTypeKey, ImageCombineColumnsKey, ImageCombineIntervalKey, ImageCombineMarginsKey;
    protected String ImageCombineEachWidthKey, ImageCombineEachHeightKey, ImageCombineTotalWidthKey, ImageCombineTotalHeightKey;
    protected String ImageCombineBgColorKey;

    private ImageCombine imageCombine;
    private File targetFile;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab fileTab, sizeTab, arrayTab;
    @FXML
    private ToggleGroup sizeGroup, arrayGroup;
    @FXML
    private RadioButton arrayColumnRadio, arrayRowRadio, arrayColumnsRadio;
    @FXML
    private RadioButton keepSizeRadio, sizeBiggerRadio, sizeSmallerRadio, eachWidthRadio, eachHeightRadio, totalWidthRadio, totalHeightRadio;
    @FXML
    private TextField totalWidthInput, totalHeightInput, eachWidthInput, eachHeightInput;
    @FXML
    private ComboBox<String> targetTypeBox, columnsBox, intervalBox, MarginsBox;
    @FXML
    private ColorPicker bgPicker;
    @FXML
    private SplitPane splitPane;
    @FXML
    private Button addButton, openTargetButton, saveButton, deleteButton, clearButton;
    @FXML
    private TableView<ImageFileInformation> sourceTable;
    @FXML
    private TableColumn<ImageFileInformation, Image> imageColumn;
    @FXML
    private TableColumn<ImageFileInformation, String> fileColumn, sizeColumn;
    @FXML
    private VBox mainPane, imagesPane;
    @FXML
    private HBox toolBox;

    public ImagesCombineController() {
        ImageCombineArrayTypeKey = "ImageCombineArrayTypeKey";
        ImageCombineCombineSizeTypeKey = "ImageCombineCombineSizeTypeKey";
        ImageCombineEachWidthKey = "ImageCombineEachWidthKey";
        ImageCombineEachHeightKey = "ImageCombineEachHeightKey";
        ImageCombineTotalWidthKey = "ImageCombineTotalWidthKey";
        ImageCombineTotalHeightKey = "ImageCombineTotalHeightKey";
        ImageCombineColumnsKey = "ImageCombineColumnsKey";
        ImageCombineIntervalKey = "ImageCombineIntervalKey";
        ImageCombineMarginsKey = "ImageCombineMarginsKey";
        ImageCombineBgColorKey = "ImageCombineBgColorKey";
    }

    @Override
    protected void initializeNext() {
        try {
            imageCombine = new ImageCombine();
            initSourceSection();
            initArraySection();
            initSizeSection();
            initTargetSection();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initSourceSection() {
        try {
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

            sourceTable.setItems(imageCombine.getSourceImages());
            sourceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            sourceTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkTableSelected();
                }
            });
            checkTableSelected();

            imageCombine.getSourceImages().addListener(new ListChangeListener<ImageFileInformation>() {
                @Override
                public void onChanged(ListChangeListener.Change<? extends ImageFileInformation> change) {
                    combineImages();
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

    private void initArraySection() {
        try {
            columnsBox.getItems().addAll(Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9", "10"));
            columnsBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int columnsValue = Integer.valueOf(newValue);
                        if (columnsValue > 0) {
                            imageCombine.setColumnsValue(columnsValue);
                            columnsBox.getEditor().setStyle(null);
                            AppVaribles.setConfigValue(ImageCombineColumnsKey, columnsValue + "");
                            combineImages();
                        } else {
                            imageCombine.setColumnsValue(-1);
                            columnsBox.getEditor().setStyle(badStyle);
                        }

                    } catch (Exception e) {
                        imageCombine.setColumnsValue(-1);
                        columnsBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            columnsBox.getSelectionModel().select(AppVaribles.getConfigValue(ImageCombineColumnsKey, "2"));

            intervalBox.getItems().addAll(Arrays.asList("5", "10", "15", "20", "1", "3", "30", "0"));
            intervalBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int intervalValue = Integer.valueOf(newValue);
                        if (intervalValue >= 0) {
                            imageCombine.setIntervalValue(intervalValue);
                            intervalBox.getEditor().setStyle(null);
                            AppVaribles.setConfigValue(ImageCombineIntervalKey, intervalValue + "");
                            combineImages();
                        } else {
                            imageCombine.setIntervalValue(-1);
                            intervalBox.getEditor().setStyle(badStyle);
                        }

                    } catch (Exception e) {
                        imageCombine.setIntervalValue(-1);
                        intervalBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            intervalBox.getSelectionModel().select(AppVaribles.getConfigValue(ImageCombineIntervalKey, "5"));

            MarginsBox.getItems().addAll(Arrays.asList("5", "10", "15", "20", "1", "3", "30", "0"));
            MarginsBox.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int MarginsValue = Integer.valueOf(newValue);
                        if (MarginsValue >= 0) {
                            imageCombine.setMarginsValue(MarginsValue);
                            MarginsBox.getEditor().setStyle(null);
                            AppVaribles.setConfigValue(ImageCombineMarginsKey, MarginsValue + "");
                            combineImages();
                        } else {
                            imageCombine.setMarginsValue(-1);
                            MarginsBox.getEditor().setStyle(badStyle);
                        }

                    } catch (Exception e) {
                        imageCombine.setMarginsValue(-1);
                        MarginsBox.getEditor().setStyle(badStyle);
                    }
                }
            });
            MarginsBox.getSelectionModel().select(AppVaribles.getConfigValue(ImageCombineMarginsKey, "5"));

            bgPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> ov,
                        Color oldValue, Color newValue) {
                    imageCombine.setBgColor(newValue);
                    AppVaribles.setConfigValue(ImageCombineBgColorKey, newValue.toString());
                    combineImages();
                }
            });
            bgPicker.setValue(Color.web(AppVaribles.getConfigValue(ImageCombineBgColorKey, Color.WHITE.toString())));

            arrayGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    RadioButton selected = (RadioButton) arrayGroup.getSelectedToggle();
                    if (AppVaribles.getMessage("SingleColumn").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.SingleColumn);
                        columnsBox.setDisable(true);
                        AppVaribles.setConfigValue(ImageCombineArrayTypeKey, "SingleColumn");
                    } else if (AppVaribles.getMessage("SingleRow").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.SingleRow);
                        columnsBox.setDisable(true);
                        AppVaribles.setConfigValue(ImageCombineArrayTypeKey, "SingleRow");
                    } else if (AppVaribles.getMessage("ColumnsNumber").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.ColumnsNumber);
                        columnsBox.setDisable(false);
                        AppVaribles.setConfigValue(ImageCombineArrayTypeKey, "ColumnsNumber");
                    }
                    combineImages();
                }
            });
            String arraySelect = AppVaribles.getConfigValue(ImageCombineArrayTypeKey, "SingleColumn");
            switch (arraySelect) {
                case "SingleColumn":
                    arrayColumnRadio.setSelected(true);
                    break;
                case "SingleRow":
                    arrayRowRadio.setSelected(true);
                    break;
                case "ColumnsNumber":
                    arrayColumnsRadio.setSelected(true);
                    break;
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initSizeSection() {
        try {
            eachWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkEachWidthValue();
                }
            });
            eachWidthInput.setText(AppVaribles.getConfigValue(ImageCombineEachWidthKey, ""));

            eachHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkEachHeightValue();
                }
            });
            eachHeightInput.setText(AppVaribles.getConfigValue(ImageCombineEachHeightKey, ""));

            totalWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkTotalWidthValue();
                }
            });
            totalWidthInput.setText(AppVaribles.getConfigValue(ImageCombineTotalWidthKey, ""));

            totalHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkTotalHeightValue();
                }
            });
            totalHeightInput.setText(AppVaribles.getConfigValue(ImageCombineTotalHeightKey, ""));

            sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    totalWidthInput.setDisable(true);
                    totalWidthInput.setStyle(null);
                    totalHeightInput.setDisable(true);
                    totalHeightInput.setStyle(null);
                    eachWidthInput.setDisable(true);
                    eachWidthInput.setStyle(null);
                    eachHeightInput.setDisable(true);
                    eachHeightInput.setStyle(null);
                    RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
                    if (AppVaribles.getMessage("KeepSize").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.KeepSize);
                        AppVaribles.setConfigValue(ImageCombineCombineSizeTypeKey, "KeepSize");
                        combineImages();
                    } else if (AppVaribles.getMessage("AlignAsBigger").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.AlignAsBigger);
                        AppVaribles.setConfigValue(ImageCombineCombineSizeTypeKey, "AlignAsBigger");
                        combineImages();
                    } else if (AppVaribles.getMessage("AlignAsSmaller").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.AlignAsSmaller);
                        AppVaribles.setConfigValue(ImageCombineCombineSizeTypeKey, "AlignAsSmaller");
                        combineImages();
                    } else if (AppVaribles.getMessage("EachWidth").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.EachWidth);
                        eachWidthInput.setDisable(false);
                        checkEachWidthValue();
                        AppVaribles.setConfigValue(ImageCombineCombineSizeTypeKey, "EachWidth");
                    } else if (AppVaribles.getMessage("EachHeight").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.EachHeight);
                        eachHeightInput.setDisable(false);
                        checkEachHeightValue();
                        AppVaribles.setConfigValue(ImageCombineCombineSizeTypeKey, "EachHeight");
                    } else if (AppVaribles.getMessage("TotalWidth").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.TotalWidth);
                        totalWidthInput.setDisable(false);
                        checkTotalWidthValue();
                        AppVaribles.setConfigValue(ImageCombineCombineSizeTypeKey, "TotalWidth");
                    } else if (AppVaribles.getMessage("TotalHeight").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.TotalHeight);
                        totalHeightInput.setDisable(false);
                        checkTotalHeightValue();
                        AppVaribles.setConfigValue(ImageCombineCombineSizeTypeKey, "TotalHeight");
                    }
                }
            });
            String arraySelect = AppVaribles.getConfigValue(ImageCombineCombineSizeTypeKey, "KeepSize");
            switch (arraySelect) {
                case "KeepSize":
                    keepSizeRadio.setSelected(true);
                    break;
                case "AlignAsBigger":
                    sizeBiggerRadio.setSelected(true);
                    break;
                case "AlignAsSmaller":
                    sizeSmallerRadio.setSelected(true);
                    break;
                case "EachWidth":
                    eachWidthRadio.setSelected(true);
                    break;
                case "EachHeight":
                    eachHeightRadio.setSelected(true);
                    break;
                case "TotalWidth":
                    totalWidthRadio.setSelected(true);
                    break;
                case "TotalHeight":
                    totalHeightRadio.setSelected(true);
                    break;
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void checkEachWidthValue() {
        try {
            int eachWidthValue = Integer.valueOf(eachWidthInput.getText());
            if (eachWidthValue > 0) {
                imageCombine.setEachWidthValue(eachWidthValue);
                eachWidthInput.setStyle(null);
                AppVaribles.setConfigValue(ImageCombineEachWidthKey, eachWidthValue + "");
                combineImages();
            } else {
                imageCombine.setEachWidthValue(-1);
                eachWidthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            imageCombine.setEachWidthValue(-1);
            eachWidthInput.setStyle(badStyle);
        }
    }

    private void checkEachHeightValue() {
        try {
            int eachHeightValue = Integer.valueOf(eachHeightInput.getText());
            if (eachHeightValue > 0) {
                imageCombine.setEachHeightValue(eachHeightValue);
                eachHeightInput.setStyle(null);
                AppVaribles.setConfigValue(ImageCombineEachHeightKey, eachHeightValue + "");
                combineImages();
            } else {
                imageCombine.setEachHeightValue(-1);
                eachHeightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            imageCombine.setEachHeightValue(-1);
            eachHeightInput.setStyle(badStyle);
        }
    }

    private void checkTotalWidthValue() {
        try {
            int totalWidthValue = Integer.valueOf(totalWidthInput.getText());
            if (totalWidthValue > 0) {
                imageCombine.setTotalWidthValue(totalWidthValue);
                totalWidthInput.setStyle(null);
                AppVaribles.setConfigValue(ImageCombineTotalWidthKey, totalWidthValue + "");
                combineImages();
            } else {
                imageCombine.setTotalWidthValue(-1);
                totalWidthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            imageCombine.setTotalWidthValue(-1);
            totalWidthInput.setStyle(badStyle);
        }
    }

    private void checkTotalHeightValue() {
        try {
            int totalHeightValue = Integer.valueOf(totalHeightInput.getText());
            if (totalHeightValue > 0) {
                imageCombine.setTotalHeightValue(totalHeightValue);
                totalHeightInput.setStyle(null);
                AppVaribles.setConfigValue(ImageCombineTotalHeightKey, totalHeightValue + "");
                combineImages();
            } else {
                imageCombine.setTotalHeightValue(-1);
                totalHeightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            imageCombine.setTotalHeightValue(-1);
            totalHeightInput.setStyle(badStyle);
        }
    }

    private void initTargetSection() {
        try {
            toolBox.disableProperty().bind(
                    Bindings.isEmpty(imageCombine.getSourceImages())
            );

            saveButton.disableProperty().bind(
                    Bindings.isEmpty(imageCombine.getSourceImages())
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void saveAction(ActionEvent event) {
        if (image == null) {
            return;
        }
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(targetPathKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setConfigValue(targetPathKey, file.getParent());
            targetFile = file;

            Task saveTask = new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    try {
                        String filename = targetFile.getAbsolutePath();
                        String format = FileTools.getFileSuffix(filename);
                        final BufferedImage bufferedImage = FxmlImageTools.getBufferedImage(image);
                        ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    return null;
                }
            };
            openHandlingStage(saveTask, Modality.WINDOW_MODAL);
            Thread thread = new Thread(saveTask);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void newWindow(ActionEvent event) {
        showImageView(image);
//        if (targetFile != null) {
//            openImageManufactureInNew(targetFile.getAbsolutePath());
//        } else if (image != null) {
//            showImageView(image);
//        }
    }

    @FXML
    private void bgTransparent(ActionEvent event) {
        bgPicker.setValue(Color.TRANSPARENT);
    }

    @FXML
    private void bgWhite(ActionEvent event) {
        bgPicker.setValue(Color.WHITE);
    }

    @FXML
    private void bgBlack(ActionEvent event) {
        bgPicker.setValue(Color.BLACK);
    }

    @FXML
    private void addAction(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = new File(AppVaribles.getConfigValue(sourcePathKey, CommonValues.UserFilePath));
            if (!defaultPath.isDirectory()) {
                defaultPath = new File(CommonValues.UserFilePath);
            }
            fileChooser.setInitialDirectory(defaultPath);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            String path = files.get(0).getParent();
            AppVaribles.setConfigValue(LastPathKey, path);
            AppVaribles.setConfigValue(sourcePathKey, path);
            loadImages(files);

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void loadImages(final List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        Task loadTask = new Task<Void>() {

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
                        infos.add(info);
                    }
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (infos == null || infos.isEmpty()) {
                            return;
                        }
                        imageCombine.getSourceImages().addAll(infos);
                        sourceTable.refresh();
                    }
                });
                return null;
            }
        };
        openHandlingStage(loadTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(loadTask);
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
            if (index < 0 || index > imageCombine.getSourceImages().size() - 1) {
                continue;
            }
            imageCombine.getSourceImages().remove(index);
        }
        sourceTable.refresh();
    }

    @FXML
    private void clearAction(ActionEvent event) {
        imageCombine.getSourceImages().clear();
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
            ImageFileInformation info = imageCombine.getSourceImages().get(index);
            imageCombine.getSourceImages().set(index, imageCombine.getSourceImages().get(index - 1));
            imageCombine.getSourceImages().set(index - 1, info);
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
            if (index == imageCombine.getSourceImages().size() - 1) {
                continue;
            }
            ImageFileInformation info = imageCombine.getSourceImages().get(index);
            imageCombine.getSourceImages().set(index, imageCombine.getSourceImages().get(index + 1));
            imageCombine.getSourceImages().set(index + 1, info);
        }
        for (int i = selected.size() - 1; i >= 0; i--) {
            int index = selected.get(i);
            if (index < imageCombine.getSourceImages().size() - 1) {
                sourceTable.getSelectionModel().select(index + 1);
            }
        }
        sourceTable.refresh();
    }

    private void combineImages() {
        if (imageCombine.getSourceImages() == null || imageCombine.getSourceImages().isEmpty()) {
            clearButton.setDisable(true);
        } else {
            clearButton.setDisable(false);
        }
        if (imageCombine.getSourceImages() == null || imageCombine.getSourceImages().isEmpty()
                || totalWidthInput.getStyle().equals(badStyle)
                || totalHeightInput.getStyle().equals(badStyle)
                || eachWidthInput.getStyle().equals(badStyle)
                || eachHeightInput.getStyle().equals(badStyle)) {
            image = null;
            imageView.setImage(null);
            bottomLabel.setText("");
            return;
        }

        bottomLabel.setText(AppVaribles.getMessage("Loading..."));

        List<ImageFileInformation> sources = imageCombine.getSourceImages();
        if (imageCombine.getArrayType() == ArrayType.SingleColumn) {
            image = ImageConvertTools.combineSingleColumn(imageCombine, sources, false, true);
        } else if (imageCombine.getArrayType() == ArrayType.SingleRow) {
            image = ImageConvertTools.combineSingleRow(imageCombine, sources, false, true);
        } else if (imageCombine.getArrayType() == ArrayType.ColumnsNumber) {
            image = combineImagesColumns(sources);
        } else {
            image = null;
        }
        if (image == null) {
            return;
        }
        imageView.setImage(image);
        fitSize();
        bottomLabel.setText(AppVaribles.getMessage("CombinedSize") + ": "
                + (int) image.getWidth() + "x" + (int) image.getHeight());
    }

    public Image combineImagesColumns(List<ImageFileInformation> images) {
        if (images == null || images.isEmpty() || imageCombine.getColumnsValue() <= 0) {
            return null;
        }
        try {
            List<ImageFileInformation> rowImages = new ArrayList();
            List<ImageFileInformation> rows = new ArrayList();
            for (ImageFileInformation imageInfo : images) {
                rowImages.add(imageInfo);
                if (rowImages.size() == imageCombine.getColumnsValue()) {
                    Image rowImage = ImageConvertTools.combineSingleRow(imageCombine, rowImages, true, false);
                    rows.add(new ImageFileInformation(rowImage));
                    rowImages = new ArrayList();
                }
            }
            if (!rowImages.isEmpty()) {
                Image rowImage = ImageConvertTools.combineSingleRow(imageCombine, rowImages, true, false);
                rows.add(new ImageFileInformation(rowImage));
            }
            Image newImage = ImageConvertTools.combineSingleColumn(imageCombine, rows, true, true);
            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

}
