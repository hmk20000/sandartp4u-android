package org.kccc.sandartp4u;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by whylee on 2014. 5. 30..
 */
public class DetailActivity extends AppCompatActivity {
    public static final String CLASS_NAME = DetailActivity.class.getName();
    public static final String FRAGMENT_NAME = CLASS_NAME + ".FRAGMENT_NAME";
    public static final String ARGUMENTS = CLASS_NAME + ".ARGUMENTS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.merge);

        String fragmentName = getIntent().getStringExtra(FRAGMENT_NAME);
        Bundle arguments = getIntent().getBundleExtra(ARGUMENTS);

        Fragment fragment = Fragment.instantiate(this, fragmentName, arguments);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.frame, fragment, fragmentName);
        ft.commit();

    }
}
