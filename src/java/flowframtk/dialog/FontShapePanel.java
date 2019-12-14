// File          : FontShapePanel.java
// Description   : Panel for selecting font shape
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

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

/**
 * Panel for selecting font shape.
 * @author Nicola L C Talbot
 */

public class FontShapePanel extends JPanel implements ItemListener
{
   public FontShapePanel(JDRSelector selector)
   {
      selector_ = selector;

      JLabel fontShapeLabel = getResources().createAppLabel("font.shape");
      add(fontShapeLabel);

      fontShape = new JComboBox<String>(
         new String[] {getResources().getString("font.shape.upright"),
                       getResources().getString("font.shape.italic")});
      fontShapeLabel.setLabelFor(fontShape);
      add(fontShape);

      fontShape.addItemListener(this);

      latexFontShape = new JComboBox<String>(
         new String[]
         {
            "\\upshape",
            "\\em",
            "\\itshape",
            "\\slshape",
            "\\scshape"
         }
      );
      latexFontShape.setToolTipText(
         getResources().getToolTipText("tooltip.latex_font_shape"));
      latexFontShape.setEditable(true);
      add(latexFontShape);
   }

   public void itemStateChanged(ItemEvent evt)
   {
      Object source = evt.getSource();

      if (evt.getStateChange() == ItemEvent.SELECTED)
      {
         if (source == fontShape)
         {
            int shape = fontShape.getSelectedIndex();
            latexFontShape.setSelectedIndex(shape);
         }

         selector_.repaintSample();
      }
   }

   public void setFontShape(int shape)
   {
      fontShape.setSelectedIndex(shape);
   }

   public int getFontShape()
   {
      return fontShape.getSelectedIndex();
   }

   public void setLaTeXFontShape(String shape)
   {
      latexFontShape.setSelectedItem(shape);
   }

   public String getLaTeXFontShape()
   {
      return (String)latexFontShape.getSelectedItem();
   }

   public void setDefaults()
   {
      setFontShape(JDRFont.SHAPE_UPRIGHT);
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   private JDRSelector selector_;
   private JComboBox<String> fontShape, latexFontShape;
}
