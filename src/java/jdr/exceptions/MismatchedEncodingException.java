/*
    Copyright (C) 2026 Nicola L.C. Talbot

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/

package com.dickimawbooks.jdr.exceptions;

import java.nio.charset.Charset;

import com.dickimawbooks.jdr.io.JDRAJR;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;
import com.dickimawbooks.jdr.CanvasGraphics;

/**
 * Exception thrown when one encoding name was expected but another found.
 * @author Nicola L C Talbot
 */
public class MismatchedEncodingException extends InvalidFormatException
{
   public MismatchedEncodingException(Charset expected, Charset found, CanvasGraphics cg)
   {
      this(expected, found, cg.getMessageDictionary());
   }

   public MismatchedEncodingException(Charset expected, Charset found, 
     JDRMessageDictionary msgSys)
   {
      super(msgSys.getMessageWithFallback("error.mismatched_encoding",
            "Mismatched encoding (expected '{0}' but found '{1}'",
            expected, found));
   }

   public MismatchedEncodingException(Charset expected, Charset found, JDRAJR jdr)
   {
      super(jdr.getMessageSystem().getMessageWithFallback(
            "error.mismatched_encoding",
            "Mismatched encoding (expected '{0}' but found '{1}'",
            expected, found), jdr);
   }

   public MismatchedEncodingException(Charset expected, Charset found, JDRAJR jdr,
      Throwable cause)
   {
      super(jdr.getMessageSystem().getMessageWithFallback(
            "error.mismatched_encoding",
            "Mismatched encoding (expected '{0}' but found '{1}'",
            expected, found), jdr, cause);
   }

   public Charset getFound()
   {
      return found;
   }

   public Charset getExpected()
   {
      return expected;
   }

   private Charset found, expected;
}
