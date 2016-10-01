package cs.android.viewbase;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;

import cs.android.CSAndroidApplication;
import cs.android.HasContext;
import cs.android.aq.CSQuery;
import cs.java.collections.CSList;
import cs.java.lang.Base;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static cs.java.lang.Lang.*;

public abstract class ContextController extends Base implements HasContext {

    private Context _context;
    private CSQuery _aq;

    public ContextController() {
        _context = CSAndroidApplication.instance();
    }

    public ContextController(Context context) {
        setContext(context);
    }

    public ContextController(HasContext context) {
        setContext(context.context());
    }

    void setContext(Context context) {
        this._context = context;
    }

    public NotificationManager notifications() {
        return (NotificationManager) context().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public Context context() {
        return _context;
    }

    public CSQuery aq() {
        if (no(_aq)) _aq = new CSQuery(this);
        return _aq;
    }

    protected void unregisterReceiver(BroadcastReceiver receiver) {
        try {
            context().unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            warn(e);
        }
    }

    public AlarmManager getAlarmManager() {
        return (AlarmManager) context().getSystemService(Context.ALARM_SERVICE);
    }

    public String getAppKeyHash() {
        try {
            PackageInfo info = context().getPackageManager().getPackageInfo(context().getPackageName(), PackageManager.GET_SIGNATURES);
            if (set(info.signatures)) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(info.signatures[0].toByteArray());
                return Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (NameNotFoundException | NoSuchAlgorithmException e) {
            error(e);
        }
        return "";
    }

    public PackageInfo getPackageInfo() {
        try {
            return _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public String getString(int id) {
        if (empty(id)) return "";
        return context().getResources().getString(id);
    }

    public String getStringResource(int id) {
        return getStringResource(id, "UTF-8");
    }

    public String getStringResource(int id, String encoding) {
        try {
            return new String(getResource(id, context()), encoding);
        } catch (UnsupportedEncodingException e) {
            error(e);
            return null;
        }
    }

    protected byte[] getResource(int id, Context context) {
        try {
            Resources resources = context.getResources();
            InputStream is = resources.openRawResource(id);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] readBuffer = new byte[4 * 1024];
            try {
                int read;
                do {
                    read = is.read(readBuffer, 0, readBuffer.length);
                    if (read == -1) break;
                    bout.write(readBuffer, 0, read);
                } while (true);
                return bout.toByteArray();
            } finally {
                close(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int dimension(int id) {
        return (int) context().getResources().getDimension(id);
    }

    public boolean isNetworkConnected() {
        ConnectivityManager connectivity = getConnectivity();
        return is(connectivity.getActiveNetworkInfo()) && connectivity.getActiveNetworkInfo().isConnected();
    }

    protected ConnectivityManager getConnectivity() {
        return (ConnectivityManager) context().getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public Bitmap loadBitmap(int id) {
        if (empty(id)) return null;
        Drawable drawable = getDrawable(id);
        return ((BitmapDrawable) drawable).getBitmap();
    }

    public Drawable getDrawable(int id) {
        return context().getResources().getDrawable(id);
    }

    protected float getBatteryPercent() {
        Intent batteryStatus = context().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return level / (float) scale;
    }

    protected int getColor(int color) {
        return context().getResources().getColor(color);
    }

    protected <T> T service(String serviceName, Class<T> serviceClass) {
        return (T) context().getSystemService(serviceName);
    }

    protected Date timeFormatParse(String text) {
        try {
            return getTimeFormat(context()).parse(text);
        } catch (ParseException e) {
            warn(e);
        }
        return null;
    }

    protected Date dateFormatParse(String text) {
        try {
            return getDateFormat(context()).parse(text);
        } catch (ParseException e) {
            warn(e);
        }
        return null;
    }

    protected String dateFormat(Date date) {
        if (is(date)) return getDateFormat(context()).format(date);
        return null;
    }

    protected String timeFormat(Date date) {
        if (is(date)) return getTimeFormat(context()).format(date);
        return null;
    }

    protected String getString(int id, Object... args) {
        if (empty(id)) return "";
        return String.format(context().getResources().getString(id), args);
    }

    protected CSList<String> getStringList(int id) {
        if (empty(id)) return list();
        return list(context().getResources().getStringArray(id));
    }

    protected CSList<Integer> getIntList(int id) {
        if (empty(id)) return list();
        int[] intArray = context().getResources().getIntArray(id);
        CSList<Integer> list = list(intArray.length);
        for (Integer integer : intArray) list.add(integer);
        return list;
    }


    public InputStream openInputStream(Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = context().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            warn(e);
        }
        return inputStream;
    }

    protected TelephonyManager getTelephony() {
        return (TelephonyManager) context().getSystemService(Context.TELEPHONY_SERVICE);
    }

    protected boolean isServiceRunning(Class<? extends Service> serviceClass) {
        for (RunningServiceInfo service : getActivityManager().getRunningServices(Integer.MAX_VALUE))
            if (serviceClass.getName().equals(service.service.getClassName())) return true;
        return false;
    }

    public ActivityManager getActivityManager() {
        return (ActivityManager) _context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    protected void startService(Class<? extends Service> serviceClass) {
        startService(new Intent(context(), serviceClass));
    }

    protected void startService(Intent intent) {
        context().startService(intent);
    }

    protected void stopService(Class<? extends Service> serviceClass) {
        stopService(new Intent(context(), serviceClass));
    }

    protected void stopService(Intent intent) {
        context().stopService(intent);
    }

    protected void onDestroy() {
        _context = null;
        _aq = null;
    }

}