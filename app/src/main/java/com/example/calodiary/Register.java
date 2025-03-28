package com.example.calodiary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calodiary.databinding.ActivityRegisterBinding;
import com.example.calodiary.Model.User;
import com.example.calodiary.utils.DateInputFormatter;
import com.example.calodiary.utils.FirebaseManager;
import com.example.calodiary.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseUser;


import java.util.Calendar;

public class Register extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private Uri imageUri;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private FirebaseManager firebaseManager;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(this, Profile.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseManager = FirebaseManager.getInstance();

        binding.usernameInput.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (Character.isWhitespace(source.charAt(i))) return "";
                    }
                    return null;
                }
        });

        binding.avatarImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        });

        binding.togglePassword.setOnClickListener(v -> togglePasswordVisibility());
        binding.toggleConfirmPassword.setOnClickListener(v -> toggleConfirmPasswordVisibility());

        binding.dobInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.dobInput.addTextChangedListener(new DateInputFormatter(binding.dobInput));

        binding.dobPicker.setOnClickListener(v -> showDatePickerDialog());
        binding.backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });

        binding.registerBtn.setOnClickListener(v -> registerUser());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            binding.avatarImage.setImageURI(imageUri);
        }
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.togglePassword.setImageResource(R.drawable.ic_visibility_off);
            isPasswordVisible = false;
        } else {
            binding.passwordInput.setInputType(InputType.TYPE_CLASS_TEXT);
            binding.togglePassword.setImageResource(R.drawable.ic_visibility);
            isPasswordVisible = true;
        }
        binding.passwordInput.setSelection(binding.passwordInput.getText().length());
    }

    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            binding.cfpasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.toggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off);
            isConfirmPasswordVisible = false;
        } else {
            binding.cfpasswordInput.setInputType(InputType.TYPE_CLASS_TEXT);
            binding.toggleConfirmPassword.setImageResource(R.drawable.ic_visibility);
            isConfirmPasswordVisible = true;
        }
        binding.cfpasswordInput.setSelection(binding.cfpasswordInput.getText().length());
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

    private void registerUser() {
        if (validateInputs()) {
            String email = binding.emailInput.getText().toString().trim();
            String username = binding.usernameInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();

            firebaseManager.checkEmailAvailability(email, () -> {
                firebaseManager.checkUsernameAvailability(username, () -> {
                    firebaseManager.getInstance().mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseManager.getCurrentUser();
                                    if (user != null) {
                                        user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                            if (verifyTask.isSuccessful()) {
                                                User userData = new User();
                                                userData.setUid(user.getUid());
                                                userData.setEmail(email);
                                                userData.setFullName(binding.fullnameInput.getText().toString().trim());
                                                userData.setUsername(username);
                                                userData.setDob(binding.dobInput.getText().toString().trim());
                                                userData.setHeight(Float.parseFloat(binding.heightInput.getText().toString().trim()));
                                                userData.setWeight(Float.parseFloat(binding.weightInput.getText().toString().trim()));
                                                userData.setGender(binding.radioGroup.getCheckedRadioButtonId() == R.id.radioButton ? "male" : "female");
                                                userData.setRole("user");
                                                userData.setEmailVerified(false);

                                                firebaseManager.setUserProfile(user, userData, this, () -> {
                                                    firebaseManager.signOut();
                                                    startActivity(new Intent(this, Login.class));
                                                    finish();
                                                });
                                            }
                                        });
                                    }
                                }
                            });
                }, this);
            }, this);
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;
        isValid &= ValidationUtils.validateEmail(binding.emailInput, binding.emailError);
        isValid &= ValidationUtils.validatePassword(binding.passwordInput, binding.cfpasswordInput, binding.passwordError, binding.confirmPasswordError, true);
        isValid &= ValidationUtils.validateRequiredField(binding.fullnameInput, binding.fullnameError, "Vui lòng nhập họ tên");
        isValid &= ValidationUtils.validateRequiredField(binding.usernameInput, binding.usernameError, "Vui lòng nhập tên người dùng");
        isValid &= ValidationUtils.validateDob(binding.dobInput, binding.dobError);
        isValid &= ValidationUtils.validateNumberField(binding.heightInput, binding.heightError, "Chiều cao phải lớn hơn 0");
        isValid &= ValidationUtils.validateNumberField(binding.weightInput, binding.weightError, "Cân nặng phải lớn hơn 0");
        isValid &= ValidationUtils.validateGender(binding.radioGroup, binding.genderError);
        return isValid;
    }
}