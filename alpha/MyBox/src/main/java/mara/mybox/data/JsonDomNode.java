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

    protected String name, text;
    protected JsonNode node;
    protected NodeType type;
    protected final BooleanProperty selected = new SimpleBooleanProperty(false);

    public static enum NodeType {
        Value, Array, Object, Unknown
    }

    public JsonDomNode() {
        name = null;
        node = null;
        type = null;
        text = null;
    }

    public JsonDomNode(String name, JsonNode node) {
        this.name = name;
        this.node = node;
        if (node == null) {
            return;
        }
        if (node.isValueNode()) {
            type = NodeType.Value;
            text = node.asText();

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
    public JsonDomNode setName(String name) {
        this.name = name;
        return this;
    }

    public JsonDomNode setNode(JsonNode node) {
        this.node = node;
        return this;
    }

    /*
        get
     */
    public String getName() {
        return name;
    }

    public JsonNode getNode() {
        return node;
    }

    public String getText() {
        return text;
    }

    public NodeType getType() {
        return type;
    }

    public BooleanProperty getSelected() {
        return selected;
    }

}
