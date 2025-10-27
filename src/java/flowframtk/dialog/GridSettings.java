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
   implements ActionListener
{
   public GridSettings(FlowframTk application)
   {
      super(application, application.getResources().getMessage("grid.title"),
            true);
      application_ = application;

      JDRResources resources = getResources();

      rectangularGridPanel = new RectangularGridPanel(resources);

      radialGridPanel = new RadialGridPanel(resources);

      isoGridPanel = new IsoGridPanel(resources);

      tschicholdGridPanel = new TschicholdGridPanel(resources);

      pathGridPanel = new PathGridPanel(application);

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

      JComponent offsetPanel = new JPanel(new FlowLayout());
      mainPanel.add(offsetPanel, "South");

      JLabel label = resources.createAppLabel("grid.offset");
      offsetPanel.add(label);
      offsetPanel.add(resources.createLabelSpacer());

      label = resources.createAppLabel("grid.offset.x");
      offsetPanel.add(label);

      offsetXModel = new SpinnerNumberModel();
      offsetXSpinner = new JSpinner(offsetXModel);
      label.setLabelFor(offsetXSpinner);
      offsetPanel.add(offsetXSpinner);

      JSpinner.NumberEditor editor = (JSpinner.NumberEditor)offsetXSpinner.getEditor();
      editor.getTextField().setColumns(6);

      offsetPanel.add(resources.createLabelSpacer());
      label = resources.createAppLabel("grid.offset.y");
      offsetPanel.add(label);

      offsetYModel = new SpinnerNumberModel();
      offsetYSpinner = new JSpinner(offsetYModel);
      label.setLabelFor(offsetYSpinner);
      offsetPanel.add(offsetYSpinner);

      editor = (JSpinner.NumberEditor)offsetYSpinner.getEditor();
      editor.getTextField().setColumns(6);

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

      setLocationRelativeTo(application_);
      pack();
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

   private JDRFrame mainPanel = null;
}
