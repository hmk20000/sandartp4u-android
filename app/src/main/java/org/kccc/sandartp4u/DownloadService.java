package org.kccc.sandartp4u;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class DownloadService extends IntentService {
    public static final String CLASS_NAME = DownloadService.class.getName();
    public static final String QUEUE_CHANGE = CLASS_NAME + ".ACTION_DATA_LOADED";
    public static final String KEY = "key";
    public static final String KEYS = "keys";
    public static final String LENGTH = "length";
    public static final String QUEUE = "queue";

    private static DownloadService sInstance;

    private LinkedHashSet<String> mQueue;
    private final Object mQueueLock = new Object();
    private String mCurrentMediaKey;
    private boolean mShouldFinish;
    private float mCurrentRatio;

    public DownloadService() {
        super(DownloadService.class.getSimpleName());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder<DownloadService>(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mQueue = new LinkedHashSet<String>();
        restoreQueue();
        sInstance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mShouldFinish = false;
        String key = intent.getStringExtra(KEY);
        boolean hasKey = false;
        if (key != null && !key.isEmpty()) {
            synchronized (mQueueLock) {
                mQueue.add(key);
            }
            hasKey = true;
        }

        ArrayList<String> keys = intent.getStringArrayListExtra(KEYS);
        if (keys != null && !keys.isEmpty()) {
            synchronized (mQueueLock) {
                mQueue.addAll(keys);
            }
            hasKey = true;
        }
        Logger.d("DownloadService", "onStartCommand " + hasKey);

        if (hasKey) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            lbm.sendBroadcast(new Intent(QUEUE_CHANGE));
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.d("DownloadService", "onHandleIntent " + mQueue.size());
        do {
            synchronized (mQueueLock) {
                Iterator<String> iterator = mQueue.iterator();
                if (iterator.hasNext()) {
                    mCurrentMediaKey = iterator.next();
                } else {
                    break;
                }
            }

            long length = requestAndWait(mCurrentMediaKey);
            String key = mCurrentMediaKey;
            synchronized (mQueueLock) {
                if (!mShouldFinish) mQueue.remove(mCurrentMediaKey);
                mCurrentMediaKey = null;
            }

            storeQueue();
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
            Intent broadcastIntent = new Intent(DownloadService.QUEUE_CHANGE);
            if (length > 0) {
                broadcastIntent.putExtra(KEY, key);
                broadcastIntent.putExtra(LENGTH, length);
            }
            lbm.sendBroadcast(broadcastIntent);
        } while (!mShouldFinish);
        Logger.d("DownloadService", "onHandleIntent end " + mQueue.size());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }

    private void restoreQueue() {
        SharedPreferences sharedPreferences = getSharedPreferences(CLASS_NAME, MODE_PRIVATE);
        String queueString = sharedPreferences.getString(QUEUE, null);
        if (queueString == null) {
            return;
        }

        String[] keys = queueString.split(",");
        synchronized (mQueueLock) {
            for (String key : keys) {
                mQueue.add(key);
            }
        }
        Logger.d("DownloadService", "restoreQueue " + queueString);
    }

    private void storeQueue() {
        StringBuilder sb = new StringBuilder();
        synchronized (mQueueLock) {
            for (String key : mQueue) {
                sb.append(",").append(key);
            }

        }

        SharedPreferences sharedPreferences = getSharedPreferences(CLASS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        if (sb.length() > 0) {
            edit.putString(QUEUE, sb.substring(1));
            Logger.d("DownloadService", "storeQueue " + sb.substring(1));
        } else {
            edit.remove(QUEUE);
            Logger.d("DownloadService", "storeQueue empty");
        }
        edit.commit();
    }


    private long requestAndWait(String key) {
        Logger.d("DownloadService", "requestAndWait " + key);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(key)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                Toast.makeText(this, "Couldn't communicate with the server. Try again later.", Toast.LENGTH_SHORT).show();
                return -1;
            }
            String contentLength = response.header("Content-Length");

            long totalLength = Long.parseLong(contentLength);

            byte[] buffer = new byte[2048];
            InputStream is = response.body().byteStream();
            FileOutputStream fos = new FileOutputStream(App.getStoredPath(key));
            int readByte, readTotal = 0;
            while ((readByte = is.read(buffer)) > -1 && !mShouldFinish) {
                fos.write(buffer, 0, readByte);
                readTotal += readByte;
                mCurrentRatio = (float) readTotal / totalLength;
            }
            fos.close();
            is.close();

            return totalLength;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Couldn't communicate with the server. Try again later.", Toast.LENGTH_SHORT).show();
        }
        return -1;
    }


    public static DownloadService get() {
        return sInstance;
    }

    public String getCurrentMediaKey() {
        return mCurrentMediaKey;
    }

    public float getCurrentRatio() {
        return mCurrentRatio;
    }

    public void setFinishFlag() {
        Logger.d("DownloadService", "setFinishFlag " + mQueue.size());
        mShouldFinish = true;
    }

    public Collection<String> getQueueList() {
        ArrayList<String> queue;
        Logger.d("DownloadService", "getQueueList " + mQueue.size());
        synchronized (mQueueLock) {
            queue = new ArrayList<String>(mQueue);
        }
        Logger.d("DownloadService", "getQueueList " + queue.size());
        return queue;
    }

    public void remove(String key) {
        synchronized (mQueueLock) {
            if (key != null && key.equals(mCurrentMediaKey)) {
                setFinishFlag();
            }
            mQueue.remove(key);
        }
        startService(new Intent(this, getClass()));

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.sendBroadcast(new Intent(QUEUE_CHANGE));
    }

    public void removeAll() {
        synchronized (mQueueLock) {
            setFinishFlag();
            mQueue.clear();
        }
        SharedPreferences sharedPreferences = getSharedPreferences(CLASS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.remove(QUEUE);
        edit.commit();

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        lbm.sendBroadcast(new Intent(QUEUE_CHANGE));
    }

    public boolean contains(String key) {
        if (key == null) return false;
        if (key.equals(mCurrentMediaKey)) return true;
        synchronized (mQueueLock) {
            return mQueue.contains(key);
        }
    }
}
