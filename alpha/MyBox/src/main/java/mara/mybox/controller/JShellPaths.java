package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import jdk.jshell.JShell;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.LocateTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.RecentVisitMenu;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-5-26
 * @License Apache License Version 2.0
 */
public class JShellPaths extends BaseController {

    protected JShellController jShellController;

    @FXML
    protected TextArea pathsArea;
    @FXML
    protected TextField pathInput;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Jar);
    }

    protected void setParameters(JShellController jShellController) {
        this.jShellController = jShellController;
    }

    public JShell jShell() {
        return jShellController.editorController.jShell;
    }

    protected void resetPaths() {
        try {
            String paths = jShellController.editorController.expValue("System.getProperty(\"java.class.path\")");
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
        jShell().addToClasspath(path);
        String text = pathsArea.getText();
        if (!text.isBlank() && !text.endsWith(";\n")) {
            path = ";" + path;
        }
        path = path.replace(";", ";\n");
        pathsArea.appendText(path);
        TableStringValues.add("JarPathHistories", path);
    }

    @FXML
    protected void popHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, pathInput, mouseEvent, "JarPathHistories");
    }

    @FXML
    protected void selectJar() {
        try {
            File file = FxFileTools.selectFile(this, VisitHistory.FileType.Jar);
            if (file == null) {
                return;
            }
            selectJar(file);
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    protected void selectJar(File file) {
        pathInput.setText(file.getAbsolutePath());
    }

    @FXML
    public void popJarFiles(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return VisitHistoryTools.getRecentReadWrite(VisitHistory.FileType.Jar,
                        AppVariables.fileRecentNumber * 3 / 4);
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistoryTools.getRecentPath(VisitHistory.FileType.Jar,
                        AppVariables.fileRecentNumber / 4 + 1);
            }

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
                recordFileOpened(file);
                selectJar(file);
            }

        }.pop();
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
            MyBoxLog.error(e.toString());
        }
    }

    public void selectPath(File directory) {
        if (pathInput != null) {
            pathInput.setText(directory.getPath());
        }
        recordFileOpened(directory);
    }

    @FXML
    public void popJarPath(MouseEvent event) {
        if (AppVariables.fileRecentNumber <= 0) {
            return;
        }
        new RecentVisitMenu(this, event) {
            @Override
            public List<VisitHistory> recentFiles() {
                return null;
            }

            @Override
            public List<VisitHistory> recentPaths() {
                return VisitHistoryTools.getRecentPath(VisitHistory.FileType.Jar);
            }

            @Override
            public void handleSelect() {
                selectPath();
            }

            @Override
            public void handleFile(String fname) {
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
    protected void popExamplesMenu(MouseEvent mouseEvent) {
        try {
            if (popMenu != null && popMenu.isShowing()) {
                popMenu.hide();
            }
            popMenu = new ContextMenu();
            popMenu.setAutoHide(true);

            MenuItem menu;

            menu = new MenuItem(message("MyBoxClassPaths"));
            menu.setOnAction((ActionEvent event) -> {
                pathInput.setText(System.getProperty("java.class.path"));
            });
            popMenu.getItems().add(menu);

            popMenu.getItems().add(new SeparatorMenuItem());
            menu = new MenuItem(message("PopupClose"), StyleTools.getIconImage("iconCancel.png"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    popMenu.hide();
                }
            });
            popMenu.getItems().add(menu);

            LocateTools.locateBelow((Region) mouseEvent.getSource(), popMenu);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
