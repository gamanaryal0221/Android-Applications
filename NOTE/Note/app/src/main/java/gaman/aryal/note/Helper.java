package gaman.aryal.note;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.spec.KeySpec;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import gaman.aryal.note.add_update.Add_Update;
import gaman.aryal.note.databasehelper.Room_Database;
import gaman.aryal.note.databasehelper.model.HistoryModel;
import gaman.aryal.note.databasehelper.model.My_DataModel;
import gaman.aryal.note.databasehelper.model.TaskModel;
import gaman.aryal.note.home.MainPage;

import static android.provider.Telephony.Carriers.PASSWORD;

public class Helper {


    public String retrieveFrontDetail(int data_status, int image_status) {

        // 0 = not added
        // 1 = added
        // 2 = deleted
        // 3 = re added

        return dataManager(data_status) + "<br>" + imageManager(image_status);

    }

    public String dataManager(int data_status) {

        String data = "";
        if (data_status == 0) {
            data = "<span style=\"color:red\">empty</span>";
        } else if (data_status == 1) {
            data = "<span style=\"color:Olive\">available</span>";
        } else if (data_status == 2) {
            data = "<span style=\"color:red\">deleted</span>";
        } else if (data_status == 3) {
            data = "<span style=\"color:red\">re added</span>";
        }

        return "<b>Data :</b> " + data;
    }
    public String imageManager(int image_status) {

        String data = "";
        if (image_status == 0) {
            data = "<span style=\"color:red\">not added</span>";
        } else if (image_status == 1) {
            data = "<span style=\"color:Olive\">available</span>";
        } else if (image_status == 2) {
            data = "<span style=\"color:red\">deleted</span>";
        }else if (image_status == 3) {
            data = "<span style=\"color:red\">re added</span>";
        }

        return "<b>Image :</b> " + data;
    }

    public String encryptData(String actualData) {
        String encryptedText = "";
        try {
            Cipher c = getCipher(Cipher.ENCRYPT_MODE);
            byte[] encryptedVal = c.doFinal(getBytes(actualData));
            encryptedText = Base64.encodeToString(encryptedVal, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedText;
    }
    public String decryptData(String encryptedData) {
        String decryptedText = "";
        try {
            byte[] decodedValue = Base64.decode(getBytes(encryptedData), Base64.DEFAULT);
            Cipher c = getCipher(Cipher.DECRYPT_MODE);
            byte[] decValue = c.doFinal(decodedValue);
            decryptedText = new String(decValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedText;
    }
    public static Key generateKey() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        char[] password = PASSWORD.toCharArray();
        byte[] salt = getBytes("Gaman_Aryal_0221");

        KeySpec spec = new PBEKeySpec(password, salt, 65536, 128);
        SecretKey tmp = factory.generateSecret(spec);
        byte[] encoded = tmp.getEncoded();
        return new SecretKeySpec(encoded, "AES");
    }
    public static Cipher getCipher(int mode) throws Exception {
        Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = getBytes("IV_VALUE_16_BYTE");
        c.init(mode, generateKey(), new IvParameterSpec(iv));
        return c;
    }
    public static byte[] getBytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public void askAllPermissions(int all_permission_code, String[] all_permissions, Activity activity) {
        if (checkPermissionOf(Manifest.permission.CAMERA, activity) +
                checkPermissionOf(Manifest.permission.READ_EXTERNAL_STORAGE, activity) +
                checkPermissionOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, activity) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    activity,
                    all_permissions,
                    all_permission_code
            );
        }

        return;
    }
    public int checkPermissionOf(String permission, Context context) {
        return ContextCompat.checkSelfPermission(context, permission);
    }

    public Uri bitmapToUri(Bitmap bitmap, Context context) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Image", null);
        return Uri.parse(path);
    }
    public void saveImage(String image_name, Uri image_uri, File path, Activity activity) {
        File file = new File(path, image_name);
        if (file.exists()) {
            boolean deleted = file.delete();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            uriToBitmap(image_uri, activity).compress(Bitmap.CompressFormat.PNG, 100, fos);
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
    public Bitmap uriToBitmap(Uri image_uri, Activity activity) {
        try {
            return MediaStore.Images.Media.getBitmap(activity.getContentResolver(), image_uri);
        } catch (Exception e) {
            return null;
        }
    }
    public boolean doesImageExist(String image_name, File path) {
        boolean result = false;
        try {
            File f = new File(path, image_name);
            result = BitmapFactory.decodeStream(new FileInputStream(f)) != null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
    public Bitmap loadImageFromStorage(String image_name, File path) {
        try {
            File f = new File(path, image_name);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            return null;
        }

    }

    public void showProgressDialog(ProgressDialog pd, String title, String message) {
        pd.setTitle(title);
        pd.setMessage(message);
        pd.show();
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
    public void displayToast(int text, Context context) {
        Toast.makeText(context,
                text,
                Toast.LENGTH_SHORT)
                .show();
    }

    public void closeKeyboard(InputMethodManager imm, Activity activity) {
        View focusedView = activity.getCurrentFocus();
        if (focusedView != null)
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setName(String name, Activity activity) {
        Room_Database.getInstance(activity)
                .getAccess()
                .setName(name);
    }
    public void setEmail(String email, Activity activity) {
        Room_Database.getInstance(activity)
                .getAccess()
                .setEmail(email);
    }

    public boolean moveNote(int note_id, int toBin, Activity activity) {

        return !String.valueOf(Room_Database.getInstance(activity)
                .getAccess()
                .moveNote(note_id,
                        toBin))
                .isEmpty();
    }

    public boolean updateHistory(int note_id, String task, Activity activity) {
        HistoryModel newHistory = new HistoryModel();
        newHistory.setNote_ID(note_id);
        newHistory.setTask(task);
        newHistory.setDate(DateFormat.getDateTimeInstance()
                .format(new Date()));

        return !String.valueOf(Room_Database.getInstance(activity)
                .getAccess()
                .addHistory(newHistory))
                .isEmpty();
    }

    public List<TaskModel> makeTask(String task_identifier) {
        List<TaskModel> allTasks = new ArrayList<>();
        String task = "";

        for (int i = 0; i < task_identifier.length(); i++) {
            if (task.length()==2) {

                TaskModel eachTask = new TaskModel();
                eachTask.setTask(task);
                eachTask.setDescription(elaborateTask(task));

                allTasks.add(eachTask);
                task = "";
            } else {
                task = task + task_identifier.charAt(i);
            }
        }
        return allTasks;
    }

    public String elaborateTask(String short_task) {
        switch (short_task) {
            case "nc":
                return "Olive\">note created";
            case "da":
                return "Olive\">data added";
            case "ia":
                return "Olive\">image uploaded";
            case "tm":
                return "purple\">title modified";
            case "dm":
                return "purple\">data modified";
            case "im":
                return "purple\">image changed";
            case "dd":
                return "red\">data deleted";
            case "id":
                return "red\">image deleted";
            case "nd":
                return "red\">note moved to trash";
            case "nr":
                return "Olive\">note recovered from trash";
            case "ns":
                return "purple\">original data made visible";
            case "us":
                return "purple\">data encrypted";
            default:
                return "";
        }
    }

    public boolean changeVisibility(int toSee, int note_id, Activity activity) {
        return !String.valueOf(Room_Database.getInstance(activity)
                .getAccess()
                .changeVisibility(note_id,toSee))
                .isEmpty();
    }

    public boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean deleteNote(int note_id, Activity activity) {
        if (!String.valueOf(Room_Database.getInstance(activity)
        .getAccess()
        .deleteHistory(note_id)).isEmpty()){

            return  !String.valueOf(Room_Database.getInstance(activity)
                    .getAccess()
                    .deleteNote(note_id)).isEmpty();
        }else {
            return false;
        }
    }

    public String generatePIN() {
        Random random = new Random();
        int low = 1000, high = 9999999;
        return String.valueOf(random.nextInt(high - low) + low);
    }

    public boolean isNetworkConnected(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public boolean updateTempPin(String generated_pin, Activity activity) {
        return !String
                .valueOf(
                        Room_Database
                                .getInstance(activity)
                                .getAccess()
                                .setTemp_pin(generated_pin))
                .isEmpty();
    }
}
