package xyz.bnayagrawal.android.kat.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bnayagrawal on 9/3/18.
 */

public final class Torrent implements Parcelable {
    private final String mName;
    private final String mDetailsPageLink;
    private final String mAge;
    private final String mSize; //No need to store in bytes (the page already returns a formatted string)
    private final int mSeedsCount;
    private final int mPeersCount;
    private final String mCategory;

    public Torrent(String mName, String mCategory, String mDetailsPageLink, String mAge, String mSize, int mSeedsCount, int mPeersCount) {
        this.mName = mName;
        this.mCategory = mCategory;
        this.mDetailsPageLink = mDetailsPageLink;
        this.mAge = mAge;
        this.mSize = mSize;
        this.mSeedsCount = mSeedsCount;
        this.mPeersCount = mPeersCount;
    }

    public String getName() {
        return mName;
    }

    public String getCategory() {return mCategory;}

    public String getDetailsPageLink() {
        return mDetailsPageLink;
    }

    public String getAge() {
        return mAge;
    }

    public String getSize() {
        return mSize;
    }

    public int getSeeds() {
        return mSeedsCount;
    }

    public int getPeers() {
        return mPeersCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeString(this.mCategory);
        dest.writeString(this.mDetailsPageLink);
        dest.writeString(this.mAge);
        dest.writeString(this.mSize);
        dest.writeInt(this.mSeedsCount);
        dest.writeInt(this.mPeersCount);
    }

    protected Torrent(Parcel in) {
        this.mName = in.readString();
        this.mCategory = in.readString();
        this.mDetailsPageLink = in.readString();
        this.mAge = in.readString();
        this.mSize = in.readString();
        this.mSeedsCount = in.readInt();
        this.mPeersCount = in.readInt();
    }

    public static final Creator<Torrent> CREATOR = new Creator<Torrent>() {
        @Override
        public Torrent createFromParcel(Parcel source) {
            return new Torrent(source);
        }

        @Override
        public Torrent[] newArray(int size) {
            return new Torrent[size];
        }
    };
}
