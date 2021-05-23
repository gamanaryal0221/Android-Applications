package gaman.aryal.note.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import gaman.aryal.note.Helper;
import gaman.aryal.note.R;
import gaman.aryal.note.databasehelper.model.NoteModel;
import gaman.aryal.note.single.Single_Note;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NotesViewHolder> {

    private final List<NoteModel> noteList;
    private final Context context;
    private int noteCount;
    private final Helper helper;
    private NoteListener noteListener;

    public NoteAdapter(List<NoteModel> noteList, Context context, NoteListener noteListener) {
        this.noteList = noteList;
        this.context = context;
        this.noteListener = noteListener;
        helper = new Helper();
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_note, parent, false);
        return new NotesViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        holder.setData(noteList.get(position), noteListener);
    }

    @Override
    public int getItemCount() {
        noteCount = noteList.size();
        return noteList.size();
    }

    public class NotesViewHolder extends RecyclerView.ViewHolder {

        TextView pageTitleCount, title, data, adHeadline, adAdvertiser, adBodyText, viewMore;
        ImageView sNotifier, noteOpt, adIcon, closeAd;
        NoteListener noteListener;

        @SuppressLint("SetTextI18n")
        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            pageTitleCount = ((Activity) context).findViewById(R.id.pageTitleCount);
            pageTitleCount.setText("("+noteCount+")");

            title = itemView.findViewById(R.id.sTitle);
            data = itemView.findViewById(R.id.sData);
            sNotifier = itemView.findViewById(R.id.sNotifier);
            noteOpt = itemView.findViewById(R.id.sOpt);
        }

        @SuppressLint({"SetTextI18n", "ResourceAsColor"})
        public void setData(final NoteModel eachNote, NoteListener noteListener) {
            this.noteListener = noteListener;

            if (eachNote.getIsEncrypted() == 1) {
                if (eachNote.getIsSeen() == 1) {
                    sNotifier.setImageResource(R.drawable.show);
                } else {
                    sNotifier.setImageResource(R.drawable.hide);
                }
            } else {
                sNotifier.setImageResource(R.drawable.original);
            }

            title.setText(helper.makeTextShort(eachNote.getTitle(), 40));
            data.setText(
                    Html.fromHtml(
                            helper.retrieveFrontDetail(eachNote.getC_status(), eachNote.getI_status())
                    )
            );
            noteOpt.setOnClickListener(v -> noteListener.onNoteOptionClicked(eachNote.getID()));

            itemView.setOnClickListener(v -> {
                Intent openSingleNote = new Intent(context, Single_Note.class);
                openSingleNote.putExtra("NOTE_ID", eachNote.getID());
                context.startActivity(openSingleNote);
            });
        }
    }
    public interface NoteListener{
        void onNoteOptionClicked(int note_id);
    }
}
