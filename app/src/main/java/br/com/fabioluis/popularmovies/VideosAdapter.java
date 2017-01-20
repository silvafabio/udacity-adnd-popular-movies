package br.com.fabioluis.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.fabioluis.popularmovies.model.Video;

/**
 * Created by silva on 16/01/2017.
 */

public class VideosAdapter extends ArrayAdapter<Video> {


    public VideosAdapter(Context context, List<Video> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Video video = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.videos_list, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.video_name);
        name.setText(video.getName());

        return convertView;
    }
}
