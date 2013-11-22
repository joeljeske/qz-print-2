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
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import javax.print.PrintService;

/**
 * Printer is an abstract class that defines the common functionality of all
 * types of printers
 * 
 * @author Thomas Hart II
 */
public interface Printer {

    // getName should return a String representation of the Printer's name
    public abstract String getName();
    
    // Print is implmemented from the Printable interface.
    // This function does the heavy lifting of actually pushing data to 
    // the printer.
    public abstract void print(ByteArrayBuilder data) throws PrinterException;
    
    /**
     * Returns a boolean value based on whether the printer is ready to accept 
     * a job
     * 
     * @return Boolean, true if printer is ready
    */ 
    public abstract boolean ready();
    
    /**
     * Sets the PrintService associated with this Printer
     */
    public abstract void setPrintService(PrintService ps);
    
    /**
     * Returns the PrintService associated with this Printer
     * 
     * @return 
     */
    public abstract PrintService getPrintService();
    
    /**
     * Returns a String with the type of printer
     * Possibilities: RAW, PS, FILE, DEBUG
     * 
     * @return The printer type
     */
    public abstract String getType();
}
