package id.web.dmalvian.invman;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import id.web.dmalvian.invman.model.Filter;

public class SearchActivity extends AppCompatActivity {
    private ArrayAdapter<Filter> adapter;

    public static final String RESULT_SEARCH_BY = "searchBy";
    public static final String RESULT_KEYWORD = "keyword";
    public static final int REQ_CODE = 25;

    private Spinner spnSearchBy;
    private EditText edtKeyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSearch(view);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spnSearchBy = findViewById(R.id.spn_search_by);
        edtKeyword = findViewById(R.id.edt_keyword);

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reset) {
            spnSearchBy.setSelection(0);
            edtKeyword.setText("");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        adapter.add(new Filter("Judul", "titleLower"));
        adapter.add(new Filter("Stock Code", "stockCode"));
        spnSearchBy.setAdapter(adapter);

        Intent searchIntent = getIntent();
        String searchBy = searchIntent.getStringExtra(RESULT_SEARCH_BY);
        String keyword = searchIntent.getStringExtra(RESULT_KEYWORD);

        if (keyword != null) {
            spnSearchBy.setSelection(getIndex(spnSearchBy, searchBy));
            edtKeyword.setText(keyword);
        }
    }

    private void setSearch(View v) {
        Filter searchBy = (Filter) spnSearchBy.getSelectedItem();
        String keyword = edtKeyword.getText().toString();

        Intent result = new Intent();
        result.putExtra(RESULT_SEARCH_BY, searchBy.getValue());
        result.putExtra(RESULT_KEYWORD, keyword);
        setResult(RESULT_OK, result);
        finish();
    }

    private int getIndex(Spinner spinner, String str) {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++) {
            Filter filter = (Filter) spinner.getItemAtPosition(i);
            if (filter.getValue().equals(str)) {
                index = i;
            }
        }
        return index;
    }

}
