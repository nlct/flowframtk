// File          : LineWidthPanel.java
// Description   : Panel to set line width
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

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;
/**
 * Panel for setting line width.
 * @author Nicola L C Talbot
 */
public class LineWidthPanel extends JPanel
{
   public LineWidthPanel(JDRSelector selector)
   {
      super();

      selector_ = selector;

      // JDRLine thickness

      lineWidthPanel = getResources().createNonNegativeLengthPanel(
         "linestyle.thickness", selector_.getSamplePathPanel());
      lineWidthPanel.getTextField().setColumns(2);
      lineWidthPanel.getTextField().requestFocusInWindow();

      add(lineWidthPanel);

   }

   public JDRBasicStroke getStroke()
   {
      JDRFrame frame = selector_.application_.getCurrentFrame();

      JDRBasicStroke stroke;

      if (frame == null)
      {
         stroke = (JDRBasicStroke)selector_.getStroke().clone();
      }
      else
      {
         stroke = (JDRBasicStroke)frame.getSelectedStroke().clone();
      }

      stroke.setPenWidth(getPenWidth());

      return stroke;
   }

   public void setStroke(JDRBasicStroke stroke)
   {
      setPenWidth(stroke.getPenWidth());
   }

   public JDRLength getPenWidth()
   {
      return lineWidthPanel.getLength();
   }

   public void setPenWidth(CanvasGraphics cg, double width)
   {
      setPenWidth(width, cg.getStorageUnit());
   }

   public void setPenWidth(double width, JDRUnit unit)
   {
      lineWidthPanel.setValue(width, unit);
   }

   public void setPenWidth(JDRLength penWidth)
   {
      lineWidthPanel.setLength(penWidth);
   }

   public void setDefaults()
   {
      setPenWidth(1.0, JDRUnit.bp);
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   // pen thickness
   private NonNegativeLengthPanel lineWidthPanel;

   private JDRSelector selector_;
}
