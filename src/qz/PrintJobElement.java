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

/**
 * A PrintJobElement is a piece of a PrintJob that contains a data string,
 * format and sequence number for ordering.
 * @author Thomas Hart
 */
public class PrintJobElement {
    
    public Integer sequence;
    public boolean prepared;
    public String type;
    public PrintJob pj;
    
    private ByteArrayBuilder data;
    private Charset charset;
    
    PrintJobElement(PrintJob pj, ByteArrayBuilder data, String type, Charset charset, Integer sequence) {
        
        this.pj = pj;
        this.data = data;
        this.type = type;
        this.charset = charset;
        this.sequence = sequence;
        
        prepared = false;
    }
    
    public boolean prepare() {
        //TODO: Add prepare code
        prepared = true;
        return true;
    }
    
    public boolean isPrepared() {
        return prepared;
    }
    
    public ByteArrayBuilder getData() {
        return data;
    }
    
    public Charset getCharset() {
        return charset;
    }
    
}
