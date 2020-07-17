package telas;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rfidscanner.R;
import com.uk.tsl.rfid.DeviceListActivity;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.device.ConnectionState;
import com.uk.tsl.rfid.asciiprotocol.device.IAsciiTransport;
import com.uk.tsl.rfid.asciiprotocol.device.ObservableReaderList;
import com.uk.tsl.rfid.asciiprotocol.device.Reader;
import com.uk.tsl.rfid.asciiprotocol.device.ReaderManager;
import com.uk.tsl.rfid.asciiprotocol.device.TransportType;
import com.uk.tsl.rfid.asciiprotocol.responders.ISignalStrengthReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;
import com.uk.tsl.utils.Observable;
import com.uk.tsl.utils.StringHelper;

import util.InventoryModel;
import util.ModelBase;
import util.SignalPercentageConverter;
import util.WeakHandler;

import static com.uk.tsl.rfid.DeviceListActivity.EXTRA_DEVICE_ACTION;
import static com.uk.tsl.rfid.DeviceListActivity.EXTRA_DEVICE_INDEX;

/* Classe criada para testar a conexao - 11/07/20 */
public class TesteConexaoActivity extends AppCompatActivity {


    private TextView potencia, tvPotenciaSubtitulo;
    private EditText tag;

    private InventoryModel mModel;

    private SignalPercentageConverter mPercentageConverter = new SignalPercentageConverter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste_conexao);

        mGenericModelHandler = new GenericHandler(this);

        tag = (EditText) findViewById(R.id.tvTag);
        potencia = (TextView) findViewById(R.id.tvPotencia);
        tvPotenciaSubtitulo = (TextView) findViewById(R.id.tvPotenciaSubtitulo);

        String tagProcura = "2019022812776A031A70067A";
        tag.setText(tagProcura);

        tag.addTextChangedListener(mTargetTagEditTextChangedListener);
        tag.setOnFocusChangeListener(mTargetTagFocusChangedListener);
        // Create a (custom) model and configure its commander and handler
        mModel = new InventoryModel();
        mModel.setCommander(getCommander());
        mModel.setHandler(mGenericModelHandler);

        mModel.setRawSignalDelegate(new ISignalStrengthReceivedDelegate() {
            @Override
            public void signalStrengthReceived(Integer level) {
                final String value = level == null ? "---" : String.format("%d %%", mPercentageConverter.asPercentage(level));
                potencia.post(new Runnable() {
                    @Override
                    public void run() {
                        potencia.setText(mModel.isScanning() ? value : "---");
                    }
                });
            }
        });

        mModel.setPercentageSignalDelegate(new ISignalStrengthReceivedDelegate() {
            @Override
            public void signalStrengthReceived(Integer level) {
                final String value = level == null ? "---" : level.toString() + "%";
                potencia.post(new Runnable() {
                    @Override
                    public void run() {
                        potencia.setText(mModel.isScanning() ? value : "---");
                    }
                });
            }
        });
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

            mModel.setTargetTagEpc(value);
            UpdateUI();
        }
    };

    //----------------------------------------------------------------------------------------------
    // Handler for when editing has finished on the target tag
    //----------------------------------------------------------------------------------------------

    private View.OnFocusChangeListener mTargetTagFocusChangedListener =
            new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        mModel.setTargetTagEpc(tag.getText().toString());
                        mModel.updateTarget();
                    }
                }
            };


    private static class GenericHandler extends WeakHandler<TesteConexaoActivity> {
        public GenericHandler(TesteConexaoActivity t) {
            super(t);
        }

        @Override
        public void handleMessage(Message msg, TesteConexaoActivity t) {
            try {
                switch (msg.what) {
                    case ModelBase.BUSY_STATE_CHANGED_NOTIFICATION:
                        if (t.mModel.error() != null) {
                            //   t.appendMessage("\n Task failed:\n" + t.mModel.error().getMessage() + "\n\n");
                            Toast.makeText(t, t.mModel.error().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        t.UpdateUI();
                        break;

                    case ModelBase.MESSAGE_NOTIFICATION:
                        String message = (String) msg.obj;
                        //  t.appendMessage(message);
                        Toast.makeText(t, message, Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
            }

        }
    }

    ;

    //
    // Set the state for the UI controls
    //
    private void UpdateUI() {
        boolean isConnected = getCommander().isConnected();
        boolean canIssueCommand = isConnected & !mModel.isBusy();

        tvPotenciaSubtitulo.setText(String.format("Using: %s ASCII command", mModel.isFindTagCommandAvailable() ? "\".ft\" - Find Tag" : "\".iv\" - Inventory"));
        String instructions = "";
        if (isConnected) {
            if (StringHelper.isNullOrEmpty(mModel.getTargetTagEpc())) {
                instructions = "Enter a full or partial EPC.";
            } else {
                instructions = "Pull trigger to scan";
            }
        } else {
            instructions = "Connect a TSL Reader";
        }
        tvPotenciaSubtitulo.setText(instructions);

    }


    // The handler for model messages
    private static GenericHandler mGenericModelHandler;

    protected AsciiCommander getCommander() {
        return AsciiCommander.sharedInstance();
    }

}