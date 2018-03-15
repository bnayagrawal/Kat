package xyz.bnayagrawal.android.kat.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bnayagrawal on 15/3/18.
 */

public final class TorrentDetails implements Parcelable {
    private final String mHash;
    private final String mTrackers;
    private final String mDescription;
    private final String mMagnetLink;

    public TorrentDetails(String mHash, String mTrackers, String mDescription, String mMagnetLink) {
        this.mHash = mHash;
        this.mTrackers = mTrackers;
        this.mDescription = mDescription;
        this.mMagnetLink = mMagnetLink;
    }

    public String getHash() {
        return mHash;
    }

    public String getTrackers() {
        return mTrackers;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getMagnetLink() {
        return mMagnetLink;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mHash);
        dest.writeString(this.mTrackers);
        dest.writeString(this.mDescription);
        dest.writeString(this.mMagnetLink);
    }

    protected TorrentDetails(Parcel in) {
        this.mHash = in.readString();
        this.mTrackers = in.readString();
        this.mDescription = in.readString();
        this.mMagnetLink = in.readString();
    }

    public static final Parcelable.Creator<TorrentDetails> CREATOR = new Parcelable.Creator<TorrentDetails>() {
        @Override
        public TorrentDetails createFromParcel(Parcel source) {
            return new TorrentDetails(source);
        }

        @Override
        public TorrentDetails[] newArray(int size) {
            return new TorrentDetails[size];
        }
    };
}
