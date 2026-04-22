package com.cumtenn.printerlib;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<String> currentIp = new MutableLiveData<>();

    public void setIp(String ip) {
        currentIp.setValue(ip);
    }

    public LiveData<String> getIp() {
        return currentIp;
    }

    public String getIpValue() {
        return currentIp.getValue();
    }
}
