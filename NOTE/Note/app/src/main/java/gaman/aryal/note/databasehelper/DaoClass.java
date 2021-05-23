package gaman.aryal.note.databasehelper;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import gaman.aryal.note.databasehelper.model.HistoryModel;
import gaman.aryal.note.databasehelper.model.My_DataModel;
import gaman.aryal.note.databasehelper.model.NoteModel;

@Dao
public interface DaoClass {

    @Insert
    long createNote(NoteModel newNote);

    //Extract all Notes
    @Query("select * from  Note where `Bin`= :isFromBin")
    List<NoteModel> extractAllNotes(int isFromBin);

    //extract data of single Note
    @Query("select * from Note where `ID`= :note_id")
    List<NoteModel> extractContentOfsingleNote(int note_id);

    //move Note
    @Query("update Note SET Bin =:toBin " +
            "where `ID`= :note_id")
    long moveNote(int note_id, int toBin);

    //Change visibility
    @Query("update Note SET Seen =:isSeen " +
            "where `ID`= :note_id")
    long changeVisibility(int note_id, int isSeen);

    //Update Note
    @Query("update Note SET Title= :title," +
            "Content =:content, " +
            "Image =:image, " +
            "Seen =:isSeen, " +
            "Encrypted =:isEncrypted, " +
            "C_status =:c_status, " +
            "I_status =:i_status " +
            "where `ID`= :note_id")

    long updateNote(int note_id,
                    String title, String content, String image,
                    int isSeen, int isEncrypted,
                    int c_status, int i_status);

    //move Note
    @Query("delete from Note where `ID`= :note_id")
    int deleteNote(int note_id);

    //delete Note
    @Query("delete from Note")
    int cleanDatabase();

    //delete Note
    @Query("delete from History")
    int cleanHistory();

    @Insert
    long addHistory(HistoryModel newHistory);

    //Extract all History
    @Query("select * from History where `Note_ID`= :note_id")
    List<HistoryModel> extractAllHistory(int note_id);

    //move Note
    @Query("delete from History where `Note_ID`= :note_id")
    int deleteHistory(int note_id);

    @Insert
    long createMyDummyData(My_DataModel myDataModel);
    //Update Name
    @Query("update My_Data SET Name =:name " +
            "where `ID`==1")
    long setName(String name);

    //Update Email
    @Query("update My_Data SET Email =:email " +
            "where `ID`==1")
    long setEmail(String email);

    //Update Pin
    @Query("update My_Data SET PIN =:pin " +
            "where `ID`==1")
    long setPin(String pin);

    //Update Temp pin
    @Query("update My_Data SET TEMP_PIN =:temp_pin " +
            "where `ID`==1")
    long setTemp_pin(String temp_pin);

    //extract My detail
    @Query("select * from My_Data where `ID`==1")
    List<My_DataModel> extractMyData();
}
