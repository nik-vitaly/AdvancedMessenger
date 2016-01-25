package net.nikonorov.advancedmessenger.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import net.nikonorov.advancedmessenger.R;

/**
 * Created by vitaly on 25.01.16.
 */
public class ActivityMain extends AppCompatActivity {

    private final int PAGE_COUNT = 3;

    private Fragment[] fragments = new Fragment[PAGE_COUNT];
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private String[] titles = new String[PAGE_COUNT];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragments[FragmentSet.CHATLIST] = new FragmentChatList();
        fragments[FragmentSet.CONTACTS] = new FragmentContacts();
        fragments[FragmentSet.PROFILE] = new FragmentProfile();

        viewPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        titles[FragmentSet.CHATLIST] = getString(R.string.main_chats);
        titles[FragmentSet.CONTACTS] = getString(R.string.main_contacts);
        titles[FragmentSet.PROFILE] = getString(R.string.main_profile);
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}