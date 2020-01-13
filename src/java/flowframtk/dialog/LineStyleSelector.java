// File          : LineStyleSelector.java
// Description   : Dialog for setting line style
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
import com.dickimawbooks.jdr.marker.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for setting line style.
 * @author Nicola L C Talbot
 */

public class LineStyleSelector extends JDRSelector
{
   public LineStyleSelector(FlowframTk application)
   {
      super(application,application.getResources().getString("linestyle.title"),
            true,false);

      application.enableHelpOnButton(help, "pathstyle");

      // line style selection panel

      linestylePanel = new LineStylePanel(this);

      linestylePanel.setBorder(
         BorderFactory.createLoweredBevelBorder());
      setToMain(linestylePanel);

      pack();
      setLocationRelativeTo(application);
   }

   public void initialise()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      setStroke(mainPanel.getSelectedStroke());
      super.initialise();
   }

   public void okay()
   {
      int joinStyle = linestylePanel.getJoinStyle();

      if (joinStyle == BasicStroke.JOIN_MITER
        && linestylePanel.getEnteredMitreLimit() < 1.0)
      {
         getResources().error(this,
            getResources().getString("error.invalid_mitre_limit"));
         return;
      }

      JDRFrame mainPanel = application_.getCurrentFrame();
      mainPanel.setSelectedStroke(getStroke());
      super.okay();
   }

   public JDRBasicStroke getStroke()
   {
      if (linestylePanel == null)
      {
         return super.getStroke();
      }

      return linestylePanel.getStroke(getCanvasGraphics());
   }

   public void setStroke(JDRBasicStroke stroke)
   {
      linestylePanel.setStroke(stroke);
   }

   public void setDefaults()
   {
      linestylePanel.setDefaults();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "LineStyleSelector:"+eol;

      return str+super.info();
   }

   private LineStylePanel linestylePanel;

}
