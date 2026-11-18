package io.github.makaveli63.prisme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Launcher Prisme Home : une WebView plein écran qui affiche index.html (copié dans les assets au build)
 * et expose au JavaScript l'objet « PrismeAndroid » pour lister et lancer les applications installées.
 */
public class MainActivity extends Activity {

    /** Origine HTTPS locale : localStorage fonctionne et les appels réseau partent d'une origine sécurisée. */
    private static final String HOME_URL = "https://appassets.androidplatform.net/assets/index.html";

    private WebView web;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web = new WebView(this);
        web.setBackgroundColor(0xFF0B0D13);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        // Permet plus tard d'appeler un Home Assistant local en HTTP depuis la page.
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("appassets.androidplatform.net".equals(uri.getHost())) return false;
                // Tout autre lien s'ouvre dans l'application adaptée (navigateur, Play Store…).
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (ActivityNotFoundException ignored) { }
                return true;
            }
        });
        web.addJavascriptInterface(new Bridge(), "PrismeAndroid");
        setContentView(web);
        hideSystemBars();

        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl(HOME_URL);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
        web.evaluateJavascript("window.dispatchEvent(new Event('prisme-resume'))", null);
    }

    @Override
    protected void onPause() {
        web.onPause();
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Bouton Accueil pressé alors que le launcher est déjà affiché : on ferme les fenêtres ouvertes.
        web.evaluateJavascript("window.dispatchEvent(new Event('prisme-home'))", null);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Un launcher ne se ferme pas : le bouton Retour ferme seulement les fenêtres de la page.
        web.evaluateJavascript("window.dispatchEvent(new Event('prisme-back'))", null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    protected void onDestroy() {
        web.destroy();
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    /** Méthodes appelées depuis index.html via window.PrismeAndroid (thread du pont JavaScript). */
    private class Bridge {

        @JavascriptInterface
        public String listApps() {
            PackageManager pm = getPackageManager();
            Intent main = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> list = pm.queryIntentActivities(main, 0);
            JSONArray out = new JSONArray();
            for (ResolveInfo r : list) {
                String pkg = r.activityInfo.packageName;
                if (pkg.equals(getPackageName())) continue;
                try {
                    out.put(new JSONObject().put("pkg", pkg).put("label", String.valueOf(r.loadLabel(pm))));
                } catch (Exception ignored) { }
            }
            return out.toString();
        }

        @JavascriptInterface
        public boolean isInstalled(String pkg) {
            return pkg != null && getPackageManager().getLaunchIntentForPackage(pkg) != null;
        }

        /** Icône de l'application en data URL PNG, ou chaîne vide. */
        @JavascriptInterface
        public String appIcon(String pkg) {
            try {
                Drawable d = getPackageManager().getApplicationIcon(pkg);
                int size = 144;
                Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(bmp);
                d.setBounds(0, 0, size, size);
                d.draw(c);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                bmp.recycle();
                return "data:image/png;base64," + Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP);
            } catch (Exception e) {
                return "";
            }
        }

        /** Lance l'application ; renvoie false si elle n'est pas installée. */
        @JavascriptInterface
        public boolean launchApp(String pkg) {
            if (pkg == null) return false;
            Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
            if (i == null) return false;
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            final Intent launch = i;
            runOnUiThread(() -> {
                try { startActivity(launch); } catch (Exception ignored) { }
            });
            return true;
        }

        /** Ouvre la fiche Play Store de l'application (ou la page web si le Play Store est absent). */
        @JavascriptInterface
        public void openStore(String pkg) {
            runOnUiThread(() -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (ActivityNotFoundException e) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" + pkg))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } catch (ActivityNotFoundException ignored) { }
                }
            });
        }

        /** Réglages Android : « home » ouvre le choix du launcher par défaut, sinon les réglages généraux. */
        @JavascriptInterface
        public void openSettings(String which) {
            final String action = "home".equals(which) ? Settings.ACTION_HOME_SETTINGS : Settings.ACTION_SETTINGS;
            runOnUiThread(() -> {
                try {
                    startActivity(new Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            });
        }

        /** Ouvre la fiche « Infos sur l'application » (désinstaller, permissions…). */
        @JavascriptInterface
        public void openAppInfo(String pkg) {
            runOnUiThread(() -> {
                try {
                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pkg))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (ActivityNotFoundException ignored) { }
            });
        }

        @JavascriptInterface
        public boolean isDefaultLauncher() {
            Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
            ResolveInfo r = getPackageManager().resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY);
            return r != null && r.activityInfo != null && getPackageName().equals(r.activityInfo.packageName);
        }

        @JavascriptInterface
        public String version() {
            try {
                return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (Exception e) {
                return "";
            }
        }
    }
}
