// File          : TextPaintSelector.java
// Description   : Dialog for setting text paint
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
 * Dialog for setting text paint.
 * @author Nicola L C Talbot
 */

public class TextPaintSelector extends JDRSelector
{
   public TextPaintSelector(FlowframTk application)
   {
      super(application, 
            application.getResources().getString("textcolour.title"),
            false, true);

      application.enableHelpOnButton(help, "sec:textcolour");

      paintPanel = new PaintPanel(this);
      paintPanel.setBorder(BorderFactory.createLoweredBevelBorder());

      setToMain(paintPanel);

      Dimension dim = getSize();
      int width = dim.width;
      pack();
      dim = getSize();
      dim.width = width;
      setSize(dim);
      setLocationRelativeTo(application);
   }

   public String getSampleText()
   {
      if (textArea != null && !textArea.getText().equals(""))
      {
         return textArea.getText();
      }

      return super.getSampleText();
   }

   public JDRPaint getTextPaint()
   {
      return paintPanel.getPaint(getCanvasGraphics());
   }

   public void initialise()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      textArea = mainPanel.getSelectedTextual();
      paintPanel.setPaint(mainPanel.getSelectedTextPaint());
      super.initialise();
   }

   public void okay()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      mainPanel.setSelectedTextPaint(getTextPaint());
      super.okay();
   }

   public void setDefaults()
   {
      paintPanel.setPaint(
         new JDRColor(getCanvasGraphics(), Color.black));
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "LinePaintSelector:"+eol;

      return str+super.info();
   }

   private PaintPanel paintPanel;
   private JDRTextual textArea=null;
}
