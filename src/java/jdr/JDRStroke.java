// File          : JDRStroke.java
// Creation Date : 7th July 2009
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

import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.*;

/**
 * Stroke interface.
 * @author Nicola L C Talbot
 */

public interface JDRStroke extends Cloneable,Serializable
{
   /**
    * Gets a copy of this stroke.
    */
   public Object clone();

   /**
    * Gets the winding rule.
    * @return the winding rule
    */
   public int getWindingRule();

   /**
    * Sets the winding rule.
    * @param rule the winding rule
    */
   public void setWindingRule(int rule);

   /**
    * Draws the given path using this stroke.
    * @param path the path to draw
    */
   public void drawStoragePath(JDRShape path);

   public void drawStoragePath(JDRShape shape, Shape generalPath);

   public void printPath(Graphics2D g2, JDRShape shape, Shape generalPath);

   /**
    * Gets the stroked outline for the given path including
    * any markers.
    * @param path the path to which this stroke applies
    * @return the path outlining this stroked shape
    * @see #getStorageStrokedArea(JDRShape)
    */
   public Shape getStorageStrokedPath(JDRShape path);

   public Shape getBpStrokedPath(JDRShape path);
   public Shape getComponentStrokedPath(JDRShape path);

   /**
    * Gets the outline for the given path including any markers.
    * @param path the path to which this stroke applies
    * @return the path outlining the stroked shape including
    * any markers
    * @see #getStorageStrokedPath(JDRShape)
    */
   public Area getStorageStrokedArea(JDRShape path);
   public Area getBpStrokedArea(JDRShape path);
   public Area getComponentStrokedArea(JDRShape path);

   /**
    * Saves this stroke in the given JDR/AJR format.
    * @throws IOException if I/O error occurs
    */
   public void save(JDRAJR jdr)
      throws IOException;

   public void saveEPS(JDRShape path, PrintWriter out)
      throws IOException;

   public abstract void writeSVGdefs(SVG svg, JDRShape shape) throws IOException;

   /**
    * Gets the path style listener for the shape associated with
    * this stroke.
    * @return the path style listener for the shape associated with
    * this stroke
    */
   public JDRPathStyleListener getPathStyleListener();

   public String info();

   public void setCanvasGraphics(CanvasGraphics cg);

   public void applyCanvasGraphics(CanvasGraphics cg);

   public CanvasGraphics getCanvasGraphics();

   public Shape createStrokedShape(Shape shape, JDRUnit shapeUnit);
}
