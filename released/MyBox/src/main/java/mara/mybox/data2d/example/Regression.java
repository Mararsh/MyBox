package mara.mybox.data2d.example;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.controller.BaseData2DLoadController;
import mara.mybox.data2d.DataFileCSV;
import static mara.mybox.data2d.example.Data2DExampleTools.makeExampleFile;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import static mara.mybox.fxml.style.NodeStyleTools.linkStyle;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-15
 * @License Apache License Version 2.0
 */
public class Regression {

    public static Menu menu(String lang, BaseData2DLoadController controller) {
        try {
            Menu regressionMenu = new Menu(message(lang, "RegressionData"),
                    StyleTools.getIconImageView("iconLinearPgression.png"));

            MenuItem menu = new MenuItem(message(lang, "IncomeHappiness"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = IncomeHappiness(lang);
                if (makeExampleFile("DataAnalyse_IncomeHappiness", data)) {
                    controller.loadDef(data);
                }
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ExperienceSalary"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ExperienceSalary(lang);
                if (makeExampleFile("DataAnalyse_ExperienceSalary", data)) {
                    controller.loadDef(data);
                }
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "IrisSpecies"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = IrisSpecies(lang);
                if (makeExampleFile("DataAnalyse_IrisSpecies", data)) {
                    controller.loadDef(data);
                }
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "DiabetesPrediction"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = DiabetesPrediction(lang);
                if (makeExampleFile("DataAnalyse_DiabetesPrediction", data)) {
                    controller.loadDef(data);
                }
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "DiabetesPredictionStandardized"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = DiabetesPredictionStandardized(lang);
                if (makeExampleFile("DataAnalyse_DiabetesPrediction_standardized", data)) {
                    controller.loadDef(data);
                }
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "HeartFailure"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = HeartFailure(lang);
                if (makeExampleFile("DataAnalyse_HeartFailure", data)) {
                    controller.loadDef(data);
                }
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ConcreteCompressiveStrength"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ConcreteCompressiveStrength(lang);
                if (makeExampleFile("DataAnalyse_ConcreteCompressiveStrength", data)) {
                    controller.loadDef(data);
                }
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "DogRadiographsDataset"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = DogRadiographsDataset(lang);
                if (makeExampleFile("DataAnalyse_DogRadiographs", data)) {
                    controller.loadDef(data);
                }
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "BaseballSalaries"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = BaseballSalaries(lang);
                if (makeExampleFile("DataAnalyse_BaseballSalaries", data)) {
                    controller.loadDef(data);
                }
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "SouthGermanCredit"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = SouthGermanCredit(lang);
                if (makeExampleFile("DataAnalyse_SouthGermanCredit", data)) {
                    controller.loadDef(data);
                }
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "BostonHousingPrices"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = BostonHousingPrices(lang);
                if (makeExampleFile("DataAnalyse_BostonHousingPrices", data)) {
                    controller.loadDef(data);
                }
            });
            regressionMenu.getItems().add(menu);

            regressionMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message(lang, "AboutDataAnalysis"));
            menu.setStyle(linkStyle());
            menu.setOnAction((ActionEvent event) -> {
                controller.openHtml(HelpTools.aboutDataAnalysis());
            });
            regressionMenu.getItems().add(menu);

            return regressionMenu;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        exmaples of data 2D definition
     */
    public static DataFileCSV IncomeHappiness(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "收入" : "income", ColumnDefinition.ColumnType.Double, true));
        columns.add(new Data2DColumn(isChinese ? "快乐" : "happiness", ColumnDefinition.ColumnType.Double, true));
        data.setColumns(columns).setDataName(message(lang, "IncomeHappiness"))
                .setComments("https://www.scribbr.com/statistics/simple-linear-regression/");
        return data;
    }

    public static DataFileCSV ExperienceSalary(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "工作经验" : "YearsExperience", ColumnDefinition.ColumnType.Double, true));
        columns.add(new Data2DColumn(isChinese ? "薪资" : "Salary", ColumnDefinition.ColumnType.Double, true));
        data.setColumns(columns).setDataName(message(lang, "ExperienceSalary"))
                .setComments("https://github.com/krishnaik06/simple-Linear-Regression");
        return data;
    }

    public static DataFileCSV IrisSpecies(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "花萼长度(cm)" : "SepalLengthCm", ColumnDefinition.ColumnType.Double, true));
        columns.add(new Data2DColumn(isChinese ? "花萼宽度(cm)" : "SepalWidthCm", ColumnDefinition.ColumnType.Double, true));
        columns.add(new Data2DColumn(isChinese ? "花瓣长度(cm)" : "PetalLengthCm", ColumnDefinition.ColumnType.Double, true));
        columns.add(new Data2DColumn(isChinese ? "花瓣宽度(cm)" : "PetalWidthCm", ColumnDefinition.ColumnType.Double, true));
        columns.add(new Data2DColumn(isChinese ? "种类" : "Species", ColumnDefinition.ColumnType.String, true));
        data.setColumns(columns).setDataName(message(lang, "IrisSpecies"))
                .setComments("http://archive.ics.uci.edu/ml/datasets/Iris");
        return data;
    }

    public static DataFileCSV DiabetesPrediction(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年龄" : "age", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "性别" : "sex", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "BMI(体质指数)" : "BMI(body mass index)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "BP(平均血压)" : "BP(average blood pressure)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "S1(血清指标1)" : "S1(blood serum measurement 1)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "S2(血清指标2)" : "S2(blood serum measurement 2)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "S3(血清指标3)" : "S3(blood serum measurement 3)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "S4(血清指标4)" : "S4(blood serum measurement 4)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "S5(血清指标5)" : "S5(blood serum measurement 5)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "S6(血清指标6)" : "S6(blood serum measurement 6)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "一年后病情进展" : "disease progression one year after baseline", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "DiabetesPrediction"))
                .setComments("https://hastie.su.domains/Papers/LARS/");
        return data;
    }

    public static DataFileCSV DiabetesPredictionStandardized(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年龄" : "age", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "性别" : "sex", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "BMI(体质指数)" : "BMI(body mass index)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "BP(平均血压)" : "BP(average blood pressure)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "S1(血清指标1)" : "S1(blood serum measurement 1)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "S2(血清指标2)" : "S2(blood serum measurement 2)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "S3(血清指标3)" : "S3(blood serum measurement 3)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "S4(血清指标4)" : "S4(blood serum measurement 4)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "S5(血清指标5)" : "S5(blood serum measurement 5)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "S6(血清指标6)" : "S6(blood serum measurement 6)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "一年后病情进展" : "disease progression one year after baseline", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "DiabetesPredictionStandardized"))
                .setComments("https://hastie.su.domains/Papers/LARS/ \n"
                        + "first 10 columns have been normalized to have mean 0 and "
                        + "Euclidean norm 1 and the last column y has been centered (mean 0).");
        return data;
    }

    public static DataFileCSV HeartFailure(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年龄" : "age", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "贫血" : "anaemia", ColumnDefinition.ColumnType.Boolean)
                .setDescription("decrease of red blood cells or hemoglobin (boolean)"));
        columns.add(new Data2DColumn(isChinese ? "肌酐磷酸激酶(CPK_mcg/L)" : "creatinine_phosphokinase(CPK_mcg/L)", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "糖尿病" : "diabetes", ColumnDefinition.ColumnType.Boolean));
        columns.add(new Data2DColumn(isChinese ? "喷血分数" : "ejection fraction", ColumnDefinition.ColumnType.Integer)
                .setDescription("percentage of blood leaving the heart at each contraction (percentage)"));
        columns.add(new Data2DColumn(isChinese ? "高血压" : "high blood pressure", ColumnDefinition.ColumnType.Boolean));
        columns.add(new Data2DColumn(isChinese ? "血小板(kiloplatelets/mL)" : "platelets(kiloplatelets/mL)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "血清肌酸酐(mg/dL)" : "serum creatinine(mg/dL)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "血清钠(mEq/L)" : "serum sodium(mEq/L)", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "性别" : "sex", ColumnDefinition.ColumnType.Boolean));
        columns.add(new Data2DColumn(isChinese ? "抽烟" : "smoking", ColumnDefinition.ColumnType.Boolean));
        columns.add(new Data2DColumn(isChinese ? "观察期" : "follow-up period(days)", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "死亡" : "death event", ColumnDefinition.ColumnType.Boolean));
        data.setColumns(columns).setDataName(message(lang, "HeartFailure"))
                .setComments("http://archive.ics.uci.edu/ml/datasets/Heart+failure+clinical+records \n"
                        + "Davide Chicco, Giuseppe Jurman: \"Machine learning can predict survival of patients with heart failure "
                        + "from serum creatinine and ejection fraction alone\". BMC Medical Informatics and Decision Making 20, 16 (2020)");
        return data;
    }

    public static DataFileCSV ConcreteCompressiveStrength(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "水泥(公斤)" : "Cement(kg)", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "矿渣(公斤)" : "Blast Furnace Slag(kg)", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "煤灰(公斤)" : "Fly Ash(kg)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "水(公斤)" : "Water(kg)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "塑化剂(公斤)" : "Superplasticizer(kg)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "粗颗粒(公斤)" : "Coarse Aggregate(kg)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "细颗料(公斤)" : "Fine Aggregate(kg)", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "已使用天数" : "Age(days)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "1立方米混凝土抗压强度(兆帕)" : "Concrete compressive strength(MPa)", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "ConcreteCompressiveStrength"))
                .setComments("http://archive.ics.uci.edu/ml/datasets/Concrete+Compressive+Strength \n"
                        + "https://zhuanlan.zhihu.com/p/168747748");
        return data;
    }

    public static DataFileCSV DogRadiographsDataset(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "透亮度有改变" : "lucency changed", ColumnDefinition.ColumnType.Boolean)
                .setDescription("changed(1) or not changed(0)"));
        columns.add(new Data2DColumn(isChinese ? "刀片尺寸" : "blade size", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "胫骨结节面积" : "tibial tuberosity area", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "胫骨结节宽度" : "tibial tuberosity width", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "年龄(年)" : "age in years", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "重量(公斤)" : "weight in kilograms", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "抗滚针的位置" : "location of the anti-rotational pin", ColumnDefinition.ColumnType.Boolean));
        columns.add(new Data2DColumn(isChinese ? "双侧手术" : "bilateral surgery", ColumnDefinition.ColumnType.Boolean)
                .setDescription("bilateral surgery(1) or unilateral surgery(0)"));
        data.setColumns(columns).setDataName(message(lang, "DogRadiographsDataset"))
                .setComments("https://www4.stat.ncsu.edu/~boos/var.select/lucency.html \n"
                        + "Radiographic and Clinical Changes of the Tibial Tuberosity after Tibial Plateau Leveling Osteomtomy.");
        return data;
    }

    public static DataFileCSV BaseballSalaries(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "薪水(千美元)" : "Salary (thousands of dollars)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "击球平均得分数" : "Batting average", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "上垒率" : "On-base percentage", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "跑动数" : "Number of runs", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "击球数" : "Number of hits", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "二垒数" : "Number of doubles", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "三垒数" : "Number of triples", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "全垒数" : "Number of home runs", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "打点数" : "Number of runs batted in", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "走动数" : "Number of walks", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "三振出局数" : "Number of strike-outs", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "盗垒数" : "Number of stolen bases", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "失误数" : "Number of errors", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "自由球员资格" : "free agency eligibility", ColumnDefinition.ColumnType.Boolean));
        columns.add(new Data2DColumn(isChinese ? "1991/2的自由球员" : "free agent in 1991/2", ColumnDefinition.ColumnType.Boolean));
        columns.add(new Data2DColumn(isChinese ? "仲裁资格" : "arbitration eligibility", ColumnDefinition.ColumnType.Boolean));
        columns.add(new Data2DColumn(isChinese ? "于1991/2仲裁" : "arbitration in 1991/2", ColumnDefinition.ColumnType.Boolean));
        data.setColumns(columns).setDataName(message(lang, "BaseballSalaries"))
                .setComments("https://www4.stat.ncsu.edu/~boos/var.select/baseball.html \n"
                        + "Salary information for 337 Major League Baseball (MLB) players who are not pitchers "
                        + "and played at least one game during both the 1991 and 1992 seasons.");
        return data;
    }

    public static DataFileCSV SouthGermanCredit(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "状态" : "status", ColumnDefinition.ColumnType.Short)
                .setDescription("status of the debtor's checking account with the bank\n"
                        + "1:no checking account   \n"
                        + "2: ... < 0 DM    \n"
                        + "3: 0<= ... < 200 DM   \n"
                        + "4: >= 200 DM \n"
                        + "salary for at least 1 year"));
        columns.add(new Data2DColumn(isChinese ? "持续时间(月)" : "duration(months)", ColumnDefinition.ColumnType.Short)
                .setDescription("credit duration in months"));
        columns.add(new Data2DColumn(isChinese ? "信用卡历史" : "credit_history", ColumnDefinition.ColumnType.Short)
                .setDescription("history of compliance with previous or concurrent credit contracts\n"
                        + " 0 : delay in paying off in the past   \n"
                        + " 1 : critical account/other credits elsewhere \n"
                        + " 2 : no credits taken/all credits paid back duly\n"
                        + " 3 : existing credits paid back duly till now   \n"
                        + " 4 : all credits at this bank paid back duly "));
        columns.add(new Data2DColumn(isChinese ? "用途" : "purpose", ColumnDefinition.ColumnType.Short)
                .setDescription("purpose for which the credit is needed\n"
                        + " 0 : others             \n"
                        + " 1 : car (new)          \n"
                        + " 2 : car (used)         \n"
                        + " 3 : furniture/equipment\n"
                        + " 4 : radio/television   \n"
                        + " 5 : domestic appliances\n"
                        + " 6 : repairs            \n"
                        + " 7 : education          \n"
                        + " 8 : vacation           \n"
                        + " 9 : retraining         \n"
                        + " 10 : business  "));
        columns.add(new Data2DColumn(isChinese ? "金额" : "amount", ColumnDefinition.ColumnType.Short)
                .setDescription("credit amount in DM (quantitative; result of monotonic transformation; "
                        + "actual data and type of transformation unknown)"));
        columns.add(new Data2DColumn(isChinese ? "储蓄" : "savings", ColumnDefinition.ColumnType.Short)
                .setDescription("debtor's savings\n"
                        + " 1 : unknown/no savings account\n"
                        + " 2 : ... <  100 DM             \n"
                        + " 3 : 100 <= ... <  500 DM      \n"
                        + " 4 : 500 <= ... < 1000 DM      \n"
                        + " 5 : ... >= 1000 DM   "));
        columns.add(new Data2DColumn(isChinese ? "职业年限" : "employment_duration", ColumnDefinition.ColumnType.Short)
                .setDescription("duration of debtor's employment with current employer\n"
                        + " 1 : unemployed      \n"
                        + " 2 : < 1 yr          \n"
                        + " 3 : 1 <= ... < 4 yrs\n"
                        + " 4 : 4 <= ... < 7 yrs\n"
                        + " 5 : >= 7 yrs "));
        columns.add(new Data2DColumn(isChinese ? "信贷率" : "installment_rate", ColumnDefinition.ColumnType.Short)
                .setDescription("credit installments as a percentage of debtor's disposable income\n"
                        + "1 : >= 35         \n"
                        + " 2 : 25 <= ... < 35\n"
                        + " 3 : 20 <= ... < 25\n"
                        + " 4 : < 20  "));
        columns.add(new Data2DColumn(isChinese ? "个人状态" : "personal_status_sex", ColumnDefinition.ColumnType.Short)
                .setDescription("combined information on sex and marital status\n"
                        + " 1 : male : divorced/separated           \n"
                        + " 2 : female : non-single or male : single\n"
                        + " 3 : male : married/widowed              \n"
                        + " 4 : female : single "));
        columns.add(new Data2DColumn(isChinese ? "其他债务人" : "other_debtors", ColumnDefinition.ColumnType.Short)
                .setDescription("Is there another debtor or a guarantor for the credit\n"
                        + " 1 : none        \n"
                        + " 2 : co-applicant\n"
                        + " 3 : guarantor "));
        columns.add(new Data2DColumn(isChinese ? "当前居住年限(年)" : "present_residence", ColumnDefinition.ColumnType.Short)
                .setDescription("length of time (in years) the debtor lives in the present residence\n"
                        + " 1 : < 1 yr          \n"
                        + " 2 : 1 <= ... < 4 yrs\n"
                        + " 3 : 4 <= ... < 7 yrs\n"
                        + " 4 : >= 7 yrs "));
        columns.add(new Data2DColumn(isChinese ? "财产" : "property", ColumnDefinition.ColumnType.Short)
                .setDescription("the debtor's most valuable property\n"
                        + " 1 : unknown / no property                    \n"
                        + " 2 : car or other                             \n"
                        + " 3 : building soc. savings agr./life insurance\n"
                        + " 4 : real estate   "));
        columns.add(new Data2DColumn(isChinese ? "年龄" : "age", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "其它贷款计划" : "other_installment_plans", ColumnDefinition.ColumnType.Short)
                .setDescription("installment plans from providers other than the credit-giving bank\n"
                        + " 1 : bank  \n"
                        + " 2 : stores\n"
                        + " 3 : none"));
        columns.add(new Data2DColumn(isChinese ? "居所类型" : "housing", ColumnDefinition.ColumnType.Short)
                .setDescription("type of housing the debtor lives in\n"
                        + " 1 : for free\n"
                        + " 2 : rent    \n"
                        + " 3 : own "));
        columns.add(new Data2DColumn(isChinese ? "信用卡数目" : "number_credits", ColumnDefinition.ColumnType.Short)
                .setDescription("number of credits including the current one the debtor has (or had) at this bank\n"
                        + " 1 : 1   \n"
                        + " 2 : 2-3 \n"
                        + " 3 : 4-5 \n"
                        + " 4 : >= 6"));
        columns.add(new Data2DColumn(isChinese ? "工作类型" : "job", ColumnDefinition.ColumnType.Short)
                .setDescription(" 1 : unemployed/unskilled - non-resident       \n"
                        + " 2 : unskilled - resident                      \n"
                        + " 3 : skilled employee/official                 \n"
                        + " 4 : manager/self-empl./highly qualif. employee"));
        columns.add(new Data2DColumn(isChinese ? "被依赖人数" : "people_liable", ColumnDefinition.ColumnType.Short)
                .setDescription("number of persons who financially depend on the debtor\n"
                        + " 1 : 3 or more\n"
                        + " 2 : 0 to 2"));
        columns.add(new Data2DColumn(isChinese ? "有电话" : "telephone", ColumnDefinition.ColumnType.Short)
                .setDescription("Is there a telephone landline registered on the debtor's name? "
                        + "(binary; remember that the data are from the 1970s)\n"
                        + " 1 : no                       \n"
                        + " 2 : yes (under customer name)"));
        columns.add(new Data2DColumn(isChinese ? "是外籍雇员" : "foreign_worker", ColumnDefinition.ColumnType.Short)
                .setDescription("Is the debtor a foreign worker\n"
                        + " 1 : yes\n"
                        + " 2 : no "));
        columns.add(new Data2DColumn(isChinese ? "信用风险" : "credit_risk", ColumnDefinition.ColumnType.Short)
                .setDescription("Has the credit contract been complied with (good) or not (bad) ?\n"
                        + " 0 : bad \n"
                        + " 1 : good"));

        data.setColumns(columns).setDataName(message(lang, "SouthGermanCredit"))
                .setComments("http://archive.ics.uci.edu/ml/datasets/South+German+Credit\n"
                        + "700 good and 300 bad credits with 20 predictor variables. Data from 1973 to 1975. "
                        + "Stratified sample from actual credits with bad credits heavily oversampled. A cost matrix can be used.");
        return data;
    }

    public static DataFileCSV BostonHousingPrices(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "镇" : "town", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "经度" : "longitude", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "纬度" : "latitude", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "犯罪率" : "crime_ratio", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "超过25000平米的区" : "zoned_bigger_25000", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "工业用地" : "industrial_land", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "靠近查理斯河" : "near_Charies_River", ColumnDefinition.ColumnType.Boolean));
        columns.add(new Data2DColumn(isChinese ? "一氧化氮浓度" : "nitrogen_density", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "平均房间数" : "average_room_number", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "早于1940建成比率" : "built_before_1940_ratio", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "到达市中心的距离" : "distance_to_centre", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "公路可达性" : "accessbility_to_hightway", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "税率" : "tax_rate", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "学生教师比" : "pupil_teacher_ratio", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "低收入比" : "lower_class_ratio", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "价格中位数" : "median_price", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "BostonHousingPrices"))
                .setComments("https://github.com/tomsharp/SVR/tree/master/data");
        return data;
    }

}
