package util;


import android.util.Log;

import com.uk.tsl.rfid.asciiprotocol.AsciiCommander;
import com.uk.tsl.rfid.asciiprotocol.commands.BarcodeCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.InventoryCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.ReadTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.WriteTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySelect;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySession;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QueryTarget;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectTarget;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;
import com.uk.tsl.rfid.asciiprotocol.responders.IBarcodeReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ICommandResponseLifecycleDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;
import com.uk.tsl.utils.HexEncoding;

import java.util.Locale;

/* Classe tirada da documentação da biblioteca Rfid.AsciiProtocol-Library2 */
public class InventoryModel extends ModelBase {

    // The instances used to issue commands
    private final ReadTransponderCommand mReadCommand = ReadTransponderCommand.synchronousCommand();
    private final WriteTransponderCommand mWriteCommand = WriteTransponderCommand.synchronousCommand();
    private int mTransponderCount;

    // The inventory command configuration
    public ReadTransponderCommand getReadCommand() {
        return mReadCommand;
    }

    public WriteTransponderCommand getWriteCommand() {
        return mWriteCommand;
    }


    // Control
    private boolean mAnyTagSeen;
    private boolean mEnabled;

    public boolean enabled() {
        return mEnabled;
    }

    public void setEnabled(boolean state) {
        boolean oldState = mEnabled;
        mEnabled = state;

        // Update the commander for state changes
        if (oldState != state) {
            if (mEnabled) {
                // Listen for transponders
                getCommander().addResponder(mInventoryResponder);
                // Listen for barcodes
                getCommander().addResponder(mBarcodeResponder);
            } else {
                // Stop listening for transponders
                getCommander().removeResponder(mInventoryResponder);
                // Stop listening for barcodes
                getCommander().removeResponder(mBarcodeResponder);
            }

        }
    }

    // The command to use as a responder to capture incoming inventory responses
    private InventoryCommand mInventoryResponder;
    // The command used to issue commands
    private InventoryCommand mInventoryCommand;

    // The command to use as a responder to capture incoming barcode responses
    private BarcodeCommand mBarcodeResponder;

    // The inventory command configuration
    public InventoryCommand getCommand() {
        return mInventoryCommand;
    }

    public InventoryModel() {
        mReadCommand.setOffset(0);
        mReadCommand.setLength(1);
        mWriteCommand.setOffset(0);
        mWriteCommand.setLength(1);
        // This is the command that will be used to perform configuration changes and inventories
        mInventoryCommand = new InventoryCommand();
        mInventoryCommand.setResetParameters(TriState.YES);
        // Configure the type of inventory
        mInventoryCommand.setIncludeTransponderRssi(TriState.YES);
        mInventoryCommand.setIncludeChecksum(TriState.YES);
        mInventoryCommand.setIncludePC(TriState.YES);
        mInventoryCommand.setIncludeDateTime(TriState.YES);

        // Use an InventoryCommand as a responder to capture all incoming inventory responses
        mInventoryResponder = new InventoryCommand();

        // Also capture the responses that were not from App commands
        mInventoryResponder.setCaptureNonLibraryResponses(true);

        // Notify when each transponder is seen
        mInventoryResponder.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {

            int mTagsSeen = 0;

            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                mAnyTagSeen = true;

                String tidMessage = transponder.getTidData() == null ? "" : HexEncoding.bytesToString(transponder.getTidData());
                String infoMsg = String.format(Locale.US, "\nRSSI: %d  PC: %04X  CRC: %04X", transponder.getRssi(), transponder.getPc(), transponder.getCrc());
                sendMessageNotification("EPC: " + transponder.getEpc() + infoMsg + "\nTID: " + tidMessage + "\n# " + mTagsSeen);
                mTagsSeen++;
                if (!moreAvailable) {
                    sendMessageNotification("");
                    Log.d("TagCount", String.format("Tags seen: %s", mTagsSeen));
                }
            }
        });

        mInventoryResponder.setResponseLifecycleDelegate(new ICommandResponseLifecycleDelegate() {

            @Override
            public void responseEnded() {
                if (!mAnyTagSeen && mInventoryCommand.getTakeNoAction() != TriState.YES) {
                    sendMessageNotification("No transponders seen");
                }
                mInventoryCommand.setTakeNoAction(TriState.NO);
            }

            @Override
            public void responseBegan() {
                mAnyTagSeen = false;
            }
        });

        // This command is used to capture barcode responses
        mBarcodeResponder = new BarcodeCommand();
        mBarcodeResponder.setCaptureNonLibraryResponses(true);
        mBarcodeResponder.setUseEscapeCharacter(TriState.YES);
        mBarcodeResponder.setBarcodeReceivedDelegate(new IBarcodeReceivedDelegate() {
            @Override
            public void barcodeReceived(String barcode) {
                sendMessageNotification("BC: " + barcode);
            }
        });


    }

    //
    // Reset the reader configuration to default command values
    //
    public void resetDevice() {
        if (getCommander().isConnected()) {
            FactoryDefaultsCommand fdCommand = new FactoryDefaultsCommand();
            fdCommand.setResetParameters(TriState.YES);
            getCommander().executeCommand(fdCommand);
        }
    }

    //
    // Update the reader configuration from the command
    // Call this after each change to the model's command
    //
    public void updateConfiguration() {
        if (getCommander().isConnected()) {
            mInventoryCommand.setTakeNoAction(TriState.YES);
            getCommander().executeCommand(mInventoryCommand);
        }
    }

    //
    // Perform an inventory scan with the current command parameters
    //
    public void scan() {
        testForAntenna();
        if (getCommander().isConnected()) {
            mInventoryCommand.setTakeNoAction(TriState.NO);
            getCommander().executeCommand(mInventoryCommand);
        }
    }


    //
    // Test for the presence of the antenna
    //
    public void testForAntenna() {
        if (getCommander().isConnected()) {
            InventoryCommand testCommand = InventoryCommand.synchronousCommand();
            testCommand.setTakeNoAction(TriState.YES);
            getCommander().executeCommand(testCommand);
            if (!testCommand.isSuccessful()) {
                sendMessageNotification("ER:Error! Code: " + testCommand.getErrorCode() + " " + testCommand.getMessages().toString());
            }
        }
    }

    /* Gravação */
    private void setFixedReadParameters() {
        mReadCommand.setResetParameters(TriState.YES);

        // Configure the select to match the given EPC
        // EPC is in hex and length is in bits
        String epcHex = mReadCommand.getSelectData();

        if (epcHex == null || epcHex.length() == 0) {
            // Match anything by not selecting tags and querying the default A state
            mReadCommand.setInventoryOnly(TriState.YES);

            mReadCommand.setQuerySelect(QuerySelect.ALL);
            mReadCommand.setQuerySession(QuerySession.SESSION_0);
            mReadCommand.setQueryTarget(QueryTarget.TARGET_A);

            // Reset other properties used when matching
            mReadCommand.setSelectData(null);
            mReadCommand.setSelectOffset(-1);
            mReadCommand.setSelectLength(-1);
            mReadCommand.setSelectAction(SelectAction.NOT_SPECIFIED);
            mReadCommand.setSelectTarget(SelectTarget.NOT_SPECIFIED);
        } else {
            mReadCommand.setInventoryOnly(TriState.NO);

            // Only match the EPC value not the CRC or PC
            mReadCommand.setSelectOffset(0x20);
            mReadCommand.setSelectLength(epcHex.length() * 4);

            // Use session with long persistence and select tags away from default state
            mReadCommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A);
            mReadCommand.setSelectTarget(SelectTarget.SESSION_2);

            mReadCommand.setQuerySelect(QuerySelect.ALL);
            mReadCommand.setQuerySession(QuerySession.SESSION_2);
            mReadCommand.setQueryTarget(QueryTarget.TARGET_B);
        }


        mReadCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {

            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                byte[] data = transponder.getReadData();
                String dataMessage = (data == null) ? "No data!" : HexEncoding.bytesToString(data);
                String eaMsg = transponder.getAccessErrorCode() == null ? "" : "\n" + transponder.getAccessErrorCode().getDescription() + " (EA)";
                String ebMsg = transponder.getBackscatterErrorCode() == null ? "" : "\n" + transponder.getBackscatterErrorCode().getDescription() + " (EB)";
                String errorMsg = eaMsg + ebMsg;
                if (errorMsg.length() > 0) {
                    errorMsg = "Error: " + errorMsg + "\n";
                }

                sendMessageNotification(String.format(
                        "\nEPC: %s\nData: %s\n%s",
                        transponder.getEpc(),
                        dataMessage,
                        errorMsg
                ));
                ++mTransponderCount;

                if (!moreAvailable) {
                    sendMessageNotification("\n");
                }
            }
        });
    }


    public void read() {
        try {
            sendMessageNotification("\nLendo...\n");

            setFixedReadParameters();
            mTransponderCount = 0;

            performTask(new Runnable() {
                @Override
                public void run() {

                    getCommander().executeCommand(mReadCommand);

                    sendMessageNotification("\nTAGs lidas: " + mTransponderCount + "\n");
                    reportErrors(mReadCommand);
                    sendMessageNotification(String.format("Tempo total: %.2fs", getTaskExecutionDuration()));

                }
            });

        } catch (ModelException e) {
            sendMessageNotification("Unable to perform action: " + e.getMessage());
        }

    }


    //----------------------------------------------------------------------------------------------
    // Write
    //----------------------------------------------------------------------------------------------

    // Set the parameters that are not user-specified
    private void setFixedWriteParameters() {
        mWriteCommand.setResetParameters(TriState.YES);

        // Set the data length
        if (mWriteCommand.getData() == null) {
            mWriteCommand.setLength(0);
        } else {
            mWriteCommand.setLength(mWriteCommand.getData().length / 2);
        }

        // Configure the select to match the given EPC
        // EPC is in hex and length is in bits
        String epcHex = mWriteCommand.getSelectData();

        if (epcHex != null) {
            // Only match the EPC value not the CRC or PC
            mWriteCommand.setSelectOffset(0x20);
            mWriteCommand.setSelectLength(epcHex.length() * 4);
        }

        mWriteCommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A);
        mWriteCommand.setSelectTarget(SelectTarget.SESSION_2);

        mWriteCommand.setQuerySelect(QuerySelect.ALL);
        mWriteCommand.setQuerySession(QuerySession.SESSION_2);
        mWriteCommand.setQueryTarget(QueryTarget.TARGET_B);

        mWriteCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {

            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                String eaMsg = transponder.getAccessErrorCode() == null ? "" : "\n" + transponder.getAccessErrorCode().getDescription() + " (EA)";
                String ebMsg = transponder.getBackscatterErrorCode() == null ? "" : "\n" + transponder.getBackscatterErrorCode().getDescription() + " (EB)";
                String errorMsg = eaMsg + ebMsg;
                if (errorMsg.length() > 0) {
                    errorMsg = "Error: " + errorMsg + "\n";
                }

                sendMessageNotification(String.format(
                        "\nEPC: %s\n\n" + "Foi Gravado: %d of %d\n%s",
                        transponder.getEpc(),
                        transponder.getWordsWritten(), mWriteCommand.getLength(),
                        errorMsg
                ));
                ++mTransponderCount;

                if (!moreAvailable) {
                    sendMessageNotification("\n");
                }
            }
        });
    }


    public void write() {
        try {
            sendMessageNotification("\nGravando...\n");

            setFixedWriteParameters();
            mTransponderCount = 0;

            performTask(new Runnable() {
                @Override
                public void run() {

                    getCommander().executeCommand(mWriteCommand);

                    sendMessageNotification("\nTAGs lidas: " + mTransponderCount + "\n");
                    reportErrors(mWriteCommand);
                    sendMessageNotification(String.format("Tempo total: %.2fs", getTaskExecutionDuration()));

                }
            });

        } catch (ModelException e) {
            sendMessageNotification("Unable to perform action: " + e.getMessage());
        }
    }

    private void reportErrors(AsciiSelfResponderCommandBase command) {
        if (!command.isSuccessful()) {
            sendMessageNotification(String.format(
                    "%s failed!\nError code: %s\n", command.getClass().getSimpleName(), command.getErrorCode()));
            for (String message : command.getMessages()) {
                sendMessageNotification(message + "\n");
            }
        }

    }
}
