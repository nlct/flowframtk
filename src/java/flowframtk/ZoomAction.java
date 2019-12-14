// File          : ZoomAction.java
// Description   : zoom changing action
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

import java.awt.event.ActionEvent;
import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;

public class ZoomAction extends FlowframTkAction
  implements FlowframTkActionListener
{
   public ZoomAction(FlowframTk application, double factor)
   {
      super(application, "zoom_"+factor, null);
      setListener(this);
      this.factor = factor;
   }
   
   public void doAction(FlowframTkAction action, ActionEvent evt)
   {
      action.getFrame().setMagnification(factor);
   }

   public static JRadioButtonMenuItem createRadioMenuItem(FlowframTk application,
     String parentId, String name,
     JMenu menu, ButtonGroup buttonGroup,
     double zoomFactor)
   {
      return createRadioMenuItem(application, parentId, name,
         zoomFactor == 1.0, "tooltip."+name, menu, buttonGroup,
         zoomFactor);
   }

   public static JRadioButtonMenuItem createRadioMenuItem(FlowframTk application,
     String parentId, String name, boolean selected,
     String tooltipId,
     JMenu menu, ButtonGroup buttonGroup,
     double zoomFactor)
   {
      String menuId = (parentId == null ? name : parentId+"."+name);

      ZoomAction action = new ZoomAction(application, zoomFactor);

      action.setRequiresCanvas(true);
      action.setValidDuringIO(true);

      application.addAppAction(action);

      JRadioButtonMenuItem item = action.createRadioButtonMenuItem(menuId,
         buttonGroup, selected, tooltipId);

      menu.add(item);

      return item;
   }

   private double factor;
}

