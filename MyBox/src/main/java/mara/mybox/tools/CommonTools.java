/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mara.mybox.dev.MyBoxLog;

/**
 *
 * @author mara
 */
public class CommonTools {

    public static String run(String cmd) {
        try {
            if (cmd == null || cmd.isBlank()) {
                return null;
            }
            List<String> p = new ArrayList<>();
            p.addAll(Arrays.asList(StringTools.splitBySpace(cmd)));
            ProcessBuilder pb = new ProcessBuilder(p).redirectErrorStream(true);
            final Process process = pb.start();
            StringBuilder s = new StringBuilder();
            try ( BufferedReader inReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = inReader.readLine()) != null) {
                    s.append(line).append("\n");
                }
            }
            process.waitFor();
            return s.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }

    // https://blog.csdn.net/qq_33314107/article/details/80271963
    public static Object copy(Object source) {
        try {
            Object target;
            PipedOutputStream out = new PipedOutputStream();
            PipedInputStream in = new PipedInputStream();
            try {
                in.connect(out);
            } catch (IOException e) {
                MyBoxLog.debug(e.toString());
                return null;
            }
            try ( ObjectOutputStream bo = new ObjectOutputStream(out);
                     ObjectInputStream bi = new ObjectInputStream(in);) {
                bo.writeObject(source);
                target = bi.readObject();
            } catch (Exception e) {
                MyBoxLog.debug(e.toString());
                return null;
            }
            return target;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

}
