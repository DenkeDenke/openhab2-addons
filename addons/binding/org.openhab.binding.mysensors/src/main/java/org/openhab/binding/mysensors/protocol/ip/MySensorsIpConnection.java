package org.openhab.binding.mysensors.protocol.ip;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.openhab.binding.mysensors.internal.MySensorsBridgeConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsIpConnection extends MySensorsBridgeConnection {

    private Logger logger = LoggerFactory.getLogger(MySensorsIpConnection.class);

    private String ipAddress = "";
    private int tcpPort = 0;
    public int sendDelay = 0;

    private Socket sock = null;

    private MySensorsIpWriter mysConWriter = null;
    private MySensorIpReader mysConReader = null;

    public MySensorsIpConnection(String ipAddress, int tcpPort, int sendDelay) {
        super();
        this.ipAddress = ipAddress;
        this.tcpPort = tcpPort;
        this.sendDelay = sendDelay;
    }

    public MySensorsIpConnection(String ipAddress, int tcpPort) {
        super();
        this.ipAddress = ipAddress;
        this.tcpPort = tcpPort;
    }

    @Override
    public boolean connect() {
        logger.debug("Connecting to bridge ...");

        try {
            sock = new Socket(ipAddress, tcpPort);
            mysConReader = new MySensorIpReader(sock.getInputStream(), this);
            mysConWriter = new MySensorsIpWriter(sock, this, sendDelay);

            mysConReader.startReader();
            mysConWriter.startWriter();

            connected = true;
        } catch (UnknownHostException e) {
            logger.error("Error while trying to connect to: " + ipAddress + ":" + tcpPort);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Error while trying to connect InputStreamReader");
            e.printStackTrace();
        }

        return connected;
    }

    @Override
    public void disconnect() {

        if (mysConWriter != null) {
            mysConWriter.stopWriting();
        }

        mysConReader.stopReader();

        // Shut down socket
        try {
            if (sock != null && sock.isConnected()) {
                sock.close();
            }
        } catch (IOException e) {
            logger.error("cannot disconnect from socket, message: {}", e.getMessage());
        }

    }
}
