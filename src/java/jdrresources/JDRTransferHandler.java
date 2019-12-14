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

/**
 * Transfer handler for JDRGroup.
 */

public class JDRTransferHandler extends TransferHandler implements Transferable
{
   public JDRTransferHandler()
   {
      super();
      flavors[0] = new DataFlavor((new JDRGroup()).getClass(), "JDRGroup");
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
      group  = null;

      if (c instanceof JDRImage)
      {
         JDRImage img = (JDRImage)c;
         JDRGroup g = img.getSelection();
         group = g;
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
               group = (JDRGroup)t.getTransferData(flavors[0]);
               img.copySelection(group);
               return true;
            }
            catch (UnsupportedFlavorException ignored)
            {
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }
      return false;
   }

   public Object getTransferData(DataFlavor f)
   {
      if (isDataFlavorSupported(f))
      {
         return group;
      }
      return null;
   }

   public DataFlavor[] getTransferDataFlavors()
   {
      return flavors;
   }

   public boolean isDataFlavorSupported(DataFlavor f)
   {
      return f.isMimeTypeEqual(DataFlavor.javaSerializedObjectMimeType);
   }

   private DataFlavor flavors[] = new DataFlavor[1]; 

   private JDRImage source;
   private JDRGroup group;

}
