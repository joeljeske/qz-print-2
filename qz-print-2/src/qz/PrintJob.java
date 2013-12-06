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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import static java.awt.print.Printable.PAGE_EXISTS;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;

/**
 * PrintJob will provide an object to hold an entire job. It should contain the 
 * raw data for the job, the title of the job, and it's current state.
 * 
 * @author Thomas Hart
 */
public class PrintJob implements Runnable, Printable {
    
    private PrintJobState state = PrintJobState.STATE_CREATED;
    private String title = "Print Job";
    private ArrayList<PrintJobElement> rawData = new ArrayList<PrintJobElement>();;
    private Boolean running = true;
    private int updateDelay = 100;
    private Printer printer;
    private PrintJobType type;
    private Graphics graphics;
    private PageFormat pageFormat;
    private int pageIndex;
    private PaperFormat paperSize;
    
    private boolean autoSize;

    public void run() {
        
        this.autoSize = false;
        
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
        type = PrintJobType.TYPE_RAW;
        try {
            PrintJobElement pje = new PrintJobElement(this, appendData, "RAW", charset);
            rawData.add(pje);
        }
        catch(NullPointerException e) {
            LogIt.log(e);
        }
    }
    
    public void appendImage(ByteArrayBuilder imagePath, Charset charset, String lang, int imageX, int imageY) {
        type = PrintJobType.TYPE_RAW;
        try {
            PrintJobElement pje = new PrintJobElement(this, imagePath, "IMAGE", charset, lang, imageX, imageY);
            rawData.add(pje);
        }
        catch(NullPointerException e) {
            LogIt.log(e);
        }
    }
    public void appendImage(ByteArrayBuilder imagePath, Charset charset, String lang, int dotDensity) {
        type = PrintJobType.TYPE_RAW;
        try {
            PrintJobElement pje = new PrintJobElement(this, imagePath, "IMAGE", charset, lang, dotDensity);
            rawData.add(pje);
        }
        catch(NullPointerException e) {
            LogIt.log(e);
        }
    }
    
    public void appendPSImage(String url) {
        type = PrintJobType.TYPE_PS;
        ByteArrayBuilder data = new ByteArrayBuilder(url.getBytes());
        PrintJobElement pje = new PrintJobElement(this, data, "IMAGE_PS");
        rawData.add(pje);
    }

    public void appendXML(ByteArrayBuilder url, Charset charset, String xmlTag) {
        PrintJobElement pje = new PrintJobElement(this, url, "XML", charset, xmlTag);
        rawData.add(pje);
    }
    
    public void appendFile(ByteArrayBuilder url, Charset charset) {
        PrintJobElement pje = new PrintJobElement(this, url, "FILE", charset);
        rawData.add(pje);
    }
    
    public void prepareJob() {
        
        state = PrintJobState.STATE_PROCESSING;
        
        if(type == PrintJobType.TYPE_RAW || type == PrintJobType.TYPE_PS) {
            
            ListIterator dataIterator = rawData.listIterator();

            while(dataIterator.hasNext()) {
                try {
                    PrintJobElement pje = (PrintJobElement) dataIterator.next();
                    pje.prepare();
                } catch (IOException ex) {
                    LogIt.log(ex);
                }
            }
        }
        else {
            LogIt.log("Error: Unsupported job type.");
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
        
        if(type == PrintJobType.TYPE_RAW) {
            ListIterator dataIterator = rawData.listIterator();

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
        }
        else {
            LogIt.log("Error: Unsupported job type.");
        }
        
        return jobInfo;
        
    }
    
    // TODO: Stub for now.
    public void print() {
        state = PrintJobState.STATE_SENDING;
        
        if(type == PrintJobType.TYPE_RAW) {
            ByteArrayBuilder jobData = new ByteArrayBuilder();

            // Concatenate all the PrintJobElements into one ByteArrayBuilder
            ListIterator dataIterator = rawData.listIterator();

            while(dataIterator.hasNext()) {
                PrintJobElement pje = (PrintJobElement) dataIterator.next();
                ByteArrayBuilder bytes = pje.getData();
                jobData.append(bytes.getByteArray());
            }
            
            try {
                printer.printRaw(jobData);
            }
            catch(PrinterException ex) {
                LogIt.log(ex);
            }
        }
        else if(type == PrintJobType.TYPE_PS) {
            // PostScript jobs only support a single PrintJobElement
            // This should be either an image (type IMAGE_PS) or pdf
            try {
                
                PrintJobElement firstElement = rawData.get(0);
                PrinterJob job = PrinterJob.getPrinterJob();
                
                int w;
                int h;

                if (firstElement.getBufferedImage() != null) {
                    w = firstElement.getBufferedImage().getWidth();
                    h = firstElement.getBufferedImage().getHeight();
                } 
                /* else if (firstElement.getPDFFile() != null) {
                    w = ((Float)getPDFFile().call("getPage", 1).call("getWidth").get()).intValue();
                    h = ((Float)getPDFFile().call("getPage", 1).call("getHeight").get()).intValue();
                } */
                else {
                    throw new PrinterException("Corrupt or missing file supplied.");
                }
                
                HashPrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
                
                if (paperSize != null) {
                    attr.add(paperSize.getOrientationRequested());
                    if (paperSize.isAutoSize()) {
                        if(rawData.get(0).type == "IMAGE_PS") {
                            paperSize.setAutoSize(rawData.get(0).getBufferedImage());
                        }
                    }
                    attr.add(new MediaPrintableArea(0f, 0f, paperSize.getAutoWidth(), paperSize.getAutoHeight(), paperSize.getUnits()));

                } else {
                    attr.add(new MediaPrintableArea(0f, 0f, w / 72f, h / 72f, MediaSize.INCH));
                }
                
                job.setPrintService(printer.getPrintService());
                job.setPrintable(this);
                job.setJobName(title);
                job.print(attr);

            } catch (PrinterException ex) {
                LogIt.log(ex);
            }
        }
        else {
            LogIt.log("Error: Unsupported job type.");
        }
            
        state = PrintJobState.STATE_COMPLETE;

    }
    
    public void setPrinter(Printer printer) {
        this.printer = printer;
    }
    
    public Printer getPrinter() {
        return printer;
    }

    
    /**
     * This function is not called directly. It's used by the Printable interface to render each page
     * @param graphics
     * @param pageFormat
     * @param pageIndex
     * @return
     * @throws PrinterException 
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if(pageIndex < rawData.size()) {
            PrintJobElement pje = rawData.get(pageIndex);
            if(pje.type == "IMAGE_PS") {
                /* User (0,0) is typically outside the imageable area, so we must
                * translate by the X and Y values in the PageFormat to avoid clipping
                */
               Graphics2D g2d = (Graphics2D) graphics;

               // Sugested by Bahadir 8/23/2012
               g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
               g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
               g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

               g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
               BufferedImage imgToPrint = pje.getBufferedImage();
               /* Now we perform our rendering */
               g2d.drawImage(imgToPrint, 0, 0, (int) pageFormat.getImageableWidth(), (int) pageFormat.getImageableHeight(), imgToPrint.getMinX(), imgToPrint.getMinY(), imgToPrint.getWidth(), imgToPrint.getHeight(), null);

               /* tell the caller that this page is part of the printed document */
               return PAGE_EXISTS;
            }
        }
        
        return NO_SUCH_PAGE;
    }
    
    void setPaperSize(PaperFormat paperSize) {
        this.paperSize = paperSize;
    }

    void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
    }

}