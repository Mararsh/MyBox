package mara.mybox.data2d.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseData2DLoadController;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileText;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-15
 * @License Apache License Version 2.0
 */
public class Data2DExampleTools {

    public static List<MenuItem> examplesMenu(BaseData2DLoadController controller) {
        return examplesMenu(controller, Languages.embedFileLang());
    }

    public static List<MenuItem> examplesMenu(BaseData2DLoadController controller, String lang) {
        try {
            List<MenuItem> items = new ArrayList<>();

            items.add(MyData.menu(lang, controller));

            items.add(StatisticDataOfChina.menu(lang, controller));

            items.add(Regression.menu(lang, controller));

            items.add(Location.menu(lang, controller));

            items.add(ChineseCharacters.menu(lang, controller));

            items.add(Matrix.menu(lang, controller));

            items.add(ProjectManagement.menu(lang, controller));

            items.add(SoftwareTesting.menu(lang, controller));

            items.add(new SeparatorMenuItem());

            CheckMenuItem onlyMenu = new CheckMenuItem(message(lang, "ImportDefinitionOnly"),
                    StyleTools.getIconImageView("iconHeader.png"));
            onlyMenu.setSelected(UserConfig.getBoolean("Data2DExampleImportDefinitionOnly", false));
            onlyMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DExampleImportDefinitionOnly", onlyMenu.isSelected());
                }
            });
            items.add(onlyMenu);

            return items;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static boolean makeExampleFile(String fileName, DataFileCSV targetData) {
        try {
            if (fileName == null || targetData == null) {
                return false;
            }
            File srcFile = FxFileTools.getInternalFile("/data/examples/" + fileName + ".csv");
            File targetFile = targetData.tmpFile(fileName, "example", "csv");
            if (targetFile.exists()) {
                targetFile.delete();
            }
            Charset charset = Charset.forName("utf-8");
            try (BufferedReader reader = new BufferedReader(new FileReader(srcFile, charset));
                    BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, charset, false))) {
                String line = null;
                while ((line = reader.readLine()) != null && line.startsWith(DataFileText.CommentsMarker)) {
                    writer.write(line + System.lineSeparator());
                }
                if (line != null) {
                    List<Data2DColumn> columns = targetData.getColumns();
                    if (columns != null) {
                        String header = null;
                        for (Data2DColumn column : columns) {
                            String name = "\"" + column.getColumnName() + "\"";
                            if (header == null) {
                                header = name;
                            } else {
                                header += "," + name;
                            }
                        }
                        writer.write(header + System.lineSeparator());
                    } else {
                        writer.write(line + System.lineSeparator());
                    }
                    if (!UserConfig.getBoolean("Data2DExampleImportDefinitionOnly", false)) {
                        while ((line = reader.readLine()) != null) {
                            writer.write(line + System.lineSeparator());
                        }
                    }
                }
                writer.flush();
            } catch (Exception e) {
                MyBoxLog.error(e);
                return false;
            }
            targetData.setFile(targetFile).setHasHeader(true).setCharset(charset).setDelimiter(",");
            targetData.saveAttributes();
            FileDeleteTools.delete(srcFile);
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

}
