// File          : FontAnchorSelector.java
// Description   : Dialog for setting font anchor
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
 * Dialog for setting font anchor.
 * @author Nicola L C Talbot
 */

public class FontAnchorSelector extends JDRSelector
{
   public FontAnchorSelector(FlowframTk application)
   {
      super(application,application.getResources().getMessage("font.anchor"),
         false,true,true, "sec:fontanchor");

/*
      Dimension dim = getSize();
      dim.height=220;
      setSize(dim);
*/

      // anchor panel

      anchorPanel = new FontAnchorPanel(this);
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
         setAnchor(text.getHAlign(), text.getVAlign());
      }
      else
      {
         setAnchor(application_.getCurrentPGFHAlign(),
                   application_.getCurrentPGFVAlign());
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
      mainPanel.setSelectedAnchor(getHalign(), getValign());
      super.okay();
   }

   public void setAnchor(int halign, int valign)
   {
      anchorPanel.setHalign(halign);
      anchorPanel.setValign(valign);
   }

   public int getHalign()
   {
      return anchorPanel.getHalign();
   }

   public int getValign()
   {
      return anchorPanel.getValign();
   }

   public void setDefaults()
   {
      anchorPanel.setDefaults();
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "";

      str = "FontAnchorSelector:"+eol;
      str += "text: "+text+eol;

      return str+super.info();
   }

   private FontAnchorPanel anchorPanel;

   private JDRTextual text=null;
}
