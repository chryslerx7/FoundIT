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
    Uri imageUri;
    String type;
    int editItemId = -1;
    String initName, initDesc, initLoc, initDate, initContact, initCat;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if(!requireLogin()) return;
        setContentView(R.layout.activity_report);

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

    private void setSpinnerSelection(Spinner s, String value) {
        for (int i = 0; i < s.getCount(); i++) {
            if (s.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                s.setSelection(i); break;
            }
        }
    }

    private void pickImage() {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("image/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i,100);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==100&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null) {
            imageUri=data.getData();
            preview.setVisibility(View.VISIBLE);
            preview.setImageURI(imageUri);
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
                } else toast("Operation failed.");
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
