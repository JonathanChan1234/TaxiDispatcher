package com.jonathan.taxidispatcher.ui.start_main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.driver_main.DriverMainActivity;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class StartActivity extends AppCompatActivity implements HasSupportFragmentInjector, Injectable {
    static FragmentManager manager;

    @Inject
    DispatchingAndroidInjector<Fragment> fragmentDispatchingAndroidInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        manager = getSupportFragmentManager();
        manager.beginTransaction()
                .add(R.id.container, LaunchFragment.newInstance())
                .commit();
        checkLogInState();
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return fragmentDispatchingAndroidInjector;
    }

    private static void changeFragment(Fragment fragment, boolean init) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        if (!init) transaction.addToBackStack(null);
        manager.popBackStack();
        transaction.commit();
    }

    public static void switchToCreateAccountFragment() {
        changeFragment(CreateAccountFragment.newInstance(), false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkLogInState() {
        if (Session.checkLogInState(this)) {
            if (!Session.checkIdentity(this).equals("")) {
                if (Session.checkIdentity(this).equals("user")) {
                    Intent intent = new Intent(StartActivity.this, PassengerMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(StartActivity.this, DriverMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        } else {
            changeFragment(MainFragment.newInstance(), true);
        }
    }
}
