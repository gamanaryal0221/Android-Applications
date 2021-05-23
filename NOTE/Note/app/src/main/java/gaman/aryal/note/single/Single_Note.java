package gaman.aryal.note.single;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import gaman.aryal.note.Helper;
import gaman.aryal.note.R;
import gaman.aryal.note.BottomSheet;
import gaman.aryal.note.databasehelper.Room_Database;
import gaman.aryal.note.databasehelper.model.NoteModel;

public class Single_Note extends AppCompatActivity implements BottomSheet.BottomSheetListener {

    int NOTE_ID;
    Helper helper;
    boolean isDeleted = false;

    ContextWrapper cw;
    File internal_storage_path;

    TextView Title, Content, showOriginal, showHistory;
    ImageView Image, multipleUseBtn;
    LinearLayout noteOptions;
    NoteModel currentNote;

    BottomSheet bottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.individual_note);

        getFromIntent();

        helper = new Helper();

        cw = new ContextWrapper(getApplicationContext());
        internal_storage_path = cw.getDir("NOTE", Context.MODE_PRIVATE);

        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadNoteContent();
    }

    private void getFromIntent() {
        if (getIntent().getExtras() != null) {
            NOTE_ID = getIntent().getExtras().getInt("NOTE_ID");
        }
    }
    private void init() {
        Title = findViewById(R.id.Title);
        Content = findViewById(R.id.Content);
        showOriginal = findViewById(R.id.showOriginal);
        showHistory = findViewById(R.id.showHistory);
        Image = findViewById(R.id.Image);
        noteOptions = findViewById(R.id.noteOpt);
        multipleUseBtn = findViewById(R.id.multipleUseBtn);

        Content.setText(Html.fromHtml("<span style=\"color:Olive\">"
                + getString(R.string.loading)
                + "</span"));
    }

    @SuppressLint("ResourceAsColor")
    private void loadNoteContent() {

        currentNote = Room_Database.getInstance(this)
                .getAccess()
                .extractContentOfsingleNote(NOTE_ID)
                .get(0);

        LinearLayout.LayoutParams historyparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams contentparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);

        Title.setText(currentNote.getTitle());

        if (currentNote.getIsInBin()==1){
            Title.setTextColor(Color.RED);
            multipleUseBtn.setImageResource(R.drawable.restore);
            multipleUseBtn.setBackgroundResource(R.drawable.green_btn);
        }else {
            Title.setTextColor(Color.BLUE);
            multipleUseBtn.setImageResource(R.drawable.edit);
            multipleUseBtn.setBackgroundResource(R.drawable.blue_btn);
        }

        if (currentNote.getContent().isEmpty()){
            Content.setVisibility(View.GONE);
            showOriginal.setVisibility(View.GONE);
            historyparams.weight = 2.0f;
            contentparams.weight = 0f;
        }else {
            setNoteContent(currentNote.getContent());
            if (currentNote.getIsEncrypted()==1){
                showOriginal.setVisibility(View.VISIBLE);
                historyparams.weight = 1.0f;
                contentparams.weight = 1.0f;
                if (currentNote.getIsSeen()==1){
                    showOriginal.setText(R.string.hide_original_content);
                    setNoteContent(helper.decryptData(currentNote.getContent()));
                }else {
                    showOriginal.setText(R.string.show_original_content);
                }
            }else {
                showOriginal.setVisibility(View.GONE);
                historyparams.weight = 2.0f;
                contentparams.weight = 0f;
            }
            Content.setVisibility(View.VISIBLE);
        }

        showHistory.setLayoutParams(historyparams);
        showOriginal.setLayoutParams(contentparams);
        showHistory.setGravity(Gravity.CENTER);
        showOriginal.setGravity(Gravity.CENTER);


        if (currentNote.getImage().isEmpty()){
            Image.setVisibility(View.GONE);
        }else {
            Image.setVisibility(View.VISIBLE);
            Image.setImageBitmap(helper.loadImageFromStorage(currentNote.getImage(),internal_storage_path));
        }
    }

    private void setNoteContent(String note_content) {
        Content.setText(note_content);
    }

    public void shareNote(View view) {
    }

    public void showNoteOptions(View view) {
        if (noteOptions.getVisibility()==View.VISIBLE){
            hideNoteOpt();
        }else {
            noteOptions.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> hideNoteOpt(),4000);
        }
    }
    private void hideNoteOpt() {
        noteOptions.setVisibility(View.GONE);
    }

    public void showOriginalData(View view) {

        Content.setText(Html.fromHtml("<span style=\"color:Olive\">"
                + getString(R.string.loading)
                + "</span"));

        changeView(showOriginal,
                R.color.black_transparent,
                Color.WHITE);
        if (currentNote.getIsSeen()==1){
            helper.changeVisibility(0,
                    NOTE_ID,
                    this);

            helper.updateHistory(NOTE_ID,
                    "us+",
                    this);
            loadNoteContent();
            helper.displayToast(R.string.done,
                    this);
        }else {
            isDeleted = false;
            openBottomSheet("EDITTEXT",
                    getString(R.string.t_check_pin),
                    "DECRYPT",
                    NOTE_ID);
        }
    }

    public void showHistory(View view) {
        changeView(showHistory,
                R.color.black_transparent,
                Color.WHITE);
        isDeleted = false;
        openBottomSheet("HISTORY",
                "",
                "",
                NOTE_ID);
    }
    public void changeView(TextView text_view, int background, int text_color){
        text_view.setBackgroundResource(background);
        text_view.setTextColor(text_color);

        new Handler().postDelayed(() -> changeView(text_view,
                R.color.white,
                Color.BLACK),100);
    }

    public void editOrRestoreNote(View view) {
        hideNoteOpt();
        if (currentNote.getIsInBin()==1){
            helper.moveNote(NOTE_ID,
                    0,
                    this);
            helper.updateHistory(NOTE_ID,
                    "nr+",
                    this);
            loadNoteContent();
            helper.displayToast(R.string.done,
                    this);
        }else {
            isDeleted = false;
            openBottomSheet("EDITTEXT",
                    getString(R.string.t_check_pin),
                    "EDIT",
                    NOTE_ID);
        }
    }

    public void deleteNote(View view) {
        hideNoteOpt();
        if (currentNote.getIsInBin()==1){
            isDeleted = true;
            openBottomSheet("EDITTEXT",
                    getString(R.string.t_check_pin),
                    "DELETE_PERMANENTLY",
                    NOTE_ID);
        }else {
            isDeleted = false;
            openBottomSheet("EDITTEXT",
                    getString(R.string.t_check_pin),
                    "MOVE_TO_TRASH",
                    NOTE_ID);
        }

    }

    public void moveBack(View view) {
        super.onBackPressed();
    }
    @Override
    public void onBackPressed() {
        if (noteOptions.getVisibility()==View.VISIBLE){
            hideNoteOpt();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNoteContent();
    }
    @Override
    public void refresh(boolean activity) {
        if (isDeleted){
            finish();
        }else {
            loadNoteContent();
        }
    }

    @Override
    public void createNewPin() {
        openBottomSheet("EDITTEXT",
                getString(R.string.t_add_pin),
                "",
                0);
    }

    @Override
    public void forgotPin() {
    }
    @Override
    public void resendMail(String task) {
    }

    @Override
    public void dismissed() {
        Content.setText(currentNote.getContent());
    }

    public void openBottomSheet(String looking_for, String task, String btn_task, int note_id){
        bottomSheet = new BottomSheet(looking_for,task,btn_task,note_id, this, this,true);
        bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
    }
}