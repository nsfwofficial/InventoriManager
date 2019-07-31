package id.web.dmalvian.invman;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;

import id.web.dmalvian.invman.adapter.GridAdapter;
import id.web.dmalvian.invman.adapter.ListAdapter;
import id.web.dmalvian.invman.model.QueryFilter;
import id.web.dmalvian.invman.model.Tool;
import id.web.dmalvian.invman.util.VectorHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "MainActivity";
    public static final String ID = "ID";

    private BottomNavigationView navigation;
    private MenuItem itemCategory;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private RecyclerView rvTools;

    private FirebaseFirestore db;
    private CollectionReference toolsRef;
    private QueryFilter filter;

    private ListAdapter listAdapter;
    private GridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        itemCategory = navigation.getMenu().getItem(1);

        progressBar = findViewById(R.id.toolbar_progress_bar);
        tvEmpty = findViewById(R.id.tv_empty_data);

        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        toolsRef = db.collection("Tools");

        filter = new QueryFilter();
        filter.setSortOrder("newest");
        filter.setView(QueryFilter.LIST);

        initRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (filter.getView() == QueryFilter.LIST) {
            listAdapter.startListening();
        }
        else {
            gridAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (filter.getView() == QueryFilter.LIST) {
            listAdapter.stopListening();
        }
        else {
            gridAdapter.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
            searchIntent.putExtra(SearchActivity.RESULT_SEARCH_BY, filter.getSearchBy());
            searchIntent.putExtra(SearchActivity.RESULT_KEYWORD, filter.getKeyword());
            startActivityForResult(searchIntent, SearchActivity.REQ_CODE);
            return true;
        }
        else if (id == R.id.action_view) {
            Intent viewIntent = new Intent(MainActivity.this, ViewTypeActivity.class);
            startActivityForResult(viewIntent, ViewTypeActivity.REQ_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            profileDialog();
        }
        else if (id == R.id.nav_contact) {
            dialPhoneNumber(getString(R.string.info_phone));
        }
        else if (id == R.id.nav_about) {
            aboutDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if (navigation.getSelectedItemId() == R.id.navigation_category) {
                        itemCategory.setTitle(getString(R.string.label_category));
                        if (filter.getCategory() != null) {
                            filter.setCategory(null);
                            initRecyclerView();
                        }
                    }
                    return true;
                case R.id.navigation_category:
                    Intent categoryIntent = new Intent(MainActivity.this, CategoryActivity.class);
                    startActivityForResult(categoryIntent, CategoryActivity.REQ_CODE);
                    return true;
                case R.id.navigation_create:
                    Intent intentCreate = new Intent(MainActivity.this, CreateActivity.class);
                    startActivity(intentCreate);
                    return false;
            }
            return false;
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CategoryActivity.REQ_CODE:
                if (resultCode == RESULT_OK) {
                    String category = data.getStringExtra(CategoryActivity.RESULT);
                    if (filter.getCategory() != category) {
                        itemCategory.setTitle(data.getStringExtra(CategoryActivity.RESULT_TITLE));
                        filter.setCategory(category);
                        initRecyclerView();
                    }
                }
                else if (resultCode == RESULT_CANCELED && filter.getCategory() == null) {
                    navigation.setSelectedItemId(R.id.navigation_home);
                }
                return;
            case SearchActivity.REQ_CODE:
                if (resultCode == RESULT_OK) {
                    String searchBy = data.getStringExtra(SearchActivity.RESULT_SEARCH_BY);
                    String keyword = data.getStringExtra(SearchActivity.RESULT_KEYWORD);
                    setSearch(searchBy, keyword);
                }
                return;
            case ViewTypeActivity.REQ_CODE:
                if (resultCode == RESULT_OK) {
                    int viewType = Integer.parseInt(data.getStringExtra(ViewTypeActivity.RESULT));
                    if (filter.getView() != viewType) {
                        filter.setView(viewType);
                        initRecyclerView();
                    }
                }
                return;
        }
    }

    private void showProgressBar(Boolean status) {
        if (status) {
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showEmptyText(Boolean status) {
        if (status) {
            tvEmpty.setVisibility(View.VISIBLE);
        }
        else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void initRecyclerView() {
        showProgressBar(true);

        if (filter.getView() == QueryFilter.LIST) {
            listAdapter = new ListAdapter(filter.getQueryOptions(toolsRef)) {
                @Override
                public void onDataChanged() {
                    showProgressBar(false);
                    if (getItemCount() == 0) {
                        showEmptyText(true);
                    }
                    else {
                        showEmptyText(false);
                    }
                }
            };

            rvTools = findViewById(R.id.rv_tools);
            rvTools.setLayoutManager(new LinearLayoutManager(this));
            rvTools.setAdapter(listAdapter);

            listAdapter.setOnItemClickListener(new ListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(DocumentSnapshot documentSnapshot, int postition) {
                    String id = documentSnapshot.getId();

                    Intent intentView = new Intent(MainActivity.this, ViewActivity.class);
                    intentView.putExtra(ID, id);
                    startActivity(intentView);
                }
            });
            listAdapter.startListening();
        }
        else {
            gridAdapter = new GridAdapter(filter.getQueryOptions(toolsRef)){
                @Override
                public void onDataChanged() {
                    showProgressBar(false);
                    if (getItemCount() == 0) {
                        showEmptyText(true);
                    }
                    else {
                        showEmptyText(false);
                    }
                }
            };

            rvTools = findViewById(R.id.rv_tools);
            rvTools.setLayoutManager(new GridLayoutManager(this, 2));
            rvTools.setAdapter(gridAdapter);

            gridAdapter.setOnItemClickListener(new GridAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(DocumentSnapshot documentSnapshot, int postition) {
                    String id = documentSnapshot.getId();

                    Intent intentView = new Intent(MainActivity.this, ViewActivity.class);
                    intentView.putExtra(ID, id);
                    startActivity(intentView);
                }
            });
            gridAdapter.startListening();
        }

        showProgressBar(false);
    }

    private void setSearch(String searchBy, String keyword) {
        if (searchBy != filter.getSearchBy() && keyword != filter.getKeyword()) {
            if (!TextUtils.isEmpty(keyword)) {
                setTitle("Cari: " + keyword);
                if (searchBy.equals("titleLower")) {
                    filter.setKeyword(keyword.toLowerCase());
                }
                else {
                    filter.setKeyword(keyword);
                }
                filter.setSearchBy(searchBy);
                initRecyclerView();
            }
            else {
                if (filter.getKeyword() != null) {
                    setTitle(getString(R.string.app_name));
                    filter.setKeyword(null);
                    initRecyclerView();
                }
            }
        }
    }

    public void aboutDialog() {
        final Dialog about = new Dialog(this);
        about.requestWindowFeature(Window.FEATURE_NO_TITLE);
        about.setContentView(R.layout.dialog_about);
        about.setTitle("About");

        Button btnClose = about.findViewById(R.id.btn_close);
        btnClose.setEnabled(true);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about.cancel();
            }
        });

        about.show();
    }

    public void profileDialog() {
        final Dialog profile = new Dialog(this);
        profile.requestWindowFeature(Window.FEATURE_NO_TITLE);
        profile.setContentView(R.layout.dialog_profile);
        profile.setTitle("Profile");

        Button btnClose = profile.findViewById(R.id.btn_close);
        btnClose.setEnabled(true);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.cancel();
            }
        });

        profile.show();
    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}
