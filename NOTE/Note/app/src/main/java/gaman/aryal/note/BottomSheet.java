package gaman.aryal.note;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gaman.aryal.note.adapter.HistoryAdapter;
import gaman.aryal.note.add_update.Add_Update;
import gaman.aryal.note.databasehelper.Room_Database;
import gaman.aryal.note.databasehelper.model.HistoryModel;
import gaman.aryal.note.databasehelper.model.My_DataModel;
import gaman.aryal.note.databasehelper.model.NoteModel;

public class BottomSheet extends BottomSheetDialogFragment implements View.OnClickListener, TextWatcher {


    String LOOKING_FOR, TASK, BUTTON_TASK;
    int NOTE_ID;
    Activity activity;

    RelativeLayout history;
    LinearLayout noteOption, editText, blue, green, red;
    TextView purpose, errorMsg, blueText, greenText, redText, rvbackgroundText, editTextHelper, refresh;
    ImageView dismiss, blueIcon, greenIcon, redIcon;
    NoteModel currentNote;

    RecyclerView historyRecyclerView;
    List<HistoryModel> allHistory;

    EditText firstEditText, secondEditText, thirdEditText;
    Button allInOneBtn;

    Helper helper;
    My_DataModel myData;

    ProgressDialog progressDialog;
    BottomSheetListener bottomSheetListener;

    InputMethodManager imm;
    View view;

    boolean isFromInside;

    public BottomSheet(String LOOKING_FOR, String TASK, String BUTTON_TASK, int NOTE_ID, Activity activity, BottomSheetListener bottomSheetListener, boolean isFromInside) {
        this.LOOKING_FOR = LOOKING_FOR;
        this.TASK = TASK;
        this.BUTTON_TASK = BUTTON_TASK;
        this.NOTE_ID = NOTE_ID;
        this.activity = activity;
        this.bottomSheetListener = bottomSheetListener;
        this.isFromInside = isFromInside;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        view = v = inflater.inflate(R.layout.bottom_sheet, container, false);

        helper = new Helper();
        progressDialog = new ProgressDialog(activity);
        imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        initMain(v);
        switch (LOOKING_FOR) {
            case "NOTE_OPTION":
                currentNote = Room_Database.getInstance(activity)
                        .getAccess()
                        .extractContentOfsingleNote(NOTE_ID)
                        .get(0);
                initNoteOption(v);
                extractMyData();
                break;
            case "HISTORY":
                allHistory = new ArrayList<>();
                initHistory(v);
                break;
            case "EDITTEXT":
                extractMyData();
                initEdittext(v);
                break;
        }

        return v;
    }

    private void extractMyData() {
        myData = Room_Database.getInstance(activity)
                .getAccess()
                .extractMyData()
                .get(0);

        if (myData.getPIN().isEmpty()){
            helper.displayToast(R.string.p_add_pin,activity);
        }
    }

    private void initMain(View v) {
        dismiss = v.findViewById(R.id.dismiss);

        history = v.findViewById(R.id.history);
        noteOption = v.findViewById(R.id.noteOption);
        editText = v.findViewById(R.id.editText);

        dismiss.setOnClickListener(this);
    }

    private void initNoteOption(View v) {

        blue = v.findViewById(R.id.blue);
        blueIcon = v.findViewById(R.id.blueIcon);
        blueText = v.findViewById(R.id.blueText);

        green = v.findViewById(R.id.green);
        greenIcon = v.findViewById(R.id.greenIcon);
        greenText = v.findViewById(R.id.greenText);

        red = v.findViewById(R.id.red);
        redIcon = v.findViewById(R.id.redIcon);
        redText = v.findViewById(R.id.redText);

        blue.setOnClickListener(this);
        green.setOnClickListener(this);
        red.setOnClickListener(this);

        noteOption.setVisibility(View.VISIBLE);
        designOptions();
    }
    private void designOptions() {

        if (currentNote.getIsInBin() == 0) {

            if (currentNote.getIsEncrypted() == 1) {
                if (currentNote.getIsSeen() == 1) {
                    greenIcon.setImageResource(R.drawable.hide);
                    greenText.setText(R.string.encrypt);

                } else {
                    greenIcon.setImageResource(R.drawable.show);
                    greenText.setText(R.string.decrypt);
                }
                green.setVisibility(View.VISIBLE);
            }

            blueIcon.setImageResource(R.drawable.edit);
            blueText.setText(R.string.edit);
            blue.setVisibility(View.VISIBLE);

            redIcon.setImageResource(R.drawable.delete);
            redText.setText(R.string.move_to_trash);

        } else {

            greenIcon.setImageResource(R.drawable.restore);
            greenText.setText(R.string.restore);
            green.setVisibility(View.VISIBLE);

            redIcon.setImageResource(R.drawable.delete);
            redText.setText(R.string.delete_permanently);
        }
        red.setVisibility(View.VISIBLE);
    }

    private void initHistory(View v) {

        historyRecyclerView = v.findViewById(R.id.historyRecyclerView);
        rvbackgroundText = v.findViewById(R.id.rvbackgroundText);
        refresh = v.findViewById(R.id.refresh);

        refresh.setOnClickListener(this);

        history.setVisibility(View.VISIBLE);
        loadHistory();
    }
    private void loadHistory() {
        allHistory.clear();
        rvbackgroundText.setText(R.string.loading);
        allHistory = Room_Database.getInstance(activity)
                .getAccess()
                .extractAllHistory(NOTE_ID);
        Collections.reverse(allHistory);

        historyRecyclerView.setAdapter(new HistoryAdapter(allHistory, activity));
        rvbackgroundText.setText("");
    }

    private void initEdittext(View v) {

        purpose = v.findViewById(R.id.purpose);
        errorMsg = v.findViewById(R.id.errorMsg);

        editTextHelper = v.findViewById(R.id.editTextHelper);

        firstEditText = v.findViewById(R.id.firstEditText);
        secondEditText = v.findViewById(R.id.secondEditText);
        thirdEditText = v.findViewById(R.id.thirdEditText);

        allInOneBtn = v.findViewById(R.id.allInOneBtn);

        firstEditText.addTextChangedListener(this);
        secondEditText.addTextChangedListener(this);
        thirdEditText.addTextChangedListener(this);

        editTextHelper.setOnClickListener(this);
        allInOneBtn.setOnClickListener(this);

        editText.setVisibility(View.VISIBLE);

        designEditTexts();
    }
    private void designEditTexts() {
        if (getString(R.string.t_add_pin).equals(TASK)) {
            finalDesign(R.string.p_add_pin,
                    false,
                    R.string.create_pin,
                    R.string.confirm_pin,
                    0,
                    R.string.save);
            inputTypeForPin();
        }else if (getString(R.string.t_change_pin).equals(TASK)){
            finalDesign(R.string.p_change_pin,
                    true,
                    R.string.current_pin,
                    R.string.create_pin,
                    R.string.confirm_pin,
                    R.string.update);
            inputTypeForPin();
            thirdEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }else if (getString(R.string.t_forgot_pin).equals(TASK)){
            finalDesign(R.string.p_change_pin,
                    true,
                    R.string.master_pin,
                    R.string.create_pin,
                    R.string.confirm_pin,
                    R.string.save);
            inputTypeForPin();
            thirdEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        }else if (getString(R.string.t_add_name_email).equals(TASK)){
            finalDesign(R.string.p_add_name_email,
                    false,
                    R.string.your_name,
                    R.string.valid_email,
                    0,
                    R.string.save);
            inputTypeForNameEmail();
        }else if (getString(R.string.t_add_email_only).equals(TASK)){
            finalDesign(R.string.p_no_email_to_proceed,
                    false,
                    0,
                    R.string.valid_email,
                    0,
                    R.string.save);
            inputTypeForNameEmail();
        }else if (getString(R.string.t_change_name_email).equals(TASK)){
            finalDesign(R.string.p_change_name_email,
                    true,
                    R.string.your_name,
                    R.string.valid_email,
                    R.string.current_pin,
                    R.string.update);
            inputTypeForNameEmail();
            thirdEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

            if (!myData.getName().equals("null")) firstEditText.setText(myData.getName());
            if (!myData.getEmail().equals("null")) secondEditText.setText(myData.getEmail());

        }else if (getString(R.string.t_check_pin).equals(TASK)){
            finalDesign(R.string.p_verify_to_proceed,
                    true,
                    R.string.enter_pin,
                    0,
                    0,
                    R.string.verify);
            inputTypeForPin();
        }else if (getString(R.string.t_clean_db).equals(TASK)){
            finalDesign(R.string.p_verify_to_proceed,
                    true,
                    R.string.master_pin,
                    0,
                    0,
                    R.string.verify);
            inputTypeForPin();
        }
    }

    private void inputTypeForPin() {
        firstEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        secondEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
    }
    private void inputTypeForNameEmail() {
        firstEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        secondEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    private void finalDesign(int purpose_txt, boolean show_helper, int first_txt, int second_txt, int third_txt, int btn_txt) {
        purpose.setText(purpose_txt);

        if (show_helper){
            if (TASK.equals(getString(R.string.t_clean_db)) || TASK.equals(getString(R.string.t_forgot_pin))){
                editTextHelper.setText(R.string.resend_pin);
            }else {
                if (myData.getPIN().equals("null")){
                    editTextHelper.setText(R.string.no_pin_error);
                }else {
                    if (!isFromInside) editTextHelper.setText(R.string.forgot_pin);
                }
            }

            editTextHelper.setVisibility(View.VISIBLE);
        }

        if (first_txt != 0){
            firstEditText.setHint(first_txt);
            firstEditText.setVisibility(View.VISIBLE);
        }

        if (second_txt != 0){
            secondEditText.setHint(second_txt);
            secondEditText.setVisibility(View.VISIBLE);
        }

        if (third_txt != 0){
            thirdEditText.setHint(third_txt);
            thirdEditText.setVisibility(View.VISIBLE);
        }

        allInOneBtn.setText(btn_txt);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dismiss:
                dismiss();
                break;
            case R.id.red:
                helper.showProgressDialog(progressDialog,
                        "Delete",
                        "initializing ...");
                if (redText.getText().toString().equals(getString(R.string.move_to_trash))){
                    BUTTON_TASK = "MOVE_TO_TRASH";

                }else {
                    BUTTON_TASK = "DELETE_PERMANENTLY";

                }
                makeReadyForEnteringPin();
                break;
            case R.id.green:
                helper.showProgressDialog(progressDialog,
                        "Note",
                        "processing ...");
                if (greenText.getText().toString().equals(getString(R.string.decrypt))){
                    BUTTON_TASK = "DECRYPT";
                    makeReadyForEnteringPin();
                }else if (greenText.getText().toString().equals(getString(R.string.restore))){
                    helper.moveNote(NOTE_ID,
                            0,
                            activity);
                    addHistory("nr+");
                    refreshActivity();
                } else{
                    helper.changeVisibility(0,
                            NOTE_ID,
                            activity);
                    addHistory("us+");
                    refreshActivity();
                }
                break;
            case R.id.blue:
                helper.showProgressDialog(progressDialog,
                        "Edit Note",
                        "initializing ...");
                BUTTON_TASK = "EDIT";
                makeReadyForEnteringPin();
                break;
            case R.id.allInOneBtn:
                handleEditText();
                break;
            case R.id.refresh:
                loadHistory();
                break;
            case R.id.editTextHelper:
                String text = editTextHelper.getText().toString();
                dismiss();
                if (text.equals(getString(R.string.no_pin_error))){
                    bottomSheetListener.createNewPin();
                }else if (text.equals(getString(R.string.forgot_pin))){
                    bottomSheetListener.forgotPin();
                }else if (text.equals(getString(R.string.resend_pin))){
                    bottomSheetListener.resendMail(TASK);
                }
                break;
        }
    }

    private void handleEditText() {
        String first_data = "",
                second_data = "",
                third_data = "";

        if (firstEditText.getVisibility()==View.VISIBLE) first_data = firstEditText.getText().toString().trim();
        if (secondEditText.getVisibility()==View.VISIBLE) second_data = secondEditText.getText().toString().trim();
        if (thirdEditText.getVisibility()==View.VISIBLE) third_data = thirdEditText.getText().toString().trim();

        if (getString(R.string.t_add_pin).equals(TASK)) {

            addPin(first_data,second_data);

        }else if (getString(R.string.t_change_pin).equals(TASK)){

            if (checkPin(first_data,false)) addPin(second_data,third_data);

        }else if (getString(R.string.t_forgot_pin).equals(TASK)){

            if (checkPin(first_data,true)) addPin(second_data,third_data);

        }else if (getString(R.string.t_add_name_email).equals(TASK)){

            addNameAndEmail(first_data,second_data);

        }else if (getString(R.string.t_change_name_email).equals(TASK)){

            if (checkPin(third_data,false)) addNameAndEmail(first_data,second_data);

        }else if (getString(R.string.t_add_email_only).equals(TASK)){

            if (second_data.isEmpty()){
                helper.displayToast(R.string.enter_email,activity);
            }else {
                checkEmailValidityAndSave(second_data,true);
            }

        }else if (getString(R.string.t_check_pin).equals(TASK)){

            if (checkPin(first_data,false)) onPinVerified();

        }else if (getString(R.string.t_clean_db).equals(TASK)){

            if (checkPin(first_data,true)) {
                helper.showProgressDialog(progressDialog,
                        getString(R.string.format),
                        "processing ...");
                cleanDatabase();
            }
        }
    }

    private boolean checkPin(String pin, boolean isTemp) {

        if (pin.isEmpty()){
            errorMsg.setText(R.string.enter_pin);
            return false;
        }else {
            String PIN;
            if (isTemp){
                PIN = myData.getTEMP_PIN();
            }else {
                PIN = myData.getPIN();
            }
            if (PIN.equals(pin)){
                return true;
            }else {
                errorMsg.setText(R.string.invalid_pin);
                return false;
            }
        }
    }

    private void cleanDatabase() {
        if (!String.valueOf(Room_Database.getInstance(activity)
                .getAccess()
                .cleanDatabase()).isEmpty()){

            if (!String.valueOf(Room_Database.getInstance(activity)
                    .getAccess()
                    .cleanHistory()).isEmpty()){

                bottomSheetListener.refresh(true);
                dismiss();
                progressDialog.dismiss();
                helper.displayToast(R.string.done,
                        activity);
            }else {
                progressDialog.dismiss();
                helper.displayToast(R.string.smth_wrong,
                        activity);
            }
        }else {
            progressDialog.dismiss();
            helper.displayToast(R.string.smth_wrong,
                    activity);
        }
    }

    private void addPin(String pin, String confirm_pin) {
        if (!pin.isEmpty()){
            if (pin.length()>=4 && pin.length()<=8){
                if (!confirm_pin.isEmpty()){
                    if (pin.equals(confirm_pin)){
                        dismiss();
                        Room_Database.getInstance(activity)
                                .getAccess()
                                .setPin(confirm_pin);

                        helper.displayToast(R.string.saved,activity);

                    }else {
                        errorMsg.setText(R.string.not_match);
                    }
                }else {
                    errorMsg.setText(R.string.re_write);
                }
            }else {
                errorMsg.setText(R.string.pin_must);
            }
        }else {
            errorMsg.setText(R.string.pin_must_not_empty);
        }
    }
    private void addNameAndEmail(String name, String email) {

        if (name.isEmpty() && email.isEmpty()){
            errorMsg.setText(R.string.name_email_empty);
        }else {
            if (name.isEmpty()){
                checkEmailValidityAndSave(email,true);
            }else if (email.isEmpty()){
                helper.setName(name,activity);
                dismiss();
            }else {
                helper.setName(name,activity);
                checkEmailValidityAndSave(email,false);
            }
            helper.displayToast(R.string.done,activity);
        }
        return;
    }

    private void checkEmailValidityAndSave(String email, boolean onlyEmail) {
        if (helper.isEmailValid(email)){
            helper.setEmail(email,activity);
            helper.displayToast(R.string.saved,activity);
            dismiss();
        }else {
            if (onlyEmail){
                errorMsg.setText(R.string.enter_valid_email);
            }else {
                dismiss();
                helper.displayToast(R.string.email_was_invalid,activity);
            }
        }
    }

    private void makeReadyForEnteringPin() {
        TASK = getString(R.string.t_check_pin);
        initEdittext(view);
        noteOption.setVisibility(View.GONE);
        progressDialog.dismiss();
    }

    private void onPinVerified() {
        switch (BUTTON_TASK){
            case "MOVE_TO_TRASH":

                    helper.moveNote(NOTE_ID,
                            1,
                            activity);
                    addHistory("nd+");
                refreshActivity();
                break;
            case "DELETE_PERMANENTLY":

                    helper.deleteNote(NOTE_ID,
                            activity);
                refreshActivity();
                break;
            case "DECRYPT":
                helper.changeVisibility(1,
                        NOTE_ID,
                        activity);
                addHistory("ns+");
                refreshActivity();
                break;
            case "EDIT":
                dismiss();
                Intent openSingleNote = new Intent(activity, Add_Update.class);
                openSingleNote.putExtra("NOTE_ID", NOTE_ID);
                activity.startActivity(openSingleNote);
                break;
        }
    }

    private void addHistory(String task) {
        helper.updateHistory(NOTE_ID,
                task,
                activity);
    }

    private void refreshActivity() {
        dismiss();
        bottomSheetListener.refresh(true);
        helper.displayToast(R.string.done,
                activity);
        progressDialog.dismiss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (TASK.equals(getString(R.string.t_forgot_pin)) || TASK.equals(getString(R.string.t_clean_db))){
            helper.updateTempPin("null",activity);
        }

        if (noteOption.getVisibility()==View.VISIBLE){
            noteOption.setVisibility(View.GONE);
            if (blue.getVisibility()==View.VISIBLE){
                blue.setVisibility(View.GONE);
                blueIcon.setImageBitmap(null);
                blueText.setText("");
            }
            if (green.getVisibility()==View.VISIBLE){
                green.setVisibility(View.GONE);
                greenIcon.setImageBitmap(null);
                greenText.setText("");
            }
            if (red.getVisibility()==View.VISIBLE){
                red.setVisibility(View.GONE);
                redIcon.setImageBitmap(null);
                redText.setText("");
            }
        }else if (editText.getVisibility()==View.VISIBLE){
            bottomSheetListener.refresh(false);
            purpose.setText("");
            errorMsg.setText("");
            editTextHelper.setText("");
            editTextHelper.setVisibility(View.GONE);

            if (firstEditText.getVisibility()==View.VISIBLE){
                firstEditText.setHint("");
                firstEditText.setText("");
            }
            if (secondEditText.getVisibility()==View.VISIBLE){
                secondEditText.setHint("");
                secondEditText.setText("");
            }
            if (thirdEditText.getVisibility()==View.VISIBLE){
                thirdEditText.setHint("");
                thirdEditText.setText("");
            }
            allInOneBtn.setText("");
            helper.closeKeyboard(imm,
                    activity);
        }else {
            history.setVisibility(View.GONE);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        errorMsg.setText("");
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public interface BottomSheetListener {
        void refresh(boolean activity);
        void createNewPin();
        void forgotPin();
        void resendMail(String task);
        void dismissed();
    }
}
