package com.jonathan.taxidispatcher.ui.passenger_main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jonathan.taxidispatcher.R;

import java.util.List;

public class PassengerSettingListAdapter extends RecyclerView.Adapter<PassengerSettingListAdapter.ViewHolder> {
    private Context mContext;
    private List<SettingItem> mData;
    private OnItemClickedInterface onItemClickedInterface;

    public PassengerSettingListAdapter(Context context, List<SettingItem> data, OnItemClickedInterface onItemClickedInterface) {
        this.mContext = context;
        this.mData = data;
        this.onItemClickedInterface = onItemClickedInterface;
    }

    public static class SettingItem {
        String text;
        int drawableId;

        public SettingItem(String text, int drawableId) {
            this.text = text;
            this.drawableId = drawableId;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemIcon;
        private TextView settingText;

        ViewHolder(View itemView) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.passegnerSettingIcon);
            settingText = itemView.findViewById(R.id.passengerSettingItemText);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.passenger_setting_item, parent, false);
        view.setOnClickListener(view1 -> {
            onItemClickedInterface.onItemClicked(position, mData.get(position).text);
        });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.settingText.setText(mData.get(position).text);
        viewHolder.itemIcon.setImageResource(mData.get(position).drawableId);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    interface OnItemClickedInterface {
        public void onItemClicked(int position, String text);
    }
}
