package com.engineercalc.mohrscircle;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.engineercalc.elasticity.Tensor;
import com.engineercalc.mohrscircle.MohrsCircleActivity.MohrsCircleInterface;

public class MohrsCircleTabs implements MohrsCircleInterface {

	private static final int DEFAULT_TAB = 0;
	private MohrsCircleActivity mActivity;

	ViewPager mViewPager;
	TabsAdapter mTabsAdapter;
	
	public MohrsCircleTabs(MohrsCircleActivity activity) {
		mActivity = activity;
	}

	@Override
	public void onCreate() {
        final ActionBar actionBar = mActivity.getSupportActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mActivity.setContentView(R.layout.main_phone);
    	mViewPager = (ViewPager)mActivity.findViewById(R.id.pager);
    	mViewPager.setId(R.id.pager);
    	mViewPager.setOffscreenPageLimit(1);
    	
        mTabsAdapter = new TabsAdapter(mActivity, mViewPager);
        mTabsAdapter.addTab(actionBar.newTab().setText(R.string.inputtab), InputFragment.class, null, "input_tab");
        mTabsAdapter.addTab(actionBar.newTab().setText(R.string.circletab), PlotFragment.class, null, "analysis_tab");
        mTabsAdapter.addTab(actionBar.newTab().setText(R.string.propertiestab), PropertiesDialogFragment.class, null, "properties_tab");
	}

	@Override
	public void pushInputChange() {
		final InputFragment inputFragment = (InputFragment)mTabsAdapter.getFragmentByTag("input_tab");
		final PlotFragment plotFragment = (PlotFragment)mTabsAdapter.getFragmentByTag("analysis_tab");
		final PropertiesDialogFragment propertiesFragment = (PropertiesDialogFragment)mTabsAdapter.getFragmentByTag("properties_tab");
		
		final Tensor tensor = mActivity.getTensor();
		
		// Any tab can be out of view, deflated, so check whether they are null
		if (inputFragment != null) {
			inputFragment.setTensor(tensor);
		}
		
		if (plotFragment != null) {
			plotFragment.update(tensor);
		}
		
		if (propertiesFragment != null) {
			propertiesFragment.update(tensor);
		}
	}

	@Override
	public void addPrefsToEditorForSaving(Editor editor) {
		editor.putInt("tab", mActivity.getSupportActionBar().getSelectedNavigationIndex());
	}

	@Override
	public void loadSavedPrefs(SharedPreferences settings) {
		int tab = settings.getInt("tab", DEFAULT_TAB);
		final int count = mTabsAdapter.getCount();
		if (tab < 0 || tab >= count) {
			tab = DEFAULT_TAB;
		}
    	mActivity.getSupportActionBar().setSelectedNavigationItem(tab);
	}


    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     * 
     */
    protected class TabsAdapter extends FragmentPagerAdapter
            implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
        //private final FragmentActivity mActivity;
    	private final MohrsCircleActivity mActivity;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
            private final String myFragmentTag;
            private String autoFragmentTag;

            TabInfo(Class<?> _class, Bundle _args, String _fragmentTag) {
                clss = _class;
                args = _args;
                myFragmentTag = _fragmentTag;
            }
        }

        public TabsAdapter(MohrsCircleActivity activity /*SherlockFragmentActivity activity*/, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mActivity = activity;
            mActionBar = activity.getSupportActionBar();
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args, String fragmentTag) {
            TabInfo info = new TabInfo(clss, args, fragmentTag);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mActivity, info.clss.getName(), info.args);
        }
        
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
        	Fragment frag = (Fragment)super.instantiateItem(container, position);
            TabInfo info = mTabs.get(position);
        	info.autoFragmentTag = frag.getTag();	// or we could just store a reference to the Fragment itself
        	return frag;
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

        public void onPageSelected(int position) {
        	// hide the keyboard and clear focus so that input gets saved
        	mActivity.hideKeyboard();
        	
            mActionBar.setSelectedNavigationItem(position);
        }

        public void onPageScrollStateChanged(int state) {}

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
            for (int i=0; i<mTabs.size(); i++) {
                if (mTabs.get(i) == tag) {
                    mViewPager.setCurrentItem(i);
                }
            }
        }
        
        public Fragment getFragmentByTag(String tag) {
        	for (TabInfo tab : mTabs) {
        		if (tab.myFragmentTag == tag) {
        			return mActivity.getSupportFragmentManager().findFragmentByTag(tab.autoFragmentTag);
        		}
        	}
        	return null;
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) { }

        public void onTabReselected(Tab tab, FragmentTransaction ft) { }
    }
}
