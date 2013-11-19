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

import java.util.ArrayList;
import java.util.ListIterator;

/**
 * PrintJob will provide an object to hold an entire job. It should contain the 
 * raw data for the job, the title of the job, and it's current state.
 * 
 * @author Thomas Hart
 */
public class PrintJob {
    
    private PrintJobState state;
    private String title;
    private ArrayList<PrintJobElement> data;
    private Integer sequence;
    
    PrintJob(String jobTitle) {
        state = PrintJobState.STATE_CREATED;
        sequence = 0;
        this.title = jobTitle;
        data = new ArrayList<PrintJobElement>();
    }
    
    public void cancel() {
        state = PrintJobState.STATE_CANCELLED;
    }
    
    public PrintJobState getState() {
        return state;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void append(ByteArrayBuilder appendData) {
        try {
            PrintJobElement pje = new PrintJobElement(this, appendData, "RAW", sequence);
            sequence++;
            data.add(pje);
        }
        catch(NullPointerException e) {
            LogIt.log(e);
        }
    }

    public void print() {
        state = PrintJobState.STATE_READY;
        prepareJob();
    }
    
    public void prepareJob() {
        
        state = PrintJobState.STATE_PROCESSING;
        
        ListIterator dataIterator = data.listIterator();
        
        while(dataIterator.hasNext()) {
            PrintJobElement pje = (PrintJobElement) dataIterator.next();
            pje.prepare();
        }

        state = PrintJobState.STATE_PROCESSED;
        
    }
    
    public void queue() {
        state = PrintJobState.STATE_QUEUED;
    }
    
    // Returns a string with the contents of the job data
    // TODO: This is a debugging/testing function. Not needed for release
    public String getInfo(Integer jobIndex) {
        
        String jobInfo = "";
        
        ListIterator dataIterator = data.listIterator();
        
        while(dataIterator.hasNext()) {
            PrintJobElement pje = (PrintJobElement) dataIterator.next();
            ByteArrayBuilder bytes = pje.getData();
            String info = new String(bytes.getByteArray());
            jobInfo += info;
        }
        
        return jobInfo;
        
    }
    
}