package com.example.calodiary.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.calodiary.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ValidationUtils {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public static boolean validateEmail(EditText editText, TextView errorText) {
        String email = editText.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editText.setBackgroundResource(R.drawable.error_border);
            errorText.setText("Vui lòng nhập email hợp lệ");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        editText.setBackgroundResource(R.drawable.rounded_corner_text);
        errorText.setVisibility(View.GONE);
        return true;
    }

    public static boolean validatePassword(EditText password, EditText confirmPassword,
                                           TextView passwordError, TextView confirmPasswordError, boolean isRequired) {
        String pwd = password.getText().toString().trim();
        String confirmPwd = confirmPassword.getText().toString().trim();
        boolean isValid = true;

        if (isRequired && TextUtils.isEmpty(pwd)) {
            password.setBackgroundResource(R.drawable.error_border);
            passwordError.setText("Vui lòng nhập mật khẩu");
            passwordError.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!TextUtils.isEmpty(pwd)) {
            if (pwd.length() < 6) {
                password.setBackgroundResource(R.drawable.error_border);
                passwordError.setText("Password phải có tối thiểu 6 ký tự");
                passwordError.setVisibility(View.VISIBLE);
                isValid = false;
            } else if (!pwd.equals(confirmPwd)) {
                confirmPassword.setBackgroundResource(R.drawable.error_border);
                confirmPasswordError.setText("Confirm password không đúng với password ở trên");
                confirmPasswordError.setVisibility(View.VISIBLE);
                isValid = false;
            } else {
                password.setBackgroundResource(R.drawable.rounded_corner_text);
                confirmPassword.setBackgroundResource(R.drawable.rounded_corner_text);
                passwordError.setVisibility(View.GONE);
                confirmPasswordError.setVisibility(View.GONE);
            }
        }
        return isValid;
    }

    public static boolean validateRequiredField(EditText editText, TextView errorText, String errorMessage) {
        String value = editText.getText().toString().trim();
        if (TextUtils.isEmpty(value)) {
            editText.setBackgroundResource(R.drawable.error_border);
            errorText.setText(errorMessage);
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        editText.setBackgroundResource(R.drawable.rounded_corner_text);
        errorText.setVisibility(View.GONE);
        return true;
    }

    public static boolean validateDob(EditText editText, TextView errorText) {
        String dob = editText.getText().toString().trim();
        String currentDateStr = dateFormat.format(new Date());
        try {
            if (TextUtils.isEmpty(dob) || dob.length() != 10) {
                editText.setBackgroundResource(R.drawable.error_border);
                errorText.setText("Vui lòng nhập đúng ngày sinh của bạn");
                errorText.setVisibility(View.VISIBLE);
                return false;
            }
            Date dobDate = dateFormat.parse(dob);
            Date currentDate = dateFormat.parse(currentDateStr);
            if (dobDate == null || dobDate.compareTo(currentDate) >= 0) {
                editText.setBackgroundResource(R.drawable.error_border);
                errorText.setText("Vui lòng nhập đúng ngày sinh của bạn");
                errorText.setVisibility(View.VISIBLE);
                return false;
            }
            editText.setBackgroundResource(R.drawable.rounded_corner_text);
            errorText.setVisibility(View.GONE);
            return true;
        } catch (ParseException e) {
            editText.setBackgroundResource(R.drawable.error_border);
            errorText.setText("Định dạng ngày sinh không hợp lệ");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
    }

    public static boolean validateNumberField(EditText editText, TextView errorText, String errorMessage) {
        try {
            float value = Float.parseFloat(editText.getText().toString().trim());
            if (value <= 0) {
                editText.setBackgroundResource(R.drawable.error_border);
                errorText.setText(errorMessage);
                errorText.setVisibility(View.VISIBLE);
                return false;
            }
            editText.setBackgroundResource(R.drawable.rounded_corner_text);
            errorText.setVisibility(View.GONE);
            return true;
        } catch (NumberFormatException e) {
            editText.setBackgroundResource(R.drawable.error_border);
            errorText.setText("Vui lòng nhập giá trị hợp lệ");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
    }

    public static boolean validateGender(RadioGroup radioGroup, TextView errorText) {
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            radioGroup.setBackgroundResource(R.drawable.error_border);
            errorText.setText("Vui lòng chọn giới tính");
            errorText.setVisibility(View.VISIBLE);
            return false;
        }
        radioGroup.setBackgroundResource(0);
        errorText.setVisibility(View.GONE);
        return true;
    }
}