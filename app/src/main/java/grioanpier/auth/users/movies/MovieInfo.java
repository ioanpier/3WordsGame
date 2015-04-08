package grioanpier.auth.users.movies;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

/**
 * Created by Ioannis on 8/3/2015.
 */
public class MovieInfo {
    private final String name;
    private final String description;
    private Drawable image;

    public MovieInfo(String tName, String tDescription, String path){
        name=tName;
        description=tDescription;
        //image=new DownloadImageTask().execute(path);
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Drawable> {

        private final String LOG_TAG = DownloadImageTask.class.getSimpleName();

        public DownloadImageTask() {
           super();
        }

        protected Drawable doInBackground(String... urls) {
            String urldisplay = urls[0];
            Drawable drawable = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                drawable = Drawable.createFromStream(in, "stream");
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return drawable;
        }

        protected void onPostExecute(Drawable result) {

        }
    }

}
