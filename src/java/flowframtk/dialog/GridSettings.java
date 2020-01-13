// File          : GridSettings.java
// Description   : Dialog in which to specify the grid settings
// Creation Date : 1st February 2006
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
      super(application, application.getResources().getString("grid.title"),
            true);
      application_ = application;

      rectangularGridPanel = new RectangularGridPanel(getResources());

      radialGridPanel = new RadialGridPanel(getResources());

      isoGridPanel = new IsoGridPanel(getResources());

      tschicholdGridPanel = new TschicholdGridPanel(getResources());

      pathGridPanel = new PathGridPanel(application);

      tabbedPane = new JTabbedPane();

      int idx = 0;

      tabbedPane.addTab(getResources().getString("grid.rectangular"),
         null, rectangularGridPanel,
        getResources().getString("tooltip.grid.rectangular"));

      tabbedPane.setMnemonicAt(idx,
         getResources().getChar("grid.rectangular.mnemonic"));

      idx++;

      tabbedPane.addTab(getResources().getString("grid.radial"),
         null, radialGridPanel,
         getResources().getString("tooltip.grid.radial"));

      tabbedPane.setMnemonicAt(idx,
         getResources().getChar("grid.radial.mnemonic"));

      idx++;

      tabbedPane.addTab(getResources().getString("grid.iso"),
         null, isoGridPanel,
         getResources().getString("tooltip.grid.iso"));

      tabbedPane.setMnemonicAt(idx,
         getResources().getChar("grid.iso.mnemonic"));

      idx++;

      tabbedPane.addTab(getResources().getString("grid.tschichold"),
         null, tschicholdGridPanel,
         getResources().getString("tooltip.grid.tschichold"));

      tabbedPane.setMnemonicAt(idx,
         getResources().getChar("grid.tschichold.mnemonic"));

      idx++;

      tabbedPane.addTab(getResources().getString("grid.path"),
         null, pathGridPanel,
         getResources().getString("tooltip.grid.path"));

      tabbedPane.setMnemonicAt(idx,
         getResources().getChar("grid.path.mnemonic"));

      tabbedPane.addChangeListener(pathGridPanel);

      idx++;

      getContentPane().add(tabbedPane, "Center");

      getContentPane().add(
         getResources().createAppInfoArea("grid.info"), "North");

      JPanel p2 = new JPanel();

      p2.add(getResources().createOkayButton(this));
      p2.add(getResources().createCancelButton(this));
      p2.add(getResources().createHelpButton("gridmenu"));

      getContentPane().add(p2, "South");

      setLocationRelativeTo(application_);
      pack();
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

   private JDRFrame mainPanel = null;
}
