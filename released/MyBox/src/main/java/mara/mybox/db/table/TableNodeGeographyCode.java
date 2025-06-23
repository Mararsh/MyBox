package mara.mybox.db.table;

import java.sql.Connection;
import mara.mybox.data.GeographyCode;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.GeographyCodeTools;
import mara.mybox.tools.LongTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
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
        majorColumnName = Languages.isChinese() ? "chinese_name" : "english_name";
        nodeExecutable = true;
        defineColumns();
    }

    public final TableNodeGeographyCode defineColumns() {
        defineNodeColumns();
        addColumn(new ColumnDefinition("chinese_name", ColumnType.String)
                .setLabel(message("ChineseName")));
        addColumn(new ColumnDefinition("english_name", ColumnType.String)
                .setLabel(message("EnglishName")));
        addColumn(new ColumnDefinition("level", ColumnType.EnumeratedShort)
                .setFormat(GeographyCodeTools.addressLevelMessageNames())
                .setLabel(message("Level")));
        addColumn(new ColumnDefinition("coordinate_system", ColumnType.EnumeratedShort)
                .setFormat(GeographyCodeTools.coordinateSystemMessageNames())
                .setLabel(message("CoordinateSystem")));
        addColumn(new ColumnDefinition("longitude", ColumnType.Longitude)
                .setLabel(message("Longitude")));
        addColumn(new ColumnDefinition("latitude", ColumnType.Latitude)
                .setLabel(message("Latitude")));
        addColumn(new ColumnDefinition("altitude", ColumnType.Double)
                .setLabel(message("Altitude")));
        addColumn(new ColumnDefinition("precision", ColumnType.Double)
                .setLabel(message("Precision")));
        addColumn(new ColumnDefinition("continent", ColumnType.String)
                .setLabel(message("Continent")));
        addColumn(new ColumnDefinition("country", ColumnType.String)
                .setLabel(message("Country")));
        addColumn(new ColumnDefinition("province", ColumnType.String)
                .setLabel(message("Province")));
        addColumn(new ColumnDefinition("city", ColumnType.String)
                .setLabel(message("City")));
        addColumn(new ColumnDefinition("county", ColumnType.String)
                .setLabel(message("County")));
        addColumn(new ColumnDefinition("town", ColumnType.String)
                .setLabel(message("Town")));
        addColumn(new ColumnDefinition("village", ColumnType.String)
                .setLabel(message("Village")));
        addColumn(new ColumnDefinition("building", ColumnType.String)
                .setLabel(message("Building")));
        addColumn(new ColumnDefinition("poi", ColumnType.String)
                .setLabel(message("PointOfInterest")));
        addColumn(new ColumnDefinition("code1", ColumnType.String)
                .setLabel(message("Code1")));
        addColumn(new ColumnDefinition("code2", ColumnType.String)
                .setLabel(message("Code2")));
        addColumn(new ColumnDefinition("code3", ColumnType.String)
                .setLabel(message("Code3")));
        addColumn(new ColumnDefinition("code4", ColumnType.String)
                .setLabel(message("Code4")));
        addColumn(new ColumnDefinition("code5", ColumnType.String)
                .setLabel(message("Code5")));
        addColumn(new ColumnDefinition("alias1", ColumnType.String)
                .setLabel(message("Alias1")));
        addColumn(new ColumnDefinition("alias2", ColumnType.String)
                .setLabel(message("Alias2")));
        addColumn(new ColumnDefinition("alias3", ColumnType.String)
                .setLabel(message("Alias3")));
        addColumn(new ColumnDefinition("alias4", ColumnType.String)
                .setLabel(message("Alias4")));
        addColumn(new ColumnDefinition("alias5", ColumnType.String)
                .setLabel(message("Alias5")));
        addColumn(new ColumnDefinition("area", ColumnType.Double).setFormat("GroupInThousands")
                .setLabel(message("SquareMeters")));
        addColumn(new ColumnDefinition("population", ColumnType.Long).setFormat("GroupInThousands")
                .setLabel(message("Population")));
        addColumn(new ColumnDefinition("description", ColumnType.String)
                .setLabel(message("Description")));
        addColumn(new ColumnDefinition("image", ColumnType.Image)
                .setLabel(message("Image")));

        return this;
    }

    @Override
    public DataNode getRoot(Connection conn) {
        try {
            if (conn == null || tableName == null) {
                return null;
            }
            DataNode root = super.getRoot(conn);
            if (root.getStringValue("chinese_name") == null
                    || root.getStringValue("english_name") == null) {
                root.setValue("chinese_name", message("zh", "GeographyCode"));
                root.setValue("english_name", message("en", "GeographyCode"));
                root = updateData(conn, root);
            }
            return root;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return null;
        }
    }

    @Override
    public String displayValue(ColumnDefinition column, Object v) {
        if (column == null || v == null) {
            return null;
        }
        switch (column.getColumnName().toLowerCase()) {
            case "area":
                double area = (double) v;
                if (area <= 0 || DoubleTools.invalidDouble(area)) {
                    return null;
                }
                break;
            case "population":
                long population = (long) v;
                if (population <= 0 || LongTools.invalidLong(population)) {
                    return null;
                }
                break;
            case "precision":
            case "altitude":
            case "image":
                return null;
        }
        return column.formatValue(v);
    }

    @Override
    public String exportValue(ColumnDefinition column, Object v, boolean format) {
        return displayValue(column, v);
    }

    @Override
    public Object importValue(ColumnDefinition column, String v) {
        if (column == null || v == null) {
            return null;
        }
        switch (column.getColumnName().toLowerCase()) {
            case "area":
                return area(v);
            case "population":
                return population(v);
            case "precision":
            case "altitude":
            case "image":
                return null;
        }
        return column.fromString(v, ColumnDefinition.InvalidAs.Use);
    }

    public Object area(String v) {
        try {
            double area = Double.parseDouble(v.replaceAll(",", ""));
            if (area <= 0 || DoubleTools.invalidDouble(area)) {
                return null;
            } else {
                return area;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public Object population(String v) {
        try {
            long population = Math.round(Double.parseDouble(v.replaceAll(",", "")));
            if (population <= 0 || LongTools.invalidLong(population)) {
                return null;
            } else {
                return population;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public DataNode toNode(GeographyCode code) {
        return GeographyCodeTools.toNode(code);
    }

    public GeographyCode fromNode(DataNode node) {
        return GeographyCodeTools.fromNode(node);
    }

    public String text(GeographyCode code) {
        return valuesText(GeographyCodeTools.toNode(code));
    }

}
