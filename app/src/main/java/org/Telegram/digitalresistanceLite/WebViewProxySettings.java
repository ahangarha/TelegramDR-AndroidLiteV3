/*
Licensed under Creative Commons Zero (CC0).
https://creativecommons.org/publicdomain/zero/1.0/
*/

package org.Telegram.digitalresistanceLite;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.os.Build;
import android.os.Parcelable;
import android.util.ArrayMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WebViewProxySettings {

    public static void setLocalProxy(Context ctx, int port) {
        setProxy(ctx, "localhost", port);
    }

    /* 
    Proxy setting code taken directly from Orweb, with some modifications.
    (...And some of the Orweb code was taken from an earlier version of our code.)
    See: https://github.com/guardianproject/Orweb/blob/master/src/org/torproject/android/OrbotHelper.java#L39
    Note that we tried and abandoned doing feature detection by trying the 
    newer (>= ICS) proxy setting, catching, and then failing over to the older
    approach. The problem was that on Android 3.0, an exception would be thrown
    *in another thread*, so we couldn't catch it and the whole app would force-close.
    Orweb has always been doing an explicit version check, and it seems to work,
    so we're so going to switch to that approach.
    */
    private static boolean setProxy(Context ctx, String host, int port) {
        boolean worked = false;

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            worked = setWebkitProxyKitKat(ctx.getApplicationContext(), host, port);
        } else {
            worked = setWebkitProxyLollipop(ctx.getApplicationContext(), host, port);
        }

        return worked;
    }

    // http://stackoverflow.com/questions/19979578/android-webview-set-proxy-programatically-kitkat
    // http://src.chromium.org/viewvc/chrome/trunk/src/net/android/java/src/org/chromium/net/ProxyChangeListener.java
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressWarnings("rawtypes")
    private static boolean setWebkitProxyKitKat(Context appContext, String host, int port) {
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port + "");
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port + "");
        try {
            Class applicationClass = Class.forName("android.app.Application");
            Field loadedApkField = applicationClass.getDeclaredField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(appContext);
            Class loadedApkClass = Class.forName("android.app.LoadedApk");
            Field receiversField = loadedApkClass.getDeclaredField("mReceivers");
            receiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object receiver : ((ArrayMap) receiverMap).keySet()) {
                    Class receiverClass = receiver.getClass();
                    if (receiverClass.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = receiverClass.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);

                        final String CLASS_NAME = "android.net.ProxyProperties";
                        Class proxyPropertiesClass = Class.forName(CLASS_NAME);
                        Constructor constructor = proxyPropertiesClass.getConstructor(String.class, Integer.TYPE, String.class);
                        constructor.setAccessible(true);
                        Object proxyProperties = constructor.newInstance(host, port, null);
                        intent.putExtra("proxy", (Parcelable) proxyProperties);

                        onReceiveMethod.invoke(receiver, appContext, intent);
                    }
                }
            }
            return true;
        } catch (ClassNotFoundException ignored) {
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalArgumentException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        } catch (NoSuchMethodException ignored) {
        } catch (InstantiationException ignored) {
        }
        return false;
    }

    // http://stackanswers.com/questions/25272393/android-webview-set-proxy-programmatically-on-android-l
    @TargetApi(Build.VERSION_CODES.KITKAT) // for android.util.ArrayMap methods
    @SuppressWarnings("rawtypes")
    private static boolean setWebkitProxyLollipop(Context appContext, String host, int port) {
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port + "");
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port + "");
        try {
            Class applictionClass = Class.forName("android.app.Application");
            Field mLoadedApkField = applictionClass.getDeclaredField("mLoadedApk");
            mLoadedApkField.setAccessible(true);
            Object mloadedApk = mLoadedApkField.get(appContext);
            Class loadedApkClass = Class.forName("android.app.LoadedApk");
            Field mReceiversField = loadedApkClass.getDeclaredField("mReceivers");
            mReceiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) mReceiversField.get(mloadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object receiver : ((ArrayMap) receiverMap).keySet()) {
                    Class clazz = receiver.getClass();
                    if (clazz.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);
                        onReceiveMethod.invoke(receiver, appContext, intent);
                    }
                }
            }
            return true;
        } catch (ClassNotFoundException e) {
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        }
        return false;
    }
}
