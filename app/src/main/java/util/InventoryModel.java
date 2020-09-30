package util;


import android.util.Log;

import com.uk.tsl.rfid.asciiprotocol.commands.BarcodeCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.FindTagCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.InventoryCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.LockCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.ReadTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.SwitchActionCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.WriteTransponderCommand;
import com.uk.tsl.rfid.asciiprotocol.device.Reader;
import com.uk.tsl.rfid.asciiprotocol.enumerations.Databank;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySelect;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QuerySession;
import com.uk.tsl.rfid.asciiprotocol.enumerations.QueryTarget;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SelectTarget;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SwitchAction;
import com.uk.tsl.rfid.asciiprotocol.enumerations.SwitchState;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.responders.AsciiSelfResponderCommandBase;
import com.uk.tsl.rfid.asciiprotocol.responders.IBarcodeReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ICommandResponseLifecycleDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ISignalStrengthReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ISwitchStateReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.SignalStrengthResponder;
import com.uk.tsl.rfid.asciiprotocol.responders.SwitchResponder;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;
import com.uk.tsl.utils.HexEncoding;
import com.uk.tsl.utils.StringHelper;

import java.util.Locale;

/* Classe tirada da documentação da biblioteca Rfid.AsciiProtocol-Library2 */
public class InventoryModel extends ModelBase {

    // The instances used to issue commands
    private final ReadTransponderCommand mReadCommand = ReadTransponderCommand.synchronousCommand();
    private final WriteTransponderCommand mWriteCommand = WriteTransponderCommand.synchronousCommand();
    private final LockCommand mLockCommand = LockCommand.synchronousCommand();
    private int mTransponderCount;

    // The inventory command configuration
    public ReadTransponderCommand getReadCommand() {
        return mReadCommand;
    }

    public WriteTransponderCommand getWriteCommand() {
        return mWriteCommand;
    }

    // True if the User is scanning
    public boolean isScanning() {
        return mScanning;
    }

    public void setScanning(boolean scanning) {
        mScanning = scanning;
    }

    private boolean mScanning = false;
    private FindTagCommand mFindTagCommand;

    // The responder to capture incoming RSSI responses
    private SignalStrengthResponder mSignalStrengthResponder;

    // The switch state responder
    private SwitchResponder mSwitchResponder;

    private boolean mUseFindTagCommand = false;


    private String mTargetTagEpc = null;

    // Control
    private boolean mAnyTagSeen;
    private boolean mEnabled;

    public boolean enabled() {
        return mEnabled;
    }

  //  private String value = "2019022812776A031A700661";

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

        mLockCommand.setTakeNoAction(TriState.YES);
        mLockCommand.setResetParameters(TriState.YES);
        mLockCommand.setReadParameters(TriState.YES);
        mLockCommand.setAccessPassword("12345678");
        // sendMessageNotification("\nQuerying lock command for default parameters...");
        // sendMessageNotification("\nCarga Útil: " + command.getLockPayload());

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

        mInventoryCommand = InventoryCommand.synchronousCommand();
        mFindTagCommand = FindTagCommand.synchronousCommand();
        mSignalStrengthResponder = new SignalStrengthResponder();
        mSwitchResponder = new SwitchResponder();
        mSwitchResponder.setSwitchStateReceivedDelegate(new ISwitchStateReceivedDelegate() {
            @Override
            public void switchStateReceived(SwitchState switchState) {
                // When trigger released
                if (switchState == SwitchState.OFF) {
                    mScanning = false;
                    // Fake a signal report for both percentage and RSSI to indicate action stopped
                    if (mSignalStrengthResponder.getRawSignalStrengthReceivedDelegate() != null) {
                        mSignalStrengthResponder.getRawSignalStrengthReceivedDelegate().signalStrengthReceived(null);
                    }
                    if (mSignalStrengthResponder.getPercentageSignalStrengthReceivedDelegate() != null) {
                        mSignalStrengthResponder.getPercentageSignalStrengthReceivedDelegate().signalStrengthReceived(null);
                    }
                } else if (switchState == SwitchState.SINGLE) {
                    mScanning = true;
                }
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


    /* GRAVAÇÃO */
    //----------------------------------------------------------------------------------------------
    // LEITURA
    //----------------------------------------------------------------------------------------------

    // Set the parameters that are not user-specified
    private void setFixedReadParameters() {
        mReadCommand.setResetParameters(TriState.YES);

        String epcHex = mReadCommand.getSelectData();

        if (epcHex == null || epcHex.length() == 0) {

            mReadCommand.setInventoryOnly(TriState.YES);

            mReadCommand.setQuerySelect(QuerySelect.ALL);
            mReadCommand.setQuerySession(QuerySession.SESSION_0);
            mReadCommand.setQueryTarget(QueryTarget.TARGET_A);

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
                String eaMsg = transponder.getAccessErrorCode() == null ? "" : transponder.getAccessErrorCode().getDescription() + " (EA)";
                String ebMsg = transponder.getBackscatterErrorCode() == null ? "" : transponder.getBackscatterErrorCode().getDescription() + " (EB)";
                String errorMsg = eaMsg + ebMsg;
                if (errorMsg.length() > 0) {
                    errorMsg = "Error: " + errorMsg + "";
                }

                sendMessageNotification(String.format(
                        "EPC: %s\nData: %s\n%s",
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
            sendMessageNotification("Reading...");

            setFixedReadParameters();
            mTransponderCount = 0;

            performTask(new Runnable() {
                @Override
                public void run() {

                    getCommander().executeCommand(mReadCommand);

                    sendMessageNotification("Transponders seen: " + mTransponderCount);
                    reportErrors(mReadCommand);
                    sendMessageNotification(String.format("Time taken: %.2fs", getTaskExecutionDuration()));

                }
            });

        } catch (ModelException e) {
            sendMessageNotification("Unable to perform action: " + e.getMessage());
        }

    }


    //----------------------------------------------------------------------------------------------
    // GRAVAÇÃO
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

        Reader r = getCommander().getReader();

        if (epcHex != null) {
            //r.executeTagOp(new Gen2.Lock(0, new Gen2.LockAction(Gen2.LockAction.EPC_UNLOCK)), t);
            // Only match the EPC value not the CRC or PC
            mWriteCommand.setSelectOffset(0x20);
            mWriteCommand.setSelectLength(epcHex.length() * 4);
        }

        mWriteCommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A);
        mWriteCommand.setSelectTarget(SelectTarget.SESSION_2);

        mWriteCommand.setQuerySelect(QuerySelect.ALL);
        mWriteCommand.setQuerySession(QuerySession.SESSION_2);
        mWriteCommand.setQueryTarget(QueryTarget.TARGET_B);
        mWriteCommand.setAccessPassword(ACCESS_PASSWORD);

        Log.i("Teste", "Gravando: " + mWriteCommand.getData() + " na TAG: " + mWriteCommand.getSelectData() + " no Banco: " + mWriteCommand.getBank());
        //Log.i("Teste", "Data: " + mWriteCommand.getData());

        mWriteCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {

            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                String eaMsg = transponder.getAccessErrorCode() == null ? "" : transponder.getAccessErrorCode().getDescription() + " (EA)";
                String ebMsg = transponder.getBackscatterErrorCode() == null ? "" : transponder.getBackscatterErrorCode().getDescription() + " (EB)";
                String errorMsg = eaMsg + ebMsg;
                if (errorMsg.length() > 0) {
                    errorMsg = "Error: " + errorMsg;
                }

                sendMessageNotification(String.format(
                        "EPC: %s\nWords Written: %d of %d\n%s",
                        transponder.getEpc(),
                        transponder.getWordsWritten(), mWriteCommand.getLength(),
                        errorMsg
                ));
                ++mTransponderCount;

            }
        });
    }

    public void write() {
        try {

            try {
                // usingEPCMemory();

                sendMessageNotification("Gravando...");

                setFixedWriteParameters();
                mTransponderCount = 0;

                Log.i("Teste", "TAG: " + mWriteCommand.getSelectData());
                performTask(new Runnable() {
                    @Override
                    public void run() {
                        mWriteCommand.setAccessPassword(PASSWORD_DATA);
                        mCommander.executeCommand(mWriteCommand);

                        //sendMessageNotification(mWriteCommand.getAccessPassword());
                        //sendMessageNotification(mWriteCommand.getCommandLine());
                        Log.i("Teste", "Linha de Comando: " + mWriteCommand.getCommandLine());
                        sendMessageNotification("Transponders seen: " + mTransponderCount);
                        reportErrors(mWriteCommand);
                        sendMessageNotification(String.format("Time taken: %.2fs", getTaskExecutionDuration()));

                    }
                });

            } catch (ModelException e) {
                sendMessageNotification("Unable to perform action: " + e.getMessage());
                Log.i("Teste", "Erro: " + e.getMessage());
            }
        } catch(Exception e) {
            Log.i("Teste", "Erro: " + e.getMessage());
        }
    }

    // Default passwords
    private static final String DEFAULT_PASSWORD = "00000000";
    private static final String DEFAULT_PASSWORD_DATA = "0000000000000000";

    // Test passwords
    private static final String ACCESS_PASSWORD = "FEDCBA90";
    private static final String PASSWORD_DATA = "87654321FEDCBA90";

    private static final String WRITE_DATA_EXPECTED_OK = "FADEBEEFC0DEFEED";
    private static final String WRITE_DATA_EXPECTED_FAIL = "DEADFACEF00DFAD5";
    private static final String WRITE_BLANK_EXPECTED_OK = "0000000000000000";
    private int mUserDataLength;

    public void lockTest() throws ModelException {
        performTask(new Runnable() {
            @Override
            public void run() {
                //
                // Query current lock payload
                //
                LockCommand command = LockCommand.synchronousCommand();
                command.setTakeNoAction(TriState.YES);
                command.setResetParameters(TriState.YES);
                command.setReadParameters(TriState.YES);
                sendMessageNotification("Querying lock command for default parameters...");
                mCommander.executeCommand(command);
                sendMessageNotification("Payload: " + command.getLockPayload());

                //
                // Read the current passwords
                //
                //  ReadTransponderCommand rCommand = ReadTransponderCommand.synchronousCommand();
                mReadCommand.setSelectBank(Databank.ELECTRONIC_PRODUCT_CODE);
                //   mReadCommand.setSelectData("111122223333444455556666");
                mReadCommand.setSelectOffset(0x20);
                mReadCommand.setSelectLength(0x60);

                mReadCommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A);
                mReadCommand.setSelectTarget(SelectTarget.SESSION_0);

                // Set up the query for the target transponder
                mReadCommand.setQuerySelect(QuerySelect.ALL);
                mReadCommand.setQuerySession(QuerySession.SESSION_0);
                mReadCommand.setQueryTarget(QueryTarget.TARGET_B);

                // Set up the data to be read
                mReadCommand.setBank(Databank.RESERVED);
                mReadCommand.setOffset(0x0);
                mReadCommand.setLength(0x4);

                // Set up the transponder delegate
                mReadCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {

                    @Override
                    public void transponderReceived(TransponderData transponder, boolean moreAvailable) {

                        byte[] data = transponder.getReadData();
                        String dataMessage = (data == null) ? "No data!" : HexEncoding.bytesToString(data);
                        String accessPassword = dataMessage.substring(8);
                        String killPassword = dataMessage.substring(0, 8);

                        sendMessageNotification(
                                String.format("%-6s%s%-4s%-8s    %-4s%-8s",
                                        "EPC:", transponder.getEpc(),
                                        "AP:", accessPassword,
                                        "KP:", killPassword
                                )
                        );

                    }
                });

                // Don't indicate successful reads initially
//          rCommand.setUseAlert(TriState.NO);

                sendMessageNotification("Querying current passwords...");
                mCommander.executeCommand(mReadCommand);

                //
                // Set the passwords
                //
                //  WriteTransponderCommand wrPasswordCommand = WriteTransponderCommand.synchronousCommand();
                mWriteCommand.setResetParameters(TriState.YES);

                mWriteCommand.setSelectBank(Databank.ELECTRONIC_PRODUCT_CODE);
                mWriteCommand.setSelectOffset(0x20);
                mWriteCommand.setSelectLength(0x60);   // Write to any tags matching up to the last bit
                //mWriteCommand.setSelectData(mWriteCommand.getSelectData());

                mWriteCommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A);
                mWriteCommand.setSelectTarget(SelectTarget.SESSION_2);

                mWriteCommand.setQuerySelect(QuerySelect.ALL);
                mWriteCommand.setQuerySession(QuerySession.SESSION_2);
                mWriteCommand.setQueryTarget(QueryTarget.TARGET_B);

                mWriteCommand.setAccessPassword("00000000");

                mWriteCommand.setBank(Databank.RESERVED);
                mWriteCommand.setOffset(0x0);
                mWriteCommand.setLength(PASSWORD_DATA.length() / 4);
                mWriteCommand.setData(HexEncoding.stringToBytes(PASSWORD_DATA));

                mWriteCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {
                    public int transponderCount = 0;

                    @Override
                    public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                        String eaMsg = transponder.getAccessErrorCode() == null ? "" : transponder.getAccessErrorCode().getDescription();
                        String ebMsg = transponder.getBackscatterErrorCode() == null ? "" : transponder.getBackscatterErrorCode().getDescription();

                        sendMessageNotification(String.format(
                                "\nE: %s\nWW: %d\nEA: %s\nEB: %s",
                                transponder.getEpc(),
                                transponder.getWordsWritten(),
                                eaMsg,
                                ebMsg
                        ));
                        ++transponderCount;

                        if (!moreAvailable) {
                            sendMessageNotification("Transponders seen: " + transponderCount);
                            transponderCount = 0;
                        }
                    }
                });

                sendMessageNotification("Setting new passwords...");
                mCommander.executeCommand(mWriteCommand);
                sendMessageNotification(String.format("\nTime taken: %.2fs", getTaskExecutionDuration()));


                //
                // Read back the new passwords
                //
                sendMessageNotification("Querying current passwords...");
                mCommander.executeCommand(mReadCommand);

                // Pause
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //
                // Lock the User Memory
                //
                LockCommand lCommand = LockCommand.synchronousCommand();
                lCommand.setResetParameters(TriState.YES);

                lCommand.setSelectBank(Databank.ELECTRONIC_PRODUCT_CODE);
                lCommand.setSelectOffset(0x20);
                lCommand.setSelectLength(0x60);    // Write to any tags matching up to the last bit

                lCommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A);
                lCommand.setSelectTarget(SelectTarget.SESSION_2);

                lCommand.setQuerySession(QuerySession.SESSION_2);
                lCommand.setQueryTarget(QueryTarget.TARGET_B);

                lCommand.setAccessPassword(ACCESS_PASSWORD);

                lCommand.setLockPayload("00802");

                lCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {
                    public int transponderCount = 0;

                    @Override
                    public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                        String eaMsg = transponder.getAccessErrorCode() == null ? "" : transponder.getAccessErrorCode().getDescription();
                        String ebMsg = transponder.getBackscatterErrorCode() == null ? "" : transponder.getBackscatterErrorCode().getDescription();

                        sendMessageNotification(String.format(
                                "E: %s\nEA: %s\nEB: %s\nLS: %s",
                                transponder.getEpc(),
                                eaMsg,
                                ebMsg,
                                transponder.didLock() ? "Yes" : "No"
                        ));
                        ++transponderCount;

                        if (!moreAvailable) {
                            sendMessageNotification("Transponders seen: " + transponderCount);
                            transponderCount = 0;
                        }
                    }
                });

                sendMessageNotification("Locking User memory...");
                mCommander.executeCommand(lCommand);
                sendMessageNotification(String.format("\nTime taken: %.2fs", getTaskExecutionDuration()));


                //
                // Read the current User memory
                //
                //  ReadTransponderCommand rUserCommand = ReadTransponderCommand.synchronousCommand();
                mReadCommand.setSelectBank(Databank.ELECTRONIC_PRODUCT_CODE);
                // rUserCommand.setSelectData("111122223333444455556666");
                mReadCommand.setSelectOffset(0x20);
                mReadCommand.setSelectLength(0x60);

                mReadCommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A);
                mReadCommand.setSelectTarget(SelectTarget.SESSION_2);

                // Set up the query for the target transponder
                mReadCommand.setQuerySelect(QuerySelect.ALL);
                mReadCommand.setQuerySession(QuerySession.SESSION_2);
                mReadCommand.setQueryTarget(QueryTarget.TARGET_B);

                // Set up the data to be read
                mReadCommand.setBank(Databank.USER);
                mReadCommand.setOffset(0x0);
                mReadCommand.setLength(WRITE_DATA_EXPECTED_OK.length() / 4);

                // Set up the transponder delegate
                mReadCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {

                    @Override
                    public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                        byte[] data = transponder.getReadData();
                        String dataMessage = (data == null) ? "No data!" : HexEncoding.bytesToString(data);

                        sendMessageNotification(
                                String.format("%-6s%s%-18s%s",
                                        "EPC:", transponder.getEpc(),
                                        "User data:", dataMessage
                                )
                        );
                    }
                });

                // Don't indicate successful reads initially
//          rTestCommand.setUseAlert(TriState.NO);

                sendMessageNotification("Reading User data...");
                mCommander.executeCommand(mReadCommand);

                // Pause
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //
                // Attempt to write the user memory with an incorrect password
                //
                // WriteTransponderCommand wrUserCommand = WriteTransponderCommand.synchronousCommand();
                mWriteCommand.setResetParameters(TriState.YES);

                mWriteCommand.setSelectBank(Databank.ELECTRONIC_PRODUCT_CODE);
                mWriteCommand.setSelectOffset(0x20);
                mWriteCommand.setSelectLength(0x60);   // Write to any tags matching up to the last bit

                mWriteCommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A);
                mWriteCommand.setSelectTarget(SelectTarget.SESSION_2);

                mWriteCommand.setQuerySelect(QuerySelect.ALL);
                mWriteCommand.setQuerySession(QuerySession.SESSION_2);
                mWriteCommand.setQueryTarget(QueryTarget.TARGET_B);

                mWriteCommand.setAccessPassword(DEFAULT_PASSWORD);

                mWriteCommand.setBank(Databank.USER);
                mWriteCommand.setOffset(0x0);
                mWriteCommand.setData(HexEncoding.stringToBytes(WRITE_DATA_EXPECTED_OK));
                mUserDataLength = mWriteCommand.getData().length / 2;
                mWriteCommand.setLength(mUserDataLength);

                mWriteCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {
                    public int transponderCount = 0;

                    @Override
                    public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                        String eaMsg = transponder.getAccessErrorCode() == null ? "" : transponder.getAccessErrorCode().getDescription();
                        String ebMsg = transponder.getBackscatterErrorCode() == null ? "" : transponder.getBackscatterErrorCode().getDescription();

                        sendMessageNotification(String.format(
                                "E: %s\nEA: %s\nEB: %s\nWW: %d (%s)",
                                transponder.getEpc(),
                                eaMsg,
                                ebMsg,
                                transponder.getWordsWritten(), transponder.getWordsWritten() != mUserDataLength ? "FAILED" : "Succeeded"
                        ));
                        ++transponderCount;

                        if (!moreAvailable) {
                            sendMessageNotification("Transponders seen: " + transponderCount);
                            transponderCount = 0;
                        }
                    }
                });

                sendMessageNotification("Writing User memory with incorrect passwords...");
                sendMessageNotification(String.format("Data: %s", HexEncoding.bytesToString(mWriteCommand.getData())));
                mCommander.executeCommand(mWriteCommand);
                sendMessageNotification(String.format("Time taken: %.2fs", getTaskExecutionDuration()));


                //
                // Show the result
                //
                sendMessageNotification("Reading User memory...");
                mCommander.executeCommand(mWriteCommand);

                // Pause
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //
                // Attempt to write the User memory with the correct password
                //
                mWriteCommand.setAccessPassword(ACCESS_PASSWORD);

                sendMessageNotification("Writing User memory with CORRECT passwords...");
                sendMessageNotification(String.format("Data: %s", HexEncoding.bytesToString(mWriteCommand.getData())));
                mCommander.executeCommand(mWriteCommand);
                sendMessageNotification(String.format("Time taken: %.2fs", getTaskExecutionDuration()));

                //
                // Show the result
                //
                sendMessageNotification("Reading User memory...");
                mCommander.executeCommand(mWriteCommand);

                // Pause
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //
                // Unlock the user memory
                //
                lCommand.setLockPayload("00800");
                sendMessageNotification("Unlocking User memory...");
                mCommander.executeCommand(lCommand);

                // Pause
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //
                // Attempt to write the User memory with the default password
                //
                mWriteCommand.setAccessPassword(DEFAULT_PASSWORD);
                mWriteCommand.setData(HexEncoding.stringToBytes(WRITE_DATA_EXPECTED_FAIL));

                sendMessageNotification("Writing User memory with DEFAULT passwords...");
                sendMessageNotification(String.format("Data: %s", HexEncoding.bytesToString(mWriteCommand.getData())));
                mCommander.executeCommand(mWriteCommand);
                sendMessageNotification(String.format("Time taken: %.2fs", getTaskExecutionDuration()));

                //
                // Show the result
                //
                sendMessageNotification("Reading User memory...");
                mCommander.executeCommand(mReadCommand);

                // Pause
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //
                // Reset the passwords to default
                //
                mWriteCommand.setData(HexEncoding.stringToBytes(DEFAULT_PASSWORD_DATA));
                sendMessageNotification("Resetting passwords...");
                mCommander.executeCommand(mWriteCommand);

                //
                // Read back the new passwords
                //
                sendMessageNotification("Querying current passwords...");
                mCommander.executeCommand(mReadCommand);

                //
                // Attempt to write the user memory with default password
                //
                mWriteCommand.setAccessPassword(DEFAULT_PASSWORD);
                mWriteCommand.setData(HexEncoding.stringToBytes(WRITE_BLANK_EXPECTED_OK));

                sendMessageNotification("Clearing User memory with default passwords...");
                sendMessageNotification(String.format("\nData: %s", HexEncoding.bytesToString(mWriteCommand.getData())));
                mCommander.executeCommand(mWriteCommand);
                sendMessageNotification(String.format("\nTime taken: %.2fs", getTaskExecutionDuration()));

                // Pause
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //
                // Show the result
                //
                sendMessageNotification("Reading User memory...");
                mCommander.executeCommand(mWriteCommand);
                sendMessageNotification(String.format("\nTime taken: %.2fs", getTaskExecutionDuration()));
            }
        });
    }

    private void usingEPCMemory() {
        mWriteCommand.setSelectOffset(0x20);
        mWriteCommand.setSelectLength(0x60);   // Write to any tags matching up to the last bit
        // wrUserCommand.setSelectData("111122223333444455556666");

        mWriteCommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A);
        mWriteCommand.setSelectTarget(SelectTarget.SESSION_2);

        mWriteCommand.setQuerySelect(QuerySelect.ALL);
        mWriteCommand.setQuerySession(QuerySession.SESSION_2);
        mWriteCommand.setQueryTarget(QueryTarget.TARGET_B);

        mWriteCommand.setAccessPassword(DEFAULT_PASSWORD);

        mWriteCommand.setBank(Databank.ELECTRONIC_PRODUCT_CODE);
        mWriteCommand.setOffset(0x0);
        mWriteCommand.setData(HexEncoding.stringToBytes(WRITE_DATA_EXPECTED_OK));
        mUserDataLength = mWriteCommand.getData().length / 4;
        mWriteCommand.setLength(mUserDataLength);

        mWriteCommand.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {
            public int transponderCount = 0;

            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                String eaMsg = transponder.getAccessErrorCode() == null ? "" : transponder.getAccessErrorCode().getDescription();
                String ebMsg = transponder.getBackscatterErrorCode() == null ? "" : transponder.getBackscatterErrorCode().getDescription();

                sendMessageNotification(String.format(
                        "E: %s\nEA: %s\nEB: %s\nWW: %d (%s)",
                        transponder.getEpc(),
                        eaMsg,
                        ebMsg,
                        transponder.getWordsWritten(), transponder.getWordsWritten() != mUserDataLength ? "FAILED" : "Succeeded"
                ));
                ++transponderCount;

                if (!moreAvailable) {
                    sendMessageNotification("Transponders seen: " + transponderCount);
                    transponderCount = 0;
                }
            }
        });

        sendMessageNotification("Writing User memory with incorrect passwords...");
        sendMessageNotification(String.format("Data: %s", HexEncoding.bytesToString(mWriteCommand.getData())));
        mCommander.executeCommand(mWriteCommand);
        sendMessageNotification(String.format("Time taken: %.2fs", getTaskExecutionDuration()));


        //
        // Show the result
        //
        sendMessageNotification("Reading User memory...");
        mCommander.executeCommand(mWriteCommand);

    }

    /* TAGFINDER */

    /**
     * @return the delegate for the raw signal strength responses in dBm
     */
    public ISignalStrengthReceivedDelegate getRawSignalDelegate() {
        return mSignalStrengthResponder.getRawSignalStrengthReceivedDelegate();
    }

    /**
     * @param delegate the delegate for the raw signal strength responses in dBm
     */
    public void setRawSignalDelegate(ISignalStrengthReceivedDelegate delegate) {
        mSignalStrengthResponder.setRawSignalStrengthReceivedDelegate(delegate);
    }

    /**
     * @return the delegate for the percentage signal strength responses in range 0 - 100 %
     */
    public ISignalStrengthReceivedDelegate getPercentageSignalDelegate() {
        return mSignalStrengthResponder.getPercentageSignalStrengthReceivedDelegate();
    }

    /**
     * @param delegate the delegate for the percentage signal strength responses in range 0 - 100 %
     */
    public void setPercentageSignalDelegate(ISignalStrengthReceivedDelegate delegate) {
        mSignalStrengthResponder.setPercentageSignalStrengthReceivedDelegate(delegate);
    }

    /**
     * @return true if the current Reader supports the .ft (Find Tag) command
     */
    public boolean isFindTagCommandAvailable() {
        return mUseFindTagCommand;
    }

    public void updateTargetParameters() {
        if (getCommander().isConnected()) {
            // Configure the switch actions
            SwitchActionCommand switchActionCommand = SwitchActionCommand.synchronousCommand();
            switchActionCommand.setResetParameters(TriState.YES);
            switchActionCommand.setAsynchronousReportingEnabled(TriState.YES);

            // Only change defaults if there is a valid target tag
            if (!StringHelper.isNullOrEmpty(mTargetTagEpc)) {
                // Configure the single press switch action for the appropriate command
                switchActionCommand.setSinglePressAction(mUseFindTagCommand ? SwitchAction.FIND_TAG : SwitchAction.INVENTORY);
                // Lower the repeat delay to maximise the response rate
                switchActionCommand.setSinglePressRepeatDelay(10);
            }

            mCommander.executeCommand(switchActionCommand);


            // Now adjust the commands to target the chosen tag
            boolean succeeded = false;

            if (mUseFindTagCommand) {
                mFindTagCommand = FindTagCommand.synchronousCommand();
                mFindTagCommand.setResetParameters(TriState.YES);

                // Only configure if target valid
                if (!StringHelper.isNullOrEmpty(mTargetTagEpc) && mEnabled) {
                    mFindTagCommand.setSelectData(mTargetTagEpc);
                    mFindTagCommand.setSelectLength(mTargetTagEpc.length() * 4);
                    mFindTagCommand.setSelectOffset(0x20);

                }
//                else
//                {
//                    mFindTagCommand.setTriggerOverride(StringHelper.isNullOrEmpty(mTargetTagEpc) ? StartStop.STOP : StartStop.NOT_SPECIFIED);
//                }

                mFindTagCommand.setTakeNoAction(TriState.YES);

                //mFindTagCommand.setReadParameters(TriState.YES);
                getCommander().executeCommand(mFindTagCommand);

                succeeded = mFindTagCommand.isSuccessful();

            } else {
                // Configure the inventory
                mInventoryCommand = InventoryCommand.synchronousCommand();
                mInventoryCommand.setResetParameters(TriState.YES);
                mInventoryCommand.setTakeNoAction(TriState.YES);

                // Only configure if target valid
                if (!StringHelper.isNullOrEmpty(mTargetTagEpc) && mEnabled) {
                    mInventoryCommand.setIncludeTransponderRssi(TriState.YES);

                    mInventoryCommand.setQuerySession(QuerySession.SESSION_0);
                    mInventoryCommand.setQueryTarget(QueryTarget.TARGET_B);

                    mInventoryCommand.setInventoryOnly(TriState.NO);

                    mInventoryCommand.setSelectData(mTargetTagEpc);
                    mInventoryCommand.setSelectOffset(0x20);
                    mInventoryCommand.setSelectLength(mTargetTagEpc.length() * 4);
                    mInventoryCommand.setSelectAction(SelectAction.DEASSERT_SET_B_NOT_ASSERT_SET_A);
                    mInventoryCommand.setSelectTarget(SelectTarget.SESSION_0);

                    mInventoryCommand.setUseAlert(TriState.NO);
                }

                getCommander().executeCommand(mInventoryCommand);
                succeeded = mInventoryCommand.isSuccessful();
            }

            if (succeeded) {
                sendMessageNotification("updated");
            } else {
                sendMessageNotification("!!! update failed - ensure only hex characters used !!!\n");
            }
        }
    }

    public void updateTarget() {
        if (!this.isBusy()) {
            try {
                sendMessageNotification("Updating target...");

                performTask(new Runnable() {
                    @Override
                    public void run() {
                        updateTargetParameters();
                    }
                });

            } catch (ModelException e) {
                sendMessageNotification("Unable to perform action: " + e.getMessage());
            }
        }
    }

    public String getTargetTagEpc() {
        return mTargetTagEpc;
    }

    public void setTargetTagEpc(String targetTagEpc) {
        if (targetTagEpc != null) {
            mTargetTagEpc = targetTagEpc.toUpperCase();
        }
    }

    private void reportErrors(AsciiSelfResponderCommandBase command) {
        if (!command.isSuccessful()) {
            sendMessageNotification(String.format(
                    "%s failed! Error code: %s", command.getClass().getSimpleName(), command.getErrorCode()));
            for (String message : command.getMessages()) {
                sendMessageNotification(message);
            }
        }

    }
}
