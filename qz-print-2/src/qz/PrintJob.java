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

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintService;

/**
 * PrintJob will provide an object to hold an entire job. It should contain the 
 * raw data for the job, the title of the job, and it's current state.
 * 
 * @author Thomas Hart
 */
public class PrintJob implements Runnable {
    
    private PrintJobState state = PrintJobState.STATE_CREATED;
    private String title = "Print Job";
    private ArrayList<PrintJobElement> data = new ArrayList<PrintJobElement>();;
    private Boolean running = true;
    private Integer updateDelay = 100;
    private Printer printer;
    
    public void run() {
        while(running) {
            try {
                Thread.sleep(updateDelay);
            } catch (InterruptedException ex) {
                LogIt.log(ex);
                running = false;
            }
        }
    }
    
    public void cancel() {
        state = PrintJobState.STATE_CANCELLED;
        running = false;
    }
    
    public PrintJobState getJobState() {
        return state;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void append(ByteArrayBuilder appendData, Charset charset) {
        try {
            PrintJobElement pje = new PrintJobElement(this, appendData, "RAW", charset);
            data.add(pje);
        }
        catch(NullPointerException e) {
            LogIt.log(e);
        }
    }
    
    public void appendImage(ByteArrayBuilder imagePath, Charset charset, String lang, Integer imageX, Integer imageY) {
        
        try {
            PrintJobElement pje = new PrintJobElement(this, imagePath, "IMAGE", charset, lang, imageX, imageY);
            data.add(pje);
        }
        catch(NullPointerException e) {
            LogIt.log(e);
        }
    }
    public void appendImage(ByteArrayBuilder imagePath, Charset charset, String lang, Integer dotDensity) {
        
        try {
            PrintJobElement pje = new PrintJobElement(this, imagePath, "IMAGE", charset, lang, dotDensity);
            data.add(pje);
        }
        catch(NullPointerException e) {
            LogIt.log(e);
        }
    }

    public void appendXML(ByteArrayBuilder url, Charset charset, String xmlTag) {
        PrintJobElement pje = new PrintJobElement(this, url, "XML", charset, xmlTag);
        data.add(pje);
    }
    
    public void appendFile(ByteArrayBuilder url, Charset charset) {
        PrintJobElement pje = new PrintJobElement(this, url, "FILE", charset);
        data.add(pje);
    }
    
    public void prepareJob() {
        
        state = PrintJobState.STATE_PROCESSING;
        
        ListIterator dataIterator = data.listIterator();
        
        while(dataIterator.hasNext()) {
            try {
                PrintJobElement pje = (PrintJobElement) dataIterator.next();
                pje.prepare();
            } catch (IOException ex) {
                LogIt.log(ex);
            }
        }

        state = PrintJobState.STATE_PROCESSED;
        
    }
    
    public void queue() {
        state = PrintJobState.STATE_QUEUED;
    }
    
    // Returns a string with the contents of the job data
    // TODO: This is a debugging/testing function. Not needed for release
    public String getInfo() {
        
        String jobInfo = "";
        
        ListIterator dataIterator = data.listIterator();
        
        while(dataIterator.hasNext()) {
            PrintJobElement pje = (PrintJobElement) dataIterator.next();
            ByteArrayBuilder bytes = pje.getData();
            String info;
            try {
                info = new String(bytes.getByteArray(), pje.getCharset().name());
                jobInfo += info;
            } catch (UnsupportedEncodingException ex) {
                LogIt.log(ex);
            }
        }
        
        return jobInfo;
        
    }
    
    // TODO: Stub for now.
    public void print() {
        state = PrintJobState.STATE_SENDING;
        
        ByteArrayBuilder jobData = new ByteArrayBuilder();
        
        // Concatenate all the PrintJobElements into one ByteArrayBuilder
        ListIterator dataIterator = data.listIterator();
        
        while(dataIterator.hasNext()) {
            PrintJobElement pje = (PrintJobElement) dataIterator.next();
            ByteArrayBuilder bytes = pje.getData();
            jobData.append(bytes.getByteArray());
        }
        
        
        
        if(printer.getType().equals("FILE")) {
            try {
                printer.print(jobData);
            }
            catch(PrinterException ex) {
                LogIt.log(ex);
            }
        }
        else if(printer.getType().equals("DEBUG")) {
            try {
                printer.print(jobData);
            }
            catch(PrinterException ex) {
                LogIt.log(ex);
            }
        }
        else {
            PrintService ps = printer.getPrintService();
            PrinterJob pj = PrinterJob.getPrinterJob();
            try {
                pj.setPrintService(ps);
            } catch (PrinterException ex) {
                LogIt.log(ex);
            }
            
            // TODO: Finish implementing this function for print jobs that aren't "to file"
        }
        
        state = PrintJobState.STATE_COMPLETE;
    }
    
    public void setPrinter(Printer printer) {
        this.printer = printer;
    }
    
    public Printer getPrinter() {
        return printer;
    }

 }