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
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.print.PrintService;

/**
 * FilePrinter is a special type of Printer that outputs to a file
 * 
 * @author Thomas Hart II
 */
public class FilePrinter implements Printer {
    
    private String name = "File Printer";
    private String outputPath;
    
    FilePrinter() {
        
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    public String getName() {
        return name;
    }

    public boolean ready() {
        // FilePrinter is always ready
        return true;
    }

    public PrintService getPrintService() {
        // No PrintService associated with FilePrinter.
        // This function should not be called
        return null;
    }

    public String getType() {
        return "FILE";
    }

    public void printRaw(ByteArrayBuilder data) throws PrinterException {
        LogIt.log("Printing to file: " + outputPath);
        
        try {
             FileOutputStream fos = new FileOutputStream(outputPath);
             fos.write(data.getByteArray());
             fos.close();
        } catch (FileNotFoundException ex) {
            LogIt.log(ex);
        } catch (IOException ex) {
            LogIt.log(ex);
        }
        
    }

    // Empty function. FilePrinter's should never have a ps set
    public void setPrintService(PrintService ps) {
        
    }

    public void printPS(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
