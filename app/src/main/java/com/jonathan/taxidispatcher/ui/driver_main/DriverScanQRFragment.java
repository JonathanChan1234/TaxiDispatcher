package com.jonathan.taxidispatcher.ui.driver_main;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.databinding.FragmentDriverScanQrBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.DriverMainViewModelFactory;
import com.jonathan.taxidispatcher.room.TransactionDao;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.driver_transaction.DriverTransactionActivity;
import com.jonathan.taxidispatcher.utils.PhotoUtils;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import javax.inject.Inject;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

@RuntimePermissions
public class DriverScanQRFragment extends Fragment implements Injectable {
    FragmentDriverScanQrBinding binding;
    DriverMainViewModel viewModel;

    @Inject
    DriverMainViewModelFactory factory;
    @Inject
    TransactionDao dao;
    LayoutInflater inflater;

    public DriverScanQRFragment() {
        // Required empty public constructor
    }

    public static DriverScanQRFragment newInstance() {
        return new DriverScanQRFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDriverScanQrBinding.inflate(inflater, container, false);
        binding.createTaxiAccountButton.setOnClickListener(view -> {
            createNewAccount();
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("Taxi LogIn");
        viewModel = ViewModelProviders.of(getActivity(), factory).get(DriverMainViewModel.class);
        binding.setViewModel(viewModel);
        DriverScanQRFragmentPermissionsDispatcher.initUIWithPermissionCheck(this);
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION})
    public void initUI() {
        binding.scanQRcodeButton.setOnClickListener(view -> {
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            integrator.setPrompt("Scan the Label");
            integrator.setCameraId(0);
            integrator.setOrientationLocked(true);
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(true);
            integrator.initiateScan();
        });
    }

    private void signInTaxi(String token) {
        if (token.length() < 34) {
            Toast.makeText(getContext(), "Invalid token", Toast.LENGTH_SHORT).show();
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            viewModel.taxiSignIn(token.substring(32, token.length()), token.substring(0, 32), Session.getUserId(getContext()))
                    .observe(this, response -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            if (response.body.success == 1) {
                                try {
                                    Session.saveTaxiId(getContext(), Integer.parseInt(response.body.message));
                                    Session.saveTaxiPlateNumber(getContext(), response.body.taxi.platenumber);
                                    Intent intent = new Intent(getActivity(), DriverTransactionActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } catch (IllegalArgumentException e) {
                                    Timber.e(e);
                                }
                            } else {
                                Toast.makeText(getContext(), response.body.message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), response.errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                signInTaxi(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void createNewAccount() {
        inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_taxi_registration, null);
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Taxi Account Registration")
                .setView(dialogView)
                .setPositiveButton("Confirm", ((dialogInterface, i) -> {
                    EditText plateNumber = dialogView.findViewById(R.id.plateNumberText);
                    EditText password = dialogView.findViewById(R.id.passwordText);
                    if (!TextUtils.isEmpty(plateNumber.getText().toString()) &&
                            !TextUtils.isEmpty(password.getText().toString())) {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        viewModel.registerNewTaxi(Session.getUserId(getContext()),
                                plateNumber.getText().toString(),
                                password.getText().toString())
                                .observe(this, response -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    if (response.isSuccessful()) {
                                        if (response.body.success == 1) {
                                            showQRCodeDialog(response.body.message, plateNumber.getText().toString());
                                        } else {
                                            Toast.makeText(getContext(), response.body.message, Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), response.errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "Don't leave empty", Toast.LENGTH_SHORT).show();
                    }
                })).show();
    }

    private void showQRCodeDialog(String accessToken, String plateNumber) {
        View dialogView = inflater.inflate(R.layout.dialog_qrcode, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Registration completed")
                .setView(dialogView)
                .setPositiveButton("OK", ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }));
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(accessToken + plateNumber, BarcodeFormat.QR_CODE, 400, 400);
            ImageView imageView = dialogView.findViewById(R.id.qrCodeView);
            imageView.setImageBitmap(bitmap);
            PhotoUtils.storeQRCodeImage(bitmap, plateNumber);
            alertDialog.show();
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
