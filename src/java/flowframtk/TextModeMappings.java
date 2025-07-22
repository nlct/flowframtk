// File          : TextModeMappings.java
// Purpose       : Text mode mappings of special characters
// Creation Date : 2014-04-25
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

/*
    Copyright (C) 2014-2025 Nicola L.C. Talbot

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

package com.dickimawbooks.flowframtk;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

import com.dickimawbooks.jdrresources.*;

public class TextModeMappings extends TeXMappings
{
   public TextModeMappings(JDRResources resources)
   {
      super(resources, resources.getMessage("symbol.mode.text"));
   }

   public static TextModeMappings createDefaultMappings(JDRResources resources)
   {
      TextModeMappings mappings = new TextModeMappings(resources);

      mappings.put('\\', "\\textbackslash{}");
      mappings.put('#', "\\#");
      mappings.put('%', "\\%");
      mappings.put('_', "\\_");
      mappings.put('$', "\\$");
      mappings.put('&', "\\&");
      mappings.put('{', "\\{");
      mappings.put('}', "\\}");
      mappings.put('~', "\\textasciitilde{}");
      mappings.put('^', "\\textasciicircum{}");

      mappings.put(0x003C, new TeXLookup("\\textless{}", "none"));
      mappings.put(0x003E, new TeXLookup("\\textgreater{}", "none"));
      mappings.put(0x007C, new TeXLookup("\\textbar{}", "none"));
      mappings.put(0x00A2, new TeXLookup("\\textcent{}", "textcomp"));
      mappings.put(0x00A3, new TeXLookup("\\pounds{}", "none"));
      mappings.put(0x00A7, new TeXLookup("\\S{}", "none"));
      mappings.put(0x00A9, new TeXLookup("\\copyright{}", "none"));
      mappings.put(0x00AE, new TeXLookup("\\textregistered{}", "none"));
      mappings.put(0x00B0, new TeXLookup("\\textdegree{}", "textcomp"));
      mappings.put(0x00B1, new TeXLookup("\\textpm{}", "textcomp"));
      mappings.put(0x00B2, new TeXLookup("\\texttwosuperior{}", "textcomp"));
      mappings.put(0x00B3, new TeXLookup("\\textthreesuperior{}", "textcomp"));
      mappings.put(0x00B6, new TeXLookup("\\P{}", "none"));
      mappings.put(0x00B9, new TeXLookup("\\textonesuperior{}", "textcomp"));
      mappings.put(0x00BC, new TeXLookup("\\sfrac{1}{4}", "xfrac"));
      mappings.put(0x00BD, new TeXLookup("\\sfrac{1}{2}", "xfrac"));
      mappings.put(0x00BE, new TeXLookup("\\sfrac{3}{4}", "xfrac"));

      mappings.put(0x00D7, new TeXLookup("\\texttimes{}", "textcomp"));
      mappings.put(0x00F7, new TeXLookup("\\textdiv{}", "textcomp"));

      mappings.put(0x2018, new TeXLookup("\\textquoteleft{}", "none"));
      mappings.put(0x2019, new TeXLookup("\\textquoteright{}", "none"));
      mappings.put(0x201C, new TeXLookup("\\textquotedblleft{}", "none"));
      mappings.put(0x201D, new TeXLookup("\\textquotedblright{}", "none"));

      mappings.put(0x2020, new TeXLookup("\\dag{}", "none"));
      mappings.put(0x2021, new TeXLookup("\\ddag{}", "none"));

      return mappings;
   }

   public static TextModeMappings load(JDRResources resources, File file)
      throws IOException
   {
      TextModeMappings mappings = new TextModeMappings(resources);

      mappings.read(file);

      return mappings;
   }

}
