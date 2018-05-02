package org.kccc.sandartp4u;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.astuetz.PagerSlidingTabStrip;

import cn.jzvd.JZVideoPlayer;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int[] TAB_ICONS = {
            R.drawable.ic_tab_hand,
//            R.drawable.ic_tab_note,
            R.drawable.ic_tab_setting
    };

    private static final String[] FRAGMENT_NAMES = {
            SandArtFragment.class.getName(),
//            ContactFragment.class.getName(),
            SettingWrapperFragment.class.getName()
    };


    private PageAdapter mAdapter;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        mAdapter = new PageAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pagerContent);
        mPager.setAdapter(mAdapter);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) View.inflate(this, R.layout.tabs, null);
        tabs.setLayoutParams(new ViewGroup.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
        tabs.setViewPager(mPager);
        mActionBar.setCustomView(tabs);

        startService(new Intent(this, DownloadService.class));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadService downloadService = DownloadService.get();
        if (downloadService != null) {
            downloadService.setFinishFlag();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d(TAG, "onActivityResult " + requestCode + " " + (resultCode == RESULT_OK));
        Fragment foreground = mAdapter.getPrimaryItem();
        if (foreground != null) {
            foreground.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (JZVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        JZVideoPlayer.releaseAllVideos();
    }

    public class PageAdapter extends FragmentPagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

        private Fragment mPrimaryItem;

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return FRAGMENT_NAMES[position];
        }

        @Override
        public int getCount() {
            return FRAGMENT_NAMES.length;
        }

        @Override
        public Fragment getItem(int position) {
            String fragmentName = FRAGMENT_NAMES[position];
            return Fragment.instantiate(MainActivity.this, fragmentName);
        }

        @Override
        public int getPageIconResId(int position) {
            return TAB_ICONS[position];
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            Fragment fragment = (Fragment) object;
            if (fragment != mPrimaryItem) {
                mPrimaryItem = fragment;
                if (mPrimaryItem instanceof ForegroundCallback) {
                    ((ForegroundCallback) mPrimaryItem).onForeground();
                }
            }
        }

        public Fragment getPrimaryItem() {
            return mPrimaryItem;
        }
    }

    public interface ForegroundCallback {
        public void onForeground();
    }


}
