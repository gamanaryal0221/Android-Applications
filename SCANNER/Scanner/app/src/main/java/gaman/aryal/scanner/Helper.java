package gaman.aryal.scanner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import gaman.aryal.scanner.dabtabase.DatabaseManager;

public class Helper {

    public int checkPermissionOf(String permission, Context context) {
        return ContextCompat.checkSelfPermission(context, permission);
    }

    public void askPermission(int permission_code, String[] permission, Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                permission,
                permission_code
        );

        return;
    }

    public void displayToast(int text, Context context) {
        Toast.makeText(context,
                text,
                Toast.LENGTH_SHORT)
                .show();
    }

    public static class Save extends AsyncTask<Void, Void, Boolean> {
        String title, data, type;
        Bitmap image_bitmap;
        File storage_path;
        DatabaseManager myDb;
        DatabaseListener databaseListener;

        public Save(String title, String data, String type, Bitmap image_bitmap, File storage_path, DatabaseManager myDb, DatabaseListener databaseListener) {
            this.title = title;
            this.data = data;
            this.type = type;
            this.image_bitmap = image_bitmap;
            this.storage_path = storage_path;
            this.myDb = myDb;
            this.databaseListener = databaseListener;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String refined_data = "";
            boolean result;

            if (type.equals("s")) {
                result = myDb.save(title, data, "s");
            } else {
                refined_data = UUID.randomUUID().toString();
                saveImgInInternalStorage(refined_data, image_bitmap, storage_path);
                if (doesImageExist(refined_data, storage_path)) {
                    result = myDb.save(refined_data, data, "g");
                } else {
                    result = false;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            databaseListener.onSaved(type, aBoolean);
        }
    }

    private static void saveImgInInternalStorage(String image_name, Bitmap image_bitmap, File storage_path) {
        File file = new File(storage_path, image_name);
        if (file.exists()) {
            boolean deleted = file.delete();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            image_bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean doesImageExist(String image_name, File storage_path) {
        boolean result = false;
        try {
            File f = new File(storage_path, image_name);
            result = BitmapFactory.decodeStream(new FileInputStream(f)) != null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Bitmap loadImageFromStorage(String image_name, File storage_path) {
        try {
            File f = new File(storage_path, image_name);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    public String makeTextShort(String text, int limit) {

        if (text.length() < limit) {
            return text;
        } else {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < limit; i++) {
                str.append(text.charAt(i));
            }
            return str.append("...").toString();
        }
    }
}
