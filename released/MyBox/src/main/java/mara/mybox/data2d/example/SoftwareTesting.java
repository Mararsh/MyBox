package mara.mybox.data2d.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.BaseData2DLoadController;
import mara.mybox.data.FunctionsList;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.DataFileCSV;
import static mara.mybox.data2d.example.Data2DExampleTools.makeExampleFile;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-15
 * @License Apache License Version 2.0
 */
public class SoftwareTesting {

    public static Menu menu(String lang, BaseData2DLoadController controller) {
        try {
            Menu stMenu = new Menu(message(lang, "SoftwareTesting"), StyleTools.getIconImageView("iconVerify.png"));

            MenuItem menu = new MenuItem(message(lang, "TestEnvironment"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = TestEnvironment(lang);
                if (makeExampleFile("ST_TestEnvironment_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            stMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "CompatibilityTesting"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = CompatibilityTesting(lang);
                if (makeExampleFile("ST_CompatibilityTesting_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            stMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "DetailedTesting"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = DetailedTesting(lang);
                if (makeExampleFile("ST_DetailedTesting_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            stMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "MyBoxBaseVerificationList"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = MyBoxBaseVerificationList(controller, lang,
                        UserConfig.getBoolean("Data2DExampleImportDefinitionOnly", false));
                controller.loadDef(data);
            });
            stMenu.getItems().add(menu);

            return stMenu;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        exmaples of data 2D definition
     */
    public static DataFileCSV TestEnvironment(String lang) {
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "Title"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Hardwares"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn("CPU", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Memory"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "OS"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Softwares"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Language"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Target"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(message(lang, "TestEnvironment"));
        return data;
    }

    public static DataFileCSV CompatibilityTesting(String lang) {
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "TestEnvironment"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Item"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "ModifyTime"), ColumnDefinition.ColumnType.Datetime));
        columns.add(new Data2DColumn(message(lang, "Description"), ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(message(lang, "CompatibilityTesting"));
        return data;
    }

    public static DataFileCSV DetailedTesting(String lang) {
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "TestEnvironment"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Version"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Type"), ColumnDefinition.ColumnType.EnumerationEditable)
                .setFormat("\n" + message(lang, "Function") + "\n"
                        + message(lang, "UserInterface") + "\n" + message(lang, "Bundary") + "\n"
                        + message(lang, "Invalid") + "\n"
                        + message(lang, "Data") + "\n" + message(lang, "API") + "\n"
                        + message(lang, "IO") + "\n" + message(lang, "Exception") + "\n"
                        + message(lang, "Performance") + "\n" + message(lang, "Robustness") + "\n"
                        + message(lang, "Usability") + "\n" + message(lang, "Compatibility") + "\n"
                        + message(lang, "Security") + "\n" + message(lang, "Document")));
        columns.add(new Data2DColumn(message(lang, "Object"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Title"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Steps"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Status"), ColumnDefinition.ColumnType.EnumerationEditable)
                .setFormat("\n" + message(lang, "NotTested") + "\n"
                        + message(lang, "Testing") + "\n"
                        + message(lang, "Success") + "\n"
                        + message(lang, "Fail")));
        columns.add(new Data2DColumn(message(lang, "ModifyTime"), ColumnDefinition.ColumnType.Datetime));
        columns.add(new Data2DColumn(message(lang, "Description"), ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(message(lang, "DetailedTesting"));
        return data;
    }

    public static DataFileCSV MyBoxBaseVerificationList(BaseController controller,
            String lang, boolean onlyDef) {
        try {
            boolean isChinese = Languages.isChinese(lang);

            DataFileCSV targetData = new DataFileCSV();
            List<Data2DColumn> columns = new ArrayList<>();
            columns.add(new Data2DColumn(message(lang, "Index"), ColumnDefinition.ColumnType.Integer));
            columns.add(new Data2DColumn(message(lang, "HierarchyNumber"), ColumnDefinition.ColumnType.String));
            for (int i = 1; i <= FunctionsList.MaxLevel; i++) {
                columns.add(new Data2DColumn(message(lang, "Level") + " " + i, ColumnDefinition.ColumnType.String));
            }
            for (Data2DColumn c : columns) {
                c.setEditable(false);
            }
            int preSize = columns.size();
            columns.add(0, new Data2DColumn(message(lang, "TestEnvironment"), ColumnDefinition.ColumnType.String));
            String defauleValue = "";
            String format = defauleValue + "\n" + message(lang, "Success") + "\n" + message(lang, "Fail");
            columns.add(new Data2DColumn(isChinese ? "打开界面" : "Open interface", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "正常值" : "Normal values", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "正常操作" : "Normal operations", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "正常结果" : "Normal results", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "边界值" : "Boundary values", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "异常值" : "Abnormal values", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "异常操作" : "Abnormal operations", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "异常结果" : "Exception results", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "提示" : "Tips", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "按钮" : "Buttons", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "快捷键" : "Shortcuts", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "菜单" : "Menu", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "响应时间" : "Response time", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "内存占用" : "Memory occupied", ColumnDefinition.ColumnType.EnumerationEditable)
                    .setFormat(format));
            columns.add(new Data2DColumn(isChinese ? "检验者" : "Verifier", ColumnDefinition.ColumnType.String));
            columns.add(new Data2DColumn(message(lang, "ModifyTime"), ColumnDefinition.ColumnType.Datetime));
            columns.add(new Data2DColumn(message(lang, "Description"), ColumnDefinition.ColumnType.String));

            String dataName = message(lang, "MyBoxBaseVerificationList") + " - " + DateTools.nowString();
            targetData.setColumns(columns).setDataName(dataName);

            File targetFile = FileTmpTools.generateFile(dataName, "csv");
            if (targetFile.exists()) {
                targetFile.delete();
            }
            Charset charset = Charset.forName("utf-8");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, charset, false))) {
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
                if (!onlyDef) {
                    FunctionsList list = new FunctionsList(controller, false, lang);
                    StringTable table = list.make();
                    if (table == null) {
                        return null;
                    }
                    String values = "";
                    for (int i = 0; i < columns.size() - preSize; i++) {
                        values += "," + defauleValue;
                    }
                    String line;
                    for (List<String> row : table.getData()) {
                        line = "win";
                        for (String v : row) {
                            line += "," + v;
                        }
                        writer.write(line + values + System.lineSeparator());
                    }
                }
                writer.flush();
            } catch (Exception e) {
                MyBoxLog.error(e);
                return null;
            }
            targetData.setFile(targetFile).setHasHeader(true).setCharset(charset).setDelimiter(",");
            targetData.saveAttributes();
            return targetData;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
