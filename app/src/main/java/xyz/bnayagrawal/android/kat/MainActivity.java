package xyz.bnayagrawal.android.kat;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.bnayagrawal.android.kat.adapter.SearchResultAdapter;
import xyz.bnayagrawal.android.kat.data.Torrent;
import xyz.bnayagrawal.android.kat.net.Kat;
import xyz.bnayagrawal.android.kat.net.Url;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.search_view)
    SearchView mSearchView;

    @BindView(R.id.button_browse)
    Button mButtonBrowse;

    @BindView(R.id.button_search)
    Button mButtonSearch;

    @BindView(R.id.text_label_developer)
    TextView mTextLabelDeveloper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mTextLabelDeveloper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Url.MY_GIT_ACCOUNT_URL));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this,
                            "Web browser not installed!",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
        initSearchView();
        initButtons();
    }

    private void initSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(getApplicationContext(), SearchResultActivity.class))
        );
        searchView.setIconifiedByDefault(false);

    }

    private void initButtons() {
        mButtonBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = mSearchView.getQuery().toString();
                if(query.length() == 0) {
                    Toast.makeText(MainActivity.this,"Please enter a query",Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(MainActivity.this,SearchResultActivity.class);
                intent.setAction(Intent.ACTION_SEARCH);
                intent.putExtra(SearchManager.QUERY,query);
                startActivity(intent);
            }
        });
    }
}
