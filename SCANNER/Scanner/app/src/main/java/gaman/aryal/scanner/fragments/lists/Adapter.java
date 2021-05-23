package gaman.aryal.scanner.fragments.lists;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import gaman.aryal.scanner.Helper;
import gaman.aryal.scanner.R;

public class Adapter extends RecyclerView.Adapter<Adapter.AdapterViewHolder> {

    public static final int SCANNED = 0;
    public static final int GENERATED = 1;

    private final List<Model> allData;
    private final Context context;
    private final int width_of_screen;
    private final File storage_path;
    private final Helper helper;
    private final ItemsListener itemsListener;

    public Adapter(List<Model> allData, int width_of_screen, File storage_path, Context context, ItemsListener itemsListener) {
        this.allData = allData;
        this.context = context;
        this.width_of_screen = width_of_screen;
        this.storage_path = storage_path;
        this.itemsListener = itemsListener;
        helper = new Helper();
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if (viewType == SCANNED) {
            view = LayoutInflater.from(context).inflate(R.layout.single_scanned, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.single_generated, parent, false);
        }
        return new AdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.AdapterViewHolder holder, int position) {
        holder.setData(allData.get(position), itemsListener);
    }

    @Override
    public int getItemCount() {
        return allData.size();
    }

    public class AdapterViewHolder extends RecyclerView.ViewHolder {

        TextView sTitle, sData, otherDetail;
        ImageView barCode, delete;
        ItemsListener itemsListener;

        public AdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            sTitle = itemView.findViewById(R.id.sTitle);
            sData = itemView.findViewById(R.id.sData);
            otherDetail = itemView.findViewById(R.id.otherDetail);
            barCode = itemView.findViewById(R.id.barCode);
            delete = itemView.findViewById(R.id.delete);
        }

        public void setData(Model eachData, ItemsListener itemsListener) {
            this.itemsListener = itemsListener;

            if (eachData.getType().equals("s")) {
                sTitle.setText(eachData.getTitle());
                sData.setText(eachData.getData());
                otherDetail.setText(Html.fromHtml("<b>Date:</b> " + eachData.getDate()
                        + " & <b>Time:</b> " + eachData.getTime()));
            } else {
                barCode.setImageBitmap(helper.loadImageFromStorage(eachData.getTitle(), storage_path));
                barCode.getLayoutParams().width = (int) (0.25 * width_of_screen);
                barCode.getLayoutParams().height = (int) (0.25 * width_of_screen);
                sData.setText(helper.makeTextShort(eachData.getData(), 70));
                otherDetail.setText(eachData.getDate());
            }


            delete.setOnClickListener(v -> itemsListener.onDeleteClicked(eachData.getID()));

            barCode.setOnClickListener(v -> {
                if (eachData.getType().equals("g")) {
                    itemsListener.onItemClicked(eachData.getTitle(), eachData.getData(), "<b>Date:</b> " + eachData.getDate()
                            + "<br>& <b>Time:</b> " + eachData.getTime());
                }
            });
        }

    }

    @Override

    public int getItemViewType(int position) {
        if (allData.get(position).getType().equals("s")) {
            return SCANNED;
        } else {
            return GENERATED;
        }
    }

    interface ItemsListener {
        void onDeleteClicked(String id);

        void onItemClicked(String image_name, String description, String date);
    }
}
