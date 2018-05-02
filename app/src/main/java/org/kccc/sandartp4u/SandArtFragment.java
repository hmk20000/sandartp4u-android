package org.kccc.sandartp4u;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.google.gson.Gson;
import com.pascalwelsch.holocircularprogressbar.HoloCircularProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;


public class SandArtFragment extends ListFragment implements MainActivity.ForegroundCallback, ServiceConnection {
    public static final String MEDIA_LIST_JSON = "media_list.json";
    private static final String KEY = SandArtFragment.class.getSimpleName();

    private List<Media> mList;
    private AppCompatActivity mActivity;
    private MediaAdapter mAdapter;
    private ListView mListView;

    private IInAppBillingService mService;


    private View.OnClickListener mDownloadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("Download Click", "onClick: 다운로드 시작");
            String key = (String) view.getTag();
            if (key == null) return;
            Intent service = new Intent(mActivity, DownloadService.class);
            service.putExtra(DownloadService.KEY, key);
            mActivity.startService(service);
        }
    };

    private View.OnClickListener mCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String key = (String) view.getTag();
            if (key == null) return;
            DownloadService downloadService = DownloadService.get();
            if (downloadService != null) downloadService.remove(key);

        }
    };

    private View.OnClickListener mPlayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String key = (String) view.getTag();
            if (key == null) return;
//
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            File file = new File(App.getStoredPath(key));
//            intent.setDataAndType(Uri.fromFile(file), "video/mp4");
//            startActivity(intent);

            File file = new File(App.getStoredPath(key));
            JZVideoPlayerStandard.startFullscreen(getActivity(),JZVideoPlayerStandard.class,Uri.fromFile(file).toString());
//            JZVideoPlayerStandard.startFullscreen(this, JZVideoPlayerStandard.class, "http://2449.vod.myqcloud.com/2449_22ca37a6ea9011e5acaaf51d105342e3.f20.mp4", "嫂子辛苦了");
        }
    };


    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DownloadService.QUEUE_CHANGE.equals(action)) {

                String key = intent.getStringExtra(DownloadService.KEY);
                if (key != null) {
                    long length = intent.getLongExtra(DownloadService.LENGTH, 0);
                    for (Media media : mList) {
                        if (media.url.equals(key)) {
                            media.fileSize = length;
                            File file = new File(App.getStoredPath(media.url));
                            media.downloadSize = file.length();
                            break;
                        }
                    }
                }

                try {
                    mAdapter.notifyDataSetChanged();
                } catch (NullPointerException ignored) {
                }
            }
        }
    };

    private final OnScrollListener onScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView v, int scrollState) {
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                mHandler.sendEmptyMessage(0);
            } else {
                mHandler.removeMessages(0);
            }
        }

        @Override
        public void onScroll(
                AbsListView v, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    };

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            setProgress();
        }

    };
    private LinkedHashMap<String, Media> mediaMap;
    private AsyncTask<Object, Object, Object> mAsyncTask;


    private void setProgress() {

        final int first = mListView.getFirstVisiblePosition();
        final int last = mListView.getLastVisiblePosition();
        int headerViewsCount = mListView.getHeaderViewsCount();
        for (int i = first; i <= last; i++) {
            try {
                int position = i - headerViewsCount;
                if (position < 0) continue;

                final Media media = mAdapter.getItem(position);

                final HoloCircularProgressBar progress = (HoloCircularProgressBar) mListView.findViewWithTag(media);
                assert progress != null;
                DownloadService downloadService = DownloadService.get();
                String url = downloadService.getCurrentMediaKey();
                float currentRatio = downloadService.getCurrentRatio();
                if (media.url.equals(url)) {
                    progress.setProgress(currentRatio);
                } else {
                    progress.setProgress(0);
                }
            } catch (ClassCastException ignored) {
            } catch (NullPointerException ignored) {
            }
        }
        mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 1000);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout v = (LinearLayout) inflater.inflate(R.layout.fragment_sand_art, container, false);
        assert v != null;
        v.addView(super.onCreateView(inflater, container, savedInstanceState));
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
//        if (!mActivity.getPackageManager().queryIntentServices(serviceIntent, 0).isEmpty()) {
            // service available to handle that Intent
            serviceIntent.setPackage("com.android.vending");

            mActivity.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
//        }

//        mActivity.bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), this, Context.BIND_AUTO_CREATE);
*/

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        mActivity.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);

        restoreList();
        readOwnedItem();
//        readSkuList();


        mListView = getListView();
        mListView.setOnScrollListener(onScrollListener);
        mAdapter = new MediaAdapter();
        setListAdapter(mAdapter);

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
        final IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.QUEUE_CHANGE);
        lbm.registerReceiver(mLocalBroadcastReceiver, filter);

        mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = IInAppBillingService.Stub.asInterface(service);
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        mAsyncTask = new AsyncTask<Object, Object, Object>() {

            @Override
            protected Object doInBackground(Object... objects) {
                readOwnedItem();
//                readSkuList();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (!isCancelled()) mAdapter.notifyDataSetChanged();
            }
        };
        mAsyncTask.execute();


    }

    private void readSkuList() {
        if (mService == null) return;
        Logger.d(KEY, "skuList start");
        ArrayList<String> skuList = new ArrayList<String>(mediaMap.keySet());
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
        Bundle skuDetails = null;
        try {
            skuDetails = mService.getSkuDetails(3, mActivity.getPackageName(), "inapp", querySkus);


            int response = skuDetails.getInt("RESPONSE_CODE");
            Logger.d(KEY, "skuList:" + skuDetails);
            if (response == 0) {
                ArrayList<String> responseList
                        = skuDetails.getStringArrayList("DETAILS_LIST");

                for (String thisResponse : responseList) {
                    JSONObject object = new JSONObject(thisResponse);
                    Logger.d(KEY, "skuList:" + thisResponse);
                    String sku = object.getString("productId");
                    if (!"korean".equals(sku) && !"english".equals(sku)) {
                        String price = object.getString("price");
                        Media media = mediaMap.get(sku);
                        if (media != null) media.price = price;
                    }
                }
            }
        } catch (RemoteException e) {
        } catch (JSONException e) {
        }
        Logger.d(KEY, "skuList end");
    }

    private void readOwnedItem() {
        if (mService == null) return;
        Logger.d(KEY, "ownedItems start");
        try {
            Bundle ownedItems = mService.getPurchases(3, mActivity.getPackageName(), "inapp", null);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                ArrayList<String> signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
                String continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                for (int i = 0; i < purchaseDataList.size(); ++i) {
//                    String purchaseData = purchaseDataList.get(i);
//                    JSONObject jData = new JSONObject(purchaseData);
//                    String purchaseToken = jData.getString("purchaseToken");
//                    mService.consumePurchase(3, mActivity.getPackageName(), purchaseToken);

                    String sku = ownedSkus.get(i);

                    Media media = mediaMap.get(sku);
                    if (media != null) media.isPurchased = true;
                    Logger.d(KEY, "sku:" + sku + ", continuationToken:" + continuationToken);

                }

            }
        } catch (RemoteException e) {
        }
        Logger.d(KEY, "ownedItems end");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeMessages(0);
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
        lbm.unregisterReceiver(mLocalBroadcastReceiver);

        storeList();
        if (mService != null) mActivity.unbindService(this);
    }

    private void restoreList() {
        if (mActivity == null) return;
        SharedPreferences preferences = mActivity.getPreferences(Context.MODE_PRIVATE);
        String listString = preferences.getString(MEDIA_LIST_JSON, null);
        Logger.d(KEY, "preferences-listString:" + listString);
        if (listString == null) {
            byte[] data = readAssetFile(MEDIA_LIST_JSON);
            listString = new String(data);
            Logger.d(KEY, "readAssetFile-listString:" + listString);
        }

        mediaMap = parseMediaList(listString);
        mList = new ArrayList<Media>(mediaMap.values());
    }

    private void storeList() {
        if (mList.size() > 0) {
            Gson gson = new Gson();
            String listString = gson.toJson(mList);

            SharedPreferences preferences = mActivity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString(MEDIA_LIST_JSON, listString);
            edit.commit();
        }
    }

    private byte[] readAssetFile(String assetName) {
        AssetManager assets = getResources().getAssets();
        byte[] data = null;
        try {
            InputStream is = assets.open(assetName);
            int available = is.available();
            data = new byte[available];
            int readByte = is.read(data);
            is.close();
            if (readByte > 0) return data;
        } catch (IOException ignored) {
        }
        return data;
    }

    private LinkedHashMap<String, Media> parseMediaList(String listString) {
        LinkedHashMap<String, Media> list = new LinkedHashMap<String, Media>();
        try {
            JSONArray jItems = new JSONArray(listString);
            int length = jItems.length();
            for (int i = 0; i < length; i++) {
                Gson gson = new Gson();
                JSONObject jItem = jItems.getJSONObject(i);
                Media media = gson.fromJson(jItem.toString(), Media.class);

                File file = new File(App.getStoredPath(media.url));
                if (media.fileSize > 0 && file.exists()) {
                    media.downloadSize = file.length();
                } else {
                    media.downloadSize = 0;
                }
                media.price = "free";
                if (media.sku != null) list.put(media.sku, media);
            }

        } catch (JSONException ignored) {
        }
        return list;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.d("tag","purchase Start");
        int index = position - mListView.getHeaderViewsCount();
        Media media = mList.get(index);
        if ("free".equals(media.price)) {
            media.isPurchased = true;
            mAdapter.notifyDataSetChanged();
            return;
        }

        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, mActivity.getPackageName(), media.sku, "inapp", "payload." + media.sku);
            if (buyIntentBundle == null) {
                media.isPurchased = true;
                mAdapter.notifyDataSetChanged();
                return;
            }
            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

            mActivity.startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
        } catch (IntentSender.SendIntentException e) {
        } catch (RemoteException e) {
        }


//        media.isPurchased = true;
//        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d(KEY, "onActivityResult");
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            /*if (resultCode == Activity.RESULT_OK) {
                try {
                    JSONObject jData = new JSONObject(purchaseData);
                    String sku = jData.getString("productId");
                    Media media = mediaMap.get(sku);
                    if (media != null) {
                        media.isPurchased = true;
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    Logger.d(KEY, "Failed to parse purchase data.");
                }
            }*/
            switch (responseCode){
                case 0:case 7:
                    try {
                        JSONObject jData = new JSONObject(purchaseData);
                        String sku = jData.getString("productId");
                        Media media = mediaMap.get(sku);
                        if (media != null) {
                            media.isPurchased = true;
                            mAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        Logger.d(KEY, "Failed to parse purchase data. [ 0, 7 ]");
                    }
                    break;
                default:
                    Logger.d(KEY, "Failed to parse purchase data. [ default ]");
            }
        }


    }

    @Override
    public void onForeground() {
        restoreList();
//        readOwnedItem();
//        readSkuList();
//        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    public class MediaAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Media getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            TextView nameView;
            TextView priceView;
            ImageButton buttonView;
            HoloCircularProgressBar progressView;

            if (view == null) {
                LayoutInflater layoutInflater = mActivity.getLayoutInflater();
                view = layoutInflater.inflate(R.layout.row_media, viewGroup, false);
                assert view != null;
                nameView = (TextView) view.findViewById(R.id.name);
                priceView = (TextView) view.findViewById(R.id.price);
                buttonView = (ImageButton) view.findViewById(R.id.button);
                progressView = (HoloCircularProgressBar) view.findViewById(R.id.progress);
                view.setTag(R.id.name, nameView);
                view.setTag(R.id.price, priceView);
                view.setTag(R.id.button, buttonView);
                view.setTag(R.id.progress, progressView);
            } else {
                nameView = (TextView) view.getTag(R.id.name);
                priceView = (TextView) view.getTag(R.id.price);
                buttonView = (ImageButton) view.getTag(R.id.button);
                progressView = (HoloCircularProgressBar) view.getTag(R.id.progress);
            }

            assert nameView != null;
            assert priceView != null;
            assert buttonView != null;
            assert progressView != null;

            Media media = getItem(position);
            nameView.setText(media.language);
            if (media.isPurchased) {
                priceView.setVisibility(View.GONE);

                buttonView.setVisibility(View.VISIBLE);
                DownloadService downloadService = DownloadService.get();
                if (media.fileSize > 0 && media.fileSize == media.downloadSize) {
                    buttonView.setImageResource(R.drawable.ic_play);
                    progressView.setVisibility(View.GONE);

                    buttonView.setTag(media.url);
                    buttonView.setOnClickListener(mPlayClickListener);
                } else if (downloadService != null &&
                        (media.url.equals(downloadService.getCurrentMediaKey()) || downloadService.contains(media.url))) {
                    buttonView.setImageResource(R.drawable.ic_stop);
                    progressView.setVisibility(View.VISIBLE);

                    progressView.setTag(media);
                    buttonView.setTag(media.url);
                    buttonView.setOnClickListener(mCancelClickListener);
                } else {
                    buttonView.setImageResource(R.drawable.ic_download);
                    progressView.setVisibility(View.GONE);

                    buttonView.setTag(media.url);
                    buttonView.setOnClickListener(mDownloadClickListener);

                }
            } else {
                priceView.setText(media.price);
                priceView.setVisibility(View.VISIBLE);

                buttonView.setVisibility(View.INVISIBLE);
                progressView.setVisibility(View.GONE);
            }

            return view;
        }
    }

    public static class Media implements Parcelable {
        public String language;
        public String url;
        public String price;
        public boolean isPurchased = false;
        public long fileSize = 0;
        public long downloadSize = 0;
        public String sku;

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(language);
            out.writeString(url);
            out.writeString(price);
            out.writeInt(isPurchased ? 1 : 0);
            out.writeLong(fileSize);
            out.writeLong(downloadSize);
        }

        public static final Parcelable.Creator<Media> CREATOR
                = new Parcelable.Creator<Media>() {
            public Media createFromParcel(Parcel in) {
                return new Media(in);
            }

            public Media[] newArray(int size) {
                return new Media[size];
            }
        };

        private Media(Parcel in) {
            language = in.readString();
            url = in.readString();
            price = in.readString();
            isPurchased = in.readInt() == 1;
            fileSize = in.readLong();
            downloadSize = in.readLong();
        }

        public Media() {
        }

    }

}
