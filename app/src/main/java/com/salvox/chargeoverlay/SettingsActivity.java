package com.salvox.chargeoverlay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0A0A12"));
        root.setPadding(48, 96, 48, 48);

        TextView title = new TextView(this);
        title.setText("Salvox Charge Overlay");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        statusText = new TextView(this);
        statusText.setTextColor(Color.parseColor("#B4B2A9"));
        statusText.setTextSize(14);
        statusText.setPadding(0, 32, 0, 32);

        Button grantButton = new Button(this);
        grantButton.setText("Conceder permiso 'Mostrar sobre otras apps'");
        grantButton.setOnClickListener(v -> requestOverlayPermission());

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.gravity = Gravity.START;

        root.addView(title);
        root.addView(statusText);
        root.addView(grantButton, buttonParams);
        setContentView(root);
    }

    private void updateStatus() {
        boolean granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || Settings.canDrawOverlays(this);
        statusText.setText(granted
                ? "Permiso concedido. La animación se mostrará automáticamente al "
                    + "enchufar el cargador con el móvil bloqueado."
                : "Falta conceder el permiso 'Mostrar sobre otras apps' para que "
                    + "la animación pueda mostrarse. Pulsa el botón de abajo.");
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }
}
