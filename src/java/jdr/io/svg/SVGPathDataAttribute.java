package com.dickimawbooks.jdr.io.svg;

import java.text.*;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.regex.*;

import org.xml.sax.*;

import com.dickimawbooks.jdr.exceptions.*;

public class SVGPathDataAttribute implements SVGAttribute
{
   public SVGPathDataAttribute(String valueString)
   {
      if (valueString == null || valueString.equals("inherit"))
      {
         data = null;
      }
      else
      {
         data = valueString;
      }
   }

   public String getName()
   {
      return "d";
   }

   public Object getValue()
   {
      return data;
   }

   public Path2D getPath(SVGAbstractElement element)
     throws InvalidFormatException
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
                    throw new InvalidFormatException(
                      "Coordinate pairs required for '"+c+"' operation");
                 }

                 if (c == 'M')
                 {
                    path.moveTo(x.getBpValue(element, true), y.getBpValue(element, false));
                 }
                 else if (c == 'L')
                 {
                    path.lineTo(x.getBpValue(element, true), y.getBpValue(element, false));
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
                       path.moveTo(p.getX()+x.getBpValue(element, true),
                                   p.getY()+y.getBpValue(element, false));
                    }
                    else
                    {
                       path.lineTo(p.getX()+x.getBpValue(element, true),
                                   p.getY()+y.getBpValue(element, false));
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
                    path.lineTo(coord.getBpValue(element, true),
                                p.getY());
                 }
                 else if (c == 'h')
                 {
                    path.lineTo(p.getX()+coord.getBpValue(element, true),
                                p.getY());
                 }
                 else if (c == 'V')
                 {
                    path.lineTo(p.getX(),
                                coord.getBpValue(element, false));
                 }
                 else
                 {
                    path.lineTo(p.getX(),
                                p.getY()+coord.getBpValue(element, false));
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
                    throw new InvalidFormatException(
                      "Three coordinate pairs required for '"+c+"' operation");
                 }

                 double cx;
                 double cy;

                 if (c == 'C')
                 {
                    cx = x2.getBpValue(element, true);
                    cy = y2.getBpValue(element, false);

                    path.curveTo
                      (
                         x1.getBpValue(element, true),
                         y1.getBpValue(element, false),
                         cx, cy,
                         x.getBpValue(element, true),
                         y.getBpValue(element, false)
                      );
                 }
                 else
                 {
                    p = path.getCurrentPoint();

                    cx = p.getX()+x2.getBpValue(element, true);
                    cy = p.getY()+y2.getBpValue(element, false);

                    path.curveTo
                      (
                         p.getX()+x1.getBpValue(element, true),
                         p.getY()+y1.getBpValue(element, false),
                         cx, cy,
                         p.getX()+x.getBpValue(element, true),
                         p.getY()+y.getBpValue(element, false)
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
                    throw new InvalidFormatException(
                      "Two coordinate pairs required for '"+c+"' operation");
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
                    c2x = x2.getBpValue(element, true);
                    c2y = y2.getBpValue(element, false);

                    path.curveTo
                      (
                         cx, cy,
                         c2x, c2y,
                         x.getBpValue(element, true),
                         y.getBpValue(element, false)
                      );
                 }
                 else
                 {
                    c2x = p.getX()+x2.getBpValue(element, true);
                    c2y = p.getY()+y2.getBpValue(element, false);

                    path.curveTo
                      (
                         cx, cy,
                         c2x, c2y,
                         p.getX()+x.getBpValue(element, true),
                         p.getY()+y.getBpValue(element, false)
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
                    throw new InvalidFormatException(
                      "Two coordinate pairs required for '"+c+"' operation");
                 }

                 double cx;
                 double cy;

                 if (c == 'Q')
                 {
                    cx = x1.getBpValue(element, true);
                    cy = y1.getBpValue(element, false);

                    path.quadTo
                      (
                         cx, cy,
                         x.getBpValue(element, true),
                         y.getBpValue(element, false)
                      );
                 }
                 else
                 {
                    p = path.getCurrentPoint();

                    cx = p.getX()+x1.getBpValue(element, true);
                    cy = p.getY()+y1.getBpValue(element, false);

                    path.quadTo
                      (
                         cx, cy,
                         p.getX()+x.getBpValue(element, true),
                         p.getY()+y.getBpValue(element, false)
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
                    throw new InvalidFormatException(
                      "Coordinate pair required for '"+c+"' operation");
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
                         x.getBpValue(element, true),
                         y.getBpValue(element, false)
                      );
                 }
                 else
                 {
                    path.quadTo
                      (
                         cx, cy,
                         p.getX()+x.getBpValue(element, true),
                         p.getY()+y.getBpValue(element, false)
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
                    throw new InvalidFormatException(
                     "Path '"+c+"' command requires 7 parameters");
                 }

                 double x1 = x.getBpValue(element, true);
                 double y1 = y.getBpValue(element, false);

                 if (c == 'a')
                 {
                    Point2D p1 = path.getCurrentPoint();

                    x1 += p1.getX();
                    y1 += p1.getY();
                 }

                 arcTo(path, rx.getBpValue(element, true),
                       ry.getBpValue(element, false),
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
              throw new InvalidFormatException("Unknown path operator '"+c+"'");
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
     throws InvalidFormatException
   {
      int idx = iter.getIndex();

      Matcher m = pattern.matcher(text.substring(idx));

      SVGLengthAttribute coord = null;

      if (m.matches())
      {
         String group1 = m.group(1);
         String group2 = m.group(2);

         coord = new SVGLengthAttribute("coordinate", group2,
            isHorizontal);

         iter.setIndex(idx+group1.length()+group2.length());

      }

      return coord;
   }

   private SVGAngleAttribute getAngle(String text, CharacterIterator iter)
     throws InvalidFormatException
   {
      int idx = iter.getIndex();

      Matcher m = pattern.matcher(text.substring(idx));

      SVGAngleAttribute angle = null;

      if (m.matches())
      {
         String group1 = m.group(1);
         String group2 = m.group(2);

         angle = new SVGAngleAttribute(group2);

         iter.setIndex(idx+group1.length()+group2.length());

      }

      return angle;
   }

   private Boolean getBoolean(String text, CharacterIterator iter)
     throws InvalidFormatException
   {
      int idx = iter.getIndex();

      Matcher m = pattern.matcher(text.substring(idx));

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
               throw new NumberFormatException();
            }

            flag = new Boolean(val == 1);

            iter.setIndex(idx+group1.length()+group2.length());
         }
         catch (NumberFormatException e)
         {
            throw new InvalidFormatException(
              "boolean flag requires '0' or '1' value. Found: '"+group2+"'");
         }

      }

      return flag;
   }

   private boolean hasNextCoord(String text, CharacterIterator iter)
   {
      int idx = iter.getIndex();

      Matcher m = pattern.matcher(text.substring(idx));

      return m.matches();
   }

   public Object clone()
   {
      SVGPathDataAttribute attr = new SVGPathDataAttribute(null);

      attr.makeEqual(this);

      return attr;
   }

   public void makeEqual(SVGPathDataAttribute attr)
   {
      data = attr.data;
   }

   private String data;

   private static final Pattern pattern = 
     Pattern.compile("([\\s,]*)((?:[+\\-]?\\d*)(?:\\.?\\d+)(?:[eE][=\\-]?\\d+)?[a-zA-Z]*)([,\\s].*)?");
}
