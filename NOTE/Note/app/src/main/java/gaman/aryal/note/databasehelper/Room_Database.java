package gaman.aryal.note.databasehelper;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import gaman.aryal.note.Helper;
import gaman.aryal.note.R;
import gaman.aryal.note.databasehelper.model.HistoryModel;
import gaman.aryal.note.databasehelper.model.My_DataModel;
import gaman.aryal.note.databasehelper.model.NoteModel;

@androidx.room.Database(
        entities = {
                NoteModel.class,
                HistoryModel.class,
                My_DataModel.class
        },
        version = 1)
public abstract class Room_Database extends RoomDatabase {

    private static Room_Database instance;

    public abstract DaoClass getAccess();

    public static Room_Database getInstance(final Context context) {
        if (instance == null) {
            synchronized (Room_Database.class) {
                instance = androidx.room.Room.databaseBuilder(context, Room_Database.class, "NOTE")
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .addCallback(roomCallBack)
                        .build();
            }
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallBack = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new CreateDummyData(instance).execute();
        }
    };

    public static class CreateDummyData extends AsyncTask<Void,Void,Void>{

        private DaoClass daoClass;
        public CreateDummyData(Room_Database db) {
            this.daoClass = db.getAccess();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            My_DataModel my_dataModel = new My_DataModel();
            my_dataModel.setID(1);
            my_dataModel.setName("null");
            my_dataModel.setEmail("null");
            my_dataModel.setPIN("null");
            my_dataModel.setTEMP_PIN("null");

            daoClass.createMyDummyData(my_dataModel);
            return null;
        }

    }
}
