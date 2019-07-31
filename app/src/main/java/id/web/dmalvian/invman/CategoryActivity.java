package id.web.dmalvian.invman;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import id.web.dmalvian.invman.model.Filter;

public class CategoryActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ListView lstCategory;
    private ArrayAdapter<Filter> adapter;

    public static final String RESULT = "category";
    public static final String RESULT_TITLE = "title";
    public static final int REQ_CODE = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lstCategory = findViewById(R.id.lst_category);
        initAdapter();
        lstCategory.setAdapter(adapter);
        lstCategory.setOnItemClickListener(this);
    }

    private void initAdapter() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
//        adapter.add(new Filter("Semua Kategori", null));
        adapter.add(new Filter("1 Tahun", "1 Th"));
        adapter.add(new Filter("2 Tahun", "2 Th"));
        adapter.add(new Filter("3 tahun", "3 Th"));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent result = new Intent();
        result.putExtra(RESULT, adapter.getItem(position).getValue());
        result.putExtra(RESULT_TITLE, adapter.getItem(position).toString());
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        setResult(RESULT_CANCELED, result);
        finish();
    }
}
