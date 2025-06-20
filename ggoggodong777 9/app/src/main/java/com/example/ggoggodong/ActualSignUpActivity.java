package com.example.ggoggodong;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.ggoggodong.profile.ProfileActivity;

import java.util.HashMap;
import java.util.Map;
public class ActualSignUpActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPassword, editTextConfirmPassword;
    private CheckBox checkboxConsent, checkboxMarketingConsent, checkboxTerms, checkboxAll;
    private Button btnVerifyCode;
    private ImageView signUpImage;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean isVerificationSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actualsignupactivity);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        checkboxConsent = findViewById(R.id.checkboxConsent);
        checkboxMarketingConsent = findViewById(R.id.checkboxMarketingConsent);
        checkboxTerms = findViewById(R.id.checkboxTerms);
        checkboxAll = findViewById(R.id.checkboxAll);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);
        signUpImage = findViewById(R.id.signupImage);

        TextView consentText = findViewById(R.id.consentText);
        TextView marketingText = findViewById(R.id.marketingText);
        TextView termsText = findViewById(R.id.termsText);

        consentText.setOnClickListener(v -> openTermsActivity("consent"));
        marketingText.setOnClickListener(v -> openTermsActivity("marketing"));
        termsText.setOnClickListener(v -> openTermsActivity("privacy"));

        checkboxAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkboxConsent.setChecked(isChecked);
            checkboxMarketingConsent.setChecked(isChecked);
            checkboxTerms.setChecked(isChecked);
        });

        CheckBox[] subCheckboxes = { checkboxConsent, checkboxMarketingConsent, checkboxTerms };
        for (CheckBox checkBox : subCheckboxes) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkboxAll.setChecked(
                        checkboxConsent.isChecked() &&
                                checkboxMarketingConsent.isChecked() &&
                                checkboxTerms.isChecked()
                );
            });
        }

        btnVerifyCode.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "이메일을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            String tempPassword = "temporaryPassword123";

            mAuth.createUserWithEmailAndPassword(email, tempPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verifyTask -> {
                                            if (verifyTask.isSuccessful()) {
                                                isVerificationSent = true;
                                                Toast.makeText(this, "인증 메일이 전송되었습니다. 이메일을 확인해주세요.", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(this, "이메일 전송 실패: " + verifyTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(this, "인증 메일 발송 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        signUpImage.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();

            if (!checkboxConsent.isChecked() || !checkboxMarketingConsent.isChecked() || !checkboxTerms.isChecked()) {
                Toast.makeText(this, "모든 동의 항목을 체크해야 회원가입이 가능합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isVerificationSent) {
                Toast.makeText(this, "먼저 이메일 인증을 진행해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password) || password.length() < 6) {
                Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                user.reload().addOnCompleteListener(task -> {
                    if (!user.isEmailVerified()) {
                        Toast.makeText(this, "이메일 인증이 완료되지 않았습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    user.updatePassword(password).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            db.collection("users").whereEqualTo("username", username)
                                    .get().addOnSuccessListener(querySnapshot -> {
                                        boolean isDuplicate = false;
                                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                            String existingUid = doc.getString("uid");
                                            if (!user.getUid().equals(existingUid)) {
                                                isDuplicate = true;
                                                break;
                                            }
                                        }

                                        if (isDuplicate) {
                                            Toast.makeText(this, "이미 사용 중인 닉네임입니다.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            saveUserData(username, user);
                                        }
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(this, "닉네임 중복 확인 실패", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "비밀번호 설정 실패: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });
    }

    // ✅ 여기가 onCreate 밖에 있어야 정상 작동합니다!
    private void saveUserData(String username, FirebaseUser user) {
        String email = user.getEmail();
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", email);
        userData.put("uid", user.getUid());
        userData.put("emailVerified", true);

        db.collection("users").document(user.getUid()).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "회원정보 저장 실패", Toast.LENGTH_SHORT).show();
                });
    }

    private void openTermsActivity(String type) {
        Intent intent = new Intent(this, TermsActivity1.class);
        intent.putExtra("terms_type", type);
        startActivity(intent);
    }
}