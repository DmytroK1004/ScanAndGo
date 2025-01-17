package com.example.uhf_bt;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uhf_bt.dto.Login;
import com.example.uhf_bt.dto.LoginVM;
import com.example.uhf_bt.json.JsonTaskLogin;
import com.google.gson.Gson;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends BaseActivity{

    TextView userName = null;

    TextView password = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        userName = (TextView) findViewById(R.id.txtUserName);
        password = (TextView) findViewById(R.id.txtPassword);

        userName.setText("admin");
        password.setText("Sc@n@ng0#2024");
    }

        public void btnLogin(View v)
        {
            if (userName.getText().length() > 0 && password.getText().length() > 0)
            {
                Login model = new Login();

                model.username = userName.getText().toString();
                model.password = password.getText().toString();

                Globals g = (Globals)getApplication();

                String req = g.apiUrl + "user/signin";

                try {

                    LoginVM result = new LoginVM();

                    Gson gson = new Gson();
                    String jsonString = gson.toJson(model);

                    result = new JsonTaskLogin().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                            req, jsonString).get();

                    if (result.status == 1) {

                        g.isLogin = true;

                        Log.d("success", "Login Success");

                        startActivityForResult(new Intent(getApplicationContext(), ShortCutActivity.class), 0);

                    } else {
                        Toast.makeText(getApplicationContext(), "Unknown code", Toast.LENGTH_SHORT).show();
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
}
