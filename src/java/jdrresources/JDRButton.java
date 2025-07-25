// File        : JDRButton.java
// Description : Toolbar buttons
// Date        : 5th June 2008
// Author      : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

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
package com.dickimawbooks.jdrresources;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

import com.dickimawbooks.texjavahelplib.IconSet;
import com.dickimawbooks.texjavahelplib.TJHAbstractAction;

/**
 * Application button.
 * @author Nicola L C Talbot
 */
public class JDRButton extends JButton
{
   public JDRButton(Icon icon_up, Icon icon_down, 
                     Icon icon_rollover, Icon icon_disabled,
                     ActionListener listener)
   {
      this(icon_up, icon_down, icon_rollover, icon_disabled, listener, null);
   }

   public JDRButton(Icon icon_up, Icon icon_down, 
                     Icon icon_rollover,
                     ActionListener listener)
   {
      this(icon_up, icon_down, icon_rollover, null, listener, null);
   }

   public JDRButton(Icon icon_up, Icon icon_down, 
                     Icon icon_rollover, Icon icon_disabled,
                     ActionListener listener, String info)
   {
      super();

      if (icon_up != null)
      {
         setIcon(icon_up);
      }

      if (icon_down != null)
      {
         setPressedIcon(icon_down);
         setSelectedIcon(icon_down);
      }

      if (icon_rollover != null)
      {
         setRolloverIcon(icon_rollover);
      }

      if (icon_disabled != null)
      {
         setDisabledIcon(icon_disabled);
      }

      if (listener != null)
      {
         addActionListener(listener);
      }

      if (info != null)
      {
         setToolTipText(info);
      }
   }

   public JDRButton(String text, Icon icon_up, Icon icon_down, 
                     Icon icon_rollover, Icon icon_disabled,
                     ActionListener listener, String info)
   {
      super(text);

      if (icon_up != null)
      {
         setIcon(icon_up);
      }

      if (icon_down != null)
      {
         setPressedIcon(icon_down);
         setSelectedIcon(icon_down);
      }

      if (icon_rollover != null)
      {
         setRolloverIcon(icon_rollover);
      }

      if (icon_disabled != null)
      {
         setDisabledIcon(icon_disabled);
      }

      if (listener != null)
      {
         addActionListener(listener);
      }

      if (info != null)
      {
         setToolTipText(info);
      }
   }

   public JDRButton(Icon icon_up, Icon icon_down, 
                     Icon icon_rollover,
                     ActionListener listener, String info)
   {
      this(icon_up, icon_down, icon_rollover, null, listener, info);
   }

   public JDRButton(String text, ActionListener listener, String info)
   {
      super(text);

      if (listener != null)
      {
         addActionListener(listener);
      }

      if (info != null)
      {
         setToolTipText(info);
      }
   }

   public JDRButton(String text, Icon icon,
                    ActionListener listener, String info)
   {
      super(text);

      if (icon != null)
      {
         setIcon(icon);
      }

      if (listener != null)
      {
         addActionListener(listener);
      }

      if (info != null)
      {
         setToolTipText(info);
      }
   }

   public JDRButton(Icon icon, ActionListener listener, String info)
   {
      super();

      if (icon != null)
      {
         setIcon(icon);
      }

      if (listener != null)
      {
         addActionListener(listener);
      }

      if (info != null)
      {
         setToolTipText(info);
      }
   }

   public JDRButton(TJHAbstractAction action)
   {
      super(action);

      IconSet icSet = action.getIconSet();

      if (icSet != null)
      {
         icSet.setButtonIcons(this);
      }
   }
}
