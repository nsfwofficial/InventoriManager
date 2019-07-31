package id.web.dmalvian.invman.model;

public class Filter {
    private String title;
    private String value;

    public Filter(String title, String value) {
        this.title = title;
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return title;
    }
}
