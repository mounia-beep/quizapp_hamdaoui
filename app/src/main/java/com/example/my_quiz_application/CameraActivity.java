package com.example.my_quiz_application;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private Button btnStart;
    private TextView tvCountdown;
    private boolean isRecording = false;
    private String videoFileName;
    private boolean permissionsGranted = false;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        surfaceView = findViewById(R.id.surfaceView);
        btnStart = findViewById(R.id.btnStart);
        tvCountdown = findViewById(R.id.tvCountdown);
        surfaceHolder = surfaceView.getHolder();

        // Vérifier et demander les permissions
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (hasPermissions()) {
            permissionsGranted = true;
            initCamera();
            enableStartButton();
        } else {
            // Demander les permissions
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean hasPermissions() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void enableStartButton() {
        btnStart.setEnabled(true);
        btnStart.setOnClickListener(v -> {
            btnStart.setVisibility(View.GONE);
            startRecording();
        });
    }

    private void initCamera() {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur caméra", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRecording() {
        if (camera != null) {
            camera.unlock();
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        videoFileName = "quiz_" + System.currentTimeMillis() + ".mp4";
        File videoFile = new File(getExternalFilesDir(null), videoFileName);
        mediaRecorder.setOutputFile(videoFile.getAbsolutePath());

        mediaRecorder.setVideoSize(640, 480);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Toast.makeText(this, "🎥 Caméra + Micro actifs", Toast.LENGTH_SHORT).show();
            startCountdown();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startCountdown() {
        tvCountdown.setVisibility(View.VISIBLE);
        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000 + 1));
            }
            public void onFinish() {
                tvCountdown.setVisibility(View.GONE);
                startQuiz();
            }
        }.start();
    }

    private void startQuiz() {
        Intent intent = new Intent(CameraActivity.this, QuizActivity.class);
        intent.putExtra("quizNumber", 1);
        intent.putExtra("score", 0);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null && isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (hasPermissions()) {
                permissionsGranted = true;
                Toast.makeText(this, "✅ Permissions accordées", Toast.LENGTH_SHORT).show();
                initCamera();
                enableStartButton();
            } else {
                // Une ou plusieurs permissions refusées
                Toast.makeText(this, "❌ Permissions nécessaires (caméra + micro)", Toast.LENGTH_LONG).show();
                // On peut quand même lancer le quiz sans caméra
                btnStart.setEnabled(true);
                btnStart.setOnClickListener(v -> {
                    startQuiz();
                });
            }
        }
    }
}