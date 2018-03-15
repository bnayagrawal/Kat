package xyz.bnayagrawal.android.kat;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

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
import xyz.bnayagrawal.android.kat.adapter.SearchResultAdapter;
import xyz.bnayagrawal.android.kat.data.Torrent;
import xyz.bnayagrawal.android.kat.net.Kat;

import static xyz.bnayagrawal.android.kat.net.Url.BASE_URL;
import static xyz.bnayagrawal.android.kat.util.KatDocumentUtil.getTorrentList;

public class SearchResultActivity extends AppCompatActivity {
    private static final String TAG = SearchResultActivity.class.getSimpleName();

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
    private SearchResultAdapter mAdapter;

    private Animation mFadeInAnimation;
    private Animation mFadeOutAnimation;

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

        //These lines wont execute if a new intent is requested
        Intent data = getIntent();
        if(data != null) {
            handleIntent(data);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
    protected void onStop() {
        super.onStop();
        if(null != mCall && mCall.isExecuted())
            mCall.cancel();
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
        mAdapter = new SearchResultAdapter(this,mTorrents);
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
                    showProgress("Parsing data...");
                    ArrayList<Torrent> torrents = getTorrentList(response.body());
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
                        Toast.makeText(SearchResultActivity.this, "No torrents found!", Toast.LENGTH_SHORT).show();
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
