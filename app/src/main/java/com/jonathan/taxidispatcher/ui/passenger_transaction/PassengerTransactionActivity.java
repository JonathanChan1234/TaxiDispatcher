package com.jonathan.taxidispatcher.ui.passenger_transaction;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.service.PassengerSocketService;

import org.greenrobot.eventbus.EventBus;

public class PassengerTransactionActivity extends AppCompatActivity {
    static FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_transaction);
        getActionBar().setTitle("Your Transaction");

        manager = getSupportFragmentManager();
        // Start the service
        Intent intent = new Intent(PassengerTransactionActivity.this, PassengerSocketService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public static void changeFragment(Fragment fragment, boolean init) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.passengerTransactionContainer, fragment);
        if(!init) transaction.addToBackStack(null);
        manager.popBackStack();
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
