package xyz.bnayagrawal.android.kat.adapter;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import xyz.bnayagrawal.android.kat.fragment.TabBrowseFragment;

/**
 * Created by bnayagrawal on 15/3/18.
 */

public class TabPagerAdapter extends FragmentPagerAdapter {

    private int mTabCount;

    public TabPagerAdapter(FragmentManager mFragmentManager, int mTabCount) {
        super(mFragmentManager);
        this.mTabCount = mTabCount;
    }

    @Override
    public Fragment getItem(int position) {
        TabBrowseFragment tabBrowseFragment;
        Bundle bundle;

        switch (position) {
            case 0:
                tabBrowseFragment = new TabBrowseFragment();
                bundle = new Bundle();
                bundle.putSerializable(TabBrowseFragment.EXTRA_CATEGORY,Category.MOVIES);
                tabBrowseFragment.setArguments(bundle);
                return tabBrowseFragment;
            case 1:
                tabBrowseFragment = new TabBrowseFragment();
                bundle = new Bundle();
                bundle.putSerializable(TabBrowseFragment.EXTRA_CATEGORY,Category.TV);
                tabBrowseFragment.setArguments(bundle);
                return tabBrowseFragment;
            case 2:
                tabBrowseFragment = new TabBrowseFragment();
                bundle = new Bundle();
                bundle.putSerializable(TabBrowseFragment.EXTRA_CATEGORY,Category.MUSIC);
                tabBrowseFragment.setArguments(bundle);
                return tabBrowseFragment;
            case 3:
                tabBrowseFragment = new TabBrowseFragment();
                bundle = new Bundle();
                bundle.putSerializable(TabBrowseFragment.EXTRA_CATEGORY,Category.BOOKS);
                tabBrowseFragment.setArguments(bundle);
                return tabBrowseFragment;
            case 4:
                tabBrowseFragment = new TabBrowseFragment();
                bundle = new Bundle();
                bundle.putSerializable(TabBrowseFragment.EXTRA_CATEGORY,Category.GAMES);
                tabBrowseFragment.setArguments(bundle);
                return tabBrowseFragment;
            case 5:
                tabBrowseFragment = new TabBrowseFragment();
                bundle = new Bundle();
                bundle.putSerializable(TabBrowseFragment.EXTRA_CATEGORY,Category.APPS);
                tabBrowseFragment.setArguments(bundle);
                return tabBrowseFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mTabCount;
    }

    public enum Category {
        MOVIES,
        TV,
        MUSIC,
        BOOKS,
        GAMES,
        APPS
    }
}
