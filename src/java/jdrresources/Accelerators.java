// File          : Accelerators.java
// Purpose       : Accelerators used by FlowframTk and associated
//                 applications
// Creation Date : 2014-04-25
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

import java.util.Properties;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

public class Accelerators extends Properties
{
   public Accelerators()
   {
      super();
   }

   public KeyStroke getAccelerator(String propName, String defValue)
   {
      String prop = getProperty(propName, defValue);

      if (prop == null) return null;

      return KeyStroke.getKeyStroke(prop);
   }

   public KeyStroke getAccelerator(String propName)
   {
      String prop = getProperty(propName);

      if (prop == null) return null;

      return KeyStroke.getKeyStroke(prop);
   }

   public void putAccelerator(String propName, KeyStroke keyStroke)
   {
      setProperty(propName, keyStroke.toString());
   }

   public static Accelerators createDefaultAccelerators()
   {
      Accelerators acc = new Accelerators();

      acc.putAccelerator("label.okay",
        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

      acc.putAccelerator("label.alt_okay",
        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK));

      acc.putAccelerator("label.cancel",
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

      acc.putAccelerator("label.close",
        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

      acc.putAccelerator("label.help",
        KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

      acc.putAccelerator("label.contexthelp",
        KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.SHIFT_MASK));

      acc.putAccelerator("info.help",
        KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.CTRL_MASK));

      acc.putAccelerator("accelerator.popup",
        KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));

      acc.putAccelerator("accelerator.alt_popup",
        KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0));

      acc.putAccelerator("accelerator.construct_click",
        KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));

      acc.putAccelerator("accelerator.cursor_left",
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));

      acc.putAccelerator("accelerator.cursor_right",
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));

      acc.putAccelerator("accelerator.cursor_up",
         KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));

      acc.putAccelerator("accelerator.cursor_down",
         KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));

      acc.putAccelerator("accelerator.scroll_home_up",
         KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));

      acc.putAccelerator("accelerator.scroll_home_left",
         KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_MASK));

      acc.putAccelerator("accelerator.scroll_end_down",
         KeyStroke.getKeyStroke(KeyEvent.VK_END, 0));

      acc.putAccelerator("accelerator.scroll_end_right",
         KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK));

      acc.putAccelerator("accelerator.scroll_block_down",
         KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));

      acc.putAccelerator("accelerator.scroll_block_right",
         KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK));

      acc.putAccelerator("accelerator.scroll_block_up",
         KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));

      acc.putAccelerator("accelerator.scroll_block_left",
         KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK));

      acc.putAccelerator("accelerator.cursor_word_left",
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK));

      acc.putAccelerator("accelerator.cursor_word_right",
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK));

      acc.putAccelerator("accelerator.cursor_right",
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));

      acc.putAccelerator("accelerator.delete_last",
         KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0));

      acc.putAccelerator("text.copy",
         KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));

      acc.putAccelerator("text.cut",
         KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));

      acc.putAccelerator("text.paste",
         KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));

      acc.putAccelerator("text.select_all",
         KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));

      acc.putAccelerator("text.insert_symbol",
         KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));

      acc.putAccelerator("editpath.next_control",
         KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));

      acc.putAccelerator("editpath.prev_control",
         KeyStroke.getKeyStroke(KeyEvent.VK_F6,
         InputEvent.SHIFT_MASK));

      acc.putAccelerator("editpath.delete_control",
         KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

      acc.putAccelerator("editpath.add_control",
         KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));

      acc.putAccelerator("editpath.coordinates",
         KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));

      acc.putAccelerator("editpath.snap",
         KeyStroke.getKeyStroke(KeyEvent.VK_S,
         InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK));

      acc.putAccelerator("file.new", 
        KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));

      acc.putAccelerator("file.open",
        KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));

      acc.putAccelerator("file.save",
        KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));

      acc.putAccelerator("file.quit",
        KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));

      acc.putAccelerator("edit.undo",
        KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));

      acc.putAccelerator("edit.redo",
        KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));

      acc.putAccelerator("edit.select_all",
        KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));

      acc.putAccelerator("edit.deselect_all",
        KeyStroke.getKeyStroke(KeyEvent.VK_A,
         InputEvent.CTRL_MASK+InputEvent.SHIFT_MASK));

      acc.putAccelerator("edit.cut",
        KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));

      acc.putAccelerator("edit.copy",
        KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));

      acc.putAccelerator("edit.paste",
        KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));

      acc.putAccelerator("edit.moveby",
        KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));

      acc.putAccelerator("edit.path.edit",
        KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));

      acc.putAccelerator("edit.path.style.all",
        KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));

      acc.putAccelerator("edit.text.edit",
        KeyStroke.getKeyStroke(KeyEvent.VK_I,
         InputEvent.CTRL_MASK+InputEvent.SHIFT_MASK));

      acc.putAccelerator("edit.front",
        KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));

      acc.putAccelerator("edit.back",
        KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK));

      acc.putAccelerator("edit.moveup",
        KeyStroke.getKeyStroke(KeyEvent.VK_F,
         InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK));

      acc.putAccelerator("edit.movedown",
        KeyStroke.getKeyStroke(KeyEvent.VK_B,
         InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK));

      acc.putAccelerator("transform.rotate",
        KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));

      acc.putAccelerator("transform.scale",
        KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));

      acc.putAccelerator("transform.shear",
        KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));

      acc.putAccelerator("transform.merge",
        KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK));

      acc.putAccelerator("transform.convert",
        KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));

      acc.putAccelerator("transform.group",
        KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));

      acc.putAccelerator("transform.ungroup",
         KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));

      acc.putAccelerator("tools.select",
         KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));

      acc.putAccelerator("tools.open_line",
         KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));

      acc.putAccelerator("tools.closed_line",
         KeyStroke.getKeyStroke(KeyEvent.VK_L,
            InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK));

      acc.putAccelerator("tools.open_curve",
        KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));

      acc.putAccelerator("tools.closed_curve",
        KeyStroke.getKeyStroke(KeyEvent.VK_K,
            InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK));

      acc.putAccelerator("tools.rectangle",
        KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));

      acc.putAccelerator("tools.ellipse",
        KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));

      acc.putAccelerator("tools.text",
        KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));

      acc.putAccelerator("tools.math",
        KeyStroke.getKeyStroke(KeyEvent.VK_T, 
         InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK));

      acc.putAccelerator("tools.gap",
        KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK));

      acc.putAccelerator("tools.abandon",
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, InputEvent.SHIFT_MASK));

      acc.putAccelerator("tools.finish",
        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

      acc.putAccelerator("navigate.goto",
        KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

      acc.putAccelerator("navigate.select",
        KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_MASK));

      acc.putAccelerator("navigate.add_next",
        KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_MASK));

      acc.putAccelerator("navigate.skip",
        KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));

      acc.putAccelerator("navigate.find",
        KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.SHIFT_MASK));

      acc.putAccelerator("texeditor.selectText",
        KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));

      acc.putAccelerator("texeditor.cutText",
        KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));

      acc.putAccelerator("texeditor.copyText",
        KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));

      acc.putAccelerator("texeditor.pasteText",
        KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));

      acc.putAccelerator("texeditor.search.find",
        KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK));

      acc.putAccelerator("texeditor.search.find_again",
        KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));

      acc.putAccelerator("texeditor.search.replace",
        KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_MASK));

      acc.putAccelerator("settings.grid.show",
        KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));

      acc.putAccelerator("settings.grid.lock",
        KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.SHIFT_MASK));

      acc.putAccelerator("debug.objectinfo",
        KeyStroke.getKeyStroke(KeyEvent.VK_F11, 
          InputEvent.SHIFT_MASK));

      acc.putAccelerator("debug.writelog",
        KeyStroke.getKeyStroke(KeyEvent.VK_F11, 
          InputEvent.SHIFT_MASK+InputEvent.CTRL_MASK));

      acc.putAccelerator("debug.dumpall",
        KeyStroke.getKeyStroke(KeyEvent.VK_F11, InputEvent.CTRL_MASK));

      acc.putAccelerator("debug.revalidate",
        KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));

      return acc;
   }
}
