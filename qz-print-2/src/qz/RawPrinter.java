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
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Locale;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

/**
 *
 * @author Thomas Hart II
 */
public class RawPrinter implements Printer {

    private String name;
    private PrintService ps;
    private boolean isFinished;
    private DocFlavor docFlavor;
    private DocAttributeSet docAttr;
    private PrintRequestAttributeSet reqAttr;
    private String jobTitle;
    
    public String getName() {
        return name;
    }

    public void printRaw(ByteArrayBuilder data) throws PrinterException {
        
        docFlavor = DocFlavor.BYTE_ARRAY.TEXT_PLAIN_UTF_8;
        
        SimpleDoc doc = new SimpleDoc(data.getByteArray(), docFlavor, docAttr);
        
        reqAttr.add(new JobName(jobTitle, Locale.getDefault()));
        DocPrintJob pj = ps.createPrintJob();
        pj.addPrintJobListener(new PrintJobListener() {
            //@Override //JDK 1.6
            public void printDataTransferCompleted(PrintJobEvent pje) {
                LogIt.log(pje);
                isFinished = true;
            }

            //@Override //JDK 1.6
            public void printJobCompleted(PrintJobEvent pje) {
                LogIt.log(pje);
                isFinished = true;
            }

            //@Override //JDK 1.6
            public void printJobFailed(PrintJobEvent pje) {
                LogIt.log(pje);
                isFinished = true;
            }

            //@Override //JDK 1.6
            public void printJobCanceled(PrintJobEvent pje) {
                LogIt.log(pje);
                isFinished = true;
            }

            //@Override //JDK 1.6
            public void printJobNoMoreEvents(PrintJobEvent pje) {
                LogIt.log(pje);
                isFinished = true;
            }

            //@Override //JDK 1.6
            public void printJobRequiresAttention(PrintJobEvent pje) {
                LogIt.log(pje);
            }
        });

        LogIt.log("Sending print job to printer: \"" + ps.getName() + "\"");
        
        try {
            pj.print(doc, reqAttr);
        } catch (PrintException ex) {
            LogIt.log(ex);
        }

        while (!isFinished) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LogIt.log(ex);
            }
        }

        LogIt.log("Print job received by printer: \"" + ps.getName() + "\"");
    }

    public void printToHost(ByteArrayBuilder data, String jobHost, int jobPort) {
        LogIt.log("Printing to host " + jobHost + ":" + jobPort);
        
        try {
            Socket socket = new Socket(jobHost, jobPort);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.write(data.getByteArray());
            socket.close();
        }
        catch (IOException ex) {
            LogIt.log(ex);
        }
        
    }
    
    public boolean ready() {
        return true;
    }

    public void setPrintService(PrintService ps) {
        this.ps = ps;
    }

    public PrintService getPrintService() {
        return ps;
    }

    public String getType() {
        return "RAW";
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
}
