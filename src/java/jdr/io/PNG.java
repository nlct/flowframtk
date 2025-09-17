// File          : PNG.java
// Purpose       : functions to save JDRGroup as PNG file
// Creation Date : 1st February 2006
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
package com.dickimawbooks.jdr.io;

import java.io.*;
import java.util.Enumeration;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Functions to save image in PNG format. Note that this writes the
 * image as a bitmap rather not in vector format so enlarging the PNG
 * image will degrade the quality.
 * @author Nicola L C Talbot
 */

public class PNG
{
   /**
    * Saves image in PNG format.
    * @param group image
    * @param filename name of file to write
    * @throws IOException if I/O exception occurs
    */
   public static void save(JDRGroup group, String filename,
     ExportSettings exportSettings)
      throws IOException
   {
      save(group, new File(filename), exportSettings);
   }

   /**
    * Saves image in PNG format.
    * @param group image
    * @param file output file
    * @throws IOException if I/O exception occurs
    */
   public static void save(JDRGroup group, File file,
    ExportSettings exportSettings)
      throws IOException
   {
      CanvasGraphics cg = group.getCanvasGraphics();
      JDRMessage msgSys = cg.getMessageSystem();
      MessageInfoPublisher publisher = msgSys.getPublisher();

      boolean hasAlpha = exportSettings.pngUseAlpha;
      boolean cropimage = 
       (exportSettings.bounds != ExportSettings.Bounds.PAPER);

      boolean indeter = (group.size() <= 1);

      publisher.publishMessages(MessageInfo.createIndeterminate(indeter));

      if (!indeter)
      {
         publisher.publishMessages(MessageInfo.createMaxProgress(group.size()));
      }

      int width;
      int height;
      double offsetx = 0;
      double offsety = 0;

      if (cropimage)
      {
         BBox bbox = group.getBpBBox();

         width = (int)Math.ceil(bbox.getWidth()+1);
         height = (int)Math.ceil(bbox.getHeight()+1);

         offsetx = -bbox.getMinX();
         offsety = -bbox.getMinY();
      }
      else
      {
         width = (int)Math.ceil(cg.getPaperWidth());
         height = (int)Math.ceil(cg.getPaperHeight());
      }

      BufferedImage buffImage = new BufferedImage(width, height,
        hasAlpha ? BufferedImage.TYPE_INT_ARGB
                 : BufferedImage.TYPE_INT_RGB);

      Graphics2D g2 = null;

      try
      {
         g2 = buffImage.createGraphics();

         RenderingHints renderHints =
               new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                  RenderingHints.VALUE_ANTIALIAS_ON);

         renderHints.add(new RenderingHints(
                             RenderingHints.KEY_RENDERING,
                             RenderingHints.VALUE_RENDER_QUALITY));

         g2.setRenderingHints(renderHints);

         if (hasAlpha)
         {
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0,0,width,height);
            g2.setComposite(AlphaComposite.Src);
         }
         else
         {
            g2.setColor(Color.white);
            g2.fillRect(0,0,width,height);
         }

         if (cropimage)
         {
            g2.translate(offsetx, offsety);
         }

         for (int i = 0; i < group.size(); i++)
         {
             publisher.publishMessages(MessageInfo.createIncProgress());

             group.get(i).print(g2);
         }

      }
      finally
      {
         if (g2 != null)
         {
            g2.dispose();
         }
      }

      ImageIO.write(buffImage, "png", file);
   }
}
