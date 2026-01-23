package com.dickimawbooks.jdr.io.svg;

import java.util.Vector;
import org.xml.sax.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
   
import com.dickimawbooks.jdr.exceptions.*;

public class XHTMLCodeElement extends XHTMLSpanElement
{
   public XHTMLCodeElement(SVGHandler handler, SVGAbstractElement parent)
   {
      super("code", handler, parent);
      containsVerb = true;
   }

   @Override
   public boolean supportsParseMaths()
   {
      return false;
   }

   @Override
   public boolean isVerbatim()
   {
      return true;
   }

   @Override
   protected CharSequence getLaTeXContent()
   {
      Vector<String> delims = new Vector<String>();

      for (String delim : POSSIBLE_DELIMS)
      {
         delims.add(delim);
      }

      for (int i = 0; i < contents.length(); )
      {
         int cp = contents.codePointAt(i);
         i += Character.charCount(cp);

         delims.remove(new String(Character.toChars(cp)));
      }

      if (delims.isEmpty())
      {
         return "\\texttt{\\detokenize{" + contents.toString() + "}}";
      }
      else
      {
         String delim = delims.firstElement();

         return "\\verb" + delim + contents.toString() + delim;
      }
   }

   @Override
   public Object clone()
   {
      XHTMLCodeElement elem = new XHTMLCodeElement(handler, parent);
      elem.makeEqual(this);
      return elem;
   }

   static final String[] POSSIBLE_DELIMS = 
     {
      "|", "!", "\"", "+", "-", "=", "'", "`", "/", "?", "@", ".", ",", ":", ";",
      "<", ">", "(", ")", "[", "]", "~", "0", "1", "2", "3", "4", "5", "6",
      "7", "8", "9", "_", "^", "#", "$", "&", "A", "B", "C", "D", "E", "F", "G",
      "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
      "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g",
      "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
      "w", "x", "y", "z"
     };
}
