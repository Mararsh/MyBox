package mara.mybox.controller;

import com.sun.javafx.charts.Legend;
import mara.mybox.fxml.FxmlStage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.fxml.image.ImageTools;
import mara.mybox.fxml.image.FxmlScopeTools;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlTools;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.data.ImageFileInformation;
import mara.mybox.data.IntRectangle;
import mara.mybox.data.IntStatistic;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.image.ImageStatistic;
import static mara.mybox.value.AppVaribles.getMessage;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends ImageBaseController {

    final protected String ConfirmDeleteKey, OpenAfterSaveKey, SelectKey;

    protected double mouseX, mouseY;
    protected int xZoomStep = 50, yZoomStep = 50;
    protected int currentAngle = 0, rotateAngle = 90;
    protected File nextFile, previousFile;
    protected int cropLeftX, cropLeftY, cropRightX, cropRightY;

    protected ObservableList<IntStatistic> statisticList = FXCollections.observableArrayList();
    protected Map<String, Color> colorTable;

    @FXML
    protected TextField imageFile;
    @FXML
    protected HBox operation1Box, operation2Box, operation3Box, navBox;
    @FXML
    protected VBox contentBox;
    @FXML
    protected Button upButton, downButton, renameButton, metaButton, manufactureButton, statisticButton;
    @FXML
    protected ToggleGroup sortGroup;
    @FXML
    protected CheckBox cropCheck, deleteConfirmCheck, openSaveCheck;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected CheckBox dataCheck, greyHistCheck, redHistCheck, greenHistCheck, blueHistCheck, alphaHistCheck,
            hueHistCheck, saturationHistCheck, brightnessHistCheck;
    @FXML
    protected SplitPane dataPane;
    @FXML
    protected TableView<IntStatistic> dataTable;
    @FXML
    protected TableColumn<IntStatistic, String> colorColumn;
    @FXML
    protected TableColumn<IntStatistic, Integer> meanColumn, varianceColumn,
            skewnessColumn, maximumColumn, minimumColumn, modeColumn;
    @FXML
    protected BarChart histogramChart;
    @FXML
    protected ToolBar manuBar;

    public ImageViewerController() {
        ConfirmDeleteKey = "ImageConfirmDeleteKey";
        OpenAfterSaveKey = "ImageOpenAfterSaveKey";
        SelectKey = "ImageSelectKey";
        TipsLabelKey = "ImageViewerTips";
    }

    @Override
    protected void initializeNext() {
        try {
            initOperation1Box();
            initOperation2Box();
            initOperation3Box();
            initImageView();
            initDataPane();
            initializeNext2();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initOperation1Box() {
        if (operation1Box != null) {
            operation1Box.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );
        }

        if (openSaveCheck != null) {
            openSaveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue(OpenAfterSaveKey, openSaveCheck.isSelected());
                }
            });
            openSaveCheck.setSelected(AppVaribles.getUserConfigBoolean(OpenAfterSaveKey, false));
        }
        if (deleteConfirmCheck != null) {
            deleteConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue(ConfirmDeleteKey, deleteConfirmCheck.isSelected());
                }
            });
            deleteConfirmCheck.setSelected(AppVaribles.getUserConfigBoolean(ConfirmDeleteKey, true));
        }

    }

    protected void initOperation2Box() {

        if (operation2Box != null) {
            operation2Box.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );
        }

        if (cropCheck != null) {
            cropCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue(SelectKey, cropCheck.isSelected());
                    checkSelect();
                }
            });
            cropCheck.setSelected(AppVaribles.getUserConfigBoolean(SelectKey, true));
            checkSelect();
        }

    }

    protected void initOperation3Box() {

        if (navBox != null) {
            navBox.setDisable(true);
        }

        if (manufactureButton != null) {
            manufactureButton.setDisable(true);
        }

        if (manuBar != null) {
            manuBar.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );
        }

    }

    protected void initImageView() {
        if (imageView == null || scrollPane == null) {
            return;
        }

        imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                moveCenter();
            }
        });
        scrollPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                if (Math.abs(imageView.getFitHeight() - old_val.doubleValue()) < 20) {
                    paneSize();
                }
                moveCenter();
            }
        });
        scrollPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                moveCenter();
            }
        });

    }

    protected void checkSelect() {
        if (image == null || cropCheck == null) {
            return;
        }
        if (!cropCheck.isSelected()) {
            imageView.setImage(image);
            FxmlTools.removeTooltip(imageView);
            bottomLabel.setText("");
        } else {
            selectAllAction();

            bottomLabel.setText(getMessage("CropLabel"));
        }
        selectAllButton.setDisable(!cropCheck.isSelected());

    }

    public void moveCenter() {
        imageView.setTranslateX(0);
        if (imageView.getImage() == null) {
            return;
        }
        double w = imageView.getImage().getWidth() * imageView.getFitHeight() / imageView.getImage().getHeight();
        double offset = scrollPane.getWidth() - w;
        if (offset > 0) {
            imageView.setTranslateX(offset / 2);
        }
    }

    protected void initDataPane() {
        try {
            if (dataCheck == null || dataPane == null) {
                return;
            }
            splitPane.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );

            dataCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    if (isSettingValues) {
                        return;
                    }
                    loadDataBox();
                    AppVaribles.setUserConfigValue("ImageDataShow", new_val);
                }
            });
            isSettingValues = true;
            dataCheck.setSelected(AppVaribles.getUserConfigBoolean("ImageDataShow", true));
            isSettingValues = false;
            loadDataBox();

            dataTable.setItems(statisticList);
            colorColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, String>("name"));
            colorColumn.setCellFactory(new Callback<TableColumn<IntStatistic, String>, TableCell<IntStatistic, String>>() {
                @Override
                public TableCell<IntStatistic, String> call(TableColumn<IntStatistic, String> param) {
                    return new NameCell();
                }
            });
            meanColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, Integer>("mean"));
            meanColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new ColorCell();
                }
            });
            varianceColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, Integer>("variance"));
            skewnessColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, Integer>("skewness"));
            maximumColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, Integer>("maximum"));
            maximumColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new ColorCell();
                }
            });
            minimumColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, Integer>("minimum"));
            minimumColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new ColorCell();
                }
            });
            modeColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, Integer>("mode"));
            modeColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new ColorCell();
                }
            });

            colorTable = new HashMap<>();
            colorTable.put(AppVaribles.getMessage("Red"), Color.RED);
            colorTable.put(AppVaribles.getMessage("Green"), Color.GREEN);
            colorTable.put(AppVaribles.getMessage("Blue"), Color.BLUE);
            colorTable.put(AppVaribles.getMessage("Alpha"), Color.CORNFLOWERBLUE);
            colorTable.put(AppVaribles.getMessage("Hue"), Color.MEDIUMVIOLETRED);
            colorTable.put(AppVaribles.getMessage("Brightness"), Color.GOLD);
            colorTable.put(AppVaribles.getMessage("Saturation"), Color.MEDIUMAQUAMARINE);
            colorTable.put(AppVaribles.getMessage("Gray"), Color.GRAY);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private class NameCell extends TableCell<IntStatistic, String> {

        final Text text = new Text();

        @Override
        protected void updateItem(final String item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !empty) {
                text.setText(AppVaribles.getMessage(item));
                setGraphic(text);
            }
        }
    }

    private class ColorCell extends TableCell<IntStatistic, Integer> {

        private final Rectangle rectangle;

        {
            setContentDisplay(ContentDisplay.LEFT);
            rectangle = new Rectangle(30, 20);
        }
        private Color color;

        @Override
        protected void updateItem(final Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || item < 0 || empty) {
                setGraphic(null);
            } else {
                IntStatistic row = getTableView().getItems().get(getTableRow().getIndex());

                switch (row.getName()) {
                    case "Red":
                        color = new Color(item / 255.0, 0, 0, 1);
                        break;
                    case "Green":
                        color = new Color(0, item / 255.0, 0, 1);
                        break;
                    case "Blue":
                        color = new Color(0, 0, item / 255.0, 1);
                        break;
                    case "Alpha":
                        color = new Color(1, 1, 1, item / 255.0);
                        break;
                    case "Grey":
                        double c = item / 255.0;
                        color = new Color(c, c, c, 1);
                        break;
                    case "Hue":
                        color = Color.hsb(item, 1, 1);
                        break;
                    case "Saturation":
                        color = Color.hsb(66, item / 100.0, 1);
                        break;
                    case "Brightness":
                        color = Color.hsb(66, 1, item / 100.0);
                        break;
                    default:
                        color = null;
                        break;
                }
                if (color != null) {
                    rectangle.setFill(color);
                    setGraphic(rectangle);
                }
                setText(item + "");

            }
        }

    }

    protected void loadDataBox() {
        try {
            if (dataCheck == null) {
                return;
            }
            if (dataCheck.isSelected()) {

                if (!splitPane.getItems().contains(dataPane)) {
                    splitPane.getItems().add(0, dataPane);
                }
                if (imageData != null) {
                    showData();
                } else {
                    loadData();
                }

            } else {

                if (splitPane.getItems().contains(dataPane)) {
                    splitPane.getItems().remove(dataPane);
                }
                statisticList.clear();
                histogramChart.getData().clear();
                adjustSplitPane();

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void loadData(Map<String, Object> imageData) {
        if (imageData != null) {
            this.imageData = imageData;
            showData();
        } else {
            loadData();
        }
    }

    protected void loadData() {
        imageData = null;
        if (imageView.getImage() == null || isSettingValues
                || dataCheck == null || !dataCheck.isSelected()
                || dataPane == null) {
            return;
        }
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    imageData = ImageStatistic.analyze(SwingFXUtils.fromFXImage(imageView.getImage(), null));
                    if (task.isCancelled() || imageData == null) {
                        return null;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            showData();
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void showData(Map<String, Object> imageData) {
        this.imageData = imageData;
        showData();
    }

    protected void showData() {
        if (imageData == null || isSettingValues
                || histogramChart == null) {
            return;
        }
        statisticList.clear();
        clearHistogram();
        List<IntStatistic> statistic = (List<IntStatistic>) imageData.get("statistic");
        statisticList.addAll(statistic);
        greyHistCheck.fire();
        adjustSplitPane();
    }

    protected void showHistogram(final String colorName) {
        if (imageData == null || isSettingValues || colorName == null
                || histogramChart == null || histogramChart.getData() == null) {
            return;
        }
        for (Object s : histogramChart.getData()) {
            XYChart.Series xy = (XYChart.Series) s;
            if (colorName.equals(xy.getName())) {
                return;
            }
        }

        int[] histogram;
        final String colorValue;
        if (AppVaribles.getMessage("Red").equals(colorName)) {
            histogram = (int[]) imageData.get("redHistogram");
            colorValue = "Red";
        } else if (AppVaribles.getMessage("Green").equals(colorName)) {
            histogram = (int[]) imageData.get("greenHistogram");
            colorValue = "Green";
        } else if (AppVaribles.getMessage("Blue").equals(colorName)) {
            histogram = (int[]) imageData.get("blueHistogram");
            colorValue = "Blue";
        } else if (AppVaribles.getMessage("Alpha").equals(colorName)) {
            histogram = (int[]) imageData.get("alphaHistogram");
            colorValue = "SkyBlue";
        } else if (AppVaribles.getMessage("Hue").equals(colorName)) {
            histogram = (int[]) imageData.get("hueHistogram");
            colorValue = "Pink";
        } else if (AppVaribles.getMessage("Brightness").equals(colorName)) {
            histogram = (int[]) imageData.get("brightnessHistogram");
            colorValue = "Wheat";
        } else if (AppVaribles.getMessage("Saturation").equals(colorName)) {
            histogram = (int[]) imageData.get("saturationHistogram");
            colorValue = "DarkRed";
        } else {
            histogram = (int[]) imageData.get("greyHistogram");
            colorValue = "Gray";
        }

        XYChart.Series series = new XYChart.Series();
        for (int i = 0; i < histogram.length; i++) {
            series.getData().add(new XYChart.Data(i + "", histogram[i]));
        }
        series.setName(colorName);

        histogramChart.getData().addAll(series);
        int index = histogramChart.getData().size() - 1;
        String colorString = FxmlColor.rgb2Hex(colorTable.get(colorName));
        for (Node n : histogramChart.lookupAll(".default-color" + index + ".chart-bar")) {
            n.setStyle("-fx-bar-fill: " + colorString);
        }
        updateLegend();

    }

    // https://stackoverflow.com/questions/12197877/javafx-linechart-legend-style
    private void updateLegend() {
        if (histogramChart == null) {
            return;
        }
        for (Node n : histogramChart.getChildrenUnmodifiable()) {
            if (n instanceof Legend) {
                for (Legend.LegendItem legendItem : ((Legend) n).getItems()) {
                    String colorString = FxmlColor.rgb2Hex(colorTable.get(legendItem.getText()));
                    legendItem.getSymbol().setStyle("-fx-background-color: " + colorString);
                }
            }
        }
    }

    protected void hideHistogram(String color) {
        if (imageData == null || isSettingValues || color == null
                || histogramChart == null || histogramChart.getData() == null) {
            return;
        }
        for (Object s : histogramChart.getData()) {
            XYChart.Series xy = (XYChart.Series) s;
            if (color.equals(xy.getName())) {
                histogramChart.getData().remove(s);
                updateLegend();
                return;
            }
        }
    }

    @FXML
    public void clearHistogram() {
        if (histogramChart != null) {
            isSettingValues = true;
            greyHistCheck.setSelected(false);
            redHistCheck.setSelected(false);
            greenHistCheck.setSelected(false);
            blueHistCheck.setSelected(false);
            alphaHistCheck.setSelected(false);
            hueHistCheck.setSelected(false);
            saturationHistCheck.setSelected(false);
            brightnessHistCheck.setSelected(false);
            isSettingValues = false;
            histogramChart.getData().clear();
        }
    }

    @FXML
    protected void colorChecked(ActionEvent event) {
        CheckBox checked = (CheckBox) event.getSource();
        if (checked.isSelected()) {
            showHistogram(checked.getText());
        } else {
            hideHistogram(checked.getText());
        }
    }

    protected void adjustSplitPane() {
        try {
            int size = splitPane.getItems().size();
            float p = 1.0f / size;
            if (size == 1) {
                splitPane.setDividerPositions(1);
            } else {
                for (int i = 0; i < size - 1; i++) {
                    splitPane.getDividers().get(i).setPosition(p);
                }
            }
            splitPane.layout();
            fitSize();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file.isDirectory()) {
            AppVaribles.setUserConfigValue(sourcePathKey, file.getPath());
        } else {
            AppVaribles.setUserConfigValue(sourcePathKey, file.getParent());
        }
        loadImage(file, false);
    }

    @Override
    protected void afterInfoLoaded() {
        if (infoButton != null) {
            infoButton.setDisable(imageInformation == null);
        }
        if (metaButton != null) {
            metaButton.setDisable(imageInformation == null);
        }
        if (statisticButton != null) {
            statisticButton.setDisable(image == null);
        }
        if (deleteButton != null) {
            deleteButton.setDisable(sourceFile == null);
        }
        if (renameButton != null) {
            renameButton.setDisable(sourceFile == null);
        }
        if (deleteConfirmCheck != null) {
            deleteConfirmCheck.setDisable(sourceFile == null);
        }
        if (navBox != null) {
            navBox.setDisable(sourceFile == null);
        }
        if (manufactureButton != null) {
            manufactureButton.setDisable(imageInformation == null && image == null);
        }

    }

    @Override
    public void afterImageLoaded() {
        try {
            afterInfoLoaded();
            if (image == null) {
                return;
            }
            imageView.setPreserveRatio(true);

            imageView.setImage(image);
            xZoomStep = (int) image.getWidth() / 10;
            yZoomStep = (int) image.getHeight() / 10;

            fitSize();
            if (imageFile != null && sourceFile != null) {
                imageFile.setText(sourceFile.getName());
            }

            if (imageInformation != null && imageInformation.isIsSampled()) {
                handleSampledImage();
                if (imageFile != null && sourceFile != null) {
                    imageFile.setText(sourceFile.getName() + " " + getMessage("Sampled"));
                }
            } else {
                if (sourceFile != null) {
                    if (imageInformation != null && imageInformation.getIndex() > 0) {
                        getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                                + " - " + getMessage("Image") + " " + imageInformation.getIndex());
                    } else {
                        getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
                    }
                } else {
                    getMyStage().setTitle(getBaseTitle());
                }
                if (bottomLabel != null) {
                    bottomLabel.setText("");
                }
            }

            if (sourceFile != null && navBox != null) {
                checkImageNevigator();
            }

            if (cropCheck != null) {
                checkSelect();
            } else {
                cropLeftX = 0;
                cropLeftY = 0;
                cropRightX = (int) image.getWidth() - 1;
                cropRightY = (int) image.getHeight() - 1;
            }

            if (dataPane != null) {
                loadData(imageData);
            }

        } catch (Exception e) {
            logger.error(e.toString());
            imageView.setImage(null);
            alertInformation(AppVaribles.getMessage("NotSupported"));
        }
    }

    protected void handleSampledImage() {
//            logger.debug(availableMem + "  " + pixelsSize + "  " + requiredMem + " " + sampledWidth + " " + sampledSize);
        if (imageInformation.getIndex() > 0) {
            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                    + " - " + getMessage("Image") + " " + imageInformation.getIndex() + " " + getMessage("Sampled"));
        } else {
            getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath() + " " + getMessage("Sampled"));
        }

        if (sizes == null) {
            if (bottomLabel != null) {
                bottomLabel.setText(getMessage("ImageSampled"));
            }
            return;
        }

        int sampledSize = (int) (image.getWidth() * image.getHeight() * imageInformation.getColorChannels() / (1014 * 1024));
        String msg = MessageFormat.format(getMessage("ImageTooLarge"),
                imageInformation.getWidth(), imageInformation.getHeight(), imageInformation.getColorChannels(),
                sizes.get("pixelsSize"), sizes.get("requiredMem"), sizes.get("availableMem"),
                (int) image.getWidth(), (int) image.getHeight(), sampledSize);

        if (bottomLabel != null) {
            bottomLabel.setText(msg);
        }

        VBox box = new VBox();
        Label label = new Label(msg);
        Hyperlink helpLink = new Hyperlink(getMessage("Help"));
        helpLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showHelp(event);
            }
        });
        box.getChildren().add(label);
        box.getChildren().add(helpLink);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(getMyStage().getTitle());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setContent(box);
        alert.setContentText(msg);

        ButtonType buttonExit = new ButtonType(AppVaribles.getMessage("Exit"));
        ButtonType buttonSplit = new ButtonType(AppVaribles.getMessage("ImageSplit"));
        ButtonType buttonSample = new ButtonType(AppVaribles.getMessage("ImageSubsample"));
        ButtonType buttonView = new ButtonType(AppVaribles.getMessage("ImageViewer"));
        ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("SaveSampledImage"));
        ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
        alert.getButtonTypes().setAll(buttonExit, buttonSample, buttonSplit, buttonView, buttonSave, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonExit) {
            closeStage();

        } else if (result.get() == buttonSplit) {
            ImageSplitController controller
                    = (ImageSplitController) reloadStage(CommonValues.ImageSplitFxml, AppVaribles.getMessage("ImageSplit"));
            controller.setSizes(sizes);
            controller.loadImage(sourceFile, image, imageInformation);

        } else if (result.get() == buttonSample) {
            ImageSampleController controller
                    = (ImageSampleController) reloadStage(CommonValues.ImageSampleFxml, AppVaribles.getMessage("ImageSubsample"));
            controller.setSizes(sizes);
            controller.loadImage(sourceFile, image, imageInformation);

        } else if (result.get() == buttonView) {
            ImageViewerController controller
                    = (ImageViewerController) reloadStage(CommonValues.ImageViewerFxml, AppVaribles.getMessage("ImageViewer"));
            controller.loadImage(sourceFile, image, imageInformation);
        } else if (result.get() == buttonSave) {
            saveAction();
        }

    }

    @Override
    protected void handleMultipleFramesImage() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.setContentText(getMessage("MultipleFramesImagesInfo"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        ButtonType buttonSure = new ButtonType(AppVaribles.getMessage("Sure"));
        ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
        alert.getButtonTypes().setAll(buttonSure, buttonCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonSure) {
            String format = FileTools.getFileSuffix(sourceFile.getAbsolutePath()).toLowerCase();
            if (format.contains("gif")) {
                final ImageGifViewerController controller
                        = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml, false, true);
                controller.setBaseTitle(AppVaribles.getMessage("ImageGifViewer"));
                controller.loadImage(sourceFile.getAbsolutePath());

            } else {
                final ImageFramesViewerController controller
                        = (ImageFramesViewerController) openStage(CommonValues.ImageFramesViewerFxml, false, true);
                controller.setBaseTitle(AppVaribles.getMessage("ImageFramesViewer"));
                controller.openFile(sourceFile);
            }
        }

        if (sourceFile != null && navBox != null) {
            navBox.setDisable(false);
            checkImageNevigator();
        }
    }

    @FXML
    @Override
    public void infoAction() {
        if (imageInformation == null) {
            return;
        }
        showImageInformation(imageInformation);
    }

    @FXML
    public void statisticAction() {
        if (image == null) {
            return;
        }
        showImageStatistic(image);
    }

    @FXML
    @Override
    public void nextAction() {
        if (nextFile != null) {
            loadImage(nextFile.getAbsoluteFile(), false);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (previousFile != null) {
            loadImage(previousFile.getAbsoluteFile(), false);
        }
    }

    @FXML
    public void browseImagesAction() {
        try {
            final ImagesBrowserController controller = FxmlStage.openImagesBrowser(getClass(), null);
            if (controller != null && sourceFile != null) {
                controller.loadImages(sourceFile.getParentFile(), 9);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void viewImageAction() {
        try {
            final ImageViewerController controller = FxmlStage.openImageViewer(getClass(), null);
            if (controller != null && sourceFile != null) {
                controller.loadImage(sourceFile, false);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void popMetaData() {
        showImageMetaData(imageInformation);
    }

    @FXML
    @Override
    public void zoomIn() {
        double currentWidth = imageView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = imageView.getImage().getWidth();
        }
        imageView.setFitWidth(currentWidth + xZoomStep);
        double currentHeight = imageView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = imageView.getImage().getHeight();
        }
        imageView.setFitHeight(currentHeight + yZoomStep);
    }

    @FXML
    @Override
    public void zoomOut() {
        double currentWidth = imageView.getFitWidth();
        if (currentWidth == -1) {
            currentWidth = imageView.getImage().getWidth();
        }
        if (currentWidth <= xZoomStep) {
            return;
        }
        imageView.setFitWidth(currentWidth - xZoomStep);
        double currentHeight = imageView.getFitHeight();
        if (currentHeight == -1) {
            currentHeight = imageView.getImage().getHeight();
        }
        if (currentHeight <= yZoomStep) {
            return;
        }
        imageView.setFitHeight(currentHeight - yZoomStep);
    }

    @FXML
    @Override
    public void imageSize() {
        imageView.setFitWidth(imageView.getImage().getWidth());
        imageView.setFitHeight(imageView.getImage().getHeight());

    }

    @FXML
    @Override
    public void paneSize() {
        imageView.setFitWidth(scrollPane.getWidth() - 1);
        imageView.setFitHeight(scrollPane.getHeight() - 5);
    }

    public void fitSize() {
        if (imageView.getImage() == null) {
            return;
        }
        if (scrollPane.getHeight() < imageView.getImage().getHeight()
                || scrollPane.getWidth() < imageView.getImage().getWidth()) {
            paneSize();
        } else {
            imageSize();
        }
    }

    @FXML
    public void moveRight() {
        FxmlTools.setScrollPane(scrollPane, -40, scrollPane.getVvalue());
    }

    @FXML
    public void moveLeft() {
        FxmlTools.setScrollPane(scrollPane, 40, scrollPane.getVvalue());
    }

    @FXML
    public void moveUp() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), 40);
    }

    @FXML
    public void moveDown() {
        FxmlTools.setScrollPane(scrollPane, scrollPane.getHvalue(), -40);
    }

    @FXML
    public void rotateRight() {
        currentAngle = (currentAngle + rotateAngle) % 360;
        imageView.setRotate(currentAngle);
    }

    @FXML
    public void rotateLeft() {
        currentAngle = (360 - rotateAngle + currentAngle) % 360;
        imageView.setRotate(currentAngle);
    }

    @FXML
    public void turnOver() {
        currentAngle = (180 + currentAngle) % 360;
        imageView.setRotate(currentAngle);
    }

    @FXML
    public void straighten() {
        currentAngle = 0;
        imageView.setRotate(currentAngle);
    }

    @FXML
    @Override
    public void selectAllAction() {
        if (image == null) {
            return;
        }
        cropLeftX = 0;
        cropLeftY = 0;
        cropRightX = (int) image.getWidth() - 1;
        cropRightY = (int) image.getHeight() - 1;
        imageView.setImage(image);
        bottomLabel.setText(AppVaribles.getMessage("SelectedSize") + ": "
                + (cropRightX - cropLeftX + 1) + "x" + (cropRightY - cropLeftY + 1));
    }

    @FXML
    public void clickImage(MouseEvent event) {
        if (image == null
                || (cropCheck != null && !cropCheck.isSelected())) {
            imageView.setCursor(Cursor.OPEN_HAND);
            return;
        }

        imageView.setCursor(Cursor.HAND);

        int x = (int) Math.round(event.getX() * image.getWidth() / imageView.getBoundsInLocal().getWidth());
        int y = (int) Math.round(event.getY() * image.getHeight() / imageView.getBoundsInLocal().getHeight());

        if (event.getButton() == MouseButton.PRIMARY) {
            cropLeftX = x;
            cropLeftY = y;

        } else if (event.getButton() == MouseButton.SECONDARY) {
            cropRightX = x;
            cropRightY = y;
        }

        if (cropLeftX < cropRightX && cropLeftY < cropRightY) {
            indicateSelection();
            bottomLabel.setText(getMessage("SelectedSize") + ": "
                    + (cropRightX - cropLeftX + 1) + "x" + (cropRightY - cropLeftY + 1));
        }

    }

    private void indicateSelection() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    int lineWidth = 1;
                    if (image.getWidth() >= 200) {
                        lineWidth = (int) image.getWidth() / 200;
                    }
                    final Image newImage = FxmlScopeTools.indicateRectangle(image,
                            Color.RED, lineWidth,
                            new IntRectangle(cropLeftX, cropLeftY, cropRightX, cropRightY));
                    if (task.isCancelled()) {
                        return null;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImage(newImage);
//                            infoAction(AppVaribles.getMessage("CropComments"));
                        }
                    });
                } catch (Exception e) {
                    logger.debug(e.toString());
                }
                return null;
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    protected boolean isWholeImage(Image currentImage) {
        return cropLeftX == 0
                && cropLeftY == 0
                && cropRightX == (int) currentImage.getWidth() - 1
                && cropRightY == (int) currentImage.getHeight() - 1;
    }

    @FXML
    @Override
    public void copyAction() {
        if (imageView == null) {
            return;
        }
        copyAction(imageView.getImage());
    }

    public void copyAction(Image currentImage) {
        if (currentImage == null || copyButton == null) {
            return;
        }
        Image cropImage;
        if (isWholeImage(currentImage)) {
            cropImage = currentImage;
        } else {
            cropImage = ImageTools.cropImage(currentImage, cropLeftX, cropLeftY, cropRightX, cropRightY);
        }
        ClipboardContent cc = new ClipboardContent();
        cc.putImage(cropImage);
        Clipboard.getSystemClipboard().setContent(cc);
        popInformation(getMessage("ImageSelectionInClipBoard"));
    }

    @FXML
    @Override
    public void saveAction() {
        if (image == null) {
            return;
        }
        try {
            if (imageInformation != null && imageInformation.isIsSampled()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(getMyStage().getTitle());
                alert.setContentText(getMessage("SureSaveSampled"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

                ButtonType buttonSure = new ButtonType(AppVaribles.getMessage("Sure"));
                ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
                alert.getButtonTypes().setAll(buttonSure, buttonCancel);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == buttonCancel) {
                    return;
                }
            }

            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(targetPathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(CommonValues.ImageExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            AppVaribles.setUserConfigValue(targetPathKey, file.getParent());

            task = new Task<Void>() {
                private boolean ok;

                @Override
                protected Void call() throws Exception {
                    Image savedImage = image;
                    if (cropCheck != null && cropCheck.isSelected()) {
                        if (!isWholeImage(image)) {
                            savedImage = ImageTools.cropImage(image,
                                    cropLeftX, cropLeftY, cropRightX, cropRightY);
                        }
                    }
                    String format = FileTools.getFileSuffix(file.getName());
                    final BufferedImage bufferedImage = ImageTools.getBufferedImage(savedImage);
                    if (task.isCancelled()) {
                        return null;
                    }
                    ok = bufferedImage != null
                            && ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());
                    if (task.isCancelled()) {
                        return null;
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ok) {
                                if (openSaveCheck != null && openSaveCheck.isSelected()) {
                                    openImageViewer(file.getAbsolutePath());
                                } else {
                                    popInformation(AppVaribles.getMessage("Successful"));
                                }
                            } else {
                                popInformation(AppVaribles.getMessage("Failed"));
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

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    @Override
    public void deleteAction() {
        if (deleteFile(sourceFile)) {
            sourceFile = null;
            image = null;
            imageView.setImage(null);
            if (nextFile != null) {
                nextAction();
            } else if (previousFile != null) {
                previousAction();
            } else {
                navBox.setDisable(true);
            }
        }
    }

    public boolean deleteFile(File sfile) {
        if (sfile == null) {
            return false;
        }
        if (deleteConfirmCheck != null && deleteConfirmCheck.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(getMyStage().getTitle());
            alert.setContentText(AppVaribles.getMessage("SureDelete"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVaribles.getMessage("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonCancel) {
                return false;
            }
        }
        if (sfile.delete()) {
            popInformation(AppVaribles.getMessage("Successful"));
            return true;
        } else {
            popError(AppVaribles.getMessage("Failed"));
            return false;
        }
    }

    @FXML
    public void renameAction() {
        try {
            File file = renameFile(sourceFile);
            if (file == null) {
                return;
            }
            sourceFile = file;
            if (imageInformation != null) {
                ImageFileInformation finfo = imageInformation.getImageFileInformation();
                if (finfo != null) {
                    finfo.setFile(file);
                    finfo.setFileName(file.getAbsolutePath());
                }
                imageInformation.setFilename(file.getAbsolutePath());
            }
            if (imageInformation != null && imageInformation.isIsSampled()) {
                if (imageInformation.getIndex() > 0) {
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                            + " - " + getMessage("Image") + " " + imageInformation.getIndex()
                            + " " + getMessage("Sampled"));
                } else {
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                            + " " + getMessage("Sampled"));
                }

            } else {
                if (imageInformation != null && imageInformation.getIndex() > 0) {
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                            + " - " + getMessage("Image") + " " + imageInformation.getIndex());
                } else {
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath());
                }
                bottomLabel.setText("");
            }
            checkImageNevigator();

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public File renameFile(File sfile) {
        if (sfile == null) {
            return null;
        }
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(targetPathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(CommonValues.ImageExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return null;
            }
            AppVaribles.setUserConfigValue(targetPathKey, file.getParent());

            if (file.exists()) {
                if (!file.delete()) {
                    popError(AppVaribles.getMessage("Failed"));
                }
            }

            if (sfile.renameTo(file)) {
                popInformation(AppVaribles.getMessage("Successful"));
                return file;
            } else {
                popError(AppVaribles.getMessage("Failed"));
                return null;
            }

        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }

    }

    @FXML
    protected void manufactureAction() {
        if (image == null) {
            return;
        }
        try {
            final ImageManufactureController controller
                    = (ImageManufactureController) FxmlStage.openStage(getClass(), null,
                            CommonValues.ImageManufactureFileFxml, AppVaribles.getMessage("ImageManufacture"));
            controller.loadImage(sourceFile, image, imageInformation);
            controller.loadData(imageData);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void checkImageNevigator() {
        checkImageNevigator(sourceFile);
    }

    public void checkImageNevigator(File currentfile) {
        try {
            if (currentfile == null) {
                previousFile = null;
                previousButton.setDisable(true);
                nextFile = null;
                nextButton.setDisable(true);
                return;
            }
            File path = currentfile.getParentFile();
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
                if (sortedFiles.get(i).getAbsoluteFile().equals(currentfile.getAbsoluteFile())) {
                    if (i < sortedFiles.size() - 1) {
                        nextFile = sortedFiles.get(i + 1);
                        nextButton.setDisable(false);
                    } else {
                        nextFile = null;
                        nextButton.setDisable(true);
                    }
                    if (i > 0) {
                        previousFile = sortedFiles.get(i - 1);
                        previousButton.setDisable(false);
                    } else {
                        previousFile = null;
                        previousButton.setDisable(true);
                    }
                    return;
                }
            }
            previousFile = null;
            previousButton.setDisable(true);
            nextFile = null;
            nextButton.setDisable(true);
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public double getMouseX() {
        return mouseX;
    }

    public void setMouseX(double mouseX) {
        this.mouseX = mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public void setMouseY(double mouseY) {
        this.mouseY = mouseY;
    }

    public Button getImageSizeButton() {
        return imageSizeButton;
    }

    public void setImageSizeButton(Button imageSizeButton) {
        this.imageSizeButton = imageSizeButton;
    }

    public Button getPaneSizeButton() {
        return paneSizeButton;
    }

    public void setPaneSizeButton(Button paneSizeButton) {
        this.paneSizeButton = paneSizeButton;
    }

    public Button getZoomInButton() {
        return zoomInButton;
    }

    public void setZoomInButton(Button zoomInButton) {
        this.zoomInButton = zoomInButton;
    }

    public Button getZoomOutButton() {
        return zoomOutButton;
    }

    public void setZoomOutButton(Button zoomOutButton) {
        this.zoomOutButton = zoomOutButton;
    }

    public int getxZoomStep() {
        return xZoomStep;
    }

    public void setxZoomStep(int xZoomStep) {
        this.xZoomStep = xZoomStep;
    }

    public int getyZoomStep() {
        return yZoomStep;
    }

    public void setyZoomStep(int yZoomStep) {
        this.yZoomStep = yZoomStep;
    }

}
