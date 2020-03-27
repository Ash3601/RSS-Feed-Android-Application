package com.example.exp6;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class Bookmarks extends AppCompatActivity {

    private DatabaseHelper myDb;
    private WebView webView;
    private ListView listView;
    private List<String> headlines;
    private List<String> links;
    private ArrayAdapter<String> adapter;
    private EditText filer;
    private boolean confirmation = false;
    private boolean isItemDeleted = false;
    private boolean showData() {
        Cursor res = myDb.getAllData();
        if (res.getCount() == 0) {
            // no data
            Toast.makeText(this, "No data found!", Toast.LENGTH_SHORT);
            Log.i("Error", "No data found!");
            return false;
        }
        while (res.moveToNext()) {
            headlines.add(res.getString(0));
            links.add(res.getString(1));
        }
        return true;
    }

    // Used for searching queries
    private boolean showData(Cursor res) {
        headlines.clear();
        links.clear();
        if (res.getCount() == 0) {
            // no data
            Toast.makeText(this, "No data found!", Toast.LENGTH_SHORT);
            Log.i("Error", "No data found!");
            return false;
        }
        StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            headlines.add(res.getString(0));
            links.add(res.getString(1));
        }
        return true;
    }


    private boolean showDataOrderBy(Cursor res) {
        if (res.getCount() == 0) {
            // no data
            Toast.makeText(this, "No data present", Toast.LENGTH_SHORT).show();
            Log.i("Error", "No data found!");
            return false;
        }
        headlines.clear();
        links.clear();
        while (res.moveToNext()) {
            headlines.add(res.getString(0));
            links.add(res.getString(1));
        }
        return true;
    }


    public void showWebView() {
        webView = new WebView(this);

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

        webView.loadUrl(String.valueOf(getIntent().getData()));

    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        onRestart();
        Intent resIntent = new Intent();
        if (isItemDeleted)
            resIntent.putExtra("isDeletionOccured", "true");
        else
            resIntent.putExtra("isDeletionOccured", "false");

        setResult(RESULT_OK, resIntent);
        finish();
    }

//    @Override
//    public void onRestart() {
//        super.onRestart();
//        //When BACK BUTTON is pressed, the activity on the stack is restarted
//        //Do what you want on the refresh procedure here
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        getSupportActionBar().setTitle("Bookmarks");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        filer = findViewById(R.id.edit_text_filter);
        myDb = new DatabaseHelper(this);
        listView = findViewById(R.id.list_view_bookmarks);
        headlines = new ArrayList();
        links = new ArrayList();
        showData();
        adapter = new ArrayAdapter<>(Bookmarks.this, android.R.layout.simple_list_item_1, headlines);
        listView.setAdapter(adapter);

        // setting add text listener on edit text
        filer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void afterTextChanged(Editable editable) {
                Cursor cursor = myDb.queryData(filer.getText().toString().trim());
                showData(cursor);
                adapter = new ArrayAdapter<String>(Bookmarks.this, android.R.layout.simple_list_item_1, headlines);
                listView.setAdapter(adapter);

            }
        });


        // on press
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(Bookmarks.this, WebPageActivity.class);
                Uri uri = Uri.parse((links.get(position)));
                intent.setData(uri);
                startActivity(intent);
            }
        });

        // on long press
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String headline = headlines.get(i);
                boolean isDeleted = myDb.deleteData(headline);
                headlines.remove(i);
                links.remove(i);
                adapter.notifyDataSetChanged();
                if (isDeleted) {
                    isItemDeleted = true;
                    if (filer.getText().toString().equals("") == false) {
                        Cursor cursor = myDb.queryData(filer.getText().toString().trim());
                        showData(cursor);
                        Toast.makeText(Bookmarks.this, "Deleted", Toast.LENGTH_SHORT).show();

                    } else {

                        adapter.notifyDataSetChanged();
                        Toast.makeText(Bookmarks.this, "Deleted", Toast.LENGTH_SHORT).show();

                    }
                }
                return isDeleted;

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public boolean askConfirmation(String title, String msg, final String result, final Context c) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        confirmation = true;
                        myDb.removeAllEntries();
                        headlines.clear();
                        links.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(c, result, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        confirmation = false;
                        Toast.makeText(c, "Cancelled", Toast.LENGTH_SHORT).show();
                    }

                }).show();
        return confirmation;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bookmarks_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        Cursor cursor;
        switch (item.getItemId()) {
            case R.id.delete_all:
                askConfirmation("Confirm", "Are you really want to delete all bookmarks", "All entries deleted.", Bookmarks.this);
                break;

            case R.id.order_by_news:
                cursor = myDb.getAllDataOrderBy("News");
                showDataOrderBy(cursor);
                adapter.notifyDataSetChanged();
                break;
            case R.id.order_by_programming:
                cursor = myDb.getAllDataOrderBy("Programming");
                showDataOrderBy(cursor);
                adapter.notifyDataSetChanged();
                break;
            case R.id.order_by_show_all:
                headlines.clear();
                links.clear();
                showData();
                adapter.notifyDataSetChanged();
                break;

//            case R.id.menu_exit:
////                Toast.makeText(this, "Exiting", Toast.LENGTH_LONG);
////                finish();
        }
        return true;
    }


}
