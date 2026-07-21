package com.salvox.chargeoverlay;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebView;

public class ChargeOverlayService extends Service {

    private static final String TAG = "SalvoxCharge";
    private static final long AUTO_DISMISS_MS = 15000;

    private WindowManager windowManager;
    private WebView webView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable dismissRunnable = this::stopSelf;

    private final BroadcastReceiver disconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
                stopSelf();
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
        registerReceiver(disconnectReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
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
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
        );

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        webView.getSettings().setJavaScriptEnabled(true);
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

    @Override
    public void onDestroy() {
        handler.removeCallbacks(dismissRunnable);
        try {
            unregisterReceiver(disconnectReceiver);
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
