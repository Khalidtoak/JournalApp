package com.gram.meme.journalapp.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gram.meme.journalapp.JournalPojos.JournalPojo;
import com.gram.meme.journalapp.R;

import java.util.List;

public class JournalAdapter extends ArrayAdapter<JournalPojo> {
    public JournalAdapter(Context context, int resource, List<JournalPojo> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)  {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.journal_list_layout, parent, false);
        }

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.JournalImage);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.JournalContent);
        TextView date = (TextView) convertView.findViewById(R.id.journal_date);
        TextView textView = convertView.findViewById(R.id.userName);

        JournalPojo message = getItem(position);
            Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);
            messageTextView.setText(message.getJournalContent());
           date.setText(message.getTimeOfPost());
           textView.setText(message.getName());

        return convertView;
    }
}