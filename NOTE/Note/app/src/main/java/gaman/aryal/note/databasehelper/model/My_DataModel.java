package gaman.aryal.note.databasehelper.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "My_Data")
public class My_DataModel {

    //Primary key
    @PrimaryKey(autoGenerate = false)
    @NonNull
    private int ID;

    @ColumnInfo(name = "Name")
    private String Name;

    @ColumnInfo(name = "Email")
    private String Email;

    @ColumnInfo(name = "PIN")
    private String PIN;

    @ColumnInfo(name = "TEMP_PIN")
    private String TEMP_PIN;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPIN() {
        return PIN;
    }

    public void setPIN(String PIN) {
        this.PIN = PIN;
    }

    public String getTEMP_PIN() {
        return TEMP_PIN;
    }

    public void setTEMP_PIN(String TEMP_PIN) {
        this.TEMP_PIN = TEMP_PIN;
    }
}
