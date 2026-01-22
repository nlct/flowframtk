/*
    Copyright (C) 2026 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package com.dickimawbooks.jdr.io;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.dickimawbooks.texparserlib.*;
import com.dickimawbooks.texparserlib.html.L2HStringConverter;

import com.dickimawbooks.jdr.CanvasGraphics;

public class JDRTeXApp extends TeXAppAdapter
{
   public JDRTeXApp(CanvasGraphics cg)
   {
      super();

      this.canvasGraphics = cg;
   }

   public String convertToString(String source)
   {
      String result = source;

      if (l2hConverter == null)
      {
         l2hConverter = new L2HStringConverter(this);
         l2hConverter.setUseMathJax(false);
         l2hConverter.setSupportUnicodeScript(true);
         l2hConverter.setMathModeSupportUnicodeScript(true);

         l2hParser = new TeXParser(l2hConverter);
      }

      try
      {
         result = l2hConverter.convert(source, false);

         result = HTML_TAG_PATTERN.matcher(result).replaceAll("");
      }
      catch (PatternSyntaxException e)
      {
         canvasGraphics.debugMessage(e);
      }
      catch (IOException e)
      {
         canvasGraphics.warning(e);
      }

      return result;
   }

   @Override
   public String getMessage(String label, Object... params)
   {
      return getMessageDictionary().getMessageWithFallback(
        label, null, params);
   }

   @Override
   public String getApplicationName()
   {
      return getMessageDictionary().getApplicationName();
   }

   @Override
   public String getApplicationVersion()
   {
      return getMessageDictionary().getApplicationVersion();
   }

   public JDRMessage getMessageSystem()
   {
      return canvasGraphics.getMessageSystem();
   }

   public JDRMessageDictionary getMessageDictionary()
   {
      return canvasGraphics.getMessageDictionary();
   }

   CanvasGraphics canvasGraphics;

   L2HStringConverter l2hConverter;
   TeXParser l2hParser;

   public static final Pattern HTML_TAG_PATTERN
     = Pattern.compile("(<style>.+?</style>|<.+?\\/?>|<\\/.+?>)");
}
