/**
 * @author Tres Finocchiaro
 * 
 * Copyright (C) 2013 Tres Finocchiaro, QZ Industries
 *
 * IMPORTANT:  This software is dual-licensed
 * 
 * LGPL 2.1
 * This is free software.  This software and source code are released under 
 * the "LGPL 2.1 License".  A copy of this license should be distributed with 
 * this software. http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * QZ INDUSTRIES SOURCE CODE LICENSE
 * This software and source code *may* instead be distributed under the 
 * "QZ Industries Source Code License", available by request ONLY.  If source 
 * code for this project is to be made proprietary for an individual and/or a
 * commercial entity, written permission via a copy of the "QZ Industries Source
 * Code License" must be obtained first.  If you've obtained a copy of the 
 * proprietary license, the terms and conditions of the license apply only to 
 * the licensee identified in the agreement.  Only THEN may the LGPL 2.1 license
 * be voided.
 * 
 */

package qz;

import java.applet.Applet;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.logging.Level;
import qz.json.JSONArray;
import qz.reflection.ReflectException;

/**
 * The PrintApplet is the main component of the Applet
 * It will manage all communication between the script listener and print 
 * spooler components.
 * 
 * @author Thomas Hart
 */
public class PrintApplet extends Applet {
    
    public static final String VERSION = "2.0.0";
    
    private BrowserTools btools;
    private PrintSpooler spooler;
    private Charset charset;
    
    @Override
    public void start() {
        
        super.start();
        
        LogIt.log("Applet Started");
        
        btools = new BrowserTools(this);
        spooler = new PrintSpooler();
        spooler.setApplet(this);
        
        new Thread(spooler).start();
        
        charset = Charset.defaultCharset();
        
        btools.notifyBrowser("qzReady");
        
    }
    
    /* Javascript methods that the browser can call */
    
    /* Testing Functions */
    
    public void cancelJob(int jobIndex) {
        spooler.cancelJob(jobIndex);
    }
    
    public String getQueueInfo() {
        try {
            JSONArray queueInfo = spooler.getQueueInfo();
            return queueInfo.toString();
        }
        catch (NullPointerException ex) {
            LogIt.log(ex);
            return "";
        }
    }
    
    public String getJobInfo(int jobIndex) {
        return spooler.getJobInfo(jobIndex);
    }
    
    /*
        Functions needed:
        
        findPrinter
        getPrinter
        getPrinters
        setPrinter
        append
        append64
        appendImage
        appendHex
        appendXML
        appendPDF
        print
        setAutoSize
        setOrientation
        setCopies
        setPaperSize
        setEndOfDocument
        setDocumentsPerSpool
        isDoneAppending
        isDoneFinding
        getVersion
        getException
        getNetworkUtilities
        findNetworkAdapters
        findNetworkInfo
        getMac
        getIP
        setHostname
        setPort
        getAllowMultipleInstances
        clearException
        findPorts
        openPort
        closePort
        getLogPostScriptFeatures
        isAlternatePrinting
        useAlternatePrinting
        setSerialBegin
        setSerialEnd
        setSerialProperties
        send
    
    */

    
    // Javascript functions for spooler actions
    
    public void findPrinter() {
        findPrinter(null);
    }
    
    public void findPrinter(String printerName) {
        spooler.findPrinter(printerName);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneFinding");
    }
    
    public String getPrinter() {
        return spooler.getPrinter();
    }
    
    public String getPrinters() {
        
        try {
            String printerListString = spooler.getPrinters();
            return printerListString;
        }
        catch(NullPointerException ex) {
            LogIt.log(ex);
            return "";
        }
        
    }
    public void setPrinter(int printerIndex) {
        spooler.setPrinter(printerIndex);
    }
    
    // Backwards compatability function. Finding printers is done instantly now,
    // so it's always "done finding"
    public boolean isDoneFinding() {
        return true;
    }
    
    /**
     * Appends String <var>data</var> to the spool after converting to a byte 
     * array, which will add it to the current job or start a new one.
     * @param data 
     */
    public void append(String data) {
        ByteArrayBuilder bytes = new ByteArrayBuilder();
        try {
            bytes.append(data, charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.append(bytes, charset);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneAppending");
    }
    
    /**
     * Converts String base64 into a byte array then appends the array to the
     * spool, which will add it to the current job or start a new one
     * @param base64 
     */
    public void append64(String base64) {
        
        byte[] base64Array = null;
        try {
            base64Array = Base64.decode(base64);
        } catch (IOException ex) {
            LogIt.log(ex);
        }
        
        ByteArrayBuilder data = new ByteArrayBuilder(base64Array);
        spooler.append(data, charset);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneAppending");
    }
    
    /**
     * Converts Hex String data into a byte array then appends the array to the
     * spool, which will add it to the current job or start a new one
     * @param hexString 
     */
    // TODO: This function, when echoed back with getJobInfo testing function,
    //       seems to have extra newlines between each line. Test with hardware
    //       or original version to see if this is normal.
    public void appendHex(String hexString) {
        
        byte[] bytes = ByteUtilities.hexStringToByteArray(hexString);
        ByteArrayBuilder data = new ByteArrayBuilder(bytes);
        spooler.append(data, charset);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneAppending");
    }
    
    //Stub appendImage function
    // TODO: Implement appendImage
    public void appendImage(String imagePath, String lang) {
        
        ByteArrayBuilder bytes = new ByteArrayBuilder();
        try {
            bytes.append(imagePath, charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.appendImage(bytes, charset, lang, 0, 0);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneAppending");
    }
    
    public void appendImage(String imagePath, String lang, int imageX, int imageY) {
        
        ByteArrayBuilder bytes = new ByteArrayBuilder();
        try {
            bytes.append(imagePath, charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.appendImage(bytes, charset, lang, imageX, imageY);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneAppending");
    }
    
    public void appendImage(String imagePath, String lang, String dotDensityString) {
        
        int dotDensity = 32;
        
        if (dotDensityString.equalsIgnoreCase("single")) {
            dotDensity = 32;
        } else if (dotDensityString.equalsIgnoreCase("double")) {
            dotDensity = 33;
        } else if (dotDensityString.equalsIgnoreCase("triple")) {
            dotDensity = 39;
        } else {
            LogIt.log(Level.WARNING, "Cannot translate dotDensity value of '"
                    + dotDensityString + "'.  Using '" + dotDensity + "'.");
        }
        
        appendImage(imagePath, lang, dotDensity);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneAppending");
    }
    
    public void appendImage(String imagePath, String lang, int dotDensity) {
        ByteArrayBuilder bytes = new ByteArrayBuilder();
        try {
            bytes.append(imagePath, charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.appendImage(bytes, charset, lang, dotDensity);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneAppending");
    }
    
    public void appendImage(String url) {
        // if appendImage is called without a lang, it's a postscript job
        ByteArrayBuilder bytes = new ByteArrayBuilder();
        try {
            bytes.append(url, charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.appendPSImage(bytes, charset);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneAppending");
    }
    
    /**
     * Gets the first XML node identified by <code>tagName</code>, reads its
     * contents and appends it to the buffer. Assumes XML content is base64
     * formatted.
     * 
     * @param url
     * @param xmlTag 
     */
    public void appendXML(String url, String xmlTag) {
        ByteArrayBuilder bytes = new ByteArrayBuilder();
        try {
            bytes.append(url, charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.appendXML(bytes, charset, xmlTag);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneAppending");
    }
    
    /**
     * appendFile will read a text file and append the data directly without
     * any translation
     * 
     * @param url 
     */
    public void appendFile(String url) {
        ByteArrayBuilder bytes = new ByteArrayBuilder();
            
        try {
            bytes.append(url, charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.appendFile(bytes, charset);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneAppending");
    }
    
    public void appendHTML(String html) {
        ByteArrayBuilder bytes = new ByteArrayBuilder();
        
        try {
            bytes.append(html, charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.appendHTML(bytes, charset);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneAppending");
    }
    
    public void appendPDF(String url) {
        ByteArrayBuilder bytes = new ByteArrayBuilder();
            
        try {
            bytes.append(url, charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.appendPDF(bytes, charset);
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneAppending");
    }
    
    public boolean print() {
        Boolean success = spooler.print();
        if(success) {
            LogIt.log("Print Successful");
        }
        else {
            LogIt.log("Print Failed");
        }
        btools.notifyBrowser("qzDonePrinting");
        return success;
    }
    
    // Stub function for backwards compatability
    // PrintJobElements will determine their type when printing
    public boolean printPS() {
        return print();
    }
    
    // Stub function for backwards compatability
    // PrintJobElements will determine their type when printing
    public boolean printHTML() {
        return print();
    }
    
    public void printToFile(String filePath) {
        spooler.printToFile(filePath);
    }
    
    public void printToHost(String jobHost, int jobPort) {
        spooler.printToHost(jobHost, jobPort);
    }
    
    public String getVersion() {
        return VERSION;
    }
    
    public Throwable getException() {
        return spooler.getException();
    }

    public void clearException() {
        spooler.clearException();
    }
    
    public void setEncoding(String charset) {
        // Example:  Charset.forName("US-ASCII");
        LogIt.log("Default charset encoding: " + Charset.defaultCharset().name());
        try {
            this.charset = Charset.forName(charset);
            LogIt.log("Current applet charset encoding: " + this.charset.name());
        } catch (IllegalCharsetNameException e) {
            LogIt.log(Level.WARNING, "Could not find specified charset encoding: "
                    + charset + ". Using default.", e);
        }
    }
    
    public void setPaperSize(String width, String height) {
        PaperFormat paperSize = PaperFormat.parseSize(width, height);
        spooler.setPaperSize(paperSize);
        LogIt.log(Level.INFO, "Set paper size to " + paperSize.getWidth()
                + paperSize.getUnitDescription() + "x"
                + paperSize.getHeight() + paperSize.getUnitDescription());
    }

    public void setPaperSize(float width, float height) {
        PaperFormat paperSize = new PaperFormat(width, height);
        spooler.setPaperSize(paperSize);
        LogIt.log(Level.INFO, "Set paper size to " + paperSize.getWidth()
                + paperSize.getUnitDescription() + "x"
                + paperSize.getHeight() + paperSize.getUnitDescription());
    }

    public void setPaperSize(float width, float height, String units) {
        PaperFormat paperSize = PaperFormat.parseSize("" + width, "" + height, units);
        spooler.setPaperSize(paperSize);
        LogIt.log(Level.INFO, "Set paper size to " + paperSize.getWidth()
                + paperSize.getUnitDescription() + "x"
                + paperSize.getHeight() + paperSize.getUnitDescription());
    }
    
    public void setAutoSize(boolean autoSize) {
        spooler.setAutoSize(autoSize);
    }
    
    public boolean getLogPostScriptFeatures() {
        return spooler.getLogPostScriptFeatures();
    }
    
    public void setLogPostScriptFeatures(boolean logPSFeatures) {
        spooler.setLogPostScriptFeatures(logPSFeatures);
    }
    
    public void setEndOfDocument(String endOfDocument) {
        spooler.setEndOfDocument(endOfDocument);
    }
    
    public void setDocumentsPerSpool(int docsPerSpool) {
        spooler.setDocumentsPerSpool(docsPerSpool);
    }
    
    public void findNetworkInfo() {
        spooler.findNetworkInfo();
        // Deprecated callback. Remove in a future version.
        btools.notifyBrowser("qzDoneFindingNetwork");
    }
    
    public String getMac() {
        return spooler.getMac();
    }
    
    public String getIP() {
        return spooler.getIP();
    }
    
    public void useAlternatePrinting() {
        this.useAlternatePrinting(true);
    }

    public void useAlternatePrinting(boolean alternatePrint) {
        spooler.useAlternatePrinting(alternatePrint);
    }
    
    public boolean isAlternatePrinting() {
        return spooler.isAlternatePrinting();
    }

    public void findPorts() {
        spooler.findPorts();
        btools.notifyBrowser("qzDoneFindingPorts");
    }
    
    public String getPorts() {
        return spooler.getPorts();
    }
    
    public void openPort(String portName) {
        spooler.openPort(portName);
    }
    
    public void closePort(String portName) {
        spooler.closePort(portName);
    }
    
    public void setSerialBegin(String serialBegin) {
        ByteArrayBuilder serialBeginBytes = new ByteArrayBuilder(serialBegin.getBytes());
        spooler.setSerialBegin(serialBeginBytes);
    }
    
    public void setSerialEnd(String serialEnd) {
        ByteArrayBuilder serialEndBytes = new ByteArrayBuilder(serialEnd.getBytes());
        spooler.setSerialEnd(serialEndBytes);
    }
    
    public void setSerialProperties(int baud, int dataBits, String stopBits, int parity, String flowControl) {
        setSerialProperties(Integer.toString(baud), Integer.toString(dataBits),
                stopBits, Integer.toString(parity), flowControl);
    }

    public void setSerialProperties(String baud, String dataBits, String stopBits, String parity, String flowControl) {
        spooler.setSerialProperties(baud, dataBits, stopBits, parity, flowControl);
    }
    
    public void send(String portName, String serialData) {
        // portName is only used for display. Get current port name from SerialPrinter
        spooler.sendSerialData(serialData);
    }
    
    public String getReturnData() {
        return spooler.getReturnData();
    }
    
    // Deprecated functions
    // TODO: Properly address deprecating these functions
    
    /**
     * Check if the appending operation is complete.
     * 
     * @return Whether the element is done appending
     * @deprecated This function is no longer needed as appending is instant.
     */
    @Deprecated
    public boolean isDoneAppending() {
        LogIt.log(Level.WARNING, "isDoneAppending() has been deprecated and will be removed in a future version.");
        return true;
    }
    
    /**
     * Check if the printing operation is complete.
     * 
     * @return Whether the job is done printing
     * @deprecated This function is no longer useful in a spooling context.
     * Use {@link getQueueInfo()} to get information about existing jobs.
     */
    @Deprecated
    public boolean isDonePrinting() {
        LogIt.log(Level.WARNING, "isDonePrinting() has been deprecated and will be removed in a future version. Try using getQueueInfo().");
        return true;
    }
    
    /**
     * Check whether multiple instances are currently allowed.
     * 
     * @return The current value of allowMultipleInstances
     * @deprecated This functionality is no longer supported.
     */
    @Deprecated
    public boolean getAllowMultipleInstances() {
        LogIt.log(Level.WARNING, "getAllowMultipleInstances() has been deprecated and will be removed in a future version. This functionality is no longer supported.");
        return false;
    }
    
    /**
     * Set whether multiple instances are currently allowed.
     * 
     * @param newValue The value to be set
     * @deprecated This functionality is no longer supported.
     */
    @Deprecated
    public void allowMultipleInstances(Boolean newValue) {
        LogIt.log(Level.WARNING, "allowMultipleInstances() has been deprecated and will be removed in a future version. This functionality is no longer supported.");
    }
    
}
