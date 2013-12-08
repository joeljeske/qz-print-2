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
import java.nio.charset.Charset;
import javax.print.PrintService;
import javax.print.attribute.standard.PrinterState;

/**
 *
 * @author Thomas Hart II
 */
public class PSPrinter implements Printer {

    private String name;
    private PrintService ps;
    private String jobTitle;
    
    public String getName() {
        return name;
    }

    public void printRaw(ByteArrayBuilder data) throws PrinterException {
        // This function should not be called on a Postscript Printer
    }

    public void printPS(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean ready() {
        LogIt.log("Printer Attributes" + ps.getAttributes().toString());
        return true;
    }

    public void setPrintService(PrintService ps) {
        this.ps = ps;
    }

    public PrintService getPrintService() {
        return ps;
    }

    public String getType() {
        return "PS";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
}
