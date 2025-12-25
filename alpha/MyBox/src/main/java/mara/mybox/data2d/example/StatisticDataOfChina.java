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
import static mara.mybox.fxml.style.NodeStyleTools.linkStyle;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-15
 * @License Apache License Version 2.0
 */
public class StatisticDataOfChina {

    public static Menu menu(String lang, BaseData2DLoadController controller) {
        try {
            // https://data.stats.gov.cn/index.htm
            Menu chinaMenu = new Menu(message(lang, "StatisticDataOfChina"), StyleTools.getIconImageView("iconChina.png"));

            MenuItem menu = new MenuItem(message(lang, "ChinaPopulation"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinaPopulation(lang);
                if (makeExampleFile("ChinaPopulation", data)) {
                    controller.loadDef(data);
                }
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChinaCensus"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinaCensus(lang);
                if (makeExampleFile("ChinaCensus", data)) {
                    controller.loadDef(data);
                }
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChinaGDP"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinaGDP(lang);
                if (makeExampleFile("ChinaGDP", data)) {
                    controller.loadDef(data);
                }
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChinaCPI"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinaCPI(lang);
                if (makeExampleFile("ChinaCPI", data)) {
                    controller.loadDef(data);
                }
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChinaFoodConsumption"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinaFoodConsumption(lang);
                if (makeExampleFile("ChinaFoods_" + lang, data)) {
                    controller.loadDef(data);
                }
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChinaGraduates"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinaGraduates(lang);
                if (makeExampleFile("ChinaGraduates", data)) {
                    controller.loadDef(data);
                }
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChinaMuseums"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinaMuseums(lang);
                if (makeExampleFile("ChinaMuseums", data)) {
                    controller.loadDef(data);
                }
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChinaHealthPersonnel"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinaHealthPersonnel(lang);
                if (makeExampleFile("ChinaHealthPersonnel", data)) {
                    controller.loadDef(data);
                }
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChinaMarriage"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinaMarriage(lang);
                if (makeExampleFile("ChinaMarriage", data)) {
                    controller.loadDef(data);
                }
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChinaSportWorldChampions"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinaSportWorldChampions(lang);
                if (makeExampleFile("ChinaSportWorldChampions", data)) {
                    controller.loadDef(data);
                }
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "CrimesFiledByChinaPolice"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = CrimesFiledByChinaPolice(lang);
                if (makeExampleFile("ChinaCrimesFiledByPolice", data)) {
                    controller.loadDef(data);
                }
            });
            chinaMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "CrimesFiledByChinaProcuratorate"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = CrimesFiledByChinaProcuratorate(lang);
                if (makeExampleFile("ChinaCrimesFiledByProcuratorate", data)) {
                    controller.loadDef(data);
                }
            });
            chinaMenu.getItems().add(menu);

            chinaMenu.getItems().add(new SeparatorMenuItem());

            menu = new MenuItem(message(lang, "ChinaNationalBureauOfStatistics"));
            menu.setStyle(linkStyle());
            menu.setOnAction((ActionEvent event) -> {
                controller.browse("https://data.stats.gov.cn/");
            });
            chinaMenu.getItems().add(menu);

            return chinaMenu;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        exmaples of data 2D definition
     */
    public static DataFileCSV ChinaPopulation(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年" : "year_", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "年末总人口(万人)" : "population at year-end(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "男性人口(万人)" : "male(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "女性人口(万人)" : "female(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "城镇人口(万人)" : "urban(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "乡村人口(万人)" : "rural(ten thousand)", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "ChinaPopulation"))
                .setComments("https://data.stats.gov.cn/index.htm");
        return data;
    }

    public static DataFileCSV ChinaCensus(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年" : "year_", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "人口普查总人口(万人)" : "total population of census(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "男性(万人)" : "male(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "女性(万人)" : "female(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "性别比(女=100)" : "sex ratio(female=100)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "城镇(万人)" : "urban(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "乡村(万人)" : "rural(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "家庭户规模(人/户)" : "family size", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "0-14岁占比(%)" : "aged 0-14(%)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "15-64岁占比(%)" : "aged 15-64(%)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "65岁及以上占比(%)" : "aged over 65(%)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "汉族(万人)" : "han nationality population(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "汉族占比(%)" : "han nationality precentage(%)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "少数民族(万人)" : "minority nationality population(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "少数民族占比(%)" : "minority nationality precentage(%)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "每十万人口中受大专及以上教育人口数(人)" : "junior college or above education per one hundred thousand", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "每十万人口中受高中和中专教育人口数(人)" : "high school and secondary education per hundred thousand", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "每十万人口中受初中教育人口数(人)" : "junior high school education per hundred thousand", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "每十万人口中受小学教育人口数(人)" : "primary school education per hundred thousand", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "文盲人口数(万人)" : "illiteracy(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "文盲率(%)" : "illiteracy percentage(%)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "城镇化率(%)" : "urbanization rate(%)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "平均预期寿命(岁)" : "average life expectancy(years)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "男性平均预期寿命(岁)" : "male average life expectancy(years)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "女性平均预期寿命(岁)" : "female average life expectancy(years)", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "ChinaCensus"))
                .setComments("https://data.stats.gov.cn/index.htm");
        return data;
    }

    public static DataFileCSV ChinaGDP(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年" : "year_", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "国民总收入(GNI 亿元)" : "gross national income(GNI hundred million yuan)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "国内生产总值(GDP 亿元)" : "gross domestic product(GDP hundred million yuan)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "第一产业增加值(VA1 亿元)" : "value-added of first industry(VA1 hundred million yuan)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "第二产业增加值(VA2 亿元)" : "value-added of secondary industry(VA2 hundred million yuan)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "第三产业增加值(VA3 亿元)" : "value-added of tertiary industry(VA3 hundred million yuan)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "人均国内生产总值(元)" : "GDP per capita(yuan)", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "ChinaGDP"))
                .setComments("https://data.stats.gov.cn/index.htm");
        return data;
    }

    public static DataFileCSV ChinaCPI(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年" : "year_", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "居民消费价格指数(CPI 上年=100)" : "consumer price index(CPI last_year=100)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "城市居民消费价格指数(上年=100)" : "urban consumer price index(last_year=100)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "农村居民消费价格指数(上年=100)" : "rural consumer price index(last_year=100)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "商品零售价格指数(RPI 上年=100)" : "retail price index(RPI last_year=100)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "工业生产者出厂价格指数(PPI 上年=100)" : "producer price index(PPI last_year=100)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "工业生产者购进价格指数(PPIRM 上年=100)" : "producer price pndices of raw material(PPIRM last_year=100)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "固定资产投资价格指数(上年=100)" : "price indices of investment in fixed assets(last_year=100)", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "ChinaCPI"))
                .setComments("https://data.stats.gov.cn/index.htm");
        return data;
    }

    public static DataFileCSV ChinaFoodConsumption(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "指标" : "item", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "2020年" : "year 2020", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "2019年" : "year 2019", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "2018年" : "year 2018", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "2017年" : "year 2017", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "2016年" : "year 2016", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "2015年" : "year 2015", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "2014年" : "year 2014", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "2013年" : "year 2013", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "ChinaFoodConsumption"))
                .setComments("https://data.stats.gov.cn/index.htm");
        return data;
    }

    public static DataFileCSV ChinaGraduates(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年" : "year_", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "普通高等学校毕业生数(万人)" : "college graduates(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "普通中学毕业生数(万人)" : "middle school graduates(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "普通高中毕业生数(万人)" : "high school graduates(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "初中毕业生数(万人)" : "junior high school graduates(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "职业中学毕业生数(万人)" : "vocational high school graduates(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "普通小学毕业生数(万人)" : "primary school graduates(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "特殊教育学校毕业生数(万人)" : "special education school graduates(ten thousand)", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "ChinaGraduates"))
                .setComments("https://data.stats.gov.cn/index.htm");
        return data;
    }

    public static DataFileCSV ChinaMuseums(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年" : "year_", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "博物馆机构数(个)" : "museum institutions", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "博物馆从业人员(人)" : "employed", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "博物馆文物藏品(件/套)" : "relics", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "本年博物馆从有关部门接收文物数(件/套)" : "received in the year", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "本年博物馆修复文物数(件/套)" : "fixed in the year", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "博物馆考古发掘项目(个)" : "archaeology projects", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "博物馆基本陈列(个)" : "basical exhibition", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "博物馆举办展览(个)" : "special exhibition", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "博物馆参观人次(万人次)" : "visits(ten thousand)", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "ChinaMuseums"))
                .setComments("https://data.stats.gov.cn/index.htm");
        return data;
    }

    public static DataFileCSV ChinaHealthPersonnel(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年" : "year_", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "卫生人员数(万人)" : "health personnel(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "卫生技术人员数(万人)" : "medical personnel(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "执业(助理)医师数(万人)" : "practitioner(assistant)(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "执业医师数(万人)" : "practitioner(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "注册护士数(万人)" : "registered nurse(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "药师数(万人)" : "pharmacist(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "乡村医生和卫生员数(万人)" : "rural(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "其他技术人员数(万人)" : "other technical personnel(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "管理人员数(万人)" : "managerial personnel(ten thousand)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "工勤技能人员数(万人)" : "worker(ten thousand)", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "ChinaHealthPersonnel"))
                .setComments("https://data.stats.gov.cn/index.htm");
        return data;
    }

    public static DataFileCSV ChinaMarriage(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年" : "year_", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "结婚登记(万对)" : "married(ten thousand pairs)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "内地居民登记结婚(万对)" : "mainland residents married(ten thousand pairs)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "内地居民初婚登记(万人)" : "mainland residents newly married(ten thousand persons)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "内地居民再婚登记(万人)" : "mainland residents remarried(ten thousand persons)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "涉外及港澳台居民登记结婚(万对)" : "foreigners/HongKong/Macao/Taiwan married(ten thousand pairs)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "离婚登记(万对)" : "divorced(ten thousand pairs)", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "粗离婚率(千分比)" : "divorced ratio(permillage)", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(message(lang, "ChinaMarriage"))
                .setComments("https://data.stats.gov.cn/index.htm");
        return data;
    }

    public static DataFileCSV ChinaSportWorldChampions(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年" : "year_", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "世界冠军项数" : "categories of world champions", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "女子世界冠军项数" : "categories of female world champions", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "世界冠军人数" : "athletes of world champions", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "女子世界冠军人数" : "female athletes of world champions", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "世界冠军个数" : "number of world champions", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "女子世界冠军个数" : "number of female world champions", ColumnDefinition.ColumnType.Integer));
        data.setColumns(columns).setDataName(message(lang, "ChinaSportWorldChampions"))
                .setComments("https://data.stats.gov.cn/index.htm");
        return data;
    }

    public static DataFileCSV CrimesFiledByChinaPolice(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年" : "year_", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "立案的刑事案件" : "filed crimes", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "杀人" : "murder", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "伤害" : "injure", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "抢劫" : "rob", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "强奸" : "rape", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "拐卖妇女儿童" : "trafficking", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "盗窃" : "steal", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "诈骗" : "scam", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "走私" : "smuggle", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "假币" : "counterfeit money", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "其他" : "others", ColumnDefinition.ColumnType.Integer));
        data.setColumns(columns).setDataName(message(lang, "CrimesFiledByChinaPolice"))
                .setComments("https://data.stats.gov.cn/index.htm");
        return data;
    }

    public static DataFileCSV CrimesFiledByChinaProcuratorate(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年" : "year_", ColumnDefinition.ColumnType.String, true));
        columns.add(new Data2DColumn(isChinese ? "受案数" : "filed crimes", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "贪污贿赂" : "corruption and bribery", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "贪污" : "corruption", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "贿赂" : "bribery", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "挪用公款" : "embezzlement", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "集体私分" : "collectively dividing up state-owned properties without permission", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "巨额财产来源不明" : "huge unidentified property", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "其他贪污贿赂" : "other corruption and bribery", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "渎职" : "malfeasance", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "滥用职权" : "abuses of power", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "玩忽职守" : "neglecting of duty", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "徇私舞弊" : "favoritism", ColumnDefinition.ColumnType.Integer));
        columns.add(new Data2DColumn(isChinese ? "其他渎职" : "other malfeasance", ColumnDefinition.ColumnType.Integer));
        data.setColumns(columns).setDataName(message(lang, "CrimesFiledByChinaProcuratorate"))
                .setComments("https://data.stats.gov.cn/index.htm");
        return data;
    }

}
