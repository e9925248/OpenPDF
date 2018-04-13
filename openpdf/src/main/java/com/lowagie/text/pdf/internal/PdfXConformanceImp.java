/*
 * $Id: PdfXConformanceImp.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2006 Bruno Lowagie (based on code by Paulo Soares)
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.pdf.internal;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.ExtendedColor;
import com.lowagie.text.pdf.GrayColor;
import com.lowagie.text.pdf.PatternColor;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfContentParser;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfImage;
import com.lowagie.text.pdf.PdfLiteral;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfStream;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfXConformanceException;
import com.lowagie.text.pdf.PRTokeniser;
import com.lowagie.text.pdf.RGBColor;
import com.lowagie.text.pdf.ShadingColor;
import com.lowagie.text.pdf.SpotColor;
import com.lowagie.text.pdf.interfaces.PdfXConformance;

public class PdfXConformanceImp implements PdfXConformance {

    /** A key for an aspect that can be checked for PDF/X Conformance. */
    public static final int PDFXKEY_COLOR = 1;
    /** A key for an aspect that can be checked for PDF/X Conformance. */
    public static final int PDFXKEY_CMYK = 2;
    /** A key for an aspect that can be checked for PDF/X Conformance. */
    public static final int PDFXKEY_RGB = 3;
    /** A key for an aspect that can be checked for PDF/X Conformance. */
    public static final int PDFXKEY_FONT = 4;
    /** A key for an aspect that can be checked for PDF/X Conformance. */
    public static final int PDFXKEY_IMAGE = 5;
    /** A key for an aspect that can be checked for PDF/X Conformance. */
    public static final int PDFXKEY_GSTATE = 6;
    /** A key for an aspect that can be checked for PDF/X Conformance. */
    public static final int PDFXKEY_LAYER = 7;
    /** A key for an aspect that can be checked for PDF/X Conformance. */
    public static final int PDFXKEY_CONTENT = 8;
    
    /**
     * The value indicating if the PDF has to be in conformance with PDF/X.
     */
    protected int pdfxConformance = PdfWriter.PDFXNONE;
    
    /**
     * @see com.lowagie.text.pdf.interfaces.PdfXConformance#setPDFXConformance(int)
     */
    public void setPDFXConformance(int pdfxConformance) {
        this.pdfxConformance = pdfxConformance;
    }

	/**
	 * @see com.lowagie.text.pdf.interfaces.PdfXConformance#getPDFXConformance()
	 */
	public int getPDFXConformance() {
		return pdfxConformance;
	}
    
    /**
     * Checks if the PDF/X Conformance is necessary.
     * @return true if the PDF has to be in conformance with any of the PDF/X specifications
     */
    public boolean isPdfX() {
    	return pdfxConformance != PdfWriter.PDFXNONE;
    }
    /**
     * Checks if the PDF has to be in conformance with PDF/X-1a:2001
     * @return true of the PDF has to be in conformance with PDF/X-1a:2001
     */
    public boolean isPdfX1A2001() {
    	return pdfxConformance == PdfWriter.PDFX1A2001;
    }
    /**
     * Checks if the PDF has to be in conformance with PDF/X-3:2002
     * @return true of the PDF has to be in conformance with PDF/X-3:2002
     */
    public boolean isPdfX32002() {
    	return pdfxConformance == PdfWriter.PDFX32002;
    }
    
    /**
     * Checks if the PDF has to be in conformance with PDFA1
     * @return true of the PDF has to be in conformance with PDFA1
     */
    public boolean isPdfA1() {
    	return pdfxConformance == PdfWriter.PDFA1A || pdfxConformance == PdfWriter.PDFA1B;
    }
    
    /**
     * Checks if the PDF has to be in conformance with PDFA1A
     * @return true of the PDF has to be in conformance with PDFA1A
     */
    public boolean isPdfA1A() {
    	return pdfxConformance == PdfWriter.PDFA1A;
    }
    
    public void completeInfoDictionary(PdfDictionary info) {
        if (isPdfX() && !isPdfA1()) {
            if (info.get(PdfName.GTS_PDFXVERSION) == null) {
                if (isPdfX1A2001()) {
                    info.put(PdfName.GTS_PDFXVERSION, new PdfString("PDF/X-1:2001"));
                    info.put(new PdfName("GTS_PDFXConformance"), new PdfString("PDF/X-1a:2001"));
                }
                else if (isPdfX32002())
                    info.put(PdfName.GTS_PDFXVERSION, new PdfString("PDF/X-3:2002"));
            }
            if (info.get(PdfName.TITLE) == null) {
                info.put(PdfName.TITLE, new PdfString("Pdf document"));
            }
            if (info.get(PdfName.CREATOR) == null) {
                info.put(PdfName.CREATOR, new PdfString("Unknown"));
            }
            if (info.get(PdfName.TRAPPED) == null) {
                info.put(PdfName.TRAPPED, new PdfName("False"));
            }
        }
    }
    
    public void completeExtraCatalog(PdfDictionary extraCatalog) {
        if (isPdfX() && !isPdfA1()) {
            if (extraCatalog.get(PdfName.OUTPUTINTENTS) == null) {
                PdfDictionary out = new PdfDictionary(PdfName.OUTPUTINTENT);
                out.put(PdfName.OUTPUTCONDITION, new PdfString("SWOP CGATS TR 001-1995"));
                out.put(PdfName.OUTPUTCONDITIONIDENTIFIER, new PdfString("CGATS TR 001"));
                out.put(PdfName.REGISTRYNAME, new PdfString("http://www.color.org"));
                out.put(PdfName.INFO, new PdfString(""));
                out.put(PdfName.S, PdfName.GTS_PDFX);
                extraCatalog.put(PdfName.OUTPUTINTENTS, new PdfArray(out));
            }
        }
    }

    static class PdfXContentChecker
    {
	PdfName strokeColorSpace = null;
	PdfName fillColorSpace = null;
	PdfWriter writer;
	/** A map with all supported operators operators (PDF syntax). */
	public Map<String, ContentOperator> operators;

	    public interface ContentOperator {
		    /**
		     * Invokes a content operator.
		     * 
		     * @param operands
		     *            the operands that come with the operator
		     * @param resources
		     *            TODO
		     */
		    void invoke(ArrayList<PdfObject> operands, PdfDictionary resources);

		    /**
		     * @return the name of the operator as it will be recognized in the pdf
		     *         stream
		     */
		    String getOperatorName();
		    
	    }

	    void checkColor(PdfWriter writer, PdfDictionary resources, ArrayList<Float> numbers, PdfName space) {
		    ExtendedColor color = null;
		    if (PdfName.DEVICERGB.equals(space))
			    color = new RGBColor(numbers.get(0), numbers.get(1), numbers.get(2));
		    else if (PdfName.DEVICEGRAY.equals(space))
			    color = new GrayColor(numbers.get(0));
		    else if (PdfName.DEVICECMYK.equals(space))
			    color = new CMYKColor(numbers.get(0), numbers.get(1), numbers.get(2), numbers.get(3));
		    checkPDFXConformance(writer, PDFXKEY_COLOR, color);
	    }
	    
	/**
	 * A content operator implementation (CS).
	 */
	class SetStrokeColorspace implements ContentOperator {
		/**
		 * @see com.lowagie.text.pdf.parser.ContentOperator#getOperatorName()
		 */
		@Override
		public String getOperatorName() {
			return "CS";
		}

		@Override
		public void invoke(ArrayList<PdfObject> operands, PdfDictionary resources) {
			PdfName colorSpace = (PdfName) operands.get(0);
			strokeColorSpace = colorSpace;
		}
	}

	/**
	 * A content operator implementation (cs).
	 */
	class SetFillColorspace implements ContentOperator {
		/**
		 * @see com.lowagie.text.pdf.parser.ContentOperator#getOperatorName()
		 */
		@Override
		public String getOperatorName() {
			return "cs";
		}

		@Override
		public void invoke(ArrayList<PdfObject> operands, PdfDictionary resources) {
			PdfName colorSpace = (PdfName) operands.get(0);
			fillColorSpace = colorSpace;
		}
	}

	/**
	 * A content operator implementation (sc).
	 */
	class SetFillColorPart implements ContentOperator {
		/**
		 * @see com.lowagie.text.pdf.parser.ContentOperator#getOperatorName()
		 */
		@Override
		public String getOperatorName() {
			return "sc";
		}

		@Override
		public void invoke(ArrayList<PdfObject> operands, PdfDictionary resources) {
			ArrayList<Float> nums = new ArrayList<Float>();
			for(int i = 0; i < operands.size() - 1; i++)
				nums.add(new Float(((PdfNumber)operands.get(i)).floatValue()));
			checkColor(writer, resources, nums, fillColorSpace);
		}
	}

	/**
	 * A content operator implementation (SC).
	 */
	class SetStrokeColorPart implements ContentOperator {
		/**
		 * @see com.lowagie.text.pdf.parser.ContentOperator#getOperatorName()
		 */
		@Override
		public String getOperatorName() {
			return "SC";
		}

		@Override
		public void invoke(ArrayList<PdfObject> operands, PdfDictionary resources) {
			ArrayList<Float> nums = new ArrayList<Float>();
			for(int i = 0; i < operands.size() - 1; i++)
				nums.add(new Float(((PdfNumber)operands.get(i)).floatValue()));
			checkColor(writer, resources, nums, strokeColorSpace);
		}
	}

	/**
	 * A content operator implementation (scn).
	 */
	class SetFillColor implements ContentOperator {
		/**
		 * @see com.lowagie.text.pdf.parser.ContentOperator#getOperatorName()
		 */
		@Override
		public String getOperatorName() {
			return "scn";
		}

		@Override
		public void invoke(ArrayList<PdfObject> operands, PdfDictionary resources) {
			ArrayList<Float> nums = new ArrayList<Float>();
			for(int i = 0; i < operands.size() - 2; i++)
				nums.add(new Float(((PdfNumber)operands.get(i)).floatValue()));
			checkColor(writer, resources, nums, (PdfName)operands.get(operands.size() - 1));
		}
	}

	/**
	 * A content operator implementation (SCN).
	 */
	class SetStrokeColor implements ContentOperator {
		/**
		 * @see com.lowagie.text.pdf.parser.ContentOperator#getOperatorName()
		 */
		@Override
		public String getOperatorName() {
			return "SCN";
		}

		@Override
		public void invoke(ArrayList<PdfObject> operands, PdfDictionary resources) {
			ArrayList<Float> nums = new ArrayList<Float>();
			for(int i = 0; i < operands.size() - 2; i++)
				nums.add(new Float(((PdfNumber)operands.get(i)).floatValue()));
			checkColor(writer, resources, nums, (PdfName)operands.get(operands.size() - 1));
		}
	}

	/**
	 * A content operator implementation (g).
	 */
	class SetFillColorCMYK implements ContentOperator {
		/**
		 * @see com.lowagie.text.pdf.parser.ContentOperator#getOperatorName()
		 */
		@Override
		public String getOperatorName() {
			return "g";
		}

		@Override
		public void invoke(ArrayList<PdfObject> operands, PdfDictionary resources) {
			PdfNumber g = (PdfNumber) operands.get(0);
			GrayColor color = new GrayColor(g.floatValue());
			checkPDFXConformance(writer, PDFXKEY_COLOR, color);
		}
	}

	/**
	 * A content operator implementation (G).
	 */
	class SetStrokeColorCMYK implements ContentOperator {
		/**
		 * @see com.lowagie.text.pdf.parser.ContentOperator#getOperatorName()
		 */
		@Override
		public String getOperatorName() {
			return "G";
		}

		@Override
		public void invoke(ArrayList<PdfObject> operands, PdfDictionary resources) {
			PdfNumber g = (PdfNumber) operands.get(0);
			GrayColor color = new GrayColor(g.floatValue());
			checkPDFXConformance(writer, PDFXKEY_COLOR, color);
		}
	}

	/**
	 * A content operator implementation (rg).
	 */
	class SetFillColorRGB implements ContentOperator {
		/**
		 * @see com.lowagie.text.pdf.parser.ContentOperator#getOperatorName()
		 */
		@Override
		public String getOperatorName() {
			return "rg";
		}

		@Override
		public void invoke(ArrayList<PdfObject> operands, PdfDictionary resources) {
			PdfNumber r = (PdfNumber) operands.get(0);
			PdfNumber g = (PdfNumber) operands.get(1);
			PdfNumber b = (PdfNumber) operands.get(2);
			RGBColor color = new RGBColor(r.floatValue(), g.floatValue(), b.floatValue());
			checkPDFXConformance(writer, PDFXKEY_COLOR, color);
		}
	}

	/**
	 * A content operator implementation (RG).
	 */
	class SetStrokeColorRGB implements ContentOperator {
		/**
		 * @see com.lowagie.text.pdf.parser.ContentOperator#getOperatorName()
		 */
		@Override
		public String getOperatorName() {
			return "RG";
		}

		@Override
		public void invoke(ArrayList<PdfObject> operands, PdfDictionary resources) {
			PdfNumber r = (PdfNumber) operands.get(0);
			PdfNumber g = (PdfNumber) operands.get(1);
			PdfNumber b = (PdfNumber) operands.get(2);
			RGBColor color = new RGBColor(r.floatValue(), g.floatValue(), b.floatValue());
			checkPDFXConformance(writer, PDFXKEY_COLOR, color);
		}
	}

	/**
	 * A content operator implementation (k).
	 */
	class SetFillColorGrey implements ContentOperator {
		/**
		 * @see com.lowagie.text.pdf.parser.ContentOperator#getOperatorName()
		 */
		@Override
		public String getOperatorName() {
			return "k";
		}

		@Override
		public void invoke(ArrayList<PdfObject> operands, PdfDictionary resources) {
			PdfNumber c = (PdfNumber) operands.get(0);
			PdfNumber m = (PdfNumber) operands.get(1);
			PdfNumber y = (PdfNumber) operands.get(2);
			PdfNumber k = (PdfNumber) operands.get(3);
			CMYKColor color = new CMYKColor(c.floatValue(), m.floatValue(), y.floatValue(), k.floatValue());
			checkPDFXConformance(writer, PDFXKEY_COLOR, color);
		}
	}

	/**
	 * A content operator implementation (K).
	 */
	class SetStrokeColorGrey implements ContentOperator {
		/**
		 * @see com.lowagie.text.pdf.parser.ContentOperator#getOperatorName()
		 */
		@Override
		public String getOperatorName() {
			return "K";
		}

		@Override
		public void invoke(ArrayList<PdfObject> operands, PdfDictionary resources) {
			PdfNumber c = (PdfNumber) operands.get(0);
			PdfNumber m = (PdfNumber) operands.get(1);
			PdfNumber y = (PdfNumber) operands.get(2);
			PdfNumber k = (PdfNumber) operands.get(3);
			CMYKColor color = new CMYKColor(c.floatValue(), m.floatValue(), y.floatValue(), k.floatValue());
			checkPDFXConformance(writer, PDFXKEY_COLOR, color);
		}
	}

	/**
	 * Registers a content operator that will be called when the specified
	 * operator string is encountered during content processing. Each operator
	 * may be registered only once (it is not legal to have multiple operators
	 * with the same operatorString)
	 * 
	 * @param operator
	 *            the operator that will receive notification when the operator
	 *            is encountered
	 * 
	 * @since 2.1.7
	 */
	public void registerContentOperator(ContentOperator operator) {
		String operatorString = operator.getOperatorName();
		if (operators.containsKey(operatorString)) {
			throw new IllegalArgumentException(
					MessageLocalization.getComposedMessage(
							"operator.1.already.registered", operatorString));
		}
		operators.put(operatorString, operator);
	}

    	/**
	 * Get the operator to process a command with a given name
	 * 
	 * @param operatorName
	 *            name of the operator that we might need to call
	 * 
	 * @return the operator or null if none present
	 */
	public ContentOperator lookupOperator(String operatorName) {
		return operators.get(operatorName);
	}

	/**
	 * Invokes an operator.
	 * 
	 * @param operator
	 *            the PDF Syntax of the operator
	 * @param operands
	 *            a list with operands
	 * @param resources
	 *            Pdf Resources found in the file containing the stream.
	 */
	public void invokeOperator(PdfLiteral operator,
			ArrayList<PdfObject> operands, PdfDictionary resources) {
		String operatorName = operator.toString();
		ContentOperator op = lookupOperator(operatorName);
		if (op == null) {
			// System.out.println("Skipping operator " + operator);
			return;
		}
		// System.err.println(operator);
		// System.err.println(operands);
		op.invoke(operands, resources);
	}

	    PdfXContentChecker()
	    {
		    installDefaultOperators();
	    }

	/**
	 * Loads all the supported graphics and text state operators in a map.
	 */
	protected void installDefaultOperators() {
		operators = new HashMap<String, ContentOperator>();

		registerContentOperator(new SetFillColorspace());
		registerContentOperator(new SetStrokeColorspace());
		registerContentOperator(new SetFillColorPart());
		registerContentOperator(new SetStrokeColorPart());
		//registerContentOperator(new SetFillColor());
		//registerContentOperator(new SetStrokeColor());
		registerContentOperator(new SetFillColorCMYK());
		registerContentOperator(new SetStrokeColorCMYK());
		registerContentOperator(new SetFillColorRGB());
		registerContentOperator(new SetStrokeColorRGB());
		registerContentOperator(new SetFillColorGrey());
		registerContentOperator(new SetStrokeColorGrey());
	}

	    public void checkContent(byte[] data, PdfDictionary resources, PdfWriter writer) throws IOException {
		    this.writer = writer;

		    PdfContentParser ps = new PdfContentParser(new PRTokeniser(data));
		    ArrayList<PdfObject> operands = new ArrayList<PdfObject>();
		    while (ps.parse(operands).size() > 0) {
			    PdfLiteral operator = (PdfLiteral) operands.get(operands.size() - 1);
			    invokeOperator(operator, operands, resources);
		    }
	    }
   }

	private static void checkPDFContentStream(PdfStream content, PdfWriter writer) {
        try {
		byte[] data = content.getContentBytes();
		PdfXContentChecker checker = new PdfXContentChecker();
		PdfObject obj = content.get(PdfName.RESOURCES);
		PdfDictionary resources = (PdfDictionary) obj;
		checker.checkContent(data, resources, writer);
	}
	catch(IOException e)
	{
		throw new ExceptionConverter(e);
	}
    }

    /**
	 * Business logic that checks if a certain object is in conformance with PDF/X.
     * @param writer	the writer that is supposed to write the PDF/X file
     * @param key		the type of PDF/X conformance that has to be checked
     * @param obj1		the object that is checked for conformance
     */
    public static void checkPDFXConformance(PdfWriter writer, int key, Object obj1) {
        if (writer == null || !writer.isPdfX())
            return;
        int conf = writer.getPDFXConformance();
        switch (key) {
            case PDFXKEY_COLOR:
                switch (conf) {
                    case PdfWriter.PDFX1A2001:
                        if (obj1 instanceof ExtendedColor) {
                            ExtendedColor ec = (ExtendedColor)obj1;
                            switch (ec.getType()) {
                                case ExtendedColor.TYPE_CMYK:
                                case ExtendedColor.TYPE_GRAY:
                                    return;
                                case ExtendedColor.TYPE_RGB:
                                    throw new PdfXConformanceException(MessageLocalization.getComposedMessage("colorspace.rgb.is.not.allowed"));
                                case ExtendedColor.TYPE_SEPARATION:
                                    SpotColor sc = (SpotColor)ec;
                                    checkPDFXConformance(writer, PDFXKEY_COLOR, sc.getPdfSpotColor().getAlternativeCS());
                                    break;
                                case ExtendedColor.TYPE_SHADING:
                                    ShadingColor xc = (ShadingColor)ec;
                                    checkPDFXConformance(writer, PDFXKEY_COLOR, xc.getPdfShadingPattern().getShading().getColorSpace());
                                    break;
                                case ExtendedColor.TYPE_PATTERN:
                                    PatternColor pc = (PatternColor)ec;
                                    checkPDFXConformance(writer, PDFXKEY_COLOR, pc.getPainter().getDefaultColor());
                                    break;
                            }
                        }
                        else if (obj1 instanceof Color)
                            throw new PdfXConformanceException(MessageLocalization.getComposedMessage("colorspace.rgb.is.not.allowed"));
                        break;
                }
                break;
            case PDFXKEY_CMYK:
                break;
            case PDFXKEY_RGB:
                if (conf == PdfWriter.PDFX1A2001)
                    throw new PdfXConformanceException(MessageLocalization.getComposedMessage("colorspace.rgb.is.not.allowed"));
                break;
            case PDFXKEY_FONT:
                if (!((BaseFont)obj1).isEmbedded())
                    throw new PdfXConformanceException(MessageLocalization.getComposedMessage("all.the.fonts.must.be.embedded.this.one.isn.t.1", ((BaseFont)obj1).getPostscriptFontName()));
                break;
            case PDFXKEY_IMAGE:
                PdfImage image = (PdfImage)obj1;
                if (image.get(PdfName.SMASK) != null)
                    throw new PdfXConformanceException(MessageLocalization.getComposedMessage("the.smask.key.is.not.allowed.in.images"));
                switch (conf) {
                    case PdfWriter.PDFX1A2001:
                        PdfObject cs = image.get(PdfName.COLORSPACE);
                        if (cs == null)
                            return;
                        if (cs.isName()) {
                            if (PdfName.DEVICERGB.equals(cs))
                                throw new PdfXConformanceException(MessageLocalization.getComposedMessage("colorspace.rgb.is.not.allowed"));
                        }
                        else if (cs.isArray()) {
                            if (PdfName.CALRGB.equals(((PdfArray)cs).getPdfObject(0)))
                                throw new PdfXConformanceException(MessageLocalization.getComposedMessage("colorspace.calrgb.is.not.allowed"));
                        }
                        break;
                }
                break;
            case PDFXKEY_GSTATE:
                PdfDictionary gs = (PdfDictionary)obj1;
                PdfObject obj = gs.get(PdfName.BM);
                if (obj != null && !PdfGState.BM_NORMAL.equals(obj) && !PdfGState.BM_COMPATIBLE.equals(obj))
                    throw new PdfXConformanceException(MessageLocalization.getComposedMessage("blend.mode.1.not.allowed", obj.toString()));
                obj = gs.get(PdfName.CA);
                double v = 0.0;
                if (obj != null && (v = ((PdfNumber)obj).doubleValue()) != 1.0)
                    throw new PdfXConformanceException(MessageLocalization.getComposedMessage("transparency.is.not.allowed.ca.eq.1", String.valueOf(v)));
                obj = gs.get(PdfName.ca);
                v = 0.0;
                if (obj != null && (v = ((PdfNumber)obj).doubleValue()) != 1.0)
                    throw new PdfXConformanceException(MessageLocalization.getComposedMessage("transparency.is.not.allowed.ca.eq.1", String.valueOf(v)));
                break;
            case PDFXKEY_LAYER:
                throw new PdfXConformanceException(MessageLocalization.getComposedMessage("layers.are.not.allowed"));
            case PDFXKEY_CONTENT:
		    checkPDFContentStream((PdfStream)obj1, writer);
        }
    }
}
