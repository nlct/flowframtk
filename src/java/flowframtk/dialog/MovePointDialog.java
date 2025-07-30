// File          : MovePointDialog.java
// Description   : Dialog box for specifying co-ordinates
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
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog box for specifying new co-ordinates for a control point.
 * @author Nicola L C Talbot
 */
public class MovePointDialog extends JDialog
   implements ActionListener
{
   public MovePointDialog(JDRFrame frame)
   {
      super(frame.getApplication(),
         frame.getResources().getMessage("coordinates.title"), true);
      frame_ = frame;

      locationPane = new LocationPane(frame.getResources());

      getContentPane().add(locationPane, "Center");

      JPanel p2 = new JPanel();

      p2.add(frame.getResources().createOkayButton(getRootPane(), this));
      p2.add(frame.getResources().createCancelButton(this));

      getContentPane().add(p2, "South");
      pack();
      setLocationRelativeTo(frame);
   }

   public void display()
   {
      canvas = frame_.getCanvas();
      JDRPoint point = canvas.getSelectedStoragePoint();
      JDRShape editedPath = canvas.getEditedPath();

      CanvasGraphics cg = canvas.getCanvasGraphics();
      hoffset = 0.0;
      voffset = 0.0;

      FlowFrame flowframe = editedPath.getFlowFrame();

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

      locationPane.setCoords(point.x+hoffset, point.y+voffset, 
        cg.getStorageUnit(),
        frame_.getPaper(), frame_.getGrid());

      setVisible(true);
   }

   public void okay()
   {
      JDRLength xcoord = locationPane.getXCoord();
      xcoord.subtract(hoffset, canvas.getCanvasGraphics().getStorageUnit());

      JDRLength ycoord = locationPane.getYCoord();
      ycoord.subtract(voffset, canvas.getCanvasGraphics().getStorageUnit());

      canvas.setSelectedPoint(xcoord, ycoord);
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

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str += "MovePointDialog:"+eol;
      str += "has focus: "+hasFocus()+eol;

      return str+eol;
   }

   private JDRCanvas canvas;
   private JDRFrame frame_;
   private double hoffset = 0.0, voffset = 0.0;

   private LocationPane locationPane;
}
