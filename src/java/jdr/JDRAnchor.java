// File          : JDRAnchor.java
// Creation Date : 1st Oct 2013
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

package com.dickimawbooks.jdr;

import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.exceptions.*;

public class JDRAnchor
{
   public JDRAnchor()
   {
      mainPath = new GeneralPath();
      mainPath.moveTo(0f,-60f);
      mainPath.lineTo(0f,130f);
      mainPath.moveTo(-80f,-30f);
      mainPath.lineTo(80f,-30f);
      mainPath.moveTo(-90f,90f);
      mainPath.curveTo(-37f,148f,
                       40f, 148f,
                       90f, 90f);

      mainStroke = new BasicStroke(10f, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_ROUND);

      rightEndPath = new GeneralPath();
      rightEndPath.moveTo(109f, 105f);
      rightEndPath.curveTo(120f, 88f,
                           123f, 68f,
                           119f, 50f);
      rightEndPath.curveTo(101f, 53f,
                           83f, 61f,
                           71f, 76f);
      rightEndPath.closePath();

      width = (float)(2*123);

      leftEndPath = new GeneralPath();
      leftEndPath.moveTo(-109f, 106f);
      leftEndPath.curveTo(-120f, 89f,
                          -123f, -69f,
                          -119f, 51f);
      leftEndPath.curveTo(-101f, 54f,
                          -83f, 63f,
                          -70f, 78f);
      leftEndPath.closePath();

      endStroke = new BasicStroke();

      loopPath = new Ellipse2D.Float(-30f, -130f, 60f, 60f);

      loopStroke = new BasicStroke(16f, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_ROUND);

      height = (float) (148+5+130+8);
   }

   public void draw(Graphics2D g2, float x, float y, float size)
   {
      Stroke orgStroke = g2.getStroke();
      AffineTransform orgAf = g2.getTransform();

      float factor = size/height;

      g2.translate(x, y);
      g2.scale(factor, factor);

      g2.setStroke(mainStroke);
      g2.draw(mainPath);

      g2.setStroke(endStroke);
      g2.fill(leftEndPath);
      g2.fill(rightEndPath);

      g2.setStroke(loopStroke);
      g2.draw(loopPath);

      g2.setStroke(orgStroke);
      g2.setTransform(orgAf);
   }

   private GeneralPath mainPath, rightEndPath, leftEndPath;

   private Ellipse2D loopPath;

   private BasicStroke mainStroke, endStroke, loopStroke;

   private float width, height;
}
