package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.HtmlTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;

/**
 * @Author Mara
 * @CreateDate 2021-5-5
 * @License Apache License Version 2.0
 */
public class WebElementsController extends ControlWebBrowserBox {

    protected int foundCount;
    protected String loadedHtml;

    @FXML
    protected HBox elementsBox;
    @FXML
    protected RadioButton tagRadio, idRadio, nameRadio;
    @FXML
    protected ControlStringSelector elementInputController;
    @FXML
    protected ToggleGroup elementGroup;
    @FXML
    protected Button queryElementButton;

    public WebElementsController() {
        baseTitle = AppVariables.message("WebElements");
        fetchIcon = false;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            String keyName = baseName + "WebTag";
            try ( Connection conn = DerbyBase.getConnection()) {
                if (TableStringValues.size(conn, keyName) <= 0) {
                    List<String> values = Arrays.asList("table", "img", "a", "frame", "div", "li",
                            "button", "input", "tr", "th");
                    TableStringValues.add(conn, keyName, values);
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
            elementInputController.init(this, keyName, null, 20);
            elementGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    if (tagRadio.isSelected()) {
                        elementInputController.refreshList(keyName, "table");
                    } else if (idRadio.isSelected()) {
                        elementInputController.refreshList(baseName + "ID", "id");
                    } else if (nameRadio.isSelected()) {
                        elementInputController.refreshList(baseName + "Name", "name");
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    public void elements(String html) {
        loadedHtml = null;
        loadContents(html);
    }

    @FXML
    @Override
    public void goAction() {
        foundCount = 0;
        loadedHtml = null;
        super.goAction();
    }

    @Override
    protected void pageIsLoading() {
        super.pageIsLoading();
        queryElementButton.setDisable(true);
        recoverButton.setDisable(true);
    }

    @Override
    protected void afterPageLoaded() {
        try {
            super.afterPageLoaded();
            bottomLabel.setText(message("Count") + ": " + foundCount);
            if (loadedHtml == null) {
                loadedHtml = FxmlControl.getHtml(webEngine);
            }
            queryElementButton.setDisable(false);
            recoverButton.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    protected void queryElement() {
        try {
            foundCount = 0;
            String value = elementInputController.value();
            if (value == null || value.isBlank()) {
                popError(message("InvalidData"));
                return;
            }
            elementInputController.refreshList();
            HTMLDocument htmlDoc = (HTMLDocument) webEngine.getDocument();
            if (htmlDoc == null) {
                popInformation(message("NoData"));
                return;
            }
            loadedHtml = loadedHtml == null ? FxmlControl.getHtml(webEngine) : loadedHtml;
            if (loadedHtml == null) {
                popInformation(message("NoData"));
                return;
            }
            webEngine.loadContent("");
            NodeList aList = null;
            Element element = null;
            if (tagRadio.isSelected()) {
                aList = htmlDoc.getElementsByTagName(value);
            } else if (idRadio.isSelected()) {
                element = htmlDoc.getElementById(value);
            } else if (nameRadio.isSelected()) {
                aList = htmlDoc.getElementsByName(value);
            }
            List<Element> elements = new ArrayList<>();
            if (aList != null && aList.getLength() > 0) {
                for (int i = 0; i < aList.getLength(); i++) {
                    org.w3c.dom.Node node = aList.item(i);
                    if (node != null) {
                        elements.add((Element) node);
                    }
                }
            } else if (element != null) {
                elements.add(element);
            }
            if (elements.isEmpty()) {
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(
                    message("Index"), message("Tag"), message("Name"), message("Type"), message("Value"),
                    message("Attributes"), message("Texts")
            ));
            StringTable table = new StringTable(names);
            int index = 1;
            for (Element el : elements) {
                List<String> row = new ArrayList<>();
                NamedNodeMap attrs = el.getAttributes();
                String attrsString = "";
                if (attrs != null) {
                    for (int i = 0; i < attrs.getLength(); i++) {
                        org.w3c.dom.Node node = attrs.item(i);
                        attrsString += node.getNodeName() + "=" + node.getNodeValue() + " ";
                    }
                }
                row.addAll(Arrays.asList(
                        index + "", el.getTagName(), el.getNodeName(), el.getNodeType() + "", el.getNodeValue(),
                        attrsString, el.getTextContent()
                ));
                table.add(row);
                index++;
            }
            foundCount = index - 1;
            String style = AppVariables.getUserConfigValue(baseName + "HtmlStyle", "Default");
            String html = HtmlTools.html(null, style, StringTable.tableDiv(table));
            webEngine.loadContent(html);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void recoverAction() {
        if (address != null && !address.isBlank()) {
            loadAddress(address);
        } else if (loadedHtml != null && !loadedHtml.isBlank()) {
            webEngine.loadContent(loadedHtml);
        }

    }

}
