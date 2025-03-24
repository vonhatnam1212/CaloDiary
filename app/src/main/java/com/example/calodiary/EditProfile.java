package com.example.calodiary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    EditText editTextFullName, editTextUsername, editTextPassword, editTextConfirmPassword,
            editTextEmail, editTextDob, editTextHeight, editTextWeight;
    RadioGroup radioGroupGender;
    Button buttonSave;
    ImageButton backButton;
    ImageView avatarImage, dobPicker, togglePassword, toggleConfirmPassword;
    TextView emailError, passwordError, confirmPasswordError, fullNameError, usernameError,
            dobError, heightError, weightError, genderError;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseStorage storage;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private String currentEmail;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        editTextFullName = findViewById(R.id.fullname_input);
        editTextUsername = findViewById(R.id.username1_input);
        editTextPassword = findViewById(R.id.password2_input);
        editTextConfirmPassword = findViewById(R.id.cfpassword_input);
        editTextEmail = findViewById(R.id.email_input);
        editTextDob = findViewById(R.id.dob_input);
        editTextHeight = findViewById(R.id.height_input);
        editTextWeight = findViewById(R.id.weight_input);
        radioGroupGender = findViewById(R.id.radioGroup);
        buttonSave = findViewById(R.id.login_btn);
        backButton = findViewById(R.id.back_button);
        avatarImage = findViewById(R.id.avatar_image);
        dobPicker = findViewById(R.id.dob_picker);
        togglePassword = findViewById(R.id.toggle_password);
        toggleConfirmPassword = findViewById(R.id.toggle_confirm_password);
        emailError = findViewById(R.id.email_error);
        passwordError = findViewById(R.id.password_error);
        confirmPasswordError = findViewById(R.id.confirm_password_error);
        fullNameError = findViewById(R.id.fullname_error);
        usernameError = findViewById(R.id.username_error);
        dobError = findViewById(R.id.dob_error);
        heightError = findViewById(R.id.height_error);
        weightError = findViewById(R.id.weight_error);
        genderError = findViewById(R.id.gender_error);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentEmail = user.getEmail();
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            editTextFullName.setText(document.getString("full_name"));
                            editTextUsername.setText(document.getString("username"));
                            editTextEmail.setText(currentEmail);
                            editTextDob.setText(document.getString("dob"));
                            editTextHeight.setText(String.valueOf(document.getDouble("height")));
                            editTextWeight.setText(String.valueOf(document.getDouble("weight")));
                            String gender = document.getString("gender");
                            if ("male".equalsIgnoreCase(gender)) {
                                radioGroupGender.check(R.id.radioButton);
                            } else if ("female".equalsIgnoreCase(gender)) {
                                radioGroupGender.check(R.id.female);
                            }
                            String imgBase64 = document.getString("img");
                            if (imgBase64 != null && !imgBase64.isEmpty()) {
                                avatarImage.setImageResource(R.drawable.add_avatar);
                            }
                        }
                    });
        }

        avatarImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        editTextDob.setInputType(InputType.TYPE_CLASS_NUMBER);
        editTextDob.addTextChangedListener(new TextWatcher() {
            private String previousText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                previousText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replaceAll("[^0-9]", "");
                if (input.length() > 8) {
                    input = input.substring(0, 8);
                }
                String formatted = formatDate(input);
                if (!formatted.equals(s.toString())) {
                    editTextDob.removeTextChangedListener(this);
                    editTextDob.setText(formatted);
                    editTextDob.setSelection(formatted.length());
                    editTextDob.addTextChangedListener(this);
                }
            }

            private String formatDate(String input) {
                if (input.length() <= 2) {
                    return input;
                } else if (input.length() <= 4) {
                    return input.substring(0, 2) + "/" + input.substring(2);
                } else {
                    return input.substring(0, 2) + "/" + input.substring(2, 4) + "/" + input.substring(4);
                }
            }
        });

        dobPicker.setOnClickListener(v -> showDatePickerDialog());
        backButton.setOnClickListener(v -> finish());

        togglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_visibility_off);
                isPasswordVisible = false;
            } else {
                editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                togglePassword.setImageResource(R.drawable.ic_visibility);
                isPasswordVisible = true;
            }
            editTextPassword.setSelection(editTextPassword.getText().length());
        });

        toggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                editTextConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off);
                isConfirmPasswordVisible = false;
            } else {
                editTextConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                toggleConfirmPassword.setImageResource(R.drawable.ic_visibility);
                isConfirmPasswordVisible = true;
            }
            editTextConfirmPassword.setSelection(editTextConfirmPassword.getText().length());
        });

        buttonSave.setOnClickListener(v -> {
            if (validateInputs()) {
                FirebaseUser user1 = mAuth.getCurrentUser();
                if (user != null) {
                    Map<String, Object> userData = new HashMap<>();
                    String newEmail = editTextEmail.getText().toString().trim();
                    userData.put("full_name", editTextFullName.getText().toString().trim());
                    userData.put("username", editTextUsername.getText().toString().trim());
                    userData.put("email", newEmail);
                    userData.put("dob", editTextDob.getText().toString().trim());
                    userData.put("height", Float.parseFloat(editTextHeight.getText().toString().trim()));
                    userData.put("weight", Float.parseFloat(editTextWeight.getText().toString().trim()));
                    String gender = radioGroupGender.getCheckedRadioButtonId() == R.id.radioButton ? "male" : "female";
                    userData.put("gender", gender);
                    userData.put("updated_at", new Date());

                    String password = editTextPassword.getText().toString().trim();
                    if (!TextUtils.isEmpty(password)) {
                        user.updatePassword(password).addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Toast.makeText(EditProfile.this, "Lỗi khi cập nhật mật khẩu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    if (!newEmail.equals(currentEmail)) {
                        checkEmailAvailability(newEmail, () -> {
                            user.verifyBeforeUpdateEmail(newEmail)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            userData.put("email", newEmail);
                                            updateUserData(user, userData);
                                            Toast.makeText(EditProfile.this, "Email xác nhận đã được gửi đến " + newEmail + ". Vui lòng xác nhận!", Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                            startActivity(new Intent(getApplicationContext(), Login.class));
                                            finish();
                                        } else {
                                            Toast.makeText(EditProfile.this, "Lỗi khi gửi email xác nhận: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        });
                    } else {
                        if (imageUri != null) {
                            uploadAvatarAndUpdate(user, userData);
                        } else {
                            updateUserData(user, userData);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            avatarImage.setImageURI(imageUri);
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String date = String.format("%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    editTextDob.setText(date);
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
        datePickerDialog.show();
    }

    private void checkEmailAvailability(String email, Runnable onSuccess) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        editTextEmail.setBackgroundResource(R.drawable.error_border);
                        emailError.setText("Email đã được sử dụng");
                        Toast.makeText(EditProfile.this, "Email đã được sử dụng!", Toast.LENGTH_SHORT).show();
                    } else {
                        onSuccess.run();
                    }
                });
    }

    private void uploadAvatarAndUpdate(FirebaseUser user, Map<String, Object> userData) {
        StorageReference fileRef = storage.getReference().child("avatars/" + user.getUid() + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    userData.put("img", uri.toString()); // Lưu URL thay vì base64
                    updateUserData(user, userData);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfile.this, "Lỗi khi upload avatar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateUserData(user, userData);
                });
    }

    private void updateUserData(FirebaseUser user, Map<String, Object> userData) {
        db.collection("users").document(user.getUid())
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfile.this, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfile.this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String dob = editTextDob.getText().toString().trim();

        // Reset lỗi và background trước khi kiểm tra
        emailError.setText(""); emailError.setVisibility(View.GONE);
        passwordError.setText(""); passwordError.setVisibility(View.GONE);
        confirmPasswordError.setText(""); confirmPasswordError.setVisibility(View.GONE);
        fullNameError.setText(""); fullNameError.setVisibility(View.GONE);
        usernameError.setText(""); usernameError.setVisibility(View.GONE);
        dobError.setText(""); dobError.setVisibility(View.GONE);
        heightError.setText(""); heightError.setVisibility(View.GONE);
        weightError.setText(""); weightError.setVisibility(View.GONE);
        genderError.setText(""); genderError.setVisibility(View.GONE);
        editTextEmail.setBackgroundResource(R.drawable.rounded_corner_text);
        editTextPassword.setBackgroundResource(R.drawable.rounded_corner_text);
        editTextConfirmPassword.setBackgroundResource(R.drawable.rounded_corner_text);
        editTextFullName.setBackgroundResource(R.drawable.rounded_corner_text);
        editTextUsername.setBackgroundResource(R.drawable.rounded_corner_text);
        editTextDob.setBackgroundResource(R.drawable.rounded_corner_text);
        editTextHeight.setBackgroundResource(R.drawable.rounded_corner_text);
        editTextWeight.setBackgroundResource(R.drawable.rounded_corner_text);
        radioGroupGender.setBackgroundResource(0);

        // Kiểm tra email
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setBackgroundResource(R.drawable.error_border);
            emailError.setText("Vui lòng nhập email hợp lệ");
            emailError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Kiểm tra password (nếu có nhập)
        if (!TextUtils.isEmpty(password)) {
            if (password.length() < 6) {
                editTextPassword.setBackgroundResource(R.drawable.error_border);
                passwordError.setText("Password phải có tối thiểu 6 ký tự");
                passwordError.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (!password.equals(confirmPassword)) {
                editTextConfirmPassword.setBackgroundResource(R.drawable.error_border);
                confirmPasswordError.setText("Confirm password không đúng với password ở trên");
                confirmPasswordError.setVisibility(View.VISIBLE);
                isValid = false;
            }
        }

        // Kiểm tra full name
        if (TextUtils.isEmpty(editTextFullName.getText().toString().trim())) {
            editTextFullName.setBackgroundResource(R.drawable.error_border);
            fullNameError.setText("Vui lòng nhập họ tên");
            fullNameError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Kiểm tra username
        String username = editTextUsername.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setBackgroundResource(R.drawable.error_border);
            usernameError.setText("Vui lòng nhập tên người dùng");
            usernameError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Kiểm tra ngày sinh
        String currentDateStr = dateFormat.format(new Date());
        try {
            if (TextUtils.isEmpty(dob) || dob.length() != 10) {
                editTextDob.setBackgroundResource(R.drawable.error_border);
                dobError.setText("Vui lòng nhập đúng ngày sinh của bạn");
                dobError.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                Date dobDate = dateFormat.parse(dob);
                Date currentDate = dateFormat.parse(currentDateStr);
                if (dobDate == null || dobDate.compareTo(currentDate) >= 0) {
                    editTextDob.setBackgroundResource(R.drawable.error_border);
                    dobError.setText("Vui lòng nhập đúng ngày sinh của bạn");
                    dobError.setVisibility(View.VISIBLE);
                    isValid = false;
                }
            }
        } catch (ParseException e) {
            editTextDob.setBackgroundResource(R.drawable.error_border);
            dobError.setText("Định dạng ngày sinh không hợp lệ");
            dobError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Kiểm tra height
        try {
            float heightValue = Float.parseFloat(editTextHeight.getText().toString().trim());
            if (heightValue <= 0) {
                editTextHeight.setBackgroundResource(R.drawable.error_border);
                heightError.setText("Chiều cao phải lớn hơn 0");
                heightError.setVisibility(View.VISIBLE);
                isValid = false;
            }
        } catch (NumberFormatException e) {
            editTextHeight.setBackgroundResource(R.drawable.error_border);
            heightError.setText("Vui lòng nhập chiều cao hợp lệ");
            heightError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Kiểm tra weight
        try {
            float weightValue = Float.parseFloat(editTextWeight.getText().toString().trim());
            if (weightValue <= 0) {
                editTextWeight.setBackgroundResource(R.drawable.error_border);
                weightError.setText("Cân nặng phải lớn hơn 0");
                weightError.setVisibility(View.VISIBLE);
                isValid = false;
            }
        } catch (NumberFormatException e) {
            editTextWeight.setBackgroundResource(R.drawable.error_border);
            weightError.setText("Vui lòng nhập cân nặng hợp lệ");
            weightError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Kiểm tra gender
        if (radioGroupGender.getCheckedRadioButtonId() == -1) {
            radioGroupGender.setBackgroundResource(R.drawable.error_border);
            genderError.setText("Vui lòng chọn giới tính");
            genderError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        return isValid;
    }
}