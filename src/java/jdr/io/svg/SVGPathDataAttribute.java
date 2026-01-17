package com.dickimawbooks.jdr.io.svg;

import java.text.*;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.JDRCompleteObject;
import com.dickimawbooks.jdr.exceptions.*;

public class SVGPathDataAttribute implements SVGAttribute
{
   public SVGPathDataAttribute(SVGHandler handler, String valueString)
   {
      this(handler, "d", valueString);
   }

   public SVGPathDataAttribute(SVGHandler handler, String name, String valueString)
   {
      this.name = name;
      this.handler = handler;
      this.valueString = valueString;

      if (valueString == null || valueString.equals("inherit"))
      {
         data = null;
      }
      else
      {
         data = valueString;
      }
   }

   @Override
   public String getName()
   {
      return name;
   }

   @Override
   public Object getValue()
   {
      return data;
   }

   @Override
   public void applyTo(SVGAbstractElement element, JDRCompleteObject object)
   {
   }

   public Path2D getPath(SVGAbstractElement element)
     throws SVGException
   {
      if (data == null || data.equals("inherit"))
      {
         return null;
      }

      Path2D.Double path = new Path2D.Double();

      Point2D prevC = null;

      StringCharacterIterator it = new StringCharacterIterator(data);

      for (char c = it.first(); c != CharacterIterator.DONE; c = it.next())
      {
         if (Character.isWhitespace(c))
         {
            continue;
         }

         Point2D p;

         switch (c)
         {
            case 'M':
            case 'm':
            case 'L':
            case 'l':
              it.next();
              while (hasNextCoord(data, it))
              {
                 SVGLengthAttribute x = getCoordinate(data, it, true);
                 SVGLengthAttribute y = getCoordinate(data, it, false);

                 if (y == null)
                 {
                    throw new InvalidPathSpecException(handler,
                      getName(), c, "x y");
                 }

                 if (c == 'M')
                 {
                    path.moveTo(
                      x.getStorageValue(element, true),
                      y.getStorageValue(element, false));
                 }
                 else if (c == 'L')
                 {
                    path.lineTo(
                      x.getStorageValue(element, true),
                      y.getStorageValue(element, false));
                 }
                 else if (c == 'm' || c == 'l')
                 {
                    p = path.getCurrentPoint();

                    if (p == null)
                    {
                       p = new Point2D.Double(0, 0);
                    }

                    if (c == 'm')
                    {
                       path.moveTo(p.getX()+x.getStorageValue(element, true),
                                   p.getY()+y.getStorageValue(element, false));
                    }
                    else
                    {
                       path.lineTo(p.getX()+x.getStorageValue(element, true),
                                   p.getY()+y.getStorageValue(element, false));
                    }
                 }
              }
              prevC = null;
            break;
            case 'H':
            case 'h':
            case 'V':
            case 'v':
              it.next();
              while (hasNextCoord(data, it))
              {
                 SVGLengthAttribute coord = getCoordinate(data, it,
                    c == 'H' || c == 'h');

                 p = path.getCurrentPoint();

                 if (p == null)
                 {
                    p = new Point2D.Double(0, 0);
                 }

                 if (c == 'H')
                 {
                    path.lineTo(coord.getStorageValue(element, true),
                                p.getY());
                 }
                 else if (c == 'h')
                 {
                    path.lineTo(p.getX()+coord.getStorageValue(element, true),
                                p.getY());
                 }
                 else if (c == 'V')
                 {
                    path.lineTo(p.getX(),
                                coord.getStorageValue(element, false));
                 }
                 else
                 {
                    path.lineTo(p.getX(),
                                p.getY()+coord.getStorageValue(element, false));
                 }
              }
              prevC = null;
            break;
            case 'C':
            case 'c':
              it.next();
              while (hasNextCoord(data, it))
              {
                 SVGLengthAttribute x1 = getCoordinate(data, it, true);
                 SVGLengthAttribute y1 = getCoordinate(data, it, false);
                 SVGLengthAttribute x2 = getCoordinate(data, it, true);
                 SVGLengthAttribute y2 = getCoordinate(data, it, false);
                 SVGLengthAttribute x = getCoordinate(data, it, true);
                 SVGLengthAttribute y = getCoordinate(data, it, false);

                 if (y == null || x == null
                  || y2 == null || x2 == null
                  || y1 == null)
                 {
                    throw new InvalidPathSpecException(handler,
                      getName(), c, "x1 y1 x2 y2 x y");
                 }

                 double cx;
                 double cy;

                 if (c == 'C')
                 {
                    cx = x2.getStorageValue(element, true);
                    cy = y2.getStorageValue(element, false);

                    path.curveTo
                      (
                         x1.getStorageValue(element, true),
                         y1.getStorageValue(element, false),
                         cx, cy,
                         x.getStorageValue(element, true),
                         y.getStorageValue(element, false)
                      );
                 }
                 else
                 {
                    p = path.getCurrentPoint();

                    cx = p.getX()+x2.getStorageValue(element, true);
                    cy = p.getY()+y2.getStorageValue(element, false);

                    path.curveTo
                      (
                         p.getX()+x1.getStorageValue(element, true),
                         p.getY()+y1.getStorageValue(element, false),
                         cx, cy,
                         p.getX()+x.getStorageValue(element, true),
                         p.getY()+y.getStorageValue(element, false)
                      );
                 }

                 if (prevC == null)
                 {
                    prevC = new Point2D.Double(cx, cy);
                 }
                 else
                 {
                    prevC.setLocation(cx, cy);
                 }

              }
            break;
            case 'S':
            case 's':
              it.next();
              while (hasNextCoord(data, it))
              {
                 SVGLengthAttribute x2 = getCoordinate(data, it, true);
                 SVGLengthAttribute y2 = getCoordinate(data, it, false);
                 SVGLengthAttribute x = getCoordinate(data, it, true);
                 SVGLengthAttribute y = getCoordinate(data, it, false);

                 if (y == null || x == null || y2 == null)
                 {
                    throw new InvalidPathSpecException(handler,
                      getName(), c, "x2 y2 x y");
                 }

                 p = path.getCurrentPoint();

                 double cx, c2x;
                 double cy, c2y;

                 if (prevC == null)
                 {
                    cx = p.getX();
                    cy = p.getY();
                 }
                 else
                 {
                    cx = 2*p.getX() - prevC.getX();
                    cy = 2*p.getY() - prevC.getY();
                 }

                 if (c == 'S')
                 {
                    c2x = x2.getStorageValue(element, true);
                    c2y = y2.getStorageValue(element, false);

                    path.curveTo
                      (
                         cx, cy,
                         c2x, c2y,
                         x.getStorageValue(element, true),
                         y.getStorageValue(element, false)
                      );
                 }
                 else
                 {
                    c2x = p.getX()+x2.getStorageValue(element, true);
                    c2y = p.getY()+y2.getStorageValue(element, false);

                    path.curveTo
                      (
                         cx, cy,
                         c2x, c2y,
                         p.getX()+x.getStorageValue(element, true),
                         p.getY()+y.getStorageValue(element, false)
                      );
                 }

                 if (prevC == null)
                 {
                    prevC = new Point2D.Double(c2x, c2y);
                 }
                 else
                 {
                    prevC.setLocation(c2x, c2y);
                 }
              }
            break;
            case 'Q':
            case 'q':
              it.next();
              while (hasNextCoord(data, it))
              {
                 SVGLengthAttribute x1 = getCoordinate(data, it, true);
                 SVGLengthAttribute y1 = getCoordinate(data, it, false);
                 SVGLengthAttribute x = getCoordinate(data, it, true);
                 SVGLengthAttribute y = getCoordinate(data, it, false);

                 if (y == null || x == null || y1 == null)
                 {
                    throw new InvalidPathSpecException(handler,
                      getName(), c, "x1 y1 x y");
                 }

                 double cx;
                 double cy;

                 if (c == 'Q')
                 {
                    cx = x1.getStorageValue(element, true);
                    cy = y1.getStorageValue(element, false);

                    path.quadTo
                      (
                         cx, cy,
                         x.getStorageValue(element, true),
                         y.getStorageValue(element, false)
                      );
                 }
                 else
                 {
                    p = path.getCurrentPoint();

                    cx = p.getX()+x1.getStorageValue(element, true);
                    cy = p.getY()+y1.getStorageValue(element, false);

                    path.quadTo
                      (
                         cx, cy,
                         p.getX()+x.getStorageValue(element, true),
                         p.getY()+y.getStorageValue(element, false)
                      );
                 }

                 if (prevC == null)
                 {
                    prevC = new Point2D.Double(cx, cy);
                 }
                 else
                 {
                    prevC.setLocation(cx, cy);
                 }
              }
            break;
            case 'T':
            case 't':
              it.next();
              while (hasNextCoord(data, it))
              {
                 SVGLengthAttribute x = getCoordinate(data, it, true);
                 SVGLengthAttribute y = getCoordinate(data, it, false);

                 if (y == null)
                 {
                    throw new InvalidPathSpecException(handler,
                      getName(), c, "x y");
                 }

                 p = path.getCurrentPoint();

                 double cx;
                 double cy;

                 if (prevC == null)
                 {
                    cx = p.getX();
                    cy = p.getY();
                 }
                 else
                 {
                    cx = 2*p.getX() - prevC.getX();
                    cy = 2*p.getY() - prevC.getY();
                 }

                 if (c == 'S')
                 {
                    path.quadTo
                      (
                         cx, cy,
                         x.getStorageValue(element, true),
                         y.getStorageValue(element, false)
                      );
                 }
                 else
                 {
                    path.quadTo
                      (
                         cx, cy,
                         p.getX()+x.getStorageValue(element, true),
                         p.getY()+y.getStorageValue(element, false)
                      );
                 }

                 if (prevC == null)
                 {
                    prevC = new Point2D.Double(cx, cy);
                 }
                 else
                 {
                    prevC.setLocation(cx, cy);
                 }
              }
            break;
            case 'A':
            case 'a':
              it.next();
              while (hasNextCoord(data, it))
              {
                 SVGLengthAttribute rx = getCoordinate(data, it, true);
                 SVGLengthAttribute ry = getCoordinate(data, it, false);

                 SVGAngleAttribute angle = getAngle(data, it);

                 Boolean largeArcFlag = getBoolean(data, it);
                 Boolean sweepFlag = getBoolean(data, it);

                 SVGLengthAttribute x = getCoordinate(data, it, true);
                 SVGLengthAttribute y = getCoordinate(data, it, false);

                 if (y == null || x == null || sweepFlag == null
                  || largeArcFlag == null || angle == null
                  || ry == null)
                 {
                    throw new InvalidPathSpecException(handler,
                      getName(), c, "rx ry a 0|1 0|1 x y");
                 }

                 double x1 = x.getStorageValue(element, true);
                 double y1 = y.getStorageValue(element, false);

                 if (c == 'a')
                 {
                    Point2D p1 = path.getCurrentPoint();

                    x1 += p1.getX();
                    y1 += p1.getY();
                 }

                 arcTo(path, rx.getStorageValue(element, true),
                       ry.getStorageValue(element, false),
                       angle.getRadians(),
                       largeArcFlag.booleanValue(),
                       sweepFlag.booleanValue(),
                       x1, y1);
              }
              prevC = null;
            break;
            case 'Z':
            case 'z':
              path.closePath();
              prevC = null;
            break;
            default:
              throw new UnknownPathCommandException(handler, getName(), c);
         }
      }

      return path;
   }

   // The arcTo method is adapted from
   // http://stackoverflow.com/questions/1805101/svg-elliptical-arcs-with-java

   private void arcTo(Path2D.Double path, double rx, double ry,
    double theta, boolean largeArcFlag, boolean sweepFlag, double x, double y)
   {
       // Ensure radii are valid
       if (rx == 0 || ry == 0)
       {
          path.lineTo(x, y);
          return;
       }

       // Get the current (x, y) coordinates of the path

       Point2D p2d = path.getCurrentPoint();
       double x0 = p2d.getX();
       double y0 = p2d.getY();

       // Compute the half distance between the current and the final point

       double dx2 = (x0 - x) * 0.5;
       double dy2 = (y0 - y) * 0.5;

       //
       // Step 1 : Compute (x1, y1)
       //

       double x1 = (Math.cos(theta) * dx2
                 + Math.sin(theta) * dy2);
       double y1 = (-Math.sin(theta) * dx2
                 + Math.cos(theta) * dy2);

       // Ensure radii are large enough

       rx = Math.abs(rx);
       ry = Math.abs(ry);

       double Prx = rx * rx;
       double Pry = ry * ry;
       double Px1 = x1 * x1;
       double Py1 = y1 * y1;
       double d = Px1 / Prx + Py1 / Pry;

       if (d > 1)
       {
          rx = Math.abs((Math.sqrt(d) * rx));
          ry = Math.abs((Math.sqrt(d) * ry));
          Prx = rx * rx;
          Pry = ry * ry;
       }

       //
       // Step 2 : Compute (cx1, cy1)
       //

       double sign = (largeArcFlag == sweepFlag) ? -1d : 1d;

       double coef = (sign * Math
                       .sqrt(((Prx * Pry) - (Prx * Py1) - (Pry * Px1))
                                       / ((Prx * Py1) + (Pry * Px1))));
       if (Double.isNaN(coef))
       {
          coef = 0.0;
       }

       double cx1 = coef * ((rx * y1) / ry);
       double cy1 = coef * -((ry * x1) / rx);


       //
       // Step 3 : Compute (cx, cy) from (cx1, cy1)
       //

       double sx2 = (x0 + x) * 0.5;
       double sy2 = (y0 + y) * 0.5;

       double cx = sx2
                 + (Math.cos(theta) * cx1
                 - Math.sin(theta) * cy1);
       double cy = sy2
                 + (Math.sin(theta) * cx1
                 + Math.cos(theta) * cy1);

       //
       // Step 4 : Compute the angleStart (theta1) and the angleExtent (dtheta)
       //

       double ux = (x1 - cx1) / rx;
       double uy = (y1 - cy1) / ry;
       double vx = (-x1 - cx1) / rx;
       double vy = (-y1 - cy1) / ry;
       double p, n;

       // Compute the angle start

       n = Math.sqrt((ux * ux) + (uy * uy));
       p = ux; // (1 * ux) + (0 * uy)
       sign = (uy < 0) ? -1d : 1d;
       double angleStart = sign * Math.acos(p / n);

       // Compute the angle extent

       n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
       p = ux * vx + uy * vy;
       sign = (ux * vy - uy * vx < 0) ? -1d : 1d;
       double angleExtent = sign * Math.acos(p / n);

       if (!sweepFlag && angleExtent > 0)
       {
          angleExtent -= 2*Math.PI;
       }
       else if (sweepFlag && angleExtent < 0)
       {
          angleExtent += 2*Math.PI;
       }

       angleStart = Math.toDegrees(angleStart);
       angleExtent = Math.toDegrees(angleExtent);

       angleExtent %= 360;
       angleStart %= 360;

       Arc2D.Double arc = new Arc2D.Double();

       arc.x = -rx;
       arc.y = -ry;
       arc.width = rx * 2.0;
       arc.height = ry * 2.0;
       arc.start = -angleStart;
       arc.extent = -angleExtent;

       AffineTransform af = AffineTransform.getTranslateInstance(cx,cy);
       af.concatenate(AffineTransform.getRotateInstance(theta));

       path.append(af.createTransformedShape(arc), true);
    }


   private SVGLengthAttribute getCoordinate(String text, CharacterIterator iter,
      boolean isHorizontal)
     throws SVGException
   {
      int idx = iter.getIndex();

      Matcher m = NUMERIC_PATTERN.matcher(text.substring(idx));

      SVGLengthAttribute coord = null;

      if (m.matches())
      {
         String group1 = m.group(1);
         String group2 = m.group(2);

         coord = SVGLengthAttribute.valueOf(handler, "coordinate", group2,
            isHorizontal);

         iter.setIndex(idx+group1.length()+group2.length());

      }

      return coord;
   }

   private SVGAngleAttribute getAngle(String text, CharacterIterator iter)
     throws SVGException
   {
      int idx = iter.getIndex();

      Matcher m = NUMERIC_PATTERN.matcher(text.substring(idx));

      SVGAngleAttribute angle = null;

      if (m.matches())
      {
         String group1 = m.group(1);
         String group2 = m.group(2);

         angle = SVGAngleAttribute.valueOf(handler, group2);

         iter.setIndex(idx+group1.length()+group2.length());
      }

      return angle;
   }

   private Boolean getBoolean(String text, CharacterIterator iter)
     throws SVGException
   {
      int idx = iter.getIndex();

      Matcher m = NUMERIC_PATTERN.matcher(text.substring(idx));

      Boolean flag = null;

      if (m.matches())
      {
         String group1 = m.group(1);
         String group2 = m.group(2);

         try
         {
            int val = Integer.parseInt(group2);

            if (val < 0 || val > 1)
            {
               throw new InvalidPathSpecBooleanException(handler, getName(), group2);
            }

            flag = Boolean.valueOf(val == 1);

            iter.setIndex(idx+group1.length()+group2.length());
         }
         catch (NumberFormatException e)
         {
            throw new InvalidPathSpecBooleanException(handler, getName(), group2, e);
         }

      }

      return flag;
   }

   private boolean hasNextCoord(String text, CharacterIterator iter)
   {
      int idx = iter.getIndex();

      Matcher m = NUMERIC_PATTERN.matcher(text.substring(idx));

      return m.matches();
   }

   @Override
   public Object clone()
   {
      SVGPathDataAttribute attr = new SVGPathDataAttribute(handler, null);
      attr.makeEqual(this);
      return attr;
   }

   @Override
   public void makeEqual(SVGAttribute other)
   {
      if (other instanceof SVGPathDataAttribute)
      { 
         SVGPathDataAttribute attr = (SVGPathDataAttribute)other;
         data = attr.data;
         valueString = attr.valueString;
      }
   }

   @Override
   public String getSourceValue()
   {
      return valueString;
   }

   String name;
   private String data;
   SVGHandler handler;
   String valueString;

   private static final Pattern NUMERIC_PATTERN = 
     Pattern.compile("([\\s,]*)((?:[+\\-]?\\d*)(?:\\.?\\d+)(?:[eE][=\\-]?\\d+)?[a-zA-Z]*)([,\\s].*)?");
}
