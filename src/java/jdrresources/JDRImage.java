// File          : JDRImage.java
// Description   : Interface representing a JDR image
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

import com.dickimawbooks.jdr.*;

/**
 * Interface representing a JDR image.
 */
public interface JDRImage extends JDRConstants
{
   /**
    * Gets all the objects that have been selected and returns
    * them in a group.
    */
   public JDRGroup getSelection();

   /**
    * Copies all the objects within the group and adds them to 
    * this image.
    * @param group the contents of which are copied to this image.
    * Note that the group itself is not copied.
    */
   public void copySelection(JDRGroup group);

   /**
    * Returns the current font settings.
    */
   public JDRFont getCurrentFont();
}
