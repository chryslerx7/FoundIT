package com.example.foundit;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.*;
import androidx.activity.OnBackPressedCallback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.View;
import android.widget.*;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foundit.adapter.ItemImageAdapter;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ReportActivity extends BaseActivity {
    EditText name, desc, location, date, contact;
    Spinner category;
    Button photoBtn, submit, cancel;
    TextView tvPhotoCount;
    RecyclerView recyclerPhotos;
    ItemImageAdapter imageAdapter;

    List<Uri> selectedImageUris = new ArrayList<>();
    Uri photoUri;
    String type;
    int editItemId = -1;
    String initName, initDesc, initLoc, initDate, initContact, initCat;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_report);
        applyWindowInsets(findViewById(R.id.rootReportLayout));

        type = getIntent().getStringExtra("type");
        if(type == null) type = "LOST";
        ((TextView)findViewById(R.id.tvReportTitle)).setText(
                "LOST".equals(type) ? "Report Lost Item" : "Report Found Item");

        name = findViewById(R.id.etItemName);
        desc = findViewById(R.id.etDescription);
        location = findViewById(R.id.etLocation);
        date = findViewById(R.id.etDate);
        contact = findViewById(R.id.etContact);
        category = findViewById(R.id.spCategory);
        photoBtn = findViewById(R.id.btnPhoto);
        submit = findViewById(R.id.btnSubmit);
        cancel = findViewById(R.id.btnCancelReport);
        tvPhotoCount = findViewById(R.id.tvPhotoCount);
        recyclerPhotos = findViewById(R.id.recyclerReportPhotos);

        recyclerPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ItemImageAdapter(this, true, new ItemImageAdapter.OnItemClickListener() {
            @Override public void onItemClick(Object item, int position) {}
            @Override public void onRemoveClick(int position) {
                if (position >= 0 && position < selectedImageUris.size()) {
                    selectedImageUris.remove(position);
                    updatePhotoUI();
                }
            }
        });
        recyclerPhotos.setAdapter(imageAdapter);

        date.setFocusable(false);
        date.setClickable(true);
        date.setOnClickListener(v -> showDatePicker());

        if (getIntent().getIntExtra("edit_item_id", -1) == -1) {
            Calendar cal = Calendar.getInstance();
            updateDateLabel(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        }

        setupBottomNavigation(-1);

        photoBtn.setOnClickListener(v -> {
            if (selectedImageUris.size() >= 5) {
                toast("Maximum 5 photos allowed.");
                return;
            }
            pickImage();
        });
        submit.setOnClickListener(v -> submit());
        cancel.setOnClickListener(v -> handleCancel());

        editItemId = getIntent().getIntExtra("edit_item_id", -1);
        if (editItemId != -1) {
            ((TextView)findViewById(R.id.tvReportTitle)).setText("Edit Report");
            submit.setText("UPDATE REPORT");
            initName = getIntent().getStringExtra("name");
            initDesc = getIntent().getStringExtra("description");
            initLoc = getIntent().getStringExtra("location");
            initDate = getIntent().getStringExtra("date");
            initContact = getIntent().getStringExtra("contact");
            initCat = getIntent().getStringExtra("category");
            type = getIntent().getStringExtra("type");

            name.setText(initName);
            desc.setText(initDesc);
            location.setText(initLoc);
            date.setText(initDate);
            contact.setText(initContact);
            setSpinnerSelection(category, initCat);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                handleCancel();
            }
        });

        updatePhotoUI();
    }

    private void updatePhotoUI() {
        int count = selectedImageUris.size();
        tvPhotoCount.setText(count + "/5 photos");
        if (count > 0) {
            recyclerPhotos.setVisibility(View.VISIBLE);
            imageAdapter.setUriItems(selectedImageUris);
        } else {
            recyclerPhotos.setVisibility(View.GONE);
        }
    }

    private boolean hasChanges() {
        if (editItemId == -1) {
            return !name.getText().toString().isEmpty() ||
                    !desc.getText().toString().isEmpty() ||
                    !location.getText().toString().isEmpty() ||
                    !selectedImageUris.isEmpty();
        }
        return !name.getText().toString().equals(initName) ||
                !desc.getText().toString().equals(initDesc) ||
                !location.getText().toString().equals(initLoc) ||
                !date.getText().toString().equals(initDate) ||
                !contact.getText().toString().equals(initContact) ||
                !category.getSelectedItem().toString().equals(initCat) ||
                !selectedImageUris.isEmpty();
    }

    private void handleCancel() {
        if (hasChanges()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Cancel Report?")
                    .setMessage("Your changes will be lost if you leave this screen.")
                    .setNegativeButton("Keep Editing", null)
                    .setPositiveButton("Cancel", (d, w) -> finish())
                    .show();
        } else {
            finish();
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (editItemId != -1 || !date.getText().toString().isEmpty()) {
            try {
                String[] parts = date.getText().toString().split("-");
                if (parts.length == 3) {
                    cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
                }
            } catch (Exception ignored) {}
        }
        new DatePickerDialog(this, (view, year, month, day) -> updateDateLabel(year, month, day),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel(int year, int month, int day) {
        date.setText(String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day));
    }

    private void setSpinnerSelection(Spinner s, String value) {
        for (int i = 0; i < s.getCount(); i++) {
            if (s.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                s.setSelection(i); break;
            }
        }
    }

    private void pickImage() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                }).show();
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("image/*");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, 100);
    }

    private void openCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            try {
                File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File f = File.createTempFile("IMG_", ".jpg", dir);
                photoUri = FileProvider.getUriForFile(this, "com.example.foundit.fileprovider", f);
            } catch (IOException e) {
                toast("Could not create image file.");
            }
            if (photoUri != null) {
                i.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(i, 101);
            }
        } else {
            toast("Camera app not found.");
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                if (data != null) {
                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            if (selectedImageUris.size() < 5) {
                                selectedImageUris.add(uri);
                            }
                        }
                    } else if (data.getData() != null) {
                        if (selectedImageUris.size() < 5) {
                            selectedImageUris.add(data.getData());
                        }
                    }
                }
            } else if (requestCode == 101) {
                if (photoUri != null && selectedImageUris.size() < 5) {
                    selectedImageUris.add(photoUri);
                }
            }
            updatePhotoUI();
        }
    }

    private RequestBody text(String value) {
        return RequestBody.create(value == null ? "" : value, MediaType.parse("text/plain"));
    }

    private void submit() {
        if(name.getText().toString().trim().isEmpty() || location.getText().toString().trim().isEmpty()) {
            toast("Item name and location are required.");
            return;
        }

        submit.setEnabled(false);
        toast("Processing images...");

        // Process images off the UI thread
        Executors.newSingleThreadExecutor().execute(() -> {
            List<MultipartBody.Part> imageParts = new ArrayList<>();
            MultipartBody.Part legacyPart = null;

            try {
                for (int i = 0; i < selectedImageUris.size(); i++) {
                    Uri uri = selectedImageUris.get(i);
                    byte[] compressedBytes = compressImageToBytes(uri);
                    if (compressedBytes != null) {
                        RequestBody body = RequestBody.create(compressedBytes, MediaType.parse("image/jpeg"));
                        String filename = "item_" + i + ".jpg";
                        MultipartBody.Part part = MultipartBody.Part.createFormData("images[]", filename, body);
                        imageParts.add(part);

                        if (i == 0) {
                            legacyPart = MultipartBody.Part.createFormData("image", filename, body);
                        }
                    }
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    submit.setEnabled(true);
                    toast("Could not process images.");
                });
                return;
            }

            final MultipartBody.Part finalLegacyPart = legacyPart;
            final List<MultipartBody.Part> finalImageParts = imageParts;

            new Handler(Looper.getMainLooper()).post(() -> executeApiCall(finalLegacyPart, finalImageParts));
        });
    }

    private byte[] compressImageToBytes(Uri uri) throws IOException {
        InputStream in = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        if (in != null) in.close();

        if (bitmap == null) return null;

        // Resize if too large (max dimension 1280px)
        int maxDim = 1280;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > maxDim || height > maxDim) {
            float ratio = (float) width / height;
            if (width > height) {
                width = maxDim;
                height = (int) (maxDim / ratio);
            } else {
                height = maxDim;
                width = (int) (maxDim * ratio);
            }
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        return out.toByteArray();
    }

    private void executeApiCall(MultipartBody.Part legacyPart, List<MultipartBody.Part> imageParts) {
        Callback<ItemResponse> cb = new Callback<ItemResponse>() {
            @Override public void onResponse(Call<ItemResponse> c, Response<ItemResponse> r) {
                submit.setEnabled(true);
                if(r.isSuccessful()) {
                    toast(editItemId != -1 ? "Report updated." : "Report submitted.");
                    finish();
                } else {
                    if (r.code() == 403) toast("Unauthorized: You do not own this report.");
                    else toast("Operation failed.");
                }
            }
            @Override public void onFailure(Call<ItemResponse> c, Throwable t) {
                submit.setEnabled(true);
                toast("Connection failed: "+t.getMessage());
            }
        };

        if (editItemId != -1) {
            RetrofitClient.api().updateItem(
                    session.authHeader(),
                    editItemId,
                    text("PUT"),
                    text(name.getText().toString().trim()),
                    text(category.getSelectedItem().toString()),
                    text(desc.getText().toString().trim()),
                    text(location.getText().toString().trim()),
                    text(date.getText().toString().trim()),
                    text(type),
                    text(contact.getText().toString().trim()),
                    legacyPart,
                    imageParts
            ).enqueue(cb);
        } else {
            RetrofitClient.api().createItem(
                    session.authHeader(),
                    text(name.getText().toString().trim()),
                    text(category.getSelectedItem().toString()),
                    text(desc.getText().toString().trim()),
                    text(location.getText().toString().trim()),
                    text(date.getText().toString().trim()),
                    text(type),
                    text(contact.getText().toString().trim()),
                    legacyPart,
                    imageParts
            ).enqueue(cb);
        }
    }
}
