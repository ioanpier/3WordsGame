package grioanpier.auth.users.movies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import grioanpier.auth.users.movies.utility.SplitView;


public class Home extends ActionBarActivity {

    private Button mHalves;
    private Button mMaximizePrimaryContent;
    private Button mMaximizeSecondaryContent;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test23);

        //mMaximizePrimaryContent = (Button)findViewById(R.id.maximize_primary);
        //mMaximizePrimaryContent.setOnClickListener( new OnClickListener() {
        //    @Override public void onClick(View v) {
        //        ((SplitView)findViewById(R.id.split_view)).maximizePrimaryContent();
        //    }
//
        //});
//
        //mMaximizeSecondaryContent = (Button)findViewById(R.id.maximize_secondary);
        //mMaximizeSecondaryContent.setOnClickListener( new OnClickListener() {
        //    @Override public void onClick(View v) {
        //        ((SplitView)findViewById(R.id.split_view)).maximizeSecondaryContent();
        //    }
//
        //});
//
        //mHalves = (Button)findViewById(R.id.halves);
        //mHalves.setOnClickListener(new OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        ((SplitView) findViewById(R.id.split_view)).setPrimaryContentSize(200);
        //    }
//
        //});

        ((SplitView)findViewById(R.id.split_view)).maximizePrimaryContent();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
