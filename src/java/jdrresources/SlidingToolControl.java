// File          : SlidingToolControl.java
// Purpose       : Control for sliding tool bar
// Date          : 23rd May 2011
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

public class SlidingToolControl extends JComponent
{
   protected AbstractButton block, unit, endPoint;

   protected SlidingToolBar slidingToolBar;

   protected int direction = UP;

   public static final int UP=0, DOWN=1;

   private JDRResources resources;

   public SlidingToolControl(JDRResources resources,
      SlidingToolBar slidingToolBar, int direction)
   {
      super();
      this.resources = resources;

      setSlidingToolBar(slidingToolBar);
      setDirection(direction);

      if (JDRResources.getButtonStyle().isCompact())
      {
         if (direction == UP)
         {
            setEndControl(createEndControl());
            setBlockControl(createBlockControl());
            setUnitControl(createUnitControl());
         }
         else
         {
            setUnitControl(createUnitControl());
            setBlockControl(createBlockControl());
            setEndControl(createEndControl());
         }

         setBorder(null);
      }
      else
      {
         setUnitControl(createUnitControl());
         setBlockControl(createBlockControl());
         setEndControl(createEndControl());

         setBorder(BorderFactory.createRaisedBevelBorder());
      }

   }

   public void removeSlidingToolBar()
   {
      if (this.slidingToolBar != null)
      {
         if (block != null)
         {
            block.removeActionListener(this.slidingToolBar);
         }

         if (unit != null)
         {
            unit.removeActionListener(this.slidingToolBar);
         }

         if (endPoint != null)
         {
            endPoint.removeActionListener(this.slidingToolBar);
         }
      }

      this.slidingToolBar = null;
   }

   public void setSlidingToolBar(SlidingToolBar slidingToolBar)
   {
      SlidingToolBar oldValue = this.slidingToolBar;

      if (this.slidingToolBar != null)
      {
         if (block != null)
         {
            block.removeActionListener(this.slidingToolBar);
         }

         if (unit != null)
         {
            unit.removeActionListener(this.slidingToolBar);
         }

         if (endPoint != null)
         {
            endPoint.removeActionListener(this.slidingToolBar);
         }
      }

      this.slidingToolBar = slidingToolBar;

      if (this.slidingToolBar != null)
      {
         if (block != null)
         {
            block.addActionListener(this.slidingToolBar);
         }

         if (unit != null)
         {
            unit.addActionListener(this.slidingToolBar);
         }

         if (endPoint != null)
         {
            endPoint.addActionListener(this.slidingToolBar);
         }
      }

      if ((oldValue != null && this.slidingToolBar != null
             && oldValue.getOrientation()
                   != this.slidingToolBar.getOrientation())
       || (oldValue == null && this.slidingToolBar != null))
      {
         setLayout(createLayout());
      }
   }

   public SlidingToolBar getSlidingToolBar()
   {
      return slidingToolBar;
   }

   public void setDirection(int direction)
   {
      switch (direction)
      {
         case UP:
         case DOWN:
            break;
         default:
            throw new IllegalArgumentException(
               "Direction must be one of: UP, DOWN");
      }

      this.direction = direction;

      if (unit != null) setUnitActionCommand();
      if (block != null) setBlockActionCommand();
      if (endPoint != null) setEndActionCommand();
   }

   public int getDirection()
   {
      return direction;
   }

   public void setUnitControl(AbstractButton button)
   {
      if (unit != null)
      {
         remove(unit);

         if (slidingToolBar != null)
         {
            unit.removeActionListener(slidingToolBar);
            unit.setActionCommand(null);
         }
      }

      unit = button;

      if (unit != null)
      {
         add(unit);

         if (slidingToolBar != null)
         {
            unit.addActionListener(slidingToolBar);
            setUnitActionCommand();
         }
      }
   }

   private void setUnitActionCommand()
   {
      unit.setActionCommand(direction == UP ?
         "scrollUnitUp" : "scrollUnitDown");

      if (unit instanceof SlidingToolControlButton)
      {
         ((SlidingToolControlButton)unit).updateToolTipText();
      }
   }

   private void setBlockActionCommand()
   {
      block.setActionCommand(direction == UP ?
         "scrollBlockUp" : "scrollBlockDown");

      if (block instanceof SlidingToolControlButton)
      {
         ((SlidingToolControlButton)block).updateToolTipText();
      }
   }

   private void setEndActionCommand()
   {
      endPoint.setActionCommand(direction == UP ?
         "scrollEndUp" : "scrollEndDown");

      if (endPoint instanceof SlidingToolControlButton)
      {
         ((SlidingToolControlButton)endPoint).updateToolTipText();
      }
   }

   public void setBlockControl(AbstractButton button)
   {
      if (block != null)
      {
         remove(block);

         if (slidingToolBar != null)
         {
            block.removeActionListener(slidingToolBar);
            block.setActionCommand(null);
         }
      }

      block = button;

      if (block != null)
      {
         add(block);

         if (slidingToolBar != null)
         {
            block.addActionListener(slidingToolBar);
            setBlockActionCommand();
         }
      }
   }

   public void setEndControl(AbstractButton button)
   {
      if (endPoint != null)
      {
         remove(endPoint);

         if (slidingToolBar != null)
         {
            endPoint.removeActionListener(slidingToolBar);
            endPoint.setActionCommand(null);
         }
      }

      endPoint = button;

      if (endPoint != null)
      {
         add(endPoint);

         if (slidingToolBar != null)
         {
            endPoint.addActionListener(slidingToolBar);
            setEndActionCommand();
         }
      }
   }

   public AbstractButton getUnitControl()
   {
      return unit;
   }

   public AbstractButton getBlockControl()
   {
      return block;
   }

   public AbstractButton getEndControl()
   {
      return endPoint;
   }

   protected LayoutManager createLayout()
   {
      if (resources.getButtonStyle().isCompact())
      {
         if (slidingToolBar.getOrientation()==SlidingToolBar.VERTICAL)
         {
            GridLayout layout = new GridLayout(3, 1);
            layout.setVgap(0);
            return layout;
         }
         else
         {
            GridLayout layout = new GridLayout(1, 3);
            layout.setHgap(0);
            return layout;
         }
      }

      if (slidingToolBar.getOrientation()==SlidingToolBar.HORIZONTAL)
      {
         GridLayout layout = new GridLayout(3, 1);
         layout.setVgap(0);
         return layout;
      }
      else
      {
         GridLayout layout = new GridLayout(1, 3);
         layout.setHgap(0);
         return layout;
      }
   }

   protected AbstractButton createUnitControl()
   {
      return SlidingToolControlButton.createUnit(this);
   }

   protected AbstractButton createBlockControl()
   {
      return SlidingToolControlButton.createBlock(this);
   }

   protected AbstractButton createEndControl()
   {
      return SlidingToolControlButton.createEnd(this);
   }

   public JDRResources getResources()
   {
      return resources;
   }
}

class SlidingToolControlButton extends JButton
{
   protected SlidingToolControl control;

   protected static ImageIcon vScrollBlockDown, vScrollBlockUp,
      vScrollEndDown, vScrollEndUp, vScrollUnitDown, vScrollUnitUp,
      hScrollBlockLeft, hScrollBlockRight, hScrollEndLeft,
      hScrollEndRight, hScrollUnitLeft, hScrollUnitRight;

   protected static ImageIcon vScrollBlockDownPressed, vScrollBlockUpPressed,
      vScrollEndDownPressed, vScrollEndUpPressed, vScrollUnitDownPressed, vScrollUnitUpPressed,
      hScrollBlockLeftPressed, hScrollBlockRightPressed, hScrollEndLeftPressed,
      hScrollEndRightPressed, hScrollUnitLeftPressed, hScrollUnitRightPressed;

   protected static String vScrollBlockDownTT, vScrollBlockUpTT,
      vScrollEndDownTT, vScrollEndUpTT, vScrollUnitDownTT, vScrollUnitUpTT,
      hScrollBlockLeftTT, hScrollBlockRightTT, hScrollEndLeftTT,
      hScrollEndRightTT, hScrollUnitLeftTT, hScrollUnitRightTT;

   private static boolean imagesLoaded = false;

   private int type;

   public static final int UNIT=0, BLOCK=1, END=2;

   private SlidingToolControlButton()
   {
      this(null, UNIT);
   }

   public SlidingToolControlButton(SlidingToolControl control, int type)
   {
      super();
      this.control = control;
      setType(type);

      JDRResources resources = control.getResources();

      if (!imagesLoaded)
      {
         loadImages(resources);
      }

      JDRButtonStyle style = resources.getButtonStyle();

      setIconTextGap(0);
      setBorderPainted(style.hasBorderPainted());
      setContentAreaFilled(style.hasAreaFilled());
      setMargin(new Insets(0, 0, 0, 0));
      setBorder(null);
      updateToolTipText();
   }

   public static SlidingToolControlButton createBlock(SlidingToolControl c)
   {
      return new SlidingToolControlButton(c, BLOCK);
   }

   public static SlidingToolControlButton createUnit(SlidingToolControl c)
   {
      return new SlidingToolControlButton(c, UNIT);
   }

   public static SlidingToolControlButton createEnd(SlidingToolControl c)
   {
      return new SlidingToolControlButton(c, END);
   }

   private static void loadImages(JDRResources resources)
   {
      JDRButtonStyle style = resources.getButtonStyle();

      vScrollBlockDown = resources.appIcon(
        "vScrollBlockDown.png");
      vScrollBlockUp = resources.appIcon(
        "vScrollBlockUp.png");
      vScrollEndDown = resources.appIcon(
        "vScrollEndDown.png");
      vScrollEndUp = resources.appIcon(
        "vScrollEndUp.png");
      vScrollUnitDown = resources.appIcon(
        "vScrollUnitDown.png");
      vScrollUnitUp = resources.appIcon(
        "vScrollUnitUp.png");
      hScrollBlockLeft = resources.appIcon(
        "hScrollBlockLeft.png");
      hScrollBlockRight = resources.appIcon(
        "hScrollBlockRight.png");
      hScrollEndLeft = resources.appIcon(
        "hScrollEndLeft.png");
      hScrollEndRight = resources.appIcon(
        "hScrollEndRight.png");
      hScrollUnitLeft = resources.appIcon(
        "hScrollUnitLeft.png");
      hScrollUnitRight = resources.appIcon(
        "hScrollUnitRight.png");

      vScrollBlockDownPressed = resources.appIcon(
        "vScrollBlockDownPressed.png");
      vScrollBlockUpPressed = resources.appIcon(
        "vScrollBlockUpPressed.png");
      vScrollEndDownPressed = resources.appIcon(
        "vScrollEndDownPressed.png");
      vScrollEndUpPressed = resources.appIcon(
        "vScrollEndUpPressed.png");
      vScrollUnitDownPressed = resources.appIcon(
        "vScrollUnitDownPressed.png");
      vScrollUnitUpPressed = resources.appIcon(
        "vScrollUnitUpPressed.png");
      hScrollBlockLeftPressed = resources.appIcon(
        "hScrollBlockLeftPressed.png");
      hScrollBlockRightPressed = resources.appIcon(
        "hScrollBlockRightPressed.png");
      hScrollEndLeftPressed = resources.appIcon(
        "hScrollEndLeftPressed.png");
      hScrollEndRightPressed = resources.appIcon(
        "hScrollEndRightPressed.png");
      hScrollUnitLeftPressed = resources.appIcon(
        "hScrollUnitLeftPressed.png");
      hScrollUnitRightPressed = resources.appIcon(
        "hScrollUnitRightPressed.png");

      vScrollBlockDownTT = resources.getString("tooltip.vScrollBlockDown");
      vScrollBlockUpTT = resources.getString("tooltip.vScrollBlockUp");
      vScrollEndDownTT = resources.getString("tooltip.vScrollEndDown");
      vScrollEndUpTT = resources.getString("tooltip.vScrollEndUp");
      vScrollUnitDownTT = resources.getString("tooltip.vScrollUnitDown");
      vScrollUnitUpTT = resources.getString("tooltip.vScrollUnitUp");
      hScrollBlockLeftTT = resources.getString("tooltip.hScrollBlockLeft");
      hScrollBlockRightTT = resources.getString("tooltip.hScrollBlockRight");
      hScrollEndLeftTT = resources.getString("tooltip.hScrollEndLeft");
      hScrollEndRightTT = resources.getString("tooltip.hScrollEndRight");
      hScrollUnitLeftTT = resources.getString("tooltip.hScrollUnitLeft");
      hScrollUnitRightTT = resources.getString("tooltip.hScrollUnitRight");

      imagesLoaded = true;
   }

   public void setType(int type)
   {
      switch (type)
      {
         case UNIT:
         case BLOCK:
         case END:
            break;
         default:
            throw new IllegalArgumentException(
               "Control type must be one of: UNIT, BLOCK, END");
      }

      this.type = type;
   }

   public int getType()
   {
      return type;
   }

   public String getControlLabel()
   {
      if (control == null || control.getSlidingToolBar() == null)
      {
         return null;
      }

      String direction;

      String prefix;

      if (control.getSlidingToolBar().getOrientation()
            == SlidingToolBar.HORIZONTAL)
      {
         direction = (control.getDirection() == SlidingToolControl.UP?
            "Left" : "Right");
         prefix = "h";
      }
      else
      {
         direction = (control.getDirection() == SlidingToolControl.UP?
            "Up" : "Down");
         prefix = "v";
      }

      String typeString="";

      switch (type)
      {
         case UNIT:
            typeString = "Unit";
            break;
         case BLOCK:
            typeString = "Block";
            break;
         case END:
            typeString = "End";
            break;
      }

      return prefix+"Scroll"+typeString+direction;
   }

   public void updateToolTipText()
   {
      String label = getControlLabel();

      String text = null;

      if (label == null) return;

      if (label.equals("vScrollBlockDown"))
      {
         text = vScrollBlockDownTT;
      }
      else if (label.equals("vScrollBlockUp"))
      {
         text = vScrollBlockUpTT;
      }
      else if (label.equals("vScrollUnitDown"))
      {
         text = vScrollUnitDownTT;
      }
      else if (label.equals("vScrollUnitUp"))
      {
         text = vScrollUnitUpTT;
      }
      else if (label.equals("vScrollEndUp"))
      {
         text = vScrollEndUpTT;
      }
      else if (label.equals("vScrollEndDown"))
      {
         text = vScrollEndDownTT;
      }
      else if (label.equals("hScrollBlockLeft"))
      {
         text = hScrollBlockLeftTT;
      }
      else if (label.equals("hScrollBlockRight"))
      {
         text = hScrollBlockRightTT;
      }
      else if (label.equals("hScrollUnitLeft"))
      {
         text = hScrollUnitLeftTT;
      }
      else if (label.equals("hScrollUnitRight"))
      {
         text = hScrollUnitRightTT;
      }
      else if (label.equals("hScrollEndLeft"))
      {
         text = hScrollEndLeftTT;
      }
      else if (label.equals("hScrollEndRight"))
      {
         text = hScrollEndRightTT;
      }

      setToolTipText(text);
   }

   public Icon getIcon()
   {
      String label = getControlLabel();

      if (label == null) return null;

      if (label.equals("vScrollBlockDown"))
      {
         return vScrollBlockDown;
      }

      if (label.equals("vScrollBlockUp"))
      {
         return vScrollBlockUp;
      }

      if (label.equals("vScrollUnitDown"))
      {
         return vScrollUnitDown;
      }

      if (label.equals("vScrollUnitUp"))
      {
         return vScrollUnitUp;
      }

      if (label.equals("vScrollEndUp"))
      {
         return vScrollEndUp;
      }

      if (label.equals("vScrollEndDown"))
      {
         return vScrollEndDown;
      }

      if (label.equals("hScrollBlockLeft"))
      {
         return hScrollBlockLeft;
      }

      if (label.equals("hScrollBlockRight"))
      {
         return hScrollBlockRight;
      }

      if (label.equals("hScrollUnitLeft"))
      {
         return hScrollUnitLeft;
      }

      if (label.equals("hScrollUnitRight"))
      {
         return hScrollUnitRight;
      }

      if (label.equals("hScrollEndLeft"))
      {
         return hScrollEndLeft;
      }

      if (label.equals("hScrollEndRight"))
      {
         return hScrollEndRight;
      }

      return null;
   }

   public Icon getPressedIcon()
   {
      String label = getControlLabel();

      if (label == null) return null;

      if (label.equals("vScrollBlockDown"))
      {
         return vScrollBlockDownPressed;
      }

      if (label.equals("vScrollBlockUp"))
      {
         return vScrollBlockUpPressed;
      }

      if (label.equals("vScrollUnitDown"))
      {
         return vScrollUnitDownPressed;
      }

      if (label.equals("vScrollUnitUp"))
      {
         return vScrollUnitUpPressed;
      }

      if (label.equals("vScrollEndUp"))
      {
         return vScrollEndUpPressed;
      }

      if (label.equals("vScrollEndDown"))
      {
         return vScrollEndDownPressed;
      }

      if (label.equals("hScrollBlockLeft"))
      {
         return hScrollBlockLeftPressed;
      }

      if (label.equals("hScrollBlockRight"))
      {
         return hScrollBlockRightPressed;
      }

      if (label.equals("hScrollUnitLeft"))
      {
         return hScrollUnitLeftPressed;
      }

      if (label.equals("hScrollUnitRight"))
      {
         return hScrollUnitRightPressed;
      }

      if (label.equals("hScrollEndLeft"))
      {
         return hScrollEndLeftPressed;
      }

      if (label.equals("hScrollEndRight"))
      {
         return hScrollEndRightPressed;
      }

      return null;
   }
}
