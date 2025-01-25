package mara.mybox.db.migration;

import java.io.File;
import java.sql.Connection;
import mara.mybox.controller.MyBoxLoadingController;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.DevTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppValues;
import mara.mybox.value.AppVariables;
import mara.mybox.value.SystemConfig;

/**
 * @Author Mara
 * @CreateDate 2020-06-09
 * @License Apache License Version 2.0
 */
public class DataMigration {

    public static boolean checkUpdates(MyBoxLoadingController controller, String lang) {
        SystemConfig.setString("CurrentVersion", AppValues.AppVersion);
        controller.info("CurrentVersion: " + AppValues.AppVersion);
        try (Connection conn = DerbyBase.getConnection()) {
            int lastVersion = DevTools.lastVersion(conn);
            int currentVersion = DevTools.myboxVersion(AppValues.AppVersion);
            if (lastVersion != currentVersion
                    || SystemConfig.getBoolean("IsAlpha", false) && !AppValues.Alpha) {
                reloadInternalResources();
            }
            SystemConfig.setBoolean("IsAlpha", AppValues.Alpha);
            if (lastVersion == currentVersion) {
                return true;
            }
            MyBoxLog.info("Last version: " + lastVersion + " " + "Current version: " + currentVersion);
            controller.info("Last version: " + lastVersion + " " + "Current version: " + currentVersion);
            if (lastVersion > 0) {

                DataMigrationBefore65.handleVersions(lastVersion, conn);

                DataMigrationFrom65to67.handleVersions(lastVersion, conn);

                DataMigrationFrom68.handleVersions(controller, lastVersion, conn, lang);

            }
            TableStringValues.add(conn, "InstalledVersions", AppValues.AppVersion);
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
        return true;
    }

    public static void reloadInternalResources() {
        new Thread() {
            @Override
            public void run() {
                try {
                    MyBoxLog.info("Refresh internal resources...");
                    File dir = new File(AppVariables.MyboxDataPath + File.separator + "doc");
                    File[] list = dir.listFiles();
                    if (list != null) {
                        for (File file : list) {
                            if (file.isDirectory()) {
                                continue;
                            }
                            file.delete();
//                            String name = file.getName().toLowerCase();
//                            if (name.contains("mybox") || name.contains("readme")) {
//                                file.delete();
//                            }
                        }
                    }

                    dir = new File(AppVariables.MyboxDataPath + File.separator + "image");
                    list = dir.listFiles();
                    if (list != null) {
                        for (File file : list) {
                            if (file.isDirectory()) {
                                continue;
                            }
                            file.delete();
//                            String name = file.getName();
//                            if (name.startsWith("icon") && name.endsWith(".png")) {
//                                file.delete();
//                            }
                        }
                    }

                    dir = new File(AppVariables.MyboxDataPath + File.separator + "buttons");
                    list = dir.listFiles();
                    if (list != null) {
                        for (File file : list) {
                            if (file.isDirectory()) {
                                continue;
                            }
                            file.delete();
//                            String name = file.getName();
//                            if (name.startsWith("icon") && name.endsWith(".png")) {
//                                file.delete();
//                            }
                        }
                    }

                    dir = new File(AppVariables.MyboxDataPath + File.separator + "data");
                    list = dir.listFiles();
                    if (list != null) {
                        for (File file : list) {
                            if (file.isDirectory()) {
                                continue;
                            }
                            file.delete();
//                            String name = file.getName();
//                            if (name.endsWith("_Examples_en.txt") && name.endsWith("_Examples_zh.txt")) {
//                                file.delete();
//                            }
                        }
                    }

                    dir = new File(AppVariables.MyboxDataPath + File.separator + "js");
                    list = dir.listFiles();
                    if (list != null) {
                        for (File file : list) {
                            if (file.isDirectory()) {
                                continue;
                            }
                            file.delete();
                        }
                    }

                    MyBoxLog.info("Internal resources refreshed.");

                } catch (Exception e) {
                    MyBoxLog.console(e.toString());
                }
            }
        }.start();
    }

    public static void alterColumnLength(Connection conn, String tableName, String colName, int length) {
        String sql = "ALTER TABLE " + tableName + "  alter  column  " + colName + " set data type VARCHAR(" + length + ")";
        DerbyBase.update(conn, sql);
    }

}
