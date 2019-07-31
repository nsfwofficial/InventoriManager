package id.web.dmalvian.invman;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import id.web.dmalvian.invman.model.Filter;

public class ViewTypeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ListView lstSort;
    private ArrayAdapter<Filter> adapter;

    public static final String RESULT = "viewType";
    public static final int REQ_CODE = 21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_type);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lstSort = findViewById(R.id.lst_sort);
        initAdapter();
        lstSort.setAdapter(adapter);
        lstSort.setOnItemClickListener(this);
    }

    private void initAdapter() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        adapter.add(new Filter("Tampilan List", "1"));
        adapter.add(new Filter("Tampilan Grid", "2"));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent result = new Intent();
        result.putExtra(RESULT, adapter.getItem(position).getValue());
        setResult(RESULT_OK, result);
        finish();
    }

}
