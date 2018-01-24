
package org.kccc.sandartp4u;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import java.io.File;

@SuppressWarnings("deprecation")
public class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {


    public static final String DELETE_MEDIA = "delete_media";
    public static final String DELETE_CONTACTS = "delete_contacts";
    public static final String VERSION = "version";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_setting);
        findPreference(DELETE_MEDIA).setOnPreferenceClickListener(this);
        findPreference(DELETE_CONTACTS).setOnPreferenceClickListener(this);
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            findPreference(VERSION).setTitle(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
        }


    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (DELETE_MEDIA.equals(key)) {
            File file = new File(App.sFilesPath);
            File[] files = file.listFiles();
            for (File media : files) {
                media.delete();
            }
        } else if (DELETE_CONTACTS.equals(key)) {
            SharedPreferences preferences = getSharedPreferences(ContactFragment.class.getName(), Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            edit.remove(ContactFragment.CONTACT_LIST_JSON);
            edit.commit();
        }

        return true;
    }
}
