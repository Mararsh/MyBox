/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.dev;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mara.mybox.db.table.TableStringValues;

/**
 *
 * @author mara
 */
public class DevTools {

    public static List<Integer> installedVersion(Connection conn) {
        List<Integer> versions = new ArrayList<>();
        try {
            List<String> installed = TableStringValues.read(conn, "InstalledVersions");
            for (String v : installed) {
                versions.add(myboxVersion(v));
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
        return versions;
    }

    public static int lastVersion(Connection conn) {
        try {
            List<Integer> versions = installedVersion(conn);
            if (!versions.isEmpty()) {
                Collections.sort(versions);
                return versions.get(versions.size() - 1);
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
        return 0;
    }

    public static int myboxVersion(String string) {
        try {
            String[] vs = string.split("\\.");
            switch (vs.length) {
                case 1:
                    return Integer.parseInt(vs[0]) * 1000000;
                case 2:
                    return Integer.parseInt(vs[0]) * 1000000 + Integer.parseInt(vs[1]) * 1000;
                case 3:
                    return Integer.parseInt(vs[0]) * 1000000 + Integer.parseInt(vs[1]) * 1000 + Integer.parseInt(vs[2]);
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
        return 0;
    }

    public static String myboxVersion(int i) {
        try {
            int v1 = i / 1000000;
            int ii = i % 1000000;
            int v2 = ii / 1000;
            int v3 = ii % 1000;
            if (v3 == 0) {
                return v1 + "." + v2;
            } else {
                return v1 + "." + v2 + "." + v3;
            }
        } catch (Exception e) {
//            MyBoxLog.debug(e);
        }
        return i + "";
    }

    public static String getFileName() {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[1].getFileName();
    }

    public static String getMethodName() {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[1].getMethodName();
    }

    public static String getClassName() {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[1].getClassName();
    }

    public static int getLineNumber() {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[1].getLineNumber();
    }

}
