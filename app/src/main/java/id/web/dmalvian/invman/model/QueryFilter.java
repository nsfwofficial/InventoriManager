package id.web.dmalvian.invman.model;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

public class QueryFilter {
    public static final int LIST = 1;
    public static final int GRID = 2;
    private String keyword;
    private String category;
    private String sortOrder;
    private String searchBy;
    private int view;

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setSearchBy(String searchBy) {
        this.searchBy = searchBy;
    }

    public int getView() {
        return view;
    }

    public void setView(int view) {
        this.view = view;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getCategory() {
        return category;
    }

    public String getSearchBy() {
        return searchBy;
    }

    private Query.Direction getSortOrder() {
        if (sortOrder != null && sortOrder.equals("oldest")) {
            return Query.Direction.ASCENDING;
        }
        return Query.Direction.DESCENDING;
    }

    public FirestoreRecyclerOptions<Tool> getQueryOptions(CollectionReference ref){
        Query query = ref;

        if (keyword != null) {
            query = query.orderBy(searchBy).startAt(keyword).endAt(keyword + "\uf8ff");
        }
        else {
            query = query.orderBy("timeStamp", getSortOrder());
        }
        if (category != null) {
            query = query.whereEqualTo("category", category);
        }

        FirestoreRecyclerOptions<Tool> options = new FirestoreRecyclerOptions.Builder<Tool>()
                .setQuery(query, Tool.class)
                .build();

        return options;
    }

}
