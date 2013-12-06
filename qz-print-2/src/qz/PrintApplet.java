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
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.logging.Level;

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
    private Exception currentException;
    private Charset charset;
    private PaperFormat paperSize;
    
    @Override
    public void start() {
        
        currentException = null;
        
        super.start();
        
        LogIt.log("Applet Started");
        
        btools = new BrowserTools(this);
        spooler = new PrintSpooler();
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
        return spooler.getQueueInfo();
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
    }
    
    public void appendImage(String imagePath, String lang, int imageX, int imageY) {
        
        ByteArrayBuilder bytes = new ByteArrayBuilder();
        try {
            bytes.append(imagePath, charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.appendImage(bytes, charset, lang, imageX, imageY);
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
    }
    
    public void appendImage(String imagePath, String lang, int dotDensity) {
        ByteArrayBuilder bytes = new ByteArrayBuilder();
        try {
            bytes.append(imagePath, charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.appendImage(bytes, charset, lang, dotDensity);
    }
    
    public void appendImage(String url) {
        // if appendImage is called without a lang, it's a postscript job
        spooler.appendPSImage(url);
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
    }
    
    public boolean print() {
        if(spooler.print()) {
            LogIt.log("Print Successful");
            return true;
        }
        else {
            LogIt.log("Print Failed");
            return false;
        }
    }
    
    // Stub function for backwards compatability
    // PrintJob will determine if it's a raw or ps job when data is being added
    public boolean printPS() {
        return print();
    }
    
    public void printToFile(String filePath) {
        spooler.printToFile(filePath);
    }
    
    public String getVersion() {
        return VERSION;
    }
    
    // TODO: Implement get Exception
    public Exception getException() {
        return currentException;
    }
    public void clearException() {
        currentException = null;
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
        this.paperSize = PaperFormat.parseSize(width, height);
        spooler.setPaperSize(paperSize);
        LogIt.log(Level.INFO, "Set paper size to " + paperSize.getWidth()
                + paperSize.getUnitDescription() + "x"
                + paperSize.getHeight() + paperSize.getUnitDescription());
    }

    public void setPaperSize(float width, float height) {
        this.paperSize = new PaperFormat(width, height);
        spooler.setPaperSize(paperSize);
        LogIt.log(Level.INFO, "Set paper size to " + paperSize.getWidth()
                + paperSize.getUnitDescription() + "x"
                + paperSize.getHeight() + paperSize.getUnitDescription());
    }

    public void setPaperSize(float width, float height, String units) {
        this.paperSize = PaperFormat.parseSize("" + width, "" + height, units);
        spooler.setPaperSize(paperSize);
        LogIt.log(Level.INFO, "Set paper size to " + paperSize.getWidth()
                + paperSize.getUnitDescription() + "x"
                + paperSize.getHeight() + paperSize.getUnitDescription());
    }
    
    public void setAutoSize(boolean autoSize) {
        spooler.setAutoSize(autoSize);
    }
    
    // Deprecated functions
    // TODO: Properly address deprecating these functions
    
    // isDone functions no longer apply to a spool based system
    // Legacy javascript may rely on these. For now just return true;
    public boolean isDoneAppending() {
        return true;
    }
    public boolean isDonePrinting() {
        return true;
    }
    
}
