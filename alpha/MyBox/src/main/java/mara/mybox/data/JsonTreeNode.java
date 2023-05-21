package mara.mybox.data;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class JsonTreeNode {

    public static ObjectMapper jsonMapper;

    protected String title;
    protected JsonNode jsonNode;
    protected NodeType type;
    protected boolean isArrayElement;
    protected final BooleanProperty selected = new SimpleBooleanProperty(false);

    public static enum NodeType {
        Root, Array, Object, String, Number, Boolean, Null, Unknown
    }

    public JsonTreeNode() {
        title = null;
        jsonNode = null;
        type = null;
        isArrayElement = false;
    }

    public JsonTreeNode(String name, JsonNode jsonNode) {
        this.title = name;
        this.jsonNode = jsonNode;

        if (jsonNode == null) {
            type = NodeType.Root;

        } else if (jsonNode.isNull()) {
            type = NodeType.Null;

        } else {
            if (jsonNode.isBoolean()) {
                type = NodeType.Boolean;

            } else if (jsonNode.isNumber()) {
                type = NodeType.Number;

            } else if (jsonNode.isTextual() || jsonNode.isBinary()) {
                type = NodeType.String;

            } else if (jsonNode.isArray()) {
                type = NodeType.Array;

            } else if (jsonNode.isObject()) {
                type = NodeType.Object;

            } else {
                type = NodeType.Unknown;

            }
        }
    }

    public String getTypename() {
        return type == null ? null : message(type.name());
    }

    public String getValue() {
        return jsonNode == null || type == NodeType.Null ? null : formatByJackson(jsonNode);
    }

    public boolean isRoot() {
        return jsonNode == null || type == NodeType.Root;
    }

    public boolean isValue() {
        return jsonNode != null
                && type != NodeType.Array && type != NodeType.Object;
    }

    public boolean isArray() {
        return jsonNode != null && type == NodeType.Array;
    }

    public boolean isObject() {
        return jsonNode != null && type == NodeType.Object;
    }

    public boolean isNull() {
        return jsonNode != null || type == NodeType.Null;
    }

    public boolean isString() {
        return jsonNode != null || type == NodeType.String;
    }

    public boolean isNumber() {
        return jsonNode != null || type == NodeType.Number;
    }

    public boolean isBoolean() {
        return jsonNode != null || type == NodeType.Boolean;
    }

    public String stringByJackson() {
        return stringByJackson(jsonNode);
    }

    public String formatByJackson() {
        return formatByJackson(jsonNode);
    }


    /*
        static
     */
    public static ObjectMapper jsonMapper() {
        if (jsonMapper == null) {
            try (Connection conn = DerbyBase.getConnection()) {
                JsonMapper.Builder builder = JsonMapper.builder();

                if (UserConfig.getBoolean(conn, "JacksonAllowJavaComments", false)) {
                    builder.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_JAVA_COMMENTS);
                }
                if (UserConfig.getBoolean(conn, "JacksonAllowYamlComments", false)) {
                    builder.enable(JsonReadFeature.ALLOW_YAML_COMMENTS);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_YAML_COMMENTS);
                }
                if (UserConfig.getBoolean(conn, "JacksonAllowSingleQuotes", false)) {
                    builder.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_SINGLE_QUOTES);
                }
                if (UserConfig.getBoolean(conn, "JacksonAllowUnquotedFieldNames", false)) {
                    builder.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES);
                }
                if (UserConfig.getBoolean(conn, "JacksonAllowUnescapedControlChars", false)) {
                    builder.enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS);
                }
                if (UserConfig.getBoolean(conn, "JacksonAllowBackslashEscapingAny", false)) {
                    builder.enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
                }
                if (UserConfig.getBoolean(conn, "JacksonAllowLeadingZerosForNumbers", false)) {
                    builder.enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS);
                }
                if (UserConfig.getBoolean(conn, "JacksonAllowLeadingPlusSignForNumbers", false)) {
                    builder.enable(JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_LEADING_PLUS_SIGN_FOR_NUMBERS);
                }
                if (UserConfig.getBoolean(conn, "JacksonAllowLeadingDecimalPointForNumbers", false)) {
                    builder.enable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS);
                }
                if (UserConfig.getBoolean(conn, "JacksonAllowTrailingDecimalPointForNumbers", false)) {
                    builder.enable(JsonReadFeature.ALLOW_TRAILING_DECIMAL_POINT_FOR_NUMBERS);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_TRAILING_DECIMAL_POINT_FOR_NUMBERS);
                }
                if (UserConfig.getBoolean(conn, "JacksonAllowNonNumericNumbers", false)) {
                    builder.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS);
                }
                if (UserConfig.getBoolean(conn, "JacksonAllowMissingValues", false)) {
                    builder.enable(JsonReadFeature.ALLOW_MISSING_VALUES);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_MISSING_VALUES);
                }
                if (UserConfig.getBoolean(conn, "JacksonAllowTrailingComma", false)) {
                    builder.enable(JsonReadFeature.ALLOW_TRAILING_COMMA);
                } else {
                    builder.disable(JsonReadFeature.ALLOW_TRAILING_COMMA);
                }
                if (UserConfig.getBoolean(conn, "JacksonQuoteFieldNames", true)) {
                    builder.enable(JsonWriteFeature.QUOTE_FIELD_NAMES);
                } else {
                    builder.disable(JsonWriteFeature.QUOTE_FIELD_NAMES);
                }
                if (UserConfig.getBoolean(conn, "JacksonWriteNanAsStrings", true)) {
                    builder.enable(JsonWriteFeature.WRITE_NAN_AS_STRINGS);
                } else {
                    builder.disable(JsonWriteFeature.WRITE_NAN_AS_STRINGS);
                }
                if (UserConfig.getBoolean(conn, "JacksonWriteNumbersAsStrings", false)) {
                    builder.enable(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS);
                } else {
                    builder.disable(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS);
                }
                if (UserConfig.getBoolean(conn, "JacksonEscapeNonASCII", false)) {
                    builder.enable(JsonWriteFeature.ESCAPE_NON_ASCII);
                } else {
                    builder.disable(JsonWriteFeature.ESCAPE_NON_ASCII);
                }

                jsonMapper = builder.build();
            } catch (Exception e) {
                MyBoxLog.error(e);
                return null;
            }
        }
        return jsonMapper;
    }

    public static void resetJsonMapper() {
        jsonMapper = null;
    }

    public static String stringByJackson(JsonNode jsonNode) {
        try {
            if (jsonNode == null) {
                return null;
            }
            return jsonMapper().writeValueAsString(jsonNode);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static String formatByJackson(JsonNode jsonNode) {
        try {
            if (jsonNode == null) {
                return null;
            }
            return jsonMapper().writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonNode);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static JsonNode parseByJackson(String json) {
        try {
            if (json == null) {
                return null;
            }
            return jsonMapper().readTree(json);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static boolean replaceName(JsonTreeNode jsonTreeNode, String oldName, String newName) {
        try {
            if (jsonTreeNode == null) {
                return false;
            }
            if (oldName == null || newName == null || !jsonTreeNode.isObject()) {
                return false;

            }
            JsonNode jsonNode = jsonTreeNode.getJsonNode();
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            if (fields == null) {
                return false;
            }
            ObjectNode objectNode = (ObjectNode) jsonNode;
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String name = field.getKey();
                if (oldName.equals(name)) {
                    objectNode.set(newName, field.getValue());
                    objectNode.remove(oldName);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    /*
        set
     */
    public JsonTreeNode setTitle(String title) {
        this.title = title;
        return this;
    }

    public JsonTreeNode setJsonNode(JsonNode node) {
        this.jsonNode = node;
        return this;
    }

    public JsonTreeNode setIsArrayElement(boolean isArrayElement) {
        this.isArrayElement = isArrayElement;
        return this;
    }

    /*
        get
     */
    public String getTitle() {
        return title;
    }

    public JsonNode getJsonNode() {
        return jsonNode;
    }

    public NodeType getType() {
        return type;
    }

    public boolean isIsArrayElement() {
        return isArrayElement;
    }

    public BooleanProperty getSelected() {
        return selected;
    }

}
