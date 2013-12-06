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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;
import qz.exception.InvalidRawImageException;
import qz.exception.NullCommandException;

/**
 * A PrintJobElement is a piece of a PrintJob that contains a data string,
 * format and sequence number for ordering.
 * @author Thomas Hart
 */
public class PrintJobElement {
    
    public int sequence;
    public boolean prepared;
    public String type;
    public PrintJob pj;
    
    private ByteArrayBuilder data;
    private Charset charset;
    private int imageX = 0;
    private int imageY = 0;
    private int dotDensity = 32;
    private LanguageType lang;
    private String xmlTag;
    private BufferedImage bufferedImage;
    
    PrintJobElement(PrintJob pj, ByteArrayBuilder data, String type, Charset charset, String lang, int dotDensity) {
        
        this.lang = LanguageType.getType(lang);
        this.dotDensity = dotDensity;
        
        this.pj = pj;
        this.data = data;
        this.type = type;
        this.charset = charset;
        
        prepared = false;
        
    }

    PrintJobElement(PrintJob pj, ByteArrayBuilder data, String type, Charset charset, String lang, int imageX, int imageY) {
        
        this.lang = LanguageType.getType(lang);
        this.imageX = imageX;
        this.imageY = imageY;
        
        this.pj = pj;
        this.data = data;
        this.type = type;
        this.charset = charset;
        
        prepared = false;
        
    }
    PrintJobElement(PrintJob pj, ByteArrayBuilder data, String type, Charset charset, String xmlTag) {
        
        this.xmlTag = xmlTag;
        
        this.pj = pj;
        this.data = data;
        this.type = type;
        this.charset = charset;
        
        prepared = false;
        
    }

    PrintJobElement(PrintJob pj, ByteArrayBuilder data, String type, Charset charset) {
        
        this.pj = pj;
        this.data = data;
        this.type = type;
        this.charset = charset;
        
        prepared = false;
    }
    
    PrintJobElement(PrintJob pj, ByteArrayBuilder data, String type) {
        
        this.pj = pj;
        this.data = data;
        this.type = type;
        this.charset = Charset.defaultCharset();
        
        prepared = false;
    }
    
    public boolean prepare() throws IOException {

        //TODO: Add prepare code for all types
        /*
            RAW
            IMAGE
            IMAGE_PS
            XML
            HTML
            PDF
        */

        // An image file, pull the file into an ImageWrapper and get the 
        // encoded data
        if(type.equals("IMAGE")) {
            
            // Prepare the image
            String file = new String(data.getByteArray(), charset.name());
            
            BufferedImage bi;
            ImageWrapper iw;
            if (ByteUtilities.isBase64Image(file)) {
                byte[] imageData = Base64.decode(file.split(",")[1]);
                bi = ImageIO.read(new ByteArrayInputStream(imageData));
            } else {
                bi = ImageIO.read(new URL(file));
            }
            iw = new ImageWrapper(bi, lang);
            iw.setCharset(charset);
            // Image density setting (ESCP only)
            iw.setDotDensity(dotDensity);
            // Image coordinates, (EPL only)
            iw.setxPos(imageX);
            iw.setyPos(imageY);
            
            try {
                this.data = new ByteArrayBuilder(iw.getImageCommand());
            } catch (InvalidRawImageException ex) {
                LogIt.log(ex);
            } catch (UnsupportedEncodingException ex) {
                LogIt.log(ex);
            }
        }
        else if(type.equals("IMAGE_PS")) {
            String file = new String(data.getByteArray(), charset.name());
            bufferedImage = ImageIO.read(new URL(file));
        }
        else if(type.equals("XML")) {
            String file = new String(data.getByteArray(), charset.name());
            String dataString;
            byte[] dataByteArray;
            
            try {
                dataString = FileUtilities.readXMLFile(file, xmlTag);
                dataByteArray = Base64.decode(dataString);
                data = new ByteArrayBuilder(dataByteArray);
            } catch (DOMException ex) {
                LogIt.log(ex);
            } catch (NullCommandException ex) {
                LogIt.log(ex);
            } catch (ParserConfigurationException ex) {
                LogIt.log(ex);
            } catch (SAXException ex) {
                LogIt.log(ex);
            }
        }
        else if(type.equals("FILE")) {
            String file = new String(data.getByteArray(), charset.name());
            data = new ByteArrayBuilder(FileUtilities.readRawFile(file));
        }

        prepared = true;
        return true;
    }
    
    public boolean isPrepared() {
        return prepared;
    }
    
    public ByteArrayBuilder getData() {
        return data;
    }
    
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
    
    public Charset getCharset() {
        return charset;
    }
    
}
