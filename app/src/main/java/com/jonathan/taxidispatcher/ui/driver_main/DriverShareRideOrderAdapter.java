package com.jonathan.taxidispatcher.ui.driver_main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.data.model.RideShareTransaction;

import java.util.List;

public class DriverShareRideOrderAdapter extends RecyclerView.Adapter<DriverShareRideOrderAdapter.ViewHolder> {
    private Context mContext;
    private List<RideShareTransaction> mData;
    private DriverManageListAdapter.OnItemClickedInterface onItemClickedInterface;

    public DriverShareRideOrderAdapter(Context context,
                                  List<RideShareTransaction> data) {
        this.mContext = context;
        this.mData = data;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView firstPassengerText;
        private TextView firstRouteText;
        private TextView secondPassengerText;
        private TextView secondRouteText;
        private TextView statusText;
        private TextView dateText;

        ViewHolder(View itemView) {
            super(itemView);
            firstPassengerText = itemView.findViewById(R.id.firstPassengerText);
            secondPassengerText = itemView.findViewById(R.id.secondPassengerText);
            firstRouteText = itemView.findViewById(R.id.firstRouteText);
            secondRouteText = itemView.findViewById(R.id.secondRouteText);
            statusText = itemView.findViewById(R.id.statusText);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }

    @NonNull
    @Override
    public DriverShareRideOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.driver_share_order_item_layout, parent, false);
        return new DriverShareRideOrderAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverShareRideOrderAdapter.ViewHolder viewHolder, int position) {
        RideShareTransaction transaction = mData.get(position);
        viewHolder.dateText.setText("Date: " + transaction.updatedAt.date.substring(0,19));
        if(transaction.first_transaction != null ) {
            String firstRoute = "From " + transaction.first_transaction.startAddr +
                    " To " + transaction.first_transaction.desAddr;
            viewHolder.firstRouteText.setText(firstRoute);
            viewHolder.firstPassengerText.setText(transaction.first_transaction.user.username);
        }

        if(transaction.second_transaction != null ) {
            String secondRoute = "From " + transaction.second_transaction.startAddr +
                    " To " + transaction.second_transaction.desAddr;
            viewHolder.secondRouteText.setText(secondRoute);
            viewHolder.secondPassengerText.setText(transaction.second_transaction.user.username);
        }

        int status = transaction.status;
        String statusText = "";
        if (status >= 300 && status < 400) statusText = "Completed";
        else if (status >= 400) statusText = "Cancelled";
        else statusText = "Processing";
        viewHolder.statusText.setText(statusText);

        Drawable img;
        if(statusText.equals("Completed")) {
            img = mContext.getResources().getDrawable( R.drawable.ic_complete_24px);
        } else if(statusText.equals("Cancelled")) {
            img = mContext.getResources().getDrawable( R.drawable.ic_cancel_24dp);
        } else  {
            img = mContext.getResources().getDrawable( R.drawable.ic_processing);
        }

        img.setBounds( 0, 0, 30, 30);
        viewHolder.statusText.setCompoundDrawables( img, null, null, null );
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
