package com.example.exp6;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    private ArrayAdapter<String> adapter;
    private EditText filer;
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

        filer = findViewById(R.id.edit_text_filter);
        myDb = new DatabaseHelper(this);
        listView = findViewById(R.id.list_view_bookmarks);
        headlines = new ArrayList();
        links = new ArrayList();
        showData();
         adapter = new ArrayAdapter<String>(Bookmarks.this, android.R.layout.simple_list_item_1, headlines);
        listView.setAdapter(adapter);

        // setting add text listener on edit text
        filer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                (Bookmarks.this).adapter.getFilter().filter(charSequence);

            }

            @Override
            public void afterTextChanged(Editable editable) {

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
                adapter.notifyDataSetChanged();
                if (isDeleted) {
//                    boolean isText =
                    if (filer.getText().toString().equals("") == false) {
                        filer.getText().clear();
                        adapter.notifyDataSetChanged();
                        Intent intent = getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        Toast.makeText(Bookmarks.this, "Deleted", Toast.LENGTH_SHORT).show();
                    } else{

                        adapter.notifyDataSetChanged();
                        Toast.makeText(Bookmarks.this, "Deleted", Toast.LENGTH_SHORT).show();

                    }
                }
                return isDeleted;

            }
        });

    }
}
