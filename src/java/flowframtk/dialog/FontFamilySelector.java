// File          : FontFamilySelector.java
// Description   : Dialog for setting font family
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
 * Dialog for setting font family.
 * @author Nicola L C Talbot
 */

public class FontFamilySelector extends JDRSelector
{
   public FontFamilySelector(FlowframTk application)
   {
      super(application,application.getResources().getString("font.family"),
         false,true);

/*
      Dimension dim = getSize();
      dim.height=220;
      setSize(dim);
*/

      application.enableHelpOnButton(help, "sec:fontfamily");

      // font panel

      JDRFrame mainPanel = application_.getCurrentFrame();
      fontPanel = new FontFamilyPanel(this,
         application_.getFontFamilies());
      fontPanel.setBorder(BorderFactory.createLoweredBevelBorder());

      setToMain(fontPanel);
   }

   public void initialise()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      if (mainPanel != null)
      {
         text = mainPanel.getSelectedFont();
         setFontName(text.getFontFamily());
         setLaTeXFontFamily(text.getLaTeXFamily());
      }
      else
      {
         setFontName(application_.getCurrentFontFamily());
         setLaTeXFontFamily(application_.getCurrentLaTeXFontFamily());
      }
      super.initialise();
   }

   public void okay()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      mainPanel.setSelectedFontFamily(getFontName(),
         getLaTeXFontFamily());
      super.okay();
   }

   public String getSampleText()
   {
      if (text != null && text.getText().length() > 0)
      {
         return text.getText();
      }

      return super.getSampleText();
   }

   public void setFontName(String name)
   {
      fontPanel.setFontName(name);
   }

   public String getFontName()
   {
      return fontPanel.getFontName();
   }

   public JDRLength getFontSize()
   {
      return text == null ? 
         new JDRLength(getResources().getMessageDictionary(),
          10, JDRUnit.pt) : text.getFontSize();
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

   public void setLaTeXFontFamily(String name)
   {
      fontPanel.setLaTeXFontFamily(name);
   }

   public String getLaTeXFontFamily()
   {
      return fontPanel.getLaTeXFontFamily();
   }

   public void setDefaults()
   {
      fontPanel.setDefaults();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "FontFamilySelector:"+eol;
      str += "text: "+text+eol;

      return str+super.info();
   }

   private FontFamilyPanel fontPanel;
   private JDRTextual text=null;
}
