package com.jonathan.taxidispatcher.ui.start_main;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.model.AccountDriverResponse;
import com.jonathan.taxidispatcher.data.model.AccountUserResponse;
import com.jonathan.taxidispatcher.databinding.FragmentRegisterBinding;
import com.jonathan.taxidispatcher.di.Injectable;
import com.jonathan.taxidispatcher.factory.LogInViewModelFactory;
import com.jonathan.taxidispatcher.session.Session;
import com.jonathan.taxidispatcher.ui.driver_main.DriverMainActivity;
import com.jonathan.taxidispatcher.ui.passenger_main.PassengerMainActivity;
import com.jonathan.taxidispatcher.utils.PhotoUtils;
import com.jonathan.taxidispatcher.utils.Utils;

import java.io.IOException;

import javax.inject.Inject;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

@RuntimePermissions
public class CreateAccountFragment extends Fragment implements Injectable {
    public static final int REQUEST_CAMERA = 100;
    public static final int SELECT_FILE = 101;
    FragmentRegisterBinding binding;

    Bitmap thumbnail;

    @Inject
    LogInViewModelFactory factory;

    LogInViewModel viewModel;

    public CreateAccountFragment() {
        // Required empty public constructor
    }

    public static CreateAccountFragment newInstance() {
        return new CreateAccountFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, factory).get(LogInViewModel.class);

        binding.imageView.setOnClickListener(view -> {
            selectImg();
        });
        binding.registerButton.setOnClickListener(view -> {
            registerAccount();
        });
    }


    public void selectImg() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Take Photo")) {
                    CreateAccountFragmentPermissionsDispatcher.cameraIntentWithPermissionCheck(CreateAccountFragment.this);
                } else if (items[i].equals("Choose from Library")) {
                    CreateAccountFragmentPermissionsDispatcher.galleryIntentWithPermissionCheck(CreateAccountFragment.this);
                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select file"), SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    thumbnail = (Bitmap) data.getExtras().get("data");
                    binding.imageView.setImageBitmap(thumbnail);
                    break;
                case SELECT_FILE:
                    try {
                        thumbnail = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
                        binding.imageView.setImageBitmap(thumbnail);
                    } catch (IOException e) {
                        Timber.i(e);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CreateAccountFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void registerAccount() {
        String username = binding.usernameText.getText().toString();
        String password = binding.passwordText.getText().toString();
        String phoneNumber = binding.phoneText.getText().toString();
        String email = binding.emailText.getText().toString();
        String img;
        if (thumbnail != null) {
            img = PhotoUtils.getStringImage(thumbnail);
        } else {
            img = "";
        }
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(getContext(), "Google Service Not available", Toast.LENGTH_SHORT).show();
                } else {
                    if (task.getResult() != null) {
                        String token = task.getResult().getToken();
                        if (binding.passengerButtonInSignIn.isChecked()) {

                            viewModel.passengerRegister(username, password, phoneNumber, email, img, token)
                                    .observe(CreateAccountFragment.this, new Observer<ApiResponse<AccountUserResponse>>() {
                                        @Override
                                        public void onChanged(@Nullable ApiResponse<AccountUserResponse> response) {
                                            if (response != null) {
                                                if (response.isSuccessful()) {
                                                    if (response.body.success == 1) {
                                                        Session.logIn(getContext(),
                                                                response.body.user.id,
                                                                response.body.user.phonenumber,
                                                                response.body.user.username,
                                                                response.body.user.email,
                                                                "user",
                                                                response.body.access_token
                                                        );
                                                        Intent intent = new Intent(getActivity(), PassengerMainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    } else {    //Login unsuccessfully
                                                        Toast.makeText(getContext(),
                                                                Utils.stringListToString(response.body.message),
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toast.makeText(getContext(), "Network connection issue", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });
                        } else {
                            viewModel.driverRegister(username, password, phoneNumber, email, img, token)
                                    .observe(CreateAccountFragment.this, new Observer<ApiResponse<AccountDriverResponse>>() {
                                        @Override
                                        public void onChanged(@Nullable ApiResponse<AccountDriverResponse> response) {
                                            if (response != null) {
                                                if (response.isSuccessful()) {
                                                    if (response.body.success == 1) {
                                                        Session.logIn(getContext(),
                                                                response.body.user.id,
                                                                response.body.user.phonenumber,
                                                                response.body.user.username,
                                                                response.body.user.email,
                                                                "driver",
                                                                response.body.access_token
                                                        );
                                                        Intent intent = new Intent(getActivity(), DriverMainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                } else {    //Login unsuccessfully
                                                    Toast.makeText(getContext(),
                                                            Utils.stringListToString(response.body.message),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(getContext(), "Network connection issue", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    } else {

                    }
                }
            }
        });

    }
}
