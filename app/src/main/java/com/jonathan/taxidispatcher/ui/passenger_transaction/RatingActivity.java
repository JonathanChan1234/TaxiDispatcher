package com.jonathan.taxidispatcher.ui.passenger_transaction;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.api.APIInterface;
import com.jonathan.taxidispatcher.databinding.ActivityRatingBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RatingActivity extends AppCompatActivity implements Injectable {
    ActivityRatingBinding binding;

    @Inject
    APIInterface apiService;
    int driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rating);
        Intent intent = getIntent();
        String route = intent.getStringExtra("route");
        String driverUsername = intent.getStringExtra("driver");
        driverId = intent.getIntExtra("driverId", 0);
        binding.driverUsernameText.setText(driverUsername);
        binding.routeText.setText(route);
        binding.submitButton.setOnClickListener(view -> submitRating(driverId));
        binding.noNeedButton.setOnClickListener(view -> returnToMainActivity());
    }

    private void returnToMainActivity() {
        Intent intent = new Intent(RatingActivity.this, PassengerMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void submitRating(int id) {
        binding.loadingProgressBar.setVisibility(View.VISIBLE);
        int rating = binding.rating.getNumStars();
        apiService.rateDriver(driverId, Session.getCurrentTransactionID(this), rating)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        binding.loadingProgressBar.setVisibility(View.GONE);
                        if(response.isSuccessful()) {
                            Toast.makeText(RatingActivity.this, "Thanks", Toast.LENGTH_SHORT).show();
                            returnToMainActivity();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(RatingActivity.this, "Fail to Connect to the Internet", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
