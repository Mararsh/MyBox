package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.image.FxImageTools;
import mara.mybox.fxml.image.ShapeDemos;
import mara.mybox.image.data.PixelsBlend;
import mara.mybox.image.data.PixelsBlend.ImagesBlendMode;
import mara.mybox.image.data.PixelsBlend.TransparentAs;
import static mara.mybox.image.data.PixelsBlend.fixWeight;
import mara.mybox.image.data.PixelsBlendFactory;
import mara.mybox.image.tools.ScaleTools;
import mara.mybox.value.InternalImages;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-2-6
 * @License Apache License Version 2.0
 */
public class ControlColorsBlend extends BaseController {

    protected ImagesBlendMode blendMode;
    protected float weight;
    protected int keepRatioType;
    protected TransparentAs baseTransparentAs = TransparentAs.Transparent,
            overlayTransparentAs = TransparentAs.Another;
    protected SimpleBooleanProperty changeNotify;

    @FXML
    protected ListView<String> modeList;
    @FXML
    protected ComboBox<String> weightSelector;
    @FXML
    protected CheckBox baseAboveCheck;
    @FXML
    protected ToggleGroup baseGroup, overlayGroup;
    @FXML
    protected RadioButton baseAsOverlayRadio, baseAsTransparentRadio, baseBlendRadio,
            overlayAsBaseRadio, overlayAsTransparentRadio, overlayBlendRadio;
    @FXML
    protected Button demoButton;
    @FXML
    protected VBox transBox;

    public void setParameters(BaseController parent) {
        if (parent instanceof ColorsBlendController) {
            thisPane.getChildren().remove(transBox);
        }
        try (Connection conn = DerbyBase.getConnection()) {
            setParameters(conn, parent);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(Connection conn, BaseController parent) {
        try {
            this.parentController = parent;
            baseName = parentController.baseName + "_Blend";
            changeNotify = new SimpleBooleanProperty(false);

            baseAboveCheck.setSelected(UserConfig.getBoolean(conn, baseName + "BaseAbove", false));

            String v = UserConfig.getString(conn, baseName + "BaseTransparentAs", "Another");
            if ("Transparent".equals(v)) {
                baseAsTransparentRadio.setSelected(true);
                baseTransparentAs = TransparentAs.Transparent;
            } else if ("Blend".equals(v)) {
                baseBlendRadio.setSelected(true);
                baseTransparentAs = TransparentAs.Blend;
            } else {
                baseAsOverlayRadio.setSelected(true);
                baseTransparentAs = TransparentAs.Another;
            }

            v = UserConfig.getString(conn, baseName + "OverlayTransparentAs", "Another");
            if ("Transparent".equals(v)) {
                overlayAsTransparentRadio.setSelected(true);
                overlayTransparentAs = TransparentAs.Transparent;
            } else if ("Blend".equals(v)) {
                overlayBlendRadio.setSelected(true);
                overlayTransparentAs = TransparentAs.Blend;
            } else {
                overlayAsBaseRadio.setSelected(true);
                overlayTransparentAs = TransparentAs.Another;
            }

            String mode = UserConfig.getString(conn, baseName + "BlendMode", message("MultiplyMode"));
            if (mode == null) {
                mode = message("MultiplyMode");
            }
            blendMode = PixelsBlendFactory.blendMode(mode);
            modeList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            modeList.getItems().setAll(PixelsBlendFactory.blendModes());
            modeList.scrollTo(mode);
            modeList.getSelectionModel().select(mode);

            modeList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    valueChanged();
                }
            });

            weight = UserConfig.getInt(conn, baseName + "BlendWeight", 100) / 100f;
            weight = (weight >= 0.0f && weight <= 1.0f) ? weight : 1.0f;
            weightSelector.getItems().addAll(Arrays.asList("0.5", "1.0", "0.3", "0.1", "0.8", "0.2", "0.9", "0.0"));
            weightSelector.setValue(weight + "");

            weightSelector.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> v, String ov, String nv) {
                    valueChanged();
                }
            });

            baseAboveCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> v, Boolean ov, Boolean nv) {
                    valueChanged();
                }
            });

            baseGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    valueChanged();
                }
            });

            overlayGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> v, Toggle ov, Toggle nv) {
                    valueChanged();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void valueChanged() {
        if (isSettingValues) {
            return;
        }
        changeNotify.set(!changeNotify.get());
    }

    public TransparentAs baseTransparentAs() {
        if (baseAsOverlayRadio.isSelected()) {
            baseTransparentAs = TransparentAs.Another;
        } else if (baseBlendRadio.isSelected()) {
            baseTransparentAs = TransparentAs.Blend;
        } else {
            baseTransparentAs = TransparentAs.Transparent;
        }
        return baseTransparentAs;
    }

    public TransparentAs overlayTransparentAs() {
        if (overlayAsTransparentRadio.isSelected()) {
            overlayTransparentAs = TransparentAs.Transparent;
        } else if (overlayBlendRadio.isSelected()) {
            overlayTransparentAs = TransparentAs.Blend;
        } else {
            overlayTransparentAs = TransparentAs.Another;
        }
        return overlayTransparentAs;
    }

    public float checkWeight() {
        float f = -1;
        try {
            f = Float.parseFloat(weightSelector.getValue());
        } catch (Exception e) {
        }
        if (f >= 0.0f && f <= 1.0f) {
            weight = f;
        } else {
            popError(message("InvalidParameter") + ": " + message("Weight2"));
        }
        return f;
    }

    public boolean checkValues() {
        return checkWeight() >= 0;
    }

    public PixelsBlend pickValues(float t) {
        if (t < 0) {
            if (!checkValues()) {
                return null;
            }
        } else {
            weight = t;
        }
        PixelsBlend blend = null;
        try (Connection conn = DerbyBase.getConnection()) {
            blend = pickValues(conn);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return blend;
    }

    public PixelsBlend pickValues(Connection conn) {
        try {
            String mode = modeList.getSelectionModel().getSelectedItem();
            if (mode == null) {
                mode = message("MultiplyMode");
            }
            blendMode = PixelsBlendFactory.blendMode(mode);
            UserConfig.setString(conn, baseName + "BlendMode", mode);

            UserConfig.setInt(conn, baseName + "BlendWeight", (int) (weight * 100));

            baseTransparentAs();
            UserConfig.setString(conn, baseName + "BaseTransparentAs", baseTransparentAs.name());

            overlayTransparentAs();
            UserConfig.setString(conn, baseName + "OverlayTransparentAs", overlayTransparentAs.name());

            UserConfig.setBoolean(conn, baseName + "BaseAbove", baseAboveCheck.isSelected());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return PixelsBlendFactory.create(blendMode)
                .setBlendMode(blendMode)
                .setWeight(fixWeight(weight))
                .setBaseAbove(baseAboveCheck.isSelected())
                .setBaseTransparentAs(baseTransparentAs)
                .setOverlayTransparentAs(overlayTransparentAs);
    }

    @FXML
    public void demo() {
        demo(Color.PINK);
    }

    public void demo(Color color) {
        Image baseImage = InternalImages.exampleImage();
        Image overlay = FxImageTools.createImage(
                (int) (baseImage.getWidth()), (int) (baseImage.getHeight()),
                color);
        demo(baseImage, overlay);
    }

    public void demo(Image baseImage, Image overlay) {
        if (baseImage == null || overlay == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private List<String> files;

            @Override
            protected boolean handle() {
                try {
                    BufferedImage overlayBI = SwingFXUtils.fromFXImage(overlay, null);
                    overlayBI = ScaleTools.demoImage(overlayBI);
                    BufferedImage baseBI = SwingFXUtils.fromFXImage(baseImage, null);
                    baseBI = ScaleTools.demoImage(baseBI);
                    files = new ArrayList<>();
                    int x = (int) (baseImage.getWidth() - overlay.getWidth()) / 2;
                    int y = (int) (baseImage.getHeight() - overlay.getHeight()) / 2;
                    ShapeDemos.blendImage(this, files, message("BlendColor"), baseBI, overlayBI, x, y, null);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (files != null && !files.isEmpty()) {
                    ImagesBrowserController.loadNames(files);
                }
            }

        };
        start(task);
    }

}
