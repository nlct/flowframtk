// File          : CanvasUndoableEdit.java
// Description   : Undoable edits for JDRCanvas actions
// Creation Date : 2014-04-05
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

package com.dickimawbooks.flowframtk;

import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import javax.swing.undo.*;

import com.dickimawbooks.jdr.*;

public abstract class CanvasUndoableEdit extends AbstractUndoableEdit
{
   private CanvasUndoableEdit()
   {
   }

   public CanvasUndoableEdit(JDRCanvas canvas)
   {
      super();
      this.canvas = canvas;
      canvasGraphics = canvas.getCanvasGraphics();
   }

   public CanvasUndoableEdit(JDRFrame frame)
   {
      super();
      this.canvas = frame.getCanvas();
      canvasGraphics = canvas.getCanvasGraphics();
   }

   public BBox getRefreshBounds(JDRTextual object)
   {
      return getRefreshBounds((JDRCompleteObject)object);
   }

   public BBox getRefreshBounds(JDRPattern object)
   {
      return getRefreshBounds((JDRCompleteObject)object);
   }

   public BBox getRefreshBounds(JDRCompleteObject object)
   {
      return getRefreshBounds(object, object.getFlowFrame());
   }

   public BBox getRefreshBounds(JDRObject object, FlowFrame flowframe)
   {
      BBox box = object.getBpBBox();

      if (object instanceof JDRCompleteObject)
      {
         JDRCompleteObject completeObj = (JDRCompleteObject)object;
         JDRCompleteObject parent = completeObj.getParent();

         if (parent != null)
         {
            mergeBpBBox(parent, box);
         }
      }

      if (flowframe != null)
      {
         double xshift = flowframe.getEvenXShift();
         double yshift = flowframe.getEvenYShift();

         if (canvasGraphics.isEvenPage())
         {
            FlowFrame typeblock = canvas.getTypeblock();
   
            if (typeblock != null && typeblock != flowframe)
            {
               xshift += typeblock.getEvenXShift();
            }
         }

         box.translate(-canvasGraphics.storageToBp(xshift), 
                       -canvasGraphics.storageToBp(yshift));
      }

      return box;
   }

   public BBox getControlRefreshBounds(JDRPathSegment object, FlowFrame flowframe)
   {
      return getControlRefreshBounds((JDRObject)object, flowframe);
   }

   public BBox getControlRefreshBounds(JDRObject object, FlowFrame flowframe)
   {
      BBox box = object.getBpControlBBox();

      if (object instanceof JDRCompleteObject)
      {
         JDRCompleteObject completeObj = (JDRCompleteObject)object;
         JDRCompleteObject parent = completeObj.getParent();

         if (parent != null)
         {
            mergeBpBBox(parent, box);
         }
      }

      if (flowframe != null)
      {
         double xshift = flowframe.getEvenXShift();
         double yshift = flowframe.getEvenYShift();

         if (canvasGraphics.isEvenPage())
         {
            FlowFrame typeblock = canvas.getTypeblock();
   
            if (typeblock != null && typeblock != flowframe)
            {
               xshift += typeblock.getEvenXShift();
            }
         }

         box.translate(-canvasGraphics.storageToBp(xshift), 
                       -canvasGraphics.storageToBp(yshift));
      }

      return box;
   }

   public void mergeRefreshBounds(JDRTextual textual, BBox box)
   {
      mergeRefreshBounds((JDRCompleteObject)textual, box);
   }

   public void mergeRefreshBounds(JDRPattern pattern, BBox box)
   {
      mergeRefreshBounds((JDRCompleteObject)pattern, box);
   }

   public void mergeRefreshBounds(JDRCompleteObject object, BBox box)
   {
      mergeRefreshBounds(object, object.getFlowFrame(), box);
   }

   public void mergeRefreshBounds(JDRObject object, FlowFrame flowframe, BBox box)
   {
      BBox bpBox = object.getBpBBox();

      if (object instanceof JDRCompleteObject)
      {
         JDRCompleteObject completeObj = (JDRCompleteObject)object;
         JDRCompleteObject parent = completeObj.getParent();

         if (parent != null)
         {
            mergeBpBBox(parent, box);
         }
      }

      if (flowframe != null)
      {
         double xshift = flowframe.getEvenXShift();
         double yshift = flowframe.getEvenYShift();

         if (canvasGraphics.isEvenPage())
         {
            FlowFrame typeblock = canvas.getTypeblock();
      
            if (typeblock != null && typeblock != flowframe)
            {
               xshift += typeblock.getEvenXShift();
            }
         }

         bpBox.translate(-canvasGraphics.storageToBp(xshift), 
                       -canvasGraphics.storageToBp(yshift));
      }

      box.merge(bpBox);
   }

   private void mergeBpBBox(JDRCompleteObject object, BBox box)
   {
      BBox bpBox = object.getBpBBox();

      FlowFrame flowframe = object.getFlowFrame();

      if (flowframe != null)
      {
         double xshift = flowframe.getEvenXShift();
         double yshift = flowframe.getEvenYShift();

         if (canvasGraphics.isEvenPage())
         {
            FlowFrame typeblock = canvas.getTypeblock();
      
            if (typeblock != null && typeblock != flowframe)
            {
               xshift += typeblock.getEvenXShift();
            }
         }

         bpBox.translate(-canvasGraphics.storageToBp(xshift), 
                       -canvasGraphics.storageToBp(yshift));
      }

      box.merge(bpBox);

      JDRCompleteObject parent = object.getParent();

      if (parent != null)
      {
         mergeBpBBox(parent, box);
      }
   }

   public void setRefreshBounds(JDRCompleteObject object)
   {
      setRefreshBounds(object, object.getFlowFrame());
   }

   public void setRefreshBounds(JDRObject object, FlowFrame flowframe)
   {
      setRefreshBounds(getRefreshBounds(object, flowframe));
   }

   public void setRefreshBounds(JDRTextual object)
   {
      setRefreshBounds((JDRCompleteObject)object);
   }

   public void setRefreshBounds(JDRCompleteObject object,
      boolean markAsModified)
   {
      setRefreshBounds(object, object.getFlowFrame(), markAsModified);
   }

   public void setRefreshBounds(JDRObject object, FlowFrame flowframe,
      boolean markAsModified)
   {
      setRefreshBounds(getRefreshBounds(object, flowframe), markAsModified);
   }

   public void setRefreshBounds(JDRCompleteObject oldObject, 
      JDRCompleteObject newObject)
   {
      setRefreshBounds(oldObject, oldObject.getFlowFrame(),
        newObject, newObject.getFlowFrame());
   }

   public void setRefreshBounds(JDRObject oldObject, FlowFrame oldFlowFrame, 
      JDRObject newObject, FlowFrame newFlowFrame)
   {
      BBox box = getRefreshBounds(oldObject, oldFlowFrame);

      mergeRefreshBounds(newObject, newFlowFrame, box);

      setRefreshBounds(box);
   }

   public void setRefreshControlBounds(JDRCompleteObject object)
   {
      setRefreshControlBounds(object, object.getFlowFrame());
   }

   public void setRefreshControlBounds(JDRObject object, FlowFrame flowframe)
   {
      BBox box = getRefreshBounds(object, flowframe);

      BBox bpBox = object.getBpControlBBox();

      if (flowframe != null)
      {
         double xshift = flowframe.getEvenXShift();
         double yshift = flowframe.getEvenYShift();

         if (canvasGraphics.isEvenPage())
         {
            FlowFrame typeblock = canvas.getTypeblock();
     
            if (typeblock != null && typeblock != flowframe)
            {
               xshift += typeblock.getEvenXShift();
            }
         }

         bpBox.translate(-canvasGraphics.storageToBp(xshift), 
                       -canvasGraphics.storageToBp(yshift));
      }

      box.merge(bpBox);

      setRefreshBounds(box);
   }

   public void setRefreshControlBounds(JDRCompleteObject oldObject,
      JDRCompleteObject newObject)
   {
      setRefreshControlBounds(oldObject, oldObject.getFlowFrame(),
         newObject, newObject.getFlowFrame());
   }

   public void setRefreshControlBounds(JDRObject oldObject, FlowFrame oldObjectFlowFrame,
      JDRObject newObject, FlowFrame newObjectFlowFrame)
   {
      BBox box = getRefreshBounds(oldObject, oldObjectFlowFrame);
      mergeRefreshBounds(newObject, newObjectFlowFrame, box);

      box.merge(oldObject.getBpControlBBox());
      box.merge(newObject.getBpControlBBox());

      setRefreshBounds(box);
   }

   public void setRefreshBounds(BBox bpBox)
   {
      setRefreshBounds(bpBox, true);
   }

   public void setRefreshBounds(BBox bpBox, boolean markAsModified)
   {
      if (markAsModified)
      {
         canvas.markAsModified();
      }

      bounds = bpBox;

      double x = bpBox.getMinX();
      double y = bpBox.getMinY();

      double bpToCompXScale = canvasGraphics.bpToComponentX(1.0);
      double bpToCompYScale = canvasGraphics.bpToComponentY(1.0);

      Dimension2D dim = canvasGraphics.getComponentPointSize();

      region = new Rectangle(
                  (int)Math.floor(bpToCompXScale*x-0.5*dim.getWidth()),
                  (int)Math.floor(bpToCompYScale*y-0.5*dim.getHeight()),
                  (int)Math.ceil(bpToCompXScale*bpBox.getWidth()+dim.getWidth())+2,
                  (int)Math.ceil(bpToCompYScale*bpBox.getHeight()+dim.getHeight())+2
               );

      magnification = canvasGraphics.getMagnification();
      canvas.repaint(region);
   }

   public void repaintRegion()
   {
      repaintRegion(true);
   }

   public void repaintRegion(boolean markAsModified)
   {
      if (markAsModified)
      {
         canvas.markAsModified();
      }

      if (magnification == canvasGraphics.getMagnification())
      {
         canvas.repaint(region);
         return;
      }

      double x = bounds.getMinX();

      double bpToCompXScale = canvasGraphics.bpToComponentX(1.0);
      double bpToCompYScale = canvasGraphics.bpToComponentY(1.0);

      region.setBounds
      (
         (int)Math.floor(bpToCompXScale*x),
         (int)Math.floor(bpToCompYScale*bounds.getMinY()),
         (int)Math.ceil(bpToCompXScale*bounds.getWidth()),
         (int)Math.ceil(bpToCompYScale*bounds.getHeight())
      );

   }

   public String getUndoPresentationName()
   {
      return canvas.getResources().getStringWithValue("undo.undo", 
         getPresentationName());
   }

   public String getRedoPresentationName()
   {
      return canvas.getResources().getStringWithValue("undo.redo", 
         getPresentationName());
   }

   private volatile BBox bounds;
   private volatile Rectangle region;
   private volatile double magnification;
   private JDRCanvas canvas;
   private CanvasGraphics canvasGraphics;
}
