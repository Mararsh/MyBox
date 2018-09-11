package mara.mybox.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageConvertionTools;
import mara.mybox.image.ImageGrayTools;
import mara.mybox.imagefile.ImageFileReaders;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.ImageFileInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlImageTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.PdfTools;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * @Author Mara
 * @CreateDate 2018-9-8
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesCombinePdfController extends ImageBaseController {

    private final ObservableList<ImageFileInformation> sourceImages = FXCollections.observableArrayList();
    private final String ImagesCombinePdfPathKey, ImageCombineLoadImagesKey, ImageCombineTargetPathKey;
    private final String ImageCombineSizeKey, ImageCombineMarginsKey;
    private final int snapSize = 150;
    private int marginSize, pageWidth, pageHeight, jpegQuality, format, threshold;
    private File targetFile;
    private PDRectangle pageSize;
    private boolean isImageSize;

    @FXML
    private Button addButton, openTargetButton, saveButton, deleteButton, clearButton;
    @FXML
    private TableView<ImageFileInformation> sourceTable;
    @FXML
    private TableColumn<ImageFileInformation, Image> imageColumn;
    @FXML
    private TableColumn<ImageFileInformation, String> fileColumn, pixelsColumn, modifyTimeColumn, sizeColumn;
    @FXML
    private ColorPicker bgPicker;
    @FXML
    private CheckBox loadCheck, pageNumberCheck;
    @FXML
    private ComboBox<String> MarginsBox, standardSizeBox, standardDpiBox, jpegBox, fontBox;
    @FXML
    private ToggleGroup sizeGroup, formatGroup;
    @FXML
    private TextField customWidthInput, customHeightInput, authorInput, thresholdInput, headerInput;
    @FXML
    private HBox sizeBox;

    public static class PdfImageFormat {

        public static int Original = 0;
        public static int Tiff = 1;
        public static int Jpeg = 2;

    }

    public ImagesCombinePdfController() {
        ImagesCombinePdfPathKey = "ImagesCombinePdfPathKey";
        ImageCombineLoadImagesKey = "ImageCombineLoadImagesKey";
        ImageCombineTargetPathKey = "ImageCombineTargetPathKey";
        ImageCombineMarginsKey = "ImageCombineMarginsKey";
        ImageCombineSizeKey = "ImageCombineSizeKey";
    }

    @Override
    protected void initializeNext() {
        try {
            initSourceSection();
            initTargetSection();
            initOptionsSection();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initSourceSection() {
        try {
            fileColumn.setCellValueFactory(new PropertyValueFactory<ImageFileInformation, String>("filename"));
            pixelsColumn.setCellValueFactory(new PropertyValueFactory<ImageFileInformation, String>("pixels"));
            modifyTimeColumn.setCellValueFactory(new PropertyValueFactory<ImageFileInformation, String>("modifyTime"));
            sizeColumn.setCellValueFactory(new PropertyValueFactory<ImageFileInformation, String>("fileSize"));
            imageColumn.setCellValueFactory(new PropertyValueFactory<ImageFileInformation, Image>("image"));
            imageColumn.setCellFactory(new Callback<TableColumn<ImageFileInformation, Image>, TableCell<ImageFileInformation, Image>>() {
                @Override
                public TableCell<ImageFileInformation, Image> call(TableColumn<ImageFileInformation, Image> param) {
                    final ImageView imageview = new ImageView();
                    imageview.setPreserveRatio(true);
                    imageview.setFitWidth(snapSize);
                    imageview.setFitHeight(snapSize);
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
            sourceTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (event.getClickCount() > 1) {
                        openAction();
                    }
                }
            });

            loadCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setConfigValue(ImageCombineLoadImagesKey, loadCheck.isSelected());
                }
            });
            loadCheck.setSelected(AppVaribles.getConfigBoolean(ImageCombineLoadImagesKey));

            Tooltip tips = new Tooltip(getMessage("LoadImagesComments"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(loadCheck, tips);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initTargetSection() {

        targetFileInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                openTargetButton.setDisable(true);
                try {
                    targetFile = new File(newValue);
                    if (!newValue.toLowerCase().endsWith(".pdf")) {
                        targetFile = null;
                        targetFileInput.setStyle(badStyle);
                        return;
                    }
                    targetFileInput.setStyle(null);
                    AppVaribles.setConfigValue(ImageCombineTargetPathKey, targetFile.getParent());
                } catch (Exception e) {
                    targetFile = null;
                    targetFileInput.setStyle(badStyle);
                }
            }
        });
        saveButton.disableProperty().bind(
                Bindings.isEmpty(sourceImages)
                        .or(Bindings.isEmpty(targetFileInput.textProperty()))
                        .or(targetFileInput.styleProperty().isEqualTo(badStyle))
                        .or(customWidthInput.styleProperty().isEqualTo(badStyle))
                        .or(customHeightInput.styleProperty().isEqualTo(badStyle))
                        .or(jpegBox.styleProperty().isEqualTo(badStyle))
                        .or(thresholdInput.styleProperty().isEqualTo(badStyle))
        );

    }

    private void initOptionsSection() {

        if (AppVaribles.showComments) {
            Tooltip tips = new Tooltip(getMessage("PdfPageSizeComments"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(sizeBox, tips);
        }

        sizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkPageSize();
            }
        });
        checkPageSize();

        standardSizeBox.getItems().addAll(Arrays.asList(
                "A4 (16k)  21.0cm x 29.7cm",
                "A5 (32k)  14.8cm x 21.0cm",
                "A6 (64k)  10.5cm x 14.8cm",
                "A3 (8k)   29.7cm x 42.0cm",
                "A2 (4k)   42.0cm x 59.4cm",
                "A1 (2k)   59.4cm x 84.1cm",
                "A0 (1k)   84.1cm x 118.9cm",
                "B5        17.6cm x 25.0cm",
                "B4	    25.0cm x 35.3cm",
                "B2	    35.3cm x 50.0cm",
                "C4	    22.9cm x 32.4cm",
                "C5	    16.2cm x 22.9cm",
                "C6	    11.4cm x 16.2cm"
        ));
        standardSizeBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                checkStandardValues();
            }
        });
        standardSizeBox.getSelectionModel().select(0);
        isImageSize = true;

        standardDpiBox.getItems().addAll(Arrays.asList(
                "72 dpi",
                "96 dpi",
                "150 dpi",
                "300 dpi",
                "450 dpi",
                "720 dpi",
                "120 dpi",
                "160 dpi",
                "240 dpi",
                "320 dpi"
        ));
        standardDpiBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                checkStandardValues();
            }
        });
        standardDpiBox.getSelectionModel().select(0);

        customWidthInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkCustomValues();
            }
        });
        customHeightInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkCustomValues();
            }
        });

        formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkFormat();
            }
        });
        checkFormat();

        jpegBox.getItems().addAll(Arrays.asList(
                "100",
                "75",
                "90",
                "50",
                "60",
                "80",
                "30",
                "10"
        ));
        jpegBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                checkJpegQuality();
            }
        });
        jpegBox.getSelectionModel().select(0);
        checkJpegQuality();

        thresholdInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                checkThreshold();
            }
        });
        checkThreshold();

        MarginsBox.getItems().addAll(Arrays.asList("20", "10", "15", "5", "25", "30"));
        MarginsBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
                try {
                    marginSize = Integer.valueOf(newValue);
                    if (marginSize >= 0) {
                        MarginsBox.getEditor().setStyle(null);
                        AppVaribles.setConfigValue(ImageCombineMarginsKey, newValue);
                    } else {
                        marginSize = 0;
                        MarginsBox.getEditor().setStyle(badStyle);
                    }

                } catch (Exception e) {
                    marginSize = 0;
                    MarginsBox.getEditor().setStyle(badStyle);
                }
            }
        });
        MarginsBox.getSelectionModel().select(AppVaribles.getConfigValue(ImageCombineMarginsKey, "20"));

        fontBox.getItems().addAll(Arrays.asList(
                "幼圆",
                "仿宋",
                "隶书",
                "Helvetica",
                "Courier",
                "Times New Roman"
        ));
        fontBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov,
                    String oldValue, String newValue) {
            }
        });
        fontBox.getSelectionModel().select(0);
        if (AppVaribles.showComments) {
            Tooltip tips = new Tooltip(getMessage("FontFileComments"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(fontBox, tips);
        }

    }

    private void checkPageSize() {
        standardSizeBox.setDisable(true);
        standardDpiBox.setDisable(true);
        customWidthInput.setDisable(true);
        customHeightInput.setDisable(true);
        customWidthInput.setStyle(null);
        customHeightInput.setStyle(null);
        isImageSize = false;

        RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
        if (AppVaribles.getMessage("ImagesSize").equals(selected.getText())) {
            isImageSize = true;
        } else if (AppVaribles.getMessage("StandardSize").equals(selected.getText())) {
            standardSizeBox.setDisable(false);
            standardDpiBox.setDisable(false);
            checkStandardValues();

        } else if (AppVaribles.getMessage("Custom").equals(selected.getText())) {
            customWidthInput.setDisable(false);
            customHeightInput.setDisable(false);
            checkCustomValues();
        }

//        AppVaribles.setConfigValue(ImageCombineSizeKey, selected.getText());
    }

    private int calculateCmPixels(float cm, int dpi) {
        return (int) Math.round(cm * dpi / 2.54);
    }

    private void checkStandardValues() {
        String d = standardDpiBox.getSelectionModel().getSelectedItem();
        int dpi = 72;
        try {
            dpi = Integer.valueOf(d.substring(0, d.length() - 4));
        } catch (Exception e) {
        }
        String s = standardSizeBox.getSelectionModel().getSelectedItem();
        switch (s.substring(0, 2)) {
            case "A4":
                pageWidth = calculateCmPixels(21.0f, dpi);
                pageHeight = calculateCmPixels(29.7f, dpi);
                break;
            case "A5":
                pageWidth = calculateCmPixels(14.8f, dpi);
                pageHeight = calculateCmPixels(21.0f, dpi);
                break;
            case "A6":
                pageWidth = calculateCmPixels(10.5f, dpi);
                pageHeight = calculateCmPixels(14.8f, dpi);
                break;
            case "A3":
                pageWidth = calculateCmPixels(29.7f, dpi);
                pageHeight = calculateCmPixels(42.0f, dpi);
                break;
            case "A2":
                pageWidth = calculateCmPixels(42.0f, dpi);
                pageHeight = calculateCmPixels(59.4f, dpi);
                break;
            case "A1":
                pageWidth = calculateCmPixels(59.4f, dpi);
                pageHeight = calculateCmPixels(84.1f, dpi);
                break;

            case "A0":
                pageWidth = calculateCmPixels(84.1f, dpi);
                pageHeight = calculateCmPixels(118.9f, dpi);
                break;
            case "B5":
                pageWidth = calculateCmPixels(17.6f, dpi);
                pageHeight = calculateCmPixels(25.0f, dpi);
                break;
            case "B4":
                pageWidth = calculateCmPixels(25.0f, dpi);
                pageHeight = calculateCmPixels(35.3f, dpi);
                break;
            case "B2":
                pageWidth = calculateCmPixels(35.3f, dpi);
                pageHeight = calculateCmPixels(50.0f, dpi);
                break;
            case "C4":
                pageWidth = calculateCmPixels(22.9f, dpi);
                pageHeight = calculateCmPixels(32.4f, dpi);
                break;
            case "C5":
                pageWidth = calculateCmPixels(16.2f, dpi);
                pageHeight = calculateCmPixels(22.9f, dpi);
                break;
            case "C6":
                pageWidth = calculateCmPixels(11.4f, dpi);
                pageHeight = calculateCmPixels(16.2f, dpi);
                break;
        }
        customWidthInput.setText(pageWidth + "");
        customHeightInput.setText(pageHeight + "");
    }

    private void checkCustomValues() {

        RadioButton selected = (RadioButton) sizeGroup.getSelectedToggle();
        if (!AppVaribles.getMessage("Custom").equals(selected.getText())) {
            return;
        }
        try {
            pageWidth = Integer.valueOf(customWidthInput.getText());
            if (pageWidth > 0) {
                customWidthInput.setStyle(null);
            } else {
                pageWidth = 0;
                customWidthInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            pageWidth = 0;
            customWidthInput.setStyle(badStyle);
        }

        try {
            pageHeight = Integer.valueOf(customHeightInput.getText());
            if (pageHeight > 0) {
                customHeightInput.setStyle(null);
            } else {
                pageHeight = 0;
                customHeightInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            pageHeight = 0;
            customHeightInput.setStyle(badStyle);
        }

    }

    private void checkFormat() {
        jpegBox.setDisable(true);
        jpegBox.setStyle(null);
        thresholdInput.setDisable(true);

        RadioButton selected = (RadioButton) formatGroup.getSelectedToggle();
        if (AppVaribles.getMessage("PNG").equals(selected.getText())) {
            format = PdfImageFormat.Original;
        } else if (AppVaribles.getMessage("CCITT4").equals(selected.getText())) {
            format = PdfImageFormat.Tiff;
            thresholdInput.setDisable(false);
        } else if (AppVaribles.getMessage("JpegQuailty").equals(selected.getText())) {
            format = PdfImageFormat.Jpeg;
            jpegBox.setDisable(false);
            checkJpegQuality();
        }
    }

    private void checkJpegQuality() {
        jpegQuality = 100;
        try {
            jpegQuality = Integer.valueOf(jpegBox.getSelectionModel().getSelectedItem());
            if (jpegQuality >= 0 && jpegQuality <= 100) {
                jpegBox.setStyle(null);
            } else {
                jpegBox.setStyle(badStyle);
            }
        } catch (Exception e) {
            jpegBox.setStyle(badStyle);
        }
    }

    private void checkThreshold() {
        try {
            if (thresholdInput.getText().isEmpty()) {
                threshold = -1;
                thresholdInput.setStyle(null);
                return;
            }
            threshold = Integer.valueOf(thresholdInput.getText());
            if (threshold >= 0 && threshold <= 100) {
                thresholdInput.setStyle(null);
            } else {
                threshold = -1;
                thresholdInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            threshold = -1;
            thresholdInput.setStyle(badStyle);
        }
    }

    @FXML
    private void addAction(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File defaultPath = new File(AppVaribles.getConfigValue(ImagesCombinePdfPathKey, System.getProperty("user.home")));
            if (!defaultPath.isDirectory()) {
                defaultPath = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(defaultPath);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);

            List<File> files = fileChooser.showOpenMultipleDialog(getMyStage());
            if (files == null || files.isEmpty()) {
                return;
            }
            String path = files.get(0).getParent();
            AppVaribles.setConfigValue(LastPathKey, path);
            AppVaribles.setConfigValue(ImagesCombinePdfPathKey, path);
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
                    ImageFileInformation info;
                    if (loadCheck.isSelected()) {
                        info = ImageFileReaders.readImageMetaData(fileName);
                        BufferedImage bufferImage;
                        String format = FileTools.getFileSuffix(fileName).toLowerCase();
                        if ("raw".equals(format)) {
                            continue;
                        }
                        bufferImage = ImageIO.read(file);
                        Image image = SwingFXUtils.toFXImage(bufferImage, null);
                        info.setImage(image);
                    } else {
                        info = new ImageFileInformation(file);
                    }
                    infos.add(info);
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (infos == null || infos.isEmpty()) {
                            return;
                        }
                        sourceImages.addAll(infos);
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
    public void loadSelected() {
        Task loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Integer> selected = new ArrayList<>();
                selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
                for (Integer index : selected) {
                    if (index < 0 || index > sourceImages.size() - 1) {
                        continue;
                    }
                    ImageFileInformation info = sourceImages.get(index);
                    if (info.getImage() == null) {
                        final String fileName = info.getFilename();
                        info = ImageFileReaders.readImageMetaData(fileName);
                        String format = FileTools.getFileSuffix(fileName).toLowerCase();
                        if ("raw".equals(format)) {
                            continue;
                        }
                        BufferedImage bufferImage = ImageIO.read(info.getFile());
                        Image image = SwingFXUtils.toFXImage(bufferImage, null);
                        info.setImage(image);
                        sourceImages.set(index, info);
                    }
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
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
    private void openAction() {
        List<Integer> selected = new ArrayList<>();
        selected.addAll(sourceTable.getSelectionModel().getSelectedIndices());
        if (selected.isEmpty()) {
            return;
        }
        for (Integer index : selected) {
            if (index < 0 || index > sourceImages.size() - 1) {
                continue;
            }
            ImageFileInformation info = sourceImages.get(index);
            if (info.getImage() != null) {
                showImageView(info.getImage());
            } else {
                showImageView(info.getFilename());
            }
        }
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
    protected void selectTargetFile(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(ImageCombineTargetPathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(CommonValues.PdfExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            targetFile = file;
            AppVaribles.setConfigValue(LastPathKey, targetFile.getParent());
            AppVaribles.setConfigValue(ImageCombineTargetPathKey, targetFile.getParent());

            if (targetFileInput != null) {
                targetFileInput.setText(targetFile.getAbsolutePath());
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    protected void openTargetAction(ActionEvent event) {
        if (!targetFile.exists()) {
            openTargetButton.setDisable(true);
            return;
        }
        openTargetButton.setDisable(false);
        try {
            Desktop.getDesktop().browse(targetFile.toURI());
        } catch (Exception e) {

        }
    }

    @FXML
    protected void saveAction(ActionEvent event) {
        if (sourceImages == null || sourceImages.isEmpty() || targetFile == null) {
            return;
        }
        Task saveTask = new Task<Void>() {
            private boolean fail;

            @Override
            protected Void call() throws Exception {
                try {
                    int count = 0;
                    try (PDDocument document = new PDDocument()) {
                        PDPageContentStream content;
                        PDFont font = PdfTools.getFont(document, fontBox.getSelectionModel().getSelectedItem());
                        PDDocumentInformation info = new PDDocumentInformation();
                        info.setCreationDate(Calendar.getInstance());
                        info.setModificationDate(Calendar.getInstance());
                        info.setProducer("MyBox v" + CommonValues.AppVersion);
                        info.setAuthor(authorInput.getText());
                        document.setDocumentInformation(info);
                        BufferedImage bufferedImage;
                        for (ImageFileInformation source : sourceImages) {
                            PDImageXObject imageObject;
                            if (source.getImage() == null) {
                                bufferedImage = ImageIO.read(source.getFile());
                                if (format == PdfImageFormat.Jpeg) {
                                    bufferedImage = ImageConvertionTools.clearAlpha(bufferedImage);
                                    imageObject = JPEGFactory.createFromImage(document, bufferedImage, jpegQuality / 100f);
                                } else if (format == PdfImageFormat.Tiff) {
                                    if (threshold < 0) {
                                        bufferedImage = ImageGrayTools.color2Binary(bufferedImage);
                                    } else {
                                        bufferedImage = ImageGrayTools.color2BinaryWithPercentage(bufferedImage, threshold);
                                    }
                                    imageObject = CCITTFactory.createFromImage(document, bufferedImage);
                                } else {
                                    imageObject = LosslessFactory.createFromImage(document, bufferedImage);
                                }
                            } else {
//                                logger.debug(source.getFile().getAbsolutePath() + "  " + source.getImageFormat());
                                if (format == PdfImageFormat.Tiff) {
                                    bufferedImage = SwingFXUtils.fromFXImage(source.getImage(), null);
                                    if (threshold < 0) {
                                        bufferedImage = ImageGrayTools.color2Binary(bufferedImage);
                                    } else {
                                        bufferedImage = ImageGrayTools.color2BinaryWithPercentage(bufferedImage, threshold);
                                    }
                                    imageObject = CCITTFactory.createFromImage(document, bufferedImage);
                                } else if (format == PdfImageFormat.Jpeg) {
                                    bufferedImage = FxmlImageTools.getWritableData(source.getImage(), "jpg");
                                    imageObject = JPEGFactory.createFromImage(document, bufferedImage, jpegQuality / 100f);
                                } else {
                                    bufferedImage = FxmlImageTools.getWritableData(source.getImage(), source.getImageFormat());
                                    imageObject = LosslessFactory.createFromImage(document, bufferedImage);
                                }
                            }

                            if (isImageSize) {
                                pageSize = new PDRectangle(imageObject.getWidth() + marginSize * 2, imageObject.getHeight() + marginSize * 2);
                            } else {
                                pageSize = new PDRectangle(pageWidth, pageHeight);
                            }
                            PDPage page = new PDPage(pageSize);
                            document.addPage(page);
                            content = new PDPageContentStream(document, page);

                            float w, h;
                            if (isImageSize) {
                                w = imageObject.getWidth();
                                h = imageObject.getHeight();
                            } else {
                                if (imageObject.getWidth() > imageObject.getHeight()) {
                                    w = page.getTrimBox().getWidth() - marginSize * 2;
                                    h = imageObject.getHeight() * w / imageObject.getWidth();
                                } else {
                                    h = page.getTrimBox().getHeight() - marginSize * 2;
                                    w = imageObject.getWidth() * h / imageObject.getHeight();
                                }
                            }
                            content.drawImage(imageObject, marginSize, page.getTrimBox().getHeight() - marginSize - h, w, h);

                            if (pageNumberCheck.isSelected()) {
                                content.beginText();
                                content.setFont(font, 12);
                                content.newLineAtOffset(w + marginSize - 80, 5);
                                content.showText((++count) + " / " + sourceImages.size());
//                            content.showText(MessageFormat.format(AppVaribles.getMessage("PageNumber"), ++count, sourceImages.size()));
                                content.endText();
                            }

                            if (!headerInput.getText().isEmpty()) {
                                try {
                                    content.beginText();
                                    content.setFont(font, 16);
                                    content.newLineAtOffset(marginSize, page.getTrimBox().getHeight() - marginSize + 2);
                                    content.showText(headerInput.getText());
//                            content.showText(MessageFormat.format(AppVaribles.getMessage("PageNumber"), ++count, sourceImages.size()));
                                    content.endText();
                                } catch (Exception e) {
                                    logger.error(e.toString());
                                }
                            }

                            content.close();
                        }

                        document.save(targetFile);
                        fail = false;
                    }

                } catch (Exception e) {
                    fail = true;
                    logger.error(e.toString());
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!fail && targetFile.exists()) {
                                Desktop.getDesktop().browse(targetFile.toURI());
                                openTargetButton.setDisable(false);
                            } else {
                                FxmlTools.popError(loadCheck, AppVaribles.getMessage("ImageCombinePdfFail"));
                            }
                        } catch (Exception e) {
                            logger.error(e.toString());
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(saveTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(saveTask);
        thread.setDaemon(true);
        thread.start();
    }

}
