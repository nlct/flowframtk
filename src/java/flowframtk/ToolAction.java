// File          : ToolAction.java
// Description   : tool changing action
// Date          : 2014-04-28
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2014-2025 Nicola L.C. Talbot

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

public class ToolAction extends FlowframTkAction
  implements FlowframTkActionListener
{
   public ToolAction(FlowframTk application, int toolId)
   {
      super(application, ACTIONS[toolId], null);
      setListener(this);
      this.toolId = toolId;
      setAccelerator(getResources().getAccelerator("tools."+ACTIONS[toolId]));
   }
   
   public void doAction(FlowframTkAction action, ActionEvent evt)
   {
      getApplication().setTool(toolId);
   }

   public static JDRToolButtonItem createToolButtonItem(FlowframTk application,
     String parentId,
     JComponent comp, JMenu menu, ToolButtonGroup buttonGroup,
     int toolId)
   {
      return createToolButtonItem(application, parentId,
         false, comp, menu, buttonGroup,
         toolId);
   }

   public static JDRToolButtonItem createToolButtonItem(FlowframTk application,
     String parentId, boolean selected,
     JComponent comp, JMenu menu, ToolButtonGroup buttonGroup,
     int toolId)
   {
      ToolAction action = new ToolAction(application, toolId);

      action.setRequiresCanvas(true);
      action.setValidDuringIO(true);

      application.addAppAction(action);

      JDRToolButtonItem item = new JDRToolButtonItem(application.getResources(),
         parentId, ACTIONS[toolId], action, selected, buttonGroup,
         comp, menu);

      action.setActionButton(item);

      return item;
   }

   public static void setTool(FlowframTk application,
       ToolButtonGroup toolButtonGroup, int tool)
   {
      String name = ACTIONS[tool];

      JDRToolButtonItem item = toolButtonGroup.getButtonItem(name);

      if (item != null && !item.isSelected())
      {
         item.setSelected(true);
      }

      String infoId;

      if (tool == ACTION_OPEN_CURVE || tool == ACTION_OPEN_LINE)
      {
         infoId = "info.open_path";
      }
      else if (tool == ACTION_CLOSED_CURVE || tool == ACTION_CLOSED_LINE)
      {
         infoId = "info.closed_path";
      }
      else
      {
         infoId = "info."+name;
      }

      application.setStatusInfo(application.getResources().getMessage(infoId),
        HELP_ID[tool]);
   }

   private int toolId;

   private static final String[] ACTIONS =
    {"select", "open_line", "closed_line", "open_curve",
     "closed_curve", "rectangle", "ellipse", "textarea", "math"};

   private static final String[] HELP_ID =
    {"sec:selectobjects", "sec:newlinepath", "sec:newlinepath", "sec:newcurvepath",
     "sec:newcurvepath", "sec:rectangles", "sec:ellipses", "sec:newtext", "sec:newtext"};
}

