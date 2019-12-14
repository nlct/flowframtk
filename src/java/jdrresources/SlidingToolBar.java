// File          : SlidingToolBar.java
// Purpose       : A sliding tool bar component
// Creation Date : 23rd May 2011
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

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
package com.dickimawbooks.jdrresources;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class SlidingToolBar extends JComponent 
   implements ActionListener,ChangeListener
{
   private Border viewportBorder;

   protected int unitIncrement = 10;

   protected int policy = AS_NEEDED;

   protected int orientation = HORIZONTAL;

   protected JViewport viewport;

   protected SlidingToolControl upComponent;

   protected SlidingToolControl downComponent;

   protected JDRResources resources;

   public static final int AS_NEEDED=0;
   public static final int ALWAYS=1;
   public static final int NEVER=2;
   public static final int HORIZONTAL=0;
   public static final int VERTICAL=1;

   public SlidingToolBar(JDRResources resources, 
      Component view, int policy, int orientation)
   {
      super();
      this.resources = resources;
      setLayout(new BorderLayout());

      setOrientation(orientation);
      setPolicy(policy);
      setViewport(createViewport());
      setUpComponent(createUpComponent());
      setDownComponent(createDownComponent());

      if (view != null)
      {
         setViewportView(view);
      }
   }

   public SlidingToolBar(JDRResources resources, Component view)
   {
      this(resources, view, AS_NEEDED, HORIZONTAL);
   }

   public SlidingToolBar(JDRResources resources, Component view, int orientation)
   {
      this(resources, view, AS_NEEDED, orientation);
   }

   public void setOrientation(int orientation)
   {
      switch (orientation)
      {
         case HORIZONTAL:
         case VERTICAL:
            break;
         default:
            throw new IllegalArgumentException(
               "orientation must be one of: VERTICAL, HORIZONTAL");
      }

      this.orientation = orientation;

      if (upComponent != null)
      {
         remove(upComponent);
         add(upComponent, orientation == HORIZONTAL ? "East" : "North");
      }
   }

   public int getOrientation()
   {
      return orientation;
   }

   public void setPolicy(int policy)
   {
      switch (policy)
      {
         case AS_NEEDED:
         case ALWAYS:
         case NEVER:
            break;
         default:
            throw new IllegalArgumentException(
               "policy must be one of: AS_NEEDED, ALWAYS, NEVER");
      }

      this.policy = policy;
      revalidate();
   }

   public void setViewport(JViewport viewport)
   {
      if (this.viewport != null)
      {
         remove(this.viewport);
         this.viewport.removeChangeListener(this);
      }

      this.viewport = viewport;

      if (viewport != null)
      {
         add(viewport, "Center");
         viewport.addChangeListener(this);
      }
   }

   public JViewport getViewport()
   {
      return viewport;
   }

   public void setViewportView(Component view)
   {
      viewport.setView(view);
   }

   public Component getViewportView()
   {
      if (viewport == null) return null;

      return viewport.getView();
   }

   public void setUpComponent(SlidingToolControl comp)
   {
      if (this.upComponent != null)
      {
         remove(this.upComponent);
         this.upComponent.removeSlidingToolBar();
      }

      this.upComponent = comp;

      if (comp != null)
      {
         add(comp, orientation==HORIZONTAL? "West" : "North");
         comp.setSlidingToolBar(this);
      }

      checkControls();
   }

   public SlidingToolControl getUpComponent()
   {
      return upComponent;
   }

   public void setDownComponent(SlidingToolControl comp)
   {
      if (this.downComponent != null)
      {
         remove(this.downComponent);
         this.downComponent.removeSlidingToolBar();
      }

      this.downComponent = comp;

      if (comp != null)
      {
         add(comp, orientation==HORIZONTAL? "East" : "South");
         comp.setSlidingToolBar(this);
      }

      checkControls();
   }

   public SlidingToolControl getDownComponent()
   {
      return downComponent;
   }

   protected JViewport createViewport()
   {
      return new JViewport();
   }

   protected SlidingToolControl createUpComponent()
   {
      return new SlidingToolControl(resources, this, SlidingToolControl.UP);
   }

   protected SlidingToolControl createDownComponent()
   {
      return new SlidingToolControl(resources, this, SlidingToolControl.DOWN);
   }

   protected void checkControls()
   {
      if (policy == ALWAYS || policy == NEVER || 
          getViewportView() == null)
      {
         if (upComponent != null)
         {
             upComponent.setVisible(policy == ALWAYS);
         }

         if (downComponent != null)
         {
             downComponent.setVisible(policy == ALWAYS);
         }

         return;
      }

      Dimension viewExtent = viewport.getExtentSize();

      Point pos = viewport.getViewPosition();

      Dimension prefSize = getViewportView().getPreferredSize();

      Dimension upSize = (upComponent == null ? 
         new Dimension(0,0) : upComponent.getPreferredSize());

      Dimension downSize = (downComponent == null ? 
         new Dimension(0,0) : downComponent.getPreferredSize());

      boolean showUpControl, showDownControl;

      if (orientation == HORIZONTAL)
      {
         showUpControl = (viewExtent.width < prefSize.width);
         showDownControl = showUpControl;

         if (pos.x == 0)
         {
            showUpControl = false;
         }

         if (pos.x+viewExtent.width >= prefSize.width)
         {
            showDownControl = false;
         }
      }
      else
      {
         showUpControl = (viewExtent.height < prefSize.height);
         showDownControl = showUpControl;

         if (pos.y == 0)
         {
            showUpControl = false;
         }

         if (pos.y+viewExtent.height >= prefSize.height)
         {
            showDownControl = false;
         }
      }

      if (upComponent != null)
      {
         upComponent.setVisible(showUpControl);
      }

      if (downComponent != null)
      {
         downComponent.setVisible(showDownControl);
      }
   }

   public int getUnitIncrement()
   {
      return unitIncrement;
   }

   public void setUnitIncrement(int increment)
   {
      if (increment <= 0)
      {
         throw new IllegalArgumentException(
            "Unit increment must be > 0");
      }

      unitIncrement = increment;
   }

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource();
      String action = evt.getActionCommand();

      if (action == null || viewport == null)
      {
         return;
      }

      Rectangle viewRect = viewport.getViewRect();
      Dimension prefSize = getViewportView().getPreferredSize();

      int increment = 0;

      if (action.equals("scrollUnitUp"))
      {
         increment = -unitIncrement;
      }
      else if (action.equals("scrollUnitDown"))
      {
         increment = unitIncrement;
      }
      else if (action.equals("scrollBlockUp"))
      {
         increment = -(orientation == HORIZONTAL ?
            viewRect.width : viewRect.height);
      }
      else if (action.equals("scrollBlockDown"))
      {
         increment = (orientation == HORIZONTAL ?
            viewRect.width : viewRect.height);
      }
      else if (action.equals("scrollEndUp"))
      {
         increment = -(orientation == HORIZONTAL ?
            prefSize.width : prefSize.height);
      }
      else if (action.equals("scrollEndDown"))
      {
         increment = (orientation == HORIZONTAL ?
            prefSize.width : prefSize.height);
      }

      if (orientation == HORIZONTAL)
      {
         viewRect.x += increment;

         if (viewRect.x + viewRect.width > prefSize.width)
         {
            viewRect.x = (prefSize.width-viewRect.width);
         }

         if (viewRect.x < 0)
         {
            viewRect.x = 0;
         }
      }
      else
      {
         viewRect.y += increment;

         if (viewRect.y + viewRect.height > prefSize.height)
         {
            viewRect.y = (prefSize.height-viewRect.height);
         }

         if (viewRect.y < 0)
         {
            viewRect.y = 0;
         }
      }

      viewport.setViewPosition(new Point(viewRect.x, viewRect.y));
      checkControls();
   }

   public void stateChanged(ChangeEvent evt)
   {
      if (evt.getSource() == getViewport())
      {
         checkControls();
      }
   }
}
