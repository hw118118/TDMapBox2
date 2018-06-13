package com.example.hgis.tdmapbox2.com.search;

import android.support.v4.app.FragmentActivity;
import android.widget.ProgressBar;

import com.example.hgis.tdmapbox2.CustomRecyclerView;
import com.example.hgis.tdmapbox2.R;
import com.mancj.materialsearchbar.MaterialSearchBar;

import butterknife.BindView;

/**
 * Created by HGIS on 2017/8/29.
 */

public class SearchActivity extends FragmentActivity  {
    private static final String TAG=SearchActivity.class.getName();
    @BindView(R.id.search_bar)
    MaterialSearchBar searchBar;
    @BindView(R.id.recycler)
    CustomRecyclerView recyclerView;
    @BindView(R.id.progressbar)
    ProgressBar progressBar;

}
