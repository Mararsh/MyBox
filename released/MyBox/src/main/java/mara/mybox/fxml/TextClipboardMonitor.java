package mara.mybox.fxml;

import java.sql.Connection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import mara.mybox.controller.TextInMyBoxClipboardController;
import mara.mybox.controller.TextInSystemClipboardController;
import mara.mybox.db.table.TableTextClipboard;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2021-8-3
 * @License Apache License Version 2.0
 */
public class TextClipboardMonitor extends Timer {

    public final static int DefaultInterval = 200;
    protected Date startTime = null;
    protected int number;
    protected final TableTextClipboard tableTextClipboard = new TableTextClipboard();
    protected String lastString = "";
    protected Connection conn = null;
    protected TextInSystemClipboardController controller;

    public TextClipboardMonitor start(int inInterval) {
        int interval = TextClipboardTools.setMonitorInterval(inInterval);
        startTime = new Date();
        number = 0;
        schedule(new MonitorTask(), 0, interval);
        Platform.runLater(() -> {
            TextInSystemClipboardController.updateSystemClipboardStatus();
            TextInMyBoxClipboardController.updateMyBoxClipboardStatus();
        });
        MyBoxLog.debug("Text Clipboard Monitor started. Interval:" + interval);
        return this;
    }

    public void stop() {
        cancel();
        Platform.runLater(() -> {
            TextInSystemClipboardController.updateSystemClipboardStatus();
            TextInMyBoxClipboardController.updateMyBoxClipboardStatus();
        });
        MyBoxLog.debug("Text Clipboard Monitor stopped.");
    }

    class MonitorTask extends TimerTask {

        @Override
        public void run() {

            Platform.runLater(new Runnable() {

                @Override
                public synchronized void run() {
                    try {
                        controller = TextInSystemClipboardController.running();
                        if (!TextClipboardTools.isCopy() && controller == null) {
                            TextClipboardTools.stopTextClipboardMonitor();
                            return;
                        }
                        Clipboard clipboard = Clipboard.getSystemClipboard();
                        if (!clipboard.hasString()) {
                            return;
                        }
                        String clip = clipboard.getString();
                        if (clip == null || clip.isEmpty() || clip.equals(lastString)) {
                            return;
                        }
                        lastString = clip;
                        number++;
                        if (TextClipboardTools.isCopy()) {
                            TextClipboardTools.stringToMyBoxClipboard(tableTextClipboard, conn, lastString);
                        }
                        if (controller != null) {
                            controller.loadClip(clip);
                        }
                    } catch (Exception e) {
                        MyBoxLog.debug(e.toString());
                    }
                }
            });
        }
    }


    /*
        get/set
     */
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getLastString() {
        return lastString;
    }

    public void setLastString(String lastString) {
        this.lastString = lastString;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    public TextInSystemClipboardController getController() {
        return controller;
    }

    public void setController(TextInSystemClipboardController controller) {
        this.controller = controller;
    }

}
