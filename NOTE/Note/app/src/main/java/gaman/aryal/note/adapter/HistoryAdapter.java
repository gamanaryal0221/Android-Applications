package gaman.aryal.note.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import gaman.aryal.note.Helper;
import gaman.aryal.note.R;
import gaman.aryal.note.databasehelper.model.HistoryModel;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<HistoryModel> allHistory;
    private final Helper helper;
    private final Context context;

    public HistoryAdapter(List<HistoryModel> allHistory, Context context) {
        this.allHistory = allHistory;
        this.context = context;
        helper = new Helper();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_history_holder, parent, false);
        return new HistoryAdapter.HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.setData(allHistory.get(position));
    }

    @Override
    public int getItemCount() {
        return allHistory.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {

        TextView hDate;
        RecyclerView task_recycler_view;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            hDate = itemView.findViewById(R.id.hDate);
            task_recycler_view = itemView.findViewById(R.id.task_recycler_view);
        }

        public void setData(HistoryModel eachHistory) {

            hDate.setText(eachHistory.getDate());
            task_recycler_view.setAdapter(new TaskAdapter(helper.makeTask(eachHistory.getTask()),context));
        }

    }
}
