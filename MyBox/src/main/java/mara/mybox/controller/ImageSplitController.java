package mara.mybox.controller;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.image.ImageConvertionTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;
import mara.mybox.tools.FxmlImageTools;

/**
 * @Author Mara
 * @CreateDate 2018-8-8
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageSplitController extends ImageViewerController {

    private List<Integer> rows, cols;
    protected SimpleBooleanProperty parametersValid;
    private int rowsNumber, colsNumber, pixelPickingType;
    private boolean isCustom;

    public static class PixelPickingType {

        public static int None = 0;
        public static int Row = 1;
        public static int Columm = 2;
    }

    @FXML
    private TabPane tabPane;
    @FXML
    private SplitPane splitPane;
    @FXML
    private Tab sourceTab, targetTab, equiTab, customTab;
    @FXML
    private ToolBar hotBar, sourceBar;
    @FXML
    private Button infoButton, metaButton, equiOkButton, saveButton, rowsPositionButton, colsPositionButton;
    @FXML
    private Button imageButton, wButton, openTargetButton;
    @FXML
    private TextField rowsNumberInput, colsNumberInput, rowsInput, colsInput;
    @FXML
    private CheckBox displaySizeCheck;
    @FXML
    private ComboBox<String> targetTypeBox;
    @FXML
    private ComboBox<Integer> lineWidthBox;
    @FXML
    private RadioButton customRadio, euqipartitionRadio;
    @FXML
    private ToggleGroup splitGroup;
    @FXML
    private ColorPicker lineColorPicker;

    @Override
    protected void initializeNext() {
        try {

            initCommon();
            initTargetTab();
            initEquiParttitonTab();
            initCustomTab();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initCommon() {

        sourceBar.setDisable(true);
        hotBar.setDisable(true);
        targetTab.setDisable(true);
        equiTab.setDisable(true);
        customTab.setDisable(true);

        splitGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov,
                    Toggle old_toggle, Toggle new_toggle) {
                imageView.setImage(image);
                bottomLabel.setText("");
                RadioButton selected = (RadioButton) splitGroup.getSelectedToggle();
                if (AppVaribles.getMessage("Equipartition").equals(selected.getText())) {
                    isCustom = false;
                    equiTab.setDisable(false);
                    customTab.setDisable(true);
                    tabPane.getSelectionModel().select(equiTab);
                } else if (AppVaribles.getMessage("Custom").equals(selected.getText())) {
                    isCustom = true;
                    equiTab.setDisable(true);
                    customTab.setDisable(false);
                    tabPane.getSelectionModel().select(customTab);
                    checkCustomValues();
                }

            }
        });

        lineWidthBox.getItems().addAll(Arrays.asList(15, 10, 20, 5, 30, 8, 40));
        lineWidthBox.getSelectionModel().select(0);
        lineWidthBox.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> ov,
                    Integer oldValue, Integer newValue) {
                indicateSplit();
            }
        });

        lineColorPicker.setValue(Color.RED);
        lineColorPicker.valueProperty().addListener(new ChangeListener<Color>() {
            @Override
            public void changed(ObservableValue<? extends Color> ov,
                    Color oldValue, Color newValue) {
                indicateSplit();

            }
        });

        if (AppVaribles.showComments) {
            Tooltip tips = new Tooltip(getMessage("SplitComments"));
            tips.setFont(new Font(16));
            FxmlTools.setComments(lineWidthBox, tips);
            FxmlTools.setComments(lineColorPicker, tips);
        }

        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable,
                    Tab oldValue, Tab newValue) {
                if (customTab.equals(tabPane.getSelectionModel().getSelectedItem())) {
                    bottomLabel.setText(getMessage("SplitCustomComments"));
                    popInformation(getMessage("SplitCustomComments"));
                } else {
                    hidePopup();
                }
            }
        });

        parametersValid = new SimpleBooleanProperty(false);
        saveButton.disableProperty().bind(
                Bindings.isEmpty(targetPathInput.textProperty())
                        .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                        .or(parametersValid.isEqualTo(new SimpleBooleanProperty(false)))
        );

    }

    private void initTargetTab() {
        targetTypeBox.getItems().addAll(CommonValues.SupportedImages);
//        targetTypeBox.valueProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue ov, String oldValue, String newValue) {
//
//            }
//        });
        targetTypeBox.getSelectionModel().select(0);

        targetPathInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                try {
                    final File file = new File(newValue);
                    if (!file.exists() || !file.isDirectory()) {
                        targetPathInput.setStyle(badStyle);
                        targetPath = null;
                        return;
                    }
                    targetPathInput.setStyle(null);
                    AppVaribles.setConfigValue(targetPathKey, file.getPath());
                    targetPath = file;
                } catch (Exception e) {
                    targetPathInput.setStyle(badStyle);
                    targetPath = null;
                }
            }
        });

        openTargetButton.disableProperty().bind(
                Bindings.isEmpty(targetPathInput.textProperty())
                        .or(targetPathInput.styleProperty().isEqualTo(badStyle))
        );
    }

    private void initEquiParttitonTab() {
        rowsNumberInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkEquiValues();
            }
        });
        colsNumberInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkEquiValues();
            }
        });

    }

    private void checkEquiValues() {
        boolean isValid = true;
        try {
            rowsNumber = Integer.valueOf(rowsNumberInput.getText());
            rowsNumberInput.setStyle(null);
            if (rowsNumber > 0) {
                rowsNumberInput.setStyle(null);
            } else {
                rowsNumberInput.setStyle(badStyle);
                isValid = false;
            }
        } catch (Exception e) {
            rowsNumberInput.setStyle(badStyle);
            isValid = false;
        }
        try {
            colsNumber = Integer.valueOf(colsNumberInput.getText());
            colsNumberInput.setStyle(null);
            if (colsNumber > 0) {
                colsNumberInput.setStyle(null);
            } else {
                colsNumberInput.setStyle(badStyle);
                isValid = false;
            }
        } catch (Exception e) {
            colsNumberInput.setStyle(badStyle);
            isValid = false;
        }
        if (isValid) {
            equiOkButton.setDisable(false);
            parametersValid.set(true);
        } else {
            equiOkButton.setDisable(true);
            parametersValid.set(false);
            bottomLabel.setText("");
        }
    }

    private void initCustomTab() {
        rowsInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkCustomValues();
            }
        });
        colsInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                checkCustomValues();
            }
        });

    }

    private void checkCustomValues() {
        boolean isValidRows = true, isValidcols = true;
        rows = new ArrayList();
        rows.add(0);
        rows.add((int) image.getHeight() - 1);
        cols = new ArrayList();
        cols.add(0);
        cols.add((int) image.getWidth() - 1);

        String[] rowStrings = rowsInput.getText().split(",");
        for (String row : rowStrings) {
            try {
                int value = Integer.valueOf(row.trim());
                if (value < 0 || value > image.getHeight() - 1) {
                    isValidRows = false;
                    break;
                }
                if (!rows.contains(value)) {
                    rows.add(value);
                }
            } catch (Exception e) {
                isValidRows = false;
                break;
            }
        }

        String[] colStrings = colsInput.getText().split(",");
        for (String col : colStrings) {
            try {
                int value = Integer.valueOf(col.trim());
                if (value <= 0 || value >= image.getWidth() - 1) {
                    isValidcols = false;
                    break;
                }
                if (!cols.contains(value)) {
                    cols.add(value);
                }
            } catch (Exception e) {
                isValidcols = false;
                break;
            }
        }

        if (isValidRows || isValidcols) {
            rowsInput.setStyle(null);
            colsInput.setStyle(null);
            parametersValid.set(true);
            indicateSplit();
        } else {
            colsInput.setStyle(badStyle);
            rowsInput.setStyle(badStyle);
            parametersValid.set(false);
            imageView.setImage(image);
            bottomLabel.setText("");
            popInformation(getMessage("SplitCustomComments"));
        }
    }

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();
            if (image == null) {
                return;
            }

//            currentImage = image;
            sourceBar.setDisable(false);
            hotBar.setDisable(false);
            targetTab.setDisable(false);

            euqipartitionRadio.setSelected(true);
            targetPathInput.setText(sourceFile.getParent());
            targetPrefixInput.setText(FileTools.getFilePrefix(sourceFile.getName()));
            targetTypeBox.getSelectionModel().select(FileTools.getFileSuffix(sourceFile.getName()));

            cols = new ArrayList();
            rows = new ArrayList();
            Integer v = 10 + imageInformation.getyPixels() * 3 / 1000;
            lineWidthBox.getItems().add(0, v);
            lineWidthBox.getSelectionModel().select(v);

            parametersValid.set(false);

            bottomLabel.setText(getMessage("SplitComments"));

            getMyStage().setTitle(getBaseTitle() + "  " + sourceFile.getAbsolutePath());
        } catch (Exception e) {
            logger.debug(e.toString());
        }

    }

    @FXML
    private void equiOkAction(ActionEvent event) {
        divideImage(rowsNumber, colsNumber);
    }

    @FXML
    private void openTargetPath(ActionEvent event) {
        try {
            Desktop.getDesktop().browse(targetPath.toURI());
        } catch (Exception e) {

        }
    }

    @FXML
    private void do42Action(ActionEvent event) {
        divideImage(4, 2);
    }

    @FXML
    private void do43Action(ActionEvent event) {
        divideImage(4, 3);
    }

    @FXML
    private void do44Action(ActionEvent event) {
        divideImage(4, 4);
    }

    @FXML
    private void do13Action(ActionEvent event) {
        divideImage(1, 3);
    }

    @FXML
    private void do31Action(ActionEvent event) {
        divideImage(3, 1);
    }

    @FXML
    private void do12Action(ActionEvent event) {
        divideImage(1, 2);
    }

    @FXML
    private void do21Action(ActionEvent event) {
        divideImage(2, 1);
    }

    @FXML
    private void do32Action(ActionEvent event) {
        divideImage(3, 2);
    }

    @FXML
    private void do23Action(ActionEvent event) {
        divideImage(2, 3);
    }

    @FXML
    private void do22Action(ActionEvent event) {
        divideImage(2, 2);
    }

    @FXML
    private void do33Action(ActionEvent event) {
        divideImage(3, 3);

    }

    @FXML
    private void defineOkAction(ActionEvent event) {
        checkCustomValues();
        indicateSplit();
    }

    @FXML
    private void clearRows(ActionEvent event) {
        rowsInput.setText("");
    }

    @FXML
    private void clearCols(ActionEvent event) {
        colsInput.setText("");
    }

    @FXML
    private void clickImage(MouseEvent event) {
        if (image == null || !customTab.equals(tabPane.getSelectionModel().getSelectedItem())) {
            return;
        }
//        imageView.setCursor(Cursor.OPEN_HAND);
        bottomLabel.setText(getMessage("SplitCustomComments"));
        popInformation(getMessage("SplitCustomComments"));

        if (event.getButton() == MouseButton.PRIMARY) {

            int y = (int) Math.round(event.getY() * image.getHeight() / imageView.getBoundsInLocal().getHeight());
            String str = rowsInput.getText().trim();
            if (str.isEmpty()) {
                rowsInput.setText(y + "");
            } else {
                rowsInput.setText(str + "," + y);
            }

        } else if (event.getButton() == MouseButton.SECONDARY) {
            int x = (int) Math.round(event.getX() * image.getWidth() / imageView.getBoundsInLocal().getWidth());
            String str = colsInput.getText().trim();
            if (str.isEmpty()) {
                colsInput.setText(x + "");
            } else {
                colsInput.setText(str + "," + x);
            }

        }

    }

    @FXML
    private void saveAction(ActionEvent event) {
        if (image == null || targetPath == null
                || rows == null || cols == null
                || rows.size() < 1 || cols.size() < 1) {
            parametersValid.set(false);
            parametersValid.set(false);
            return;
        }
        Task divideTask = new Task<Void>() {
            List<String> fileNames = new ArrayList<>();

            @Override
            protected Void call() throws Exception {
                int x1, y1, x2, y2, count = 0;
                final BufferedImage source = FxmlImageTools.getWritableData(image, imageInformation.getImageFormat());
                String format = targetTypeBox.getSelectionModel().getSelectedItem();
                for (int i = 0; i < rows.size() - 1; i++) {
                    y1 = rows.get(i);
                    y2 = rows.get(i + 1);
                    for (int j = 0; j < cols.size() - 1; j++) {
                        x1 = cols.get(j);
                        x2 = cols.get(j + 1);
                        BufferedImage target = ImageConvertionTools.cropImage(source, x1, y1, x2, y2);
                        String fileName = targetPath.getAbsolutePath() + "/"
                                + targetPrefixInput.getText() + "_"
                                + (rows.size() - 1) + "x" + (cols.size() - 1) + "_"
                                + (i + 1) + "-" + (j + 1)
                                + "." + format;
                        ImageFileWriters.writeImageFile(target, format, fileName);
                        fileNames.add(new File(fileName).getAbsolutePath());
                    }
                }

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle(getMyStage().getTitle());
                            String info = MessageFormat.format(AppVaribles.getMessage("SplitResult"),
                                    fileNames.size(), "\"" + targetPath.getAbsolutePath() + "\"");
                            int num = fileNames.size();
                            if (num > 10) {
                                num = 10;
                            }
                            for (int i = 0; i < num; i++) {
                                info += "\n    " + fileNames.get(i);
                            }
                            if (fileNames.size() > num) {
                                info += "\n    ......";
                            }
                            alert.setContentText(info);
                            ButtonType buttonOpen = new ButtonType(AppVaribles.getMessage("OpenTargetPath"));
                            ButtonType buttonBrowse = new ButtonType(AppVaribles.getMessage("Browse"));
                            ButtonType buttonBrowseNew = new ButtonType(AppVaribles.getMessage("BrowseInNew"));
                            ButtonType buttonClose = new ButtonType(AppVaribles.getMessage("Close"));
                            alert.getButtonTypes().setAll(buttonBrowseNew, buttonBrowse, buttonOpen, buttonClose);
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.get() == buttonOpen) {
                                Desktop.getDesktop().browse(targetPath.toURI());
                            } else if (result.get() == buttonBrowse) {
                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImagesViewerFxml), AppVaribles.CurrentBundle);
                                Pane pane = fxmlLoader.load();
                                final ImagesViewerController controller = fxmlLoader.getController();
                                controller.setMyStage(myStage);
                                myStage.setScene(new Scene(pane));
                                myStage.setTitle(AppVaribles.getMessage("MultipleImagesViewer"));
                                controller.loadImages(fileNames, cols.size() - 1);
                            } else if (result.get() == buttonBrowseNew) {
                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.ImagesViewerFxml), AppVaribles.CurrentBundle);
                                Pane pane = fxmlLoader.load();
                                final ImagesViewerController controller = fxmlLoader.getController();
                                Stage stage = new Stage();
                                controller.setMyStage(stage);
                                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                                    @Override
                                    public void handle(WindowEvent event) {
                                        if (!controller.stageClosing()) {
                                            event.consume();
                                        }
                                    }
                                });
                                stage.setScene(new Scene(pane));
                                stage.setTitle(AppVaribles.getMessage("MultipleImagesViewer"));
                                stage.show();
                                controller.loadImages(fileNames, cols.size() - 1);
                            }

                        } catch (Exception e) {
                        }

                    }
                });
                return null;
            }
        };
        openHandlingStage(divideTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(divideTask);
        thread.setDaemon(true);
        thread.start();

    }

    private void divideImage(int rowsNumber, int colsNumber) {
        if (rowsNumber <= 0 || colsNumber <= 0) {
            parametersValid.set(false);
            return;
        }
        cols = new ArrayList();
        cols.add(0);
        for (int i = 1; i < colsNumber; i++) {
            cols.add(i * imageInformation.getxPixels() / colsNumber);
        }
        cols.add(imageInformation.getxPixels() - 1);
        rows = new ArrayList();
        rows.add(0);
        for (int i = 1; i < rowsNumber; i++) {
            rows.add(i * imageInformation.getyPixels() / rowsNumber);
        }
        rows.add(imageInformation.getyPixels() - 1);
        parametersValid.set(true);

        indicateSplit();
    }

    private void indicateSplit() {
        if (image == null) {
            return;
        }
        if (rows == null || cols == null
                || rows.size() < 2 || cols.size() < 2) {
            imageView.setImage(image);
            return;
        }
//        final Image newImage = FxImageTools.indicateSplitFx(image, rows, cols,
//                lineColorPicker.getValue(), lineWidthBox.getValue(), displaySizeCheck.isSelected());
//        if (newImage != null) {
//            imageView.setImage(newImage);
//            String comments = AppVaribles.getMessage("SplittedNumber") + ": "
//                    + (cols.size() - 1) * (rows.size() - 1);
//            if (!isCustom) {
//                comments += "  " + AppVaribles.getMessage("ImageSize") + ": "
//                        + ((int) image.getWidth() / (cols.size() - 1))
//                        + " x " + ((int) image.getHeight() / (rows.size() - 1));
//            }
//            bottomLabel.setText(comments);
//            return;
//        }

        // If JavaFx way fail, then go way of Java2D
        Task divideTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Collections.sort(rows, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer p1, Integer p2) {
                        return p1 - p2;
                    }
                });
                Collections.sort(cols, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer p1, Integer p2) {
                        return p1 - p2;
                    }
                });
                final Image newImage = FxmlImageTools.indicateSplit(image, rows, cols,
                        lineColorPicker.getValue(), lineWidthBox.getValue(), displaySizeCheck.isSelected());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        String comments = AppVaribles.getMessage("SplittedNumber") + ": "
                                + (cols.size() - 1) * (rows.size() - 1);
                        if (!isCustom) {
                            comments += "  " + AppVaribles.getMessage("ImageSize") + ": "
                                    + ((int) image.getWidth() / (cols.size() - 1))
                                    + " x " + ((int) image.getHeight() / (rows.size() - 1));
                        }
                        bottomLabel.setText(comments);
                        if (customTab.equals(tabPane.getSelectionModel().getSelectedItem())) {
                            popInformation(AppVaribles.getMessage("SplitCustomComments"));
                        }
                    }
                });
                return null;
            }
        };
        openHandlingStage(divideTask, Modality.WINDOW_MODAL);
        Thread thread = new Thread(divideTask);
        thread.setDaemon(true);
        thread.start();
    }

}
