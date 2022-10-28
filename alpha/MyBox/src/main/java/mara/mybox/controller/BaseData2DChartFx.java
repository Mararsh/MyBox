package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.chart.Chart;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.chart.ChartOptions.ChartType;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-5-7
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DChartFx extends BaseController {

    protected Chart chart;
    protected List<List<String>> data, checkedData;
    protected List<Data2DColumn> columns, checkedColumns;
    protected ChartType chartType;
    protected Map<String, String> palette;
    protected SimpleBooleanProperty redrawNotify;

    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected AnchorPane chartPane;
    @FXML
    protected FlowPane buttonsPane;

    public BaseData2DChartFx() {
        redrawNotify = new SimpleBooleanProperty(false);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            buttonsPane.setDisable(true);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setChart(Chart chart) {
        try {
            this.chart = chart;
            chartPane.getChildren().clear();
            if (chart != null) {
                chartPane.getChildren().add(chart);
            }
            buttonsPane.setDisable(chart == null);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void clearChart() {
        setChart(null);
    }

    public void redraw() {
        redrawNotify.set(!redrawNotify.get());
    }

    public Map<String, String> makePalette() {
        try {
            if (palette == null) {
                palette = new HashMap();
            }
            if (columns == null) {
                return palette;
            }
            Random random = new Random();
            for (int i = 0; i < columns.size(); i++) {
                Data2DColumn column = columns.get(i);
                String rgb = palette.get(column.getColumnName());
                if (rgb == null) {
                    Color color = column.getColor();
                    if (color == null) {
                        color = FxColorTools.randomColor(random);
                    }
                    rgb = FxColorTools.color2rgb(color);
                    palette.put(column.getColumnName(), rgb);
                }
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        return palette;
    }

    @FXML
    public void randomColors() {
        try {
            if (palette == null) {
                palette = new HashMap();
            } else {
                palette.clear();
            }
            if (columns != null) {
                Random random = new Random();
                for (int i = 0; i < columns.size(); i++) {
                    Data2DColumn column = columns.get(i);
                    Color color = FxColorTools.randomColor(random);
                    String rgb = FxColorTools.color2rgb(color);
                    palette.put(column.getColumnName(), rgb);
                }
            }
            redraw();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void paneSize() {
        if (chartPane == null || chart == null) {
            return;
        }
        try {
            AnchorPane.setTopAnchor(chart, 2d);
            AnchorPane.setBottomAnchor​(chart, 2d);
            AnchorPane.setLeftAnchor(chart, 2d);
            AnchorPane.setRightAnchor​(chart, 2d);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void zoomIn() {
        if (chartPane == null || chart == null) {
            return;
        }
        try {
            Bounds bounds = chart.getBoundsInLocal();
            AnchorPane.clearConstraints(chart);
            chart.setPrefSize(bounds.getWidth() + 40, bounds.getHeight() + 40);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public boolean popAction() {
        ImagePopController.openImage(this, snapChart());
        return true;
    }

    public Image snapChart() {
        return NodeTools.snap(chart);
    }

    @FXML
    public void htmlAction() {
        if (data == null || data.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        Image image = snapChart();
        SingletonTask htmlTask = new SingletonTask<Void>(this) {

            private String html;

            @Override
            protected boolean handle() {
                try {
                    File imageFile = new File(AppPaths.getGeneratedPath() + File.separator + DateTools.nowFileString() + ".jpg");
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                    ImageFileWriters.writeImageFile(bufferedImage, "jpg", imageFile.getAbsolutePath());

                    StringBuilder s = new StringBuilder();
                    String title = chart.getTitle();
                    if (title != null) {
                        s.append("<h1  class=\"center\">").append(title).append("</h1>\n");
                        s.append("<hr>\n");
                    }
                    s.append("<h2  class=\"center\">").append(message("Image")).append("</h2>\n");
                    s.append("<div align=\"center\"><img src=\"").append(imageFile.toURI().toString())
                            .append("\"  style=\"max-width:95%;\"></div>\n");
                    s.append("<hr>\n");

                    checkData();
                    List<String> names = new ArrayList<>();
                    if (checkedColumns != null) {
                        for (Data2DColumn c : checkedColumns) {
                            names.add(c.getColumnName());
                        }
                    }
                    StringTable table = new StringTable(names);
                    for (List<String> row : checkedData) {
                        table.add(row);
                    }

                    s.append(table.div());

                    html = HtmlWriteTools.html(title, HtmlStyles.styleValue("Default"), s.toString());

                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                HtmlEditorController.load(html);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                checkedColumns = null;
                checkedData = null;
            }

        };
        start(htmlTask, false);
    }

    @FXML
    public void dataAction() {
        if (data == null || data.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        SingletonTask dataTask = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    checkData();
                    return checkedData != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                DataManufactureController.open(checkedColumns, checkedData);
            }

        };
        start(dataTask, false);
    }

    /*
        remove duplicated columns
     */
    public void checkData() {
        checkedData = data;
        checkedColumns = columns;
        if (columns == null || columns.isEmpty()) {
            return;
        }
        List<Integer> indice = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            String name = columns.get(i).getColumnName();
            if (!names.contains(name)) {
                names.add(name);
                indice.add(i);
            }
        }
        if (indice.size() == columns.size()) {
            return;
        }
        checkedData = new ArrayList<>();
        checkedColumns = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            if (indice.contains(i)) {
                checkedColumns.add(columns.get(i));
            }
        }
        for (List<String> row : data) {
            List<String> checkedRow = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                if (indice.contains(i)) {
                    checkedRow.add(row.get(i));
                }
            }
            checkedData.add(checkedRow);
        }
    }

    @Override
    public boolean controlAlt2() {
        paneSize();
        return true;
    }

    @Override
    public boolean controlAlt3() {
        zoomIn();
        return true;
    }

}
