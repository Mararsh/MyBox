package mara.mybox.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;

/**
 * @Author Mara
 * @CreateDate 2021-5-5
 * @License Apache License Version 2.0
 */
public class WebElementsController extends BaseWebViewController {

    protected int foundCount;
    protected HTMLDocument loadedDoc;

    @FXML
    protected HBox elementsBox;
    @FXML
    protected RadioButton tagRadio, idRadio, nameRadio;
    @FXML
    protected ControlStringSelector elementInputController;
    @FXML
    protected ToggleGroup elementGroup;
    @FXML
    protected Button queryElementButton, examplesButton;
    @FXML
    protected TextArea codesArea;
    @FXML
    protected Label codesLabel;

    public WebElementsController() {
        baseTitle = Languages.message("WebElements");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            String keyName = baseName + "WebTag";
            try ( Connection conn = DerbyBase.getConnection()) {
                if (TableStringValues.size(conn, keyName) <= 0) {
                    List<String> values = Arrays.asList(
                            "p", "img", "a", "div", "li", "ul", "ol", "h1", "h2", "h3",
                            "button", "input", "label", "form", "table", "tr", "th", "td",
                            "script", "style", "font", "span", "b", "hr", "br", "frame", "pre");
                    TableStringValues.add(conn, keyName, values);
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }

            elementInputController.init(this, keyName, null, 40);

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

            NodeStyleTools.removeTooltip(examplesButton);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void elements(String html) {
        setCodes(html);
        loadContents(html);
    }

    public void setCodes(String html) {
        codesArea.setText(html);
        codesLabel.setText(Languages.message("Total") + ":" + codesArea.getLength());
        loadedDoc = null;
        foundCount = 0;
    }

    @FXML
    @Override
    public void goAction() {
        setCodes("");
        super.goAction();
    }

    @Override
    public void pageIsLoading() {
        super.pageIsLoading();
        queryElementButton.setDisable(true);
        recoverButton.setDisable(true);
    }

    @Override
    protected void afterPageLoaded() {
        try {
            super.afterPageLoaded();
            bottomLabel.setText(Languages.message("Count") + ": " + foundCount);
            if (codesArea.getText().isBlank()) {
                setCodes(WebViewTools.getHtml(webEngine));
            }
            if (loadedDoc == null) {
                loadedDoc = (HTMLDocument) webEngine.getDocument();
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
                popError(Languages.message("InvalidData"));
                return;
            }
            elementInputController.refreshList();
            if (loadedDoc == null) {
                popInformation(Languages.message("NoData"));
                return;
            }
            webEngine.loadContent("");
            NodeList aList = null;
            Element element = null;
            if (tagRadio.isSelected()) {
                aList = loadedDoc.getElementsByTagName(value);
            } else if (idRadio.isSelected()) {
                element = loadedDoc.getElementById(value);
            } else if (nameRadio.isSelected()) {
                aList = loadedDoc.getElementsByName(value);
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
            names.addAll(Arrays.asList(Languages.message("Index"), Languages.message("Tag"), Languages.message("Name"), Languages.message("Type"), Languages.message("Value"),
                    Languages.message("Attributes"), Languages.message("Texts")
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
            String style = UserConfig.getString(baseName + "HtmlStyle", "Default");
            String html = HtmlWriteTools.html(null, style, StringTable.tableDiv(table));
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
        } else {
            webEngine.loadContent(codesArea.getText());
        }

    }

    @FXML
    public void popExamples(MouseEvent mouseEvent) {
        try {
            List<String> values = new ArrayList<>();
            values.addAll(Arrays.asList(
                    "p", "img", "a", "div", "li", "ul", "ol", "h1", "h2", "h3",
                    "button", "input", "label", "form", "table", "tr", "th", "td",
                    "script", "style", "font", "span", "b", "hr", "br", "frame", "pre"
            ));

            List<Node> buttons = new ArrayList<>();
            for (String value : values) {
                Button button = new Button(value);
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        tagRadio.fire();
                        elementInputController.set(value);
                        queryElement();
                    }
                });
                buttons.add(button);
            }

            MenuController controller = MenuController.open(this, elementInputController.selector, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            controller.addFlowPane(buttons);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
