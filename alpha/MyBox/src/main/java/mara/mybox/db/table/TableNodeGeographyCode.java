package mara.mybox.db.table;

import java.io.File;
import java.sql.Connection;
import mara.mybox.controller.BaseController;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataNode;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.FxTask;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-4-23
 * @License Apache License Version 2.0
 */
public class TableNodeGeographyCode extends BaseNodeTable {

    public TableNodeGeographyCode() {
        tableName = "Node_Geography_Code";
        treeName = message("GeographyCode");
        dataName = message("GeographyCode");
        dataFxml = Fxmls.ControlDataGeographyCodeFxml;
        examplesFileName = "GeographyCode";
        nodeExecutable = true;
        defineColumns();
    }

    public final TableNodeGeographyCode defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("level", ColumnType.Short));
        addColumn(new ColumnDefinition("coordinate_system", ColumnType.Short));
        addColumn(new ColumnDefinition("longitude", ColumnType.Longitude));
        addColumn(new ColumnDefinition("latitude", ColumnType.Latitude));
        addColumn(new ColumnDefinition("altitude", ColumnType.Double));
        addColumn(new ColumnDefinition("precision", ColumnType.Double));
        addColumn(new ColumnDefinition("name", ColumnType.String));
        addColumn(new ColumnDefinition("chinese_name", ColumnType.String));
        addColumn(new ColumnDefinition("english_name", ColumnType.String));
        addColumn(new ColumnDefinition("code1", ColumnType.String));
        addColumn(new ColumnDefinition("code2", ColumnType.String));
        addColumn(new ColumnDefinition("code3", ColumnType.String));
        addColumn(new ColumnDefinition("code4", ColumnType.String));
        addColumn(new ColumnDefinition("code5", ColumnType.String));
        addColumn(new ColumnDefinition("alias1", ColumnType.String));
        addColumn(new ColumnDefinition("alias2", ColumnType.String));
        addColumn(new ColumnDefinition("alias3", ColumnType.String));
        addColumn(new ColumnDefinition("alias4", ColumnType.String));
        addColumn(new ColumnDefinition("alias5", ColumnType.String));
        addColumn(new ColumnDefinition("area", ColumnType.Double));
        addColumn(new ColumnDefinition("population", ColumnType.Long));
        addColumn(new ColumnDefinition("description", ColumnType.String));

        return this;
    }

    @Override
    public String label(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        switch (name.toLowerCase()) {
            case "level":
                return message("Level");
            case "coordinate_system":
                return message("CoordinateSystem");
            case "longitude":
                return message("Longitude");
            case "latitude":
                return message("Latitude");
            case "altitude":
                return message("Altitude");
            case "precision":
                return message("Precision");
            case "name":
                return message("Name");
            case "chinese_name":
                return message("ChineseName");
            case "english_name":
                return message("EnglishName");
            case "code1":
                return message("Code1");
            case "code2":
                return message("Code2");
            case "code3":
                return message("Code3");
            case "code4":
                return message("Code4");
            case "code5":
                return message("Code5");
            case "alias1":
                return message("Alias1");
            case "alias2":
                return message("Alias2");
            case "alias3":
                return message("Alias3");
            case "alias4":
                return message("Alias4");
            case "alias5":
                return message("Alias5");
            case "area":
                return message("Area");
            case "population":
                return message("Population");
            case "description":
                return message("Description");
        }
        return super.label(name);
    }

    @Override
    public String valuesHtml(FxTask task, Connection conn, BaseController controller, DataNode node) {
        try {
            String address = node.getStringValue("address");
            String icon = node.getStringValue("icon");
            if (address == null || address.isBlank()) {
                return null;
            }
            String html = "<A href=\"" + address + "\">";
            if (icon != null && !icon.isBlank()) {
                try {
                    String base64 = FxImageTools.base64(null, new File(icon), "png");
                    if (base64 != null) {
                        html += "<img src=\"data:image/png;base64," + base64 + "\" width=" + 40 + " >";
                    }
                } catch (Exception e) {
                }
            }
            html += address + "</A>\n";
            return html;
        } catch (Exception e) {
            return null;
        }
    }

}