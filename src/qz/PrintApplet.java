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
import java.util.logging.Logger;

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
    
    public void cancelJob(Integer jobIndex) {
        spooler.cancelJob(jobIndex);
    }
    
    public String getQueueInfo() {
        return spooler.getQueueInfo();
    }
    
    public String getJobInfo(Integer jobIndex) {
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
    public void append(String data) {
        ByteArrayBuilder bytes = new ByteArrayBuilder();
        try {
            bytes.append(data, charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.append(bytes, charset);
    }
    
    public void append64(final String base64) {
        
        byte[] base64Array = null;
        try {
            base64Array = Base64.decode(base64);
        } catch (IOException ex) {
            LogIt.log(ex);
        }
        
        ByteArrayBuilder data = new ByteArrayBuilder(base64Array);
        spooler.append(data, charset);
    }
    
    
    
    //Stub appendImage function
    // TODO: Implement appendImage
    public void appendImage(String imagePath, String format, Integer width, Integer height) {
        
        ByteArrayBuilder bytes = new ByteArrayBuilder();
        try {
            bytes.append("[IMAGEDATA]\n", charset);
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        }
        spooler.append(bytes, charset);
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
