
package org.kccc.sandartp4u;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;


@SuppressWarnings("deprecation")
public class SettingWrapperFragment extends Fragment {
//    public static final String TITLE = "Preferences";

    private LocalActivityManager lam;
    private Window nestedWindow;
    private LinearLayout mLinearLayout;
    private boolean contentAdded;
    private AppCompatActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
        lam = new LocalActivityManager(activity, true);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lam.dispatchCreate(savedInstanceState);
        nestedWindow = lam.startActivity(SettingActivity.class.getName(), new Intent(mActivity, SettingActivity.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return mLinearLayout = new LinearLayout(getActivity());
    }


    @Override
    public void onStart() {
        super.onStart();
        lam.dispatchResume();
        if (!contentAdded) {
            final View decorView = nestedWindow.getDecorView();
            mLinearLayout.addView(decorView);
            contentAdded = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        lam.dispatchPause(getActivity().isFinishing());
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLinearLayout.removeAllViews();
        contentAdded = false;
    }

    @Override
    public void onDestroy() {
        lam.dispatchDestroy(getActivity().isFinishing());
        super.onDestroy();
    }

}
