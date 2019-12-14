// File          : LineWidthSelector.java
// Description   : Dialog for setting line width
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
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for setting line width.
 * @author Nicola L C Talbot
 */

public class LineWidthSelector extends JDRSelector
{
   public LineWidthSelector(FlowframTk application)
   {
      super(application,
            application.getResources().getString("linestyle.thickness"),
            true);

/*
      Dimension dim = getSize();
      dim.height=220;
      setSize(dim);
*/

      application.enableHelpOnButton(help, "sec:penwidth");

      // line width selection panel

      linewidthPanel = new LineWidthPanel(this);

      linewidthPanel.setBorder(
         BorderFactory.createLoweredBevelBorder());
      setToMain(linewidthPanel);
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
      mainPanel.setSelectedLineWidth(getPenWidth());
      super.okay();
   }

   public JDRLength getPenWidth()
   {
      if (linewidthPanel == null)
      {
         return new JDRLength(getResources().getMessageDictionary(),
           1.0, JDRUnit.bp);
      }

      return linewidthPanel.getPenWidth();
   }

   public JDRBasicStroke getStroke()
   {
      if (linewidthPanel == null)
      {
         return application_.getCurrentStroke();
      }

      return linewidthPanel.getStroke();
   }

   public void setStroke(JDRBasicStroke stroke)
   {
      linewidthPanel.setStroke(stroke);
   }

   public void setDefaults()
   {
      linewidthPanel.setDefaults();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "LineWidthSelector:"+eol;

      return str+super.info();
   }

   private LineWidthPanel linewidthPanel;
}
