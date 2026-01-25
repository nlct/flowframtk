/*
    Copyright (C) 2026 Nicola L.C. Talbot

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
 * Dialog for setting outline text fill paint.
 * @author Nicola L C Talbot
 */

public class OutlineFillPaintSelector extends JDRSelector
{
   public OutlineFillPaintSelector(FlowframTk application)
   {
      super(application, 
            application.getResources().getMessage("textpaintselector.title"),
            false, true, "sec:textpaint");

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

   @Override
   public boolean isTextOutline()
   {
      return true;
   }

   @Override
   public String getSampleText()
   {
      if (textual != null && !textual.getText().equals(""))
      {
         return textual.getText();
      }

      return super.getSampleText();
   }

   @Override
   public JDRPaint getOutlineFillPaint()
   {
      return paintPanel.getPaint(getCanvasGraphics());
   }

   @Override
   public void initialise()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      textual = mainPanel.getSelectedTextual();

      JDRPaint paint = mainPanel.getSelectedOutlineFillPaint();

      if (paint == null)
      {
         paint = new JDRTransparent(getCanvasGraphics());
      }

      paintPanel.setPaint(paint);

      super.initialise();
   }

   @Override
   public void okay()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      mainPanel.setSelectedOutlineFillPaint(getOutlineFillPaint());
      super.okay();
   }

   @Override
   public void setDefaults()
   {
      paintPanel.setPaint(
         new JDRColor(getCanvasGraphics(), Color.black));
   }

   @Override
   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "LinePaintSelector:"+eol;

      return str+super.info();
   }

   private PaintPanel paintPanel;
   private JDRTextual textual=null;
}
