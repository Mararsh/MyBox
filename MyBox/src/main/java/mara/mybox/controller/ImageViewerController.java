package mara.mybox.controller;

import mara.mybox.controller.base.ImageManufactureController;
import com.sun.javafx.charts.Legend;
import mara.mybox.fxml.FxmlStage;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.controller.base.ImageMaskBaseController;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.fxml.ImageManufacture;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.data.ImageFileInformation;
import mara.mybox.data.IntStatistic;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.IntStatisticColorCell;
import mara.mybox.image.ImageStatistic;
import mara.mybox.image.file.ImageFileReaders;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools.FileSortMode;
import static mara.mybox.value.AppVaribles.logger;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends ImageMaskBaseController {

    final protected String ImageConfirmDeleteKey, OpenAfterSaveKey, ModifyImageKey,
            ImageDataGreyShowKey,
            ImageDataRedShowKey, ImageDataGreenShowKey, ImageDataBlueShowKey, ImageDataAlphaShowKey,
            ImageDataHueShowKey, ImageDataBrightnessShowKey, ImageDataSaturationShowKey;
    protected String ImageSelectKey, ImageDataShowKey, ImageLoadWidthKey;
    protected int xZoomStep = 50, yZoomStep = 50;
    protected int currentAngle = 0, rotateAngle = 90;
    protected File nextFile, previousFile;

    protected ObservableList<IntStatistic> statisticList = FXCollections.observableArrayList();
    protected Map<String, Color> colorTable;
    protected FileSortMode sortMode;

    @FXML
    protected HBox operation1Box, operation2Box, operation3Box, navBox;
    @FXML
    protected VBox contentBox, imageBox;
    @FXML
    protected Button upButton, downButton, renameButton, metaButton, manufactureButton, statisticButton,
            saveAsButton, cropButton, recoverButton, saveSelectedButton, splitButton, sampleButton;
    @FXML
    protected SplitPane splitPane;
    @FXML
    protected CheckBox selectCheck, deleteConfirmCheck, dataCheck,
            greyHistCheck, redHistCheck, greenHistCheck, blueHistCheck,
            alphaHistCheck, hueHistCheck, saturationHistCheck, brightnessHistCheck,
            openSaveCheck;
    @FXML
    protected SplitPane dataPane;
    @FXML
    protected TableView<IntStatistic> dataTable;
    @FXML
    protected TableColumn<IntStatistic, String> colorColumn;
    @FXML
    protected TableColumn<IntStatistic, Integer> meanColumn, varianceColumn,
            skewnessColumn, maximumColumn, minimumColumn, modeColumn, medianColumn;
    @FXML
    protected BarChart histogramChart;
    @FXML
    protected ToolBar actionsBar;
    @FXML
    protected ComboBox<String> loadWidthBox, sortBox;

    public ImageViewerController() {
        baseTitle = AppVaribles.getMessage("ImageViewer");

        ImageConfirmDeleteKey = "ImageConfirmDeleteKey";
        OpenAfterSaveKey = "ImageOpenAfterSaveKey";
        ModifyImageKey = "ModifyImageKey";
        TipsLabelKey = "ImageViewerTips";
        ImageDataShowKey = "ImageDataShowKey";
        ImageDataGreyShowKey = "ImageDataGreyShowKey";
        ImageDataRedShowKey = "ImageDataRedShowKey";
        ImageDataGreenShowKey = "ImageDataGreenShowKey";
        ImageDataBlueShowKey = "ImageDataBlueShowKey";
        ImageDataAlphaShowKey = "ImageDataAlphaShowKey";
        ImageDataHueShowKey = "ImageDataHueShowKey";
        ImageDataBrightnessShowKey = "ImageDataBrightnessShowKey";
        ImageDataSaturationShowKey = "ImageDataSaturationShowKey";
        ImageLoadWidthKey = "ImageViewerLoadWidthKey";
        ImageSelectKey = "ImageSelectKey";

    }

    @Override
    public void initializeNext() {
        try {
            initOperation1Box();
            initOperation2Box();
            initOperation3Box();
            initImageView();
            initMaskPane();
            initDataPane();
            initNavBar();
            initializeNext2();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void keyEventsHandler(KeyEvent event) {
        super.keyEventsHandler(event);
        if (event.isControlDown()) {
            String key = event.getText();
            if (key == null || key.isEmpty()) {
                return;
            }
            switch (key) {
                case "r":
                case "R":
                    if (recoverButton != null) {
                        recoverAction();
                    }
                    break;
                case "x":
                case "X":
                    if (cropButton != null) {
                        cropAction();
                    }
                    break;
                case "t":
                case "T":
                    if (selectCheck != null) {
                        selectCheck.setSelected(!selectCheck.isSelected());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    protected void initOperation1Box() {
        if (operation1Box != null) {
            operation1Box.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );
        }

        if (selectCheck != null) {
            selectCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue(ImageSelectKey, selectCheck.isSelected());
                    checkSelect();
                }
            });
            selectCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageSelectKey, false));
            checkSelect();
            Tooltip tips = new Tooltip("CTRL+t");
            FxmlControl.quickTooltip(selectCheck, tips);
        }

        if (deleteConfirmCheck != null) {
            deleteConfirmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue(ImageConfirmDeleteKey, deleteConfirmCheck.isSelected());
                }
            });
            deleteConfirmCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageConfirmDeleteKey, true));
        }

        if (rulerXCheck != null) {
            rulerXCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue(ImageRulerXKey, rulerXCheck.isSelected());
                    drawMaskRulerX();
                }
            });
            rulerXCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageRulerXKey, false));
        }
        if (rulerYCheck != null) {
            rulerYCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue(ImageRulerYKey, rulerYCheck.isSelected());
                    drawMaskRulerY();
                }
            });
            rulerYCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageRulerYKey, false));
        }

        if (coordinateCheck != null) {
            coordinateCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    checkCoordinate();
                }
            });
            coordinateCheck.setSelected(AppVaribles.getUserConfigBoolean(ImagePopCooridnateKey, false));
            xyText.setVisible(coordinateCheck.isSelected());
        }

        if (recoverButton != null) {
            Tooltip tips = new Tooltip("CTRL+r");
            FxmlControl.quickTooltip(recoverButton, tips);
        }

        if (cropButton != null) {
            Tooltip tips = new Tooltip("CTRL+x");
            FxmlControl.quickTooltip(cropButton, tips);
        }

    }

    protected void checkCoordinate() {
        AppVaribles.setUserConfigValue(ImagePopCooridnateKey, coordinateCheck.isSelected());
        xyText.setVisible(coordinateCheck.isSelected());
    }

    protected void checkSelect() {
        if (cropButton != null) {
            cropButton.setDisable(!selectCheck.isSelected());
        }
        if (selectAllButton != null) {
            selectAllButton.setDisable(!selectCheck.isSelected());
        }

        initMaskRectangleLine(selectCheck.isSelected());
        updateLabelTitle();
    }

    protected void initOperation2Box() {

        if (operation2Box != null) {
            operation2Box.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );
        }

        loadWidth = defaultLoadWidth;
        if (loadWidthBox != null) {
            List<String> values = Arrays.asList(AppVaribles.getMessage("OrignalSize"),
                    "512", "1024", "256", "128", "2048", "100", "80", "4096");
            loadWidthBox.getItems().addAll(values);
            loadWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
//                    logger.debug(oldValue + " " + newValue + " " + (String) loadWidthBox.getSelectionModel().getSelectedItem());
                    if (AppVaribles.getMessage("OrignalSize").equals(newValue)) {
                        loadWidth = -1;
                    } else {
                        try {
                            loadWidth = Integer.valueOf(newValue);
                            FxmlControl.setEditorNormal(loadWidthBox);
                        } catch (Exception e) {
                            FxmlControl.setEditorBadStyle(loadWidthBox);
                            return;
                        }
                    }
                    if (isSettingValues) {
                        return;
                    }
                    AppVaribles.setUserConfigInt(ImageLoadWidthKey, loadWidth);
                    if (!isSettingValues) {
                        setLoadWidth();
                    }
                }
            });

            isSettingValues = true;
            int v = AppVaribles.getUserConfigInt(ImageLoadWidthKey, defaultLoadWidth);
            if (v <= 0) {
                loadWidthBox.getSelectionModel().select(0);
            } else {
                loadWidthBox.getSelectionModel().select(v + "");
            }
            isSettingValues = false;
            FxmlControl.quickTooltip(loadWidthBox, new Tooltip(AppVaribles.getMessage("ImageLoadWidthCommnets")));
        }

    }

    protected void setLoadWidth() {
        careFrames = false;
        if (sourceFile != null) {
            loadImage(sourceFile, loadWidth);
        } else if (imageView.getImage() != null) {
            loadImage(imageView.getImage(), loadWidth);
        } else if (image != null) {
            loadImage(image, loadWidth);
        }
        if (imageInformation != null) {
            setImageChanged(imageInformation.isIsScaled());
        } else {
            setImageChanged(false);
        }
    }

    protected void initOperation3Box() {

        if (navBox != null) {
            navBox.setDisable(true);
        }

        if (manufactureButton != null) {
            manufactureButton.setDisable(true);
        }

        if (actionsBar != null) {
            actionsBar.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );
        }

    }

    protected void initImageView() {
        if (imageView == null) {
            return;
        }

        imageView.fitWidthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                refinePane();
            }
        });
        imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                refinePane();
            }
        });
        scrollPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                refinePane();
            }
        });
    }

    public void refinePane() {
        if (imageView.getImage() == null) {
            return;
        }
        FxmlControl.moveXCenter(scrollPane, imageView);
        scrollPane.setVvalue(scrollPane.getVmin());
        drawMaskControls();
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
                    AppVaribles.setUserConfigValue(ImageDataShowKey, new_val);
                }
            });
            isSettingValues = true;
            dataCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageDataShowKey, false));
            greyHistCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageDataGreyShowKey, false));
            redHistCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageDataRedShowKey, false));
            greenHistCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageDataGreenShowKey, false));
            blueHistCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageDataBlueShowKey, false));
            hueHistCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageDataHueShowKey, false));
            brightnessHistCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageDataBrightnessShowKey, false));
            saturationHistCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageDataSaturationShowKey, false));
            alphaHistCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageDataAlphaShowKey, false));
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
                    return new IntStatisticColorCell();
                }
            });
            varianceColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, Integer>("variance"));
            skewnessColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, Integer>("skewness"));
            medianColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, Integer>("median"));
            medianColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new IntStatisticColorCell();
                }
            });
            modeColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, Integer>("mode"));
            modeColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new IntStatisticColorCell();
                }
            });
            maximumColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, Integer>("maximum"));
            maximumColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new IntStatisticColorCell();
                }
            });
            minimumColumn.setCellValueFactory(new PropertyValueFactory<IntStatistic, Integer>("minimum"));
            minimumColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new IntStatisticColorCell();
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
            colorTable.put(AppVaribles.getMessage("Grey"), Color.GREY);

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
                if (handleLoadedSize || imageInformation == null) {
                    imageData = ImageStatistic.analyze(SwingFXUtils.fromFXImage(imageView.getImage(), null));
                } else {
                    imageData = ImageStatistic.analyze(SwingFXUtils.fromFXImage(imageInformation.getImage(), null));
                }

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        showData();
                    }
                });
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
        histogramChart.getData().clear();
        List<IntStatistic> statistic = (List<IntStatistic>) imageData.get("statistic");
        statisticList.addAll(statistic);
        showHistogram(AppVaribles.getMessage("Grey"), greyHistCheck.isSelected());
        showHistogram(AppVaribles.getMessage("Red"), redHistCheck.isSelected());
        showHistogram(AppVaribles.getMessage("Green"), greenHistCheck.isSelected());
        showHistogram(AppVaribles.getMessage("Blue"), blueHistCheck.isSelected());
        showHistogram(AppVaribles.getMessage("Hue"), hueHistCheck.isSelected());
        showHistogram(AppVaribles.getMessage("Brightness"), brightnessHistCheck.isSelected());
        showHistogram(AppVaribles.getMessage("Saturation"), saturationHistCheck.isSelected());
        showHistogram(AppVaribles.getMessage("Alpha"), alphaHistCheck.isSelected());
        adjustSplitPane();
    }

    protected void showHistogram(String colorName, boolean isShow) {
        if (isShow) {
            showHistogram(colorName);
        } else {
            hideHistogram(colorName);
        }
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
        if (AppVaribles.getMessage("Red").equals(colorName)) {
            histogram = (int[]) imageData.get("redHistogram");
        } else if (AppVaribles.getMessage("Green").equals(colorName)) {
            histogram = (int[]) imageData.get("greenHistogram");
        } else if (AppVaribles.getMessage("Blue").equals(colorName)) {
            histogram = (int[]) imageData.get("blueHistogram");
        } else if (AppVaribles.getMessage("Alpha").equals(colorName)) {
            histogram = (int[]) imageData.get("alphaHistogram");
        } else if (AppVaribles.getMessage("Hue").equals(colorName)) {
            histogram = (int[]) imageData.get("hueHistogram");
        } else if (AppVaribles.getMessage("Brightness").equals(colorName)) {
            histogram = (int[]) imageData.get("brightnessHistogram");
        } else if (AppVaribles.getMessage("Saturation").equals(colorName)) {
            histogram = (int[]) imageData.get("saturationHistogram");
        } else {
            histogram = (int[]) imageData.get("greyHistogram");
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

    @FXML
    public void clearHistogram() {
        if (histogramChart != null) {
            greyHistCheck.setSelected(false);
            redHistCheck.setSelected(false);
            greenHistCheck.setSelected(false);
            blueHistCheck.setSelected(false);
            alphaHistCheck.setSelected(false);
            hueHistCheck.setSelected(false);
            saturationHistCheck.setSelected(false);
            brightnessHistCheck.setSelected(false);
            histogramChart.getData().clear();
        }
    }

    @FXML
    protected void colorChecked(ActionEvent event) {
        if (isSettingValues) {
            return;
        }
        CheckBox checked = (CheckBox) event.getSource();
        String name = checked.getText();
        boolean v = checked.isSelected();
        showHistogram(name, v);
        if (AppVaribles.getMessage("Red").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataRedShowKey, v);
        } else if (AppVaribles.getMessage("Green").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataGreenShowKey, v);
        } else if (AppVaribles.getMessage("Blue").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataBlueShowKey, v);
        } else if (AppVaribles.getMessage("Alpha").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataAlphaShowKey, v);
        } else if (AppVaribles.getMessage("Hue").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataHueShowKey, v);
        } else if (AppVaribles.getMessage("Brightness").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataBrightnessShowKey, v);
        } else if (AppVaribles.getMessage("Saturation").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataSaturationShowKey, v);
        } else {
            AppVaribles.setUserConfigValue(ImageDataGreyShowKey, v);
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

    protected void initNavBar() {
        if (sortBox != null) {
            List<String> svalues = Arrays.asList(AppVaribles.getMessage("ModifyTimeDesc"),
                    AppVaribles.getMessage("ModifyTimeAsc"),
                    AppVaribles.getMessage("SizeDesc"),
                    AppVaribles.getMessage("SizeAsc"),
                    AppVaribles.getMessage("NameDesc"),
                    AppVaribles.getMessage("NameAsc"),
                    AppVaribles.getMessage("FormatDesc"),
                    AppVaribles.getMessage("FormatAsc"),
                    AppVaribles.getMessage("CreateTimeDesc"),
                    AppVaribles.getMessage("CreateTimeAsc")
            );
            sortBox.getItems().addAll(svalues);
            sortBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (AppVaribles.getMessage("ModifyTimeDesc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.ModifyTimeDesc;
                    } else if (AppVaribles.getMessage("ModifyTimeAsc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.ModifyTimeAsc;
                    } else if (AppVaribles.getMessage("SizeDesc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.SizeDesc;
                    } else if (AppVaribles.getMessage("SizeAsc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.SizeAsc;
                    } else if (AppVaribles.getMessage("NameDesc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.NameDesc;
                    } else if (AppVaribles.getMessage("NameAsc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.NameAsc;
                    } else if (AppVaribles.getMessage("FormatDesc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.FormatDesc;
                    } else if (AppVaribles.getMessage("FormatAsc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.FormatAsc;
                    } else if (AppVaribles.getMessage("CreateTimeDesc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.CreateTimeDesc;
                    } else if (AppVaribles.getMessage("CreateTimeAsc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.CreateTimeAsc;
                    } else {
                        sortMode = FileTools.FileSortMode.ModifyTimeDesc;
                    }
                    if (!isSettingValues) {
                        makeImageNevigator();
                    }
                }
            });
//            sortBox.getSelectionModel().select(0);
        }

    }

    @Override
    public void sourceFileChanged(final File file) {
        if (file == null) {
            return;
        }
        super.sourceFileChanged(file);
        careFrames = true;
        loadImage(file, loadWidth);
    }

    @Override
    public void afterInfoLoaded() {
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
            super.afterImageLoaded();

            afterInfoLoaded();
            if (image == null) {
                return;
            }
            imageView.setPreserveRatio(true);

            imageView.setImage(image);
            imageChanged = isCroppped = false;
            xZoomStep = (int) image.getWidth() / 10;
            yZoomStep = (int) image.getHeight() / 10;
            careFrames = true;

            if (sourceFile != null && navBox != null) {
                makeImageNevigator();
            }
            fitSize();

            setMaskStroke();

            if (selectCheck != null) {
                checkSelect();
            }

            if (imageInformation == null) {
                setImageChanged(true);
            } else if (!imageInformation.isIsSampled()) {
                setImageChanged(imageInformation.isIsScaled());
            }

            if (imageInformation != null && imageInformation.isIsSampled()) {
                if (sampledTips != null) {
                    sampledTips.setVisible(true);
                    FxmlControl.quickTooltip(sampledTips, new Tooltip(getSmapledInfo()));
                }
                loadWidth = (int) image.getWidth();
                loadSampledImage();
            } else {
                if (sampledTips != null) {
                    sampledTips.setVisible(false);
                }
            }

            if (loadWidthBox != null) {
                isSettingValues = true;
                if (loadWidth == -1) {
                    loadWidthBox.getSelectionModel().select(AppVaribles.getMessage("OrignalSize"));
                } else {
                    loadWidthBox.getSelectionModel().select(loadWidth + "");
                }
                isSettingValues = false;
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

    protected String getSmapledInfo() {
        Map<String, Long> sizes = imageInformation.getSizes();
        if (sizes == null) {
            return "";
        }
        int sampledSize = (int) (image.getWidth() * image.getHeight() * imageInformation.getColorChannels() / (1014 * 1024));
        String msg = MessageFormat.format(getMessage("ImageTooLarge"),
                imageInformation.getWidth(), imageInformation.getHeight(), imageInformation.getColorChannels(),
                sizes.get("pixelsSize"), sizes.get("requiredMem"), sizes.get("availableMem"),
                (int) image.getWidth(), (int) image.getHeight(), sampledSize);
        return msg;
    }

    protected void loadSampledImage() {
        if (sampledTips != null) {
            sampledTips.setOnMouseMoved(null);
            sampledTips.setOnMouseMoved(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    popSampleInformation(getSmapledInfo());
                }
            });
        }

        popSampleInformation(getSmapledInfo());

    }

    protected void popSampleInformation(String msg) {
        if (imageInformation == null || !imageInformation.isIsSampled()
                || msg == null || msg.isEmpty()) {
            return;
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setContent(box);
        alert.setContentText(msg);

        ButtonType buttonClose = new ButtonType(AppVaribles.getMessage("Close"));
        ButtonType buttonSplit = new ButtonType(AppVaribles.getMessage("ImageSplit"));
        ButtonType buttonSample = new ButtonType(AppVaribles.getMessage("ImageSubsample"));
        ButtonType buttonView = new ButtonType(AppVaribles.getMessage("ImageViewer"));
        ButtonType buttonSave = new ButtonType(AppVaribles.getMessage("SaveSampledImage"));
        ButtonType buttonCancel = new ButtonType(AppVaribles.getMessage("Cancel"));
        switch (myFxml) {
            case CommonValues.ImageViewerFxml:
                alert.getButtonTypes().setAll(buttonClose, buttonSample, buttonSplit, buttonSave, buttonCancel);
                break;
            case CommonValues.ImageSplitFxml:
                alert.getButtonTypes().setAll(buttonClose, buttonSample, buttonView, buttonSave, buttonCancel);
                break;
            case CommonValues.ImageSampleFxml:
                alert.getButtonTypes().setAll(buttonClose, buttonSplit, buttonView, buttonSave, buttonCancel);
                break;
            default:
                alert.getButtonTypes().setAll(buttonClose, buttonSample, buttonSplit, buttonView, buttonSave, buttonCancel);
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonClose) {
            closeStage();

        } else if (result.get() == buttonSplit) {
            splitAction();

        } else if (result.get() == buttonSample) {
            sampleAction();

        } else if (result.get() == buttonView) {
            if (!CommonValues.ImageViewerFxml.equals(myFxml)) {
                ImageViewerController controller
                        = (ImageViewerController) loadScene(CommonValues.ImageViewerFxml);
                controller.loadImage(sourceFile, image, imageInformation);
            }

        } else if (result.get() == buttonSave) {
            saveAction();
        }

    }

    @Override
    public void loadMultipleFramesImage() {
        String format = FileTools.getFileSuffix(sourceFile.getAbsolutePath()).toLowerCase();
        if (format.contains("gif")) {
            final ImageGifViewerController controller
                    = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml);
            controller.loadImage(sourceFile.getAbsolutePath());

        } else {
            final ImageFramesViewerController controller
                    = (ImageFramesViewerController) openStage(CommonValues.ImageFramesViewerFxml);
            controller.selectSourceFile(sourceFile);
        }

    }

    @Override
    public void setImageChanged(boolean imageChanged) {
        this.imageChanged = imageChanged;

        if (saveButton != null && !saveButton.disableProperty().isBound()) {

            if (imageInformation != null
                    && imageInformation.getImageFileInformation().getNumberOfImages() > 1) {
                saveButton.setDisable(true);
            } else {
                saveButton.setDisable(!imageChanged);
            }
        }

        if (imageChanged) {
            resetMaskControls();
        }

        updateLabelTitle();
    }

    @Override
    public void updateLabelTitle() {
        if (imageView.getImage() == null) {
            return;
        }
        if (bottomLabel != null) {
            String bottom = "";
            if (imageInformation != null) {
                bottom += AppVaribles.getMessage("Format") + ":" + imageInformation.getImageFormat() + "  ";
                bottom += AppVaribles.getMessage("Pixels") + ":" + imageInformation.getWidth() + "x" + imageInformation.getHeight() + "  ";
            }
            bottom += AppVaribles.getMessage("LoadedSize") + ":"
                    + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight() + "  "
                    + AppVaribles.getMessage("DisplayedSize") + ":"
                    + (int) imageView.getFitWidth() + "x" + (int) imageView.getFitHeight();

            if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
                bottom += "  " + getMessage("SelectedSize") + ": "
                        + (int) maskRectangleData.getWidth() + "x" + (int) maskRectangleData.getHeight();
            }
            if (sourceFile != null) {
                bottom += "  " + AppVaribles.getMessage("FileSize") + ":" + FileTools.showFileSize(sourceFile.length()) + "  "
                        + AppVaribles.getMessage("ModifyTime") + ":" + DateTools.datetimeToString(sourceFile.lastModified()) + "  ";
            }

            bottomLabel.setText(bottom);
        }
        String title = getBaseTitle();
        if (sourceFile != null) {
            title += " " + sourceFile.getAbsolutePath();
            if (imageInformation != null) {
                if (imageInformation.getImageFileInformation().getNumberOfImages() > 1) {
                    title += " - " + getMessage("Image") + " " + imageInformation.getIndex();
                }
                if (imageInformation.isIsSampled()) {
                    title += " - " + getMessage("Sampled");
                }
            }
        }
        if (imageChanged) {
            title += "  " + "*";
        }

        getMyStage().setTitle(title);

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
            careFrames = false;
            loadImage(nextFile.getAbsoluteFile(), loadWidth);
        }
    }

    @FXML
    @Override
    public void previousAction() {
        if (previousFile != null) {
            careFrames = false;
            loadImage(previousFile.getAbsoluteFile(), loadWidth);
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
                controller.loadImage(sourceFile);
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
        FxmlControl.zoomIn(scrollPane, imageView, xZoomStep, yZoomStep);
    }

    @FXML
    @Override
    public void zoomOut() {
        FxmlControl.zoomOut(scrollPane, imageView, xZoomStep, yZoomStep);
    }

    @FXML
    @Override
    public void loadedSize() {
        FxmlControl.imageSize(scrollPane, imageView);

    }

    @FXML
    @Override
    public void paneSize() {
        FxmlControl.paneSize(scrollPane, imageView);
    }

    public void fitSize() {
        try {
            if (scrollPane == null || imageView == null || imageView.getImage() == null) {
                return;
            }
            if (scrollPane.getHeight() < getImageHeight()
                    || scrollPane.getWidth() < getImageWidth()) {
                paneSize();
            } else {
                loadedSize();
            }
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    public void moveRight() {
        FxmlControl.setScrollPane(scrollPane, -40, scrollPane.getVvalue());
    }

    @FXML
    public void moveLeft() {
        FxmlControl.setScrollPane(scrollPane, 40, scrollPane.getVvalue());
    }

    @FXML
    public void moveUp() {
        FxmlControl.setScrollPane(scrollPane, scrollPane.getHvalue(), 40);
    }

    @FXML
    public void moveDown() {
        FxmlControl.setScrollPane(scrollPane, scrollPane.getHvalue(), -40);
    }

    @FXML
    public void rotateRight() {
        rotateModify(90);
    }

    public void rotateModify(final int rotateAngle) {
        if (imageView.getImage() == null) {
            return;
        }
        task = new Task<Void>() {
            private Image newImage;

            @Override
            protected Void call() throws Exception {
                newImage = ImageManufacture.rotateImage(imageView.getImage(), rotateAngle);

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImage(newImage);
                        checkSelect();
                        setImageChanged(true);
                        refinePane();
                    }
                });
            }

        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void rotateLeft() {
        rotateModify(270);
    }

    @FXML
    public void turnOver() {
        rotateModify(180);
    }

    @FXML
    public void straighten() {
        currentAngle = 0;
        imageView.setRotate(currentAngle);
    }

    @FXML
    @Override
    public void selectAllAction() {
        if (imageView.getImage() == null
                || maskRectangleLine == null || !maskRectangleLine.isVisible()) {
            return;
        }
        maskRectangleData = new DoubleRectangle(0, 0,
                getImageWidth() - 1, getImageHeight() - 1);

        drawMaskRectangleLine();
    }

    @FXML
    public void cropAction() {
        if (imageView == null || imageView.getImage() == null) {
            return;
        }
        try {
            task = new Task<Void>() {
                private Image areaImage;

                @Override
                protected Void call() throws Exception {

                    areaImage = cropImage();
                    if (areaImage == null) {
                        areaImage = imageView.getImage();
                    }
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImage(areaImage);
                            loadData();
                            isCroppped = true;
                            setImageChanged(true);
                            resetMaskControls();
                        }
                    });
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

    protected Image cropImage() {
        Image inImage = imageView.getImage();
//        if (handleLoadedSize || imageInformation == null) {
//            inImage = imageView.getImage();
//        } else {
//            inImage = imageInformation.getImage();
//        }

        if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
            if (maskRectangleData.getSmallX() == 0
                    && maskRectangleData.getSmallY() == 0
                    && maskRectangleData.getBigX() == (int) inImage.getWidth() - 1
                    && maskRectangleData.getBigY() == (int) inImage.getHeight() - 1) {
                return null;
            }
            return ImageManufacture.cropOutsideFx(inImage, maskRectangleData, Color.WHITE);

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            return ImageManufacture.cropOutsideFx(inImage, maskCircleData, Color.WHITE);

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            return ImageManufacture.cropOutsideFx(inImage, maskEllipseData, Color.WHITE);

        } else if (maskPolygonLine != null && maskPolygonLine.isVisible()) {
            return ImageManufacture.cropOutsideFx(inImage, maskPolygonData, Color.WHITE);

        } else {
            return null;
        }

    }

    @FXML
    public void recoverAction() {
        if (imageView == null) {
            return;
        }
        boolean sizeChanged = getImageWidth() != image.getWidth()
                || getImageHeight() != image.getHeight();
        imageView.setImage(image);
        if (sizeChanged) {
            resetMaskControls();
        }
        if (isCroppped) {
            loadData();
            isCroppped = false;
        }
        setImageChanged(false);
    }

    @FXML
    @Override
    public void copyAction() {
        if (imageView == null || imageView.getImage() == null || copyButton == null) {
            return;
        }
        task = new Task<Void>() {
            private boolean ok;
            private Image areaImage;

            @Override
            protected Void call() throws Exception {

                areaImage = cropImage();
                if (areaImage == null) {
                    areaImage = imageView.getImage();
                }
                if (AppVaribles.getUserConfigBoolean("RemoveAlphaCopy", true)) {
                    areaImage = ImageManufacture.clearAlpha(areaImage);
                }
                ok = true;
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ClipboardContent cc = new ClipboardContent();
                        cc.putImage(areaImage);
                        Clipboard.getSystemClipboard().setContent(cc);
                        popInformation(AppVaribles.getMessage("ImageSelectionInClipBoard"));
                    }
                });
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    @Override
    public void saveAction() {
        if (imageView == null) {
            return;
        }
        if (sourceFile == null) {
            saveAsAction();
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

            task = new Task<Void>() {
                private boolean ok;

                @Override
                protected Void call() throws Exception {
                    String format = FileTools.getFileSuffix(sourceFile.getName());
                    final BufferedImage bufferedImage = ImageManufacture.getBufferedImage(imageView.getImage());
                    if (bufferedImage == null || task == null || task.isCancelled()) {
                        return null;
                    }
                    String filename = sourceFile.getAbsolutePath();
                    ok = ImageFileWriters.writeImageFile(bufferedImage, format, filename);
                    if (!ok || task == null || task.isCancelled()) {
                        return null;
                    }
                    imageInformation = ImageFileReaders.readImageFileMetaData(filename).getImageInformation();
                    ok = true;
                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ok) {
                                image = imageView.getImage();
                                popInformation(AppVaribles.getMessage("Saved"));
                                setImageChanged(false);
                            } else {
                                popInformation(AppVaribles.getMessage("Failed"));
                            }
                        }
                    });
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
    public void saveAsAction() {
        if (imageView == null) {
            return;
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
                return;
            }
            AppVaribles.setUserConfigValue(targetPathKey, file.getParent());

            task = new Task<Void>() {
                private boolean ok;

                @Override
                protected Void call() throws Exception {
                    Image selected = cropImage();
                    if (selected == null) {
                        selected = imageView.getImage();
                    }

                    String format = FileTools.getFileSuffix(file.getName());
                    final BufferedImage bufferedImage = ImageManufacture.getBufferedImage(selected);
                    if (task == null || task.isCancelled()) {
                        return null;
                    }
                    ok = bufferedImage != null
                            && ImageFileWriters.writeImageFile(bufferedImage, format, file.getAbsolutePath());

                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ok) {
                                popInformation(AppVaribles.getMessage("Saved"));
                                if (sourceFile == null) {
                                    loadImage(file);
                                }
                                if (openSaveCheck != null && openSaveCheck.isSelected()) {
                                    openImageViewer(file.getAbsolutePath());
                                }
                            } else {
                                popInformation(AppVaribles.getMessage("Failed"));
                            }
                        }
                    });
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
            saveAction();
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
                updateLabelTitle();
            }
            makeImageNevigator();

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
            if (imageInformation != null && imageInformation.isIsMultipleFrames()) {
                String format = FileTools.getFileSuffix(sourceFile.getAbsolutePath()).toLowerCase();
                if (format.contains("gif")) {
                    final ImageGifViewerController controller
                            = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml);
                    controller.loadImage(sourceFile.getAbsolutePath());

                } else {
                    final ImageFramesViewerController controller
                            = (ImageFramesViewerController) openStage(CommonValues.ImageFramesViewerFxml);
                    controller.selectSourceFile(sourceFile);
                }
            } else {
                final ImageManufactureController controller
                        = (ImageManufactureController) FxmlStage.openScene(getClass(), null,
                                CommonValues.ImageManufactureFileFxml);
                controller.loadImage(sourceFile, image, imageInformation);
                controller.loadData(imageData);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void sampleAction() {
        ImageSampleController controller
                = (ImageSampleController) loadScene(CommonValues.ImageSampleFxml);
        controller.loadImage(sourceFile, image, imageInformation);
    }

    @FXML
    public void splitAction() {
        ImageSplitController controller
                = (ImageSplitController) loadScene(CommonValues.ImageSplitFxml);
        controller.loadImage(sourceFile, image, imageInformation);
    }

    public void makeImageNevigator() {
        makeImageNevigator(sourceFile);
    }

    public void makeImageNevigator(File currentfile) {
        try {
            if (currentfile == null) {
                previousFile = null;
                previousButton.setDisable(true);
                nextFile = null;
                nextButton.setDisable(true);
                return;
            }
            File path = currentfile.getParentFile();
            List<File> pathFiles = new ArrayList<>();
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isFile() && FileTools.isSupportedImage(file)) {
                    pathFiles.add(file);
                }
            }
            FileTools.sortFiles(pathFiles, sortMode);

            for (int i = 0; i < pathFiles.size(); i++) {
                if (pathFiles.get(i).getAbsoluteFile().equals(currentfile.getAbsoluteFile())) {
                    if (i < pathFiles.size() - 1) {
                        nextFile = pathFiles.get(i + 1);
                        nextButton.setDisable(false);
                    } else {
                        nextFile = null;
                        nextButton.setDisable(true);
                    }
                    if (i > 0) {
                        previousFile = pathFiles.get(i - 1);
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

}
