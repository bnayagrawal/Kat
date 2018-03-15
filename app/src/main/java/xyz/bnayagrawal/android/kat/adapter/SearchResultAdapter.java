package xyz.bnayagrawal.android.kat.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.bnayagrawal.android.kat.R;
import xyz.bnayagrawal.android.kat.TorrentDetailsActivity;
import xyz.bnayagrawal.android.kat.data.Torrent;

/**
 * Created by bnayagrawal on 14/3/18.
 */

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Torrent> mTorrents;

    public SearchResultAdapter(Context context, ArrayList<Torrent> torrents) {
        this.mContext = context;
        this.mTorrents = torrents;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_torrent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String seeds = mContext.getString(R.string.seeds)
                + " " + mTorrents.get(position).getSeeds();
        String peers = mContext.getString(R.string.peers)
                + " " + mTorrents.get(position).getPeers();
        String desc = mTorrents.get(position).getCategory()
                + " " + mContext.getString(R.string.symbol_bullet)
                + " " + mTorrents.get(position).getAge();
        holder.textViewTorrentName.setText(mTorrents.get(position).getName());
        holder.textViewTorentCategoryAge.setText(desc);
        holder.textViewTorrentFileSize.setText(mTorrents.get(position).getSize());
        holder.textViewTorrentSeedsCount.setText(seeds);
        holder.textViewTorrentPeersCount.setText(peers);

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, TorrentDetailsActivity.class);
                intent.putExtra(TorrentDetailsActivity.EXTRA_TORRENT,mTorrents.get(position));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTorrents.size();
    }

    public void swapDataSet(@NonNull ArrayList<Torrent> torrents) {
        this.mTorrents = torrents;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View root;

        @BindView(R.id.text_torrent_name)
        TextView textViewTorrentName;

        @BindView(R.id.text_torrent_category_age)
        TextView textViewTorentCategoryAge;

        @BindView(R.id.text_torrent_file_size)
        TextView textViewTorrentFileSize;

        @BindView(R.id.text_torrent_seeds_count)
        TextView textViewTorrentSeedsCount;

        @BindView(R.id.text_torrent_peers_count)
        TextView textViewTorrentPeersCount;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
            root = view;
        }
    }
}
