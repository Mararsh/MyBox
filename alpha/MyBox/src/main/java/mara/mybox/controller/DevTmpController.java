package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2024-12-3
 * @License Apache License Version 2.0
 */
public class DevTmpController extends BaseTaskController {

    @Override
    public void initControls() {
        try {
            super.initControls();

//            refineFxmls();
//            refineFxmls2();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void refineFxmls() {
        task = new FxTask<Void>(this) {
            private File targetPath;
            private String name;

            @Override
            protected boolean handle() {
                try {
                    int count = 0;
                    File srcPath = new File("D:\\MyBox\\src\\main\\resources\\fxml\\");
                    targetPath = new File("D:\\tmp\\1\\");
                    updateLogs(srcPath.getAbsolutePath());
                    List<File> fxmls = Arrays.asList(srcPath.listFiles());
                    String scrollImport = "<?import javafx.scene.control.ScrollPane?>";
                    String controlImport = "<?import javafx.scene.control.*?>";
                    String newControlPrefx = "<ScrollPane fitToHeight=\"true\" fitToWidth=\"true\" "
                            + "maxHeight=\"1.7976931348623157E308\" maxWidth=\"1.7976931348623157E308\" "
                            + "pannable=\"true\" xmlns=\"http://javafx.com/javafx/23.0.1\" "
                            + "xmlns:fx=\"http://javafx.com/fxml/1\" ";
                    String newSuffix = "\n   </content>\n</ScrollPane>\n";
                    Charset utf8 = Charset.forName("UTF-8");
                    for (File fxml : fxmls) {
                        name = fxml.getName();
                        String texts = TextFileTools.readTexts(this, fxml, utf8);
                        int menuPos = texts.indexOf("<fx:include fx:id=\"mainMenu\" source=\"MainMenu.fxml\"");
                        if (menuPos < 0) {
//                            MyBoxLog.console("No menu: " + fxml);
                            continue;
                        }
                        int startPos = texts.indexOf("<BorderPane fx:id=\"thisPane\"");
                        if (startPos < 0) {
                            startPos = texts.indexOf("<VBox fx:id=\"thisPane\"");
                            if (startPos < 0) {
                                startPos = texts.indexOf("<StackPane fx:id=\"thisPane\"");
                                if (startPos < 0) {
                                    MyBoxLog.console(fxml);
                                    continue;
                                }
                            }
                        }
                        int nsPos = texts.indexOf("xmlns=", startPos);
                        if (nsPos < 0) {
                            MyBoxLog.console(fxml);
                            continue;
                        }
                        int controlPos = texts.indexOf("fx:controller=", startPos);
                        if (nsPos < 0) {
                            MyBoxLog.console(fxml);
                            continue;
                        }
                        int endPos = texts.indexOf(">", startPos);
                        if (endPos < 0) {
                            MyBoxLog.console(fxml);
                            continue;
                        }

                        String newFxml = texts.substring(0, startPos);
                        if (!texts.contains(scrollImport) && !texts.contains(controlImport)) {
                            newFxml += "\n" + scrollImport + "\n";
//                            MyBoxLog.console("no import:" + fxml);
                        }
                        newFxml += newControlPrefx
                                + texts.substring(controlPos, endPos)
                                + ">\n   <content>\n"
                                + texts.substring(startPos, nsPos) + ">"
                                + texts.substring(endPos + 1)
                                + newSuffix;
                        File file = new File(targetPath + File.separator + name);
                        TextFileTools.writeFile(file, newFxml, utf8);
                        updateLogs(++count + ": " + file);
                    }
                    return true;
                } catch (Exception e) {
                    error = name + "  -  " + e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                browse(targetPath);
            }

        };
        start(task);
    }

    public void refineFxmls2() {
        task = new FxTask<Void>(this) {
            private File targetPath;
            private String name;

            @Override
            protected boolean handle() {
                try {
                    int count = 0;
                    File srcPath = new File("D:\\MyBox\\src\\main\\resources\\fxml\\");
                    targetPath = new File("D:\\tmp\\1\\");
                    updateLogs(srcPath.getAbsolutePath());
                    List<File> fxmls = Arrays.asList(srcPath.listFiles());
                    String scrollPrefix = "<ScrollPane fitToHeight=\"true\" fitToWidth=\"true\" "
                            + "maxHeight=\"1.7976931348623157E308\" maxWidth=\"1.7976931348623157E308\" "
                            + "pannable=\"true\" xmlns=\"http://javafx.com/javafx/23.0.1\" "
                            + "xmlns:fx=\"http://javafx.com/fxml/1\" ";
                    Charset utf8 = Charset.forName("UTF-8");
                    for (File fxml : fxmls) {
                        name = fxml.getName();
                        String texts = TextFileTools.readTexts(this, fxml, utf8);
                        int scrollStart = texts.indexOf(scrollPrefix);
                        if (scrollStart < 0) {
//                            MyBoxLog.console("No menu: " + fxml);
                            continue;
                        }
                        int scrollEnd = texts.indexOf(">", scrollStart);
                        String newFxml = texts.substring(0, scrollEnd)
                                .replaceFirst("ScrollPane fitToHeight",
                                        "ScrollPane  fx:id=\"thisPane\"  prefHeight=\"700.0\" prefWidth=\"1000.0\" fitToHeight");
                        int startPane = texts.indexOf("<BorderPane fx:id=\"thisPane\"", scrollEnd);
                        if (startPane > 0) {
                            newFxml += ">\n   <content>\n<BorderPane maxHeight=\"1.7976931348623157E308\" maxWidth=\"1.7976931348623157E308\"";
                        } else {
                            startPane = texts.indexOf("<StackPane fx:id=\"thisPane\"", scrollEnd);
                            if (startPane > 0) {
                                newFxml += ">\n   <content>\n<StackPane maxHeight=\"1.7976931348623157E308\" maxWidth=\"1.7976931348623157E308\"";
                            } else {
                                MyBoxLog.console(fxml);
                                continue;
                            }
                        }
                        int endPane = texts.indexOf(">", startPane);
                        if (endPane < 0) {
                            MyBoxLog.console(fxml);
                            continue;
                        }
                        newFxml += texts.substring(endPane);
                        File file = new File(targetPath + File.separator + name);
                        TextFileTools.writeFile(file, newFxml, utf8);
                        updateLogs(++count + ": " + file);
                    }
                    return true;
                } catch (Exception e) {
                    error = name + "  -  " + e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                browse(targetPath);
            }

        };
        start(task);
    }

    /*
        static
     */
    public static DevTmpController open() {
        try {
            DevTmpController controller = (DevTmpController) WindowTools.openStage(Fxmls.DevTmpFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
