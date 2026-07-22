package com.salvox.chargeoverlay;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ChargeOverlayService extends Service {

    private static final String TAG = "SalvoxCharge";
    private static final long AUTO_DISMISS_MS = 15000;

    private WindowManager windowManager;
    private WebView webView;
    private boolean pageReady = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable dismissRunnable = this::stopSelf;

    // Se dispara cuando se desenchufa, y tambien cada vez que cambia el
    // nivel real de bateria mientras el overlay esta visible, para
    // mantener el numero sincronizado con el que reporta Android.
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
                stopSelf();
                return;
            }
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                pushBatteryLevel(intent);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ChargeOverlayService: onStartCommand");
        showOverlay();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);

        handler.postDelayed(dismissRunnable, AUTO_DISMISS_MS);
        return START_NOT_STICKY;
    }

    private void showOverlay() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        int overlayType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        webView.getSettings().setJavaScriptEnabled(true);

        // Pantalla completa de verdad: oculta barra de estado y de
        // navegacion, dibuja por debajo de ambas.
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        webView.setSystemUiVisibility(uiFlags);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                pageReady = true;
                pushCurrentBatteryLevel();
            }
        });

        webView.loadUrl("file:///android_asset/charge_animation.html");
        webView.setOnClickListener(v -> stopSelf());

        try {
            windowManager.addView(webView, params);
            Log.d(TAG, "Overlay añadido correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error añadiendo overlay: " + e.getMessage());
            stopSelf();
        }
    }

    private void pushCurrentBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        if (batteryStatus != null) {
            pushBatteryLevel(batteryStatus);
        }
    }

    private void pushBatteryLevel(Intent batteryIntent) {
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level < 0 || scale <= 0 || webView == null || !pageReady) {
            return;
        }
        int pct = Math.round(100f * level / scale);
        Log.d(TAG, "Nivel de bateria real: " + pct + "%");
        webView.evaluateJavascript("setBatteryLevel(" + pct + ");", null);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(dismissRunnable);
        try {
            unregisterReceiver(batteryReceiver);
        } catch (IllegalArgumentException ignored) {
        }
        if (webView != null && windowManager != null) {
            try {
                windowManager.removeView(webView);
            } catch (IllegalArgumentException ignored) {
            }
            webView.destroy();
        }
        super.onDestroy();
    }
}
