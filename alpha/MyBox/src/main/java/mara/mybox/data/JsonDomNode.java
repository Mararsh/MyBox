package mara.mybox.data;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-4-30
 * @License Apache License Version 2.0
 */
public class JsonDomNode {

    protected String title, value;
    protected JsonNode node;
    protected NodeType type;
    protected final BooleanProperty selected = new SimpleBooleanProperty(false);

    public static enum NodeType {
        Value, Array, Object, Unknown
    }

    public JsonDomNode() {
        title = null;
        node = null;
        type = null;
        value = null;
    }

    public JsonDomNode(String name, JsonNode node) {
        this.title = name;
        this.node = node;
        if (node == null) {
            return;
        }
        if (node.isValueNode()) {
            type = NodeType.Value;
            value = node.asText();

        } else if (node.isArray()) {
            type = NodeType.Array;

        } else if (node.isObject()) {
            type = NodeType.Object;

        } else {
            type = NodeType.Unknown;

        }
    }

    public String getTypename() {
        return type == null ? null : message(type.name());
    }

    public boolean isValue() {
        return type == NodeType.Value;
    }

    public boolean isArray() {
        return type == NodeType.Array;
    }

    public boolean isObject() {
        return type == NodeType.Object;
    }

    /*
        set
     */
    public JsonDomNode setTitle(String title) {
        this.title = title;
        return this;
    }

    public JsonDomNode setNode(JsonNode node) {
        this.node = node;
        return this;
    }

    /*
        get
     */
    public String getTitle() {
        return title;
    }

    public JsonNode getNode() {
        return node;
    }

    public String getValue() {
        return value;
    }

    public NodeType getType() {
        return type;
    }

    public BooleanProperty getSelected() {
        return selected;
    }

}
