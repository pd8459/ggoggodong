package com.example.ggoggodong;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.core.widget.NestedScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.ggoggodong.story.AdventureFragment;
import com.example.ggoggodong.story.ComicFragment;
import com.example.ggoggodong.story.FableFragment;
import com.example.ggoggodong.story.FamilyFragment;
import com.example.ggoggodong.story.FantasyFragment;
import com.example.ggoggodong.story.MoralFragment;
import com.example.ggoggodong.story.TraditionalFragment;


public class GenreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.genre);

        NestedScrollView tabContainer = findViewById(R.id.tabContainer);

        // HomeActivity에서 전달된 장르 정보 가져오기 (기본값: 1)
        int genre = getIntent().getIntExtra("genre", 1);
        displayGenreFragment(genre);
        findViewById(R.id.tab1).setOnClickListener(v -> {
            displayGenreFragment(1);
            tabContainer.setBackgroundResource(R.drawable.backtab7);
        });

        findViewById(R.id.tab2).setOnClickListener(v -> {
            displayGenreFragment(2);
            tabContainer.setBackgroundResource(R.drawable.backtab5);
        });

        findViewById(R.id.tab3).setOnClickListener(v -> {
            displayGenreFragment(3);
            tabContainer.setBackgroundResource(R.drawable.backtab6);
        });

        findViewById(R.id.tab4).setOnClickListener(v -> {
            displayGenreFragment(4);
            tabContainer.setBackgroundResource(R.drawable.backtab3);
        });

        findViewById(R.id.tab5).setOnClickListener(v -> {
            displayGenreFragment(5);
            tabContainer.setBackgroundResource(R.drawable.backtab);
        });

        findViewById(R.id.tab6).setOnClickListener(v -> {
            displayGenreFragment(6);
            tabContainer.setBackgroundResource(R.drawable.backtab2);
        });

        findViewById(R.id.tab7).setOnClickListener(v -> {
            displayGenreFragment(7);
            tabContainer.setBackgroundResource(R.drawable.backtab4);
        });

        // 마이페이지 버튼 클릭 이벤트 설정
        ImageButton myPageButton = findViewById(R.id.rightTopButton);
        myPageButton.setOnClickListener(v -> {
            Intent intent = new Intent(GenreActivity.this, MyPageActivity.class);
            startActivity(intent);
        });
    }

    private void displayGenreFragment(int genre) {
        Fragment fragment;
        switch (genre) {
            case 2:
                fragment = new FantasyFragment();
                break;
            case 3:
                fragment = new MoralFragment();
                break;
            case 4:
                fragment = new FableFragment();
                break;
            case 5:
                fragment = new AdventureFragment();
                break;
            case 6:
                fragment = new ComicFragment();
                break;
            case 7:
                fragment = new FamilyFragment();
                break;
            case 1:
            default:
                fragment = new TraditionalFragment();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}