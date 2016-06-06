package com.github.jbarr21.goproremote.common.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.github.jbarr21.goproremote.common.data.storage.ConfigStorage;

import java.lang.reflect.Field;

import rx.Observable;
import timber.log.Timber;

public final class WifiUtils {

    private WifiUtils() { }

    public static enum SecurityType { WEP, PSK }

    public static final int WIFI_CONFIG_MAX_PRIORITY = 99999;

    public static boolean isConnectedToGoProWifi(@NonNull Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            ConfigStorage configStorage = new ConfigStorage(context);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo != null ? wifiInfo.getSSID() : null;
            return !TextUtils.isEmpty(ssid) && ssid.replaceAll("\"", "").equals(configStorage.getWifiSsid());
        }
        return false;
    }

    public static Observable<WifiConfiguration> addGoProWifiNetwork(@NonNull Context context) {
        ConfigStorage configStorage = new ConfigStorage(context);
        String ssid = configStorage.getWifiSsid();
        String password = configStorage.getWifiPassword();
        int priority = WIFI_CONFIG_MAX_PRIORITY; // we are controlling the GoPro, so use max so we aren't disconnected
        return addWifiNetwork(context, ssid, password, SecurityType.PSK, priority);
    }

    public static Observable<WifiConfiguration> addWifiNetwork(@NonNull Context context,
                                                               String ssid, String password, SecurityType securityType, int priority) {
        return Observable.defer(() -> {
            try {
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", ssid);
                wifiConfig.preSharedKey = String.format("\"%s\"", password);
                wifiConfig.priority = clamp(priority, 0, WIFI_CONFIG_MAX_PRIORITY);
                setNetworkValidatedFlag(wifiConfig);

                setSecurityModes(wifiConfig, securityType);

                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                int networkId = wifiManager.addNetwork(wifiConfig);
                boolean isDisconnected = wifiManager.disconnect();
                boolean isNewNetworkEnabled = wifiManager.enableNetwork(networkId, true);
                boolean isReconnected = wifiManager.reconnect();
                Timber.d("isDisconnected = %b, isNewNetworkEnabled = %b, isReconnected = %b", isDisconnected, isNewNetworkEnabled, isReconnected);

                return Observable.just(wifiConfig);
            } catch (Exception e) {
                return Observable.error(e);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void setNetworkValidatedFlag(WifiConfiguration wifiConfig) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Class<WifiConfiguration> clazz = WifiConfiguration.class;
                Field validatedInternetAccess = clazz.getDeclaredField("validatedInternetAccess");
                validatedInternetAccess.setAccessible(true);
                validatedInternetAccess.set(wifiConfig, true);

                Field numNoInternetAccessReports = clazz.getDeclaredField("numNoInternetAccessReports");
                numNoInternetAccessReports.setAccessible(true);
                numNoInternetAccessReports.set(wifiConfig, 0);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                Timber.e(e, "Failed to accept unvalidated Wi-Fi network");
            }
        }
    }

    private static void setSecurityModes(WifiConfiguration wifiConfig, SecurityType securityType) {
        switch (securityType) {
            case PSK:
                wifiConfig.status = WifiConfiguration.Status.ENABLED;
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                break;
            case WEP:
                wifiConfig.wepTxKeyIndex = 0;
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                break;
        }
    }

    public static String ssidFromWifiChangedIntent(Intent intent) {
        WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
        return ssidFromWifiInfo(wifiInfo);
    }

    public static String ssidFromWifiInfo(WifiInfo info) {
        return info != null ? info.getSSID().replaceAll("\"", "") : null;
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
}