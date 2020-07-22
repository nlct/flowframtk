// File          : JDRFrame.java
// Description   : Internal frame used by FlowframTk.
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
package com.dickimawbooks.flowframtk;

import java.util.Vector;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.image.*;
import java.awt.datatransfer.*;
import java.awt.print.PageFormat;

import java.io.*;

import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.undo.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.marker.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.dialog.*;

/**
 * Internal frame used by FlowframTk.
 * @author Nicola L C Talbot
 */

public class JDRFrame extends JInternalFrame
   implements ActionListener,InternalFrameListener,FocusListener
{
   public JDRFrame(File file, CanvasGraphics cg, 
      JMenu menu, ButtonGroup buttonGroup, FlowframTk application)
   {
      super("FlowframTk", true, true, true, true);
      application_ = application;

      init(file, cg, menu, buttonGroup);

      canvas.requestFocusInWindow();
   }

   private void init(File file, CanvasGraphics cg, 
      JMenu menu, ButtonGroup buttonGroup)
   {
      currentFile = file;

      setFrameIcon(getResources().getSmallAppIcon());

      preambleEditor = new PreambleEditor(this);

      canvas = new JDRCanvas(this, cg);
      canvas.setTransferHandler(new JDRTransferHandler());

      int prefWidth = (int)getComponentPaperWidth();
      int prefHeight = (int)getComponentPaperHeight();

      canvas.setPreferredSize(new Dimension(prefWidth, prefHeight));

      addInternalFrameListener(this);
      setAutoscrolls(true);
      newImage = true;

      if (file == null)
      {
         String label="";
         count++;
         label=getResources().getString("label.untitled");

         if (count > 1) label += count;

         defaultName = label;
      }

      menu_ = menu;
      menuItem = new JRadioButtonMenuItem(getFilename());
      menuItem.addActionListener(this);
      menu_.insert(menuItem, 0);

      buttonGroup.add(menuItem);
      setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

      scrollPane = new JScrollPane(canvas);

      hRuler = 
         new RulerPanel(SwingConstants.HORIZONTAL,
                        canvas.getPreferredSize().width,
                        application_.getHRulerHeight(), canvas);
      vRuler =
         new RulerPanel(SwingConstants.VERTICAL,
                        application_.getVRulerWidth(), 
                        canvas.getPreferredSize().height,
                        canvas);

      MouseAdapter mouseAdapter = new MouseAdapter()
      {
         public void mouseClicked(MouseEvent evt)
         {
            if (isEditingPreamble())
            {
               canvas.requestFocusInWindow();
            }
         }
      };

      hRuler.addMouseListener(mouseAdapter);
      vRuler.addMouseListener(mouseAdapter);

      unitLabel = new JLabel(cg.getGrid().getMainUnit().getLabel());
      unitLabel.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent evt)
         {
            if (evt.getClickCount() > 1)
            {
               application_.displayGridSettings();
            }

            if (isEditingPreamble())
            {
               canvas.requestFocusInWindow();
            }
         }
      });

      scrollPane.setColumnHeaderView(hRuler);
      scrollPane.setRowHeaderView(vRuler);
      scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, unitLabel);
      scrollPane.getViewport().addChangeListener(canvas);

      canvas.addMouseMotionListener(hRuler);
      canvas.addMouseMotionListener(vRuler);

      addFocusListener(this);

      // deal with page up/down etc in canvas listener
      scrollPane.getActionMap().getParent().clear();

      if (file != null)
      {
         load(file);
      }

      if (application_.getSettings().isCanvasFirst())
      {
         splitPane = new JSplitPane(
           application_.getSettings().getCanvasSplit(), 
           scrollPane, preambleEditor);
      }
      else
      {
         splitPane = new JSplitPane(
           application_.getSettings().getCanvasSplit(), 
           preambleEditor, scrollPane);
      }

      getContentPane().add(splitPane, "Center");

      splitPane.setResizeWeight(0.75);
      splitPane.setOneTouchExpandable(true);

      splitPane.setDividerLocation(prefWidth);

      updateTitle();

      try
      {
         // set a default size for the internal frame restore button
         // (otherwise the frame will disappear when the restore
         // button is pressed)
         setMaximum(false);
         setIcon(false);
         reshape(0, 0, prefWidth/2, prefHeight/2);
      }
      catch (PropertyVetoException e)
      {
         getResources().debugMessage(e);
      }
   }

   public void setImage(JDRGroup image)
   {
      canvas.removeBackgroundImage();

      CanvasGraphics cg = image.getCanvasGraphics();
      newImage = false;

      boolean isMod = isModified();
      canvas.setImage(image);
      saved = !isMod;
      updateTitle();

      apply(cg, true);
      canvas.updateGeneralActions(false);
      getApplication().setModified(isMod);
   }

   public void selectThisFrame()
   {
      try
      {
         setSelected(true);

         if (isIcon())
         {
            setIcon(false);
            setMaximum(true);
         }
      }
      catch (PropertyVetoException ignore)
      {
      }
      moveToFront();
   }

   public void updateTextAreaBounds()
   {
      canvas.updateTextAreaBounds();
   }

   public void setDefaultName(String name)
   {
      defaultName = name;
      updateTitle();
   }

   public void setFile(File file)
   {
      currentFile = file;

      updateTitle();
      application_.setCurrentFile(currentFile, !saved);
      application_.updateWindowMenu();
   }

   public void updateTitle()
   {
      int numHidden = canvas.getNumberOfHiddenObjects();

      int displayPage = canvas.getDisplayPage();

      String prefix = "";

      switch (displayPage)
      {
         case JDRCanvas.PAGES_NONE:
            prefix = getResources().getMessage(
               "flowframe.pages.label",
               getResources().getString("flowframe.pages_none"));
            break;
         case JDRCanvas.PAGES_ODD:
            prefix = getResources().getMessage(
               "flowframe.pages.label",
               getResources().getString("flowframe.pages_odd"));
            break;
         case JDRCanvas.PAGES_EVEN:
            prefix = getResources().getMessage(
               "flowframe.pages.label",
               getResources().getString("flowframe.pages_even"));
            break;
         case JDRCanvas.PAGES_ALL:
            break;
         default:
            prefix = getResources().getMessage(
               "flowframe.pages.label", displayPage);
      }

      if (!prefix.isEmpty())
      {
         prefix = "["+prefix+"] ";
      }

      String suffix = isModified() ? 
      " ["+getResources().getString("info.modified")+"]" : "";

      String name = defaultName;

      if (currentFile != null)
      {
         name = currentFile.getName().toString();
      }

      if (numHidden == 0)
      {
         setTitle(prefix+name+suffix);
      }
      else if (numHidden == 1)
      {
         setTitle(String.format("%s%s (%s)%s", prefix, name,
           getResources().getMessage("label.hidden_object", 1),
           suffix));
      }
      else
      {
         setTitle(String.format("%s%s (%s)%s", prefix, name, 
           getResources().getMessage("label.hidden_objects", numHidden),
           suffix));
      }
   }

   public String getFilename()
   {
      return currentFile == null ? defaultName : currentFile.toString();
   }

   public File getFile()
   {
      return currentFile;
   }

   public boolean hasFileName()
   {
      return currentFile != null;
   }

   public void addRecentFile(File file)
   {
      application_.addRecentFile(file);
   }

   public void saveString(String str)
   {
      application_.saveString(str);
   }

   public void apply(CanvasGraphics cg, boolean updateBackground)
   {
      canvas.applyCanvasGraphics(cg); 

      application_.setTool(cg.getTool());
      setNormalSize(cg.getLaTeXNormalSize());
      showRulers(cg.showRulers());
      application_.setLockGrid(cg.isGridLocked());
      application_.setShowGridButtonItemState(cg.isGridDisplayed());
      setPaperSize(cg.getPaper(), updateBackground);
      setMagnification(cg.getMagnification(), updateBackground);
      application_.setStatusStorageUnit(cg.getStorageUnit());
      setGrid(cg.getGrid(), updateBackground);
      setAction(cg.getTool());
   }

   public byte getEditFlag()
   {
      return canvas == null ? JDRConstants.EDIT_FLAG_NONE : 
         canvas.getEditFlag();
   }

   public byte getConstructionFlag()
   {
      return canvas == null ? FlowframTkAction.CONSTRUCTION_FLAG_NONE : 
         canvas.getConstructionFlag();
   }

   public JDRSelection getSelectionFlags()
   {
      return canvas == null ? null : canvas.getSelectionFlags();
   }

   public void setStorageUnit(byte unitId)
   {
      canvas.setStorageUnit(unitId);
   }

   public void setGrid(JDRGrid grid)
   {
      setGrid(grid, true);
   }

   public void setGrid(JDRGrid grid, boolean updateBackground)
   {
      getApplication().getDefaultCanvasGraphics().setGrid(grid);

      getCanvasGraphics().setGrid(grid);

      unitLabel.setText(grid.getUnitLabel());

      if (updateBackground) 
      {
         canvas.setBackgroundImage(true);
         repaint();
      }
   }

   public void showGrid(boolean show)
   {
      showGrid(show, true);
   }

   public void showGrid(boolean show, boolean updateBackground)
   {
      getCanvasGraphics().setDisplayGrid(show);
      canvas.setBackgroundImage(updateBackground);

      if (updateBackground)
      {
         canvas.repaint();
      }
   }

   public void setMagnification(double mag)
   {
      setMagnification(mag, true);
   }

   public void setMagnification(double mag, boolean updateBackground)
   {
      if (mag == getCanvasGraphics().getMagnification())
      {
         return;
      }

      getCanvasGraphics().setMagnification(mag);

      Dimension dim = new Dimension(
         (int)getComponentPaperWidth(),
         (int)getComponentPaperHeight());

      setPreferredSize(dim);
      canvas.setPreferredSize(dim);
      revalidate();
      scrollPane.setPreferredSize(dim);
      scrollPane.revalidate();
      Dimension rulerDim = hRuler.getPreferredSize();
      rulerDim.width = dim.width;
      hRuler.setPreferredSize(rulerDim);
      hRuler.revalidate();
      rulerDim = vRuler.getPreferredSize();
      rulerDim.height = dim.height;
      vRuler.setPreferredSize(rulerDim);
      vRuler.revalidate();

      if (updateBackground) canvas.setBackgroundImage(true);

      canvas.updateTextFieldBounds();

      if (updateBackground) repaint();

      application_.updateZoom(getMagnification());
   }

   public void setRulerConf(Font rulerFont, int sideWidth, int topHeight)
   {
      Dimension dim = vRuler.getPreferredSize();
      dim.width = sideWidth;
      vRuler.setPreferredSize(dim);
      vRuler.setFont(rulerFont);

      dim = hRuler.getPreferredSize();
      dim.height = topHeight;
      hRuler.setPreferredSize(dim);
      hRuler.setFont(rulerFont);

      setMagnification(getMagnification());
   }

   public void zoomWidth()
   {
      double paperWidth = getCanvasGraphics().bpToComponentX(getBpPaperWidth())
                        / getCanvasGraphics().getMagnification();
      double paperHeight = getCanvasGraphics().bpToComponentY(getBpPaperHeight())
                         / getCanvasGraphics().getMagnification();

      Dimension dim = scrollPane.getViewport().getExtentSize();

      int viewWidth = dim.width;

      JScrollBar vBar = scrollPane.getVerticalScrollBar();

      if (!vBar.isVisible())
      {
         // Vertical scroll bar is currently not visible.
         // Will scaling cause it to reappear?

         int viewHeight = dim.height;
         dim = vBar.getSize();

         if (paperHeight*(viewWidth-dim.width)/paperWidth > viewHeight)
         {
            viewWidth -= dim.width;
         }
      }
      else if (scrollPane.getVerticalScrollBarPolicy()
            == ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)
      {
         // Vertical scroll bar is currently visible.
         // Will scaling cause it to disappear?

         int viewHeight = dim.height;
         dim = vBar.getSize();

         if (paperHeight*(viewWidth+dim.width)/paperWidth < viewHeight)
         {
            viewWidth += dim.width;
         }
      }

      double mag = viewWidth/paperWidth;

      if (mag != 0.0) setMagnification(mag);
   }

   public void zoomHeight()
   {
      double paperWidth = getCanvasGraphics().bpToComponentX(getBpPaperWidth())
                        / getCanvasGraphics().getMagnification();
      double paperHeight = getCanvasGraphics().bpToComponentY(getBpPaperHeight())
                         / getCanvasGraphics().getMagnification();


      Dimension dim = scrollPane.getViewport().getExtentSize();

      int viewWidth = dim.width;
      int viewHeight = dim.height;

      JScrollBar hBar = scrollPane.getHorizontalScrollBar();

      if (!hBar.isVisible())
      {
         // Horizontal scroll bar is currently not visible.
         // Will scaling cause it to reappear?

         dim = hBar.getSize();

         if (paperWidth*(viewHeight-dim.height)/paperHeight > viewWidth)
         {
            viewHeight -= dim.height;
         }
      }
      else if (scrollPane.getHorizontalScrollBarPolicy()
            == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
      {
         // Horizontal scroll bar is currently visible.
         // Will scaling cause it to disappear?

         dim = hBar.getSize();

         if (paperWidth*(viewHeight+dim.height)/paperHeight < viewWidth)
         {
            viewHeight += dim.height;
         }
      }

      double mag = viewHeight/paperHeight;

      if (mag != 0.0) setMagnification(mag);
   }

   public void zoomPage()
   {
      double paperWidth = getCanvasGraphics().bpToComponentX(getBpPaperWidth())
                        / getCanvasGraphics().getMagnification();
      double paperHeight = getCanvasGraphics().bpToComponentY(getBpPaperHeight())
                         / getCanvasGraphics().getMagnification();

      Dimension dim = scrollPane.getViewport().getExtentSize();

      int viewWidth = dim.width;
      int viewHeight = dim.height;

      double magX = viewWidth/paperWidth;
      double magY = viewHeight/paperHeight;

      if (magX < magY)
      {
         JScrollBar vBar = scrollPane.getVerticalScrollBar();

         if (vBar.isVisible() && 
            scrollPane.getVerticalScrollBarPolicy()
               == ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)
         {
            // Vertical scroll bar is currently visible.
            // Will scaling cause it to disappear?

            dim = vBar.getSize();

            if (paperHeight*(viewWidth+dim.width)/paperWidth 
                  < viewHeight)
            {
               viewWidth += dim.width;

               magX = viewWidth/paperWidth;
            }
         }
      }
      else
      {
         JScrollBar hBar = scrollPane.getHorizontalScrollBar();

         if (hBar.isVisible() && 
            scrollPane.getHorizontalScrollBarPolicy()
               == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
         {
            // Horizontal scroll bar is currently visible.
            // Will scaling cause it to disappear?

            dim = hBar.getSize();

            if (paperWidth*(viewHeight+dim.height)/paperHeight 
                  < viewWidth)
            {
               viewHeight += dim.height;

               magY = viewHeight/paperHeight;
            }
         }
      }

      double mag = (magX < magY ? magX : magY);

      if (mag != 0.0) setMagnification(mag);
   }

   public void setOrientation(boolean isPortrait)
   {
      JDRPaper orgPaper = getCanvasGraphics().getPaper();
      JDRPaper paper = getCanvasGraphics().getPaper(isPortrait);

      if (paper != orgPaper)
      {
         setPaperSize(paper);
         canvas.setBackgroundImage(true);
      }
   }

   public void setPaperSize(JDRPaper p)
   {
      setPaperSize(p, true);
   }

   public void setPaperSize(JDRPaper p, boolean updateBackground)
   {
      getCanvasGraphics().setPaper(p);

      revalidateImage(updateBackground);
   }

   public void forceUpdate()
   {
      revalidateImage(true);
   }

   public void revalidateImage(boolean updateBackground)
   {
      double mag = getMagnification();

      Dimension dim = new Dimension(
         (int)getComponentPaperWidth(),
         (int)getComponentPaperHeight());

      canvas.setPreferredSize(dim);
      canvas.revalidate();
      scrollPane.setPreferredSize(dim);
      scrollPane.revalidate();
      Dimension rulerDim = hRuler.getPreferredSize();
      rulerDim.width = dim.width;
      hRuler.setPreferredSize(rulerDim);
      hRuler.revalidate();
      rulerDim = vRuler.getPreferredSize();
      rulerDim.height = dim.height;
      vRuler.setPreferredSize(rulerDim);
      vRuler.revalidate();

      CanvasGraphics cg = getCanvasGraphics();

      cg.setDisplayGrid(application_.isGridDisplayed());

      if (updateBackground) canvas.setBackgroundImage(updateBackground);
      canvas.resetTextField();

      if (updateBackground) repaint();
      application_.setPaperSize(cg.getPaper());
   }

   public Font getRulerFont()
   {
      return vRuler.getFont();
   }

   public void setMargins(PageFormat pf, boolean updateBackground)
   {
      getCanvasGraphics().setMargins(pf);
      application_.setPaperSize(getPaper());
      canvas.setBackgroundImage(updateBackground);
      if (updateBackground) repaint();
   }

   public void setMargins()
   {
      application_.setPaperSize(getPaper());
      canvas.setBackgroundImage(true);
      repaint();
   }

   public void updateBackground()
   {
      canvas.setBackgroundImage(true);
      repaint();
   }

   public boolean showMargins()
   {
      return application_.showMargins();
   }

   public void showRulers(boolean flag)
   {
      getCanvasGraphics().setShowRulers(flag);
      scrollPane.getColumnHeader().setVisible(flag);
      scrollPane.getRowHeader().setVisible(flag);
      application_.setRulers(flag);
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvas == null ? null : canvas.getCanvasGraphics();
   }

   public void goToCoordinate(JDRLength x, JDRLength y)
   {
      JDRUnit unit = getCanvasGraphics().getStorageUnit();

      canvas.goToStorageCoordinate(x.getValue(unit), y.getValue(unit));
   }

   public RenderingHints getRenderingHints()
   {
      return application_.getRenderingHints();
   }

   public JDRFont getCurrentFont()
   {
      return application_.getCurrentFont();
   }

   public JDRBasicStroke getCurrentStroke()
   {
      return application_.getCurrentStroke();
   }

   public JDRPaint getCurrentLinePaint()
   {
      return application_.getCurrentLinePaint();
   }

   public JDRPaint getCurrentFillPaint()
   {
      return application_.getCurrentFillPaint();
   }

   public JDRPaint getCurrentTextPaint()
   {
      return application_.getCurrentTextPaint();
   }

   public String getCurrentFontFamily()
   {
      return application_.getCurrentFontFamily();
   }

   public String getCurrentLaTeXFontFamily()
   {
      return application_.getCurrentLaTeXFontFamily();
   }

   public JDRLength getCurrentFontSize()
   {
      return application_.getCurrentFontSize();
   }

   public int getCurrentFontSeries()
   {
      return application_.getCurrentFontSeries();
   }

   public int getCurrentFontShape()
   {
      return application_.getCurrentFontShape();
   }

   public int getCurrentPGFHAlign()
   {
      return application_.getCurrentPGFHAlign();
   }

   public int getCurrentPGFVAlign()
   {
      return application_.getCurrentPGFVAlign();
   }

   public String getCurrentLaTeXFontSize()
   {
      return application_.getCurrentLaTeXFontSize();
   }

   public String getCurrentLaTeXFontSeries()
   {
      return application_.getCurrentLaTeXFontSeries();
   }

   public String getCurrentLaTeXFontShape()
   {
      return application_.getCurrentLaTeXFontShape();
   }

   public int getSelectedFontSeries()
   {
      return canvas.getSelectedFontSeries();
   }

   public JDRLength getSelectedFontSize()
   {
      return canvas.getSelectedFontSize();
   }

   public int getSelectedFontShape()
   {
      return canvas.getSelectedFontShape();
   }

   public int getSelectedHalign()
   {
      return canvas.getSelectedHalign();
   }

   public int getSelectedValign()
   {
      return canvas.getSelectedValign();
   }

   public String getSelectedFontName()
   {
      return canvas.getSelectedFontName();
   }

   public JDRPaint getSelectedLinePaint()
   {
      return canvas.getSelectedLinePaint();
   }

   public JDRPaint getSelectedFillPaint()
   {
      return canvas.getSelectedFillPaint();
   }

   public JDRPaint getSelectedTextPaint()
   {
      return canvas.getSelectedTextPaint();
   }

   public Font getSymbolButtonFont()
   {
      return canvas.getSymbolButtonFont();
   }

   public JDRBasicStroke getSelectedStroke()
   {
      return canvas.getSelectedBasicStroke();
   }

   public JDRTextual getSelectedTextual()
   {
      return canvas.getSelectedTextual();
   }

   public JDRTextual getSelectedFont()
   {
      return canvas.getSelectedFont();
   }

   public JDRBitmap getSelectedBitmap()
   {
      return canvas.getSelectedBitmap();
   }

   public JDRCompleteObject getSelectedObject()
   {
      return canvas.getSelectedObject();
   }

   public JDRCompleteObject getObject(int index)
   {
      return canvas.getObject(index);
   }

   public JDRPath getSelectedPath()
   {
      return canvas.getSelectedPath();
   }

   public int getSelectedPatternIndex()
   {
      return canvas.getSelectedPatternIndex();
   }

   public void reduceToGrey()
   {
      canvas.reduceToGrey();
   }

   public void removeAlpha()
   {
      canvas.removeAlpha();
   }

   public void convertToCMYK()
   {
      canvas.convertToCMYK();
   }

   public void convertToRGB()
   {
      canvas.convertToRGB();
   }

   public void convertToHSB()
   {
      canvas.convertToHSB();
   }

   public void fade(double value)
   {
      canvas.fade(value);
   }

   public void setSelectedTextPaint(JDRPaint paint)
   {
      paint.applyCanvasGraphics(getCanvasGraphics());
      canvas.setSelectedTextPaint(paint);
   }

   public void setSelectedLinePaint(JDRPaint paint)
   {
      paint.applyCanvasGraphics(getCanvasGraphics());
      canvas.setSelectedLinePaint(paint);
   }

   public void setSelectedFillPaint(JDRPaint paint)
   {
      paint.applyCanvasGraphics(getCanvasGraphics());
      canvas.setSelectedFillPaint(paint);
   }

   public void setSelectedStroke(JDRBasicStroke stroke)
   {
      stroke.applyCanvasGraphics(getCanvasGraphics());
      canvas.setSelectedStroke(stroke);
   }

   public void setSelectedLineWidth(JDRLength lineWidth)
   {
      canvas.setSelectedLineWidth(lineWidth);
   }

   public void setSelectedDashPattern(DashPattern pattern)
   {
      pattern.applyCanvasGraphics(getCanvasGraphics());
      canvas.setSelectedDashPattern(pattern);
   }

   public void setSelectedCapStyle(int capStyle)
   {
      canvas.setSelectedCapStyle(capStyle);
   }

   public void setSelectedJoinStyle(int joinStyle)
   {
      canvas.setSelectedJoinStyle(joinStyle);
   }

   public void setSelectedJoinStyle(int joinStyle, double mitreLimit)
   {
      canvas.setSelectedJoinStyle(joinStyle, mitreLimit);
   }

   public void setSelectedStartArrow(JDRMarker marker)
   {
      marker.applyCanvasGraphics(getCanvasGraphics());
      canvas.setSelectedStartArrow(marker);
   }

   public void setSelectedMidArrow(JDRMarker marker)
   {
      marker.applyCanvasGraphics(getCanvasGraphics());
      canvas.setSelectedMidArrow(marker);
   }

   public void setSelectedEndArrow(JDRMarker marker)
   {
      marker.applyCanvasGraphics(getCanvasGraphics());
      canvas.setSelectedEndArrow(marker);
   }

   public void setSelectedMarkers(JDRMarker marker)
   {
      marker.applyCanvasGraphics(getCanvasGraphics());
      canvas.setSelectedMarkers(marker);
   }

   public void setSelectedWindingRule(int rule)
   {
      canvas.setSelectedWindingRule(rule);
   }

   public void setSelectedHalign(int align)
   {
      canvas.setSelectedHalign(align);
   }

   public void setSelectedValign(int align)
   {
      canvas.setSelectedValign(align);
   }

   public void setSelectedAnchor(int halign, int valign)
   {
      canvas.setSelectedAnchor(halign, valign);
   }

   public void setSelectedText(String text, int leftDelim, int rightDelim)
   {
      canvas.setSelectedText(text, leftDelim, rightDelim);
   }

   public void setSelectedText(String text, String ltxText, int leftDelim, int rightDelim, Vector<String> styNames)
   {
      canvas.setSelectedText(text, ltxText, leftDelim, rightDelim, styNames);
   }

   public void setSelectedFont(JDRText text)
   {
      text.applyCanvasGraphics(getCanvasGraphics());
      canvas.setSelectedFont(text);
   }

   public void setSelectedFontFamily(String family, String latexFam)
   {
      canvas.setSelectedFontFamily(family, latexFam);
   }

   public void setSelectedFontSize(JDRLength size, String latexSize)
   {
      canvas.setSelectedFontSize(size, latexSize);
   }

   public void setSelectedFontShape(int shape, String latexShape)
   {
      canvas.setSelectedFontShape(shape, latexShape);
   }

   public void setSelectedFontSeries(int series, String latexSeries)
   {
      canvas.setSelectedFontSeries(series, latexSeries);
   }

   public void setBitmapProperties(JDRBitmap bitmap,
      String newfilename, String newlatexfilename,
      String command, double[] matrix)
   {
      canvas.setBitmapProperties(bitmap, newfilename, newlatexfilename,
         command, matrix);
   }

   public LaTeXFontBase getLaTeXFonts()
   {
      return getCanvasGraphics().getLaTeXFontBase();
   }

   public void setNormalSize(double normalsize)
   {
      getCanvasGraphics().setLaTeXNormalSize(normalsize);
   }

   public double getNormalSize()
   {
      return getCanvasGraphics().getLaTeXNormalSize();
   }

   public void postEdit(UndoableEdit edit)
   {
      application_.postEdit(edit);
   }

   public void disableUndoRedo()
   {
      application_.disableUndoRedo();
   }

   public void refreshUndoRedo()
   {
      application_.refreshUndoRedo();
   }

/*
   public void enableTools()
   {
      canvas.enableTools();
   }

   public void enableTools(int size, int[] number, boolean[] any)
   {
      application_.enableTools(currentTool(), size, number, any);
   }

   public void enableEditTools(boolean enable, boolean enableGap)
   {
      application_.enableEditTools(enable, enableGap);
   }

   public void enableEditTools(boolean enable)
   {
      application_.enableEditTools(enable);
   }
*/

   public int currentTool()
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (cg == null)
      {
         return application_.getDefaultCanvasGraphics().getTool();
      }

      return cg.getTool();
   }

   public void setTool(int tool)
   {
      getCanvasGraphics().setTool(tool);
      application_.setTool(tool);
      canvas.enableTools();
   }

   public void lockGrid(boolean lock)
   {
      getCanvasGraphics().setGridLock(lock);
   }

   public boolean isPathEdited()
   {
      return application_.isPathEdited();
   }

   public boolean isObjectDistorting()
   {
      return application_.isObjectDistorting();
   }

   public void setAction(int tool)
   {
      canvas.setAction(tool);
   }

   public void refresh()
   {
      canvas.refresh();
   }

   public void insertBitmap(String filename)
   {
      canvas.insertBitmap(filename);
   }

   public void insertBitmap(File file)
   {
      canvas.insertBitmap(file);
   }

   public void finishPath()
   {
      canvas.finishPath();
   }

   public void abandonPath()
   {
      canvas.abandonPath();
   }

   public void gap()
   {
      canvas.gap();
   }

/*
   public void resetGapButton()
   {
      application_.resetGapButton();
   }
*/

   public void bottomAlign()
   {
      canvas.bottomAlign();
   }

   public void middleAlign()
   {
      canvas.middleAlign();
   }

   public void topAlign()
   {
      canvas.topAlign();
   }

   public void rightAlign()
   {
      canvas.rightAlign();
   }

   public void centreAlign()
   {
      canvas.centreAlign();
   }

   public void leftAlign()
   {
      canvas.leftAlign();
   }

   public void group()
   {
      canvas.group();
   }

   public void ungroup()
   {
      canvas.ungroup();
   }

   public void mergePaths()
   {
      canvas.mergePaths();
   }

   public void xorPaths()
   {
      canvas.xorPaths();
   }

   public void pathIntersect()
   {
      canvas.pathIntersect();
   }

   public void subtractPaths()
   {
      canvas.subtractPaths();
   }

   public void pathUnion()
   {
      canvas.pathUnion();
   }

   public void splitText()
   {
      canvas.splitText();
   }

   public void convertToPath()
   {
      canvas.convertToPath();
   }

   public void convertToFullPath()
   {
      canvas.convertToFullPath();
   }

   public void convertToTextPath()
   {
      canvas.convertToTextPath();
   }

   public void separate()
   {
      canvas.separate();
   }

   public void reverseSelectedPaths()
   {
      canvas.reverseSelectedPaths();
   }

   public void unsetAllFlowFrames()
   {
      canvas.unsetAllFlowFrames();
   }

   public void rotateSelectedPaths(JDRAngle angle)
   {
      canvas.rotateSelectedPaths(angle);
   }

   public void scaleXSelectedPaths(double factor)
   {
      canvas.scaleXSelectedPaths(factor);
   }

   public void scaleYSelectedPaths(double factor)
   {
      canvas.scaleYSelectedPaths(factor);
   }

   public void scaleSelectedPaths(double factor)
   {
      canvas.scaleSelectedPaths(factor);
   }

   public void shearSelectedPaths(double factorX, double factorY)
   {
      canvas.shearSelectedPaths(factorX, factorY);
   }

   public void shapepar(boolean useOutline)
   {
      canvas.shapepar(useOutline);
   }

   public void parshape(boolean useOutline)
   {
      canvas.parshape(useOutline);
   }

   public void resetTextField()
   {
      canvas.resetTextField();
   }

   public void setCurrentPosition(String position)
   {
      application_.setCurrentPosition(position);
   }

   public boolean isUniqueLabel(int frameType, JDRCompleteObject object,
                                   String label)
   {
      return canvas.isUniqueLabel(frameType, object, label);
   }

   public void setFlowFrame(JDRCompleteObject object, FlowFrame f)
   {
      f.setCanvasGraphics(getCanvasGraphics());
      canvas.setFlowFrame(object, f);
   }

   public void setTypeblock(double left, double right,
                            double top, double bottom,
                            double evenHshift)
   {
      canvas.setTypeblock(left, right, top, bottom, evenHshift);
   }

   public FlowFrame getTypeblock()
   {
      return canvas.getTypeblock();
   }

   public void distortObject()
   {
      canvas.distortObject();
   }

   public void finishDistortObject()
   {
      canvas.finishDistortObject();
   }

   public void selectAll()
   {
      canvas.selectAll();
   }

   public void deselectAll()
   {
      canvas.deselectAll();
   }

   public void markAsSaved()
   {
      saved = true;
      newImage = false;
      updateTitle();
      application_.setModified(false);
   }

   public void markAsModified()
   {
      saved = false;
      newImage = false;
      updateTitle();
      application_.setModified(true);
   }

   public boolean isModified()
   {
      return !saved;
   }

   public boolean isSaved()
   {
      return saved;
   }

   public void print()
   {
      canvas.print();
   }

   public void load(File file)
   {
      canvas.load(file);
   }

   public void preSave()
   {
      if (preambleEditor.isModified())
      {
         canvas.setPreamble(preambleEditor.getPreambleText());
         canvas.setMidPreamble(preambleEditor.getMidPreambleText());
         canvas.setEndPreamble(preambleEditor.getEndPreambleText());
      }
   }

   public void save()
   {
      save(currentFile, JDRAJR.CURRENT_VERSION, false);
   }

   public void save(boolean exitAfter)
   {
      canvas.save(currentFile, JDRAJR.CURRENT_VERSION, exitAfter);
   }

   public void save(File file, float jdrversion)
   {
      canvas.save(file, JDRAJR.CURRENT_VERSION, false);
   }

   public void save(File file, float jdrversion, boolean exitAfter)
   {
      canvas.save(file, jdrversion, exitAfter);
   }

   public void saveAJR()
   {
      canvas.saveAJR(currentFile, JDRAJR.CURRENT_VERSION, false);
   }

   public void saveAJR(boolean exitAfter)
   {
      canvas.saveAJR(currentFile, JDRAJR.CURRENT_VERSION, exitAfter);
   }

   public void saveAJR(File file, float ajrversion)
   {
      canvas.saveAJR(file, ajrversion, false);
   }

   public void saveAJR(File file, float ajrversion, boolean exitAfter)
   {
      canvas.saveAJR(file, ajrversion, exitAfter);
   }

   public void savePGF(File file)
   {
      canvas.savePGF(file);
   }

   public void savePGFDoc(File file, boolean encapsulate)
   {
      canvas.savePGFDoc(file, encapsulate);
   }

   public void saveFlowFrame(File file)
   {
      canvas.saveFlowFrame(file);
   }

   public void savePNG(File file)
   {
      canvas.savePNG(file);
   }

   public void saveEPS(File file, String latexApp, String dvipsApp)
   {
      canvas.saveEPS(file, latexApp, dvipsApp);
   }

   public void savePdf(File file, String pdflatexApp)
   {
      canvas.savePdf(file, pdflatexApp);
   }

   public void saveSVG(File file, String latexApp, String dvisvgmApp, String libgs)
   {
      canvas.saveSVG(file, latexApp, dvisvgmApp, libgs);
   }

   public boolean canDiscard()
   {
      if (isIoInProgress())
      {
         getResources().error(this,
            getResources().getString("error.io.in_progress"));

         return false;
      }

      if (!canvas.canDiscard())
      {
         return false;
      }

      return true;
   }

   public boolean discard()
   {
      discardWithoutSaving();

      canvas.discard();

      newImage = false;
      saved = true;

      return true;
   }

   public boolean saveAndDiscard()
   {
      if (hasFileName())
      {
         save();
      }
      else
      {
         if (!application_.promptAndSave(this))
         {
            return false;
         }
      }

      discardWithoutSaving();
      return true;
   }

   public void discardWithoutSaving()
   {
      setVisible(false);
      application_.removeFrame(this);
      menu_.remove(menuItem);
      application_.updateWindowMenu();
      newImage = true;
      application_.discardAllEdits();
      canvas.discardImage();
      dispose();
   }

   public void internalFrameClosing(InternalFrameEvent e)
   {
      if (canDiscard())
      {
         discard();
      }
   }

   public void internalFrameClosed(InternalFrameEvent e)
   {
   }

   public void internalFrameOpened(InternalFrameEvent e)
   {
   }

   public void internalFrameIconified(InternalFrameEvent e)
   {
      application_.updateStatus();
   }
 
   public void internalFrameDeiconified(InternalFrameEvent e)
   {
   }

   public void internalFrameActivated(InternalFrameEvent e)
   {
      menuItem.setSelected(true);
      apply(getCanvasGraphics(), false);
      application_.setCurrentFile(currentFile, !saved);
      application_.updateActionButtons(true);
   }

   public void internalFrameDeactivated(InternalFrameEvent e)
   {
      abandonPath();
      application_.updateActionButtons();
   }

   public void focusLost(FocusEvent evt)
   {
   }

   public void focusGained(FocusEvent evt)
   {
      if (canvas == null) return;

      if (canvas.isTextFieldVisible() && !evt.isTemporary())
      {
         canvas.requestSymbolFocus();
      }

      application_.updateZoom(getMagnification());
   }

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource();
      if (source == menuItem)
      {
         selectThisFrame();
      }
   }

   public void moveDrawObjectToFront()
   {
      canvas.moveToFront();
   }

   public void moveDrawObjectToBack()
   {
      canvas.moveToBack();
   }

   public void moveDrawObjectUp()
   {
      canvas.moveUp();
   }

   public void moveDrawObjectDown()
   {
      canvas.moveDown();
   }

   public void convertSelectedToPattern(JDRPattern pattern)
   {
      pattern.setCanvasGraphics(getCanvasGraphics());
      canvas.convertSelectedToPattern(pattern);
   }

   public void updatePattern(int index, JDRPattern pattern)
   {
      pattern.setCanvasGraphics(getCanvasGraphics());
      canvas.updatePattern(index, pattern);
   }

   public void removePattern()
   {
      canvas.removeSelectedPattern();
   }

   public double getMagnification()
   {
      CanvasGraphics cg = getCanvasGraphics();
      return cg == null ? 1.0 : cg.getMagnification();
   }

   public JDRGrid getGrid()
   {
      return getCanvasGraphics().getGrid();
   }

   public JDRUnit getUnit()
   {
      return getCanvasGraphics().getGrid().getMainUnit();
   }

   public double getAbsoluteMinorDistance(int major, int minor,
                       JDRUnit unit)
   {
      double majorH = unit.toBp(major);
      double minorH = minor;
      if (minorH > 0)
      {
         minorH = majorH/minorH;
      }
      else
      {
         minorH = 0;
      }

      return minorH*getMagnification();
   }

   public void updateRulersFromComponent(double x, double y)
   {
      vRuler.updateFromComponent(x, y);
      hRuler.updateFromComponent(x, y);
   }

   public void updateRulersFromBp(double x, double y)
   {
      vRuler.updateFromBp(x, y);
      hRuler.updateFromBp(x, y);
   }

   public void updateRulersFromStorage(double x, double y)
   {
      vRuler.updateFromStorage(x, y);
      hRuler.updateFromStorage(x, y);
   }

   public double getBpPaperWidth()
   {
      return getCanvasGraphics().getPaperWidth();
   }

   public double getBpPaperHeight()
   {
      return getCanvasGraphics().getPaperHeight();
   }

   public double getComponentPaperWidth()
   {
      return getCanvasGraphics().bpToComponentX(getBpPaperWidth());
   }

   public double getComponentPaperHeight()
   {
      return getCanvasGraphics().bpToComponentY(getBpPaperHeight());
   }

   public double getStoragePaperWidth()
   {
      return getCanvasGraphics().bpToStorage(getBpPaperWidth());
   }

   public double getStoragePaperHeight()
   {
      return getCanvasGraphics().bpToStorage(getBpPaperHeight());
   }

   public boolean getGridLock()
   {
      return getCanvasGraphics().isGridLocked();
   }

   public void setGridLock(boolean lock)
   {
      getCanvasGraphics().setGridLock(lock);
   }

   public boolean showGrid()
   {
      return getCanvasGraphics().isGridDisplayed();
   }

   public int getSaveJDRsettings()
   {
      return application_.getSaveJDRsettings();
   }

   public boolean warnOnOldJdr()
   {
      return application_.warnOnOldJdr();
   }

   public JDRPaper getPaper()
   {
      return getCanvasGraphics().getPaper();
   }

   public void pasteSelectedPaths()
   {
      Clipboard clipboard = getClipboard();

      Transferable clipData = clipboard.getContents(clipboard);
      if (clipData != null)
      {
         if (clipData.isDataFlavorSupported(
           new DataFlavor((new JDRGroup(getCanvasGraphics())).getClass(),
                          "JDRGroup")))
         {
            TransferHandler handler = canvas.getTransferHandler();
            handler.importData(canvas, clipData);
            repaint();
         }
      }
   }

   public void copySelectedPaths()
   {
      TransferHandler handler = canvas.getTransferHandler();
      handler.exportToClipboard(canvas,
         getClipboard(),TransferHandler.COPY);
   }

   public void cutSelectedPaths()
   {
      TransferHandler handler = canvas.getTransferHandler();
      handler.exportToClipboard(canvas,
         getClipboard(),TransferHandler.MOVE);
      canvas.deleteSelection();
      canvas.repaint();
   }

   public Clipboard getClipboard()
   {
      return application_.clipboard;
   }

   public JDRCanvas getCanvas()
   {
      return canvas;
   }

   public FlowframTk getApplication()
   {
      return application_;
   }

   public void setDisplayPage(int page)
   {
      canvas.setDisplayPage(page);
   }

   public int getDisplayPage()
   {
      return canvas.getDisplayPage();
   }

   public JDRGroup getAllPaths()
   {
      return canvas.getAllPaths();
   }

   public void selectObjectAndScroll(JDRCompleteObject object)
   {
      canvas.selectObjectAndScroll(object);
   }

   public void updateLaTeXFontSize()
   {
      canvas.updateLaTeXFontSize(getCanvasGraphics().getLaTeXFontBase());
   }

   public void addObject(JDRCompleteObject object, String undoText)
   {
      canvas.addObject(object, undoText);
   }

   public void printInfo(PrintWriter out) throws IOException
   {
      out.println("is saved: "+saved);
      out.println("new image: "+newImage);
      out.println("current filename: "+currentFile.toString());
      out.println(getCanvasGraphics().getLaTeXFontBase());
      out.println("Image:");
      canvas.printInfo(out);

      out.println(this);
   }

   /**
    * Gets information on selected objects.
    * @return string containing information about selected objects
    */
   public String getSelectedInfo()
   {
      return canvas.getSelectedInfo();
   }

   public void setIoInProgress(boolean flag)
   {
      this.ioInProgress = flag;

      if (flag)
      {
         repaint();
      }
      else
      {
         getApplication().updateGeneralActionButtons(true);

         revalidateImage(true);
      }
   }

   public boolean isIoInProgress()
   {
      return ioInProgress;
   }

   public void setGraphicsDevice(Graphics2D g2)
   {
      getCanvasGraphics().setGraphicsDevice(g2);
   }

   public LaTeXFontBase getLaTeXFontBase()
   {
      return getCanvasGraphics().getLaTeXFontBase();
   }

   public boolean isNewImage()
   {
      return newImage;
   }

   public void setNewImageState(boolean isnew)
   {
      newImage = isnew;
   }

   public void displayPreambleEditor()
   {
      splitPane.setDividerLocation(0.5);
      preambleEditor.display();
   }

   public PreambleEditor getPreambleEditor()
   {
      return preambleEditor;
   }

   public void updatePreamble(String newPreamble, 
     String newMidPreamble, String newEndPreamble)
   {
      preambleEditor.updatePreambleText(newPreamble, newMidPreamble, newEndPreamble);
   }

   public void updateEditorStyles(FlowframTkSettings appSettings)
   {
      preambleEditor.updateStyles(appSettings);
   }

   public boolean isEditingPreamble()
   {
      return preambleEditor == null ? false : preambleEditor.isEditing();
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   public JScrollPane scrollPane;

   private RulerPanel vRuler, hRuler;

   private FlowframTk application_;

   private volatile File currentFile;

   private String defaultName = "untitled";

   private volatile boolean newImage;
   private volatile boolean saved=true;

   private volatile JDRCanvas canvas;

   public static int count=0;

   private JMenu menu_;
   public JMenuItem menuItem;

   private JLabel unitLabel;

   private PreambleEditor preambleEditor;

   private JSplitPane splitPane;

   private volatile boolean ioInProgress=false;
}
