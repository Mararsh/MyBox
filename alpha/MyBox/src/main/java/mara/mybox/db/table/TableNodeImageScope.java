package mara.mybox.db.table;

import java.sql.Connection;
import mara.mybox.controller.BaseController;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataNode;
import mara.mybox.fxml.FxTask;
import mara.mybox.image.data.ImageScope;
import mara.mybox.image.tools.ImageScopeTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableNodeImageScope extends BaseNodeTable {

    public TableNodeImageScope() {
        tableName = "Node_Image_Scope";
        treeName = message("ImageScope");
        dataName = message("ImageScope");
        dataFxml = Fxmls.ControlDataImageScopeFxml;
        examplesFileName = "ImageScope";
        majorColumnName = "area_data";
        defineColumns();
    }

    public final TableNodeImageScope defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("shape_type", ColumnType.String)
                .setLength(128)
                .setLabel(message("ShapeType")));
        addColumn(new ColumnDefinition("color_algorithm", ColumnType.String)
                .setLength(128)
                .setLabel(message("ColorMatchAlgorithm")));
        addColumn(new ColumnDefinition("shape_excluded", ColumnType.Boolean)
                .setLabel(message("ShapeExcluded")));
        addColumn(new ColumnDefinition("color_excluded", ColumnType.Boolean)
                .setLabel(message("ColorExcluded")));
        addColumn(new ColumnDefinition("color_threshold", ColumnType.Double)
                .setLabel(message("ColorMatchThreshold")));
        addColumn(new ColumnDefinition("color_weights", ColumnType.String)
                .setLabel(message("ColorWeights")));
        addColumn(new ColumnDefinition("background_file", ColumnType.File)
                .setLabel(message("Background")));
        addColumn(new ColumnDefinition("outline_file", ColumnType.File)
                .setLabel(message("Outline")));
        addColumn(new ColumnDefinition("shape_data", ColumnType.Clob)
                .setLabel(message("Shape")));
        addColumn(new ColumnDefinition("color_data", ColumnType.Clob)
                .setLabel(message("Colors")));
        return this;
    }

    @Override
    public String valuesHtml(FxTask task, Connection conn, BaseController controller, DataNode node) {
        try {
            ImageScope scope = ImageScopeTools.fromDataNode(task, controller, node);
            if (scope == null) {
                return null;
            }
            return ImageScopeTools.toHtml(task, scope);
        } catch (Exception e) {
            return null;
        }
    }

}
