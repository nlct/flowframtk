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

import java.nio.ByteBuffer;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.geom.Point2D;

import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.InvalidFormatException;

/**
 * Transfer handler for JDRGroup.
 */

public class JDRTransferHandler extends TransferHandler implements Transferable
{
   public JDRTransferHandler(CanvasGraphics cg, ExportSettings exportSettings)
   {
      super();
      this.canvasGraphics = cg;
      this.jdr = new JDR();
      this.exportSettings = exportSettings;
   }

   /**
    * Returns the type of transfer actions supported by the source.
    * Method defined by TransferHandler class.
    * @param comp the component holding the data to be 
    * transferred
    * @return any bitwise-OR combination of COPY, MOVE and LINK
    */
   @Override
   public int getSourceActions(JComponent comp)
   {
      return TransferHandler.COPY_OR_MOVE;
   }

   /**
    * Indicates whether a component will accept an import of the
    * given set of data flavors prior to actually attempting to
    * import it.
    * Method defined by TransferHandler class.
    * @param comp the component to receive the transfer
    * @param transferFlavors the data formats available
    * @return true if the data can be inserted into the component
    */
   @Override
   public boolean canImport(JComponent comp, DataFlavor transferFlavors[])
   {
      if (!(comp instanceof JDRImage))
      {
         return false;
      }

      for (int i = 0, n=transferFlavors.length; i < n; i++)
      {
         for (int j = 0, m=SUPPORTED_FLAVORS.length; j < m; j++)
         {
            if (transferFlavors[i].equals(SUPPORTED_FLAVORS[j])) return true;
         }
      }

      return false;
   }

   /**
    * Creates a Transferable to use as the source for a data
    * transfer.
    * Method defined by TransferHandler class.
    * @param comp the component holding the data to be 
    * transferred
    * @return the representation of the data to be transferred or
    * null if the property associated with comp is null
    */
   @Override
   protected Transferable createTransferable(JComponent comp)
   {
      source = null;
      jdrByteArray  = null;
      svgString = null;
      textOnly = null;

      if (comp instanceof JDRImage)
      {
         JDRImage img = (JDRImage)comp;
         JDRGroup g = img.getSelection();

         try
         {
            jdrByteArray = jdr.toByteArray(g);

            StringWriter writer = new StringWriter();
            SVG.save(g, "", writer, exportSettings);
            svgString = writer.toString();

            textOnly = new StringBuilder();
            appendTextual(g);
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

   protected void appendTextual(JDRGroup g)
   {
      for (int i = 0, n = g.size(); i < n; i++)
      {
         JDRCompleteObject obj = g.get(i);

         if (obj instanceof JDRTextual)
         {
            textOnly.append(((JDRTextual)obj).getText());
            textOnly.append(String.format("%n"));
         }
         else if (obj instanceof JDRGroup)
         {
            appendTextual((JDRGroup)obj);
         }
      }
   }

   /**
    * Causes a transfer to a component from a clipboard or DND.
    * Method defined by TransferHandler class.
    * @param comp the component to receive the transfer
    * @param t the data to import
    * @return true if the data was inserted into the component.
    */
   @Override
   public boolean importData(JComponent comp, Transferable t)
   {
      if (comp instanceof JDRImage)
      {
         JDRImage img = (JDRImage)comp;
         JDRGroup group = null;

         try
         {
            if (t.isDataFlavorSupported(DATA_FLAVOR_JDR))
            {
               group = (JDRGroup)t.getTransferData(DATA_FLAVOR_JDR);
            }
            else if (t.isDataFlavorSupported(DATA_FLAVOR_SVG))
            {
// TODO
            }
            else if (t.isDataFlavorSupported(DATA_FLAVOR_TEXT))
            {
               Object data = t.getTransferData(DATA_FLAVOR_TEXT);

               if (data instanceof InputStream)
               {
                  InputStream ins = (InputStream)data;

                  ByteBuffer buffer = ByteBuffer.allocate(256);

                  int b;
                  double offset = 0;
                  JDRFont font = new JDRFont(canvasGraphics.getMessageDictionary());
                  double skip = font.getSize(canvasGraphics.getStorageUnit());

                  while ((b = ins.read()) != -1)
                  {
                     buffer.put((byte)b);
                  }

                  String text = new String(buffer.array()).trim();

                  if (!text.isEmpty())
                  {
                     group = new JDRGroup(canvasGraphics);

                     String[] split = text.split("\\r?\\n");

                     for (String s : split)
                     {
                        s = s.trim();

                        if (!s.isEmpty())
                        {
                           JDRText jdrText = new JDRText(canvasGraphics,
                             new Point2D.Double(0, offset), font, s);
                           group.add(jdrText);
                        }

                        offset += skip;
                     }
                  }

                  ins.close();
               }
            }
         }
         catch (UnsupportedFlavorException ignored)
         {
         }
         catch (Exception e)
         {
            canvasGraphics.getMessageSystem().postMessage(
              MessageInfo.createError(e));
         }

         if (group != null && group.size() > 0)
         {
            img.copySelection(group);

            return true;
         }
      }

      return false;
   }

   /**
    * Returns an object which represents the data to be transferred.
    * Method from Transferable interface.
    * @param flavor the requested flavor for the data
    * @return an object which represents the data to be transferred
    * @throws IOException if the data is no longer available
    * @throws UnsupportedFlavorException if the requested data
    * flavor is not supported
    */
   @Override
   public Object getTransferData(DataFlavor f)
   throws UnsupportedFlavorException,IOException
   {
      try
      {
         if (f.isMimeTypeEqual(DATA_FLAVOR_JDR))
         {
            return jdr.fromByteArray(jdrByteArray, canvasGraphics);
         }
         else if (f.isMimeTypeEqual(DATA_FLAVOR_SVG))
         {
            return new ByteArrayInputStream(
              svgString.getBytes(StandardCharsets.UTF_8));
         }
         else if (f.isMimeTypeEqual(DATA_FLAVOR_TEXT))
         {
            return new ByteArrayInputStream(
              textOnly.toString().getBytes(StandardCharsets.UTF_8));
         }
      }
      catch (InvalidFormatException e)
      {
         canvasGraphics.debugMessage(e);
      }

      throw new UnsupportedFlavorException(f);
   }

   /**
    * Returns an array of DataFlavor objects indicating the flavors
    * the data can be provided in.
    * Method from Transferable interface.
    * @return an array of data flavors in which this data can be
    * transferred
    */
   @Override
   public DataFlavor[] getTransferDataFlavors()
   {
      return SUPPORTED_FLAVORS;
   }

   /**
    * Returns whether or not the specified data flavor is supported
    * for this object.
    * Method from Transferable interface.
    * @param flavor the requested flavor for the data
    * @return true if the data flavor is supported
    */
   @Override
   public boolean isDataFlavorSupported(DataFlavor flavor)
   {
      for (DataFlavor f : SUPPORTED_FLAVORS)
      {
         if (f.isMimeTypeEqual(flavor))
         {
            return true;
         }
      }

      return false;
   }

   private JDRImage source;
   private byte[] jdrByteArray;
   private String svgString;
   private StringBuilder textOnly;
   private CanvasGraphics canvasGraphics;
   private JDR jdr;
   private ExportSettings exportSettings;

   public static final DataFlavor DATA_FLAVOR_JDR
    = new DataFlavor("application/x-jdr", "FlowFramTk Binary Format");

   public static final DataFlavor DATA_FLAVOR_SVG
    = new DataFlavor("image/svg+xml; class=java.io.InputStream", "SVG");

   public static final DataFlavor DATA_FLAVOR_TEXT
    = new DataFlavor("text/plain; class=java.io.InputStream", "Plain Text");

   private static final DataFlavor SUPPORTED_FLAVORS[] = new DataFlavor[]
    {
      DATA_FLAVOR_JDR,
      DATA_FLAVOR_SVG,
      DATA_FLAVOR_TEXT
    }; 
}
