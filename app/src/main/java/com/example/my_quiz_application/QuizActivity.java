package com.example.my_quiz_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.firebase.database.*;

public class QuizActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private Button buttonNext;
    private TextView textQuestion, textTitle;
    private ImageView imageView;
    private int score = 0;
    private int quizNumber;
    private String correctAnswer;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        radioGroup = findViewById(R.id.rg);
        buttonNext = findViewById(R.id.bNext);
        textQuestion = findViewById(R.id.tvQuestion);
        textTitle = findViewById(R.id.tvQuizTitle);
        imageView = findViewById(R.id.ivImage);

        Intent intent = getIntent();
        quizNumber = intent.getIntExtra("quizNumber", 1);
        score = intent.getIntExtra("score", 0);

        textTitle.setText("Quiz " + quizNumber);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // ✅ DEMANDE LA PERMISSION GPS À CHAQUE FOIS (même si déjà accordée)
        demanderPermissionGPS();

        loadQuestion();
        buttonNext.setOnClickListener(v -> checkAnswer());
    }

    private void demanderPermissionGPS() {
        // Vérifie si la permission est déjà accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission déjà accordée, on récupère la position
            Toast.makeText(this, "📍 GPS déjà autorisé, récupération de la position...", Toast.LENGTH_SHORT).show();
            obtenirPositionGPS();
        } else {
            // Permission pas encore accordée, on la demande
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void obtenirPositionGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // On tente d'abord de récupérer la dernière position connue
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                afficherToastPosition(location);
            } else {
                // Si la dernière position est nulle, on demande la position actuelle en temps réel
                demanderPositionActuelle();
            }
        });
    }

    private void demanderPositionActuelle() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Force la détection GPS immédiate
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationTokenSource().getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        afficherToastPosition(location);
                    } else {
                        Toast.makeText(QuizActivity.this, "📍 GPS actif mais position non détectée.\nAssurez-vous d'être à l'extérieur ou d'activer la position sur l'émulateur.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(QuizActivity.this, "Erreur GPS : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void afficherToastPosition(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        Toast.makeText(QuizActivity.this,
                "📍 Position détectée :\nLat: " + lat + "\nLon: " + lon,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Permission GPS accordée !", Toast.LENGTH_SHORT).show();
                obtenirPositionGPS();
            } else {
                Toast.makeText(this, "❌ Permission GPS refusée. La position ne peut pas être affichée.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadQuestion() {
        String dbUrl = "https://myquizapplication-74579-default-rtdb.europe-west1.firebasedatabase.app";
        DatabaseReference ref = FirebaseDatabase.getInstance(dbUrl).getReference("quizzes/quiz" + quizNumber);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    textQuestion.setText(snapshot.child("question").getValue(String.class));

                    if (snapshot.hasChild("image")) {
                        String nomImage = snapshot.child("image").getValue(String.class);
                        if (nomImage != null && !nomImage.isEmpty()) {
                            String cleanName = nomImage.contains(".") ? nomImage.substring(0, nomImage.lastIndexOf(".")) : nomImage;
                            int resId = getResources().getIdentifier(cleanName, "drawable", getPackageName());
                            if (resId != 0) {
                                imageView.setVisibility(View.VISIBLE);
                                Glide.with(QuizActivity.this).load(resId).override(800, 800).into(imageView);
                            } else {
                                imageView.setVisibility(View.GONE);
                            }
                        } else {
                            imageView.setVisibility(View.GONE);
                        }
                    } else {
                        imageView.setVisibility(View.GONE);
                    }

                    correctAnswer = snapshot.child("bonne_reponse").getValue(String.class);
                    radioGroup.removeAllViews();

                    DataSnapshot reponsesSnap = snapshot.child("reponses");
                    if (reponsesSnap.exists()) {
                        for (DataSnapshot child : reponsesSnap.getChildren()) {
                            String value = child.getValue(String.class);
                            if (value != null) {
                                RadioButton rb = new RadioButton(QuizActivity.this);
                                rb.setText(value);
                                radioGroup.addView(rb);
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void checkAnswer() {
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Choisissez une réponse", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selected = findViewById(radioGroup.getCheckedRadioButtonId());
        if (selected.getText().toString().equals(correctAnswer)) {
            score++;
        }

        if (quizNumber < 5) {
            Intent intent = new Intent(QuizActivity.this, QuizActivity.class);
            intent.putExtra("quizNumber", quizNumber + 1);
            intent.putExtra("score", score);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(QuizActivity.this, Score.class);
            intent.putExtra("score", score);
            startActivity(intent);
            finish();
        }
    }
}