/*
    Copyright (C) 2025 Nicola L.C. Talbot
    www.dickimaw-books.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package com.dickimawbooks.flowframtk.dialog;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import com.dickimawbooks.jdr.JDRCompleteObject;
import com.dickimawbooks.jdrresources.JDRResources;

public class JDRCompleteObjectJList extends JList<FindListItem>
{
   public JDRCompleteObjectJList()
   {
      super();
      descriptionModel = new DefaultListModel<FindListItem>();
      setModel(descriptionModel);
   }

   public void setPrototype(String text)
   {
      setPrototypeCellValue(new FindListItem(null, text));
   }

   public void removeAllElements()
   {
      descriptionModel.removeAllElements();
   }

   public void addElement(FindListItem element)
   {
      descriptionModel.addElement(element);
   }

   public void addObject(JDRCompleteObject object, JDRResources resources)
   {
      String description = object.getDescription();
      String displayedDescription = description;

      if (description.isEmpty())
      {
         displayedDescription = resources.getDefaultDescription(object);
      }

      addElement(new FindListItem(object, displayedDescription));
   }

   public JDRCompleteObject getObject(int idx)
   {
      return descriptionModel.get(idx).getObject();
   }

   public JDRCompleteObject getSelectedObject()
   {
      FindListItem item = getSelectedValue();

      return item == null ? null : item.getObject();
   }

   DefaultListModel<FindListItem> descriptionModel;
}
