package br.com.fabioluis.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.fabioluis.popularmovies.model.Review;

/**
 * Created by silva on 16/01/2017.
 */

public class ReviewsAdapter extends ArrayAdapter<Review> {


    public ReviewsAdapter(Context context, List<Review> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Review review = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.reviews_list, parent, false);
        }

        TextView author = (TextView) convertView.findViewById(R.id.review_author);
        TextView content = (TextView) convertView.findViewById(R.id.review_content);

        author.setText(review.getAuthor());
        content.setText(review.getContent());

        return convertView;
    }
}
