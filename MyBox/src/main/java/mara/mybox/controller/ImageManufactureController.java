/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import static mara.mybox.controller.BaseController.logger;
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

    protected int saturateStep = 5, brightnessStep = 5, hueStep = 5, percent = 60;
    protected int saturateOffset = 0, brightnessOffset = 0, hueOffset = 0;

    @FXML
    protected ToolBar fileBar, operationBar, setBar;

    @Override
    protected void initializeNext2() {
        try {

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void afterImageLoaded() {
        super.afterImageLoaded();
        if (image != null) {
            fileBar.setDisable(false);
            operationBar.setDisable(false);
            setBar.setDisable(false);
        }
    }

    @FXML
    public void recovery() {
        imageView.setImage(image);
    }

    @FXML
    public void opSaturate() {
        setBar.getItems().clear();
        bButton = new Button(getMessage("Recovery"));
        bButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                recovery();
            }
        });
        Button increaseButton = new Button(getMessage("Increase"));
        increaseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FxmlTools.changeSaturate(imageView, saturateStep / 100.0f);
                saturateOffset += saturateStep;
                if (saturateOffset > 100) {
                    saturateOffset = 100;
                }
            }
        });
        Button decreaseButton = new Button(getMessage("Decrease"));
        decreaseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FxmlTools.changeSaturate(imageView, 0.0f - saturateStep / 100.0f);
                saturateOffset -= saturateStep;
            }
        });
        final Label stepLabel = new Label(getMessage("AdjustmentStep"));
        final Label stepValue = new Label(saturateStep + "%");
        stepValue.setPrefWidth(80);
        stepValue.setAlignment(Pos.CENTER_RIGHT);
        Slider stepSlider = new Slider(5, 95, saturateStep);
        stepSlider.setBlockIncrement(5.0f);
        stepSlider.setShowTickLabels(true);
        stepSlider.setShowTickMarks(true);
        stepSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                saturateStep = newValue.intValue();
                stepValue.setText(saturateStep + "%");
            }
        });

        setBar.getItems().addAll(bButton, increaseButton, decreaseButton, stepValue, stepSlider, stepLabel);
    }

    @FXML
    public void opZoom() {
        setBar.getItems().clear();
        oButton = new Button(getMessage("OriginalSize"));
        oButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                originalSize();
            }
        });
        wButton = new Button(getMessage("WindowSize"));
        wButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                windowSize();
            }
        });
        inButton = new Button(getMessage("ZoomIn"));
        inButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                zoomIn();
            }
        });
        outButton = new Button(getMessage("ZoomOut"));
        outButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                zoomOut();
            }
        });

        final Label stepLabel = new Label(getMessage("ZoomStep"));
        final Label stepValue = new Label(zoomStep + "%");
        stepValue.setPrefWidth(80);
        stepValue.setAlignment(Pos.CENTER_RIGHT);
        Slider stepSlider = new Slider(5, 95, zoomStep);
        stepSlider.setBlockIncrement(5.0f);
        stepSlider.setShowTickLabels(true);
        stepSlider.setShowTickMarks(true);
        stepSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                zoomStep = newValue.intValue();
                stepValue.setText(zoomStep + "%");
            }
        });

        setBar.getItems().addAll(oButton, wButton, inButton, outButton, stepValue, stepSlider, stepLabel);
    }

    @FXML
    public void opRotate() {
        setBar.getItems().clear();
        bButton = new Button(getMessage("Recovery"));
        bButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                back();
            }
        });
        rButton = new Button(getMessage("RotateRight"));
        rButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                rotateRight();
            }
        });
        lButton = new Button(getMessage("RotateLeft"));
        lButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                rotateLeft();
            }
        });
        tButton = new Button(getMessage("TurnOver"));
        tButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                turnOver();
            }
        });
        final Label stepLabel = new Label(getMessage("RotateAngle"));
        final Label stepValue = new Label(rotateAngle + "");
        stepValue.setPrefWidth(80);
        stepValue.setAlignment(Pos.CENTER_RIGHT);
        Slider stepSlider = new Slider(5, 355, rotateAngle);
        stepSlider.setBlockIncrement(5.0f);
        stepSlider.setShowTickLabels(true);
        stepSlider.setShowTickMarks(true);
        stepSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                stepValue.setText(newValue.intValue() + "");
                rotateAngle = newValue.intValue();
            }
        });
        setBar.getItems().addAll(bButton, rButton, lButton, tButton, stepValue, stepSlider, stepLabel);

    }

    @FXML
    public void opHue() {
        setBar.getItems().clear();
        bButton = new Button(getMessage("Recovery"));
        bButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                recovery();
            }
        });
        Button increaseButton = new Button(getMessage("Increase"));
        increaseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FxmlTools.changeHue(imageView, hueStep);
            }
        });
        Button decreaseButton = new Button(getMessage("Decrease"));
        decreaseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FxmlTools.changeHue(imageView, 0 - hueStep);
            }
        });
        final Label stepLabel = new Label(getMessage("AdjustmentStep"));
        final Label stepValue = new Label(hueStep + "");
        stepValue.setPrefWidth(80);
        stepValue.setAlignment(Pos.CENTER_RIGHT);
        Slider stepSlider = new Slider(1, 359, hueStep);
        stepSlider.setBlockIncrement(5.0f);
        stepSlider.setShowTickLabels(true);
        stepSlider.setShowTickMarks(true);
        stepSlider.setPrefWidth(200);
        stepSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                hueStep = newValue.intValue();
                stepValue.setText(hueStep + "");
            }
        });

        setBar.getItems().addAll(bButton, increaseButton, decreaseButton, stepValue, stepSlider, stepLabel);

    }

    @FXML
    public void opBrighterness() {
        setBar.getItems().clear();
        bButton = new Button(getMessage("Recovery"));
        bButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                recovery();
            }
        });
        Button increaseButton = new Button(getMessage("Increase"));
        increaseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FxmlTools.changeBrightness(imageView, brightnessStep / 100.0f);
            }
        });
        Button decreaseButton = new Button(getMessage("Decrease"));
        decreaseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FxmlTools.changeBrightness(imageView, 0.0f - brightnessStep / 100.0f);
            }
        });
        final Label stepLabel = new Label(getMessage("AdjustmentStep"));
        final Label stepValue = new Label(brightnessStep + "%");
        stepValue.setPrefWidth(80);
        stepValue.setAlignment(Pos.CENTER_RIGHT);
        Slider stepSlider = new Slider(5, 95, brightnessStep);
        stepSlider.setBlockIncrement(5.0f);
        stepSlider.setShowTickLabels(true);
        stepSlider.setShowTickMarks(true);
        stepSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                brightnessStep = newValue.intValue();
                stepValue.setText(brightnessStep + "%");
            }
        });

        setBar.getItems().addAll(bButton, increaseButton, decreaseButton, stepValue, stepSlider, stepLabel);

    }

    @FXML
    public void opFilters() {
        setBar.getItems().clear();
        bButton = new Button(getMessage("Recovery"));
        bButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                recovery();
            }
        });
        Button invertButton = new Button(getMessage("InvertColor"));
        invertButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FxmlTools.manufactureImage(imageView, ImageManufactureType.Invert);
            }
        });
        Button grayButton = new Button(getMessage("Gray"));
        grayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FxmlTools.manufactureImage(imageView, ImageManufactureType.Gray);
            }
        });
        Button binaryButton = new Button(getMessage("BlackOrWhite"));
        binaryButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FxmlTools.makeBinary(imageView, percent);
            }
        });
        final Label stepLabel = new Label(getMessage("BinaryThreshold"));
        final Label stepValue = new Label(percent + "%");
        stepValue.setPrefWidth(80);
        stepValue.setAlignment(Pos.CENTER_RIGHT);
        Slider stepSlider = new Slider(5, 95, percent);
        stepSlider.setBlockIncrement(5.0f);
        stepSlider.setShowTickLabels(true);
        stepSlider.setShowTickMarks(true);
        stepSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                percent = newValue.intValue();
                stepValue.setText(percent + "%");
            }
        });

        setBar.getItems().addAll(bButton, invertButton, grayButton, binaryButton, stepValue, stepSlider, stepLabel);

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
    public void setBrighter() {
        FxmlTools.manufactureImage(imageView, ImageManufactureType.Brighter);
    }

    @FXML
    public void setDarker() {
        FxmlTools.manufactureImage(imageView, ImageManufactureType.Darker);
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
            showImageManufacture(file.getAbsolutePath());
//            ImageIO.write(SwingFXUtils.fromFXImage(imageView.getImage(), null), format, file);
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

}
