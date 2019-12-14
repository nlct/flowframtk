// File          : StyleSelector.java
// Description   : Dialog for selecting path and text styles
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
 * Dialog for selecting path and text styles.
 * @author Nicola L C Talbot
 */

public class StyleSelector extends JDRSelector
{
   public StyleSelector(FlowframTk application)
   {
      super(application, application.getResources().getString("styles.title"),
            true, true, true);

      application.enableHelpOnButton(help, "sec:styles");

      tabbedPane = new JTabbedPane();

      setToMain(tabbedPane);

      // JDRLine colour selection panel

      linePanel = new PaintPanel(this);

      tabbedPane.addTab(
         getResources().getString("linecolour.label"), null, linePanel,
         getResources().getString("tooltip.line_colour"));

      linePanel.setBorder(BorderFactory.createLoweredBevelBorder());
      tabbedPane.setMnemonicAt(0,
         getResources().getChar("linecolour.mnemonic"));

      // Fill colour selection panel

      fillPanel = new PaintPanel(this);

      tabbedPane.addTab(getResources().getString("fillcolour.label"),
         null, fillPanel,
         getResources().getString("tooltip.fill_colour"));

      fillPanel.setBorder(BorderFactory.createLoweredBevelBorder());
      tabbedPane.setMnemonicAt(1,
         getResources().getChar("fillcolour.mnemonic"));

      // line style panel
      linestylePanel = new LineStylePanel(this);

      tabbedPane.addTab(getResources().getString("linestyle.label"),
         null, linestylePanel,
         getResources().getString("tooltip.line_style"));

      linestylePanel.setBorder(
         BorderFactory.createLoweredBevelBorder());
      tabbedPane.setMnemonicAt(2,
         getResources().getChar("linestyle.mnemonic"));

      // JDRText colour selection panel
      textPanel = new PaintPanel(this);

      tabbedPane.addTab(getResources().getString("textcolour.label"),
         null, textPanel,
         getResources().getString("tooltip.text.colour"));

      textPanel.setBorder(BorderFactory.createLoweredBevelBorder());
      tabbedPane.setMnemonicAt(3,
         getResources().getChar("textcolour.mnemonic"));

      // font panel

      JDRFrame mainPanel = application_.getCurrentFrame();
      fontPanel = new FontPanel(this, application_.getFontFamilies());
      tabbedPane.addTab(getResources().getString("font.label"), null,
         fontPanel, getResources().getString("tooltip.font"));

      fontPanel.setBorder(BorderFactory.createLoweredBevelBorder());

      tabbedPane.setMnemonicAt(4,
         getResources().getChar("font.mnemonic"));

      setDefaults();

      pack();
      Dimension dim = getSize();
      dim.height = 600;
      setSize(dim);
      setLocationRelativeTo(application_);
   }

   public void display()
   {
      fontPanel.setLaTeXFonts(
         application_.getCurrentLaTeXFontBase());

      setLinePaint(application_.getCurrentLinePaint());
      setFillPaint(application_.getCurrentFillPaint());
      setTextPaint(application_.getCurrentTextPaint());
      setStroke(application_.getCurrentStroke());

      setFontName(application_.getCurrentFontFamily());
      setFontSize(application_.getCurrentFontSize());
      setFontShape(application_.getCurrentFontShape());
      setFontSeries(application_.getCurrentFontSeries());

      setLaTeXFontFamily(application_.getCurrentLaTeXFontFamily());
      setLaTeXFontSeries(application_.getCurrentLaTeXFontSeries());
      setLaTeXFontShape(application_.getCurrentLaTeXFontShape());
      setLaTeXFontSize(application_.getCurrentLaTeXFontSize());

      setHalign(application_.getCurrentPGFHAlign());
      setValign(application_.getCurrentPGFVAlign());
      super.initialise();
   }

   public void okay()
   {
      int joinStyle = linestylePanel.getJoinStyle();

      if (joinStyle == BasicStroke.JOIN_MITER
        && linestylePanel.getEnteredMitreLimit() < 1.0)
      {
         getResources().error(this,
            getResources().getString("error.invalid_mitre_limit"));
         return;
      }

      application_.setCurrentSettings(getLinePaint(),
         getFillPaint(), getTextPaint(),
         getStroke(),
         getFontName(),
         getFontSize(),
         getFontSeries(),
         getFontShape(),
         getLaTeXFontFamily(),
         getLaTeXFontSize(),
         getLaTeXFontSeries(),
         getLaTeXFontShape(),
         getHalign(),
         getValign());

      super.okay();
   }

   public void set(FlowframTkSettings appSettings)
   {
      setLinePaint(appSettings.getLinePaint());
      setFillPaint(appSettings.getFillPaint());
      setTextPaint(appSettings.getTextPaint());
      setFontName(appSettings.getFontFamily());
      setFontSize(appSettings.getFontSize());
      setFontSeries(appSettings.getFontSeries());
      setFontShape(appSettings.getFontShape());
      setLaTeXFontFamily(appSettings.getLaTeXFontFamily());
      setLaTeXFontShape(appSettings.getLaTeXFontShape());
      setLaTeXFontSeries(appSettings.getLaTeXFontSeries());
      setLaTeXFontSize(appSettings.getLaTeXFontSize());
      setHalign(appSettings.pgfHalign);
      setValign(appSettings.pgfValign);
      setStroke(appSettings.getStroke());
   }

   public JDRPaint getLinePaint()
   {
      return linePanel.getPaint(getCanvasGraphics());
   }

   public void setLinePaint(JDRPaint paint)
   {
      linePanel.setPaint(paint);
   }

   public JDRPaint getTextPaint()
   {
      if (textPanel == null)
      {
         return super.getTextPaint();
      }

      return textPanel.getPaint(getCanvasGraphics());
   }

   public void setTextPaint(JDRPaint paint)
   {
      textPanel.setPaint(paint);
   }

   public JDRPaint getFillPaint()
   {
      return fillPanel.getPaint(getCanvasGraphics());
   }

   public void setFillPaint(JDRPaint paint)
   {
      fillPanel.setPaint(paint);
   }

   public JDRBasicStroke getStroke()
   {
      if (linestylePanel == null)
      {
         return super.getStroke();
      }

      return linestylePanel.getStroke(getCanvasGraphics());
   }

   public void setStroke(JDRBasicStroke stroke)
   {
      linestylePanel.setStroke(stroke);
   }

   public String getFontName()
   {
      if (fontPanel == null)
      {
         return "Serif";
      }

      return fontPanel.getFontName();
   }

   public void setFontName(String name)
   {
      fontPanel.setFontName(name);
   }

   public JDRLength getFontSize()
   {
      if (fontPanel == null)
      {
         return new JDRLength(getResources().getMessageDictionary(),
           10, JDRUnit.pt);
      }

      return fontPanel.getFontSize();
   }

   public void setFontSize(JDRLength size)
   {
      fontPanel.setFontSize(size);
   }

   public int getFontSeries()
   {
      if (fontPanel == null)
      {
         return JDRFont.SERIES_MEDIUM;
      }

      return fontPanel.getFontSeries();
   }

   public void setFontSeries(int series)
   {
      fontPanel.setFontSeries(series);
   }

   public int getFontShape()
   {
      if (fontPanel == null)
      {
         return JDRFont.SHAPE_UPRIGHT;
      }

      return fontPanel.getFontShape();
   }

   public void setFontShape(int shape)
   {
      fontPanel.setFontShape(shape);
   }

   public String getLaTeXFontFamily()
   {
      return fontPanel.getLaTeXFontFamily();
   }

   public void setLaTeXFontFamily(String name)
   {
      fontPanel.setLaTeXFontFamily(name);
   }

   public String getLaTeXFontShape()
   {
      return fontPanel.getLaTeXFontShape();
   }

   public void setLaTeXFontShape(String shape)
   {
      fontPanel.setLaTeXFontShape(shape);
   }

   public String getLaTeXFontSeries()
   {
      return fontPanel.getLaTeXFontSeries();
   }

   public void setLaTeXFontSeries(String series)
   {
      fontPanel.setLaTeXFontSeries(series);
   }

   public String getLaTeXFontSize()
   {
      return fontPanel.getLaTeXFontSize();
   }

   public void setLaTeXFontSize(String size)
   {
      fontPanel.setLaTeXFontSize(size);
   }

   public int getHalign()
   {
      if (fontPanel == null)
      {
         return 0;
      }

      return fontPanel.getHalign();
   }

   public int getValign()
   {
      if (fontPanel == null)
      {
         return 0;
      }

      return fontPanel.getValign();
   }

   public void setHalign(int align)
   {
      fontPanel.setHalign(align);
   }

   public void setValign(int align)
   {
      fontPanel.setValign(align);
   }

   public void setDefaults()
   {
      CanvasGraphics cg = application_.getDefaultCanvasGraphics();

      setLinePaint(new JDRColor(cg, Color.black));

      setTextPaint(new JDRColor(cg, Color.black));

      setFillPaint(new JDRTransparent(cg));

      linestylePanel.setDefaults();

      fontPanel.setDefaults();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "StyleSelector:"+eol;

      return str+super.info();
   }

   private PaintPanel linePanel, fillPanel, textPanel;

   private LineStylePanel linestylePanel;

   private FontPanel fontPanel;

   private JTabbedPane tabbedPane;
}
