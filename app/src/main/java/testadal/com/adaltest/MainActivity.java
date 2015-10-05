package testadal.com.adaltest;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.Logger;
import com.microsoft.aad.adal.PromptBehavior;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;

public class MainActivity extends AppCompatActivity {

    private AuthenticationContext mAuthContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                officeSignIn();
            }
        });
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            mAuthContext.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
        }
    }

    private void officeSignIn() {
        try {
            mAuthContext = new AuthenticationContext(this, Constants.AUTHORITY_URL, true);
            mAuthContext.getCache().removeAll();
//            AuthenticationSettings.INSTANCE.setSecretKey();
//            mAuthContext.acquireToken(Constants.DISCOVERY_RESOURCE_ID, Constants.CLIENT_ID, Constants.REDIRECT_URI, "", PromptBehavior.Auto, "",
            mAuthContext.acquireToken(this, Constants.DISCOVERY_RESOURCE_ID, Constants.CLIENT_ID, Constants.REDIRECT_URI, PromptBehavior.Auto,
                    new AuthenticationCallback<AuthenticationResult>() {
                        @Override
                        public void onSuccess(AuthenticationResult authenticationResult) {
                            Toast.makeText(MainActivity.this, "SUCCESS ACQUIRING TOKEN", Toast.LENGTH_LONG).show();
                            Log.e("MainActivity", "onSuccess acquireToken");
                            login(authenticationResult.getIdToken());
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(MainActivity.this, "FAILED TO ACQUIRE TOKEN", Toast.LENGTH_LONG).show();
                            Log.e("MainActivity", "onError acquireToken");
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void login(final String idToken) {
        try {
            String baseUrl = "https://www.switch.co/";
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Login login = retrofit.create(Login.class);

            // Create a call instance for looking up Retrofit contributors.
            Call<Session> call = login.microsoft(idToken);
            call.enqueue(new Callback<Session>() {
                @Override
                public void onResponse(Response<Session> response) {
                    Toast.makeText(MainActivity.this, "Login success", Toast.LENGTH_LONG).show();
                    Log.e("MainActivity", "login success");
                    Log.e("MainActivity", "access token = " + response.body().auth.access_token);
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(MainActivity.this, "Login failure", Toast.LENGTH_LONG).show();
                    Log.e("MainActivity", "login failure");
                    Log.e("MainActivity", t.getMessage());
                    t.printStackTrace();
                }
            });
        } catch (Exception e) {
        }
    }

    public interface Login {
        @GET("api/login?remote_service=microsoft")
        Call<Session> microsoft(
                @Query("id_token") String idToken);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
