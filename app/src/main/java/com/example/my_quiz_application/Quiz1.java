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

public class Quiz1 extends AppCompatActivity {

    RadioGroup rg;
    RadioButton rb;
    Button bNext;
    ImageView ivQuiz1;
    int score;
    String RepCorrect = "Plus grasse que les joues";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz1);

        rg = (RadioGroup) findViewById(R.id.rg);
        bNext = (Button) findViewById(R.id.bNext);
        ivQuiz1 = (ImageView) findViewById(R.id.ivQuiz1);

        // Load image using Glide to avoid black screen/memory issues
        Glide.with(this)
                .load(R.drawable.q1)
                .into(ivQuiz1);

        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);

        bNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rg.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getApplicationContext(),
                            "Merci de choisir une réponse S.V.P !", Toast.LENGTH_SHORT).show();
                }
                else {
                    rb = (RadioButton) findViewById(rg.getCheckedRadioButtonId());
                    if(rb.getText().toString().trim().equals(RepCorrect)){
                        score += 1;
                    }

                    Intent intent = new Intent(Quiz1.this, Quiz2.class);
                    intent.putExtra("score", score);
                    startActivity(intent);
                    overridePendingTransition(R.anim.exit, R.anim.entry);
                    finish();
                }
            }
        });
    }
}