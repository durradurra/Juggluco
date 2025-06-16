package tk.glucodata;

import static tk.glucodata.Applic.Toaster;
import static tk.glucodata.Applic.isWearable;
import static tk.glucodata.Applic.useZXing;
import static tk.glucodata.InsulinTypeHolder.getradiobutton;
import static tk.glucodata.Log.doLog;
import static tk.glucodata.MainActivity.REQUEST_BARCODE;
import static tk.glucodata.MainActivity.REQUEST_BARCODE_SIB2;
import static tk.glucodata.ZXing.scanZXingAlg;
import static tk.glucodata.settings.Settings.removeContentView;
import static tk.glucodata.util.getbutton;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Sibionics {
    private static final String LOG_ID = "Sibionics";

    // ... (другие методы остались без изменений)

    private static void selectType(String name, long dataptr, MainActivity act) {
        int subtype = Natives.getSiSubtype(dataptr);

        var group = new RadioGroup(act);
        int id = 0;
        group.addView(getradiobutton(act, R.string.eusibionics, id++));
        group.addView(getradiobutton(act, R.string.hematonix, id++));
        group.addView(getradiobutton(act, R.string.chsibionics, id++));
        group.addView(getradiobutton(act, R.string.sibionics2, id));
        group.check(subtype);
        var ok = getbutton(act, R.string.ok);
        int height = GlucoseCurve.getheight();
        int width = GlucoseCurve.getwidth();
        final int rand = (int) tk.glucodata.GlucoseCurve.metrics.density * 15;
        group.setPadding(rand, rand, (int) tk.glucodata.GlucoseCurve.metrics.density * 25, (int) tk.glucodata.GlucoseCurve.metrics.density * 20);
        var layout = new Layout(act, (l, w, h) -> {
            l.setX((width - w) * .5f);
            l.setY((height - h) * .3f);
            return new int[]{w, h};
        }, new View[]{group}, new View[]{ok});
        layout.setBackgroundColor(Applic.backgroundcolor);
        layout.setPadding(0, 0, 0, rand);

        MainActivity.setonback(() -> {
            removeContentView(layout);
            int type = group.getCheckedRadioButtonId();
            Log.i(LOG_ID, "getCheckedRadioButtonId()=" + type);
            if (type >= 0) {
                Natives.setSiSubtype(dataptr, type);
                if (type == 3) {
                    Confirm.ask2(act, act.getString(R.string.scantranstitle), act.getString(R.string.scantransmessage),
                        () -> {
                            scanner(act, REQUEST_BARCODE_SIB2, dataptr);
                        },
                        () -> {
                            // [BYPASS] Не завершаем сенсор
                            Log.i(LOG_ID, "Sensor expiration bypassed, not calling finishSensor()");
                            // Natives.finishSensor(dataptr);
                            // Natives.freedataptr(dataptr);
                            // SensorBluetooth.sensorEnded(name);
                        }
                    );
                }
            }
        });
        ok.setOnClickListener(v -> MainActivity.doonback());

        act.addContentView(layout, new ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
    }

    static void transmitterScanCancelled(long dataptr2) {
        if (dataptr2 != 0L) {
            // [BYPASS] Пропускаем завершение сенсора
            Log.i(LOG_ID, "Sensor expiration bypassed in transmitterScanCancelled()");
            // Natives.finishSensor(dataptr2);
            var serial = Natives.getSensorName(dataptr2);
            Natives.freedataptr(dataptr2);
            if (serial != null) {
                Log.i(LOG_ID, "transmitterScanCancelled (but not ended): " + serial);
                // SensorBluetooth.sensorEnded(serial); // [BYPASS]
            }
        } else {
            Log.i(LOG_ID, "transmitterScanCancelled dataptr==0");
        }
    }

    // ... (остальная часть файла без изменений)
}
