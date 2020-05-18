package telas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.example.rfidscanner.R;
import com.felipecsl.gifimageview.library.GifImageView;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import java.io.InputStream;

public class SplashScreen extends AppCompatActivity {

    private GifImageView gifSos;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        View decorView = getWindow().getDecorView();
        // Esconde tanto a barra de navegação e a barra de status .
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        gifSos = (GifImageView) findViewById(R.id.gifSos);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility( progressBar.VISIBLE );

        //Setando a resolução do gif
        try{
            InputStream inputStream = getAssets().open("carrega3.gif");
            byte[] bytes = IOUtils.toByteArray(inputStream);
            gifSos.setBytes(bytes);
            gifSos.startAnimation();
        } catch (IOException e){
            //Faz alguma coisa

        }

        //Aguardando um tempo para redirecionar
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SplashScreen.this.startActivity(new Intent(SplashScreen.this, HomeActivity.class));
                SplashScreen.this.finish();
            }
        }, 3000);

    }
}
