package com.example.android_onenet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * 历史数据列表适配器
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<HistoryDataItem> dataList = new ArrayList<>();
    private SimpleDateFormat dateFormat;

    public HistoryAdapter() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
    }

    public void setDataList(List<HistoryDataItem> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public void clearData() {
        this.dataList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryDataItem item = dataList.get(position);
        holder.tvName.setText(item.getDisplayName());
        holder.tvValue.setText(item.getDisplayValue());
        holder.tvTime.setText(dateFormat.format(new Date(item.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvValue;
        TextView tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_history_name);
            tvValue = itemView.findViewById(R.id.tv_history_value);
            tvTime = itemView.findViewById(R.id.tv_history_time);
        }
    }
}
