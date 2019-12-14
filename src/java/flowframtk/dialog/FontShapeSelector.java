// File          : FontShapeSelector.java
// Description   : Dialog for setting font shape
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
 * Dialog for setting font shape.
 * @author Nicola L C Talbot
 */

public class FontShapeSelector extends JDRSelector
{
   public FontShapeSelector(FlowframTk application)
   {
      super(application,application.getResources().getString("font.shape"),
         false, true);

/*
      Dimension dim = getSize();
      dim.height=220;
      setSize(dim);
*/

      application.enableHelpOnButton(help, "sec:fontshape");

      // font panel

      JDRFrame mainPanel = application_.getCurrentFrame();
      fontPanel = new FontShapePanel(this);
      fontPanel.setBorder(BorderFactory.createLoweredBevelBorder());
      setToMain(fontPanel);
   }

   public void initialise()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      if (mainPanel != null)
      {
         text = mainPanel.getSelectedFont();
         setFontShape(text.getFontShape());
         setLaTeXFontShape(text.getLaTeXShape());
      }
      else
      {
         setFontShape(application_.getCurrentFontShape());
         setLaTeXFontShape(application_.getCurrentLaTeXFontShape());
      }
      super.initialise();
   }

   public void okay()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      mainPanel.setSelectedFontShape(getFontShape(),
         getLaTeXFontShape());
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

   public void setFontShape(int shape)
   {
      fontPanel.setFontShape(shape);
   }

   public int getFontShape()
   {
      return fontPanel.getFontShape();
   }

   public JDRLength getFontSize()
   {
      return text == null ? 
         new JDRLength(getResources().getMessageDictionary(),
           10, JDRUnit.pt) : text.getFontSize();
   }

   public int getFontSeries()
   {
      return text == null ? JDRFont.SERIES_MEDIUM : text.getFontSeries();
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

   public void setLaTeXFontShape(String shape)
   {
      fontPanel.setLaTeXFontShape(shape);
   }

   public String getLaTeXFontShape()
   {
      return fontPanel.getLaTeXFontShape();
   }

   public void setDefaults()
   {
      fontPanel.setDefaults();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "FontShapeSelector:"+eol;
      str += "text: "+text+eol;

      return str+super.info();
   }

   private FontShapePanel fontPanel;
   private JDRTextual text=null;
}
