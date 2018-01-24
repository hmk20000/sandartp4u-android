package org.kccc.sandartp4u;

import android.app.Application;
import android.os.Environment;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by whylee on 2014. 5. 27..
 */
public class App extends Application {
    public static String sFilesPath;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.isDebuggable = true;
        sFilesPath = getFilesPath();
        new File(sFilesPath).mkdirs();
    }

    private String getFilesPath() {
        File externalDir = getExternalFilesDir(null);
        if (externalDir == null) {
            return Environment.getExternalStorageDirectory() + "/Android/data/" + getPackageName() + "/cache";
        } else {
            return externalDir.toString();
        }
    }

    public static final String getStoredPath(String url) {
        return App.sFilesPath + "/" + md5(url.getBytes());
    }

    public static final String md5(byte[] rawData) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        digest.reset();
        digest.update(rawData);
        byte[] msgDigest = digest.digest();

        StringBuffer hexPass = new StringBuffer();

        for (int i = 0; i < msgDigest.length; i++) {
            String h = Integer.toHexString(0xFF & msgDigest[i]);
            while (h.length() < 2) {
                h = "0" + h;
            }

            hexPass.append(h);
        }

        return hexPass.toString();
    }
}
