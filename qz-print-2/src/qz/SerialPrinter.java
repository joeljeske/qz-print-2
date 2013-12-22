/**
 * @author Tres Finocchiaro
 *
 * Copyright (C) 2013 Tres Finocchiaro, QZ Industries
 *
 * IMPORTANT: This software is dual-licensed
 *
 * LGPL 2.1 This is free software. This software and source code are released
 * under the "LGPL 2.1 License". A copy of this license should be distributed
 * with this software. http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * QZ INDUSTRIES SOURCE CODE LICENSE This software and source code *may* instead
 * be distributed under the "QZ Industries Source Code License", available by
 * request ONLY. If source code for this project is to be made proprietary for
 * an individual and/or a commercial entity, written permission via a copy of
 * the "QZ Industries Source Code License" must be obtained first. If you've
 * obtained a copy of the proprietary license, the terms and conditions of the
 * license apply only to the licensee identified in the agreement. Only THEN may
 * the LGPL 2.1 license be voided.
 *
 */
package qz;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.print.PrintException;
import javax.print.PrintService;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 *
 * @author Thomas Hart II
 */
public class SerialPrinter implements Printer {

    // Serial port attributes obtained from the system
    private int baudRate;
    private int dataBits;
    private int stopBits;
    private int flowControl;
    private int parity;
    
    // Beginning and ending patterns that signify port has responded
    private byte[] begin = { '\u0002' };
    private byte[] end = { '\r' };
    
    // Timeout to wait before giving up on reading the specified amount of bytes
    private int timeout;
    
    // A buffer to hold data returned from the serial port
    private ByteArrayBuilder outputBuffer;
    
    // The informaiton to be sent to the serial port
    private ByteArrayBuilder inputBuffer;
    
    private byte[] output;
    
    private SerialPort port;
    private String serialPorts;
    private String[] portArray;
    private String portName;
    
    private boolean ready;

    public SerialPrinter() {
        //port = new SerialPort(portName);
        this.baudRate = SerialPort.BAUDRATE_9600;
        this.dataBits = SerialPort.DATABITS_8;
        this.stopBits = SerialPort.STOPBITS_1;
        this.flowControl = SerialPort.FLOWCONTROL_NONE;
        this.parity = SerialPort.PARITY_NONE;
        this.ready = true;
        setTimeout(1200);
    }
    
    public String getName() {
        return "Serial Printer";
    }

    public void printRaw(ByteArrayBuilder data) throws PrintException {
        LogIt.log("Serial Printer does not support raw printing.");
    }

    public void printAlternate(ByteArrayBuilder data) throws PrintException {
        LogIt.log("Serial Printer does not support alternate printing.");
    }

    public boolean ready() {
        return ready;
    }

    public void setPrintService(PrintService ps) {
        LogIt.log("Serial Printer does not require a print service.");
    }

    public PrintService getPrintService() {
        LogIt.log("Serial Printer does not require a print service.");
        return null;
    }

    public String getType() {
        return "Serial";
    }

    public void setName(String name) {
        
    }

    public void setJobTitle(String jobTitle) {
        
    }

    public void findPorts() {
        
        LogIt.log("Serial Printer now finding ports.");
        
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                fetchPortList();
                return null;
            }
        });
 
    }

    public String getPorts() {
        return serialPorts;
    }

    public void openPort(String portName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void closePort(String portName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setSerialBegin(char serialBegin) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setSerialEnd(char serialEnd) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setSerialProperties(String baud, String dataBits, String stopBits, String parity, String flowControl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void send(String serialData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getReturnData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Timeout in milliseconds for the port.readBytes() function.
     * Default is 1200 (1.2 seconds)
     * @param timeout 
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public void fetchPortList() {
        try {
            StringBuilder sb = new StringBuilder();
            portArray = SerialPortList.getPortNames();
            for (int i = 0; i < portArray.length; i++) {
                sb.append(portArray[i]).append(i < portArray.length - 1 ? "," : "");
            }
            serialPorts = sb.toString();
            LogIt.log("Found Serial Ports: " + serialPorts);
        }
        catch (NullPointerException ex) {
            LogIt.log("NullPointerException: " + ex);
        }
        catch (NoClassDefFoundError ex) {
            LogIt.log("NoClassDefFoundError: " + ex);
        }
        
    }
}
