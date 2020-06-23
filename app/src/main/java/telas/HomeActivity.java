package telas;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rfidscanner.R;
import com.uk.tsl.rfid.DeviceListActivity;

import bluetooth.BluetoothListener;
import bluetooth.*;
import service.BluetoothService;
import util.Permissao;
import util.Preferencias;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TableRow trConectar, trLeitura, trGravacao, trInventario, trConfiguracoes;
    private Context context = this;
    private static boolean conexao;

    private BluetoothAdapter adapter = null;
    private static BluetoothDevice device = null;
    private BluetoothSocket socket = null;

    private Intent service;
    private BluetoothService bluetoothService;

    private static final int SOLICITA_BLUETOOTH = 1, SOLICITA_CONEXAO = 2, MESSAGE_READ = 3;
    private static String MAC = null;
    private static final String TAG = "Bluetooth", TAGLEITURA = "Teste";
    private TextView tvConectar, tvLeitura, tvGravacao;

    private String verificaConexao;

    @SuppressLint({"ResourceAsColor", "NewApi", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            TextView versao = (TextView) findViewById(R.id.txt_versaapp);
            versao.setText("Versão " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Permissao.Permissoes(this);

        validaCampo();

        Preferencias preferencias = new Preferencias(HomeActivity.this);
        // conexao = preferencias.getConexao();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bluetoothService = new BluetoothService();

        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(context, "Bluetooth não suportado", Toast.LENGTH_SHORT).show();
        } else if (!adapter.isEnabled()) {
            Intent ativaIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaIntent, SOLICITA_BLUETOOTH);
        } else { /* faz algo */ }

        /* VERIFICANDO SE POSSUI CONEXÃO ATIVA COM O LEITOR */
        if (!conexao)
            toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.vermelhodesativado)));
        else {
            toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            tvConectar.setText("Desconectar");
            /*Log.i(TAGLEITURA, commander.getLastCommandLine());*/
        }

        registrarBluetoothReceiver();
    }

    private void validaCampo() {
        trConectar = (TableRow) findViewById(R.id.trConectar);
        trLeitura = (TableRow) findViewById(R.id.trLeitura);
        trGravacao = (TableRow) findViewById(R.id.trGravacao);
        trInventario = (TableRow) findViewById(R.id.trInventario);
        trConfiguracoes = (TableRow) findViewById(R.id.trConfiguracao);
        tvConectar = (TextView) findViewById(R.id.tvConectar);
        tvLeitura = (TextView) findViewById(R.id.tvLeitura);
        tvGravacao = (TextView) findViewById(R.id.tvGravacao);

        trConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //listar

                if (conexao) { //conexao ativa -> desconectar
                    conexao = false;
                    bluetoothService.setConexao(conexao);
                    pararServer();
                    tvConectar.setText("Conectar");
                    toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.vermelhodesativado)));
                } else { //conexao desativada
                    Intent lista = new Intent(HomeActivity.this, DeviceListActivity.class);
                    startActivityForResult(lista, SOLICITA_CONEXAO);
                }
            }
        });

        trLeitura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao == true) {
                    acessaActivity(LeituraActivity.class);
                    /*acessaActivity(LerActivity.class);*/
                } else
                    Toast.makeText(context, "Conecte com o leitor para prosseguir", Toast.LENGTH_SHORT).show();
            }
        });

        trGravacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao == true) acessaActivity(GravacaoActivity.class);
                else
                    Toast.makeText(context, "Conecte com o leitor para prosseguir", Toast.LENGTH_SHORT).show();
            }
        });

        trInventario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (conexao == true)
                    acessaActivity(InventarioActivity.class);
                else
                    Toast.makeText(context, "Conecte com o leitor para prosseguir", Toast.LENGTH_SHORT).show();
            }
        });

        trConfiguracoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                solicitaAcesso();

            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void solicitaAcesso() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.Dialog);
        builder.setTitle("Atenção");
        builder.setMessage("Insira a senha para prosseguir");
        builder.setIcon(R.drawable.ic_key);

        final EditText input = new EditText(HomeActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        input.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setTextColor(R.color.colorPrimary);
       // input.setHighlightColor(R.color.colorPrimary);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setPositiveButton("Prosseguir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_text = input.getText().toString();
                if(m_text != null && m_text.equals("00000")) {
                    acessaActivity(ConfiguracaoActivity.class);
                } else {
                    Toast.makeText(context, "Senha incorreta", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SOLICITA_CONEXAO:
                    MAC = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    Log.i(TAG, "> MAC foi recebido: " + MAC);
                    validaConexao();
                    break;
                case SOLICITA_BLUETOOTH:
                    //Faz algo
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /* UTILIZANDO A API RFID */
    private void validaConexao() {
        iniciarServer();

        toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        tvConectar.setText("Desconectar");
    }

    private void iniciarServer() {
        service = new Intent(this, BluetoothService.class);
        service.putExtra("address", MAC);
        startService(service);
    }

    private void pararServer() {
        stopService(new Intent(this, BluetoothService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    private void acessaActivity(Class c) {
        Intent it = new Intent(HomeActivity.this, c);
        startActivity(it);
    }

    private void registrarBluetoothReceiver() {
        BluetoothReceiver.bindListener(new BluetoothListener() {
            @Override
            public void messageReceived(Intent intent) {

                conexao = intent.getBooleanExtra("conexao", false);

                runOnUiThread(new Runnable() {
                    public void run() {
                        if (!conexao) {
                            toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.vermelhodesativado)));
                            tvConectar.setText("Conectar");
                        } else {
                            toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                            tvConectar.setText("Desconectar");
                        }
                    }
                });
            }
        });
    }
}

