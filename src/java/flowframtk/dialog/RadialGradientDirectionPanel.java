// File          : RadialGradientDirectionPanel.java
// Description   : Panel for selecting radial gradient start location
// Creation Date : 6th February 2006
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
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

/**
 * Panel for selecting radial gradient start location.
 * @author Nicola L C Talbot
 */

public class RadialGradientDirectionPanel extends JPanel
   implements ActionListener
{
   public RadialGradientDirectionPanel(GradientPanel gradientPanel)
   {
      super();
      this.resources = gradientPanel.getResources();

      GridLayout layout = new GridLayout(3, 3, 0, 0);
      setLayout(layout);

      ButtonGroup directionGroup = new ButtonGroup();

      // NORTH-WEST
      gNorthWest = createDirectionButton(directionGroup, gradientPanel, 
        JDRRadial.NORTH_WEST, "northwest", '7');

      // NORTH
      gNorth = createDirectionButton(directionGroup, gradientPanel, 
        JDRRadial.NORTH, "north", '8');

      // NORTH-EAST
      gNorthEast = createDirectionButton(directionGroup, gradientPanel, 
        JDRRadial.NORTH_EAST, "northeast", '9');

      // WEST
      gWest = createDirectionButton(directionGroup, gradientPanel, 
        JDRRadial.WEST, "west", '4');

      // Centre
      gCenter = createDirectionButton(directionGroup, gradientPanel, 
        JDRRadial.CENTER, "centre", '5');

      // East

      gEast = createDirectionButton(directionGroup, gradientPanel, 
        JDRRadial.EAST, "east", '6');

      // SOUTH-WEST

      gSouthWest = createDirectionButton(directionGroup, gradientPanel, 
        JDRRadial.SOUTH_WEST, "southwest", '1');

      // SOUTH

      gSouth = createDirectionButton(directionGroup, gradientPanel, 
        JDRRadial.SOUTH, "south", '2');

      // SOUTH-EAST

      gSouthEast = createDirectionButton(directionGroup, gradientPanel, 
        JDRRadial.SOUTH_EAST, "southeast", '3');

      Dimension dim = gSouthEast.getPreferredSize();
      setMaximumSize(new Dimension(3*dim.width,3*dim.height));

      setDirection(JDRRadial.CENTER);
   }

   private DirectionButton createDirectionButton(ButtonGroup directionGroup, 
     GradientPanel gradientPanel, int direction, String action, char mnemonic)
   {
      DirectionButton button =
       getResources().createDirectionButton("radial", direction);
      directionGroup.add(button);
      add(button);
      button.addActionListener(gradientPanel);
      button.addActionListener(this);
      button.setActionCommand(action);
      button.setMnemonic(mnemonic);
      button.setToolTipText(getResources().getToolTipText(
         "direction.radial."+action));

      return button;
   }

   public void setEnabled(boolean flag)
   {
      super.setEnabled(flag);
      gNorth.setEnabled(flag);
      gNorthEast.setEnabled(flag);
      gEast.setEnabled(flag);
      gSouthEast.setEnabled(flag);
      gSouth.setEnabled(flag);
      gSouthWest.setEnabled(flag);
      gWest.setEnabled(flag);
      gNorthWest.setEnabled(flag);
      gCenter.setEnabled(flag);
   }

   public void actionPerformed(ActionEvent e)
   {
      Object source = e.getSource();

      if (source instanceof DirectionButton)
      {
         gradientDir = ((DirectionButton)source).getDirection();
      }
   }

   public int getDirection()
   {
      return gradientDir;
   }

   public void setDirection(int direction)
   {
      gradientDir = direction;

      switch (gradientDir)
      {
         case JDRRadial.NORTH :
            gNorth.setSelected(true);
         break;
         case JDRRadial.SOUTH :
            gSouth.setSelected(true);
         break;
         case JDRRadial.EAST :
            gEast.setSelected(true);
         break;
         case JDRRadial.WEST :
            gWest.setSelected(true);
         break;
         case JDRRadial.NORTH_WEST :
            gNorthWest.setSelected(true);
         break;
         case JDRRadial.SOUTH_EAST :
            gSouthEast.setSelected(true);
         break;
         case JDRRadial.NORTH_EAST :
            gNorthEast.setSelected(true);
         break;
         case JDRRadial.SOUTH_WEST :
            gSouthWest.setSelected(true);
         break;
         case JDRRadial.CENTER :
            gCenter.setSelected(true);
      }
   }

   public JDRResources getResources()
   {
      return resources;
   }

   private int gradientDir;

   private DirectionButton gNorth, gSouth, gEast, gWest;
   private DirectionButton gNorthWest, gSouthEast;
   private DirectionButton gNorthEast, gSouthWest, gCenter;

   private JDRResources resources;
}
