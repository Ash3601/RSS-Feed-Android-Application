package com.example.exp6;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Bookmarks extends AppCompatActivity {

    DatabaseHelper myDb;
    WebView webView;
    private ListView listView;
    private List<String> headlines;
    private List<String> links;

    private boolean showData() {
        Cursor res = myDb.getAllData();
        if (res.getCount() == 0) {
            // no data
            Log.i("Error", "No data found!");
            return false;
        }
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            headlines.add(res.getString(0));
            links.add(res.getString(1));

//            buffer.append("Id: " + res.getString(0) + "\n" + " First Name: " + res.getString(1) + "\n" + " Last Name: " + res.getString(2) + "\n" + " Marks: " + res.getString(3) + "\n");
        }
        return true;

    }

    public void showWebView() {
        webView  = new WebView(this);

        webView.getSettings().setJavaScriptEnabled(true); // enable javascript

        final Activity activity = this;

        webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });

        webView .loadUrl(String.valueOf(getIntent().getData()));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Bookmarks");

        myDb = new DatabaseHelper(this);
        listView = findViewById(R.id.list_view_bookmarks);
        headlines = new ArrayList();
        links = new ArrayList();
        showData();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Bookmarks.this, android.R.layout.simple_list_item_1, headlines);
        listView.setAdapter(adapter);
        // on press
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(Bookmarks.this, WebPageActivity.class);
                Uri uri = Uri.parse((links.get(position)).toString());
                intent.setData(uri);
                startActivity(intent);
            }
        });

        // on press
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(Bookmarks.this, WebPageActivity.class);
                Uri uri = Uri.parse((links.get(position)).toString());
                intent.setData(uri);
                startActivity(intent);
            }
        });

        // on long press
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String headline = (String)headlines.get(i);
//                String link = (String) links.get(i);
//                String headline =
                boolean isDeleted = myDb.deleteData(headline);
                Log.i("On item long clicked data deleted", "deleteCheck");
                headlines.remove(i);
                links.remove(i);
                if (isDeleted) {
                    adapter.notifyDataSetChanged();
//                    startActivity(Bookm/arks.this);
                }
                return isDeleted;

            }
        });

    }
}
