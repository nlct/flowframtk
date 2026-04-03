/*
    Copyright (C) 2025 Nicola L.C. Talbot

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
package com.dickimawbooks.flowframtk.dialog;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class TagDocument extends PlainDocument
{
   public TagDocument()
   {
      this(true);
   }

   public TagDocument(boolean allowSpaces)
   {
      super();

      this.allowSpaces = allowSpaces;
   }

   public void insertString(int offs, String str, AttributeSet a)
      throws BadLocationException
   {
      if (str == null) return;

      Pattern p = (allowSpaces ? PATTERN_ALLOW_SPACES : PATTERN_NO_SPACES);

      Matcher m = p.matcher(str);

      str = m.replaceAll("");

      if (!str.isEmpty())
      {
         super.insertString(offs, str, a);
      }
   }

   boolean allowSpaces;

   static final Pattern PATTERN_ALLOW_SPACES =
     Pattern.compile("[^\\+\\-\\|\\./\\p{IsAlphabetic}\\p{IsDigit} ]");

   static final Pattern PATTERN_NO_SPACES =
     Pattern.compile("[^\\+\\-\\|\\./\\p{IsAlphabetic}\\p{IsDigit}]");
}
