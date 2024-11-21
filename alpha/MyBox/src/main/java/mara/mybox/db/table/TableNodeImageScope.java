package mara.mybox.db.table;

import java.sql.Connection;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.ImageScopeTools;
import mara.mybox.controller.BaseController;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataNode;
import mara.mybox.fxml.FxTask;
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
        defineColumns();
    }

    public final TableNodeImageScope defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("scope_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("color_type", ColumnType.String).setLength(128));
        addColumn(new ColumnDefinition("area_excluded", ColumnType.Boolean));
        addColumn(new ColumnDefinition("color_excluded", ColumnType.Boolean));
        addColumn(new ColumnDefinition("color_distance", ColumnType.Integer));
        addColumn(new ColumnDefinition("background_file", ColumnType.File));
        addColumn(new ColumnDefinition("outline_file", ColumnType.File));
        addColumn(new ColumnDefinition("area_data", ColumnType.Clob));
        addColumn(new ColumnDefinition("color_data", ColumnType.Clob));
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
