package mara.mybox.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class JsonTreeNode {

    public static ObjectMapper jsonMapper;

    protected String title, value;
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
        value = null;
        isArrayElement = false;
    }

    public JsonTreeNode(String name, JsonNode jsonNode) {
        this.title = name;
        this.jsonNode = jsonNode;
        if (jsonNode == null) {
            type = NodeType.Root;

        } else if (jsonNode.isNull()) {
            type = NodeType.Null;
            value = null;

        } else if (jsonNode.isBoolean()) {
            type = NodeType.Boolean;
            value = jsonNode.asText();

        } else if (jsonNode.isNumber()) {
            type = NodeType.Number;
            value = jsonNode.asText();

        } else if (jsonNode.isTextual() || jsonNode.isBinary()) {
            type = NodeType.String;
            value = jsonNode.asText();

        } else if (jsonNode.isArray()) {
            type = NodeType.Array;

        } else if (jsonNode.isObject()) {
            type = NodeType.Object;

        } else {
            type = NodeType.Unknown;

        }
    }

    public String getTypename() {
        return type == null ? null : message(type.name());
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
            jsonMapper = new ObjectMapper();
        }
        return jsonMapper;
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

    public String getValue() {
        return value;
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
