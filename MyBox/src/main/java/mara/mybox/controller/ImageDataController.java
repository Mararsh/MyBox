package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.data.IntStatistic;
import mara.mybox.fxml.FxmlColor;
import mara.mybox.fxml.IntStatisticColorCell;
import mara.mybox.image.ImageStatistic;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-6
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageDataController extends ImageViewerController {

    protected ImageViewerController parent;
    protected ObservableList<IntStatistic> statisticList = FXCollections.observableArrayList();
    protected Map<String, Color> colorTable;
    protected boolean embedded;

    @FXML
    protected CheckBox topCheck, medianCheck,
            greyHistCheck, redHistCheck, greenHistCheck, blueHistCheck,
            alphaHistCheck, hueHistCheck, saturationHistCheck, brightnessHistCheck;
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
    protected Button refreshButton;
    @FXML
    protected HBox opBox, manageBox;

    public ImageDataController() {
        baseTitle = message("ImageData");

    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            if (topCheck != null) {
                topCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                        if (!embedded && myStage != null) {
                            myStage.setAlwaysOnTop(topCheck.isSelected());
                        }
                        AppVariables.setUserConfigValue("ImageDataTop", newValue);
                    }
                });
            }

            selectAreaCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    loadData();
                }
            });
            selectAreaCheck.setSelected(false);

            initDataPane();
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void toFront() {
        super.toFront();
        if (!embedded && topCheck != null) {
            myStage.setAlwaysOnTop(topCheck.isSelected());
        }
    }

    protected void initDataPane() {
        try {
            greyHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue("ImageHistGrey", newVal);
                    showHistogram(message("Grey"), newVal);
                }
            });
            greyHistCheck.setSelected(AppVariables.getUserConfigBoolean("ImageHistGrey", false));
            redHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue("ImageHistRed", newVal);
                    showHistogram(message("Red"), newVal);
                }
            });
            redHistCheck.setSelected(AppVariables.getUserConfigBoolean("ImageHistRed", false));
            greenHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue("ImageHistGreen", newVal);
                    showHistogram(message("Green"), newVal);
                }
            });
            greenHistCheck.setSelected(AppVariables.getUserConfigBoolean("ImageHistGreen", false));
            blueHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue("ImageHistBlue", newVal);
                    showHistogram(message("Blue"), newVal);
                }
            });
            blueHistCheck.setSelected(AppVariables.getUserConfigBoolean("ImageHistBlue", false));
            hueHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue("ImageHistHue", newVal);
                    showHistogram(message("Hue"), newVal);
                }
            });
            hueHistCheck.setSelected(AppVariables.getUserConfigBoolean("ImageHistHue", false));
            brightnessHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue("ImageHistBrightness", newVal);
                    showHistogram(message("Brightness"), newVal);
                }
            });
            brightnessHistCheck.setSelected(AppVariables.getUserConfigBoolean("ImageHistBrightness", false));
            saturationHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue("ImageHistSaturation", newVal);
                    showHistogram(message("Saturation"), newVal);
                }
            });
            saturationHistCheck.setSelected(AppVariables.getUserConfigBoolean("ImageHistSaturation", false));
            alphaHistCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal) {
                    AppVariables.setUserConfigValue("ImageHistAlpha", newVal);
                    showHistogram(message("Alpha"), newVal);
                }
            });
            alphaHistCheck.setSelected(AppVariables.getUserConfigBoolean("ImageHistAlpha", false));

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
            colorTable.put(message("Red"), Color.RED);
            colorTable.put(message("Green"), Color.GREEN);
            colorTable.put(message("Blue"), Color.BLUE);
            colorTable.put(message("Alpha"), Color.CORNFLOWERBLUE);
            colorTable.put(message("Hue"), Color.MEDIUMVIOLETRED);
            colorTable.put(message("Brightness"), Color.GOLD);
            colorTable.put(message("Saturation"), Color.MEDIUMAQUAMARINE);
            colorTable.put(message("Gray"), Color.GRAY);
            colorTable.put(message("Grey"), Color.GREY);

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
                text.setText(message(item));
                setGraphic(text);
            } else {
                setGraphic(null);
                setText(null);
            }
        }
    }

    public void init(File sourceFile, Image image, boolean embedded) {
        try {
            super.init(sourceFile, image);
            this.embedded = embedded;
            if (embedded) {
                thisPane.getChildren().remove(mainMenu);
                selectSourceButton.setVisible(false);
                if (opBox.getChildren().contains(manageBox)) {
                    opBox.getChildren().remove(manageBox);
                }
            } else {
                topCheck.setSelected(AppVariables.getUserConfigBoolean("ImageDataTop", false));
                if (topCheck.isSelected() && myStage != null) {
                    myStage.setAlwaysOnTop(true);
                }
            }

            loadData();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    public void loadData(Map<String, Object> imageData) {

        if (imageData != null) {
            this.imageData = imageData;
            showData();
        } else if (image != null) {
            loadData();
        }
    }

    protected void loadData() {
        if (image == null || isSettingValues) {
            return;
        }
        synchronized (this) {
            if (task != null) {
                return;
            }
            imageData = null;
            task = new SingletonTask<Void>() {

                private Image aImage;

                @Override
                protected Void call() {
                    if (selectAreaCheck.isSelected()) {
                        aImage = cropImage();
                    }
                    if (aImage == null) {
                        aImage = image;
                    }
                    imageData = ImageStatistic.analyze(SwingFXUtils.fromFXImage(aImage, null));
                    ok = imageData != null;
                    return null;
                }

                @Override
                protected void whenSucceeded() {
                    showData();
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void showData(Map<String, Object> imageData) {
        this.imageData = imageData;
        showData();
    }

    protected void showData() {
        try {
            if (image == null || imageData == null || isSettingValues) {
                return;
            }
            statisticList.clear();
            histogramChart.getData().clear();
            List<IntStatistic> statistic = (List<IntStatistic>) imageData.get("statistic");
            statisticList.addAll(statistic);
            showHistogram(message("Grey"), greyHistCheck.isSelected());
            showHistogram(message("Red"), redHistCheck.isSelected());
            showHistogram(message("Green"), greenHistCheck.isSelected());
            showHistogram(message("Blue"), blueHistCheck.isSelected());
            showHistogram(message("Hue"), hueHistCheck.isSelected());
            showHistogram(message("Brightness"), brightnessHistCheck.isSelected());
            showHistogram(message("Saturation"), saturationHistCheck.isSelected());
            showHistogram(message("Alpha"), alphaHistCheck.isSelected());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void showHistogram(String colorName, boolean isShow) {
        if (image == null || imageData == null || isSettingValues || colorName == null
                || histogramChart.getData() == null) {
            return;
        }
        if (isShow) {
            showHistogram(colorName);
        } else {
            hideHistogram(colorName);
        }
    }

    protected void showHistogram(final String colorName) {
        if (imageData == null || isSettingValues || colorName == null
                || histogramChart.getData() == null) {
            return;
        }
        for (Object s : histogramChart.getData()) {
            XYChart.Series xy = (XYChart.Series) s;
            if (colorName.equals(xy.getName())) {
                return;
            }
        }

        int[] histogram;
        if (message("Red").equals(colorName)) {
            histogram = (int[]) imageData.get("redHistogram");
        } else if (message("Green").equals(colorName)) {
            histogram = (int[]) imageData.get("greenHistogram");
        } else if (message("Blue").equals(colorName)) {
            histogram = (int[]) imageData.get("blueHistogram");
        } else if (message("Alpha").equals(colorName)) {
            histogram = (int[]) imageData.get("alphaHistogram");
        } else if (message("Hue").equals(colorName)) {
            histogram = (int[]) imageData.get("hueHistogram");
        } else if (message("Brightness").equals(colorName)) {
            histogram = (int[]) imageData.get("brightnessHistogram");
        } else if (message("Saturation").equals(colorName)) {
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
                || histogramChart.getData() == null) {
            return;
        }

        List<Object> data = new ArrayList<>();
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

    // https://stackoverflow.com/questions/37634769/dynamically-change-chart-colors-using-colorpicker/37646943
    private void updateLegend() {
        try {
            Set<Node> legendItems = histogramChart.lookupAll("Label.chart-legend-item");
            if (legendItems.isEmpty()) {
                return;
            }
            for (Node legendItem : legendItems) {
                Label legendLabel = (Label) legendItem;
                Node legend = legendLabel.getGraphic();
                if (legend != null) {
                    String colorString = FxmlColor.rgb2Hex(colorTable.get(legendLabel.getText()));
                    legend.setStyle("-fx-background-color: " + colorString);
                }
            }
        } catch (Exception e) {

        }
    }

    @FXML
    public void clearHistogram() {
        histogramChart.getData().clear();
        greyHistCheck.setSelected(false);
        redHistCheck.setSelected(false);
        greenHistCheck.setSelected(false);
        blueHistCheck.setSelected(false);
        alphaHistCheck.setSelected(false);
        hueHistCheck.setSelected(false);
        saturationHistCheck.setSelected(false);
        brightnessHistCheck.setSelected(false);

    }

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();

            loadData(imageData);

        } catch (Exception e) {
            logger.error(e.toString());
            imageView.setImage(null);
            alertInformation(message("NotSupported"));
        }
    }

    @FXML
    public void refreshData() {
        if (parent != null) {
            init(parent.sourceFile, parent.imageView.getImage(), embedded);
        } else {
            init(sourceFile, imageView.getImage(), embedded);
        }
    }

    @FXML
    @Override
    public boolean drawMaskRectangleLine() {
        if (selectAreaCheck.isSelected() && super.drawMaskRectangleLine()) {
            loadData();
            return true;
        } else {
            return false;
        }

    }

}
