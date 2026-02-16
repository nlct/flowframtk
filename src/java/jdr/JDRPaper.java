// File          : JDRPaper.java
// Description   : provides paper sizes
// Creation Date : 18th March 2006
// Author        : Nicola L. C. Talbot
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
import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.MediaPrintableArea;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.print.PageFormat;

import com.dickimawbooks.jdr.io.*;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing paper sizes.
 * @author Nicola L C Talbot
 */

public class JDRPaper
{
   /**
    * Creates a new paper size. The orientation is determined by
    * the dimensions. If the paper width is less than the paper
    * height, the paper is considered to be portrait, otherwise
    * it is considered to be landscape.
    * @param w the paper width
    * @param h the paper height
    */
   public JDRPaper(JDRMessageDictionary msgSys, double w, double h)
   {
      if (w < 0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.PAPER_WIDTH, w, msgSys);
      }

      if (h < 0)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.PAPER_HEIGHT, h, msgSys);
      }

      id_ = ID_USER;

      portrait = (w < h);

      width_ = (portrait ? w : h);
      height_ = (portrait ? h : w);

      double marginX = width_ < 180 ? 0 : 54;
      double marginY = height_ < 180 ? 0 : 54;

      imageableArea = new Rectangle2D.Double(marginX,
        marginY, width_-marginX*2, height_-marginY*2);
   }

   private JDRPaper(double w, double h, byte id)
   {
      if (w < 0)
      {
         throw new IllegalArgumentException(
            String.format("Invalid paper width '%f'", w));
      }

      if (h < 0)
      {
         throw new IllegalArgumentException(
            String.format("Invalid paper height '%f'", h));
      }

      if (id < 0 || id > MAX_ID_1_3)
      {
         throw new IllegalArgumentException(
            String.format("Invalid paper id '%d'", id));
      }

      portrait = (w < h);

      width_ = (portrait ? w : h);
      height_ = (portrait ? h : w);
      id_ = id;

      double marginX = width_ < 180 ? 0 : 54;
      double marginY = height_ < 180 ? 0 : 54;

      imageableArea = new Rectangle2D.Double(marginX,
        marginY, width_-marginX*2, height_-marginY*2);
   }

   public JDRPaper(JDRLength w, JDRLength h)
   {
      this(w.getMessageSystem(), 
           w.getValue(JDRUnit.bp),
           h.getValue(JDRUnit.bp));
   }

   public Rectangle2D getImageableArea()
   {
      return imageableArea;
   }

   public void setImageableArea(double marginX, double marginY,
     double imageableWidth, double imageableHeight)
   {
      imageableArea.setRect(marginX, marginY,
         imageableWidth, imageableHeight);
   }

   public void setImageableArea(PageFormat pf)
   {
      setImageableArea(pf.getImageableX(), pf.getImageableY(),
        pf.getImageableWidth(), pf.getImageableHeight());
   }

   public MediaPrintableArea getMediaPrintableArea()
   {
      return new MediaPrintableArea(
       (float)(imageableArea.getX()/72),
       (float)(imageableArea.getY()/72),
       (float)(imageableArea.getWidth()/72),
       (float)(imageableArea.getHeight()/72),
       MediaPrintableArea.INCH);
   }

   /**
    * Gets the width of this paper.
    * @return the paper width
    */
   public double getWidth()
   {
      return (portrait ? width_ : height_);
   }

   /**
    * Gets the height of this paper.
    * @return the paper height
    */
   public double getHeight()
   {
      return (portrait ? height_ : width_);
   }

   /**
    * Determines if this paper is portrait.
    * @return true if this paper orientation is portrait
    */
   public boolean isPortrait()
   {
      return portrait;
   }

   /**
    * Gets a new paper with the same dimensions as this but 
    * orientation as specified. If the specified orientation is
    * the same as the orientation for this paper, this paper is
    * returned.
    * @param orientation true if new paper should be portrait
    * @return new paper with given orientation or this paper if
    * orientation is the same
    */
   public JDRPaper getPaper(boolean orientation)
   {
      if (portrait == orientation)
      {
         return this;
      }
      else if (id_ == ID_USER)
      {
         return new JDRPaper(getHeight(), getWidth(), id_);
      }
      else
      {
         JDRPaper paper = getPredefinedPaper(getHeight(), getWidth());

         if (paper != null) return paper;
      }

      return new JDRPaper(getHeight(), getWidth(), ID_USER);
   }

   /**
    * Gets the LaTeX options required by the geometry package to
    * specify this paper size.
    * @return geometry package options to set this paper size
    */
   public String tex(CanvasGraphics cg)
   {
      if (this == A0)
      {
         return "a0paper,portrait";
      }
      else if (this == A0R)
      {
         return "a0paper,landscape";
      }
      else if (this == A1)
      {
         return "a1paper,portrait";
      }
      else if (this == A1R)
      {
         return "a1paper,landscape";
      }
      else if (this == A2)
      {
         return "a2paper,portrait";
      }
      else if (this == A2R)
      {
         return "a2paper,landscape";
      }
      else if (this == A3)
      {
         return "a3paper,portrait";
      }
      else if (this == A3R)
      {
         return "a3paper,landscape";
      }
      else if (this == A4)
      {
         return "a4paper,portrait";
      }
      else if (this == A4R)
      {
         return "a4paper,landscape";
      }
      else if (this == A5)
      {
         return "a5paper,portrait";
      }
      else if (this == A5R)
      {
         return "a5paper,landscape";
      }
      else if (this == A6)
      {
         return "a6paper,portrait";
      }
      else if (this == A6R)
      {
         return "a6paper,landscape";
      }
      else if (this == B0)
      {
         return "b0paper,portrait";
      }
      else if (this == B0R)
      {
         return "b0paper,landscape";
      }
      else if (this == B1)
      {
         return "b1paper,portrait";
      }
      else if (this == B1R)
      {
         return "b1paper,landscape";
      }
      else if (this == B2)
      {
         return "b2paper,portrait";
      }
      else if (this == B2R)
      {
         return "b2paper,landscape";
      }
      else if (this == B3)
      {
         return "b3paper,portrait";
      }
      else if (this == B3R)
      {
         return "b3paper,landscape";
      }
      else if (this == B4)
      {
         return "b4paper,portrait";
      }
      else if (this == B4R)
      {
         return "b4paper,landscape";
      }
      else if (this == B5)
      {
         return "b5paper,portrait";
      }
      else if (this == B5R)
      {
         return "b5paper,landscape";
      }
      else if (this == B6)
      {
         return "b6paper,portrait";
      }
      else if (this == B6R)
      {
         return "b6paper,landscape";
      }
      else if (this == LETTER)
      {
         return "letter,portrait";
      }
      else if (this == LETTERR)
      {
         return "letter,landscape";
      }
      else if (this == LEGAL)
      {
         return "legal,portrait";
      }
      else if (this == LEGALR)
      {
         return "legal,landscape";
      }
      else if (this == EXECUTIVE)
      {
         return "executive,portrait";
      }
      else if (this == EXECUTIVER)
      {
         return "executive,landscape";
      }

      double bpToStorage = cg.bpToStorage(1.0);

      return "paperwidth="
              + PGF.length(cg, getWidth()*bpToStorage)
            +",paperheight="
              + PGF.length(cg, getHeight()*bpToStorage);
   }

   /**
    * Gets the media size name for this paper.
    * @return media size name
    */
   public MediaSizeName getMediaSizeName()
   {
      if (this == A0)
      {
         return MediaSizeName.ISO_A0;
      }
      else if (this == A1)
      {
         return MediaSizeName.ISO_A1;
      }
      else if (this == A2)
      {
         return MediaSizeName.ISO_A2;
      }
      else if (this == A3)
      {
         return MediaSizeName.ISO_A3;
      }
      else if (this == A4)
      {
         return MediaSizeName.ISO_A4;
      }
      else if (this == A5)
      {
         return MediaSizeName.ISO_A5;
      }
      else if (this == A6)
      {
         return MediaSizeName.ISO_A6;
      }
      else if (this == A7)
      {
         return MediaSizeName.ISO_A7;
      }
      else if (this == A8)
      {
         return MediaSizeName.ISO_A8;
      }
      else if (this == A9)
      {
         return MediaSizeName.ISO_A9;
      }
      else if (this == A10)
      {
         return MediaSizeName.ISO_A10;
      }
      else if (this == LETTER)
      {
         return MediaSizeName.NA_LETTER;
      }
      else if (this == LEGAL)
      {
         return MediaSizeName.NA_LEGAL;
      }
      else if (this == EXECUTIVE)
      {
         return MediaSizeName.EXECUTIVE;
      }
      else if (this == B0)
      {
         return MediaSizeName.ISO_B0;
      }
      else if (this == B1)
      {
         return MediaSizeName.ISO_B1;
      }
      else if (this == B2)
      {
         return MediaSizeName.ISO_B2;
      }
      else if (this == B3)
      {
         return MediaSizeName.ISO_B3;
      }
      else if (this == B4)
      {
         return MediaSizeName.ISO_B4;
      }
      else if (this == B5)
      {
         return MediaSizeName.ISO_B5;
      }
      else if (this == B6)
      {
         return MediaSizeName.ISO_B6;
      }
      else if (this == B7)
      {
         return MediaSizeName.ISO_B7;
      }
      else if (this == B8)
      {
         return MediaSizeName.ISO_B8;
      }
      else if (this == B9)
      {
         return MediaSizeName.ISO_B9;
      }
      else if (this == B10)
      {
         return MediaSizeName.ISO_B10;
      }
      else if (this == C0)
      {
         return MediaSizeName.ISO_C0;
      }
      else if (this == C1)
      {
         return MediaSizeName.ISO_C1;
      }
      else if (this == C2)
      {
         return MediaSizeName.ISO_C2;
      }
      else if (this == C3)
      {
         return MediaSizeName.ISO_C3;
      }
      else if (this == C4)
      {
         return MediaSizeName.ISO_C4;
      }
      else if (this == C5)
      {
         return MediaSizeName.ISO_C5;
      }
      else if (this == C6)
      {
         return MediaSizeName.ISO_C6;
      }

      JDRUnit unit = JDRUnit.in;
      return 
          MediaSize.findMedia((float)unit.fromBp(width_),
                              (float)unit.fromBp(height_),
                              Size2DSyntax.INCH);
   }

   /**
    * Gets the ID associated with this paper size.
    * @return ID associated with this paper size
    */
   public byte getID()
   {
      return id_;
   }

   /**
    * Gets the name associated with this paper. If this paper is
    * non-standard, returns "user".
    * @return name associated with this paper
    */
   public String getName()
   {
      return name[id_];
   }

   public String toString()
   {
      if (id_ == ID_USER)
      {
         return ""+getWidth()+"bp x "+getHeight()+"bp"
            + (portrait ? " portrait" : " landscape");
      }

      return name[id_];
   }

   public String getName(JDRMessageDictionary msgSys, JDRUnit unit,
      String portraitMsgTag, String landscapeMsgTag,
      String userMsgTag)
   {
      String paperName;
 
      if (id_ == ID_USER)
      {
         paperName = msgSys.getMessageWithFallback(
           userMsgTag, "{0}{3} x {1}{3}",
           unit.fromBp(getWidth()),
           unit.fromBp(getHeight()),
           unit.getLabel());
      }
      else
      {
         paperName = msgSys.getMessageWithFallback(
             "paper."+tag[id_], tag[id_].toUpperCase());

         if (portrait)
         {
            paperName = msgSys.getMessageWithFallback(
              portraitMsgTag, "{0} portrait", paperName);
         }
         else
         {
            paperName = msgSys.getMessageWithFallback(
             landscapeMsgTag, "{0} landscape", paperName);
         }
      }

      return paperName;
   }

   /**
    * Gets predefined paper that has the given dimensions.
    * @param w the paper width
    * @param h the paper height
    * @return the matching predefined paper size or null if
    * no match is found
    * @see #getClosestPredefinedPaper(double,double,float)
    */
   public static JDRPaper getPredefinedPaper(double w, double h)
   {
      for (int i = 0; i < MAX_ID_1_3; i++)
      {
         JDRPaper paper = getPredefinedPaper(i);

         if (paper == null) continue;

         if (paper.getWidth() == w && paper.getHeight() == h)
         {
            return paper;
         }
      }

      return null;
   }

   /**
    * Gets the predefined paper size associated with the given
    * identifying name.
    * @param idName the identifying name
    * @return predefined paper size identified by idName or null
    * if none found
    */
   public static JDRPaper getPredefinedPaper(String idName)
   {
      for (int i = 0; i < name.length; i++)
      {
         if (idName.equals(name[i]))
         {
            return getPredefinedPaper((byte)i);
         }
      }

      return null;
   }

   /**
    * Gets predefined paper identified by given ID.
    * @param id the numerical identifier
    * @return predefined paper identified by given ID or null
    * if not found
    * @see #getPredefinedPaper(byte)
    */
   public static JDRPaper getPredefinedPaper(int id)
   {
      return getPredefinedPaper((byte)id);
   }

   /**
    * Gets predefined paper identified by given ID.
    * @param id the numerical identifier
    * @return predefined paper identified by given ID or null
    * if not found
    * @see #getPredefinedPaper(int)
    */
   public static JDRPaper getPredefinedPaper(byte id)
   {
      switch (id)
      {
         case ID_A0 : return A0;
         case ID_A1 : return A1;
         case ID_A2 : return A2;
         case ID_A3 : return A3;
         case ID_A4 : return A4;
         case ID_A5 : return A5;
         case ID_A6 : return A6;
         case ID_A7 : return A7;
         case ID_A8 : return A8;
         case ID_A9 : return A9;
         case ID_A10 : return A10;
         case ID_B0 : return B0;
         case ID_B1 : return B1;
         case ID_B2 : return B2;
         case ID_B3 : return B3;
         case ID_B4 : return B4;
         case ID_B5 : return B5;
         case ID_B6 : return B6;
         case ID_B7 : return B7;
         case ID_B8 : return B8;
         case ID_B9 : return B9;
         case ID_B10 : return B10;
         case ID_C0 : return C0;
         case ID_C1 : return C1;
         case ID_C2 : return C2;
         case ID_C3 : return C3;
         case ID_C4 : return C4;
         case ID_C5 : return C5;
         case ID_C6 : return C6;
         case ID_C7 : return C7;
         case ID_C8 : return C8;
         case ID_C9 : return C9;
         case ID_C10 : return C10;
         case ID_LETTER : return LETTER;
         case ID_LEGAL : return LEGAL;
         case ID_EXECUTIVE : return EXECUTIVE;
         case ID_A0R : return A0R;
         case ID_A1R : return A1R;
         case ID_A2R : return A2R;
         case ID_A3R : return A3R;
         case ID_A4R : return A4R;
         case ID_A5R : return A5R;
         case ID_A6R : return A6R;
         case ID_A7R : return A7R;
         case ID_A8R : return A8R;
         case ID_A9R : return A9R;
         case ID_A10R : return A10R;
         case ID_B0R : return B0R;
         case ID_B1R : return B1R;
         case ID_B2R : return B2R;
         case ID_B3R : return B3R;
         case ID_B4R : return B4R;
         case ID_B5R : return B5R;
         case ID_B6R : return B6R;
         case ID_B7R : return B7R;
         case ID_B8R : return B8R;
         case ID_B9R : return B9R;
         case ID_B10R : return B10R;
         case ID_C0R : return C0R;
         case ID_C1R : return C1R;
         case ID_C2R : return C2R;
         case ID_C3R : return C3R;
         case ID_C4R : return C4R;
         case ID_C5R : return C5R;
         case ID_C6R : return C6R;
         case ID_C7R : return C7R;
         case ID_C8R : return C8R;
         case ID_C9R : return C9R;
         case ID_C10R : return C10R;
         case ID_LETTERR : return LETTERR;
         case ID_LEGALR : return LEGALR;
         case ID_EXECUTIVER : return EXECUTIVER;
      }

      return null;
   }

   /**
    * Gets the predefined paper that is closest to the given
    * paper dimensions.
    * @param width the paper width
    * @param height the paper height
    * @param version the JDR version
    * @return the closest matching predefined paper
    * @see #getPredefinedPaper(double,double)
    * @see #getClosestEnclosingPredefinedPaper(double,double,float)
    */
   public static JDRPaper getClosestPredefinedPaper(
      double width, double height, float version)
   {
      JDRPaper match = getPredefinedPaper(0);
      double dx = width-match.getWidth();
      double dy = height-match.getHeight();
      double distance = dx*dx + dy*dy;
      double minDistance = distance;

      int maxID = (version < 1.3f ? MAX_ID_1_0 : MAX_ID_1_3);

      for (int i = 1; i <= maxID; i++)
      {
         if (i != ID_USER)
         {
            JDRPaper paper = getPredefinedPaper(i);
            dx = width-paper.getWidth();
            dy = height-paper.getHeight();
            distance = dx*dx + dy*dy;

            if (distance < minDistance)
            {
               match = paper;
               minDistance = distance;
            }
         }
      }

      return match;
   }

   /**
    * Gets the predefined paper that is the closest to the given
    * paper dimensions that completely encompasses the given
    * dimensions.
    * @param width the paper width
    * @param height the paper height
    * @param version the JDR version
    * @return the closest matching encompassing predefined paper or null if
    * none found
    * @see #getClosestPredefinedPaper(double,double,float)
    */
   public static JDRPaper getClosestEnclosingPredefinedPaper(
      double width, double height, float version)
   {
      JDRPaper match = null;
      double dx = 0;
      double dy = 0;
      double distance = 0;
      double minDistance = java.lang.Double.MAX_VALUE;

      int maxID = (version < 1.3f ? MAX_ID_1_0 : MAX_ID_1_3);

      for (int i = 0; i <= maxID; i++)
      {
         if (i != ID_USER)
         {
            JDRPaper paper = getPredefinedPaper(i);
            dx = paper.getWidth()-width;
            dy = paper.getHeight()-height;
            distance = dx*dx + dy*dy;

            if (distance < minDistance && dx >= 0 && dy >= 0)
            {
               match = paper;
               minDistance = distance;
            }
         }
      }

      return match;
   }

   public static JDRPaper getClosestEnclosingPredefinedPaper(
      double width, double height, byte... paperIDs)
   {
      JDRPaper match = null;
      double dx = 0;
      double dy = 0;
      double distance = 0;
      double minDistance = java.lang.Double.MAX_VALUE;

      for (byte i : paperIDs)
      {
         if (i != ID_USER)
         {
            JDRPaper paper = getPredefinedPaper(i);
            dx = paper.getWidth()-width;
            dy = paper.getHeight()-height;
            distance = dx*dx + dy*dy;

            if (distance < minDistance && dx >= 0 && dy >= 0)
            {
               match = paper;
               minDistance = distance;
            }
         }
      }

      return match;
   }


   /**
    * Saves this paper size in JDR/AJR format.
    * @throws IOException if I/O error occurs
    */
   public void save(JDRAJR jdr)
      throws IOException
   {
      float version = jdr.getVersion();

      if (version < 1.3f)
      {
         JDRPaper paper = this;

         if (paper.getID() > MAX_ID_1_0)
         {
            paper = new JDRPaper(getWidth(),getHeight(), ID_USER);
         }

         jdr.writeByte(paper.getID());

         if (paper.getID() == ID_USER)
         {
            jdr.writeDouble(paper.getWidth());
            jdr.writeDouble(paper.getHeight());
            jdr.writeBoolean(paper.portrait);
         }
      }
      else
      {
         jdr.writeByte(getID());

         if (getID() == ID_USER)
         {
            jdr.writeDouble(getWidth());
            jdr.writeDouble(getHeight());
         }
      }
   }

   /**
    * Reads paper size from JDR/AJR input stream.
    * @throws InvalidFormatException if the data is incorrectly
    * formatted
    * @return paper described by the input stream
    */
   public static JDRPaper read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      byte id = -1;

      int max_id = (version < 1.3f ? MAX_ID_1_0 : MAX_ID_1_3);

      if (jdr instanceof AJR)
      {
         String word;

         try
         {
            word = ((AJR)jdr).readWord();
         }
         catch (Exception e)
         {
            throw new InvalidValueException(
               InvalidFormatException.PAPER_ID, jdr, e);
         }

         try
         {
            id = Byte.parseByte(word);

            if (id < 0 || id > max_id)
            {
               throw new InvalidValueException(
                 InvalidFormatException.PAPER_ID, word, jdr);
            }
         }
         catch (NumberFormatException e)
         {
            for (int i = 0; i <= max_id; i++)
            {
               if (name[i].equals(word))
               {
                  id = (byte)i;
                  break;
               }
            }

            if (id == -1)
            {
               throw new InvalidValueException(
                 InvalidFormatException.PAPER_ID, word, jdr);
            }
         }
      }
      else
      {
         id = jdr.readByte(
           InvalidFormatException.PAPER_ID, 0, max_id, true, true);
      }

      JDRPaper paper = null;

      if (id == ID_USER)
      {
         double width = jdr.readDoubleGe(
            InvalidFormatException.PAPER_WIDTH, 0);

         double height = jdr.readDoubleGe(
            InvalidFormatException.PAPER_HEIGHT, 0);

         paper = new JDRPaper(width, height, ID_USER);

         if (version < 1.3f)
         {
            paper.portrait = jdr.readBoolean(
               InvalidFormatException.PAPER_PORTRAIT_FLAG);
         }
      }
      else
      {
         paper = getPredefinedPaper(id);

         if (paper == null)
         {
            // This shouldn't happen

            throw new InvalidValueException(
               InvalidFormatException.PAPER_ID, id, jdr);
         }
      }

      return paper;
   }

   /**
    * Identifier used in JDR/AJR files to indicate the paper size.
    */
   public static final byte ID_A0=0, ID_A1=1, ID_A2=2, ID_A3=3, ID_A4=4, ID_A5=5,
                           ID_LETTER=6, ID_LEGAL=7, ID_EXECUTIVE=8,
                           ID_A0R=9, ID_A1R=10, ID_A2R=11, ID_A3R=12, ID_A4R=13,
                           ID_A5R=14, ID_LETTERR=15, ID_LEGALR=16,
                           ID_EXECUTIVER=17, ID_USER=18;

   /**
    * Identifier used in JDR/AJR files to indicate the paper size
    * (version 1.3 onwards).
    */
   public static final byte ID_A6=19, ID_A7=20, ID_A8=21, ID_A9=22, ID_A10=23,
      ID_B0=24, ID_B1=25, ID_B2=26, ID_B3=27, ID_B4=28, ID_B5=29, ID_B6=30, ID_B7=31,
      ID_B8=32, ID_B9=33, ID_B10=34, ID_C0=35, ID_C1=36, ID_C2=37, ID_C3=38, ID_C4=39,
      ID_C5=40, ID_C6=41, ID_C7=42, ID_C8=43, ID_C9=44, ID_C10=45, ID_A6R=46, ID_A7R=47,
      ID_A8R=48, ID_A9R=49, ID_A10R=50, ID_B0R=51, ID_B1R=52, ID_B2R=53, ID_B3R=54,
      ID_B4R=55, ID_B5R=56, ID_B6R=57, ID_B7R=58, ID_B8R=59, ID_B9R=60, ID_B10R=61,
      ID_C0R=62, ID_C1R=63, ID_C2R=64, ID_C3R=65, ID_C4R=66, ID_C5R=67, ID_C6R=68,
      ID_C7R=69, ID_C8R=70, ID_C9R=71, ID_C10R=72;

   /**
    * Maximum number of paper ID for versions less than 1.3.
    */
   public static final byte MAX_ID_1_0 = 18;
   /**
    * Maximum number of paper ID for versions 1.3 onwards.
    */
   public static final byte MAX_ID_1_3=72;

   /**
    * Predefined A0 paper size.
    */
   public static final JDRPaper A0 = new JDRPaper(JDRUnit.mm.toBp(841),JDRUnit.mm.toBp(1189),ID_A0);
   /**
    * Predefined A1 paper size.
    */
   public static final JDRPaper A1 = new JDRPaper(JDRUnit.mm.toBp(594),JDRUnit.mm.toBp(841),ID_A1);
   /**
    * Predefined A2 paper size.
    */
   public static final JDRPaper A2 = new JDRPaper(JDRUnit.mm.toBp(420),JDRUnit.mm.toBp(594),ID_A2);
   /**
    * Predefined A3 paper size.
    */
   public static final JDRPaper A3 = new JDRPaper(JDRUnit.mm.toBp(297),JDRUnit.mm.toBp(420),ID_A3);
   /**
    * Predefined A4 paper size.
    */
   public static final JDRPaper A4 = new JDRPaper(JDRUnit.mm.toBp(210),JDRUnit.mm.toBp(297),ID_A4);
   /**
    * Predefined A5 paper size.
    */
   public static final JDRPaper A5 = new JDRPaper(JDRUnit.mm.toBp(148),JDRUnit.mm.toBp(210),ID_A5);

   /**
    * Predefined letter paper size.
    */
   public static final JDRPaper LETTER = new JDRPaper(JDRUnit.mm.toBp(216),JDRUnit.mm.toBp(279),ID_LETTER);
   /**
    * Predefined legal paper size.
    */
   public static final JDRPaper LEGAL = new JDRPaper(JDRUnit.mm.toBp(216),JDRUnit.mm.toBp(356),ID_LEGAL);
   /**
    * Predefined executive paper size.
    */
   public static final JDRPaper EXECUTIVE = new JDRPaper(JDRUnit.mm.toBp(184),JDRUnit.mm.toBp(267),ID_EXECUTIVE);

   /**
    * Predefined A0 landscape paper size.
    */
   public static final JDRPaper A0R = new JDRPaper(JDRUnit.mm.toBp(1189),JDRUnit.mm.toBp(841),ID_A0R);
   /**
    * Predefined A1 landscape paper size.
    */
   public static final JDRPaper A1R = new JDRPaper(JDRUnit.mm.toBp(841),JDRUnit.mm.toBp(594),ID_A1R);
   /**
    * Predefined A2 landscape paper size.
    */
   public static final JDRPaper A2R = new JDRPaper(JDRUnit.mm.toBp(594),JDRUnit.mm.toBp(420),ID_A2R);
   /**
    * Predefined A3 landscape paper size.
    */
   public static final JDRPaper A3R = new JDRPaper(JDRUnit.mm.toBp(420),JDRUnit.mm.toBp(297),ID_A3R);
   /**
    * Predefined A4 landscape paper size.
    */
   public static final JDRPaper A4R = new JDRPaper(JDRUnit.mm.toBp(297),JDRUnit.mm.toBp(210),ID_A4R);
   /**
    * Predefined A5 landscape paper size.
    */
   public static final JDRPaper A5R = new JDRPaper(JDRUnit.mm.toBp(210),JDRUnit.mm.toBp(148),ID_A5R);

   /**
    * Predefined letter landscape paper size.
    */
   public static final JDRPaper LETTERR = new JDRPaper(JDRUnit.mm.toBp(279),JDRUnit.mm.toBp(216),ID_LETTERR);
   /**
    * Predefined legal landscape paper size.
    */
   public static final JDRPaper LEGALR = new JDRPaper(JDRUnit.mm.toBp(356),JDRUnit.mm.toBp(216),ID_LEGALR);
   /**
    * Predefined executive landscape paper size.
    */
   public static final JDRPaper EXECUTIVER = new JDRPaper(JDRUnit.mm.toBp(267),JDRUnit.mm.toBp(184),ID_EXECUTIVER);

   // new to jdr v1.3:

   /**
    * Predefined A6 paper size.
    */
   public static final JDRPaper A6 = new JDRPaper(JDRUnit.mm.toBp(105), JDRUnit.mm.toBp(148),ID_A6);
   /**
    * Predefined A7 paper size.
    */
   public static final JDRPaper A7 = new JDRPaper(JDRUnit.mm.toBp(74), JDRUnit.mm.toBp(105),ID_A7);
   /**
    * Predefined A8 paper size.
    */
   public static final JDRPaper A8 = new JDRPaper(JDRUnit.mm.toBp(52), JDRUnit.mm.toBp(74),ID_A8);
   /**
    * Predefined A9 paper size.
    */
   public static final JDRPaper A9 = new JDRPaper(JDRUnit.mm.toBp(37), JDRUnit.mm.toBp(52),ID_A9);
   /**
    * Predefined A10 paper size.
    */
   public static final JDRPaper A10 = new JDRPaper(JDRUnit.mm.toBp(26), JDRUnit.mm.toBp(37),ID_A10);

   /**
    * Predefined B0 paper size.
    */
   public static final JDRPaper B0 = new JDRPaper(JDRUnit.mm.toBp(1000), JDRUnit.mm.toBp(1414),ID_B0);
   /**
    * Predefined B1 paper size.
    */
   public static final JDRPaper B1 = new JDRPaper(JDRUnit.mm.toBp(707), JDRUnit.mm.toBp(1000),ID_B1);
   /**
    * Predefined B2 paper size.
    */
   public static final JDRPaper B2 = new JDRPaper(JDRUnit.mm.toBp(500), JDRUnit.mm.toBp(707),ID_B2);
   /**
    * Predefined B3 paper size.
    */
   public static final JDRPaper B3 = new JDRPaper(JDRUnit.mm.toBp(353), JDRUnit.mm.toBp(500),ID_B3);
   /**
    * Predefined B4 paper size.
    */
   public static final JDRPaper B4 = new JDRPaper(JDRUnit.mm.toBp(250), JDRUnit.mm.toBp(353),ID_B4);
   /**
    * Predefined B5 paper size.
    */
   public static final JDRPaper B5 = new JDRPaper(JDRUnit.mm.toBp(176), JDRUnit.mm.toBp(250),ID_B5);
   /**
    * Predefined B6 paper size.
    */
   public static final JDRPaper B6 = new JDRPaper(JDRUnit.mm.toBp(125), JDRUnit.mm.toBp(176),ID_B6);
   /**
    * Predefined B7 paper size.
    */
   public static final JDRPaper B7 = new JDRPaper(JDRUnit.mm.toBp(88), JDRUnit.mm.toBp(125),ID_B7);
   /**
    * Predefined B8 paper size.
    */
   public static final JDRPaper B8 = new JDRPaper(JDRUnit.mm.toBp(62), JDRUnit.mm.toBp(88),ID_B8);
   /**
    * Predefined B9 paper size.
    */
   public static final JDRPaper B9 = new JDRPaper(JDRUnit.mm.toBp(44), JDRUnit.mm.toBp(62),ID_B9);
   /**
    * Predefined B10 paper size.
    */
   public static final JDRPaper B10 = new JDRPaper(JDRUnit.mm.toBp(31), JDRUnit.mm.toBp(44),ID_B10);

   /**
    * Predefined C0 paper size.
    */
   public static final JDRPaper C0 = new JDRPaper(JDRUnit.mm.toBp(917), JDRUnit.mm.toBp(1297),ID_C0);
   /**
    * Predefined C1 paper size.
    */
   public static final JDRPaper C1 = new JDRPaper(JDRUnit.mm.toBp(648), JDRUnit.mm.toBp(917),ID_C1);
   /**
    * Predefined C2 paper size.
    */
   public static final JDRPaper C2 = new JDRPaper(JDRUnit.mm.toBp(458), JDRUnit.mm.toBp(648),ID_C2);
   /**
    * Predefined C3 paper size.
    */
   public static final JDRPaper C3 = new JDRPaper(JDRUnit.mm.toBp(324), JDRUnit.mm.toBp(458),ID_C3);
   /**
    * Predefined C4 paper size.
    */
   public static final JDRPaper C4 = new JDRPaper(JDRUnit.mm.toBp(229), JDRUnit.mm.toBp(324),ID_C4);
   /**
    * Predefined C5 paper size.
    */
   public static final JDRPaper C5 = new JDRPaper(JDRUnit.mm.toBp(162), JDRUnit.mm.toBp(229),ID_C5);
   /**
    * Predefined C6 paper size.
    */
   public static final JDRPaper C6 = new JDRPaper(JDRUnit.mm.toBp(114), JDRUnit.mm.toBp(162),ID_C6);
   /**
    * Predefined C7 paper size.
    */
   public static final JDRPaper C7 = new JDRPaper(JDRUnit.mm.toBp(81), JDRUnit.mm.toBp(114),ID_C7);
   /**
    * Predefined C8 paper size.
    */
   public static final JDRPaper C8 = new JDRPaper(JDRUnit.mm.toBp(57), JDRUnit.mm.toBp(81),ID_C8);
   /**
    * Predefined C9 paper size.
    */
   public static final JDRPaper C9 = new JDRPaper(JDRUnit.mm.toBp(40), JDRUnit.mm.toBp(57),ID_C9);
   /**
    * Predefined C10 paper size.
    */
   public static final JDRPaper C10 = new JDRPaper(JDRUnit.mm.toBp(28), JDRUnit.mm.toBp(40),ID_C10);

   /**
    * Predefined A6 landscape paper size.
    */
   public static final JDRPaper A6R = new JDRPaper(JDRUnit.mm.toBp(148), JDRUnit.mm.toBp(105),ID_A6R);
   /**
    * Predefined A7 landscape paper size.
    */
   public static final JDRPaper A7R = new JDRPaper(JDRUnit.mm.toBp(105), JDRUnit.mm.toBp(74),ID_A7R);
   /**
    * Predefined A8 landscape paper size.
    */
   public static final JDRPaper A8R = new JDRPaper(JDRUnit.mm.toBp(74), JDRUnit.mm.toBp(52),ID_A8R);
   /**
    * Predefined A9 landscape paper size.
    */
   public static final JDRPaper A9R = new JDRPaper(JDRUnit.mm.toBp(52), JDRUnit.mm.toBp(37),ID_A9R);
   /**
    * Predefined A10 landscape paper size.
    */
   public static final JDRPaper A10R = new JDRPaper(JDRUnit.mm.toBp(37), JDRUnit.mm.toBp(26),ID_A10R);

   /**
    * Predefined B0 landscape paper size.
    */
   public static final JDRPaper B0R = new JDRPaper(JDRUnit.mm.toBp(1414), JDRUnit.mm.toBp(1000),ID_B0R);
   /**
    * Predefined B1 landscape paper size.
    */
   public static final JDRPaper B1R = new JDRPaper(JDRUnit.mm.toBp(1000), JDRUnit.mm.toBp(707),ID_B1R);
   /**
    * Predefined B2 landscape paper size.
    */
   public static final JDRPaper B2R = new JDRPaper(JDRUnit.mm.toBp(707), JDRUnit.mm.toBp(500),ID_B2R);
   /**
    * Predefined B3 landscape paper size.
    */
   public static final JDRPaper B3R = new JDRPaper(JDRUnit.mm.toBp(500), JDRUnit.mm.toBp(353),ID_B3R);
   /**
    * Predefined B4 landscape paper size.
    */
   public static final JDRPaper B4R = new JDRPaper(JDRUnit.mm.toBp(353), JDRUnit.mm.toBp(250),ID_B4R);
   /**
    * Predefined B5 landscape paper size.
    */
   public static final JDRPaper B5R = new JDRPaper(JDRUnit.mm.toBp(250), JDRUnit.mm.toBp(176),ID_B5R);
   /**
    * Predefined B6 landscape paper size.
    */
   public static final JDRPaper B6R = new JDRPaper(JDRUnit.mm.toBp(176), JDRUnit.mm.toBp(125),ID_B6R);
   /**
    * Predefined B7 landscape paper size.
    */
   public static final JDRPaper B7R = new JDRPaper(JDRUnit.mm.toBp(125), JDRUnit.mm.toBp(88),ID_B7R);
   /**
    * Predefined B8 landscape paper size.
    */
   public static final JDRPaper B8R = new JDRPaper(JDRUnit.mm.toBp(88), JDRUnit.mm.toBp(62),ID_B8R);
   /**
    * Predefined B9 landscape paper size.
    */
   public static final JDRPaper B9R = new JDRPaper(JDRUnit.mm.toBp(62), JDRUnit.mm.toBp(44),ID_B9R);
   /**
    * Predefined B10 landscape paper size.
    */
   public static final JDRPaper B10R = new JDRPaper(JDRUnit.mm.toBp(44), JDRUnit.mm.toBp(31),ID_B10R);

   /**
    * Predefined C0 landscape paper size.
    */
   public static final JDRPaper C0R = new JDRPaper(JDRUnit.mm.toBp(1297), JDRUnit.mm.toBp(917),ID_C0R);
   /**
    * Predefined C1 landscape paper size.
    */
   public static final JDRPaper C1R = new JDRPaper(JDRUnit.mm.toBp(917), JDRUnit.mm.toBp(648),ID_C1R);
   /**
    * Predefined C2 landscape paper size.
    */
   public static final JDRPaper C2R = new JDRPaper(JDRUnit.mm.toBp(648), JDRUnit.mm.toBp(458),ID_C2R);
   /**
    * Predefined C3 landscape paper size.
    */
   public static final JDRPaper C3R = new JDRPaper(JDRUnit.mm.toBp(458), JDRUnit.mm.toBp(324),ID_C3R);
   /**
    * Predefined C4 landscape paper size.
    */
   public static final JDRPaper C4R = new JDRPaper(JDRUnit.mm.toBp(324), JDRUnit.mm.toBp(229),ID_C4R);
   /**
    * Predefined C5 landscape paper size.
    */
   public static final JDRPaper C5R = new JDRPaper(JDRUnit.mm.toBp(229), JDRUnit.mm.toBp(162),ID_C5R);
   /**
    * Predefined C6 landscape paper size.
    */
   public static final JDRPaper C6R = new JDRPaper(JDRUnit.mm.toBp(162), JDRUnit.mm.toBp(114),ID_C6R);
   /**
    * Predefined C7 landscape paper size.
    */
   public static final JDRPaper C7R = new JDRPaper(JDRUnit.mm.toBp(114), JDRUnit.mm.toBp(81),ID_C7R);
   /**
    * Predefined C8 landscape paper size.
    */
   public static final JDRPaper C8R = new JDRPaper(JDRUnit.mm.toBp(81), JDRUnit.mm.toBp(57),ID_C8R);
   /**
    * Predefined C9 landscape paper size.
    */
   public static final JDRPaper C9R = new JDRPaper(JDRUnit.mm.toBp(57), JDRUnit.mm.toBp(40),ID_C9R);
   /**
    * Predefined C10 landscape paper size.
    */
   public static final JDRPaper C10R = new JDRPaper(JDRUnit.mm.toBp(40), JDRUnit.mm.toBp(28),ID_C10R);

   private static final String[]
      name = {"a0", "a1", "a2", "a3", "a4", "a5",
              "letter", "legal", "executive",
              "a0r", "a1r", "a2r", "a3r", "a4r",
              "a5r", "letterr", "legalr",
              "executiver", "user",
              "a6", "a7", "a8", "a9", "a10",
              "b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7",
              "b8", "b9", "b10", "c0", "c1", "c2", "c3", "c4",
              "c5", "c6", "c7", "c8", "c9", "c10", "a6r", "a7r",
              "a8r", "a9r", "a10r", "b0r", "b1r", "b2r", "b3r",
              "b4r", "b5r", "b6r", "b7r", "b8r", "b9r", "b10r",
              "c0r", "c1r", "c2r", "c3r", "c4r", "c5r", "c6r",
              "c7r", "c8r", "c9r", "c10r"
      };

   private static final String[]
      tag = {"a0", "a1", "a2", "a3", "a4", "a5",
              "letter", "legal", "executive",
              "a0", "a1", "a2", "a3", "a4",
              "a5", "letter", "legal",
              "executive", "user",
              "a6", "a7", "a8", "a9", "a10",
              "b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7",
              "b8", "b9", "b10", "c0", "c1", "c2", "c3", "c4",
              "c5", "c6", "c7", "c8", "c9", "c10", "a6", "a7",
              "a8", "a9", "a10", "b0", "b1", "b2", "b3",
              "b4", "b5", "b6", "b7", "b8", "b9", "b10",
              "c0", "c1", "c2", "c3", "c4", "c5", "c6",
              "c7", "c8", "c9", "c10"
      };

   /**
    * Indicates the orientation of this paper.
    */
   protected boolean portrait;
   private double width_, height_; // bp (PostScript Points)

   private byte id_ = ID_USER;

   private Rectangle2D imageableArea;

}
