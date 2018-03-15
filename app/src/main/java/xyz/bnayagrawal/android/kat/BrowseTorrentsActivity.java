package xyz.bnayagrawal.android.kat;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.bnayagrawal.android.kat.adapter.TabPagerAdapter;

public class BrowseTorrentsActivity extends AppCompatActivity {

    @BindView(R.id.tab_layout) TabLayout mTabLayout;
    @BindView(R.id.viewpager) ViewPager mViewPager;
    @BindView(R.id.toolbar_act_bta) Toolbar mToolbar;

    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_torrents);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar actionbar = getSupportActionBar();
        if(null != actionbar) {
            actionbar.setDisplayShowHomeEnabled(true);
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        initTabs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }

    private void initTabs() {
        mTabLayout.addTab(mTabLayout.newTab().setText("MOVIES"));
        mTabLayout.addTab(mTabLayout.newTab().setText("TV"));
        mTabLayout.addTab(mTabLayout.newTab().setText("MUSIC"));
        mTabLayout.addTab(mTabLayout.newTab().setText("BOOKS"));
        mTabLayout.addTab(mTabLayout.newTab().setText("GAMES"));
        mTabLayout.addTab(mTabLayout.newTab().setText("APPS"));

        mPagerAdapter = new TabPagerAdapter(
                getSupportFragmentManager(),
                mTabLayout.getTabCount()
        );

        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                /////////////////////
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                /////////////////////
            }
        });
    }
}
