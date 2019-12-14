// File          : JDRShading.java
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

package com.dickimawbooks.jdr;

import java.io.*;
import java.util.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Shading paint interface.
 * @author Nicola L C Talbot
 */

public interface JDRShading
{
   /**
    * Converts this shading to another shading type.
    * @param label identifies the shading type to convert to
    */
   public JDRShading convertShading(String label);

   /**
    * Gets the start colour for this shading.
    * @return the start colour for this shading
    */
   public JDRPaint getStartColor();


   /**
    * Gets the end colour for this shading.
    * @return the end colour for this shading
    */
   public JDRPaint getEndColor();

   /**
    * Converts the start and end colours to grey.
    */
   public void reduceToGreyScale();

   /**
    * Converts the start and end colours to CMYK.
    */
   public void convertToCMYK();

   /**
    * Converts the start and end colours to RGB.
    */
   public void convertToRGB();

   /**
    * Converts the start and end colours to HSB.
    */
   public void convertToHSB();

   public void setCanvasGraphics(CanvasGraphics cg);

   public CanvasGraphics getCanvasGraphics();
}
