package telas;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.MenuItem;
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
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.DeviceProperties;
import com.uk.tsl.rfid.asciiprotocol.device.Reader;
import com.uk.tsl.rfid.asciiprotocol.device.ReaderManager;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;
import com.uk.tsl.utils.Observable;

import bluetooth.BluetoothListener;
import bluetooth.*;
import service.BluetoothService;
import util.Permissao;
import util.Preferencias;

import static com.uk.tsl.rfid.DeviceListActivity.EXTRA_DEVICE_ACTION;
import static com.uk.tsl.rfid.DeviceListActivity.EXTRA_DEVICE_INDEX;

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

    private Reader mReader = null;

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

        verificaConexao();

        Preferencias preferencias = new Preferencias(HomeActivity.this);
        // conexao = preferencias.getConexao();

        bluetoothService = new BluetoothService();

        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(context, "Bluetooth não suportado", Toast.LENGTH_SHORT).show();
        } else if (!adapter.isEnabled()) {
            Intent ativaIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaIntent, SOLICITA_BLUETOOTH);
        } else { /* faz algo */ }

        AsciiCommander.createSharedInstance(getApplicationContext());

        AsciiCommander commander = getCommander();

        commander.clearResponders();

        commander.addResponder(new LoggerResponder());

        commander.addSynchronousResponder();

        if(commander.isConnected()) conexao = true;

        ReaderManager.create(getApplicationContext());

        ReaderManager.sharedInstance().getReaderList().readerAddedEvent().addObserver(mAddedObserver);
        ReaderManager.sharedInstance().getReaderList().readerUpdatedEvent().addObserver(mUpdatedObserver);
        ReaderManager.sharedInstance().getReaderList().readerRemovedEvent().addObserver(mRemovedObserver);

        registrarBluetoothReceiver();
    }

    private void verificaConexao() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* VERIFICANDO SE POSSUI CONEXÃO ATIVA COM O LEITOR */
       if(getCommander() != null){
           switch (getCommander().getConnectionState()) {
               case CONNECTED:
                   toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                   tvConectar.setText("Desconectar");
                   conexao = true;
                   break;
               case CONNECTING:
                   toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.amareloconectando)));
                   tvConectar.setText("Conectando....");
                   break;
               case DISCONNECTED:
                   toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.vermelhodesativado)));
                   conexao = false;
                   break;
               default:
                   toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.vermelhodesativado)));
                   conexao = false;
           }
       }

    }

    Observable.Observer<Reader> mAddedObserver = new Observable.Observer<Reader>() {
        @Override
        public void update(Observable<? extends Reader> observable, Reader reader) {
            // See if this newly added Reader should be used
            //AutoSelectReader(true);
        }
    };

    Observable.Observer<Reader> mUpdatedObserver = new Observable.Observer<Reader>() {
        @Override
        public void update(Observable<? extends Reader> observable, Reader reader) {
        }
    };

    Observable.Observer<Reader> mRemovedObserver = new Observable.Observer<Reader>() {
        @Override
        public void update(Observable<? extends Reader> observable, Reader reader) {
            mReader = null;
            // Was the current Reader removed
            if (reader == mReader) {
                mReader = null;

                // Stop using the old Reader
                getCommander().setReader(mReader);
            }
        }
    };

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
                    verificaConexao();
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
                    /*acessaActivity(TesteConexaoActivity.class);*/
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
                //acessaActivity(InventarioActivity.class);
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
                if (m_text != null && m_text.equals("00000")) {
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
                    if (resultCode == Activity.RESULT_OK) {
                        int readerIndex = data.getExtras().getInt(EXTRA_DEVICE_INDEX);
                        Reader chosenReader = ReaderManager.sharedInstance().getReaderList().list().get(readerIndex);

                        int action = data.getExtras().getInt(EXTRA_DEVICE_ACTION);

                        // If already connected to a different reader then disconnect it
                        if (mReader != null) {
                            if (action == DeviceListActivity.DEVICE_CHANGE || action == DeviceListActivity.DEVICE_DISCONNECT) {
                                mReader.disconnect();
                                if (action == DeviceListActivity.DEVICE_DISCONNECT) {
                                    mReader = null;
                                }
                            }
                        }

                        // Use the Reader found
                        if (action == DeviceListActivity.DEVICE_CHANGE || action == DeviceListActivity.DEVICE_CONNECT) {
                            mReader = chosenReader;
                            getCommander().setReader(mReader);
                        }
                        // displayReaderState();
                    }

                  /*  MAC = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    for(int i = 0; i < ReaderManager.sharedInstance().getReaderList().list().size(); i++){
                        Reader r = ReaderManager.sharedInstance().getReaderList().list().get(i);
                        //if(r.connect())
                    }
                   // Reader chosenReader = ReaderManager.sharedInstance().getReaderList().list();
                    Log.i(TAG, "> MAC foi recebido: " + MAC);*/
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
        mReader.connect();
        //conexao = true;
        //AsciiCommander.createSharedInstance(this);
        toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        tvConectar.setText("Desconectar");
    }

    private void iniciarServer() {
        service = new Intent(this, BluetoothService.class);
        service.putExtra("nome", mReader.getDisplayName());
        //service.putExtra("address", MAC);
        startService(service);
    }

    private void pararServer() {
        mReader.disconnect();
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
        it.putExtra("service", bluetoothService);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                abrirDialog();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void abrirDialog() {
        String versao = "";
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versao = "Versão " + version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.Dialog);
        builder.setTitle("SOS RFiD");
        builder.setMessage(versao);
        builder.setNegativeButton("Fechar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * @return the current AsciiCommander
     */
    protected AsciiCommander getCommander() {
        return AsciiCommander.sharedInstance();
    }

}

