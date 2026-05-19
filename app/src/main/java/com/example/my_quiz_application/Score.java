package com.example.my_quiz_application;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Score extends AppCompatActivity {

    Button bLogout, bTry, bChatIA;  // ← AJOUTÉ bChatIA
    ProgressBar progressBar;
    TextView tvScore;
    ImageView ivScore;
    int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        // Initialisation des vues
        tvScore = (TextView) findViewById(R.id.tvScore);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        bLogout = (Button) findViewById(R.id.bLogout);
        bTry = (Button) findViewById(R.id.bTry);
        ivScore = (ImageView) findViewById(R.id.ivScore);
        bChatIA = (Button) findViewById(R.id.bChatIA);  // ← AJOUTÉ

        // Afficher l'image de score
        ivScore.setImageResource(R.drawable.images);

        // Récupérer le score final
        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);

        // Calculer et afficher le pourcentage
        int pourcentage = 100 * score / 5;
        progressBar.setProgress(pourcentage);
        tvScore.setText(score + " / 5");

        // Gestion du bouton Logout
        bLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        "Merci de votre Participation !", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Score.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        // Gestion du bouton Try again
        bTry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Score.this, QuizActivity.class);
                intent.putExtra("quizNumber", 1);
                intent.putExtra("score", 0);
                startActivity(intent);
                finish();
            }
        });

        // 🤖 Gestion du bouton Poser une question à l'IA (AJOUTÉ)
        bChatIA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Score.this, ChatActivity.class);
                startActivity(intent);
            }
        });
    }
}