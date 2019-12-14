// File          : EPSSystemDict.java
// Purpose       : class representing an EPS system dictionary
// Date          : 1st February 2006
// Last Modified : 28 July 2007
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

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
package com.dickimawbooks.jdr.io.eps;

import java.io.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.eps.operators.*;
import com.dickimawbooks.jdr.io.EPS;

import com.dickimawbooks.jdr.exceptions.*;

/**
 * Class representing an EPS system dictionary.
 * @author Nicola L C Talbot
 */
public class EPSSystemDict extends EPSDict
{
   /**
    * Initialises with an initial capacity of 11.
    */
   public EPSSystemDict()
   {
      this(300);
   }

   /**
    * Initialises with the given capacity.
    * @param initialCapacity the initial capacity of this dictionary
    */
   public EPSSystemDict(int initialCapacity)
   {
      super(initialCapacity);

      putOp(new EPSDoubleLeftAngle());
      putOp(new EPSDoubleRightAngle());
      putOp(new EPSForall());
      putOp(new EPSLength());
      putOp(new EPSGet());
      putOp(new EPSPut());
      putOp(new EPSGetInterval());
      putOp(new EPSPutInterval());
      putOp(new EPSToken());
      putOp(new EPSDef());
      putOp(new EPSPop());
      putOp(new EPSExch());
      putOp(new EPSDup());
      putOp(new EPSCopy());
      putOp(new EPSIndex());
      putOp(new EPSRoll());
      putOp(new EPSClear());
      putOp(new EPSCount());
      putOp(new EPSMarkOp());
      putOp(new EPSClearToMark());
      putOp(new EPSCountToMark());
      putOp(new EPSExec());
      putOp(new EPSStopped());
      putOp(new EPSStop());
      putOp(new EPSIf());
      putOp(new EPSIfElse());
      putOp(new EPSFor());
      putOp(new EPSRepeat());
      putOp(new EPSLoop());
      putOp(new EPSExit());
      putOp(new EPSAdd());
      putOp(new EPSDiv());
      putOp(new EPSIdiv());
      putOp(new EPSMod());
      putOp(new EPSMul());
      putOp(new EPSSub());
      putOp(new EPSAbs());
      putOp(new EPSNeg());
      putOp(new EPSCeiling());
      putOp(new EPSFloor());
      putOp(new EPSRound());
      putOp(new EPSTruncate());
      putOp(new EPSSqrt());
      putOp(new EPSAtan());
      putOp(new EPSCos());
      putOp(new EPSSin());
      putOp(new EPSExp());
      putOp(new EPSLn());
      putOp(new EPSLog());
      putOp(new EPSRand());
      putOp(new EPSSrand());
      putOp(new EPSRrand());
      putOp(new EPSEq());
      putOp(new EPSNe());
      putOp(new EPSGe());
      putOp(new EPSGt());
      putOp(new EPSLe());
      putOp(new EPSLt());
      putOp(new EPSAnd());
      putOp(new EPSOr());
      putOp(new EPSXor());
      putOp(new EPSNot());
      putOp(new EPSBitshift());
      putOp(new EPSSquareLeft());
      putOp(new EPSSquareRight());
      putOp(new EPSArrayOp());
      putOp(new EPSAload());
      putOp(new EPSAstore());
      putOp(new EPSPackedArrayOp());
      putOp(new EPSCurrentPacking());
      putOp(new EPSSetPacking());
      putOp(new EPSStringOp());
      putOp(new EPSSearch());
      putOp(new EPSAnchorSearch());
      putOp(new EPSDictOp());
      putOp(new EPSMaxLength());
      putOp(new EPSBegin());
      putOp(new EPSEnd());
      putOp(new EPSLoad());
      putOp(new EPSStore());
      putOp(new EPSKnown());
      putOp(new EPSWhere());
      putOp(new EPSCountDictStack());
      putOp(new EPSDictStack());
      putOp(new EPSType());
      putOp(new EPSCvlit());
      putOp(new EPSCvx());
      putOp(new EPSXcheck());
      putOp(new EPSExecuteOnly());
      putOp(new EPSNoAccess());
      putOp(new EPSReadOnly());
      putOp(new EPSRcheck());
      putOp(new EPSWcheck());
      putOp(new EPSCvi());
      putOp(new EPSCvr());
      putOp(new EPSCvn());
      putOp(new EPSCvs());
      putOp(new EPSCvrs());
      putOp(new EPSFileOp());
      putOp(new EPSCloseFile());
      putOp(new EPSRead());
      putOp(new EPSWrite());
      putOp(new EPSReadHexString());
      putOp(new EPSWriteHexString());
      putOp(new EPSWriteString());
      putOp(new EPSReadLine());
      putOp(new EPSReadString());
      putOp(new EPSBytesAvailable());
      putOp(new EPSFlush());
      putOp(new EPSFlushFile());
      putOp(new EPSResetFile());
      putOp(new EPSStatus());
      putOp(new EPSRun());
      putOp(new EPSCurrentFile());
      putOp(new EPSFilterOp());
      putOp(new EPSPrint());
      putOp(new EPSPrompt());
      putOp(new EPSPstack());
      putOp(new EPSStackOp());
      putOp(new EPSEqualSign());
      putOp(new EPSDoubleEqualSign());
      putOp(new EPSEcho());
      putOp(new EPSGsave());
      putOp(new EPSGrestore());
      putOp(new EPSGstate());
      putOp(new EPSSave());
      putOp(new EPSRestore());
      putOp(new EPSSetLineWidth());
      putOp(new EPSCurrentLineWidth());
      putOp(new EPSSetLineCap());
      putOp(new EPSCurrentLineCap());
      putOp(new EPSSetLineJoin());
      putOp(new EPSCurrentLineJoin());
      putOp(new EPSSetMiterLimit());
      putOp(new EPSCurrentMiterLimit());
      putOp(new EPSSetDash());
      putOp(new EPSCurrentDash());
      putOp(new EPSSetTransfer());
      putOp(new EPSSetColorTransfer());
      putOp(new EPSSetRgbColor());
      putOp(new EPSSetCmykColor());
      putOp(new EPSSetGray());
      putOp(new EPSSetColorSpace());
      putOp(new EPSSetColor());
      putOp(new EPSCurrentColor());
      putOp(new EPSCurrentCmykColor());
      putOp(new EPSCurrentRgbColor());
      putOp(new EPSCurrentHsbColor());
      putOp(new EPSCurrentGray());
      putOp(new EPSCurrentTransfer());
      putOp(new EPSCurrentColorTransfer());
      putOp(new EPSNewPath());
      putOp(new EPSCurrentPoint());
      putOp(new EPSMoveTo());
      putOp(new EPSRmoveTo());
      putOp(new EPSLineTo());
      putOp(new EPSRlineTo());
      putOp(new EPSArc());
      putOp(new EPSArcn());
      putOp(new EPSCurveTo());
      putOp(new EPSRcurveTo());
      putOp(new EPSArcTo());
      putOp(new EPSArcT());
      putOp(new EPSClosePath());
      putOp(new EPSFlattenPath());
      putOp(new EPSCurrentFlat());
      putOp(new EPSSetFlat());
      putOp(new EPSReversePath());
      putOp(new EPSStrokePath());
      putOp(new EPSCharPath());
      putOp(new EPSPathBBox());
      putOp(new EPSPathForAll());
      putOp(new EPSClip());
      putOp(new EPSEoClip());
      putOp(new EPSRectClip());
      putOp(new EPSShFill());
      putOp(new EPSFill());
      putOp(new EPSEoFill());
      putOp(new EPSStroke());
      putOp(new EPSRectStroke());
      putOp(new EPSRectFill());
      putOp(new EPSImage());
      putOp(new EPSColorImage());
      putOp(new EPSImageMask());
      putOp(new EPSMatrixOp());
      putOp(new EPSIdentMatrix());
      putOp(new EPSDefaultMatrix());
      putOp(new EPSCurrentMatrix());
      putOp(new EPSSetMatrix());
      putOp(new EPSTranslate());
      putOp(new EPSScale());
      putOp(new EPSRotate());
      putOp(new EPSConcat());
      putOp(new EPSCurrentDict());
      putOp(new EPSConcatMatrix());
      putOp(new EPSTransform());
      putOp(new EPSDTransform());
      putOp(new EPSITransform());
      putOp(new EPSIDTransform());
      putOp(new EPSInvertMatrix());
      putOp(new EPSShowPage());
      putOp(new EPSNullDevice());
      putOp(new EPSFindFont());
      putOp(new EPSScaleFont());
      putOp(new EPSMakeFont());
      putOp(new EPSSetFont());
      putOp(new EPSSelectFont());
      putOp(new EPSCurrentFont());
      putOp(new EPSShow());
      putOp(new EPSAshow());
      putOp(new EPSWidthShow());
      putOp(new EPSAWidthShow());
      putOp(new EPSKShow());
      putOp(new EPSStringWidth());
      putOp(new EPSSetCacheDevice());
      putOp(new EPSSetCharWidth());
      putOp(new EPSSetCacheLimit());
      putOp(new EPSSetCacheParams());
      putOp(new EPSBind());

      putOp(new EPSDefineFont());// not implemented

      try
      {
         super.setWriteAccess(false);
      }
      catch (InvalidEPSObjectException e)
      {
      }
   }

   private void putOp(EPSOperator op)
   {
      try
      {
         super.putValue(op.getName(), op);
      }
      catch (NoWriteAccessException e)
      {
      }
   }

   public EPSName pstype()
   {
      return new EPSName("systemdict");
   }

   private EPSObject getNamedConstant(String name)
   {
      if (name.equals("true"))
      {
         return new EPSBoolean(true);
      }

      if (name.equals("false"))
      {
         return new EPSBoolean(false);
      }

      if (name.equals("null"))
      {
         return new EPSNull();
      }

      return null;
   }

   public EPSObject get(EPSObject key)
      throws InvalidEPSObjectException,NoReadAccessException
   {
      if (!hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      if (!(key instanceof EPSName))
      {
         throw new InvalidEPSObjectException("(get) invalid key");
      }

      String name = ((EPSName)key).toString();

      EPSObject obj = getNamedConstant(name);

      if (obj != null)
      {
         return obj;
      }

      obj = super.get(name);

      if (obj != null)
      {
         return obj;
      }

      if (name.startsWith("/"))
      {
         name = name.substring(1);
      }

      return super.get(name);
   }

   public EPSObject get(String key)
      throws NoReadAccessException
   {
      if (!hasReadAccess())
      {
         throw new NoReadAccessException();
      }

      EPSObject object = getNamedConstant(key);

      if (object != null)
      {
         return object;
      }

      object = super.get(key);

      if (object == null)
      {
         object = super.get("/"+key);
      }

      return object;
   }

}
