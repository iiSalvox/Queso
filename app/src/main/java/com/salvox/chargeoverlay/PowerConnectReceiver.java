package com.salvox.chargeoverlay;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
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

        if (!locked) {
            Log.d(TAG, "No esta bloqueado, no se muestra overlay");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Log.e(TAG, "Falta permiso 'Mostrar sobre otras apps' - no se puede mostrar overlay");
            return;
        }

        Log.d(TAG, "Arrancando ChargeOverlayService");
        Intent serviceIntent = new Intent(context, ChargeOverlayService.class);
        context.startService(serviceIntent);
    }
}
