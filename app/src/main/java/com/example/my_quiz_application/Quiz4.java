package com.example.my_quiz_application;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class Quiz4 extends AppCompatActivity {

    RadioGroup rg;
    RadioButton rb;
    Button bNext;
    ImageView ivQuiz4;
    int score;
    String RepCorrect = "Avant la crème hydratante";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz4);

        rg = (RadioGroup) findViewById(R.id.rg);
        bNext = (Button) findViewById(R.id.bNext);
        ivQuiz4 = (ImageView) findViewById(R.id.ivQuiz4);

        // Load image using Glide
        Glide.with(this)
                .load(R.drawable.q4)
                .into(ivQuiz4);

        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);

        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rg.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getApplicationContext(), "Merci de choisir une réponse !", Toast.LENGTH_SHORT).show();
                } else {
                    rb = (RadioButton) findViewById(rg.getCheckedRadioButtonId());
                    if(rb.getText().toString().trim().equalsIgnoreCase(RepCorrect)){
                        score += 1;
                    }
                    Intent intent = new Intent(Quiz4.this, Quiz5.class);
                    intent.putExtra("score", score);
                    startActivity(intent);
                    overridePendingTransition(R.anim.exit, R.anim.entry);
                    finish();
                }
            }
        });
    }
}