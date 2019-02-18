package com.jonathan.taxidispatcher.ui.driver_main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerSettingFragment;
import com.jonathan.taxidispatcher.ui.start_main.StartActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class DriverMainActivity extends AppCompatActivity implements HasSupportFragmentInjector, Injectable {
    static FragmentManager manager;
    private DrawerLayout mDrawLayout;

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);
        manager = getSupportFragmentManager();
        DriverScanQRFragment fragment = DriverScanQRFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.driverMainContainer, fragment)
                .commit();
        initDrawer();
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    private static void changeFragment(Fragment fragment, boolean init) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.driverMainContainer, fragment);
        if(!init) transaction.addToBackStack(null);
        manager.popBackStack();
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }


    private void initDrawer() {
        mDrawLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.inflateHeaderView(R.layout.navigation_header);
        TextView usernameText  = headerView.findViewById(R.id.usernameText);
        TextView emailText = headerView.findViewById(R.id.emailText);
        TextView phoneText = headerView.findViewById(R.id.phoneText);
        ImageView profileImage = headerView.findViewById(R.id.profileImageView);
        usernameText.setText(Session.getUsername(this));
        emailText.setText(Session.getEmail(this));
        phoneText.setText(Session.getPhoneNumber(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawLayout.closeDrawers();
                switch(menuItem.getItemId()) {
                    case R.id.settingButton:
                        changeFragment(PassengerSettingFragment.newInstance(), false);
                        break;
                    case R.id.orderHistoryButton:
                        break;
                    case R.id.manageTaxiButton:
                        changeFragment(DriverManageTaxiFragment.newInstance(), false);
                        break;
                    case R.id.logoutButton:
                        logout();
                        break;
                }
                return false;
            }
        });
    }


    private void logout() {
        Session.logout(this);
        Intent intent = new Intent(DriverMainActivity.this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
