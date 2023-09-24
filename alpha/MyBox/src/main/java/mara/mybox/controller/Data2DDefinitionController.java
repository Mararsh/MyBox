package mara.mybox.controller;

import java.sql.Connection;
import javafx.fxml.FXML;
import mara.mybox.data2d.Data2DExampleTools;
import mara.mybox.data2d.Data2DTools;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-9
 * @License Apache License Version 2.0
 */
public class Data2DDefinitionController extends InfoTreeManageController {

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
            editor = editorController;
            editorController.setParameters(this);

            super.initControls();

//            importExamples();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean controlAltL() {
        editorController.columnsController.clearAction();
        return true;
    }

    // this only works for development
    public void importExamples() {
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    conn.createStatement().execute("DELETE FROM Tree_Node WHERE category=\'" + category + "\'");
                    InfoNode root = tableTreeNode.findAndCreateRoot(conn, category);
                    String lang = Languages.getLangName();
                    conn.setAutoCommit(true);

                    InfoNode parent = new InfoNode(root, message("MyData"));
                    parent = tableTreeNode.insertData(conn, parent);

                    InfoNode child = new InfoNode(parent, message("Notes"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.Notes(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("Contacts"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.Contacts(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("CashFlow"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.CashFlow(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("PrivateProperty"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.PrivateProperty(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("Eyesight"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.Eyesight(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("Weight"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.Weight(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("Height"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.Height(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("Menstruation"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.Menstruation(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    parent = new InfoNode(root, message("StatisticDataOfChina"));
                    tableTreeNode.insertData(conn, parent);

                    child = new InfoNode(parent, message("ChinaPopulation"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ChinaPopulation(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ChinaCensus"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ChinaCensus(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ChinaGDP"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ChinaGDP(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ChinaCPI"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ChinaCPI(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ChinaFoodConsumption"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ChinaFoodConsumption(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ChinaGraduates"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ChinaGraduates(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ChinaMuseums"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ChinaMuseums(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ChinaHealthPersonnel"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ChinaHealthPersonnel(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ChinaMarriage"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ChinaMarriage(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ChinaSportWorldChampions"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ChinaSportWorldChampions(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("CrimesFiledByChinaPolice"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.CrimesFiledByChinaPolice(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("CrimesFiledByChinaProcuratorate"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.CrimesFiledByChinaProcuratorate(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    parent = new InfoNode(root, message("RegressionData"));
                    tableTreeNode.insertData(conn, parent);

                    child = new InfoNode(parent, message("IncomeHappiness"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.IncomeHappiness(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ExperienceSalary"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ExperienceSalary(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("IrisSpecies"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.IrisSpecies(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("DiabetesPrediction"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.DiabetesPrediction(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("DiabetesPredictionStandardized"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.DiabetesPredictionStandardized(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("HeartFailure"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.HeartFailure(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ConcreteCompressiveStrength"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ConcreteCompressiveStrength(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("DogRadiographsDataset"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.DogRadiographsDataset(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("BaseballSalaries"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.BaseballSalaries(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("SouthGermanCredit"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.SouthGermanCredit(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("BostonHousingPrices"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.BostonHousingPrices(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    parent = new InfoNode(root, message("LocationData"));
                    tableTreeNode.insertData(conn, parent);

                    child = new InfoNode(parent, message("ChineseHistoricalCapitals"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ChineseHistoricalCapitals(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("AutumnMovementPatternsOfEuropeanGadwalls"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.AutumnMovementPatternsOfEuropeanGadwalls(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("SpermWhalesGulfOfMexico"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.SpermWhalesGulfOfMexico(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("EpidemicReportsCOVID19"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.EpidemicReportsCOVID19(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    parent = new InfoNode(root, message("ProjectManagement"));
                    tableTreeNode.insertData(conn, parent);

                    child = new InfoNode(parent, message("ProjectRegister"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ProjectRegister(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ProjectStatus"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ProjectStatus(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("TaskRegister"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.TaskRegister(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("TaskStatus"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.TaskStatus(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("PersonRegister"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.PersonRegister(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("PersonStatus"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.PersonStatus(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ResourceRegister"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ResourceRegister(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("ResourceStatus"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.ResourceStatus(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("RiskAnalysis"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.RiskAnalysis(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("CostRecord"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.CostRecord(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    child = new InfoNode(parent, message("VerificationRecord"))
                            .setInfo(Data2DTools.definitionToXML(Data2DExampleTools.VerificationRecord(lang), true, ""));
                    tableTreeNode.insertData(conn, child);

                    conn.commit();
                } catch (Exception e) {
                    error = e.toString();
                    MyBoxLog.error(e);
                    return false;
                }
                return true;
            }

            @Override
            protected void whenSucceeded() {
                loadTree();
            }

        };
        start(task);
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
