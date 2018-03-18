package xyz.bnayagrawal.android.kat;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.FadeInUpAnimator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import xyz.bnayagrawal.android.kat.adapter.TorrentRecyclerAdapter;
import xyz.bnayagrawal.android.kat.data.Torrent;
import xyz.bnayagrawal.android.kat.net.Kat;

import static xyz.bnayagrawal.android.kat.net.Url.BASE_URL;
import static xyz.bnayagrawal.android.kat.util.KatDocumentUtil.getTorrentList;

public class SearchResultActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Torrent>> {
    private static final String TAG = SearchResultActivity.class.getSimpleName();
    private static final String EXTRA_TORRENT_LIST = "torrent_list";
    private static final String EXTRA_RAW_HTML_TEXT = "raw_html_text";
    private static final int ASYNC_LOADER_TASK_DOCUMENT_PARSER_ID = 101;

    @BindView(R.id.toolbar_act_sra)
    Toolbar mToolbar;

    @BindView(R.id.search_view_act_sra)
    SearchView mSearchView;

    @BindView(R.id.frame_search_result_container)
    FrameLayout mFrameSearchResultContainer;

    @BindView(R.id.constraint_progress_view)
    ConstraintLayout mConstraintProgressView;

    @BindView(R.id.text_searching)
    TextView mTextSearching;

    @BindView(R.id.recycler_search_result)
    RecyclerView mRecyclerSearchResult;

    private Kat mKat;
    private Retrofit mRetrofit;
    private retrofit2.Call<String> mCall;

    private ArrayList<Torrent> mTorrents;
    private TorrentRecyclerAdapter mAdapter;

    private Animation mFadeInAnimation;
    private Animation mFadeOutAnimation;

    private boolean torrentFound = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        ButterKnife.bind(this);

        initSearchView();
        initRecyclerView();
        initRetrofit();

        //animation
        mFadeInAnimation= AnimationUtils.loadAnimation(this,R.anim.fade_in);
        mFadeOutAnimation = AnimationUtils.loadAnimation(this,R.anim.fade_out);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        if(savedInstanceState != null && savedInstanceState.containsKey(EXTRA_TORRENT_LIST)) {
            mTorrents = savedInstanceState.getParcelableArrayList(EXTRA_TORRENT_LIST);
            if(null != mTorrents)
                mAdapter.swapDataSet(mTorrents);
        } else {
            //These lines wont execute if a new intent is requested (as onCreate is not called)
            getSupportLoaderManager().initLoader(ASYNC_LOADER_TASK_DOCUMENT_PARSER_ID,null,this);
            Intent data = getIntent();
            if (data != null) {
                handleIntent(data);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(mTorrents != null && mTorrents.size() > 0) {
            mTorrents.clear();
            mAdapter.notifyItemRangeRemoved(0, mTorrents.size());
        }
        handleIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(torrentFound && mTorrents.size() > 0)
            outState.putParcelableArrayList(EXTRA_TORRENT_LIST,mTorrents);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mCall && mCall.isExecuted())
            mCall.cancel();
    }

    @Override
    public Loader<ArrayList<Torrent>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<Torrent>>(this) {

            ArrayList<Torrent> processedData;

            @Override
            protected void onStartLoading() {
                //The args is expected to contain the raw html text.
                if(args == null)
                    return;

                if(processedData != null)
                    deliverResult(processedData);
                else
                    forceLoad();
            }

            @Override
            public ArrayList<Torrent> loadInBackground() {
                String rawHtmlText = args.getString(EXTRA_RAW_HTML_TEXT);
                if(rawHtmlText == null || rawHtmlText.length() == 0)
                    return null;
                return getTorrentList(rawHtmlText);
            }

            @Override
            public void deliverResult(ArrayList<Torrent> data) {
                processedData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Torrent>> loader, ArrayList<Torrent> torrents) {
        if(torrents != null) {
            //Sucks, but only for animation to work.
            for(Torrent torrent: torrents) {
                mTorrents.add(torrent);
                mAdapter.notifyItemInserted(torrents.size());
            }
            hideProgress();
            Toast.makeText(SearchResultActivity.this,torrents.size() + " torrents found!",Toast.LENGTH_SHORT).show();
        }
        else {
            hideProgress();
            torrentFound = false;
            Toast.makeText(SearchResultActivity.this, "No torrents found!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Torrent>> loader) {
        //Unimplemented...
    }

    private void initSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(getApplicationContext(), SearchResultActivity.class))
        );
        mSearchView.setIconifiedByDefault(false);
    }

    private void initRecyclerView() {
        //Layout Manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false);
        mRecyclerSearchResult.setLayoutManager(layoutManager);

        //Item decorator (divider)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mRecyclerSearchResult.getContext(),
                DividerItemDecoration.VERTICAL
        );
        mRecyclerSearchResult.addItemDecoration(dividerItemDecoration);

        //Animator
        FadeInUpAnimator animator = new FadeInUpAnimator();
        animator.setAddDuration(150);
        animator.setChangeDuration(150);
        animator.setRemoveDuration(300);
        animator.setMoveDuration(300);
        mRecyclerSearchResult.setItemAnimator(animator);

        //Adapter
        mTorrents = new ArrayList<>();
        mAdapter = new TorrentRecyclerAdapter(this,mTorrents);
        mRecyclerSearchResult.setAdapter(mAdapter);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            mSearchView.setQuery(query,false);
            mFrameSearchResultContainer.requestFocus();
            performSearch(query);
        }
    }

    private void initRetrofit() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void performSearch(String query) {
        if(mKat == null) mKat = mRetrofit.create(Kat.class);
        if(mCall != null && mCall.isExecuted()) mCall.cancel();
        mCall = mKat.searchTorrents(query);
        showProgress("Fetching data...");
        mCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {
                    Bundle bundle = new Bundle();
                    bundle.putString(EXTRA_RAW_HTML_TEXT,response.body());

                    LoaderManager loaderManager = getSupportLoaderManager();
                    Loader<String> documentParserLoader = loaderManager.getLoader(ASYNC_LOADER_TASK_DOCUMENT_PARSER_ID);

                    showProgress("Parsing data...");
                    if (documentParserLoader == null) {
                        loaderManager.initLoader(ASYNC_LOADER_TASK_DOCUMENT_PARSER_ID, bundle, SearchResultActivity.this);
                    } else {
                        loaderManager.restartLoader(ASYNC_LOADER_TASK_DOCUMENT_PARSER_ID, bundle, SearchResultActivity.this);
                    }

                } else {
                    Toast.makeText(SearchResultActivity.this,"Error occur! Please retry",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void showProgress(String message) {
        mRecyclerSearchResult.setVisibility(View.GONE);
        mRecyclerSearchResult.startAnimation(mFadeOutAnimation);
        mTextSearching.setText(message);
        mConstraintProgressView.setVisibility(View.VISIBLE);
        mConstraintProgressView.startAnimation(mFadeInAnimation);
    }

    private void hideProgress() {
        mConstraintProgressView.setVisibility(View.GONE);
        mConstraintProgressView.startAnimation(mFadeOutAnimation);
        mRecyclerSearchResult.setVisibility(View.VISIBLE);
        mRecyclerSearchResult.startAnimation(mFadeInAnimation);
    }


}
