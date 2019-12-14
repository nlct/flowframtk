// File          : JDRTextListener.java
// Creation Date : 29th February 2008
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

package com.dickimawbooks.jdr.io;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Loader listener for text areas.
 * @author Nicola L C Talbot
 */

public class JDRTextListener implements JDRObjectLoaderListener
{
   public char getId(float version)
   {
      return 'T';
   }

   public JDRObject getObject(JDRAJR jdr, JDRObject object, float version)
   {
      return object;
   }

   public void write(JDRAJR jdr, JDRObject object)
      throws IOException
   {
      float version = jdr.getVersion();

      JDRText text = (JDRText)object;

      if (version >= 1.8f)
      {
         jdr.writeBoolean(text.isOutline());

         if (text.isOutline())
         {
            JDRPaint paint = text.getFillPaint();
            JDRPaintLoader paintLoader = jdr.getPaintLoader();
            paintLoader.save(jdr, (paint==null? 
              new JDRTransparent(text.getCanvasGraphics()) :
              paint));
         }
      }

      // font specs
      text.getJDRFont().save(jdr);

      // transformation

      text.getTransform().save(jdr);

      // LaTeX stuff
      jdr.writeBoolean(true);
      text.getLaTeXFont().save(jdr);

      jdr.writeByte((byte)text.getHAlign());
      jdr.writeByte((byte)text.getVAlign());

      String latexText = text.getLaTeXText();

      jdr.writeString(latexText.equals(text.getText()) ? null : 
         latexText);

      // text colour

      JDRPaint paint = text.getTextPaint();

      JDRPaintLoader paintLoader = jdr.getPaintLoader();
      paintLoader.save(jdr, paint);

      // text
      jdr.writeString(text.getText());
   }

   public JDRObject read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      boolean isOutline = false;
      JDRPaint fillPaint = null;

      if (version >= 1.8f)
      {
         isOutline = jdr.readBoolean(
            InvalidFormatException.TEXT_OUTLINE_FLAG);

         if (isOutline)
         {
            JDRPaintLoader paintLoader = jdr.getPaintLoader();
            fillPaint = paintLoader.load(jdr);

            if (fillPaint instanceof JDRTransparent)
            {
               fillPaint = null;
            }
         }
      }

      // font specs
      JDRFont jdrfont = JDRFont.read(jdr);

      // transformation matrix

      double[] matrix = JDRTransform.read(jdr,
         InvalidFormatException.FONT_TRANSFORM);

      // LaTeX font specs

      LaTeXFont ltxfont = new LaTeXFont();

      String ltxText   = null;
      int halign = JDRText.PGF_HALIGN_LEFT;
      int valign = JDRText.PGF_VALIGN_BASE;

      if (jdr.readBoolean(InvalidFormatException.LATEX_FONT_FLAG))
      {
         ltxfont = LaTeXFont.read(jdr);

         halign = (int)jdr.readByte(InvalidFormatException.HALIGN_STYLE,
           0, JDRText.PGF_HALIGN_RIGHT, true, true);

         valign = (int)jdr.readByte(InvalidFormatException.VALIGN_STYLE,
           0, JDRText.PGF_VALIGN_BOTTOM, true, true);

         ltxText = jdr.readString(InvalidFormatException.LATEX_TEXT);
      }

      // JDRText colour

      JDRPaintLoader paintLoader = JDR.getPaintLoader();

      JDRPaint foreground = paintLoader.load(jdr);

      // text

      String string = jdr.readString(InvalidFormatException.TEXT);

      // Specs loaded, so now create the object

      CanvasGraphics cg = jdr.getCanvasGraphics();

      JDRTransform trans = new JDRTransform(cg, matrix);

      JDRText textsegment = new JDRText(trans,
         jdrfont.getFamily(), jdrfont.getWeight(),
         jdrfont.getShape(), jdrfont.getSize(), string);

      textsegment.setTextPaint(foreground);

      textsegment.setLaTeXFont(ltxfont);

      textsegment.setAlign(halign, valign);

      if (ltxText != null) textsegment.setLaTeXText(ltxText);

      textsegment.setOutlineMode(isOutline);
      textsegment.setFillPaint(fillPaint);

      if (cg.getGraphics() == null)
      {
         // update bounds (not needed for FlowframTk, as it
         // can call updateBounds, but required for jdr2ajr)
         BufferedImage buffImage = new BufferedImage(1,1,
         BufferedImage.TYPE_INT_RGB);

         Graphics2D g2 = buffImage.createGraphics();
         cg.setGraphicsDevice(g2);
         textsegment.updateBounds();
         g2.dispose();
         cg.setGraphicsDevice(null);
      }

      return textsegment;
   }

}
