/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @Author mara
 * @CreateDate 2018-6-2 11:01:45
 *
 * @Description
 */
public class FileTools {

    private static final Logger logger = LogManager.getLogger();

    // Solution from https://stackoverflow.com/questions/941754/how-to-get-a-path-to-a-resource-in-a-java-jar-file
    public static File getResourceFile(Class someClass, String resourceFile) {
        if (someClass == null || resourceFile == null) {
            return null;
        }

        File file = null;
        URL url = someClass.getResource(resourceFile);
        if (url.toString().startsWith("jar:")) {
            try {
                InputStream input = someClass.getResourceAsStream(resourceFile);
                file = File.createTempFile("MyBox", "." + getFileSuffix(resourceFile));
                OutputStream out = new FileOutputStream(file);
                int read;
                byte[] bytes = new byte[1024];
                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
                file.deleteOnExit();
            } catch (Exception e) {
                logger.error(e.toString());
            }
        } else {
            //this will probably work in your IDE, but not from a JAR
            file = new File(someClass.getResource(resourceFile).getFile());
        }
        return file;
    }

    public static String getFilePath(String filename) {
        if (filename == null) {
            return null;
        }
        int pos = filename.lastIndexOf("/");
        if (pos < 0) {
            return "";
        }
        return filename.substring(0, pos);
    }

    public static String getFileName(String filename) {
        if (filename == null) {
            return null;
        }
        String fname = filename;
        int pos = filename.lastIndexOf("/");
        if (pos >= 0) {
            fname = fname.substring(pos + 1);
        }
        return fname;
    }

    public static String getFilePrefix(String filename) {
        String fname = getFileName(filename);
        if (fname == null) {
            return null;
        }
        int pos = fname.lastIndexOf(".");
        if (pos >= 0) {
            fname = fname.substring(0, pos);
        }
        return fname;
    }

    public static String getFileSuffix(String filename) {
        if (filename == null) {
            return null;
        }
        String fname = filename;
        int pos = filename.lastIndexOf(".");
        if (pos >= 0) {
            fname = fname.substring(pos + 1);
        }
        return fname;
    }

    public static boolean isPDF(String filename) {
        String suffix = getFileSuffix(filename);
        if (suffix == null) {
            return false;
        }
        return "PDF".equals(suffix.toUpperCase());
    }

    public static String insertFileName(String filename, String inStr) {
        if (filename == null) {
            return null;
        }
        if (inStr == null) {
            return filename;
        }
        int pos = filename.lastIndexOf(".");
        if (pos < 0) {
            return filename + inStr;
        }
        return filename.substring(0, pos) + inStr + "." + filename.substring(pos + 1);
    }
}
