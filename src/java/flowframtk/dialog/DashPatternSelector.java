// File          : DashPatternSelector.java
// Description   : Dialog for setting dash style
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
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Dialog for setting dash style.
 * @author Nicola L C Talbot
 */

public class DashPatternSelector extends JDRSelector
{
   public DashPatternSelector(FlowframTk application)
   {
      super(application,application.getResources().getMessage("dashpattern.title"),
            true, "sec:dashpattern");

      // dash pattern selection panel

      dashpatternPanel = new DashPatternPanel(this);

      dashpatternPanel.setBorder(
         BorderFactory.createLoweredBevelBorder());
      setToMain(dashpatternPanel);
   }

   public void initialise()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      setStroke((JDRBasicStroke)mainPanel.getSelectedStroke().clone());
      super.initialise();
   }

   public void okay()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      mainPanel.setSelectedDashPattern(getDashPattern(mainPanel.getCanvasGraphics()));
      super.okay();
   }

   public DashPattern getDashPattern(CanvasGraphics cg)
   {
      if (dashpatternPanel == null)
      {
         return null;
      }

      DashPattern pat = dashpatternPanel.getDashPattern(cg);

      if (pat == null)
      {
         return new DashPattern(cg);
      }

      return pat;
   }

   public void setStroke(JDRBasicStroke stroke)
   {
      dashpatternPanel.setStroke(stroke);
   }

   public JDRBasicStroke getStroke()
   {
      JDRFrame frame = application_.getCurrentFrame();

      JDRBasicStroke stroke = null;

      if (frame != null)
      {
         stroke = (JDRBasicStroke)frame.getSelectedStroke().clone();
      }
      
      if (stroke == null)
      {
         stroke = (JDRBasicStroke)application_.getCurrentStroke().clone();
      }

      stroke.setDashPattern(getDashPattern(stroke.getCanvasGraphics()));

      return stroke;
   }

   public void setDefaults()
   {
      dashpatternPanel.setDefaults();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "DashPatternSelector:"+eol;

      return str+super.info();
   }

   private DashPatternPanel dashpatternPanel;
}
