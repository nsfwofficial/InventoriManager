package id.web.dmalvian.invman;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;
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

public class ViewActivity extends AppCompatActivity {
    //    Widget
    private EditText edtTitle, edtStockCode, edtPartNumber, edtMnemonic, edtDescription, edtSite, edtAvailability;
    private Spinner spnCategory;
    private ProgressBar progressBar;
    private CarouselView carousel;
    private CarouselHelper carouselHelper;
    private Button btnRmImg, btnChgImg;
    FloatingActionButton fab;
    private MenuItem actionEdit;

    //    Var
    private List<Uri> filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private String id;
    private List<String> imageRefUri;
    private List<String> fullImageRefUri;
    private List<String> deletedImageRefUri;
    private List<String> imagesURL;
    private Map<Integer, Integer> imagesIndex;
    private Map<Integer, Integer> eImagesIndex;
    private Boolean editState;

    //    Firebase
    private StorageReference storage;
    private FirebaseFirestore db;
    private DocumentReference toolRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
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

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateImages();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent viewIntent = getIntent();
        id = viewIntent.getStringExtra(MainActivity.ID);

        storage = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        carousel = findViewById(R.id.carousel);
        btnRmImg = findViewById(R.id.btn_rm_image);
        btnChgImg = findViewById(R.id.btn_change_image);

        initCarousel();
        editMode(false);
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_view, menu);
        actionEdit = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            if (!editState) {
                editMode(true);
            }
        }
        else if (id == R.id.action_delete) {
            deleteDialog();
            return true;
        }
        else if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                if (eImagesIndex.get(carouselHelper.getCurrentItem()) != null) {
                    deletedImageRefUri.add(imageRefUri.get(eImagesIndex.get(carouselHelper.getCurrentItem())));
                    imageRefUri.remove((int)  eImagesIndex.get(carouselHelper.getCurrentItem()));
                    imagesURL.remove((int)  eImagesIndex.get(carouselHelper.getCurrentItem()));
                    updateImagesIndex(carouselHelper.getCurrentItem());
                }
                else if (imagesIndex.get(carouselHelper.getCurrentItem()) != null){
                    Toast.makeText(getApplicationContext(), "CEK", Toast.LENGTH_LONG).show();
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

    private void updateImagesIndex(int removed) {
        eImagesIndex.put(removed, null);
        for (int i = removed + 1; i < 4; i++) {
            if (eImagesIndex.get(i) != null) {
                eImagesIndex.put(i, i-1);
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private int getIndex(Spinner spinner, String str) {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++) {
            if (spinner.getItemAtPosition(i).equals(str)) {
                index = i;
            }
        }
        return index;
    }

    private void showProgressBar(Boolean status) {
        if (status) {
            progressBar.setVisibility(View.VISIBLE);
        }
        else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void editMode(Boolean status) {
        editState = status;
        if (status) {
            edtTitle.setInputType(InputType.TYPE_CLASS_TEXT);
            edtStockCode.setInputType(InputType.TYPE_CLASS_NUMBER);
            edtPartNumber.setInputType(InputType.TYPE_CLASS_TEXT);
            edtMnemonic.setInputType(InputType.TYPE_CLASS_TEXT);
            edtSite.setInputType(InputType.TYPE_CLASS_TEXT);
            edtAvailability.setInputType(InputType.TYPE_CLASS_TEXT);
            edtDescription.setInputType(InputType.TYPE_CLASS_TEXT);
            spnCategory.setEnabled(status);

            btnChgImg.setVisibility(View.VISIBLE);
            btnRmImg.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);
            if (actionEdit != null) {
                actionEdit.setVisible(false);
            }
            setTitle("Edit");

            edtTitle.requestFocus();
        }
        else {
            edtTitle.setInputType(InputType.TYPE_NULL);
            edtStockCode.setInputType(InputType.TYPE_NULL);
            edtPartNumber.setInputType(InputType.TYPE_NULL);
            edtMnemonic.setInputType(InputType.TYPE_NULL);
            edtSite.setInputType(InputType.TYPE_NULL);
            edtAvailability.setInputType(InputType.TYPE_NULL);
            edtDescription.setInputType(InputType.TYPE_NULL);
            spnCategory.setEnabled(status);

            btnChgImg.setVisibility(View.INVISIBLE);
            btnRmImg.setVisibility(View.INVISIBLE);
            fab.setVisibility(View.INVISIBLE);
            if (actionEdit != null) {
                actionEdit.setVisible(true);
            }
            setTitle("Detail");
        }
    }

    private void zoomPhoto() {
        if (eImagesIndex.get(carouselHelper.getCurrentItem()) != null) {
            Intent intent = new Intent(ViewActivity.this, ZoomActivity.class);
            intent.putExtra(ZoomActivity.PHOTO_URI, imagesURL.get(eImagesIndex.get(carouselHelper.getCurrentItem())));
            startActivity(intent);
        }
        else if (imagesIndex.get(carouselHelper.getCurrentItem()) != null){
            Intent intent = new Intent(ViewActivity.this, ZoomActivity.class);
            intent.putExtra(ZoomActivity.PHOTO_URI, filePath.get(imagesIndex.get(carouselHelper.getCurrentItem())).toString());
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (editState) {
            discardDialog();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            if (eImagesIndex.get(carouselHelper.getCurrentItem()) != null) {
                deletedImageRefUri.add(imageRefUri.get(eImagesIndex.get(carouselHelper.getCurrentItem())));
                imageRefUri.remove((int) eImagesIndex.get(carouselHelper.getCurrentItem()));
                imagesURL.remove((int) eImagesIndex.get(carouselHelper.getCurrentItem()));
                updateImagesIndex(carouselHelper.getCurrentItem());
                imagesIndex.put(carouselHelper.getCurrentItem(), filePath.size());
                filePath.add(data.getData());
            }
            else if (imagesIndex.get(carouselHelper.getCurrentItem()) != null){
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

    private void resetCarousel() {
        carousel.setCurrentItem(0);
        for (int n = 0;n<4;n++) {
            carouselHelper.getImageView(n).setImageResource(R.drawable.ic_insert_photo);
        }
    }

    private void loadData() {
        editState = false;

        filePath = new ArrayList<>();
        imageRefUri = new ArrayList<>();
        fullImageRefUri = new ArrayList<>();
        deletedImageRefUri = new ArrayList<>();
        imagesURL = new ArrayList<>();
        imagesIndex = new HashMap<>();
        imagesIndex.put(0, null);
        imagesIndex.put(1, null);
        imagesIndex.put(2, null);
        imagesIndex.put(3, null);

        eImagesIndex = new HashMap<>();
        eImagesIndex.put(0, null);
        eImagesIndex.put(1, null);
        eImagesIndex.put(2, null);
        eImagesIndex.put(3, null);

        showProgressBar(true);
        toolRef = db.collection("Tools").document(id);

        toolRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        showProgressBar(false);
                        if (documentSnapshot.exists()) {
                            Tool tool = documentSnapshot.toObject(Tool.class);

                            edtTitle.setText(tool.getTitle());
                            edtStockCode.setText(tool.getStockCode());
                            edtPartNumber.setText(tool.getPartNumber());
                            edtMnemonic.setText(tool.getMnemonic());
                            edtSite.setText(tool.getSite());
                            edtAvailability.setText(tool.getAvailability());
                            spnCategory.setSelection(getIndex(spnCategory, tool.getCategory()));
                            edtDescription.setText(tool.getDescription());
                            int i = 0;
                            for (String uri : tool.getImages().keySet()) {

                                imageRefUri.add(i, uri);
                                fullImageRefUri.add(i, uri);
                                imagesURL.add(i, tool.getImages().get(uri));
                                eImagesIndex.put(i, i);

                                Glide.with(getApplicationContext())
                                        .load(tool.getImages().get(uri))
                                        .into(carouselHelper.getImageView(i));

                                i++;
                            }
                        }
                    }
                });
    }

    private void updateData() {
        String title = edtTitle.getText().toString();
        String stockCode = edtStockCode.getText().toString();
        String partNumber = edtPartNumber.getText().toString();
        String mnemonic = edtMnemonic.getText().toString();
        String site = edtSite.getText().toString();
        String availability = edtAvailability.getText().toString();
        String category = (String) spnCategory.getSelectedItem();
        String description = edtDescription.getText().toString();

        Map<String, Object> tool = new HashMap<>();
        tool.put("title", title);
        tool.put("titleLower", title.toLowerCase());
        tool.put("stockCode", stockCode);
        tool.put("partNumber", partNumber);
        tool.put("mnemonic", mnemonic);
        tool.put("site", site);
        tool.put("availability", availability);
        tool.put("category", category);
        tool.put("description", description);

        toolRef.update(tool);
    }

    private void updateImages() {
        if (filePath.size() != 0 || deletedImageRefUri.size() != 0) {

            if (filePath.size() == 0 && deletedImageRefUri.size() == fullImageRefUri.size()) {
                Snackbar.make(progressBar, "Silakan pilih gambar terlebih dahulu.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            else if (deletedImageRefUri.size() != 0) {
                showProgressBar(true);
                Iterator<String> refIterator = deletedImageRefUri.iterator();

                updateImage(refIterator);
            }
            else if (filePath.size() != 0){
                showProgressBar(true);
                uploadImages();
            }
        }
        else {
            showProgressBar(false);
            updateData();
            editMode(false);
            showSuccessSnackbar();
        }
    }

    private void updateImage(final Iterator<String> refIterator) {
        final String uriRef = refIterator.next();
        StorageReference imageRef = storage.child("images/" + uriRef);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                toolRef.update("images." + uriRef, FieldValue.delete());
                if (!refIterator.hasNext()) {
                    if (filePath.size() != 0) {
                        uploadImages();
                    }
                    else {
                        showProgressBar(false);
                        updateData();
                        editMode(false);
                        showSuccessSnackbar();
                    }
                }
                else {
                    updateImage(refIterator);
                }
            }
        });
    }

    private void uploadImages() {
        Map<String, String> images = new HashMap<>();
        Iterator<Uri> pathIterator = filePath.iterator();

        uploadImage(pathIterator, images);
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
                        Map<String, Object> image = new HashMap<>();
                        image.put("images", images);
                        toolRef.set(image, SetOptions.merge());
                        updateData();
                        editMode(false);
                        showSuccessSnackbar();
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

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.dialog_msg_delete);
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteImages();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void discardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.dialog_msg_discard);
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                editMode(false);
                resetCarousel();
                loadData();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSuccessSnackbar() {
        Snackbar.make(carousel, "Data berhasil diubah.", Snackbar.LENGTH_SHORT).show();
    }


    private void deleteImages() {
        showProgressBar(true);
        Iterator<String> refIterator = fullImageRefUri.iterator();

        deleteImage(refIterator);
    }

    private void deleteImage(final Iterator<String> refIterator) {
        final String uriRef = refIterator.next();
        StorageReference imageRef = storage.child("images/" + uriRef);
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                toolRef.update("images." + uriRef, FieldValue.delete());
                if (!refIterator.hasNext()) {
                    toolRef.delete();
                    showProgressBar(false);
                    finish();
                }
                else {
                    deleteImage(refIterator);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(progressBar, e.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                showProgressBar(false);
            }
        });
    }

}
