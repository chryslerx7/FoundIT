package com.example.foundit;

import android.content.*;
import androidx.activity.OnBackPressedCallback;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.view.View;
import android.widget.*;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.*;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.*;

public class ReportActivity extends BaseActivity {
    EditText name, desc, location, date, contact;
    Spinner category;
    ImageView preview;
    Button photo, submit, cancel;
    Uri imageUri, photoUri;
    String type;
    int editItemId = -1;
    String initName, initDesc, initLoc, initDate, initContact, initCat;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_report);
        applyWindowInsets(findViewById(R.id.rootReportLayout));

        type=getIntent().getStringExtra("type");
        if(type==null) type="LOST";
        ((TextView)findViewById(R.id.tvReportTitle)).setText(
                "LOST".equals(type)?"Report Lost Item":"Report Found Item");

        name=findViewById(R.id.etItemName);
        desc=findViewById(R.id.etDescription);
        location=findViewById(R.id.etLocation);
        date=findViewById(R.id.etDate);
        contact=findViewById(R.id.etContact);
        category=findViewById(R.id.spCategory);
        preview=findViewById(R.id.imgPreview);
        photo=findViewById(R.id.btnPhoto);
        submit=findViewById(R.id.btnSubmit);
        cancel=findViewById(R.id.btnCancelReport);

        date.setFocusable(false);
        date.setClickable(true);
        date.setOnClickListener(v -> showDatePicker());

        if (getIntent().getIntExtra("edit_item_id", -1) == -1) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            updateDateLabel(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH));
        }

        setupBottomNavigation(-1);

        photo.setOnClickListener(v->pickImage());
        submit.setOnClickListener(v->submit());
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
    }

    private boolean hasChanges() {
        if (editItemId == -1) {
            return !name.getText().toString().isEmpty() ||
                    !desc.getText().toString().isEmpty() ||
                    !location.getText().toString().isEmpty() ||
                    imageUri != null;
        }
        return !name.getText().toString().equals(initName) ||
                !desc.getText().toString().equals(initDesc) ||
                !location.getText().toString().equals(initLoc) ||
                !date.getText().toString().equals(initDate) ||
                !contact.getText().toString().equals(initContact) ||
                !category.getSelectedItem().toString().equals(initCat) ||
                imageUri != null;
    }

    private void handleCancel() {
        if (hasChanges()) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("You have unsaved changes. Are you sure you want to cancel?")
                    .setPositiveButton("Discard", (d, w) -> finish())
                    .setNegativeButton("Keep Editing", null)
                    .show();
        } else {
            finish();
        }
    }

    private void showDatePicker() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        if (editItemId != -1 || !date.getText().toString().isEmpty()) {
            try {
                String[] parts = date.getText().toString().split("-");
                if (parts.length == 3) {
                    cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
                }
            } catch (Exception ignored) {}
        }
        new android.app.DatePickerDialog(this, (view, year, month, day) -> updateDateLabel(year, month, day),
                cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateLabel(int year, int month, int day) {
        date.setText(String.format(java.util.Locale.US, "%d-%02d-%02d", year, month + 1, day));
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
        new android.app.AlertDialog.Builder(this)
                .setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                }).show();
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("image/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, 100);
    }

    private void openCamera() {
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            try {
                File dir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
                File f = File.createTempFile("IMG_", ".jpg", dir);
                photoUri = androidx.core.content.FileProvider.getUriForFile(this, "com.example.foundit.fileprovider", f);
            } catch (IOException e) {
                toast("Could not create image file.");
            }
            if (photoUri != null) {
                i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(i, 101);
            }
        } else {
            toast("Camera app not found.");
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100 && data != null && data.getData() != null) {
                imageUri = data.getData();
            } else if (requestCode == 101) {
                imageUri = photoUri;
            }
            if (imageUri != null) {
                preview.setVisibility(View.VISIBLE);
                preview.setImageURI(imageUri);
            }
        }
    }

    private RequestBody text(String value) {
        return RequestBody.create(value==null?"":value, MediaType.parse("text/plain"));
    }

    private void submit() {
        if(name.getText().toString().trim().isEmpty() || location.getText().toString().trim().isEmpty()) {
            toast("Item name and location are required.");
            return;
        }

        MultipartBody.Part part=null;
        if(imageUri!=null) {
            try {
                byte[] bytes=readBytes(imageUri);
                String mime=getContentResolver().getType(imageUri);
                if(mime==null) mime="image/jpeg";
                RequestBody body=RequestBody.create(bytes,MediaType.parse(mime));
                String filename=getFileName(imageUri);
                part=MultipartBody.Part.createFormData("image",filename,body);
            } catch(Exception e) {
                toast("Could not read image.");
                return;
            }
        }

        submit.setEnabled(false);
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
                    part
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
                    part
            ).enqueue(cb);
        }
    }

    private byte[] readBytes(Uri uri) throws IOException {
        InputStream in=getContentResolver().openInputStream(uri);
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] buf=new byte[8192]; int n;
        while((n=in.read(buf))!=-1) out.write(buf,0,n);
        in.close(); return out.toByteArray();
    }

    private String getFileName(Uri uri) {
        Cursor c=getContentResolver().query(uri,null,null,null,null);
        if(c!=null) {
            try {
                int idx=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if(c.moveToFirst()&&idx>=0) return c.getString(idx);
            } finally { c.close(); }
        }
        return "item.jpg";
    }
}
