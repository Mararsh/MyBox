package mara.mybox.fxml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javafx.scene.paint.Color;
import mara.mybox.color.SRGB;
import mara.mybox.data.ColorData;
import mara.mybox.db.TableColorData;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2018-11-13 12:38:14
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class FxmlColor {

    public static Map<String, Color> WebColors, ChineseColors, JapaneseColors;
    public static Map<Color, String> WebColorNames, ChineseColorNames, JapaneseColorNames;

    public static String colorName(Color color) {
        String colorName = webColorNames().get(color);
        if (colorName != null) {
            return colorName;
        }
        colorName = chineseColorNames().get(color);
        if (colorName != null) {
            return colorName;
        }
        colorName = japaneseColorNames().get(color);
        return colorName;
    }

    // https://tool.lanrentuku.com/color/china.html
    public static Map<String, Color> chineseColors() {
        if (ChineseColors != null) {
            return ChineseColors;
        }
        ChineseColors = new LinkedHashMap<>();
        ChineseColors.put("蔚蓝", Color.web("#70f3ff"));
        ChineseColors.put("蓝", Color.web("#44cef6"));
        ChineseColors.put("碧蓝", Color.web("#3eede7"));
        ChineseColors.put("石青", Color.web("#1685a9"));
        ChineseColors.put("靛青", Color.web("#177cb0"));
        ChineseColors.put("靛蓝", Color.web("#065279"));
        ChineseColors.put("花青", Color.web("#003472"));
        ChineseColors.put("宝蓝", Color.web("#4b5cc4"));
        ChineseColors.put("蓝灰色", Color.web("#a1afc9"));
        ChineseColors.put("藏青", Color.web("#2e4e7e"));
        ChineseColors.put("藏蓝", Color.web("#3b2e7e"));
        ChineseColors.put("黛", Color.web("#4a4266"));
        ChineseColors.put("黛绿", Color.web("#426666"));
        ChineseColors.put("黛蓝", Color.web("#425066"));
        ChineseColors.put("黛紫", Color.web("#574266"));
        ChineseColors.put("紫色", Color.web("#8d4bbb"));
        ChineseColors.put("紫酱", Color.web("#815463"));
        ChineseColors.put("酱紫", Color.web("#815476"));
        ChineseColors.put("紫檀", Color.web("#4c221b"));
        ChineseColors.put("绀青", Color.web("#003371"));
        ChineseColors.put("紫棠", Color.web("#56004f"));
        ChineseColors.put("青莲", Color.web("#801dae"));
        ChineseColors.put("群青", Color.web("#4c8dae"));
        ChineseColors.put("雪青", Color.web("#b0a4e3"));
        ChineseColors.put("丁香色", Color.web("#cca4e3"));
        ChineseColors.put("藕色", Color.web("#edd1d8"));
        ChineseColors.put("藕荷色", Color.web("#e4c6d0"));
        ChineseColors.put("朱砂", Color.web("#ff461f"));
        ChineseColors.put("火红", Color.web("#ff2d51"));
        ChineseColors.put("朱膘", Color.web("#f36838"));
        ChineseColors.put("妃色", Color.web("#ed5736"));
        ChineseColors.put("洋红", Color.web("#ff4777"));
        ChineseColors.put("品红", Color.web("#f00056"));
        ChineseColors.put("粉红", Color.web("#ffb3a7"));
        ChineseColors.put("桃红", Color.web("#f47983"));
        ChineseColors.put("海棠红", Color.web("#db5a6b"));
        ChineseColors.put("樱桃色", Color.web("#c93756"));
        ChineseColors.put("酡颜", Color.web("#f9906f"));
        ChineseColors.put("银红", Color.web("#f05654"));
        ChineseColors.put("大红", Color.web("#ff2121"));
        ChineseColors.put("石榴红", Color.web("#f20c00"));
        ChineseColors.put("绛紫", Color.web("#8c4356"));
        ChineseColors.put("绯红", Color.web("#c83c23"));
        ChineseColors.put("胭脂", Color.web("#9d2933"));
        ChineseColors.put("朱红", Color.web("#ff4c00"));
        ChineseColors.put("丹", Color.web("#ff4e20"));
        ChineseColors.put("彤", Color.web("#f35336"));
        ChineseColors.put("酡红", Color.web("#dc3023"));
        ChineseColors.put("炎", Color.web("#ff3300"));
        ChineseColors.put("茜色", Color.web("#cb3a56"));
        ChineseColors.put("绾", Color.web("#a98175"));
        ChineseColors.put("檀", Color.web("#b36d61"));
        ChineseColors.put("嫣红", Color.web("#ef7a82"));
        ChineseColors.put("洋红", Color.web("#ff0097"));
        ChineseColors.put("枣红", Color.web("#c32136"));
        ChineseColors.put("殷红", Color.web("#be002f"));
        ChineseColors.put("赫赤", Color.web("#c91f37"));
        ChineseColors.put("银朱", Color.web("#bf242a"));
        ChineseColors.put("赤", Color.web("#c3272b"));
        ChineseColors.put("胭脂", Color.web("#9d2933"));
        ChineseColors.put("栗色", Color.web("#60281e"));
        ChineseColors.put("玄色", Color.web("#622a1d"));
        ChineseColors.put("松花色", Color.web("#bce672"));
        ChineseColors.put("柳黄", Color.web("#c9dd22"));
        ChineseColors.put("嫩绿", Color.web("#bddd22"));
        ChineseColors.put("柳绿", Color.web("#afdd22"));
        ChineseColors.put("葱黄", Color.web("#a3d900"));
        ChineseColors.put("葱绿", Color.web("#9ed900"));
        ChineseColors.put("豆绿", Color.web("#9ed048"));
        ChineseColors.put("豆青", Color.web("#96ce54"));
        ChineseColors.put("油绿", Color.web("#00bc12"));
        ChineseColors.put("葱倩", Color.web("#0eb83a"));
        ChineseColors.put("葱青", Color.web("#0eb83a"));
        ChineseColors.put("青葱", Color.web("#0aa344"));
        ChineseColors.put("石绿", Color.web("#16a951"));
        ChineseColors.put("松柏绿", Color.web("#21a675"));
        ChineseColors.put("松花绿", Color.web("#057748"));
        ChineseColors.put("绿沈", Color.web("#0c8918"));
        ChineseColors.put("绿色", Color.web("#00e500"));
        ChineseColors.put("草绿", Color.web("#40de5a"));
        ChineseColors.put("青翠", Color.web("#00e079"));
        ChineseColors.put("青色", Color.web("#00e09e"));
        ChineseColors.put("翡翠色", Color.web("#3de1ad"));
        ChineseColors.put("碧绿", Color.web("#2add9c"));
        ChineseColors.put("玉色", Color.web("#2edfa3"));
        ChineseColors.put("缥", Color.web("#7fecad"));
        ChineseColors.put("艾绿", Color.web("#a4e2c6"));
        ChineseColors.put("石青", Color.web("#7bcfa6"));
        ChineseColors.put("碧色", Color.web("#1bd1a5"));
        ChineseColors.put("青碧", Color.web("#48c0a3"));
        ChineseColors.put("铜绿", Color.web("#549688"));
        ChineseColors.put("竹青", Color.web("#789262"));
        ChineseColors.put("墨灰", Color.web("#758a99"));
        ChineseColors.put("墨色", Color.web("#50616d"));
        ChineseColors.put("鸦青", Color.web("#424c50"));
        ChineseColors.put("黯", Color.web("#41555d"));
        ChineseColors.put("樱草色", Color.web("#eaff56"));
        ChineseColors.put("鹅黄", Color.web("#fff143"));
        ChineseColors.put("鸭黄", Color.web("#faff72"));
        ChineseColors.put("杏黄", Color.web("#ffa631"));
        ChineseColors.put("橙黄", Color.web("#ffa400"));
        ChineseColors.put("橙色", Color.web("#fa8c35"));
        ChineseColors.put("杏红", Color.web("#ff8c31"));
        ChineseColors.put("橘黄", Color.web("#ff8936"));
        ChineseColors.put("橘红", Color.web("#ff7500"));
        ChineseColors.put("藤黄", Color.web("#ffb61e"));
        ChineseColors.put("姜黄", Color.web("#ffc773"));
        ChineseColors.put("雌黄", Color.web("#ffc64b"));
        ChineseColors.put("赤金", Color.web("#f2be45"));
        ChineseColors.put("缃色", Color.web("#f0c239"));
        ChineseColors.put("雄黄", Color.web("#e9bb1d"));
        ChineseColors.put("秋香色", Color.web("#d9b611"));
        ChineseColors.put("金色", Color.web("#eacd76"));
        ChineseColors.put("牙色", Color.web("#eedeb0"));
        ChineseColors.put("枯黄", Color.web("#d3b17d"));
        ChineseColors.put("黄栌", Color.web("#e29c45"));
        ChineseColors.put("乌金", Color.web("#a78e44"));
        ChineseColors.put("昏黄", Color.web("#c89b40"));
        ChineseColors.put("棕黄", Color.web("#ae7000"));
        ChineseColors.put("琥珀", Color.web("#ca6924"));
        ChineseColors.put("棕色", Color.web("#b25d25"));
        ChineseColors.put("茶色", Color.web("#b35c44"));
        ChineseColors.put("棕红", Color.web("#9b4400"));
        ChineseColors.put("赭", Color.web("#9c5333"));
        ChineseColors.put("驼色", Color.web("#a88462"));
        ChineseColors.put("秋色", Color.web("#896c39"));
        ChineseColors.put("棕绿", Color.web("#827100"));
        ChineseColors.put("褐色", Color.web("#6e511e"));
        ChineseColors.put("棕黑", Color.web("#7c4b00"));
        ChineseColors.put("赭色", Color.web("#955539"));
        ChineseColors.put("赭石", Color.web("#845a33"));
        ChineseColors.put("精白", Color.web("#ffffff"));
        ChineseColors.put("银白", Color.web("#e9e7ef"));
        ChineseColors.put("铅白", Color.web("#f0f0f4"));
        ChineseColors.put("霜色", Color.web("#e9f1f6"));
        ChineseColors.put("雪白", Color.web("#f0fcff"));
        ChineseColors.put("莹白", Color.web("#e3f9fd"));
        ChineseColors.put("月白", Color.web("#d6ecf0"));
        ChineseColors.put("象牙白", Color.web("#fffbf0"));
        ChineseColors.put("缟", Color.web("#f2ecde"));
        ChineseColors.put("鱼肚白", Color.web("#fcefe8"));
        ChineseColors.put("白粉", Color.web("#fff2df"));
        ChineseColors.put("荼白", Color.web("#f3f9f1"));
        ChineseColors.put("鸭卵青", Color.web("#e0eee8"));
        ChineseColors.put("素", Color.web("#e0f0e9"));
        ChineseColors.put("青白", Color.web("#c0ebd7"));
        ChineseColors.put("蟹壳青", Color.web("#bbcdc5"));
        ChineseColors.put("花白", Color.web("#c2ccd0"));
        ChineseColors.put("老银", Color.web("#bacac6"));
        ChineseColors.put("灰色", Color.web("#808080"));
        ChineseColors.put("苍色", Color.web("#75878a"));
        ChineseColors.put("水色", Color.web("#88ada6"));
        ChineseColors.put("黝", Color.web("#6b6882"));
        ChineseColors.put("乌色", Color.web("#725e82"));
        ChineseColors.put("玄青", Color.web("#3d3b4f"));
        ChineseColors.put("乌黑", Color.web("#392f41"));
        ChineseColors.put("黎", Color.web("#75664d"));
        ChineseColors.put("黧", Color.web("#5d513c"));
        ChineseColors.put("黝黑", Color.web("#665757"));
        ChineseColors.put("缁色", Color.web("#493131"));
        ChineseColors.put("煤黑", Color.web("#312520"));
        ChineseColors.put("漆黑", Color.web("#161823"));
        ChineseColors.put("黑色", Color.web("#000000"));
        return ChineseColors;
    }

    public static Map<Color, String> chineseColorNames() {
        if (ChineseColorNames != null) {
            return ChineseColorNames;
        }
        Map<String, Color> named = chineseColors();
        ChineseColorNames = new LinkedHashMap<>();
        for (String name : named.keySet()) {
            ChineseColorNames.put(named.get(name), name);
        }
        return ChineseColorNames;
    }

    public static List<Color> chineseColorValues() {
        Set<Color> keys = chineseColorNames().keySet();
        List<Color> colors = new ArrayList<>();
        colors.addAll(keys);
        return colors;
    }

    // https://tool.lanrentuku.com/color/japan.html
    public static Map<String, Color> japaneseColors() {
        if (JapaneseColors != null) {
            return JapaneseColors;
        }
        JapaneseColors = new LinkedHashMap<>();
        JapaneseColors.put("古代紫 ", Color.web("#895b8a"));
        JapaneseColors.put("茄子紺 ", Color.web("#824880"));
        JapaneseColors.put("二藍 ", Color.web("#915c8b"));
        JapaneseColors.put("京紫 ", Color.web("#9d5b8b"));
        JapaneseColors.put("蒲葡", Color.web("#7a4171"));
        JapaneseColors.put("若紫", Color.web("#bc64a4"));
        JapaneseColors.put("紅紫", Color.web("#b44c97"));
        JapaneseColors.put("梅紫", Color.web("#aa4c8f"));
        JapaneseColors.put("菖蒲色", Color.web("#cc7eb1"));
        JapaneseColors.put("紅藤色 ", Color.web("#cca6bf"));
        JapaneseColors.put("浅紫 ", Color.web("#c4a3bf"));
        JapaneseColors.put("紫水晶 ", Color.web("#e7e7eb"));
        JapaneseColors.put("薄梅鼠", Color.web("#dcd6d9"));
        JapaneseColors.put("暁鼠", Color.web("#d3cfd9"));
        JapaneseColors.put("牡丹鼠", Color.web("#d3ccd6"));
        JapaneseColors.put("霞色", Color.web("#c8c2c6"));
        JapaneseColors.put("藤鼠", Color.web("#a6a5c4"));
        JapaneseColors.put("半色", Color.web("#a69abd"));
        JapaneseColors.put("薄色", Color.web("#a89dac"));
        JapaneseColors.put("薄鼠", Color.web("#9790a4"));
        JapaneseColors.put("鳩羽鼠", Color.web("#9e8b8e"));
        JapaneseColors.put("鳩羽色", Color.web("#95859c"));
        JapaneseColors.put("桔梗鼠", Color.web("#95949a"));
        JapaneseColors.put("紫鼠", Color.web("#71686c"));
        JapaneseColors.put("葡萄鼠", Color.web("#705b67"));
        JapaneseColors.put("濃色", Color.web("#634950"));
        JapaneseColors.put("紫鳶", Color.web("#5f414b"));
        JapaneseColors.put("濃鼠 ", Color.web("#4f455c"));
        JapaneseColors.put("藤煤竹 ", Color.web("#5a5359"));
        JapaneseColors.put("滅紫", Color.web("#594255"));
        JapaneseColors.put("紅消鼠", Color.web("#524748"));
        JapaneseColors.put("似せ紫", Color.web("#513743"));
        JapaneseColors.put("灰黄緑 ", Color.web("#e6eae3"));
        JapaneseColors.put("蕎麦切色 ", Color.web("#d4dcd6"));
        JapaneseColors.put("薄雲鼠", Color.web("#d4dcda"));
        JapaneseColors.put("枯野色", Color.web("#d3cbc6"));
        JapaneseColors.put("潤色", Color.web("#c8c2be"));
        JapaneseColors.put("利休白茶", Color.web("#b3ada0"));
        JapaneseColors.put("茶鼠 ", Color.web("#a99e93"));
        JapaneseColors.put("胡桃染", Color.web("#a58f86"));
        JapaneseColors.put("江戸鼠", Color.web("#928178"));
        JapaneseColors.put("煤色", Color.web("#887f7a"));
        JapaneseColors.put("丁子茶", Color.web("#b4866b"));
        JapaneseColors.put("柴染 ", Color.web("#b28c6e"));
        JapaneseColors.put("宗伝唐茶", Color.web("#a16d5d"));
        JapaneseColors.put("砺茶", Color.web("#9f6f55"));
        JapaneseColors.put("煎茶色", Color.web("#8c6450"));
        JapaneseColors.put("銀煤竹 ", Color.web("#856859"));
        JapaneseColors.put("黄枯茶", Color.web("#765c47"));
        JapaneseColors.put("煤竹色 ", Color.web("#6f514c"));
        JapaneseColors.put("焦茶", Color.web("#6f4b3e"));
        JapaneseColors.put("黒橡", Color.web("#544a47"));
        JapaneseColors.put("憲法色", Color.web("#543f32"));
        JapaneseColors.put("涅色", Color.web("#554738"));
        JapaneseColors.put("檳榔子染", Color.web("#433d3c"));
        JapaneseColors.put("黒鳶", Color.web("#432f2f"));
        JapaneseColors.put("赤墨", Color.web("#3f312b"));
        JapaneseColors.put("黒紅", Color.web("#302833"));
        JapaneseColors.put("白", Color.web("#ffffff"));
        JapaneseColors.put("胡粉色", Color.web("#fffffc"));
        JapaneseColors.put("卯の花色", Color.web("#f7fcfe"));
        JapaneseColors.put("白磁", Color.web("#f8fbf8"));
        JapaneseColors.put("生成り色", Color.web("#fbfaf5"));
        JapaneseColors.put("乳白色", Color.web("#f3f3f3"));
        JapaneseColors.put("白練", Color.web("#f3f3f2"));
        JapaneseColors.put("素色", Color.web("#eae5e3"));
        JapaneseColors.put("白梅鼠", Color.web("#e5e4e6"));
        JapaneseColors.put("白鼠", Color.web("#dcdddd"));
        JapaneseColors.put("絹鼠", Color.web("#dddcd6"));
        JapaneseColors.put("灰青", Color.web("#c0c6c9"));
        JapaneseColors.put("銀鼠", Color.web("#afafb0"));
        JapaneseColors.put("薄鈍", Color.web("#adadad"));
        JapaneseColors.put("薄墨色", Color.web("#a3a3a2"));
        JapaneseColors.put("錫色", Color.web("#9ea1a3"));
        JapaneseColors.put("素鼠", Color.web("#9fa0a0"));
        JapaneseColors.put("鼠色", Color.web("#949495"));
        JapaneseColors.put("源氏鼠", Color.web("#888084"));
        JapaneseColors.put("灰色", Color.web("#7d7d7d"));
        JapaneseColors.put("鉛色", Color.web("#7b7c7d"));
        JapaneseColors.put("鈍色", Color.web("#727171"));
        JapaneseColors.put("墨", Color.web("#595857"));
        JapaneseColors.put("丼鼠", Color.web("#595455"));
        JapaneseColors.put("消炭色", Color.web("#524e4d"));
        JapaneseColors.put("藍墨茶 ", Color.web("#474a4d"));
        JapaneseColors.put("羊羹色", Color.web("#383c3c"));
        JapaneseColors.put("蝋色", Color.web("#2b2b2b"));
        JapaneseColors.put("黒", Color.web("#2b2b2b"));
        JapaneseColors.put("烏羽色", Color.web("#180614"));
        JapaneseColors.put("鉄黒", Color.web("#281a14"));
        JapaneseColors.put("濡羽色", Color.web("#000b00"));
        JapaneseColors.put("黒壇", Color.web("#250d00"));
        JapaneseColors.put("憲法黒茶 ", Color.web("#241a08"));
        JapaneseColors.put("暗黒色", Color.web("#16160e"));
        JapaneseColors.put("萌葱色 ", Color.web("#006e54"));
        JapaneseColors.put("花緑青 ", Color.web("#00a381"));
        JapaneseColors.put("翡翠色 ", Color.web("#38b48b"));
        JapaneseColors.put("青緑", Color.web("#00a497"));
        JapaneseColors.put("水浅葱 ", Color.web("#80aba9"));
        JapaneseColors.put("錆浅葱 ", Color.web("#5c9291"));
        JapaneseColors.put("青碧", Color.web("#478384"));
        JapaneseColors.put("御召茶", Color.web("#43676b"));
        JapaneseColors.put("湊鼠", Color.web("#80989b"));
        JapaneseColors.put("高麗納戸 ", Color.web("#2c4f54"));
        JapaneseColors.put("百入茶", Color.web("#1f3134"));
        JapaneseColors.put("錆鼠", Color.web("#47585c"));
        JapaneseColors.put("錆鉄御納戸", Color.web("#485859"));
        JapaneseColors.put("藍鼠", Color.web("#6c848d"));
        JapaneseColors.put("錆御納戸", Color.web("#53727d"));
        JapaneseColors.put("舛花色 ", Color.web("#5b7e91"));
        JapaneseColors.put("熨斗目花色 ", Color.web("#426579"));
        JapaneseColors.put("御召御納戸 ", Color.web("#4c6473"));
        JapaneseColors.put("鉄御納戸 ", Color.web("#455765"));
        JapaneseColors.put("紺鼠 ", Color.web("#44617b"));
        JapaneseColors.put("藍鉄", Color.web("#393f4c"));
        JapaneseColors.put("青褐", Color.web("#393e4f"));
        JapaneseColors.put("褐返", Color.web("#203744"));
        JapaneseColors.put("褐色", Color.web("#4d4c61"));
        JapaneseColors.put("月白", Color.web("#eaf4fc"));
        JapaneseColors.put("白菫色 ", Color.web("#eaedf7"));
        JapaneseColors.put("白花色", Color.web("#e8ecef"));
        JapaneseColors.put("藍白", Color.web("#ebf6f7"));
        JapaneseColors.put("白藍", Color.web("#c1e4e9"));
        JapaneseColors.put("水色", Color.web("#bce2e8"));
        JapaneseColors.put("瓶覗", Color.web("#a2d7dd"));
        JapaneseColors.put("秘色色 ", Color.web("#abced8"));
        JapaneseColors.put("空色", Color.web("#a0d8ef"));
        JapaneseColors.put("勿忘草色 ", Color.web("#89c3eb"));
        JapaneseColors.put("青藤色 ", Color.web("#84a2d4"));
        JapaneseColors.put("白群", Color.web("#83ccd2"));
        JapaneseColors.put("浅縹", Color.web("#84b9cb"));
        JapaneseColors.put("薄花色", Color.web("#698aab"));
        JapaneseColors.put("納戸色", Color.web("#008899"));
        JapaneseColors.put("浅葱色", Color.web("#00a3af"));
        JapaneseColors.put("花浅葱 ", Color.web("#2a83a2"));
        JapaneseColors.put("新橋色", Color.web("#59b9c6"));
        JapaneseColors.put("天色", Color.web("#2ca9e1"));
        JapaneseColors.put("露草色 ", Color.web("#38a1db"));
        JapaneseColors.put("青", Color.web("#0095d9"));
        JapaneseColors.put("薄藍", Color.web("#0094c8"));
        JapaneseColors.put("縹色", Color.web("#2792c3"));
        JapaneseColors.put("紺碧", Color.web("#007bbb"));
        JapaneseColors.put("薄群青", Color.web("#5383c3"));
        JapaneseColors.put("薄花桜", Color.web("#5a79ba"));
        JapaneseColors.put("群青色", Color.web("#4c6cb3"));
        JapaneseColors.put("杜若色", Color.web("#3e62ad"));
        JapaneseColors.put("瑠璃色", Color.web("#1e50a2"));
        JapaneseColors.put("薄縹", Color.web("#507ea4"));
        JapaneseColors.put("瑠璃紺", Color.web("#19448e"));
        JapaneseColors.put("紺瑠璃 ", Color.web("#164a84"));
        JapaneseColors.put("藍色", Color.web("#165e83"));
        JapaneseColors.put("青藍", Color.web("#274a78"));
        JapaneseColors.put("深縹", Color.web("#2a4073"));
        JapaneseColors.put("紺色", Color.web("#223a70"));
        JapaneseColors.put("紺青", Color.web("#192f60"));
        JapaneseColors.put("留紺", Color.web("#1c305c"));
        JapaneseColors.put("濃藍", Color.web("#0f2350"));
        JapaneseColors.put("鉄紺", Color.web("#17184b"));
        JapaneseColors.put("漆黒", Color.web("#0d0015"));
        JapaneseColors.put("淡藤色 ", Color.web("#bbc8e6"));
        JapaneseColors.put("藤色", Color.web("#bbbcde"));
        JapaneseColors.put("紅掛空色", Color.web("#8491c3"));
        JapaneseColors.put("紅碧", Color.web("#8491c3"));
        JapaneseColors.put("紺桔梗 ", Color.web("#4d5aaf"));
        JapaneseColors.put("花色", Color.web("#4d5aaf"));
        JapaneseColors.put("紺藍", Color.web("#4a488e"));
        JapaneseColors.put("紅桔梗 ", Color.web("#4d4398"));
        JapaneseColors.put("桔梗色", Color.web("#5654a2"));
        JapaneseColors.put("藤納戸", Color.web("#706caa"));
        JapaneseColors.put("紅掛花色 ", Color.web("#68699b"));
        JapaneseColors.put("紫苑色 ", Color.web("#867ba9"));
        JapaneseColors.put("白藤色 ", Color.web("#dbd0e6"));
        JapaneseColors.put("藤紫", Color.web("#a59aca"));
        JapaneseColors.put("菫色", Color.web("#7058a3"));
        JapaneseColors.put("青紫", Color.web("#674598"));
        JapaneseColors.put("菖蒲色", Color.web("#674196"));
        JapaneseColors.put("竜胆色", Color.web("#9079ad"));
        JapaneseColors.put("江戸紫 ", Color.web("#745399"));
        JapaneseColors.put("本紫", Color.web("#65318e"));
        JapaneseColors.put("葡萄色", Color.web("#522f60"));
        JapaneseColors.put("深紫", Color.web("#493759"));
        JapaneseColors.put("紫黒", Color.web("#2e2930"));
        JapaneseColors.put("紫", Color.web("#884898"));
        JapaneseColors.put("薄葡萄", Color.web("#c0a2c7"));
        JapaneseColors.put("紫紺", Color.web("#460e44"));
        JapaneseColors.put("暗紅色 ", Color.web("#74325c"));
        JapaneseColors.put("桑の実色 ", Color.web("#55295b"));
        JapaneseColors.put("黄金", Color.web("#e6b422"));
        JapaneseColors.put("櫨染", Color.web("#d9a62e"));
        JapaneseColors.put("黄朽葉色 ", Color.web("#d3a243"));
        JapaneseColors.put("山吹茶 ", Color.web("#c89932"));
        JapaneseColors.put("芥子色 ", Color.web("#d0af4c"));
        JapaneseColors.put("豆がら茶 ", Color.web("#8b968d"));
        JapaneseColors.put("麹塵 ", Color.web("#6e7955"));
        JapaneseColors.put("山鳩色", Color.web("#767c6b"));
        JapaneseColors.put("利休鼠 ", Color.web("#888e7e"));
        JapaneseColors.put("海松茶 ", Color.web("#5a544b"));
        JapaneseColors.put("藍海松茶 ", Color.web("#56564b"));
        JapaneseColors.put("藍媚茶 ", Color.web("#56564b"));
        JapaneseColors.put("千歳茶", Color.web("#494a41"));
        JapaneseColors.put("岩井茶", Color.web("#6b6f59"));
        JapaneseColors.put("仙斎茶 ", Color.web("#474b42"));
        JapaneseColors.put("黒緑", Color.web("#333631"));
        JapaneseColors.put("柳煤竹 ", Color.web("#5b6356"));
        JapaneseColors.put("樺茶色 ", Color.web("#726250"));
        JapaneseColors.put("空五倍子色 ", Color.web("#9d896c"));
        JapaneseColors.put("生壁色 ", Color.web("#94846a"));
        JapaneseColors.put("肥後煤竹 ", Color.web("#897858"));
        JapaneseColors.put("媚茶 ", Color.web("#716246"));
        JapaneseColors.put("白橡", Color.web("#cbb994"));
        JapaneseColors.put("亜麻色", Color.web("#d6c6af"));
        JapaneseColors.put("榛色", Color.web("#bfa46f"));
        JapaneseColors.put("灰汁色 ", Color.web("#9e9478"));
        JapaneseColors.put("利休茶 ", Color.web("#a59564"));
        JapaneseColors.put("鶯茶", Color.web("#715c1f"));
        JapaneseColors.put("木蘭色", Color.web("#c7b370"));
        JapaneseColors.put("砂色", Color.web("#dcd3b2"));
        JapaneseColors.put("油色", Color.web("#a19361"));
        JapaneseColors.put("利休色", Color.web("#8f8667"));
        JapaneseColors.put("梅幸茶 ", Color.web("#887938"));
        JapaneseColors.put("璃寛茶 ", Color.web("#6a5d21"));
        JapaneseColors.put("黄海松茶 ", Color.web("#918754"));
        JapaneseColors.put("菜種油色", Color.web("#a69425"));
        JapaneseColors.put("青朽葉 ", Color.web("#ada250"));
        JapaneseColors.put("根岸色 ", Color.web("#938b4b"));
        JapaneseColors.put("鶸茶", Color.web("#8c8861"));
        JapaneseColors.put("柳茶", Color.web("#a1a46d"));
        JapaneseColors.put("海松色 ", Color.web("#726d40"));
        JapaneseColors.put("鶯色 ", Color.web("#928c36"));
        JapaneseColors.put("緑黄色 ", Color.web("#dccb18"));
        JapaneseColors.put("鶸色 ", Color.web("#d7cf3a"));
        JapaneseColors.put("抹茶色", Color.web("#c5c56a"));
        JapaneseColors.put("若草色 ", Color.web("#c3d825"));
        JapaneseColors.put("黄緑", Color.web("#b8d200"));
        JapaneseColors.put("若芽色", Color.web("#e0ebaf"));
        JapaneseColors.put("若菜色", Color.web("#d8e698"));
        JapaneseColors.put("若苗色 ", Color.web("#c7dc68"));
        JapaneseColors.put("青丹", Color.web("#99ab4e"));
        JapaneseColors.put("草色", Color.web("#7b8d42"));
        JapaneseColors.put("苔色", Color.web("#69821b"));
        JapaneseColors.put("萌黄", Color.web("#aacf53"));
        JapaneseColors.put("苗色", Color.web("#b0ca71"));
        JapaneseColors.put("若葉色", Color.web("#b9d08b"));
        JapaneseColors.put("松葉色", Color.web("#839b5c"));
        JapaneseColors.put("夏虫色 ", Color.web("#cee4ae"));
        JapaneseColors.put("鶸萌黄 ", Color.web("#82ae46"));
        JapaneseColors.put("柳色", Color.web("#a8c97f"));
        JapaneseColors.put("青白橡 ", Color.web("#9ba88d"));
        JapaneseColors.put("柳鼠", Color.web("#c8d5bb"));
        JapaneseColors.put("裏葉柳 ", Color.web("#c1d8ac"));
        JapaneseColors.put("山葵色", Color.web("#a8bf93"));
        JapaneseColors.put("老竹色", Color.web("#769164"));
        JapaneseColors.put("白緑 ", Color.web("#d6e9ca"));
        JapaneseColors.put("淡萌黄 ", Color.web("#93ca76"));
        JapaneseColors.put("柳染", Color.web("#93b881"));
        JapaneseColors.put("薄萌葱", Color.web("#badcad"));
        JapaneseColors.put("深川鼠", Color.web("#97a791"));
        JapaneseColors.put("若緑", Color.web("#98d98e"));
        JapaneseColors.put("浅緑", Color.web("#88cb7f"));
        JapaneseColors.put("薄緑", Color.web("#69b076"));
        JapaneseColors.put("青鈍", Color.web("#6b7b6e"));
        JapaneseColors.put("青磁鼠", Color.web("#bed2c3"));
        JapaneseColors.put("薄青", Color.web("#93b69c"));
        JapaneseColors.put("錆青磁 ", Color.web("#a6c8b2"));
        JapaneseColors.put("緑青色", Color.web("#47885e"));
        JapaneseColors.put("千歳緑", Color.web("#316745"));
        JapaneseColors.put("若竹色", Color.web("#68be8d"));
        JapaneseColors.put("緑", Color.web("#3eb370"));
        JapaneseColors.put("常磐色", Color.web("#007b43"));
        JapaneseColors.put("千草鼠", Color.web("#bed3ca"));
        JapaneseColors.put("千草色", Color.web("#92b5a9"));
        JapaneseColors.put("青磁色", Color.web("#7ebea5"));
        JapaneseColors.put("青竹色", Color.web("#7ebeab"));
        JapaneseColors.put("常磐緑", Color.web("#028760"));
        JapaneseColors.put("木賊色", Color.web("#3b7960"));
        JapaneseColors.put("天鵞絨 ", Color.web("#2f5d50"));
        JapaneseColors.put("虫襖", Color.web("#3a5b52"));
        JapaneseColors.put("革色", Color.web("#475950"));
        JapaneseColors.put("深緑", Color.web("#00552e"));
        JapaneseColors.put("鉄色", Color.web("#005243"));
        JapaneseColors.put("小豆色", Color.web("#96514d"));
        JapaneseColors.put("枯茶", Color.web("#8d6449"));
        JapaneseColors.put("饴色", Color.web("#deb068"));
        JapaneseColors.put("骆驼色", Color.web("#bf794e"));
        JapaneseColors.put("土色", Color.web("#bc763c"));
        JapaneseColors.put("黄唐色", Color.web("#b98c46"));
        JapaneseColors.put("桑染", Color.web("#b79b5b"));
        JapaneseColors.put("栌色", Color.web("#b77b57"));
        JapaneseColors.put("黄橡", Color.web("#b68d4c"));
        JapaneseColors.put("丁字染", Color.web("#ad7d4c"));
        JapaneseColors.put("香染", Color.web("#ad7d4c"));
        JapaneseColors.put("枇杷茶", Color.web("#ae7c4f"));
        JapaneseColors.put("芝翫茶", Color.web("#ad7e4e"));
        JapaneseColors.put("焦香", Color.web("#ae7c58"));
        JapaneseColors.put("胡桃色", Color.web("#a86f4c"));
        JapaneseColors.put("渋纸色", Color.web("#946243"));
        JapaneseColors.put("朽葉色", Color.web("#917347"));
        JapaneseColors.put("桑茶", Color.web("#956f29"));
        JapaneseColors.put("路考茶", Color.web("#8c7042"));
        JapaneseColors.put("国防色", Color.web("#7b6c3e"));
        JapaneseColors.put("伽羅色", Color.web("#d8a373"));
        JapaneseColors.put("江戸茶", Color.web("#cd8c5c"));
        JapaneseColors.put("樺色", Color.web("#cd5e3c"));
        JapaneseColors.put("紅鬱金", Color.web("#cb8347"));
        JapaneseColors.put("土器色", Color.web("#c37854"));
        JapaneseColors.put("狐色", Color.web("#c38743"));
        JapaneseColors.put("黄土色", Color.web("#c39143"));
        JapaneseColors.put("琥珀色", Color.web("#bf783a"));
        JapaneseColors.put("赤茶", Color.web("#bb5535"));
        JapaneseColors.put("代赭", Color.web("#bb5520"));
        JapaneseColors.put("煉瓦色", Color.web("#b55233"));
        JapaneseColors.put("雀茶", Color.web("#aa4f37"));
        JapaneseColors.put("団十郎茶", Color.web("#9f563a"));
        JapaneseColors.put("柿渋色", Color.web("#9f563a"));
        JapaneseColors.put("紅鳶", Color.web("#9a493f"));
        JapaneseColors.put("灰茶", Color.web("#98623c"));
        JapaneseColors.put("茶色", Color.web("#965042"));
        JapaneseColors.put("檜皮色", Color.web("#965036"));
        JapaneseColors.put("鳶色", Color.web("#95483f"));
        JapaneseColors.put("柿茶", Color.web("#954e2a"));
        JapaneseColors.put("弁柄色", Color.web("#8f2e14"));
        JapaneseColors.put("赤錆色", Color.web("#8a3319"));
        JapaneseColors.put("褐色", Color.web("#8a3b00"));
        JapaneseColors.put("栗梅", Color.web("#852e19"));
        JapaneseColors.put("紅檜皮", Color.web("#7b4741"));
        JapaneseColors.put("海老茶", Color.web("#773c30"));
        JapaneseColors.put("唐茶", Color.web("#783c1d"));
        JapaneseColors.put("栗色", Color.web("#762f07"));
        JapaneseColors.put("赤銅色", Color.web("#752100"));
        JapaneseColors.put("錆色", Color.web("#6c3524"));
        JapaneseColors.put("赤褐色", Color.web("#683f36"));
        JapaneseColors.put("茶褐色", Color.web("#664032"));
        JapaneseColors.put("栗皮茶", Color.web("#6d3c32"));
        JapaneseColors.put("黒茶", Color.web("#583822"));
        JapaneseColors.put("葡萄茶", Color.web("#6c2c2f"));
        JapaneseColors.put("葡萄色", Color.web("#640125"));
        JapaneseColors.put("萱草色", Color.web("#f8b862"));
        JapaneseColors.put("柑子色", Color.web("#f6ad49"));
        JapaneseColors.put("金茶", Color.web("#f39800"));
        JapaneseColors.put("蜜柑色", Color.web("#f08300"));
        JapaneseColors.put("鉛丹色", Color.web("#ec6d51"));
        JapaneseColors.put("黄丹", Color.web("#ee7948"));
        JapaneseColors.put("柿色", Color.web("#ed6d3d"));
        JapaneseColors.put("黄赤", Color.web("#ec6800"));
        JapaneseColors.put("人参色", Color.web("#ec6800"));
        JapaneseColors.put("橙色", Color.web("#ee7800"));
        JapaneseColors.put("照柿", Color.web("#eb6238"));
        JapaneseColors.put("赤橙", Color.web("#ea5506"));
        JapaneseColors.put("金赤", Color.web("#ea5506"));
        JapaneseColors.put("朱色", Color.web("#eb6101"));
        JapaneseColors.put("小麦色", Color.web("#e49e61"));
        JapaneseColors.put("丹色", Color.web("#e45e32"));
        JapaneseColors.put("黄茶", Color.web("#e17b34"));
        JapaneseColors.put("肉桂色", Color.web("#dd7a56"));
        JapaneseColors.put("赤朽葉色", Color.web("#db8449"));
        JapaneseColors.put("黄櫨染", Color.web("#d66a35"));
        JapaneseColors.put("蒲公英色", Color.web("#ffd900"));
        JapaneseColors.put("黄色", Color.web("#ffd900"));
        JapaneseColors.put("中黄", Color.web("#ffea00"));
        JapaneseColors.put("菜の花色", Color.web("#ffec47"));
        JapaneseColors.put("黄檗色", Color.web("#fef263"));
        JapaneseColors.put("卵色", Color.web("#fcd575"));
        JapaneseColors.put("花葉色", Color.web("#fbd26b"));
        JapaneseColors.put("刈安色", Color.web("#f5e56b"));
        JapaneseColors.put("玉蜀黍色", Color.web("#eec362"));
        JapaneseColors.put("金糸雀色", Color.web("#ebd842"));
        JapaneseColors.put("黄支子色", Color.web("#ffdb4f"));
        JapaneseColors.put("支子色", Color.web("#fbca4d"));
        JapaneseColors.put("向日葵色", Color.web("#fcc800"));
        JapaneseColors.put("山吹色", Color.web("#f8b500"));
        JapaneseColors.put("鬱金色", Color.web("#fabf14"));
        JapaneseColors.put("藤黄", Color.web("#f7c114"));
        JapaneseColors.put("金色", Color.web("#e6b422"));
        JapaneseColors.put("桜色", Color.web("#bf242a"));
        JapaneseColors.put("薄桜", Color.web("#fdeff2"));
        JapaneseColors.put("桜鼠", Color.web("#e9dfe5"));
        JapaneseColors.put("鸨鼠", Color.web("#e4d2d8"));
        JapaneseColors.put("虹色", Color.web("#f6bfbc"));
        JapaneseColors.put("珊瑚色", Color.web("#f5b1aa"));
        JapaneseColors.put("一斤染", Color.web("#f5b199"));
        JapaneseColors.put("宍色", Color.web("#efab93"));
        JapaneseColors.put("红梅色", Color.web("#f2a0a1"));
        JapaneseColors.put("薄红", Color.web("#f0908d"));
        JapaneseColors.put("甚三红", Color.web("#ee827c"));
        JapaneseColors.put("桃色", Color.web("#f09199"));
        JapaneseColors.put("鸨色", Color.web("#f4b3c2"));
        JapaneseColors.put("撫子色", Color.web("#eebbcb"));
        JapaneseColors.put("灰梅", Color.web("#e8d3c7"));
        JapaneseColors.put("灰桜", Color.web("#e8d3d1"));
        JapaneseColors.put("淡红藤", Color.web("#e6cde3"));
        JapaneseColors.put("石竹色", Color.web("#e5abbe"));
        JapaneseColors.put("薄红梅", Color.web("#e597b2"));
        JapaneseColors.put("桃花色", Color.web("#e198b4"));
        JapaneseColors.put("水柿", Color.web("#e4ab9b"));
        JapaneseColors.put("ときがら茶", Color.web("#e09e87"));
        JapaneseColors.put("退红", Color.web("#d69090"));
        JapaneseColors.put("薄柿", Color.web("#d4acad"));
        JapaneseColors.put("长春色", Color.web("#c97586"));
        JapaneseColors.put("梅鼠", Color.web("#c099a0"));
        JapaneseColors.put("鸨浅葱", Color.web("#b88884"));
        JapaneseColors.put("梅染", Color.web("#b48a76"));
        JapaneseColors.put("苏芳香", Color.web("#a86965"));
        JapaneseColors.put("浅苏芳", Color.web("#a25768"));
        JapaneseColors.put("真朱", Color.web("#ec6d71"));
        JapaneseColors.put("赤紫", Color.web("#eb6ea5"));
        JapaneseColors.put("躑躅色", Color.web("#e95295"));
        JapaneseColors.put("牡丹色", Color.web("#e7609e"));
        JapaneseColors.put("今样色", Color.web("#d0576b"));
        JapaneseColors.put("中红", Color.web("#c85179"));
        JapaneseColors.put("蔷薇色", Color.web("#e9546b"));
        JapaneseColors.put("韩红", Color.web("#e95464"));
        JapaneseColors.put("银朱", Color.web("#c85554"));
        JapaneseColors.put("赤红", Color.web("#c53d43"));
        JapaneseColors.put("红緋", Color.web("#e83929"));
        JapaneseColors.put("赤", Color.web("#e60033"));
        JapaneseColors.put("猩緋", Color.web("#e2041b"));
        JapaneseColors.put("红", Color.web("#d7003a"));
        JapaneseColors.put("深緋", Color.web("#c9171e"));
        JapaneseColors.put("绯色", Color.web("#d3381c"));
        JapaneseColors.put("赤丹", Color.web("#ce5242"));
        JapaneseColors.put("红赤", Color.web("#d9333f"));
        JapaneseColors.put("胭脂", Color.web("#b94047"));
        JapaneseColors.put("朱緋", Color.web("#ba2636"));
        JapaneseColors.put("茜色", Color.web("#b7282e"));
        JapaneseColors.put("深海老茶", Color.web("#a73836"));
        JapaneseColors.put("苏芳", Color.web("#9e3d3f"));
        JapaneseColors.put("真红", Color.web("#a22041"));
        JapaneseColors.put("浓红", Color.web("#a22041"));
        JapaneseColors.put("象牙色", Color.web("#f8f4e6"));
        JapaneseColors.put("练色", Color.web("#ede4cd"));
        JapaneseColors.put("灰白色", Color.web("#e9e4d4"));
        JapaneseColors.put("蒸栗色", Color.web("#ede1a9"));
        JapaneseColors.put("女郎花", Color.web("#f2f2b0"));
        JapaneseColors.put("枯草色", Color.web("#e4dc8a"));
        JapaneseColors.put("淡黄", Color.web("#f8e58c"));
        JapaneseColors.put("白茶", Color.web("#ddbb99"));
        JapaneseColors.put("赤白橡", Color.web("#d7a98c"));
        JapaneseColors.put("洗柿", Color.web("#f2c9ac"));
        JapaneseColors.put("鸟の子色", Color.web("#fff1cf"));
        JapaneseColors.put("蜂蜜色", Color.web("#fddea5"));
        JapaneseColors.put("肌色", Color.web("#fce2c4"));
        JapaneseColors.put("薄卵色", Color.web("#fde8d0"));
        JapaneseColors.put("雄黄", Color.web("#f9c89b"));
        JapaneseColors.put("洒落柿", Color.web("#f7bd8f"));
        JapaneseColors.put("赤香", Color.web("#f6b894"));
        JapaneseColors.put("砥粉色", Color.web("#f4dda5"));
        JapaneseColors.put("肉色", Color.web("#f1bf99"));
        JapaneseColors.put("人色", Color.web("#f1bf99"));
        JapaneseColors.put("丁子色", Color.web("#efcd9a"));
        JapaneseColors.put("香色", Color.web("#efcd9a"));
        JapaneseColors.put("薄香", Color.web("#f0cfa0"));
        JapaneseColors.put("浅黄", Color.web("#edd3a1"));
        JapaneseColors.put("枯色", Color.web("#e0c38c"));
        JapaneseColors.put("淡香", Color.web("#f3bf88"));
        JapaneseColors.put("杏色", Color.web("#f7b977"));
        JapaneseColors.put("东云色", Color.web("#f19072"));
        JapaneseColors.put("曙色", Color.web("#f19072"));
        JapaneseColors.put("珊瑚朱色", Color.web("#ee836f"));
        JapaneseColors.put("深支子", Color.web("#eb9b6f"));
        JapaneseColors.put("纁", Color.web("#e0815e"));
        JapaneseColors.put("浅绯", Color.web("#df7163"));
        JapaneseColors.put("真赭", Color.web("#d57c6b"));
        JapaneseColors.put("洗朱", Color.web("#d0826c"));
        JapaneseColors.put("遠州茶", Color.web("#ca8269"));
        JapaneseColors.put("红桦色", Color.web("#bb5548"));
        JapaneseColors.put("赭", Color.web("#ab6953"));
        return JapaneseColors;
    }

    public static Map<Color, String> japaneseColorNames() {
        if (JapaneseColorNames != null) {
            return JapaneseColorNames;
        }
        Map<String, Color> named = japaneseColors();
        JapaneseColorNames = new LinkedHashMap<>();
        for (String name : named.keySet()) {
            JapaneseColorNames.put(named.get(name), name);
        }
        return JapaneseColorNames;
    }

    public static List<Color> japaneseColorValues() {
        Set<Color> keys = japaneseColorNames().keySet();
        List<Color> colors = new ArrayList<>();
        colors.addAll(keys);
        return colors;
    }

    public static Map<String, Color> webColors() {
        if (WebColors != null) {
            return WebColors;
        }
        WebColors = new HashMap<>();
        WebColors.put("aliceblue", Color.ALICEBLUE);
        WebColors.put("antiquewhite", Color.ANTIQUEWHITE);
        WebColors.put("aqua", Color.AQUA);
        WebColors.put("aquamarine", Color.AQUAMARINE);
        WebColors.put("azure", Color.AZURE);
        WebColors.put("beige", Color.BEIGE);
        WebColors.put("bisque", Color.BISQUE);
        WebColors.put("black", Color.BLACK);
        WebColors.put("blanchedalmond", Color.BLANCHEDALMOND);
        WebColors.put("blue", Color.BLUE);
        WebColors.put("blueviolet", Color.BLUEVIOLET);
        WebColors.put("brown", Color.BROWN);
        WebColors.put("burlywood", Color.BURLYWOOD);
        WebColors.put("cadetblue", Color.CADETBLUE);
        WebColors.put("chartreuse", Color.CHARTREUSE);
        WebColors.put("chocolate", Color.CHOCOLATE);
        WebColors.put("coral", Color.CORAL);
        WebColors.put("cornflowerblue", Color.CORNFLOWERBLUE);
        WebColors.put("cornsilk", Color.CORNSILK);
        WebColors.put("crimson", Color.CRIMSON);
        WebColors.put("cyan", Color.CYAN);
        WebColors.put("darkblue", Color.DARKBLUE);
        WebColors.put("darkcyan", Color.DARKCYAN);
        WebColors.put("darkgoldenrod", Color.DARKGOLDENROD);
        WebColors.put("darkgray", Color.DARKGRAY);
        WebColors.put("darkgreen", Color.DARKGREEN);
        WebColors.put("darkgrey", Color.DARKGREY);
        WebColors.put("darkkhaki", Color.DARKKHAKI);
        WebColors.put("darkmagenta", Color.DARKMAGENTA);
        WebColors.put("darkolivegreen", Color.DARKOLIVEGREEN);
        WebColors.put("darkorange", Color.DARKORANGE);
        WebColors.put("darkorchid", Color.DARKORCHID);
        WebColors.put("darkred", Color.DARKRED);
        WebColors.put("darksalmon", Color.DARKSALMON);
        WebColors.put("darkseagreen", Color.DARKSEAGREEN);
        WebColors.put("darkslateblue", Color.DARKSLATEBLUE);
        WebColors.put("darkslategray", Color.DARKSLATEGRAY);
        WebColors.put("darkslategrey", Color.DARKSLATEGREY);
        WebColors.put("darkturquoise", Color.DARKTURQUOISE);
        WebColors.put("darkviolet", Color.DARKVIOLET);
        WebColors.put("deeppink", Color.DEEPPINK);
        WebColors.put("deepskyblue", Color.DEEPSKYBLUE);
        WebColors.put("dimgray", Color.DIMGRAY);
        WebColors.put("dimgrey", Color.DIMGREY);
        WebColors.put("dodgerblue", Color.DODGERBLUE);
        WebColors.put("firebrick", Color.FIREBRICK);
        WebColors.put("floralwhite", Color.FLORALWHITE);
        WebColors.put("forestgreen", Color.FORESTGREEN);
        WebColors.put("fuchsia", Color.FUCHSIA);
        WebColors.put("gainsboro", Color.GAINSBORO);
        WebColors.put("ghostwhite", Color.GHOSTWHITE);
        WebColors.put("gold", Color.GOLD);
        WebColors.put("goldenrod", Color.GOLDENROD);
        WebColors.put("gray", Color.GRAY);
        WebColors.put("green", Color.GREEN);
        WebColors.put("greenyellow", Color.GREENYELLOW);
        WebColors.put("grey", Color.GREY);
        WebColors.put("honeydew", Color.HONEYDEW);
        WebColors.put("hotpink", Color.HOTPINK);
        WebColors.put("indianred", Color.INDIANRED);
        WebColors.put("indigo", Color.INDIGO);
        WebColors.put("ivory", Color.IVORY);
        WebColors.put("khaki", Color.KHAKI);
        WebColors.put("lavender", Color.LAVENDER);
        WebColors.put("lavenderblush", Color.LAVENDERBLUSH);
        WebColors.put("lawngreen", Color.LAWNGREEN);
        WebColors.put("lemonchiffon", Color.LEMONCHIFFON);
        WebColors.put("lightblue", Color.LIGHTBLUE);
        WebColors.put("lightcoral", Color.LIGHTCORAL);
        WebColors.put("lightcyan", Color.LIGHTCYAN);
        WebColors.put("lightgoldenrodyellow", Color.LIGHTGOLDENRODYELLOW);
        WebColors.put("lightgray", Color.LIGHTGRAY);
        WebColors.put("lightgreen", Color.LIGHTGREEN);
        WebColors.put("lightgrey", Color.LIGHTGREY);
        WebColors.put("lightpink", Color.LIGHTPINK);
        WebColors.put("lightsalmon", Color.LIGHTSALMON);
        WebColors.put("lightseagreen", Color.LIGHTSEAGREEN);
        WebColors.put("lightskyblue", Color.LIGHTSKYBLUE);
        WebColors.put("lightslategray", Color.LIGHTSLATEGRAY);
        WebColors.put("lightslategrey", Color.LIGHTSLATEGREY);
        WebColors.put("lightsteelblue", Color.LIGHTSTEELBLUE);
        WebColors.put("lightyellow", Color.LIGHTYELLOW);
        WebColors.put("lime", Color.LIME);
        WebColors.put("limegreen", Color.LIMEGREEN);
        WebColors.put("linen", Color.LINEN);
        WebColors.put("magenta", Color.MAGENTA);
        WebColors.put("maroon", Color.MAROON);
        WebColors.put("mediumaquamarine", Color.MEDIUMAQUAMARINE);
        WebColors.put("mediumblue", Color.MEDIUMBLUE);
        WebColors.put("mediumorchid", Color.MEDIUMORCHID);
        WebColors.put("mediumpurple", Color.MEDIUMPURPLE);
        WebColors.put("mediumseagreen", Color.MEDIUMSEAGREEN);
        WebColors.put("mediumslateblue", Color.MEDIUMSLATEBLUE);
        WebColors.put("mediumspringgreen", Color.MEDIUMSPRINGGREEN);
        WebColors.put("mediumturquoise", Color.MEDIUMTURQUOISE);
        WebColors.put("mediumvioletred", Color.MEDIUMVIOLETRED);
        WebColors.put("midnightblue", Color.MIDNIGHTBLUE);
        WebColors.put("mintcream", Color.MINTCREAM);
        WebColors.put("mistyrose", Color.MISTYROSE);
        WebColors.put("moccasin", Color.MOCCASIN);
        WebColors.put("navajowhite", Color.NAVAJOWHITE);
        WebColors.put("navy", Color.NAVY);
        WebColors.put("oldlace", Color.OLDLACE);
        WebColors.put("olive", Color.OLIVE);
        WebColors.put("olivedrab", Color.OLIVEDRAB);
        WebColors.put("orange", Color.ORANGE);
        WebColors.put("orangered", Color.ORANGERED);
        WebColors.put("orchid", Color.ORCHID);
        WebColors.put("palegoldenrod", Color.PALEGOLDENROD);
        WebColors.put("palegreen", Color.PALEGREEN);
        WebColors.put("paleturquoise", Color.PALETURQUOISE);
        WebColors.put("palevioletred", Color.PALEVIOLETRED);
        WebColors.put("papayawhip", Color.PAPAYAWHIP);
        WebColors.put("peachpuff", Color.PEACHPUFF);
        WebColors.put("peru", Color.PERU);
        WebColors.put("pink", Color.PINK);
        WebColors.put("plum", Color.PLUM);
        WebColors.put("powderblue", Color.POWDERBLUE);
        WebColors.put("purple", Color.PURPLE);
        WebColors.put("red", Color.RED);
        WebColors.put("rosybrown", Color.ROSYBROWN);
        WebColors.put("royalblue", Color.ROYALBLUE);
        WebColors.put("saddlebrown", Color.SADDLEBROWN);
        WebColors.put("salmon", Color.SALMON);
        WebColors.put("sandybrown", Color.SANDYBROWN);
        WebColors.put("seagreen", Color.SEAGREEN);
        WebColors.put("seashell", Color.SEASHELL);
        WebColors.put("sienna", Color.SIENNA);
        WebColors.put("silver", Color.SILVER);
        WebColors.put("skyblue", Color.SKYBLUE);
        WebColors.put("slateblue", Color.SLATEBLUE);
        WebColors.put("slategray", Color.SLATEGRAY);
        WebColors.put("slategrey", Color.SLATEGREY);
        WebColors.put("snow", Color.SNOW);
        WebColors.put("springgreen", Color.SPRINGGREEN);
        WebColors.put("steelblue", Color.STEELBLUE);
        WebColors.put("tan", Color.TAN);
        WebColors.put("teal", Color.TEAL);
        WebColors.put("thistle", Color.THISTLE);
        WebColors.put("tomato", Color.TOMATO);
        WebColors.put("transparent", Color.TRANSPARENT);
        WebColors.put("turquoise", Color.TURQUOISE);
        WebColors.put("violet", Color.VIOLET);
        WebColors.put("wheat", Color.WHEAT);
        WebColors.put("white", Color.WHITE);
        WebColors.put("whitesmoke", Color.WHITESMOKE);
        WebColors.put("yellow", Color.YELLOW);
        WebColors.put("yellowgreen", Color.YELLOWGREEN);
        return WebColors;
    }

    public static Map<Color, String> webColorNames() {
        if (WebColorNames != null) {
            return WebColorNames;
        }
        Map<String, Color> named = webColors();
        WebColorNames = new HashMap<>();
        for (String name : named.keySet()) {
            WebColorNames.put(named.get(name), name);
        }
        return WebColorNames;
    }

    public static List<Color> webColorValues() {
        Set<Color> colors = webColorNames().keySet();
        List<Color> ordered = new ArrayList<>();
        ordered.addAll(colors);
        List<Color> special = new ArrayList<>();
        special.add(Color.WHITE);
        special.add(Color.BLACK);
        special.add(Color.TRANSPARENT);
        ordered.removeAll(special);
        Collections.sort(ordered, (Color o1, Color o2) -> compareColor(o1, o2));
        ordered.addAll(special);
        return ordered;
    }

    public static int compareColor(Color o1, Color o2) {
        double diff = o2.getHue() - o1.getHue();
        if (diff > 0) {
            return 1;
        } else if (diff < 0) {
            return -1;
        } else {
            diff = o2.getSaturation() - o1.getSaturation();
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                diff = o2.getBrightness() - o1.getBrightness();
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                } else {
                    diff = o2.getOpacity() - o1.getOpacity();
                    if (diff > 0) {
                        return 1;
                    } else if (diff < 0) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }

    public static String colorNameDisplay(Color color) {
        if (color == null) {
            return "";
        }
        ColorData data = TableColorData.read(color);
        if (data != null) {
            return data.display();
        }
        data = new ColorData(color.toString());
        return data.display();
    }

    public static String colorDisplaySimple(Color color) {
        if (color == null) {
            return "";
        }
        String s = color.toString() + "\n";
        s += "sRGB  " + message("Red") + ":" + Math.round(color.getRed() * 255) + " "
                + message("Green") + ":" + Math.round(color.getGreen() * 255) + " "
                + message("Blue") + ":" + Math.round(color.getBlue() * 255)
                + message("Opacity") + ":" + Math.round(color.getOpacity() * 100) + "%\n";
        s += "HSB  " + message("Hue") + ":" + Math.round(color.getHue()) + " "
                + message("Saturation") + ":" + Math.round(color.getSaturation() * 100) + "% "
                + message("Brightness") + ":" + Math.round(color.getBrightness() * 100) + "%\n";

        return s;
    }

    public static String rgb2Hex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static List<String> randomColorsHex(int size) {
        Random random = new Random();
        List<String> colors = new ArrayList<>();
        if (size > 256 * 256 * 256 - 1) {
            return null;
        }
        while (colors.size() < size) {
            while (true) {
                String color = String.format("#%02X%02X%02X",
                        random.nextInt(256),
                        random.nextInt(256),
                        random.nextInt(256));
                if (!"#FFFFFF".equals(color) && !colors.contains(color)) {
                    colors.add(color);
                    break;
                }
            }
        }
        return colors;
    }

    public static String randomColorExcept(Collection<String> excepts) {
        Random random = new Random();
        while (true) {
            String color = String.format("#%02X%02X%02X",
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256));
            if (!"#FFFFFF".equals(color) && !excepts.contains(color)) {
                return color;
            }
        }
    }

    public static String rgb2AlphaHex(Color color) {
        return String.format("#%02X%02X%02X%02X",
                (int) (color.getOpacity() * 255),
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static String rgb2css(Color color) {
        return "rgba(" + (int) (color.getRed() * 255) + ","
                + (int) (color.getGreen() * 255) + ","
                + (int) (color.getBlue() * 255) + ","
                + color.getOpacity() + ")";
    }

    public static String rgb2Hex(java.awt.Color color) {
        return String.format("#%02X%02X%02X",
                color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String rgb2AlphaHex(java.awt.Color color) {
        return String.format("#%02X%02X%02X%02X",
                color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
    }

    public static float[] toFloat(Color color) {
        float[] srgb = new float[3];
        srgb[0] = (float) color.getRed();
        srgb[1] = (float) color.getGreen();
        srgb[2] = (float) color.getBlue();
        return srgb;
    }

    public static double[] toDouble(Color color) {
        double[] srgb = new double[3];
        srgb[0] = color.getRed();
        srgb[1] = color.getGreen();
        srgb[2] = color.getBlue();
        return srgb;
    }

    public static double[] SRGBtoAdobeRGB(Color color) {
        return SRGB.SRGBtoAdobeRGB(toDouble(color));
    }

    public static double[] SRGBtoAppleRGB(Color color) {
        return SRGB.SRGBtoAppleRGB(toDouble(color));
    }

}
