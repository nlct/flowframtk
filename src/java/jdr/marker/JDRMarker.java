// File          : JDRMarker.java
// Creation Date : 28th September 2006
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

package com.dickimawbooks.jdr.marker;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;

import java.awt.*;
import java.awt.geom.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Generic vertex marker.
 * <p>
 * Markers may appear at the start, mid or end vertices of
 * a path. When a marker is drawn, it needs the segment to
 * which it is attached in order to correctly align the
 * marker so that its origin coincides with the vertex. 
 * The mid and end markers are always drawn at the
 * end of the given segment. The start markers are reflected
 * and drawn at the start of the segment. (See 
 * <a href="#fig1">Figure 1</a>.)
 * <p>
 * <center>
 * <table width=60%>
 * <tr align=center><td colspan=2><a name="fig1"></a>
 * <img src="../images/basicMarker.png" alt="[image illustrating basic marker shape]"></td></tr>
 * <tr><th valign=top>Figure&nbsp;1:</th>
 * <td>(a) basic marker shape (for LaTeX style arrow 
 * marker defined by {@link ArrowSingle}), (b) marker appearance
 * as mid or end marker, (c) marker appearance as a start marker.
 * </td></tr>
 * </table>
 * </center>
 * <p>
 * A marker has a basic shape which is given by 
 * {@link #getGeneralPath()}, but the shape of the marker when
 * it is drawn may be a reflection of this shape, or may 
 * repeat this shape. (See <a href="#fig2">Figure 2</a>.)
 * <p>
 * <center>
 * <table width=60%>
 * <tr align=center><td colspan=2><a name="fig2"></a>
 * <img src="../images/basicMarker2.png" alt="[image illustrating variations of marker shape]"></td></tr>
 * <tr><th valign=top>Figure&nbsp;2:</th>
 * <td>(a) basic marker shape (for LaTeX style arrow 
 * marker defined by {@link ArrowSingle}), (b) marker appearance
 * as mid or end marker where the marker has been reversed, 
 * (c) marker appearance where the marker has a repeat factor
 * of 2, (d) marker appearance where the marker has a repeat
 * factor of 2 and is reversed.</td></tr>
 *</table>
 * </center>
 * <p>
 * A marker may even be combined with a 
 * secondary marker. This secondary marker may be overlaid on top
 * of the primary marker so that both the marker origins coincide
 * with the vertex, or the secondary marker may be offset. (See
 * <a href="#fig3">Figure 3</a>.)
 * <p>
 * <center>
 * <table width=60%>
 * <tr align=center><td colspan=2><a name="fig3"></a>
 * <img src="../images/basicMarker3.png" alt="[image illustrating composite markers]"></td></tr>
 * <tr><th valign=top>Figure&nbsp;3:</th>
 * <td>(a) basic marker shape for LaTeX style arrow 
 * marker defined by {@link ArrowSingle}, (b) basic marker
 * shape for square bracket marker defined by {@link ArrowSquare}.
 * A composite marker was then formed from the LaTeX arrow marker
 * as the primary marker and the square bracket as the
 * secondary marker: (c) the secondary marker is offset from
 * the primary marker, (d) the secondary marker is overlaid on
 * top of the primary marker.</td></tr>
 *</table>
 * </center>
 * <p>
 * Sub classes should overload {@link #getGeneralPath()}
 * to construct the basic marker shape, but should not
 * overload the other <code>get&lt;X&gt;GeneralPath</code> methods
 * as these are generic methods which use 
 * <code>getGeneralPath()</code> to construct the actual
 * marker shape given its properties. Note that the origin of the
 * path given by <code>getGeneralPath()</code> is mapped onto
 * the required vertex. Any repeated shapes will be offset
 * along the marker's x axis. The origin need not be the centre
 * of the marker.
 * <p>
 * A marker will either be oriented so that its x axis lies along
 * the gradient of the path at the given vertex (auto orient
 * enabled), or it may be oriented by a fixed angle (auto orient
 * disabled.) Note that if the auto orientation property
 * is disabled, start markers will not be reflected. (See
 * <a href="#fig4">Figure 4</a>.)
 * <p>
 * <center>
 * <table width=60%>
 * <tr align=center><td colspan=2><a name="fig4"></a>
 * <img src="../images/basicMarker4.png" alt="[image illustrating marker orientation]"></td></tr>
 * <tr><th valign=top>Figure&nbsp;4:</th>
 * <td>(a) basic marker shape for LaTeX style arrow 
 * marker, (b) the marker
 * is oriented so that its x axis lies along the line's 
 * gradient (the start marker has been reflected), (c) the marker is oriented according to its
 * angle of rotation (0 radians by default.) The start marker 
 * hasn't been reflected.</td></tr>
 * </table>
 * </center>
 * <p>
 * The marker's paint may be dependent on the colour of the
 * associated path, or it may be a fixed colour independent of
 * the line colour. (See
 * <a href="#fig5">Figure 5</a>.)
 * <p>
 * <center>
 * <table width=60%>
 * <tr align=center><td colspan=2><a name="fig5"></a>
 * <img src="../images/basicMarker5.png" alt="[image illustrating colours]"></td></tr>
 * <tr><th valign=top>Figure&nbsp;5:</th>
 * <td>(a) the marker colour is dependent on the line
 * colour, (b) the marker colour is independent of the line
 * colour.</td></tr>
 * </table>
 * </center>
 * <p>
 * A marker may have an associated size, or it may scale
 * according to the line width of the associated path (or
 * both.) For this reason, you can specify both the line
 * width of the associated path, and the marker size.
 *<p>
 * The secondary (or composite) marker has its
 * own independent settings.
 * (See <a href="#fig6">Figure 6</a>.)
 * The code in fact allows for 
 * composite markers to have their own composite markers, 
 * but these are not implemented in 
 * <code>flowframtk</code>.
 * <p>
 * <center>
 * <table width=60%>
 * <tr align=center><td colspan=2><a name="fig6"></a>
 * <img src="../images/basicMarker6.png" alt="[image illustrating composite markers independent settings]"></td></tr>
 * <tr><th valign=top>Figure&nbsp;6:</th>
 * <td>(a) basic marker shape for LaTeX style arrow 
 * marker defined by {@link ArrowSingle}, (b) basic marker
 * shape for square bracket marker defined by {@link ArrowSquare},
 * (c) Composite marker formed with the LaTeX arrow marker as
 * the primary marker and the square bracket as the secondary marker.
 * The primary marker has a repeat factor of 2 and its fill
 * colour is set to red, while the secondary marker has a blue
 * fill colour and has a repeat factor of 1 but has been reversed.
 *</td></tr>
 *</table>
 * </center>
 * <p>
 * Each known subclass of <code>JDRMarker</code> has an
 * associated numeric value used in the JDR/AJR file formats
 * to identify that marker type. The <code>JDRMarker</code>
 * class itself has the numeric type {@link #ARROW_NONE} indicating 
 * no visible marker.
 * <p>
 * <b>Notes:</b>
 * <ul>
 * <li> The markers have become too complicated to use the
 * <code>pgf</code> LaTeX package arrow mechanism, so each 
 * marker is now set as an individual path when the image
 * is exported to a TeX file.
 * <li> Similarly, the markers are set as individual paths
 * when exporting the image to an EPS file.
 * <li> I need to update the SVG related code.
 * </ul>
 * @author Nicola L C Talbot
 */
public class JDRMarker implements Serializable,Cloneable,JDRConstants
{
   /**
    * Create a generic marker for a path with given pen width.
    * Markers may be repeated and/or reversed. Some markers
    * may depend on the line width of the associated path.
    * @param penwidth the width of the associated path in
    * PostScript points
    * @param repeat the repeat factor (can't be less than 1)
    * @param isReversed specifies if marker should be reversed
    */
   public JDRMarker(JDRLength penwidth, int repeat, boolean isReversed)
   {
      JDRMessageDictionary msgSys = penwidth.getMessageSystem();

      if (repeat < 1)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.REPEAT, repeat, msgSys);
      }

      repeated = repeat;
      reversed = isReversed;
      penWidth = penwidth;
      offset_  = new JDRLength(msgSys);
      repeatOffset = new JDRLength(msgSys, 7.0*penWidth.getValue(), 
         penWidth.getUnit());
      angle_ = new JDRAngle(msgSys);
      size = new JDRLength(msgSys, 5, JDRUnit.bp);
      width = null;
   }

   /**
    * Create a generic marker of given size for a path with 
    * given pen width.
    * Markers may be repeated and/or reversed. Some markers
    * may depend on the line width of the associated path as
    * well as having a specified size.
    * @param penwidth the width of the associated path in
    * PostScript points
    * @param repeat the repeat factor (can't be less than 1)
    * @param isReversed specifies if marker should be reversed
    * @param arrowSize the size of the marker
    */
   public JDRMarker(JDRLength penwidth, int repeat, boolean isReversed,
                    JDRLength arrowSize)
   {
      this(penwidth, repeat, isReversed, arrowSize, null);
   }

   /**
    * Create a generic marker of given length and width for a path with 
    * given pen width.
    * Markers may be repeated and/or reversed. Some markers
    * may depend on the line width of the associated path as
    * well as having a specified size or length and width.
    * @param penwidth the width of the associated path in
    * PostScript points
    * @param repeat the repeat factor (can't be less than 1)
    * @param isReversed specifies if marker should be reversed
    * @param arrowLength the length of the marker (or size if no width)
    * @param arrowWidth the width of the marker (may be null)
    */
   public JDRMarker(JDRLength penwidth, int repeat, boolean isReversed,
                    JDRLength arrowLength, JDRLength arrowWidth)
   {
      this(penwidth, repeat, isReversed);
      size.makeEqual(arrowLength);

      if (arrowWidth == null || width == null)
      {
         width = arrowWidth;
      }
      else
      {
         width.makeEqual(arrowWidth);
      }
   }


   /**
    * Create default marker. This is equivalent to
    * <code>JDRMarker(1.0, 1, false)</code>
    */
   public JDRMarker(JDRMessageDictionary msgSys)
   {
      this(new JDRLength(msgSys, 1.0, JDRUnit.bp), 1, false);
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      if (cg == null)
      {
         throw new NullPointerException();
      }

      canvasGraphics = cg;

      if (composite != null)
      {
         composite.setCanvasGraphics(cg);
      }

      if (fillPaint != null)
      {
         fillPaint.setCanvasGraphics(cg);
      }
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      if (cg == null)
      {
         throw new NullPointerException();
      }

      if (composite != null)
      {
         composite.applyCanvasGraphics(cg);
      }

      if (fillPaint != null)
      {
         fillPaint.applyCanvasGraphics(cg);
      }
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   /**
    * Draw this marker on given graphics context for given segment
    * where the segment line width is given by {@link #penWidth}.
    * If this marker is to be drawn at
    * the end of the segment it is drawn in the direction of
    * the segment, otherwise it is drawn in the opposite 
    * direction unless the auto orientation is disabled in which
    * case it is drawn at the given angle of orientation
    * given by {@link #angle_}.
    * @param segment the segment on which to draw marker
    * @param start marker is drawn at the start of the
    * segment if this is true, otherwise it is drawn at the
    * end.
    */
   public void draw(JDRPathSegment segment, boolean start)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (cg == null)
      {
         cg = segment.getCanvasGraphics();
         setCanvasGraphics(cg);
      }

      Graphics2D g2 = cg.getGraphics();

      if (g2 == null) return;

      Stroke oldStroke = g2.getStroke();
      g2.setStroke(new BasicStroke(1.0f));

      Shape shape = getStorageShape(segment, start);

      Paint oldPaint = null;

      if (fillPaint != null)
      {
         oldPaint = g2.getPaint();
         BBox box = getStorageBBox();
         g2.setPaint(fillPaint.getPaint(box));
      }

      g2.fill(shape);

      g2.setStroke(oldStroke);

      if (fillPaint != null) g2.setPaint(oldPaint);

      if (composite != null)
      {
         composite.draw(segment, start);
      }
   }

   public void print(Graphics2D g2, JDRPathSegment segment, boolean start)
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (cg == null)
      {
         cg = segment.getCanvasGraphics();
         setCanvasGraphics(cg);
      }

      if (g2 == null) return;

      Stroke oldStroke = g2.getStroke();
      g2.setStroke(new BasicStroke(1.0f));

      Shape shape = getBpShape(segment, start);

      Paint oldPaint = null;

      if (fillPaint != null)
      {
         oldPaint = g2.getPaint();
         BBox box = getStorageBBox();
         g2.setPaint(fillPaint.getPaint(box));
      }

      g2.fill(shape);

      g2.setStroke(oldStroke);

      if (fillPaint != null) g2.setPaint(oldPaint);

      if (composite != null)
      {
         composite.draw(segment, start);
      }
   }

   /**
    * Saves this marker in Encapsulated PostScript format.
    * @param pathPaint the colour of the associated path
    * @param bpPathBBox the bounding box of the associated path
    * @param segment the segment on which the marker is to be
    * drawn
    * @param start if this is true, draw the marker at the start
    * of the segment, otherwise draw it at the end
    * @param out the output stream
    * @throws IOException if I/O error occurs
    */
   public void saveEPS(JDRPaint pathPaint, BBox bpPathBBox,
      JDRPathSegment segment, boolean start, PrintWriter out)
      throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      out.println("gsave");

      Shape shape = getStorageShape(segment, start);

      if (cg.getStorageUnitID() != JDRUnit.BP)
      {
         double storageToBp = cg.storageToBp(1.0);

         shape = AffineTransform.getScaleInstance(storageToBp, storageToBp)
             .createTransformedShape(shape);
      }

      JDRPaint paint = pathPaint;

      if (fillPaint != null)
      {
         paint = fillPaint;
      }

      paint.saveEPS(out, bpPathBBox);

      EPS.savePath(shape, out);

      if ((paint instanceof JDRGradient)
         || (paint instanceof JDRRadial))
      {
         out.println("clip shfill");
      }
      else
      {
         out.println("fill");
      }

      out.println("grestore");

      if (composite != null)
      {
         composite.saveEPS(pathPaint, bpPathBBox, segment, start, out);
      }
   }

   public Shape getBpShape(JDRPathSegment segment, boolean start)
   {
      Shape path = getStorageShape(segment, start);

      CanvasGraphics cg = getCanvasGraphics();

      if (cg.getStorageUnitID() == JDRUnit.BP)
      {
         return path;
      }

      AffineTransform af = new AffineTransform();

      double factor = cg.storageToBp(1.0);

      af.scale(factor, factor);

      return af.createTransformedShape(path);
   }


   /**
    * Gets this marker's primary shape. 
    * The marker shape depends on whether
    * it is at the start or end of the segment, and whether
    * the auto orientation is set. (If this marker is at the
    * start of the segment, the shape is reflected, 
    * unless the auto orientation is off.) This shape does not
    * include the secondary marker shape, but does include
    * rotation.
    * @see #getStoragePrimaryGeneralPath()
    * @param segment the segment on which this marker should be
    * drawn
    * @param start indicates whether this marker should be
    * drawn at the start (<code>true</code>) or the end 
    * (<code>false</code>) of the segment
    */
   public Shape getStorageShape(JDRPathSegment segment, boolean start)
   {
      double p0x=0, p0y=0, p1x=0, p1y=0;

      if (start)
      {
         if (segment instanceof JDRBezier)
         {
            JDRBezier curve = (JDRBezier)segment;

            p1x = segment.getStart().x;
            p1y = segment.getStart().y;

            p0x = 3*curve.getControl1().x - 2*p1x;
            p0y = 3*curve.getControl1().y - 2*p1y;
         }
         else
         {
            p0x = segment.getEnd().x;
            p0y = segment.getEnd().y;

            p1x = segment.getStart().x;
            p1y = segment.getStart().y;
         }
      }
      else
      {
         if (segment instanceof JDRBezier)
         {
            JDRBezier curve = (JDRBezier)segment;

            p1x = segment.getEnd().x;
            p1y = segment.getEnd().y;

            p0x = 3*curve.getControl2().x - 2*p1x;
            p0y = 3*curve.getControl2().y - 2*p1y;
         }
         else if (segment instanceof JDRPartialBezier)
         {
            JDRPartialBezier curve = (JDRPartialBezier)segment;

            JDRPoint end = curve.getEnd();

            p1x = end.getX();
            p1y = end.getY();

            Point2D c2 = curve.getControl2();

            p0x = 3*c2.getX() - 2*p1x;
            p0y = 3*c2.getY() - 2*p1y;
         }
         else
         {
            JDRPoint end = segment.getEnd();

            p1x = end.getX();
            p1y = end.getY();

            p0x = segment.getStart().x;
            p0y = segment.getStart().y;
         }
      }

      Shape shape = getStorageShape(p0x, p0y, p1x, p1y,
         start && !hasXAxisSymmetry());

      return shape;
   }

   /**
    * Gets complete shape include composite markers.
    * This is only used to determine the area taken up by
    * this marker and its composites.
    * @see #getStorageShape(JDRPathSegment,boolean)
    */
   public Path2D getCompleteShape(JDRPathSegment segment, boolean start)
   {
      GeneralPath path = new GeneralPath(getStorageShape(segment, start));

      if (composite != null)
      {
         path.append(composite.getCompleteShape(segment, start), false);
      }

      return path;
   }

   /**
    * Indicates if this marker shape is symmetric about the x-axis.
    * @return true if this marker shape is symmetric about the
    * marker's x-axis
    */
   public boolean hasXAxisSymmetry()
   {
      return true;
   }

   /**
    * Gets the shape of the marker aligned along the given 
    * end points (of the gradient vector).
    * @param p0x the x co-ordinate of the starting point
    * @param p0y the y co-ordinate of the starting point
    * @param p1x the x co-ordinate of the end point
    * @param p1y the y co-ordinate of the end point
    * @param reflect indicates whether to reflect marker
    */
   private Shape getStorageShape(double p0x, double p0y,
                         double p1x, double p1y,
                         boolean reflect)
   {
      AffineTransform af = new AffineTransform();

      GeneralPath marker = getStoragePrimaryGeneralPath();

      // rotate so that the x-axis lies along 
      // the line defined by p0 -> p1

      double angle=0;

      if (autoOrient_)
      {
         if (reflect)
         {
            af.scale(1, -1);
            marker.transform(af);
            af.setToIdentity();
         }

         double dx = p1x-p0x;
         double dy = p1y-p0y;

         if (dy == 0)
         {
            angle = dx < 0 ? Math.PI : 0;
         }
         else if (dx == 0)
         {
            angle = dy < 0 ? -Math.PI/2 : Math.PI/2;
         }
         else
         {
            angle = Math.atan(dy/dx);
            if (dx < 0) angle += Math.PI;
         }
      }
      else
      {
         angle = angle_.toRadians();
      }

      af.rotate(angle);

      double offsetValue = offset_.getValue(getCanvasGraphics().getStorageUnit());

      Point2D r0 = new Point2D.Double(offsetValue, 0);
      Point2D r1 = new Point2D.Double(offsetValue, 0);
      af.transform(r0, r1);

      // shift so that marker origin lies at p1
      af.preConcatenate(AffineTransform.getTranslateInstance(p1x-r1.getX(), p1y-r1.getY()));

      Shape shape;

      shape = marker.createTransformedShape(af);

      double[] coords = new double[6];

      return shape;
   }

   /**
    * Writes the complete shape of this marker as a series of PGF 
    * commands.
    * This includes the composite marker (if
    * one exists.)
    * @param pathPaint the colour of the associated path
    * @param pathBBox the bounding box of the associated path
    * @param segment the segment on which this marker should
    * be drawn
    * @param start indicates whether this marker should be
    * drawn at the start or end of the segment
    */
   public void pgfShape(JDRPaint pathPaint, BBox pathBBox,
      JDRPathSegment segment, boolean start, TeX tex)
    throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (getType() == ARROW_NONE) return;

      Shape shape = getStorageShape(segment, start);

      JDRUnit unit = cg.getStorageUnit();

      PathIterator pi = shape.getPathIterator(tex.getTransform());

      double[] coords = new double[6];

      tex.comment("marker type "+getType());

      tex.println("{\\begin{pgfscope}");

      JDRPaint paint = (fillPaint == null ? pathPaint : fillPaint);

      if (paint instanceof JDRShading)
      {
         // not yet implemented gradient fill

         //tex.println(paint.pgffillcolor(getStorageBBox()));
         tex.println(paint.pgffillcolor(null));
         cg.warning("warning.pgf-no-marker-shading",
          "marker shading paint can't be exported to pgf");
      }
      else
      {
         tex.println(paint.pgffillcolor(null));
      }

      while (!pi.isDone())
      {
         switch (pi.currentSegment(coords))
         {
            case PathIterator.SEG_MOVETO:
               tex.println("\\pgfpathqmoveto{"+tex.length(cg, coords[0])+"}{"
                    + tex.length(cg, coords[1])+"}");
            break;
            case PathIterator.SEG_LINETO:
               tex.println("\\pgfpathqlineto{"+tex.length(cg, coords[0])+"}{"
                    + tex.length(cg, coords[1])+"}");
            break;
            case PathIterator.SEG_CUBICTO:
               tex.println("\\pgfpathqcurveto{"
                    + tex.length(cg, coords[0])+"}{"
                    + tex.length(cg, coords[1])+"}{"
                    + tex.length(cg, coords[2])+"}{"
                    + tex.length(cg, coords[3])+"}{"
                    + tex.length(cg, coords[4])+"}{"
                    + tex.length(cg, coords[5])+"}");
            break;
            case PathIterator.SEG_CLOSE:
               tex.println("\\pgfclosepath");
            break;
         }

         pi.next();
      }

      tex.println("\\pgfusepathqfill");
      tex.println("\\end{pgfscope}}");

      if (composite != null)
      {
         tex.comment("composite");
         composite.pgfShape(pathPaint, pathBBox, segment, start, tex);
      }
   }

   /**
    * Gets the width of the complete marker.
    * This includes space taken up by repeats and composites.
    * @return width of marker (in storage points)
    */
   public double getStorageWidth()
   {
      GeneralPath path = getStorageCompleteGeneralPath();

      return path.getBounds2D().getWidth();
   }

   /**
    * Gets the bounding box of the complete marker.
    * This includes space taken up by repeats and composites.
    */
   public BBox getStorageBBox()
   {
      GeneralPath path = getStorageCompleteGeneralPath();

      return new BBox(getCanvasGraphics(), path.getBounds2D());
   }

   public BBox getBpBBox()
   {
      Shape path = getBpCompleteGeneralPath();

      return new BBox(getCanvasGraphics(), path.getBounds2D());
   }

   /**
    * Gets the <code>GeneralPath</code> describing the basic
    * shape of this marker (in storage units).
    * Sub classes need to override this method (using 
    * {@link #getSize()} if the marker may have a variable size,
    * or {@link #getWidth()} if the marker may have a variable width,
    * and using {@link #getPenWidth()} if marker size
    * should depend on the line width of the associated path.)
    * @return empty <code>GeneralPath</code>
    */
   public GeneralPath getGeneralPath()
   {
      return new GeneralPath();
   }

   /**
    * Gets the <code>GeneralPath</code> describing the primary
    * shape of this marker. This includes repeats and reflections but
    * not composites. This does not include rotation. Uses storage
    * units.
    * @see #getStorageShape(JDRPathSegment, boolean)
    * @return primary path created from the path defined by
    * {@link #getGeneralPath()}, {@link #reversed} and
    * {@link #repeated}
    */
   public GeneralPath getStoragePrimaryGeneralPath()
   {
      GeneralPath path = getGeneralPath();
      GeneralPath marker;
      AffineTransform af;

      if (reversed)
      {
         af = new AffineTransform(-1, 0, 0, 1, 0, 0);
         marker = new GeneralPath(af.createTransformedShape(path));
      }
      else
      {
         af = new AffineTransform(1, 0, 0, 1, 0, 0);
         marker = new GeneralPath(path);
      }

      if (repeated > 1)
      {
         double storageRepeatOffset = repeatOffset.getValue(
            getCanvasGraphics().getStorageUnit());

         for (int i = 2; i <= repeated; i++)
         {
            af.translate(-storageRepeatOffset,0.0);

            marker.append(af.createTransformedShape(path), false);
         }
      }

      return marker;
   }

   /**
    * Gets the <code>GeneralPath</code> describing the complete
    * marker. This includes repeats, reflections and composite
    * markers, but not rotation. This is equivalent to 
    * <code>getCompleteGeneralPath(false)</code>.
    * @return the <code>GeneralPath</code> describing the
    * complete marker
    */
   public GeneralPath getStorageCompleteGeneralPath()
   {
      return getStorageCompleteGeneralPath(false);
   }

   /**
    * Gets the <code>GeneralPath</code> describing the complete
    * marker where the composite may be reversed. This includes 
    * repeats, reflections and composite markers, but not rotation.
    * @param reverseComposite indicates whether to reverse the
    * composite marker
    * @return the <code>GeneralPath</code> describing the
    * complete marker
    */
   public GeneralPath getStorageCompleteGeneralPath(boolean reverseComposite)
   {
      GeneralPath path = getGeneralPath();
      GeneralPath markerPath;

      markerPath = getStoragePrimaryGeneralPath();

      if (composite != null)
      {
         markerPath.append(composite.getStorageCompleteGeneralPath(), false);
      }

      return markerPath;
   }

   public Shape getBpCompleteGeneralPath()
   {
      return getBpCompleteGeneralPath(false);
   }

   public Shape getBpCompleteGeneralPath(boolean reverseComposite)
   {
      GeneralPath path = getStorageCompleteGeneralPath(reverseComposite);

      CanvasGraphics cg = getCanvasGraphics();

      if (cg.getStorageUnitID() == JDRUnit.BP)
      {
         return path;
      }

      AffineTransform af = new AffineTransform();

      double factor = cg.storageToBp(1.0);

      af.scale(factor, factor);

      return af.createTransformedShape(path);
   }

   /**
    * Determines if this marker uses {@link #size} in determining
    * its shape.
    * @return true if {@link #getGeneralPath()} uses {@link #size}
    */
   public boolean isResizable()
   {
      return false;
   }

   /**
    * Determines if this marker uses the associated path's line
    * with in determining its shape.
    * @return true if {@link #getGeneralPath()} uses the path's
    * line width
    */
   public boolean usesLineWidth()
   {
      return false;
   }

   /**
    * Saves marker definition in SVG format.
    * @param svg the SVG data
    * @param id a unique identifier for this marker
    * @throws IOException if I/O error occurs
    */
   public void svgDef(SVG svg, String id)
      throws IOException
   {
      CanvasGraphics cg = getCanvasGraphics();

      if (penWidth.getValue()==0.0 || type==ARROW_NONE) return;

      GeneralPath path = getStorageCompleteGeneralPath(isStart_);

      double scale = 1.0/penWidth.getValue(JDRUnit.bp);

      AffineTransform af = new AffineTransform(
         scale, 0.0f, 0.0f, scale, 0.0f, 0.0f);

      Shape shape = path.createTransformedShape(af);

      Rectangle2D bounds = shape.getBounds2D();

      double minX = bounds.getX();
      double minY = bounds.getY();
      double width = bounds.getWidth();
      double height = bounds.getHeight();

      svg.println("      <marker id=\""+id+"\" ");
      svg.println("         markerWidth=\""+svg.length(width)+"\" "
                + "         markerHeight=\""+svg.length(height)+"\"");
      svg.println("         viewBox=\""+svg.length(0)+" "+svg.length(0)
         +svg.length(width)+" " + svg.length(height)
         +"\" refX=\""+svg.length(-minX)+"\" refY=\""+svg.length(-minY)+"\"");
      svg.println("         markerUnits=\"strokeWidth\"");
      svg.println("         orient=\""+(autoOrient_?"auto":angle_.svg())+"\" >");
      svg.print("        <path d=\"");

      PathIterator pi = shape.getPathIterator(null);
      double[] coords = new double[6];

      while (!pi.isDone())
      {
         switch (pi.currentSegment(coords))
         {
            case PathIterator.SEG_MOVETO:
               svg.print("M "+svg.length(coords[0]-minX)
                        +" "+svg.length(coords[1]-minY)+" ");
            break;
            case PathIterator.SEG_LINETO:
               svg.print("L "+svg.length(coords[0]-minX)
                         +" "+svg.length(coords[1]-minY)+" ");
            break;
            case PathIterator.SEG_QUADTO:
               svg.print("S "+svg.length(coords[0]-minX)
                         +" "+svg.length(coords[1]-minY)
                         +" "+svg.length(coords[2]-minX)
                         +" "+svg.length(coords[3]-minY));
            break;
            case PathIterator.SEG_CUBICTO:
               svg.print("C "+svg.length(coords[0]-minX)
                         +" "+svg.length(coords[1]-minY)
                         +" "+svg.length(coords[2]-minX)
                         +" "+svg.length(coords[3]-minY)
                         +" "+svg.length(coords[4]-minX)
                         +" "+svg.length(coords[5]-minY));
            break;
            case PathIterator.SEG_CLOSE:
               svg.print("Z ");
            break;
         }

         pi.next();
      }

      svg.println("\"");

      if (fillPaint != null) svg.print(fillPaint.svgFill());

      svg.println("/>");

      svg.println("      </marker>");
   }

   /**
    * Returns PGF arrow command.
    * No longer used, as all markers are now saved as PGF paths
    * when saving as a PGF image.
    * @deprecated
    */
   protected String pgfarrow()
   {
      return "";
   }

   /**
    * Returns PGF commands to reverse or repeat markers.
    * No longer used, as all markers are now saved as PGF paths
    * when saving as a PGF image.
    * @deprecated
    */
   private String pgfdoarrow()
   {
      String str = "";

      String arrow = pgfarrow();

      if (reversed) str += "\\pgfarrowswap{";
 
      switch (repeated)
      {
         case 1:
            str += arrow;
         break;
         case 2:
            str += ("\\pgfarrowdouble{"+arrow+"}");
         break;
         case 3:
            str += ("\\pgfarrowtriple{"+arrow+"}");
         break;
      }

      if (reversed) str += "}";

      return str;
   }

   /**
    * Returns PGF commands to define the marker as a start/end
    * arrow.
    * No longer used as the markers are now drawn as paths
    * instead of using the PGF arrow commands.
    * @deprecated
    */
   public String pgf(boolean start)
   {

      return (start ? "\\pgfsetstartarrow{}"
                    : "\\pgfsetendarrow{}");
   }

   /**
    * Gets a known marker with default settings.
    * This assumes a line width of 1 PostScript point, a
    * repeat value of 1, not reversed and a marker size of
    * 5 PostScript points.
    * @param markerID the number uniquely identifying a 
    * known marker
    * @return the marker
    */
   public static JDRMarker getPredefinedMarker(CanvasGraphics cg,
      int markerID)
   {
      return getPredefinedMarker(cg, markerID, 
       new JDRLength(cg.getMessageSystem(), 1.0, JDRUnit.bp), 1, false, 
       new JDRLength(cg.getMessageSystem(), 5.0, JDRUnit.bp));

   }

   /**
    * Gets a known marker with given settings.
    * This assumes a marker size of
    * 5 PostScript points.
    * @param markerID the number uniquely identifying a 
    * known marker
    * @param penwidth the line width of the associated path
    * @param repeat the number of times the marker is repeated
    * @param isReversed determines whether or not to reverse
    * the marker
    * @return the marker
    */
   public static JDRMarker getPredefinedMarker(
      CanvasGraphics cg, int markerID,
      JDRLength penwidth, int repeat, boolean isReversed)
   {
      return getPredefinedMarker(cg, markerID, penwidth,
         repeat, isReversed, new JDRLength(cg, 5.0, JDRUnit.bp));
   }

   /**
    * Gets a known marker with given settings and size.
    * Note that some predefined markers may only have a size
    * dependent on the line width.
    * @param markerID the number uniquely identifying a 
    * known marker
    * @param penwidth the line width of the associated path (can't
    * be negative).
    * @param repeat the number of times the marker is repeated
    * (can't be less than 1)
    * @param isReversed determines whether or not to reverse
    * @param arrowSize the size of the marker where the marker
    * may have a variable size
    * the marker
    * @return the marker
    */
   public static JDRMarker getPredefinedMarker(CanvasGraphics cg,
      int markerID, JDRLength penwidth, int repeat, boolean isReversed,
      JDRLength arrowSize)
   {
      return getPredefinedMarker(cg, markerID, penwidth, repeat, isReversed,
      arrowSize, null);
   }

   /**
    * Gets a known marker with given settings and size.
    * Note that some predefined markers may only have a size
    * dependent on the line width.
    * @param markerID the number uniquely identifying a 
    * known marker
    * @param penwidth the line width of the associated path (can't
    * be negative).
    * @param repeat the number of times the marker is repeated
    * (can't be less than 1)
    * @param isReversed determines whether or not to reverse
    * @param arrowSize the size of the marker where the marker
    * may have a variable size
    * the marker
    * @param arrowWidth the width of the marker if the marker may
    * have both an length (size) and width (may be null).
    * @return the marker
    */
   public static JDRMarker getPredefinedMarker(CanvasGraphics cg,
      int markerID, JDRLength penwidth, int repeat, boolean isReversed,
      JDRLength arrowSize, JDRLength arrowWidth)
   {
      if (cg == null) throw new NullPointerException();

      JDRMarker marker = null;

      switch (markerID)
      {
         case ARROW_NONE:
            marker = new JDRMarker(penwidth, repeat, isReversed,
               arrowSize);
            break;
         case ARROW_POINTED :
            marker = new ArrowPointed(penwidth, repeat, isReversed,
               arrowSize);
            break;
         case ARROW_TRIANGLE :
            marker = new ArrowTriangle(penwidth, repeat, isReversed,
               arrowSize);
            break;
         case ARROW_INDEP_TRIANGLE2:
            marker = new ArrowIndepTriangle2(penwidth, repeat,
              isReversed, arrowSize, 
              arrowWidth == null ? (JDRLength)arrowSize.clone() : arrowWidth);
            break;
         case ARROW_DEP_TRIANGLE2:
            marker = new ArrowDepTriangle2(penwidth, repeat,
              isReversed, arrowSize, 
              arrowWidth == null ? (JDRLength)arrowSize.clone() : arrowWidth);
            break;
         case ARROW_OFFSET_TRIANGLE2:
            marker = new ArrowOffsetTriangle2(penwidth, repeat,
              isReversed, arrowSize, 
              arrowWidth == null ? (JDRLength)arrowSize.clone() : arrowWidth);
            break;
         case ARROW_STEALTH2:
            marker = new ArrowStealth2(penwidth, repeat,
              isReversed, arrowSize, 
              arrowWidth == null ? (JDRLength)arrowSize.clone() : arrowWidth);
            break;
         case ARROW_CIRCLE :
            marker = new ArrowCircle(penwidth, repeat, isReversed,
               arrowSize);
            break;
         case ARROW_DIAMOND :
            marker = new ArrowDiamond(penwidth, repeat, isReversed);
            break;
         case ARROW_SQUARE :
            marker = new ArrowSquare(penwidth, repeat, isReversed);
            break;
         case ARROW_BAR :
            marker = new ArrowBar(penwidth, repeat, isReversed);
            break;
         case ARROW_SINGLE :
            marker = new ArrowSingle(penwidth, repeat, isReversed);
            break;
         case ARROW_ROUND :
            marker = new ArrowRound(penwidth, repeat, isReversed);
            break;
         case ARROW_DOTFILLED :
            marker = new ArrowDotFilled(penwidth, repeat, isReversed,
              arrowSize);
            break;
         case ARROW_DOTOPEN :
            marker = new ArrowDotOpen(penwidth, repeat, isReversed,
              arrowSize);
            break;
         case ARROW_BOXFILLED :
            marker = new ArrowBoxFilled(penwidth, repeat, isReversed,
              arrowSize);
            break;
         case ARROW_BOXOPEN :
            marker = new ArrowBoxOpen(penwidth, repeat, isReversed,
              arrowSize);
            break;
         case ARROW_CROSS :
            marker = new ArrowCross(penwidth, repeat, isReversed,
              arrowSize);
            break;
         case ARROW_PLUS :
            marker = new ArrowPlus(penwidth, repeat, isReversed,
              arrowSize);
            break;
         case ARROW_STAR :
            marker = new ArrowStar(penwidth, repeat, isReversed,
              arrowSize);
            break;
         case ARROW_TRIANGLE_UP_FILLED :
            marker = new ArrowTriangleUpFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_TRIANGLE_UP_OPEN :
            marker = new ArrowTriangleUpOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_TRIANGLE_DOWN_FILLED :
            marker = new ArrowTriangleDownFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_TRIANGLE_DOWN_OPEN :
            marker = new ArrowTriangleDownOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_RHOMBUS_FILLED :
            marker = new ArrowRhombusFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_RHOMBUS_OPEN :
            marker = new ArrowRhombusOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_PENTAGON_FILLED:
            marker = new ArrowPentagonFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_PENTAGON_OPEN:
            marker = new ArrowPentagonOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HEXAGON_FILLED:
            marker = new ArrowHexagonFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HEXAGON_OPEN:
            marker = new ArrowHexagonOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_OCTAGON_FILLED:
            marker = new ArrowOctagonFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_OCTAGON_OPEN:
            marker = new ArrowOctagonOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_POINTED60:
            marker = new ArrowPointed60(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_POINTED45:
            marker = new ArrowPointed45(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HOOKS:
            marker = new ArrowHooks(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HOOK_UP:
            marker = new ArrowHookUp(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HOOK_DOWN:
            marker = new ArrowHookDown(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HALF_POINTED_UP:
            marker = new ArrowHalfPointedUp(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HALF_POINTED_DOWN:
            marker = new ArrowHalfPointedDown(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HALF_POINTED60_UP:
            marker = new ArrowHalfPointed60Up(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HALF_POINTED60_DOWN:
            marker = new ArrowHalfPointed60Down(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HALF_POINTED45_UP:
            marker = new ArrowHalfPointed45Up(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HALF_POINTED45_DOWN:
            marker = new ArrowHalfPointed45Down(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_CUSP:
            marker = new ArrowCusp(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HALF_CUSP_UP:
            marker = new ArrowHalfCuspUp(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HALF_CUSP_DOWN:
            marker = new ArrowHalfCuspDown(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_ALT_SINGLE:
            marker = new ArrowAltSingle(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_ALT_SINGLE_OPEN:
            marker = new ArrowAltSingleOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_TRIANGLE_OPEN:
            marker = new ArrowTriangleOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_CIRCLE_OPEN:
            marker = new ArrowCircleOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_DIAMOND_OPEN:
            marker = new ArrowDiamondOpen(penwidth, repeat,
              isReversed);
            break;
         case ARROW_BRACE:
            marker = new ArrowBrace(penwidth, repeat,
              isReversed);
            break;
         case ARROW_RECTANGLE_CAP:
            marker = new ArrowRectangleCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_BALL_CAP:
            marker = new ArrowBallCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_LEAF_CAP:
            marker = new ArrowLeafCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_LEAF2_CAP:
            marker = new ArrowLeaf2Cap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_LEAF3_CAP:
            marker = new ArrowLeaf3Cap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_CLUB_CAP:
            marker = new ArrowClubCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_LEAF3FOR_CAP:
            marker = new ArrowLeaf3ForCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_LEAF3BACK_CAP:
            marker = new ArrowLeaf3BackCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_LEAF2FOR_CAP:
            marker = new ArrowLeaf2ForCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_LEAF2BACK_CAP:
            marker = new ArrowLeaf2BackCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_BULGE_CAP:
            marker = new ArrowBulgeCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_CUTOUTBULGE_CAP:
            marker = new ArrowCutoutBulgeCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_CHEVRON_CAP:
            marker = new ArrowChevronCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_FAST_CAP:
            marker = new ArrowFastCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_ROUND_CAP:
            marker = new ArrowRoundCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_TRIANGLE_CAP:
            marker = new ArrowTriangleCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_INVERT_TRIANGLE_CAP:
            marker = new ArrowInvertTriangleCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_INVERT_CHEVRON_CAP:
            marker = new ArrowInvertChevronCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_INVERT_FAST_CAP:
            marker = new ArrowInvertFastCap(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_ALT_BAR:
            marker = new ArrowAltBar(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_ALT_ROUND:
            marker = new ArrowAltRound(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_ALT_SQUARE:
            marker = new ArrowAltSquare(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_ALT_BRACE:
            marker = new ArrowAltBrace(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_SEMICIRCLE_OPEN:
            marker = new ArrowSemiCircleOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_SEMICIRCLE_FILLED:
            marker = new ArrowSemiCircleFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_STAR5_OPEN:
            marker = new ArrowStar5Open(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_STAR5_FILLED:
            marker = new ArrowStar5Filled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_ASTERISK:
            marker = new ArrowAsterisk(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_SCISSORS_DOWN_FILLED:
            marker = new ArrowScissorsDownFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_SCISSORS_UP_FILLED:
            marker = new ArrowScissorsUpFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_SCISSORS_DOWN_OPEN:
            marker = new ArrowScissorsDownOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_SCISSORS_UP_OPEN:
            marker = new ArrowScissorsUpOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HEART_RIGHT_FILLED:
            marker = new ArrowHeartRightFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HEART_RIGHT_OPEN:
            marker = new ArrowHeartRightOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HEART_FILLED:
            marker = new ArrowHeartFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_HEART_OPEN:
            marker = new ArrowHeartOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_SNOWFLAKE:
            marker = new ArrowSnowflake(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_STAR_CHEVRON_OPEN:
            marker = new ArrowStarChevronOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_STAR_CHEVRON_FILLED:
            marker = new ArrowStarChevronFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_STAR6_OPEN:
            marker = new ArrowStar6Open(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_STAR6_FILLED:
            marker = new ArrowStar6Filled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_EQUILATERAL_FILLED:
            marker = new ArrowEquilateralFilled(penwidth, repeat,
              isReversed, arrowSize);
            break;
         case ARROW_EQUILATERAL_OPEN:
            marker = new ArrowEquilateralOpen(penwidth, repeat,
              isReversed, arrowSize);
            break;
         default:
           throw new JdrIllegalArgumentException(
              JdrIllegalArgumentException.MARKER_ID, markerID, cg);
      }

      marker.setCanvasGraphics(cg);
      return marker;
   }

   /**
    * Gets a copy of this marker.
    * @return a copy of this marker
    */
   public Object clone()
   {
      JDRMarker marker = new JDRMarker(penWidth, 
         repeated, reversed, (JDRLength)size.clone());

      marker.makeOtherEqual(this);

      return marker;
   }

   /**
    * Determines if this object is the same as another object.
    * @param o the other object
    * @return true if this object is the same as the other object
    */
   public boolean equals(Object o)
   {
      if (this == o) return true;

      if (o == null) return false;
      if (!(o instanceof JDRMarker)) return false;

      JDRMarker m = (JDRMarker)o;

      if (!penWidth.equals(m.penWidth)) return false;
      if (repeated != m.repeated) return false;
      if (reversed != m.reversed) return false;
      if (!size.equals(m.size)) return false;
      if (autoOrient_ != m.autoOrient_) return false;
      if (!angle_.equals(m.angle_)) return false;
      if (overlay != m.overlay) return false;
      if (!offset_.equals(m.offset_)) return false;
      if (!repeatOffset.equals(m.repeatOffset)) return false;
      if (userOffset != m.userOffset) return false;
      if (userRepeatOffset != m.userRepeatOffset) return false;

      if ((width == null && m.width != null)
       || (width != null && m.width == null)
       || (width != null && m.width != null && !width.equals(m.width)))
      {
         return false;
      }

      if (fillPaint == null)
      {
         if (m.fillPaint != null) return false;
      }
      else
      {
         if (!fillPaint.equals(m.fillPaint)) return false;
      }

      if (composite == null)
      {
         if (m.composite != null) return false;
      }
      else
      {
         if (!composite.equals(m.composite)) return false;
      }

      return true;
   }

   /**
    * Makes given marker identical to this marker.
    * NB this was makeEqual but the behaviour is inconsistent with
    * other classes with makeEqual method.
    * @param marker the marker to make the same as this marker
    */
   public void makeOtherEqual(JDRMarker marker)
   {
      if (fillPaint != null)
      {
         marker.fillPaint = (JDRPaint)fillPaint.clone();
      }

      marker.repeated = repeated;
      marker.reversed = reversed;
      marker.size.makeEqual(size);
      marker.autoOrient_ = autoOrient_;
      marker.angle_.makeEqual(angle_);
      marker.overlay  = overlay;
      marker.offset_.makeEqual(offset_);
      marker.penWidth.makeEqual(penWidth);
      marker.repeatOffset.makeEqual(repeatOffset);
      marker.userOffset  = userOffset;
      marker.userRepeatOffset  = userRepeatOffset;

      if (width == null)
      {
         marker.width = null;
      }
      else if (marker.width == null)
      {
         marker.width = (JDRLength)width.clone();
      }
      else
      {
         marker.width.makeEqual(width);
      }

      CanvasGraphics cg = getCanvasGraphics();

      if (cg != null)
      {
         marker.setCanvasGraphics(cg);
      }

      if (composite == null)
      {
         marker.composite = null;
      }
      else
      {
         marker.composite = (JDRMarker)composite.clone();
         marker.composite.parent = this;
      }
   }

   /**
    * Gets this marker's identifying number. (As used by
    * <code>getPredefinedMarker</code>)
    * @return the identifying number
    */
   public int getType()
   {
      return type;
   }

   /**
    * Saves this marker in given JDR/AJR format.
    * @throws IOException if I/O error occurs
    */
   public void save(JDRAJR jdr)
      throws IOException
   {
      float version = jdr.getVersion();
      CanvasGraphics cg = getCanvasGraphics();

      if (version == 1.0f)
      {
         if (type >= NUM_ARROWS1_0)
         {
            jdr.writeByte((byte)ARROW_NONE);
            return;
         }

         jdr.writeByte((byte)type);

         if (type != JDRMarker.ARROW_NONE)
         {
            jdr.writeFloat((float)size.getValue(JDRUnit.bp));

            if (jdr instanceof JDR)
            {
               jdr.writeBoolean(repeated==2);
            }
            else
            {
               jdr.writeInt(repeated);
            }

            jdr.writeBoolean(reversed);
         }
      }
      else
      {
         if (version < 1.4f && type >= NUM_ARROWS1_1)
         {
            jdr.writeByte((byte)ARROW_NONE);
            return;
         }

         jdr.writeByte((byte)type);

         if (type != JDRMarker.ARROW_NONE)
         {
            if (version < 1.8f)
            {
               jdr.writeFloat((float)size.getValue(JDRUnit.bp));
            }
            else
            {
               jdr.writeLength(size);
            }

            if (version >= 2.1f)
            {
               boolean hasWidth = (supportsWidth() && width != null);

               jdr.writeBoolean(hasWidth);

               if (hasWidth)
               {
                  jdr.writeLength(width);
               }
            }

            jdr.writeByte((byte)repeated);
            jdr.writeBoolean(reversed);

            jdr.writeBoolean(autoOrient_);

            if (!autoOrient_) 
            {
               if (version < 1.8f)
               {
                  jdr.writeFloat((float)angle_.toRadians());
               }
               else
               {
                  jdr.writeAngle(angle_);
               }
            }

            JDRPaintLoader paintLoader = jdr.getPaintLoader();

            paintLoader.save(jdr, (fillPaint == null ?
                              new JDRTransparent(cg) :
                              fillPaint));

            jdr.writeBoolean(overlay);

            if (version >= 1.4f)
            {
               if (!overlay)
               {
                  jdr.writeBoolean(userOffset);

                  if (userOffset)
                  {
                     if (version < 1.8f)
                     {
                        jdr.writeFloat((float)offset_.getValue(JDRUnit.bp));
                     }
                     else
                     {
                        jdr.writeLength(offset_);
                     }
                  }

                  jdr.writeBoolean(userRepeatOffset);

                  if (userRepeatOffset)
                  {
                     if (version < 1.8f)
                     {
                        jdr.writeFloat((float)repeatOffset.getValue(JDRUnit.bp));
                     }
                     else
                     {
                        jdr.writeLength(repeatOffset);
                     }
                  }
               }
            }

            // is it a composite arrow?

            if (composite == null)
            {
               jdr.writeByte((byte)ARROW_NONE);
            }
            else
            {
               composite.save(jdr);
            }
         }
      }
   }

   /**
    * Reads marker data from input stream in given version of 
    * JDR file format.
    * @throws InvalidFormatException if marker data is incorrectly
    * formatted
    * @return the marker defined by the input stream
    */
   public static JDRMarker read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      CanvasGraphics cg = jdr.getCanvasGraphics();
      JDRMessage msgSys = jdr.getMessageSystem();

      if (version == 1.0f)
      {
         int arrowType = (int)jdr.readByte(
            InvalidFormatException.MARKER_ID, 0, NUM_ARROWS1_0, true, false);

         double arrowSize = 5.0;
         int arrowRepeat = 1;
         boolean arrowReversed=false;

         if (arrowType != ARROW_NONE)
         {
            arrowSize = jdr.readFloat(InvalidFormatException.MARKER_SIZE);

            if (jdr instanceof JDR)
            {
               arrowRepeat =
                 jdr.readBoolean(InvalidFormatException.MARKER_REPEAT) ? 2 : 1;
            }
            else
            {
               arrowRepeat =
                 jdr.readIntGe(InvalidFormatException.MARKER_REPEAT, 1);
            }

            arrowReversed = jdr.readBoolean(
              InvalidFormatException.MARKER_REVERSED);
         }

         return getPredefinedMarker(cg,
                  arrowType, new JDRLength(msgSys, 1.0, JDRUnit.bp),
                  arrowRepeat, arrowReversed,
                  new JDRLength(msgSys, arrowSize, JDRUnit.bp));
      }
      else
      {
         int maxType;

         if (version < 1.4f)
         {
            maxType = NUM_ARROWS1_1;
         }
         else if (version < 1.6f)
         {
            maxType = NUM_ARROWS1_4;
         }
         else if (version < 2.1f)
         {
            maxType = NUM_ARROWS1_6;
         }
         else
         {
            maxType = NUM_ARROWS2_1;
         }

         int arrowType = (int)jdr.readByte(
            InvalidFormatException.MARKER_ID, 0, maxType, true, false);

         JDRLength arrowSize = new JDRLength(msgSys, 5.0, JDRUnit.bp);
         JDRLength width = null;
         int arrowRepeated = 1;
         boolean arrowReversed=false;
         boolean autoorient=true;
         JDRAngle angle = null;
         boolean overlayFlag=false;
         boolean userOffsetFlag=false;
         JDRLength userOffsetValue = new JDRLength(msgSys);
         JDRLength repeatOffsetValue = new JDRLength(msgSys);
         boolean repeatOffsetFlag=false;

         JDRPaint paint=null;

         JDRMarker compositeMarker=null;

         if (arrowType != ARROW_NONE)
         {
            if (version < 1.8f)
            {
               arrowSize.setValue(
                  jdr.readFloat(InvalidFormatException.MARKER_SIZE));
            }
            else
            {
               jdr.readLength(InvalidFormatException.MARKER_SIZE, arrowSize);
            }

            if (version >= 2.1f)
            {
               if (jdr.readBoolean(InvalidFormatException.MARKER_WIDTH_FLAG))
               {
                  width = new JDRLength(msgSys, 1.0, JDRUnit.bp);
                  jdr.readLength(InvalidFormatException.MARKER_WIDTH, width);
               }
            }

            arrowRepeated = (int)jdr.readByteGe(
              InvalidFormatException.MARKER_REPEAT, 0);

            arrowReversed = jdr.readBoolean(
              InvalidFormatException.MARKER_REVERSED);

            autoorient = jdr.readBoolean(
              InvalidFormatException.MARKER_AUTO_ORIENT);

            if (!autoorient)
            {
               if (version < 1.8f)
               {
                  angle = new JDRAngle(msgSys,
                   jdr.readFloat(InvalidFormatException.MARKER_ORIENT_ANGLE),
                   JDRAngle.RADIAN);
               }
               else
               {
                  angle = jdr.readAngle(InvalidFormatException.MARKER_ORIENT_ANGLE);
               }
            }

            JDRPaintLoader paintLoader = jdr.getPaintLoader();
            paint = paintLoader.load(jdr);

            if (paint instanceof JDRTransparent)
            {
               paint = null;
            }

            overlayFlag = jdr.readBoolean(
               InvalidFormatException.MARKER_OVERLAY_FLAG);

            if (version >= 1.4f)
            {
               if (!overlayFlag)
               {
                  userOffsetFlag = jdr.readBoolean(
                     InvalidFormatException.MARKER_OFFSET_FLAG);

                  if (userOffsetFlag)
                  {
                     if (version < 1.8f)
                     {
                        userOffsetValue.setValue(jdr.readFloat(
                          InvalidFormatException.MARKER_OFFSET), JDRUnit.bp);
                     }
                     else
                     {
                         jdr.readLength(
                          InvalidFormatException.MARKER_OFFSET, userOffsetValue);
                     }
                  }

                  repeatOffsetFlag = jdr.readBoolean(
                     InvalidFormatException.MARKER_REPEAT_OFFSET_FLAG);

                  if (repeatOffsetFlag)
                  {
                     if (version < 1.8f)
                     {
                        repeatOffsetValue.setValue(jdr.readFloat(
                           InvalidFormatException.MARKER_REPEAT_OFFSET), JDRUnit.bp);
                     }
                     else
                     {
                        jdr.readLength(
                          InvalidFormatException.MARKER_REPEAT_OFFSET, repeatOffsetValue);
                     }
                  }
               }
            }

            compositeMarker = read(jdr);

            if (compositeMarker.getType() == ARROW_NONE)
            {
               compositeMarker = null;
            }
         }

         if (angle == null)
         {
            angle = new JDRAngle(msgSys);
         }

         JDRMarker marker = getPredefinedMarker(cg,
                  arrowType, new JDRLength(msgSys, 1.0, JDRUnit.bp),
                  arrowRepeated, arrowReversed, arrowSize, width);

         marker.fillPaint=paint;
         marker.setCompositeMarker(compositeMarker);
         marker.setOrient(autoorient, angle);
         marker.setOverlay(overlayFlag);

         marker.enableUserRepeatOffset(repeatOffsetFlag);

         if (repeatOffsetFlag)
         {
            marker.setRepeatOffset(repeatOffsetValue);
         }

         marker.enableUserOffset(userOffsetFlag);

         if (userOffsetFlag)
         {
            marker.setOffset(userOffsetValue);
         }

         return marker;
      }
   }

   /**
    * Gets string identifying this marker. Sub classes should
    * overload this method. This is used for the SVG definition
    * of this marker.
    * @return identifying string
    */
   public String getID()
   {
      return "none";
   }

   /**
    * Gets string identifying this marker with independent colour
    * where this marker's location is given. 
    * @param start if this is true, this marker occurs at the
    * start of a segment, otherwise it is at the end of a
    * segment.
    * @return the identifying string
    */
   public String getID(boolean start)
   {
      return getID(start, fillPaint);
   }

   /**
    * Gets string identifying this marker with the given colour
    * and location. 
    * @param start if this is true, this marker occurs at the
    * start of a segment, otherwise it is at the end of a
    * segment.
    * @return the identifying string
    */
   public String getID(boolean start, JDRPaint p)
   {
      if (start) reversed=!reversed;
      String id = getID();
      if (start) reversed=!reversed;

      if (fillPaint == null)
      {
         if (p != null) id += p.getID();
      }
      else
      {
         id += fillPaint.getID();
      }

      return id;
   }

   /**
    * Gets the start marker SVG syntax for this marker.
    * @param p the line paint
    * @return the string containing the <code>marker-start</code>
    * syntax
    */
   public String svgStartMarker(JDRPaint p)
   {
      if (type==JDRMarker.ARROW_NONE)
         return "marker-start=\"none\"";

      return "marker-start=\"url(#" + getID(true, p)+")\"";
   }

   /**
    * Gets the mid marker SVG syntax for this marker.
    * @param p the line paint
    * @return the string containing the <code>marker-mid</code>
    * syntax
    */
   public String svgMidMarker(JDRPaint p)
   {
      if (type==JDRMarker.ARROW_NONE)
         return "marker-mid=\"none\"";

      return "marker-mid=\"url(#" + getID(false, p)+")\"";
   }

   /**
    * Gets the end marker SVG syntax for this marker.
    * @param p the line paint
    * @return the string containing the <code>marker-end</code>
    * syntax
    */
   public String svgEndMarker(JDRPaint p)
   {
      if (type==ARROW_NONE)
          return "marker-end=\"none\"";

      return "marker-end=\"url(#" +getID(false, p)+")\"";
   }

   /**
    * Writes all marker SVG definitions to output stream.
    * Some markers may be used more than once, but only
    * want to define them once at the start of the SVG file.
    * This iterates through all markers used in paths contained 
    * in the group, ignoring duplicates, and writes the 
    * SVG definition.
    * @param svg the export handler
    * @param group the group of objects which may or may not
    * contain paths
    * @throws IOException if I/O error occurs
    */
   public static void svgDefs(SVG svg, JDRGroup group)
      throws IOException
   {
      Hashtable<String,JDRMarker> markers
         = new Hashtable<String,JDRMarker>();

      // get all markers used in this image
      // some markers may be used more than once, but
      // only want to define it once.

      for (int i = 0, n=group.size(); i < n; i++)
      {
         JDRCompleteObject object = group.get(i);

         if (object instanceof JDRShape
          && ((JDRShape)object).getStroke() instanceof JDRBasicStroke)
         {
            JDRBasicStroke s = (JDRBasicStroke)((JDRShape)object).getStroke();
            JDRPaint paint = ((JDRShape)object).getLinePaint();

            // start marker

            JDRMarker start = (JDRMarker)s.getStartArrow().clone();
            if (start.fillPaint == null) start.fillPaint=paint;

            String id = start.getID(true, paint);
            start.reversed = !start.reversed;
            start.isStart_=true;

            if (!id.equals("none")) markers.put(id, start);

            // mid-point marker

            JDRMarker mid = (JDRMarker)s.getMidArrow().clone();
            if (mid.fillPaint == null) mid.fillPaint = paint;
            mid.isStart_=false;

            id = mid.getID(false, paint);
            if (!id.equals("none")) markers.put(id, mid);

            // end marker

            JDRMarker end = (JDRMarker)s.getEndArrow().clone();
            if (end.fillPaint == null) end.fillPaint = paint;
            end.isStart_=false;

            id = end.getID(false, paint);
            if (!id.equals("none")) markers.put(id, end);
         }
      }

      for (Enumeration e = markers.keys(); e.hasMoreElements(); )
      {
         String id = (String)e.nextElement();

         JDRMarker marker = (JDRMarker)markers.get(id);
         marker.svgDef(svg, id);
      }
   }

   /**
    * Gets this marker's auto orientation property. 
    * If true, this marker should be oriented to lie along the 
    * segment's gradient, otherwise it should be oriented 
    * according to the value returned by {@link #getAngle()}.
    * @return the auto orientation property
    */
   public boolean getAutoOrient()
   {
      return autoOrient_;
   }

   /**
    * Gets this marker's angle to be used if the auto 
    * orientation is disabled. If auto orientation is enabled
    * this value is ignored.
    * @see #getAutoOrient()
    * @see #setAngle(JDRAngle)
    * @return the angle of orientation
    */
   public JDRAngle getAngle()
   {
      return angle_;
   }

   /**
    * Sets this marker's angle to be used if the auto 
    * orientation is disabled. If auto orientation is enabled
    * this value is ignored.
    * @see #getAutoOrient()
    * @see #getAngle()
    */
   public void setAngle(JDRAngle angle)
   {
      angle_.makeEqual(angle);
   }

   /**
    * Sets whether or not to auto orient this marker.
    * @see #getAutoOrient()
    * @param autoOrient if true, automatically orient this
    * marker, otherwise orient it according to the value
    * of {@link #getAngle()}
    */
   public void setOrient(boolean autoOrient)
   {
      autoOrient_ = autoOrient;
   }

   /**
    * Sets whether or not to auto orient this marker and
    * the angle of orientation. The angle is only used
    * if the auto orientation property is disabled
    * @see #getAutoOrient()
    * @see #setAngle(double)
    * @param autoOrient if true, automatically orient this
    * marker, otherwise orient it according to the given angle
    */
   public void setOrient(boolean autoOrient, JDRAngle angle)
   {
      autoOrient_ = autoOrient;
      setAngle(angle);
   }

   /**
    * Gets the composite marker for this marker.
    * @return the composite marker or <code>null</code> if this 
    * marker doesn't have a composite
    */
   public JDRMarker getCompositeMarker()
   {
      return composite;
   }

   /**
    * Gets this marker's parent if this marker is a composite
    * marker.
    * @return parent marker or <code>null</code> if this marker
    * is a primary marker
    */
   public JDRMarker getParentMarker()
   {
      return parent;
   }

   /**
    * Sets the composite marker for this marker.
    * This will override any previous composite marker.
    * Setting the composite marker to <code>null</code>
    * will mean this marker no longer has a composite.
    * @param marker the composite marker or <code>null</code>
    * to remove current composite marker
    * @throws JdrIllegalArgumentException if this marker
    * is of type {@link #ARROW_NONE}
    */
   public void setCompositeMarker(JDRMarker marker)
   {
      if (marker == null)
      {
         composite = null;
         return;
      }
      else if (marker.getType() == ARROW_NONE)
      {
         composite = null;
         return;
      }

      if (getType() == ARROW_NONE)
      {
         throw new JdrIllegalArgumentException(
          JdrIllegalArgumentException.COMPOSITE_MARKER, ARROW_NONE,
          getCanvasGraphics());
      }

      composite = marker;
      composite.setPenWidth(penWidth);
      composite.parent = this;

      updateCompositeOffset();
   }

   /**
    * Updates this marker's knowledge of the line width of the 
    * associated path.
    * @param penwidth the line width of the associated path
    */
   public void setPenWidth(JDRLength penwidth)
   {
      penWidth = penwidth;

      if (!userRepeatOffset)
      {
         repeatOffset.setValue(7.0*penWidth.getValue(), penWidth.getUnit());
      }

      if (composite != null) composite.setPenWidth(penwidth);

      updateCompositeOffset();
   }

   /**
    * Gets line width of this marker's associated path.
    * @return the line width of the associated path.
    */
   public JDRLength getPenWidth()
   {
      return penWidth;
   }

   /**
    * Sets the fill paint for this marker. If the fill paint
    * is transparent or <code>null</code>, the marker uses the 
    * line paint of the associated path.
    * @param paint the paint for this marker
    * @see #getFillPaint()
    */
   public void setFillPaint(JDRPaint paint)
   {
      if (paint instanceof JDRTransparent)
      {
         fillPaint = null;
      }
      else
      {
         fillPaint = paint;
      }
   }

   /**
    * Gets the paint for this marker. If <code>null</code>, this
    * marker uses the line paint of the associated path.
    * @return this marker's paint or <code>null</code> if paint 
    * dependent on associated path
    * @see #setFillPaint(JDRPaint)
    */
   public JDRPaint getFillPaint()
   {
      return fillPaint;
   }

   public void fade(double factor)
   {
      if (fillPaint != null)
      {
         fillPaint.fade(factor);
      }
   }

   /**
    * Sets the repeat factor for this marker.
    * @param factor the repeat factor (can't be less than 1)
    * @see #getRepeated()
    */
   public void setRepeated(int factor)
   {
      if (factor < 1)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.REPEAT, factor,
          getCanvasGraphics());
      }

      repeated = factor;
      updateCompositeOffset();
   }

   /**
    * Gets the repeat factor for this marker.
    * @return the number of replicates
    * @see #setRepeated(int)
    */
   public int getRepeated()
   {
      return repeated;
   }

   /**
    * Sets whether or not to reverse this marker.
    * @param isReversed this should be <code>true</code> if this
    * marker should be reversed, otherwise <code>false</code>
    * @see #isReversed()
    */
   public void setReversed(boolean isReversed)
   {
      reversed = isReversed;
      updateCompositeOffset();
   }

   /**
    * Gets whether or not to reverse this marker.
    * @return <code>true</code> if this marker is reversed, 
    * otherwise <code>false</code>
    * @see #setReversed(boolean)
    */
   public boolean isReversed()
   {
      return reversed;
   }

   /**
    * Sets this marker's size, if the marker has a size setting.
    * (Some markers ignore the size setting.) The marker size
    * may also depend on the line width of the associated
    * path.
    * @param markerSize the size of the marker
    * @see #getSize()
    */
   public void setSize(JDRLength markerSize)
   {
      size = markerSize;
      updateCompositeOffset();
   }

   /**
    * Gets this marker's size. Some markers may ignore the
    * size setting, and some may also depend on the line
    * width of the associated path.
    * @see #setSize(JDRLength)
    * @return the size of this marker
    */
   public JDRLength getSize()
   {
      return size;
   }

   public boolean supportsWidth()
   {
      return false;
   }

   public void setWidth(JDRLength markerWidth)
   {
      if (supportsWidth())
      {
         width = markerWidth;
      }
   }

   public JDRLength getWidth()
   {
      return width;
   }

   /**
    * Sets the overlay property for this marker.
    * If the overlay property is enabled, the composite
    * marker will overlap the primary marker, otherwise
    * the composite marker will be offset from the
    * primary marker.
    * @param isOverlaid if <code>true</code> enable overlay
    * property for this marker, otherwise disable it
    * @see #isOverlaid()
    */
   public void setOverlay(boolean isOverlaid)
   {
      overlay = isOverlaid;
      updateCompositeOffset();
   }

   /**
    * Gets the overlay property for this marker.
    * @see #setOverlay(boolean)
    * @return <code>true</code> if composite marker should overlap
    * primary marker, <code>false</code> otherwise
    */
   public boolean isOverlaid()
   {
      return overlay;
   }

   /**
    * Updates the composite marker offset. Required when 
    * marker settings are updated.
    */
   private void updateCompositeOffset()
   {
      if (composite != null)
      {
         if (composite.userOffset)
         {
            return;
         }

         if (overlay)
         {
            composite.offset_.makeEqual(offset_);
         }
         else if (!userOffset)
         {
            JDRUnit unit = composite.offset_.getUnit();
            JDRUnit storageUnit = getCanvasGraphics().getStorageUnit();

            composite.offset_.setValue(
               unit.fromUnit(
                 getStoragePrimaryGeneralPath().getBounds2D().getWidth()
                 - getGeneralPath().getBounds2D().getWidth(),
                   storageUnit)
               );

            composite.offset_.add(repeatOffset);
            composite.offset_.subtract(offset_);
         }
      }
   }

   /**
    * Enables or disables whether this marker's offset can be
    * specified by the user. If this is enabled, the user can
    * specify the offset using {@link #setOffset(double)}, otherwise
    * the offset is zero if this marker is the primary marker or
    * the offset is computed from the parent marker's bounds and
    * the line width if this marker is a composite marker.
    * @param flag if true, allow user to specify the offset
    * @see #setOffset(JDRLength)
    * @see #isUserOffsetEnabled()
    */
   public void enableUserOffset(boolean flag)
   {
      userOffset = flag;

      if (!userOffset)
      {
         if (parent == null)
         {
            offset_ = new JDRLength(getCanvasGraphics().getMessageSystem());
         }
         else
         {
            parent.updateCompositeOffset();
         }

         updateCompositeOffset();
      }
   }

   /**
    * Enables or disables whether this marker's repeat offset can be
    * specified by the user. If this is enabled, the user can
    * specify the offset using {@link #setRepeatOffset(double)}, 
    * the offset is given by 7 times the line width.
    * @param flag if true, allow user to specify the repeat offset
    * @see #setRepeatOffset(JDRLength)
    * @see #isUserRepeatOffsetEnabled()
    */
   public void enableUserRepeatOffset(boolean flag)
   {
      userRepeatOffset = flag;

      if (!userRepeatOffset)
      {
         repeatOffset.setValue(7.0*penWidth.getValue(), penWidth.getUnit());

         updateCompositeOffset();
      }
   }

   /**
    * Determines whether this marker's offset can be specified
    * using {@link #setOffset(double)}
    * @return true if user can specify this marker's offset
    * @see #setOffset(JDRLength)
    * @see #enableUserOffset(boolean)
    */
   public boolean isUserOffsetEnabled()
   {
      return userOffset;
   }

   /**
    * Determines whether this marker's offset can be specified
    * using {@link #setRepeatOffset(double)}
    * @return true if user can specify this marker's repeat offset
    * @see #setRepeatOffset(double)
    * @see #enableUserRepeatOffset(boolean)
    */
   public boolean isUserRepeatOffsetEnabled()
   {
      return userRepeatOffset;
   }

   /**
    * Sets the offset for this marker if user offset flag is
    * enabled. This method only has an effect if
    * {@link #isUserOffsetEnabled()} returns true.
    * @param offset the new value of the offset
    * @see #setRepeatOffset(JDRLength)
    */

   public void setOffset(JDRLength offset)
   {
      if (userOffset)
      {
         offset_.makeEqual(offset);
      }
   }

   /**
    * Gets this marker's offset.
    * @return this marker's offset
    */
   public JDRLength getOffset()
   {
      return offset_;
   }

   /**
    * Sets the repeat offset for this marker if user offset flag is
    * enabled. This method only has an effect if
    * {@link #isUserOffsetEnabled()} returns true.
    * @param offset the new value of the repeat offset (gap between
    * repeat markers)
    * @see #setOffset(JDRLength)
    */
   public void setRepeatOffset(JDRLength offset)
   {
      if (userRepeatOffset)
      {
         repeatOffset.makeEqual(offset);
      }
   }

   /**
    * Gets the gap between repeat markers.
    * @return repeat offset
    */
   public JDRLength getRepeatOffset()
   {
      return repeatOffset;
   }

   public String toString()
   {
      return "JDRMarker[type:"+type+",size:"+size
        +",width:"+width
        +",autoorient:"+autoOrient_+",angle:"+angle_
        +",fill:"+fillPaint+",repeat:"+repeated
        +",reversed:"+reversed+",overlay:"+overlay
        +",offset:"+offset_+",userOffset:"+userOffset
        +",repeatOffset:"+repeatOffset+",userRepeatOffset:"+userRepeatOffset
        + ",composite:"+composite +"]";
   }

   /**
    * Gets the number of known markers for the latest version.
    * @return number of known markers
    */
   public static int maxMarkers()
   {
      return NUM_ARROWS2_1;
   }

   /**
    * Auto orient property. If <code>true</code>, this marker
    * is oriented to align with the associated path's gradient,
    * otherwise it is oriented by {@link #angle_}
    */
   protected boolean autoOrient_=true;
   /**
    * Angle (in radians) to rotate this marker if auto orient 
    * property is disabled.
    * @see #autoOrient_
    */
   protected JDRAngle angle_;
   /**
    * Fill paint for this marker. If <code>null</code>, the
    * line paint for the associated path is used instead.
    */
   public JDRPaint fillPaint=null;
   /**
    * Line width of the associated path.
    * (Some marker sizes are dependent on the line 
    * width.)
    */
   protected JDRLength penWidth;

   /**
    * This marker's repeat factor.
    */
   protected int repeated=1;
   /**
    * This marker's directional setting.
    */
   protected boolean reversed=false;

   /**
    * This marker's size property. Some markers ignore this
    * value.
    */
   protected JDRLength size;

   /**
    * This marker's width property. Requires minimum JDR/AJR v2.1.
    */
   protected JDRLength width;


   /**
    * This marker's composite marker. If <code>null</code>, this
    * marker has no secondary marker.
    */
   protected JDRMarker composite=null;

   /**
    * This marker's parent. If <code>null</code>, this marker is
    * a primary marker.
    */
   protected JDRMarker parent = null;

   /**
    * Overlay property for this marker. If <code>true</code>, the
    * composite marker overlaps this marker.
    */

   protected boolean overlay=false;
   /**
    * The offset from the vertex to the marker origin.
    * This must be updated whenever the marker properties are
    * changed.
    */
   private JDRLength offset_;

   /**
    * Determines whether the user has specified an explicit value
    * for this marker's offset.
    */
   private boolean userOffset = false;

   /**
    * The offset between repeated markers.
    */
   private JDRLength repeatOffset;

   /**
    * Determines whether the user has specified an explicit value
    * for this marker's repeat offset.
    */
   private boolean userRepeatOffset = false;

   /**
    * Identifying number for this marker type, as used in 
    * the AJR and JDR file formats.
    */
   protected int type=ARROW_NONE;

   /**
    * No marker.
    */
   public static final int ARROW_NONE=0;
   /**
    * Pointed marker corresponding to \pgfarrowpointed.
    */
   public static final int ARROW_POINTED=1;
   /**
    * Triangle marker corresponding to \pgfarrowtriangle.
    */
   public static final int ARROW_TRIANGLE=2;
   /**
    * Circle marker corresponding to \pgfarrowcircle.
    */
   public static final int ARROW_CIRCLE=3;
   /**
    * Diamond marker corresponding to \pgfarrowdiamond.
    */
   public static final int ARROW_DIAMOND=4;
   /**
    * Square marker corresponding to \pgfarrowsquare.
    */
   public static final int ARROW_SQUARE=5;
   /**
    * Bar marker corresponding to \pgfarrowbar.
    */
   public static final int ARROW_BAR=6;
   /**
    * LaTeX arrow style marker corresponding to \pgfarrowsingle.
    */
   public static final int ARROW_SINGLE=7;
   /**
    * Round bracket marker corresponding to \pgfarrowround.
    */
   public static final int ARROW_ROUND=8;
   /**
    * Filled dot marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_DOTFILLED=9;
   /**
    * Open dot marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_DOTOPEN=10;
   /**
    * Filled box marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_BOXFILLED=11;
   /**
    * Open box marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_BOXOPEN=12;
   /**
    * Cross marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_CROSS=13;
   /**
    * Plus marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_PLUS=14;
   /**
    * Star marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_STAR=15;
   /**
    * Filled up triangle marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_TRIANGLE_UP_FILLED=16;
   /**
    * Open up triangle marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_TRIANGLE_UP_OPEN=17;
   /**
    * Filled down triangle marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_TRIANGLE_DOWN_FILLED=18;
   /**
    * Open down triangle marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_TRIANGLE_DOWN_OPEN=19;
   /**
    * Filled rhombus marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_RHOMBUS_FILLED=20;
   /**
    * Open rhombus marker.
    * (JDR/AJR file formats version 1.1 onwards.)
    */
   public static final int ARROW_RHOMBUS_OPEN=21;

   /**
    * Filled pentagon marker.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_PENTAGON_FILLED=22;

   /**
    * Open pentagon marker.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_PENTAGON_OPEN=23;

   /**
    * Filled hexagon marker.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HEXAGON_FILLED=24;

   /**
    * Open hexagon marker.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HEXAGON_OPEN=25;

   /**
    * Filled octagon marker.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_OCTAGON_FILLED=26;

   /**
    * Open octagon marker.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_OCTAGON_OPEN=27;

   /**
    * Pointed arrow (60 degree angle).
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_POINTED60=28;

   /**
    * Pointed arrow (45 degree angle).
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_POINTED45=29;

   /**
    * Hooks arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HOOKS=30;

   /**
    * Hook up arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HOOK_UP=31;

   /**
    * Hook down arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HOOK_DOWN=32;

   /**
    * Upper half pointed arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HALF_POINTED_UP=33;

   /**
    * Lower half pointed arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HALF_POINTED_DOWN=34;

   /**
    * Upper half pointed 60 arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HALF_POINTED60_UP=35;

   /**
    * Lower half pointed 60 arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HALF_POINTED60_DOWN=36;

   /**
    * Upper half pointed 45 arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HALF_POINTED45_UP=37;

   /**
    * Lower half pointed 45 arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HALF_POINTED45_DOWN=38;

   /**
    * Cusp arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_CUSP=39;

   /**
    * Upper half cusp.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HALF_CUSP_UP=40;

   /**
    * Lower half cusp.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_HALF_CUSP_DOWN=41;

   /**
    * Alternative LaTeX style arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_ALT_SINGLE=42;

   /**
    * Outline LaTeX style arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_ALT_SINGLE_OPEN=43;

   /**
    * Outline of right triangle arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_TRIANGLE_OPEN=44;

   /**
    * Outline of circle arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_CIRCLE_OPEN=45;

   /**
    * Outline of diamond arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_DIAMOND_OPEN=46;

   /**
    * Brace arrow.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_BRACE=47;

   /**
    * Rectangle cap.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_RECTANGLE_CAP=48;

   /**
    * Chevron cap.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_CHEVRON_CAP=49;

   /**
    * Fast cap.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_FAST_CAP=50;

   /**
    * Round cap.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_ROUND_CAP=51;

   /**
    * Triangle cap.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_TRIANGLE_CAP=52;

   /**
    * Inverted Triangle cap.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_INVERT_TRIANGLE_CAP=53;

   /**
    * Inverted Chevron cap.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_INVERT_CHEVRON_CAP=54;

   /**
    * Inverted Fast cap.
    * (JDR/AJR file formats version 1.4 onwards.)
    */
   public static final int ARROW_INVERT_FAST_CAP=55;

   /**
    * Alternative bar marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_ALT_BAR=56;

   /**
    * Alternative round bracket marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_ALT_ROUND=57;

   /**
    * Alternative square bracket marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_ALT_SQUARE=58;

   /**
    * Alternative brace bracket marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_ALT_BRACE=59;

   /**
    * Open semi-circle marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_SEMICIRCLE_OPEN=60;

   /**
    * Closed semi-circle marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_SEMICIRCLE_FILLED=61;

   /**
    * Open 5 pointed star marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_STAR5_OPEN=62;

   /**
    * Filled 5 pointed star marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_STAR5_FILLED=63;

   /**
    * Asterisk marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_ASTERISK=64;

   /**
    * Down partial filled scissor marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_SCISSORS_DOWN_FILLED=65;

   /**
    * Up partial filled scissor marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_SCISSORS_UP_FILLED=66;

   /**
    * Down partial open scissor marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_SCISSORS_DOWN_OPEN=67;

   /**
    * Up partial open scissor marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_SCISSORS_UP_OPEN=68;

   /**
    * Filled right pointing heart shaped marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_HEART_RIGHT_FILLED=69;

   /**
    * Open right pointing heart shaped marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_HEART_RIGHT_OPEN=70;

   /**
    * Filled heart shaped marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_HEART_FILLED=71;

   /**
    * Open heart shaped marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_HEART_OPEN=72;

   /**
    * Snowflake shaped marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_SNOWFLAKE=73;

   /**
    * Open star chevron marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_STAR_CHEVRON_OPEN=74;

   /**
    * Filled star chevron marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_STAR_CHEVRON_FILLED=75;

   /**
    * Filled 6 pointed star marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_STAR6_FILLED=76;

   /**
    * Open 6 pointed star marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_STAR6_OPEN=77;

   /**
    * Filled equilateral marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_EQUILATERAL_FILLED=78;

   /**
    * Open equilateral marker.
    * (JDR/AJR file formats version 1.4 onwards)
    */
   public static final int ARROW_EQUILATERAL_OPEN=79;

   /**
    * Ball cap.
    * (JDR/AJR file formats version 1.6 onwards.)
    */
   public static final int ARROW_BALL_CAP=80;

   /**
    * Leaf cap.
    * (JDR/AJR file formats version 1.6 onwards.)
    */
   public static final int ARROW_LEAF_CAP=81;

   /**
    * Double leaf cap.
    * (JDR/AJR file formats version 1.6 onwards.)
    */
   public static final int ARROW_LEAF2_CAP=82;

   /**
    * Triple leaf cap.
    * (JDR/AJR file formats version 1.6 onwards.)
    */
   public static final int ARROW_LEAF3_CAP=83;

   /**
    * Club cap.
    * (JDR/AJR file formats version 1.6 onwards.)
    */
   public static final int ARROW_CLUB_CAP=84;

   /**
    * Triple leaf forward cap.
    * (JDR/AJR file formats version 1.6 onwards.)
    */
   public static final int ARROW_LEAF3FOR_CAP=85;

   /**
    * Triple leaf backwards cap.
    * (JDR/AJR file formats version 1.6 onwards.)
    */
   public static final int ARROW_LEAF3BACK_CAP=86;

   /**
    * Double leaf forward cap.
    * (JDR/AJR file formats version 1.6 onwards.)
    */
   public static final int ARROW_LEAF2FOR_CAP=87;

   /**
    * Double leaf backwards cap.
    * (JDR/AJR file formats version 1.6 onwards.)
    */
   public static final int ARROW_LEAF2BACK_CAP=88;

   /**
    * Bulge cap.
    * (JDR/AJR file formats version 1.6 onwards.)
    */
   public static final int ARROW_BULGE_CAP=89;

   /**
    * Cutout Bulge cap.
    * (JDR/AJR file formats version 1.6 onwards.)
    */
   public static final int ARROW_CUTOUTBULGE_CAP=90;

   /**
    * Triangle2. This marker has width and length and is
    * independent of the pen width.
    * (JDR/AJR file formats version 2.1 onwards.)
    */
   public static final int ARROW_INDEP_TRIANGLE2=91;

   /**
    * Alt Triangle2. This marker has width and length and is
    * dependent of the pen width.
    * (JDR/AJR file formats version 2.1 onwards.)
    */
   public static final int ARROW_DEP_TRIANGLE2=92;

   /**
    * Offset Triangle2. This marker has width and length and is
    * dependent of the pen width but is shifted back.
    * (JDR/AJR file formats version 2.1 onwards.)
    */
   public static final int ARROW_OFFSET_TRIANGLE2=93;

   /**
    * Stealth2. As offset but a stealth arrow.
    * (JDR/AJR file formats version 2.1 onwards.)
    */
   public static final int ARROW_STEALTH2=94;

   /**
    * Maximum number of known markers for AJR and JDR file
    * versions 2.1 onwards
    */
   public static final int NUM_ARROWS2_1=95;

   /**
    * Maximum number of known markers for AJR and JDR file
    * versions 1.6 onwards
    */
   public static final int NUM_ARROWS1_6=91;

   /**
    * Maximum number of known markers for AJR and JDR file
    * versions 1.4 onwards
    */
   public static final int NUM_ARROWS1_4=80;

   /**
    * Maximum number of known markers for AJR and JDR file 
    * versions 1.1 to 1.3.
    */
   public static final int NUM_ARROWS1_1=22;

   /**
    * Maximum number of known markers for AJR/JDR file version
    * 1.0.
    */
   public static final int NUM_ARROWS1_0=8;

   /**
    * Indicates whether or not this marker is at the start of
    * a segment.
    */
   private boolean isStart_=false;

   private CanvasGraphics canvasGraphics;
}
