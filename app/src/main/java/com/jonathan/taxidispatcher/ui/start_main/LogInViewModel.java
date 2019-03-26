package com.jonathan.taxidispatcher.ui.start_main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.jonathan.taxidispatcher.api.ApiResponse;
import com.jonathan.taxidispatcher.data.UserRepository;
import com.jonathan.taxidispatcher.data.model.AccountDriverResponse;
import com.jonathan.taxidispatcher.data.model.AccountUserResponse;

public class LogInViewModel extends ViewModel {
    private LiveData<ApiResponse<AccountUserResponse>> userResponse = new MutableLiveData<>();
    private LiveData<ApiResponse<AccountDriverResponse>> driverResponse = new MutableLiveData<>();
    private UserRepository userRepository;

    public LogInViewModel(UserRepository userRepository) {
        super();
        this.userRepository = userRepository;
    }

    public LiveData<ApiResponse<AccountUserResponse>> passengerLogIn(String phoneNumber, String password, String token) {
        userResponse = userRepository.passengerLogIn(phoneNumber, password, token);
        return userResponse;
    }

    public LiveData<ApiResponse<AccountDriverResponse>> driverLogIn(String phoneNumber, String password, String token) {
        driverResponse = userRepository.driverLogIn(phoneNumber, password, token);
        return driverResponse;
    }

    public LiveData<ApiResponse<AccountUserResponse>> passengerRegister(String username,
                                                                        String password,
                                                                        String phoneNumber,
                                                                        String email,
                                                                        String img,
                                                                        String token) {
        userResponse = userRepository.passengerRegister(username, password, phoneNumber, email, img, token);
        return userResponse;
    }

    public LiveData<ApiResponse<AccountDriverResponse>> driverRegister(String username, String phonenumber, String password, String email, String img, String token) {
        driverResponse = userRepository.driverRegister(username, phonenumber, password, email, img, token);
        return driverResponse;
    }
}