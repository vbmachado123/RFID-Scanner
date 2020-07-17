package telas;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.rfidscanner.R;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.DeviceProperties;
import com.uk.tsl.rfid.asciiprotocol.parameters.AntennaParameters;
import com.uk.tsl.rfid.asciiprotocol.responders.ISignalStrengthReceivedDelegate;

import util.InventoryModel;
import util.ModelBase;
import util.SignalPercentageConverter;
import util.WeakHandler;

public class ProcurarTagActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText tvTag;
    private String tag;
    private String forcaSinal;
    private InventoryModel mModel;
    private AsciiCommander commander;
    private int mPowerLevel = AntennaParameters.MaximumCarrierPower;
    private SeekBar sbPotencia;
    private TextView tvPotencia;
    private TextView forcaTag;
    private SignalPercentageConverter mPercentageConverter = new SignalPercentageConverter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procurar_tag);

        mGenericModelHandler = new GenericHandler(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tag = getIntent().getStringExtra("tagProura");

        validaCampo();

        mModel = new InventoryModel();
        mModel.setCommander(getCommander());
        mModel.setHandler(mGenericModelHandler);

        tvTag.addTextChangedListener(mTargetTagEditTextChangedListener);
        tvTag.setOnFocusChangeListener(mTargetTagFocusChangedListener);

        mModel.setRawSignalDelegate(new ISignalStrengthReceivedDelegate() {
            @Override
            public void signalStrengthReceived(Integer level)
            {
                final String value = level == null ? "---" : String.format("%d %%", mPercentageConverter.asPercentage(level));
                forcaTag.post(new Runnable() {
                    @Override
                    public void run()
                    {
                        forcaTag.setText(mModel.isScanning() ? value : "---");
                    }
                });
            }
        });

        mModel.setPercentageSignalDelegate(new ISignalStrengthReceivedDelegate() {
            @Override
            public void signalStrengthReceived(Integer level)
            {
                final String value = level == null ? "---" : level.toString() + "%";
                forcaTag.post(new Runnable() {
                    @Override
                    public void run()
                    {
                        forcaTag.setText(mModel.isScanning() ? value : "---");
                    }
                });
            }
        });

        defineLimitesPotencia();
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

    private void validaCampo() {
        sbPotencia = (SeekBar) findViewById(R.id.sbPotencia);
        tvPotencia = (TextView) findViewById(R.id.tvPotencia);
        forcaTag = (TextView) findViewById(R.id.forcaTag);
        tvTag = (EditText) findViewById(R.id.tvTag);
        tvTag.setText(tag);

        sbPotencia.setOnSeekBarChangeListener(mPowerSeekBarListener);
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

    private static GenericHandler mGenericModelHandler;

    private static class GenericHandler extends WeakHandler<ProcurarTagActivity>
    {
        public GenericHandler(ProcurarTagActivity t)
        {
            super(t);
        }

        @Override
        public void handleMessage(Message msg, ProcurarTagActivity t)
        {
            try {
                switch (msg.what) {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        if( t.mModel.error() != null ) {
                          //  t.appendMessage("\n Task failed:\n" + t.mModel.error().getMessage() + "\n\n");
                        }
                      //  t.UpdateUI();
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        String message = (String)msg.obj;
                       // t.appendMessage(message);
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
            }

        }
    };

    public AsciiCommander getCommander() {
        return AsciiCommander.sharedInstance();
    }

    private TextWatcher mTargetTagEditTextChangedListener = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            String value = s.toString();

            mModel.setTargetTagEpc(value);
          //  UpdateUI();
        }
    };


    //----------------------------------------------------------------------------------------------
    // Handler for when editing has finished on the target tag
    //----------------------------------------------------------------------------------------------

    private View.OnFocusChangeListener mTargetTagFocusChangedListener =
            new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if (!hasFocus)
                    {
                        mModel.setTargetTagEpc(tvTag.getText().toString());
                        mModel.updateTarget();
                    }
                }
            };


}