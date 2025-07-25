// File        : JDRButtonStyle.java
// Description : button styles
// Date        : 2015-09-16
// Author      : Nicola L.C. Talbot
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

import java.net.URL;
import java.io.FileNotFoundException;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

import com.dickimawbooks.texjavahelplib.TJHAbstractAction;
import com.dickimawbooks.texjavahelplib.IconSet;

public class JDRButtonStyle
{
   public JDRButtonStyle(String name, String location, boolean isCompact)
   {
      this(name, location, isCompact, false, false, true, true, 
           JDRButtonStyleDisplayType.ICON_ONLY,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, boolean useSmallIcons,
            String location, boolean isCompact)
   {
      this(name, useSmallIcons, location, isCompact, false, false, true, true, 
           JDRButtonStyleDisplayType.ICON_ONLY,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, String location, String presslocation,
       boolean isCompact)
   {
      this(name, location, presslocation, isCompact, false, false, true, true,
           JDRButtonStyleDisplayType.ICON_ONLY,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, boolean useSmallIcons,
       String location, String presslocation, boolean isCompact)
   {
      this(name, useSmallIcons, location, presslocation,
           isCompact, false, false, true, true,
           JDRButtonStyleDisplayType.ICON_ONLY,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, String location)
   {
      this(name, location, false, false, false, true, true,  
           JDRButtonStyleDisplayType.ICON_ONLY,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, boolean useSmallIcons, String location)
   {
      this(name, useSmallIcons, location, false, false, false, true, true,  
           JDRButtonStyleDisplayType.ICON_ONLY,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, String location, String presslocation)
   {
      this(name, location, presslocation,
           false, false, false, true, true, JDRButtonStyleDisplayType.ICON_ONLY,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, boolean useSmallIcons,
            String location, String presslocation)
   {
      this(name, useSmallIcons, location, presslocation,
           false, false, false, true, true, JDRButtonStyleDisplayType.ICON_ONLY,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, String location, boolean isCompact,
     boolean paintBorder, boolean fillArea, boolean hasDownIcon,
     boolean hasRolloverIcon)
   {
      this(name, location, isCompact, paintBorder, fillArea, hasDownIcon,
           hasRolloverIcon, 
           JDRButtonStyleDisplayType.ICON_ONLY,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, boolean useSmallIcons,
     String location, boolean isCompact,
     boolean paintBorder, boolean fillArea, boolean hasDownIcon,
     boolean hasRolloverIcon)
   {
      this(name, useSmallIcons, location, isCompact, paintBorder,
           fillArea, hasDownIcon, hasRolloverIcon, 
           JDRButtonStyleDisplayType.ICON_ONLY,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, String location, String presslocation,
     boolean isCompact, boolean paintBorder, boolean fillArea,
     boolean hasDownIcon, boolean hasRolloverIcon)
   {
      this(name, location, presslocation, isCompact,
           paintBorder, fillArea, hasDownIcon,
           hasRolloverIcon,  
           JDRButtonStyleDisplayType.ICON_ONLY,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, boolean useSmallIcons,
     String location, String presslocation,
     boolean isCompact, boolean paintBorder, boolean fillArea,
     boolean hasDownIcon, boolean hasRolloverIcon)
   {
      this(name, useSmallIcons, location, presslocation, isCompact,
           paintBorder, fillArea, hasDownIcon,
           hasRolloverIcon,  
           JDRButtonStyleDisplayType.ICON_ONLY,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, String location, boolean isCompact,
     boolean paintBorder, boolean fillArea, boolean hasDownIcon,
     boolean hasRolloverIcon, JDRButtonStyleDisplayType display)
   {
      this(name, location, isCompact, paintBorder, fillArea, hasDownIcon,
           hasRolloverIcon, display,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, boolean useSmallIcons,
     String location, boolean isCompact,
     boolean paintBorder, boolean fillArea, boolean hasDownIcon,
     boolean hasRolloverIcon, JDRButtonStyleDisplayType display)
   {
      this(name, useSmallIcons, location, isCompact, paintBorder, fillArea, hasDownIcon,
           hasRolloverIcon, display,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, String location, String presslocation,
     boolean isCompact, boolean paintBorder,
     boolean fillArea, boolean hasDownIcon,
     boolean hasRolloverIcon, JDRButtonStyleDisplayType display)
   {
      this(name, location, presslocation, isCompact, paintBorder,
           fillArea, hasDownIcon, hasRolloverIcon, display,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, boolean useSmallIcons,
     String location, String presslocation,
     boolean isCompact, boolean paintBorder,
     boolean fillArea, boolean hasDownIcon,
     boolean hasRolloverIcon, JDRButtonStyleDisplayType display)
   {
      this(name, useSmallIcons, location, presslocation, isCompact, paintBorder,
           fillArea, hasDownIcon, hasRolloverIcon, display,
           SwingConstants.TRAILING, SwingConstants.CENTER);
   }

   public JDRButtonStyle(String name, String location, boolean isCompact,
     boolean paintBorder, boolean fillArea, boolean hasDownIcon,
     boolean hasRolloverIcon, JDRButtonStyleDisplayType display, int hPos, int vPos)
   {
      this(name, location, null, isCompact,
           paintBorder, fillArea, hasDownIcon,
           hasRolloverIcon, display, hPos, vPos);
   }

   public JDRButtonStyle(String name, boolean useSmallIcons,
     String location, boolean isCompact,
     boolean paintBorder, boolean fillArea, boolean hasDownIcon,
     boolean hasRolloverIcon, JDRButtonStyleDisplayType display, int hPos, int vPos)
   {
      this(name, useSmallIcons, location, null, isCompact,
           paintBorder, fillArea, hasDownIcon,
           hasRolloverIcon, display, hPos, vPos);
   }

   public JDRButtonStyle(String name, String location,
     String presslocation, boolean isCompact,
     boolean paintBorder, boolean fillArea, boolean hasDownIcon,
     boolean hasRolloverIcon, JDRButtonStyleDisplayType display, int hPos, int vPos)
   {
      this(name, false, location, presslocation, isCompact, paintBorder,
       fillArea, hasDownIcon, hasRolloverIcon, display, hPos, vPos);
   }

   public JDRButtonStyle(String name, boolean useSmallIcons, String location,
     String presslocation, boolean isCompact,
     boolean paintBorder, boolean fillArea, boolean hasDownIcon,
     boolean hasRolloverIcon, JDRButtonStyleDisplayType display, int hPos, int vPos)
   {
      this.name = name;
      this.useSmallIcons = useSmallIcons;
      this.location = location;
      this.presslocation = presslocation;
      this.isCompact = isCompact;
      this.paintBorder = paintBorder;
      this.fillArea = fillArea;
      this.hasDownIcon = hasDownIcon;
      this.hasRolloverIcon = hasRolloverIcon;
      this.horizontalPosition = hPos;
      this.verticalPosition = vPos;

      switch (display)
      {
         case ICON_ONLY:
         case TEXT_ONLY:
         case ICON_TEXT:
         break;
         default:
            throw new IllegalArgumentException("Invalid display value "+display);
      }

      this.display = display;
   }

   public boolean isIconOnly()
   {
      return display == JDRButtonStyleDisplayType.ICON_ONLY;
   }

   public boolean isCompact()
   {
      return isCompact;
   }

   public boolean hasBorderPainted()
   {
      return paintBorder;
   }

   public boolean hasAreaFilled()
   {
      return fillArea;
   }

   public String getName()
   {
      return name;
   }

   public String getLocation()
   {
      return location;
   }

   public JDRButtonStyleDisplayType getDisplayStyle()
   {
      return display;
   }

   public IconSet getIconSet(JDRResources resources, String base)
   {
      IconSet icSet = resources.getHelpLib().getHelpIconSet(location+"/"+base,
         useSmallIcons);

      if (icSet == null)
      {
         icSet = resources.getHelpLib().getHelpIconSet(base,
            useSmallIcons);
      }

      if (presslocation != null)
      {
         Icon ic = null;

         if (useSmallIcons)
         {
            ic = resources.getHelpLib().getSmallIcon(presslocation+"/"+base);
         }
         else
         {
            ic = resources.getHelpLib().getLargeIcon(presslocation+"/"+base);
         }

         if (ic != null)
         {
            icSet.setPressedIcon(ic);
            icSet.setSelectedIcon(ic);
         }
      }

      return icSet;
   }

   public ImageIcon getPressIcon(JDRResources resources, String base)
   {
      return resources.appIcon(presslocation+"/"+base+"_pressed.png");
   }

   public ImageIcon getUpIcon(JDRResources resources, String base)
   {
      return resources.appIcon(location+"/"+base+".png");
   }

   public ImageIcon getDownIcon(JDRResources resources, String base)
   {
      if (hasDownIcon)
      {
         URL imgURL = getClass().getResource(
            resources.getIconDir()+"/"+location+"/"+base+"_selected.png");

         return imgURL == null ? null : new ImageIcon(imgURL);
      }

      return null;
   }

   public ImageIcon getRolloverIcon(JDRResources resources, String base)
   {
      if (hasRolloverIcon)
      {
         URL imgURL = getClass().getResource(
            resources.getIconDir()+"/"+location+"/"+base+"_rollover.png");

         return imgURL == null ? null : new ImageIcon(imgURL);
      }

      return null;
   }

   @Deprecated
   public ImageIcon getDisabledIcon(JDRResources resources, String base)
   {
      URL imgURL = getClass().getResource(
         resources.getIconDir()+"/"+location+"/"+base+"dis.png");

      return imgURL == null ? null : new ImageIcon(imgURL);
   }

   public JDRButton createButton(JDRResources resources, String base,
      ActionListener listener, String tooltipText)
   {
      return createButton(resources, resources.getMessage("button."+base), base,
         listener, tooltipText);
   }

   public JDRButton createButton(JDRResources resources, String text, 
      String base, ActionListener listener, String tooltipText)
   {
      JDRButton button;

      if (display == JDRButtonStyleDisplayType.TEXT_ONLY)
      {
         button = new JDRButton(text, listener, tooltipText);
      }
      else
      {
         IconSet icSet = getIconSet(resources, base);

         if (icSet == null)
         {
            try
            {
               throw new FileNotFoundException(resources.getMessage(
                "error.iconset_not_found", base));
            }
            catch (FileNotFoundException e)
            {
               resources.warning(e.getMessage(), e);
            }

            button = new JDRButton(text, listener, tooltipText);
         }
         else
         {
            if (display == JDRButtonStyleDisplayType.ICON_ONLY)
            {
               button = new JDRButton(
                  icSet.getDefaultIcon(),
                  listener,
                  tooltipText);

               if (!paintBorder)
               {
                  button.setBorder(BorderFactory.createEmptyBorder());
               }

               if (tooltipText == null)
               {
                  button.setToolTipText(text);
               }
            }
            else //if (display == ICON_TEXT)
            {
               button = new JDRButton(text,
                  icSet.getDefaultIcon(),
                  listener,
                  tooltipText);

               button.setHorizontalTextPosition(horizontalPosition);
               button.setVerticalTextPosition(verticalPosition);
            }

            icSet.setButtonIcons(button);
         }
      }

      button.setBorderPainted(paintBorder);
      button.setContentAreaFilled(fillArea);

      if (paintBorder)
      {
         button.setMargin(new Insets(0, 0, 0, 0));
      }
      else
      {
         button.setMargin(new Insets(1, 1, 1, 1));
      }

      return button;
   }

   public JDRButton createButton(JDRResources resources, TJHAbstractAction action)
   {
      JDRButton button = new JDRButton(action);

      if (display == JDRButtonStyleDisplayType.TEXT_ONLY)
      {
         button.setIcon(null);
         button.setPressedIcon(null);
         button.setDisabledIcon(null);
         button.setRolloverIcon(null);
         button.setSelectedIcon(null);
      }
      else
      {
         if (presslocation != null)
         {
            IconSet icSet = action.getIconSet();
            String base = icSet.getBase();

            int idx = base.lastIndexOf("/");

            if (idx > -1)
            {
               base = base.substring(idx+1);
            }

            Icon ic = null;

            if (useSmallIcons)
            {
               ic = resources.getHelpLib().getSmallIcon(
                     presslocation+"/"+base);
            }
            else
            {
               ic = resources.getHelpLib().getLargeIcon(
                     presslocation+"/"+base);
            }

            if (ic != null)
            {
               button.setPressedIcon(ic);
               button.setSelectedIcon(ic);
            }
         }

         if (display == JDRButtonStyleDisplayType.ICON_ONLY)
         {
            if (!paintBorder)
            {
               button.setBorder(BorderFactory.createEmptyBorder());
            }
         }
         else //if (display == ICON_TEXT)
         {
             button.setHorizontalTextPosition(horizontalPosition);
             button.setVerticalTextPosition(verticalPosition);
         }
      }

      button.setBorderPainted(paintBorder);
      button.setContentAreaFilled(fillArea);

      if (paintBorder)
      {
         button.setMargin(new Insets(0, 0, 0, 0));
      }
      else
      {
         button.setMargin(new Insets(1, 1, 1, 1));
      }

      return button;
   }

   public JDRToggleButton createToggle(JDRResources resources,
      String text, String base, ActionListener listener, String tooltipText)
   {
      JDRToggleButton button;

      if (display == JDRButtonStyleDisplayType.TEXT_ONLY)
      {
         button = new JDRToggleButton(text, listener, tooltipText);
      }
      else
      {
         IconSet icSet = getIconSet(resources, base);

         if (display == JDRButtonStyleDisplayType.ICON_ONLY)
         {
            button = new JDRToggleButton(
               icSet.getDefaultIcon(),
               listener,
               tooltipText);

            if (!paintBorder)
            {
               button.setBorder(BorderFactory.createEmptyBorder());
            }

            if (tooltipText == null)
            {
               button.setToolTipText(text);
            }
         }
         else //if (display == ICON_TEXT)
         {
            button = new JDRToggleButton(text,
               icSet.getDefaultIcon(),
               listener,
               tooltipText);

             button.setHorizontalTextPosition(horizontalPosition);
             button.setVerticalTextPosition(verticalPosition);
         }

         icSet.setButtonIcons(button);
      }

      button.setBorderPainted(paintBorder);
      button.setContentAreaFilled(fillArea);

      if (paintBorder)
      {
         button.setMargin(new Insets(0, 0, 0, 0));
      }
      else
      {
         button.setMargin(new Insets(1, 1, 1, 1));
      }

      return button;
   }

   public JDRToggleButton createToggle(JDRResources resources,
      TJHAbstractAction action)
   {
      JDRToggleButton button = new JDRToggleButton(action);

      if (display == JDRButtonStyleDisplayType.TEXT_ONLY)
      {
         button.setIcon(null);
         button.setPressedIcon(null);
         button.setDisabledIcon(null);
         button.setRolloverIcon(null);
         button.setSelectedIcon(null);
      }
      else
      {
         if (presslocation != null)
         {
            IconSet icSet = action.getIconSet();
            String base = icSet.getBase();

            int idx = base.lastIndexOf("/");

            if (idx > -1)
            {
               base = base.substring(idx+1);
            }

            Icon ic = null;

            if (useSmallIcons)
            {
               ic = resources.getHelpLib().getSmallIcon(
                     presslocation+"/"+base);
            }
            else
            {
               ic = resources.getHelpLib().getLargeIcon(
                     presslocation+"/"+base);
            }

            if (ic != null)
            {
               button.setPressedIcon(ic);
               button.setSelectedIcon(ic);
            }
         }

         if (display == JDRButtonStyleDisplayType.ICON_ONLY)
         {
            if (!paintBorder)
            {
               button.setBorder(BorderFactory.createEmptyBorder());
            }
         }
         else //if (display == JDRButtonStyleDisplayType.ICON_TEXT)
         {
             button.setHorizontalTextPosition(horizontalPosition);
             button.setVerticalTextPosition(verticalPosition);
         }
      }

      button.setBorderPainted(paintBorder);
      button.setContentAreaFilled(fillArea);

      if (paintBorder)
      {
         button.setMargin(new Insets(0, 0, 0, 0));
      }
      else
      {
         button.setMargin(new Insets(1, 1, 1, 1));
      }

      return button;
   }

   public JDRToolButton createTool(JDRResources resources,
      String buttonText, String base,
      ActionListener listener, ButtonGroup group,
      boolean selected, String tooltipText)
   {
      JDRToolButton button;

      if (display == JDRButtonStyleDisplayType.TEXT_ONLY)
      {
         button = new JDRToolButton(
            buttonText, listener, group, selected, tooltipText);
      }
      else
      {
         IconSet icSet = getIconSet(resources, base);

         if (display == JDRButtonStyleDisplayType.ICON_ONLY)
         {
            button = new JDRToolButton(
               icSet.getDefaultIcon(),
               listener, group, selected,
               tooltipText);

            if (!paintBorder)
            {
               button.setBorder(BorderFactory.createEmptyBorder());
            }

            if (tooltipText == null)
            {
               button.setToolTipText(buttonText);
            }
         }
         else //if (display == ICON_TEXT)
         {
            button = new JDRToolButton(
               buttonText,
               icSet.getDefaultIcon(),
               listener, group, selected,
               tooltipText);

             button.setHorizontalTextPosition(horizontalPosition);
             button.setVerticalTextPosition(verticalPosition);
         }

         icSet.setButtonIcons(button);
      }

      button.setBorderPainted(paintBorder);
      button.setContentAreaFilled(fillArea);

      if (paintBorder)
      {
         button.setMargin(new Insets(0, 0, 0, 0));
      }
      else
      {
         button.setMargin(new Insets(1, 1, 1, 1));
      }

      return button;
   }

   public JDRToolButton createTool(JDRResources resources,
      TJHAbstractAction action, 
      ButtonGroup group, boolean selected)
   {
      JDRToolButton button = new JDRToolButton(action, group, selected);

      if (display == JDRButtonStyleDisplayType.TEXT_ONLY)
      {
         button.setIcon(null);
         button.setPressedIcon(null);
         button.setDisabledIcon(null);
         button.setRolloverIcon(null);
         button.setSelectedIcon(null);
      }
      else
      {
         if (presslocation != null)
         {
            IconSet icSet = action.getIconSet();
            String base = icSet.getBase();

            int idx = base.lastIndexOf("/");

            if (idx > -1)
            {
               base = base.substring(idx+1);
            }

            Icon ic = null;

            if (useSmallIcons)
            {
               ic = resources.getHelpLib().getSmallIcon(
                     presslocation+"/"+base);
            }
            else
            {
               ic = resources.getHelpLib().getLargeIcon(
                     presslocation+"/"+base);
            }

            if (ic != null)
            {
               button.setPressedIcon(ic);
               button.setSelectedIcon(ic);
            }
         }

         if (display == JDRButtonStyleDisplayType.ICON_ONLY)
         {
            if (!paintBorder)
            {
               button.setBorder(BorderFactory.createEmptyBorder());
            }
         }
         else //if (display == ICON_TEXT)
         {
             button.setHorizontalTextPosition(horizontalPosition);
             button.setVerticalTextPosition(verticalPosition);
         }
      }

      button.setBorderPainted(paintBorder);
      button.setContentAreaFilled(fillArea);

      if (paintBorder)
      {
         button.setMargin(new Insets(0, 0, 0, 0));
      }
      else
      {
         button.setMargin(new Insets(1, 1, 1, 1));
      }

      return button;
   }

   public DirectionButton createDirectionButton(JDRResources resources,
     String action, int direction)
   {
      return createDirectionButton(resources, action, direction, false);
   }

   public DirectionButton createDirectionButton(JDRResources resources,
     String name, int direction, boolean selected)
   {
      return new DirectionButton(getUpIcon(resources, name),
         getDownIcon(resources, name), direction, selected);
   }

   public String toString()
   {
      return String.format("%s[name=%s,location=%s,presslocation=%s,paintBorder=%s,fillArea=%s,hasDownIcon=%s,hasRolloverIcon=%s,isCompact=%s,display=%d,horizontalPosition=%d,verticalPosition=%d]", 
        getClass().getSimpleName(),
        name, location, presslocation, paintBorder, fillArea, 
        hasDownIcon, hasRolloverIcon, isCompact, display, horizontalPosition, verticalPosition);
   }

   private String name, location, presslocation;

   private boolean paintBorder, fillArea;

   private boolean hasDownIcon, hasRolloverIcon;

   private boolean isCompact, useSmallIcons;

   private JDRButtonStyleDisplayType display = JDRButtonStyleDisplayType.ICON_ONLY;

   private int horizontalPosition = SwingConstants.TRAILING;
   private int verticalPosition = SwingConstants.CENTER;

   private JDRResources resources;
}
