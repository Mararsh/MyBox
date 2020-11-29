/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.tools;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import mara.mybox.dev.MyBoxLog;

/**
 *
 * @author mara
 */
public class CommonTools {

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
