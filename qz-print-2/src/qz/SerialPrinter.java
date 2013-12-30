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

import java.applet.Applet;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import javax.print.PrintException;
import javax.print.PrintService;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;

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
    private final Applet applet;
    private final BrowserTools btools;
    private final boolean ready;

    public SerialPrinter(Applet applet) {
        //port = new SerialPort(portName);
        this.baudRate = SerialPort.BAUDRATE_9600;
        this.dataBits = SerialPort.DATABITS_8;
        this.stopBits = SerialPort.STOPBITS_1;
        this.flowControl = SerialPort.FLOWCONTROL_NONE;
        this.parity = SerialPort.PARITY_NONE;
        this.applet = applet;
        this.btools = new BrowserTools(applet);
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

    public boolean openPort(String portName) {
        if (port == null) {
            port = new SerialPort(this.portName = portName);
            
            // Use a privileged action to open the port
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    try {
                        port.openPort();
                    } catch (SerialPortException ex) {
                        port = null;
                        LogIt.log(ex);
                    }
                    return null;
                }
            });
            
            // Add a listener to the port to check for incoming data
            try {
                port.addEventListener(new SerialPortEventListener() {
                    public void serialEvent(SerialPortEvent spe) {
                        serialEventListener(spe);
                    }
                });
            } catch (SerialPortException ex) {
                LogIt.log(ex);
            }
            
            this.portName = portName;
            LogIt.log("Opened Serial Port " + this.portName);
        } else {
            LogIt.log(Level.WARNING, "Serial Port [" + this.portName + "] already appears to be open.");
        }
        this.btools.notifyBrowser("qzDoneOpeningPort", portName);
        return port.isOpened();
    }

    public boolean closePort(String portName) {
        if (port == null || !port.isOpened()) {
            LogIt.log(Level.WARNING, "Serial Port [" + portName + "] does not appear to be open.");
            return false;
        }
        
        boolean closed = false;
        try {
            closed = port.closePort();
        } catch (SerialPortException ex) {
            LogIt.log(ex);
        }
        
        if (!closed) {
            LogIt.log(Level.WARNING, "Serial Port [" + portName + "] was not closed properly.");
        } else {
            LogIt.log("Port [" + portName + "] closed successfully.");
        }
        btools.notifyBrowser("qzDoneClosingPort", portName);
        port = null;
        this.portName = null;
        return closed;
    }

    public void setSerialBegin(ByteArrayBuilder serialBegin) {
        this.begin = serialBegin.getByteArray();
    }

    public void setSerialEnd(ByteArrayBuilder serialEnd) {
        this.end = serialEnd.getByteArray();
    }

    public void setSerialProperties(String baud, String dataBits, String stopBits, String parity, String flowControl) {
        this.baudRate = SerialUtilities.parseBaudRate(baud);
        this.dataBits = SerialUtilities.parseDataBits(dataBits);
        this.stopBits = SerialUtilities.parseStopBits(stopBits);
        this.parity = SerialUtilities.parseParity(parity);
        this.flowControl = SerialUtilities.parseFlowControl(flowControl);
    }

    // TODO: Finish this function and figure out how to get serialData set so that
    // it sends properly.
    public void send(String serialData) {
        if(port != null) {
            inputBuffer = getInputBuffer();
            inputBuffer.append(serialData.getBytes());
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    try {
                        port.setParams(baudRate, dataBits, stopBits, parity);
                        port.setFlowControlMode(flowControl);
                        LogIt.log("Sending data to [" + portName + "]:\r\n\r\n" + new String(getInputBuffer().getByteArray()) + "\r\n\r\n");
                        port.writeBytes(getInputBuffer().getByteArray());
                        getInputBuffer().clear();
                    } catch (SerialPortException ex) {
                        LogIt.log(ex);
                    }
                    return null;
                }
            });
        }
        else {
            LogIt.log("Error. Serial port not opened.");
        }
    }

    public String getReturnData() {
        if(output != null) {
            String returnData = new String(output);
            output = null;
            return returnData;
        }
        else {
            return null;
        }
    }
    
    /**
     * Timeout in milliseconds for the port.readBytes() function.
     * Default is 1200 (1.2 seconds)
     * @param timeout 
     */
    private void setTimeout(int timeout) {
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

    public void serialEventListener(SerialPortEvent event) {
        try {
            // Receive data
            if (event.isRXCHAR()) {
                getOutputBuffer().append(port.readBytes(event.getEventValue(), timeout));
                
                int[] beginPos = ByteUtilities.indicesOfSublist(getOutputBuffer().getByteArray(), begin);
                int[] endPos = ByteUtilities.indicesOfSublist(getOutputBuffer().getByteArray(), end);
                if (beginPos.length > 0 && endPos.length > 0) {
                    int _begin = beginPos[beginPos.length -1];
                    int _end  = endPos[endPos.length -1];
                    // TODO:  Use specified charset in PrintApplet.
                    LogIt.log(new String(getOutputBuffer().getByteArray(), _begin, _end - _begin));
                    output = new byte[_end - _begin];
                    System.arraycopy(getOutputBuffer().getByteArray(), _begin, output, 0, _end - _begin);
                    getOutputBuffer().clear();
                }
                
                if(output != null) {
                    LogIt.log("Received Serial Data: " + new String(output));
                    btools.notifyBrowser("qzSerialReturned", new String(output));
                }
                else {
                    LogIt.log("Received serial data but it was null. Please check the begin and end characters.");
                }
            }
        } catch (SerialPortException e) {
            LogIt.log(Level.SEVERE, "Exception occured while reading data from port.", e);
        } catch (SerialPortTimeoutException e) {
            LogIt.log(Level.WARNING, "Timeout occured waiting for port to respond.  Timeout value: " + timeout, e);
        }
        
    }
    
    public ByteArrayBuilder getInputBuffer() {
        if (this.inputBuffer == null) {
            this.inputBuffer = new ByteArrayBuilder();
        }
        return this.inputBuffer;
    }
    
    private ByteArrayBuilder getOutputBuffer() {
        if (this.outputBuffer == null) {
            this.outputBuffer = new ByteArrayBuilder();
        }
        return this.outputBuffer;
    }
}
