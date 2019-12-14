// File          : PathGridPanel.java
// Description   : Panel in which to specify isometric grid settings
// Creation Date : 2014-06-06
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
import java.awt.geom.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Panel in which to specify path grid settings.
 * @author Nicola L C Talbot
 */
public class PathGridPanel extends GridPanel 
   implements ActionListener,ChangeListener
{
   public PathGridPanel(FlowframTk application)
   {
      super();

      this.application = application;
      JDRResources resources = application.getResources(); 

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      Box row = Box.createHorizontalBox();
      row.setAlignmentX(0.0f);
      add(row);

      JLabel majorLabel = resources.createAppLabel("grid.major");
      row.add(majorLabel);

      majorDivisions = new NonNegativeIntField(100);
      majorLabel.setLabelFor(majorDivisions);
      row.add(majorDivisions);

      unitBox = new JComboBox<String>(JDRUnit.UNIT_LABELS);
      unitBox.setSelectedIndex(JDRUnit.PT);
      row.add(unitBox);

      row = Box.createHorizontalBox();
      row.setAlignmentX(0.0f);
      add(row);

      JLabel subdivisionsLabel = resources.createAppLabel(
         "grid.sub_divisions");

      row.add(subdivisionsLabel);

      subDivisions = new NonNegativeIntField(10);
      subdivisionsLabel.setLabelFor(subDivisions);

      row.add(subDivisions);

      row.add(Box.createHorizontalStrut(
         (int)unitBox.getPreferredSize().getWidth()));

      Dimension majorLabelDim = majorLabel.getPreferredSize();

      Dimension subDivLabelDim = subdivisionsLabel.getPreferredSize();

      int maxWidth = 
        (int)Math.max(majorLabelDim.getWidth(), subDivLabelDim.getWidth());

      majorLabelDim.width = maxWidth;
      subDivLabelDim.width = maxWidth;

      majorLabel.setPreferredSize(majorLabelDim);
      subdivisionsLabel.setPreferredSize(subDivLabelDim);

      Dimension dim = majorDivisions.getPreferredSize();
      dim.width = Integer.MAX_VALUE;
      majorDivisions.setMaximumSize(dim);

      dim = unitBox.getPreferredSize();
      unitBox.setMaximumSize(dim);

      dim = subDivisions.getPreferredSize();
      dim.width = Integer.MAX_VALUE;
      subDivisions.setMaximumSize(dim);

      JTextArea info = resources.createAppInfoArea("grid.path.info");
      info.setAlignmentX(0.0f);
      add(info);

      row = Box.createHorizontalBox();
      row.setAlignmentX(0.0f);
      add(row);

      ButtonGroup bg = new ButtonGroup();

      keepCurrentPathButton = resources.createAppRadioButton("grid.path",
       "keep", bg, false, this);
      keepCurrentPathButton.setEnabled(false);

      row.add(keepCurrentPathButton);

      row = Box.createHorizontalBox();
      row.setAlignmentX(0.0f);
      add(row);

      useBoundaryPathButton = resources.createAppRadioButton("grid.path",
       "usepage", bg, true, this);

      row.add(useBoundaryPathButton);

      row = Box.createHorizontalBox();
      row.setAlignmentX(0.0f);
      add(row);

      selectNewPathButton = resources.createAppRadioButton("grid.path",
       "select", bg, false, this);
      selectNewPathButton.setEnabled(false);

      row.add(selectNewPathButton);

      descriptionModel = new DefaultComboBoxModel<String>();
      descriptionBox = new JComboBox<String>(descriptionModel);
      descriptionBox.setEnabled(false);
      row.add(descriptionBox);

      // add a temporary element to help pack the container
      descriptionModel.addElement(resources.getString("label.choose"));

      dim = descriptionBox.getPreferredSize();
      dim.width = Integer.MAX_VALUE;
      descriptionBox.setMaximumSize(dim);


      add(Box.createVerticalStrut(40));
      add(Box.createVerticalGlue());
   }

   public void stateChanged(ChangeEvent evt)
   {
      if (isVisible())
      {
         updatePathSelector();
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if ("keep".equals(action) || "usepage".equals(action))
      {
         descriptionBox.setEnabled(false);
      }
      else if ("select".equals(action))
      {
         descriptionBox.setEnabled(true);
      }
   }

   public void requestDefaultFieldFocus()
   {
      if (descriptionBox.isEnabled())
      {
         descriptionBox.requestFocusInWindow();
      }
      else
      {
         majorDivisions.requestFocusInWindow();
      }
   }

   private void updatePathSelector()
   {
      JDRFrame frame = application.getCurrentFrame();
      descriptionModel.removeAllElements();
      paths = frame.getAllPaths();
      int n = paths.size();

      int idx = -1;

      for (int i = 0; i < n; i++)
      {
         JDRCompleteObject object = paths.get(i);
         String description = object.getDescription();

         if (description.isEmpty())
         {
            description = application.getResources().getDefaultDescription(object);
         }

         descriptionModel.addElement(description);

         if (idx == -1 && object.isSelected())
         {
            idx = i;
         }
      }

      if (currentGrid == null)
      {
         if (keepCurrentPathButton.isSelected())
         {
            useBoundaryPathButton.setSelected(true);
         }

         keepCurrentPathButton.setEnabled(false);

         if (idx > -1 && !selectNewPathButton.isEnabled())
         {
            selectNewPathButton.setEnabled(true);
            selectNewPathButton.setSelected(true);
         }
      }
      else
      {
         keepCurrentPathButton.setEnabled(true);
      }

      if (n > 0)
      {
         selectNewPathButton.setEnabled(true);
      }
      else
      {
         if (selectNewPathButton.isSelected())
         {
            useBoundaryPathButton.setSelected(true);
         }

         selectNewPathButton.setEnabled(false);
      }

      descriptionBox.setEnabled(selectNewPathButton.isSelected());

      if (idx != -1)
      {
         descriptionBox.setSelectedIndex(idx);
      }
      else if (descriptionBox.isEnabled())
      {
         descriptionBox.setSelectedIndex(0);
      }
   }

   public void setGrid(JDRGrid grid)
   {
      majorDivisions.setValue(
         (int)((JDRPathGrid)grid).getMajorInterval());
      subDivisions.setValue(((JDRPathGrid)grid).getSubDivisions());
      setUnit(((JDRPathGrid)grid).getUnit());

      currentGrid = (JDRPathGrid)grid;

      keepCurrentPathButton.setEnabled(true);

      updatePathSelector();
   }

   public JDRGrid getGrid(JDRGrid grid)
   {
      Shape shape = null;
      JDRUnit shapeUnit = null;

      if (currentGrid != null)
      {
         shape = currentGrid.getShape();
         shapeUnit = currentGrid.getUnit();
      }

      if (descriptionBox.isEnabled())
      {
         int idx = descriptionBox.getSelectedIndex();

         if (idx > -1)
         {
            JDRCompleteObject object = paths.get(idx);

            if (object instanceof JDRShape)
            {
               shape = ((JDRShape)object).getGeneralPath();
            }
            else if (object instanceof JDRText)
            {
               JDRCanvas canvas = application.getCurrentFrame().getCanvas();

               Graphics2D g2 = (Graphics2D)canvas.getGraphics();

               if (g2 == null)
               {
                  BBox bbox = object.getStorageBBox();
                  shape = new Rectangle2D.Double(bbox.getMinX(), bbox.getMinY(),
                    bbox.getWidth(), bbox.getHeight());
               }
               else
               {
                  shape = ((JDRText)object).getOutline(g2.getFontRenderContext());

                  g2.dispose();
               }
            }
            else
            {
               BBox bbox = object.getStorageBBox();
               shape = new Rectangle2D.Double(bbox.getMinX(), bbox.getMinY(),
                 bbox.getWidth(), bbox.getHeight());
            }

            shapeUnit = object.getCanvasGraphics().getStorageUnit();
         }
      }

      if (grid instanceof JDRPathGrid)
      {
         JDRPathGrid g = (JDRPathGrid)grid;

         g.set(getUnit(), getMajor(), getSubDivisions());

         if (shape != null)
         {
            g.setShape(shape, shapeUnit);
         }
      }
      else
      {
         if (shape != null)
         {
            grid = new JDRPathGrid(grid.getCanvasGraphics(),
               getUnit(), getMajor(), getSubDivisions(),
               shape, shapeUnit);
         }
         else
         {
            grid = new JDRPathGrid(grid.getCanvasGraphics());
            ((JDRPathGrid)grid).set(getUnit(), getMajor(), getSubDivisions());
         }
      }

      return grid;
   }

   public int getMajor()
   {
      int d = majorDivisions.getInt();
      if (d == 0) d = 1;

      return d;
   }

   public int getSubDivisions()
   {
      int d = subDivisions.getInt();
      if (d == 0) d = 1;

      return d;
   }

   public void setUnit(JDRUnit unit)
   {
      unitBox.setSelectedIndex(unit.getID());
   }

   public JDRUnit getUnit()
   {
      return JDRUnit.getUnit(unitBox.getSelectedIndex()); 
   }

   private NonNegativeIntField majorDivisions, subDivisions;
   private JComboBox<String> unitBox;

   private JComboBox<String> descriptionBox;
   private DefaultComboBoxModel<String> descriptionModel;

   private JRadioButton keepCurrentPathButton, useBoundaryPathButton,
      selectNewPathButton;

   private FlowframTk application;
   private JDRGroup paths;

   private JDRPathGrid currentGrid;
}
