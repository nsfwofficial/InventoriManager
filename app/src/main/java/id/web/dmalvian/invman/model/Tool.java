package id.web.dmalvian.invman.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.Map;

public class Tool {
    private String title;
    private String titleLower;
    private String stockCode;
    private String partNumber;
    private String mnemonic;
    private String site;
    private String availability;
    private String category;
    private String description;
    private Map<String, String> images;
    @ServerTimestamp
    private Timestamp timeStamp;

    public Tool() {
        // Needed by Firebase
    }

    public Tool(String title,
                String titleLower,
                String stockCode,
                String partNumber,
                String mnemonic,
                String site,
                String availability,
                String category,
                String description,
                Map<String, String> images) {
        this.title = title;
        this.titleLower = titleLower;
        this.stockCode = stockCode;
        this.partNumber = partNumber;
        this.mnemonic = mnemonic;
        this.site = site;
        this.availability = availability;
        this.category = category;
        this.description = description;
        this.images = images;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleLower() {
        return titleLower;
    }

    public String getStockCode() {
        return stockCode;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public String getSite() {
        return site;
    }

    public String getAvailability() {
        return availability;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getImages() {
        return images;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

}
