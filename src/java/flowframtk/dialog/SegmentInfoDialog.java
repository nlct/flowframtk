// File          : SegmentInfoDialog.java
// Description   : Dialog box containing path segment information.
// Creation Date : 2020-08-09
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2020-2025 Nicola L.C. Talbot

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

import java.util.Vector;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for displaying (and editing) segment details.
 * @author Nicola L C Talbot
 */
public class SegmentInfoDialog extends JDialog
   implements ActionListener,JDRApp
{
   public SegmentInfoDialog(FlowframTk application)
   {
      super(application,
         application.getResources().getMessage("segmentinfo.title"), true);
      this.application = application;

      init();
   }

   private void init()
   {
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      addWindowListener(new WindowAdapter()
       {
          public void windowClosing(WindowEvent evt)
          {
             cancel();
          }
       });

      JDRResources resources = application.getResources();

      samplePanel = new SegmentSamplePanel(this);
      samplePanelSp = new JScrollPane(samplePanel);

      zoomComp = new ZoomComponent(this);

      JComponent lowerPane = new JPanel(new BorderLayout());

      textComp = resources.createAppInfoArea();
      textComp.setRows(6);
      lowerPane.add(new JScrollPane(textComp), "North");

      JComponent detailsComp = Box.createVerticalBox();
      lowerPane.add(new JScrollPane(detailsComp), "Center");

      mainControlComp = Box.createVerticalBox();
      detailsComp.add(mainControlComp);

      endControlPanel = new ControlInfoPanel(this, -1);
      detailsComp.add(endControlPanel);

      detailsComp.add(Box.createVerticalGlue());

      JSplitPane splitPane = new JSplitPane(
        JSplitPane.VERTICAL_SPLIT, samplePanelSp, lowerPane);
      splitPane.setResizeWeight(0.5);

      getContentPane().add(splitPane, "Center");

      Dimension prefSize = endControlPanel.getLabel().getPreferredSize();
      int maxWidth = prefSize.width;

      infoPanels = new Vector<ControlInfoPanel>();

      for (int i = 0; i < 3; i++)
      {
         ControlInfoPanel infoPanel = new ControlInfoPanel(this, i);
         infoPanels.add(infoPanel);

         prefSize = infoPanel.getLabel().getPreferredSize();

         if (prefSize.width > maxWidth)
         {
            maxWidth = prefSize.width;
         }
      }

      prefSize = endControlPanel.getLabel().getPreferredSize();

      if (prefSize.width < maxWidth)
      {
         prefSize.width = maxWidth;
         endControlPanel.getLabel().setPreferredSize(prefSize);
      }

      for (ControlInfoPanel infoPanel : infoPanels)
      {
         prefSize = infoPanel.getLabel().getPreferredSize();

         if (prefSize.width < maxWidth)
         {
            prefSize.width = maxWidth;
            infoPanel.getLabel().setPreferredSize(prefSize);
         }
      }

      JPanel bottomPanel = new JPanel(new BorderLayout());
      getContentPane().add(bottomPanel, "South");

      JPanel p2 = new JPanel();
      bottomPanel.add(p2, "Center");

      p2.add(resources.createOkayButton(getRootPane(), this));
      p2.add(resources.createCancelButton(this));
      p2.add(resources.createHelpDialogButton(this, "mi:segmentinfo"));

      revertButton = resources.createDialogButton(
        "segmentinfo", "default", this, null);
      revertButton.setEnabled(true);
      bottomPanel.add(revertButton, "East");

      bottomPanel.add(zoomComp, "West");
   }

   @Override
   public JDRResources getResources()
   {
      return application.getResources();
   }

   @Override
   public double getCurrentMagnification()
   {
      CanvasGraphics cg = getCanvasGraphics();

      return cg == null ? 1.0 : cg.getMagnification();
   }

   @Override
   public void setCurrentMagnification(double factor)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (cg != null && factor > 0.0)
      {
         cg.setMagnification(factor);
         detailsUpdated();
      }
   }

   @Override
   public double zoomAction(ZoomValue zoomValue)
   {
      updateCurrentFactor(zoomValue);

      detailsUpdated();

      return getCurrentMagnification();
   }

   private void updateCurrentFactor(ZoomValue zoomValue)
   {
      String id = zoomValue.getActionCommand();
      BBox bbox = samplePanel.getBpBounds();

      if (bbox == null) return;

      double factor = getCurrentMagnification();

      if (id.equals(ZoomValue.ZOOM_PAGE_WIDTH_ID))
      {
         double width = bbox.getWidth();

         if (width > 0)
         {
            Dimension dim = samplePanel.getSize();
            factor = dim.getWidth()/width;
         }
      }
      else if (id.equals(ZoomValue.ZOOM_PAGE_HEIGHT_ID))
      {
         double height = bbox.getHeight();

         if (height > 0)
         {
            Dimension dim = samplePanel.getSize();
            factor = dim.getHeight()/(double)height;
         }
      }
      else if (id.equals(ZoomValue.ZOOM_PAGE_ID))
      {
         double width = bbox.getWidth();
         double height = bbox.getHeight();

         if (height <= 0 || width <= 0)
         {
            factor = 1.0;
         }
         else
         {
            Dimension dim = samplePanel.getSize();

            if (height > width)
            {
               factor = dim.getHeight()/height;
            }
            else
            {
               factor = dim.getWidth()/width;
            }
         }
      }
      else if (zoomValue instanceof PercentageZoomValue)
      {
         factor = ((PercentageZoomValue)zoomValue).getValue();
      }

      zoomComp.setZoom(zoomValue, factor);

      setCurrentMagnification(factor);
   }

   @Override
   public void showZoomChooser()
   {
   }

   public JDRPathSegment getSegment()
   {
      return workingSegment;
   }

   public JDRShape getWorkingPath()
   {
      return workingPath;
   }

   public Shape getCompleteShape()
   {
      return completeShape;
   }

   public JDRGrid getGrid()
   {
      return frame.getGrid();
   }

   public void setModified(boolean changed)
   {
      if (modified != changed)
      {
         modified = changed;
         revertButton.setEnabled(modified);
      }
   }

   public void detailsUpdated()
   {
      detailsUpdated(modified);
   }

   public void detailsUpdated(boolean changed)
   {
      completeShape = workingPath.getComponentGeneralPath();
      textComp.setText(workingSegment.getDetails());
      samplePanel.detailsUpdated();
      samplePanel.repaint();
      setModified(changed);
   }

   public void scrollSample(double x, double y)
   {
      JScrollBar hBar = samplePanelSp.getHorizontalScrollBar();
      JScrollBar vBar = samplePanelSp.getVerticalScrollBar();

      int min = hBar.getMinimum();
      int max = hBar.getMaximum();

      hBar.setValue((int)Math.floor(min+x*(max-min)));

      min = vBar.getMinimum();
      max = vBar.getMaximum();

      vBar.setValue((int)Math.floor(min+y*(max-min)));
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return workingSegment.getCanvasGraphics();
   }

   public void revert()
   {
      CanvasGraphics cg = workingSegment.getCanvasGraphics();

      JDRUnit unit = cg.getStorageUnit();
      JDRPaper paper = frame.getPaper();
      JDRGrid grid = frame.getGrid();

      int n = workingSegment.controlCount();

      for (int i = 0; i < n; i++)
      {
         JDRPoint orgPoint = segment.getControl(i);

         JDRPoint point = workingSegment.getControl(i);
         point.set(orgPoint.getX(), orgPoint.getY());
         point.setSelected(orgPoint.isSelected());

         infoPanels.get(i).setPoint(point, hoffset, voffset, 
          unit, paper, grid);
      }

      if (endControlPanel.isEnabled())
      {
         JDRPoint orgPoint = segment.getEnd();

         JDRPoint point = workingSegment.getEnd();
         point.set(orgPoint.getX(), orgPoint.getY());
         point.setSelected(orgPoint.isSelected());

         endControlPanel.setPoint(point, hoffset, voffset,
            unit, paper, grid);
      }

      detailsUpdated(false);
   }

   public void display(JDRFrame frame, JDRShape path, JDRPathSegment segment)
   {
      this.frame = frame;
      this.segment = segment;
      JDRCanvas canvas = frame.getCanvas();

      int editedSegmentIndex = path.getSelectedIndex();

      workingPath = (JDRShape)path.clone();

      workingSegment = workingPath.get(editedSegmentIndex);

      CanvasGraphics cg = (CanvasGraphics)workingPath.getCanvasGraphics().clone();
      cg.setComponent(samplePanel);
      workingPath.setCanvasGraphics(cg);
      zoomComp.setZoom(cg.getMagnification());

      completeShape = workingPath.getBpGeneralPath();

      textComp.setText(workingSegment.getDetails());

      int n = segment.controlCount();

      for (int i = infoPanels.size(); i < n; i++)
      {
         infoPanels.add(new ControlInfoPanel(this, i));
      }

      int compCount = mainControlComp.getComponentCount();

      if (compCount < n)
      {
         for (int i = compCount; i < n; i++)
         {
            mainControlComp.add(infoPanels.get(i));
         }
      }
      else if (compCount > n)
      {
         for (int i = compCount-1; i >= n; i--)
         {
            mainControlComp.remove(i);
         }
      }

      hoffset = 0.0;
      voffset = 0.0;

      FlowFrame flowframe = path.getFlowFrame();

      if (flowframe != null && cg.isEvenPage())
      {
         hoffset = flowframe.getEvenXShift();
         voffset = flowframe.getEvenYShift();

         FlowFrame typeblock = canvas.getTypeblock();

         if (typeblock != null)
         {
            hoffset += typeblock.getEvenXShift();
         }
      }

      JDRUnit unit = cg.getStorageUnit();
      JDRPaper paper = frame.getPaper();
      JDRGrid grid = frame.getGrid();
      selectedIndex = 0;

      for (int i = 0; i < n; i++)
      {
         JDRPoint point = workingSegment.getControl(i);
         infoPanels.get(i).setPoint(point, hoffset, voffset, 
          unit, paper, grid);

         if (point.isSelected())
         {
            selectedIndex = i;
         }
      }

      JDRPoint endPt = workingSegment.getEnd();
      endControlPanel.setPoint(endPt, hoffset, voffset,
         unit, paper, grid);

      if (workingPath.segmentHasEnd(workingSegment))
      {
         endControlPanel.setEnabled(true);

         if (endPt.isSelected())
         {
            selectedIndex = n;
         }
      }
      else
      {
         endControlPanel.setEnabled(false);
      }

      detailsUpdated(false);
      pack();
      setLocationRelativeTo(frame);
      setVisible(true);
   }

   public JDRUnit getStorageUnit()
   {
      return frame.getCanvasGraphics().getStorageUnit();
   }

   public void okay()
   {
      if (!modified)
      {
         setVisible(false);
         return;
      }

      JDRCanvas canvas = frame.getCanvas();
      JDRUnit unit = getStorageUnit();

      for (int i = 0, n = Math.min(infoPanels.size(), workingSegment.controlCount());
           i < n; i++)
      {
         ControlInfoPanel panel = infoPanels.get(i);

         if (panel.hasChanged())
         {
            JDRLength xcoord = panel.getXCoord();
            xcoord.subtract(hoffset, unit);

            JDRLength ycoord = panel.getYCoord();
            ycoord.subtract(voffset, unit);

            canvas.setControlPoint(segment, segment.getControl(i),
               xcoord, ycoord);
         }
      }

      if (endControlPanel.isEnabled() && endControlPanel.hasChanged())
      {
          JDRLength xcoord = endControlPanel.getXCoord();
          xcoord.subtract(hoffset, unit);

          JDRLength ycoord = endControlPanel.getYCoord();
          ycoord.subtract(voffset, unit);

          canvas.setControlPoint(segment, segment.getEnd(),
             xcoord, ycoord);
      }

      setVisible(false);
   }

   public void cancel()
   {
      JDRResources resources = getResources();

      if (!modified || resources.confirm(this, 
           resources.getMessage("segmentinfo.confirm_discard"))
         == JOptionPane.YES_OPTION)
      {
         setVisible(false);
      }
   }

   public void actionPerformed(ActionEvent e)
   {
      String action = e.getActionCommand();

      if (action == null) return;

      if (action.equals("okay"))
      {
         okay();
      } 
      else if (action.equals("cancel"))
      {
         cancel();
      }
      else if (action.equals("default"))
      {
         if (!modified) return;// shouldn't happen as revertButton should be disabled

         JDRResources resources = getResources();

         if (resources.confirm(this, 
              resources.getMessage("segmentinfo.confirm_discard"))
            == JOptionPane.YES_OPTION)
         {
            revert();
         }
      }
   }

   public String info()
   {
      return String.format("%s%nhas focus: %s%n",
        getClass().getSimpleName(), hasFocus());
   }

   public RenderingHints getRenderingHints()
   {
      return frame.getRenderingHints();
   }

   public double getHOffset()
   {
      return hoffset;
   }

   public double getVOffset()
   {
      return voffset;
   }

   public boolean isEndEnabled()
   {
      return endControlPanel.isEnabled();
   }

   private JDRShape workingPath;
   private Shape completeShape;
   private JDRPathSegment segment, workingSegment;
   private JDRFrame frame;
   private FlowframTk application;
   private double hoffset = 0.0, voffset = 0.0;
   private int selectedIndex;
   private boolean modified=false;

   private Vector<ControlInfoPanel> infoPanels;

   private ControlInfoPanel endControlPanel;

   private JComponent mainControlComp;
   private JTextArea textComp;
   private SegmentSamplePanel samplePanel;
   private JScrollPane samplePanelSp;
   private JButton revertButton;
   private ZoomComponent zoomComp;
}

class SegmentSamplePanel extends JPanel 
   implements ComponentListener,Scrollable
{
   public SegmentSamplePanel(SegmentInfoDialog dialog)
   {
      super(null);
      this.dialog = dialog;
      setOpaque(true);
      setBackground(Color.WHITE);
      setPreferredSize(MIN_SIZE);

      addComponentListener(this);
   }

   public Dimension getMinimumSize()
   {
      return MIN_SIZE;
   }

   public Dimension getPreferredScrollableViewportSize()
   {
      return prefViewportSize;
   }

   public boolean getScrollableTracksViewportWidth() {return false;}
   public boolean getScrollableTracksViewportHeight(){return false;}

   public Dimension getUnitIncrement()
   {
      JDRGrid grid = dialog.getGrid();
      Point2D minor = grid.getMinorTicDistance();
      Point2D major = grid.getMinorTicDistance();

      double incX = (minor.getX() > 0 ? minor.getX() : major.getX());
      double incY = (minor.getY() > 0 ? minor.getY() : major.getY());

      return new Dimension(
       (int)Math.ceil(dialog.getCanvasGraphics().bpToComponentX(incX)),
       (int)Math.ceil(dialog.getCanvasGraphics().bpToComponentY(incY)));
   }

   public int getScrollableUnitIncrement(Rectangle visibleRect,
                                         int orientation,
                                         int direction)
   {
      int currentPos = 0;

      Dimension unitIncrement = getUnitIncrement();

      int inc;

      if (orientation == SwingConstants.HORIZONTAL)
      {
         currentPos = visibleRect.x;
         inc = unitIncrement.width;
      }
      else
      {
         currentPos = visibleRect.y;
         inc = unitIncrement.height;
      }

      if (direction < 0)
      {
         int newPosition = currentPos - (currentPos/inc) * inc;

         return newPosition==0 ? inc : newPosition;
      }
      else
      {
         return ((currentPos/inc)+1)*inc - currentPos;
      }
   }

   public int getScrollableBlockIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction)
   {
      Dimension unitIncrement = getUnitIncrement();

      if (orientation == SwingConstants.HORIZONTAL)
         return (unitIncrement.width >= visibleRect.width) ?
           visibleRect.width :
           visibleRect.width - unitIncrement.width;
      else
         return (unitIncrement.height >= visibleRect.height) ?
           visibleRect.height :
           visibleRect.height - unitIncrement.height;
   }

   public void detailsUpdated()
   {
      Shape shape = dialog.getCompleteShape();
      CanvasGraphics cg = dialog.getCanvasGraphics();

      Rectangle2D newBounds = shape.getBounds2D();

      JDRPathSegment segment = dialog.getSegment();
      segmentBBox = segment.getStorageControlBBox();
      segmentBBox.scale(cg.storageToComponentX(1.0), 
                        cg.storageToComponentY(1.0));

      bounds = new BBox(cg, newBounds);

      bounds.merge(segmentBBox);

      Dimension dim = new Dimension(
        (int)Math.ceil(bounds.getWidth())+PADDING,
        (int)Math.ceil(bounds.getHeight())+PADDING);

      if (dim.width < MIN_SIZE.width)
      {
         dim.width = MIN_SIZE.width;
      }

      if (dim.height < MIN_SIZE.height)
      {
         dim.height = MIN_SIZE.height;
      }

      Dimension orgSize = getPreferredSize();

      setPreferredSize(dim);

      prefViewportSize = new Dimension(
          (int)Math.ceil(segmentBBox.getWidth())+PADDING,
          (int)Math.ceil(segmentBBox.getHeight())+PADDING);

      if (prefViewportSize.width < MIN_SIZE.width)
      {
         prefViewportSize.width = MIN_SIZE.width;
      }

      if (prefViewportSize.height < MIN_SIZE.height)
      {
         prefViewportSize.height = MIN_SIZE.height;
      }

      int halfPadding = PADDING/2;

      xOffset = halfPadding - bounds.getMinX();
      yOffset = halfPadding - bounds.getMinY();

      if (dim.width > orgSize.width || dim.height > orgSize.height)
      {
         revalidate();
      }
   }

   protected void paintComponent(Graphics g)
   {
      super.paintComponent(g);

      Shape shape = dialog.getCompleteShape();
      JDRPathSegment segment = dialog.getSegment();

      if (shape == null || segment == null)
      {
         return;
      }

      Graphics2D g2 = (Graphics2D)g;
      segment.getCanvasGraphics().setGraphicsDevice(g2);

      AffineTransform oldAf = g2.getTransform();
      RenderingHints oldHints = g2.getRenderingHints();
      RenderingHints hints = dialog.getRenderingHints();

      g2.translate(xOffset, yOffset);

      if (hints != null)
      {
         g2.setRenderingHints(hints);
      }

      g2.setPaint(PATH_PAINT);
      g2.draw(shape);

      g2.setPaint(Color.BLACK);
      segment.draw();
      segment.drawControls(dialog.isEndEnabled());

      g2.setTransform(oldAf);
   }

   public void scrollToSegment()
   {
      if (bounds == null || segmentBBox == null)
      {
         return;
      }

      double totalW = bounds.getWidth();
      double totalH = bounds.getHeight();

      double x = (segmentBBox.getMinX() - bounds.getMinX())/totalW;
      double y = (segmentBBox.getMinY() - bounds.getMinY())/totalH;

      dialog.scrollSample(x, y);
   }

   public void componentHidden(ComponentEvent e)
   {
   }

   public void componentMoved(ComponentEvent e)
   {
   }

   public void componentResized(ComponentEvent e)
   {
      scrollToSegment();
   }

   public void componentShown(ComponentEvent e)
   {
      scrollToSegment();
   }

   public BBox getBpBounds()
   {
      return bounds;
   }

   private BBox bounds=null, segmentBBox=null;
   private Dimension prefViewportSize = MIN_SIZE;
   private double xOffset=0.0, yOffset = 0.0;
   private SegmentInfoDialog dialog;
   private static final Dimension MIN_SIZE = new Dimension(100, 100);

   public static final Color PATH_PAINT = new Color(220, 220, 220);
   public static final int PADDING=10;
}

class ControlInfoPanel extends JPanel implements ChangeListener
{
   public ControlInfoPanel(SegmentInfoDialog dialog, int idx)
   {
      super(new BorderLayout());
      this.dialog = dialog;
      this.index = idx;

      JDRResources resources = dialog.getResources();

      if (index == -1)
      {
         label = new JLabel(resources.getMessage("segmentinfo.end_control"));
      }
      else if (index == 0)
      {
         label = new JLabel(resources.getMessage("segmentinfo.start_control"));
      }
      else
      {
         label = new JLabel(resources.getMessage("segmentinfo.control", idx));
      }

      add(label, "West");

      idx++;
      locationPane = new LocationPane(resources, 
        "segmentinfo.control"+idx);
      locationPane.addCoordinateChangeListener(this);

      if (index == -1)
      {
         setEnabled(false);
      }

      add(locationPane, "Center");
      setOpaque(false);
   }

   public void setPoint(JDRPoint point, double hoffset, double voffset,
     JDRUnit unit, JDRPaper paper, JDRGrid grid)
   {
      this.point = point;
      changed = false;

      locationPane.setCoords(point.x+hoffset, point.y+voffset, 
        unit, paper, grid);
   }

   public JDRPoint getPoint()
   {
      return point;
   }

   public int getIndex()
   {
      return index;
   }

   public JDRLength getXCoord()
   {
      return locationPane.getXCoord();
   }

   public JDRLength getYCoord()
   {
      return locationPane.getYCoord();
   }

   public void setEnabled(boolean enable)
   {
      super.setEnabled(enable);
      label.setEnabled(enable);
      locationPane.setPanelsEnabled(enable);
   }

   public boolean hasChanged()
   {
      return changed;
   }

   public void stateChanged(ChangeEvent evt)
   {
      changed = true;

      if (point != null)
      {
         JDRUnit unit = dialog.getStorageUnit();

         JDRLength xcoord = getXCoord();
         xcoord.subtract(dialog.getHOffset(), unit);

         JDRLength ycoord = getYCoord();
         ycoord.subtract(dialog.getVOffset(), unit);

         point.x = xcoord.getValue(unit);
         point.y = ycoord.getValue(unit);

         dialog.detailsUpdated(true);
      }
   }

   public JLabel getLabel()
   {
      return label;
   }

   private SegmentInfoDialog dialog;
   private LocationPane locationPane;
   private JDRPoint point;
   private int index;
   private boolean changed = false;
   private JLabel label;
}
