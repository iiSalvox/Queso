package com.salvox.chargeoverlay;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0A0A12"));
        root.setPadding(48, 96, 48, 48);

        TextView title = new TextView(this);
        title.setText("Salvox Charge Overlay");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        TextView body = new TextView(this);
        body.setText("\nActiva. Se mostrara automaticamente al enchufar el cargador "
                + "con la pantalla apagada o bloqueada.\n\n"
                + "No hace falta abrir esta pantalla de nuevo.");
        body.setTextColor(Color.parseColor("#B4B2A9"));
        body.setTextSize(14);

        root.addView(title);
        root.addView(body);
        setContentView(root);
    }
}
