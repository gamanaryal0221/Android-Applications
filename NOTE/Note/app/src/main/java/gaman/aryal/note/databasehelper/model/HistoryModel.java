package gaman.aryal.note.databasehelper.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "History")
public class HistoryModel {

    //Primary key
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int ID;

    @ColumnInfo(name = "Note_ID")
    private int Note_ID;

    // nc = note created
    // da = data added
    // ia = image added
    // tm = title modified
    // dm = data modified
    // im = image modified
    // dd = data deleted
    // id = image deleted
    // nd = note deleted
    // nr = note restored
    // ns = seen
    // us = unseen
    @ColumnInfo(name = "Task")
    private String Task;

    @ColumnInfo(name = "Date")
    private String Date;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getNote_ID() {
        return Note_ID;
    }

    public void setNote_ID(int note_ID) {
        Note_ID = note_ID;
    }

    public String getTask() {
        return Task;
    }

    public void setTask(String task) {
        Task = task;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
