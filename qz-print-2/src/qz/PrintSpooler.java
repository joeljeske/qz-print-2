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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;

/**
 * The PrintSpooler will maintain a list of all print jobs and their status.
 * It will also initiate threaded processes for preparing print jobs and sending
 * them to the printer with the appropriate interface.
 * 
 * @author Thomas Hart
 */
public class PrintSpooler implements Runnable {
    
    public boolean running;
    public int loopDelay;
    public PrintJob currentJob;
    public Thread currentJobThread;
    
    private String queueInfo = "";
    private ArrayList<PrintJob> spool = new ArrayList<PrintJob>();
    private ListIterator<PrintJob> spoolIterator;
    private Printer currentPrinter;
    private ArrayList<Printer> printerList;
    private String printerListString;
    private FilePrinter filePrinter;
    private PaperFormat paperSize;
    private boolean autoSize;
    
    public void PrintSpooler() {
        
    }
    
    // The run loop will consistently check the spool List and call functions
    // based on the state of each PrintJob
    public void run() {
        
        LogIt.log("PrintSpooler started");
        
        // Get the list of all installed printers
        printerList = new ArrayList<Printer>();
        printerListString = "";
        findAllPrinters();
        
        // Initialize system variables
        running = true;
        filePrinter = new FilePrinter();
        
        // Configurable variables
        loopDelay = 1000;
    
        // TODO: Get Default Printer
        currentPrinter = null;
        
        // Main loop - run every loopDelay milliseconds
        while(running) {
            try {
                
                queueInfo = "";
                spoolIterator = spool.listIterator();
                
                if(spool.size() > 0) {
                    
                    while(spoolIterator.hasNext()) {

                        int jobIndex = spoolIterator.nextIndex();
                        PrintJob job = spoolIterator.next();
                        PrintJobState jobState = job.getJobState();

                        switch(jobState) {
                            case STATE_PROCESSED:
                                job.queue();
                                break;
                            case STATE_QUEUED:
                                // Get Printer Status from the job
                                if(job.getPrinter().ready()) {
                                    job.print();
                                }
                                break;
                            default:
                                break;
                        };
                        
                        queueInfo += "Job #" + jobIndex + " Title: " + 
                                job.getTitle() + " State: " + 
                                jobState.toString() + "\n";
                        
                    }
                    
                }
                else {
                    
                }
                
                Thread.sleep(loopDelay);
                
            } catch (InterruptedException ex) {
                LogIt.log(ex);
                running = false;
            }
        }
    }
    
    public void createJob() {
        currentJob = new PrintJob();
        currentJobThread = new Thread(currentJob);
        currentJobThread.start();
        
        if(paperSize != null) {
            currentJob.setPaperSize(paperSize);
        }
        if(autoSize) {
            currentJob.setAutoSize(true);
        }
        
        spool.add(currentJob);
    }
    
    public void append(ByteArrayBuilder data, Charset charset) {
        if(currentJob == null) {
            createJob();
        }
        
        currentJob.append(data, charset);
    }
    /**
     * Creates an image PrintJobElement and adds it to the current print job
     */
    public void appendImage(ByteArrayBuilder imagePath, Charset charset, String lang, int imageX, int imageY) {
        if(currentJob == null) {
            createJob();
        }
        
        currentJob.appendImage(imagePath, charset, lang, imageX, imageY);
    }

    public void appendImage(ByteArrayBuilder imagePath, Charset charset, String lang, int dotDensity) {
        if(currentJob == null) {
            createJob();
        }
        
        currentJob.appendImage(imagePath, charset, lang, dotDensity);
    }
    
    public void appendPSImage(ByteArrayBuilder url, Charset charset) {
        if(currentJob == null) {
            createJob();
        }
        
        currentJob.appendPSImage(url, charset);
    }
    
    public void appendXML(ByteArrayBuilder url, Charset charset, String xmlTag) {
        if(currentJob == null) {
            createJob();
        }
        
        currentJob.appendXML(url, charset, xmlTag);
    }

    public void appendFile(ByteArrayBuilder url, Charset charset) {
        if(currentJob == null) {
            createJob();
        }
        
        currentJob.appendFile(url, charset);
    }
    
    public void appendHTML(ByteArrayBuilder html, Charset charset) {
        if(currentJob == null) {
            createJob();
        }
        
        currentJob.appendHTML(html, charset);
    }
    
    public void appendPDF(ByteArrayBuilder url, Charset charset) {
        if(currentJob == null) {
            createJob();
        }
        
        currentJob.appendPDF(url, charset);
    }
    
    public boolean print() {
        if(currentJob == null) {
            LogIt.log("No data has been provided.");
            return false;
        }
        
        if(currentPrinter == null) {
            LogIt.log("No printer specified.");
            return false;
        }
        currentJob.setPrinter(currentPrinter);
        currentJob.prepareJob();
        currentJob = null;
        return true;
    }
    
    public void printToFile(String filePath) {
        if(currentJob != null) {
            
            filePrinter.setOutputPath(filePath);
            currentJob.setPrinter(filePrinter);
            
            currentJob.prepareJob();
            currentJob = null;
        }
    }
    
    public void printToHost(String jobHost, int jobPort) {
        if(currentJob != null) {
            
            currentJob.setHostOutput(jobHost, jobPort);
            currentJob.prepareJob();
            currentJob = null;
            
        }
    }
    
    public void cancelJob(int jobIndex) {
        PrintJob job = spool.get(jobIndex);
        job.cancel();
        spool.set(jobIndex, job);
    }
    
    public String getQueueInfo() {
       return queueInfo;
    }
    
    public String getJobInfo(int jobIndex) {
        PrintJob job = spool.get(jobIndex);
        return job.getInfo();
    }

    public void findAllPrinters() {
        
        PrintService[] psList;
        
        psList = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService ps : psList) {
            PrintServiceAttributeSet psa = ps.getAttributes();
            
            if(printerListString != "") {
                printerListString += ",";
            }
            String printerName = psa.get(PrinterName.class).toString();
            printerListString += printerName;
            
            Printer printer;
            
            if(ps.isDocFlavorSupported(DocFlavor.INPUT_STREAM.POSTSCRIPT)) {
                printer = (PSPrinter)new PSPrinter();
            }
            else {
                printer = (RawPrinter)new RawPrinter();
            }
            
            printer.setPrintService(ps);
            printer.setName(printerName);
            printerList.add(printer);
        }
        
    }
    
    public String getPrinters() {
        return printerListString;
    }

    void findPrinter(String printerName) {
        
        
        
        ListIterator<Printer> iterator = printerList.listIterator();
        
        while(iterator.hasNext()) {
            
            Printer printer = iterator.next();
            if(printerName == null) {
                PrintService defaultPS = PrintServiceLookup.lookupDefaultPrintService();
                if(printer.getPrintService().equals(defaultPS)) {
                    currentPrinter = printer;
                    break;
                }
            }
            else {
                if(printer.getName().equals(printerName)) {
                    currentPrinter = printer;
                    break;
                }
            }
        }
    }

    void setPrinter(int printerIndex) {
        currentPrinter = printerList.get(printerIndex);
    }

    String getPrinter() {
        if(currentPrinter != null) {
            return currentPrinter.getName();
        }
        else {
            return null;
        }
    }

    void setPaperSize(PaperFormat paperSize) {
        this.paperSize = paperSize;
        
    }

    void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
    }
    
}
