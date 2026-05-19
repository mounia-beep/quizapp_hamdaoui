package com.example.my_quiz_application;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private EditText etQuestion;
    private Button bEnvoyer, bRetour;
    private ProgressBar progressBar;
    private LinearLayout chatContainer;
    private ScrollView scrollView;
    private RequestQueue requestQueue;

    private static final String API_KEY = BuildConfig.GROQ_API_KEY;
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etQuestion = findViewById(R.id.etQuestion);
        bEnvoyer = findViewById(R.id.bEnvoyer);
        bRetour = findViewById(R.id.bRetour);
        progressBar = findViewById(R.id.progressBar);
        chatContainer = findViewById(R.id.chatContainer);
        scrollView = findViewById(R.id.scrollView);

        requestQueue = Volley.newRequestQueue(this);

        bEnvoyer.setOnClickListener(v -> askToGroq());
        bRetour.setOnClickListener(v -> finish());
    }

    private void askToGroq() {
        String question = etQuestion.getText().toString().trim();

        if (question.isEmpty()) {
            Toast.makeText(this, "Écris ta question", Toast.LENGTH_SHORT).show();
            return;
        }

        // Afficher la question de l'utilisateur
        ajouterMessage("👤 **Vous** :\n" + question, "#E3F2FD");

        // Effacer le champ de texte
        etQuestion.setText("");

        progressBar.setVisibility(View.VISIBLE);
        bEnvoyer.setEnabled(false);

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "llama-3.3-70b-versatile");

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messages.put(userMessage);
            jsonBody.put("messages", messages);
            jsonBody.put("temperature", 0.7);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    API_URL,
                    jsonBody,
                    response -> {
                        progressBar.setVisibility(View.GONE);
                        bEnvoyer.setEnabled(true);
                        try {
                            String answer = response
                                    .getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            // Afficher la réponse de l'IA
                            ajouterMessage("🤖 **IA** :\n" + answer, "#F5F5F5");

                        } catch (Exception e) {
                            ajouterMessage("❌ **Erreur** :\n" + e.getMessage(), "#FFCDD2");
                        }
                    },
                    error -> {
                        progressBar.setVisibility(View.GONE);
                        bEnvoyer.setEnabled(true);
                        ajouterMessage("❌ **Erreur réseau** :\n" + error.getMessage(), "#FFCDD2");
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + API_KEY);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            requestQueue.add(request);

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            bEnvoyer.setEnabled(true);
            ajouterMessage("❌ **Erreur** :\n" + e.getMessage(), "#FFCDD2");
        }
    }

    private void ajouterMessage(String message, String couleur) {
        // Créer un nouveau TextView pour le message
        TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setTextSize(14);
        messageView.setPadding(20, 20, 20, 20);
        messageView.setBackgroundColor(android.graphics.Color.parseColor(couleur));

        // Définir les marges
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(10, 10, 10, 10);
        messageView.setLayoutParams(params);

        // Ajouter le message au conteneur
        chatContainer.addView(messageView);

        // Défiler automatiquement vers le bas
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}