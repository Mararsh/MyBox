package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.value.Languages;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-03-28
 * @License Apache License Version 2.0
 */
public class GeographyCodeImportExternalCSVController extends BaseImportCsvController<GeographyCode> {

    public GeographyCodeImportExternalCSVController() {
        baseTitle = Languages.message("ImportGeographyCodeExternalCSVFormat");
    }

    @Override
    public BaseTable getTableDefinition() {
        if (tableDefinition == null) {
            tableDefinition = new TableGeographyCode();
        }
        return tableDefinition;
    }

    @Override
    protected boolean validHeader(List<String> names) {
        boolean invalid = !names.contains("chinese_name")
                && !names.contains(Languages.message("en", "ChineseName"))
                && !names.contains(Languages.message("zh", "ChineseName"))
                && !names.contains("english_name")
                && !names.contains(Languages.message("en", "EnglishName"))
                && !names.contains(Languages.message("zh", "EnglishName"));
        invalid = invalid || (!names.contains("level")
                && !names.contains(Languages.message("en", "Level"))
                && !names.contains(Languages.message("zh", "Level")));
        if (invalid) {
            updateLogs(Languages.message("InvalidFormat"), true);
            return false;
        }
        return true;
    }

    @Override
    protected String insertStatement() {
        return TableGeographyCode.Insert;
    }

    @Override
    protected String updateStatement() {
        return TableGeographyCode.Update;
    }

    @Override
    protected GeographyCode readRecord(Connection conn, List<String> names, CSVRecord record) {
        return GeographyCodeTools.readExtenalRecord(conn, names, record);
    }

    @Override
    protected GeographyCode readData(Connection conn, GeographyCode data) {
        return TableGeographyCode.readCode(conn, data, false);
    }

    @Override
    protected String dataValues(GeographyCode data) {
        return data.getLevelCode().getName() + " \"" + data.getName()
                + "\" " + data.getLongitude() + "," + data.getLatitude();
    }

    @Override
    protected boolean update(Connection conn, PreparedStatement update, GeographyCode data) {
        return TableGeographyCode.update(conn, update, data);
    }

    @Override
    protected boolean insert(Connection conn, PreparedStatement insertStatement, GeographyCode data) {
        return TableGeographyCode.insert(conn, insertStatement, data);
    }

}
