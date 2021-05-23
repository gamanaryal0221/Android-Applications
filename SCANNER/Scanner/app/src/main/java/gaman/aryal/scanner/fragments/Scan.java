package gaman.aryal.scanner.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;

import gaman.aryal.scanner.BottomSheetListener;
import gaman.aryal.scanner.Helper;
import gaman.aryal.scanner.R;
import gaman.aryal.scanner.ScannerListener;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.WHITE;

public class Scan extends Fragment implements BottomSheetListener {

    Helper helper;

    private static final int CAMERA_CODE = 100;
    private static final String[] CAMERA_PERMISSION = {
            Manifest.permission.CAMERA,
    };

    CodeScanner codeScanner;
    CodeScannerView codeScannerView;

    ScannerListener scannerListener;
    Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ScannerListener) {
            scannerListener = (ScannerListener) context;
        }
    }


    public Scan() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        helper = new Helper();
        getPermission();
    }

    private void getPermission() {

        if (helper.checkPermissionOf(Manifest.permission.CAMERA, activity) !=
                PackageManager.PERMISSION_GRANTED) {

            helper.displayToast(R.string.camera_perm_required, activity);
            helper.askPermission(CAMERA_CODE, CAMERA_PERMISSION, activity);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_scan, container, false);

        init(v);
        return v;
    }

    private void init(View v) {

        codeScannerView = v.findViewById(R.id.scanner_view);
        codeScanner = new CodeScanner(activity, codeScannerView);
        activateScanner();

        // Callbacks
        codeScanner.setDecodeCallback(result -> activity.runOnUiThread(() -> {
            codeScannerView.setFrameColor(GREEN);
            scannerListener.onCodeScanned(result.toString(), Scan.this);

        }));
    }

    private void activateScanner() {
        if (helper.checkPermissionOf(Manifest.permission.CAMERA, activity) == PackageManager.PERMISSION_GRANTED) {
            codeScanner.startPreview();
        } else {
            helper.displayToast(R.string.p_allow_camera_perm, activity);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        codeScannerView.setFrameColor(WHITE);
        activateScanner();
    }

    @Override
    public void onSheetCollapsed() {
        onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        codeScanner.releaseResources();
    }
}