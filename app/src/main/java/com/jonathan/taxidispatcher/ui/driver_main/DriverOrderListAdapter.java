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
import com.jonathan.taxidispatcher.data.model.Transcation;

import java.util.List;

public class DriverOrderListAdapter extends RecyclerView.Adapter<DriverOrderListAdapter.ViewHolder> {
    private Context mContext;
    private List<Transcation> mData;
    private DriverManageListAdapter.OnItemClickedInterface onItemClickedInterface;

    public DriverOrderListAdapter(Context context,
                                  List<Transcation> data) {
        this.mContext = context;
        this.mData = data;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView routeText;
        private TextView statusText;
        private TextView dateText;
        private TextView passengerText;

        ViewHolder(View itemView) {
            super(itemView);
            routeText = itemView.findViewById(R.id.routeText);
            statusText = itemView.findViewById(R.id.statusText);
            dateText = itemView.findViewById(R.id.dateText);
            passengerText = itemView.findViewById(R.id.passengerText);
        }
    }

    @NonNull
    @Override
    public DriverOrderListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.driver_order_item_layout, parent, false);
        return new DriverOrderListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverOrderListAdapter.ViewHolder viewHolder, int position) {
        String route = "From " + mData.get(position).startAddr + " To " + mData.get(position).desAddr;
        int status = mData.get(position).status;
        String statusText = "";
        if (status >= 300 && status < 400) statusText = "Completed";
        else if (status >= 400) statusText = "Cancelled";
        else statusText = "Processing";
        viewHolder.routeText.setText(route);
        viewHolder.statusText.setText(statusText);
        viewHolder.passengerText.setText(mData.get(position).user.username);
        viewHolder.dateText.setText(mData.get(position).updatedAt.date.substring(0,19));
        Drawable img;
        if(statusText.equals("Completed")) {
             img = mContext.getResources().getDrawable( R.drawable.ic_complete_24px);
        } else if(statusText.equals("Cancelled")) {
            img = mContext.getResources().getDrawable( R.drawable.ic_cancel_24dp);
        } else {
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
