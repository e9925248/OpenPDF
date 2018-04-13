/*
 * $Id: PdfContentConverter.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 1999, 2000, 2001, 2002 Bruno Lowagie
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

package com.lowagie.text.pdf;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.error_messages.MessageLocalization;

public class PdfContentConvert
{
	PdfWriter writer;
	ByteBuffer content;
	ColorspaceConverter convert;

	public PdfContentConvert(PdfWriter writer, ColorspaceConverter convert)
	{
		this.writer = writer;
		content = new ByteBuffer();
		this.convert = convert;
		installDefaultOperators();
	}

	void outputColorNumbers(Color color) {
		int type = ExtendedColor.getType(color);
		switch (type) {
		case ExtendedColor.TYPE_RGB:
			content.append((float)(color.getRed()) / 0xFF);
			content.append(' ');
			content.append((float)(color.getGreen()) / 0xFF);
			content.append(' ');
			content.append((float)(color.getBlue()) / 0xFF);
			break;
		case ExtendedColor.TYPE_GRAY:
			content.append(((GrayColor)color).getGray());
			break;
		case ExtendedColor.TYPE_CMYK: {
			CMYKColor cmyk = (CMYKColor)color;
			content.append(cmyk.getCyan()).append(' ').append(cmyk.getMagenta());
			content.append(' ').append(cmyk.getYellow()).append(' ').append(cmyk.getBlack());
			break;
		}
		default:
			throw new RuntimeException(MessageLocalization.getComposedMessage("invalid.color.type"));
		}
	}

	protected void setFillColor(ExtendedColor color)
	{
		color = convert.convert(color);
		outputColorNumbers(color);
		int type = ExtendedColor.getType(color);
		switch (type) {
		case ExtendedColor.TYPE_RGB:
			content.append(" rg\n");
			break;
		case ExtendedColor.TYPE_GRAY:
			content.append(" g\n");
			break;
		case ExtendedColor.TYPE_CMYK:
			content.append(" k\n");
			break;
		default:
			throw new RuntimeException(MessageLocalization.getComposedMessage("invalid.color.type"));
		}
	}
	
	protected void setStrokeColor(ExtendedColor color)
	{
		color = convert.convert(color);
		outputColorNumbers(color);
		int type = ExtendedColor.getType(color);
		switch (type) {
		case ExtendedColor.TYPE_RGB:
			content.append(" RG\n");
			break;
		case ExtendedColor.TYPE_GRAY:
			content.append(" G\n");
			break;
		case ExtendedColor.TYPE_CMYK:
			content.append(" K\n");
			break;
		default:
			throw new RuntimeException(MessageLocalization.getComposedMessage("invalid.color.type"));
		}
	}
	
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
			setFillColor(color);
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
			setStrokeColor(color);
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
			setFillColor(color);
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
			setStrokeColor(color);
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
			setFillColor(color);
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
			setStrokeColor(color);
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
			ArrayList<PdfObject> operands, PdfDictionary resources) throws IOException {
		String operatorName = operator.toString();
		ContentOperator op = lookupOperator(operatorName);
		if (op == null) {
			for(int i = 0; i < operands.size(); i++)
			{
				operands.get(i).toPdf(writer, content);
				content.append(' ');
			}
			content.append('\n');
			return;
		}
		// System.err.println(operator);
		// System.err.println(operands);
		op.invoke(operands, resources);
	}

		/**
	 * Loads all the supported graphics and text state operators in a map.
	 */
	protected void installDefaultOperators() {
		operators = new HashMap<String, ContentOperator>();

		registerContentOperator(new SetFillColorCMYK());
		registerContentOperator(new SetStrokeColorCMYK());
		registerContentOperator(new SetFillColorRGB());
		registerContentOperator(new SetStrokeColorRGB());
		registerContentOperator(new SetFillColorGrey());
		registerContentOperator(new SetStrokeColorGrey());
	}


	protected byte[] filterContent(byte[] data, PdfDictionary resources) throws IOException {
		this.writer = writer;

		PdfContentParser ps = new PdfContentParser(new PRTokeniser(data));
		ArrayList<PdfObject> operands = new ArrayList<PdfObject>();
		while (ps.parse(operands).size() > 0) {
			PdfLiteral operator = (PdfLiteral) operands.get(operands.size() - 1);
			invokeOperator(operator, operands, resources);
		}
		return content.getBuffer();
	}

	public void filterStream(PRStream stream) {
		try {
			byte[] data = stream.getContentBytes();
			PdfObject obj = stream.get(PdfName.RESOURCES);
			PdfDictionary resources = (PdfDictionary) obj;
			stream.setData(filterContent(data, resources));
		}
		catch(IOException e) {
			throw new ExceptionConverter(e);
		}
	}

}
