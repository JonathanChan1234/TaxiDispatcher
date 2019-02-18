package com.jonathan.taxidispatcher.factory;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.jonathan.taxidispatcher.data.UserRepository;
import com.jonathan.taxidispatcher.ui.start_main.LogInViewModel;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LogInViewModelFactory implements ViewModelProvider.Factory {
    UserRepository userRepository;

    @Inject
    public LogInViewModelFactory(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(LogInViewModel.class)) {
            return (T) new LogInViewModel(userRepository);
        }
        throw new IllegalArgumentException("Unknown View Model class");
    }
}
