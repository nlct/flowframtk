// File          : FlowFrame.java
// Purpose       : provides flowframe information for FlowframTk
// Creation Date : 5th June 2006
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2025 Nicola L.C. Talbot

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
import java.util.regex.Pattern;
import java.awt.*;
import java.util.*;
import java.lang.*;
import java.awt.geom.*;
import java.awt.font.*;
import javax.swing.*;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing <a target="_top" href="http://ctan.org/pkg/flowfram">flowframe</a> information.
 * @author Nicola L C Talbot
 */
public class FlowFrame implements Cloneable,Serializable
{
   /**
    * Constructs a frame of the given type. The type may be one of:
    * {@link #STATIC}, {@link #FLOW}, {@link #DYNAMIC} or 
    * {@link #TYPEBLOCK}. Note that the border, label and page list
    * properties are ignored if the frame type is 
    * {@link #TYPEBLOCK}. The margins are all initialised to 0 and
    * the shape is set to {@link #STANDARD}. The vertical alignment
    * is set to {@link #CENTER} if the frame type is {@link #STATIC},
    * otherwise it is set to {@link #TOP}.
    * @param frameType the type of frame
    * @param hasBorder flag to indicate whether or not this frame has
    * a border
    * @param idl the label to assign to this frame
    * @param pageList the list of pages for which this frame is defined
    */
   public FlowFrame(CanvasGraphics cg,
                    int frameType, boolean hasBorder, String idl,
                    String pageList)
   {
      setCanvasGraphics(cg);
      setType(frameType);
      border = hasBorder;
      label  = idl;
      pages  = pageList;
      top    = 0.0f;
      bottom = 0.0f;
      left   = 0.0f;
      right  = 0.0f;
      shape  = STANDARD;
      valign = (frameType == STATIC ? CENTER : TOP);
      if (label == "") label = ""+maxid;
      maxid++;
   }


   /**
    * Constructs a default frame of the given type. The frame is set
    * to have no border, a default label and to be shown on all pages.
    * @param frameType the type of frame
    * @see #FlowFrame(int,boolean,String,String)
    */
   public FlowFrame(CanvasGraphics cg, int frameType)
   {
      this(cg, frameType, false, "", "all");
   }

   private FlowFrame()
   {
   }

   /**
    * Create a copy.
    */ 
   public FlowFrame(FlowFrame flowframe)
   {
      setCanvasGraphics(flowframe.canvasGraphics);
      setType(flowframe.type);
      border = flowframe.border;
      label = flowframe.label;
      pages = flowframe.pages;
      top = flowframe.top;
      bottom = flowframe.bottom;
      left = flowframe.left;
      right = flowframe.right;
      shape = flowframe.shape;
      valign = flowframe.valign;
      contents = flowframe.contents;
      evenXShift = flowframe.evenXShift;
      evenYShift = flowframe.evenYShift;
   }

   /**
    * Makes this frame the same as another frame.
    * @param f the other frame
    */
   public void makeEqual(FlowFrame f)
   {
      type   = f.type;
      border = f.border;
      label  = f.label;
      pages  = f.pages;
      top    = f.top;
      bottom = f.bottom;
      left   = f.left;
      right  = f.right;
      shape  = f.shape;
      valign  = f.valign;
      evenXShift = f.evenXShift;
      evenYShift = f.evenYShift;
      contents  = f.contents;
      setCanvasGraphics(f.getCanvasGraphics());
   }

   /**
    * Returns a copy of this object.
    * @return a copy of this object
    */
   public Object clone()
   {
      return new FlowFrame(this);
   }

   /**
    * Determines if this object is equal to another object.
    * @param obj the other object
    * @return true if this object is consider equal to another object
    */
   public boolean equals(Object obj)
   {
      if (this == obj) return true;

      if (obj == null) return false;
      if (!(obj instanceof FlowFrame)) return false;

      FlowFrame f = (FlowFrame)obj;

      if (type != f.type) return false;
      if (!label.equals(f.label)) return false;
      if (border != f.border) return false;
      if (!pages.equals(f.pages)) return false;
      if (top != f.top) return false;
      if (bottom != f.bottom) return false;
      if (left != f.left) return false;
      if (right != f.right) return false;
      if (shape != f.shape) return false;
      if (evenXShift != f.evenXShift) return false;
      if (evenYShift != f.evenYShift) return false;

      if ((contents == null && f.contents != null)
        ||(contents != null && f.contents == null))
      {
         return false;
      }

      if (contents == f.contents) return true;

      return contents.equals(f.contents);
   }


   /**
    * Draws the labelled text area for the given bounding box
    * (specified in storage units). 
    * This is the area in which the text will be placed. A rectangle 
    * will always been drawn regardless of the {@link #shape} 
    * specification. The margins between the text area and the 
    * bounding box are given by {@link #top}, {@link #bottom}, 
    * {@link #left} and {@link #right}.
    * @param bbox bounding box
    */
   public void draw(BBox bbox)
   {
      CanvasGraphics cg = getCanvasGraphics();

      Graphics2D g2 = cg.getGraphics();

      if (g2 == null) return;

      AffineTransform orgTransform = g2.getTransform();

      AffineTransform resetTransform = cg.getResetTransform();

      if (resetTransform != null)
      {
         g2.setTransform(resetTransform);
      }

      double scaleX = cg.storageToComponentX(1.0);
      double scaleY = cg.storageToComponentY(1.0);

      double hoffset = 0.0;
      double voffset = 0.0;

      if (cg.isEvenPage())
      {
         hoffset = evenXShift;
         voffset = evenYShift;
      }

      double x = scaleX*(bbox.getMinX()+left+hoffset);
      double y = scaleY*(bbox.getMinY()+top+voffset);
      double width = scaleX*(bbox.getWidth()-(left+right));
      double height = scaleY*(bbox.getHeight()-(top+bottom));

      Rectangle2D rect = new Rectangle2D.Double(x, y,
         width, height);

      Stroke oldStroke = g2.getStroke();
      Font oldFont = g2.getFont();
      g2.setStroke(new BasicStroke());
      g2.setFont(JDRCompleteObject.annoteFont);

      g2.draw(rect);

      String str = getDisplayLabel();

      // why does this sometimes throw a NullPointerException
      // at AATextRenderer.java:41 ?
      try
      {
         g2.drawString(str, (int)x, (int)(y+height));
      }
      catch (NullPointerException e)
      {
         cg.getMessageSystem().getPublisher().publishMessages(
           MessageInfo.createVerbose(1, 
            "NullPointerException occurred while attempting to draw string '"
            +str+"' on graphics device "+g2),
           MessageInfo.createVerbose(1, e));
      }

      g2.setStroke(oldStroke);
      g2.setFont(oldFont);

      if (resetTransform != null)
      {
         g2.setTransform(orgTransform);
      }

   }

   /**
    * Gets the annotation text.
    * @return annotation text
    */
   public String getDisplayLabel()
   {
      switch (type)
      {
         case STATIC :
            return "static:"+label+":"+pages;
         case FLOW :
            return "flow:"+label+":"+pages;
         case DYNAMIC :
            return "dynamic:"+label+":"+pages;
         case TYPEBLOCK :
            return "typeblock";
      }

      // This shouldn't happen

      throw new IllegalArgumentException("Invalid frame type '"+type+"'");
   }

   /**
    * Gets the bounds of the annotation text.
    * @return annotation text bounds
    */
   public Rectangle2D getLabelBounds(BBox bbox)
   {
      FontRenderContext frc = new FontRenderContext(null,true,true);
      TextLayout layout = new TextLayout(getDisplayLabel(),
         JDRCompleteObject.annoteFont, frc);

      double x = bbox.getMinX()+left;
      double y = bbox.getMinY()+top;
      double height = bbox.getHeight()-(top+bottom);

      Rectangle2D bounds = layout.getBounds();

      bounds.setRect(bounds.getX()+x, bounds.getY()+y+height,
                     bounds.getWidth(), bounds.getHeight());

      return bounds;
   }

   /**
    * Writes the flowframe information in TeX format.
    * The flowfram package requires the frames to be positioned
    * relative to the typeblock while FlowframTk positions objects
    * relative to the top left corner of the canvas so the typeblock
    * is required to determine the correct co-ordinates.
    * @param object the object to which this frame belongs
    * @param typeblock the typeblock for the LaTeX document
    * @param out the output stream
    * @param baselineskip the value of \baselineskip for the LaTeX
    * document
    * @param unit the unit of measurement
    * @see #tex(JDRObject,Rectangle2D,String,double)
    * @throws IOException if I/O error occurs
    * @throws InvalidShapeException if frame has a nonstandard
    * shape but the required shape command can't reproduce the
    * required shape
    */
   public void tex(TeX pgf, JDRObject object, Rectangle2D typeblock,
                   double baselineskip, boolean useHPaddingShapepar)
      throws IOException,InvalidShapeException
   {
      tex(pgf, object, typeblock, 
           getCanvasGraphics().getMessageWithFallback
            ("tex.comment.border_command", "Border command for frame"),
          baselineskip, useHPaddingShapepar);
   }

   /**
    * Writes the flowframe information in TeX format.
    * The flowfram package requires the frames to be positioned
    * relative to the typeblock while FlowframTk positions objects
    * relative to the top left corner of the canvas so the typeblock
    * is required to determine the correct co-ordinates.
    * @param object the object to which this frame belongs
    * @param typeblock the typeblock for the LaTeX document
    * @param borderCommandComment comment for the definition of the border 
    * command
    * @param baselineskip the value of \baselineskip for the LaTeX
    * document (in terms of the storage unit)
    * @throws IOException if I/O error occurs
    * @throws InvalidShapeException if frame has a nonstandard
    * shape but the required shape command can't reproduce the
    * required shape
    */
   public void tex(TeX pgf, JDRObject object, Rectangle2D typeblock,
                   String borderCommandComment,
                   double baselineskip, boolean useHPaddingShapepar)
      throws IOException,InvalidShapeException
   {
      tex(pgf, object, typeblock, borderCommandComment, baselineskip,
          useHPaddingShapepar, pages, label, label);
   }

   public void tex(TeX pgf, JDRObject object, Rectangle2D typeblock,
                   String borderCommandComment,
                   double baselineskip, boolean useHPaddingShapepar,
                   String pageList, String objectLabel, String borderLabel)
      throws IOException,InvalidShapeException
   {
      String typeStr = "";

      switch (type)
      {
         case FLOW: typeStr = "flow"; break;
         case STATIC: typeStr = "static"; break;
         case DYNAMIC: typeStr = "dynamic"; break;
      }

      CanvasGraphics cg = getCanvasGraphics();

      BBox bbox = object.getStorageBBox();

      double bheight = bbox.getHeight();

      double x0 = bbox.getMinX()-typeblock.getX();
      double x = x0 + left;

      double y0 = typeblock.getHeight()-bheight
                - (bbox.getMinY()-typeblock.getY());
      double y = y0 + bottom;

      double width = bbox.getWidth()-left-right;
      double height = bheight-bottom-top;

      switch (type)
      {
         case TYPEBLOCK :
            pgf.println("\\geometry{lmargin="
              +pgf.length(cg, left)
            +",rmargin="
              +pgf.length(cg, right)
           +",tmargin="
              +pgf.length(cg, top)
           +",bmargin="
              +pgf.length(cg, bottom)+"}");

           if (evenXShift != 0.0)
           {
              pgf.println("\\setlength{\\evensidemargin}{\\oddsidemargin}");
              pgf.println("\\addtolength{\\evensidemargin}{"
               +pgf.length(cg, evenXShift)+"}");
           }

            return;
         case STATIC :
            pgf.print("\\newstaticframe");
         break;
         case FLOW :
            pgf.print("\\newflowframe");
         break;
         case DYNAMIC :
            if (pgf instanceof FLF)
            {
               FLF flf = (FLF)pgf;

               if (THUMBTAB_LABEL.matcher(objectLabel).matches())
               {
                  flf.foundThumbtab(object, height);
               }
            }

            pgf.print("\\newdynamicframe");
         break;
      }

      pgf.println("["+pageList+"]{"
         + pgf.length(cg, width)+"}{"
         + pgf.length(cg, height)+"}{"
         + pgf.length(cg, x)+"}{" 
         + pgf.length(cg, y)+"}["+objectLabel+"]");
      pgf.println();

      if (border)
      {
         if (objectLabel.equals(borderLabel))
         {
            pgf.comment(borderCommandComment+ " '"+objectLabel+"'");

            if (pgf.isFlowframTkStyUsed())
            {
               pgf.println("\\flowframtkNewFrameBorder{"+borderLabel+"}{%");
            }
            else
            {
               pgf.println("\\expandafter\\def\\csname @flf@border@"
                 +borderLabel+"\\endcsname#1{%");
            }

            pgf.println("\\begin{pgfpicture}{0pt"+"}{0pt"+"}{"
               +pgf.length(cg, bbox.getWidth())+"}{"
               +pgf.length(cg, bbox.getHeight())+"}");

            pgf.println("\\pgfputat{"
               + pgf.point(cg, -left, -bottom) +"}{%");

            AffineTransform af = new AffineTransform(1, 0, 0, -1,
               -bbox.getMinX(), bbox.getMaxY());

            pgf.setTransform(af);

            object.savePgf(pgf);

            pgf.setTransform(null);

            pgf.println("}");
            pgf.println("\\pgfputat{\\pgfpoint{0pt}{0pt}}{\\pgftext[left,bottom]{#1}}");
            pgf.println("\\end{pgfpicture}}");
         }

         switch (type)
         {
            case STATIC :
               pgf.println("\\setstaticframe*");
            break;
            case FLOW :
               pgf.println("\\setflowframe*");
            break;
            case DYNAMIC :
               pgf.println("\\setdynamicframe*");
            break;
         }

         pgf.print("{"+objectLabel +"}{offset=0pt,border={");

         if (pgf.isFlowframTkStyUsed())
         {
            pgf.print("\\flowframtkUseFrameBorderCsName{");
            pgf.print(borderLabel);
            pgf.print("}");
         }
         else
         {
            pgf.print("@flf@border@");
            pgf.print(borderLabel);
         }

         pgf.print("}");

         if (evenXShift != 0.0 || evenYShift != 0.0)
         {
           pgf.print(",evenx=" + pgf.length(cg, x+evenXShift)
           + ",eveny="+pgf.length(cg, y+evenYShift));
         }

         pgf.println("}");

         pgf.println();
      }
      else if (evenXShift != 0.0 || evenYShift != 0.0)
      { 
         switch (type)
         {
            case STATIC :
               pgf.println("\\setstaticframe*");
            break;
            case FLOW :
               pgf.println("\\setflowframe*");
            break;
            case DYNAMIC :
               pgf.println("\\setdynamicframe*");
            break;
         }

         pgf.println("{"+objectLabel
           +"}{evenx=" + pgf.length(cg, x+evenXShift)
           + ",eveny="+pgf.length(cg, y+evenYShift)
           +"}");
         pgf.println();
     }

     if (shape != STANDARD
       && (type == STATIC || type == DYNAMIC)
       && (object instanceof JDRPath))
      {
         JDRPath path = (JDRPath)object;
         Parshape parshape;

         if (shape == PARSHAPE)
         {
            parshape = path.parshape(baselineskip, false);
         }
         else
         {
            parshape = path.shapepar(useHPaddingShapepar, baselineskip, false);
         }

         String shapecmd = parshape.string;

         if (type == STATIC)
         {
            pgf.println("\\setstaticframe*{"+objectLabel+"}{shape={"+shapecmd+"}}");
         }
         else
         {
            pgf.println("\\setdynamicframe*{"+objectLabel+"}{shape={"+shapecmd+"}}");
         }
      }

      if (type == STATIC || type == DYNAMIC)
      {
         pgf.print("\\set"+typeStr +"frame*{"+objectLabel+"}{valign=");
         switch (valign)
         {
            case TOP :
               pgf.print("t");
            break;
            case CENTER :
               pgf.print("c");
            break;
            case BOTTOM :
               pgf.print("b");
            break;
         }

         if (clear)
         {
            pgf.print(",clear");
         }

         if (type == DYNAMIC && !styleCommands.isEmpty())
         {
            pgf.print(",style={");
            pgf.print(styleCommands);
            pgf.print("}");
         }

         pgf.println("}");

         if (contents != null && !contents.isEmpty())
         {
            pgf.println("\\set"+(type==STATIC?"static":"dynamic")
                      +"contents*{"+objectLabel+"}{"+contents+"}");
         }
      }
      else if (type == FLOW)
      {
         switch (marginPosition)
         {
            case MARGIN_OUTER:
              pgf.println("\\setflowframe*{"+objectLabel+"}{margin=outer}");
            break;
            case MARGIN_INNER:
              pgf.println("\\setflowframe*{"+objectLabel+"}{margin=inner}");
            break;
            case MARGIN_LEFT:
              pgf.println("\\setflowframe*{"+objectLabel+"}{margin=left}");
            break;
            case MARGIN_RIGHT:
              pgf.println("\\setflowframe*{"+objectLabel+"}{margin=right}");
            break;
         }
      }

      if (type != TYPEBLOCK && textColor != null)
      {
         float[] rgb = textColor.getRGBColorComponents(null);

         pgf.format("\\set%sframe*{%s}{textcolor=[rgb]{%f,%f,%f}}%n",
            typeStr, objectLabel, rgb[0], rgb[1], rgb[2]);
      }
   }

   /**
    * Saves the information for this frame in JDR format.
    * @throws IOException if I/O error occurs
    */
   public void save(JDRAJR jdr)
      throws IOException
   {
      float version = jdr.getVersion();

      jdr.writeByte((byte)type);

      if (type != TYPEBLOCK)
      {
         jdr.writeBoolean(border);
         jdr.writeString(label);
         jdr.writeString(pages);
      }

      if (version < 1.8f)
      {
         jdr.writeFloat((float)top);
         jdr.writeFloat((float)bottom);
         jdr.writeFloat((float)left);
         jdr.writeFloat((float)right);
      }
      else
      {
         jdr.writeDouble(top);
         jdr.writeDouble(bottom);
         jdr.writeDouble(left);
         jdr.writeDouble(right);
      }

      boolean omitted = false;

      if (version >= 1.2f)
      {
         if (type == STATIC || type == DYNAMIC)
         {
            jdr.writeByte((byte)shape);

            if (version >= 1.3f)
            {
               jdr.writeByte((byte)valign);

               if (version >= 1.8f)
               {
                  jdr.writeString(contents);

                  if (version >= 2.1f)
                  {
                     jdr.writeBoolean(clear);

                     if (clear) omitted = true;

                     if (type == DYNAMIC)
                     {
                        jdr.writeString(styleCommands);

                        if (!styleCommands.isEmpty()) omitted = true;
                     }
                  }
               }
               else if (!(contents != null || contents.isEmpty()))
               {
                  omitted = true;
               }
            }
            else if (valign != CENTER
                   ||!(contents != null || contents.isEmpty()))
            {
               omitted = true;
            }
         }
         else if (type == FLOW)
         {
            if (version >= 2.1f)
            {
               jdr.writeByte((byte)marginPosition);

               if (marginPosition != MARGIN_OUTER)
               {
                  omitted = true;
               }
            }
         }

         if (version >= 1.8f)
         {
            jdr.writeDouble(evenXShift);

            if (type != TYPEBLOCK)
            {
               jdr.writeDouble(evenYShift);

               if (version >= 2.1f)
               {
                  jdr.writeBoolean(textColor != null);

                  if (textColor != null)
                  {
                     jdr.writeInt(textColor.getRGB());
                  }
                  else
                  {
                     omitted = true;
                  }
               }
            }
         }
         else if (evenXShift != 0.0)
         {
            omitted = true;
         }
      }
      else
      {
         if (type == STATIC || type == DYNAMIC)
         {
            if (shape != STANDARD
             || valign != CENTER
             || !(contents == null || contents.isEmpty()))
            {
               omitted = true;
            }
         }

         if (evenXShift != 0.0)
         {
            omitted = true;
         }
      }

      if (omitted)
      {
         jdr.warningWithFallback(
           "warning.save_unsupported_flow_frame",
           "Flow frame data not supported by JDR/AJR version {0} has been omitted",
            version);
      }
   }

   /**
    * Reads frame information stored in JDR/AJR format.
    * @throws InvalidFormatException if data stored incorrectly
    * @return the frame defined by the given information
    */
   public static FlowFrame read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      int frameType = (int)jdr.readByte(
         InvalidFormatException.FRAME_TYPE, 0, TYPEBLOCK, true, true);

      boolean hasBorder=false;
      String idl = "";
      String pageList="all";
      double topMargin=0.0;
      double bottomMargin=0.0;
      double leftMargin=0.0;
      double rightMargin=0.0;

      if (frameType != TYPEBLOCK)
      {
         hasBorder = jdr.readBoolean(
           InvalidFormatException.FRAME_BORDER_FLAG);

         idl = jdr.readString(
           InvalidFormatException.FRAME_IDL);

         pageList = jdr.readString(
           InvalidFormatException.FRAME_PAGELIST);

      }

      if (version < 1.8f)
      {
         topMargin = jdr.readFloat(
            InvalidFormatException.FRAME_MARGIN_TOP);
         bottomMargin = jdr.readFloat(
            InvalidFormatException.FRAME_MARGIN_BOTTOM);
         leftMargin = jdr.readFloat(
            InvalidFormatException.FRAME_MARGIN_LEFT);
         rightMargin = jdr.readFloat(
            InvalidFormatException.FRAME_MARGIN_RIGHT);
      }
      else
      {
         topMargin = jdr.readDouble(
            InvalidFormatException.FRAME_MARGIN_TOP);
         bottomMargin = jdr.readDouble(
            InvalidFormatException.FRAME_MARGIN_BOTTOM);
         leftMargin = jdr.readDouble(
            InvalidFormatException.FRAME_MARGIN_LEFT);
         rightMargin = jdr.readDouble(
            InvalidFormatException.FRAME_MARGIN_RIGHT);
      }

      FlowFrame f = new FlowFrame(jdr.getCanvasGraphics(),
         frameType, hasBorder, idl, pageList);

      f.top = topMargin;
      f.bottom = bottomMargin;
      f.left = leftMargin;
      f.right = rightMargin;

      if (version >= 1.2f)
      {
         if (f.getType() == STATIC || f.getType() == DYNAMIC)
         {
            f.setShape((int)jdr.readByte(
               InvalidFormatException.FRAME_SHAPE));

            if (version >= 1.3f)
            {
               f.setVAlign((int)jdr.readByte(
                  InvalidFormatException.FRAME_VALIGN));

               if (version >= 1.8f)
               {
                  f.setContents(jdr.readString(
                     InvalidFormatException.FRAME_CONTENTS));

                  if (version >= 2.1f)
                  {
                     f.clear = jdr.readBoolean(
                      InvalidFormatException.FRAME_CLEAR);

                     if (f.type == DYNAMIC)
                     {
                        f.styleCommands = jdr.readString(
                          InvalidFormatException.FRAME_STYLE_COMMANDS);
                     }
                  }
               }
            }
         }
         else if (f.type == FLOW)
         {
            if (version >= 2.1f)
            {
               f.setMarginPosition(jdr.readByte(
                  InvalidFormatException.FRAME_MARGIN_POSITION));
            }
         }

         if (version >= 1.8f)
         {
            f.setEvenXShift(jdr.readDouble(
               InvalidFormatException.FRAME_EVEN_X_SHIFT));

            if (frameType != TYPEBLOCK)
            {
               f.setEvenYShift(jdr.readDouble(
                  InvalidFormatException.FRAME_EVEN_Y_SHIFT));

               if (version >= 2.1f)
               {
                  boolean hasTextCol = jdr.readBoolean(
                   InvalidFormatException.FRAME_TEXT_COLOUR_FLAG);

                  if (hasTextCol)
                  {
                     f.textColor = new Color(jdr.readInt(
                       InvalidFormatException.FRAME_TEXT_COLOUR));
                  }
               }
            }
         }
      }

      return f;
   } 


   /**
    * Gets the shape assigned to this frame.
    * The shape may be one of: {@link #STANDARD} (use standard
    * rectangular paragraphs), {@link #PARSHAPE} (use 
    * <code>\parshape</code> to define the paragraph shape) or
    * {@link #SHAPEPAR} (use <code>\shapepar</code> to define the
    * paragraph shape).
    * @return shape identifier
    * @see #setShape(int)
    */
   public int getShape()
   {
      return shape;
   }

   /**
    * Gets the type assigned to this frame.
    * The type may be one of: {@link #STATIC}, {@link #FLOW}, 
    * {@link #DYNAMIC} or {@link #TYPEBLOCK}.
    * @return frame type identifier
    * @see #setType(int)
    */
   public int getType()
   {
      return type;
   }

   /**
    * Sets this frame's type.
    * The type may be one of: {@link #STATIC}, {@link #FLOW}, 
    * {@link #DYNAMIC} or {@link #TYPEBLOCK}. Note that if the
    * type is neither {@link #STATIC} nor {@link #DYNAMIC}, the
    * shape is automatically set to {@link #STANDARD}, since only
    * static or dynamic frames may have a nonstandard shape.
    * @param frameType the type to set this frame
    * @throws JdrIllegalArgumentException if the specified type isn't
    * one of: {@link #STATIC}, {@link #FLOW}, 
    * {@link #DYNAMIC} or {@link #TYPEBLOCK}
    * @see #getType()
    */
   public void setType(int frameType)
   {
      if (frameType < 0 || frameType > TYPEBLOCK)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.FRAME_TYPE, frameType,
           getCanvasGraphics());
      }

      type = frameType;

      if (!(frameType == STATIC || frameType == DYNAMIC))
      {
         shape = STANDARD;
         contents = null;
      }
   }

   /**
    * Sets this frame's shape.
    * The shape may be one of: {@link #STANDARD} (use standard
    * rectangular paragraphs), {@link #PARSHAPE} (use 
    * <code>\parshape</code> to define the paragraph shape) or
    * {@link #SHAPEPAR} (use <code>\shapepar</code> to define the
    * paragraph shape). Note that only static or dynamic frames
    * may have nonstandard shapes.
    * @param frameShape the shape to use for this frame
    * @throws JdrIllegalArgumentException if the specified shape isn't
    * one of: {@link #STANDARD}, {@link #PARSHAPE} or {@link #SHAPEPAR} or if the frame type doesn't support the requested shape
    * @see #getShape()
    */
   public void setShape(int frameShape)
   {
      if (frameShape < 0 || frameShape > 2)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.FRAME_SHAPE, frameShape,
           getCanvasGraphics());
      }

      if (type == STATIC || type == DYNAMIC)
      {
         shape = frameShape;
      }
      else if (frameShape == STANDARD)
      {
         shape = STANDARD;
      }
      else
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.FRAME_SHAPE, frameShape,
           getCanvasGraphics());
      }
   }

   /**
    * Sets the vertical alignment for this frame.
    * The alignment may be one of: {@link #TOP}, {@link #CENTER} or
    * {@link #BOTTOM}. Note that this setting is only available for
    * static or dynamic frames.
    * @param align the vertical alignment for this frame
    * @throws JdrIllegalArgumentException if the specified alignment isn't
    * one of: {@link #TOP}, {@link #CENTER} or {@link #BOTTOM} or if
    * this frame's type doesn't support vertical alignment
    * @see #getVAlign()
    */
   public void setVAlign(int align)
   {
      if (align < 0 || align > 2)
      {
         throw new JdrIllegalArgumentException(
           JdrIllegalArgumentException.FRAME_VALIGN, align,
           getCanvasGraphics());
      }

      valign = align;
   }

   /**
    * Gets the vertical alignment for this frame.
    * @return the vertical alignment for this frame
    * @see #setVAlign(int)
    */
   public int getVAlign()
   {
      return valign;
   }

   public void setContents(String text)
   {
      if (type == STATIC || type == DYNAMIC)
      {
         contents = text;
         return;
      }

      if (text == null || text.isEmpty())
      {
         contents = null;
         return;
      }

      throw new JdrIllegalArgumentException(
        JdrIllegalArgumentException.FRAME_CONTENTS_TYPE, type,
        getCanvasGraphics());
   }

   public String getContents()
   {
      return contents;
   }

   public void setEvenXShift(double shift)
   {
      evenXShift = shift;
   }

   public double getEvenXShift()
   {
      return evenXShift;
   }

   public void setEvenYShift(double shift)
   {
      evenYShift = shift;
   }

   public double getEvenYShift()
   {
      return evenYShift;
   }

   /**
    * Determines whether this frame is defined on all even pages.
    * @return true if this frame's page list is either "all" or "even"
    * otherwise false
    */
   public boolean isDefinedOnEvenPages()
   {
      if (pages.equals("all") || pages.equals("even"))
      {
         return true;
      }

      return false;
   }

   /**
    * Determines whether this frame is defined on all odd pages.
    * @return true if this frame's page list is either "all" or "odd"
    * otherwise false
    */
   public boolean isDefinedOnOddPages()
   {
      if (pages.equals("all") || pages.equals("odd"))
      {
         return true;
      }

      return false;
   }

   /**
    * Determines whether this frame is defined on the given page.
    * @param page the specified page number
    * @return true if this frame's page list includes the specified
    * page otherwise false
    */
   public boolean isDefinedOnPage(int page)
   {
      if (pages.equals("none"))
      {
         return (page == 0);
      }

      if (page <= 0)
      {
         return false;
      }

      if (pages.equals("all"))
      {
         return true;
      }

      boolean isEven = (page%2==0 ? true: false);

      if (pages.equals("odd"))
      {
         return !isEven;
      }

      if (pages.equals("even"))
      {
         return isEven;
      }

      StringTokenizer st = new StringTokenizer(pages, ",");

      while (st.hasMoreTokens())
      {
         String token = st.nextToken();

         int idx=-1;
         int n = token.length();
         if (n == 0) return false;

         if ((idx=token.indexOf('<')) != -1)
         {
            if (idx == n-1) return false;
            String subStr = token.substring(idx+1);
            try
            {
               int i = Integer.parseInt(subStr);

               if (page < i) return true;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
         }
         else if ((idx=token.indexOf('>')) != -1)
         {
            if (idx == n-1) return false;
            String subStr = token.substring(idx+1);
            try
            {
               int i = Integer.parseInt(subStr);

               if (page > i) return true;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
         }
         else if ((idx=token.indexOf('-')) != -1)
         {
            if (idx == n-1 || idx == 0) return false;

            String first = token.substring(0, idx);
            String last  = token.substring(idx+1);

            try
            {
               int firstNum = Integer.parseInt(first);
               int lastNum = Integer.parseInt(last);

               if (page >= firstNum && page <= lastNum) return true;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
         }
         else
         {
            try
            {
               int i = Integer.parseInt(token);
               if (i == page) return true;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
         }
      }
      return false;
   }

   /**
    * Determines if the given page list is in a valid format.
    * @param pageList the page list to test
    * @return true if the given page list is valid otherwise false
    */
   public static boolean isValidPageList(String pageList)
   {
      if (pageList.equals("all") || pageList.equals("odd")
        || pageList.equals("even") || pageList.equals("none"))
      {
         return true;
      }

      StringTokenizer st = new StringTokenizer(pageList, ",");

      while (st.hasMoreTokens())
      {
         String token = st.nextToken();

         int idx=-1;
         int n = token.length();
         if (n == 0) return false;

         if ((idx=token.indexOf('<')) != -1)
         {
            if (idx == n-1) return false;
            String subStr = token.substring(idx+1);
            try
            {
               int i = Integer.parseInt(subStr);
            }
            catch (NumberFormatException e)
            {
                return false;
            }
         }
         else if ((idx=token.indexOf('>')) != -1)
         {
            if (idx == n-1) return false;
            String subStr = token.substring(idx+1);
            try
            {
               int i = Integer.parseInt(subStr);
            }
            catch (NumberFormatException e)
            {
                return false;
            }
         }
         else if ((idx=token.indexOf('-')) != -1)
         {
            if (idx == n-1 || idx == 0) return false;

            String first = token.substring(0, idx);
            String last  = token.substring(idx+1);

            try
            {
               int firstNum = Integer.parseInt(first);
               int lastNum = Integer.parseInt(last);
            }
            catch (NumberFormatException e)
            {
                return false;
            }
         }
         else
         {
            try
            {
               int i = Integer.parseInt(token);
            }
            catch (NumberFormatException e)
            {
                return false;
            }
         }
      }

      return true;
   }

   public String toString()
   {
      return "FlowFrame["
        + "label="+label
        + ",border="+border
        + ",type="+type
        + ",pages="+pages
        + ",top="+top
        + ",bottom="+bottom
        + ",left="+left
        + ",right="+right
        + ",shape="+shape
        + ",valign="+valign
        + ",evenXShift="+evenXShift
        + ",evenYShift="+evenYShift
        + ",contents="+contents
        + "]";
   }

   public void setCanvasGraphics(CanvasGraphics cg)
   {
      canvasGraphics = cg;
   }

   public CanvasGraphics getCanvasGraphics()
   {
      return canvasGraphics;
   }

   public void applyCanvasGraphics(CanvasGraphics cg)
   {
      if (canvasGraphics == cg)
      {
         return;
      }

      if (canvasGraphics.getStorageUnitID() == cg.getStorageUnitID())
      {
         setCanvasGraphics(cg);
         return;
      }

      JDRUnit oldUnit = canvasGraphics.getStorageUnit();
      JDRUnit newUnit = cg.getStorageUnit();

      double factor = oldUnit.toUnit(1.0, newUnit);

      top       *= factor;
      bottom    *= factor;
      left      *= factor;
      right     *= factor;
      evenXShift *= factor;

      setCanvasGraphics(cg);
   }

   public Rectangle2D getBounds2D(double storageWidth, double storageHeight)
   {
      return new Rectangle2D.Double(left, top,
        storageWidth - right - left,
        storageHeight - bottom - top);
   }

   public String getLabel()
   {
      return label;
   }

   public synchronized void setLabel(String newLabel)
   {
      label = newLabel;
   }

   public String getPages()
   {
      return pages;
   }

   public synchronized void setPages(String pagelist)
   {
      pages = pagelist;
   }

   public double getTop()
   {
      return top;
   }

   public synchronized void setTop(double margin)
   {
      top = margin;
   }

   public double getBottom()
   {
      return bottom;
   }

   public synchronized void setBottom(double margin)
   {
      bottom = margin;
   }

   public double getLeft()
   {
      return left;
   }

   public synchronized void setLeft(double margin)
   {
      left = margin;
   }

   public double getRight()
   {
      return right;
   }

   public synchronized void setRight(double margin)
   {
      right = margin;
   }

   public void setMarginPosition(int setting) throws InvalidFormatException
   {
      if (type == FLOW)
      {
         switch (setting)
         {
            case MARGIN_OUTER:
            case MARGIN_INNER:
            case MARGIN_LEFT:
            case MARGIN_RIGHT:
             marginPosition = setting;
            break;
            default:
              throw new InvalidValueException(
                InvalidFormatException.FRAME_MARGIN_POSITION,
                setting, canvasGraphics);
         }
      }
      else
      {
         throw new InvalidValueException(
           InvalidFormatException.FRAME_MARGIN_POSITION_TYPE,
           setting, canvasGraphics);
      }
   }

   public int getMarginPosition()
   {
      return marginPosition;
   }

   /**
    * Indicates that a frame is a static frame.
    */
   public static final int STATIC=0;
   /**
    * Indicates that a frame is a flow frame.
    */
   public static final int FLOW=1;
   /**
    * Indicates that a frame is a dynamic frame.
    */
   public static final int DYNAMIC=2;
   /**
    * Indicates that a frame represents the typeblock.
    */
   public static final int TYPEBLOCK=3;

   /**
    * Indicates that a frame should use a standard paragraph shape.
    */
   public static final int STANDARD=0;
   /**
    * Indicates that a frame should use <code>\parshape</code>
    * to define the paragraph shape. Only available for static or
    * dynamic frames.
    */
   public static final int PARSHAPE=1;
   /**
    * Indicates that a frame should use <code>\shapepar</code>
    * to define the paragraph shape. Only available for static or
    * dynamic frames.
    */
   public static final int SHAPEPAR=2;

   /**
    * Indicates that a frame should be aligned along the top.
    */
   public static final int TOP=0;
   /**
    * Indicates that a frame should be aligned along the middle.
    */
   public static final int CENTER=1;
   /**
    * Indicates that a frame should be aligned along the bottom.
    */
   public static final int BOTTOM=2;

   /**
    * Indicates that a flow frame should have an outer margin.
    */
   public static final int MARGIN_OUTER=0;

   /**
    * Indicates that a flow frame should have an inner margin.
    */
   public static final int MARGIN_INNER=1;

   /**
    * Indicates that a flow frame should have a left margin.
    */
   public static final int MARGIN_LEFT=2;

   /**
    * Indicates that a flow frame should have a right margin.
    */
   public static final int MARGIN_RIGHT=3;

   private static int maxid=0;

   private CanvasGraphics canvasGraphics;

   /**
    * Indicates whether this frame has a border.
    */
   public boolean border=false;

   /**
    * Indicates whether this frame should be clear by the output
    * routine (only applicable to static or dynamic frames).
    */
   private boolean clear=false;

   /**
    * Style commands (dynamic only)
    */
   private String styleCommands = "";

   private Color textColor = null;

   /**
    * The type assigned to this frame.
    */
   private volatile int type;

   private int marginPosition = MARGIN_OUTER;// only applicable for flow frames

   /**
    * The label identifying this frame.
    */
   protected volatile String label;

   /**
    * Indicates the pages on which this frame is defined.
    */
   protected volatile String pages;

   /**
    * This frame's top margin.
    */
   protected volatile double top;
   /**
    * This frame's bottom margin.
    */
   protected volatile double bottom;
   /**
    * This frame's left margin.
    */
   protected volatile double left;
   /**
    * This frame's right margin.
    */
   protected volatile double right;

   private volatile double evenXShift = 0.0, evenYShift = 0.0;

   private String contents = null;

   private int shape=STANDARD;
   private int valign=CENTER;

   public static final Pattern THUMBTAB_LABEL
     = Pattern.compile("(even)?thumbtab(index)?\\d+");
}
