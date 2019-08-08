package mara.mybox.controller;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;
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
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.controller.base.ImageManufactureController;
import mara.mybox.controller.base.ImageMaskController;
import mara.mybox.data.DoubleRectangle;
import mara.mybox.data.IntStatistic;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.IntStatisticColorCell;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageStatistic;
import mara.mybox.image.file.ImageFileReaders;
import mara.mybox.image.file.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FileTools.FileSortMode;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import static mara.mybox.value.AppVaribles.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageViewerController extends ImageMaskController {

    final protected String ImageConfirmDeleteKey, ImageOpenSaveKey, ModifyImageKey,
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
    public Button moveUpButton, moveDownButton, manufactureButton, statisticButton, splitButton, sampleButton, browseButton;
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
        baseTitle = AppVaribles.message("ImageViewer");

        ImageConfirmDeleteKey = "ImageConfirmDeleteKey";
        ImageOpenSaveKey = "ImageOpenSaveKey";
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

        if (manufactureButton != null) {
            FxmlControl.setTooltip(manufactureButton, new Tooltip(message("Manufacture")));
        }
        if (splitButton != null) {
            FxmlControl.setTooltip(splitButton, new Tooltip(message("Split")));
        }

        if (sampleButton != null) {
            FxmlControl.setTooltip(sampleButton, new Tooltip(message("Sample")));
        }

        if (browseButton != null) {
            FxmlControl.setTooltip(browseButton, new Tooltip(message("Browse")));
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
            FxmlControl.setTooltip(selectCheck, tips);
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

        if (openSaveCheck != null) {
            openSaveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    AppVaribles.setUserConfigValue(ImageOpenSaveKey, openSaveCheck.isSelected());
                }
            });
            openSaveCheck.setSelected(AppVaribles.getUserConfigBoolean(ImageOpenSaveKey, true));
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

    }

    protected void checkCoordinate() {
        AppVaribles.setUserConfigValue(ImagePopCooridnateKey, coordinateCheck.isSelected());
        xyText.setVisible(coordinateCheck.isSelected());
        xyLabel.setVisible(coordinateCheck.isSelected());
    }

    protected void checkSelect() {
        if (cropButton != null) {
            cropButton.setDisable(!selectCheck.isSelected());
        }
        if (selectAllButton != null) {
            selectAllButton.setDisable(!selectCheck.isSelected());
        }

        if (selectCheck != null) {
            initMaskRectangleLine(selectCheck.isSelected());
        }
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
            List<String> values = Arrays.asList(AppVaribles.message("OrignalSize"),
                    "512", "1024", "256", "128", "2048", "100", "80", "4096");
            loadWidthBox.getItems().addAll(values);
            loadWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
//                    logger.debug(oldValue + " " + newValue + " " + (String) loadWidthBox.getSelectionModel().getSelectedItem());
                    if (AppVaribles.message("OrignalSize").equals(newValue)) {
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
            FxmlControl.setTooltip(loadWidthBox, new Tooltip(AppVaribles.message("ImageLoadWidthCommnets")));
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
                if (Math.abs(new_val.intValue() - old_val.intValue()) > 20) {
                    refinePane();
                }
            }
        });
        imageView.fitHeightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                if (Math.abs(new_val.intValue() - old_val.intValue()) > 20) {
                    refinePane();
                }
            }
        });
        scrollPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                if (Math.abs(new_val.intValue() - old_val.intValue()) > 20) {
                    refinePane();
                }
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
            colorColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            colorColumn.setCellFactory(new Callback<TableColumn<IntStatistic, String>, TableCell<IntStatistic, String>>() {
                @Override
                public TableCell<IntStatistic, String> call(TableColumn<IntStatistic, String> param) {
                    return new NameCell();
                }
            });
            meanColumn.setCellValueFactory(new PropertyValueFactory<>("mean"));
            meanColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new IntStatisticColorCell();
                }
            });
            varianceColumn.setCellValueFactory(new PropertyValueFactory<>("variance"));
            skewnessColumn.setCellValueFactory(new PropertyValueFactory<>("skewness"));
            medianColumn.setCellValueFactory(new PropertyValueFactory<>("median"));
            medianColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new IntStatisticColorCell();
                }
            });
            modeColumn.setCellValueFactory(new PropertyValueFactory<>("mode"));
            modeColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new IntStatisticColorCell();
                }
            });
            maximumColumn.setCellValueFactory(new PropertyValueFactory<>("maximum"));
            maximumColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new IntStatisticColorCell();
                }
            });
            minimumColumn.setCellValueFactory(new PropertyValueFactory<>("minimum"));
            minimumColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
                @Override
                public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                    return new IntStatisticColorCell();
                }
            });

            colorTable = new HashMap<>();
            colorTable.put(AppVaribles.message("Red"), Color.RED);
            colorTable.put(AppVaribles.message("Green"), Color.GREEN);
            colorTable.put(AppVaribles.message("Blue"), Color.BLUE);
            colorTable.put(AppVaribles.message("Alpha"), Color.CORNFLOWERBLUE);
            colorTable.put(AppVaribles.message("Hue"), Color.MEDIUMVIOLETRED);
            colorTable.put(AppVaribles.message("Brightness"), Color.GOLD);
            colorTable.put(AppVaribles.message("Saturation"), Color.MEDIUMAQUAMARINE);
            colorTable.put(AppVaribles.message("Gray"), Color.GRAY);
            colorTable.put(AppVaribles.message("Grey"), Color.GREY);

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
                text.setText(AppVaribles.message(item));
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
        showHistogram(AppVaribles.message("Grey"), greyHistCheck.isSelected());
        showHistogram(AppVaribles.message("Red"), redHistCheck.isSelected());
        showHistogram(AppVaribles.message("Green"), greenHistCheck.isSelected());
        showHistogram(AppVaribles.message("Blue"), blueHistCheck.isSelected());
        showHistogram(AppVaribles.message("Hue"), hueHistCheck.isSelected());
        showHistogram(AppVaribles.message("Brightness"), brightnessHistCheck.isSelected());
        showHistogram(AppVaribles.message("Saturation"), saturationHistCheck.isSelected());
        showHistogram(AppVaribles.message("Alpha"), alphaHistCheck.isSelected());
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
        if (AppVaribles.message("Red").equals(colorName)) {
            histogram = (int[]) imageData.get("redHistogram");
        } else if (AppVaribles.message("Green").equals(colorName)) {
            histogram = (int[]) imageData.get("greenHistogram");
        } else if (AppVaribles.message("Blue").equals(colorName)) {
            histogram = (int[]) imageData.get("blueHistogram");
        } else if (AppVaribles.message("Alpha").equals(colorName)) {
            histogram = (int[]) imageData.get("alphaHistogram");
        } else if (AppVaribles.message("Hue").equals(colorName)) {
            histogram = (int[]) imageData.get("hueHistogram");
        } else if (AppVaribles.message("Brightness").equals(colorName)) {
            histogram = (int[]) imageData.get("brightnessHistogram");
        } else if (AppVaribles.message("Saturation").equals(colorName)) {
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

        List<Object> data = new ArrayList();
        data.addAll(histogramChart.getData());
        for (Object s : data) {
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
        try {
            Legend legend = (Legend) histogramChart.lookup(".chart-legend");
            List<LegendItem> legendItems = legend.getItems();
            for (LegendItem item : legendItems) {
                String colorString = FxmlColor.rgb2Hex(colorTable.get(item.getText()));
                item.getSymbol().setStyle("-fx-background-color: " + colorString);
            }
        } catch (Exception e) {

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
        if (AppVaribles.message("Red").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataRedShowKey, v);
        } else if (AppVaribles.message("Green").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataGreenShowKey, v);
        } else if (AppVaribles.message("Blue").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataBlueShowKey, v);
        } else if (AppVaribles.message("Alpha").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataAlphaShowKey, v);
        } else if (AppVaribles.message("Hue").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataHueShowKey, v);
        } else if (AppVaribles.message("Brightness").equals(name)) {
            AppVaribles.setUserConfigValue(ImageDataBrightnessShowKey, v);
        } else if (AppVaribles.message("Saturation").equals(name)) {
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
            List<String> svalues = Arrays.asList(AppVaribles.message("ModifyTimeDesc"),
                    AppVaribles.message("ModifyTimeAsc"),
                    AppVaribles.message("SizeDesc"),
                    AppVaribles.message("SizeAsc"),
                    AppVaribles.message("NameDesc"),
                    AppVaribles.message("NameAsc"),
                    AppVaribles.message("FormatDesc"),
                    AppVaribles.message("FormatAsc"),
                    AppVaribles.message("CreateTimeDesc"),
                    AppVaribles.message("CreateTimeAsc")
            );
            sortBox.getItems().addAll(svalues);
            sortBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (AppVaribles.message("ModifyTimeDesc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.ModifyTimeDesc;
                    } else if (AppVaribles.message("ModifyTimeAsc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.ModifyTimeAsc;
                    } else if (AppVaribles.message("SizeDesc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.SizeDesc;
                    } else if (AppVaribles.message("SizeAsc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.SizeAsc;
                    } else if (AppVaribles.message("NameDesc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.NameDesc;
                    } else if (AppVaribles.message("NameAsc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.NameAsc;
                    } else if (AppVaribles.message("FormatDesc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.FormatDesc;
                    } else if (AppVaribles.message("FormatAsc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.FormatAsc;
                    } else if (AppVaribles.message("CreateTimeDesc").equals(newValue)) {
                        sortMode = FileTools.FileSortMode.CreateTimeDesc;
                    } else if (AppVaribles.message("CreateTimeAsc").equals(newValue)) {
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
                    FxmlControl.setTooltip(sampledTips, new Tooltip(getSmapledInfo()));
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
                    loadWidthBox.getSelectionModel().select(AppVaribles.message("OrignalSize"));
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
            alertInformation(AppVaribles.message("NotSupported"));
        }
    }

    protected String getSmapledInfo() {
        Map<String, Long> sizes = imageInformation.getSizes();
        if (sizes == null) {
            return "";
        }
        int sampledSize = (int) (image.getWidth() * image.getHeight() * imageInformation.getColorChannels() / (1014 * 1024));
        String msg = MessageFormat.format(message("ImageTooLarge"),
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
//        Hyperlink helpLink = new Hyperlink(message("Help"));
//        helpLink.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                developerGuide(event);
//            }
//        });
        box.getChildren().add(label);
//        box.getChildren().add(helpLink);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(getMyStage().getTitle());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setContent(box);
        alert.setContentText(msg);

        ButtonType buttonClose = new ButtonType(AppVaribles.message("Close"));
        ButtonType buttonSplit = new ButtonType(AppVaribles.message("ImageSplit"));
        ButtonType buttonSample = new ButtonType(AppVaribles.message("ImageSubsample"));
        ButtonType buttonView = new ButtonType(AppVaribles.message("ImageViewer"));
        ButtonType buttonSave = new ButtonType(AppVaribles.message("SaveSampledImage"));
        ButtonType buttonCancel = new ButtonType(AppVaribles.message("Cancel"));
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
    public void loadMultipleFramesImage(File file) {
        String format = FileTools.getFileSuffix(file.getAbsolutePath()).toLowerCase();
        if (format.contains("gif")) {
            final ImageGifViewerController controller
                    = (ImageGifViewerController) openStage(CommonValues.ImageGifViewerFxml);
            controller.loadImage(file.getAbsolutePath());

        } else {
            final ImageFramesViewerController controller
                    = (ImageFramesViewerController) openStage(CommonValues.ImageFramesViewerFxml);
            controller.selectSourceFile(file);
        }

    }

    @Override
    public void setImageChanged(boolean imageChanged) {
        try {
            this.imageChanged = imageChanged;
            updateLabelTitle();

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

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void updateLabelTitle() {
        try {
            if (sourceFile != null) {
                String title = getBaseTitle() + " " + sourceFile.getAbsolutePath();
                if (imageInformation != null) {
                    if (imageInformation.getImageFileInformation().getNumberOfImages() > 1) {
                        title += " - " + message("Image") + " " + imageInformation.getIndex();
                    }
                    if (imageInformation.isIsSampled()) {
                        title += " - " + message("Sampled");
                    }
                }
                if (imageChanged) {
                    title += "  " + "*";
                }
                getMyStage().setTitle(title);
            }

            if (imageView != null && imageView.getImage() != null) {
                if (bottomLabel != null) {
                    String bottom = "";
                    if (imageInformation != null) {
                        bottom += AppVaribles.message("Format") + ":" + imageInformation.getImageFormat() + "  ";
                        bottom += AppVaribles.message("Pixels") + ":" + imageInformation.getWidth() + "x" + imageInformation.getHeight() + "  ";
                    }
                    bottom += AppVaribles.message("LoadedSize") + ":"
                            + (int) imageView.getImage().getWidth() + "x" + (int) imageView.getImage().getHeight() + "  "
                            + AppVaribles.message("DisplayedSize") + ":"
                            + (int) imageView.getFitWidth() + "x" + (int) imageView.getFitHeight();

                    if (maskRectangleLine != null && maskRectangleLine.isVisible()) {
                        bottom += "  " + message("SelectedSize") + ": "
                                + (int) maskRectangleData.getWidth() + "x" + (int) maskRectangleData.getHeight();
                    }
                    if (sourceFile != null) {
                        bottom += "  " + AppVaribles.message("FileSize") + ":" + FileTools.showFileSize(sourceFile.length()) + "  "
                                + AppVaribles.message("ModifyTime") + ":" + DateTools.datetimeToString(sourceFile.lastModified()) + "  ";
                    }
                    bottomLabel.setText(bottom);
                }
            }

        } catch (Exception e) {
            logger.debug(e.toString());
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
    public void browseAction() {
        try {
            final ImagesBrowserController controller = FxmlStage.openImagesBrowser(null);
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
            final ImageViewerController controller = FxmlStage.openImageViewer(null);
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
                newImage = FxmlImageManufacture.rotateImage(imageView.getImage(), rotateAngle);

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
    @Override
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
            return FxmlImageManufacture.cropOutsideFx(inImage, maskRectangleData, Color.WHITE);

        } else if (maskCircleLine != null && maskCircleLine.isVisible()) {
            return FxmlImageManufacture.cropOutsideFx(inImage, maskCircleData, Color.WHITE);

        } else if (maskEllipseLine != null && maskEllipseLine.isVisible()) {
            return FxmlImageManufacture.cropOutsideFx(inImage, maskEllipseData, Color.WHITE);

        } else if (maskPolygonLine != null && maskPolygonLine.isVisible()) {
            return FxmlImageManufacture.cropOutsideFx(inImage, maskPolygonData, Color.WHITE);

        } else {
            return null;
        }

    }

    @FXML
    @Override
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
                    areaImage = FxmlImageManufacture.clearAlpha(areaImage);
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
                        popInformation(AppVaribles.message("ImageSelectionInClipBoard"));
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
                alert.setContentText(message("SureSaveSampled"));
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

                ButtonType buttonSure = new ButtonType(AppVaribles.message("Sure"));
                ButtonType buttonCancel = new ButtonType(AppVaribles.message("Cancel"));
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
                    final BufferedImage bufferedImage = FxmlImageManufacture.getBufferedImage(imageView.getImage());
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
                                popInformation(AppVaribles.message("Saved"));
                                setImageChanged(false);
                            } else {
                                popInformation(AppVaribles.message("Failed"));
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

    public String saveAsPrefix() {
        if (sourceFile != null) {
            return FileTools.getFilePrefix(sourceFile.getName());
        } else {
            return "";
        }
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (imageView == null) {
            return;
        }
        try {
            final File file = chooseSaveFile(AppVaribles.getUserConfigPath(targetPathKey),
                    saveAsPrefix(), CommonValues.ImageExtensionFilter, true);
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
                    final BufferedImage bufferedImage = FxmlImageManufacture.getBufferedImage(selected);
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
                                popInformation(AppVaribles.message("Saved"));
                                if (sourceFile == null) {
                                    loadImage(file);
                                }
                                if (openSaveCheck != null && openSaveCheck.isSelected()) {
                                    openImageViewer(file);
                                }
                            } else {
                                popInformation(AppVaribles.message("Failed"));
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
            alert.setContentText(AppVaribles.message("SureDelete"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVaribles.message("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVaribles.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonCancel) {
                return false;
            }
        }
        if (sfile.delete()) {
            popInformation(AppVaribles.message("Successful"));
            return true;
        } else {
            popError(AppVaribles.message("Failed"));
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
                imageInformation.setFileName(file.getAbsolutePath());
            }
            if (imageInformation != null && imageInformation.isIsSampled()) {
                if (imageInformation.getIndex() > 0) {
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                            + " - " + message("Image") + " " + imageInformation.getIndex()
                            + " " + message("Sampled"));
                } else {
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                            + " " + message("Sampled"));
                }

            } else {
                if (imageInformation != null && imageInformation.getIndex() > 0) {
                    getMyStage().setTitle(getBaseTitle() + " " + sourceFile.getAbsolutePath()
                            + " - " + message("Image") + " " + imageInformation.getIndex());
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
            final File file = chooseSaveFile(AppVaribles.getUserConfigPath(targetPathKey),
                    null, CommonValues.ImageExtensionFilter, true);
            if (file == null) {
                return null;
            }
            AppVaribles.setUserConfigValue(targetPathKey, file.getParent());

            if (file.exists()) {
                if (!file.delete()) {
                    popError(AppVaribles.message("Failed"));
                }
            }

            if (sfile.renameTo(file)) {
                popInformation(AppVaribles.message("Successful"));
                return file;
            } else {
                popError(AppVaribles.message("Failed"));
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
                        = (ImageManufactureController) FxmlStage.openStage(CommonValues.ImageManufactureFileFxml);
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
                = (ImageSampleController) FxmlStage.openStage(CommonValues.ImageSampleFxml);
        controller.loadImage(sourceFile, image, imageInformation);
    }

    @FXML
    public void splitAction() {
        ImageSplitController controller
                = (ImageSplitController) FxmlStage.openStage(CommonValues.ImageSplitFxml);
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
