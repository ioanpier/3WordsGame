package grioanpier.auth.users.movies;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Ioannis on 8/3/2015.
 */
public class RowAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final java.util.ArrayList<String> movieNames;
    private final ArrayList<String> movieDescriptions;
    private final ArrayList<Drawable> movieImages;





    public RowAdapter(Context context, Collection<String> movieNames, Collection<String> movieDescriptions, Collection<Drawable> movieImages){
        //TODO see if super() makes any difference
        super(context, R.layout.rowlayout);

        this.context = context;
        this.movieNames = (ArrayList)java.util.Arrays.asList(movieNames);
        this.movieDescriptions = (ArrayList)java.util.Arrays.asList(movieDescriptions);
        this.movieImages = (ArrayList)java.util.Arrays.asList(movieImages);


    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
        TextView movieName = (TextView) rowView.findViewById(R.id.movieName);
        EditText movieDescription = (EditText) rowView.findViewById(R.id.movieDescription);
        ImageView movieImage = (ImageView) rowView.findViewById(R.id.movieImage);

        movieName.setText(movieNames.get(position));
        movieDescription.setText(movieDescriptions.get(position));
        movieImage.setImageDrawable(movieImages.get(position));

        return rowView;
    }
}
