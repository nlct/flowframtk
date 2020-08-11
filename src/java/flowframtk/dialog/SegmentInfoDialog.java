// File          : SegmentInfoDialog.java
// Description   : Dialog box containing path segment information.
// Creation Date : 2020-08-09
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2020 Nicola L.C. Talbot

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
   implements ActionListener
{
   public SegmentInfoDialog(FlowframTk application)
   {
      super(application,
         application.getResources().getString("segmentinfo.title"), true);
      this.application = application;

      JDRResources resources = application.getResources();

      samplePanel = new SegmentSamplePanel(this);

      JComponent detailsComp = Box.createVerticalBox();

      textComp = resources.createAppInfoArea();
      detailsComp.add(textComp);

      mainControlComp = Box.createVerticalBox();
      detailsComp.add(mainControlComp);

      endControlPanel = new ControlInfoPanel(this, -1);
      detailsComp.add(endControlPanel);

      JSplitPane splitPane = new JSplitPane(
        JSplitPane.VERTICAL_SPLIT, new JScrollPane(samplePanel), detailsComp);
      splitPane.setResizeWeight(1.0);

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

      p2.add(resources.createOkayButton(this));
      p2.add(resources.createCancelButton(this));

      bottomPanel.add(resources.createDialogButton(
        "segmentinfo", "default", this, null), "East");
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   public JDRPathSegment getSegment()
   {
      return workingSegment;
   }

   public Shape getCompleteShape()
   {
      return completeShape;
   }

   public void detailsUpdated()
   {
      completeShape = workingPath.getBpGeneralPath();
      textComp.setText(workingSegment.getDetails());
      samplePanel.repaint();
   }

   public void revert()
   {
      CanvasGraphics cg = (CanvasGraphics)workingSegment.getCanvasGraphics();

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

      detailsUpdated();
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
      cg.setMagnification(1.0);

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
         setVisible(false);
      }
      else if (action.equals("default"))
      {
         revert();
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

   private Vector<ControlInfoPanel> infoPanels;

   private ControlInfoPanel endControlPanel;

   private JComponent mainControlComp;
   private JTextArea textComp;
   private SegmentSamplePanel samplePanel;
}

class SegmentSamplePanel extends JPanel implements ComponentListener
{
   public SegmentSamplePanel(SegmentInfoDialog dialog)
   {
      super(null);
      this.dialog = dialog;
      setOpaque(true);
      setBackground(Color.WHITE);

      addComponentListener(this);
   }

   public Dimension getMinimumSize()
   {
      return MIN_SIZE;
   }

   public Dimension getPreferredSize()
   {
      JDRPathSegment segment = dialog.getSegment();

      if (segment == null)
      {
         return MIN_SIZE;
      }

      BBox bbox = segment.getBpControlBBox();

      if (bbox == null)
      {
         return MIN_SIZE;
      }

      return new Dimension((int)Math.ceil(bbox.getWidth())+20, 
        (int)Math.ceil(bbox.getHeight())+20);
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

   public void updateOffsets()
   {
      JDRPathSegment segment = dialog.getSegment();

      if (segment == null)
      {
         return;
      }

      Dimension dim = getSize();

      BBox bbox = segment.getBpControlBBox();

      double midX = bbox.getMidX();
      double midY = bbox.getMidY();

      xOffset = 0.5*dim.getWidth() - midX;
      yOffset = 0.5*dim.getHeight() - midY;
   }

   public void componentHidden(ComponentEvent e)
   {
   }

   public void componentMoved(ComponentEvent e)
   {
   }

   public void componentResized(ComponentEvent e)
   {
      updateOffsets();
   }

   public void componentShown(ComponentEvent e)
   {
      updateOffsets();
   }

   private double xOffset=0.0, yOffset = 0.0;
   private SegmentInfoDialog dialog;
   private static final Dimension MIN_SIZE = new Dimension(100, 100);

   public static final Color PATH_PAINT = new Color(220, 220, 220);
}

class ControlInfoPanel extends JPanel implements ChangeListener
{
   public ControlInfoPanel(SegmentInfoDialog dialog, int idx)
   {
      super(new BorderLayout());
      this.dialog = dialog;
      this.index = idx;
      idx++;

      JDRResources resources = dialog.getResources();

      if (index == -1)
      {
         label = new JLabel(resources.getString("segmentinfo.end_control"));
      }
      else
      {
         label = new JLabel(resources.getMessage("segmentinfo.control", idx));
      }

      add(label, "West");

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

         dialog.detailsUpdated();
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
