package mara.mybox.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import mara.mybox.data.GeographyCode;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableGeographyCode;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2020-03-28
 * @License Apache License Version 2.0
 */
public class GeographyCodeImportExternalCSVController extends DataImportController {

    public GeographyCodeImportExternalCSVController() {
        baseTitle = AppVariables.message("ImportGeographyCodeExternalCSVFormat");

    }

    // level,longitude,latitude,chinese_name,english_name,code1,code2,code3,code4,code5,alias1,alias2,alias3,alias4,alias5,
    // area,population,continent,country,province,city,county,town,village,building,comments
    @Override
    public long importFile(File file) {
        long importCount = 0, insertCount = 0, updateCount = 0, skipCount = 0, failedCount = 0;
        try ( CSVParser parser = CSVParser.parse(file, StandardCharsets.UTF_8,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',').withTrim().withNullString(""))) {
            List<String> names = parser.getHeaderNames();
            if ((!names.contains("Level") && !names.contains(message("en", "Level")) && !names.contains(message("zh", "Level")))
                    || (!names.contains("Country") && !names.contains(message("en", "Country")) && !names.contains(message("zh", "Country")))) {
                updateLogs(message("InvalidFormat"), true);
                return -1;
            }
            try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                     PreparedStatement insert = conn.prepareStatement(TableGeographyCode.Insert);
                     PreparedStatement update = conn.prepareStatement(TableGeographyCode.Update)) {
                conn.setAutoCommit(false);
                for (CSVRecord record : parser) {
                    if (task == null || task.isCancelled()) {
                        updateLogs("Canceled", true);
                        return importCount;
                    }
                    GeographyCode code = GeographyCode.readExtenalRecord(conn, names, record);
                    GeographyCode exist = TableGeographyCode.readCode(conn, code, false);
                    if (exist != null) {
                        if (replaceCheck.isSelected()) {
                            code.setGcid(exist.getGcid());
                            if (TableGeographyCode.update(conn, update, code)) {
                                updateCount++;
                                importCount++;
                                if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                                    updateLogs(message("Update") + ": " + updateCount + " "
                                            + code.getLevelCode().getName() + " " + code.getName()
                                            + " " + code.getLongitude() + " " + code.getLatitude(), true);
                                }
                            } else {
                                ++failedCount;
                                updateLogs(message("Failed") + ": " + failedCount + " "
                                        + code.getLevelCode().getName() + " " + code.getName()
                                        + " " + code.getLongitude() + " " + code.getLatitude(), true);
                            }
                        } else {
                            skipCount++;
                            if (verboseCheck.isSelected() || (skipCount % 100 == 0)) {
                                updateLogs(message("Skip") + ": " + skipCount + " "
                                        + code.getLevelCode().getName() + " " + code.getName()
                                        + " " + code.getLongitude() + " " + code.getLatitude(), true);
                            }
                        }
                    } else {
                        if (TableGeographyCode.insert(conn, insert, code)) {
                            insertCount++;
                            importCount++;
                            if (verboseCheck.isSelected() || (importCount % 100 == 0)) {
                                updateLogs(message("Insert") + ": " + insertCount + " "
                                        + code.getLevelCode().getName() + " \"" + code.getName()
                                        + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                            }
                        } else {
                            ++failedCount;
                            updateLogs(message("Failed") + ": " + failedCount + " "
                                    + code.getLevelCode().getName() + " \"" + code.getName()
                                    + "\" " + code.getLongitude() + "," + code.getLatitude(), true);
                        }
                    }
                }
                conn.commit();
            }
        } catch (Exception e) {
            updateLogs(e.toString(), true);
        }
        updateLogs(message("Imported") + ":" + importCount + "  " + file + "\n"
                + message("Insert") + ":" + insertCount + " "
                + message("Update") + ":" + updateCount + " "
                + message("FailedCount") + ":" + failedCount + " "
                + message("Skipped") + ":" + skipCount, true);
        return importCount;
    }

}
