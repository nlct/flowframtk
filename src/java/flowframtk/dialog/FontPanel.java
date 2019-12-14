// File          : FontPanel.java
// Description   : Panel for selecting font attributes
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

import com.dickimawbooks.jdr.io.PGF;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Panel for selecting font style.
 * @author Nicola L C Talbot
 */

public class FontPanel extends JScrollPane
   implements ItemListener,DocumentListener
{
   public FontPanel(JDRSelector selector, String[] availableFontNames)
   {
      super();

      selector_ = selector;
      latexFonts_ = new LaTeXFontBase(getResources().getMessageSystem(), 10);

      JPanel p = new JPanel();

      p.setLayout(new GridBagLayout());

      GridBagConstraints constraints = new GridBagConstraints();

      constraints.weightx = 100;
      constraints.weighty = 100;
      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.gridwidth = 1;
      constraints.gridheight= 1;
      constraints.anchor = GridBagConstraints.WEST;

      fontNames = availableFontNames;

      JLabel fontFamilyLabel = getResources().createAppLabel("font.family");
      p.add(fontFamilyLabel, constraints);

      fontNamesBox = new JComboBox<String>(fontNames);
      fontFamilyLabel.setLabelFor(fontNamesBox);
      fontNamesBox.addItemListener(this);
      fontNamesBox.requestFocusInWindow();

      fontNamesBox.setToolTipText(getResources().getToolTipText("font_family"));
      constraints.gridx++;
      p.add(fontNamesBox, constraints);

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
      constraints.gridx++;
      p.add(latexFontFamily, constraints);

      // font size 
      constraints.gridx = 0;
      constraints.gridy++;

      JLabel fontSizeLabel = getResources().createAppLabel("font.size");
      p.add(fontSizeLabel, constraints);

      fontSizePanel = getResources().createNonNegativeLengthPanel(
         selector_.getSampleTextPanel());
      fontSizePanel.getDocument().addDocumentListener(this);
      fontSizePanel.setValue(
         latexFonts_.getFontSize(LaTeXFontBase.NORMALSIZE), JDRUnit.pt);
      fontSizeLabel.setLabelFor(fontSizePanel.getTextField());
      fontSizePanel.getTextField().setColumns(3);

      constraints.gridx++;
      p.add(fontSizePanel, constraints);

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
      constraints.gridx++;
      p.add(latexFontSize, constraints);

      // font series

      constraints.gridx = 0;
      constraints.gridy++;

      JLabel fontSeriesLabel = getResources().createAppLabel("font.series");
      p.add(fontSeriesLabel, constraints);

      fontSeries = new JComboBox<String>(
         new String[] {getResources().getString("font.series.medium"),
                       getResources().getString("font.series.bold")});
      fontSeriesLabel.setLabelFor(fontSeries);
      constraints.gridx = 1;
      p.add(fontSeries, constraints);
      fontSeries.addItemListener(this);

      latexFontSeries = new JComboBox<String>(
          new String[]
          {
              "\\mdseries",
              "\\bfseries"
          }
      );
      latexFontSeries.setToolTipText(
         getResources().getToolTipText("latex_font_series"));
      latexFontSeries.setEditable(true);
      constraints.gridx = 2;
      p.add(latexFontSeries, constraints);

      // font shape

      constraints.gridx = 0;
      constraints.gridy = 3;

      JLabel fontShapeLabel = getResources().createAppLabel("font.shape");
      p.add(fontShapeLabel, constraints);

      fontShape = new JComboBox<String>(
         new String[] {getResources().getString("font.shape.upright"),
                       getResources().getString("font.shape.italic")});
      fontShapeLabel.setLabelFor(fontShape);
      constraints.gridx = 1;
      p.add(fontShape, constraints);
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
         getResources().getToolTipText("latex_font_shape"));
      latexFontShape.setEditable(true);
      constraints.gridx = 2;
      p.add(latexFontShape, constraints);

      constraints.gridx = 0;
      constraints.gridy = 4;
      constraints.gridwidth=2;
      constraints.fill = GridBagConstraints.HORIZONTAL;

      JTextArea latexFontMessage = getResources().createAppInfoArea(
         "font.latex_message");
      latexFontMessage.setColumns(20);

      p.add(latexFontMessage, constraints);

      constraints.gridx = 2;
      constraints.gridy = 4;
      constraints.gridwidth=1;

      pgfPanel = new FontAnchorPanel(selector);

      p.add(pgfPanel, constraints);

      setViewportView(p);
   }

   protected void setLaTeXFonts(LaTeXFontBase latexFonts)
   {
      latexFonts_ = latexFonts;

      fontSizePanel.setValue(
          latexFonts_.getFontSize(LaTeXFontBase.NORMALSIZE),
            JDRUnit.pt);
   }

   public void setNormalSize(int size)
   {
      fontSizePanel.setValue(size, JDRUnit.pt);
   }

   public void itemStateChanged(ItemEvent evt)
   {
      Object source = evt.getSource();

      if (evt.getStateChange() == ItemEvent.SELECTED)
      {
         if (source == fontNamesBox)
         {
            String name = fontNames[fontNamesBox.getSelectedIndex()];

            latexFontFamily.setSelectedItem(
               LaTeXFont.fromJavaFamily(name));
         }
         else if (source == fontSeries)
         {
            int series = fontSeries.getSelectedIndex();
            latexFontSeries.setSelectedIndex(series);
         }
         else if (source == fontShape)
         {
            int shape = fontShape.getSelectedIndex();
            latexFontShape.setSelectedIndex(shape);
         }

         selector_.repaintSample();
      }
   }

   public void updateLaTeXSize()
   {
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

      if (latexFontSize != null) latexFontSize.setSelectedItem(str);
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

   public JDRLength getFontSize()
   {
      return fontSizePanel.getLength();
   }

   public void setFontSize(JDRLength size)
   {
      fontSizePanel.setLength(size);
   }

   public int getFontSeries()
   {
      return fontSeries.getSelectedIndex();
   }

   public int getFontShape()
   {
      return fontShape.getSelectedIndex();
   }

   public String getLaTeXFontFamily()
   {
      return (String)latexFontFamily.getSelectedItem();
   }

   public void setLaTeXFontFamily(String family)
   {
      latexFontFamily.setSelectedItem(family);
   }

   public void setLaTeXFontSize(String size)
   {
      latexFontSize.setSelectedItem(size);
   }

   public void setLaTeXFontShape(String shape)
   {
      latexFontShape.setSelectedItem(shape);
   }

   public void setLaTeXFontSeries(String series)
   {
      latexFontSeries.setSelectedItem(series);
   }

   public void setHalign(int align)
   {
      pgfPanel.setHalign(align);
   }

   public void setValign(int align)
   {
      pgfPanel.setValign(align);
   }

   public int getHalign()
   {
      return pgfPanel.getHalign();
   }

   public int getValign()
   {
      return pgfPanel.getValign();
   }

   public String getLaTeXFontShape()
   {
      return (String)latexFontShape.getSelectedItem();
   }

   public String getLaTeXFontSeries()
   {
      return (String)latexFontSeries.getSelectedItem();
   }

   public String getLaTeXFontSize()
   {
      return (String)latexFontSize.getSelectedItem();
   }

   public void setFontSeries(int series)
   {
      fontSeries.setSelectedIndex(series);
   }

   public void setFontShape(int shape)
   {
      fontShape.setSelectedIndex(shape);
   }

   public void setDefaults()
   {
      setFontName(getResources().getString("font.default"));
      setNormalSize(
          (int)latexFonts_.getFontSize(LaTeXFontBase.NORMALSIZE));
      setFontSeries(JDRFont.SERIES_MEDIUM);
      setFontShape(JDRFont.SHAPE_UPRIGHT);

      pgfPanel.setDefaults();
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   // font names
   private String[] fontNames;
   private JComboBox<String> fontNamesBox, latexFontFamily;
   public static final int LATEX_RM=0, LATEX_SF=1, LATEX_TT=2;

   // font size
   private NonNegativeLengthPanel fontSizePanel;
   private JComboBox<String> latexFontSize;

   // font series
   private JComboBox<String> fontSeries, latexFontSeries;

   // font shape
   private JComboBox<String> fontShape, latexFontShape;

   private LaTeXFontBase latexFonts_;

   private JDRSelector selector_;

   private FontAnchorPanel pgfPanel;
}
