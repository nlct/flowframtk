// File          : GridSettings.java
// Description   : Dialog in which to specify the grid settings
// Creation Date : 1st February 2006
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
import java.awt.geom.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.texjavahelplib.HelpSetNotInitialisedException;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog in which to specify grid settings.
 * @author Nicola L C Talbot
 */
public class GridSettings extends JDialog
   implements ActionListener,ItemListener
{
   public GridSettings(FlowframTk application)
   {
      super(application, application.getResources().getMessage("grid.title"),
            true);
      application_ = application;

      JDRResources resources = getResources();

      rectangularGridPanel = new RectangularGridPanel(resources);
      rectangularGridPanel.addUnitChangeListener(this);

      radialGridPanel = new RadialGridPanel(resources);
      radialGridPanel.addUnitChangeListener(this);

      isoGridPanel = new IsoGridPanel(resources);
      isoGridPanel.addUnitChangeListener(this);

      tschicholdGridPanel = new TschicholdGridPanel(resources);
      tschicholdGridPanel.addUnitChangeListener(this);

      pathGridPanel = new PathGridPanel(application);
      pathGridPanel.addUnitChangeListener(this);

      tabbedPane = new JTabbedPane();

      int idx = 0;

      tabbedPane.addTab(resources.getMessage("grid.rectangular"),
         null, rectangularGridPanel,
        resources.getMessage("tooltip.grid.rectangular"));

      tabbedPane.setMnemonicAt(idx,
         resources.getCodePoint("grid.rectangular.mnemonic"));

      idx++;

      tabbedPane.addTab(resources.getMessage("grid.radial"),
         null, radialGridPanel,
         resources.getMessage("tooltip.grid.radial"));

      tabbedPane.setMnemonicAt(idx,
         resources.getCodePoint("grid.radial.mnemonic"));

      idx++;

      tabbedPane.addTab(resources.getMessage("grid.iso"),
         null, isoGridPanel,
         resources.getMessage("tooltip.grid.iso"));

      tabbedPane.setMnemonicAt(idx,
         resources.getCodePoint("grid.iso.mnemonic"));

      idx++;

      tabbedPane.addTab(resources.getMessage("grid.tschichold"),
         null, tschicholdGridPanel,
         resources.getMessage("tooltip.grid.tschichold"));

      tabbedPane.setMnemonicAt(idx,
         resources.getCodePoint("grid.tschichold.mnemonic"));

      idx++;

      tabbedPane.addTab(resources.getMessage("grid.path"),
         null, pathGridPanel,
         resources.getMessage("tooltip.grid.path"));

      tabbedPane.setMnemonicAt(idx,
         resources.getCodePoint("grid.path.mnemonic"));

      tabbedPane.addChangeListener(pathGridPanel);

      idx++;

      JComponent mainPanel = new JPanel(new BorderLayout());
      mainPanel.add(tabbedPane, "Center");

      JComponent offsetComp = new JPanel(new BorderLayout());
      mainPanel.add(offsetComp, "South");

      JComponent offsetRow = new JPanel(new FlowLayout());
      offsetComp.add(offsetRow, "North");

      JLabel label = resources.createAppLabel("grid.offset");
      offsetRow.add(label);
      offsetRow.add(resources.createLabelSpacer());

      label = resources.createAppLabel("grid.offset.x");
      offsetRow.add(label);

      offsetXModel = new SpinnerNumberModel(Double.valueOf(0.0), null, null, Double.valueOf(1.0));
      offsetXSpinner = new JSpinner(offsetXModel);
      label.setLabelFor(offsetXSpinner);
      offsetRow.add(offsetXSpinner);

      JSpinner.NumberEditor editor = (JSpinner.NumberEditor)offsetXSpinner.getEditor();
      editor.getTextField().setColumns(6);

      offsetRow.add(resources.createLabelSpacer());
      label = resources.createAppLabel("grid.offset.y");
      offsetRow.add(label);

      offsetYModel = new SpinnerNumberModel(Double.valueOf(0.0), null, null, Double.valueOf(1.0));
      offsetYSpinner = new JSpinner(offsetYModel);
      label.setLabelFor(offsetYSpinner);
      offsetRow.add(offsetYSpinner);

      editor = (JSpinner.NumberEditor)offsetYSpinner.getEditor();
      editor.getTextField().setColumns(6);

      offsetUnitLabel = new JLabel("mm");
      offsetRow.add(offsetUnitLabel);

      JComponent offsetCenterComp = new JPanel(new FlowLayout());
      offsetComp.add(offsetCenterComp, "Center");

      JComponent offsetButtonComp = new JPanel(new GridLayout(3, 3, 0, 0));
      offsetCenterComp.add(offsetButtonComp);

      offsetButtonComp.add(resources.createDialogButton("grid.offset", "grid_origin_topleft",
        this, null));
      offsetButtonComp.add(resources.createDialogButton("grid.offset", "grid_origin_middletop",
        this, null));
      offsetButtonComp.add(resources.createDialogButton("grid.offset", "grid_origin_topright",
        this, null));

      offsetButtonComp.add(resources.createDialogButton("grid.offset", "grid_origin_middleleft",
        this, null));
      offsetButtonComp.add(resources.createDialogButton("grid.offset", "grid_origin_middle",
        this, null));
      offsetButtonComp.add(resources.createDialogButton("grid.offset", "grid_origin_middleright",
        this, null));

      offsetButtonComp.add(resources.createDialogButton("grid.offset", "grid_origin_bottomleft",
        this, null));
      offsetButtonComp.add(resources.createDialogButton("grid.offset", "grid_origin_middlebottom",
        this, null));
      offsetButtonComp.add(resources.createDialogButton("grid.offset", "grid_origin_bottomright",
        this, null));


      offsetComp.add(resources.createAppInfoArea("grid.offset.grid_origin_info"), "South");

      getContentPane().add(mainPanel, "Center");

      getContentPane().add(
         resources.createAppInfoArea("grid.info"), "North");

      JComponent commonPanel = Box.createVerticalBox();
      getContentPane().add(commonPanel, "East");

      for (int i = 0; i < GridPanel.GRID_MAX_COMMON; i++)
      {
         commonPanel.add(createCommonButton(i));
      }

      JPanel p2 = new JPanel();

      resources.createOkayCancelHelpButtons(this, p2, this, "sec:gridmenu");

      getContentPane().add(p2, "South");

      pack();
      setLocationRelativeTo(application_);

      JDRUnit unit = getGridUnit();
      offsetUnitLabel.setText(unit.getLabel());
   }

   protected JButton createCommonButton(final int idx)
   {
      JButton btn = new JButton(GridPanel.getCommonString(idx));

      btn.addActionListener(new ActionListener()
       {
          @Override
          public void actionPerformed(ActionEvent evt)
          {
             rectangularGridPanel.setCommon(idx);
             radialGridPanel.setCommon(idx);
             isoGridPanel.setCommon(idx);
             tschicholdGridPanel.setCommon(idx);
             pathGridPanel.setCommon(idx);
          }
       });

      return btn;
   }

   public void display()
   {
      mainPanel = application_.getCurrentFrame();

      JDRGrid grid = mainPanel.getGrid();

      GridPanel gridPanel;

      if (grid instanceof JDRRectangularGrid)
      {
         gridPanel = rectangularGridPanel;
      }
      else if (grid instanceof JDRRadialGrid)
      {
         gridPanel = radialGridPanel;
      }
      else if (grid instanceof JDRIsoGrid)
      {
         gridPanel = isoGridPanel;
      }
      else if (grid instanceof JDRTschicholdGrid)
      {
         gridPanel = tschicholdGridPanel;
      }
      else if (grid instanceof JDRPathGrid)
      {
         gridPanel = pathGridPanel;
      }
      else
      {
         throw new IllegalArgumentException("Invalid grid class "
          + grid.getClass().getName());
      }

      gridPanel.setGrid(grid);
      tabbedPane.setSelectedComponent(gridPanel);

      CanvasGraphics cg = grid.getCanvasGraphics();
      offsetXModel.setValue(Double.valueOf(cg.getOriginX()));
      offsetYModel.setValue(Double.valueOf(cg.getOriginY()));

      gridPanel.requestDefaultFieldFocus();

      setVisible(true);
   }

   public void okay()
   {
      JDRGrid grid = mainPanel.getGrid();

      GridPanel panel = (GridPanel)tabbedPane.getSelectedComponent();

      grid = panel.getGrid(grid);

      if (grid == null)
      {
         return;
      }

      CanvasGraphics cg = grid.getCanvasGraphics();

      cg.setOriginX(offsetXModel.getNumber().doubleValue());
      cg.setOriginY(offsetYModel.getNumber().doubleValue());

      mainPanel.setGrid(grid);

      setVisible(false);
   }

   protected JDRUnit getGridUnit()
   {
      return ((GridPanel)tabbedPane.getSelectedComponent()).getUnit();
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
      else if (action.startsWith("grid_origin_"))
      {
         action = action.substring(12);

         if (action.equals("topleft"))
         {
            offsetXModel.setValue(Double.valueOf(0));
            offsetYModel.setValue(Double.valueOf(0));
         }
         else
         {
            JDRUnit unit = getGridUnit();
            double w = mainPanel.getBpPaperWidth();
            double h = mainPanel.getBpPaperHeight();

            if (action.equals("middletop"))
            {
               offsetXModel.setValue(Double.valueOf(unit.fromBp(0.5*w)));
               offsetYModel.setValue(Double.valueOf(0));
            }
            else if (action.equals("topright"))
            {
               offsetXModel.setValue(Double.valueOf(unit.fromBp(w)));
               offsetYModel.setValue(Double.valueOf(0));
            }
            else if (action.equals("middleleft"))
            {
               offsetXModel.setValue(Double.valueOf(0));
               offsetYModel.setValue(Double.valueOf(unit.fromBp(0.5*h)));
            }
            else if (action.equals("middle"))
            {
               offsetXModel.setValue(Double.valueOf(unit.fromBp(0.5*w)));
               offsetYModel.setValue(Double.valueOf(unit.fromBp(0.5*h)));
            }
            else if (action.equals("middleright"))
            {
               offsetXModel.setValue(Double.valueOf(unit.fromBp(w)));
               offsetYModel.setValue(Double.valueOf(unit.fromBp(0.5*h)));
            }
            else if (action.equals("bottomleft"))
            {
               offsetXModel.setValue(Double.valueOf(0));
               offsetYModel.setValue(Double.valueOf(unit.fromBp(h)));
            }
            else if (action.equals("middlebottom"))
            {
               offsetXModel.setValue(Double.valueOf(unit.fromBp(0.5*w)));
               offsetYModel.setValue(Double.valueOf(unit.fromBp(h)));
            }
            else if (action.equals("bottomright"))
            {
               offsetXModel.setValue(Double.valueOf(unit.fromBp(w)));
               offsetYModel.setValue(Double.valueOf(unit.fromBp(h)));
            }
         }
      }
   }

   public void itemStateChanged(ItemEvent evt)
   {
      if (evt.getStateChange() == ItemEvent.SELECTED && offsetUnitLabel != null)
      {
         JDRUnit unit = getGridUnit();
         offsetUnitLabel.setText(unit.getLabel());
      }
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;

   private JTabbedPane tabbedPane;

   private GridPanel rectangularGridPanel, radialGridPanel, isoGridPanel,
      tschicholdGridPanel;

   private PathGridPanel pathGridPanel;

   private SpinnerNumberModel offsetXModel, offsetYModel;
   private JSpinner offsetXSpinner, offsetYSpinner;
   private JLabel offsetUnitLabel;

   private JDRFrame mainPanel = null;
}
