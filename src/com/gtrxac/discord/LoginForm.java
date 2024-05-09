package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class LoginForm extends Form implements CommandListener {
    State s;
    private RecordStore loginRms;

    private TextField apiField;
    private TextField gatewayField;
    private TextField tokenField;
    private Command nextCommand;

    public LoginForm(State s) {
        super("Log in");
        setCommandListener(this); 
        this.s = s;

        String initialApi = "http://dsc.uwmpr.online";
        String initialGateway = "socket://uwmpr.online:8080";
        String initialToken = "";
        
        if (RecordStore.listRecordStores() != null) {
            try {
                loginRms = RecordStore.openRecordStore("login", true);

                if (loginRms.getNumRecords() > 0) {
                    String savedApi = new String(loginRms.getRecord(1));
                    if (savedApi.length() > 0) initialApi = savedApi;
                    initialToken = new String(loginRms.getRecord(2));
                }
                if (loginRms.getNumRecords() >= 3) {
                    s.theme = loginRms.getRecord(3)[0];
                }
                if (loginRms.getNumRecords() >= 4) {
                    s.oldUI = loginRms.getRecord(4)[0] != 0;
                }
                if (loginRms.getNumRecords() >= 5) {
                    String savedGateway = new String(loginRms.getRecord(5));
                    if (savedGateway.length() > 0) initialGateway = savedGateway;
                }
                if (loginRms.getNumRecords() >= 7) {
                    s.authorFontSize = loginRms.getRecord(6)[0];
                    s.messageFontSize = loginRms.getRecord(7)[0];
                }
                if (loginRms.getNumRecords() >= 8) {
                    s.use12hTime = loginRms.getRecord(8)[0] != 0;
                }
                if (loginRms.getNumRecords() >= 9) {
                    s.messageLoadCount = loginRms.getRecord(9)[0];
                } else {
                    s.messageLoadCount = 20;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (loginRms != null) loginRms.closeRecordStore();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        apiField = new TextField("API URL", initialApi, 200, 0);
        gatewayField = new TextField("Gateway URL", initialGateway, 200, 0);
        tokenField = new TextField("Token", initialToken, 200, TextField.NON_PREDICTIVE);
        nextCommand = new Command("Log in", Command.OK, 0);

        append(new StringItem(null, "Only use proxies that you trust!"));
        append(apiField);
        append(gatewayField);
        append(new StringItem(null, "The token can be found from your browser's dev tools (look online for help). Using an alt account is recommended."));
        append(tokenField);
        addCommand(nextCommand);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == nextCommand) {
            String api = apiField.getString();
            String gateway = gatewayField.getString();
            String token = tokenField.getString();
            
            try {
                loginRms = RecordStore.openRecordStore("login", true);
                if (loginRms.getNumRecords() > 0) {
                    loginRms.setRecord(1, api.getBytes(), 0, api.length());    
                    loginRms.setRecord(2, token.getBytes(), 0, token.length());    
                } else {
                    loginRms.addRecord(api.getBytes(), 0, api.length());
                    loginRms.addRecord(token.getBytes(), 0, token.length());
                }
                if (loginRms.getNumRecords() < 4) {
                    byte[] zeroByte = {0};
                    loginRms.addRecord(zeroByte, 0, 1);
                    loginRms.addRecord(zeroByte, 0, 1);
                }
                if (loginRms.getNumRecords() >= 5) {
                    loginRms.setRecord(5, gateway.getBytes(), 0, gateway.length());
                } else {
                    loginRms.addRecord(gateway.getBytes(), 0, gateway.length());
                }
                loginRms.closeRecordStore();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            s.loadFonts();
            s.http = new HTTPThing(api, token);
            s.openGuildSelector(true);
        }
    }
}
