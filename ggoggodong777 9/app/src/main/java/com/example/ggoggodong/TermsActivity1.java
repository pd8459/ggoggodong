package com.example.ggoggodong;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TermsActivity1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms_activity1);

        ImageView btnClose = findViewById(R.id.btnClose);
        TextView textTerms = findViewById(R.id.textTerms);

        btnClose.setOnClickListener(v -> finish());

        String type = getIntent().getStringExtra("terms_type");
        String filename = "terms.txt";  // 기본값 설정(안전장치)

        switch (type) {
            case "consent":
                filename = "consent.txt";
                break;
            case "marketing":
                filename = "terms.txt";
                break;
            case "privacy":
                filename = "privacy.txt";
                break;
        }

        textTerms.setText(readTermsFromAssets(filename));
    }

    private String readTermsFromAssets(String filename) {
        StringBuilder builder = new StringBuilder();
        try {
            InputStream inputStream = getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            builder.append("약관을 불러오는 데 실패했습니다.");
        }
        return builder.toString();
    }
}
