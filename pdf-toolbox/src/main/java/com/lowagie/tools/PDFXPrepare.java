/*
 * Copyright 2019 by Martin Koegler <martin.koegler@chello.at>
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
 * the Initial Developer are Copyright (C) 1999-2006 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2006 by Paulo Soares. All Rights Reserved.
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

package com.lowagie.tools;

import java.awt.color.ICC_Profile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;

public class PDFXPrepare {
	Rectangle pageSize;
	Rectangle bleed;
	int PDFType;
	FileInputStream input;
	FileOutputStream output;
	ICC_Profile icc;
	String iccName;

	PDFXPrepare(String[] args) throws IOException {
		if (args.length != 6 && args.length != 5)
			usage();

		pageSize = parsePaperSize(args[4]);
		bleed = parseBleed(args.length == 5 ? null : args[5]);

		try {
			input = new FileInputStream(args[0]);
		} catch (IOException e) {
			throw new IOException("Failed to open input file " + args[0], e);
		}
		try {
			output = new FileOutputStream(args[1]);
		} catch (IOException e) {
			throw new IOException("Failed to open output file " + args[1], e);
		}
		if ("PDFX3".equals(args[2]))
			PDFType = PdfWriter.PDFX32002;
		else if ("PDFX1A".equals(args[2]))
			PDFType = PdfWriter.PDFX1A2001;
		else if ("NONE".equals(args[2]))
			PDFType = PdfWriter.PDFXNONE;
		else
			throw new IllegalArgumentException("Invalid PDF type " + args[2]);
		try {
			File f = new File(args[3]);
			iccName = f.getName();
			FileInputStream is = new FileInputStream(f);
			icc = ICC_Profile.getInstance(is);
		} catch (IOException e) {
			throw new IOException("Failed to open icc profile " + args[3], e);
		}
	}

	void usage() {
		System.out
				.println("Usage: input-pdf output-pdf PDFX1A|PDFX3|NONE icc-profile page-size [bleed]");
		System.out
				.println("Converts the PDF for printing. It adds bleed and scales the document.");
		throw new IllegalArgumentException("Incorrect parameters");
	}

	float parseLength(String value) {
		if (value == null)
			throw new IllegalArgumentException("Invalid length " + value);
		float unit;
		if (value.endsWith("pt")) {
			value = value.substring(0, value.length() - 2);
			unit = 1;
		} else if (value.endsWith("mm")) {
			value = value.substring(0, value.length() - 2);
			unit = 2.834646f;
		} else if (value.endsWith("cm")) {
			value = value.substring(0, value.length() - 2);
			unit = 28.34646f;
		} else if (value.endsWith("dm")) {
			value = value.substring(0, value.length() - 2);
			unit = 283.4646f;
		} else if (value.endsWith("m")) {
			value = value.substring(0, value.length() - 1);
			unit = 2834.646f;
		} else
			throw new IllegalArgumentException("Unknown unit " + value);
		float number;
		try {
			number = Float.parseFloat(value);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid number " + value, e);
		}
		return number * unit;
	}

	Rectangle parseBleed(String value) {
		try {
			float l = 0, r = 0, t = 0, b = 0;
			if (value != null) {
				String[] parts = value.split("x");
				float[] values = new float[parts.length];
				for (int i = 0; i < parts.length; i++)
					values[i] = parseLength(parts[i]);
				if (values.length == 1) {
					l = r = t = b = values[0];
				} else if (values.length == 2) {
					l = r = values[0];
					t = b = values[1];
				} else if (values.length == 4) {
					l = values[0];
					r = values[1];
					b = values[2];
					t = values[3];
				} else
					throw new IllegalArgumentException(
							"Invalid number of length: " + value);
			}
			return new Rectangle(l, t, r, b);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid bleed value: "
					+ e.getMessage(), e);
		}
	}

	Rectangle parsePaperSize(String value) {
		try {
			if ("a6".equals(value))
				value = "298ptx420pt";
			if ("a6r".equals(value))
				value = "420ptx298pt";
			if ("a5".equals(value))
				value = "420ptx595pt";
			if ("a5r".equals(value))
				value = "595ptx420pt";
			if ("a4".equals(value))
				value = "595ptx842pt";
			if ("a4r".equals(value))
				value = "842ptx595pt";
			if ("a3".equals(value))
				value = "842ptx1190pt";
			if ("a3r".equals(value))
				value = "1190ptx842pt";
			if ("a2".equals(value))
				value = "1190ptx1684pt";
			if ("a2r".equals(value))
				value = "1684ptx1190pt";
			if ("a1".equals(value))
				value = "1685ptx2384pt";
			if ("a1r".equals(value))
				value = "2384ptx1685pt";
			if ("a0".equals(value))
				value = "2384ptx3371pt";
			if ("a0r".equals(value))
				value = "3371ptx2384pt";
			float w, h;
			String[] parts = value.split("x");
			if (parts.length != 2)
				throw new IllegalArgumentException("Not two size components: "
						+ value);
			w = parseLength(parts[0]);
			h = parseLength(parts[1]);
			return new Rectangle(0, 0, w, h);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid paper size: "
					+ e.getMessage(), e);
		}
	}

	FileInputStream filterInputPDF() throws IOException, DocumentException {
		PdfReader read = new PdfReader(input);
		read.enableFullAccess();

		File tmp = File.createTempFile("pdfprepare", "tmppdf");
		tmp.deleteOnExit();
		OutputStream out = new FileOutputStream(tmp);
		PdfStamper stamper = new PdfStamper(read, out);
		stamper.setFormFlattening(true);
		stamper.close();
		read.close();
		out.close();
		return new FileInputStream(tmp);
	}

	Rectangle rotateRect(Rectangle rec, boolean rotate) {
		return rotateScaleRect(rec, 1f, rotate);
	}

	Rectangle rotateScaleRect(Rectangle rec, float scale, boolean rotate) {
		if (rotate)
			return new Rectangle(rec.getTop() * scale, rec.getLeft() * scale,
					rec.getBottom() * scale, rec.getRight() * scale);
		return new Rectangle(rec.getLeft() * scale, rec.getBottom() * scale,
				rec.getRight() * scale, rec.getTop() * scale);
	}

	float getWidth(Rectangle rect) {
		return Math.abs(rect.getRight() - rect.getLeft());
	}

	float getHeight(Rectangle rect) {
		return Math.abs(rect.getTop() - rect.getBottom());
	}

	void printRect(String info, Rectangle rect) {
		System.out.println(info + " L: " + rect.getLeft() + " T: "
				+ rect.getTop() + " R: " + rect.getRight() + " B: "
				+ rect.getBottom());
	}

	void run() throws IOException, DocumentException {
		PdfReader read = new PdfReader(filterInputPDF());

		Document doc = new Document();
		PdfWriter write = PdfWriter.getInstance(doc, output);
		write.setPDFXConformance(PDFType);
		List bookmarks = new ArrayList();
		doc.open();
		write.setFullCompression();
		if (PDFType != PdfWriter.PDFXNONE)
			write.setOutputIntents("Custom", null, null, iccName, icc);

		List inputBookmarks = SimpleBookmark.getBookmark(read);
		if (inputBookmarks != null) {
			SimpleBookmark.shiftPageNumbers(inputBookmarks, 0, null);
			bookmarks.addAll(inputBookmarks);
		}

		PdfContentByte content = write.getDirectContent();
		Rectangle paperSize = new Rectangle(0, 0, pageSize.getWidth()
				+ bleed.getLeft() + bleed.getRight(), pageSize.getTop()
				+ bleed.getTop() + bleed.getBottom());
		Rectangle cropSize = new Rectangle(bleed.getLeft(), bleed.getBottom(),
				paperSize.getWidth() - bleed.getRight(), paperSize.getTop()
						- bleed.getBottom());
		for (int pageNr = 1; pageNr <= read.getNumberOfPages(); pageNr++) {
			PdfImportedPage page = write.getImportedPage(read, pageNr);
			Rectangle origPageSize = read.getPageSize(pageNr);
			doc.setPageSize(paperSize);
			write.setBoxSize("bleed", paperSize);
			write.setBoxSize("crop", paperSize);
			write.setBoxSize("trim", cropSize);
			doc.newPage();

			boolean rotate = false;
			if ((getWidth(origPageSize) <= getHeight(origPageSize) && getWidth(pageSize) >= getHeight(pageSize))
					|| (getWidth(origPageSize) >= getHeight(origPageSize) && getWidth(pageSize) <= getHeight(pageSize)))
				rotate = true;

			origPageSize = rotateRect(origPageSize, rotate);
			float scaleX = getWidth(pageSize) / getWidth(origPageSize);
			float scaleY = getHeight(pageSize) / getHeight(origPageSize);

			float scale = scaleX > scaleY ? scaleX : scaleY;
			if (Math.abs(scaleX - scaleY) >= 0.0005f)
				System.out.println("Page " + pageNr
						+ ": page aspect ratio incompatible " + scaleX
						+ " <-> " + scaleY);

			Rectangle pageBleed = rotateScaleRect(bleed, 1 / scale, rotate);

			Rectangle origSize = page.getBoundingBox();
			Rectangle newSize = new Rectangle(origSize.getLeft()
					- pageBleed.getLeft(), origSize.getBottom()
					- pageBleed.getBottom(), origSize.getRight()
					+ pageBleed.getRight(), origSize.getTop()
					+ pageBleed.getTop(), origSize.getRotation());
			page.setBoundingBox(newSize);

			float offsetX = (getWidth(pageSize) - getWidth(origPageSize)
					* scale)
					/ 2 + bleed.getLeft() * 1 / scale;
			float offsetY = (getHeight(pageSize) - getHeight(origPageSize)
					* scale)
					/ 2 + bleed.getBottom() * 1 / scale;
			float pageY = origPageSize.getBottom();
			if (rotate)
				content.addTemplate(page, 0.0f, -scale, scale, 0f, scale
						* (offsetX + origPageSize.getRight()), scale
						* (offsetY + origPageSize.getTop()));
			else
				content.addTemplate(page, scale, 0.0f, 0.0f, scale, scale
						* (offsetX + origPageSize.getLeft()), scale
						* (offsetY + origPageSize.getBottom()));
			write.setPageEmpty(false);
			content.saveState();
			content.setLineWidth(0.25f);
			content.resetCMYKColorStroke();

			if (Math.abs(paperSize.getLeft() - cropSize.getLeft()) > 1) {
				content.moveTo(paperSize.getLeft(), cropSize.getBottom());
				content.lineTo((paperSize.getLeft() + cropSize.getLeft()) / 2,
						cropSize.getBottom());
				content.stroke();
				content.moveTo(paperSize.getLeft(), cropSize.getTop());
				content.lineTo((paperSize.getLeft() + cropSize.getLeft()) / 2,
						cropSize.getTop());
				content.stroke();
			}
			if (Math.abs(paperSize.getRight() - cropSize.getRight()) > 1) {
				content.moveTo(paperSize.getRight(), cropSize.getBottom());
				content.lineTo(
						(paperSize.getRight() + cropSize.getRight()) / 2,
						cropSize.getBottom());
				content.stroke();
				content.moveTo(paperSize.getRight(), cropSize.getTop());
				content.lineTo(
						(paperSize.getRight() + cropSize.getRight()) / 2,
						cropSize.getTop());
				content.stroke();
			}
			if (Math.abs(paperSize.getTop() - cropSize.getTop()) > 1) {
				content.moveTo(cropSize.getLeft(), paperSize.getTop());
				content.lineTo(cropSize.getLeft(),
						(paperSize.getTop() + cropSize.getTop()) / 2);
				content.stroke();
				content.moveTo(cropSize.getRight(), paperSize.getTop());
				content.lineTo(cropSize.getRight(),
						(paperSize.getTop() + cropSize.getTop()) / 2);
				content.stroke();
			}
			if (Math.abs(paperSize.getBottom() - cropSize.getBottom()) > 1) {
				content.moveTo(cropSize.getLeft(), paperSize.getBottom());
				content.lineTo(cropSize.getLeft(),
						(paperSize.getBottom() + cropSize.getBottom()) / 2);
				content.stroke();
				content.moveTo(cropSize.getRight(), paperSize.getBottom());
				content.lineTo(cropSize.getRight(),
						(paperSize.getBottom() + cropSize.getBottom()) / 2);
				content.stroke();
			}

			content.restoreState();
		}
		write.freeReader(read);
		write.setOutlines(bookmarks);
		doc.close();
		output.close();
		System.out.println("Pages processed: " + read.getNumberOfPages());
	}

	public static void main(String[] args) {
		try {
			PDFXPrepare pdf = new PDFXPrepare(args);
			pdf.run();

			System.exit(0);
		} catch (Exception e) {
			System.out.println("Error: "
					+ (e.getMessage() != null ? e.getMessage() : e.getClass()
							.getName()));
			e.printStackTrace();
			System.exit(1);
		}

	}

}
