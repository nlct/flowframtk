// File          : ArrowStyleSelector.java
// Description   : Dialog for setting marker styles
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
 * Dialog for setting marker style.
 * @see ArrowStyleDialog
 * @author Nicola L C Talbot
 */

public class ArrowStyleSelector extends JDRSelector
{
   public ArrowStyleSelector(FlowframTk application, int type)
   {
      super(application, 
            ArrowStylePanel.getTitle(application.getResources(), type), true);

      type_ = type;

      application.enableHelpOnButton(help, "markers");

      // arrow selection panel

      arrowPanel = new ArrowStylePanel(this, type);

      arrowPanel.setBorder(BorderFactory.createLoweredBevelBorder());
      setToMain(arrowPanel);

      pack();
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
      switch (type_)
      {
         case ArrowStylePanel.START :
         mainPanel.setSelectedStartArrow(
            arrowPanel.getMarker());
         break;
         case ArrowStylePanel.MID :
         mainPanel.setSelectedMidArrow(
            arrowPanel.getMarker());
         break;
         case ArrowStylePanel.END :
         mainPanel.setSelectedEndArrow(
            arrowPanel.getMarker());
         break;
         case ArrowStylePanel.ALL :
         mainPanel.setSelectedMarkers(
            arrowPanel.getMarker());
         break;
      }
     
      super.okay();
   }

   public JDRBasicStroke getStroke()
   {
      if (arrowPanel == null)
      {
         return super.getStroke();
      }

      return arrowPanel.getStroke();
   }

   public void setStroke(JDRBasicStroke stroke)
   {
      arrowPanel.setStroke((JDRBasicStroke)stroke.clone());
   }

   public void setDefaults()
   {
      arrowPanel.setDefaults();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "ArrowStyleSelector:"+eol;
      str += "type: "+type_+eol;

      return str+super.info();
   }

   private ArrowStylePanel arrowPanel;

   private int type_;
}
