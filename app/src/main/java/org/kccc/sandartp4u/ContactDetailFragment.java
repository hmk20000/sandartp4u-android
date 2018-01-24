package org.kccc.sandartp4u;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ContactDetailFragment extends Fragment {
    public static final String CLASS_NAME = ContactDetailFragment.class.getName();

    private AppCompatActivity mActivity;
    private TextView mNameView;
    private TextView mEmailView;
    private TextView mMobileView;
    private TextView mNoteView;
    private TextView mDateView;


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
        actionBar.setTitle(R.string.action_bar_title_contact);

        View v = inflater.inflate(R.layout.fragment_contact_detail, container, false);
        mNameView = (TextView) v.findViewById(R.id.name);
        mEmailView = (TextView) v.findViewById(R.id.email);
        mMobileView = (TextView) v.findViewById(R.id.mobile);
        mNoteView = (TextView) v.findViewById(R.id.note);
        mDateView = (TextView) v.findViewById(R.id.date);

        Bundle arguments = getArguments();
        final ContactFragment.Contact contact = arguments.getParcelable(ContactFragment.Contact.class.getName());
        mNameView.setText(contact.name);
        mEmailView.setText(contact.email);
        mEmailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"+contact.email)));
            }
        });
        StringBuilder sb = new StringBuilder();
        int length = contact.mobileNumber.length();
        if (length > 10) {
            sb.append(contact.mobileNumber.substring(0, 3));
            sb.append("-");
            sb.append(contact.mobileNumber.substring(3, length - 4));
            sb.append("-");
            sb.append(contact.mobileNumber.substring(length - 4, length));
        } else if (length > 7) {
            sb.append(contact.mobileNumber.substring(0, 4));
            sb.append("-");
            sb.append(contact.mobileNumber.substring(4, length));
        } else {
            sb.append(contact.mobileNumber);
        }

        mMobileView.setText(sb.toString());
        mMobileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+contact.mobileNumber)));
            }
        });
        mNoteView.setText(contact.note);
        //수정
//        mDateView.setText(new DateFormat().format("yyyy-MM-dd "+DateFormat.HOUR_OF_DAY+DateFormat.HOUR_OF_DAY+":mm:ss", contact.date));
        mDateView.setText("test");
        return v;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            mActivity.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
