// File          : FontSelector.java
// Description   : Dialog for setting font attributes
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
 * Dialog for setting font attributes.
 * @author Nicola L C Talbot
 */

public class FontSelector extends JDRSelector
{
   public FontSelector(FlowframTk application)
   {
      super(application, application.getResources().getMessage("font.title"),
         false, true, true, "sec:textstyle");

      // font panel

      JDRFrame mainPanel = application_.getCurrentFrame();
      fontPanel = new FontPanel(this, application_.getFontFamilies());
      fontPanel.setBorder(BorderFactory.createLoweredBevelBorder());

      setToMain(fontPanel);

      setDefaults();
   }

   public void initialise()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      if (mainPanel != null)
      {
         textArea = mainPanel.getSelectedFont();
         setLaTeXFonts(mainPanel.getLaTeXFonts());
         setFontName(textArea.getFontFamily());
         setFontSize(textArea.getFontSize());
         setFontSeries(textArea.getFontSeries());
         setFontShape(textArea.getFontShape());
         setLaTeXFontFamily(textArea.getLaTeXFamily());
         setLaTeXFontSize(textArea.getLaTeXSize());
         setLaTeXFontShape(textArea.getLaTeXShape());
         setLaTeXFontSeries(textArea.getLaTeXSeries());
         setValign(textArea.getVAlign());
         setHalign(textArea.getHAlign());
      }
      else
      {
         setLaTeXFonts(application_.getCurrentLaTeXFontBase());
         setFontName(application_.getCurrentFontFamily());
         setFontSize(application_.getCurrentFontSize());
         setFontSeries(application_.getCurrentFontSeries());
         setFontShape(application_.getCurrentFontShape());
         setLaTeXFontFamily(application_.getCurrentLaTeXFontFamily());
         setLaTeXFontSize(application_.getCurrentLaTeXFontSize());
         setLaTeXFontShape(application_.getCurrentLaTeXFontShape());
         setLaTeXFontSeries(application_.getCurrentLaTeXFontSeries());
         setValign(application_.getCurrentPGFVAlign());
         setHalign(application_.getCurrentPGFHAlign());
         fontPanel.setLaTeXFonts(mainPanel.getLaTeXFonts());
      }
      super.initialise();
   }

   public void okay()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      Graphics2D g = (Graphics2D)mainPanel.getGraphics();

      CanvasGraphics cg = mainPanel.getCanvasGraphics();

      JDRText text = new JDRText(cg);

      cg.setGraphicsDevice(g);

      try
      {
         text.setFont(getFontName(),
                      getFontSeries(),
                      getFontShape(),
                      getFontSize());

         text.setAlign(getHalign(), getValign());
      }
      finally
      {
         cg.setGraphicsDevice(null);
         g.dispose();
      }

      text.setLaTeXFont(getLaTeXFontFamily(),
                    getLaTeXFontSize(),
                    getLaTeXFontSeries(),
                    getLaTeXFontShape());

      mainPanel.setSelectedFont(text);
      super.okay();
   }

   public String getSampleText()
   {
      if (textArea != null && !textArea.getText().equals(""))
      {
         return textArea.getText();
      }

      return super.getSampleText();
   }

   public String getFontName()
   {
      if (fontPanel == null)
      {
         return "Serif";
      }

      return fontPanel.getFontName();
   }

   public void setFontName(String name)
   {
      fontPanel.setFontName(name);
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

   public void setFontSize(JDRLength size)
   {
      fontPanel.setFontSize(size);
   }

   public int getFontSeries()
   {
      if (fontPanel == null)
      {
         return JDRFont.SERIES_MEDIUM;
      }

      return fontPanel.getFontSeries();
   }

   public int getFontShape()
   {
      if (fontPanel == null)
      {
         return JDRFont.SHAPE_UPRIGHT;
      }

      return fontPanel.getFontShape();
   }

   public String getLaTeXFontFamily()
   {
      return fontPanel.getLaTeXFontFamily();
   }

   public void setLaTeXFontFamily(String family)
   {
      fontPanel.setLaTeXFontFamily(family);
   }

   public void setLaTeXFontSize(String size)
   {
      fontPanel.setLaTeXFontSize(size);
   }

   public void setLaTeXFontShape(String shape)
   {
      fontPanel.setLaTeXFontShape(shape);
   }

   public void setLaTeXFontSeries(String series)
   {
      fontPanel.setLaTeXFontSeries(series);
   }

   public String getLaTeXFontShape()
   {
      return fontPanel.getLaTeXFontShape();
   }

   public String getLaTeXFontSeries()
   {
      return fontPanel.getLaTeXFontSeries();
   }

   public String getLaTeXFontSize()
   {
      return fontPanel.getLaTeXFontSize();
   }

   public void setFontSeries(int series)
   {
      fontPanel.setFontSeries(series);
   }

   public void setFontShape(int shape)
   {
      fontPanel.setFontShape(shape);
   }

   public int getHalign()
   {
      if (fontPanel == null)
      {
         return JDRText.PGF_HALIGN_LEFT;
      }

      return fontPanel.getHalign();
   }

   public int getValign()
   {
      if (fontPanel == null)
      {
         return JDRText.PGF_VALIGN_BASE;
      }

      return fontPanel.getValign();
   }

   public void setHalign(int align)
   {
      fontPanel.setHalign(align);
   }

   public void setValign(int align)
   {
      fontPanel.setValign(align);
   }

   public void setDefaults()
   {
      fontPanel.setDefaults();
   }

   public void setLaTeXFonts(LaTeXFontBase latexFonts)
   {
      fontPanel.setLaTeXFonts(latexFonts);
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "FontSelector:"+eol;
      str += "text: "+textArea+eol;

      return str+super.info();
   }

   private FontPanel fontPanel;
   private JDRTextual textArea=null;
}
