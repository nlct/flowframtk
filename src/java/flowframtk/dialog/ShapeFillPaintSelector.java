// File          : ShapeFillPaintSelector.java
// Description   : Dialog for setting fill paint
// Creation Date : 6th February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2026 Nicola L.C. Talbot

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
 * Dialog for setting fill paint for shapes.
 * @author Nicola L C Talbot
 */

public class ShapeFillPaintSelector extends JDRSelector
{
   public ShapeFillPaintSelector(FlowframTk application)
   {
      super(application, 
         application.getResources().getMessage("fillpaintselector.title"),
      true, false, "sec:fillpaint");

      paintPanel = new PaintPanel(this);
      paintPanel.setBorder(BorderFactory.createLoweredBevelBorder());

      setToMain(paintPanel);

/*
      Dimension dim = getSize();
      int width = dim.width;
      pack();
      dim = getSize();
      dim.width = width;
      setSize(dim);
      setLocationRelativeTo(application);
*/
   }

   @Override
   public JDRPaint getShapeFillPaint()
   {
      return paintPanel.getPaint(getCanvasGraphics());
   }

   @Override
   public void initialise()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      paintPanel.setPaint(mainPanel.getSelectedShapeFillPaint());
      super.initialise();
   }

   @Override
   public void okay()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      mainPanel.setSelectedShapeFillPaint(getShapeFillPaint());
      super.okay();
   }

   @Override
   public void setDefaults()
   {
      paintPanel.setPaint(
         new JDRTransparent(application_.getDefaultCanvasGraphics()));
   }

   @Override
   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "ShapeFillPaintSelector:"+eol;

      return str+super.info();
   }

   private PaintPanel paintPanel;
}
