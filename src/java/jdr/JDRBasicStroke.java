// File          : JDRBasicStroke.java
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

import com.dickimawbooks.jdr.marker.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing line styles used to draw a path.
 * The line style has the following attributes:
 * <ul>
 * <li> Cap style ({@link BasicStroke#CAP_SQUARE},
 * {@link BasicStroke#CAP_BUTT} or {@link BasicStroke#CAP_ROUND})
 * <li> Join style ({@link BasicStroke#JOIN_MITER},
 * {@link BasicStroke#JOIN_BEVEL} or {@link BasicStroke#JOIN_ROUND})
 * <li> Dash pattern
 * <li> Pen width (must be positive)
 * <li> Mitre limit (must be &gt;= 1)
 * <li> Winding rule ({@link GeneralPath#WIND_EVEN_ODD} or
 * {@link GeneralPath#WIND_NON_ZERO})
 * <li> Start marker
 * <li> End marker
 * <li> Mid-point marker
 * </ul>
 */

public class JDRBasicStroke implements JDRStroke
{
   private JDRBasicStroke()
   {
   }

   /**
    * Create a copy.
    */ 
   public JDRBasicStroke(JDRBasicStroke stroke)
   {
      canvasGraphics = stroke.canvasGraphics;

      if (stroke.dashPattern != null)
      {
         dashPattern = (DashPattern)stroke.dashPattern.clone();
      }

      capStyle = stroke.capStyle;
      joinStyle = stroke.joinStyle;
      windingRule = stroke.windingRule;
      mitreLimit = stroke.mitreLimit;

      penWidth = new JDRLength(stroke.penWidth);

      startMarker = stroke.startMarker;
      midMarker = stroke.midMarker;
      endMarker = stroke.endMarker;
   }

   /**
    * Creates default stroke. The default settings are: square
    * cap, mitre join, solid line, 1bp pen width, mitre limit of
    * 10.0, even-odd winding rule and no markers.
    */
   public JDRBasicStroke(CanvasGraphics cg)
   {
      setCanvasGraphics(cg);

      capStyle    = BasicStroke.CAP_SQUARE;
      joinStyle   = BasicStroke.JOIN_MITER;
      dashPattern = new DashPattern(cg);
      penWidth    = new JDRLength(cg, 1.0, JDRUnit.bp);
      mitreLimit  = 10.0;
      windingRule = GeneralPath.WIND_EVEN_ODD;

      startMarker = new JDRMarker(penWidth, 1, false);
      midMarker = new JDRMarker(penWidth, 1, false);
      endMarker   = new JDRMarker(penWidth, 1, false);
   }

   public JDRBasicStroke(CanvasGraphics cg, double bpPenWidth,
     int capStyle, int joinStyle)
   {
      setCanvasGraphics(cg);

      this.capStyle  = capStyle;
      this.joinStyle = joinStyle;
      dashPattern = new DashPattern(cg);
      penWidth    = new JDRLength(cg, bpPenWidth, JDRUnit.bp);
      mitreLimit  = 10.0;
      windingRule = GeneralPath.WIND_EVEN_ODD;

      startMarker = new JDRMarker(penWidth, 1, false);
      midMarker = new JDRMarker(penWidth, 1, false);
      endMarker   = new JDRMarker(penWidth, 1, false);
   }

   /**
    * Creates a stroke with the given settings. The new stroke
    * has no markers and even-odd winding rule.
    * @param penwidth the pen width (can't be negative).
    * @param capstyle the cap style must be one of:
    * {@link BasicStroke#CAP_SQUARE}, {@link BasicStroke#CAP_BUTT}
    * or {@link BasicStroke#CAP_ROUND}.
    * @param joinstyle the join style must be
    * one of:{@link BasicStroke#JOIN_MITER},
    * {@link BasicStroke#JOIN_BEVEL} or {@link BasicStroke#JOIN_ROUND}.
    * @param mitrelimit the mitre limit must be greater than or
    * equal to 1
    * @param dashpattern the dash pattern
    */
   public JDRBasicStroke(CanvasGraphics cg,
                    JDRLength penwidth, int capstyle, int joinstyle,
                    double mitrelimit, DashPattern dashpattern)
   {
      setCanvasGraphics(cg);

      setCapStyle(capstyle);
      setJoinStyle(joinstyle);
      setDashPattern(dashpattern);
      setMitreLimit(mitrelimit);
      windingRule = GeneralPath.WIND_EVEN_ODD;

      if (penwidth.getValue() < 0)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.PEN_WIDTH, penwidth.toString(), cg);
      }

      this.penWidth = penwidth;

      startMarker = new JDRMarker(penWidth, 1, false);
      midMarker = new JDRMarker(penWidth, 1, false);
      endMarker   = new JDRMarker(penWidth, 1, false);
   }

   /**
    * Creates a stroke with the given settings. The new stroke
    * has no markers.
    * @param penwidth the pen width (can't be negative).
    * @param capstyle the cap style must be one of:
    * {@link BasicStroke#CAP_SQUARE}, {@link BasicStroke#CAP_BUTT}
    * or {@link BasicStroke#CAP_ROUND}
    * @param joinstyle the join style must be
    * one of:{@link BasicStroke#JOIN_MITER},
    * {@link BasicStroke#JOIN_BEVEL} or {@link BasicStroke#JOIN_ROUND}.
    * @param mitrelimit the mitre limit must be greater than or
    * equal to 1
    * @param dashpattern the dash pattern
    * @param windingrule the winding rule must be
    * one of: {@link GeneralPath#WIND_EVEN_ODD} or
    * {@link GeneralPath#WIND_NON_ZERO}
    */
   public JDRBasicStroke(CanvasGraphics cg,
                      JDRLength penwidth, int capstyle, int joinstyle,
                      double mitrelimit, DashPattern dashpattern,
                      int windingrule)
   {
      setCanvasGraphics(cg);

      setCapStyle(capstyle);
      setJoinStyle(joinstyle);
      setDashPattern(dashpattern);
      setMitreLimit(mitrelimit);
      setWindingRule(windingrule);

      if (penwidth.getValue() < 0)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.PEN_WIDTH, penwidth.toString(), cg);
      }

      this.penWidth = penwidth;

      startMarker = new JDRMarker(penWidth, 1, false);
      midMarker = new JDRMarker(penWidth, 1, false);
      endMarker   = new JDRMarker(penWidth, 1, false);
   }

   /**
    * Sets the pen width for this stroke.
    * @param width the pen width (can't be negative)
    */
   public void setPenWidth(JDRLength width)
   {
      if (width.getValue() < 0)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.PEN_WIDTH, width.toString(),
           getCanvasGraphics());
      }

      penWidth.makeEqual(width);
      startMarker.setPenWidth(width);
      midMarker.setPenWidth(width);
      endMarker.setPenWidth(width);
   }

   /**
    * Gets the pen width for this stroke.
    * @return the pen width for this stroke
    */
   public JDRLength getPenWidth()
   {
      return penWidth;
   }

   public void fade(double factor)
   {
      startMarker.fade(factor);
      midMarker.fade(factor);
      endMarker.fade(factor);
   }

   public boolean hasMarkers()
   {
      return (startMarker.getType() != JDRMarker.ARROW_NONE)
          || (midMarker.getType() != JDRMarker.ARROW_NONE)
          || (endMarker.getType() != JDRMarker.ARROW_NONE);
   }

   /**
    * Gets the colour of the start marker for this stroke.
    * @return the start marker paint or null if the marker paint
    * should be the same as the path
    * @see #getMidArrowColour()
    * @see #getEndArrowColour()
    * @see #setStartArrowColour(JDRPaint)
    * @see #setMidArrowColour(JDRPaint)
    * @see #setEndArrowColour(JDRPaint)
    */
   public JDRPaint getStartArrowColour()
   {
      return startMarker.fillPaint;
   }

   /**
    * Gets the colour of the mid markers for this stroke.
    * @return the mid marker paint or null if the marker paint
    * should be the same as the path
    * @see #getStartArrowColour()
    * @see #getEndArrowColour()
    * @see #setStartArrowColour(JDRPaint)
    * @see #setMidArrowColour(JDRPaint)
    * @see #setEndArrowColour(JDRPaint)
    */
   public JDRPaint getMidArrowColour()
   {
      return midMarker.fillPaint;
   }

   /**
    * Gets the colour of the end marker for this stroke.
    * @return the end marker paint or null if the marker paint
    * should be the same as the path
    * @see #getStartArrowColour()
    * @see #getMidArrowColour()
    * @see #setStartArrowColour(JDRPaint)
    * @see #setMidArrowColour(JDRPaint)
    * @see #setEndArrowColour(JDRPaint)
    */
   public JDRPaint getEndArrowColour()
   {
      return endMarker.fillPaint;
   }

   /**
    * Sets the paint for the start marker. If the specified paint
    * is null or an instance of {@link JDRTransparent}, the marker
    * will use the same colour as the path to which it is attached.
    * @param paint the marker paint or null if the marker should
    * use the same paint as the path
    * @see #getStartArrowColour()
    * @see #getMidArrowColour()
    * @see #getEndArrowColour()
    * @see #setMidArrowColour(JDRPaint)
    * @see #setEndArrowColour(JDRPaint)
    */
   public void setStartArrowColour(JDRPaint paint)
   {
      startMarker.setFillPaint(paint);
   }

   /**
    * Sets the paint for the mid markers. If the specified paint
    * is null or an instance of {@link JDRTransparent}, the markers
    * will use the same colour as the path to which they are attached.
    * @param paint the marker paint or null if the markers should
    * use the same paint as the path
    * @see #getStartArrowColour()
    * @see #getMidArrowColour()
    * @see #getEndArrowColour()
    * @see #setStartArrowColour(JDRPaint)
    * @see #setEndArrowColour(JDRPaint)
    */
   public void setMidArrowColour(JDRPaint paint)
   {
      midMarker.setFillPaint(paint);
   }

   /**
    * Sets the paint for the end marker. If the specified paint
    * is null or an instance of {@link JDRTransparent}, the marker
    * will use the same colour as the path to which it is attached.
    * @param paint the marker paint or null if the marker should
    * use the same paint as the path
    * @see #getStartArrowColour()
    * @see #getMidArrowColour()
    * @see #getEndArrowColour()
    * @see #setStartArrowColour(JDRPaint)
    * @see #setMidArrowColour(JDRPaint)
    */
   public void setEndArrowColour(JDRPaint paint)
   {
      endMarker.setFillPaint(paint);
   }

   /**
    * Gets the ID associated with the start marker for this stroke.
    * @return the start marker's ID
    * @see JDRMarker#getType()
    * @see #getMidArrowType()
    * @see #getEndArrowType()
    */
   public int getStartArrowType()
   {
      return startMarker.getType();
   }

   /**
    * Gets the ID associated with the mid markers for this stroke.
    * @return the ID of the mid markers
    * @see JDRMarker#getType()
    * @see #getStartArrowType()
    * @see #getEndArrowType()
    */
   public int getMidArrowType()
   {
      return midMarker.getType();
   }

   /**
    * Gets the ID associated with the end marker for this stroke.
    * @return the end marker's ID
    * @see JDRMarker#getType()
    * @see #getStartArrowType()
    * @see #getMidArrowType()
    */
   public int getEndArrowType()
   {
      return endMarker.getType();
   }

   /**
    * Gets the repeat count of the start marker for this stroke.
    * A value of 1 indicates a single marker.
    * @return the repeat count of the start marker
    * @see #getMidArrowRepeated()
    * @see #getEndArrowRepeated()
    */
   public int getStartArrowRepeated()
   {
      return startMarker.getRepeated();
   }

   /**
    * Gets the repeat count of the mid markers for this stroke.
    * A value of 1 indicates a single marker at each mid point
    * vertex.
    * @return the repeat count of the mid markers
    * @see #getStartArrowRepeated()
    * @see #getEndArrowRepeated()
    */
   public int getMidArrowRepeated()
   {
      return midMarker.getRepeated();
   }

   /**
    * Gets the repeat count of the end marker for this stroke.
    * A value of 1 indicates a single marker.
    * @return the repeat count of the end marker
    * @see #getStartArrowRepeated()
    * @see #getMidArrowRepeated()
    */
   public int getEndArrowRepeated()
   {
      return endMarker.getRepeated();
   }

   /**
    * Determines if the start marker should be reversed.
    * @return true if the start marker should be reversed
    * @see #getMidArrowReverse()
    * @see #getEndArrowReverse()
    */
   public boolean getStartArrowReverse()
   {
      return startMarker.isReversed();
   }

   /**
    * Determines if the mid markers should be reversed.
    * @return true if the mid markers should be reversed
    * @see #getStartArrowReverse()
    * @see #getEndArrowReverse()
    */
   public boolean getMidArrowReverse()
   {
      return midMarker.isReversed();
   }

   /**
    * Determines if the end marker should be reversed.
    * @return true if the end marker should be reversed
    * @see #getStartArrowReverse()
    * @see #getMidArrowReverse()
    */
   public boolean getEndArrowReverse()
   {
      return endMarker.isReversed();
   }

   /**
    * Determines if the start marker should be oriented so that
    * its x axis is aligned with the path's gradient vector.
    * @return true if the start marker should be oriented so that
    * its x axis is aligned with the path's gradient vector
    * @see #getMidArrowAutoOrient()
    * @see #getEndArrowAutoOrient()
    * @see #getStartArrowAngle()
    * @see #getMidArrowAngle()
    * @see #getEndArrowAngle()
    */
   public boolean getStartArrowAutoOrient()
   {
      return startMarker.getAutoOrient();
   }

   /**
    * Gets the start marker's angle of orientation in the event that
    * {@link #getStartArrowAutoOrient()} returns false.
    * @return the start marker's angle of orientation
    * @see #getStartArrowAutoOrient()
    * @see #getMidArrowAutoOrient()
    * @see #getEndArrowAutoOrient()
    * @see #getMidArrowAngle()
    * @see #getEndArrowAngle()
    */
   public JDRAngle getStartArrowAngle()
   {
      return startMarker.getAngle();
   }

   /**
    * Determines if the mid markers should be oriented so that
    * the marker x axis is aligned with the path's gradient vector.
    * @return true if the mid markers should be oriented so that
    * the marker x axis is aligned with the path's gradient vector
    * @see #getStartArrowAutoOrient()
    * @see #getEndArrowAutoOrient()
    * @see #getStartArrowAngle()
    * @see #getMidArrowAngle()
    * @see #getEndArrowAngle()
    */
   public boolean getMidArrowAutoOrient()
   {
      return midMarker.getAutoOrient();
   }

   /**
    * Gets the angle of orientation for the mid markers in the event 
    * that {@link #getMidArrowAutoOrient()} returns false.
    * @return the angle of orientation for the mid markers
    * @see #getStartArrowAutoOrient()
    * @see #getMidArrowAutoOrient()
    * @see #getEndArrowAutoOrient()
    * @see #getStartArrowAngle()
    * @see #getEndArrowAngle()
    */
   public JDRAngle getMidArrowAngle()
   {
      return midMarker.getAngle();
   }

   /**
    * Determines if the end marker should be oriented so that
    * its x axis is aligned with the path's gradient vector.
    * @return true if the end marker should be oriented so that
    * its x axis is aligned with the path's gradient vector
    * @see #getStartArrowAutoOrient()
    * @see #getMidArrowAutoOrient()
    * @see #getStartArrowAngle()
    * @see #getMidArrowAngle()
    * @see #getEndArrowAngle()
    */
   public boolean getEndArrowAutoOrient()
   {
      return endMarker.getAutoOrient();
   }

   /**
    * Gets the end marker's angle of orientation in the event that
    * {@link #getEndArrowAutoOrient()} returns false.
    * @return the end marker's angle of orientation
    * @see #getStartArrowAutoOrient()
    * @see #getMidArrowAutoOrient()
    * @see #getEndArrowAutoOrient()
    * @see #getStartArrowAngle()
    * @see #getMidArrowAngle()
    */
   public JDRAngle getEndArrowAngle()
   {
      return endMarker.getAngle();
   }

   /**
    * Gets the start marker's size.
    * @return the start marker's size
    * @see #getMidArrowSize()
    * @see #getEndArrowSize()
    */
   public JDRLength getStartArrowSize()
   {
      return startMarker.getSize();
   }

   /**
    * Gets the start marker's width.
    * @return the start marker's width (null if not supported)
    * @see #getMidArrowWidth()
    * @see #getEndArrowWidth()
    */
   public JDRLength getStartArrowWidth()
   {
      return startMarker.getWidth();
   }

   /**
    * Gets the size of the mid markers.
    * @return the size of the mid markers
    * @see #getStartArrowSize()
    * @see #getEndArrowSize()
    */
   public JDRLength getMidArrowSize()
   {
      return midMarker.getSize();
   }

   /**
    * Gets the mid marker's width.
    * @return the mid marker's width (null if not supported)
    * @see #getStartArrowWidth()
    * @see #getEndArrowWidth()
    */
   public JDRLength getMidArrowWidth()
   {
      return midMarker.getWidth();
   }

   /**
    * Gets the end marker's size.
    * @return the sendmarker's size
    * @see #getStartArrowSize()
    * @see #getMidArrowSize()
    */
   public JDRLength getEndArrowSize()
   {
      return endMarker.getSize();
   }

   /**
    * Gets the end marker's width.
    * @return the end marker's width (null if not supported)
    * @see #getStartArrowWidth()
    * @see #getMidArrowWidth()
    */
   public JDRLength getEndArrowWidth()
   {
      return endMarker.getWidth();
   }

   /**
    * Determines if the start marker has user offset enabled.
    * @return true if the start marker has user offset enabled
    * @see #getMidUserOffsetEnabled()
    * @see #getEndUserOffsetEnabled()
    */
   public boolean getStartUserOffsetEnabled()
   {
      return startMarker.isUserOffsetEnabled();
   }

   /**
    * Determines if the mid markers have user offset enabled.
    * @return true if the mid markers have user offset enabled
    * @see #getStartUserOffsetEnabled()
    * @see #getEndUserOffsetEnabled()
    */
   public boolean getMidUserOffsetEnabled()
   {
      return midMarker.isUserOffsetEnabled();
   }

   /**
    * Determines if the end marker has user offset enabled.
    * @return true if the end marker has user offset enabled
    * @see #getStartUserOffsetEnabled()
    * @see #getMidUserOffsetEnabled()
    */
   public boolean getEndUserOffsetEnabled()
   {
      return endMarker.isUserOffsetEnabled();
   }

   /**
    * Sets whether the start marker has user offset enabled.
    * @param enabled true if user offset is to be enabled
    * @see #getStartUserOffsetEnabled()
    * @see #setMidUserOffsetEnabled(boolean)
    * @see #setEndUserOffsetEnabled(boolean)
    */
   public void setStartUserOffsetEnabled(boolean enabled)
   {
      startMarker.enableUserOffset(enabled);
   }

   public void setStartOverlay(boolean overlaid)
   {
      startMarker.setOverlay(overlaid);
   }

   public void setMidOverlay(boolean overlaid)
   {
      midMarker.setOverlay(overlaid);
   }

   public void setEndOverlay(boolean overlaid)
   {
      endMarker.setOverlay(overlaid);
   }

   /**
    * Sets whether the mid markers have user offset enabled.
    * @param enabled true if user offset is to be enabled
    * @see #getMidUserOffsetEnabled()
    * @see #setStartUserOffsetEnabled(boolean)
    * @see #setEndUserOffsetEnabled(boolean)
    */
   public void setMidUserOffsetEnabled(boolean enabled)
   {
      midMarker.enableUserOffset(enabled);
   }

   /**
    * Sets whether the end marker has user offset enabled.
    * @param enabled true if user offset is to be enabled
    * @see #setStartUserOffsetEnabled(boolean)
    * @see #setMidUserOffsetEnabled(boolean)
    * @see #getEndUserOffsetEnabled()
    */
   public void setEndUserOffsetEnabled(boolean enabled)
   {
      endMarker.enableUserOffset(enabled);
   }

   /**
    * Gets start marker offset.
    * @return start marker offset
    * @see #getMidOffset()
    * @see #getEndOffset()
    */
   public JDRLength getStartOffset()
   {
      return startMarker.getOffset();
   }

   /**
    * Gets mid marker offset.
    * @return mid marker offset
    * @see #getStartOffset()
    * @see #getEndOffset()
    */
   public JDRLength getMidOffset()
   {
      return midMarker.getOffset();
   }

   /**
    * Gets end marker offset.
    * @return end marker offset
    * @see #getStartOffset()
    * @see #getMidOffset()
    */
   public JDRLength getEndOffset()
   {
      return endMarker.getOffset();
   }

   /**
    * Sets start marker offset.
    * @param offset start marker offset
    * @see #getStartOffset()
    * @see #setMidOffset(JDRLength)
    * @see #setEndOffset(JDRLength)
    */
   public void setStartOffset(JDRLength offset)
   {
      startMarker.setOffset(offset);
   }

   /**
    * Sets mid marker offset.
    * @param offset mid marker offset
    * @see #getMidOffset()
    * @see #setStartOffset(JDRLength)
    * @see #setEndOffset(JDRLength)
    */
   public void setMidOffset(JDRLength offset)
   {
      midMarker.setOffset(offset);
   }

   /**
    * Sets end marker offset.
    * @param offset end marker offset
    * @see #getEndOffset()
    * @see #setStartOffset(JDRLength)
    * @see #setMidOffset(JDRLength)
    */
   public void setEndOffset(JDRLength offset)
   {
      endMarker.setOffset(offset);
   }

   /**
    * Determines if the start marker has repeat offset enabled.
    * @return true if the start marker has repeat offset enabled
    * @see #getMidRepeatOffsetEnabled()
    * @see #getEndRepeatOffsetEnabled()
    */
   public boolean getStartRepeatOffsetEnabled()
   {
      return startMarker.isUserRepeatOffsetEnabled();
   }

   /**
    * Determines if the mid markers have repeat offset enabled.
    * @return true if the mid markers have repeat offset enabled
    * @see #getStartRepeatOffsetEnabled()
    * @see #getEndRepeatOffsetEnabled()
    */
   public boolean getMidRepeatOffsetEnabled()
   {
      return midMarker.isUserRepeatOffsetEnabled();
   }

   /**
    * Determines if the end marker has repeat offset enabled.
    * @return true if the end marker has repeat offset enabled
    * @see #getStartRepeatOffsetEnabled()
    * @see #getMidRepeatOffsetEnabled()
    */
   public boolean getEndRepeatOffsetEnabled()
   {
      return endMarker.isUserRepeatOffsetEnabled();
   }

   /**
    * Sets whether the start marker has repeat offset enabled.
    * @param enabled true if repeat offset is to be enabled
    * @see #getStartRepeatOffsetEnabled()
    * @see #setMidRepeatOffsetEnabled(boolean)
    * @see #setEndRepeatOffsetEnabled(boolean)
    */
   public void setStartRepeatOffsetEnabled(boolean enabled)
   {
      startMarker.enableUserRepeatOffset(enabled);
   }

   /**
    * Sets whether the mid markers have repeat offset enabled.
    * @param enabled true if repeat offset is to be enabled
    * @see #getMidRepeatOffsetEnabled()
    * @see #setStartRepeatOffsetEnabled(boolean)
    * @see #setEndRepeatOffsetEnabled(boolean)
    */
   public void setMidRepeatOffsetEnabled(boolean enabled)
   {
      midMarker.enableUserRepeatOffset(enabled);
   }

   /**
    * Sets whether the end marker has repeat offset enabled.
    * @param enabled true if repeat offset is to be enabled
    * @see #setStartRepeatOffsetEnabled(boolean)
    * @see #setMidRepeatOffsetEnabled(boolean)
    * @see #getEndRepeatOffsetEnabled()
    */
   public void setEndRepeatOffsetEnabled(boolean enabled)
   {
      endMarker.enableUserRepeatOffset(enabled);
   }

   /**
    * Gets start marker repeat offset.
    * @return start marker repeat offset
    * @see #getMidRepeatOffset()
    * @see #getEndRepeatOffset()
    */
   public JDRLength getStartRepeatOffset()
   {
      return startMarker.getRepeatOffset();
   }

   /**
    * Gets mid marker repeat offset.
    * @return mid marker repeat offset
    * @see #getStartRepeatOffset()
    * @see #getEndRepeatOffset()
    */
   public JDRLength getMidRepeatOffset()
   {
      return midMarker.getRepeatOffset();
   }

   /**
    * Gets end marker repeat offset.
    * @return end marker repeat offset
    * @see #getStartRepeatOffset()
    * @see #getMidRepeatOffset()
    */
   public JDRLength getEndRepeatOffset()
   {
      return endMarker.getOffset();
   }

   /**
    * Sets start marker repeat offset.
    * @param offset start marker repeat offset
    * @see #getStartRepeatOffset()
    * @see #setMidRepeatOffset(JDRLength)
    * @see #setEndRepeatOffset(JDRLength)
    */
   public void setStartRepeatOffset(JDRLength offset)
   {
      startMarker.setRepeatOffset(offset);
   }

   /**
    * Sets mid marker repeat offset.
    * @param offset mid marker repeat offset
    * @see #getMidRepeatOffset()
    * @see #setStartRepeatOffset(JDRLength)
    * @see #setEndRepeatOffset(JDRLength)
    */
   public void setMidRepeatOffset(JDRLength offset)
   {
      midMarker.setRepeatOffset(offset);
   }

   /**
    * Sets end marker repeat offset.
    * @param offset end marker repeat offset
    * @see #getEndRepeatOffset()
    * @see #setStartRepeatOffset(JDRLength)
    * @see #setMidRepeatOffset(JDRLength)
    */
   public void setEndRepeatOffset(JDRLength offset)
   {
      endMarker.setRepeatOffset(offset);
   }

   /**
    * Sets the repeat factor for the start marker.
    * @param repeat the repeat factor
    * @throws InvalidRepeatValueException if the repeat factor is less
    * than 1
    */
   public void setStartArrowRepeat(int repeat)
   {
      startMarker.setRepeated(repeat);
   }

   /**
    * Sets whether the start marker should be reversed.
    * @param isReversed true if the start marker should be reversed
    */
   public void setStartArrowReverse(boolean isReversed)
   {
      startMarker.setReversed(isReversed);
   }

   /**
    * Sets the size of the start marker.
    * @param size the size to set the start marker
    */
   public void setStartArrowSize(JDRLength size)
   {
      startMarker.setSize(size);
   }

   /**
    * Sets the width of the start marker (if supported).
    * @param size the size to set the start marker
    */
   public void setStartArrowWidth(JDRLength width)
   {
      startMarker.setWidth(width);
   }

   /**
    * Sets whether the start marker should be oriented so that its
    * x axis lies along the path's gradient vector.
    * @param orient true if the start marker should be oriented so
    * that its x axis lies along the path's gradient vector
    */
   public void setStartArrowAutoOrient(boolean orient)
   {
      startMarker.setOrient(orient);
   }

   /**
    * Sets the angle of orientation for the start marker in the event
    * that {@link #getStartArrowAutoOrient()} returns false.
    * @param angle the angle of orientation
    */
   public void setStartArrowAngle(JDRAngle angle)
   {
      startMarker.setAngle(angle);
   }

   /**
    * Sets the start marker.
    * @param marker the marker to use at the start of the path
    * @see #setStartArrow(int)
    * @see #setStartArrow(int,double,int,boolean)
    * @see #setMidArrow(JDRMarker)
    * @see #setEndArrow(JDRMarker)
    */
   public void setStartArrow(JDRMarker marker)
   {
      startMarker = marker;
      startMarker.setPenWidth(penWidth);
      startMarker.setCanvasGraphics(getCanvasGraphics());
   }

   /**
    * Sets the start marker.
    * @param type the marker ID
    * @see #setStartArrow(JDRMarker)
    * @see #setStartArrow(int,double,int,boolean)
    * @see #setMidArrow(int)
    * @see #setEndArrow(int)
    */
   public void setStartArrow(int type)
   {
      setStartArrow(type, getStartArrowSize(),
         getStartArrowWidth(),
         getStartArrowRepeated(),
         getStartArrowReverse());
   }

   /**
    * Sets the start marker.
    * @param type the marker ID
    * @param size the marker's size
    * @param repeat the repeat factor (can't be less than 1)
    * @param isReversed true if the marker should be reversed
    * @see #setStartArrow(int)
    * @see #setStartArrow(JDRMarker)
    * @see #setMidArrow(int,double,int,boolean)
    * @see #setEndArrow(int,double,int,boolean)
    */
   @Deprecated
   public void setStartArrow(int type, JDRLength size,
                             int repeat,
                             boolean isReversed)
   {
      setStartArrow(type, size, null, repeat, isReversed);
   }

   public void setStartArrow(int type, JDRLength size, JDRLength width,
                             int repeat,
                             boolean isReversed)
   {
      startMarker = JDRMarker.getPredefinedMarker(getCanvasGraphics(),
         type, penWidth, repeat, 
         isReversed, size, width);
   }

   /**
    * Sets the repeat factor for the mid markers.
    * @param repeat the repeat factor (can't be less than 1)
    */
   public void setMidArrowRepeat(int repeat)
   {
      midMarker.setRepeated(repeat);
   }

   /**
    * Sets whether the mid markers should be reversed.
    * @param isReversed true if the mid markers should be reversed
    */
   public void setMidArrowReverse(boolean isReversed)
   {
      midMarker.setReversed(isReversed);
   }

   /**
    * Sets whether the mid markers should be oriented so that the
    * x axis lies along the path's gradient vector.
    * @param orient true if the mid markers should be oriented so
    * that the x axis lies along the path's gradient vector
    */
   public void setMidArrowAutoOrient(boolean orient)
   {
      midMarker.setOrient(orient);
   }

   /**
    * Sets the angle of orientation for the mid markers in the event
    * that {@link #getMidArrowAutoOrient()} returns false.
    * @param angle the angle of orientation
    */
   public void setMidArrowAngle(JDRAngle angle)
   {
      midMarker.setAngle(angle);
   }

   /**
    * Sets the size of the mid markers.
    * @param size the size to set the mid marker
    */
   public void setMidArrowSize(JDRLength size)
   {
      midMarker.setSize(size);
   }

   /**
    * Sets the width of the mid markers (if supported).
    * @param size the size to set the mid marker
    */
   public void setMidArrowWidth(JDRLength width)
   {
      midMarker.setWidth(width);
   }

   /**
    * Sets the mid markers.
    * @param marker the marker to use at the mid point vertices
    * of the path
    * @see #setMidArrow(int)
    * @see #setMidArrow(int,double,int,boolean)
    * @see #setStartArrow(JDRMarker)
    * @see #setEndArrow(JDRMarker)
    */
   public void setMidArrow(JDRMarker marker)
   {
      midMarker = marker;
      midMarker.setPenWidth(penWidth);
      midMarker.setCanvasGraphics(getCanvasGraphics());
   }

   /**
    * Sets the mid markers.
    * @param type the marker ID
    * @see #setMidArrow(JDRMarker)
    * @see #setMidArrow(int,double,int,boolean)
    * @see #setStartArrow(int)
    * @see #setEndArrow(int)
    */
   public void setMidArrow(int type)
   {
      setMidArrow(type, getMidArrowSize(), getMidArrowWidth(),
         getMidArrowRepeated(),
         getMidArrowReverse());
   }

   /**
    * Sets the mid markers.
    * @param type the marker ID
    * @param size the size of the markers
    * @param repeat the repeat factor (can't be less than 1)
    * @param isReversed true if the markers should be reversed
    * @see #setMidArrow(int)
    * @see #setMidArrow(JDRMarker)
    * @see #setStartArrow(int,double,int,boolean)
    * @see #setEndArrow(int,double,int,boolean)
    */
   @Deprecated
   public void setMidArrow(int type, JDRLength size,
                             int repeat,
                             boolean isReversed)
   {
      setMidArrow(type, size, null, repeat, isReversed);
   }

   public void setMidArrow(int type, JDRLength size, JDRLength width,
                             int repeat,
                             boolean isReversed)
   {
      midMarker = JDRMarker.getPredefinedMarker(getCanvasGraphics(),
         type, penWidth, repeat, 
         isReversed, size, width);
   }

   /**
    * Sets the repeat factor for the end marker.
    * @param repeat the repeat factor (can't be less than 1)
    */
   public void setEndArrowRepeat(int repeat)
   {
      endMarker.setRepeated(repeat);
   }

   /**
    * Sets whether the end marker should be reversed.
    * @param isReversed true if the end marker should be reversed
    */
   public void setEndArrowReverse(boolean isReversed)
   {
      endMarker.setReversed(isReversed);
   }

   /**
    * Sets whether the end marker should be oriented so that its
    * x axis lies along the path's gradient vector.
    * @param orient true if the end marker should be oriented so
    * that its x axis lies along the path's gradient vector
    */
   public void setEndArrowAutoOrient(boolean orient)
   {
      endMarker.setOrient(orient);
   }

   /**
    * Sets the angle of orientation for the end marker in the event
    * that {@link #getEndArrowAutoOrient()} returns false.
    * @param angle the angle of orientation
    */
   public void setEndArrowAngle(JDRAngle angle)
   {
      endMarker.setAngle(angle);
   }

   /**
    * Sets the size of the end marker.
    * @param size the size to set the end marker
    */
   public void setEndArrowSize(JDRLength size)
   {
      endMarker.setSize(size);
   }

   /**
    * Sets the width of the end marker (if supported).
    * @param width the size to set the end marker
    */
   public void setEndArrowWidth(JDRLength width)
   {
      endMarker.setWidth(width);
   }

   /**
    * Sets the end marker.
    * @param marker the marker to use at the end of the path
    * @see #setEndArrow(int)
    * @see #setEndArrow(int,double,int,boolean)
    * @see #setStartArrow(JDRMarker)
    * @see #setMidArrow(JDRMarker)
    */
   public void setEndArrow(JDRMarker marker)
   {
      endMarker = marker;
      endMarker.setPenWidth(penWidth);
      endMarker.setCanvasGraphics(getCanvasGraphics());
   }

   /**
    * Sets the end marker.
    * @param type the marker ID
    * @see #setEndArrow(JDRMarker)
    * @see #setEndArrow(int,double,int,boolean)
    * @see #setStartArrow(int)
    * @see #setMidArrow(int)
    */
   public void setEndArrow(int type)
   {
      setEndArrow(type, getEndArrowSize(), getEndArrowWidth(), getEndArrowRepeated(),
         getEndArrowReverse());
   }

   /**
    * Sets the end marker.
    * @param type the marker ID
    * @param size the marker's size
    * @param repeat the repeat factor (can't be less than 1)
    * @param isReversed true if the marker should be reversed
    * @see #setEndArrow(int)
    * @see #setEndArrow(JDRMarker)
    * @see #setStartArrow(int,double,int,boolean)
    * @see #setMidArrow(int,double,int,boolean)
    */
   @Deprecated
   public void setEndArrow(int type, JDRLength size,
                           int repeat,
                           boolean isReversed)
   {
      setEndArrow(type, size, null, repeat, isReversed);
   }

   public void setEndArrow(int type, JDRLength size, JDRLength width,
                           int repeat,
                           boolean isReversed)
   {
      endMarker = JDRMarker.getPredefinedMarker(getCanvasGraphics(), 
         type, penWidth, repeat, isReversed,
         size, width);
   }

   /**
    * Creates the stroked shape of the given path. Note that
    * this does not include the markers as they may be a different
    * colour to the path, so they need to be drawn separately.
    * @param p the path to stroke
    * @return the outline of the stroked path
    */
   public Shape createStrokedShape(Shape p, JDRUnit shapeUnit)
   {

      // this was changed in version 0.1.8b
      // the markers are no longer included in 
      // createStrokedShape to allow for different
      // coloured markers. So this now just
      // uses the BasicStroke createStrokedShape function
      BasicStroke stroke;

      if (dashPattern == null || dashPattern.getStoragePattern() == null)
      {
         stroke = new BasicStroke(
          (float)penWidth.getValue(shapeUnit), capStyle, joinStyle, (float)mitreLimit);
      }
      else
      {
         float[] compPattern = dashPattern.getStoragePattern();

         stroke = new BasicStroke(
               (float)penWidth.getValue(shapeUnit),
               capStyle, joinStyle, (float)mitreLimit,
               compPattern,
               (float)dashPattern.getStorageOffset());
      }

      Shape shape = stroke.createStrokedShape(p);

      return shape;
   }

   /**
    * Prints the PGF commands to set this line style. Note that
    * this does not include the markers as the pgf package does not
    * support mid markers or markers that have a fixed orientation.
    */
   public void savePgf(TeX tex)
     throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      tex.println("\\pgfsetlinewidth{" +PGF.length(penWidth)+"}");

      switch (capStyle)
      {
         case BasicStroke.CAP_SQUARE :
            tex.println("\\pgfsetrectcap");
         break;
         case BasicStroke.CAP_BUTT :
            tex.println("\\pgfsetbuttcap");
         break;
         case BasicStroke.CAP_ROUND :
            tex.println("\\pgfsetroundcap");
         break;
      }

      switch (joinStyle)
      {
         case BasicStroke.JOIN_MITER :
            tex.println("\\pgfsetmiterjoin");
            tex.println("\\pgfsetmiterlimit{"+mitreLimit+"}");
         break;
         case BasicStroke.JOIN_BEVEL :
            tex.println("\\pgfsetbeveljoin");
         break;
         case BasicStroke.JOIN_ROUND :
            tex.println("\\pgfsetroundjoin");
         break;
      }

      dashPattern.savePgf(tex);
   }

   /**
    * Creates a copy of this line style.
    * @return a copy of this line style
    */
   public Object clone()
   {
      JDRBasicStroke s = new JDRBasicStroke(getCanvasGraphics());

      s.penWidth.makeEqual(penWidth);
      s.capStyle    = capStyle;
      s.joinStyle   = joinStyle;
      s.mitreLimit  = mitreLimit;

      if (dashPattern == null)
      {
         s.dashPattern = null;
      }
      else
      {
         s.dashPattern = (DashPattern)dashPattern.clone();
      }

      s.windingRule = windingRule;

      if (startMarker == null)
      {
         s.startMarker = null;
      }
      else
      {
         s.startMarker = (JDRMarker)startMarker.clone();
      }

      if (midMarker == null)
      {
         s.midMarker = null;
      }
      else
      {
         s.midMarker = (JDRMarker)midMarker.clone();
      }

      if (endMarker == null)
      {
         s.endMarker = null;
      }
      else
      {
         s.endMarker = (JDRMarker)endMarker.clone();
      }

      return s;
   }

   /**
    * Determines if this object is the same as another object.
    * @param o the object with which to compare this object
    * @return true if this object is the equivalent to the other object
    */
   public boolean equals(Object o)
   {
      if (this == o) return true;

      if (o == null) return false;

      if (!(o instanceof JDRBasicStroke)) return false;

      JDRBasicStroke s = (JDRBasicStroke)o;

      if (!penWidth.equals(s.penWidth)) return false;
      if (capStyle != s.capStyle) return false;
      if (joinStyle != s.joinStyle) return false;
      if (mitreLimit != s.mitreLimit) return false;
      if (windingRule != s.windingRule) return false;
      if (!dashPattern.equals(s.dashPattern)) return false;
      if (!startMarker.equals(s.startMarker)) return false;
      if (!midMarker.equals(s.midMarker)) return false;
      if (!endMarker.equals(s.endMarker)) return false;

      return true;
   }

   /**
    * Saves this line style in the given JDR/AJR format.
    * @throws IOException if I/O error occurs
    * @see #read(JDRAJR)
    */
   public void save(JDRAJR jdr)
      throws IOException
   {
      float version = jdr.getVersion();

      if (version < 1.8f)
      {
         jdr.writeFloat((float)penWidth.getValue(JDRUnit.bp));
      }
      else
      {
         jdr.writeLength(penWidth);
      }

      dashPattern.save(jdr);
      jdr.writeByte((byte)capStyle);
      jdr.writeByte((byte)joinStyle);

      if (joinStyle==BasicStroke.JOIN_MITER)
      {
         jdr.writeFloat((float)mitreLimit);
      }

      jdr.writeByte((byte)windingRule);
      startMarker.save(jdr);

      if (version >= 1.1f)
      {
         midMarker.save(jdr);
      }

      endMarker.save(jdr);
   }

   /**
    * Reads line style data from the input stream in the given 
    * JDR/AJR file format.
    * @return the line style data read from the input stream
    * @throws InvalidFormatException if there is something wrong
    * with the format
    * @see #save(JDRAJR)
    */
   public static JDRBasicStroke read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      JDRLength pen_width;

      if (version < 1.8f)
      {
         pen_width = new JDRLength(jdr.getCanvasGraphics(),
           jdr.readFloatGe(InvalidFormatException.PEN_WIDTH, 0),
           JDRUnit.bp);
      }
      else
      {
         pen_width = jdr.readLength(InvalidFormatException.PEN_WIDTH);
      }

      DashPattern pattern = DashPattern.read(jdr);

      int cap_style = (int)jdr.readByte(
        InvalidFormatException.CAP_STYLE, 0, 2, true, true);

      int join_style = (int)jdr.readByte(
         InvalidFormatException.JOIN_STYLE, 0, 2, true, true);

      double mitre_limit = 10.0F;

      if (join_style==BasicStroke.JOIN_MITER)
      {
         mitre_limit = jdr.readFloatGe(
            InvalidFormatException.MITRE_LIMIT, 1f);
      }

      int winding_rule = (int)jdr.readByte(
         InvalidFormatException.WINDING_STYLE, 0, 1, true, true);


      CanvasGraphics cg = jdr.getCanvasGraphics();

      JDRBasicStroke stroke = new JDRBasicStroke(cg,
                                       pen_width, cap_style,
                                       join_style, mitre_limit,
                                       pattern, winding_rule);

      stroke.startMarker = JDRMarker.read(jdr);
      stroke.startMarker.setPenWidth(pen_width);
      stroke.startMarker.setCanvasGraphics(cg);

      if (jdr.getVersion() >= 1.1f)
      {
         stroke.midMarker = JDRMarker.read(jdr);
         stroke.midMarker.setPenWidth(pen_width);
         stroke.midMarker.setCanvasGraphics(cg);
      }

      stroke.endMarker = JDRMarker.read(jdr);
      stroke.endMarker.setPenWidth(pen_width);
      stroke.endMarker.setCanvasGraphics(cg);

      return stroke;
   }

   /**
    * Gets the SVG syntax for this line style.
    * @param p the line paint of the path to which this
    * line style should be applied
    * @return string containing SVG syntax for this line style
    * @see #svgStartMarker(JDRPaint)
    * @see #svgMidMarker(JDRPaint)
    * @see #svgEndMarker(JDRPaint)
    * @see #svgDefs(SVG,JDRGroup)
    */
   public String svg(JDRPaint p)
   {
      CanvasGraphics cg = getCanvasGraphics();

      String rule = (windingRule==GeneralPath.WIND_EVEN_ODD ?
                     "evenodd" : "nonzero");
      String cap = "square";

      switch (capStyle)
      {
          case BasicStroke.CAP_BUTT :
             cap = "butt";
             break;
          case BasicStroke.CAP_ROUND :
             cap = "round";
      }

      String join = "miter";

      switch (joinStyle)
      {
         case BasicStroke.JOIN_BEVEL :
            join = "bevel";
            break;
         case BasicStroke.JOIN_ROUND :
            join = "round";
      }

      String eol = System.getProperty("line.separator", "\n");

      return "fill-rule=\"" +rule+"\"" +eol
         +"      stroke-width=\""+SVG.length(penWidth)+"\"" +eol
         +"      stroke-linecap=\""+cap+"\""+eol
         +"      stroke-linejoin=\""+join+"\""+eol
         +"      stroke-miterlimit=\""+mitreLimit+"\""+eol
         +"      "+dashPattern.svg() + eol
         +"      "+svgStartMarker(p) +eol
         +"      "+svgMidMarker(p) +eol
         +"      "+svgEndMarker(p);
   }

   /**
    * Gets the SVG syntax for the start marker.
    * @param p the line paint of the path to which this
    * line style should be applied
    * @return string containing SVG syntax for the start marker
    * @see #svg(JDRPaint)
    * @see #svgMidMarker(JDRPaint)
    * @see #svgEndMarker(JDRPaint)
    * @see #svgDefs(SVG,JDRGroup)
    */
   public String svgStartMarker(JDRPaint p)
   {
      return startMarker.svgStartMarker(p);
   }

   /**
    * Gets the SVG syntax for the mid markers.
    * @param p the line paint of the path to which this
    * line style should be applied
    * @return string containing SVG syntax for the mid markers
    * @see #svg(JDRPaint)
    * @see #svgStartMarker(JDRPaint)
    * @see #svgEndMarker(JDRPaint)
    * @see #svgDefs(SVG,JDRGroup)
    */
   public String svgMidMarker(JDRPaint p)
   {
      return midMarker.svgMidMarker(p);
   }

   /**
    * Gets the SVG syntax for the end marker.
    * @param p the line paint of the path to which this
    * line style should be applied
    * @return string containing SVG syntax for the end marker
    * @see #svg(JDRPaint)
    * @see #svgStartMarker(JDRPaint)
    * @see #svgMidMarker(JDRPaint)
    * @see #svgDefs(SVG,JDRGroup)
    */
   public String svgEndMarker(JDRPaint p)
   {
      return endMarker.svgEndMarker(p);
   }

   /**
    * Writes the SVG definitions for all the markers used in the
    * given group.
    * @param out the output stream
    * @param group the group containing all the objects that will
    * be saved in SVG file
    * @see #svg(JDRPaint)
    * @see #svgStartMarker(JDRPaint)
    * @see #svgMidMarker(JDRPaint)
    * @see #svgEndMarker(JDRPaint)
    * @see JDRMarker#svgDefs(SVG,JDRGroup)
    */
   @Deprecated
   public static void svgDefs(SVG svg, JDRGroup group)
      throws IOException
   {
      JDRMarker.svgDefs(svg, group);
   }

   @Override
   public void writeSVGdefs(SVG svg, JDRShape shape) throws IOException
   {
      JDRPaint linePaint = shape.getLinePaint();
      JDRPaint fillPaint = shape.getFillPaint();

      if (linePaint != null)
      {
         linePaint.writeSVGdefs(svg);
      }

      if (fillPaint != null)
      {
         fillPaint.writeSVGdefs(svg);
      }

      JDRMarker.writeSVGdefs(svg, linePaint, this);
   }

   /**
    * Draws the start marker.
    * @param segment the first segment in the path that has this
    * line style
    * @see #drawMidArrowShape(JDRPathSegment)
    * @see #drawEndArrowShape(JDRPathSegment)
    */
   public void drawStartArrowShape(JDRPathSegment segment)
   {
      getStartArrow().draw(segment, true);
   }

   /**
    * Draws the mid marker that should be placed at the end of the
    * given segment.
    * @param segment the segment on whose end point the mid marker
    * should be placed (this should not include the last segment in
    * the path)
    * @see #drawStartArrowShape(JDRPathSegment)
    * @see #drawEndArrowShape(JDRPathSegment)
    */
   public void drawMidArrowShape(JDRPathSegment segment)
   {
      getMidArrow().draw(segment, false);
   }

   /**
    * Draws the end marker.
    * @param segment the last segment in the path that has this
    * line style
    * @see #drawStartArrowShape(JDRPathSegment)
    * @see #drawMidArrowShape(JDRPathSegment)
    */
   public void drawEndArrowShape(JDRPathSegment segment)
   {
      getEndArrow().draw(segment, false);
   }

   /**
    * Writes the shape of the start marker in EPS format.
    * @param pathPaint the line paint applied to the path that has
    * this line style
    * @param pathBBox the bounding box of the path that has this
    * line style
    * @param segment the first segment in the path that has this
    * line style
    * @param out the output stream
    * @throws IOException if I/O error occurs
    * @see #saveEPSMidArrowShape(JDRPaint,BBox,JDRPathSegment,PrintWriter)
    * @see #saveEPSEndArrowShape(JDRPaint,BBox,JDRPathSegment,PrintWriter)
    */
   public void saveEPSStartArrowShape(JDRPaint pathPaint,
      BBox pathBBox, JDRPathSegment segment, PrintWriter out)
      throws IOException
   {
      getStartArrow().saveEPS(pathPaint, pathBBox,
         segment, true, out);
   }

   /**
    * Writes the shape of the mid marker in EPS format.
    * @param pathPaint the line paint applied to the path that has
    * this line style
    * @param pathBBox the bounding box of the path that has this
    * line style
    * @param segment the segment on whose end point the mid marker
    * should be placed (this should not include the last segment in
    * the path)
    * @param out the output stream
    * @throws IOException if I/O error occurs
    * @see #saveEPSStartArrowShape(JDRPaint,BBox,JDRPathSegment,PrintWriter)
    * @see #saveEPSEndArrowShape(JDRPaint,BBox,JDRPathSegment,PrintWriter)
    */
   public void saveEPSMidArrowShape(JDRPaint pathPaint,
      BBox pathBBox, JDRPathSegment segment, PrintWriter out)
      throws IOException
   {
      getMidArrow().saveEPS(pathPaint, pathBBox,
         segment, false, out);
   }

   /**
    * Writes the shape of the end marker in EPS format.
    * @param pathPaint the line paint applied to the path that has
    * this line style
    * @param pathBBox the bounding box of the path that has this
    * line style
    * @param segment the last segment in the path that has this
    * line style
    * @param out the output stream
    * @throws IOException if I/O error occurs
    * @see #saveEPSStartArrowShape(JDRPaint,BBox,JDRPathSegment,PrintWriter)
    * @see #saveEPSMidArrowShape(JDRPaint,BBox,JDRPathSegment,PrintWriter)
    */
   public void saveEPSEndArrowShape(JDRPaint pathPaint,
      BBox pathBBox, JDRPathSegment segment, PrintWriter out)
      throws IOException
   {
      getEndArrow().saveEPS(pathPaint, pathBBox,
         segment, false, out);
   }

   /**
    * Draws the given path using this line style.
    * @param path the path to draw
    * @param cg the graphics information
    */
   public void drawStoragePath(JDRShape path)
   {
      drawStoragePath(path, path.getGeneralPath());
   }

   /**
    * Draws the given path using this line style. This method is
    * like {@link #drawStoragePath(JDRShape)} but the GeneralPath
    * is passed as a parameter.
    * @param shape the path to draw
    * @param storageGeneralPath the shape described as a GeneralPath in
    * storage units.
    * @param cg the graphics information
    */
   public void drawStoragePath(JDRShape shape, Shape storageGeneralPath)
   {
      CanvasGraphics cg = getCanvasGraphics();

      cg.fill(createStrokedShape(storageGeneralPath, cg.getStorageUnit()));

      int startType = getStartArrowType();
      int midType = getMidArrowType();
      int endType = getEndArrowType();

      if (startType == JDRMarker.ARROW_NONE
        && midType == JDRMarker.ARROW_NONE
        && endType == JDRMarker.ARROW_NONE)
      {
         return;
      }

      JDRPathIterator iterator = shape.getIterator();

      while (iterator.hasNext())
      {
         JDRPathSegment segment = iterator.next();

         JDRMarker marker = segment.getStartMarker();

         if (marker != null)
         {
            if (marker.getCanvasGraphics() == null)
            {
               marker.setCanvasGraphics(cg);
            }

            marker.draw(segment, true);
         }

         marker = segment.getEndMarker();

         if (marker != null)
         {
            if (marker.getCanvasGraphics() == null)
            {
               marker.setCanvasGraphics(cg);
            }

            marker.draw(segment, false);
         }
      }

   }

   public void drawMarkers(JDRShape shape)
   {
      int startType = getStartArrowType();
      int midType = getMidArrowType();
      int endType = getEndArrowType();

      if (startType == JDRMarker.ARROW_NONE
        && midType == JDRMarker.ARROW_NONE
        && endType == JDRMarker.ARROW_NONE)
      {
         return;
      }

      JDRPathIterator iterator = shape.getIterator();

      while (iterator.hasNext())
      {
         JDRPathSegment segment = iterator.next();

         JDRMarker marker = segment.getStartMarker();

         if (marker != null && marker.getFillPaint() != null)
         {
            marker.draw(segment, true);
         }

         marker = segment.getEndMarker();

         if (marker != null && marker.getFillPaint() != null)
         {
            marker.draw(segment, false);
         }
      }

   }

   public void printPath(Graphics2D g2, JDRShape shape, Shape bpGeneralPath)
   {
      g2.fill(createStrokedShape(bpGeneralPath, JDRUnit.bp));

      int startType = getStartArrowType();
      int midType = getMidArrowType();
      int endType = getEndArrowType();

      if (startType == JDRMarker.ARROW_NONE
        && midType == JDRMarker.ARROW_NONE
        && endType == JDRMarker.ARROW_NONE)
      {
         return;
      }

      JDRPathIterator iterator = shape.getIterator();

      while (iterator.hasNext())
      {
         JDRPathSegment segment = iterator.next();

         JDRMarker marker = segment.getStartMarker();

         if (marker != null)
         {
            marker.print(g2, segment, true);
         }

         marker = segment.getEndMarker();

         if (marker != null)
         {
            marker.print(g2, segment, false);
         }
      }

   }

   public void printMarkers(Graphics2D g2, JDRShape shape)
   {
      int startType = getStartArrowType();
      int midType = getMidArrowType();
      int endType = getEndArrowType();

      if (startType == JDRMarker.ARROW_NONE
        && midType == JDRMarker.ARROW_NONE
        && endType == JDRMarker.ARROW_NONE)
      {
         return;
      }

      JDRPathIterator iterator = shape.getIterator();

      while (iterator.hasNext())
      {
         JDRPathSegment segment = iterator.next();

         JDRMarker marker = segment.getStartMarker();

         if (marker != null && marker.getFillPaint() != null)
         {
            marker.print(g2, segment, true);
         }

         marker = segment.getEndMarker();

         if (marker != null && marker.getFillPaint() != null)
         {
            marker.print(g2, segment, false);
         }
      }

   }

   /**
    * Writes the given path using this line style in EPS format.
    * (Includes markers).
    * @param path the path to save
    * @param out the output stream
    * @throws IOException if I/O error occurs
    */
   public void saveEPS(JDRShape path, PrintWriter out)
      throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();
      JDRPaint paint = path.getLinePaint();
      BBox pathBBox = path.getBpBBox();

      out.println("gsave");

      if ((paint instanceof JDRGradient)
        || (paint instanceof JDRRadial))
      {
         Shape shape = getBpStrokedArea(path);

         EPS.fillPath(shape, paint, out);
      }
      else
      {
         out.println(""+penWidth.getValue(JDRUnit.bp)+" setlinewidth");

         switch (capStyle)
         {
            case BasicStroke.CAP_BUTT :
               out.println("0 setlinecap");
            break;
            case BasicStroke.CAP_ROUND :
               out.println("1 setlinecap");
            break;
            case BasicStroke.CAP_SQUARE :
               out.println("2 setlinecap");
            break;
         }
         switch (joinStyle)
         {
            case BasicStroke.JOIN_MITER :
               out.println("0 setlinejoin");
            break;
            case BasicStroke.JOIN_ROUND :
               out.println("1 setlinejoin");
            break;
            case BasicStroke.JOIN_BEVEL :
               out.println("2 setlinejoin");
            break;
         }
         out.println(""+mitreLimit+" setmiterlimit");

         dashPattern.saveEPS(out);
         EPS.drawPath(path.getBpGeneralPath(), paint, out);
      }

      out.println("grestore");

      epsMarkers(paint, path, pathBBox, out);
   }

   public void epsMarkers(JDRShape path, BBox bpPathBBox, PrintWriter out)
    throws IOException
   {
      epsMarkers(path.getLinePaint(), path, bpPathBBox, out);
   }

   public void epsMarkers(JDRPaint paint, JDRShape path,
       BBox bpPathBBox, PrintWriter out)
    throws IOException
   {
      if (getStartArrowType() == JDRMarker.ARROW_NONE
        && getMidArrowType() == JDRMarker.ARROW_NONE
        && getEndArrowType() == JDRMarker.ARROW_NONE)
      {
         return;
      }

      JDRPathIterator pi = path.getIterator();

      while (pi.hasNext())
      {
         JDRPathSegment segment = pi.next();

         JDRMarker marker = segment.getStartMarker();

         if (marker != null)
         {
            saveEPSStartArrowShape(paint, bpPathBBox, segment, out);
         }

         marker = segment.getEndMarker();

         if (marker != null)
         {
            saveEPSEndArrowShape(paint, bpPathBBox, segment, out);
         }
      }
   }

   /**
    * Gets the stroked outline of the given path including the markers.
    * This method is not used for drawing the path, as the markers
    * may use a different colour to the path's line colour, but
    * is used to determine the bounds of the stroked path.
    * @param path the path to which this line style applies
    * @return the path outlining the stroked shape including the
    * marker outlines
    * @see #getStorageStrokedArea(JDRShape)
    */
   public Shape getStorageStrokedPath(JDRShape path)
   {
      GeneralPath shape = new GeneralPath(
         createStrokedShape(path.getGeneralPath(),
           getCanvasGraphics().getStorageUnit()));

      int startType = getStartArrowType();
      int midType = getMidArrowType();
      int endType = getEndArrowType();

      if (startType == JDRMarker.ARROW_NONE
        && midType == JDRMarker.ARROW_NONE
        && endType == JDRMarker.ARROW_NONE)
      {
         return shape;
      }

      JDRPathIterator iterator = path.getIterator();

      while (iterator.hasNext())
      {
         JDRPathSegment segment = iterator.next();

         JDRMarker marker = segment.getStartMarker();

         if (marker != null)
         {
            Path2D path2d = marker.getCompleteShape(segment, true);
            shape.append(path2d, false);
         }

         marker = segment.getEndMarker();

         if (marker != null)
         {
            Path2D path2d = marker.getCompleteShape(segment, false);
            shape.append(path2d, false);
         }
      }

      return shape;
   }

   /**
    * Gets the stroked outline of the given path including the markers.
    * This method is not used for drawing the path, as the markers
    * may use a different colour to the path's line colour, but
    * is used to determine the bounds of the stroked path.
    * @param path the path to which this line style applies
    * @return the path outlining the stroked shape including the
    * marker outlines
    * @see #getStorageStrokedPath(JDRShape)
    */
   public Shape getComponentStrokedPath(JDRShape path)
   {
      CanvasGraphics cg = getCanvasGraphics();
      Shape shape = getStorageStrokedPath(path);

      double factorX = cg.bpToComponentX(1.0);
      double factorY = cg.bpToComponentY(1.0);

      AffineTransform af = new AffineTransform();
      af.scale(factorX, factorY);

      return af.createTransformedShape(shape);
   }

   /**
    * Gets the stroked outline of the given path including the markers.
    * This method is not used for drawing the path, as the markers
    * may use a different colour to the path's line colour, but
    * is used to determine the bounds of the stroked path.
    * @param path the path to which this line style applies
    * @return the path outlining the stroked shape including the
    * marker outlines
    * @see #getStorageStrokedPath(JDRShape)
    */
   public Shape getBpStrokedPath(JDRShape path)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Shape shape = getStorageStrokedPath(path);

      JDRUnit unit = cg.getStorageUnit();

      if (unit.getID() == JDRUnit.BP)
      {
         return shape;
      }

      double factor = unit.toBp(1.0);

      AffineTransform af = new AffineTransform();
      af.scale(factor, factor);

      return af.createTransformedShape(shape);
   }

   /**
    * Gets the stroked outline of the given path including the markers.
    * This method is not used for drawing the path, as the markers
    * may use a different colour to the path's line colour, but
    * is used for {@link JDRPath#outlineToPath()}, 
    * {@link JDRPath#parshape(Graphics2D,double,boolean)} and
    * {@link JDRPath#shapepar(Graphics2D,double,boolean)}.
    * @param path the path to which this line style applies
    * @return the path outlining the stroked shape including the
    * marker outlines
    * @see #getStorageStrokedPath(JDRShape)
    */
   public Area getStorageStrokedArea(JDRShape path)
   {
      Area area = new Area(createStrokedShape(path.getGeneralPath(),
         getCanvasGraphics().getStorageUnit()));

      int startType = getStartArrowType();
      int midType = getMidArrowType();
      int endType = getEndArrowType();

      if (startType == JDRMarker.ARROW_NONE
        && midType == JDRMarker.ARROW_NONE
        && endType == JDRMarker.ARROW_NONE)
      {
         return area;
      }

      JDRPathIterator iterator = path.getIterator();

      while (iterator.hasNext())
      {
         JDRPathSegment segment = iterator.next();

         JDRMarker marker = segment.getStartMarker();

         if (marker != null)
         {
            Path2D path2d = marker.getCompleteShape(segment, true);
            area.add(new Area(path2d));
         }

         marker = segment.getEndMarker();

         if (marker != null)
         {
            Path2D path2d = marker.getCompleteShape(segment, false);
            area.add(new Area(path2d));
         }
      }

      return area;
   }

   public Area getComponentStrokedArea(JDRShape path)
   {
      CanvasGraphics cg = getCanvasGraphics();
      double factorX = cg.bpToComponentX(1.0);
      double factorY = cg.bpToComponentY(1.0);

      AffineTransform af = new AffineTransform();

      af.scale(factorX, factorY);
      
      return new Area(af.createTransformedShape(getBpStrokedArea(path)));
   }

   public Area getBpStrokedArea(JDRShape path)
   {
      CanvasGraphics cg = getCanvasGraphics();
      JDRUnit unit = cg.getStorageUnit();

      if (unit.getID() == JDRUnit.BP)
      {
         return getStorageStrokedArea(path);
      }

      double factor = unit.toBp(1.0);

      AffineTransform af = new AffineTransform();

      af.scale(factor, factor);

      Area area = new Area(createStrokedShape(path.getBpGeneralPath(),
        JDRUnit.bp));

      int startType = getStartArrowType();
      int midType = getMidArrowType();
      int endType = getEndArrowType();

      if (startType == JDRMarker.ARROW_NONE
        && midType == JDRMarker.ARROW_NONE
        && endType == JDRMarker.ARROW_NONE)
      {
         return area;
      }

      JDRPathIterator iterator = path.getIterator();

      while (iterator.hasNext())
      {
         JDRPathSegment segment = iterator.next();

         JDRMarker marker = segment.getStartMarker();

         if (marker != null)
         {
            Path2D path2d = marker.getCompleteShape(segment, true);
            area.add(new Area(af.createTransformedShape(path2d)));
         }

         marker = segment.getEndMarker();

         if (marker != null)
         {
            Path2D path2d = marker.getCompleteShape(segment, false);
            area.add(new Area(af.createTransformedShape(path2d)));
         }
      }

      return area;
   }

   /**
    * Gets the outline of the start marker's primary shape. (In
    * storage units.)
    * @param segment the first segment in the path to which this
    * line style applies
    * @see #getMidArrowShape(JDRPathSegment)
    * @see #getEndArrowShape(JDRPathSegment)
    * @see JDRMarker#getShape(JDRPathSegment,boolean)
    */
   public Shape getStartArrowShape(JDRPathSegment segment)
   {
      return getStartArrow().getCompleteShape(segment, true);
   }

   /**
    * Gets the outline of the mid marker's primary shape. (In
    * storage units.)
    * @param segment the segment on whose end point the mid marker
    * should be placed (this should not include the last segment in
    * the path)
    * @see #getStartArrowShape(JDRPathSegment)
    * @see #getEndArrowShape(JDRPathSegment)
    * @see JDRMarker#getShape(JDRPathSegment,boolean)
    */
   public Shape getMidArrowShape(JDRPathSegment segment)
   {
      return getMidArrow().getCompleteShape(segment, false);
   }

   /**
    * Gets the outline of the end marker's primary shape. (In
    * storage units.)
    * @param segment the last segment in the path to which this
    * line style applies
    * @see #getStartArrowShape(JDRPathSegment)
    * @see #getMidArrowShape(JDRPathSegment)
    * @see JDRMarker#getShape(JDRPathSegment,boolean)
    */
   public Shape getEndArrowShape(JDRPathSegment segment)
   {
      return getEndArrow().getCompleteShape(segment, false);
   }

   /**
    * Gets the start marker.
    * @return the start marker
    * @see #getMidArrow()
    * @see #getEndArrow()
    */
   public JDRMarker getStartArrow()
   {
      return startMarker;
   }

   /**
    * Gets the mid marker.
    * @return the mid marker
    * @see #getStartArrow()
    * @see #getEndArrow()
    */
   public JDRMarker getMidArrow()
   {
      return midMarker;
   }

   /**
    * Gets the end marker.
    * @return the end marker
    * @see #getStartArrow()
    * @see #getMidArrow()
    */
   public JDRMarker getEndArrow()
   {
      return endMarker;
   }

   public void setDashPattern(DashPattern pattern)
   {
      dashPattern = pattern;
   }

   public DashPattern getDashPattern()
   {
      return dashPattern;
   }

   /**
    * Gets the winding rule.
    * @return the winding rule
    */
   public int getWindingRule()
   {
      return windingRule;
   }

   /**
    * Sets the winding rule.
    * @param rule the winding rule which must be
    * one of: {@link GeneralPath#WIND_EVEN_ODD} or
    * {@link GeneralPath#WIND_NON_ZERO}.
    */
   public void setWindingRule(int rule)
   {
      if (rule == GeneralPath.WIND_EVEN_ODD
        ||rule == GeneralPath.WIND_NON_ZERO)
      {
         windingRule = rule;
      }
      else
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.WINDING_RULE, rule,
            getCanvasGraphics());
      }
   }

   /**
    * Sets the mitre limit.
    * @param limit the mitre limit (can't be less than 1).
    */
   public void setMitreLimit(double limit)
   {
      if (limit < 1.0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.MITRE_LIMIT, limit,
            getCanvasGraphics());
      }

      mitreLimit = limit;
   }

   /**
    * Gets the mitre limit.
    * @return the mitre limit
    */
   public double getMitreLimit()
   {
      return mitreLimit;
   }

   /**
    * Gets the cap style.
    * @return the cap style
    */
   public int getCapStyle()
   {
      return capStyle;
   }

   /**
    * Sets the cap style.
    * @param style the cap style, which must be one of:
    * {@link BasicStroke#CAP_SQUARE}, {@link BasicStroke#CAP_BUTT}
    * or {@link BasicStroke#CAP_ROUND}.
    */
   public void setCapStyle(int style)
   {
      if (style == BasicStroke.CAP_BUTT
        ||style == BasicStroke.CAP_ROUND
        ||style == BasicStroke.CAP_SQUARE)
      {
         capStyle = style;
      }
      else
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.CAP_STYLE, style,
            getCanvasGraphics());
      }
   }

   /**
    * Gets the join style.
    * @return the join style
    */
   public int getJoinStyle()
   {
      return joinStyle;
   }

   /**
    * Sets the join style.
    * @param style the join style, which must be one of:
    * of: {@link BasicStroke#JOIN_MITER},
    * {@link BasicStroke#JOIN_BEVEL} or {@link BasicStroke#JOIN_ROUND}
    */
   public void setJoinStyle(int style)
   {
      if (style == BasicStroke.JOIN_MITER
        ||style == BasicStroke.JOIN_ROUND
        ||style == BasicStroke.JOIN_BEVEL)
      {
         joinStyle = style;
      }
      else
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.JOIN_STYLE, style,
           getCanvasGraphics());
      }
   }

   public JDRPathStyleListener getPathStyleListener()
   {
      return pathStyleListener;
   }

   public String info()
   {
      String str = "Basic stroke: dash="+dashPattern;

      str += ", cap="+capStyle
           + ", join="+joinStyle
           + ", mitre="+mitreLimit
           + ", winding rule="+windingRule
           + ", pen width="+penWidth
           + ", start marker="+startMarker
           + ", mid marker="+midMarker
           + ", end marker="+endMarker;

      return str;
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      canvasGraphics = cg;

      if (startMarker != null)
      {
         startMarker.setCanvasGraphics(cg);
      }

      if (midMarker != null)
      {
         midMarker.setCanvasGraphics(cg);
      }

      if (endMarker != null)
      {
         endMarker.setCanvasGraphics(cg);
      }

      if (dashPattern != null)
      {
         dashPattern.setCanvasGraphics(cg);
      }
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      if (canvasGraphics == null)
      {
         setCanvasGraphics(cg);
         return;
      }

      JDRUnit oldUnit = canvasGraphics.getStorageUnit();
      JDRUnit newUnit = cg.getStorageUnit();

      canvasGraphics = cg;

      if (dashPattern != null)
      {
         dashPattern.applyCanvasGraphics(cg);
      }

      if (startMarker != null)
      {
         startMarker.applyCanvasGraphics(cg);
      }

      if (midMarker != null)
      {
         midMarker.applyCanvasGraphics(cg);
      }

      if (endMarker != null)
      {
         endMarker.applyCanvasGraphics(cg);
      }
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   /**
    * The dash pattern.
    */
   public volatile DashPattern dashPattern;

   /**
    * The cap style.
    */
   protected volatile int capStyle;
   /**
    * The join style.
    */
   protected volatile int joinStyle;
   /**
    * The winding rule.
    */
   protected volatile int windingRule;
   /**
    * The mitre limit.
    */
   protected volatile double mitreLimit;

   private volatile JDRLength penWidth;
   private volatile JDRMarker startMarker, midMarker, endMarker;

   private static JDRPathStyleListener pathStyleListener
      = new JDRBasicPathStyleListener();

   private volatile CanvasGraphics canvasGraphics;
}

