package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import mara.mybox.data2d.Data2DExampleTools;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-9-9
 * @License Apache License Version 2.0
 */
public class Data2DDefinitionController extends TreeManageController {

    @FXML
    protected Data2DDefinitionEditor editorController;

    public Data2DDefinitionController() {
        baseTitle = message("Data2DDefinition");
        TipsLabelKey = "Data2DDefinitionTips";
        category = InfoNode.Data2DDefinition;
        nameMsg = message("DataName");
        valueMsg = message("Columns");
    }

    @Override
    public void initControls() {
        try {
            nodeController = editorController;
            editorController.setParameters(this);

            super.initControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean controlAltL() {
        editorController.columnsController.clearAction();
        return true;
    }

    /*
        examples
     */
    @FXML
    protected void popExamplesMenu(Event event) {
        if (UserConfig.getBoolean("Data2DColumnsExamplesPopWhenMouseHovering", false)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event mevent) {
        try {

            String lang = Languages.getLangName();
            List<MenuItem> items = new ArrayList<>();

            Menu myMenu = new Menu(message("MyData"), StyleTools.getIconImageView("iconCat.png"));
            items.add(myMenu);

            MenuItem menu = new MenuItem(message("Notes"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.Notes(lang);
                editorController.load(data);
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message("Contacts"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.Contacts(lang);
                editorController.load(data);
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message("CashFlow"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.CashFlow(lang);
                editorController.load(data);
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message("PrivateProperty"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.PrivateProperty(lang);
                editorController.load(data);
            });
            myMenu.getItems().add(menu);

            myMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("Eyesight"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.Eyesight(lang);
                editorController.load(data);
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message("Weight"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.Weight(lang);
                editorController.load(data);
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message("Height"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.Height(lang);
                editorController.load(data);
            });
            myMenu.getItems().add(menu);

            menu = new MenuItem(message("Menstruation"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.Menstruation(lang);
                editorController.load(data);
            });
            myMenu.getItems().add(menu);

            Menu chinaMenu = new Menu(message("StatisticDataOfChina"), StyleTools.getIconImageView("iconChina.png"));
            items.add(chinaMenu);

            menu = new MenuItem(message("ChinaPopulation"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaPopulation(lang);
                editorController.load(data);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaCensus"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaCensus(lang);
                editorController.load(data);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaGDP"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaGDP(lang);
                editorController.load(data);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaCPI"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaCPI(lang);
                editorController.load(data);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaFoodConsumption"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaFoodConsumption(lang);
                editorController.load(data);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaGraduates"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaGraduates(lang);
                editorController.load(data);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaMuseums"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaMuseums(lang);
                editorController.load(data);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaHealthPersonnel"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaHealthPersonnel(lang);
                editorController.load(data);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaMarriage"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaMarriage(lang);
                editorController.load(data);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("ChinaSportWorldChampions"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChinaSportWorldChampions(lang);
                editorController.load(data);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("CrimesFiledByChinaPolice"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.CrimesFiledByChinaPolice(lang);
                editorController.load(data);
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message("CrimesFiledByChinaProcuratorate"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.CrimesFiledByChinaProcuratorate(lang);
                editorController.load(data);
            });
            chinaMenu.getItems().add(menu);

            chinaMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("ChinaNationalBureauOfStatistics"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                myController.browse("https://data.stats.gov.cn/");
            });
            chinaMenu.getItems().add(menu);

            Menu regressionMenu = new Menu(message("RegressionData"), StyleTools.getIconImageView("iconLinearPgression.png"));
            items.add(regressionMenu);

            menu = new MenuItem(message("IncomeHappiness"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.IncomeHappiness(lang);
                editorController.load(data);
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("ExperienceSalary"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ExperienceSalary(lang);
                editorController.load(data);
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("IrisSpecies"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.IrisSpecies(lang);
                editorController.load(data);
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("DiabetesPrediction"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.DiabetesPrediction(lang);
                editorController.load(data);
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("DiabetesPredictionStandardized"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.DiabetesPredictionStandardized(lang);
                editorController.load(data);
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("HeartFailure"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.HeartFailure(lang);
                editorController.load(data);
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("ConcreteCompressiveStrength"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ConcreteCompressiveStrength(lang);
                editorController.load(data);
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("DogRadiographsDataset"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.DogRadiographsDataset(lang);
                editorController.load(data);
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("BaseballSalaries"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.BaseballSalaries(lang);
                editorController.load(data);
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("SouthGermanCredit"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.SouthGermanCredit(lang);
                editorController.load(data);
            });
            regressionMenu.getItems().add(menu);

            menu = new MenuItem(message("BostonHousingPrices"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.BostonHousingPrices(lang);
                editorController.load(data);
            });
            regressionMenu.getItems().add(menu);

            regressionMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message("AboutDataAnalysis"));
            menu.setStyle("-fx-text-fill: #2e598a;");
            menu.setOnAction((ActionEvent event) -> {
                myController.openHtml(HelpTools.aboutDataAnalysis());
            });
            regressionMenu.getItems().add(menu);

            Menu locationMenu = new Menu(message("LocationData"), StyleTools.getIconImageView("iconLocation.png"));
            items.add(locationMenu);

            menu = new MenuItem(message("ChineseHistoricalCapitals"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ChineseHistoricalCapitals(lang);
                editorController.load(data);
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message("AutumnMovementPatternsOfEuropeanGadwalls"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.AutumnMovementPatternsOfEuropeanGadwalls(lang);
                editorController.load(data);
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message("SpermWhalesGulfOfMexico"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.SpermWhalesGulfOfMexico(lang);
                editorController.load(data);
            });
            locationMenu.getItems().add(menu);

            menu = new MenuItem(message("EpidemicReportsCOVID19"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.EpidemicReportsCOVID19(lang);
                editorController.load(data);
            });
            locationMenu.getItems().add(menu);

            boolean isChinese = "zh".equals(lang);

            Menu pmMenu = new Menu(message("ProjectManagement"), StyleTools.getIconImageView("iconCalculator.png"));
            items.add(pmMenu);

            menu = new MenuItem(isChinese ? "项目登记" : "Project register");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ProjectRegister(lang);
                editorController.load(data);
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "项目状态" : "Project Status");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ProjectStatus(lang);
                editorController.load(data);
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "任务登记" : "Task register");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.TaskRegister(lang);
                editorController.load(data);
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "任务状态" : "Task Status");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.TaskStatus(lang);
                editorController.load(data);
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "人员登记" : "Person register");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.PersonRegister(lang);
                editorController.load(data);
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "人员状态" : "Person Status");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.PersonStatus(lang);
                editorController.load(data);
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "资源登记" : "Resource register");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ResourceRegister(lang);
                editorController.load(data);
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "资源状态" : "Resource Status");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.ResourceStatus(lang);
                editorController.load(data);
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "风险分析" : "Risk Analysis");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.RiskAnalysis(lang);
                editorController.load(data);
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "成本记录" : "Cost Record");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.CostRecord(lang);
                editorController.load(data);
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "检验记录" : "Verification Record");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = Data2DExampleTools.VerificationRecord(lang);
                editorController.load(data);
            });
            pmMenu.getItems().add(menu);

            items.add(new SeparatorMenuItem());

            CheckMenuItem pMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            pMenu.setSelected(UserConfig.getBoolean("Data2DColumnsExamplesPopWhenMouseHovering", false));
            pMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DColumnsExamplesPopWhenMouseHovering", pMenu.isSelected());
                }
            });
            items.add(pMenu);

            popEventMenu(mevent, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        static methods
     */
    public static Data2DDefinitionController open() {
        Data2DDefinitionController controller = (Data2DDefinitionController) WindowTools.openStage(Fxmls.Data2DDefinitionFxml);
        controller.requestMouse();
        return controller;
    }

}
