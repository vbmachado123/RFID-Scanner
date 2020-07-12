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
import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.rfid.asciiprotocol.enumerations.EnumerationBase;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderResponder;
import com.uk.tsl.utils.HexEncoding;

import org.w3c.dom.Text;

import java.util.ArrayList;

import adapter.LeituraAdapter;
import bluetooth.Bluetooth;
import bluetooth.BluetoothListener;
import bluetooth.BluetoothReceiver;
import model.Leitura;
import service.BluetoothService;
import util.Data;
import util.InventoryModel;
import util.ModelBase;
import util.WeakHandler;

public class GravacaoActivity extends AppCompatActivity {
    private static final boolean D = BuildConfig.DEBUG;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final String TAG = "GravacaoActivity";
    private Toolbar toolbar;
    private EditText numeroTag, textoGravar;
    private SeekBar sbPotencia;
    private TextView tvPotencia, tvLer, tvLimpar, tvEscrever, tvTagsLidas;
    private ListView lvLista;
    private LeituraAdapter adapter;
    private Leitura l = new Leitura();
    private String textoTag = "", dataFinal = "";
    private String dados;
    private ArrayList<Leitura> leituras;
    // Local Bluetooth adapter
    private InventoryModel mModel;
    private AsciiCommander commander;
    private ParameterEnumerationArrayAdapter<Databank> mDatabankArrayAdapter;
    private int mPowerLevel = AntennaParameters.MaximumCarrierPower;

    private TextView mResultTextView;
    private ScrollView mResultScrollView;

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
       /* leituras = new ArrayList<>();
        adapter = new LeituraAdapter(this, leituras);*/
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

    }

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

    private AdapterView.OnItemSelectedListener mBankSelectedListener = new AdapterView.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Databank targetBank = (Databank)parent.getItemAtPosition(pos);
            if( mModel.getReadCommand() != null ) {
                mModel.getReadCommand().setBank(targetBank);
            }
            if( mModel.getWriteCommand() != null ) {
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

        mResultTextView = (TextView)findViewById(R.id.resultTextView);
        mResultScrollView = (ScrollView)findViewById(R.id.resultScrollView);

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

      /*  lvLista.setAdapter(adapter);
        lvLista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Leitura l = leituras.get(position);

                numeroTag.setText(l.getNumeroTag());
            }
        });*/

        tvLer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mModel.read();
            }
        });

        tvLimpar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numeroTag.setText("");
                textoGravar.setText("");
                mResultTextView.setText("");
            }
        });

        tvEscrever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mModel.write();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public AsciiCommander getCommander(){
        return AsciiCommander.sharedInstance();
    }

    public class ParameterEnumerationArrayAdapter<T extends EnumerationBase > extends ArrayAdapter<T> {
        private final T[] mValues;

        public ParameterEnumerationArrayAdapter(Context context, int textViewResourceId, T[] objects) {
            super(context, textViewResourceId, objects);
            mValues = objects;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView)super.getView(position, convertView, parent);
            view.setTextColor(R.color.colorPrimary);
            view.setText(mValues[position].getDescription());
            return view;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView)super.getDropDownView(position, convertView, parent);
            view.setTextColor(R.color.colorPrimary);
            view.setText(mValues[position].getDescription());
            return view;
        }
    }

    private Databank[] mDatabanks = new Databank[] {
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
                        if( mModel.error() != null ) {
                            mResultTextView.append("\n Task failed:\n" + mModel.error().getMessage() + "\n\n");
                            mResultScrollView.post(new Runnable() { public void run() { mResultScrollView.fullScroll(View.FOCUS_DOWN); } });

                        }
                       // UpdateUI();
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        String message = (String)msg.obj;
                        mResultTextView.append(message);
                        mResultScrollView.post(new Runnable() { public void run() { mResultScrollView.fullScroll(View.FOCUS_DOWN); } });
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
            }

        }
    };


    private TextWatcher mTargetTagEditTextChangedListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            String value = s.toString();
            if( mModel.getReadCommand() != null ) {
                mModel.getReadCommand().setSelectData(value);
            }
            if( mModel.getWriteCommand() != null ) {
                mModel.getWriteCommand().setSelectData(value);
            }
        }
    };

    private TextWatcher mDataEditTextChangedListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

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


}
