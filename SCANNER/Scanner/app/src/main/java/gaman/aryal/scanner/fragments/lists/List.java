package gaman.aryal.scanner.fragments.lists;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import gaman.aryal.scanner.Helper;
import gaman.aryal.scanner.R;
import gaman.aryal.scanner.dabtabase.DatabaseManager;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;

public class List extends Fragment implements Adapter.ItemsListener {

    Activity activity;
    Helper helper;

    TextView error_text, description_text, date;
    RecyclerView recycler_view;

    java.util.List<Model> allData;
    Adapter adapter;

    DatabaseManager myDb;
    DisplayMetrics displayMetrics;

    ContextWrapper cw;
    File internal_storage_path;

    RelativeLayout big_image_holder;
    ImageView back_sign, big_image;
    boolean full_text_is_visible = false;

    LinearLayout navigation;

    public List() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();

        myDb = new DatabaseManager(activity);
        allData = new ArrayList<>();
        helper = new Helper();

        cw = new ContextWrapper(activity.getApplicationContext());
        internal_storage_path = cw.getDir("SCANNER", Context.MODE_PRIVATE);

        calculateSize();
    }

    private void calculateSize() {
        displayMetrics = activity.getResources().getDisplayMetrics();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        init(v);
        return v;
    }


    private void init(View v) {

        recycler_view = v.findViewById(R.id.recycler_view);
        error_text = v.findViewById(R.id.error_text);
        showLoading();

        setRecyclerView();

        back_sign = v.findViewById(R.id.back_sign);
        big_image_holder = v.findViewById(R.id.big_image_holder);
        big_image = v.findViewById(R.id.big_image);
        description_text = v.findViewById(R.id.description);
        date = v.findViewById(R.id.date);

        navigation = ((Activity) activity).findViewById(R.id.navigation);

        back_sign.setOnClickListener(v1 -> {
            navigation.setVisibility(View.VISIBLE);
            big_image_holder.setVisibility(View.GONE);
            big_image.setImageBitmap(null);
            description_text.setText("");
        });
        big_image_holder.setOnClickListener(v12 -> {
        });

    }

    private void showLoading() {
        error_text.setText(R.string.loading);
        error_text.setTextColor(BLACK);
    }

    private void setRecyclerView() {
        recycler_view.hasFixedSize();
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);
        recycler_view.setLayoutManager(layoutManager);
        adapter = new Adapter(allData, displayMetrics.widthPixels, internal_storage_path, activity, this);
        recycler_view.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        allData.clear();
        adapter.notifyDataSetChanged();
        new ExtractAllData().execute();
    }

    @Override
    public void onDeleteClicked(String id) {
        if (myDb.delete(id)) {
            allData.clear();
            new ExtractAllData().execute();
            helper.displayToast(R.string.deleted, activity);
        } else {
            helper.displayToast(R.string.smth_wrng, activity);
        }
    }

    @Override
    public void onItemClicked(String image_name, String description, String date) {
        navigation.setVisibility(View.GONE);
        big_image_holder.setVisibility(View.VISIBLE);
        description_text.setText(helper.makeTextShort(description, 120));
        this.date.setText(Html.fromHtml(date));
        big_image.setImageBitmap(helper.loadImageFromStorage(image_name, internal_storage_path));

        description_text.setOnClickListener(v -> {
            if (full_text_is_visible) {
                description_text.setText(helper.makeTextShort(description, 120));
                full_text_is_visible = false;
            } else {
                description_text.setText(description);
                full_text_is_visible = true;
            }
        });
    }


    public class ExtractAllData extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {

            Cursor all_data;
            allData.clear();
            all_data = myDb.extractAll();
            while (all_data.moveToNext()) {

                Model eachData = new Model();

                eachData.setID(all_data.getString(0));
                eachData.setTitle(all_data.getString(1));
                eachData.setData(all_data.getString(2));
                eachData.setType(all_data.getString(3));
                eachData.setDate(all_data.getString(4));
                eachData.setTime(all_data.getString(5));

                allData.add(eachData);
            }
            return allData.size();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            adapter.notifyDataSetChanged();

            if (allData.size() == 0) {
                error_text.setText(R.string.no_list);
                error_text.setTextColor(RED);
            } else {
                error_text.setText("");
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        showLoading();
        new ExtractAllData().execute();
    }
}