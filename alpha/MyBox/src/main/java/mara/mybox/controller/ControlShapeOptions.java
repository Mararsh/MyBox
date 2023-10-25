package mara.mybox.controller;

import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import static javafx.scene.shape.StrokeLineCap.ROUND;
import static javafx.scene.shape.StrokeLineCap.SQUARE;
import mara.mybox.data.DoublePathSegment;
import mara.mybox.data.DoublePoint;
import mara.mybox.data.DoubleShape;
import mara.mybox.data.DoubleShape.ShapeType;
import static mara.mybox.data.DoubleShape.ShapeType.Polygon;
import static mara.mybox.data.DoubleShape.ShapeType.Polyline;
import static mara.mybox.data.DoubleShape.ShapeType.Polylines;
import mara.mybox.data.ShapeStyle;
import static mara.mybox.data.ShapeStyle.DefaultAnchorColor;
import static mara.mybox.data.ShapeStyle.DefaultStrokeColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-6-29
 * @License Apache License Version 2.0
 */
public abstract class ControlShapeOptions extends BaseController {

    protected BaseImageController imageController;
    protected DoubleShape currentShape;
    protected ShapeStyle style;
    protected ChangeListener<Boolean> shapeDataChangeListener;
    public ShapeType shapeType = null;
    protected Label infoLabel;

    @FXML
    protected RadioButton lineRadio, rectangleRadio, circleRadio, ellipseRadio,
            polylineRadio, polygonRadio, polylinesRadio,
            arcRadio, quadraticRadio, cubicRadio, svgRadio,
            linecapSquareRadio, linecapRoundRadio, linecapButtRadio;
    @FXML
    protected ToggleGroup typeGroup, linecap;
    @FXML
    protected TextField dashInput;
    @FXML
    protected ControlShapeParameters parametersController;
    @FXML
    protected ComboBox<String> strokeWidthSelector, strokeOpacitySelector, fillOpacitySelector,
            anchorSizeSelector;
    @FXML
    protected CheckBox fillCheck, dashCheck, anchorCheck, popAnchorCheck, addPointCheck;
    @FXML
    protected FlowPane opPane;
    @FXML
    protected Button operationsButton;
    @FXML
    protected ControlColorSet strokeColorController, anchorColorController, fillColorController;

    public ControlShapeOptions() {
        TipsLabelKey = "ShapeTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            parametersController.optionsController = this;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(BaseImageController imageController) {
        try {
            this.imageController = imageController;
            imageController.anchorCheck = anchorCheck;
            imageController.popAnchorCheck = popAnchorCheck;
            imageController.addPointCheck = addPointCheck;
            imageController.initMaskControls();

            parametersController.imageController = imageController;
            parametersController.optionsController = this;
            parametersController.pathController.optionsOontroller = this;
            infoLabel = imageController.infoLabel;

            shapeDataChangeListener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    shapeDataChanged();
                }
            };

            initStyleControls();
            initShapeControls();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initShapeControls() {
        try {
            if (typeGroup != null) {
                typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue v, Toggle ov, Toggle nv) {
                        switchShapeBySelection();
                    }
                });
            }

            if (parametersController != null) {
                parametersController.pointsController.tableData.addListener(new ListChangeListener<DoublePoint>() {
                    @Override
                    public void onChanged(ListChangeListener.Change<? extends DoublePoint> c) {
                        if (isSettingValues
                                || parametersController.pointsController.isSettingValues
                                || parametersController.pointsController.isSettingTable) {
                            return;
                        }
                        goShape();
                    }
                });

                parametersController.linesController.tableData.addListener(new ListChangeListener<List<DoublePoint>>() {
                    @Override
                    public void onChanged(ListChangeListener.Change<? extends List<DoublePoint>> c) {
                        if (isSettingValues
                                || parametersController.linesController.isSettingValues
                                || parametersController.linesController.isSettingTable) {
                            return;
                        }
                        goShape();
                    }
                });

                parametersController.pathController.tableData.addListener(new ListChangeListener<DoublePathSegment>() {
                    @Override
                    public void onChanged(ListChangeListener.Change<? extends DoublePathSegment> c) {
                        if (isSettingValues
                                || parametersController.pathController.isSettingValues
                                || parametersController.pathController.isSettingTable) {
                            return;
                        }
                        MyBoxLog.console("here");
                        goShape();
                    }
                });
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void initStyleControls() {
        try {
            style = new ShapeStyle(interfaceName);

            isSettingValues = true;
            if (strokeWidthSelector != null) {
                strokeWidthSelector.getItems().addAll(Arrays.asList(
                        "3", "0", "1", "2", "5", "8", "10", "15", "25", "30", "50", "80"));
                strokeWidthSelector.setValue(style.getStrokeWidth() + "");
                strokeWidthSelector.valueProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        if (isSettingValues) {
                            return;
                        }
                        float f;
                        try {
                            f = Float.parseFloat(strokeWidthSelector.getValue());
                        } catch (Exception e) {
                            f = -1;
                        }
                        style.setStrokeWidth(f);
                        goStyle();
                    }
                });
            }

            if (strokeColorController != null) {
                strokeColorController.init(this, interfaceName + "StrokeColor", Color.web(DefaultStrokeColor));
                strokeColorController.initColor(style.getStrokeColor());
                strokeColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                    @Override
                    public void changed(ObservableValue v, Paint ov, Paint nv) {
                        if (isSettingValues) {
                            return;
                        }
                        style.setStrokeColor(strokeColorController.color());
                        goStyle();
                    }
                });
            }

            if (fillColorController != null) {
                fillColorController.init(this, interfaceName + "FillColor", Color.TRANSPARENT);
                fillColorController.initColor(style.getFillColor());
                fillColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                    @Override
                    public void changed(ObservableValue v, Paint ov, Paint nv) {
                        if (isSettingValues) {
                            return;
                        }
                        style.setFillColor(fillColorController.color());
                        if (fillCheck.isSelected()) {
                            goStyle();
                        }
                    }
                });
            }
            if (fillCheck != null) {
                fillCheck.setSelected(style.isIsFillColor());
                fillCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        if (isSettingValues) {
                            return;
                        }
                        style.setIsFillColor(fillCheck.isSelected());
                        goStyle();
                    }
                });
            }

            if (fillOpacitySelector != null) {
                fillOpacitySelector.getItems().addAll(
                        "0.3", "0.5", "0", "1.0", "0.05", "0.02", "0.1", "0.2", "0.8", "0.6", "0.4", "0.7", "0.9"
                );
                fillOpacitySelector.setValue(style.getFillOpacity() + "");
                fillOpacitySelector.valueProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        if (isSettingValues) {
                            return;
                        }
                        float f;
                        try {
                            f = Float.parseFloat(fillOpacitySelector.getValue());
                        } catch (Exception e) {
                            f = -1;
                        }
                        style.setFillOpacity(f);
                        goStyle();
                    }
                });
            }
            if (strokeOpacitySelector != null) {
                strokeOpacitySelector.getItems().addAll(
                        "1.0", "0.3", "0.5", "0", "0.05", "0.02", "0.1", "0.2", "0.8", "0.6", "0.4", "0.7", "0.9"
                );
                strokeOpacitySelector.setValue(style.getStrokeOpacity() + "");
                strokeOpacitySelector.valueProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue v, String ov, String nv) {
                        if (isSettingValues) {
                            return;
                        }
                        float f;
                        try {
                            f = Float.parseFloat(strokeOpacitySelector.getValue());
                        } catch (Exception e) {
                            f = -1;
                        }
                        style.setStrokeOpacity(f);
                        goStyle();
                    }
                });
            }

            if (dashCheck != null) {
                dashCheck.setSelected(style.isIsStrokeDash());
                dashCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        if (isSettingValues) {
                            return;
                        }
                        style.setIsStrokeDash(dashCheck.isSelected());
                        goStyle();
                    }
                });
            }

            if (dashInput != null) {
                dashInput.setText(style.getStrokeDashText());
                dashInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                        if (isSettingValues || nv) {
                            return;
                        }
                        style.setStrokeDashText(dashInput.getText());
                        if (dashCheck.isSelected()) {
                            goStyle();
                        }
                    }
                });
            }

            if (linecapSquareRadio != null) {
                StrokeLineCap lineCap = style.getLineCap();
                if (null == lineCap) {
                    linecapButtRadio.setSelected(true);
                } else {
                    switch (lineCap) {
                        case SQUARE:
                            linecapSquareRadio.setSelected(true);
                            break;
                        case ROUND:
                            linecapRoundRadio.setSelected(true);
                            break;
                        default:
                            linecapButtRadio.setSelected(true);
                            break;
                    }
                }
                linecap.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue v, Toggle ov, Toggle nv) {
                        if (isSettingValues) {
                            return;
                        }
                        if (linecapSquareRadio.isSelected()) {
                            style.setLineCap(StrokeLineCap.SQUARE);
                        } else if (linecapRoundRadio.isSelected()) {
                            style.setLineCap(StrokeLineCap.ROUND);
                        } else if (linecapButtRadio.isSelected()) {
                            style.setLineCap(StrokeLineCap.BUTT);
                        } else {
                            style.setLineCap(null);
                        }
                        goStyle();
                    }
                });
            }

            if (anchorColorController != null) {
                anchorColorController.init(this, interfaceName + "AnchorColor", Color.web(DefaultAnchorColor));
                anchorColorController.initColor(style.getAnchorColor());
                anchorColorController.rect.fillProperty().addListener(new ChangeListener<Paint>() {
                    @Override
                    public void changed(ObservableValue v, Paint ov, Paint nv) {
                        if (isSettingValues) {
                            return;
                        }
                        style.setAnchorColor(anchorColorController.color());
                        goStyle();
                    }
                });
            }

            if (anchorSizeSelector != null) {
                anchorSizeSelector.getItems().setAll(Arrays.asList("10", "15", "20", "25", "30", "40", "50"));
                anchorSizeSelector.setValue(style.getAnchorSize() + "");
                anchorSizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue ov, String oldValue, String newValue) {
                        if (isSettingValues) {
                            return;
                        }
                        float f;
                        try {
                            f = Float.parseFloat(anchorSizeSelector.getValue());
                        } catch (Exception e) {
                            f = -1;
                        }
                        style.setAnchorSize(f);
                        goStyle();
                    }
                });
            }

            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        shape
     */
    public void setImageShapeOptions() {
        if (imageController == null) {
            return;
        }
        imageController.resetShapeOptions();
        imageController.supportPath = true;
        imageController.shapeStyle = style;
    }

    public void switchShapeBySelection() {
        if (isSettingValues) {
            return;
        }
        imageController.clearMaskShapes();
        showShape();
        setShapeControls();
    }

    public boolean showShape() {
        try {
            if (imageController == null) {
                return false;
            }
            shapeType = null;
            setImageShapeOptions();

            if (rectangleRadio != null && rectangleRadio.isSelected()) {
                imageController.showMaskRectangle();
                shapeType = ShapeType.Rectangle;

            } else if (circleRadio != null && circleRadio.isSelected()) {
                imageController.showMaskCircle();
                shapeType = ShapeType.Circle;

            } else if (ellipseRadio != null && ellipseRadio.isSelected()) {
                imageController.showMaskEllipse();
                shapeType = ShapeType.Ellipse;

            } else if (lineRadio != null && lineRadio.isSelected()) {
                imageController.showMaskLine();
                shapeType = ShapeType.Line;

            } else if (polylineRadio != null && polylineRadio.isSelected()) {
                imageController.showMaskPolyline();
                shapeType = ShapeType.Polyline;

            } else if (polygonRadio != null && polygonRadio.isSelected()) {
                imageController.showMaskPolygon();
                shapeType = ShapeType.Polygon;

            } else if (polylinesRadio != null && polylinesRadio.isSelected()) {
                imageController.showMaskPolylines();
                shapeType = ShapeType.Polylines;

            } else if (quadraticRadio != null && quadraticRadio.isSelected()) {
                imageController.showMaskQuadratic();
                shapeType = ShapeType.Quadratic;

            } else if (cubicRadio != null && cubicRadio.isSelected()) {
                imageController.showMaskCubic();
                shapeType = ShapeType.Cubic;

            } else if (arcRadio != null && arcRadio.isSelected()) {
                imageController.showMaskArc();
                shapeType = ShapeType.Arc;

            } else if (svgRadio != null && svgRadio.isSelected()) {
                imageController.showMaskPath();
                shapeType = ShapeType.Path;

            }

            return shapeType != null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void setShapeControls() {
        try {
            parametersController.setShapeControls(shapeType);
            opPane.getChildren().clear();
            if (shapeType == null) {
                if (infoLabel != null) {
                    infoLabel.setText("");
                }
                return;
            }

            switch (shapeType) {
                case Polylines:
                    opPane.getChildren().addAll(operationsButton, withdrawButton, anchorCheck, popAnchorCheck);
                    NodeStyleTools.setTooltip(withdrawButton, new Tooltip(message("RemoveLastLine") + "\nCTRL+w / ALT+w"));
                    if (infoLabel != null) {
                        infoLabel.setText(message("ShapePolylinesTips"));
                    }
                    break;
                case Polyline:
                case Polygon:
                    opPane.getChildren().addAll(operationsButton, withdrawButton, anchorCheck, popAnchorCheck, addPointCheck);
                    NodeStyleTools.setTooltip(withdrawButton, new Tooltip(message("RemoveLastPoint") + "\nCTRL+w / ALT+w"));
                    if (infoLabel != null) {
                        infoLabel.setText(message("ShapeDragMoveComments"));
                    }
                    break;
                default:
                    opPane.getChildren().addAll(operationsButton, anchorCheck, popAnchorCheck);
                    if (infoLabel != null) {
                        infoLabel.setText(message("ShapeDragMoveComments"));
                    }
                    break;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void shapeDataChanged() {
        switchShapeByData();
        setShapeControls();
    }

    public boolean switchShapeByData() {
        try {
            if (imageController == null) {
                return false;
            }
            shapeType = null;
            setImageShapeOptions();

            isSettingValues = true;
            if (imageController.isMaskRectangleShown()) {
                if (rectangleRadio != null) {
                    rectangleRadio.setSelected(true);
                }
                shapeType = ShapeType.Rectangle;

            } else if (imageController.isMaskCircleShown()) {
                if (circleRadio != null) {
                    circleRadio.setSelected(true);
                }
                shapeType = ShapeType.Circle;

            } else if (imageController.isMaskEllipseShown()) {
                if (ellipseRadio != null) {
                    ellipseRadio.setSelected(true);
                }
                shapeType = ShapeType.Ellipse;

            } else if (imageController.isMaskLineShown()) {
                if (lineRadio != null) {
                    lineRadio.setSelected(true);
                }
                shapeType = ShapeType.Line;

            } else if (imageController.isMaskPolylineShown()) {
                if (polylineRadio != null) {
                    polylineRadio.setSelected(true);
                }
                shapeType = ShapeType.Polyline;

            } else if (imageController.isMaskPolygonShown()) {
                if (polygonRadio != null) {
                    polygonRadio.setSelected(true);
                }
                shapeType = ShapeType.Polygon;

            } else if (imageController.isMaskPolylinesShown()) {
                if (polylinesRadio != null) {
                    polylinesRadio.setSelected(true);
                }
                shapeType = ShapeType.Polylines;

            } else if (imageController.isMaskQuadraticShown()) {
                if (quadraticRadio != null) {
                    quadraticRadio.setSelected(true);
                }
                shapeType = ShapeType.Quadratic;

            } else if (imageController.isMaskCubicShown()) {
                if (cubicRadio != null) {
                    cubicRadio.setSelected(true);
                }
                shapeType = ShapeType.Cubic;

            } else if (imageController.isMaskArcShown()) {
                if (arcRadio != null) {
                    arcRadio.setSelected(true);
                }
                shapeType = ShapeType.Arc;

            } else if (imageController.isMaskPathShown()) {
                if (svgRadio != null) {
                    svgRadio.setSelected(true);
                }
                shapeType = ShapeType.Path;

            }
            isSettingValues = false;

            return shapeType != null;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public void addListener() {
        imageController.maskShapeDataChanged.addListener(shapeDataChangeListener);
    }

    public void removeListener() {
        imageController.maskShapeDataChanged.removeListener(shapeDataChangeListener);
    }

    public boolean pickShape() {
        return parametersController.pickShape(shapeType);
    }

    public void redrawShape() {
        if (imageController == null || shapeType == null) {
            return;
        }
        imageController.drawMaskShape();
    }

    /*
        action
     */
    public void goShape() {
        if (pickShape()) {
            redrawShape();
        }
    }

    public void goStyle() {
        redrawShape();
    }

    @FXML
    public void popShapeMenu(Event event) {
        imageController.popShapeMenu(event);
    }

    @FXML
    public void showShapeMenu(Event event) {
        imageController.showShapeMenu(event);
    }

    @FXML
    @Override
    public void withdrawAction() {
        if (null == shapeType || imageController == null) {
            return;
        }
        switch (shapeType) {
            case Polyline:
            case Polygon:
                parametersController.pointsController.removeLastItem();
                break;
            case Polylines:
                parametersController.linesController.removeLastItem();
                break;
        }
    }

}
