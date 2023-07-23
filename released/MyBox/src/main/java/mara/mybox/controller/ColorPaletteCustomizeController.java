package mara.mybox.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColorData;
import mara.mybox.db.data.ColorPalette;
import mara.mybox.db.data.ColorPaletteName;
import mara.mybox.db.table.TableColor;
import mara.mybox.db.table.TableColorPalette;
import mara.mybox.db.table.TableColorPaletteName;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-6-7
 * @License Apache License Version 2.0
 */
public class ColorPaletteCustomizeController extends BaseChildController {

    protected ControlColorPaletteSelector treeController;

    @FXML
    protected RadioButton rybRadio;
    @FXML
    protected TextField nameInput, hueFromInput, hueToInput, hueStepInput,
            brightnessFromInput, brightnessToInput, brightnessStepInput,
            saturationFromInput, saturationToInput, saturationStepInput;

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(hueFromInput, "0~359");
            NodeStyleTools.setTooltip(hueToInput, "0~359");
            NodeStyleTools.setTooltip(hueStepInput, "1~359");
            NodeStyleTools.setTooltip(brightnessFromInput, "1~100");
            NodeStyleTools.setTooltip(brightnessToInput, "1~100");
            NodeStyleTools.setTooltip(brightnessStepInput, "1~100");
            NodeStyleTools.setTooltip(saturationFromInput, "1~100");
            NodeStyleTools.setTooltip(saturationToInput, "1~100");
            NodeStyleTools.setTooltip(saturationStepInput, "1~100");
        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public void setParameters(ControlColorPaletteSelector treeController) {
        this.treeController = treeController;
    }

    public int pickValue(TextField input, String name, int min, int max) {
        try {
            int value = Integer.parseInt(input.getText());
            if (value >= min && value <= max) {
                return value;
            }
        } catch (Exception e) {
        }
        popError(message("InvalidParameter") + ": " + name);
        return -1;
    }

    @FXML
    @Override
    public void okAction() {
        try {
            String name = nameInput.getText();
            if (name == null || name.isBlank()) {
                popError(message("InvalidParameter") + ": " + message("Name"));
                return;
            }
            int hueFrom = pickValue(hueFromInput, message("Hue") + "-" + message("From"), 0, 359);
            if (hueFrom < 0) {
                return;
            }
            int hueTo = pickValue(hueToInput, message("Hue") + "-" + message("To"), 0, 359);
            if (hueTo < 0) {
                return;
            }
            int hueStep = pickValue(hueStepInput, message("Hue") + "-" + message("ValueStep"), 1, 359);
            if (hueStep <= 0) {
                return;
            }
            int brightnessFrom = pickValue(brightnessFromInput, message("Brightness") + "-" + message("From"), 1, 100);
            if (brightnessFrom < 0) {
                return;
            }
            int brightnessTo = pickValue(brightnessToInput, message("Brightness") + "-" + message("To"), 1, 100);
            if (brightnessTo < 0) {
                return;
            }
            int brightnessStep = pickValue(brightnessStepInput, message("Brightness") + "-" + message("ValueStep"), 1, 100);
            if (brightnessStep <= 0) {
                return;
            }
            int saturationFrom = pickValue(saturationFromInput, message("Saturation") + "-" + message("From"), 1, 100);
            if (saturationFrom < 0) {
                return;
            }
            int saturationTo = pickValue(saturationToInput, message("Saturation") + "-" + message("To"), 1, 100);
            if (saturationTo < 0) {
                return;
            }
            int saturationStep = pickValue(saturationStepInput, message("Saturation") + "-" + message("ValueStep"), 1, 100);
            if (saturationStep <= 0) {
                return;
            }
            long number = (Math.abs((hueTo - hueFrom) / hueStep) + 1)
                    * (Math.abs((brightnessTo - brightnessFrom) / brightnessStep) + 1)
                    * (Math.abs((saturationTo - saturationFrom) / saturationStep) + 1);
            if (!PopTools.askSure(baseTitle, message("Total") + ": " + number)) {
                return;
            }
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonCurrentTask<Void>(this) {
                private ColorPaletteName newPalatte;
                private int count = 0;

                @Override
                protected boolean handle() {
                    TableColorPaletteName tableColorPaletteName = new TableColorPaletteName();
                    TableColorPalette tableColorPalette = new TableColorPalette();
                    TableColor tableColor = new TableColor();
                    boolean ryb = rybRadio.isSelected();
                    try (Connection conn = DerbyBase.getConnection();
                            PreparedStatement queryColor = conn.prepareStatement(tableColor.QueryValue);
                            PreparedStatement insertColor = conn.prepareStatement(tableColor.insertStatement());
                            PreparedStatement insertColorPalette = conn.prepareStatement(tableColorPalette.insertStatement())) {
                        if (tableColorPaletteName.find(conn, name) != null) {
                            error = message("AlreadyExisted");
                            return false;
                        }
                        newPalatte = new ColorPaletteName(name);
                        newPalatte = tableColorPaletteName.insertData(conn, newPalatte);
                        if (newPalatte == null) {
                            return false;
                        }
                        long paletteid = newPalatte.getCpnid();
                        int hue = hueFrom;
                        conn.setAutoCommit(false);
                        while (true) {
                            if (task == null || isCancelled()) {
                                conn.close();
                                return true;
                            }
                            int saturation = saturationFrom;
                            while (true) {
                                if (task == null || task.isCancelled()) {
                                    conn.close();
                                    return true;
                                }
                                int brightness = brightnessFrom;
                                while (true) {
                                    if (task == null || task.isCancelled()) {
                                        conn.close();
                                        return true;
                                    }
                                    task.setInfo((++count) + "  "
                                            + (ryb ? message("RYBAngle") : message("Hue")) + ": " + hue + "  "
                                            + message("Saturation") + ": " + saturation + "  "
                                            + message("Brightness") + ": " + brightness + "  ");
                                    Color color = Color.hsb(ryb ? ColorConvertTools.ryb2hue(hue) : hue,
                                            saturation / 100f, brightness / 100f);
                                    ColorData colorData = new ColorData(color);
                                    queryColor.setInt(1, colorData.getColorValue());
                                    queryColor.setMaxRows(1);
                                    ResultSet results = queryColor.executeQuery();
                                    if (results != null && results.next()) {
                                        colorData = tableColor.readData(results);
                                    } else {
                                        colorData = tableColor.insertData(conn, insertColor, colorData.calculate());
                                    }
                                    if (colorData != null) {
                                        ColorPalette colorPalette = new ColorPalette()
                                                .setData(colorData)
                                                .setColorValue(colorData.getColorValue())
                                                .setPaletteid(paletteid)
                                                .setOrderNumber(count);
                                        tableColorPalette.insertData(conn, insertColorPalette, colorPalette);
                                        if (count % Database.BatchSize == 0) {
                                            conn.commit();
                                        }
                                    }
                                    if (brightnessFrom == brightnessTo) {
                                        break;
                                    } else if (brightnessFrom > brightnessTo) {
                                        brightness -= brightnessStep;
                                        if (brightness < brightnessTo) {
                                            break;
                                        }
                                    } else {
                                        brightness += brightnessStep;
                                        if (brightness > brightnessTo) {
                                            break;
                                        }
                                    }
                                }
                                if (saturationFrom == saturationTo) {
                                    break;
                                } else if (saturationFrom > saturationTo) {
                                    saturation -= saturationStep;
                                    if (saturation < saturationTo) {
                                        break;
                                    }
                                } else {
                                    saturation += saturationStep;
                                    if (saturation > saturationTo) {
                                        break;
                                    }
                                }
                            }
                            if (hueFrom == hueTo) {
                                break;
                            } else if (hueFrom > hueTo) {
                                hue -= hueStep;
                                if (hue < hueTo) {
                                    break;
                                }
                            } else {
                                hue += hueStep;
                                if (hue > hueTo) {
                                    break;
                                }
                            }
                        }
                        conn.commit();
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    treeController.palettesList.getItems().add(newPalatte);
                    treeController.palettesList.getSelectionModel().select(newPalatte);
                    treeController.popInformation(message("Create") + ": " + count);
                    close();
                }

            };
            start(task);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }


    /*
        static methods
     */
    public static ColorPaletteCustomizeController open(ControlColorPaletteSelector treeController) {
        ColorPaletteCustomizeController controller = (ColorPaletteCustomizeController) WindowTools.openChildStage(
                treeController.getMyWindow(), Fxmls.ColorPaletteCustomizeFxml);
        if (controller != null) {
            controller.setParameters(treeController);
            controller.requestMouse();
        }
        return controller;
    }

}
