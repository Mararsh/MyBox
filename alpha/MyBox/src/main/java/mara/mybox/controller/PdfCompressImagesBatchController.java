package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.image.data.ImageBinary;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.PdfTools.PdfImageFormat;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.CCITTFactory;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * @Author Mara
 * @CreateDate 2018-9-10
 * @License Apache License Version 2.0
 */
public class PdfCompressImagesBatchController extends PdfImagesConvertBatchController {

    protected String AuthorKey;
    protected int jpegQuality, threshold;
    protected PdfImageFormat pdfFormat;
    protected PDDocument targetDoc;
    protected File tmpFile;

    @FXML
    protected ToggleGroup formatGroup;
    @FXML
    protected ComboBox<String> jpegBox;
    @FXML
    protected TextField thresholdInput, authorInput;
    @FXML
    protected CheckBox ditherCheck;

    public PdfCompressImagesBatchController() {
        baseTitle = Languages.message("PdfCompressImagesBatch");
        AuthorKey = "AuthorKey";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(jpegBox.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(thresholdInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(Bindings.isEmpty(tableView.getItems()))
            );
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public void initOptionsSection() {

        formatGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                checkFormat();
            }
        });
        checkFormat();

        jpegBox.getItems().addAll(Arrays.asList(
                "100", "75", "90", "50", "60", "80", "30", "10"
        ));
        jpegBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
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

        authorInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                UserConfig.setString(AuthorKey, newValue);
            }
        });
        authorInput.setText(UserConfig.getString(AuthorKey, System.getProperty("user.name")));

    }

    protected void checkFormat() {
        jpegBox.setDisable(true);
        jpegBox.setStyle(null);
        thresholdInput.setDisable(true);

        RadioButton selected = (RadioButton) formatGroup.getSelectedToggle();
        if (Languages.message("CCITT4").equals(selected.getText())) {
            pdfFormat = PdfImageFormat.Tiff;
            thresholdInput.setDisable(false);
        } else if (Languages.message("JpegQuailty").equals(selected.getText())) {
            pdfFormat = PdfImageFormat.Jpeg;
            jpegBox.setDisable(false);
            checkJpegQuality();
        }
    }

    protected void checkJpegQuality() {
        jpegQuality = 100;
        try {
            jpegQuality = Integer.parseInt(jpegBox.getSelectionModel().getSelectedItem());
            if (jpegQuality >= 0 && jpegQuality <= 100) {
                jpegBox.setStyle(null);
            } else {
                jpegBox.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            jpegBox.setStyle(UserConfig.badStyle());
        }
    }

    protected void checkThreshold() {
        try {
            if (thresholdInput.getText().isEmpty()) {
                threshold = -1;
                thresholdInput.setStyle(null);
                return;
            }
            threshold = Integer.parseInt(thresholdInput.getText());
            if (threshold >= 0 && threshold <= 255) {
                thresholdInput.setStyle(null);
            } else {
                threshold = -1;
                thresholdInput.setStyle(UserConfig.badStyle());
            }
        } catch (Exception e) {
            threshold = -1;
            thresholdInput.setStyle(UserConfig.badStyle());
        }
    }

    @Override
    public PDImageXObject handleImage(FxTask currentTask, BufferedImage sourceImage) {
        if (sourceImage == null) {
            return null;
        }
        try {
            PDImageXObject newObject = null;
            if (pdfFormat == PdfImageFormat.Tiff) {
                ImageBinary imageBinary = new ImageBinary();
                imageBinary.setAlgorithm(ImageBinary.BinaryAlgorithm.Threshold)
                        .setImage(sourceImage)
                        .setIntPara1(threshold)
                        .setIsDithering(ditherCheck.isSelected());
                BufferedImage newImage = imageBinary.start();
                newImage = ImageBinary.byteBinary(currentTask, newImage);
                if (currentTask == null || !currentTask.isWorking()) {
                    return null;
                }
                if (newImage == null) {
                    return null;
                }
                newObject = CCITTFactory.createFromImage(doc, newImage);

            } else if (pdfFormat == PdfImageFormat.Jpeg) {
                newObject = JPEGFactory.createFromImage(doc, sourceImage, jpegQuality / 100f);
            }
            return newObject;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
