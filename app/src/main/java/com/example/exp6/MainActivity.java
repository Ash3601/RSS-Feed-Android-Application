package com.example.exp6;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private List headlines;
    private List links;
    private ProgressDialog nDialog;
    private String platform = "";
    Map<String, String> rssLinks = new HashMap<>();
    DatabaseHelper myDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDb = new DatabaseHelper(this);
        if (isNetworkConnected())
            new MyAsyncTask("https://news.google.com/news/rss").execute();
        else {
            showMessage("Error", "Internet not available.");
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException ie) {
                Log.i(ie + "", "Interrupted Exception");
            } catch (Exception e) {
                Log.i(e + "", "Exception");
            }
        }

        listView = findViewById(R.id.list_view);
        rssLinks.put("move_review","http://www.rediff.com/rss/moviesreviewsrss.xml");
        rssLinks.put("rss_review","http://www.cinemablend.com/rss_review.php");
        rssLinks.put("gnews", "https://news.google.com/news/rss");
        rssLinks.put("programming","https://codingconnect.net/feed");
    }

    // Network Connection Test
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    // Show Spinner
    public void showSpinner() {
        nDialog = new ProgressDialog(MainActivity.this);
        nDialog.setMessage("Loading..");
        nDialog.setTitle("Loading Feed");
        nDialog.setIndeterminate(false);
        nDialog.setCancelable(false);
        nDialog.show();
    }


    // To display the box
    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Closed",
                        Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });
        builder.show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rss_feed_menu, menu);
        return true;
    }

    public InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()) {
            case R.id.gnews:
                platform = "Google";
                if (isNetworkConnected()) {
                    new MyAsyncTask("https://news.google.com/news/rss").execute();
//                    boolean isInserted = myDb.insertData("testheadline" + Math.random(), "testlink", "google");
//                    if (isInserted) {
//                        Toast.makeText(this, "Data Inserted No worried", Toast.LENGTH_LONG);
//                        Log.i("Data inserted", "Database");
//                    } else {
//                        Toast.makeText(this, "Data not Inserted worried", Toast.LENGTH_LONG);
//                        Log.i("Data not inserted", "Database");

//                    }
                } else {
                    showMessage("Error", "Internet not available.");
                }
                break;

            case R.id.programming:
                platform = "Programming";
                if (isNetworkConnected()) {
                    new MyAsyncTask("https://codingconnect.net/feed").execute();
                myDb.insertData("Test2", "test", platform);
                boolean isPresent = myDb.checkIsDataAlreadyInDBorNot("Test2");
                if (isPresent)
                    Log.i("Data present", "checkDataPre");
                else {
                    Log.i("Data not present", "checkDataPre");

                }
        }
//                    Cursor res = myDb.getAllData();
//                if (res.getCount() == 0) {
//                    // no data
//                    showMessage("Error", "No data found!");
//                    return false;
//                }
//                StringBuffer buffer = new StringBuffer();
//                while (res.moveToNext()) {
//                    buffer.append("Id: " + res.getString(0) + "\n" + " First Name: " + res.getString(1) + "\n" + " Last Name: " + res.getString(2) + "\n" + " Marks: " + res.getString(3) + "\n");
//                }
//
//                // Show all the data
//                showMessage("Data", buffer.toString());
//
                else {
                    showMessage("Error", "Internet not available.");
                }

                break;
            case R.id.menu_bookmarks:
                Log.i("Showing Bookmarks", "bookmarkstest");
                Intent bookmarkActivityIntent = new Intent(MainActivity.this, Bookmarks.class);
                startActivity(bookmarkActivityIntent);
                break;

            case R.id.menu_exit:
                Toast.makeText(this, "Exiting", Toast.LENGTH_LONG);
                finish();
        }
        return true;
    }

    class MyAsyncTask extends AsyncTask<Object, Void, ArrayAdapter> {

        String urlEntered;
        String urlName = null;

        // Constructor
        public MyAsyncTask(String url) {
            this.urlEntered = url;
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            showSpinner();
        }


        @Override
        protected ArrayAdapter doInBackground(Object[] params) {
            headlines = new ArrayList();
            links = new ArrayList();

            try {
                URL url = new URL(urlEntered);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();

                // We will get the XML from an input stream
                xpp.setInput(getInputStream(url), "UTF_8");
                boolean insideItem = false;

                // Returns the type of current event: START_TAG, END_TAG, etc..
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                        } else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (insideItem)
                                headlines.add(xpp.nextText()); //extract the headline
                        } else if (xpp.getName().equalsIgnoreCase("link")) {
                            if (insideItem)
                                links.add(xpp.nextText()); //extract the link of article
                        }
                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = false;
                    }
                    eventType = xpp.next(); //move to next element
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.i("url Parser", "offline");
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                Log.i("url Parser 2", "offline");

            } catch (IOException e) {
                e.printStackTrace();
                Log.i("url Parser 3", "offline");

            }
            return null;
        }

        protected void onPostExecute(ArrayAdapter adapter) {
            populateListView();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            nDialog.dismiss();
        }
    }

    private void populateListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, headlines);
        listView.setAdapter(adapter);

        // on long press
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String headline = (String)headlines.get(i);
                String link = (String) links.get(i);
                boolean isDataPresent = myDb.checkIsDataAlreadyInDBorNot(headline);
                if (isDataPresent) {
                    Toast.makeText(MainActivity.this, "Already bookmarked", Toast.LENGTH_SHORT);
                    return false;
                }
                boolean isInserted = myDb.insertData(headline, link, platform);
                Log.i("On item long clicked", "clickCheck");
                return isInserted;

            }
        });

        // on press
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, WebPageActivity.class);
                Uri uri = Uri.parse((links.get(position)).toString());
                intent.setData(uri);
                startActivity(intent);
            }
        });
    }


}