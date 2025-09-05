// File          : JDRTransferHandler.java
// Description   : Transfer handler for JDRGroup
// Creation Date : 1st February 2006
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
package com.dickimawbooks.jdrresources;

import java.awt.*;
import java.awt.datatransfer.*;

import java.io.*;

import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;

/**
 * Transfer handler for JDRGroup.
 */

public class JDRTransferHandler extends TransferHandler implements Transferable
{
   public JDRTransferHandler(CanvasGraphics cg)
   {
      super();
      this.canvasGraphics = cg;
      this.jdr = new JDR();
   }

   public int getSourceActions(JComponent c)
   {
      return TransferHandler.COPY_OR_MOVE;
   }

   public boolean canImport(JComponent c, DataFlavor f[])
   {
      if (!(c instanceof JDRImage))
      {
         return false;
      }

      for (int i = 0, n=f.length; i < n; i++)
      {
         for (int j = 0, m=flavors.length; j < m; j++)
         {
            if (f[i].equals(flavors[j])) return true;
         }
      }

      return false;
   }

   public Transferable createTransferable(JComponent c)
   {
      source = null;
      byteArray  = null;

      if (c instanceof JDRImage)
      {
         JDRImage img = (JDRImage)c;
         JDRGroup g = img.getSelection();

         try
         {
            byteArray = jdr.toByteArray(g);
         }
         catch (IOException e)
         {
            canvasGraphics.getMessageSystem().postMessage(
              MessageInfo.createInternalError(e));
            return null;
         }

         source = img;
         return this;
      }

      return null;
   }

   public boolean importData(JComponent c, Transferable t)
   {
      if (c instanceof JDRImage)
      {
         JDRImage img = (JDRImage)c;

         if (t.isDataFlavorSupported(flavors[0]))
         {
            try
            {
               JDRGroup group = (JDRGroup)t.getTransferData(flavors[0]);
               img.copySelection(group);

               return true;
            }
            catch (UnsupportedFlavorException ignored)
            {
            }
            catch (Exception e)
            {
               canvasGraphics.getMessageSystem().postMessage(
                 MessageInfo.createError(e));
            }
         }
      }
      return false;
   }

   public Object getTransferData(DataFlavor f)
   {
      if (isDataFlavorSupported(f))
      {
         try
         {
            return jdr.fromByteArray(byteArray, canvasGraphics);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      return null;
   }

   public DataFlavor[] getTransferDataFlavors()
   {
      return flavors;
   }

   public boolean isDataFlavorSupported(DataFlavor f)
   {
      return f.isMimeTypeEqual(DATA_FLAVOR_JDR);
   }

   private JDRImage source;
   private byte[] byteArray;
   private CanvasGraphics canvasGraphics;
   private JDR jdr;

   public static final DataFlavor DATA_FLAVOR_JDR
    = new DataFlavor("application/x-jdr", "FlowFramTk Binary Format");

   private static final DataFlavor flavors[] = new DataFlavor[]
    {
      DATA_FLAVOR_JDR
    }; 
}
