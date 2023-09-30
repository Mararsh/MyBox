package mara.mybox.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.IndexRange;
import mara.mybox.data.FindReplaceString;
import static mara.mybox.data.FindReplaceString.create;

/**
 * @Author Mara
 * @CreateDate 2021-3-26
 * @License Apache License Version 2.0
 */
public class JsonTools {

    public static LinkedHashMap<String, String> jsonValues(String data, List<String> keys) {
        try {
            LinkedHashMap<String, String> values = new LinkedHashMap<>();
            String subdata = data;
            FindReplaceString endFind = create()
                    .setOperation(FindReplaceString.Operation.FindNext)
                    .setFindString("[\\},]").setAnchor(0)
                    .setIsRegex(true).setCaseInsensitive(true).setMultiline(true);
            for (String key : keys) {
                Map<String, Object> value = jsonValue(subdata, key, endFind);
                if (value == null) {
                    continue;
                }
                values.put(key, (String) value.get("value"));
                int start = (int) value.get("end") + 1;
                if (start >= subdata.length() - 1) {
                    break;
                }
                subdata = subdata.substring(start);
            }
            return values;
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String, Object> jsonValue(String json, String key, FindReplaceString endFind) {
        try {
            String flag = "\"" + key + "\":";
            int startPos = json.indexOf(flag);
            if (startPos < 0) {
                return null;
            }
            String data = json.substring(startPos + flag.length());
            endFind.setInputString(data).handleString();
            IndexRange end = endFind.getStringRange();
            if (end == null) {
                return null;
            }
            int endPos = end.getStart();
            Map<String, Object> map = new HashMap<>();
            String value = data.substring(0, endPos);
            if (value.startsWith("\"")) {
                value = value.substring(1);
            }
            if (value.endsWith("\"")) {
                value = value.substring(0, value.length() - 1);
            }
            map.put("value", value);
            map.put("start", startPos);
            map.put("end", endPos);
            return map;
        } catch (Exception e) {
            return null;
        }
    }

    public static String encode(String value) {
        try {
            return new ObjectMapper().writeValueAsString(value);
        } catch (Exception e) {
            return value;
        }
    }

}
