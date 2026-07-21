package com.salvox.chargeoverlay;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PowerConnectReceiver extends BroadcastReceiver {

    private static final String TAG = "SalvoxCharge";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.getAction());

        if (!Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
            return;
        }

        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean locked = km != null && km.isKeyguardLocked();
        Log.d(TAG, "isKeyguardLocked=" + locked);

        // Se dispara si el telefono sigue bloqueado, aunque la pantalla se
        // haya encendido sola por el gesto de "levantar para despertar" al
        // coger el movil para enchufarlo.
        if (!locked) {
            Log.d(TAG, "No esta bloqueado, no se muestra overlay");
            return;
        }

        Intent overlay = new Intent(context, ChargeOverlayActivity.class);
        overlay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        Log.d(TAG, "Lanzando ChargeOverlayActivity");
        context.startActivity(overlay);
    }
}
