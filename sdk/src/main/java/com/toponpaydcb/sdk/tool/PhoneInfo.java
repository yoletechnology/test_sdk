package com.toponpaydcb.sdk.tool;


import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.toponpaydcb.sdk.YoleSdkMgr;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

public class PhoneInfo {
    public Context m_context = null;
    public static String TAG = "Yole_PhoneInfo";
    public static String countryCode = "";//国家码
    public static String imei = "";//imei
    public static String mac = "";//mac地址
    public static String packageName = "";//包名
    public static String appName = "";
    public static Drawable icon = null;
    public static String VersionName = "";//版本名
    public static String phoneModel = "";//手机品牌型号
    public static String gaid = "";//gaid
    public static String mcc_network = "";//mcc
    public static String mnc_network = "";//mnc
//    public static String network = "";
    public static String mcc_sim = "";//mcc
    public static String mnc_sim = "";//mnc
//    public static String sim = "";
    public static String language = "en";//系统语言
    public static String[] mobile = new String[2];

    public PhoneInfo(Context context) {
        m_context = context;
        countryCode = getDeviceCountryCode(m_context);
        Log.d(TAG, "countryCode:" + countryCode);
        imei = DeviceIdFactory.getInstance(m_context).getDeviceUuid();
        Log.d(TAG, "imei:" + imei);
        mac = this.getAddress();
        Log.d(TAG, "mac:" + mac);
        packageName = m_context.getPackageName();
        Log.d(TAG, "packageName:" + packageName);
        language = getLanguage();
        Log.i(TAG, "language:" + language);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            String[] SimOperator = this.getSimOperatorName();
//            network = SimOperator[0];
            mcc_network = SimOperator[1];
            mnc_network = SimOperator[2];
//            sim = SimOperator[3];
            mcc_sim = SimOperator[4];
            mnc_sim = SimOperator[5];
        }
        else
        {
            updateMccOrMnc();
        }
        Log.i(TAG, "network mcc:"+mcc_network+";mnc:" + mnc_network);
        Log.i(TAG, "sim mcc+mnc:"+mcc_sim+";mnc:" + mnc_sim);

        try {
            getVersionName();
            Log.d(TAG, "VersionName:" + VersionName);
            Log.d(TAG, "appName:" + appName);
            Log.d(TAG, "icon:" + icon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        phoneModel = Build.BRAND + " " + Build.MODEL;
        Log.d(TAG, "phoneModel:" + phoneModel);

        new Thread(new Runnable() {
            public void run() {
                try {
                    AdvertisingIdClient.AdInfo adInfo = AdvertisingIdClient.getAdvertisingIdInfo(m_context);
                    gaid = adInfo.getId();
                    Log.d(TAG, "gaid:" + gaid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }


    /**获取系统语言*/
    private static String getLanguage() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage() + "-" + locale.getCountry();

    }

    /**获取国家码**/
    private static String getDeviceCountryCode(Context context) {

        String countryCode;

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Log.d(TAG, "---------------------------------------getDeviceCountryCode" + (tm != null));
        if (tm != null) {

            // query first getSimCountryIso()

            countryCode = tm.getSimCountryIso();
            Log.d(TAG, "---------------------------------------getDeviceCountryCode==" + countryCode);
            if (countryCode != null && countryCode.length() == 2)

                return countryCode;//.toLowerCase();

            if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {

                // special case for CDMA Devices

//                countryCode = getCDMACountryIso();

            } else {

                // for 3G devices (with SIM) query getNetworkCountryIso()

                countryCode = tm.getNetworkCountryIso();

            }

            if (countryCode != null && countryCode.length() == 2)

                return countryCode;//.toLowerCase();

        }

        // if network country not available (tablets maybe), get country code from Locale class

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            countryCode = context.getResources().getConfiguration().getLocales().get(0).getCountry();

        } else {

            countryCode = context.getResources().getConfiguration().locale.getCountry();

        }

        if (countryCode != null && countryCode.length() == 2)

            return countryCode;//.toLowerCase();

        // general fallback to "us"

        return "us";

    }

    /**获取mac地址**/
    private String getAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }
                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }
                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //获取版本名
    private void getVersionName() throws Exception {
        // 获取packagemanager的实例
        PackageManager packageManager = m_context.getPackageManager();
        // getPackageName()是你当前类的包名
        PackageInfo packInfo = packageManager.getPackageInfo(m_context.getPackageName(), 0);
        VersionName = packInfo.versionName;
        appName = packInfo.applicationInfo.loadLabel(packageManager).toString();
        icon = packInfo.applicationInfo.loadIcon(packageManager);
    }
    //更新mcc和mnc
    public void updateMccOrMnc()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager mSubscriptionManager = (SubscriptionManager) m_context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            List<SubscriptionInfo> subscriptionInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
            for(int i=0;i<subscriptionInfoList.size();i++)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mcc_sim = subscriptionInfoList.get(i).getMccString();
                    mnc_sim = subscriptionInfoList.get(i).getMncString();
                }
                else
                {
                    mcc_sim = ""+subscriptionInfoList.get(i).getMcc();
                    mnc_sim = ""+subscriptionInfoList.get(i).getMnc();
                }
                Log.e(TAG,"卡"+(i+1)+"_1Mcc="+ subscriptionInfoList.get(i).getMcc());
                Log.e(TAG,"卡"+(i+1)+"_1Mnc="+ subscriptionInfoList.get(i).getMnc());
                break;
            }
        }

    }
    private String[] getSimOperatorName() {

        String[] a = new String[10];
        TelephonyManager tm = (TelephonyManager) m_context.getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tm.getNetworkOperator();
        a[0] = ""+networkOperator;
        if(networkOperator.length() <=0)
        {
            a[1] = "";
            a[2] = "";
        }
        else
        {
            a[1] = ""+networkOperator.substring(0, 3);
            a[2] = ""+networkOperator.substring(3, 5);
        }

        String simOperator = tm.getSimOperator();
        a[3] = simOperator;
        if (tm.getSimState() != TelephonyManager.SIM_STATE_READY) {
            switch (tm.getSimState()) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    Log.i(TAG, "没有Sim卡");
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    Log.i(TAG, "Sim卡状态锁定，需要PIN解锁");
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    Log.i(TAG, "Sim卡状态锁定，需要PUK解锁");
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    Log.i(TAG, "需要网络PIN码解锁");
                    break;
            }
            a[4] = "";
            a[5] = "";
            return a;
        }
        a[4] = ""+simOperator.substring(0, 3);
        a[5] = ""+simOperator.substring(3, 5);
        return a;
    }

}


class DeviceIdFactory {
    protected static final String PREFS_FILE = "device_id.xml";
    protected static final String PREFS_DEVICE_ID = "device_id";
    protected static volatile UUID uuid;
    private static volatile DeviceIdFactory mInstance;
    public static String TAG = "Yole_PhoneInfo";
    private DeviceIdFactory(Context context) {
        if (uuid == null) {
            synchronized (DeviceIdFactory.class) {
                if (uuid == null) {
                    final SharedPreferences prefs = context
                            .getSharedPreferences(PREFS_FILE, 0);
                    final String id = prefs.getString(PREFS_DEVICE_ID, null);
                    if (id != null) {
                        // Use the ids previously computed and stored in the
                        // prefs file
                        uuid = UUID.fromString(id);
                    } else {
                        final String androidId = Settings.Secure.getString(
                                context.getContentResolver(), Settings.Secure.ANDROID_ID);

                        try {
                            if (!"9774d56d682e549c".equals(androidId)) {
                                uuid = UUID.nameUUIDFromBytes(androidId
                                        .getBytes("utf8"));
                            } else {
                                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                                    String serial = null;
                                    try {
                                        serial = Build.class.getField("SERIAL").get(null).toString();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchFieldException e) {
                                        e.printStackTrace();
                                    }
                                    String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);

                                    uuid = new UUID(m_szDevIDShort.hashCode(), serial.hashCode());

                                } else {
                                    final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                                    uuid = deviceId != null ? UUID
                                            .nameUUIDFromBytes(deviceId
                                                    .getBytes("utf8")) : UUID
                                            .randomUUID();
                                }
                            }
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                        // Write the value out to the prefs file
                        prefs.edit()
                                .putString(PREFS_DEVICE_ID, uuid.toString())
                                .commit();
                    }
                }
            }
        }
    }

    public static DeviceIdFactory getInstance(Context context) {
        if (mInstance == null) {
            synchronized (DeviceIdFactory.class) {
                if (mInstance == null) {
                    mInstance = new DeviceIdFactory(context);
                }
            }
        }
        return mInstance;
    }


    public String  getDeviceUuid() {
        Log.d(TAG, "getDeviceUuid "+uuid.toString());
        return uuid.toString();
    }
}


class AdvertisingIdClient {
    public static final class AdInfo {
        private final String advertisingId;
        private final boolean limitAdTrackingEnabled;

        AdInfo(String advertisingId, boolean limitAdTrackingEnabled) {
            this.advertisingId = advertisingId;
            this.limitAdTrackingEnabled = limitAdTrackingEnabled;
        }

        public String getId() {
            return this.advertisingId;
        }

        public boolean isLimitAdTrackingEnabled() {
            return this.limitAdTrackingEnabled;
        }
    }

    public static AdInfo getAdvertisingIdInfo(Context context) throws Exception {
        if (Looper.myLooper() == Looper.getMainLooper())
            throw new IllegalStateException(
                    "Cannot be called from the main thread");

        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("com.android.vending", 0);
        } catch (Exception e) {
            throw e;
        }

        AdvertisingConnection connection = new AdvertisingConnection();
        Intent intent = new Intent(
                "com.google.android.gms.ads.identifier.service.START");
        intent.setPackage("com.google.android.gms");
        if (context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
            try {
                AdvertisingInterface adInterface = new AdvertisingInterface(
                        connection.getBinder());
                AdInfo adInfo = new AdInfo(adInterface.getId(),
                        adInterface.isLimitAdTrackingEnabled(true));
                return adInfo;
            } catch (Exception exception) {
                throw exception;
            } finally {
                context.unbindService(connection);
            }
        }
        throw new IOException("Google Play connection failed");
    }

    private static final class AdvertisingConnection implements
            ServiceConnection {
        boolean retrieved = false;
        private final LinkedBlockingQueue<IBinder> queue = new LinkedBlockingQueue<IBinder>(
                1);

        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                this.queue.put(service);
            } catch (InterruptedException localInterruptedException) {
            }
        }

        public void onServiceDisconnected(ComponentName name) {
        }

        public IBinder getBinder() throws InterruptedException {
            if (this.retrieved)
                throw new IllegalStateException();
            this.retrieved = true;
            return (IBinder) this.queue.take();
        }
    }

    private static final class AdvertisingInterface implements IInterface {
        private IBinder binder;

        public AdvertisingInterface(IBinder pBinder) {
            binder = pBinder;
        }

        public IBinder asBinder() {
            return binder;
        }

        public String getId() throws RemoteException {
            //http://www.sjsjw.com/100/000336MYM017845/
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            String id;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                binder.transact(1, data, reply, 0);
                reply.readException();
                id = reply.readString();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return id;
        }

        public boolean isLimitAdTrackingEnabled(boolean paramBoolean)
                throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            boolean limitAdTracking;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                data.writeInt(paramBoolean ? 1 : 0);
                binder.transact(2, data, reply, 0);
                reply.readException();
                limitAdTracking = 0 != reply.readInt();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return limitAdTracking;
        }
    }


}
