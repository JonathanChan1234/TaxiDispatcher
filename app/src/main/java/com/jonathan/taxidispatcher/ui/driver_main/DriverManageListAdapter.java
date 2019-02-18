package com.jonathan.taxidispatcher.ui.driver_main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jonathan.taxidispatcher.R;
import com.jonathan.taxidispatcher.data.model.Taxi;
import com.jonathan.taxidispatcher.data.model.Taxis;
import java.util.List;

public class DriverManageListAdapter extends RecyclerView.Adapter<DriverManageListAdapter.ViewHolder> {
    private Context mContext;
    private List<Taxi> mData;
    private DriverManageListAdapter.OnItemClickedInterface onItemClickedInterface;

    public DriverManageListAdapter(Context context, List<Taxi> data, DriverManageListAdapter.OnItemClickedInterface onItemClickedInterface) {
        this.mContext = context;
        this.mData = data;
        this.onItemClickedInterface = onItemClickedInterface;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemIcon;
        private TextView settingText;
        private Button deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.driverSettingIcon);
            settingText = itemView.findViewById(R.id.driverSettingItemText);
            deleteButton = itemView.findViewById(R.id.deleteTaxiButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.driver_manage_setting_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverManageListAdapter.ViewHolder viewHolder, int position) {
        String dataText = mData.get(position).platenumber;
        viewHolder.settingText.setText(dataText);
        viewHolder.itemIcon.setImageResource(R.drawable.ic_taxi_48px);
        viewHolder.deleteButton.setOnClickListener(view -> {
            onItemClickedInterface.onItemClicked(position, dataText);
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    interface OnItemClickedInterface {
        public void onItemClicked(int position, String text);
    }
}
