package com.example.calodiary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Register extends AppCompatActivity {

    EditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextFullName,
            editTextUsername, editTextDob, editTextHeight, editTextWeight;
    TextView emailError, passwordError, confirmPasswordError, fullNameError, usernameError,
            dobError, heightError, weightError, genderError;
    RadioGroup radioGroupGender;
    Button buttonReg;
    ImageButton backButton; // Chỉ giữ nút Back
    ImageView avatarImage, togglePassword, toggleConfirmPassword, dobPicker;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        editTextEmail = findViewById(R.id.email_input);
        editTextPassword = findViewById(R.id.password_input);
        editTextConfirmPassword = findViewById(R.id.cfpassword_input);
        editTextFullName = findViewById(R.id.fullname_input);
        editTextUsername = findViewById(R.id.username_input);
        editTextDob = findViewById(R.id.dob_input);
        editTextHeight = findViewById(R.id.height_input);
        editTextWeight = findViewById(R.id.weight_input);
        radioGroupGender = findViewById(R.id.radioGroup);
        buttonReg = findViewById(R.id.register_btn);
        backButton = findViewById(R.id.back_button); // Ánh xạ nút Back
        avatarImage = findViewById(R.id.avatar_image);
        togglePassword = findViewById(R.id.toggle_password);
        toggleConfirmPassword = findViewById(R.id.toggle_confirm_password);
        dobPicker = findViewById(R.id.dob_picker);

        emailError = findViewById(R.id.email_error);
        passwordError = findViewById(R.id.password_error);
        confirmPasswordError = findViewById(R.id.confirm_password_error);
        fullNameError = findViewById(R.id.fullname_error);
        usernameError = findViewById(R.id.username_error);
        dobError = findViewById(R.id.dob_error);
        heightError = findViewById(R.id.height_error);
        weightError = findViewById(R.id.weight_error);
        genderError = findViewById(R.id.gender_error);

        editTextUsername.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (Character.isWhitespace(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
        });

        avatarImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

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

        // Xử lý sự kiện cho nút Back Arrow
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        buttonReg.setOnClickListener(v -> {
            if (validateInputs()) {
                String email = editTextEmail.getText().toString().trim();
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String fullName = editTextFullName.getText().toString().trim();
                String dob = editTextDob.getText().toString().trim();
                String height = editTextHeight.getText().toString().trim();
                String weight = editTextWeight.getText().toString().trim();
                String gender = radioGroupGender.getCheckedRadioButtonId() == R.id.radioButton ? "Male" : "Female";

                checkEmailAndUsername(email, username, () -> {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(verifyTask -> {
                                                    if (verifyTask.isSuccessful()) {
                                                        Map<String, Object> userData = new HashMap<>();
                                                        userData.put("email", email);
                                                        userData.put("fullName", fullName);
                                                        userData.put("username", username);
                                                        userData.put("dob", dob);
                                                        userData.put("height", height);
                                                        userData.put("weight", weight);
                                                        userData.put("gender", gender);
                                                        userData.put("emailVerified", false);
                                                        userData.put("role", "user");

                                                        db.collection("users").document(user.getUid())
                                                                .set(userData)
                                                                .addOnCompleteListener(task1 -> {
                                                                    if (task1.isSuccessful()) {
                                                                        Toast.makeText(Register.this, "Đăng ký thành công. Vui lòng kiểm tra email để xác nhận!", Toast.LENGTH_LONG).show();
                                                                        mAuth.signOut();
                                                                        Intent intent = new Intent(getApplicationContext(), Login.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    } else {
                                                                        Toast.makeText(Register.this, "Lỗi khi lưu dữ liệu: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    } else {
                                                        Toast.makeText(Register.this, "Không thể gửi email xác nhận: " + verifyTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(Register.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                });
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

    private void checkEmailAndUsername(String email, String username, Runnable onSuccess) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(emailTask -> {
                    if (emailTask.isSuccessful() && !emailTask.getResult().isEmpty()) {
                        editTextEmail.setBackgroundResource(R.drawable.error_border);
                        emailError.setText("Email đã được sử dụng");
                        Toast.makeText(Register.this, "Email đã được sử dụng!", Toast.LENGTH_SHORT).show();
                    } else {
                        db.collection("users")
                                .whereEqualTo("username", username)
                                .get()
                                .addOnCompleteListener(usernameTask -> {
                                    if (usernameTask.isSuccessful() && !usernameTask.getResult().isEmpty()) {
                                        editTextUsername.setBackgroundResource(R.drawable.error_border);
                                        usernameError.setText("Username đã được sử dụng");
                                        Toast.makeText(Register.this, "Username đã được sử dụng!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        onSuccess.run();
                                    }
                                });
                    }
                });
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String dob = editTextDob.getText().toString().trim();

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

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setBackgroundResource(R.drawable.error_border);
            emailError.setText("Vui lòng nhập email hợp lệ");
            emailError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setBackgroundResource(R.drawable.error_border);
            passwordError.setText("Vui lòng nhập mật khẩu");
            passwordError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (password.length() < 6) {
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

        if (TextUtils.isEmpty(editTextFullName.getText().toString().trim())) {
            editTextFullName.setBackgroundResource(R.drawable.error_border);
            fullNameError.setText("Vui lòng nhập họ tên");
            fullNameError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        String username = editTextUsername.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setBackgroundResource(R.drawable.error_border);
            usernameError.setText("Vui lòng nhập tên người dùng");
            usernameError.setVisibility(View.VISIBLE);
            isValid = false;
        }

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

        if (radioGroupGender.getCheckedRadioButtonId() == -1) {
            radioGroupGender.setBackgroundResource(R.drawable.error_border);
            genderError.setText("Vui lòng chọn giới tính");
            genderError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        return isValid;
    }
}