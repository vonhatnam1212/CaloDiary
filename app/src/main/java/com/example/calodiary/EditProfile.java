package com.example.calodiary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calodiary.databinding.ActivityEditProfileBinding;
import com.example.calodiary.model.User;
import com.example.calodiary.utils.DateInputFormatter;
import com.example.calodiary.utils.FirebaseManager;
import com.example.calodiary.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class EditProfile extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private Uri imageUri;
    private String currentEmail;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = FirebaseManager.getInstance();
        FirebaseUser user = firebaseManager.getCurrentUser();

        if (user != null) {
            currentEmail = user.getEmail();
            firebaseManager.loadUserData(user, this::populateFields, this);
        }

        binding.avatarImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        });

        binding.dobInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.dobInput.addTextChangedListener(new DateInputFormatter(binding.dobInput));

        binding.dobPicker.setOnClickListener(v -> showDatePickerDialog());
        binding.backButton.setOnClickListener(v -> finish());

        binding.togglePassword.setOnClickListener(v -> togglePasswordVisibility(binding.password2Input, binding.togglePassword));
        binding.toggleConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility(binding.cfpasswordInput, binding.toggleConfirmPassword));

        binding.loginBtn.setOnClickListener(v -> saveProfile(user));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            binding.avatarImage.setImageURI(imageUri);
        }
    }

    private void populateFields(User user) {
        binding.fullnameInput.setText(user.getFullName());
        binding.username1Input.setText(user.getUsername());
        binding.emailInput.setText(currentEmail);
        binding.dobInput.setText(user.getDob());
        binding.heightInput.setText(String.valueOf(user.getHeight()));
        binding.weightInput.setText(String.valueOf(user.getWeight()));
        if ("male".equalsIgnoreCase(user.getGender())) {
            binding.radioGroup.check(R.id.radioButton);
        } else if ("female".equalsIgnoreCase(user.getGender())) {
            binding.radioGroup.check(R.id.female);
        }
        if (user.getImg() != null && !user.getImg().isEmpty()) {
            binding.avatarImage.setImageResource(R.drawable.add_avatar);
        }
    }

    private void togglePasswordVisibility(android.widget.EditText editText, android.widget.ImageView toggle) {
        if (isPasswordVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggle.setImageResource(R.drawable.ic_visibility_off);
            isPasswordVisible = false;
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            toggle.setImageResource(R.drawable.ic_visibility);
            isPasswordVisible = true;
        }
        editText.setSelection(editText.getText().length());
    }

    private void toggleConfirmPasswordVisibility(android.widget.EditText editText, android.widget.ImageView toggle) {
        if (isConfirmPasswordVisible) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggle.setImageResource(R.drawable.ic_visibility_off);
            isConfirmPasswordVisible = false;
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            toggle.setImageResource(R.drawable.ic_visibility);
            isConfirmPasswordVisible = true;
        }
        editText.setSelection(editText.getText().length());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String date = String.format("%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    binding.dobInput.setText(date);
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
        datePickerDialog.show();
    }

    private void saveProfile(FirebaseUser user) {
        if (validateInputs()) {
            User userData = new User();
            userData.setFullName(binding.fullnameInput.getText().toString().trim());
            userData.setUsername(binding.username1Input.getText().toString().trim());
            userData.setEmail(binding.emailInput.getText().toString().trim());
            userData.setDob(binding.dobInput.getText().toString().trim());
            userData.setHeight(Float.parseFloat(binding.heightInput.getText().toString().trim()));
            userData.setWeight(Float.parseFloat(binding.weightInput.getText().toString().trim()));
            userData.setGender(binding.radioGroup.getCheckedRadioButtonId() == R.id.radioButton ? "male" : "female");
            userData.setUpdatedAt(new java.util.Date());

            String password = binding.password2Input.getText().toString().trim();
            if (!password.isEmpty()) {
                user.updatePassword(password).addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Lỗi khi cập nhật mật khẩu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            String newEmail = binding.emailInput.getText().toString().trim();
            if (!newEmail.equals(currentEmail)) {
                firebaseManager.checkEmailAvailability(newEmail, () -> {
                    user.verifyBeforeUpdateEmail(newEmail)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    firebaseManager.updateUserProfile(user, userData, this, () -> {
                                        Toast.makeText(this, "Email xác nhận đã được gửi đến " + newEmail + ". Vui lòng xác nhận!", Toast.LENGTH_LONG).show();
                                        firebaseManager.signOut();
                                        startActivity(new Intent(this, Login.class));
                                        finish();
                                    });
                                } else {
                                    Toast.makeText(this, "Lỗi khi gửi email xác nhận: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }, this);
            } else if (imageUri != null) {
                firebaseManager.uploadAvatar(user, imageUri, uri -> {
                    userData.setImg(uri);
                    firebaseManager.updateUserProfile(user, userData, this, this::finish);
                }, this);
            } else {
                firebaseManager.updateUserProfile(user, userData, this, this::finish);
            }
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;
        isValid &= ValidationUtils.validateEmail(binding.emailInput, binding.emailError);
        isValid &= ValidationUtils.validatePassword(binding.password2Input, binding.cfpasswordInput, binding.passwordError, binding.confirmPasswordError, false);
        isValid &= ValidationUtils.validateRequiredField(binding.fullnameInput, binding.fullnameError, "Vui lòng nhập họ tên");
        isValid &= ValidationUtils.validateRequiredField(binding.username1Input, binding.usernameError, "Vui lòng nhập tên người dùng");
        isValid &= ValidationUtils.validateDob(binding.dobInput, binding.dobError);
        isValid &= ValidationUtils.validateNumberField(binding.heightInput, binding.heightError, "Chiều cao phải lớn hơn 0");
        isValid &= ValidationUtils.validateNumberField(binding.weightInput, binding.weightError, "Cân nặng phải lớn hơn 0");
        isValid &= ValidationUtils.validateGender(binding.radioGroup, binding.genderError);
        return isValid;
    }
}