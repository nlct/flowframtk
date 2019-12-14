// File          : FontSeriesPanel.java
// Description   : Panel for selecting font series
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
 * Panel for selecting font series.
 * @author Nicola L C Talbot
 */

public class FontSeriesPanel extends JPanel implements ItemListener
{
   public FontSeriesPanel(JDRSelector selector)
   {
      selector_ = selector;

      JLabel fontSeriesLabel = getResources().createAppLabel("font.series");
      add(fontSeriesLabel);

      fontSeries = new JComboBox<String>(
         new String[] {getResources().getString("font.series.medium"),
                       getResources().getString("font.series.bold")});
      fontSeriesLabel.setLabelFor(fontSeries);
      add(fontSeries);

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
      add(latexFontSeries);
   }

   public void itemStateChanged(ItemEvent evt)
   {
      Object source = evt.getSource();

      if (evt.getStateChange() == ItemEvent.SELECTED)
      {
         if (source == fontSeries)
         {
            int series = fontSeries.getSelectedIndex();
            latexFontSeries.setSelectedIndex(series);
         }

         selector_.repaintSample();
      }
   }

   public void setFontSeries(int series)
   {
      fontSeries.setSelectedIndex(series);
   }

   public int getFontSeries()
   {
      return fontSeries.getSelectedIndex();
   }

   public void setLaTeXFontSeries(String series)
   {
      latexFontSeries.setSelectedItem(series);
   }

   public String getLaTeXFontSeries()
   {
      return (String)latexFontSeries.getSelectedItem();
   }

   public void setDefaults()
   {
      setFontSeries(JDRFont.SERIES_MEDIUM);
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   private JDRSelector selector_;
   private JComboBox<String> fontSeries, latexFontSeries;
}
