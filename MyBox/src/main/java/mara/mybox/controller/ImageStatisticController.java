package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.util.Callback;
import mara.mybox.data.IntStatistic;
import mara.mybox.image.ImageColor;
import mara.mybox.image.ImageQuantization;
import mara.mybox.image.ImageQuantization.QuantizationAlgorithm;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-10-12
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageStatisticController extends ImageViewerController {

    final private String ImageStatisticHueStages, ImageStatisticSaturationStages,
            ImageStatisticBrightnessStages, ImageStatisticDataNumber;
    private int paletteSize;

    protected ObservableList<IntStatistic> colorList = FXCollections.observableArrayList();
    protected ObservableList<IntStatistic> colorSummaryList = FXCollections.observableArrayList();
    protected ObservableList<IntStatistic> greyList = FXCollections.observableArrayList();
    protected ObservableList<IntStatistic> redList = FXCollections.observableArrayList();
    protected ObservableList<IntStatistic> blueList = FXCollections.observableArrayList();
    protected ObservableList<IntStatistic> greenList = FXCollections.observableArrayList();
    protected ObservableList<IntStatistic> hueList = FXCollections.observableArrayList();
    protected ObservableList<IntStatistic> saturationList = FXCollections.observableArrayList();
    protected ObservableList<IntStatistic> brightnessList = FXCollections.observableArrayList();
    protected ObservableList<IntStatistic> opacityList = FXCollections.observableArrayList();

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab imageTab, histogramTab;
    @FXML
    protected BarChart greyHistogram, redHistogram, blueHistogram, greenHistogram,
            opacityHistogram, hueHistogram, saturationHistogram, brightnessHistogram;
    @FXML
    protected PieChart colorPie;
    @FXML
    protected ComboBox paletteBox, algorithmBox;
    @FXML
    protected NumberAxis grayY;
    @FXML
    private TableView<IntStatistic> colorTable, colorSummaryTable, greyTable, redTable, blueTable, greenTable,
            hueTable, saturationTable, brightnessTable, opacityTable;
    @FXML
    private TableColumn<IntStatistic, String> colorSummaryNameColumn,
            greyNameColumn,
            redValueColumn, redNameColumn,
            blueValueColumn, blueNameColumn, greenValueColumn, greenNameColumn, hueValueColumn, hueNameColumn,
            saturationValueColumn, saturationNameColumn, brightnessValueColumn, brightnessNameColumn,
            opacityValueColumn, opacityNameColumn;
    @FXML
    private TableColumn<IntStatistic, Integer> colorSequenceColumn, colorValueColumn, colorNumberColumn, colorShowcaseColumn,
            colorSummaryValueColumn, colorSummaryShowcaseColumn, colorSummaryNumberColumn,
            greyValueColumn, greyNumberColumn, greyShowcaseColumn,
            redNumberColumn, blueNumberColumn, greenNumberColumn,
            hueNumberColumn, saturationNumberColumn, brightnessNumberColumn, opacityNumberColumn;
    @FXML
    private TableColumn<IntStatistic, Float> colorPercentageColumn, colorSummaryPercentageColumn,
            greyPercentageColumn;
    @FXML
    private CheckBox ditheringCheck;
    @FXML
    private ToolBar colorBar;

    public ImageStatisticController() {
        baseTitle = AppVariables.message("ImageStatistic");

        ImageStatisticHueStages = "ImageStatisticHueStages";
        ImageStatisticSaturationStages = "ImageStatisticSaturationStages";
        ImageStatisticBrightnessStages = "ImageStatisticBrightnessStages";
        ImageStatisticDataNumber = "ImageStatisticDataNumber";
    }

    @Override
    public void initializeNext2() {
        try {
            tabPane.disableProperty().bind(
                    Bindings.isNull(imageView.imageProperty())
            );
            imageView.requestFocus();

            initColorTab();
            initGreyTab();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private void initColorTab() {

        colorTable.setItems(colorList);
        colorValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
//        colorValueColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
//            @Override
//            public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
//                String name = (String) param.getColumns().get(0);
//                return new ValueCell();
//            }
//        });
        colorShowcaseColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        colorShowcaseColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
            @Override
            public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                return new ShowcaseCell();
            }
        });
        colorNumberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        colorSequenceColumn.setCellValueFactory(new PropertyValueFactory<>("value2"));
        colorPercentageColumn.setCellValueFactory(new PropertyValueFactory<>("percentage"));

        colorSummaryTable.setItems(colorSummaryList);
        colorSummaryValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        colorSummaryValueColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
            @Override
            public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                return new ValueCell();
            }
        });
        colorSummaryShowcaseColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        colorSummaryShowcaseColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
            @Override
            public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                return new ShowcaseCell();
            }
        });
        colorSummaryNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        colorSummaryNameColumn.setCellFactory(new Callback<TableColumn<IntStatistic, String>, TableCell<IntStatistic, String>>() {
            @Override
            public TableCell<IntStatistic, String> call(TableColumn<IntStatistic, String> param) {
                return new NameCell();
            }
        });
        colorSummaryNumberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        colorSummaryPercentageColumn.setCellValueFactory(new PropertyValueFactory<>("percentage"));

        List<String> aList = Arrays.asList(message("RGBUniformQuantization"), message("HSBUniformQuantization"));
        algorithmBox.getItems().addAll(aList);
        algorithmBox.getSelectionModel().select(message("RGBUniformQuantization"));

        List<String> paletteList = Arrays.asList(
                "512", "64", "8", "4096", "216", "343", "27", "125", "1000", "729", "1728", "8000");
        paletteBox.getItems().addAll(paletteList);
        paletteBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                try {
                    paletteSize = Integer.valueOf(newValue);
                } catch (Exception e) {
                }
            }
        });
        paletteBox.getSelectionModel().select("512");

    }

    private void initGreyTab() {
        greyTable.setItems(greyList);
        greyValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        greyValueColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
            @Override
            public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                return new ValueCell();
            }
        });
        greyShowcaseColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        greyShowcaseColumn.setCellFactory(new Callback<TableColumn<IntStatistic, Integer>, TableCell<IntStatistic, Integer>>() {
            @Override
            public TableCell<IntStatistic, Integer> call(TableColumn<IntStatistic, Integer> param) {
                return new ShowcaseCell();
            }
        });
        greyNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        greyNameColumn.setCellFactory(new Callback<TableColumn<IntStatistic, String>, TableCell<IntStatistic, String>>() {
            @Override
            public TableCell<IntStatistic, String> call(TableColumn<IntStatistic, String> param) {
                return new NameCell();
            }
        });
        greyNumberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        greyPercentageColumn.setCellValueFactory(new PropertyValueFactory<>("percentage"));

    }

    private class ValueCell extends TableCell<IntStatistic, Integer> {

        @Override
        protected void updateItem(final Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item < 0) {
                setText("");
            } else {
                if (item > 255) {
                    setText(ImageColor.pixel2hex(item));
                } else {
                    setText(item + "");
                }
            }
        }
    }

    private class ShowcaseCell extends TableCell<IntStatistic, Integer> {

        private final Rectangle rectangle;

        {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            rectangle = new Rectangle(30, 20);
        }

        @Override
        protected void updateItem(final Integer item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item < 0) {
                setGraphic(null);
            } else {
                if (item > 255) {
                    rectangle.setFill(ImageColor.converColor(new java.awt.Color(item)));
                    setGraphic(rectangle);
                } else {
                    double grey = item / 255.0;
                    rectangle.setFill(new Color(grey, grey, grey, 1.0));
                    setGraphic(rectangle);
                }

            }
        }

    }

    private class NameCell extends TableCell<IntStatistic, String> {

        final Text text = new Text();

        @Override
        protected void updateItem(final String item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty && item != null) {
                text.setText(AppVariables.message(item));
                setGraphic(text);
            } else {
                setGraphic(null);
                setText(null);
            }
        }
    }

    @Override
    public void afterImageLoaded() {
        try {
            super.afterImageLoaded();
            if (image == null) {
                return;
            }
            makeStatistic();
        } catch (Exception e) {
            logger.error(e.toString());
            imageView.setImage(null);
            alertInformation(AppVariables.message("NotSupported"));
        }
    }

    @FXML
    public void colorAction() {
        makeStatistic();
    }

    // https://stackoverflow.com/questions/15219334/javafx-change-piechart-color
    private void makeStatistic() {
        QuantizationAlgorithm algorithm;
        switch (algorithmBox.getSelectionModel().getSelectedIndex()) {
            case 0:
                algorithm = QuantizationAlgorithm.RGB_Uniform;
                break;
            case 1:
                algorithm = QuantizationAlgorithm.HSB_Uniform;
                break;
            case 4:
                algorithm = QuantizationAlgorithm.Statistic;
                break;
            case 5:
                algorithm = QuantizationAlgorithm.kMeansClustering;
                break;
            case 6:
                algorithm = QuantizationAlgorithm.ANN;
                break;
            default:
                return;
        }
        int channelSize = (int) Math.round(Math.pow(paletteSize, 1.0 / 3.0));
        final ImageQuantization quantization = new ImageQuantization(image);
        quantization.set(algorithm, channelSize);
        quantization.setIsDithering(ditheringCheck.isSelected());

        colorList.clear();
        colorSummaryList.clear();
        greyList.clear();
        redList.clear();
        blueList.clear();
        opacityList.clear();
        greenList.clear();
        hueList.clear();
        saturationList.clear();
        brightnessList.clear();

        colorPie.getData().clear();
        greyHistogram.getData().clear();
        redHistogram.getData().clear();
        blueHistogram.getData().clear();
        opacityHistogram.getData().clear();
        greenHistogram.getData().clear();
        hueHistogram.getData().clear();
        saturationHistogram.getData().clear();
        brightnessHistogram.getData().clear();

        synchronized (this) {
            if (task != null) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
//                    ImageStatistic statistic = new ImageStatistic(SwingFXUtils.fromFXImage(image, null), quantization);
//                    final Map<String, Object> statisticMap = statistic.statistic();
//                    if (isCancelled() || statisticMap == null) {
//                        return null;
//                    }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }

                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    if (ok) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
//                            Map<String, Object> statistic = (Map<String, Object>) statisticMap.get("colorQuantization");
//                            colorList.addAll((List<IntStatistic>) statistic.get("data"));
//                            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
//                            for (int i = 0; i < colorList.size(); i++) {
//                                IntStatistic s = colorList.get(i);
//                                if (s.getPercentage() < 1) {
//                                    break;
//                                }
//                                pieChartData.add(new PieChart.Data(s.getPercentage() + "%  "
//                                        + ImageColor.pixel2hex(colorList.get(i).getValue()), s.getNumber()));
//                            }
//                            colorPie.getData().addAll(pieChartData);
//                            for (int i = 0; i < pieChartData.size(); i++) {
//                                PieChart.Data d = pieChartData.get(i);
//                                d.getNode().setStyle("-fx-pie-color: " + ImageColor.pixel2hex(colorList.get(i).getValue()) + ";"
//                                );
//                            }  // Must set colors after chart generated
//                            colorSummaryList.add((IntStatistic) statistic.get("size"));
//                            colorSummaryList.add((IntStatistic) statistic.get("sum"));
//                            colorSummaryList.add((IntStatistic) statistic.get("mode"));
//                            colorSummaryList.add((IntStatistic) statistic.get("median"));
//                            colorSummaryList.add((IntStatistic) statistic.get("mean"));
//                            colorSummaryList.add((IntStatistic) statistic.get("variance"));
//
//                            statistic = (Map<String, Object>) statisticMap.get("grey");
//                            greyList.addAll((List<IntStatistic>) statistic.get("data"));
//                            XYChart.Series series = new XYChart.Series();
//                            for (int i = 0; i < greyList.size(); i++) {
//                                IntStatistic s = greyList.get(i);
//                                series.getData().add(new XYChart.Data(s.getValue() + "", s.getNumber()));
//                            }
//                            greyHistogram.getData().addAll(series);
//                            IntStatistic.setDesc(greyList); // Must sort after historgram generated

//                            greyList.add(0, (IntStatistic) statistic.get("maximum"));
//                            greyList.add(0, (IntStatistic) statistic.get("minimum"));
//                            greyList.add(0, (IntStatistic) statistic.get("median"));
//                            greyList.add(0, (IntStatistic) statistic.get("average"));
//                            greyList.add(0, (IntStatistic) statistic.get("sum"));
                            }
                        });
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

}
