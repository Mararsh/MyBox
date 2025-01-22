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
        addColumn(new ColumnDefinition("chinese_name", ColumnType.String));
        addColumn(new ColumnDefinition("english_name", ColumnType.String));
        addColumn(new ColumnDefinition("level", ColumnType.EnumeratedShort)
                .setFormat(GeographyCodeTools.addressLevelMessageNames()));
        addColumn(new ColumnDefinition("coordinate_system", ColumnType.EnumeratedShort)
                .setFormat(GeographyCodeTools.coordinateSystemMessageNames()));
        addColumn(new ColumnDefinition("longitude", ColumnType.Longitude));
        addColumn(new ColumnDefinition("latitude", ColumnType.Latitude));
        addColumn(new ColumnDefinition("altitude", ColumnType.Double));
        addColumn(new ColumnDefinition("precision", ColumnType.Double));
        addColumn(new ColumnDefinition("continent", ColumnType.String));
        addColumn(new ColumnDefinition("country", ColumnType.String));
        addColumn(new ColumnDefinition("province", ColumnType.String));
        addColumn(new ColumnDefinition("city", ColumnType.String));
        addColumn(new ColumnDefinition("county", ColumnType.String));
        addColumn(new ColumnDefinition("town", ColumnType.String));
        addColumn(new ColumnDefinition("village", ColumnType.String));
        addColumn(new ColumnDefinition("building", ColumnType.String));
        addColumn(new ColumnDefinition("poi", ColumnType.String));
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
        addColumn(new ColumnDefinition("area", ColumnType.Double).setFormat("GroupInThousands"));
        addColumn(new ColumnDefinition("population", ColumnType.Long).setFormat("GroupInThousands"));
        addColumn(new ColumnDefinition("description", ColumnType.String));
        addColumn(new ColumnDefinition("image", ColumnType.Image));

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
            case "chinese_name":
                return message("ChineseName");
            case "english_name":
                return message("EnglishName");
            case "continent":
                return message("Continent");
            case "country":
                return message("Country");
            case "province":
                return message("Province");
            case "city":
                return message("City");
            case "county":
                return message("County");
            case "town":
                return message("Town");
            case "village":
                return message("Village");
            case "building":
                return message("Building");
            case "poi":
                return message("PointOfInterest");
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
                return message("SquareMeters");
            case "population":
                return message("Population");
            case "description":
                return message("Description");
            case "image":
                return message("Image");
        }
        return super.label(name);
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

    public String html(GeographyCode code) {
        return valuesHtml(GeographyCodeTools.toNode(code));
    }

}
