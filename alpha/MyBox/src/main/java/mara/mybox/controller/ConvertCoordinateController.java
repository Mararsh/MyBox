package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebView;
import mara.mybox.data.GeoCoordinateSystem;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.GeographyCode;
import static mara.mybox.db.data.GeographyCodeTools.toGCJ02ByWebService;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.LocationTools;
import static mara.mybox.tools.LocationTools.latitudeToDmsString;
import static mara.mybox.tools.LocationTools.longitudeToDmsString;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import thridparty.CoordinateConverter;

/**
 * @Author Mara
 * @CreateDate 2020-8-4
 * @License Apache License Version 2.0
 */
public class ConvertCoordinateController extends BaseMapController {

    protected int degrees, minutes;
    protected double seconds, longitude, latitude, coordinate;

    @FXML
    protected Button equalDButton, equalDmsButton, equalCsButton;
    @FXML
    protected RadioButton wgs84Radio, gcj02Radio, bd09Radio, cgcs2000Radio, mapbarRadio;
    @FXML
    protected TextField degreesInput, minutesInput, secondsInput, dmsInput, decimalInput,
            longitudeInput, latitudeInput;
    @FXML
    protected WebView csView;

    public ConvertCoordinateController() {
        baseTitle = Languages.message("ConvertCoordinate");
        TipsLabelKey = "ConvertCoordinateTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            webEngine = csView.getEngine();
            degrees = minutes = 0;
            seconds = longitude = latitude = 0;

            degreesInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    isSettingValues = true;
                    dmsInput.clear();
                    decimalInput.clear();
                    dmsInput.setStyle(null);
                    decimalInput.setStyle(null);
                    isSettingValues = false;
                    if (newValue == null || newValue.trim().isBlank()) {
                        degrees = 0;
                        degreesInput.setStyle(null);
                        isSettingValues = true;
                        dmsInput.setText(LocationTools.dmsString(degrees, minutes, seconds));
                        coordinate = LocationTools.DMS2Coordinate(degrees, minutes, seconds);
                        decimalInput.setText(DoubleTools.scale(coordinate, 8) + "");
                        isSettingValues = false;
                        return;
                    }
                    try {
                        int v = Integer.parseInt(newValue.trim());
                        if (v >= -180 && v <= 180) {
                            degrees = v;
                            degreesInput.setStyle(null);
                            isSettingValues = true;
                            dmsInput.setText(LocationTools.dmsString(degrees, minutes, seconds));
                            coordinate = LocationTools.DMS2Coordinate(degrees, minutes, seconds);
                            decimalInput.setText(DoubleTools.scale(coordinate, 8) + "");
                            isSettingValues = false;
                        } else {
                            degreesInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        degreesInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            minutesInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    isSettingValues = true;
                    dmsInput.clear();
                    decimalInput.clear();
                    dmsInput.setStyle(null);
                    decimalInput.setStyle(null);
                    isSettingValues = false;
                    if (newValue == null || newValue.trim().isBlank()) {
                        minutes = 0;
                        minutesInput.setStyle(null);
                        isSettingValues = true;
                        dmsInput.setText(LocationTools.dmsString(degrees, minutes, seconds));
                        coordinate = LocationTools.DMS2Coordinate(degrees, minutes, seconds);
                        decimalInput.setText(DoubleTools.scale(coordinate, 8) + "");
                        isSettingValues = false;
                        return;
                    }
                    try {
                        int v = Integer.parseInt(newValue.trim());
                        if (v >= 0 && v < 60) {
                            minutes = v;
                            minutesInput.setStyle(null);
                            isSettingValues = true;
                            dmsInput.setText(LocationTools.dmsString(degrees, minutes, seconds));
                            coordinate = LocationTools.DMS2Coordinate(degrees, minutes, seconds);
                            decimalInput.setText(DoubleTools.scale(coordinate, 8) + "");
                            isSettingValues = false;
                        } else {
                            minutesInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        minutesInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            secondsInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    isSettingValues = true;
                    dmsInput.clear();
                    decimalInput.clear();
                    dmsInput.setStyle(null);
                    decimalInput.setStyle(null);
                    isSettingValues = false;
                    if (newValue == null || newValue.trim().isBlank()) {
                        secondsInput.setStyle(null);
                        seconds = 0;
                        isSettingValues = true;
                        dmsInput.setText(LocationTools.dmsString(degrees, minutes, seconds));
                        coordinate = LocationTools.DMS2Coordinate(degrees, minutes, seconds);
                        decimalInput.setText(DoubleTools.scale(coordinate, 8) + "");
                        isSettingValues = false;
                        return;
                    }
                    try {
                        double v = Double.parseDouble(newValue.trim());
                        if (v >= 0 && v < 60) {
                            seconds = v;
                            secondsInput.setStyle(null);
                            isSettingValues = true;
                            dmsInput.setText(LocationTools.dmsString(degrees, minutes, seconds));
                            coordinate = LocationTools.DMS2Coordinate(degrees, minutes, seconds);
                            decimalInput.setText(DoubleTools.scale(coordinate, 8) + "");
                            isSettingValues = false;
                        } else {
                            secondsInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        secondsInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            dmsInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    isSettingValues = true;
                    degreesInput.clear();
                    minutesInput.clear();
                    secondsInput.clear();
                    decimalInput.clear();
                    degreesInput.setStyle(null);
                    minutesInput.setStyle(null);
                    secondsInput.setStyle(null);
                    decimalInput.setStyle(null);
                    isSettingValues = false;
                    if (newValue == null || newValue.trim().isBlank()) {
                        coordinate = 0;
                        dmsInput.setStyle(null);
                        return;
                    }
                    double[] v = LocationTools.parseDMS(newValue);
                    if (v[0] < -180) {
                        dmsInput.setStyle(UserConfig.badStyle());
                    } else {
                        dmsInput.setStyle(null);
                        coordinate = v[0];
                        degrees = (int) v[1];
                        minutes = (int) v[2];
                        seconds = v[3];
                        isSettingValues = true;
                        decimalInput.setText(DoubleTools.scale(coordinate, 8) + "");
                        degreesInput.setText(degrees + "");
                        minutesInput.setText(minutes + "");
                        secondsInput.setText(DoubleTools.scale(seconds, 4) + "");
                        isSettingValues = false;
                    }
                }
            });

            decimalInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    isSettingValues = true;
                    degreesInput.clear();
                    minutesInput.clear();
                    secondsInput.clear();
                    dmsInput.clear();
                    degreesInput.setStyle(null);
                    minutesInput.setStyle(null);
                    secondsInput.setStyle(null);
                    dmsInput.setStyle(null);
                    isSettingValues = false;
                    if (newValue == null || newValue.trim().isBlank()) {
                        decimalInput.setStyle(null);
                        coordinate = 0;
                        degrees = 0;
                        minutes = 0;
                        seconds = 0;
                        return;
                    }
                    try {
                        double v = Double.parseDouble(newValue.trim());
                        if (v >= -180 && v <= 180) {
                            coordinate = v;
                            decimalInput.setStyle(null);
                            double[] dms = LocationTools.coordinate2DMS(coordinate);
                            degrees = (int) dms[0];
                            minutes = (int) dms[1];
                            seconds = dms[2];
                            isSettingValues = true;
                            dmsInput.setText(LocationTools.dmsString(degrees, minutes, seconds));
                            degreesInput.setText(degrees + "");
                            minutesInput.setText(minutes + "");
                            secondsInput.setText(DoubleTools.scale(seconds, 4) + "");
                            isSettingValues = false;
                        } else {
                            decimalInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
//                        MyBoxLog.debug(e.toString());
                        decimalInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            longitudeInput.textProperty()
                    .addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue
                        ) {
                            if (newValue == null || newValue.trim().isBlank()) {
                                longitude = 0;
                                longitudeInput.setStyle(null);
                                return;
                            }
                            try {
                                double v = Double.parseDouble(newValue.trim());
                                if (v >= -180 && v <= 180) {
                                    longitude = v;
                                    longitudeInput.setStyle(null);
                                } else {
                                    longitudeInput.setStyle(UserConfig.badStyle());
                                }
                            } catch (Exception e) {
                                longitudeInput.setStyle(UserConfig.badStyle());
                            }
                        }
                    });

            latitudeInput.textProperty()
                    .addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue
                        ) {
                            if (newValue == null || newValue.trim().isBlank()) {
                                latitude = 0;
                                latitudeInput.setStyle(null);
                                return;
                            }
                            try {
                                double v = Double.parseDouble(newValue.trim());
                                if (v >= -90 && v <= 90) {
                                    latitude = v;
                                    latitudeInput.setStyle(null);
                                } else {
                                    latitudeInput.setStyle(UserConfig.badStyle());
                                }
                            } catch (Exception e) {
                                latitudeInput.setStyle(UserConfig.badStyle());
                            }
                        }
                    });

            equalCsButton.disableProperty().bind(
                    longitudeInput.styleProperty().isEqualTo(UserConfig.badStyle())
                            .or(latitudeInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            decimalInput.setText("48.853411");
            longitudeInput.setText("117.0983");
            latitudeInput.setText("36.25551");
            csConvert();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void csConvert() {
        try {
            webEngine.load("");

            double[] inputted = {longitude, latitude};
            double[] gcj02 = null, db09 = null, wgs84 = null, mapbar = null;
            longitude = DoubleTools.scale(longitude, 6);
            latitude = DoubleTools.scale(latitude, 6);
            if (bd09Radio.isSelected()) {
                db09 = inputted;
                gcj02 = CoordinateConverter.BD09ToGCJ02(db09[0], db09[1]);
                wgs84 = CoordinateConverter.GCJ02ToWGS84(gcj02[0], gcj02[1]);
            } else if (mapbarRadio.isSelected()) {
                mapbar = inputted;
                gcj02 = toGCJ02ByWebService(GeoCoordinateSystem.Mapbar(), mapbar[0], mapbar[1]);
                wgs84 = CoordinateConverter.GCJ02ToWGS84(gcj02[0], gcj02[1]);
                db09 = CoordinateConverter.GCJ02ToBD09(gcj02[0], gcj02[1]);
            } else if (wgs84Radio.isSelected()) {
                wgs84 = inputted;
                gcj02 = CoordinateConverter.WGS84ToGCJ02(wgs84[0], wgs84[1]);
                db09 = CoordinateConverter.GCJ02ToBD09(gcj02[0], gcj02[1]);

            } else if (gcj02Radio.isSelected()) {
                gcj02 = inputted;
                wgs84 = CoordinateConverter.GCJ02ToWGS84(gcj02[0], gcj02[1]);
                db09 = CoordinateConverter.GCJ02ToBD09(gcj02[0], gcj02[1]);

            } else if (cgcs2000Radio.isSelected()) {
                wgs84 = inputted;
                gcj02 = CoordinateConverter.WGS84ToGCJ02(wgs84[0], wgs84[1]);
                db09 = CoordinateConverter.GCJ02ToBD09(gcj02[0], gcj02[1]);

            } else {
                return;
            }
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(Languages.message("CoordinateSystem"),
                    Languages.message("Longitude"), Languages.message("Latitude"),
                    Languages.message("Longitude") + "-" + Languages.message("DegreesMinutesSeconds"),
                    Languages.message("Latitude") + "-" + Languages.message("DegreesMinutesSeconds")
            ));
            StringTable table = new StringTable(names);
            List<String> row;

            row = new ArrayList<>();
            row.addAll(Arrays.asList(Languages.message("CGCS2000"), wgs84[0] + "", wgs84[1] + "",
                    longitudeToDmsString(wgs84[0]), latitudeToDmsString(wgs84[1])
            ));
            table.add(row);

            row = new ArrayList<>();
            row.addAll(Arrays.asList(Languages.message("GCJ_02"), gcj02[0] + "", gcj02[1] + "",
                    longitudeToDmsString(gcj02[0]), latitudeToDmsString(gcj02[1])
            ));
            table.add(row);

            row = new ArrayList<>();
            row.addAll(Arrays.asList(Languages.message("WGS_84"), wgs84[0] + "", wgs84[1] + "",
                    longitudeToDmsString(wgs84[0]), latitudeToDmsString(wgs84[1])
            ));
            table.add(row);

            row = new ArrayList<>();
            row.addAll(Arrays.asList(Languages.message("BD_09"), db09[0] + "", db09[1] + "",
                    longitudeToDmsString(db09[0]), latitudeToDmsString(db09[1])
            ));
            table.add(row);
            if (mapbar != null) {
                row = new ArrayList<>();
                row.addAll(Arrays.asList(Languages.message("Mapbar"), mapbar[0] + "", mapbar[1] + "",
                        longitudeToDmsString(mapbar[0]), latitudeToDmsString(mapbar[1])
                ));
                table.add(row);
            }

            webEngine.loadContent(table.html());

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    public void popDMSExamples(MouseEvent mouseEvent) {
        try {
            List<MenuItem> items = new ArrayList<>();

            MenuItem menu = new MenuItem("48°51'12.28\"");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("48°51'12.28\"");
            });
            items.add(menu);

            menu = new MenuItem("48°51'12.28\"N");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("48°51'12.28\"N");
            });
            items.add(menu);

            menu = new MenuItem("2°20'55.68\"E");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("2°20'55.68\"E");
            });
            items.add(menu);

            menu = new MenuItem("S 34°36'13.4028\"");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("S 34°36'13.4028\"");
            });
            items.add(menu);

            menu = new MenuItem("W 58°22'53.7348\"");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("W 58°22'53.7348\"");
            });
            items.add(menu);

            menu = new MenuItem("-77°3'43.9308\"");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("-77°3'43.9308\"");
            });
            items.add(menu);

            menu = new MenuItem("longitude 12°2'52.1376\"");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("longitude 12°2'52.1376\"");
            });
            items.add(menu);

            menu = new MenuItem("latitude -77°3'43.9308\"");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("latitude -77°3'43.9308\"");
            });
            items.add(menu);

            menu = new MenuItem("118度48分54.152秒");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("118度48分54.152秒");
            });
            items.add(menu);

            menu = new MenuItem("-32度04分10.461秒");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("-32度04分10.461秒");
            });
            items.add(menu);

            menu = new MenuItem("东经118度48分54.152秒");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("东经118度48分54.152秒");
            });
            items.add(menu);

            menu = new MenuItem("西经118度48分54.152秒");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("西经118度48分54.152秒");
            });
            items.add(menu);

            menu = new MenuItem("北纬32度04分10.461秒");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("北纬32度04分10.461秒");
            });
            items.add(menu);

            menu = new MenuItem("南纬32度04分10.461秒");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("南纬32度04分10.461秒");
            });
            items.add(menu);

            menu = new MenuItem("西118度48分54.152秒");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("西118度48分54.152秒");
            });
            items.add(menu);

            menu = new MenuItem("南32度04分10.461秒");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("南32度04分10.461秒");
            });
            items.add(menu);

            menu = new MenuItem("经度118度48分54.152秒");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("经度118度48分54.152秒");
            });
            items.add(menu);

            menu = new MenuItem("纬度-32度04分10.461秒");
            menu.setOnAction((ActionEvent event) -> {
                dmsInput.setText("纬度-32度04分10.461秒");
            });
            items.add(menu);

            popMouseMenu(mouseEvent, items);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void locationAction(ActionEvent event) {
        try {
            CoordinatePickerController controller = CoordinatePickerController.open(this, longitude, latitude);
            controller.notify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    setGeographyCode(controller.geographyCode);
                    controller.closeStage();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setGeographyCode(GeographyCode code) {
        try {
            if (code == null) {
                return;
            }
            if (code.getLongitude() >= -180 && code.getLongitude() <= 180) {
                longitudeInput.setText(code.getLongitude() + "");
            } else {
                longitudeInput.clear();
            }
            if (code.getLatitude() >= -90 && code.getLatitude() <= 90) {
                latitudeInput.setText(code.getLatitude() + "");
            } else {
                latitudeInput.clear();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
