package gaman.aryal.note.databasehelper.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Note")
public class NoteModel {

    //Primary key
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int ID;

    @ColumnInfo(name = "Title")
    private String Title;

    @ColumnInfo(name = "Content")
    private String Content;

    @ColumnInfo(name = "Image")
    private String Image;

    // 1 = seen
    // 0 = not seen
    @ColumnInfo(name = "Seen")
    private int isSeen;

    // 1 = encrypted
    // 0 = not encrypted
    @ColumnInfo(name = "Encrypted")
    private int isEncrypted;

    @ColumnInfo(name = "C_status")
    private int C_status;

    @ColumnInfo(name = "I_status")
    private int I_status;

    // 1 = yes
    // 0 = no
    @ColumnInfo(name = "Bin")
    private int isInBin;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public int getIsSeen() {
        return isSeen;
    }

    public void setIsSeen(int isSeen) {
        this.isSeen = isSeen;
    }

    public int getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(int isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public int getC_status() {
        return C_status;
    }

    public void setC_status(int c_status) {
        C_status = c_status;
    }

    public int getI_status() {
        return I_status;
    }

    public void setI_status(int i_status) {
        I_status = i_status;
    }

    public int getIsInBin() {
        return isInBin;
    }

    public void setIsInBin(int isInBin) {
        this.isInBin = isInBin;
    }
}

