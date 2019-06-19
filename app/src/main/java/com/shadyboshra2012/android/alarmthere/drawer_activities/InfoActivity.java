package com.shadyboshra2012.android.alarmthere.drawer_activities;

import android.content.Context;
import android.content.Intent;
import android.icu.text.IDNA;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.shadyboshra2012.android.alarmthere.AppPreferences;
import com.shadyboshra2012.android.alarmthere.MainActivity;
import com.shadyboshra2012.android.alarmthere.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InfoActivity extends AppCompatActivity {

    @BindView(R.id.facebook_img)
    ImageView facebookImg;
    @BindView(R.id.twitter_img)
    ImageView twitterImg;
    @BindView(R.id.linked_in_img)
    ImageView linkedInImg;
    @BindView(R.id.mail_img)
    ImageView mailImg;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ButterKnife.bind(this);

        setDrawerLayout();
        setToolbar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDrawerLayout() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here
                        switch (menuItem.getItemId()) {
                            case R.id.action_alarms:
                                finish();
                                break;
                            case R.id.action_show_on_maps:
                                Intent showAlarmsMap = new Intent(getApplicationContext(), ShowAlarmsMapActivity.class);
                                startActivity(showAlarmsMap);
                                finish();
                                break;
                            case R.id.action_settings:
                                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                                startActivity(settings);
                                finish();
                                break;
                            case R.id.action_rate_app:
                                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                                Intent myAppLinkToMarker = new Intent(Intent.ACTION_VIEW, uri);
                                myAppLinkToMarker.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                try {
                                    startActivity(myAppLinkToMarker);
                                } catch (Exception e) {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                                }
                                break;
                            case R.id.action_send_feedback_app:
                                openFeedback(InfoActivity.this);
                                break;
                            case R.id.action_info:
                                //Do nothing
                                break;
                        }

                        return true;
                    }
                });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    public static void openFeedback(Context paramContext) {
        Intent localIntent = new Intent(Intent.ACTION_SEND);
        localIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ShadyBoshra2011@gmail.com"});
        localIntent.putExtra(Intent.EXTRA_CC, "");
        String str = null;
        try {
            str = paramContext.getPackageManager().getPackageInfo(paramContext.getPackageName(), 0).versionName;
            localIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for AlarmThere");
            localIntent.putExtra(Intent.EXTRA_TEXT, "\n\n----------------------------------\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + str + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER);
            localIntent.setType("message/rfc822");
            paramContext.startActivity(Intent.createChooser(localIntent, "Choose an Email client :"));
        } catch (Exception e) {
           //Log.d("OpenFeedback", e.getMessage());
        }
    }

    @OnClick({R.id.facebook_img, R.id.twitter_img, R.id.linked_in_img, R.id.mail_img})
    public void onViewClicked(View view) {
        AppPreferences.getInstance(getApplicationContext()).showAd();

        switch (view.getId()) {
            case R.id.facebook_img:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://fb.com/ShadyBoshra2012")));
                break;
            case R.id.twitter_img:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://twitter.com/ShadyBoshra2012")));
                break;
            case R.id.linked_in_img:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://linkedin.com/in/ShadyBoshra2012")));
                break;
            case R.id.mail_img:
                Intent localIntent = new Intent(Intent.ACTION_SEND);
                localIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ShadyBoshra2011@gmail.com"});
                localIntent.putExtra(Intent.EXTRA_CC, "");
                String str = null;
                try {
                    localIntent.putExtra(Intent.EXTRA_SUBJECT, "Message to Shady Boshra");
                    localIntent.setType("message/rfc822");
                    startActivity(Intent.createChooser(localIntent, "Choose an Email client :"));
                } catch (Exception e) {
                   //Log.d("OpenFeedback", e.getMessage());
                }
                break;
        }
    }
}
