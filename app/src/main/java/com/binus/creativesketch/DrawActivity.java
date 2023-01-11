package com.binus.creativesketch;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

public class DrawActivity extends AppCompatActivity {
    private ImageButton btnBack, btnUndo, btnRedo, btnErase, btnBrush, btnSave, btnUpload;
    private DrawingView drawingView;
    private ImageButton mImageButtonCurrentPaint;
    private Dialog customProgressDialog;


    ActivityResultLauncher<Intent> openGalleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                ImageView imageBackground = findViewById(R.id.ivBackground);
                imageBackground.setImageURI(result.getData().getData());
            }
        }
    });

    ActivityResultLauncher<String[]> reqPermission = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> permissions) {
            for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                String permissionName = entry.getKey();
                boolean isGranted = entry.getValue();
                if (isGranted) {
                    Toast.makeText(DrawActivity.this, "Permission granted", Toast.LENGTH_LONG).show();
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    openGalleryLauncher.launch(pickIntent);
                } else {
                    if (permissionName.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        Toast.makeText(DrawActivity.this, "Permission denied", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    });

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        if(savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String method = extras.getString("requestStoragePermission");
                if (method.equals("requestStoragePermission"))
                {
                    requestStoragePermission();
                }
            }
        }

        btnBack = this.findViewById(R.id.ibBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent
                        (DrawActivity.this, MainActivity.class));
                finish();
            }
        });

        drawingView = findViewById(R.id.drawingView);
        drawingView.setBrushSize(10);

        btnUndo = findViewById(R.id.ibUndo);
        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.onClickUndo();
            }
        });

        btnRedo = findViewById(R.id.ibRedo);
        btnRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.onClickRedo();
            }
        });

        btnErase = findViewById(R.id.ibEraser);
        btnErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.onClickErase();
            }
        });

        btnBrush = findViewById(R.id.ibBrush);
        btnBrush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBrushSizeChooserDialog();
            }
        });

        btnUpload = findViewById(R.id.ibUpload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestStoragePermission();
            }
        });

        btnSave = findViewById(R.id.ibSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isReadStorageAllowed()) {
                    showProgressDialog();
                    FrameLayout flDrawingView = findViewById(R.id.flDrawViewContainer);
                    Bitmap myBitmap = getBitmapFromView(flDrawingView);
                    try {
                        saveBitmapFile(myBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void showBrushSizeChooserDialog() {
        Dialog brushDialog = new Dialog(this);
        brushDialog.setContentView(R.layout.dialog_brush_size);
        brushDialog.setTitle("Brush size = ");

        ImageButton smallBtn = brushDialog.findViewById(R.id.ibSmallBrush);
        smallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.setSizeForBrush(10f);
                brushDialog.dismiss();
            }
        });

        ImageButton mediumBtn = brushDialog.findViewById(R.id.ibMediumBrush);
        mediumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.setSizeForBrush(20f);
                brushDialog.dismiss();
            }
        });

        ImageButton largeBtn = brushDialog.findViewById(R.id.ibLargeBrush);
        largeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.setSizeForBrush(30f);
                brushDialog.dismiss();
            }
        });

        brushDialog.show();
    }


    public void paintClicked(View view) {
        if (view != mImageButtonCurrentPaint) {
            ImageButton imageButton = (ImageButton) view;
            String colorTag = imageButton.getTag().toString();
            drawingView.setColor(colorTag);

            mImageButtonCurrentPaint = (ImageButton) view;
        }
    }

    private boolean isReadStorageAllowed() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )) {
            showRationaleDialog("Creative Sketch App", "Creative Sketch App " + "needs to Access Your External Storage");
        } else {
            reqPermission.launch(new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            });
        }
    }

    private void showRationaleDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    private String saveBitmapFile(Bitmap mBitmap) throws Exception {
        String result = "";
        if (mBitmap != null) {
            try {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                File f = new File(getExternalCacheDir().getAbsoluteFile() + File.separator + "KidsDrawingApp_" + System.currentTimeMillis() / 1000 + ".png");
                FileOutputStream fo = new FileOutputStream(f);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 90, fo);
                fo.write(bytes.toByteArray());
                fo.close();
                result = f.getAbsolutePath();
                String finalResult = result;

                cancelProgressDialog();
                if (!finalResult.isEmpty()) {
                    Toast.makeText(DrawActivity.this, "File Saved: " + finalResult, Toast.LENGTH_SHORT).show();
                    shareImage(finalResult);
                } else {
                    Toast.makeText(DrawActivity.this, "Can't saved file", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                result = "";
                e.printStackTrace();
            }
        }
        return result;
    }


    private void showProgressDialog() {
        customProgressDialog = new Dialog(this);
        customProgressDialog.setContentView(R.layout.dialog_custom_progress);
        customProgressDialog.show();
    }

    private void cancelProgressDialog() {
        if (customProgressDialog != null) {
            customProgressDialog.dismiss();
            customProgressDialog = null;
        }
    }

    private void shareImage(String result) {
        MediaScannerConnection.scanFile(this, new String[]{result}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/png");
                startActivity(Intent.createChooser(shareIntent, "Share"));
            }
        });
    }


}