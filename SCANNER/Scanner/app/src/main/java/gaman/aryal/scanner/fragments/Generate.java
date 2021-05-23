package gaman.aryal.scanner.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;

import gaman.aryal.scanner.DatabaseListener;
import gaman.aryal.scanner.Helper;
import gaman.aryal.scanner.R;
import gaman.aryal.scanner.dabtabase.DatabaseManager;

public class Generate extends Fragment implements DatabaseListener {

    Helper helper;
    Activity activity;
    DatabaseManager myDb;

    DisplayMetrics displayMetrics;
    int size_of_image;

    EditText qr_data;
    AppCompatButton qr_generator, save_generated;
    ImageView generated_qr;

    Bitmap image_bitmap;

    ContextWrapper cw;
    File internal_storage_path;

    InputMethodManager imm;

    public Generate() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        helper = new Helper();
        myDb = new DatabaseManager(activity);

        calculateSize();

        cw = new ContextWrapper(activity.getApplicationContext());
        internal_storage_path = cw.getDir("SCANNER", Context.MODE_PRIVATE);

        imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void calculateSize() {
        displayMetrics = activity.getResources().getDisplayMetrics();
        size_of_image = (int) (0.75 * displayMetrics.widthPixels);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_generate, container, false);

        init(v);
        return v;
    }

    private void init(View v) {
        qr_data = v.findViewById(R.id.qr_data);
        qr_generator = v.findViewById(R.id.qr_generator);
        save_generated = v.findViewById(R.id.save_generated);
        generated_qr = v.findViewById(R.id.generated_qr);

        save_generated.setVisibility(View.GONE);
        generated_qr.setVisibility(View.GONE);

        generated_qr.getLayoutParams().width = size_of_image;
        generated_qr.getLayoutParams().height = size_of_image;

        qr_generator.setOnClickListener(v12 -> generateCode(qr_data.getText().toString().trim()));
        save_generated.setOnClickListener(v1 -> saveQR(qr_data.getText().toString().trim()));
    }

    private void generateCode(String data) {
        if (data.isEmpty()) {
            helper.displayToast(R.string.p_write_smth, activity);
        } else {

            MultiFormatWriter writer = new MultiFormatWriter();
            try {
                BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, size_of_image, size_of_image);

                BarcodeEncoder encoder = new BarcodeEncoder();
                image_bitmap = encoder.createBitmap(matrix);
                generated_qr.setImageBitmap(image_bitmap);
                generated_qr.setVisibility(View.VISIBLE);
                imm.hideSoftInputFromWindow(qr_data.getApplicationWindowToken(), 0);

                save_generated.setVisibility(View.VISIBLE);
            } catch (WriterException e) {
                e.printStackTrace();
            }


        }
    }

    private void saveQR(String data) {
        new Helper.Save("",
                data,
                "g",
                image_bitmap,
                internal_storage_path,
                myDb,
                this).execute();
    }

    @Override
    public void onSaved(String type, Boolean result) {
        if (type.equals("g")) {
            if (result.equals(true)) {
                qr_data.setText("");
                generated_qr.setImageBitmap(null);
                generated_qr.setVisibility(View.GONE);
                save_generated.setVisibility(View.GONE);
                helper.displayToast(R.string.saved, activity);
            } else {
                helper.displayToast(R.string.smth_wrng, activity);
            }
        }
    }
}