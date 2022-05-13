package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.chart.Chart;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.chart.ChartOptions;
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
public class ControlData2DChartFx extends BaseController {

    protected ChartOptions options;
    protected Chart chart;
    protected List<List<String>> data;
    protected List<Data2DColumn> columns;
    protected ChartType chartType;

    @FXML
    protected ScrollPane scrollPane;
    @FXML
    protected AnchorPane chartPane;

    public ControlData2DChartFx() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setChart(Chart chart, ChartOptions options) {
        try {
            this.chart = chart;
            this.options = options;
            chartPane.getChildren().clear();
            chartPane.getChildren().add(chart);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void drawChart() {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            chartPane.getChildren().clear();

        } catch (Exception e) {
            MyBoxLog.error(e);
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
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            Image image = snapChart();
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
            s.append("<div align=\"center\"><img src=\"").append(imageFile.toURI().toString()).append("\"  style=\"max-width:95%;\"></div>\n");
            s.append("<hr>\n");

            List<String> names = new ArrayList<>();
            if (columns != null) {
                for (Data2DColumn c : columns) {
                    names.add(c.getColumnName());
                }
            }
            StringTable table = new StringTable(names);
            for (List<String> row : data) {
                table.add(row);
            }

            s.append(table.div());

            String html = HtmlWriteTools.html("", HtmlStyles.styleValue("Default"), s.toString());
            HtmlEditorController.load(html);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void dataAction() {
        if (data == null || data.isEmpty()) {
            popError(message("NoData"));
            return;
        }
        DataManufactureController.open(columns, data);
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
