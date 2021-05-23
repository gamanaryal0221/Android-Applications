package gaman.aryal.note.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import gaman.aryal.note.R;
import gaman.aryal.note.databasehelper.model.TaskModel;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{

    private final List<TaskModel> allTasks;
    private final Context context;

    public TaskAdapter(List<TaskModel> allTasks, Context context) {
        this.allTasks = allTasks;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdapter.TaskViewHolder holder, int position) {
        holder.setData(allTasks.get(position));
    }

    @Override
    public int getItemCount() {
        return allTasks.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder{

        TextView hTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            hTask = itemView.findViewById(R.id.hTask);
        }

        public void setData(TaskModel eachTask) {

            if (eachTask.getTask().isEmpty()){
                hTask.setText("Null");
            }else {
                hTask.setText(Html.fromHtml("<span style=\"color:" + eachTask.getDescription() + "</span"));
            }
        }
    }
}
