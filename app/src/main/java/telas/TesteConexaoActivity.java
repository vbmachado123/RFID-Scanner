package telas;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.rfidscanner.R;
import com.uk.tsl.rfid.DeviceListActivity;
import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.device.ConnectionState;
import com.uk.tsl.rfid.asciiprotocol.device.IAsciiTransport;
import com.uk.tsl.rfid.asciiprotocol.device.ObservableReaderList;
import com.uk.tsl.rfid.asciiprotocol.device.Reader;
import com.uk.tsl.rfid.asciiprotocol.device.ReaderManager;
import com.uk.tsl.rfid.asciiprotocol.device.TransportType;
import com.uk.tsl.rfid.asciiprotocol.responders.LoggerResponder;
import com.uk.tsl.utils.Observable;

import static com.uk.tsl.rfid.DeviceListActivity.EXTRA_DEVICE_ACTION;
import static com.uk.tsl.rfid.DeviceListActivity.EXTRA_DEVICE_INDEX;

/* Classe criada para testar a conexao - 11/07/20 */
public class TesteConexaoActivity extends AppCompatActivity {

    private Button btConectar;
    private Reader mReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste_conexao);

        // Ensure the shared instance of AsciiCommander exists
        AsciiCommander.createSharedInstance(getApplicationContext());

        AsciiCommander commander = getCommander();

        // Ensure that all existing responders are removed
        commander.clearResponders();

        // Add the LoggerResponder - this simply echoes all lines received from the reader to the log
        // and passes the line onto the next responder
        // This is added first so that no other responder can consume received lines before they are logged.
        commander.addResponder(new LoggerResponder());

        // Add a synchronous responder to handle synchronous commands
        commander.addSynchronousResponder();

        // Create the single shared instance for this ApplicationContext
        ReaderManager.create(getApplicationContext());

        // Add observers for changes
        ReaderManager.sharedInstance().getReaderList().readerAddedEvent().addObserver(mAddedObserver);
        ReaderManager.sharedInstance().getReaderList().readerUpdatedEvent().addObserver(mUpdatedObserver);
        ReaderManager.sharedInstance().getReaderList().readerRemovedEvent().addObserver(mRemovedObserver);


        btConectar = (Button) findViewById(R.id.btConectar);
        btConectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mIsSelectingReader = true;
                int index = -1;
                if( mReader != null )
                {
                    index = ReaderManager.sharedInstance().getReaderList().list().indexOf(mReader);
                }
                Intent selectIntent = new Intent(TesteConexaoActivity.this, DeviceListActivity.class);
                if( index >= 0 )
                {
                    selectIntent.putExtra(EXTRA_DEVICE_INDEX, index);
                }
                startActivityForResult(selectIntent, DeviceListActivity.SELECT_DEVICE_REQUEST);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DeviceListActivity.SELECT_DEVICE_REQUEST:
                // When DeviceListActivity returns with a device to connect
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
                break;
        }
    }


    /**
     * @return the current AsciiCommander
     */
    protected AsciiCommander getCommander()
    {
        return AsciiCommander.sharedInstance();
    }

    //----------------------------------------------------------------------------------------------
    // ReaderList Observers
    //----------------------------------------------------------------------------------------------
    Observable.Observer<Reader> mAddedObserver = new Observable.Observer<Reader>()
    {
        @Override
        public void update(Observable<? extends Reader> observable, Reader reader)
        {
            // See if this newly added Reader should be used
            AutoSelectReader(true);
        }
    };

    Observable.Observer<Reader> mUpdatedObserver = new Observable.Observer<Reader>()
    {
        @Override
        public void update(Observable<? extends Reader> observable, Reader reader)
        {
        }
    };

    Observable.Observer<Reader> mRemovedObserver = new Observable.Observer<Reader>()
    {
        @Override
        public void update(Observable<? extends Reader> observable, Reader reader)
        {
            mReader = null;
            // Was the current Reader removed
            if( reader == mReader)
            {
                mReader = null;

                // Stop using the old Reader
                getCommander().setReader(mReader);
            }
        }
    };

    private void AutoSelectReader(boolean attemptReconnect)
    {
        ObservableReaderList readerList = ReaderManager.sharedInstance().getReaderList();
        Reader usbReader = null;
        if( readerList.list().size() >= 1)
        {
            // Currently only support a single USB connected device so we can safely take the
            // first CONNECTED reader if there is one
            for (Reader reader : readerList.list())
            {
                if (reader.hasTransportOfType(TransportType.USB))
                {
                    usbReader = reader;
                    break;
                }
            }
        }

        if( mReader == null )
        {
            if( usbReader != null )
            {
                // Use the Reader found, if any
                mReader = usbReader;
                getCommander().setReader(mReader);
            }
        }
        else
        {
            // If already connected to a Reader by anything other than USB then
            // switch to the USB Reader
            IAsciiTransport activeTransport = mReader.getActiveTransport();
            if ( activeTransport != null && activeTransport.type() != TransportType.USB && usbReader != null)
            {
                mReader.disconnect();

                mReader = usbReader;

                // Use the Reader found, if any
                getCommander().setReader(mReader);
            }
        }

        // Reconnect to the chosen Reader
        if( mReader != null
                && !mReader.isConnecting()
                && (mReader.getActiveTransport()== null || mReader.getActiveTransport().connectionStatus().value() == ConnectionState.DISCONNECTED))
        {
            // Attempt to reconnect on the last used transport unless the ReaderManager is cause of OnPause (USB device connecting)
            if( attemptReconnect )
            {
                if( mReader.allowMultipleTransports() || mReader.getLastTransportType() == null )
                {
                    // Reader allows multiple transports or has not yet been connected so connect to it over any available transport
                    mReader.connect();
                }
                else
                {
                    // Reader supports only a single active transport so connect to it over the transport that was last in use
                    mReader.connect(mReader.getLastTransportType());
                }
            }
        }
    }


}