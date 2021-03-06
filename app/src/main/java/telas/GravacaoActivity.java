package telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rfidscanner.BuildConfig;
import com.uk.tsl.rfid.DeviceListActivity;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.example.rfidscanner.R;
import com.uk.tsl.rfid.asciiprotocol.DeviceProperties;
import com.uk.tsl.rfid.asciiprotocol.commands.ReadTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.device.Reader;
import com.uk.tsl.rfid.asciiprotocol.device.ReaderManager;
import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.rfid.asciiprotocol.enumerations.EnumerationBase;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderResponder;
import com.uk.tsl.utils.HexEncoding;
import com.uk.tsl.utils.Observable;

import org.w3c.dom.Text;

import java.util.ArrayList;

import adapter.GravacaoAdapter;
import adapter.LeituraAdapter;
import bluetooth.Bluetooth;
import bluetooth.BluetoothListener;
import bluetooth.BluetoothReceiver;
import model.Leitura;
import service.BluetoothService;
import util.Data;
import util.InventoryModel;
import util.ModelBase;
import util.ModelException;
import util.WeakHandler;

public class GravacaoActivity extends AppCompatActivity {
    private static final boolean D = BuildConfig.DEBUG;
    private static final String TAG = "GravacaoActivity";
    private Toolbar toolbar;
    private EditText numeroTag, textoGravar;
    private SeekBar sbPotencia;
    private TextView tvPotencia, tvLer, tvLimpar, tvEscrever, tvTagsLidas;
    private ListView lvLista;
    private GravacaoAdapter adapter;
    private Leitura l = new Leitura();
    private String textoTag = "", dataFinal = "";
    private String dados;
    private ArrayList<Leitura> leituras;
    // Local Bluetooth adapter
    private InventoryModel mModel;
    private AsciiCommander commander;
    private ParameterEnumerationArrayAdapter<Databank> mDatabankArrayAdapter;
    private int mPowerLevel = AntennaParameters.MaximumCarrierPower;

    private Reader mReader = null;
    private ImageButton ibLimpar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gravacao);

        mModel = new InventoryModel();
        commander = getCommander();
        mModel.setCommander(commander);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        leituras = new ArrayList<>();
        adapter = new GravacaoAdapter(this, leituras);

        validaCampo();
        sbPotencia.setOnSeekBarChangeListener(mPowerSeekBarListener);
        defineLimitesPotencia();

        mModel.setHandler(mGenericModelHandler);

        // Set up the spinner with the memory bank selections
        mDatabankArrayAdapter = new ParameterEnumerationArrayAdapter<Databank>(this, android.R.layout.simple_spinner_item, mDatabanks);
        // Find and set up the sessions spinner

        Spinner spinner = (Spinner) findViewById(R.id.bankSpinner);
        mDatabankArrayAdapter.setDropDownViewResource(R.layout.spinner_text_color);
        spinner.setAdapter(mDatabankArrayAdapter);
        spinner.setOnItemSelectedListener(mBankSelectedListener);
        spinner.setSelection(mDatabanks.length - 1);

        /* NÃO FUNCIONA */
       /* mResultTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                numeroTag.setText(mResultTextView.getText().toString());
                return false;
            }
        });*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReaderManager.sharedInstance().getReaderList().readerAddedEvent().removeObserver(mAddedObserver);
        ReaderManager.sharedInstance().getReaderList().readerUpdatedEvent().removeObserver(mUpdatedObserver);
        ReaderManager.sharedInstance().getReaderList().readerRemovedEvent().removeObserver(mRemovedObserver);
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
            if (reader == mReader) {
                mReader = null;
                getCommander().setReader(mReader);
            }
        }
    };

    /* SeekBar -> Alterar potencia */
    private void defineLimitesPotencia() {
        DeviceProperties deviceProperties = getCommander().getDeviceProperties();

        sbPotencia.setMax(deviceProperties.getMaximumCarrierPower() - deviceProperties.getMinimumCarrierPower());
        mPowerLevel = deviceProperties.getMaximumCarrierPower();
        sbPotencia.setProgress(mPowerLevel - deviceProperties.getMinimumCarrierPower());
    }

    private SeekBar.OnSeekBarChangeListener mPowerSeekBarListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Nothing to do here
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            // Update the reader's setting only after the user has finished changing the value
            updatePowerSetting(getCommander().getDeviceProperties().getMinimumCarrierPower() + seekBar.getProgress());
            mModel.getCommand().setOutputPower(mPowerLevel);
            mModel.updateConfiguration();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            updatePowerSetting(getCommander().getDeviceProperties().getMinimumCarrierPower() + progress);
        }
    };

    private void updatePowerSetting(int level) {
        mPowerLevel = level;
        tvPotencia.setText(mPowerLevel + " dBm");
    }


    private AdapterView.OnItemSelectedListener mBankSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Databank targetBank = (Databank) parent.getItemAtPosition(pos);

            if (mModel.getReadCommand() != null) {
                mModel.getReadCommand().setBank(targetBank);
            }
            if (mModel.getWriteCommand() != null) {
                mModel.getWriteCommand().setBank(targetBank);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    @SuppressLint("NewApi")
    private void validaCampo() {

        numeroTag = (EditText) findViewById(R.id.etTag);
        textoGravar = (EditText) findViewById(R.id.etTextoSalvar);
        sbPotencia = (SeekBar) findViewById(R.id.sbPotencia);
        tvPotencia = (TextView) findViewById(R.id.tvPotencia);
        tvLer = (TextView) findViewById(R.id.tvLer);
        tvLimpar = (TextView) findViewById(R.id.tvLimpar);
        tvEscrever = (TextView) findViewById(R.id.tvEscrever);
        ibLimpar = (ImageButton) findViewById(R.id.ibLimpar);

        lvLista = (ListView) findViewById(R.id.resultTextView);
       // mResultScrollView = (ScrollView) findViewById(R.id.resultScrollView);

        numeroTag.addTextChangedListener(mTargetTagEditTextChangedListener);
        textoGravar.addTextChangedListener(mDataEditTextChangedListener);

        ibLimpar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numeroTag.setText("");
                textoGravar.setText("");
            }
        });

        //   tvTagsLidas = (TextView) findViewById(R.id.tvTagsLidas);

        lvLista.setAdapter(adapter);
        lvLista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Leitura l = leituras.get(position);
                if(l.getNumeroTag().contains("EPC:")) {
                    textoTag = l.getNumeroTag().replaceAll("EPC: ", "");
                    numeroTag.setText(textoTag);
                } else
                    Toast.makeText(GravacaoActivity.this, "Selecione uma TAG para gravar", Toast.LENGTH_SHORT).show();
            }
        });

        tvLer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mModel.read();
            }
        });

        tvLimpar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              limpar();
            }
        });

        tvEscrever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                   // mModel.lockTest();
                    mModel.write();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void limpar() {
        numeroTag.setText("");
        textoGravar.setText("");
        leituras.clear();
        adapter.notifyDataSetChanged();
    }

    public AsciiCommander getCommander() {
        return AsciiCommander.sharedInstance();
    }

    public class ParameterEnumerationArrayAdapter<T extends EnumerationBase> extends ArrayAdapter<T> {
        private final T[] mValues;

        public ParameterEnumerationArrayAdapter(Context context, int textViewResourceId, T[] objects) {
            super(context, textViewResourceId, objects);
            mValues = objects;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            view.setTextColor(R.color.colorPrimary);
            view.setText(mValues[position].getDescription());
            return view;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            view.setTextColor(R.color.colorPrimary);
            view.setText(mValues[position].getDescription());
            return view;
        }
    }

    private Databank[] mDatabanks = new Databank[]{
            Databank.ELECTRONIC_PRODUCT_CODE,
            Databank.TRANSPONDER_IDENTIFIER,
            Databank.RESERVED,
            Databank.USER
    };

    private final WeakHandler<GravacaoActivity> mGenericModelHandler = new WeakHandler<GravacaoActivity>(this) {

        @Override
        public void handleMessage(Message msg, GravacaoActivity thisActivity) {
            try {
                switch (msg.what) {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        if (mModel.error() != null) {
                            preencheLista("Task failed:" + mModel.error().getMessage(), "");
                           // mResultTextView.append("\n Task failed:\n" + mModel.error().getMessage() + "\n\n");
                           /* mResultScrollView.post(new Runnable() {
                                public void run() {
                                    mResultScrollView.fullScroll(View.FOCUS_DOWN);
                                }
                            });*/

                        }
                        // UpdateUI();
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        String message = (String) msg.obj;
                       if(message.contains("EPC")) {

                           String[] tagRecebida = message.split("\n");
                           preencheLista(tagRecebida[0], tagRecebida[1]);
                       }
                       else if(!(message.equals("")))
                           preencheLista(message, "");
                        // mResultTextView.append(message);
                 /*       mResultScrollView.post(new Runnable() {
                            public void run() {
                                mResultScrollView.fullScroll(View.FOCUS_DOWN);
                            }
                        });*/
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
            }

        }
    };

    private void preencheLista(String linha1, String linha2){
        Leitura l = new Leitura();
        l.setNumeroTag(linha1);
        l.setDataHora(linha2);
        leituras.add(l);
        adapter.notifyDataSetChanged();
    }

    private TextWatcher mTargetTagEditTextChangedListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String value = s.toString();
            if (mModel.getReadCommand() != null) {
                mModel.getReadCommand().setSelectData(value);
            }
            if (mModel.getWriteCommand() != null) {
                mModel.getWriteCommand().setSelectData(value);
            }
        }
    };

    private TextWatcher mDataEditTextChangedListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String value = s.toString();
            if( mModel.getWriteCommand() != null ) {
                byte[] data = null;
                try {
                    data = HexEncoding.stringToBytes(value);
                    mModel.getWriteCommand().setData(data);
                } catch (Exception e) {
                    // Ignore if invalid
                }
            }
            //UpdateUI();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.unlock_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            case R.id.item_destrava:
                try {

                    mModel.lockTest();
                    limpar();
                    mModel.read();

                } catch (ModelException e) {
                    e.printStackTrace();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
