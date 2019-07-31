package id.web.dmalvian.invman;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import id.web.dmalvian.invman.model.Tool;
import id.web.dmalvian.invman.util.CarouselHelper;
import id.web.dmalvian.invman.util.PauseHelper;

public class CreateActivity extends AppCompatActivity {
    //    Widget
    private EditText edtTitle, edtStockCode, edtPartNumber, edtMnemonic, edtDescription, edtSite, edtAvailability;
    private Spinner spnCategory;
    private ProgressBar progressBar;
    private CarouselView carousel;
    private CarouselHelper carouselHelper;
    private Button btnRmImg, btnChgImg;

    //    Var
    private List<Uri> filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private Map<Integer, Integer> imagesIndex;

    //    Firebase
    private StorageReference storage;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        edtTitle = findViewById(R.id.edt_title);
        edtStockCode = findViewById(R.id.edt_stock_code);
        edtPartNumber = findViewById(R.id.edt_part_number);
        edtMnemonic = findViewById(R.id.edt_mnemonic);
        edtSite = findViewById(R.id.edt_site);
        edtAvailability = findViewById(R.id.edt_availability);
        edtDescription = findViewById(R.id.edt_description);
        spnCategory = findViewById(R.id.spn_category);

        List<String> category = new ArrayList<>();
        category.add("1 Th");
        category.add("2 Th");
        category.add("3 Th");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, category);
        spnCategory.setAdapter(adapter);

        progressBar = findViewById(R.id.toolbar_progress_bar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImages();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storage = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        carousel = findViewById(R.id.carousel);
        btnRmImg = findViewById(R.id.btn_rm_image);
        btnChgImg = findViewById(R.id.btn_change_image);

        filePath = new ArrayList<>();
        imagesIndex = new HashMap<>();
        imagesIndex.put(0, null);
        imagesIndex.put(1, null);
        imagesIndex.put(2, null);
        imagesIndex.put(3, null);

        initCarousel();
    }

    private void initCarousel() {
        carouselHelper = new CarouselHelper(getApplicationContext(), carousel, 4);
        carousel.setImageClickListener(new ImageClickListener() {
            @Override
            public void onClick(int position) {
                zoomPhoto();
            }
        });

        btnChgImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnRmImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imagesIndex.get(carouselHelper.getCurrentItem()) != null) {
                    filePath.remove((int) imagesIndex.get(carouselHelper.getCurrentItem()));
                    imagesIndex.put(carouselHelper.getCurrentItem(), null);
                }
                carouselHelper.getImageView(carouselHelper.getCurrentItem()).setImageResource(R.drawable.ic_insert_photo);
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void showProgressBar(Boolean status) {
        if (status) {
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            if (imagesIndex.get(carouselHelper.getCurrentItem()) != null) {
                filePath.set(imagesIndex.get(carouselHelper.getCurrentItem()), data.getData());
            }
            else {
                imagesIndex.put(carouselHelper.getCurrentItem(), filePath.size());
                filePath.add(data.getData());
            }
            Glide.with(getApplicationContext())
                    .load(data.getData())
                    .into(carouselHelper.getImageView(carouselHelper.getCurrentItem()));
        }
    }

    private void zoomPhoto() {
        if (imagesIndex.get(carouselHelper.getCurrentItem()) != null){
            Intent intent = new Intent(CreateActivity.this, ZoomActivity.class);
            intent.putExtra(ZoomActivity.PHOTO_URI, filePath.get(imagesIndex.get(carouselHelper.getCurrentItem())).toString());
            startActivity(intent);
        }
    }

    private void uploadImages() {
        if(filePath.size() != 0) {
            showProgressBar(true);

            Map<String, String> images = new HashMap<>();
            Iterator<Uri> pathIterator = filePath.iterator();

            uploadImage(pathIterator, images);
        }
        else {
            Snackbar.make(progressBar, "Silakan pilih gambar terlebih dahulu.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void uploadImage(final Iterator<Uri> pathIterator, final Map<String, String> images) {
        Uri path = pathIterator.next();
        final String imageRef = UUID.randomUUID().toString();
        final StorageReference ref = storage.child("images/" + imageRef);

        UploadTask uploadTask = ref.putFile(path);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    images.put(imageRef, downloadUri.toString());
                    if (!pathIterator.hasNext()) {
                        showProgressBar(false);
                        saveData(images);
                        finish();
                    }
                    else {
                        uploadImage(pathIterator, images);
                    }
                } else {
                    Snackbar.make(progressBar, "Failed", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    private void saveData(Map<String, String> images) {
        String title = edtTitle.getText().toString();
        String stockCode = edtStockCode.getText().toString();
        String partNumber = edtPartNumber.getText().toString();
        String mnemonic = edtMnemonic.getText().toString();
        String site = edtSite.getText().toString();
        String availability = edtAvailability.getText().toString();
        String category = (String) spnCategory.getSelectedItem();
        String description = edtDescription.getText().toString();

        Tool tool = new Tool(
                title,
                title.toLowerCase(),
                stockCode,
                partNumber,
                mnemonic,
                site,
                availability,
                category,
                description,
                images
        );

        CollectionReference toolsRef = db.collection("Tools");
        toolsRef.add(tool);
    }

}
