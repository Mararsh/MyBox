/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageGrayTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import mara.mybox.tools.FxmlTools.ImageManufactureType;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureController extends ImageViewerController {

    protected int saturateStep = 5, brightnessStep = 5, hueStep = 5, percent = 50;
    protected int saturateOffset = 0, brightnessOffset = 0, hueOffset = 0;
    protected File nextFile, lastFile;
    protected String ImageSortTypeKey = "ImageSortType", ImageOpenAfterSaveAsKey = "ImageOpenAfterSaveAs";

    @FXML
    protected ToolBar fileBar, navBar;
    @FXML
    protected Tab fileTab, zoomTab, hueTab, saturateTab, brightnessTab, filtersTab;
    @FXML
    protected Slider zoomSlider, rotateSlider, hueSlider, saturateSlider, brightnessSlider, binarySlider;
    @FXML
    protected Label zoomValue, rotateValue, hueValue, saturateValue, brightnessValue, binaryValue;
    @FXML
    protected ToggleGroup sortGroup;
    @FXML
    protected Button nextButton, lastButton;
    @FXML
    protected CheckBox openCheck;

    @Override
    protected void initializeNext2() {
        try {
            fileBar.setDisable(true);
            navBar.setDisable(true);
            zoomTab.setDisable(true);
            hueTab.setDisable(true);
            saturateTab.setDisable(true);
            brightnessTab.setDisable(true);
            filtersTab.setDisable(true);

            zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    zoomStep = newValue.intValue();
                    zoomValue.setText(zoomStep + "%");
                }
            });

            rotateSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    rotateValue.setText(newValue.intValue() + "");
                    rotateAngle = newValue.intValue();
                }
            });

            saturateSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    saturateStep = newValue.intValue();
                    saturateValue.setText(saturateStep + "%");
                }
            });

            hueSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    hueStep = newValue.intValue();
                    hueValue.setText(hueStep + "");
                }
            });

            brightnessSlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    brightnessStep = newValue.intValue();
                    brightnessValue.setText(brightnessStep + "%");
                }
            });

            binarySlider.valueProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    percent = newValue.intValue();
                    binaryValue.setText(percent + "%");
                }
            });

            sortGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkNevigator();
                    RadioButton selected = (RadioButton) sortGroup.getSelectedToggle();
                    AppVaribles.setConfigValue(ImageSortTypeKey, selected.getText());
                }
            });
            FxmlTools.setRadioSelected(sortGroup, AppVaribles.getConfigValue(ImageSortTypeKey, getMessage("FileName")));

            openCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setConfigValue(ImageOpenAfterSaveAsKey, openCheck.isSelected());
                }
            });
            openCheck.setSelected(AppVaribles.getConfigBoolean(ImageOpenAfterSaveAsKey));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterImageLoaded() {
        super.afterImageLoaded();
        if (image != null) {
            fileBar.setDisable(false);
            navBar.setDisable(false);
            zoomTab.setDisable(false);
            hueTab.setDisable(false);
            saturateTab.setDisable(false);
            brightnessTab.setDisable(false);
            filtersTab.setDisable(false);
            checkNevigator();
            straighten();
        }
    }

    @FXML
    public void recovery() {
        imageView.setImage(image);
    }

    @FXML
    public void increaseSaturate() {
        FxmlTools.changeSaturate(imageView, saturateStep / 100.0f);
        saturateOffset += saturateStep;
        if (saturateOffset > 100) {
            saturateOffset = 100;
        }
    }

    @FXML
    public void decreaseSaturate() {
        FxmlTools.changeSaturate(imageView, 0.0f - saturateStep / 100.0f);
        saturateOffset -= saturateStep;
    }

    @FXML
    public void increaseHue() {
        FxmlTools.changeHue(imageView, hueStep);
    }

    @FXML
    public void decreaseHue() {
        FxmlTools.changeHue(imageView, 0 - hueStep);
    }

    @FXML
    public void increaseBrightness() {
        FxmlTools.changeBrightness(imageView, brightnessStep / 100.0f);
    }

    @FXML
    public void decreaseBrightness() {
        FxmlTools.changeBrightness(imageView, 0.0f - brightnessStep / 100.0f);
    }

    @FXML
    public void setInvert() {
        FxmlTools.manufactureImage(imageView, ImageManufactureType.Invert);
    }

    @FXML
    public void setGray() {
        FxmlTools.manufactureImage(imageView, ImageManufactureType.Gray);
    }

    @FXML
    public void setBinary() {
        FxmlTools.makeBinary(imageView, percent);
    }

    @FXML
    public void save() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(AppVaribles.getMessage("AppTitle"));
//        alert.setHeaderText("Look, a Confirmation Dialog");
        alert.setContentText(AppVaribles.getMessage("SureOverrideFile"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        try {
            String format = FileTools.getFileSuffix(sourceFile.getName());
            BufferedImage changedImage = FxmlTools.readImage(imageView);
            ImageIO.write(changedImage, format, sourceFile);
            image = imageView.getImage();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void saveAs() {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue("imageTargetPath", System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            File file = fileChooser.showSaveDialog(getMyStage());
            AppVaribles.setConfigValue("imageTargetPath", file.getParent());
            String format = FileTools.getFileSuffix(file.getName());
            BufferedImage changedImage = FxmlTools.readImage(imageView);
            ImageFileWriters.writeImageFile(changedImage, format, file.getAbsolutePath());
            if (openCheck.isSelected()) {
                showImageManufacture(file.getAbsolutePath());
            }
//            ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), format, file);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    public void next() {
        if (nextFile != null) {
            loadImage(nextFile.getAbsoluteFile(), false);
        }
    }

    @FXML
    public void last() {
        if (lastFile != null) {
            loadImage(lastFile.getAbsoluteFile(), false);
        }
    }

    @FXML
    public void calculateThreshold() {
        int threshold = ImageGrayTools.calculateThreshold(sourceFile);
        percent = threshold * 100 / 256;
        binarySlider.setValue(percent);
    }

    private void checkNevigator() {
        if (sourceFile == null) {
            lastFile = null;
            lastButton.setDisable(true);
            nextFile = null;
            nextButton.setDisable(true);
            return;
        }
        File path = sourceFile.getParentFile();
        List<File> sortedFiles = new ArrayList<>();
        File[] files = path.listFiles();
        for (File file : files) {
            if (file.isFile() && FileTools.isSupportedImage(file)) {
                sortedFiles.add(file);
            }
        }
        RadioButton sort = (RadioButton) sortGroup.getSelectedToggle();
        if (getMessage("OriginalFileName").equals(sort.getText())) {
            FileTools.sortFiles(sortedFiles, FileTools.FileSortType.FileName);

        } else if (getMessage("CreateTime").equals(sort.getText())) {
            FileTools.sortFiles(sortedFiles, FileTools.FileSortType.CreateTime);

        } else if (getMessage("ModifyTime").equals(sort.getText())) {
            FileTools.sortFiles(sortedFiles, FileTools.FileSortType.ModifyTime);

        } else if (getMessage("Size").equals(sort.getText())) {
            FileTools.sortFiles(sortedFiles, FileTools.FileSortType.Size);
        }

        for (int i = 0; i < sortedFiles.size(); i++) {
            if (sortedFiles.get(i).getAbsoluteFile().equals(sourceFile.getAbsoluteFile())) {
                if (i < sortedFiles.size() - 1) {
                    nextFile = sortedFiles.get(i + 1);
                    nextButton.setDisable(false);
                } else {
                    nextFile = null;
                    nextButton.setDisable(true);
                }
                if (i > 0) {
                    lastFile = sortedFiles.get(i - 1);
                    lastButton.setDisable(false);
                } else {
                    lastFile = null;
                    lastButton.setDisable(true);
                }
                return;
            }
        }
        lastFile = null;
        lastButton.setDisable(true);
        nextFile = null;
        nextButton.setDisable(true);
    }

}
