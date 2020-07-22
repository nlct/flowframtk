// File          : JDRCanvasCompoundEdit.java
// Description   : Undoable edits for JDRCanvas actions
// Creation Date : 2014-04-05
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

import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Cursor;
import javax.swing.undo.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;

public class JDRCanvasCompoundEdit extends CompoundEdit
{
   private JDRCanvasCompoundEdit()
   {
      super();
   }

   public JDRCanvasCompoundEdit(JDRCanvas canvas)
   {
      super();
      this.canvas = canvas;
   }

   public JDRCanvasCompoundEdit(JDRCanvas canvas, String name)
   {
      super();
      this.name = name;
      this.canvas = canvas;
   }

   public void undo () throws CannotUndoException
   {
      Cursor oldCursor = canvas.getCursor();
      canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      super.undo();
      canvas.setCursor(oldCursor);
   }

   public void redo () throws CannotRedoException
   {
      Cursor oldCursor = canvas.getCursor();
      canvas.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      super.redo();
      canvas.setCursor(oldCursor);
   }

   public String getPresentationName()
   {
      return name == null ? super.getPresentationName() : name;
   }

   public String getUndoPresentationName()
   {
      return name == null ? super.getUndoPresentationName() : 
        getResources().getMessage("undo.undo", name);
   }

   public String getRedoPresentationName()
   {
      return name == null ? super.getRedoPresentationName() : 
        getResources().getMessage("undo.redo", name);
   }

   public JDRResources getResources()
   {
      return canvas.getResources();
   }

   private String name = null;

   private JDRCanvas canvas;
}
