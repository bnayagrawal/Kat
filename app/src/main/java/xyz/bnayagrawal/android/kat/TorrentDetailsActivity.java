package xyz.bnayagrawal.android.kat;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import xyz.bnayagrawal.android.kat.data.Torrent;
import xyz.bnayagrawal.android.kat.data.TorrentDetails;
import xyz.bnayagrawal.android.kat.net.Kat;
import xyz.bnayagrawal.android.kat.net.Url;

import static xyz.bnayagrawal.android.kat.net.Url.BASE_URL;
import static xyz.bnayagrawal.android.kat.util.KatDocumentUtil.getTorrentDetails;

public class TorrentDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_TORRENT = "torrent";
    private static final String TAG = TorrentDetailsActivity.class.getSimpleName();

    @BindView(R.id.text_torrent_name)
    TextView mTextTorrentName;

    @BindView(R.id.text_torrent_category_age)
    TextView mTextTorrentCategoryAge;

    @BindView(R.id.text_torrent_file_size)
    TextView mTextTorrentFileSize;

    @BindView(R.id.text_torrent_seeds_count)
    TextView mTextTorrentSeedsCount;

    @BindView(R.id.text_torrent_peers_count)
    TextView mTextTorrentPeersCount;

    @BindView(R.id.text_torrent_hash)
    TextView mTextTorrentHash;

    @BindView(R.id.text_torrent_trackers)
    TextView mTextTorrentTrackers;

    @BindView(R.id.text_torrent_description)
    TextView mTextTorrentDescription;

    @BindView(R.id.button_open_magnet_link)
    Button mButtonOpenMagnetLink;

    @BindView(R.id.button_copy_magnet_link)
    Button mButtonCopyMagnetLink;

    private Kat mKat;
    private Retrofit mRetrofit;
    private retrofit2.Call<String> mCall;

    private Torrent mTorrent;
    private TorrentDetails mTorrentDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torrent_details);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intentExtras = getIntent();
        if(intentExtras.hasExtra(EXTRA_TORRENT)) {
            mTorrent = intentExtras.getParcelableExtra(EXTRA_TORRENT);
        }

        initRetrofit();
        bindTorrentData();
        fetchTorrentDetails();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_torrent_details,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_share:
                Intent intentShare = new Intent(Intent.ACTION_SEND);
                intentShare.setType("text/plain");
                intentShare.putExtra(Intent.EXTRA_TEXT,mTorrentDetails.getMagnetLink());
                try {
                    startActivity(intentShare);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(TorrentDetailsActivity.this,
                            "You may not have app installed to perform this action.",
                            Toast.LENGTH_LONG)
                            .show();
                }
            case R.id.action_open_link:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Url.BASE_URL + "/" + mTorrent.getDetailsPageLink()));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(this,
                            "Web browser not installed!",
                            Toast.LENGTH_LONG)
                            .show();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(null != mCall && mCall.isExecuted())
            mCall.cancel();
    }

    private void initRetrofit() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(Url.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void fetchTorrentDetails() {
        if(mKat == null) mKat = mRetrofit.create(Kat.class);
        if(mCall != null && mCall.isExecuted()) mCall.cancel();
        mCall = mKat.getDocument(mTorrent.getDetailsPageLink());
        mCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {
                    mTorrentDetails = getTorrentDetails(response.body());
                    if(mTorrentDetails != null) {
                        mTextTorrentHash.setText("BitTorrent info hash: ".concat(mTorrentDetails.getHash()));
                        mTextTorrentDescription.setText(mTorrentDetails.getDescription());
                        mTextTorrentTrackers.setText(mTorrentDetails.getTrackers());
                        setButtonClickListeners();
                    }
                    else {
                        Toast.makeText(TorrentDetailsActivity.this, "Page not found!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TorrentDetailsActivity.this,"Error occur! Please retry",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void bindTorrentData() {
        mTextTorrentName.setText(mTorrent.getName());
        String seeds = getString(R.string.seeds)
                + " " + mTorrent.getSeeds();
        String peers = getString(R.string.peers)
                + " " + mTorrent.getPeers();
        String desc = mTorrent.getCategory()
                + " " + getString(R.string.symbol_bullet)
                + " " + mTorrent.getAge();
        mTextTorrentCategoryAge.setText(desc);
        mTextTorrentFileSize.setText(mTorrent.getSize());
        mTextTorrentSeedsCount.setText(seeds);
        mTextTorrentPeersCount.setText(peers);
    }

    private void setButtonClickListeners() {
        mButtonCopyMagnetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Magnet link",mTorrentDetails.getMagnetLink());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(TorrentDetailsActivity.this,"Magnet link copied!",Toast.LENGTH_SHORT).show();
            }
        });

        mButtonOpenMagnetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(mTorrentDetails.getMagnetLink()));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(TorrentDetailsActivity.this,
                            "You may not have app which handles magnet url!",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }
}
