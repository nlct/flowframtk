// File          : FontSeriesSelector.java
// Description   : Dialog for setting font series
// Creation Date : 6th February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006 Nicola L.C. Talbot

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
 * Dialog for setting font series.
 * @author Nicola L C Talbot
 */

public class FontSeriesSelector extends JDRSelector
{
   public FontSeriesSelector(FlowframTk application)
   {
      super(application,application.getResources().getString("font.series"),
         false, true);

/*
      Dimension dim = getSize();
      dim.height=220;
      setSize(dim);
*/

      application.enableHelpOnButton(help, "fontseries");

      // font panel

      JDRFrame mainPanel = application_.getCurrentFrame();
      fontPanel = new FontSeriesPanel(this);
      fontPanel.setBorder(BorderFactory.createLoweredBevelBorder());

      setToMain(fontPanel);
   }

   public void initialise()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      if (mainPanel != null)
      {
         text = mainPanel.getSelectedFont();
         setFontSeries(text.getFontSeries());
         setLaTeXFontSeries(text.getLaTeXSeries());
      }
      else
      {
         setFontSeries(application_.getCurrentFontSeries());
         setLaTeXFontSeries(application_.getCurrentLaTeXFontSeries());
      }
      super.initialise();
   }

   public void okay()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      mainPanel.setSelectedFontSeries(getFontSeries(),
         getLaTeXFontSeries());
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

   public void setFontSeries(int series)
   {
      fontPanel.setFontSeries(series);
   }

   public int getFontSeries()
   {
      return fontPanel.getFontSeries();
   }

   public JDRLength getFontSize()
   {
      return text == null ? 
         new JDRLength(getResources().getMessageSystem(),
           10, JDRUnit.pt) : text.getFontSize();
   }

   public int getFontShape()
   {
      return text == null ? JDRFont.SHAPE_UPRIGHT : text.getFontShape();
   }

   public String getFontName()
   {
      return text == null ? "SansSerif" : text.getFontFamily();
   }

   public JDRPaint getTextPaint()
   {
      return text == null 
        ? application_.getCurrentTextPaint() : text.getTextPaint();
   }

   public void setLaTeXFontSeries(String series)
   {
      fontPanel.setLaTeXFontSeries(series);
   }

   public String getLaTeXFontSeries()
   {
      return fontPanel.getLaTeXFontSeries();
   }

   public void setDefaults()
   {
      fontPanel.setDefaults();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "FontSeriesSelector:"+eol;
      str += "text: "+text+eol;

      return str+super.info();
   }

   private FontSeriesPanel fontPanel;
   private JDRTextual text=null;
}
