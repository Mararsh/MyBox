package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import jdk.jshell.JShell;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.JShellTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-5-26
 * @License Apache License Version 2.0
 */
public class JShellPaths extends BaseController {

    protected JShell jShell;

    @FXML
    protected TextArea pathsArea;
    @FXML
    protected TextField pathInput;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Jar);
    }

    protected void resetPaths(JShell jShell) {
        try {
            this.jShell = jShell;
            pathsArea.clear();
            String paths = JShellTools.classPath(jShell);
            if (paths == null) {
                return;
            }
            pathsArea.setText(paths.replace(";", ";\n"));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    protected void addPath() {
        String path = pathInput.getText();
        if (path == null || path.isBlank()) {
            return;
        }
        path = path.trim();
        jShell.addToClasspath(path);
        String text = pathsArea.getText();
        if (!text.isBlank() && !text.endsWith(";\n")) {
            path = ";" + path;
        }
        path = path.replace(";", ";\n");
        pathsArea.appendText(path);
        TableStringValues.add("JarPathHistories", path);
    }

    @FXML
    protected void showHistories(Event event) {
        PopTools.popSavedValues(this, pathInput, event, "JarPathHistories", false);
    }

    @FXML
    protected void popHistories(Event event) {
        if (UserConfig.getBoolean("JarPathHistoriesPopWhenMouseHovering", false)) {
            showHistories(event);
        }
    }

    @FXML
    protected void selectJar() {
        try {
            File file = FxFileTools.selectFile(this);
            if (file == null) {
                return;
            }
            selectJar(file);
        } catch (Exception e) {
//            MyBoxLog.error(e);
        }
    }

    protected void selectJar(File file) {
        pathInput.setText(file.getAbsolutePath());
        recordFileOpened(file);
    }

    public void showJarFilesMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event, false) {

            @Override
            public void handleSelect() {
                selectJar();
            }

            @Override
            public void handleFile(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectJar();
                    return;
                }
                selectJar(file);
            }

        }.pop();
    }

    @FXML
    public void pickJarFiles(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)
                || AppVariables.fileRecentNumber <= 0) {
            selectJar();
        } else {
            showJarFilesMenu(event);
        }
    }

    @FXML
    public void popJarFiles(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)) {
            showJarFilesMenu(event);
        }
    }

    @FXML
    public void selectPath() {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = UserConfig.getPath(baseName + "SourcePath");
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            selectPath(directory);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void selectPath(File directory) {
        if (pathInput != null) {
            pathInput.setText(directory.getPath());
        }
        recordFileOpened(directory);
    }

    public void showJarPathMenu(Event event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event, true) {

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistoryTools.getRecentPathRead(VisitHistory.FileType.Jar);
            }

            @Override
            public void handleSelect() {
                selectPath();
            }

            @Override
            public void handlePath(String fname) {
                File file = new File(fname);
                if (!file.exists()) {
                    selectPath();
                    return;
                }
                selectPath(file);
            }

        }.pop();
    }

    @FXML
    public void pickJarPath(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)
                || AppVariables.fileRecentNumber <= 0) {
            selectPath();
        } else {
            showJarPathMenu(event);
        }
    }

    @FXML
    public void popJarPath(Event event) {
        if (UserConfig.getBoolean("RecentVisitMenuPopWhenMouseHovering", true)) {
            showJarPathMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event event) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem(message("MyBoxClassPaths"));
            menu.setOnAction((ActionEvent e) -> {
                pathInput.setText(System.getProperty("java.class.path"));
            });
            items.add(menu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            hoverMenu.setSelected(UserConfig.getBoolean("JShellPathsPopWhenMouseHovering", false));
            hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("JShellPathsPopWhenMouseHovering", hoverMenu.isSelected());
                }
            });
            items.add(hoverMenu);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popExamplesMenu(Event event) {
        if (UserConfig.getBoolean("JShellPathsPopWhenMouseHovering", false)) {
            showExamplesMenu(event);
        }
    }

}
