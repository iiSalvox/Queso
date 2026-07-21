package com.salvox.chargeoverlay;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.webkit.WebView;

public class ChargeOverlayActivity extends Activity {

    // Se cierra sola pasado este tiempo, salvo que desenchufes antes.
    private static final long AUTO_DISMISS_MS = 15000;

    private WebView webView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable dismissRunnable = this::finish;

    private final BroadcastReceiver disconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        );

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/charge_animation.html");
        setContentView(webView);

        // Un toque en pantalla la cierra al momento.
        webView.setOnClickListener(v -> finish());

        registerReceiver(disconnectReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
        handler.postDelayed(dismissRunnable, AUTO_DISMISS_MS);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(dismissRunnable);
        try {
            unregisterReceiver(disconnectReceiver);
        } catch (IllegalArgumentException ignored) {
            // ya estaba desregistrado
        }
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
