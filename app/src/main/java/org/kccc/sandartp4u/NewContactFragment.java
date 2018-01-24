package org.kccc.sandartp4u;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class NewContactFragment extends Fragment {
    public static final String CLASS_NAME = NewContactFragment.class.getName();

    private AppCompatActivity mActivity;
    private EditText mNameView;
    private EditText mEmailView;
    private EditText mMobileView;
    private EditText mNoteView;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        ActionBar actionBar = mActivity.getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP |
                        ActionBar.DISPLAY_SHOW_TITLE |
                        ActionBar.DISPLAY_SHOW_HOME
        );
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.action_new_contact);

        View v = inflater.inflate(R.layout.fragment_new_contact, container, false);
        mNameView = (EditText) v.findViewById(R.id.name);
        mEmailView = (EditText) v.findViewById(R.id.email);
        mMobileView = (EditText) v.findViewById(R.id.mobile);
        mNoteView = (EditText) v.findViewById(R.id.note);
        mNameView.requestFocus();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.new_contact, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            mActivity.finish();
        } else if (itemId == R.id.action_save) {
            ContactFragment.Contact contact = new ContactFragment.Contact();
            contact.name = mNameView.getText().toString();
            contact.email = mEmailView.getText().toString();
            String mobile = mMobileView.getText().toString();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mobile.length(); i++) {
                char c = mobile.charAt(i);
                if ('0' <= c && c <= '9') {
                    sb.append(c);
                }
            }
            contact.mobileNumber = sb.toString();
            contact.note = mNoteView.getText().toString();
            contact.date = System.currentTimeMillis();

            if (contact.name.length() > 0) {
                Intent data = new Intent();
                data.putExtra(contact.getClass().getName(), contact);
                mActivity.setResult(Activity.RESULT_OK, data);
                mActivity.finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
