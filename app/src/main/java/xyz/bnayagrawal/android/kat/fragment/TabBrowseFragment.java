package xyz.bnayagrawal.android.kat.fragment;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import xyz.bnayagrawal.android.kat.R;
import xyz.bnayagrawal.android.kat.adapter.TabPagerAdapter;
import xyz.bnayagrawal.android.kat.adapter.TorrentRecyclerAdapter;
import xyz.bnayagrawal.android.kat.data.Torrent;
import xyz.bnayagrawal.android.kat.net.Kat;
import xyz.bnayagrawal.android.kat.net.Url;

import static xyz.bnayagrawal.android.kat.net.Url.BASE_URL;
import static xyz.bnayagrawal.android.kat.util.KatDocumentUtil.getTorrentList;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabBrowseFragment extends Fragment {
    public static final String EXTRA_CATEGORY = "browse_category";
    private static final String EXTRA_TORRENT_LIST = "torrent_list";

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    @BindView(R.id.recycler_torrents)
    RecyclerView mRecyclerTorrents;

    private Kat mKat;
    private Retrofit mRetrofit;
    private retrofit2.Call<String> mCall;

    private ArrayList<Torrent> mTorrents;
    private TorrentRecyclerAdapter mAdapter;

    private TabPagerAdapter.Category mCategory;

    private int mLastLoadedPageNumber = 1;

    public TabBrowseFragment() {
        //Required
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mCategory = (TabPagerAdapter.Category) getArguments().getSerializable(EXTRA_CATEGORY);
        View view = inflater.inflate(R.layout.tab_fragment_browse, container, false);
        ButterKnife.bind(this, view);

        initRecyclerView();
        initRetrofit();

        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mAdapter != null && mTorrents != null) {
                    mAdapter.notifyItemRangeRemoved(0, mTorrents.size());
                    fetchTorrents(mCategory,1);
                }
            }
        });

        if (savedInstanceState != null
                && savedInstanceState.containsKey(EXTRA_TORRENT_LIST)
                && savedInstanceState.containsKey(EXTRA_CATEGORY)) {
            mTorrents = savedInstanceState.getParcelableArrayList(EXTRA_TORRENT_LIST);
            mCategory = (TabPagerAdapter.Category) savedInstanceState.getSerializable(EXTRA_CATEGORY);
            mAdapter.swapDataSet(mTorrents);
        } else {
            fetchTorrents(mCategory,1);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mTorrents != null && mTorrents.size() > 0) {
            outState.putSerializable(EXTRA_CATEGORY, mCategory);
            outState.putParcelableArrayList(EXTRA_TORRENT_LIST, mTorrents);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mCall && mCall.isExecuted())
            mCall.cancel();
    }

    private void initRetrofit() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void initRecyclerView() {
        //Layout Manager
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                getContext(),
                LinearLayoutManager.VERTICAL,
                false);
        mRecyclerTorrents.setLayoutManager(layoutManager);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mRecyclerTorrents.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    if(mSwipeRefresh.isRefreshing())
                        return;
                    if(!mRecyclerTorrents.canScrollVertically(RecyclerView.VERTICAL)) {
                        fetchTorrents(mCategory, mLastLoadedPageNumber + 1);
                        Toast.makeText(getContext(),"Loading page " + String.valueOf(mLastLoadedPageNumber + 1) + ", Please wait!",Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            mRecyclerTorrents.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if(mSwipeRefresh.isRefreshing())
                        return;
                    if(!mRecyclerTorrents.canScrollVertically(RecyclerView.VERTICAL)) {
                        fetchTorrents(mCategory, mLastLoadedPageNumber + 1);
                        Toast.makeText(getContext(),"Loading page " + String.valueOf(mLastLoadedPageNumber + 1) + ", Please wait!",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        //Item decorator (divider)
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mRecyclerTorrents.getContext(),
                DividerItemDecoration.VERTICAL
        );
        mRecyclerTorrents.addItemDecoration(dividerItemDecoration);

        //Animator
        FadeInUpAnimator animator = new FadeInUpAnimator();
        animator.setAddDuration(150);
        animator.setChangeDuration(150);
        animator.setRemoveDuration(300);
        animator.setMoveDuration(300);
        mRecyclerTorrents.setItemAnimator(animator);

        //Adapter
        mTorrents = new ArrayList<>();
        mAdapter = new TorrentRecyclerAdapter(getContext(), mTorrents);
        mRecyclerTorrents.setAdapter(mAdapter);
    }

    private String getPath(TabPagerAdapter.Category category) {
        String path = "";
        switch (mCategory) {
            case MOVIES:
                path = Url.PATH_MOVIES;
                break;
            case TV:
                path = Url.PATH_TV;
                break;
            case MUSIC:
                path = Url.PATH_MUSIC;
                break;
            case APPS:
                path = Url.PATH_APPS;
                break;
            case BOOKS:
                path = Url.PATH_BOOKS;
                break;
            case GAMES:
                path = Url.PATH_GAMES;
                break;
        }
        return path;
    }

    private void fetchTorrents(TabPagerAdapter.Category category, final int pageNo) {
        if (mKat == null) mKat = mRetrofit.create(Kat.class);
        if (mCall != null && mCall.isExecuted()) mCall.cancel();
        mCall = mKat.getDocumentCategory(getPath(category),pageNo);
        mSwipeRefresh.setRefreshing(true);
        mCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                mSwipeRefresh.setRefreshing(false);
                if (response.isSuccessful()) {
                    ArrayList<Torrent> torrents = getTorrentList(response.body());
                    if (torrents != null) {
                        //Sucks, but only for animation to work.
                        for (Torrent torrent : torrents) {
                            mTorrents.add(torrent);
                            mAdapter.notifyItemInserted(torrents.size());
                        }
                        mLastLoadedPageNumber = pageNo;
                    } else {
                        Toast.makeText(getContext(), "No torrents found!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("tag", response.raw().request().url().toString());
                    Toast.makeText(getContext(), "Error occur! Please retry", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
