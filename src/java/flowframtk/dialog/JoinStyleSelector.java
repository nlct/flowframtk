// File          : JoinStyleSelector.java
// Description   : Dialog for setting join styles
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
package com.dickimawbooks.flowframtk.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for setting join style.
 * @author Nicola L C Talbot
 */

public class JoinStyleSelector extends JDRSelector
{
   public JoinStyleSelector(FlowframTk application)
   {
      super(application,application.getResources().getString("linestyle.join"),
            true);

/*
      Dimension dim = getSize();
      dim.height=220;
      setSize(dim);
*/

      application.enableHelpOnButton(help, "sec:joinstyle");

      // line width selection panel

      joinStylePanel = new JoinStylePanel(this);

      joinStylePanel.setBorder(
         BorderFactory.createLoweredBevelBorder());

      setToMain(joinStylePanel);
   }

   public void initialise()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      setStroke(mainPanel.getSelectedStroke());
      super.initialise();
   }

   public void okay()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();

      int style = getJoinStyle();

      if (style == BasicStroke.JOIN_MITER)
      {
         mainPanel.setSelectedJoinStyle(style, getMitreLimit());
      }
      else
      {
         mainPanel.setSelectedJoinStyle(style);
      }

      super.okay();
   }

   public int getJoinStyle()
   {
      return joinStylePanel.getJoinStyle();
   }

   public double getMitreLimit()
   {
      return joinStylePanel.getMitreLimit();
   }

   public JDRBasicStroke getStroke()
   {
      if (joinStylePanel == null)
      {
         return application_.getCurrentStroke();
      }

      JDRBasicStroke stroke = joinStylePanel.getStroke();

      if (stroke == null)
      {
         return application_.getCurrentStroke();
      }

      return stroke;
   }

   public void setStroke(JDRBasicStroke stroke)
   {
      joinStylePanel.setStroke(stroke);
   }

   public void setDefaults()
   {
      joinStylePanel.setDefaults();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "JoinStyleSelector:"+eol;

      return str+super.info();
   }

   private JoinStylePanel joinStylePanel;
}
