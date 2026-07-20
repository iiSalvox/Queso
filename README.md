# Salvox Charge Overlay

App Android que muestra la animación de batería cargando (Salvox) a
pantalla completa cuando **enchufas el cargador con la pantalla apagada
o bloqueada**. No sustituye la animación real de "móvil apagado" (eso
lo dibuja el firmware Qualcomm y no es modificable sin riesgo), pero
da el mismo efecto visual en el uso normal del día a día.

## Cómo funciona
- `PowerConnectReceiver` escucha `ACTION_POWER_CONNECTED`. Si la
  pantalla está apagada (`PowerManager.isInteractive() == false`),
  lanza `ChargeOverlayActivity`.
- `ChargeOverlayActivity` es pantalla completa, se muestra sobre el
  bloqueo (`FLAG_SHOW_WHEN_LOCKED` + `FLAG_TURN_SCREEN_ON`) y carga
  `assets/charge_animation.html` en un WebView.
- Se cierra sola a los 15 segundos, al tocar la pantalla, o al
  desenchufar el cargador (`ACTION_POWER_DISCONNECTED`).
- `ACTION_POWER_CONNECTED` está en la lista de broadcasts implícitos
  exentos de las restricciones de Android 8+, así que el receptor
  declarado en el manifest funciona sin necesitar un servicio en
  primer plano corriendo todo el rato.

## Compilar sin PC, desde el propio Termux (GitHub Actions)
No hace falta Android Studio ni tu PC. El repo incluye
`.github/workflows/build.yml`, que compila el APK gratis en los
servidores de GitHub en cuanto subes el proyecto.

1. Crea un repo nuevo (vacío) en https://github.com/new — puede ser
   privado. Copia la URL, algo como
   `https://github.com/TU_USUARIO/SalvoxChargeOverlay.git`.

2. En Termux, instala git si no lo tienes:
   ```
   pkg install git
   ```

3. Descomprime este zip en tu móvil (por ejemplo con un gestor de
   archivos, o `unzip SalvoxChargeOverlay.zip` desde Termux si lo
   tienes en `/sdcard/Download`), entra en la carpeta y sube el
   proyecto:
   ```
   cd /sdcard/Download/SalvoxChargeOverlay
   git init
   git add .
   git commit -m "primer commit"
   git branch -M main
   git remote add origin https://github.com/TU_USUARIO/SalvoxChargeOverlay.git
   git push -u origin main
   ```
   Te pedirá usuario y un **token de acceso personal** de GitHub como
   contraseña (no la contraseña normal) — lo generas en
   github.com > Settings > Developer settings > Personal access tokens.

4. En cuanto termine el `push`, ve a la pestaña **Actions** de tu
   repo en github.com (desde el navegador del móvil vale). Verás el
   workflow "Build APK" ejecutándose — tarda unos 3-5 minutos.

5. Cuando termine (icono verde), entra en esa ejecución y descarga el
   artefacto `SalvoxChargeOverlay-debug-apk` — es un zip con el
   `app-debug.apk` dentro.

6. Descomprime, activa "orígenes desconocidos" para tu gestor de
   archivos/navegador, e instala el APK directamente en el móvil.

## Compilar con Android Studio (alternativa, si más adelante tienes el PC libre)
1. Abre la carpeta `SalvoxChargeOverlay/` como proyecto en Android
   Studio.
2. Build > Build Bundle(s) / APK(s) > Build APK(s).
3. El APK sale en `app/build/outputs/apk/debug/app-debug.apk`.

## Hacerla fiable en segundo plano (recomendado)
Android puede matar apps en segundo plano agresivamente en algunas
capas (MIUI/ROMs basadas en ella incluyen gestión de batería extra
aunque uses Project Infinity X puede heredar ajustes similares).
Para máxima fiabilidad:
1. En Ajustes > Batería > esta app, desactiva cualquier optimización
   de batería / "gestión automática" para Salvox Charge Overlay.
2. Opcional (mejor aún, ya que tienes Magisk): usa el módulo
   "Systemizer" de Magisk para convertir esta app en app de sistema.
   Las apps de sistema tienen mucha menos probabilidad de que el
   sistema las mate en segundo plano.

## Personalizar
Todo el HTML/CSS/JS de la animación está en un único fichero:
`app/src/main/assets/charge_animation.html` — ábrelo y edita colores,
velocidad de las partículas, o el tiempo de auto-cierre
(`AUTO_DISMISS_MS` en `ChargeOverlayActivity.java`) directamente.
