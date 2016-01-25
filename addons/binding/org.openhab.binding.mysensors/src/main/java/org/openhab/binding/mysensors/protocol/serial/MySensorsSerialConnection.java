package org.openhab.binding.mysensors.protocol.serial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import gnu.io.NRSerialPort;

import org.openhab.binding.mysensors.handler.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.MySensorsMessage;
import org.openhab.binding.mysensors.internal.MySensorsMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tim Oberföll
 *
 *	Connection to the serial interface where the MySensors Gateway is conncted
 */
public class MySensorsSerialConnection extends MySensorsBridgeConnection{
	
	private Logger logger = LoggerFactory.getLogger(MySensorsSerialConnection.class);
	
	private String serialPort = "";
	private int baudRate = 115200;
	public int sendDelay = 0;

	
	private NRSerialPort serialConnection = null;
	
	private MySensorsSerialWriter mysConWriter = null;
	
	
	public MySensorsSerialConnection(String serialPort, int baudRate, int sendDelay) {
		super();
		
		this.serialPort = serialPort;
		this.baudRate = baudRate;
		this.sendDelay = sendDelay;
		
		serialConnection = new NRSerialPort(serialPort, baudRate);
		if(serialConnection.connect()) {
			connected = true;
			logger.debug("Successfully connected to serial port.");
			mysConWriter = new MySensorsSerialWriter(serialConnection, this, sendDelay);
		} else {
			logger.error("Can't connect to serial port. Wrong port?");
		}
		
		
		
	}
	
	
	
	
	/** 
	 * Thread that holds the serial connection and listens to messages 
	 * send from the MySensors network via serial to the controller
	 */
	public void run() {
		
		mysConWriter.start();
		
		DataInputStream ins = new DataInputStream(serialConnection.getInputStream());
		
		BufferedReader buffRead = new BufferedReader(new InputStreamReader(ins));
		
		while(!stopReader) {
			try {
				
				// Is there something to read?
				
				String line = buffRead.readLine();
				if(line != null) {
					logger.debug(line);
					MySensorsMessage msg = MySensorsMessageParser.parse(line);
					if(msg != null) {
						MySensorsStatusUpdateEvent event = new MySensorsStatusUpdateEvent(msg);
						for (MySensorsUpdateListener mySensorsEventListener : updateListeners) {
							mySensorsEventListener.statusUpdateReceived(event);
						}
					}
				}
			} catch (IOException e) {
				// Ignore this, there is nothing to read
			}
		}
		
		mysConWriter.stopWriting();
		logger.debug("Shutting down serial connection!");
	}

}