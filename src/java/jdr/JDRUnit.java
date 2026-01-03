//File          : JDRUnit.java
//Description   : Provide conversion between TeX length units
//Author        : Nicola L.C. Talbot
//Creation Date : 17th March 2006
//              http://www.dickimaw-books.com/

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

import java.awt.*;
import java.awt.geom.*;
import java.io.Serializable;

import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdr.io.*;

/**
 * Class representing units of length. Since this library was written
 * primarily to create LaTeX files, the principle units are pt (TeX
 * points) and bp (Postscript points), where 1in = 72.27pt = 72bp.
 * @author Nicola L C Talbot
 */
public class JDRUnit implements Serializable
{
   /**
    * Creates a new unit.
    * @param name the name of this unit
    * @param conversionFactor the factor to convert from this unit
    * to TeX points (<code>pt = conversionFactor * unit</code>)
    */
   public JDRUnit (String name, double conversionFactor)
   {
      label = name;
      factor = conversionFactor;
   }

   private JDRUnit (String name, double conversionFactor, int identNum)
   {
      label = name;
      factor = conversionFactor;
      id = identNum;
   }

   /**
    * Converts value (in this unit) to TeX points.
    * @param val value in terms of this unit
    * @return value in terms of TeX points
    * @see #fromPt(double)
    * @see #toBp(double)
    * @see #fromBp(double)
    */
   public double toPt(double val)
   {
      return val*factor;
   }

   /**
    * Converts value in TeX points to value in terms of this unit.
    * @param val value in terms of TeX points
    * @return value in terms of this unit
    * @see #toPt(double)
    * @see #toBp(double)
    * @see #fromBp(double)
    */
   public double fromPt(double val)
   {
      return val/factor;
   }

   /**
    * Converts value (in this unit) to PostScript points.
    * @param val value in terms of this unit
    * @return value in terms of PostScript points
    * @see #fromBp(double)
    * @see #toPt(double)
    * @see #fromPt(double)
    */
   public double toBp(double val)
   {
      return toUnit(val, JDRUnit.bp);
   }

   /**
    * Converts value in PostScript points to value in terms of 
    * this unit.
    * @param val value in terms of PostScript points
    * @return value in terms of this unit
    * @see #toBp(double)
    * @see #toPt(double)
    * @see #fromPt(double)
    */
   public double fromBp(double val)
   {
      return fromUnit(val, JDRUnit.bp);
   }

   /**
    * Converts value (in this unit) to value in terms of another unit.
    * @param val value in terms of this unit
    * @param unit the other unit
    * @return value in terms of the other unit
    * @see #fromUnit(double,JDRUnit)
    * @see #fromBp(double)
    * @see #toBp(double)
    * @see #toPt(double)
    * @see #fromPt(double)
    */
   public double toUnit(double val, JDRUnit unit)
   {
      if (getID() == unit.getID()) return val;

      if (getID() < 0 || getID() >= CONVERSION_MAP.length
      || unit.getID() < 0 || unit.getID() >= CONVERSION_MAP.length)
      {
         return unit.fromPt(toPt(val));
      }

      return val*CONVERSION_MAP[getID()][unit.getID()];
   }

   /**
    * Converts value in another unit to value in terms of 
    * this unit.
    * @param val value in terms of another unit
    * @param unit the other unit
    * @return value in terms of this unit
    * @see #toUnit(double,JDRUnit)
    * @see #toBp(double)
    * @see #fromBp(double)
    * @see #toPt(double)
    * @see #fromPt(double)
    */
   public double fromUnit(double val, JDRUnit unit)
   {
      if (getID() == unit.getID()) return val;

      if (getID() < 0 || getID() >= CONVERSION_MAP.length
      || unit.getID() < 0 || unit.getID() >= CONVERSION_MAP.length)
      {
         return fromPt(unit.toPt(val));
      }

      return val*CONVERSION_MAP[unit.getID()][getID()];
   }

   /**
    * Gets the label identifying this unit.
    * @return this unit's label
    */
   public String getLabel()
   {
      return label;
   }

   /**
    * Gets a string representation of this unit.
    * @return string representation of this unit.
    */
   public String toString()
   {
      return "JDRUnit@"+label+"@"+factor;
   }

   public int getID()
   {
      return id;
   }

   /**
    * Gets the predefined unit given by the identifying number.
    * @param identNum the identifying number
    * @return the predefined unit associated with the given ID or
    * null
    */
   public static JDRUnit getUnit(int identNum)
   {
      switch (identNum)
      {
         case PT: return pt;
         case IN: return in;
         case CM: return cm;
         case BP: return bp;
         case MM: return mm;
         case PC: return pc;
         case DD: return dd;
         case CC: return cc;
      }

      return null;
   }

   public String tex(double value)
   {
      return PGF.format(value)+getLabel();
   }

   public String svg(double value)
   {
     return ""+toBp(value);
   }


   public static JDRUnit getUnit(String unitLabel)
   {
      for (int i = 0; i < UNIT_LABELS.length; i++)
      {
         if (unitLabel.equals(UNIT_LABELS[i]))
         {
            return getUnit(i);
         }
      }

      return null;
   }

   public boolean equals(Object object)
   {
      if (this == object) return true;

      if (!(object instanceof JDRUnit))
      {
         return false;
      }

      JDRUnit unit = (JDRUnit)object;

      if (id != -1 && unit.id != -1)
      {
         return id == unit.id;
      }

      return factor == unit.factor;
   }

   /**
    * This unit's label.
    */
   private String label;
   private double factor;

   private int id = -1;

   /**
    * JDR/AJR unit identifier.
    */
   public static final byte PT=0, IN=1, CM=2, BP=3, MM=4, PC=5, DD=6,
      CC=7;

   public static final String[] UNIT_LABELS = new String[]
    {"pt", "in", "cm", "bp", "mm", "pc", "dd", "cc"};

   // predefined units

   /** LaTeX point. */
   public static final JDRUnit pt = new JDRUnit("pt", 1.0, PT);
   /** Postscript point. */
   public static final JDRUnit bp = new JDRUnit("bp", 72.27/72.0, BP);
   /** Inch. */
   public static final JDRUnit in = new JDRUnit("in", 72.27, IN);
   /** Centimetre. */
   public static final JDRUnit cm = new JDRUnit("cm", 28.4528, CM);
   /** Millimetre. */
   public static final JDRUnit mm = new JDRUnit("mm", 2.84528, MM);
   /** Pica. */
   public static final JDRUnit pc = new JDRUnit("pc", 12.0, PC);
   /** Didot. */
   public static final JDRUnit dd = new JDRUnit("dd", 1.07001, DD);
   /** Cicero. */
   public static final JDRUnit cc = new JDRUnit("cc", 12.8401, CC);

   /* Unit conversions */
   public static final double[][] CONVERSION_MAP = new double[][]
   {
      // pt in cm bp mm pc dd cc
      new double[] {1.0, 1.0/72.27, 2.54/72.27, 72/72.27, 25.4/72.27, 1.0/12.0, 1157.0/1238.0, 1.0/14856.0}, // pt
      new double[] {72.27, 1.0, 2.54, 72.0, 25.4, 72.27/12.0, 72.27*1157.0/1238.0, 72.27/14856.0}, // in
      new double[] {72.27/2.54, 1.0/2.54, 1.0, 72.0/2.54, 10.0, 72.27/(12.0*2.54), 72.27*1157.0/(1238.0*2.54), 72.27/(14856.0*2.54)}, // cm
      new double[] {72.27/72.0, 1.0/72.0, 2.54/72.0, 1.0, 25.4/72.0, 72.27/864.0, 72.27*1157.0/89136.0, 72.27/1069632.0}, // bp
      new double[] {7.227/2.54, 1.0/25.4, 0.1, 72.0/25.4, 1.0, 72.27/(12.0*25.4), 72.27*1157.0/(1238.0*25.4), 72.27/(14856.0*25.4)}, // mm
      new double[] {12.0, 12.0/72.27, 12.0*2.54/72.27, 864.0/72.27, 12.0*25.4/72.27, 1.0, 13884.0/1238.0, 1157.0/1238.0}, // pc
      new double[] {1238.0/1157.0, 1238.0/(72.27*1157.0), 1238.0*2.54/(72.27*1157.0), 89136.0/(72.27*1157.0), 1238.0*25.4/(72.27*1157.0), 1238.0/13884.0, 1.0, 1.0/12.0}, // dd
      new double[] {14856.0, 14856.0/72.27, 14856.0*2.54/72.27, 1069632.0/72.27, 14856.0*25.4/72.27, 1238.0/1157.0, 12.0, 1.0} // cc
   };
}
