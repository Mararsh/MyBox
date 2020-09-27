package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageCombine;
import mara.mybox.image.ImageCombine.ArrayType;
import mara.mybox.image.ImageCombine.CombineSizeType;
import mara.mybox.image.ImageInformation;
import mara.mybox.image.ImageManufacture;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2018-8-11
 * @Description
 * @License Apache License Version 2.0
 */
public class ImagesCombineController extends ImagesListController {

    protected String ImageCombineArrayTypeKey, ImageCombineCombineSizeTypeKey, ImageCombineColumnsKey,
            ImageCombineIntervalKey, ImageCombineMarginsKey, ImageCombineEachWidthKey, ImageCombineEachHeightKey,
            ImageCombineTotalWidthKey, ImageCombineTotalHeightKey;
    private ImageCombine imageCombine;

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
    protected ColorSetController colorSetController;
    @FXML
    protected Button newWindowButton;
    @FXML
    protected ToolBar imageBar;
    @FXML
    protected CheckBox openCheck;
    @FXML
    protected Label imageLabel;

    public ImagesCombineController() {
        baseTitle = AppVariables.message("ImagesCombine");

        ImageCombineArrayTypeKey = "ImageCombineArrayTypeKey";
        ImageCombineCombineSizeTypeKey = "ImageCombineCombineSizeTypeKey";
        ImageCombineEachWidthKey = "ImageCombineEachWidthKey";
        ImageCombineEachHeightKey = "ImageCombineEachHeightKey";
        ImageCombineTotalWidthKey = "ImageCombineTotalWidthKey";
        ImageCombineTotalHeightKey = "ImageCombineTotalHeightKey";
        ImageCombineColumnsKey = "ImageCombineColumnsKey";
        ImageCombineIntervalKey = "ImageCombineIntervalKey";
        ImageCombineMarginsKey = "ImageCombineMarginsKey";
        sourceExtensionFilter = CommonFxValues.ImageExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            imageCombine = new ImageCombine();

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
                            AppVariables.setUserConfigValue(ImageCombineColumnsKey, columnsValue + "");
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
            columnsBox.getSelectionModel().select(AppVariables.getUserConfigValue(ImageCombineColumnsKey, "2"));

            intervalBox.getItems().addAll(Arrays.asList("5", "10", "15", "20", "1", "3", "30", "0"));
            intervalBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int intervalValue = Integer.valueOf(newValue);
                        if (intervalValue >= 0) {
                            imageCombine.setIntervalValue(intervalValue);
                            AppVariables.setUserConfigValue(ImageCombineIntervalKey, intervalValue + "");
                            FxmlControl.setEditorNormal(intervalBox);
                            combineImages();
                        } else {
                            FxmlControl.setEditorBadStyle(intervalBox);
                        }

                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(intervalBox);
                    }
                }
            });
            intervalBox.getSelectionModel().select(AppVariables.getUserConfigValue(ImageCombineIntervalKey, "5"));

            MarginsBox.getItems().addAll(Arrays.asList("5", "10", "15", "20", "1", "3", "30", "0"));
            MarginsBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> ov,
                        String oldValue, String newValue) {
                    try {
                        int MarginsValue = Integer.valueOf(newValue);
                        if (MarginsValue >= 0) {
                            imageCombine.setMarginsValue(MarginsValue);
                            AppVariables.setUserConfigValue(ImageCombineMarginsKey, MarginsValue + "");
                            FxmlControl.setEditorNormal(MarginsBox);
                            combineImages();
                        } else {
                            FxmlControl.setEditorBadStyle(MarginsBox);
                        }

                    } catch (Exception e) {
                        FxmlControl.setEditorBadStyle(MarginsBox);
                    }
                }
            });
            MarginsBox.getSelectionModel().select(AppVariables.getUserConfigValue(ImageCombineMarginsKey, "5"));

            colorSetController.init(this, baseName + "Color");
            colorSetController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                @Override
                public void changed(ObservableValue<? extends Paint> observable,
                        Paint oldValue, Paint newValue) {
                    imageCombine.setBgColor((Color) newValue);
                    combineImages();
                }
            });

            arrayGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    RadioButton selected = (RadioButton) arrayGroup.getSelectedToggle();
                    if (AppVariables.message("SingleColumn").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.SingleColumn);
                        columnsBox.setDisable(true);
                        AppVariables.setUserConfigValue(ImageCombineArrayTypeKey, "SingleColumn");
                    } else if (AppVariables.message("SingleRow").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.SingleRow);
                        columnsBox.setDisable(true);
                        AppVariables.setUserConfigValue(ImageCombineArrayTypeKey, "SingleRow");
                    } else if (AppVariables.message("ColumnsNumber").equals(selected.getText())) {
                        imageCombine.setArrayType(ArrayType.ColumnsNumber);
                        columnsBox.setDisable(false);
                        AppVariables.setUserConfigValue(ImageCombineArrayTypeKey, "ColumnsNumber");
                    }
                    combineImages();
                }
            });
            String arraySelect = AppVariables.getUserConfigValue(ImageCombineArrayTypeKey, "SingleColumn");
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
            eachWidthInput.setText(AppVariables.getUserConfigValue(ImageCombineEachWidthKey, ""));

            eachHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkEachHeightValue();
                }
            });
            eachHeightInput.setText(AppVariables.getUserConfigValue(ImageCombineEachHeightKey, ""));

            totalWidthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkTotalWidthValue();
                }
            });
            totalWidthInput.setText(AppVariables.getUserConfigValue(ImageCombineTotalWidthKey, ""));

            totalHeightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkTotalHeightValue();
                }
            });
            totalHeightInput.setText(AppVariables.getUserConfigValue(ImageCombineTotalHeightKey, ""));

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
                    if (AppVariables.message("KeepSize").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.KeepSize);
                        AppVariables.setUserConfigValue(ImageCombineCombineSizeTypeKey, "KeepSize");
                        combineImages();
                    } else if (AppVariables.message("AlignAsBigger").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.AlignAsBigger);
                        AppVariables.setUserConfigValue(ImageCombineCombineSizeTypeKey, "AlignAsBigger");
                        combineImages();
                    } else if (AppVariables.message("AlignAsSmaller").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.AlignAsSmaller);
                        AppVariables.setUserConfigValue(ImageCombineCombineSizeTypeKey, "AlignAsSmaller");
                        combineImages();
                    } else if (AppVariables.message("EachWidth").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.EachWidth);
                        eachWidthInput.setDisable(false);
                        checkEachWidthValue();
                        AppVariables.setUserConfigValue(ImageCombineCombineSizeTypeKey, "EachWidth");
                    } else if (AppVariables.message("EachHeight").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.EachHeight);
                        eachHeightInput.setDisable(false);
                        checkEachHeightValue();
                        AppVariables.setUserConfigValue(ImageCombineCombineSizeTypeKey, "EachHeight");
                    } else if (AppVariables.message("TotalWidth").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.TotalWidth);
                        totalWidthInput.setDisable(false);
                        checkTotalWidthValue();
                        AppVariables.setUserConfigValue(ImageCombineCombineSizeTypeKey, "TotalWidth");
                    } else if (AppVariables.message("TotalHeight").equals(selected.getText())) {
                        imageCombine.setSizeType(CombineSizeType.TotalHeight);
                        totalHeightInput.setDisable(false);
                        checkTotalHeightValue();
                        AppVariables.setUserConfigValue(ImageCombineCombineSizeTypeKey, "TotalHeight");
                    }
                }
            });
            String arraySelect = AppVariables.getUserConfigValue(ImageCombineCombineSizeTypeKey, "KeepSize");
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
                AppVariables.setUserConfigValue(ImageCombineEachWidthKey, eachWidthValue + "");
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
                AppVariables.setUserConfigValue(ImageCombineEachHeightKey, eachHeightValue + "");
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
                AppVariables.setUserConfigValue(ImageCombineTotalWidthKey, totalWidthValue + "");
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
                AppVariables.setUserConfigValue(ImageCombineTotalHeightKey, totalHeightValue + "");
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

    public void initTargetSection() {
        try {

            imageBar.disableProperty().bind(
                    Bindings.isEmpty(tableData)
                            .or(tableController.hasSampled)
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void dataChanged() {
        super.dataChanged();
        if (!tableController.hasSampled()) {
            combineImages();
        }
    }

    @FXML
    private void newWindow(ActionEvent event) {
        FxmlStage.openImageViewer(image);
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
            image = ImageManufacture.combineSingleColumn(imageCombine, tableData, false, true);
        } else if (imageCombine.getArrayType() == ArrayType.SingleRow) {
            image = ImageManufacture.combineSingleRow(imageCombine, tableData, false, true);
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
        imageLabel.setText(AppVariables.message("CombinedSize") + ": "
                + (int) image.getWidth() + "x" + (int) image.getHeight());
    }

    private Image combineImagesColumns(List<ImageInformation> images) {
        if (images == null || images.isEmpty() || imageCombine.getColumnsValue() <= 0) {
            return null;
        }
        try {
            List<ImageInformation> rowImages = new ArrayList<>();
            List<ImageInformation> rows = new ArrayList<>();
            for (ImageInformation imageInfo : images) {
                rowImages.add(imageInfo);
                if (rowImages.size() == imageCombine.getColumnsValue()) {
                    Image rowImage = ImageManufacture.combineSingleRow(imageCombine, rowImages, true, false);
                    rows.add(new ImageInformation(rowImage));
                    rowImages = new ArrayList<>();
                }
            }
            if (!rowImages.isEmpty()) {
                Image rowImage = ImageManufacture.combineSingleRow(imageCombine, rowImages, true, false);
                rows.add(new ImageInformation(rowImage));
            }
            Image newImage = ImageManufacture.combineSingleColumn(imageCombine, rows, true, true);
            return newImage;
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (image == null) {
            return;
        }
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                null, targetExtensionFilter, true);
        if (file == null) {
            return;
        }
        recordFileWritten(file);
        targetFile = file;

        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                private String filename;

                @Override
                protected boolean handle() {
                    filename = targetFile.getAbsolutePath();
                    String format = FileTools.getFileSuffix(filename);
                    final BufferedImage bufferedImage = FxmlImageManufacture.getBufferedImage(image);
                    return ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                }

                @Override
                protected void whenSucceeded() {
                    popSuccessful();
                    FxmlStage.openImageViewer(targetFile);
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
    public void saveAction() {
        saveAsAction();
    }

}
