package com.aprivate.sergio_l_marques.rdscng;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentActivity;

public class ViewAndUpdatePreferencesActivity extends FragmentActivity {

    private static final String HADDRESS = "haddress";
    private static final String TCPORT = "tcport";
    private static final String DSCPASS = "dscpass";
    private static final String DSCCODE = "dscUserCode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.menu.user_prefs_fragment);

    }

    // Fragment that displays the username preference
    public static class UserPreferenceFragment extends PreferenceFragment {

        protected static final String TAG = "UserPrefsFragment";
        private SharedPreferences.OnSharedPreferenceChangeListener mListener;
        private Preference mHostAddressPreference;
        private Preference mTcpPortPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.user_prefs);

            // Get the hostname Preference
            mHostAddressPreference = getPreferenceManager()
                    .findPreference(HADDRESS);
            mTcpPortPreference = getPreferenceManager()
                    .findPreference(TCPORT);
//			mDscPassPreference = (Preference) getPreferenceManager()
//					.findPreference(DSCPASS);

            // Attach a listener to update summary when the host address changes
            mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences, String key) {
                    mHostAddressPreference.setSummary(sharedPreferences.getString(
                            HADDRESS, "None Set"));
                    mTcpPortPreference.setSummary(sharedPreferences.getString(
                            TCPORT, "None Set"));
                    //mDscPassPreference.setSummary(sharedPreferences.getString(
                    //		DSCPASS, "None Set"));
                }
            };

            // Get SharedPreferences object managed by the PreferenceManager for
            // this Fragment
            SharedPreferences prefs = getPreferenceManager()
                    .getSharedPreferences();

            // Register a listener on the SharedPreferences object
            prefs.registerOnSharedPreferenceChangeListener(mListener);

            // Invoke callback manually to display the current host address
            mListener.onSharedPreferenceChanged(prefs, HADDRESS);
            mListener.onSharedPreferenceChanged(prefs, TCPORT);
            mListener.onSharedPreferenceChanged(prefs, DSCPASS);
            mListener.onSharedPreferenceChanged(prefs, DSCCODE);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        //finish();
    }

}
