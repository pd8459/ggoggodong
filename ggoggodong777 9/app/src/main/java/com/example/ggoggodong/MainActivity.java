package com.example.ggoggodong;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 일정 시간 후에 SignUpActivity로 전환
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // SignUpActivity로 화면 전환
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();  // MainActivity 종료
            }
        }, 2000);  // 2초 후에 화면 전환
    }
}
