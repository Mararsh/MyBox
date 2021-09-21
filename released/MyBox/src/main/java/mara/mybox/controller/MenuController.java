package mara.mybox.controller;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-27
 * @License Apache License Version 2.0
 */
public class MenuController extends BaseController {

    protected Node node;
    protected String baseStyle;
    protected double initX, initY;

    @FXML
    protected HBox topBox, bottomBox;
    @FXML
    protected VBox nodesBox;
    @FXML
    protected Label titleLabel;
    @FXML
    protected Button functionsButton;

    public MenuController() {
    }

    @Override
    public void initControls() {
        try {
            parentController = this;
            baseStyle = thisPane.getStyle();
            if (baseStyle == null) {
                baseStyle = "";
            }
            String style = UserConfig.getString(baseName + "WindowStyle", "");
            PopTools.setMenuLabelsStyle(thisPane, baseStyle + style);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void setStageStatus() {
        setAsPopup(baseName);
    }

    public void setParameters(BaseController parent, Node node, double x, double y) {
        try {
            this.parentController = parent;
            initX = x;
            initY = y;
            thisPane.requestFocus();

            Window window = getMyWindow();
            if (window instanceof Popup) {
                window.setX(x);
                window.setY(y);
            }
            setControlsStyle();

            if (node != null) {
                setTitleid(node.getId());
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setWidth(double w) {
        thisPane.setPrefWidth(w);
        thisPane.layout();
        thisPane.applyCss();
    }

    public void setTitleid(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        titleLabel.setText(message("Target") + ": " + (parentController.isPop ? "Pop-" : "") + id);
    }

    public void addNode(Node node) {
        nodesBox.getChildren().add(node);
    }

    public void addFlowPane(List<Node> nodes) {
        try {

            FlowPane flowPane = new FlowPane();
            flowPane.setMinHeight(Region.USE_PREF_SIZE);
            flowPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            flowPane.setVgap(5);
            flowPane.setHgap(5);
            if (nodes != null) {
                flowPane.getChildren().setAll(nodes);
            }

            addNode(flowPane);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void popFunctionsMenu(MouseEvent mouseEvent) {

    }

    @FXML
    public void popStyles(MouseEvent mouseEvent) {
        PopTools.popMenuStyles(this, baseStyle, mouseEvent);
    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
    }

    @Override
    public boolean keyESC() {
        closeStage();
        return false;
    }

    @Override
    public boolean keyF6() {
        closeStage();
        return false;
    }

    /*
        static methods
     */
    public static MenuController open(BaseController parent, Node node, double x, double y) {
        try {
            if (parent == null) {
                return null;
            }
            Popup popup = PopTools.popWindow(parent, Fxmls.MenuFxml, node, x, y);
            if (popup == null) {
                return null;
            }
            Object object = popup.getUserData();
            if (object == null && !(object instanceof MenuController)) {
                return null;
            }
            MenuController controller = (MenuController) object;
            controller.setParameters(parent, node, x, y);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
