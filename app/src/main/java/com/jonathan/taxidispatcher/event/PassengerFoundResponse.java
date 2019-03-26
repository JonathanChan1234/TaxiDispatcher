package com.jonathan.taxidispatcher.event;

import com.jonathan.taxidispatcher.data.model.Driver;
import com.jonathan.taxidispatcher.data.model.Transcation;


public class PassengerFoundResponse {
    private int response;
    private int transactionID;
    private int driverID;

    public int getResponse() {
        return response;
    }

    public void setResponse(int response) {
        this.response = response;
    }

    public int getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    public int getDriverID() {
        return driverID;
    }

    public void setDriverID(int driverID) {
        this.driverID = driverID;
    }

    public PassengerFoundResponse(int response, int transactionID, int driverID) {
        this.response = response;
        this.transactionID = transactionID;
        this.driverID = driverID;
    }
}
