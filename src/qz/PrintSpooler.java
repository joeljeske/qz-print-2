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

/**
 * The PrintSpooler will maintain a list of all print jobs and their status.
 * It will also initiate threaded processes for preparing print jobs and sending
 * them to the printer with the appropriate interface.
 * 
 * @author Thomas Hart
 */
public class PrintSpooler implements Runnable {
    
    public boolean running;
    public Integer loopDelay;
    public PrintJob currentJob;
    public Thread currentJobThread;
    
    private String queueInfo = "";
    private ArrayList<PrintJob> spool = new ArrayList<PrintJob>();
    private ListIterator<PrintJob> spoolIterator;
    
    public void PrintSpooler() {
        
    }
    
    // The run loop will consistently check the spool List and call functions
    // based on the state of each PrintJob
    public void run() {
        
        LogIt.log("PrintSpooler started");
        
        // Initialize system variables
        running = true;
        
        // Configurable variables
        loopDelay = 1000;
    
        // Main loop - run every loopDelay milliseconds
        while(running) {
            try {
                
                queueInfo = "";
                spoolIterator = spool.listIterator();
                
                if(spool.size() > 0) {
                    
                    while(spoolIterator.hasNext()) {

                        Integer jobIndex = spoolIterator.nextIndex();
                        PrintJob job = spoolIterator.next();
                        PrintJobState jobState = job.getJobState();
                        
                        switch(jobState) {
                            case STATE_PROCESSED:
                                job.queue();
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
     * @param iw 
     */
    public void appendImage(ByteArrayBuilder imagePath, Charset charset, String lang, Integer imageX, Integer imageY) {
        if(currentJob == null) {
            createJob();
        }
        
        currentJob.appendImage(imagePath, charset, lang, imageX, imageY);
    }

    public boolean print() {
        if(currentJob == null) {
            return false;
        }
        
        currentJob.print();
        currentJob = null;
        return true;
    }
    
    public void cancelJob(Integer jobIndex) {
        PrintJob job = spool.get(jobIndex);
        job.cancel();
        spool.set(jobIndex, job);
    }
    
    public String getQueueInfo() {
       return queueInfo;
    }
    
    public String getJobInfo(Integer jobIndex) {
        PrintJob job = spool.get(jobIndex);
        return job.getInfo(jobIndex);
    }
    
}
