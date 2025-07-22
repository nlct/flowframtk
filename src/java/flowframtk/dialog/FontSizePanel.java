// File          : FontSizePanel.java
// Description   : Panel for selecting font size
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
import com.dickimawbooks.jdr.io.PGF;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Panel for selecting marker style.
 * @author Nicola L C Talbot
 */

public class FontSizePanel extends JPanel implements DocumentListener
{
   public FontSizePanel(JDRSelector selector)
   {
      super();

      selector_ = selector;
      latexFonts_ = new LaTeXFontBase(getResources().getMessageSystem(), 10);

      JLabel fontSizeLabel = getResources().createAppLabel("font.size");
      add(fontSizeLabel);

      fontSizePanel = getResources().createNonNegativeLengthPanel(
         selector_.getSampleTextPanel());
      fontSizePanel.getDocument().addDocumentListener(this);
      fontSizePanel.setValue(
         latexFonts_.getFontSize(LaTeXFontBase.NORMALSIZE), JDRUnit.pt);
      fontSizeLabel.setLabelFor(fontSizePanel.getTextField());
      fontSizePanel.getTextField().setColumns(3);

      add(fontSizePanel);

      latexFontSize = new JComboBox<String>(
         new String[]
         {
            "\\tiny",
            "\\scriptsize",
            "\\footnotesize",
            "\\small",
            "\\normalsize",
            "\\large",
            "\\Large",
            "\\LARGE",
            "\\huge",
            "\\Huge",
            "\\veryHuge",
            "\\VeryHuge",
            "\\VERYHuge"
         }
      );
      latexFontSize.setToolTipText(
         getResources().getToolTipText("latex_font_size"));
      latexFontSize.setEditable(true);
      add(latexFontSize);
   }

   public void setLaTeXFonts(LaTeXFontBase latexFonts)
   {
      latexFonts_ = latexFonts;

      fontSizePanel.setValue(
            latexFonts_.getFontSize(LaTeXFontBase.NORMALSIZE),
            JDRUnit.pt);
   }

   public void setNormalSize(JDRLength size)
   {
      fontSizePanel.setLength(size);
   }

   public void updateLaTeXSize()
   {
      if (latexFontSize == null) return;

      String str;

      if (selector_.getApplication().useRelativeFontDeclarations())
      {
         str = latexFonts_.getLaTeXCmd(fontSizePanel.getLength());
      }
      else
      {
         String texHeight = PGF.length(fontSizePanel.getLength());
         str = "\\fontsize{"+texHeight+"}{" +texHeight+"}\\selectfont";
      }

      latexFontSize.setSelectedItem(str);
   }

   public void insertUpdate(DocumentEvent e)
   {
      updateLaTeXSize();
   }

   public void removeUpdate(DocumentEvent e)
   {
      updateLaTeXSize();
   }

   public void changedUpdate(DocumentEvent e)
   {
      updateLaTeXSize();
   }

   public JDRLength getFontSize()
   {
      return fontSizePanel.getLength();
   }

   public void setFontSize(JDRLength size)
   {
      fontSizePanel.setLength(size);
   }

   public void setLaTeXFontSize(String size)
   {
      latexFontSize.setSelectedItem(size);
   }

   public String getLaTeXFontSize()
   {
      return (String)latexFontSize.getSelectedItem();
   }

   public void setDefaults()
   {
      setNormalSize(
          new JDRLength(getResources().getMessageSystem(),
          latexFonts_.getFontSize(LaTeXFontBase.NORMALSIZE),
          JDRUnit.pt));
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   private JDRSelector selector_;

   // font size
   private NonNegativeLengthPanel fontSizePanel;
   private JComboBox<String> latexFontSize;

   private LaTeXFontBase latexFonts_;
}
