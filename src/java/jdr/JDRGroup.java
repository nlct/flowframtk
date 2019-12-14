// File          : JDRGroup.java
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

package com.dickimawbooks.jdr;

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing a group of {@link JDRCompleteObject} objects.
 * Supported objects are: paths ({@link JDRPath}), text areas
 * ({@link JDRText}), links to bitmaps ({@link JDRBitmap})
 * and groups (<code>JDRGroup</code>).
 * <p>
 * The objects within a group have a stacking order which is
 * the order in which they are painted. Objects at the front of
 * the stacking order are those painted last and objects at the
 * back of the stacking order are those painted first. Some of the
 * methods are applied recursively to subgroups while others 
 * treat subgroups as a single entity.
 *
 * The default initial storage capacity is
 * given by {@link JDRGroup#getInitCapacity(CanvasGraphics)}, but expands as the array
 * is filled to capacity. The expansion is determined by
 * {@link CanvasGraphics#getOptimize()}: if the value returned is
 * {@link JDR#OPTIMIZE_SPEED} the capacity is doubled, otherwise the capacity
 * is incremented by 5.

 * @author Nicola L C Talbot
 */

public class JDRGroup extends JDRCompleteObject 
   implements JDRDistortable
{
   /**
    * Initialises an empty group.
    */
   public JDRGroup(CanvasGraphics cg)
   {
      this(cg, getInitCapacity(cg));
   }

   public JDRGroup()
   {
      this((CanvasGraphics)null);
   }

   /**
    * Initialises an empty group with the specified capacity.
    */
   public JDRGroup(CanvasGraphics cg, int capacity)
   {
      super(cg);
      size_ = 0;
      objectList_ = new JDRCompleteObject[capacity];
   }

   /**
    * Creates a copy.
    */ 
   public JDRGroup(JDRGroup group)
   {
      super(group);
      size_ = group.size_;
      
      objectList_ = new JDRCompleteObject[group.getCapacity()];

      for (int i = 0; i < size_; i++)
      {
         objectList_[i] = (JDRCompleteObject)group.get(i).clone();
         objectList_[i].parent = this;
         objectList_[i].index_ = i;
      }
   }

   /**
    * Returns the current capacity of this path.
    * @return current capacity of this path
    */
   public int getCapacity()
   {
      return objectList_.length;
   }

   /**
    * Sets the current capacity of this path. The new capacity mus
t
    * be at least equal to the size of this path.
    * @param capacity the new capacity
    * @throws IllegalArgumentException if the new capacity is less
    * than the size of the path
    */
   public void setCapacity(int capacity)
      throws IllegalArgumentException
   {
      if (capacity < size_)
      {
         throw new IllegalArgumentException(
           "Can't set capacity to "+capacity+" for group of size "+size_);
      }

      enlargeList(capacity);
   }

   private void enlargeList()
   {
      int capacity = objectList_ == null ? 0 : objectList_.length;

      if (capacity == 0)
      {
         capacity = 5;
      }

      if (getCanvasGraphics().getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         enlargeList(2*capacity);
      }
      else
      {
         enlargeList(capacity+5);
      }
   }

   private synchronized void enlargeList(int capacity)
   {
      JDRCompleteObject[] list = new JDRCompleteObject[capacity];

      for (int i = 0; i < size_; i++)
      {
         list[i] = objectList_[i];
      }

      objectList_ = list;
   }

  private synchronized void addObjectToList(JDRCompleteObject object)
   throws NullPointerException
   {
      if (object == null)
      {
         throw new NullPointerException(
            "Null objects may not be added to group");
      }

      if (getCapacity() == size_)
      {
         enlargeList();
      }

      objectList_[size_] = object;

      object.parent = this;
      object.index_ = size_;

      size_++;
   }

   private synchronized void addObjectToList(int index, JDRCompleteObject object)
      throws ArrayIndexOutOfBoundsException,NullPointerException
   {
      if (index < 0 || index > size_)
      {
         throw new ArrayIndexOutOfBoundsException(index);
      }

      if (object == null)
      {
         throw new NullPointerException(
            "Null objects may not be added to group");
      }

      if (getCapacity() == size_)
      {
         enlargeList();
      }

      for (int i = size_; i > index; i--)
      {
         objectList_[i] = objectList_[i-1];
         objectList_[i].index_ = i;
      }

      objectList_[index] = object;

      object.parent = this;
      object.index_ = index;

      size_++;
   }

   private synchronized JDRCompleteObject removeObjectFromList(JDRCompleteObject object)
   {
      int index = -1;

      for (int i = 0; i < size_; i++)
      {
         if (objectList_[i] == object)
         {
            index = i;
            break;
         }
      }

      if (index == -1) return null;

      return removeObjectFromList(index);
   }

   private synchronized JDRCompleteObject removeObjectFromList(int index)
   throws ArrayIndexOutOfBoundsException
   {
      if (index < 0 || index >= size_)
      {
         throw new ArrayIndexOutOfBoundsException(index);
      }

      JDRCompleteObject object = objectList_[index];

      for (int i = index; i < size_-1; i++)
      {
         objectList_[i] = objectList_[i+1];
         objectList_[i].index_ = i;
      }

      size_--;

      // this object may have been added to another
      // group before being removed from this group
      if (object.parent == this)
      {
         object.parent = null;
         object.index_ = -1;
      }

      return object;
   }

   private synchronized JDRCompleteObject setObject(int index, JDRCompleteObject object)
   throws ArrayIndexOutOfBoundsException,NullPointerException
   {
      if (index < 0 || index >= size_)
      {
         throw new ArrayIndexOutOfBoundsException(index);
      }

      if (object == null)
      {
         throw new NullPointerException(
            "Group can't contain null objects");
      }

      JDRCompleteObject oldObject = objectList_[index];
      objectList_[index] = object;

      object.parent = this;
      object.index_ = index;

      // Old object retains its parent and index for reference.
      return oldObject;
   }


   /**
    * Refreshes each object within this group. This calls
    * {@link JDRCompleteObject#refresh()} on each object within this group.
    * This method is applied recursively to subgroups. If an
    * object can't be refreshed it is removed from the group. This
    * method always returns true.
    * @return true
    */
   public boolean refresh()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (!object.refresh())
         {
            remove(i);
            i--;
         }
      }

      return true;
   }

   /**
    * Updates the bounds of each object within this group. This
    * just calls {@link JDRCompleteObject#updateBounds()} on each
    * object within this group. This method is applied recursively
    * to subgroups.
    */
   public void updateBounds()
   {
      for (int i = 0; i < size_; i++)
      {
         get(i).updateBounds();
      }
   }

   /**
    * Adds an object to this group. The group is appended to the
    * end so it is at the front of the stacking order.
    * @param object the object to add to this group
    * @throws NullPointerException if the object is null
    */
   public void add(JDRCompleteObject object)
   throws NullPointerException
   {
      addObjectToList(object);
   }

   /**
    * Adds an object to this group at the specified location.
    * A location of 0 indicates the back of the stack (i.e. the
    * first object in this group to be painted). Later objects are
    * shifted along the stack.
    * @param location the index at which the specified object is to
    * be inserted
    * @param object the object to add to this group
    * @throws NullPointerException if the object is null
    * @throws ArrayIndexOutOfBoundsException if location &lt; 0
    * or location &gt;= size()
    */
   public void add(int location, JDRCompleteObject object)
   throws NullPointerException,ArrayIndexOutOfBoundsException
   {
      addObjectToList(location, object);
   }

   /**
    * Replaces the object at the given location in this group 
    * with a new object.
    * @param object the new object
    * @param location the location within the stack
    * @return the element previously at the specified location
    */
   public JDRCompleteObject set(int location, JDRCompleteObject object)
   {
      return setObject(location, object);
   }

   /**
    * Swaps the objects at the given indices.
    * @param idx1 index of first object
    * @param idx2 index of second object
    */ 

   public synchronized void swap(int idx1, int idx2)
   {
      if (idx1 < 0 || idx1 >= size_)
      {
         throw new ArrayIndexOutOfBoundsException(idx1);
      }

      if (idx2 < 0 || idx2 >= size_)
      {
         throw new ArrayIndexOutOfBoundsException(idx2);
      }

      JDRCompleteObject tmp = objectList_[idx1];

      objectList_[idx1] = objectList_[idx2];
      objectList_[idx2] = tmp;
   }

   /**
    * Removes the object at the given location.
    * @param location the location of the object to remove
    * @return element that was removed
    * @throws ArrayIndexOutOfBoundsException if the location is
    * out of range
    */
   public JDRCompleteObject remove(int location)
   throws ArrayIndexOutOfBoundsException
   {
      return removeObjectFromList(location);
   }

   /**
    * Removes the given object from this group.
    * @param object the object to remove
    * @return true if the object was found
    */
   public boolean remove(JDRCompleteObject object)
   {
      return (removeObjectFromList(object) != null);
   }

   /**
    * Removes all of the elements from this list.
    */
   public synchronized void clear()
   {
      for (int i = 0; i < size(); i++)
      {
         JDRCompleteObject object = get(i);
         object.parent = null;
         object.index_ = -1;
      }

      size_ = 0;
   }

   /**
    * Compares the specified object with this group for equality.
    * @param o the object with which to compare this group
    * @return true if the specified object is equal to this group
    */
   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null) return false;
      if (!(o instanceof JDRGroup)) return false;

      if (!super.equals(o)) return false;

      JDRGroup group = (JDRGroup)o;

      if (size_ != group.size_) return false;

      for (int i = 0; i < size_; i++)
      {
         if (get(i) != group.get(i)) return false;
      }

      return true;
   }

   /**
    * Gets the size of this group.
    * @return the number of objects in this group
    */
   public int size()
   {
      return size_;
   }

   /**
    * Determines if this group is empty.
    * @return true if this group contains no objects
    */
   public boolean isEmpty()
   {
      return (size_ == 0);
   }

   /**
    * Returns <code>true</code> if this group contains the specified
    * element.
    * @param o the element whose presence in this group is to be tested
    * @return true if this group contains the specified element
    * @throws ClassCastException if the type of the specified element
    * is incompatible with this group
    * @throws NullPointerException if the specified element is null
    */
   public boolean contains(Object o)
   throws NullPointerException
   {
      if (o == null)
      {
         throw new NullPointerException();
      }

      for (int i = 0; i < size_; i++)
      {
         if (get(i) == o) return true;
      }

      return false;
   }

   /**
    * Gets the number of selected objects within this group.
    * @return the number of objects in this group that have been 
    * selected
    * @see #anySelected()
    * @see #numberSelected(int[])
    * @see #numberPathsSelected()
    * @see #numberTextPathsSelected()
    * @see #numberTextSelected()
    * @see #numberBitmapsSelected()
    * @see #numberGroupsSelected()
    */
   public int numberSelected()
   {
      int n = 0;

      for (int i = 0; i < size_; i++)
      {
         if (get(i).isSelected()) n++;
      }

      return n;
   }

   /**
    * Determines if any objects within this group have been
    * selected. This is quicker than checking if 
    * {@link #numberSelected()} is greater than zero if there
    * are more than one objects selected.
    * @return true if any objects within this group have been
    * selected otherwise returns false
    * @see #numberSelected()
    * @see #anyPathsSelected()
    * @see #anyTextPathsSelected()
    * @see #anyTextSelected()
    * @see #anyBitmapsSelected()
    * @see #anyGroupsSelected()
    */
   public boolean anySelected()
   {
      for (int i = 0; i < size_; i++)
      {
         if (get(i).isSelected()) return true;
      }

      return false;
   }

   /**
    * Determines if there are any selected objects within this
    * group whose bounding box encompasses the given point.
    * @param p the given point
    * @return true if there is at least one object in this
    * group that contains the given point otherwise false
    * @see #anySelected()
    */
   public boolean anySelected(Point2D p)
   {
      CanvasGraphics cg = getCanvasGraphics();

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected()) 
         {
            double hoffset = 0.0;
            double voffset = 0.0;

            if (object.flowframe != null && cg.isEvenPage())
            {
               hoffset = -object.flowframe.getEvenXShift();
               voffset = -object.flowframe.getEvenYShift();
            }

            if (object.getStorageBBox().contains(
                  p.getX()+hoffset, p.getY()+voffset))
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Determines if there are any paths in this group that have
    * been selected. This method recursively calls 
    * {@link #anyPathsSelected()} on subgroups.
    * @return true if there are any paths in this group that have
    * been selected otherwise false
    * @see #anySelected()
    * @see #anyTextSelected()
    * @see #anyTextPathsSelected()
    * @see #anySymmetricPathsSelected()
    * @see #anyBitmapsSelected()
    * @see #anyGroupsSelected()
    */
   public boolean anyPathsSelected()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               if (((JDRGroup)object).anyPathsSelected()) return true;
            }
            else if (object instanceof JDRPath)
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Determines if there are any text paths in this group that have
    * been selected. This method recursively calls 
    * {@link #anyTextPathsSelected()} on subgroups.
    * @return true if there are any text paths in this group that have
    * been selected otherwise false
    * @see #anySelected()
    * @see #anyTextSelected()
    * @see #anyPathsSelected()
    * @see #anyBitmapsSelected()
    * @see #anyGroupsSelected()
    */
   public boolean anyTextPathsSelected()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               if (((JDRGroup)object).anyTextPathsSelected()) return true;
            }
            else if (object instanceof JDRTextPath)
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Determines if there are any symmetric paths in this group that have
    * been selected. This method recursively calls 
    * {@link #anySymmetricPathsSelected()} on subgroups.
    * @return true if there are any symmetric paths in this group that have
    * been selected otherwise false
    * @see #anySelected()
    * @see #anyTextSelected()
    * @see #anyPathsSelected()
    * @see #anyTextPathsSelected()
    * @see #anyBitmapsSelected()
    * @see #anyGroupsSelected()
    */
   public boolean anySymmetricPathsSelected()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               if (((JDRGroup)object).anySymmetricPathsSelected())
               {
                  return true;
               }
            }
            else if (object instanceof JDRSymmetricPath)
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Gets the number of paths in this group that have been
    * selected. This method does not descend subgroups (doesn't
    * include text paths).
    * @return the number of paths in this group that have been
    * selected
    * @see #numberSelected()
    * @see #numberSelected(int[])
    * @see #numberTextSelected()
    * @see #numberTextPathsSelected()
    * @see #numberBitmapsSelected()
    * @see #numberGroupsSelected()
    */
   public int numberPathsSelected()
   {
      int numPaths = 0;

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRPath)
            {
               numPaths++;
            }
         }
      }

      return numPaths;
   }

   /**
    * Gets the number of symmetric paths in this group that have been
    * selected.
    * @return the number of symmetric paths in this group that have been
    * selected
    * @see #numberSelected()
    * @see #numberSelected(int[])
    * @see #numberTextSelected()
    * @see #numberPathsSelected()
    * @see #numberTextPathsSelected()
    * @see #numberBitmapsSelected()
    * @see #numberGroupsSelected()
    */
   public int numberSymmetricPathsSelected()
   {
      int numPaths = 0;

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRSymmetricPath)
            {
               numPaths++;
            }
         }
      }

      return numPaths;
   }

   /**
    * Gets the number of text paths in this group that have been
    * selected. This method does not descend subgroups.
    * @return the number of text paths in this group that have been
    * selected
    * @see #numberSelected()
    * @see #numberSelected(int[])
    * @see #numberTextSelected()
    * @see #numberPathsSelected()
    * @see #numberBitmapsSelected()
    * @see #numberGroupsSelected()
    */
   public int numberTextPathsSelected()
   {
      int numTextPaths = 0;

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRTextPath)
            {
               numTextPaths++;
            }
         }
      }

      return numTextPaths;
   }

   /**
    * Determines if there are any text areas in this group that have
    * been selected. This method recursively calls 
    * {@link #anyTextSelected()} on subgroups.
    * @return true if there are any text areas in this group that have
    * been selected otherwise false
    * @see #anySelected()
    * @see #anyPathsSelected()
    * @see #anyTextPathsSelected()
    * @see #anyBitmapsSelected()
    * @see #anyGroupsSelected()
    */
   public boolean anyTextSelected()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               if (((JDRGroup)object).anyTextSelected()) return true;
            }
            else if (object instanceof JDRText)
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Gets the number of text areas in this group that have been
    * selected. This method does not descend subgroups.
    * @return the number of text areas in this group that have been
    * selected
    * @see #numberSelected()
    * @see #numberSelected(int[])
    * @see #numberPathsSelected()
    * @see #numberBitmapsSelected()
    * @see #numberGroupsSelected()
    */
   public int numberTextSelected()
   {
      int numText = 0;

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRText)
            {
               numText++;
            }
         }
      }

      return numText;
   }

   /**
    * Determines if there are any bitmaps in this group that have
    * been selected. This method recursively calls 
    * {@link #anyBitmapsSelected()} on subgroups.
    * @return true if there are any bitmaps in this group that have
    * been selected otherwise false
    * @see #anySelected()
    * @see #anyPathsSelected()
    * @see #anyTextSelected()
    * @see #anyGroupsSelected()
    */
   public boolean anyBitmapsSelected()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               if (((JDRGroup)object).anyBitmapsSelected())
                  return true;
            }
            else if (object instanceof JDRBitmap)
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Gets the number of bitmaps in this group that have been
    * selected. This method does not descend subgroups.
    * @return the number of bitmaps in this group that have been
    * selected
    * @see #numberSelected()
    * @see #numberSelected(int[])
    * @see #numberPathsSelected()
    * @see #numberTextSelected()
    * @see #numberGroupsSelected()
    */
   public int numberBitmapsSelected()
   {
      int numBitmaps = 0;

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRBitmap)
            {
               numBitmaps++;
            }
         }
      }

      return numBitmaps;
   }

   /**
    * Determines if there are any subgroups in this group that have
    * been selected. This method recursively calls 
    * {@link #anyGroupsSelected()} on subgroups.
    * @return true if there are any subgroups in this group that have
    * been selected otherwise false
    * @see #anySelected()
    * @see #anyPathsSelected()
    * @see #anyTextSelected()
    * @see #anyBitmapsSelected()
    */
   public boolean anyGroupsSelected()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected())
         {
            if (object instanceof JDRGroup)
            {
               return true;
            }
         }
      }

      return false;
   }

   public JDRTextual getTextual()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         JDRTextual text = object.getTextual();

         if (text != null) return text;
      }

      return null;
   }

   public boolean hasTextual()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.hasTextual()) return true;
      }

      return false;
   }

   public boolean hasShape()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.hasShape()) return true;
      }

      return false;
   }

   public JDRSymmetricPath getSymmetricPath()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         JDRSymmetricPath path = object.getSymmetricPath();

         if (path != null) return path;
      }

      return null;
   }

   public boolean hasSymmetricPath()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.hasSymmetricPath()) return true;
      }

      return false;
   }

   /**
    * Gets the first pattern within this group. This method descends
    * subgroups.
    * @return the first pattern within this group or null if none found
    * @see #getText()
    * @see #getPath()
    * @see #getBitmap()
    * @see #getRotationalPattern()
    */

   public JDRPattern getPattern()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         JDRPattern path = object.getPattern();

         if (path != null) return path;
      }

      return null;
   }

   public boolean hasPattern()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.hasPattern()) return true;
      }

      return false;
   }

   public int getObjectFlag()
   {
      int flag = super.getObjectFlag() | SELECT_FLAG_GROUP;

      for (int i = 0, n = size(); i < n; i++)
      {
         JDRCompleteObject object = get(i);

         flag = (flag | object.getObjectFlag());

         if (flag == SELECT_FLAG_ANY)
         {
            // don't need to continue
            return flag;
         }
      }

      return flag;
   }

   /**
    * Gets the first selected object in this group.
    * @return the first selected object in this group or null
    * if no objects have been selected
    */
   public JDRCompleteObject getSelected()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isSelected()) return object;
      }

      return null;
   }

   /**
    * Gets all objects in this group that intersect the given box.
    * @param box the area under inspection (in storage units)
    * @return a group containing all the objects in this group that
    * intersect the given area
    * @see #getAllInsideStorageBox(BBox)
    */
   public Vector<JDRCompleteObject> getAllIntersectsStorageBox(BBox box)
   {
      Vector<JDRCompleteObject> grp = new Vector<JDRCompleteObject>();

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.intersectsStorageBox(box))
         {
            grp.add(object);
         }
      }

      return grp;
   }

   /**
    * Gets all objects in this group that are inside the given box.
    * @param box the area under inspection
    * @return a group containing all the objects in this group that
    * are inside the given area
    * @see #getAllIntersectsStorageBox(BBox)
    */
   public Vector<JDRCompleteObject> getAllInsideStorageBox(BBox box)
   {
      Vector<JDRCompleteObject> grp = new Vector<JDRCompleteObject>();

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isCompletelyInsideStorageBox(box))
         {
            grp.add(object);
         }
      }
      return grp;
   }

   /**
    * Selects all the objects in this group that intersect the
    * given area.
    * @param box the area under inspection
    * @return the number of objects that have been selected (the
    * count does not include objects within subgroups - the subgroup
    * is counted as a single entity)
    * @see #selectAllInsideStorageBox(BBox)
    */
   public int selectAllIntersectsStorageBox(BBox box)
   {
      int n=0;

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.intersectsStorageBox(box))
         {
            object.setSelected(true);
            n++;
         }
      }

      return n;
   }

   /**
    * Selects all the objects in this group that are inside the
    * given area.
    * @param box the area under inspection
    * @return the number of objects that have been selected (the
    * count does not include objects within subgroups - the subgroup
    * is counted as a single entity)
    * @see #selectAllIntersectsStorageBox(BBox)
    */
   public int selectAllInsideStorageBox(BBox box)
   {
      int n=0;

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.isCompletelyInsideStorageBox(box))
         {
            object.setSelected(true);
            n++;
         }
      }

      return n;
   }

   /**
    * Gets the stroke associated with the first path found in this
    * group. This method descends subgroups.
    * @return the stroke of the first path found or null
    * if no paths found
    */
   public JDRStroke getStroke()
   {
      JDRShape path = getShape();

      if (path != null)
      {
         return path.getStroke();
      }

      return null;
   }

   /**
    * Gets the stroke associated with the first path found in this
    * group. This method descends subgroups.
    * @return the stroke of the first path found or null
    * if no paths found
    */
   public JDRBasicStroke getBasicStroke()
   {
      for (int i = 0; i < size(); i++)
      {
         JDRCompleteObject obj = get(i);

         if (obj instanceof JDRShape
          && ((JDRShape)obj).getStroke() instanceof JDRBasicStroke)
         {
            return (JDRBasicStroke)((JDRShape)obj).getStroke();
         }
      }

      return null;
   }

   /**
    * Gets the line colour associated with the first shape found in this
    * group. This method descends subgroups.
    * @return the line colour of the first shape found or null
    * if no paths found
    */
   public JDRPaint getLinePaint()
   {
      JDRShape path = getShape();

      if (path != null)
      {
         return path.getLinePaint();
      }

      return null;
   }

   /**
    * Gets the fill colour associated with the first shape found in this
    * group. This method descends subgroups.
    * @return the fill colour of the first shape found or null
    * if no paths found
    */
   public JDRPaint getFillPaint()
   {
      JDRShape path = getShape();

      if (path != null)
      {
         return path.getFillPaint();
      }

      return null;
   }

   /**
    * Gets the text colour associated with the first text area 
    * found in this group. This method descends subgroups.
    * @return the text colour of the first text area found or null 
    * if no text areas found
    */
   public JDRPaint getTextPaint()
   {
      JDRText text = getText();

      if (text != null)
      {
         return text.getTextPaint();
      }

      return null;
   }

   /**
    * Gets the first text area within this group. This method descends
    * subgroups.
    * @return the first text area within this group or null if none 
    * found
    * @see #getPath()
    * @see #getBitmap()
    */
   public JDRText getText()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object instanceof JDRText)
         {
            return (JDRText)object;
         }

         if (object instanceof JDRGroup)
         {
            JDRText txt = ((JDRGroup)object).getText();

            if (txt != null) return txt;
         }
      }

      return null;
   }

   /**
    * Gets the first path within this group. This method descends
    * subgroups.
    * @return the first path within this group or null if none found
    * @see #getText()
    * @see #getBitmap()
    */

   public JDRPath getPath()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object instanceof JDRPath)
         {
            return (JDRPath)object;
         }

         if (object instanceof JDRGroup)
         {
            JDRPath path = ((JDRGroup)object).getPath();

            if (path != null) return path;
         }
      }

      return null;
   }

   /**
    * Gets the first shape within this group. This method descends
    * subgroups.
    * @return the first shape within this group or null if none found
    * @see #getPath()
    * @see #getSymmetricPath()
    * @see #getTextPath()
    * @see #getText()
    * @see #getBitmap()
    */

   public JDRShape getShape()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object instanceof JDRShape)
         {
            return (JDRShape)object;
         }

         if (object instanceof JDRGroup)
         {
            JDRShape shape = ((JDRGroup)object).getShape();

            if (shape != null) return shape;
         }
      }

      return null;
   }

   /**
    * Gets the first shape within this group that isn't an instance
    * of {@link JDRTextual}. This method descends
    * subgroups.
    * @return the first non-textual shape within this group or null if none found
    * @see #getPath()
    * @see #getSymmetricPath()
    * @see #getTextPath()
    * @see #getText()
    * @see #getBitmap()
    */

   public JDRShape getNonTextShape()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object instanceof JDRShape
         && !(object instanceof JDRTextual))
         {
            return (JDRShape)object;
         }

         if (object instanceof JDRGroup)
         {
            JDRShape shape = ((JDRGroup)object).getNonTextShape();

            if (shape != null) return shape;
         }
      }

      return null;
   }

   /**
    * Gets the first text path within this group. This method descends
    * subgroups.
    * @return the first text path within this group or null if none found
    * @see #getShape()
    * @see #getPath()
    * @see #getSymmetricPath()
    * @see #getText()
    * @see #getBitmap()
    */

   public JDRTextPath getTextPath()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object instanceof JDRTextPath)
         {
            return (JDRTextPath)object;
         }

         if (object instanceof JDRGroup)
         {
            JDRTextPath path = ((JDRGroup)object).getTextPath();

            if (path != null) return path;
         }
      }

      return null;
   }

   /**
    * Gets the first bitmap within this group. This method descends
    * subgroups.
    * @return the first bitmap within this group or null if none found
    * @see #getText()
    * @see #getPath()
    */

   public JDRBitmap getBitmap()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object instanceof JDRBitmap)
         {
            return (JDRBitmap)object;
         }

         if (object instanceof JDRGroup)
         {
            JDRBitmap bitmap = ((JDRGroup)object).getBitmap();
            if (bitmap != null) return bitmap;
         }
      }

      return null;
   }

   /**
    * Gets the first rotational pattern within this group. This method descends
    * subgroups.
    * @return the first rotational pattern within this group or null if none found
    * @see #getText()
    * @see #getPath()
    * @see #getBitmap()
    */

   public JDRRotationalPattern getRotationalPattern()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object instanceof JDRRotationalPattern)
         {
            return (JDRRotationalPattern)object;
         }

         if (object instanceof JDRGroup)
         {
            JDRRotationalPattern pattern = ((JDRGroup)object).getRotationalPattern();
            if (pattern != null) return pattern;
         }
      }

      return null;
   }

   /**
    * Sets the stroke for all paths contained within this group.
    * This method descends subgroups.
    */
   public void setStroke(JDRStroke s)
   {
      if (objectList_ == null) return;

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object instanceof JDRShape)
         {
            ((JDRShape)object).setStroke(s);
         }
         else if (object instanceof JDRGroup)
         {
            ((JDRGroup)object).setStroke(s);
         }
      }
   }

   /**
    * Sets the line colour for all paths contained within this group.
    * This method descends subgroups.
    */
   public void setLinePaint(JDRPaint paint)
   {
      if (objectList_ == null) return;

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object instanceof JDRShape)
         {
            ((JDRShape)object).setLinePaint(paint);
         }
         else if (object instanceof JDRGroup)
         {
            ((JDRGroup)object).setLinePaint(paint);
         }
      }
   }

   /**
    * Sets the fill colour for all paths contained within this group.
    * This method descends subgroups.
    */
   public void setFillPaint(JDRPaint paint)
   {
      if (objectList_ == null) return;

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object instanceof JDRShape)
         {
            ((JDRShape)object).setFillPaint(paint);
         }
         else if (object instanceof JDRGroup)
         {
            ((JDRGroup)object).setFillPaint(paint);
         }
      }
   }

   /**
    * Sets the text colour for all text areas contained within this 
    * group.
    * This method descends subgroups.
    */
   public void setTextPaint(JDRPaint paint)
   {
      if (objectList_ == null) return;

      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object instanceof JDRTextual)
         {
            ((JDRTextual)object).setTextPaint(paint);
         }
         else if (object instanceof JDRGroup)
         {
            ((JDRGroup)object).setTextPaint(paint);
         }
      }
   }

   /**
    * Gets the bounding box for this group. This is the minimum box
    * that encompasses all the bounding boxes of all objects contained
    * in this group.
    */
   public BBox getStorageBBox()
   {
      if (isEmpty())
      {
         return null;
      }

      BBox box = get(0).getStorageBBox();

      for (int i = 1; i < size_; i++)
      {
         JDRCompleteObject obj = get(i);

         obj.mergeStorageBBox(box);
      }

      return box;
   }

   public void mergeStorageBBox(BBox box)
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject obj = get(i);

         obj.mergeStorageBBox(box);
      }
   }

   /**
    * Gets the object at the specified position.
    * @param index the index of the element to return
    * @return the object at the specified index
    * @throws ArrayIndexOutOfBoundsException if index is out
    * of range (index &lt; 0 || index &gt;= size())
    */
   public JDRCompleteObject get(int index)
   throws ArrayIndexOutOfBoundsException
   {
      return objectList_[index];
   }

   /**
    * Gets the last (frontmost) object in this group.
    * Returns null if this group is empty.
    * @return the last object in this group or null if this group is
    * empty
    */
   public JDRCompleteObject lastElement()
   {
      if (size_ == 0) return null;

      return objectList_[size_-1];
   }

   /**
    * Gets the first (backmost) object in this group.
    * Returns null if this group is empty.
    * @return the first object in this group or null if this group
    * is empty
    */
   public JDRCompleteObject firstElement()
   {
      if (size_ == 0) return null;

      return objectList_[0];
   }

   /**
    * Recursively sets selected flag for all objects in this group.
    * @param flag if true set all objects in this group to be
    * selected otherwise set all objects in this group to be
    * deselected
    */
   public void setSelected(boolean flag)
   {
      super.setSelected(flag);

      for (int i = 0; i < size_; i++)
      {
         get(i).setSelected(flag);
      }
   }

   /**
    * Rotates all objects in this group relative to the centre of
    * the group.
    */
   public void rotate(double angle)
   {
      BBox box = getStorageBBox();

      if (box == null)
      {
         return;
      }

      Point2D p = new Point2D.Double(box.getMinX()+box.getWidth()/2,
                                     box.getMinY()+box.getHeight()/2);

      for (int i = 0; i < size_; i++)
      {
         get(i).rotate(p,angle);
      }
   }

   public void rotate(Point2D p, double angle)
   {
      for (int i = 0; i < size_; i++)
      {
         get(i).rotate(p, angle);
      }
   }

   /**
    * Scales all objects in this group relative to the top left
    * corner of this group's bounding box.
    */
   public void scale(double factorX, double factorY)
   {
      BBox box = getStorageBBox();

      if (box == null)
      {
         return;
      }

      Point2D p = new Point2D.Double(box.getMinX(), box.getMinY());

      for (int i = 0; i < size_; i++)
      {
         get(i).scale(p, factorX, factorY);
      }
   }

   public void scale(Point2D p, double factorX, double factorY)
   {
      for (int i = 0; i < size_; i++)
      {
         get(i).scale(p, factorX, factorY);
      }
   }

   /**
    * Horizontally scales all objects in this group relative to the 
    * left edge of this group's bounding box.
    */
   public void scaleX(double factor)
   {
      BBox box = getStorageBBox();

      if (box == null)
      {
         return;
      }

      Point2D p = new Point2D.Double(box.getMinX(), box.getMinY());

      for (int i = 0; i < size_; i++)
      {
         get(i).scaleX(p, factor);
      }
   }

   /**
    * Vertically scales all objects in this group relative to the 
    * top edge of this group's bounding box.
    */
   public void scaleY(double factor)
   {
      BBox box = getStorageBBox();

      if (box == null)
      {
         return;
      }

      Point2D p = new Point2D.Double(box.getMinX(), box.getMinY());

      for (int i = 0; i < size_; i++)
      {
         get(i).scaleY(p, factor);
      }
   }

   /**
    * Shears all objects in this group relative to the bottom left
    * corner of this group's bounding box.
    */
   public void shear(double factorX, double factorY)
   {
      BBox box = getStorageBBox();

      if (box == null)
      {
         return;
      }

      Point2D p = new Point2D.Double(box.getMinX(), box.getMaxY());

      for (int i = 0; i < size_; i++)
      {
         get(i).shear(p, factorX, factorY);
      }
   }

   public void shear(Point2D p, double factorX, double factorY)
   {
      for (int i = 0; i < size_; i++)
      {
         get(i).shear(p, factorX, factorY);
      }
   }

   /**
    * Shifts all objects within this group so that they align
    * with the left edge of this group's bounding box. This
    * method does not descend subgroups.
    * @see #centreAlign()
    * @see #rightAlign()
    * @see #topAlign()
    * @see #middleAlign()
    * @see #bottomAlign()
    */
   public void leftAlign()
   {
      BBox bbox = getStorageBBox();

      if (bbox == null)
      {
         return;
      }

      double leftmostX = bbox.getMinX();

      for (int i = 0; i < size_; i++)
      {
         get(i).leftAlign(leftmostX);
      }
   }

   /**
    * Shifts all objects within this group so that they align
    * with the right edge of this group's bounding box. This
    * method does not descend subgroups.
    * @see #leftAlign()
    * @see #centreAlign()
    * @see #topAlign()
    * @see #middleAlign()
    * @see #bottomAlign()
    */
   public void rightAlign()
   {
      BBox bbox = getStorageBBox();

      if (bbox == null)
      {
         return;
      }

      double rightmostX = bbox.getMaxX();

      for (int i = 0; i < size_; i++)
      {
         get(i).rightAlign(rightmostX);
      }
   }

   /**
    * Shifts all objects within this group so that they align
    * with the centre of this group's bounding box. This
    * method does not descend subgroups.
    * @see #leftAlign()
    * @see #rightAlign()
    * @see #topAlign()
    * @see #middleAlign()
    * @see #bottomAlign()
    */
   public void centreAlign()
   {
      BBox bbox = getStorageBBox();

      if (bbox == null)
      {
         return;
      }

      double centremostX = bbox.getMinX() + 0.5*bbox.getWidth();

      for (int i = 0; i < size_; i++)
      {
         get(i).centreAlign(centremostX);
      }
   }

   /**
    * Shifts all objects within this group so that they align
    * with the top edge of this group's bounding box. This
    * method does not descend subgroups.
    * @see #leftAlign()
    * @see #centreAlign()
    * @see #rightAlign()
    * @see #middleAlign()
    * @see #bottomAlign()
    */
   public void topAlign()
   {
      BBox bbox = getStorageBBox();

      if (bbox == null)
      {
         return;
      }

      double topmostY = bbox.getMinY();

      for (int i = 0; i < size_; i++)
      {
         get(i).topAlign(topmostY);
      }
   }

   /**
    * Shifts all objects within this group so that they align
    * with the middle of this group's bounding box. This
    * method does not descend subgroups.
    * @see #leftAlign()
    * @see #centreAlign()
    * @see #rightAlign()
    * @see #topAlign()
    * @see #bottomAlign()
    */
   public void middleAlign()
   {
      BBox bbox = getStorageBBox();

      if (bbox == null)
      {
         return;
      }

      double middlemostY = bbox.getMinY()+0.5*bbox.getHeight();

      for (int i = 0; i < size_; i++)
      {
         get(i).middleAlign(middlemostY);
      }
   }

   /**
    * Shifts all objects within this group so that they align
    * with the bottom edge of this group's bounding box. This
    * method does not descend subgroups.
    * @see #middleAlign()
    * @see #topAlign()
    * @see #leftAlign()
    * @see #centreAlign()
    * @see #rightAlign()
    */
   public void bottomAlign()
   {
      BBox bbox = getStorageBBox();

      if (bbox == null)
      {
         return;
      }

      double bottommostY = bbox.getMaxY();

      for (int i = 0; i < size_; i++)
      {
         get(i).bottomAlign(bottommostY);
      }
   }

   /**
    * Translates all objects in this group. This method descends
    * subgroups.
    */
   public void translate(double x, double y)
   {
      for (int i = 0; i < size_; i++)
      {
         get(i).translate(x, y);
      }
   }

   /**
    * Transforms all objects in this group. This method descends
    * subgroups.
    */
   public void transform(double[] matrix)
   {
      for (int i = 0; i < size_; i++)
      {
         get(i).transform(matrix);
      }
   }

   /**
    * Moves the specified object to the back of the stacking
    * order. That is, it moves it to index 0.
    * @param object the object to move
    * @return true if this group contains the specified object
    * otherwise false
    * @see #moveToFront(JDRCompleteObject)
    */
   public boolean moveToBack(JDRCompleteObject object)
   {
      if (!remove(object))
      {
         return false;
      }

      add(0, object);

      return true;
   }

   /**
    * Moves the specified object to the front of the stacking
    * order. That is, it moves it to index size()-1.
    * @param object the object to move
    * @return true if this group contains the specified object
    * otherwise false
    * @see #moveToBack(JDRCompleteObject)
    */
   public boolean moveToFront(JDRCompleteObject object)
   {
      if (!remove(object))
      {
         return false;
      }

      add(object);

      return true;
   }

   /**
    * Merges all paths given by the index array within this group
    * into a single path. The indexed paths are removed from this
    * group and the new path is appended to this group.
    * @param indexArray array of indices of paths to be merged
    * @return new path created from merging indexed paths 
    * @throws NoPathException if indexArray is empty
    * @throws EmptyPathException if the first indexed path is empty
    * @throws CanOnlyMergePathsException if the object at a given
    * index is not an instanceof {@link JDRShape}
    */
   public JDRShape mergePaths(int[] indexArray)
      throws InvalidShapeException
   {
      int n = (indexArray == null ? size() : indexArray.length);

      if (n == 0)
      {
         throw new NoPathException(getCanvasGraphics());
      }

      JDRCompleteObject object = get(indexArray == null ? 0 : indexArray[0]);

      if (!(object instanceof JDRShape))
      {
         throw new CanOnlyMergePathsException(getCanvasGraphics());
      }

      JDRShape newPath = (JDRShape)object.clone();

      if (newPath.size() == 0)
      {
         throw new EmptyPathException(getCanvasGraphics());
      }

      if (newPath instanceof JDRCompoundShape)
      {
         newPath = newPath.getFullPath();
      }

      boolean isClosed = newPath.isClosed();

      if (isClosed)
      {
         newPath.open(false);
      }

      JDRPoint startPt = newPath.getFirstSegment().getStart();

      JDRPoint endPt = newPath.getLastSegment().getEnd();

      for (int i=1; i < n; i++)
      {
         object = get(indexArray == null ? i : indexArray[i]);

         if (!(object instanceof JDRShape))
         {
            throw new CanOnlyMergePathsException(getCanvasGraphics());
         }

         JDRShape path = (JDRShape)object;

         if (path instanceof JDRCompoundShape)
         {
            path = path.getFullPath();
         }

         for (int j=0, m=path.size(); j < m; j++)
         {
            JDRPathSegment segment = path.get(j);

            if (j == 0)
            {
               newPath.add(new JDRSegment(endPt, segment.getStart()));
            }

            if (segment instanceof JDRPartialSegment)
            {
               segment = ((JDRPartialSegment)segment).getFullSegment();
            }
            else
            {
               segment = (JDRSegment)segment.clone();
            }

            newPath.add((JDRSegment)segment);

            endPt = segment.getEnd();
         }
      }

      if (isClosed) newPath.close(new JDRSegment(endPt, startPt));

      if (indexArray != null)
      {
         for (int i = n-1; i >= 0; i--)
         {
            remove(indexArray[i]);
         }

         add(indexArray[0], newPath);
      }

      return newPath;
   }

   /**
    * Returns the index in this group of the first occurrence of
    * the specified object or -1 if this group does not contain
    * the object.
    * @param o the object to locate
    * @return the index in this group of the first occurrence of
    * the specified object or -1 if this group does not contain
    * the specified object
    */
   public int indexOf(Object o)
   {
      for (int i = 0; i < size_; i++)
      {
         if (get(i) == o) return i;
      }

      return -1;
   }

   /**
    * Returns the index in this group of the last occurrence of
    * the specified object or -1 if this group does not contain
    * the object.
    * @param o the object to locate
    * @return the index in this group of the last occurrence of
    * the specified object or -1 if this group does not contain
    * the specified object
    */
   public int lastIndexOf(Object o)
   {
      for (int i = size_-1; i >= 0; i--)
      {
         if (get(i) == o) return i;
      }

      return -1;
   }

   public void draw(FlowFrame parentFrame)
   {
      if (parentFrame == null)
      {
         parentFrame = flowframe;
      }

      for (int i = 0; i < size_; i++)
      {
         get(i).draw(parentFrame);
      }
   }

   public void draw(boolean draft, FlowFrame parentFrame)
   {
      if (parentFrame == null)
      {
         parentFrame = flowframe;
      }

      for (int i = 0; i < size_; i++)
      {
         get(i).draw(draft, parentFrame);
      }

      drawFlowFrame();
   }

   public void print(Graphics2D g2)
   {
      for (int i = 0; i < size_; i++)
      {
         get(i).print(g2);
      }
   }

   public void savePgf(TeX tex)
    throws IOException
   {
      if (!description.equals(""))
      {
         tex.comment(description);
      }

      for (int i = 0; i < size_; i++)
      {
         get(i).savePgf(tex);
      }
   }

   public void saveFlowframe(TeX pgf, Rectangle2D typeblock,
                             double baselineskip, boolean useHPaddingShapepar)
      throws IOException,InvalidShapeException
   {
      if (flowframe != null)
      {
         flowframe.tex(pgf, this, typeblock, baselineskip, useHPaddingShapepar);
      }
      else
      {
         for (int i = 0; i < size_; i++)
         {
            get(i).saveFlowframe(pgf, typeblock, baselineskip, useHPaddingShapepar);
         }
      }
   }

   /**
    * Checks if this group contains a flowframe of the given
    * type with the given label.
    * @param type the frame type
    * @param label flowframe label to be found
    * @return true if flowframe with given type and label
    * is defined either for this group or for any elements 
    * within this group (recursively searching subgroups)
    */
   public boolean isFlowframeDefined(int type, String label)
   {
      if (flowframe != null
       && flowframe.getType() == type
       && flowframe.label.equals(label))
      {
         return true;
      }

      for (int i = 0; i < size(); i++)
      {
         JDRCompleteObject obj = get(i);

         if (obj instanceof JDRGroup)
         {
            if (((JDRGroup)obj).isFlowframeDefined(type, label))
            {
               return true;
            }
         }
         else
         {
            if (obj.flowframe != null
             && obj.flowframe.getType() == type
             && obj.flowframe.label.equals(label))
            {
               return true;
            }
         }
      }

      return false;
   }

   public FlowFrame getFlowFrame(int type, String label)
   {
      if (flowframe != null
       && flowframe.getType() == type
       && flowframe.label.equals(label))
      {
         return flowframe;
      }

      for (int i = 0; i < size(); i++)
      {
         JDRCompleteObject obj = get(i);

         if (obj instanceof JDRGroup)
         {
            FlowFrame f = ((JDRGroup)obj).getFlowFrame(type, label);

            if (f != null) return f;
         }
         else
         {
            if (obj.flowframe != null
             && obj.flowframe.getType() == type
             && obj.flowframe.label.equals(label))
            {
               return obj.flowframe;
            }
         }
      }

      return null;
   }

   public Object clone()
   {
      return new JDRGroup(this);
   }

   public void makeEqual(JDRObject obj)
   {
      JDRGroup grp = (JDRGroup)obj;

      super.makeEqual(obj);
      clear();

      for (int i = 0; i < grp.size_; i++)
      {
         add((JDRCompleteObject)grp.get(i).clone());
      }
   }

   /**
    * Recursively checks all objects to determine whether they
    * contain any flowfram data. Doesn't check if thie group
    * is assigned flowframe data, but does check subgroups.
    * @return true if there is at least one object in this group
    * that contains flowframe data
    */
   public boolean anyFlowFrameData()
   {
      for (int i = 0; i < size(); i++)
      {
         JDRCompleteObject object = get(i);

         if (object.flowframe != null)
         {
            return true;
         }

         if (object instanceof JDRGroup)
         {
            if (((JDRGroup)object).anyFlowFrameData())
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Recursively checks all objects to determine whether there
    * are any draft bitmaps. A bitmap is set to draft if there
    * is insufficient memory to load the image.
    * @return true if there is at least one bitmap contained in
    * this group (or within subgroups of this group) that is in
    * draft mode
    * @see JDRBitmap#isDraft()
    */
   public boolean anyDraftBitmaps()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object instanceof JDRBitmap)
         {
            if (((JDRBitmap)object).isDraft())
            {
               return true;
            }
         }
         else if (object instanceof JDRGroup)
         {
            if (((JDRGroup)object).anyDraftBitmaps())
            {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Determines if there are any textpaths in this group.
    * Recursively descends subgroups.
    * @return true if there are any JDRTextPath objects in this
    * group
    */
   public boolean anyTextPaths()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject obj = get(i);

         if (obj instanceof JDRTextPath)
         {
            return true;
         }
         else if (obj instanceof JDRGroup)
         {
            if (((JDRGroup)obj).anyTextPaths())
            {
               return true;
            }
         }
      }

      return false;
   }

   public void saveEPS(PrintWriter out)
      throws IOException
   {
      for (int i = 0; i < size_; i++)
      {
         get(i).saveEPS(out);
      }
   } 

   public int psLevel()
   {
      int level = 1;

      for (int i = 0; i < size_; i++)
      {
         level = Math.max(level, get(i).psLevel());
      }

      return level;
   }

   public void saveSVG(SVG svg, String attr)
      throws IOException
   {
      svg.println("   <g>");

      if (!description.equals(""))
      {
         svg.println("   <desc>"+description+"</desc>");
      }

      for (int i = 0; i < size_; i++)
      {
         get(i).saveSVG(svg, attr);
      }

      svg.println("   </g>");
   }

   /**
    * Gets a string representation of this group.
    */
   public String toString()
   {
      return "JDRGroup: size="+size();
   }

   public JDRObjectLoaderListener getListener()
   {
      return groupListener;
   }

   /**
    * Returns the default initial capacity for new paths.
    * This method checks {@link CanvasGraphics#getOptimize()} and
    * will return either {@link #init_capacity_speed} (if
    * OPTIMIZE_SPEED) or
    * {@link #init_capacity_memory} (if OPTIMIZE_MEMORY or
    * OPTIMIZE_NONE)
    */
   public static int getInitCapacity(CanvasGraphics cg)
   {
      if (cg == null || cg.getOptimize() == CanvasGraphics.OPTIMIZE_SPEED)
      {
         return init_capacity_speed;
      }

      return init_capacity_memory;
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "Group:"+eol;
      str += "size: "+size_+eol;
      str += "capacity: "+getCapacity()+eol;
      str += "description: "+description+eol;
      str += "flowframe: "+flowframe+eol;
      str += "is selected: "+isSelected()+eol;
      BBox box = getStorageBBox();
      if (box == null)
      {
         str += "bounding box: null"+eol;
      }
      else
      {
         str += "bounding box: "+getStorageBBox().info()+eol;
      }
      str += "hash code: "+hashCode()+eol;

      for (int i = 0; i < size_; i++)
      {
         str += "Object "+i+":"+eol;
         str += objectList_[i].info()+eol;
      }

      return str;
   }

   public String[] getDescriptionInfo()
   {
      return new String[] {""+size()};
   }

   public void fade(double value)
   {
      for (int i = 0; i < size_; i++)
      {
         objectList_[i].fade(value);
      }
   }

   public boolean isDistortable()
   {
      for (int i = 0; i < size_; i++)
      {
         if (!objectList_[i].isDistortable())
         {
            return false;
         }
      }

      return true;
   }

   public BBox getStorageDistortionBounds()
   {
      BBox box = null;

      for (int i = 0; i < size_; i++)
      {
         if (box == null)
         {
            box = ((JDRDistortable)objectList_[i]).getStorageDistortionBounds();
         }
         else
         {
            box.merge(((JDRDistortable)objectList_[i]).getStorageDistortionBounds());
         }
      }

      return box;
   }

   public BBox getBpDistortionBounds()
   {
      BBox box = null;

      for (int i = 0; i < size_; i++)
      {
         if (box == null)
         {
            box = ((JDRDistortable)objectList_[i]).getBpDistortionBounds();
         }
         else
         {
            box.merge(((JDRDistortable)objectList_[i]).getBpDistortionBounds());
         }
      }

      return box;
   }

   public BBox getComponentDistortionBounds()
   {
      BBox box = null;

      for (int i = 0; i < size_; i++)
      {
         if (box == null)
         {
            box = ((JDRDistortable)objectList_[i]).getComponentDistortionBounds();
         }
         else
         {
            box.merge(((JDRDistortable)objectList_[i]).getComponentDistortionBounds());
         }
      }

      return box;
   }

   public void distort(JDRDistortable original,
     Shape[] area, AffineTransform[] trans)
   {
      JDRGroup orgGrp = (JDRGroup)original;

      for (int i = 0; i < size_; i++)
      {
         ((JDRDistortable)objectList_[i]).distort(
             ((JDRDistortable)orgGrp.get(i)),
              area, trans);
      }
   }

   public JDRPoint getControlFromStoragePoint(
     double x, double y, boolean endPoint)
   {
      return null;
   }

   public void drawControls(boolean endPoint)
   {
   }

   public boolean containsFlowFrameData()
   {
      for (int i = 0; i < size_; i++)
      {
         JDRCompleteObject object = get(i);

         if (object.flowframe != null)
         {
            return true;
         }

         if (object instanceof JDRGroup
          && ((JDRGroup)object).containsFlowFrameData())
         {
            return true;
         }
      }

      return false;
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      super.setCanvasGraphics(cg);

      for (int i = 0; i < size_; i++)
      {
         get(i).setCanvasGraphics(cg);
      }
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      super.applyCanvasGraphics(cg);

      for (int i = 0; i < size_; i++)
      {
         get(i).applyCanvasGraphics(cg);
      }
   }

   private volatile JDRCompleteObject[] objectList_;
   private volatile int size_ = 0;

   private static JDRGroupListener groupListener = new JDRGroupListener();

   /**
    * Initial capacity if {@link CanvasGraphics#getOptimize()} returns
    * {@link CanvasGraphics#OPTIMIZE_MEMORY} or {@link CanvasGraphics#OPTIMIZE_NONE}.
    */
   public static int init_capacity_memory=5;
   /**
    * Initial capacity if {@link CanvasGraphics#getOptimize()} returns
    * {@link CanvasGraphics#OPTIMIZE_SPEED}.
    */
   public static int init_capacity_speed=20;

}
