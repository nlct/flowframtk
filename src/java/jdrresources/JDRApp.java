// File          : JDRApp.java
// Description   : Interface for JDR applications
// Creation Date : 9th June 2008
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

import javax.swing.*;

import com.dickimawbooks.jdrresources.numfield.ZoomValue;

/**
 * Interface for JDR applications.
 * @author Nicola L C Talbot
 */
public interface JDRApp
{
   /**
    * Gets the magnification currently in use.
    */
   public double getCurrentMagnification();

   /**
    * Sets the current magnification factor. Invalid values are
    * ignored.
    */
   public void setCurrentMagnification(double factor);

   public double zoomAction(ZoomValue zoomValue);

   public JDRResources getResources();
}
