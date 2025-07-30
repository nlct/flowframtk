// File          : FontFamilyPanel.java
// Description   : Panel for selecting font family
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

/**
 * Panel for selecting font family.
 * @author Nicola L C Talbot
 */

public class FontFamilyPanel extends JPanel implements ItemListener
{
   public FontFamilyPanel(JDRSelector selector, String[] availableFontNames)
   {
      selector_ = selector;

      fontNames = availableFontNames;

      JLabel fontFamilyLabel = getResources().createAppLabel("font.family");
      add(fontFamilyLabel);

      fontNamesBox = new JComboBox<String>(fontNames);
      fontFamilyLabel.setLabelFor(fontNamesBox);
      fontNamesBox.addItemListener(this);
      fontNamesBox.requestFocusInWindow();

      fontNamesBox.setToolTipText(
       getResources().getToolTipText("font.family"));
      add(fontNamesBox);

      latexFontFamily = new JComboBox<String>(
        new String[]
        {
            "\\rmfamily",
            "\\sffamily",
            "\\ttfamily"
        }
     );
      latexFontFamily.setToolTipText(
         getResources().getToolTipText("latex_font"));
      latexFontFamily.setEditable(true);

      add(latexFontFamily);
   }

   public void itemStateChanged(ItemEvent evt)
   {
      Object source = evt.getSource();

      if (evt.getStateChange() == ItemEvent.SELECTED)
      {
         if (source == fontNamesBox)
         {
            String name = getFontName();

            String lName = LaTeXFont.fromJavaFamily(name);

            latexFontFamily.setSelectedItem(lName);
         }

         selector_.repaintSample();
      }
   }


   public String getFontName()
   {
      return fontNames[fontNamesBox.getSelectedIndex()];
   }

   public void setFontName(String name)
   {
      for (int i = 0; i < fontNames.length; i++)
      {
         if (fontNames[i].equals(name))
         {
            fontNamesBox.setSelectedIndex(i);
            return;
         }
      }
   }

   public String getLaTeXFontFamily()
   {
      return (String)latexFontFamily.getSelectedItem();
   }

   public void setLaTeXFontFamily(String family)
   {
      latexFontFamily.setSelectedItem(family);
   }

   public void setDefaults()
   {
      setFontName(getResources().getMessage("font.default"));
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   private JDRSelector selector_;
   private String[] fontNames;
   private JLabel fontFamilyLabel;
   private JComboBox<String> fontNamesBox, latexFontFamily;

   public static final int LATEX_RM=0, LATEX_SF=1, LATEX_TT=2;
}
