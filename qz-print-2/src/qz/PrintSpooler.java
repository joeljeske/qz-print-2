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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import qz.exception.InvalidFileTypeException;
import qz.exception.InvalidRawImageException;
import qz.exception.NullCommandException;
import qz.exception.NullPrintServiceException;
import qz.json.JSONArray;
import qz.reflection.ReflectException;

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
    
    private JSONArray queueInfo;
    private final ArrayList<PrintJob> spool = new ArrayList<PrintJob>();
    private ListIterator<PrintJob> spoolIterator;
    private Printer currentPrinter;
    private String lastPrinterName;
    private ArrayList<Printer> printerList;
    private String printerListString;
    private FilePrinter filePrinter;
    private SerialPrinter serialPrinter;
    private PaperFormat paperSize;
    private boolean autoSize;
    private boolean logPSFeatures;
    private String endOfDocument;
    private int docsPerSpool;
    private int openJobs;
    private NetworkUtilities networkUtilities;
    private String macAddress;
    private String ipAddress;
    private boolean alternatePrint;
    private Applet applet;
    private Throwable exception;
    
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
        logPSFeatures = false;
        endOfDocument = "";
        docsPerSpool = 0;
        openJobs = 0;
        alternatePrint = false;
        exception = null;
        currentPrinter = null;
        
        // Main loop
        while(running) {
            synchronized(spool) {
                if(spool.size() > 0) {
                    spoolIterator = spool.listIterator();
                    JSONArray currentQueueInfo = new JSONArray();
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
                        };

                        HashMap<String, String> jobInfo = new HashMap<String, String>();
                        jobInfo.put("id", String.valueOf(jobIndex));
                        jobInfo.put("title", job.getTitle());
                        jobInfo.put("state", jobState.name());
                        currentQueueInfo.put(jobInfo);
                    }
                    queueInfo = currentQueueInfo;
                }
            }
        }
    }
    
    public void setApplet(Applet applet) {
        this.applet = applet;
        serialPrinter = new SerialPrinter(applet);
    }
    
    public void createJob() {
        
        openJobs += 1;
        
        currentJob = new PrintJob();
        currentJobThread = new Thread(currentJob);
        currentJobThread.start();
        
        if(paperSize != null) {
            currentJob.setPaperSize(paperSize);
        }
        if(autoSize) {
            currentJob.setAutoSize(true);
        }
        
        currentJob.setLogPostScriptFeatures(logPSFeatures);
        currentJob.setAlternatePrinting(alternatePrint);
        
        synchronized(spool) {
            spool.add(currentJob);
        }
    }
    
    public void append(ByteArrayBuilder data, Charset charset) {
        if(currentJob == null) {
            createJob();
        }
        
        currentJob.append(data, charset);
    }
    /**
     * Creates an image PrintJobElement and adds it to the current print job
     * @param imagePath
     * @param charset
     * @param lang
     * @param imageX
     * @param imageY
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
        
        if(!"".equals(endOfDocument)) {
            
            String[] fileData = getFileData(url, charset).split(endOfDocument);
            String[] consolidatedData;
            
            if(docsPerSpool > 1) {
                
                int dataLength = fileData.length;
                int newArrayLength = ((dataLength - (dataLength % docsPerSpool)) / docsPerSpool) + 1;

                consolidatedData = new String[newArrayLength];
                
                for(int i=0; i < newArrayLength; i++) {
                    String jobData = "";
                    for(int j=0; j < docsPerSpool; j++) {
                        int index = (i * docsPerSpool) + j;
                        if(index < dataLength) {
                            jobData += fileData[index] + endOfDocument;
                        }
                    }
                    consolidatedData[i] = jobData;
                }
                
            }
            else {
                consolidatedData = new String[fileData.length];
                
                for(int i=0; i < fileData.length; i++) {
                    consolidatedData[i] = fileData[i] + endOfDocument;
                }
                
            }
            
            for(String dataString : consolidatedData) {
                if(currentJob == null) {
                    createJob();
                }
                ByteArrayBuilder bytes = new ByteArrayBuilder();
                try {
                    bytes.append(dataString, charset);
                } catch (UnsupportedEncodingException ex) {
                    LogIt.log(ex);
                }
                currentJob.append(bytes, charset);
                currentJob = null;
            }
            
            endOfDocument = "";
            docsPerSpool = 0;
        }
        else {
            if(currentJob == null) {
                createJob();
            }
            currentJob.appendFile(url, charset);
        }
        
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
        if(currentPrinter == null) {
            LogIt.log("A printer has not been selected.");
            setException(new NullPrintServiceException("A printer has not been selected."));
            return false;
        }
        lastPrinterName = currentPrinter.getName();
        
        if(openJobs == 0) {
            LogIt.log("No data has been provided.");
            setException(new NullCommandException("No data has been provided."));
            return false;
        }
        else if(openJobs == 1) {
            currentJob.setPrinter(currentPrinter);
            try {
                currentJob.prepareJob();
            }
            catch (InvalidRawImageException ex) {
                LogIt.log(ex);
                setException(ex);
            }
            catch (NullCommandException ex) {
                LogIt.log(ex);
                setException(ex);
            }
            currentJob = null;
            openJobs = 0;
            return true;
        }
        else {
            synchronized(spool) {
                while(openJobs > 0) {
                    PrintJob job = spool.get(spool.size() - openJobs);
                    job.setPrinter(currentPrinter);
                    try {
                        job.prepareJob();
                    }
                    catch (InvalidRawImageException ex) {
                        LogIt.log(ex);
                        setException(ex);
                    }
                    catch (NullCommandException ex) {
                        LogIt.log(ex);
                        setException(ex);
                    }
                    openJobs -= 1;
                }
            }
            currentJob = null;
            return true;
        }
    }
    
    public void printToFile(String filePath) {
        if(currentJob != null) {
            lastPrinterName = "File";
            try {
                filePrinter.setOutputPath(filePath);
                currentJob.setPrinter(filePrinter);
                currentJob.prepareJob();
            } catch (InvalidRawImageException ex) {
                LogIt.log(ex);
                setException(ex);
            } catch (NullCommandException ex) {
                LogIt.log(ex);
                setException(ex);
            } catch (InvalidFileTypeException ex) {
                LogIt.log(ex);
                setException(ex);
            }
            currentJob = null;
        }
        else {
            LogIt.log("No data has been provided.");
            setException(new NullCommandException("No data has been provided."));
        }
    }
    
    public void printToHost(String jobHost, int jobPort) {
        if(currentJob != null) {
            lastPrinterName = "Remote Host";
            currentJob.setHostOutput(jobHost, jobPort);
            try {
                currentJob.prepareJob();
            } catch (InvalidRawImageException ex) {
                LogIt.log(ex);
                setException(ex);
            } catch (NullCommandException ex) {
                LogIt.log(ex);
                setException(ex);
            }
            currentJob = null;
        }
        else {
            LogIt.log("No data has been provided.");
            setException(new NullCommandException("No data has been provided."));
        }
    }
    
    public void cancelJob(int jobIndex) {
        synchronized(spool) {
            PrintJob job = spool.get(jobIndex);
            job.cancel();
            spool.set(jobIndex, job);
        }
    }
    
    public JSONArray getQueueInfo() {
        return queueInfo;
    }
    
    public String getJobInfo(int jobIndex) {
        PrintJob job = spool.get(jobIndex);
        String jobInfo = job.getInfo();
        LogIt.log("Job Data: " + jobInfo);
        return jobInfo;
    }

    public void findAllPrinters() {
        
        PrintService[] psList;
        
        psList = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService ps : psList) {
            PrintServiceAttributeSet psa = ps.getAttributes();
            
            if(!"".equals(printerListString)) {
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

    public void findPrinter(String printerName) {
        
        currentPrinter = null;
        
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
                if(printer.getName().toLowerCase().indexOf(printerName.toLowerCase()) != -1) {
                    currentPrinter = printer;
                    break;
                }
            }
        }
        
        if(currentPrinter != null) {
            LogIt.log("Found printer \"" + currentPrinter.getName() + "\".");
        }
        else {
            LogIt.log("Could not find printer with name containing \"" + printerName + "\".");
        }
        
    }

    public void setPrinter(int printerIndex) {
        currentPrinter = printerList.get(printerIndex);
    }

    public String getPrinter() {
        if(currentPrinter != null) {
            return currentPrinter.getName();
        }
        else {
            return null;
        }
    }
    
    public String getLastPrinter() {
        if(lastPrinterName != null) {
            return lastPrinterName;
        }
        else {
            return null;
        }
    }

    public void setPaperSize(PaperFormat paperSize) {
        this.paperSize = paperSize;
        
    }

    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
    }
    
    public boolean getLogPostScriptFeatures() {
        return logPSFeatures;
    }

    public void setLogPostScriptFeatures(boolean logPSFeatures) {
        this.logPSFeatures = logPSFeatures;
        if(currentJob != null) {
            currentJob.setLogPostScriptFeatures(logPSFeatures);
        }
        LogIt.log("Console logging of PostScript printing features set to \"" + logPSFeatures + "\"");
    }

    public void setEndOfDocument(String endOfDocument) {
        this.endOfDocument = endOfDocument;
        LogIt.log("End of Document set to " + this.endOfDocument);
    }

    public void setDocumentsPerSpool(int docsPerSpool) {
        this.docsPerSpool = docsPerSpool;
        LogIt.log("Documents per Spool set to " + this.docsPerSpool);
    }

    public String getFileData(ByteArrayBuilder url, Charset charset) {
        
        String file;
        String data = null;
        
        try {
            file = new String(url.getByteArray(), charset.name());
            data = new String(FileUtilities.readRawFile(file), charset.name());
        } catch (UnsupportedEncodingException ex) {
            LogIt.log(ex);
        } catch (IOException ex) {
            LogIt.log(ex);
        }
        
        return data;
    }

    public void findNetworkInfo() {
        
        if(networkUtilities == null) {
            try {
                networkUtilities = new NetworkUtilities();
            } catch (SocketException ex) {
                LogIt.log(ex);
            } catch (ReflectException ex) {
                LogIt.log(ex);
            } catch (UnknownHostException ex) {
                LogIt.log(ex);
            }
        }
        
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    networkUtilities.gatherNetworkInfo();
                } catch (IOException ex) {
                    LogIt.log(ex);
                } catch (ReflectException ex) {
                    LogIt.log(ex);
                }
                return null;
            }
        });

        macAddress = networkUtilities.getHardwareAddress();
        ipAddress = networkUtilities.getInetAddress();
        LogIt.log("Found Network Adapter. MAC: " + macAddress + " IP: " + ipAddress);

    }

    public String getMac() {
        return macAddress;
    }

    public String getIP() {
        return ipAddress;
    }
    
    public void useAlternatePrinting(boolean alternatePrint) {
        this.alternatePrint = alternatePrint;
        
        if(currentJob != null) {
            currentJob.setAlternatePrinting(alternatePrint);
        }
        
        LogIt.log("Alternate printing set to " + alternatePrint);
    }
    
    public boolean isAlternatePrinting() {
        return alternatePrint;
    }

    public void findPorts() {
        serialPrinter.findPorts();
    }

    public String getPorts() {
        return serialPrinter.getPorts();
    }

    public void openPort(String portName) {
        serialPrinter.openPort(portName);
    }

    public void closePort(String portName) {
        serialPrinter.closePort(portName);
    }

    public void setSerialBegin(ByteArrayBuilder serialBegin) {
        serialPrinter.setSerialBegin(serialBegin);
    }

    public void setSerialEnd(ByteArrayBuilder serialEnd) {
        serialPrinter.setSerialEnd(serialEnd);
    }

    public void setSerialProperties(String baud, String dataBits, String stopBits, String parity, String flowControl) {
        serialPrinter.setSerialProperties(baud, dataBits, stopBits, parity, flowControl);
    }

    public void sendSerialData(String serialData) {
        serialPrinter.send(serialData);
    }

    public String getReturnData() {
        return serialPrinter.getReturnData();
    }

    public void setException(Throwable t) {
        this.exception = t;
    }
    
    public void clearException() {
        exception = null;
    }
    
    public Throwable getException() {
        return exception;
    }
    
}
