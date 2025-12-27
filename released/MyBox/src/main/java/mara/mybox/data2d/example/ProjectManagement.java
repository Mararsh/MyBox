package mara.mybox.data2d.example;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import mara.mybox.controller.BaseData2DLoadController;
import mara.mybox.data2d.DataFileCSV;
import static mara.mybox.data2d.example.Data2DExampleTools.makeExampleFile;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-15
 * @License Apache License Version 2.0
 */
public class ProjectManagement {

    public static Menu menu(String lang, BaseData2DLoadController controller) {
        try {

            Menu pmMenu = new Menu(message(lang, "ProjectManagement"),
                    StyleTools.getIconImageView("iconCalculator.png"));

            MenuItem menu = new MenuItem(message(lang, "ProjectRegister"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ProjectRegister(lang);
                if (makeExampleFile("PM_ProjectRegister_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ProjectStatus"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ProjectStatus(lang);
                if (makeExampleFile("PM_ProjectStatus_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "TaskRegister"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = TaskRegister(lang);
                if (makeExampleFile("PM_TaskRegister_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "TaskStatus"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = TaskStatus(lang);
                if (makeExampleFile("PM_TaskStatus_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "PersonRegister"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = PersonRegister(lang);
                if (makeExampleFile("PM_PersonRegister_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "PersonStatus"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = PersonStatus(lang);
                if (makeExampleFile("PM_PersonStatus_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ResourceRegister"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ResourceRegister(lang);
                if (makeExampleFile("PM_ResourceRegister_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ResourceStatus"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ResourceStatus(lang);
                if (makeExampleFile("PM_ResourceStatus_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "RiskAnalysis"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = RiskAnalysis(lang);
                if (makeExampleFile("PM_RiskAnalysis_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "CostRecord"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = CostRecord(lang);
                if (makeExampleFile("PM_CostRecords_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            pmMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "VerificationRecord"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = VerificationRecord(lang);
                if (makeExampleFile("PM_VerifyRecord_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            pmMenu.getItems().add(menu);

            return pmMenu;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        exmaples of data 2D definition
     */
    public static DataFileCSV ProjectRegister(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "ConfigurationID"), ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(message(lang, "Name"), ColumnDefinition.ColumnType.String, true).setWidth(200));
        columns.add(new Data2DColumn(message(lang, "LastStatus"), ColumnDefinition.ColumnType.Enumeration)
                .setFormat(isChinese ? "申请\n已批准\n需求分析\n设计\n实现\n测试\n验证\n维护\n已完成\n被否定\n失败\n已取消"
                        : "Applying\nApproved\nRequirement\nDesign\nImplementing\nTesting\nValidated\nMaintenance\nCompleted\nDenied\nFailed\nCanceled"));
        columns.add(new Data2DColumn(isChinese ? "项目经理" : "Manager", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "批准者" : "Approver", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "StartTime"), ColumnDefinition.ColumnType.Datetime));
        columns.add(new Data2DColumn(isChinese ? "关闭时间" : "Closed time", ColumnDefinition.ColumnType.Datetime));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(isChinese ? "项目登记" : "Project register");
        return data;
    }

    public static DataFileCSV ProjectStatus(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "项目编号" : "Project ID", ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(message(lang, "Status"), ColumnDefinition.ColumnType.Enumeration)
                .setFormat(isChinese ? "申请\n已批准\n需求分析\n设计\n实现\n测试\n验证\n维护\n已完成\n被否定\n失败\n已取消"
                        : "Applying\nApproved\nRequirement\nDesign\nImplementing\nTesting\nValidated\nMaintenance\nCompleted\nDenied\nFailed\nCanceled"));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Recorder"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "RecordTime"), ColumnDefinition.ColumnType.Datetime));
        data.setColumns(columns).setDataName(isChinese ? "项目状态" : "Project Status");
        return data;
    }

    public static DataFileCSV TaskRegister(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "ConfigurationID"), ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(isChinese ? "项目编号" : "Project ID", ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(message(lang, "Name"), ColumnDefinition.ColumnType.String, true).setWidth(200));
        columns.add(new Data2DColumn(message(lang, "LastStatus"), ColumnDefinition.ColumnType.Enumeration)
                .setFormat(isChinese ? "分派\n执行\n完成\n失败\n取消"
                        : "Assign\nPerform\nComplete\nFail\nCancel"));
        columns.add(new Data2DColumn(isChinese ? "执行者" : "Performer", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "开始时间" : "StartTime", ColumnDefinition.ColumnType.Datetime));
        columns.add(new Data2DColumn(isChinese ? "关闭时间" : "ClosedTime", ColumnDefinition.ColumnType.Datetime));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(isChinese ? "任务登记" : "Task register");
        return data;
    }

    public static DataFileCSV TaskStatus(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "任务编号" : "Task ID", ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(message(lang, "Status"), ColumnDefinition.ColumnType.Enumeration)
                .setFormat(isChinese ? "计划\n执行\n完成\n失败\n取消"
                        : "Plan\nPerform\nComplete\nFail\nCancel"));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Recorder"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "RecordTime"), ColumnDefinition.ColumnType.Datetime));
        data.setColumns(columns).setDataName(isChinese ? "任务状态" : "Task Status");
        return data;
    }

    public static DataFileCSV PersonRegister(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "ConfigurationID"), ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(isChinese ? "任务编号" : "Task ID", ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(message(lang, "Role"), ColumnDefinition.ColumnType.Enumeration)
                .setFormat(isChinese ? "投资人\n监管者\n项目经理\n组长\n设计者\n编程者\n测试者\n其他/她"
                        : "Investor\nSupervisor\nProject manager\nTeam leader\nDesigner\nProgrammer\nTester\n\nOther"));
        columns.add(new Data2DColumn(message(lang, "Name"), ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(message(lang, "PhoneNumber"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(isChinese ? "人员登记" : "Person register");
        return data;
    }

    public static DataFileCSV PersonStatus(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "人员编号" : "Person ID", ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(message(lang, "Status"), ColumnDefinition.ColumnType.Enumeration)
                .setFormat(isChinese ? "加入\n修改信息\n退出"
                        : "Join\nUpdate information\nQuit"));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Recorder"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "RecordTime"), ColumnDefinition.ColumnType.Datetime));
        data.setColumns(columns).setDataName(isChinese ? "人员状态" : "Person Status");
        return data;
    }

    public static DataFileCSV ResourceRegister(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "ConfigurationID"), ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(isChinese ? "任务编号" : "Task ID", ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(message(lang, "Type"), ColumnDefinition.ColumnType.Enumeration)
                .setFormat(isChinese ? "设备\n程序\n源代码\n文档\n数据\n其它"
                        : "Device\nProgram\nSource codes\nDocument\nData\nOther"));
        columns.add(new Data2DColumn(message(lang, "Name"), ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(message(lang, "LastStatus"), ColumnDefinition.ColumnType.Enumeration)
                .setFormat(isChinese ? "正常\n出借\n出售\n废弃\n损毁\n丢失"
                        : "Normal\nLent\nSaled\nDiscarded\nDamaged\nLost"));
        columns.add(new Data2DColumn(isChinese ? "保管者" : "Keeper", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "登记时间" : "Register time", ColumnDefinition.ColumnType.Datetime));
        columns.add(new Data2DColumn(isChinese ? "失效时间" : "Invalid time", ColumnDefinition.ColumnType.Datetime));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(isChinese ? "资源登记" : "Resource register");
        return data;
    }

    public static DataFileCSV ResourceStatus(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "资源编号" : "Resource ID", ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(message(lang, "Status"), ColumnDefinition.ColumnType.Enumeration)
                .setFormat(isChinese ? "正常\n出借\n出售\n废弃\n损毁\n丢失"
                        : "Normal\nLent\nSaled\nDiscarded\nDamaged\nLost"));
        columns.add(new Data2DColumn(message(lang, "Comments"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Recorder"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "RecordTime"), ColumnDefinition.ColumnType.Datetime));
        data.setColumns(columns).setDataName(isChinese ? "资源状态" : "Resource Status");
        return data;
    }

    public static DataFileCSV RiskAnalysis(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(message(lang, "ConfigurationID"), ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(isChinese ? "任务编号" : "Task ID", ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(message(lang, "Type"), ColumnDefinition.ColumnType.Enumeration)
                .setFormat(isChinese ? "范围\n质量\n时间\n资金\n技术\n人力\n法律\n其它"
                        : "Scope\nQuality\nTime\nMoney\nTechnique\nHuman\nLaw\nOther"));
        columns.add(new Data2DColumn(isChinese ? "风险项" : "Risk Item", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "可能性" : "Probability", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "严重性" : "Severity", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "优先级" : "Priority", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(message(lang, "Description"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "影响" : "Effects", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "应急措施" : "Contingency Actions", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "分析者" : "Analyzer", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "分析时间" : "Analysis time", ColumnDefinition.ColumnType.Datetime));
        data.setColumns(columns).setDataName(isChinese ? "风险分析" : "Risk Analysis");
        return data;
    }

    public static DataFileCSV CostRecord(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "任务编号" : "Task ID", ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(isChinese ? "计划开始日期" : "Planned start time", ColumnDefinition.ColumnType.Date));
        columns.add(new Data2DColumn(isChinese ? "计划结束日期" : "Planned end time", ColumnDefinition.ColumnType.Date));
        columns.add(new Data2DColumn(isChinese ? "计划工作量（人月）" : "Planned workload(person-month)", ColumnDefinition.ColumnType.Float));
        columns.add(new Data2DColumn(isChinese ? "计划成本（元）" : "Planned cost(Yuan)", ColumnDefinition.ColumnType.Float));
        columns.add(new Data2DColumn(isChinese ? "计划产出" : "Planned results", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "实际开始日期" : "Actual start time", ColumnDefinition.ColumnType.Date));
        columns.add(new Data2DColumn(isChinese ? "实际结束日期" : "Actual end time", ColumnDefinition.ColumnType.Date));
        columns.add(new Data2DColumn(isChinese ? "实际工作量（人月）" : "Actual workload(person-month)", ColumnDefinition.ColumnType.Float));
        columns.add(new Data2DColumn(isChinese ? "实际成本（元）" : "Actual cost(Yuan)", ColumnDefinition.ColumnType.Float));
        columns.add(new Data2DColumn(isChinese ? "实际产出" : "Actual results", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "Recorder"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(message(lang, "RecordTime"), ColumnDefinition.ColumnType.Datetime));
        data.setColumns(columns).setDataName(isChinese ? "成本记录" : "Cost Records");
        return data;
    }

    public static DataFileCSV VerificationRecord(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "任务编号" : "Task ID", ColumnDefinition.ColumnType.String, true).setWidth(140));
        columns.add(new Data2DColumn(isChinese ? "事项" : "Item", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "通过" : "Pass", ColumnDefinition.ColumnType.Boolean));
        columns.add(new Data2DColumn(isChinese ? "严重性" : "Severity", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(message(lang, "Description"), ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "影响" : "Effects", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "建议" : "Suggestions", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "检验者" : "Verifier", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "检验时间" : "Verification time", ColumnDefinition.ColumnType.Datetime));
        data.setColumns(columns).setDataName(isChinese ? "检验记录" : "Verify Record");
        return data;
    }

}
