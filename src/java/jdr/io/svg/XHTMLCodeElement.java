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
   protected boolean supportsParseMaths()
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

      delims.add("|");
      delims.add("!");
      delims.add("\"");
      delims.add("+");
      delims.add("-");
      delims.add("=");
      delims.add("'");
      delims.add("`");
      delims.add("/");
      delims.add("?");
      delims.add("@");
      delims.add(".");
      delims.add(":");
      delims.add(";");
      delims.add("<");
      delims.add(">");
      delims.add("(");
      delims.add(")");
      delims.add("[");
      delims.add("]");
      delims.add("~");

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
}
