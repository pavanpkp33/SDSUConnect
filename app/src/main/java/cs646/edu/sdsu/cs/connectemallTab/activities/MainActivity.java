package cs646.edu.sdsu.cs.connectemallTab.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import cs646.edu.sdsu.cs.connectemallTab.R;
import cs646.edu.sdsu.cs.connectemallTab.activities.RegisterActivity;
import cs646.edu.sdsu.cs.connectemallTab.helpers.ContextHelper;
import cs646.edu.sdsu.cs.connectemallTab.helpers.DataHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etEmail, etPassword;
    Button btnLogin, btnRegister;
    ProgressDialog loaderDialog;
    private FirebaseAuth authInstance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        authInstance = FirebaseAuth.getInstance();
        if(authInstance.getCurrentUser() != null){
            finish();
            startActivity(new Intent(this, UserHomeActivity.class));
        }
        final ContextHelper contextHelper = new ContextHelper(this);
        loaderDialog = new ProgressDialog(this);
        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);
        etEmail = (EditText) findViewById(R.id.etLoginEmailId);
        etPassword = (EditText) findViewById(R.id.etLoginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.btnLogin :
                validateAndLogin();


                break;
            case R.id.btnRegister:
                Intent registerIntent = new Intent(this,RegisterActivity.class);
                startActivity(registerIntent);
                break;
        }

    }

    private void validateAndLogin() {
        String emailID = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(TextUtils.isEmpty(emailID)){
            etEmail.setError("Enter Email ID");
            etEmail.requestFocus();
        } else if(TextUtils.isEmpty(password)){
            etPassword.setError("Enter Password");
            etPassword.requestFocus();
        }else{
            loginUser(emailID, password);
        }

    }

    private void loginUser(String emailID, String password) {
        loaderDialog.setMessage("Logging In.. Please wait.");
        loaderDialog.show();

        authInstance.signInWithEmailAndPassword(emailID, password).addOnCompleteListener(this, new
                OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loaderDialog.dismiss();
                        if(task.isSuccessful()){
                            finish();
                            startActivity(new Intent(getApplicationContext(), UserHomeActivity.class));
                        }else{
                           // Toast.makeText(getApplicationContext(), "User auth failed!", Toast.LENGTH_SHORT).show();
                            Snackbar.make(findViewById(android.R.id.content), "Invalid Credentials. Please try again.", Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    }
                }
        );
    }
}
