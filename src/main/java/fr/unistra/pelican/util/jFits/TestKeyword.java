/*
 * @(#)TestKeyword.java  $Revision: 1.2 $ $Date: 2004/01/12 13:13:23 $
 *
 * Copyright (C) 2000 European Southern Observatory 
 * License:  GNU General Public License version 2 or later
 */
package fr.unistra.pelican.util.jFits;

import java.util.*;

/** TestKeyword class performs regression test on the FitsKeyword class.
 *
 *  @version  $Id: TestKeyword.java,v 1.2 2004/01/12 13:13:23 pgrosbol Exp $
 *  @author   P.Grosbol, ESO, <pgrosbol@eso.org>
 */
public class TestKeyword{
    public static void main(String[] argv) {
	FitsKeyword kw;

	String[] cards =
	{ "ABOOLEAN=  F / Comment",
	  "REALKW  =  1201.21e-2 / Just something",
	  "INTEGER =  3321234   / An integer",
	  "INTEGER1=  12",
	  "STRING1 = 'Any thing' / Simple string",
	  "STRING2 = 'It''s nice' / One with a quote",
	  "STRING3 = 'I''ll know if it''s nice' / One with several quotes",
	  "STRING4 = 'O' / Short string",
	  "DATE-OBS= '01/11/89' / A date",
	  "DATE    = '2002-11-04' / Another date",
	  "DATE1   = '1782-09-21T11:32:47' / Yet another date",
	  "DATE123 = '01/11/89' / An old format date",
	  "ACOMMENT CARD with some words",
	  "HIERARCH ESO DET CHIP ON  = T / HIERARCH boolean",
	  "HIERARCH ESO DET CHIP ID  = 'CCD-223''en' / HIERARCH string",
	  "HIERARCH ANY INT NO  = 732 / Test HIERARCH integer",
	  "HIERARCH ANY TEMP12  = 123.90",
	  "HIERARCH ANY time DATE  = '2001-05-22' / Check HIERARCH date",
	  "\000\nTRY THIS KEYWORD",
	  "AND_JUST_A_LONG_COMMENT",
	  "END       and some terxt",
	  "        = 'this is actually a comment'",
	  "HIERARCH ESO DET CHIP ID but no equal sign"
	};

	System.out.println("-- Test reading keyword cards --");

	for (int n=0; n<cards.length; n++) {
	    testString(cards[n].getBytes());
	}
	System.out.println("-- Test construntors -----------");

	kw = new FitsKeyword("Aname", "test a string", "This is a string");
	System.out.println(kw);

	kw = new FitsKeyword("AndALONGNAME", 98, "This is an integer");
	System.out.println(kw);

	kw = new FitsKeyword("Aname", 123.231, "This is a double");
	System.out.println(kw);

	kw = new FitsKeyword("Aname", 123346347348231097120470917.501927,
			     "This is a double");
	System.out.println(kw);

	kw = new FitsKeyword("ANd.AN.Hierarch.keyword",
			     "test a string", "This is a string");
	System.out.println(kw);

	kw = new FitsKeyword("DATE-OBS", new Date(), "This is a date");
	System.out.println(kw);

	kw = new FitsKeyword("eso.ins.date", new Date(),
			     "This is a date with a long comment");
	System.out.println(kw);

	kw = new FitsKeyword("TEST", true, "This is a boolean");
	System.out.println(kw);

	kw = new FitsKeyword("TEST.and.one.more", false, "This is a boolean");
	System.out.println(kw);

	kw = new FitsKeyword("Aname", "This is a comment card");
	System.out.println(kw);

	kw = new FitsKeyword("Comment", "This is a comment card with"
			     + " a very long comment line which does not"
			     + " fit on a single card");
	System.out.println(kw);
	System.out.println("-- Test special cases ----------");
	kw = new FitsKeyword("Ctest", 12, "Test integer conversion");
	System.out.println(kw);
	System.out.println("Read int as B,R : " + kw.getBool() 
			   + ", " + kw.getReal());
	kw = new FitsKeyword("Ctest", 12.32, "Test real conversion");
	System.out.println(kw);
	System.out.println("Read real as B,I,D : " + kw.getBool() 
			   + ", " + kw.getInt()+ ", " + kw.getDate());
	kw = new FitsKeyword("Cdate", "1949-02-12" , "Test date conversion");
	System.out.println(kw);
	System.out.println("Read string as D,I : " + kw.getDate() 
			   + ", " + kw.getInt());
	kw = new FitsKeyword("Cdate", "01/01/89" , "Test date conversion");
	System.out.println(kw);
	System.out.println("Read string as D,I : " + kw.getDate() 
			   + ", " + kw.getInt() + ", " + kw.getReal());
	kw = new FitsKeyword("Cdate", "1922.11.01" , "Test date conversion");
	System.out.println(kw);
	System.out.println("Read string as D,I : " + kw.getDate() 
			   + ", " + kw.getInt());
	kw = new FitsKeyword("LSTRING", "This is a string which will be" +
			     " truncated when written out" +
			     " to the FITS 80 character card", "");
	System.out.println(kw);
	System.out.println("Was truncated : " + kw.isValueTruncated());
	try {
	    kw = new FitsKeyword("A_STRING= 'a word' / simple string keyword");
	} catch (FitsException e) {
	    System.out.println("Exception: " + e);
	}
	System.out.println(kw);
	System.out.println("Was modified : " + kw.isModified());
	kw.setValue("Any string");
	System.out.println(kw);
	System.out.println("Was modified : " + kw.isModified());
	System.out.println("Is empty card : " + kw.isEmpty());
	try {
	    kw = new FitsKeyword(" ");
	} catch (FitsException e) {
	    System.out.println("Exception: " + e);
	}
	System.out.println(kw);
	System.out.println("Is empty card : " + kw.isEmpty());
	System.out.println("Read string as D,I,R : " + kw.getDate() 
			   + ", " + kw.getInt() + ", " + kw.getReal());
	System.out.println("-- Test finished ---------------");
    }

    private static void testString(byte[] card) {
	System.out.println("--------------------------------");
	FitsKeyword kw = null;;
	FitsKeyword key = null;
	try {
	    kw = new FitsKeyword(card);
	    System.out.println(kw);
	    String str = kw.toString();
	    kw = new FitsKeyword(str);
	    System.out.println(kw);
	} catch (FitsException e) {
	    System.out.println("FitsException>" + e);
	    return;
	}
	int type = kw.getType();
	switch (type) {
	case FitsKeyword.COMMENT:
	    System.out.println("->" + kw.getName() + "<C> "
			       + kw.getComment());
	    key = new FitsKeyword(kw.getName(), "");
	    System.out.println(key);
	    key.setName(kw.getName());
	    key.setComment(kw.getComment());
	    break;
	case FitsKeyword.STRING:
	    System.out.println("->" + kw.getName() + "<S> "
			       + kw.getString() + " <> " + kw.getString());
	    key = new FitsKeyword(kw.getName(), "" , kw.getComment());
	    System.out.println(key);
	    key.setName(kw.getName());
	    key.setValue(kw.getString());
	    break;
	case FitsKeyword.BOOLEAN:
	    System.out.println("->" + kw.getName() + "<B> "
			       + kw.getBool() + " <> " + kw.getString());
	    key = new FitsKeyword(kw.getName(), false, kw.getComment());
	    System.out.println(key);
	    key.setName(kw.getName());
	    key.setValue(kw.getBool());
	    break;
	case FitsKeyword.INTEGER:
	    System.out.println("->" + kw.getName() + "<I> "
			       + kw.getInt() + " <> " + kw.getString());
	    key = new FitsKeyword(kw.getName(), -123L, kw.getComment());
	    System.out.println(key);
	    key.setName(kw.getName());
	    key.setValue(kw.getInt());
	    break;
	case FitsKeyword.REAL:
	    System.out.println("->" + kw.getName() + "<R> "
			       + kw.getReal() + " <> " + kw.getString());
	    key = new FitsKeyword(kw.getName(), -213.32e-5, kw.getComment());
	    System.out.println(key);
	    key.setName(kw.getName());
	    key.setValue(kw.getReal());
	    break;
	case FitsKeyword.DATE:
	    System.out.println("->" + kw.getName() + "<D> "
			       + kw.getDate() + " <> " + kw.getString());
	    key = new FitsKeyword("testkey", new Date(0), kw.getComment());
	    System.out.println(key);
	    key.setName(kw.getName());
	    key.setValue(kw.getDate());
	    break;
	default:
	}
	System.out.println("->" + key.getName() + " ->> " + key.getString());
	System.out.println(key);
    }
}
