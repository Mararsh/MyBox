/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import mara.mybox.controller.base.BaseController;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.util.Properties;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.value.AppVaribles;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class JvmPropertiesController extends BaseController {

    @FXML
    private TextField label0, label1, label2, label3, label4, label5, label6, label7, label8, label9,
            label10, label11, label12, label13, label14, label15, label16, label17, label18, label19,
            label20, label21, label22;

    public JvmPropertiesController() {
        baseTitle = AppVaribles.getMessage("JvmProperties");

    }

    @Override
    public void initializeNext() {
        try {
            Properties p = System.getProperties();
            label0.setText(System.getProperty("java.version"));
            label1.setText(System.getProperty("java.vendor"));
            label2.setText(System.getProperty("java.vm.name"));
            label3.setText(System.getProperty("java.vm.info"));
            label4.setText(System.getProperty("java.home"));
            label5.setText(System.getProperty("java.io.tmpdir"));
            label6.setText(System.getProperty("javafx.runtime.version"));
            label7.setText(System.getProperty("sun.jnu.encoding"));
            label8.setText(System.getProperty("sun.io.unicode.encoding"));
            label9.setText(System.getProperty("sun.cpu.endian"));
            label10.setText(System.getProperty("sun.desktop"));
            label11.setText(System.getProperty("file.encoding"));
            label12.setText(System.getProperty("file.separator"));
            label13.setText(System.getProperty("user.name"));
            label14.setText(System.getProperty("user.home"));
            label15.setText(System.getProperty("user.dir"));
            label16.setText(System.getProperty("user.country"));
            label17.setText(System.getProperty("user.language"));
            label18.setText(System.getProperty("os.name"));
            label19.setText(System.getProperty("os.version"));
            label20.setText(System.getProperty("os.arch"));

            int mb = 1024 * 1024;
            OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            label21.setText(osmxb.getTotalPhysicalMemorySize() / mb + "MB");
            Runtime r = Runtime.getRuntime();
            label22.setText(r.maxMemory() / mb + "MB");
        } catch (Exception e) {

        }
    }

}
