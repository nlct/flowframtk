// File          : LinearGradientDirectionPanel.java
// Description   : Panel for selecting linear gradient direction
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
 * Panel for selecting linear gradient direction.
 * @author Nicola L C Talbot
 */

public class LinearGradientDirectionPanel extends JPanel
   implements ActionListener
{
   public LinearGradientDirectionPanel(GradientPanel gradientPanel)
   {
      super(new FlowLayout(FlowLayout.LEFT,0,0));

      this.resources = gradientPanel.getResources();

      ButtonGroup directionGroup = new ButtonGroup();

      gNorth = createDirectionButton(directionGroup,
        gradientPanel, JDRGradient.NORTH, "north", '1');

      gNorthEast = createDirectionButton(directionGroup,
        gradientPanel, JDRGradient.NORTH_EAST, "northeast", '2');

      gEast = createDirectionButton(directionGroup,
        gradientPanel, JDRGradient.EAST, "east", '3');

      gSouthEast = createDirectionButton(directionGroup,
        gradientPanel, JDRGradient.SOUTH_EAST, "southeast", '4');

      gSouth = createDirectionButton(directionGroup,
        gradientPanel, JDRGradient.SOUTH, "south", '5');

      gSouthWest = createDirectionButton(directionGroup,
        gradientPanel, JDRGradient.SOUTH_WEST, "southwest", '6');

      gWest = createDirectionButton(directionGroup,
        gradientPanel, JDRGradient.WEST, "west", '7');

      gNorthWest = createDirectionButton(directionGroup,
        gradientPanel, JDRGradient.NORTH_WEST, "northwest", '8');

      setDirection(JDRGradient.NORTH);
   }

   private DirectionButton createDirectionButton(ButtonGroup directionGroup,
     GradientPanel gradientPanel, int direction, String action, char mnemonic)
   {
      DirectionButton button =
        getResources().createDirectionButton(action, direction);

      directionGroup.add(button);
      add(button);
      button.addActionListener(gradientPanel);
      button.addActionListener(this);
      button.setActionCommand(action);
      button.setMnemonic(mnemonic);
      button.setToolTipText(getResources().getToolTipText(
         "direction.linear."+action));

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
         case JDRGradient.NORTH :
            gNorth.setSelected(true);
         break;
         case JDRGradient.SOUTH :
            gSouth.setSelected(true);
         break;
         case JDRGradient.EAST :
            gEast.setSelected(true);
         break;
         case JDRGradient.WEST :
            gWest.setSelected(true);
         break;
         case JDRGradient.NORTH_WEST :
            gNorthWest.setSelected(true);
         break;
         case JDRGradient.SOUTH_EAST :
            gSouthEast.setSelected(true);
         break;
         case JDRGradient.NORTH_EAST :
            gNorthEast.setSelected(true);
         break;
         case JDRGradient.SOUTH_WEST :
            gSouthWest.setSelected(true);
      }
   }

   public JDRResources getResources()
   {
      return resources;
   }

   private int gradientDir;

   private DirectionButton gNorth, gSouth, gEast, gWest;
   private DirectionButton gNorthWest, gSouthEast;
   private DirectionButton gNorthEast, gSouthWest;

   private JDRResources resources;
}
