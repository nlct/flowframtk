package com.dickimawbooks.flowframtk;

import com.dickimawbooks.jdr.JDRCompleteObject;

public class OldNewObject
{
   public OldNewObject(JDRCompleteObject originalObject)
   {
      this(originalObject, originalObject.getIndex());
   }

   public OldNewObject(JDRCompleteObject originalObject, int index)
   {
      if (originalObject == null)
      {
         throw new NullPointerException();
      }

      this.oldObject = originalObject;
      this.newObject = originalObject;
      this.index = index;
   }

   public void setNewObject(JDRCompleteObject newObject)
   {
      this.newObject = newObject;
      changed = true;
   }

   public JDRCompleteObject getOldObject()
   {
      return oldObject;
   }

   public JDRCompleteObject getNewObject()
   {
      return newObject;
   }

   public int getIndex()
   {
      return index;
   }

   public boolean hasChanged()
   {
      return changed;
   }

   public void reset()
   {
      newObject = oldObject;
      changed = false;
   }

   JDRCompleteObject oldObject, newObject;
   int index;
   boolean changed = false;
}
