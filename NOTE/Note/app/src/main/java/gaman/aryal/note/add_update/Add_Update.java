package gaman.aryal.note.add_update;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

import gaman.aryal.note.Helper;
import gaman.aryal.note.R;
import gaman.aryal.note.databasehelper.Room_Database;
import gaman.aryal.note.databasehelper.model.HistoryModel;
import gaman.aryal.note.databasehelper.model.NoteModel;

public class Add_Update extends AppCompatActivity implements TextWatcher {

    boolean isForUpdate = false;
    int NOTE_ID;
    NoteModel currentNote;

    ProgressDialog progressDialog;
    Helper helper;

    InputMethodManager imm;

    TextView pageTitle, pageTitleCount;
    ImageView left_icon, selectedImage;
    Uri IMAGE_URI = null , TEMP_URI = null;

    ContextWrapper cw;
    File internal_storage_path;

    EditText title, content;
    String TITLE, CONTENT;

    CheckBox checkBox;

    LinearLayout gallerySection, cameraSection, imageUnselector;
    Button add_UpdateBtn;

    private static final int CAMERA_CODE = 100;
    private static final int STORAGE_CODE = 101;
    private static final int ALL_PERMISSIONS_CODE = 111;
    String[] ALL_NEEDED_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add__update);

        getFromIntent();

        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        helper = new Helper();
        progressDialog = new ProgressDialog(this);

        cw = new ContextWrapper(getApplicationContext());
        internal_storage_path = cw.getDir("NOTE", Context.MODE_PRIVATE);

        init();
        helper.closeKeyboard(imm,this);
    }

    private void getFromIntent() {
        if (getIntent().getExtras() != null) {
            NOTE_ID = getIntent().getExtras().getInt("NOTE_ID");
            if (String.valueOf(NOTE_ID).isEmpty()){
                isForUpdate = false;
            }else {
                isForUpdate = true;
                currentNote = Room_Database.getInstance(this)
                        .getAccess()
                        .extractContentOfsingleNote(NOTE_ID)
                        .get(0);
            }
        }
    }
    private void init() {
        pageTitle = findViewById(R.id.pageTitle);
        pageTitleCount = findViewById(R.id.pageTitleCount);
        pageTitleCount.setVisibility(View.GONE);

        left_icon = findViewById(R.id.left_icon);
        left_icon.setImageResource(R.drawable.back);

        title = findViewById(R.id.title);
        content = findViewById(R.id.data);
        content.addTextChangedListener(this);

        checkBox = findViewById(R.id.checkBox);

        selectedImage = findViewById(R.id.selectedImage);
        gallerySection = findViewById(R.id.gallerySection);
        cameraSection = findViewById(R.id.cameraSection);
        imageUnselector = findViewById(R.id.imageUnselector);

        add_UpdateBtn = findViewById(R.id.add_UpdateBtn);

        if (isForUpdate){
            pageTitle.setText(R.string.edit);
            add_UpdateBtn.setText(R.string.update);
        }else {
            pageTitle.setText(R.string.create_new_note);
            add_UpdateBtn.setText(R.string.save);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isForUpdate) setData();
    }

    private void setData() {
        title.setText(currentNote.getTitle());
        if (currentNote.getIsEncrypted()==1){
            checkBox.setChecked(true);
            checkBox.setVisibility(View.VISIBLE);
            content.setText(helper.decryptData(currentNote.getContent()));
        }else {
            content.setText(currentNote.getContent());
        }
        if (!currentNote.getImage().isEmpty()){
            Bitmap bitmap = helper.loadImageFromStorage(currentNote.getImage(),internal_storage_path);
            selectedImage.setImageBitmap(bitmap);
            imageSelected();
            IMAGE_URI = TEMP_URI = helper.bitmapToUri(bitmap,this);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() == 0){
            checkBox.setVisibility(View.GONE);
        }else {
            checkBox.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void afterTextChanged(Editable s) {
    }


    public void addPhotoClicked(View view) {
        showImageChoosingOpt();
    }
    private void showImageChoosingOpt() {
        helper.askAllPermissions(ALL_PERMISSIONS_CODE,
                ALL_NEEDED_PERMISSIONS,
                this);
        gallerySection.setVisibility(View.VISIBLE);
        cameraSection.setVisibility(View.VISIBLE);
        new Handler().postDelayed(this::hideImageChoosingOpt,
                3000);
    }
    private void hideImageChoosingOpt() {
        gallerySection.setVisibility(View.GONE);
        cameraSection.setVisibility(View.GONE);
    }

    public void galleryClicked(View view) {
        checkPermissionAndMove(false);
    }
    public void cameraClicked(View view) {
        checkPermissionAndMove(true);
    }
    private void checkPermissionAndMove(boolean isUsingCamera) {
        if (isUsingCamera) {
            if (helper.checkPermissionOf(Manifest.permission.CAMERA, this) +
                    helper.checkPermissionOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, this) !=
                    PackageManager.PERMISSION_GRANTED) {

                helper.displayToast(R.string.no_perm, this);

            } else {
                openCamera();
            }
        } else {
            if (helper.checkPermissionOf(Manifest.permission.READ_EXTERNAL_STORAGE, this) !=
                    PackageManager.PERMISSION_GRANTED) {

                helper.displayToast(R.string.no_perm, this);

            } else {
                openGallery();
            }
        }

    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, STORAGE_CODE);
    }
    private void openCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CODE && data != null ) {
                IMAGE_URI = helper.bitmapToUri((Bitmap) data.getExtras().get("data"), Add_Update.this);
            } else if (requestCode == STORAGE_CODE && data != null && data.getData() != null) {
                IMAGE_URI = data.getData();
            }
            selectedImage.setImageURI(IMAGE_URI);
            imageSelected();
            return;
        }
    }

    private void imageSelected() {
        selectedImage.setVisibility(View.VISIBLE);
        imageUnselector.setVisibility(View.VISIBLE);
    }

    public void unselectImage(View view) {
        IMAGE_URI = null;
        selectedImage.setVisibility(View.GONE);
        imageUnselector.setVisibility(View.GONE);
        showImageChoosingOpt();
    }

    public void addOrUpdateBtnClicked(View view) {
        TITLE = title.getText().toString().trim();
        CONTENT = content.getText().toString().trim();

        if (!TITLE.isEmpty()) {
            helper.closeKeyboard(imm,this);
            if (isForUpdate){
                helper.showProgressDialog(progressDialog,
                        "Update "+helper.makeTextShort(TITLE,10),
                        "saving data ...\nIt won't take long");

                new UpdateNote().execute();
            }else {
                helper.showProgressDialog(progressDialog,
                        "Create Note",
                        "creating "+helper.makeTextShort(TITLE,10)+"...\nIt won't take long");

                new CreateNote().execute();
            }

        } else {
            helper.displayToast(R.string.empty_title, this);
        }
    }
    public class CreateNote extends AsyncTask<Void,Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            String task = "nc+";

            NoteModel newNote = new NoteModel();
            newNote.setTitle(TITLE);
            newNote.setIsSeen(0);


            if (CONTENT.isEmpty()){
                newNote.setC_status(0);
                newNote.setContent("");
            }else {
                task = task + "da+";
                newNote.setC_status(1);

                if (checkBox.isChecked()){
                    newNote.setIsEncrypted(1);
                    task = task + "us+";
                    newNote.setContent(helper.encryptData(CONTENT));
                }else {
                    task = task + "ns+";
                    newNote.setIsEncrypted(0);
                    newNote.setContent(CONTENT);
                }
            }

            if (IMAGE_URI == null){
                newNote.setI_status(0);
                newNote.setImage("");
            }else {
                task = task + "ia+";
                newNote.setI_status(1);
                String image_name = UUID.randomUUID().toString();

                helper.saveImage(image_name,
                        IMAGE_URI,
                        internal_storage_path,
                        Add_Update.this);

                if (helper.doesImageExist(image_name,internal_storage_path)){
                    newNote.setImage(image_name);
                }
            }

            long note_id = Room_Database.getInstance(Add_Update.this)
                    .getAccess()
                    .createNote(newNote);

            if (!String.valueOf(note_id).isEmpty()){

                helper.updateHistory((int) note_id,
                        task,
                        Add_Update.this);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            addUpdateDone();
        }
    }

    public class UpdateNote extends AsyncTask<Void,Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            String task = "";

            NoteModel newNote = new NoteModel();

            if (!currentNote.getTitle().equals(TITLE)) task = task + "tm+";
            newNote.setTitle(TITLE);
            newNote.setIsSeen(0);


            if (CONTENT.isEmpty()){

                newNote.setContent("");

                if (currentNote.getC_status()==0){
                    newNote.setC_status(0);
                }else {
                    newNote.setC_status(2);
                }
                if (!currentNote.getContent().isEmpty()) task = task + "dd+";

            }else {
                if (currentNote.getContent().isEmpty()){
                    task = task + "da+";
                }else if (currentNote.getIsEncrypted()==1){
                    if (!helper.decryptData(currentNote.getContent()).equals(CONTENT)) task = task + "dm+";
                }else if (currentNote.getIsEncrypted()==0){
                    if (!currentNote.getContent().equals(CONTENT)) task = task + "dm+";
                }

                if (checkBox.isChecked()){
                    task = task + "us+";
                    newNote.setIsEncrypted(1);
                    newNote.setContent(helper.encryptData(CONTENT));
                }else {
                    task = task + "ns+";
                    newNote.setIsEncrypted(0);
                    newNote.setContent(CONTENT);
                }

                switch (currentNote.getC_status()) {
                    case 0:
                    case 1:
                        newNote.setC_status(1);
                        break;
                    case 2:
                    case 3:
                        newNote.setC_status(3);
                        break;
                }
            }


            if (IMAGE_URI == null){
                newNote.setImage("");
                if (currentNote.getI_status()==0){
                    newNote.setI_status(0);
                }else {
                    newNote.setI_status(2);
                }
                if (TEMP_URI != null) task = task + "id+";
            }else {

                switch (currentNote.getI_status()) {
                    case 0:
                    case 1:
                        newNote.setI_status(1);
                        break;
                    case 2:
                    case 3:
                        newNote.setI_status(3);
                        break;
                }

                if (TEMP_URI == IMAGE_URI){
                    newNote.setImage(currentNote.getImage());
                }else {
                    if (currentNote.getImage().isEmpty()){
                        task = task + "ia+";
                    }else {
                        task = task + "im+";
                    }

                    String image_name;

                    if (currentNote.getImage().isEmpty()){
                        image_name = UUID.randomUUID().toString();
                    }else {
                        image_name = currentNote.getImage();
                    }

                    helper.saveImage(image_name,
                            IMAGE_URI,
                            internal_storage_path,
                            Add_Update.this);

                    if (helper.doesImageExist(image_name,internal_storage_path)){
                        newNote.setImage(image_name);
                    }
                }
            }

            long history_id = Room_Database.getInstance(Add_Update.this)
                    .getAccess()
                    .updateNote(NOTE_ID,
                            newNote.getTitle(),
                            newNote.getContent(),
                            newNote.getImage(),
                            newNote.getIsSeen(),
                            newNote.getIsEncrypted(),
                            newNote.getC_status(),
                            newNote.getI_status());

            if (!String.valueOf(history_id).isEmpty()){

                helper.updateHistory(NOTE_ID,
                        task,
                        Add_Update.this);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            addUpdateDone();
        }
    }

    private void addUpdateDone() {
        progressDialog.dismiss();
        helper.displayToast(R.string.saved,Add_Update.this);
        finish();
    }

    public void leftIconClicked(View view) {
        onBackPressed();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}