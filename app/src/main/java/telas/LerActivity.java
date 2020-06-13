package telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.example.rfidscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import bluetooth.BluetoothListener;
import bluetooth.BluetoothReceiver;

public class LerActivity extends AppCompatActivity {

    private String dados;
    private SimpleDateFormat dataFormatada;
    private Date date;
    private String textoTag = "", dataFinal = "", retorna = "";
    private TextView tag, data;
    private FloatingActionButton fab;
    String RESPOSTA_TAG = "retorno_tag";
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ler);

        dataFormatada = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        date = new Date();
        tag = (TextView) findViewById(R.id.tvValor);
        data = (TextView) findViewById(R.id.tvData);

     /*   toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
*/
        registrarBluetoothReceiver();

      /*  fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent replyIntent = new Intent();
                if (retorna == null) {
                    setResult(RESULT_CANCELED, replyIntent);
                } else {
                    replyIntent.putExtra(RESPOSTA_TAG, retorna);
                    setResult(RESULT_OK, replyIntent);
                }
                finish();
            }
        });*/
    }

    /* RECEBER LEITURA */
    private void registrarBluetoothReceiver() {
        BluetoothReceiver.bindListener(new BluetoothListener() {
            @Override
            public void messageReceived(Intent intent) {

                dados = intent.getStringExtra("resposta");

                runOnUiThread(new Runnable() {

                    public void run() {

                        if(dados != null && dados.contains("EP: ")){ /* Tag */ /* PREENCHER A LISTA COM OS VALORES */
                           String textoTag =  dados.replaceAll("EP:","");
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            Date dataAtual = calendar.getTime();
                            dataFinal = dataFormatada.format(dataAtual);
                            retorna = textoTag + "_" + dataFinal;
                            tag.setText(textoTag);
                            data.setText(dataFinal);
                        }
                    }
                });
            }
        });
    }
}