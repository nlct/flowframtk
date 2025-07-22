// File          : FontSizeSelector.java
// Description   : Dialog for setting font size
// Creation Date : 6th February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2025 Nicola L.C. Talbot

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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for setting font size.
 * @author Nicola L C Talbot
 */

public class FontSizeSelector extends JDRSelector
{
   public FontSizeSelector(FlowframTk application)
   {
      super(application,application.getResources().getMessage("font.size"),
         false, true, "sec:fontsize");

/*
      Dimension dim = getSize();
      dim.height=220;
      setSize(dim);
*/


      // font panel

      JDRFrame mainPanel = application_.getCurrentFrame();
      fontPanel = new FontSizePanel(this);
      fontPanel.setBorder(BorderFactory.createLoweredBevelBorder());
      setToMain(fontPanel);
   }

   public void initialise()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      if (mainPanel != null)
      {
         text = mainPanel.getSelectedFont();
         setLaTeXFonts(mainPanel.getLaTeXFonts());
         setFontSize(text.getFontSize());
         setLaTeXFontSize(text.getLaTeXSize());
      }
      else
      {
         setLaTeXFonts(application_.getCurrentLaTeXFontBase());
         setFontSize(application_.getCurrentFontSize());
         setLaTeXFontSize(application_.getCurrentLaTeXFontSize());
      }
      super.initialise();
   }

   public void okay()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      mainPanel.setSelectedFontSize(getFontSize(),getLaTeXFontSize());
      super.okay();
   }

   public String getSampleText()
   {
      if (text != null && !text.getText().equals(""))
      {
         return text.getText();
      }

      return super.getSampleText();
   }

   public void setFontSize(JDRLength size)
   {
      fontPanel.setFontSize(size);
   }

   public JDRLength getFontSize()
   {
      if (fontPanel == null)
      {
         return new JDRLength(getResources().getMessageDictionary(),
            10, JDRUnit.pt);
      }

      return fontPanel.getFontSize();
   }

   public String getFontName()
   {
      return text == null ? "SansSerif" : text.getFontFamily();
   }

   public int getFontShape()
   {
      return text == null ? 0 : text.getFontShape();
   }

   public int getFontSeries()
   {
      return text == null ? 0 : text.getFontSeries();
   }

   public JDRPaint getTextPaint()
   {
      return text == null ? 
             application_.getCurrentTextPaint() : text.getTextPaint();
   }

   public void setLaTeXFontSize(String name)
   {
      fontPanel.setLaTeXFontSize(name);
   }

   public String getLaTeXFontSize()
   {
      return fontPanel.getLaTeXFontSize();
   }

   public void setLaTeXFonts(LaTeXFontBase latexFonts)
   {
      fontPanel.setLaTeXFonts(latexFonts);
   }

   public void setDefaults()
   {
      fontPanel.setDefaults();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "FontSizeSelector:"+eol;
      str += "text: "+text+eol;

      return str+super.info();
   }

   private FontSizePanel fontPanel;
   private JDRTextual text=null;
}
