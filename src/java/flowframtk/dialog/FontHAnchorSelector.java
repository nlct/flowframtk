// File          : FontHAnchorSelector.java
// Description   : Dialog for setting font anchor
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
 * Dialog for setting font anchor (horizontal setting only).
 * @author Nicola L C Talbot
 */

public class FontHAnchorSelector extends JDRSelector
{
   public FontHAnchorSelector(FlowframTk application)
   {
      super(application,application.getResources().getString("font.hanchor"),
         false, true, true);

/*
      Dimension dim = getSize();
      dim.height=220;
      setSize(dim);
*/

      application.enableHelpOnButton(help, "sec:fontanchor");

      JPanel panel = new JPanel();

      // anchor panel

      anchorPanel = new FontHAnchorPanel(this);
      anchorPanel.setBorder(
         BorderFactory.createLoweredBevelBorder());

      setToMain(anchorPanel);
   }

   public void initialise()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();

      if (mainPanel != null)
      {
         text = mainPanel.getSelectedFont();
         setHAnchor(text.getHAlign());
      }
      else
      {
         setHAnchor(application_.getCurrentPGFHAlign());
      }
      super.initialise();
   }

   public String getSampleText()
   {
      if (text != null && !text.getText().equals(""))
      {
         return text.getText();
      }

      return super.getSampleText();
   }

   public void okay()
   {
      JDRFrame mainPanel = application_.getCurrentFrame();
      mainPanel.setSelectedHalign(getHalign());
      super.okay();
   }

   public void setHAnchor(int halign)
   {
      anchorPanel.setHalign(halign);
   }

   public int getHalign()
   {
      return anchorPanel.getHalign();
   }

   public void setDefaults()
   {
      anchorPanel.setDefaults();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "FontHAnchorSelector:"+eol;
      str += "text: "+text+eol;

      return str+super.info();
   }

   private FontHAnchorPanel anchorPanel;

   private JDRTextual text=null;
}
