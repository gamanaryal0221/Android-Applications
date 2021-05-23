package gaman.aryal.note.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import gaman.aryal.note.BottomSheet;
import gaman.aryal.note.Helper;
import gaman.aryal.note.R;
import gaman.aryal.note.adapter.NoteAdapter;
import gaman.aryal.note.add_update.Add_Update;
import gaman.aryal.note.databasehelper.Room_Database;
import gaman.aryal.note.databasehelper.model.My_DataModel;
import gaman.aryal.note.databasehelper.model.NoteModel;

public class MainPage extends AppCompatActivity implements NoteAdapter.NoteListener,
        BottomSheet.BottomSheetListener {

    int isFromBin = 0;

    DrawerLayout drawerLayout;
    NavigationView drawerNav;

    TextView myInfo, pageTitle, pageTitleCount, backgroundText;
    ImageView main_btn, left_icon;

    RecyclerView recyclerView;
    List<NoteModel> allNotes;
    Helper helper;

    My_DataModel myData;
    BottomSheet bottomSheet;

    ProgressDialog progressDialog;

    RelativeLayout flash_screen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        progressDialog = new ProgressDialog(this);

        getFromIntent();
        allNotes = new ArrayList<>();
        helper = new Helper();

        init();
        leftDrawerHandler();
        loadAllNotes();
        accessMyData();

    }
    private void getFromIntent() {
        if (getIntent().getExtras() != null) {
            isFromBin = getIntent().getExtras().getInt("isFromBin");
        }else {
            isFromBin = 0;
        }
    }

    private void accessMyData() {
        myData = Room_Database.getInstance(this)
                .getAccess()
                .extractMyData()
                .get(0);

        if (myData.getPIN().equals("null") && isFromBin == 0){
//            askForPin();
        }

        setMyData();
    }

    private void setMyData() {
        if (myData.getName().equals("null") && myData.getEmail().equals("null")){
            myInfo.setText(R.string.tap_to_add);
        }else {
            String info = "";
            if (!myData.getName().equals("null")) info = "<b>"+myData.getName()+"</b>";
            if (!myData.getEmail().equals("null")) info = info + "<br><i>"+myData.getEmail()+"</i>";
            myInfo.setText(Html.fromHtml(info));
        }
    }

    private void askForPin() {
        openBottomSheet("EDITTEXT",
                getString(R.string.t_add_pin),
                "",
                0);
    }

    private void init() {

        flash_screen = findViewById(R.id.flash_screen);

        main_btn = findViewById(R.id.main_btn);
        main_btn.setVisibility(View.GONE);

        pageTitle = findViewById(R.id.pageTitle);
        if (isFromBin==1){
            pageTitle.setText(R.string.trash);
            pageTitle.setTextColor(Color.parseColor("#810404"));
        }else {
            flash_screen.setVisibility(View.VISIBLE);
            pageTitle.setText(R.string.all_notes);
            pageTitle.setTextColor(Color.parseColor("#254075"));

            new Handler().postDelayed(() -> {
                flash_screen.setVisibility(View.GONE);
                main_btn.setVisibility(View.VISIBLE);
            }, 3000);
        }

        pageTitle.setOnClickListener(v -> loadAllNotes());

        pageTitleCount = findViewById(R.id.pageTitleCount);
        left_icon = findViewById(R.id.left_icon);
        left_icon.setImageResource(R.drawable.dragger);

        backgroundText = findViewById(R.id.backgroundText);
        backgroundText.setText(
                Html.fromHtml("<span style=\"color:Olive\">"
                        + getString(R.string.loading)
                        + "</span")
        );

        recyclerView = findViewById(R.id.historyRecyclerView);

        drawerLayout = findViewById(R.id.drawerLayout);
        drawerNav = findViewById(R.id.drawerNav);

        myInfo = drawerNav.getHeaderView(0).findViewById(R.id.myInfo);
        myInfo.setOnClickListener(v -> {
            if (myInfo.getText().toString().equals(getString(R.string.tap_to_add))){
                openBottomSheet("EDITTEXT",
                        getString(R.string.t_add_name_email),
                        "",
                        0);
            }else {
                openBottomSheet("EDITTEXT",
                        getString(R.string.t_change_name_email),
                        "",
                        0);
            }
            drawerLayout.closeDrawer(drawerNav);
        });

    }
    public void loadAllNotes() {
        allNotes.clear();

        allNotes = Room_Database.getInstance(MainPage.this)
                .getAccess()
                .extractAllNotes(isFromBin);

        Collections.reverse(allNotes);
        recyclerView.setAdapter(new NoteAdapter(allNotes, MainPage.this, this));
        if (allNotes.size() == 0) {
            String text;
            if (isFromBin == 1){
                text = getString(R.string.no_trash_error);
            }else {
                text = getString(R.string.no_note_error);
            }

            backgroundText.setText(
                    Html.fromHtml("<span style=\"color:Red\">"
                            + text
                            + "</span")
            );
            pageTitleCount.setText("");
        }else {
            backgroundText.setText("");
        }
    }
    public void refreshThePage(View view) {
        helper.displayToast(R.string.refreshing,this);
        backgroundText.setText(R.string.loading);
        loadAllNotes();
    }

    @SuppressLint("NonConstantResourceId")
    private void leftDrawerHandler() {
        drawerNav.setNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.all_note:
                    if (isFromBin==0){
                        loadAllNotes();
                    }else {
                        finish();
                    }
                    break;
                case R.id.trash:
                    if (isFromBin==1){
                        loadAllNotes();
                    }else {
                        Intent trash = new Intent(this, MainPage.class);
                        trash.putExtra("isFromBin",1);
                        drawerLayout.closeDrawer(drawerNav);
                        startActivity(trash);
                    }
                    break;
                case R.id.change:
                    openBottomSheet("EDITTEXT",
                            getString(R.string.t_change_pin),
                            "",
                            0);
                    drawerLayout.closeDrawer(drawerNav);
                    break;
                case R.id.forgot:
                    pinForgotten();
                    break;
                case R.id.format:
                    showAlertDialog(getString(R.string.forgot_pin),
                            getString(R.string.alert_msg)+" format your database.",
                            "Red",
                            true);
                    drawerLayout.closeDrawer(drawerNav);
                    break;
            }
            drawerLayout.closeDrawer(drawerNav);
            return true;
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadAllNotes();
    }

    public void addNewNote(View view) {
        startActivity(new Intent(this, Add_Update.class));
    }

    public void leftIconClicked(View view) {
        drawerLayout.openDrawer(drawerNav);
    }

    @Override
    public void onNoteOptionClicked(int note_id) {
        openBottomSheet("NOTE_OPTION",
                "",
                "",
                note_id);
    }

    @Override
    public void refresh(boolean activity) {
        if (activity){
            loadAllNotes();
        }else {
            accessMyData();
        }
    }

    @Override
    public void createNewPin() {
        askForPin();
    }

    @Override
    public void forgotPin() {
        pinForgotten();
    }

    @Override
    public void resendMail(String task) {
        if (helper.isNetworkConnected(this)){
            sendMail(task.equals(getString(R.string.t_clean_db)));
        }else {
            helper.displayToast(R.string.no_internet,this);
        }
    }

    @Override
    public void dismissed() {

    }

    private void pinForgotten() {
        showAlertDialog(getString(R.string.forgot_pin),
                getString(R.string.alert_msg)+" create new pin.",
                "Olive",
                false);
    }

    public void showAlertDialog(String title, String message, String proceed_btn_color, boolean isForFormatting){
        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Material_Light_Dialog_Alert)).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(Html.fromHtml(message));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, Html.fromHtml("<i><span style=\"color:"+proceed_btn_color+"\">Proceed</span></i>"), (dialog, which) -> {
            checkMailAndProceed(isForFormatting);
            alertDialog.dismiss();
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, Html.fromHtml("<span style=\"color:black\">Cancel</span>"), (dialog, which) -> alertDialog.dismiss());

        alertDialog.show();
    }

    private void checkMailAndProceed(boolean isForFormatting) {
        if (myData.getEmail().equals("null")){
            openBottomSheet("EDITTEXT",
                    getString(R.string.t_add_email_only),
                    "",
                    0);
        }else {
            if (helper.isNetworkConnected(this)){
                sendMail(isForFormatting);
            }else {
                helper.displayToast(R.string.no_internet,this);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void sendMail(boolean isForFormatting) {

        helper.showProgressDialog(progressDialog,
                "Send Mail",
                "generating pin ...");

        String GENERATED_PIN = helper.generatePIN();

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getString(R.string.username_of_mail_sender),
                        getString(R.string.password_of_mail_sender));
            }
        });
        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(myData.getEmail()));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(myData.getEmail()));
            String msg;
            if (isForFormatting) {
                message.setSubject("Forgot Pin:- " + GENERATED_PIN);
                msg = "(" + GENERATED_PIN + ") "+getString(R.string.format_msg);
            } else {
                message.setSubject("Clean Database:- " + GENERATED_PIN);
                msg = "(" + GENERATED_PIN + ") "+getString(R.string.forgot_msg);
            }

            if (!myData.getName().equals("null")) {
                msg = "Are you <b>" + myData.getName() + "</b> ?<br>" +getString(R.string.msg_to_name)+ msg;
            } else {
                msg = getString(R.string.msg_to_unknown) + msg;
            }

            progressDialog.setMessage("sending ...");
            message.setContent(msg, "text/html; charset=utf-8");

            new SendMail(message,isForFormatting,GENERATED_PIN).execute();

        } catch (MessagingException e) {
            helper.displayToast(R.string.smth_wrong,MainPage.this);
        }
        return;
    }
    @SuppressLint("StaticFieldLeak")
    private class SendMail extends AsyncTask<Void, Void, Boolean> {

        Message message;
        boolean isForFormatting;
        String generated_pin;

        public SendMail(Message message, boolean isForFormatting, String generated_pin) {
            this.message = message;
            this.isForFormatting = isForFormatting;
            this.generated_pin = generated_pin;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Transport.send(message);
                return true;
            } catch (MessagingException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isSent) {
            if (isSent) {
                if (helper.updateTempPin(generated_pin,MainPage.this)){
                    if (isForFormatting){
                        helper.displayToast(R.string.format_pin_sent,MainPage.this);
                        openBottomSheet("EDITTEXT",
                                getString(R.string.t_clean_db),
                                "",
                                0);
                    }else {
                        helper.displayToast(R.string.forgot_pin_sent,MainPage.this);
                        openBottomSheet("EDITTEXT",
                                getString(R.string.t_forgot_pin),
                                "",
                                0);
                    }
                }else {
                    helper.displayToast(R.string.loading,MainPage.this);
                }
            } else {
                helper.displayToast(R.string.smth_wrong,MainPage.this);
            }
            progressDialog.dismiss();
            super.onPostExecute(isSent);
        }
    }

    public void openBottomSheet(String looking_for, String task, String btn_task, int note_id){
        bottomSheet = new BottomSheet(looking_for,task,btn_task,note_id, this, this,false);
        bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
    }
    @Override
    public void onBackPressed() {
        if (isFromBin==1) super.onBackPressed();
    }
}