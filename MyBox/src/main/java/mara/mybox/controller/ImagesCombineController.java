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
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.image.ImageConvert;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.ImageCombine;
import mara.mybox.data.ImageCombine.ArrayType;
import mara.mybox.data.ImageCombine.CombineSizeType;
import mara.mybox.tools.FileTools;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.ImageManufacture;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.data.ImageInformation;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.ValueTools;

/**
 * @Author Mara
 * @CreateDate 2018-8-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesCombineController extends ImageSourcesController {

    protected String ImageCombineArrayTypeKey, ImageCombineCombineSizeTypeKey, ImageCombineColumnsKey,
            ImageCombineIntervalKey, ImageCombineMarginsKey, ImageCombineEachWidthKey, ImageCombineEachHeightKey,
            ImageCombineTotalWidthKey, ImageCombineTotalHeightKey, ImageCombineBgColorKey;

    private ImageCombine imageCombine;

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab fileTab, sizeTab, arrayTab;
    @FXML
    private ToggleGroup sizeGroup, arrayGroup;
    @FXML
    private RadioButton arrayColumnRadio, arrayRowRadio, arrayColumnsRadio, keepSizeRadio, sizeBiggerRadio,
            sizeSmallerRadio, eachWidthRadio, eachHeightRadio, totalWidthRadio, totalHeightRadio;
    @FXML
    private TextField totalWidthInput, totalHeightInput, eachWidthInput, eachHeightInput;
    @FXML
    private ComboBox<String> columnsBox, intervalBox, MarginsBox;
    @FXML
    private ColorPicker bgPicker;
    @FXML
    private Label imageLabel;
    @FXML
    protected Button newWindowButton;
    @FXML
    protected ToolBar imageBar;
    @FXML
    protected CheckBox openCheck;

    public ImagesCombineController() {
        baseTitle = AppVaribles.getMessage("ImagesCombine");

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
        fileExtensionFilter = CommonValues.ImageExtensionFilter;
    }

    @Override
    public void initializeNext() {
        try {
            imageCombine = new ImageCombine();

            initTable();
            initArraySection();
            initSizeSection();
            initTargetSection();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initArraySection() {
        try {
            columnsBox.getItems().addAll(Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9", "10"));
            columnsBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int columnsValue = Integer.valueOf(newValue);
                        if (columnsValue > 0) {
                            imageCombine.setColumnsValue(columnsValue);
                            AppVaribles.setUserConfigValue(ImageCombineColumnsKey, columnsValue + "");
                            combineImages();
                            FxmlControl.setEditorNormal(columnsBox);
                        } else {
                            imageCombine.setColumnsValue(-1);
                            FxmlControl.setEditorBadStyle(columnsBox);
                        }

                    } catch (Exception e) {
                        imageCombine.setColumnsValue(-1);
                        FxmlControl.setEditorBadStyle(columnsBox);
                    }
                }
            });
            columnsBox.getSelectionModel().select(AppVaribles.getUserConfigValue(ImageCombineColumnsKey, "2"));

            intervalBox.getItems().addAll(Arrays.asList("5", "10", "15", "20", "1", "3", "30", "0"));
            intervalBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int intervalValue = Integer.valueOf(newValue);
                        if (intervalValue >= 0) {
                            imageCombine.setIntervalValue(intervalValue);
                            AppVaribles.setUserConfigValue(ImageCombineIntervalKey, intervalValue + "");
                            combineImages();
                            FxmlControl.setEditorNormal(intervalBox);
                        } else {
                            imageCombine.setIntervalValue(-1);
                            FxmlControl.setEditorBadStyle(intervalBox);
                        }

                    } catch (Exception e) {
                        imageCombine.setIntervalValue(-1);
                        FxmlControl.setEditorBadStyle(intervalBox);
                    }
                }
            });
            intervalBox.getSelectionModel().select(AppVaribles.getUserConfigValue(ImageCombineIntervalKey, "5"));

            MarginsBox.getItems().addAll(Arrays.asList("5", "10", "15", "20", "1", "3", "30", "0"));
            MarginsBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int MarginsValue = Integer.valueOf(newValue);
                        if (MarginsValue >= 0) {
                            imageCombine.setMarginsValue(MarginsValue);
                            AppVaribles.setUserConfigValue(ImageCombineMarginsKey, MarginsValue + "");
                            combineImages();
                            FxmlControl.setEditorNormal(MarginsBox);
                        } else {
                            imageCombine.setMarginsValue(-1);
                            FxmlControl.setEditorBadStyle(MarginsBox);
                        }

                    } catch (Exception e) {
                        imageCombine.setMarginsValue(-1);
                        FxmlControl.setEditorBadStyle(MarginsBox);
                    }
                }
            });
            MarginsBox.getSelectionModel().select(AppVaribles.getUserConfigValue(ImageCombineMarginsKey, "5"));

            bgPicker.valueProperty().addListener(new ChangeListener<Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> ov,
                        Color oldValue, Color newValue) {
                    imageCombine.setBgColor(newValue);
                    AppVaribles.setUserConfigValue(ImageCombineBgColorKey, newValue.toString());
                    combineImages();
                }
            });
            bgPicker.setValue(Color.web(AppVaribles.getUserConfigValue(ImageCombineBgColorKey, Color.WHITE.toString())));

            arrayGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    RadioButton selected = (RadioButton) arrayGroup.getSelectedToggle();
                    if (AppVaribles.getMessage("SingleColumn").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.SingleColumn);
                        columnsBox.setDisable(true);
                        AppVaribles.setUserConfigValue(ImageCombineArrayTypeKey, "SingleColumn");
                    } else if (AppVaribles.getMessage("SingleRow").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.SingleRow);
                        columnsBox.setDisable(true);
                        AppVaribles.setUserConfigValue(ImageCombineArrayTypeKey, "SingleRow");
                    } else if (AppVaribles.getMessage("ColumnsNumber").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.ColumnsNumber);
                        columnsBox.setDisable(false);
                        AppVaribles.setUserConfigValue(ImageCombineArrayTypeKey, "ColumnsNumber");
                    }
                    combineImages();
                }
            });
            String arraySelect = AppVaribles.getUserConfigValue(ImageCombineArrayTypeKey, "SingleColumn");
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
            eachWidthInput.setText(AppVaribles.getUserConfigValue(ImageCombineEachWidthKey, ""));

            eachHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkEachHeightValue();
                }
            });
            eachHeightInput.setText(AppVaribles.getUserConfigValue(ImageCombineEachHeightKey, ""));

            totalWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkTotalWidthValue();
                }
            });
            totalWidthInput.setText(AppVaribles.getUserConfigValue(ImageCombineTotalWidthKey, ""));

            totalHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkTotalHeightValue();
                }
            });
            totalHeightInput.setText(AppVaribles.getUserConfigValue(ImageCombineTotalHeightKey, ""));

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
                        AppVaribles.setUserConfigValue(ImageCombineCombineSizeTypeKey, "KeepSize");
                        combineImages();
                    } else if (AppVaribles.getMessage("AlignAsBigger").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.AlignAsBigger);
                        AppVaribles.setUserConfigValue(ImageCombineCombineSizeTypeKey, "AlignAsBigger");
                        combineImages();
                    } else if (AppVaribles.getMessage("AlignAsSmaller").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.AlignAsSmaller);
                        AppVaribles.setUserConfigValue(ImageCombineCombineSizeTypeKey, "AlignAsSmaller");
                        combineImages();
                    } else if (AppVaribles.getMessage("EachWidth").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.EachWidth);
                        eachWidthInput.setDisable(false);
                        checkEachWidthValue();
                        AppVaribles.setUserConfigValue(ImageCombineCombineSizeTypeKey, "EachWidth");
                    } else if (AppVaribles.getMessage("EachHeight").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.EachHeight);
                        eachHeightInput.setDisable(false);
                        checkEachHeightValue();
                        AppVaribles.setUserConfigValue(ImageCombineCombineSizeTypeKey, "EachHeight");
                    } else if (AppVaribles.getMessage("TotalWidth").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.TotalWidth);
                        totalWidthInput.setDisable(false);
                        checkTotalWidthValue();
                        AppVaribles.setUserConfigValue(ImageCombineCombineSizeTypeKey, "TotalWidth");
                    } else if (AppVaribles.getMessage("TotalHeight").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.TotalHeight);
                        totalHeightInput.setDisable(false);
                        checkTotalHeightValue();
                        AppVaribles.setUserConfigValue(ImageCombineCombineSizeTypeKey, "TotalHeight");
                    }
                }
            });
            String arraySelect = AppVaribles.getUserConfigValue(ImageCombineCombineSizeTypeKey, "KeepSize");
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
                AppVaribles.setUserConfigValue(ImageCombineEachWidthKey, eachWidthValue + "");
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
                AppVaribles.setUserConfigValue(ImageCombineEachHeightKey, eachHeightValue + "");
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
                AppVaribles.setUserConfigValue(ImageCombineTotalWidthKey, totalWidthValue + "");
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
                AppVaribles.setUserConfigValue(ImageCombineTotalHeightKey, totalHeightValue + "");
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

            imageBar.disableProperty().bind(
                    Bindings.isEmpty(tableData)
                            .or(hasSampled)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void setImageChanged(boolean c) {
        changed.setValue(c);
        long pixels = 0;
        for (ImageInformation m : tableData) {
            pixels += m.getWidth() * m.getHeight();
        }
        sourcesLabel.setText(getMessage("TotalImages") + ":" + tableData.size() + "  "
                + getMessage("TotalPixels") + ":" + ValueTools.formatData(pixels));
        if (hasSampled()) {
            hasSampled.set(true);
        } else {
            hasSampled.set(false);
            combineImages();
        }
    }

    @FXML
    private void newWindow(ActionEvent event) {
        openImageViewer(image);
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

    private void combineImages() {
        if (tableData == null || tableData.isEmpty()
                || totalWidthInput.getStyle().equals(badStyle)
                || totalHeightInput.getStyle().equals(badStyle)
                || eachWidthInput.getStyle().equals(badStyle)
                || eachHeightInput.getStyle().equals(badStyle)) {
            image = null;
            imageView.setImage(null);
            imageLabel.setText("");
            return;
        }

        if (imageCombine.getArrayType() == ArrayType.SingleColumn) {
            image = ImageConvert.combineSingleColumn(imageCombine, tableData, false, true);
        } else if (imageCombine.getArrayType() == ArrayType.SingleRow) {
            image = ImageConvert.combineSingleRow(imageCombine, tableData, false, true);
        } else if (imageCombine.getArrayType() == ArrayType.ColumnsNumber) {
            image = combineImagesColumns(tableData);
        } else {
            image = null;
        }
        if (image == null) {
            return;
        }
        xZoomStep = (int) image.getWidth() / 10;
        yZoomStep = (int) image.getHeight() / 10;
        imageView.setImage(image);
        fitSize();
        imageLabel.setText(AppVaribles.getMessage("CombinedSize") + ": "
                + (int) image.getWidth() + "x" + (int) image.getHeight());
    }

    private Image combineImagesColumns(List<ImageInformation> images) {
        if (images == null || images.isEmpty() || imageCombine.getColumnsValue() <= 0) {
            return null;
        }
        try {
            List<ImageInformation> rowImages = new ArrayList();
            List<ImageInformation> rows = new ArrayList();
            for (ImageInformation imageInfo : images) {
                rowImages.add(imageInfo);
                if (rowImages.size() == imageCombine.getColumnsValue()) {
                    Image rowImage = ImageConvert.combineSingleRow(imageCombine, rowImages, true, false);
                    rows.add(new ImageInformation(rowImage));
                    rowImages = new ArrayList();
                }
            }
            if (!rowImages.isEmpty()) {
                Image rowImage = ImageConvert.combineSingleRow(imageCombine, rowImages, true, false);
                rows.add(new ImageInformation(rowImage));
            }
            Image newImage = ImageConvert.combineSingleColumn(imageCombine, rows, true, true);
            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    @FXML
    @Override
    public void saveAction() {
        if (image == null) {
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
        targetFile = file;

        task = new Task<Void>() {
            private boolean ok;
            private String filename;

            @Override
            protected Void call() throws Exception {
                filename = targetFile.getAbsolutePath();
                String format = FileTools.getFileSuffix(filename);
                final BufferedImage bufferedImage = ImageManufacture.getBufferedImage(image);
                ok = ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (ok) {
                            popInformation(AppVaribles.getMessage("Successful"));
                            if (openCheck.isSelected()) {
                                openImageViewer(filename);
                            }
                        } else {
                            popError(AppVaribles.getMessage("Failed"));
                        }
                    }
                });
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

    }

}
