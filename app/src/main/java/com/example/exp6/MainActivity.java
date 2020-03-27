package com.example.exp6;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private static ProgressDialog pleaseWaitDialog;
    private String gNews = "https://news.google.com/news/rss";
    private List headlines;
    private List links;
    private List oHeadlines;
    private List oLinks;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    //    private List oHeadlines;
//    private List oLinks;
    private ProgressDialog nDialog = null;
    private String platform = "News";
    private EditText mainActivityEditText;
    private String[] urls = {"https://geeksforgeeks.org/feed", "https://www.thecrazyprogrammer.com/feed", "http://www.codingalpha.com/feed", "https://medium.com/feed/better-programming", "https://news.google.com/news?cf=all&hl=en&pz=1&ned=us&q=software+engineering&output=rss", "https://codingconnect.net/feed",};

    DatabaseHelper myDb;
    ArrayAdapter<String> adapter;
    boolean doneLoading = false;
    private static int urlCount = 0;

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
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        listView = findViewById(R.id.list_view);

        // Interface to allow swipe to refresh
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("swipeRefresh", "onRefresh called from SwipeRefreshLayout");
//                        mSwipeRefreshLayout.set
                        mSwipeRefreshLayout.setRefreshing(false);
                        if (platform.equals("News")) {
                            new MyAsyncTask("https://news.google.com/news/rss").execute();
                            Toast.makeText(MainActivity.this, "News Feed Refreshed!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            if (urlCount < 0)
                                urlCount += 1;
                            new MyAsyncTask(urls[urlCount - 1]).execute();
                            Toast.makeText(MainActivity.this, "Feed Refreshed!",
                                    Toast.LENGTH_SHORT).show();

                        }

//                        finish();
//                        startActivity(getIntent());
                        if (nDialog != null) {
                            nDialog.dismiss();
                        }
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
//                        myUpdateOperation();
                    }
                }
        );
//        addItemsOnRefresh();

        // setting add text listener on edit text
        mainActivityEditText = findViewById(R.id.main_activity_edit_text_filter);
        mainActivityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i0, int i1, int i2) {
                String input = mainActivityEditText.getText().toString();
                if (input.equals("") == true || input == null) {
                    headlines.clear();
                    links.clear();
                    headlines = oHeadlines;
                    links = oLinks;
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                List<String> tmpList = new ArrayList<>();
                List<String> tmpList2 = new ArrayList<>();
                String input = mainActivityEditText.getText().toString();
                for (int i = 0; i < headlines.size(); i++) {
                    String temp = (String) headlines.get(i);
                    String temp2 = (String) links.get(i);
                    if (temp.toLowerCase().contains(input.toLowerCase())) {
                        tmpList.add(temp);
                        tmpList2.add(temp2);
                    }
                }
//                if (input.equals("") == true || input == null) {
//                    headlines.clear();
//                    links.clear();
//                    overridePendingTransition(0, 0);
//                    startActivity(getIntent());
//                    overridePendingTransition(0, 0);
//                    Log.i("inputNull", "Input : " + input);
//                    listView.setAdapter(adapter);
//                    adapter.notifyDataSetChanged();
//                    return;
//                }
                headlines = tmpList;
                links = tmpList2;

                ArrayAdapter<String> myAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, tmpList);
                listView.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();


            }
        });

    }


//.setOnRefreshListener(
//        new SwipeRefreshLayout.OnRefreshListener() {
//        @Override
//        public void onRefresh() {
//            Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
//
//            // This method performs the actual data-refresh operation.
//            // The method calls setRefreshing(false) when it's finished.
//            myUpdateOperation();
//        }
//    }
//);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == MainActivity.RESULT_OK) {
                String result = data.getStringExtra("isDeletionOccured");
                if (result.equals("true")) {
                    // An item is deleted
                    finish();
                    startActivity(getIntent());
                }
                Log.i("activityRes", result);
            }
            if (resultCode == MainActivity.RESULT_CANCELED) {
                //Write your code if there's no result
                Log.i("activityRes2", "Result Cancelled");
            }
        }
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
//        return nDialog;
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

    //    @Override
//    public void onRestart() {
//        super.onRestart();
//        finish();
//        startActivity(getIntent());
//        //When BACK BUTTON is pressed, the activity on the stack is restarted
//        //Do what you want on the refresh procedure here
//    }
    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//        if (nDialog != null)
//            nDialog.dismiss();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//        if (nDialog != null)
//            nDialog.dismiss();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()) {
            case R.id.gnews:
                platform = "News";
                if (isNetworkConnected()) {
                    new MyAsyncTask("https://news.google.com/news/rss").execute();
                } else {
                    showMessage("Error", "Internet not available.");
                }
                break;

            case R.id.programming:
                platform = "Programming";
                if (isNetworkConnected()) {
                    Toast.makeText(this, "Loading...\n" + urls[urlCount], Toast.LENGTH_LONG).show();
                    new MyAsyncTask(urls[urlCount]).execute();
                    urlCount++;
                    if (urlCount == urls.length) {
                        urlCount = 0;
                    }
                } else {
                    showMessage("Error", "Internet not available.");
                }

                break;
            case R.id.menu_bookmarks:
                Log.i("Showing Bookmarks", "bookmarkstest");
                Intent bookmarkActivityIntent = new Intent(MainActivity.this, Bookmarks.class);
//                startActivity(bookmarkActivityIntent);
//                Intent i = new Intent(this, SecondActivity.class);
                startActivityForResult(bookmarkActivityIntent, 1);
                break;

            case R.id.menu_exit:
                Toast.makeText(this, "Exiting", Toast.LENGTH_LONG);
                finish();
        }
        return true;
    }

    class MyAsyncTask extends AsyncTask<Object, Void, ArrayAdapter> {

        String urlEntered;

        // Constructor
        public MyAsyncTask(String url) {
            this.urlEntered = url;
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//            if (nDialog != null) {
//                nDialog.dismiss();
//            }
            showSpinner();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected ArrayAdapter doInBackground(Object[] params) {
//            if (nDialog != null) {
//                nDialog.dismiss();
//            }
            headlines = new ArrayList();
            links = new ArrayList();
            oHeadlines = new ArrayList();
            oLinks = new ArrayList();
            try {
                URL url = new URL(urlEntered);
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                try {
                // We will get the XML from an input stream
                    xpp.setInput(getInputStream(url), "UTF_8");
                } catch (IllegalArgumentException iae) {
                    Log.i(iae + "", "Illegal Argument Exception");
                }
                boolean insideItem = false;

                // Returns the type of current event: START_TAG, END_TAG, etc..
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                        } else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (insideItem) {
                                String headline = xpp.nextText();
                                boolean isDataPresent = myDb.checkIsDataAlreadyInDBorNot(headline);
                                // ✯
                                if (isDataPresent) {
                                    headline = "✯ " + headline;
                                    System.out.println("Current headline " + headline);
                                }
                                headlines.add(headline); //extract the headline

                                oHeadlines.add(headline);

                            }
                        } else if (xpp.getName().equalsIgnoreCase("link")) {
                            if (insideItem) {
                                String link = xpp.nextText();
                                links.add(link); //extract the link of article
                                oLinks.add(link);
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = false;
                    }
                    eventType = xpp.next(); //move to next element
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.i("url Parser", "offline");
                Toast.makeText(MainActivity.this, "Offline",
                        Toast.LENGTH_SHORT).show();
            } catch (XmlPullParserException e) {
                if (nDialog != null) {
                    nDialog.dismiss();
                }
                e.printStackTrace();
                Log.i("url Parser 2", "offline");

            } catch (IOException e) {
                e.printStackTrace();
                Log.i("url Parser 3", "offline");
                Toast.makeText(MainActivity.this, "Offline",
                        Toast.LENGTH_SHORT).show();

            }
            return null;
        }

        protected void onPostExecute(ArrayAdapter adapter) {
            populateListView();
            doneLoading = true;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            nDialog.dismiss();
            if (headlines.size() == 0) {
                Toast.makeText(MainActivity.this, "Feed is not unavailable/offline",
                        Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void populateListView() {
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, headlines);
        listView.setAdapter(adapter);

        // on long press
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String headline = (String) headlines.get(i);
                String link = (String) links.get(i);
                boolean isInserted;
                if (headline.contains("✯ ")) {
                    headline = headline.substring(2);
                }
                boolean isDataPresent = myDb.checkIsDataAlreadyInDBorNot(headline);
                if (isDataPresent) {
                    // remove from the database
                    boolean isDeleted = myDb.deleteData(headline);
                    if (isDeleted) {
                        headlines.set(i, headline);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, "Bookmark Removed", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                isInserted = myDb.insertData(headline, link, platform);
                Log.i("On item long clicked", "clickCheck");
                String new_headline = "✯ " + headline;
                headlines.set(i, new_headline);

//                headlines.set(1, "testing testing testing");
                adapter.notifyDataSetChanged();
                if (!isInserted) {
                    Toast.makeText(MainActivity.this, "Already Added to Bookmark", Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    Toast.makeText(MainActivity.this, "Added", Toast.LENGTH_LONG).show();

                    return true;
                }

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