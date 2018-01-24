package org.kccc.sandartp4u;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by whylee on 2014. 5. 26..
 */
public class ContactFragment extends ListFragment implements MainActivity.ForegroundCallback{
    public static final String CONTACT_LIST_JSON = "contact_list.json";
    public static final int REQUEST_CODE = 1;

    private List<Contact> mList;
    private AppCompatActivity mActivity;
    private ContactAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restoreList();

        mAdapter = new ContactAdapter();
        setListAdapter(mAdapter);
//        setEmptyText(getString(R.string.contact_empty));
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        storeList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.contact, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new_contact) {
            Intent intent = new Intent(mActivity, DetailActivity.class);
            intent.putExtra(DetailActivity.FRAGMENT_NAME, NewContactFragment.CLASS_NAME);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d("ContactFragment","onActivityResult "+requestCode+" "+(resultCode == Activity.RESULT_OK));
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Contact contact = data.getParcelableExtra(Contact.class.getName());
            if(contact != null) {
                Logger.d("ContactFragment","onActivityResult add");
                mList.add(contact);
                mAdapter.notifyDataSetChanged();
                storeList();
            }
        }

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Contact contact = mList.get(position);

        Intent intent = new Intent(mActivity, DetailActivity.class);
        intent.putExtra(DetailActivity.FRAGMENT_NAME, ContactDetailFragment.CLASS_NAME);
        Bundle value = new Bundle();
        value.putParcelable(contact.getClass().getName(), contact);
        intent.putExtra(DetailActivity.ARGUMENTS, value);
        startActivity(intent);
    }

    private List<Contact> parseMediaList(String listString) {
        Gson gson = new Gson();
        Type arrayListType = new TypeToken<ArrayList<Contact>>() {
        }.getType();
        return gson.fromJson(listString, arrayListType);
    }

    private void restoreList() {
        Logger.d("ContactFragment", "restoreList");
        if(mActivity == null) return;
        SharedPreferences preferences = mActivity.getSharedPreferences(ContactFragment.class.getName(), Context.MODE_PRIVATE);
        String listString = preferences.getString(CONTACT_LIST_JSON, null);
        Logger.d("ContactFragment", "listString:"+listString);
        if (listString != null) {
            mList = parseMediaList(listString);
        } else {
            mList = new ArrayList<Contact>();
        }
    }

    private void storeList() {
        SharedPreferences preferences = mActivity.getSharedPreferences(ContactFragment.class.getName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();

        if (mList.size() > 0) {
            Gson gson = new Gson();
            String listString = gson.toJson(mList);
            edit.putString(CONTACT_LIST_JSON, listString);
        } else {
            edit.remove(CONTACT_LIST_JSON);
        }
        edit.commit();
    }

    @Override
    public void onForeground() {
        restoreList();
        if(mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    public class ContactAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Contact getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            TextView nameView;
            TextView dateView;
            if (view == null) {
                LayoutInflater layoutInflater = mActivity.getLayoutInflater();
                view = layoutInflater.inflate(R.layout.row_contact, viewGroup, false);
                assert view != null;

                nameView = (TextView) view.findViewById(R.id.name);
                dateView = (TextView) view.findViewById(R.id.date);
                view.setTag(R.id.name, nameView);
                view.setTag(R.id.date, dateView);
            } else {
                nameView = (TextView) view.getTag(R.id.name);
                dateView = (TextView) view.getTag(R.id.date);
            }

            assert nameView != null;
            assert dateView != null;

            Contact contact = getItem(position);
            nameView.setText(contact.name);
            dateView.setText(new DateFormat().format("yyyy-MM-dd", contact.date));

            return view;
        }
    }

    public static class Contact implements Parcelable {
        String name;
        String email;
        String mobileNumber;
        String note;
        long date;

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeString(name);
            out.writeString(email);
            out.writeString(mobileNumber);
            out.writeString(note);
            out.writeLong(date);
        }

        public static final Parcelable.Creator<Contact> CREATOR
                = new Parcelable.Creator<Contact>() {
            public Contact createFromParcel(Parcel in) {
                return new Contact(in);
            }

            public Contact[] newArray(int size) {
                return new Contact[size];
            }
        };

        private Contact(Parcel in) {
            name = in.readString();
            email = in.readString();
            mobileNumber = in.readString();
            note = in.readString();
            date = in.readLong();
        }

        public Contact() {
        }
    }

}
