package cn.dmandp.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.TrafficStats;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import java.util.Locale;

import cn.dmandp.tt.ActivityCollector;
import cn.dmandp.tt.R;


public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    ListPreference listPreference;
    SwitchPreference switchPreference;
    RingtonePreference ringtonePreference;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_setting);
        ringtonePreference= (RingtonePreference) findPreference("notification_ring");
        listPreference= (ListPreference) findPreference("language");
        if(listPreference.getValue().equalsIgnoreCase("0")){
            listPreference.setSummary(R.string.chinese);
        }else {
            listPreference.setSummary(R.string.english);
        }
        listPreference.setOnPreferenceChangeListener(this);
        switchPreference= (SwitchPreference) findPreference("notification");
        if(switchPreference.isChecked()){
            ringtonePreference.setEnabled(true);
        }else {
            ringtonePreference.setEnabled(false);
        }
        switchPreference.setOnPreferenceChangeListener(this);
        findPreference("clear_cache").setOnPreferenceClickListener(this);
        findPreference("flow_count").setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()){
            case "language":
                Resources resources=getResources();
                Configuration configuration=resources.getConfiguration();
                String language= (String) newValue;
                Locale locale;
                if(language.equalsIgnoreCase("0")){
                    locale=Locale.CHINESE;
                }else{
                    locale=Locale.US;
                }
                configuration.locale=locale;
                DisplayMetrics displayMetrics=resources.getDisplayMetrics();
                resources.updateConfiguration(configuration,displayMetrics);
                ActivityCollector.finishAll();
                Intent mainIntent=new Intent("cn.dmandp.tt.action.MAINACTIVITY");
                startActivity(mainIntent);
                break;
            case "notification":
                if((Boolean) newValue){
                    findPreference("notification_ring").setEnabled(true);
                }else{
                    findPreference("notification_ring").setEnabled(false);
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()){
            case "clear_cache":
                AlertDialog.Builder clearDialogBuilder=new AlertDialog.Builder(getActivity());
                clearDialogBuilder.setMessage(R.string.clear_cache_message).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().getSharedPreferences("message", Context.MODE_PRIVATE).edit().clear().apply();
                        getActivity().deleteDatabase("ttimDatabase");
                        Intent mainIntent=new Intent("cn.dmandp.tt.action.MAINACTIVITY");
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainIntent);
                        System.exit(0);
                    }
                }).setNegativeButton(R.string.cancel,null).show();
                break;
            case "flow_count":
                String dialogMessage=getString(R.string.have_use)+TrafficStats.getUidTxBytes(getActivity().getApplicationInfo().uid)/1024+"KB "+getString(R.string.up_flow)+TrafficStats.getUidRxBytes(getActivity().getApplicationInfo().uid)/1024+"KB "+getString(R.string.down_flow);
                AlertDialog.Builder flowDialogBuilder=new AlertDialog.Builder(getActivity());
                flowDialogBuilder.setMessage(dialogMessage).setNegativeButton(R.string.cancel,null).show();
                break;
        }
        return false;
    }
}
