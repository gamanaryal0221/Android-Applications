package gaman.aryal.scanner;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import gaman.aryal.scanner.dabtabase.DatabaseManager;
import gaman.aryal.scanner.fragments.Generate;
import gaman.aryal.scanner.fragments.lists.List;
import gaman.aryal.scanner.fragments.Scan;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.WHITE;

import java.io.File;

public class Main extends AppCompatActivity implements View.OnClickListener, ScannerListener, DatabaseListener {

    ImageView scan_icon, list_icon, generate_icon, cancel;

    View bottomSheetView;
    BottomSheetBehavior bottomSheetBehavior;

    TextView main_text, result;
    EditText title;
    BottomSheetListener bottomSheetListener;
    HorizontalScrollView btn_holder;
    AppCompatButton web_search, save;

    Helper helper;
    DatabaseManager myDb;

    ContextWrapper cw;
    File internal_storage_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        helper = new Helper();
        myDb = new DatabaseManager(this);

        init();
        initBottomSheet();

        cw = new ContextWrapper(getApplicationContext());
        internal_storage_path = cw.getDir("SCANNER", Context.MODE_PRIVATE);
    }

    private void init() {
        list_icon = findViewById(R.id.list_icon);
        scan_icon = findViewById(R.id.scan_icon);
        generate_icon = findViewById(R.id.generate_icon);

        list_icon.setOnClickListener(this);
        scan_icon.setOnClickListener(this);
        generate_icon.setOnClickListener(this);

        changeTintColor(WHITE, GREEN, WHITE);
        showFragment(new Scan());
    }

    private void changeTintColor(int list_color, int scan_color, int generate_color) {
        list_icon.setColorFilter(list_color);
        scan_icon.setColorFilter(scan_color);
        generate_icon.setColorFilter(generate_color);
    }

    private void initBottomSheet() {
        bottomSheetView = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);

        cancel = findViewById(R.id.cancel);
        main_text = findViewById(R.id.main_text);
        main_text.setText(R.string.result);
        result = findViewById(R.id.result);
        title = findViewById(R.id.title);
        btn_holder = findViewById(R.id.btn_holder);
        web_search = findViewById(R.id.web_search);
        save = findViewById(R.id.save);

        cancel.setOnClickListener(this);
        save.setOnClickListener(this);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetListener.onSheetCollapsed();
                    result.setText("");
                    web_search.setVisibility(View.GONE);
                    if (title.getVisibility() == View.VISIBLE) {
                        title.setText("");
                        main_text.setText(R.string.result);
                        title.setVisibility(View.GONE);
                        btn_holder.setVisibility(View.VISIBLE);
                        save.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_icon:
                changeTintColor(WHITE, GREEN, WHITE);
                showFragment(new Scan());
                break;
            case R.id.list_icon:
                changeTintColor(GREEN, WHITE, WHITE);
                showFragment(new List());
                break;
            case R.id.generate_icon:
                changeTintColor(WHITE, WHITE, GREEN);
                showFragment(new Generate());
                break;
            case R.id.cancel:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            case R.id.save:
                saveThis(title.getText().toString().trim());
                break;
        }
    }

    private void saveThis(String title) {
        if (title.isEmpty()) {
            helper.displayToast(R.string.no_title, this);
        } else if (title.length() > 50) {
            helper.displayToast(R.string.long_title, this);
        } else {
            new Helper.Save(title,
                    result.getText().toString(),
                    "s",
                    null,
                    null,
                    myDb,
                    this).execute();
        }
    }

    public void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    @Override
    public void onCodeScanned(String scanned_result, BottomSheetListener bottomSheetListener) {
        this.bottomSheetListener = bottomSheetListener;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        result.setText(scanned_result);
        if (URLUtil.isValidUrl(scanned_result) && Patterns.WEB_URL.matcher(scanned_result).matches()) {
            web_search.setVisibility(View.VISIBLE);
        } else {
            if (scanned_result.startsWith("www.")) {
                web_search.setVisibility(View.VISIBLE);
            } else {
                web_search.setVisibility(View.GONE);
            }
        }
    }

    public void tryMore(View view) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void webSearch(View view) {
        String url = result.getText().toString();
        if (url.startsWith("www.")) {
            search("https://" + url);
        } else {
            search(url);
        }

    }

    private void search(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public void copy(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("scan result", result.getText().toString());
        clipboard.setPrimaryClip(clip);
        helper.displayToast(R.string.copied, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @SuppressLint("SetTextI18n")
    public void proceedToSave(View view) {
        main_text.setText("Save in your device");
        title.setVisibility(View.VISIBLE);
        btn_holder.setVisibility(View.GONE);
        save.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaved(String type, Boolean result) {
        if (type.equals("s")) {
            if (result.equals(true)) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                helper.displayToast(R.string.saved, this);
            } else {
                helper.displayToast(R.string.smth_wrng, this);
            }
        }
    }

    @Override
    public void onBackPressed() {

    }
}
