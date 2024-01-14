package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegProbeMediaInformationController extends ControlFFmpegOptions {

    protected String media;

    @FXML
    protected Tab formatTab, videoTab, audioTab, streamsTab, subtitlesTab,
            framesTab, packetsTab, pixelFormatsTab;
    @FXML
    protected TextArea formatArea, streamsArea, queryArea;
    @FXML
    protected TextField queryInput;
    @FXML
    protected ComboBox<String> querySelector;
    @FXML
    protected ToggleGroup outputGroup;

    public FFmpegProbeMediaInformationController() {
        baseTitle = Languages.message("FFmpegProbeMediaInformation");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            executableName = "FFprobeExecutable";
            executableDefault = "win".equals(SystemTools.os()) ? "D:\\Programs\\ffmpeg\\bin\\ffprobe.exe" : "/home/ffprobe";

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            formatArea.setStyle("-fx-font-family: monospace");
            streamsArea.setStyle("-fx-font-family: monospace");
            queryArea.setStyle("-fx-font-family: monospace");

            boolean isChinese = Languages.isChinese();
            querySelector.getItems().addAll(Arrays.asList(
                    "-h      // " + (isChinese ? "显示帮助" : "show helps"),
                    "-show_packets -select_streams v:0  -read_intervals 3:51%+15       // "
                    + (isChinese ? "选择第一个视频流，从3分51秒起的15秒" : "15 seconds from 3:51"),
                    "-show_packets -select_streams v:0  -read_intervals 3:51%+#8      // "
                    + (isChinese ? "选择第一个视频流，从3分51秒起的第8个包" : "8 packets from 3:51"),
                    "-show_packets  -select_streams v:0  -read_intervals 3:51%4:29      // "
                    + (isChinese ? "选择第一个视频流，从3分51秒到4分29秒" : "from 3:51 to 4:29"),
                    "-show_packets  -select_streams v:0  -read_intervals %+15      // "
                    + (isChinese ? "选择第一个视频流，从开始起的15秒" : "15 seconds from start"),
                    "-show_packets  -select_streams v:0  -read_intervals %+#8      // "
                    + (isChinese ? "选择第一个视频流，从开始的第8个包" : "8 packets from start"),
                    "-show_packets  -select_streams v:0  -read_intervals %2:30      // "
                    + (isChinese ? "选择第一个视频流，从开始到2分30秒" : "from start to 2:30"),
                    "-show_packets  -select_streams v:0  -read_intervals 1:36:10%      // "
                    + (isChinese ? "选择第一个视频流，从1小时36分10秒到结束" : "from 1:36:10 to end"),
                    "-show_packets  -select_streams v:0  -read_intervals 03:51%04:51,%+15,%02:30      // "
                    + (isChinese ? "选择第一个视频流，多个间隔" : "multiple intervals"),
                    "-show_packets -select_streams a:0  -read_intervals 3:51%+15      // "
                    + (isChinese ? "选择第一个音频流，从3分51秒起的15秒" : "15 seconds from 3:51"),
                    "-show_packets -select_streams a:0  -read_intervals 3:51%+#8      // "
                    + (isChinese ? "选择第一个音频流，从3分51秒起的第8个包" : "8 packets from 3:51"),
                    "-show_packets  -select_streams a:0  -read_intervals 3:51%4:29      // "
                    + (isChinese ? "选择第一个音频流，从3分51秒到4分29秒" : "from 3:51 to 4:29"),
                    "-show_packets  -select_streams a:0  -read_intervals %+15      // "
                    + (isChinese ? "选择第一个音频流，从开始起的15秒" : "15 seconds from start"),
                    "-show_packets  -select_streams a:0  -read_intervals %+#8      // "
                    + (isChinese ? "选择第一个音频流，从开始的第8个包" : "8 packets from start"),
                    "-show_packets  -select_streams a:0  -read_intervals %2:30      // "
                    + (isChinese ? "选择第一个音频流，从开始到2分30秒" : "from start to 2:30"),
                    "-show_packets  -select_streams a:0  -read_intervals 1:36:10%      // "
                    + (isChinese ? "选择第一个音频流，从1小时36分10秒到结束" : "from 1:36:10 to end"),
                    "-show_packets  -select_streams a:0  -read_intervals 03:51%04:51,%+15,%02:30      // "
                    + (isChinese ? "选择第一个音频流，多个间隔" : "multiple intervals")
            ));
            querySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    queryInput.setText(newValue.substring(0, newValue.indexOf("      ")));
                }
            });
            querySelector.getSelectionModel().select(0);

            outputGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    startAction();
                }
            });

            functionBox.disableProperty().bind(executableInput.styleProperty().isEqualTo(UserConfig.badStyle()));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void checkSourceFileInput() {
        String v = sourceFileInput.getText();
        if (v == null || v.isEmpty()) {
            sourceFileInput.setStyle(UserConfig.badStyle());
            return;
        }
        sourceFileInput.setStyle(null);
        final File file = new File(v);
        if (file.exists()) {
            sourceFile = file;
            recordFileOpened(file);
        } else {
            VisitHistoryTools.visitStreamMedia(v);
        }
    }

    protected void mediaSelected(String name) {
        sourceFileInput.setText(name);
        File file = new File(name);
        if (file.exists()) {
            sourceFile = file;
            recordFileOpened(file);
        } else {
            sourceFile = null;
            VisitHistoryTools.visitStreamMedia(name);
        }
    }

    @FXML
    public void showMediaMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event, false) {
            @Override
            public List<VisitHistory> recentFiles() {
                List<VisitHistory> recent = recentOpenedFiles();
                List<VisitHistory> recentMedia = VisitHistoryTools.getRecentStreamMedia();
                recent.addAll(recentMedia);
                return recent;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return recentSourcePathsBesidesFiles();
            }

            @Override
            public void handleSelect() {
                selectSourceFile();
            }

            @Override
            public void handleFile(String fname) {
                mediaSelected(fname);
            }

            @Override
            public void handlePath(String fname) {
                handleSourcePath(fname);
            }

            @Override
            public void pop() {
                if (controller == null || event == null) {
                    return;
                }
                List<MenuItem> items = new ArrayList<>();

                MenuItem menu = new MenuItem(Languages.message("Select..."));
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        handleSelect();
                    }
                });
                items.add(menu);

                List<VisitHistory> his = recentFiles();
                if (his != null && !his.isEmpty()) {
                    items.add(new SeparatorMenuItem());
                    menu = new MenuItem(Languages.message("RecentAccessedFiles"));
                    menu.setStyle("-fx-text-fill: #2e598a;");
                    items.add(menu);
                    for (VisitHistory h : his) {
                        final String fname = h.getResourceValue();
                        menu = new MenuItem(fname);
                        menu.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                mediaSelected(fname);
                            }
                        });
                        items.add(menu);
                    }
                }

                menu = new MenuItem(Languages.message("Examples"));
                menu.setStyle("-fx-text-fill: #2e598a;");
                items.add(menu);
                menu = new MenuItem("http://download.oracle.com/otndocs/products/javafx/JavaRap/prog_index.m3u8");
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        mediaSelected("http://download.oracle.com/otndocs/products/javafx/JavaRap/prog_index.m3u8");
                    }
                });
                items.add(menu);
                menu = new MenuItem("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
                menu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        mediaSelected("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8");
                    }
                });
                items.add(menu);

                List<String> paths = paths();
                if (paths != null && !paths.isEmpty()) {
                    items.add(new SeparatorMenuItem());
                    menu = new MenuItem(Languages.message("RecentAccessedDirectories"));
                    menu.setStyle("-fx-text-fill: #2e598a;");
                    items.add(menu);
                    for (String path : paths) {
                        menu = new MenuItem(path);
                        menu.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                handlePath(path);
                            }
                        });
                        items.add(menu);
                    }
                }

                items.add(new SeparatorMenuItem());

                controller.popEventMenu(event, items);

            }

        }.pop();
    }

    @FXML
    public void pickMedia(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)
                || AppVariables.fileRecentNumber <= 0) {
            selectSourceFile();
        } else {
            showMediaMenu(event);
        }
    }

    @FXML
    public void popMedia(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)) {
            showMediaMenu(event);
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (executable == null) {
            return;
        }
        media = sourceFileInput.getText();
        if (media == null || media.isEmpty()) {
            sourceFileInput.setStyle(UserConfig.badStyle());
            popError(message("InvaidParameter") + ": " + message("Media"));
            return;
        }
        if (task != null) {
            task.cancel();
        }
        formatArea.clear();
        streamsArea.clear();
        queryArea.clear();
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    error = null;
                    List<String> parameters = new ArrayList<>();
                    parameters.add("-show_format");
                    String fs = SystemTools.run(makeCommand(parameters));
                    Platform.runLater(() -> {
                        formatArea.setText(fs);
                    });

                    parameters.clear();
                    parameters.add("-v");
                    parameters.add("panic");
                    parameters.add("-show_streams");
                    String ss = SystemTools.run(makeCommand(parameters));
                    Platform.runLater(() -> {
                        streamsArea.setText(ss);
                    });

                    parameters.clear();
                    parameters.add("-h");
                    String h = SystemTools.run(makeCommand(parameters));
                    Platform.runLater(() -> {
                        queryArea.setText(h);
                    });

                } catch (Exception e) {
                    error = e.toString();
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
            }
        };
        start(task);
    }

    protected List<String> makeCommand(List<String> parameters) {
        try {
            List<String> command = new ArrayList<>();
            command.add(executable.getAbsolutePath());
            command.add("-hide_banner");
            command.addAll(parameters);
            String format = ((RadioButton) outputGroup.getSelectedToggle()).getText();
            if (!message("Default").equals(format)) {
                command.add("-print_format");
                command.add(format);
            }
            command.add(media);
            return command;
        } catch (Exception e) {
            error = e.toString();
            return null;
        }
    }

    @FXML
    @Override
    public void goAction() {
        if (executable == null || media == null) {
            return;
        }
        String[] args = StringTools.splitBySpace(queryInput.getText());
        if (args.length == 0) {
            popError(message("InvaidParameter") + ": " + message("Command"));
            return;
        }
        if (queryTask != null) {
            queryTask.cancel();
        }
        queryTask = new FxTask<Void>(this) {

            private File file;

            @Override
            protected boolean handle() {
                try {
                    List<String> parameters = new ArrayList<>();
                    parameters.add("-v");
                    parameters.add("panic");
                    parameters.addAll(Arrays.asList(args));
                    file = FileTmpTools.getTempFile(".txt");
                    ProcessBuilder pb = new ProcessBuilder(makeCommand(parameters)).redirectErrorStream(true);
                    Process process = pb.start();
                    try (BufferedReader inReader = process.inputReader(Charset.defaultCharset());
                            BufferedWriter writer = new BufferedWriter(new FileWriter(file, Charset.forName("UTF-8"), false))) {
                        String line;
                        while ((line = inReader.readLine()) != null) {
                            writer.write(line + "\n");
                            if (isCancelled()) {
                                process.destroyForcibly();
                            }
                        }
                        writer.flush();
                    }
                    process.waitFor();
                    return file != null && file.exists();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                TextEditorController.open(file);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                queryTask = null;
            }
        };
        start(queryTask);
    }

}
