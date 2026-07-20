package com.salvox.chargeoverlay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

public class PowerConnectReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
            return;
        }

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean screenOff = pm != null && !pm.isInteractive();

        // Solo se dispara si el usuario enchufa con la pantalla apagada/bloqueada,
        // tal y como se pidio. Si la pantalla ya esta encendida, no hacemos nada.
        if (!screenOff) {
            return;
        }

        Intent overlay = new Intent(context, ChargeOverlayActivity.class);
        overlay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(overlay);
    }
}
