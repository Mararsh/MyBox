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
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-9-15
 * @License Apache License Version 2.0
 */
public class ChineseCharacters {

    public static Menu menu(String lang, BaseData2DLoadController controller) {
        try {
            Menu dictionariesMenu = new Menu(message(lang, "ChineseCharacters"),
                    StyleTools.getIconImageView("iconWu.png"));

            MenuItem menu = new MenuItem(message(lang, "ChineseCharactersStandard"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseCharactersStandard(lang);
                if (makeExampleFile("ChineseCharactersStandard", data)) {
                    controller.loadDef(data);
                }
            });
            dictionariesMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChineseRadicals"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseRadicals(lang);
                if (makeExampleFile("ChineseRadicals", data)) {
                    controller.loadDef(data);
                }
            });
            dictionariesMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChinesePinyin"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinesePinyin(lang);
                if (makeExampleFile("ChinesePinyin", data)) {
                    controller.loadDef(data);
                }
            });
            dictionariesMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChinesePhrases"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinesePhrases(lang);
                if (makeExampleFile("ChinesePhrases", data)) {
                    controller.loadDef(data);
                }
            });
            dictionariesMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChineseTraditional"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseTraditional(lang);
                if (makeExampleFile("ChineseTraditional", data)) {
                    controller.loadDef(data);
                }
            });
            dictionariesMenu.getItems().add(menu);

            menu = new MenuItem(message(lang, "ChinesePolyphone"));
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChinesePolyphone(lang);
                if (makeExampleFile("ChinesePolyphone", data)) {
                    controller.loadDef(data);
                }
            });
            dictionariesMenu.getItems().add(menu);

            dictionariesMenu.getItems().add(new SeparatorMenuItem());

            boolean isChinese = Languages.isChinese(lang);
            Menu blcuMenu = new Menu(isChinese ? "北京语言大学的数据" : "Data from Beijing Language and Culture University");

            menu = new MenuItem(isChinese ? "汉字字频-2012-人民日报" : "Counts of Chinese characters in 2012 - People's Daily");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyBlcuCharactersPD(lang);
                if (makeExampleFile("ChineseFrequency-blcu-characters-2012-People's Daily", data)) {
                    controller.loadDef(data);
                }
            });
            blcuMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "历年汉字字频-人民日报" : "Yearly counts of Chinese characters - People's Daily");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyBlcuYearlyCharactersPD(lang);
                if (makeExampleFile("ChineseFrequency-blcu-characters-years-People's Daily", data)) {
                    controller.loadDef(data);
                }
            });
            blcuMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "历年汉字词组-人民日报" : "Yearly counts of Chinese words - People's Daily");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyBlcuYearlyWordsPD(lang);
                if (makeExampleFile("ChineseFrequency-blcu-words-years-People's Daily", data)) {
                    controller.loadDef(data);
                }
            });
            blcuMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "汉语词组-2015-新闻频道" : "Chinese words in news channel in 2015");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyBlcuNews(lang);
                if (makeExampleFile("ChineseFrequency-blcu-news-2015", data)) {
                    controller.loadDef(data);
                }
            });
            blcuMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "汉语词组-2015-技术频道" : "Chinese words in technology channel in 2015");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyBlcuTechnology(lang);
                if (makeExampleFile("ChineseFrequency-blcu-technology-2015", data)) {
                    controller.loadDef(data);
                }
            });
            blcuMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "汉语词组-2015-文学频道" : "Chinese words in literature channel in 2015");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyBlcuLiterature(lang);
                if (makeExampleFile("ChineseFrequency-blcu-literature-2015", data)) {
                    controller.loadDef(data);
                }
            });
            blcuMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "汉语词组-2015-微博频道" : "Chinese words in weibo channel in 2015");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyBlcuWeibo(lang);
                if (makeExampleFile("ChineseFrequency-blcu-weibo-2015", data)) {
                    controller.loadDef(data);
                }
            });
            blcuMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "汉语词组-2015-博客频道" : "Chinese words in blog channel in 2015");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyBlcuBlog(lang);
                if (makeExampleFile("ChineseFrequency-blcu-blog-2015", data)) {
                    controller.loadDef(data);
                }
            });
            blcuMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "汉语词组-2015-频道总和" : "Chinese words in channels in 2015");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyBlcuGlobal(lang);
                if (makeExampleFile("ChineseFrequency-blcu-global-2015", data)) {
                    controller.loadDef(data);
                }
            });
            blcuMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "汉语成语" : "Chinese Idioms");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseIdiomsBlcu(lang);
                if (makeExampleFile("ChineseIdioms-blcu", data)) {
                    controller.loadDef(data);
                }
            });
            blcuMenu.getItems().add(menu);

            dictionariesMenu.getItems().add(blcuMenu);

            Menu othersMenu = new Menu(message(lang, "Others"));

            menu = new MenuItem(isChinese ? "国家出版局抽样统计最常用的一千个汉字" : "Mostly used chinese characters by State Publication Bureau");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencySPB(lang);
                if (makeExampleFile("ChineseFrequency-State Publication Bureau", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "微信公众号-汉语词频统计-2016" : "Chinese characters statistic of weixin public corpus in 2016");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyWeixin2016(lang);
                if (makeExampleFile("ChineseFrequency-weixin_public_corpus-2016", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "汉字字频表(基数10亿)" : "Chinese characters statistic based on billion data");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyBaseBillion(lang);
                if (makeExampleFile("ChineseFrequency-imewlconverter-BaseBillion", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "百度汉字字频表-2009" : "Chinese characters statistic from baidu in 2009");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyBaidu2009(lang);
                if (makeExampleFile("ChineseFrequency-Baidu-2009", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "Google汉字字频表-2005" : "Chinese characters statistic from Google in 2005");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyGoogle2005(lang);
                if (makeExampleFile("ChineseFrequency-Google-2005", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "Yahoo汉字字频表-2009" : "Chinese characters statistic from Yahoo in 2009");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyYahoo2009(lang);
                if (makeExampleFile("ChineseFrequency-Yahoo-2009", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "知乎语料库" : "Chinese characters statistic of Zhihu");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyZhihu(lang);
                if (makeExampleFile("ChineseFrequency-Zhihu", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "古代汉语语料库字频表" : "Chinese characters statistic in ancient China");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyCncorpusAncient(lang);
                if (makeExampleFile("ChineseFrequency-cncorpus-ancient China", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "现代汉语语料库字频表" : "Chinese characters statistic in modern China");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyCncorpusCharactersModern(lang);
                if (makeExampleFile("ChineseFrequency-cncorpus-characters-modern China", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "现代汉语语料库词频表" : "Chinese words statistic in modern China");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyCncorpusWordsModern(lang);
                if (makeExampleFile("ChineseFrequency-cncorpus-words-modern China", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "现代汉语语料库分词类词频表" : "Chinese participles statistic in modern China");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyCncorpusParticiplesModern(lang);
                if (makeExampleFile("ChineseFrequency-cncorpus-participles-modern China", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "唐诗宋词元曲" : "Chinese characters statistic about traditional literature");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyTraditionalLiterature(lang);
                if (makeExampleFile("ChineseFrequency-traditional literature", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "文淵閣四庫全書" : "Chinese characters statistic of the Si Ku Quan Shu");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencySiKuQuanShu(lang);
                if (makeExampleFile("ChineseFrequency-freemdict-SiKuQuanShu", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            menu = new MenuItem(isChinese ? "殆知阁" : "Chinese characters statistic of the Yi Zhi Ge");
            menu.setOnAction((ActionEvent event) -> {
                DataFileCSV data = ChineseFrequencyYiZhiGe(lang);
                if (makeExampleFile("ChineseFrequency-mahavivo-YiZhiGe", data)) {
                    controller.loadDef(data);
                }
            });
            othersMenu.getItems().add(menu);

            dictionariesMenu.getItems().add(othersMenu);

            return dictionariesMenu;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        exmaples of data 2D definition
     */
    public static DataFileCSV ChineseCharactersStandard(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "编号" : "Number", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "级别" : "Level", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "部首" : "Radical", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "笔画数" : "Stroke Count", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "拼音" : "Pinyin", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "多音" : "Other Pinyin", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "繁体" : "Traditional", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "异形" : "Variant", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "字形" : "Glyph", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "笔顺" : "Strokes", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "拼音首" : "Pinyin Front", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "拼音尾" : "Pinyin End", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "音调" : "Tone", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "五笔" : "Wubi", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "十六进制" : "Hex", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "十进制" : "Dec", ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(message(lang, "ChineseCharactersStandard"));
        return data;
    }

    public static DataFileCSV ChineseRadicals(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "部首" : "Radical", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "名字" : "Name", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "笔画数" : "Stroke Count", ColumnDefinition.ColumnType.Short));
        data.setColumns(columns).setDataName(message(lang, "ChineseRadicals"));
        return data;
    }

    public static DataFileCSV ChinesePinyin(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "拼音" : "Pinyin", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "字" : "Characters", ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(message(lang, "ChinesePinyin"));
        return data;
    }

    public static DataFileCSV ChinesePhrases(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "词组" : "Phrase", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "繁体" : "Traditional", ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(message(lang, "ChinesePhrases"));
        return data;
    }

    public static DataFileCSV ChineseTraditional(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "繁体" : "Traditional", ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(message(lang, "ChineseTraditional"));
        return data;
    }

    public static DataFileCSV ChinesePolyphone(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "编号" : "Number", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "拼音" : "Pinyin", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "多音" : "Other Pinyin", ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(message(lang, "ChinesePolyphone"));
        return data;
    }

    public static DataFileCSV ChineseFrequencyBlcuCharactersPD(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频率" : "Frequency", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "累计频率" : "Cumulative frequency", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(isChinese ? "汉字字频-人民日报" : "Counts of Chinese characters - People's Daily");
        return data;
    }

    public static DataFileCSV ChineseFrequencyBlcuYearlyCharactersPD(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年份" : "Year", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频率" : "Frequency", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "累计频率" : "Cumulative frequency", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(isChinese ? "历年汉字字频-人民日报" : "Yearly counts of Chinese characters - People's Daily");
        return data;
    }

    public static DataFileCSV ChineseFrequencyBlcuYearlyWordsPD(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "年份" : "Year", ColumnDefinition.ColumnType.Short));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "词" : "Word", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频率" : "Frequency", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "累计频率" : "Cumulative frequency", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(isChinese ? "历年汉字词组-人民日报" : "Yearly counts of Chinese words - People's Daily");
        return data;
    }

    public static DataFileCSV ChineseIdiomsBlcu(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "成语" : "Idiom", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "拼音" : "Pinyin", ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(isChinese ? "汉语成语" : "Chinese Idioms");
        return data;
    }

    public static DataFileCSV ChineseFrequencyBlcuNews(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "词" : "Word", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "汉语词组-2015-新闻频道" : "Chinese words in news channel in 2015");
        return data;
    }

    public static DataFileCSV ChineseFrequencyBlcuLiterature(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "词" : "Word", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "汉语词组-2015-文学频道" : "Chinese words in literature channel in 2015");
        return data;
    }

    public static DataFileCSV ChineseFrequencyBlcuTechnology(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "词" : "Word", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "汉语词组-2015-技术频道" : "Chinese words in technology channel in 2015");
        return data;
    }

    public static DataFileCSV ChineseFrequencyBlcuWeibo(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "词" : "Word", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "汉语词组-2015-微博频道" : "Chinese words in weibo channel in 2015");
        return data;
    }

    public static DataFileCSV ChineseFrequencyBlcuBlog(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "词" : "Word", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "汉语词组-2015-博客频道" : "Chinese words in blog channel in 2015");
        return data;
    }

    public static DataFileCSV ChineseFrequencyBlcuGlobal(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "词" : "Word", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "汉语词组-2015-频道总和" : "Chinese words in channels in 2015");
        return data;
    }

    public static DataFileCSV ChineseFrequencySPB(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        data.setColumns(columns).setDataName(isChinese ? "国家出版局抽样统计最常用的一千个汉字"
                : "Mostly used chinese characters by State Publication Bureau");
        return data;
    }

    public static DataFileCSV ChineseFrequencyWeixin2016(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "词" : "Word", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "微信公众号-汉语词频统计-2016"
                : "Chinese characters statistic of  by weixin public corpus in 2016");
        return data;
    }

    public static DataFileCSV ChineseFrequencyBaseBillion(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频率" : "Frequency", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "累计频率" : "Cumulative frequency", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(isChinese ? "汉字字频表(基数10亿)" : "Chinese characters statistic based on billion data");
        return data;
    }

    public static DataFileCSV ChineseFrequencyBaidu2009(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "百度汉字字频表-2009" : "Chinese characters statistic from baidu in 2009");
        return data;
    }

    public static DataFileCSV ChineseFrequencyGoogle2005(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "Google汉字字频表-2005" : "Chinese characters statistic from Google in 2005");
        return data;
    }

    public static DataFileCSV ChineseFrequencyYahoo2009(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "Yahoo汉字字频表-2009" : "Chinese characters statistic from Yahoo in 2009");
        return data;
    }

    public static DataFileCSV ChineseFrequencyTraditionalLiterature(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "唐诗宋词元曲" : "Chinese characters statistic about traditional literature");
        return data;
    }

    public static DataFileCSV ChineseFrequencyCncorpusAncient(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "频率" : "Frequency", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "累计频率" : "Cumulative frequency", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(isChinese ? "古代汉语语料库字频表" : "Chinese characters statistic of ancient China");
        return data;
    }

    public static DataFileCSV ChineseFrequencyCncorpusCharactersModern(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "频率" : "Frequency", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "累计频率" : "Cumulative frequency", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(isChinese ? "现代汉语语料库字频表" : "Chinese characters statistic in modern China");
        return data;
    }

    public static DataFileCSV ChineseFrequencyCncorpusWordsModern(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "频率" : "Frequency", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "累计频率" : "Cumulative frequency", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(isChinese ? "现代汉语语料库词频表" : "Chinese words statistic in modern China");
        return data;
    }

    public static DataFileCSV ChineseFrequencyCncorpusParticiplesModern(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "词语" : "Word", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "词类标记" : "Token", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "词类名称" : "Catelog", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "频率" : "Frequency", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "累计频率" : "Cumulative frequency", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(isChinese ? "现代汉语语料库分词类词频表" : "Chinese participles statistic in modern China");
        return data;
    }

    public static DataFileCSV ChineseFrequencySiKuQuanShu(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "文淵閣四庫全書" : "Chinese characters statistic of the Si Ku Quan Shu");
        return data;
    }

    public static DataFileCSV ChineseFrequencyYiZhiGe(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "排名" : "Rank", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        data.setColumns(columns).setDataName(isChinese ? "殆知阁" : "Chinese characters statistic of the Yi Zhi Ge");
        return data;
    }

    public static DataFileCSV ChineseFrequencyZhihu(String lang) {
        boolean isChinese = Languages.isChinese(lang);
        DataFileCSV data = new DataFileCSV();
        List<Data2DColumn> columns = new ArrayList<>();
        columns.add(new Data2DColumn(isChinese ? "汉字" : "Character", ColumnDefinition.ColumnType.String));
        columns.add(new Data2DColumn(isChinese ? "频次" : "Count", ColumnDefinition.ColumnType.Long));
        columns.add(new Data2DColumn(isChinese ? "频率" : "Frequency", ColumnDefinition.ColumnType.Double));
        columns.add(new Data2DColumn(isChinese ? "累计频率" : "Cumulative frequency", ColumnDefinition.ColumnType.Double));
        data.setColumns(columns).setDataName(isChinese ? "知乎语料库" : "Chinese characters statistic of Zhihu");
        return data;
    }

}
