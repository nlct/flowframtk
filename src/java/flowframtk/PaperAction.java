// File          : PaperAction.java
// Description   : Paper size changing action
// Date          : 2014-04-28
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
package com.dickimawbooks.flowframtk;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;

public class PaperAction extends FlowframTkAction
  implements FlowframTkActionListener
{
   public PaperAction(FlowframTk application, JDRPaper portrait,
     JDRPaper landscape)
   {
      super(application, "paper_"+portrait.getName(), null);
      setListener(this);
      this.portraitPaper = portrait;
      this.landscapePaper = landscape;
   }
   
   public void doAction(FlowframTkAction action, ActionEvent evt)
   {
      action.getFrame().setPaperSize(
        getApplication().isPortrait() ? portraitPaper : landscapePaper);
   }

   public static JRadioButtonMenuItem createRadioMenuItem(FlowframTk application,
     String parentId, String name,
     JMenu menu, ButtonGroup buttonGroup,
    JDRPaper portrait, JDRPaper landscape)
   {
      return createRadioMenuItem(application, parentId, name,
         false, "tooltip."+name, menu, buttonGroup,
         portrait, landscape);
   }

   public static JRadioButtonMenuItem createRadioMenuItem(FlowframTk application,
     String parentId, String name, boolean selected,
     String tooltipId,
     JMenu menu, ButtonGroup buttonGroup,
    JDRPaper portrait, JDRPaper landscape)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      PaperAction action = new PaperAction(application,
        portrait, landscape);

      action.setRequiresCanvas(true);
      action.setValidDuringIO(false);

      application.addAppAction(action);

      JRadioButtonMenuItem item = action.createRadioButtonMenuItem(menuId,
         buttonGroup, selected, tooltipId);

      menu.add(item);

      return item;
   }

   private JDRPaper portraitPaper, landscapePaper;
}

