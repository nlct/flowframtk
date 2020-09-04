// File          : JDRConstants.java
// Purpose       : Constants used by jdr library
// Creation Date : 2014-04-26
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

package com.dickimawbooks.jdr;

public interface JDRConstants
{
   /*
   * Object types.
   */

   public static final int OBJECT_COMPLETE=0;
   public static final int OBJECT_PATH=1;
   public static final int OBJECT_TEXT=2;
   public static final int OBJECT_BITMAP=3;
   public static final int OBJECT_GROUP=4;
   public static final int OBJECT_TEXTPATH=5;
   public static final int OBJECT_SYMMETRIC=6;
   public static final int OBJECT_SYMTEXTPATH=7;
   public static final int OBJECT_PATTERN=8;
   public static final int OBJECT_COMPOUND=9;
   public static final int OBJECT_SHAPE=10;
   public static final int OBJECT_TEXTUAL=11;
   public static final int OBJECT_NON_TEXTUAL_SHAPE=12;
   public static final int OBJECT_DISTORTABLE=13;
   public static final int OBJECT_CLOSED=14;
   public static final int OBJECT_OPEN=15;
   public static final int OBJECT_SYMMETRIC_ANCHORED_JOIN=16;
   public static final int OBJECT_SYMMETRIC_ANCHORED_CLOSE=17;
   public static final int OBJECT_DISTORTED=18;
   public static final int OBJECT_OUTLINE=19;

   public static final int OBJECT_MAX_INDEX=19;

   /*
    * Selection flags.
    */

   public static final int SELECT_FLAG_NONE = (1 << (OBJECT_MAX_INDEX+1));

   public static final int SELECT_FLAG_OBJECT = (1 << OBJECT_COMPLETE); // any type of object
   public static final int SELECT_FLAG_PATH = (1 << OBJECT_PATH);
   public static final int SELECT_FLAG_TEXT = (1 << OBJECT_TEXT);
   public static final int SELECT_FLAG_BITMAP = (1 << OBJECT_BITMAP);
   public static final int SELECT_FLAG_GROUP = 
     (1 << OBJECT_GROUP);
   public static final int SELECT_FLAG_TEXTPATH = 
     (1 << OBJECT_TEXTPATH);
   public static final int SELECT_FLAG_SYMMETRIC = 
     (1 << OBJECT_SYMMETRIC);
   public static final int SELECT_FLAG_SYMTEXTPATH = 
     (1 << OBJECT_SYMTEXTPATH);
   public static final int SELECT_FLAG_PATTERN = 
     (1 << OBJECT_PATTERN);
   public static final int SELECT_FLAG_COMPOUND = 
     (1 << OBJECT_COMPOUND);
   public static final int SELECT_FLAG_SHAPE = 
     (1 << OBJECT_SHAPE);
   public static final int SELECT_FLAG_TEXTUAL = 
     (1 << OBJECT_TEXTUAL);
   public static final int SELECT_FLAG_NON_TEXTUAL_SHAPE = 
     (1 << OBJECT_NON_TEXTUAL_SHAPE);
   public static final int SELECT_FLAG_DISTORTABLE =
     (1 << OBJECT_DISTORTABLE);

   public static final int SELECT_FLAG_CLOSED =
     (1 << OBJECT_CLOSED);

   public static final int SELECT_FLAG_OPEN =
     (1 << OBJECT_OPEN);

   public static final int SELECT_FLAG_SYMMETRIC_ANCHORED_JOIN =
     (1 << OBJECT_SYMMETRIC_ANCHORED_JOIN);

   public static final int SELECT_FLAG_SYMMETRIC_ANCHORED_CLOSE =
     (1 << OBJECT_SYMMETRIC_ANCHORED_CLOSE);

   public static final int SELECT_FLAG_DISTORTED =
     (1 << OBJECT_DISTORTED);

   public static final int SELECT_FLAG_OUTLINE = 
     (1 << OBJECT_OUTLINE);

   public static final int SELECT_FLAG_ANY
      = (1 << (OBJECT_MAX_INDEX+2)) - 1;

   public static final int SELECT_FLAG_ANY_OBJECT
      = (SELECT_FLAG_ANY & ~SELECT_FLAG_NONE);

  /*
   * Edit flags.
   */ 

   public static final byte EDIT_FLAG_NONE = 1;
   public static final byte EDIT_FLAG_PATH = 2;
   public static final byte EDIT_FLAG_DISTORT = 4;

   public static final byte EDIT_FLAG_ANY
      = EDIT_FLAG_NONE
      | EDIT_FLAG_PATH
      | EDIT_FLAG_DISTORT;

   public static final byte EDIT_FLAG_NONE_OR_DISTORT
      = EDIT_FLAG_NONE | EDIT_FLAG_DISTORT;

   public static final byte EDIT_FLAG_NONE_OR_PATH
      = EDIT_FLAG_NONE | EDIT_FLAG_PATH;

   /**
    * Tools.
    */
   public static final int ACTION_SELECT=0;
   /**
    * Open line tool.
    */
   public static final int ACTION_OPEN_LINE=1;
   /**
    * Closed line tool.
    */
   public static final int ACTION_CLOSED_LINE=2;
   /**
    * Open curve tool.
    */
   public static final int ACTION_OPEN_CURVE=3;
   /**
    * Closed curve tool.
    */
   public static final int ACTION_CLOSED_CURVE=4;
   /**
    * Rectangle tool.
    */
   public static final int ACTION_RECTANGLE=5;
   /**
    * Ellipse tool.
    */
   public static final int ACTION_ELLIPSE=6;
   /**
    * Text tool.
    */
   public static final int ACTION_TEXT=7;

   /**
    * Maths tool.
    */
   public static final int ACTION_MATH=8;

   /**
    * Total number of tool settings.
    */
   public static final int MAX_TOOLS=9;

   /*
    * Tool flags.
    */ 
   public static final int TOOL_FLAG_ANY = (1 << MAX_TOOLS)-1;

   public static final int TOOL_FLAG_SELECT
      = (1 << ACTION_SELECT);

   public static final int TOOL_FLAG_TEXT
      = (1 << ACTION_TEXT);

   public static final int TOOL_FLAG_MATH
      = (1 << ACTION_MATH);

   public static final int TOOL_FLAG_ANY_TEXT
      = TOOL_FLAG_TEXT | TOOL_FLAG_MATH;

   public static final int TOOL_FLAG_GEOMETRIC_PATHS
      = (1 << ACTION_RECTANGLE)
      | (1 << ACTION_ELLIPSE);

   public static final int TOOL_FLAG_NON_GEOMETRIC_PATHS
      = (1 << ACTION_OPEN_LINE)
      | (1 << ACTION_CLOSED_LINE)
      | (1 << ACTION_OPEN_CURVE)
      | (1 << ACTION_CLOSED_CURVE);

   public static final int TOOL_FLAG_ANY_PATHS
      = TOOL_FLAG_GEOMETRIC_PATHS
      | TOOL_FLAG_NON_GEOMETRIC_PATHS;


   public static final int SEGMENT_MOVE = 0;
   public static final int SEGMENT_LINE = 1;
   public static final int SEGMENT_CURVE = 2;
   public static final int SEGMENT_PARTIAL_MOVE = 3;
   public static final int SEGMENT_PARTIAL_LINE = 4;
   public static final int SEGMENT_PARTIAL_CURVE = 5;
   public static final int SEGMENT_SYMMETRY_LINE = 6;
   public static final int SEGMENT_FIRST = 7;
   public static final int SEGMENT_LAST = 8;
   public static final int SEGMENT_MID = 9;
   public static final int SEGMENT_NONE = 10;

   public static final int SEGMENT_FLAG_MOVE = (1 << SEGMENT_MOVE);
   public static final int SEGMENT_FLAG_LINE = (1 << SEGMENT_LINE);
   public static final int SEGMENT_FLAG_CURVE = (1 << SEGMENT_CURVE);
   public static final int SEGMENT_FLAG_PARTIAL_MOVE = 
      (1 << SEGMENT_PARTIAL_MOVE);
   public static final int SEGMENT_FLAG_PARTIAL_LINE = 
      (1 << SEGMENT_PARTIAL_LINE);
   public static final int SEGMENT_FLAG_PARTIAL_CURVE = 
      (1 << SEGMENT_PARTIAL_CURVE);
   public static final int SEGMENT_FLAG_SYMMETRY_LINE = 
      (1 << SEGMENT_SYMMETRY_LINE);
   public static final int SEGMENT_FLAG_FIRST =
      (1 << SEGMENT_FIRST);
   public static final int SEGMENT_FLAG_LAST =
      (1 << SEGMENT_LAST);
   public static final int SEGMENT_FLAG_MID =
      (1 << SEGMENT_MID);

   public static final int SEGMENT_FLAG_NONE = (1 << SEGMENT_NONE);

   public static final int SEGMENT_FLAG_ANY_FULL =
    (SEGMENT_FLAG_MOVE | SEGMENT_FLAG_LINE | SEGMENT_FLAG_CURVE);
   public static final int SEGMENT_FLAG_ANY_PARTIAL =
    (SEGMENT_FLAG_PARTIAL_MOVE 
   | SEGMENT_FLAG_PARTIAL_LINE
   | SEGMENT_FLAG_PARTIAL_CURVE);

   public static final int SEGMENT_FLAG_ANY =
    (1 << (SEGMENT_NONE+1)) - 1;

   public static final int CONTROL_REGULAR = 0;
   public static final int CONTROL_ANCHORED = 1;
   public static final int CONTROL_SYMMETRY_LINE = 2;
   public static final int CONTROL_PATTERN_ANCHOR = 3;
   public static final int CONTROL_PATTERN_ADJUST = 4;
   public static final int CONTROL_START = 5;
   public static final int CONTROL_END = 6;
   public static final int CONTROL_CURVATURE = 7;
   public static final int CONTROL_CAN_MAKE_JOIN_CONTINUOUS = 8;
   public static final int CONTROL_CAN_ANCHOR = 9;
   public static final int CONTROL_NONE = 10;

   public static final int CONTROL_FLAG_REGULAR
    = (1 << CONTROL_REGULAR);

   public static final int CONTROL_FLAG_ANCHORED
    = (1 << CONTROL_ANCHORED);

   public static final int CONTROL_FLAG_SYMMETRY_LINE
    = (1 << CONTROL_SYMMETRY_LINE);

   public static final int CONTROL_FLAG_PATTERN_ANCHOR
    = (1 << CONTROL_PATTERN_ANCHOR);

   public static final int CONTROL_FLAG_PATTERN_ADJUST
    = (1 << CONTROL_PATTERN_ADJUST);

   public static final int CONTROL_FLAG_START
    = (1 << CONTROL_START);

   public static final int CONTROL_FLAG_END
    = (1 << CONTROL_END);

   public static final int CONTROL_FLAG_CURVATURE
    = (1 << CONTROL_CURVATURE);

   public static final int CONTROL_FLAG_CAN_MAKE_JOIN_CONTINUOUS
    = (1 << CONTROL_CAN_MAKE_JOIN_CONTINUOUS);

   public static final int CONTROL_FLAG_CAN_ANCHOR
    = (1 << CONTROL_CAN_ANCHOR);

   public static final int CONTROL_FLAG_NONE
    = (1 << CONTROL_NONE);

   public static final int CONTROL_FLAG_ANY =
    (1 << (CONTROL_NONE+1)) - 1;

   public static final double HALF_PI = 0.5*Math.PI;

   public static final double QUARTER_PI = 0.25*Math.PI;

   public static final double THREEQUARTER_PI = 0.75*Math.PI;

   public static final double ROOT_3 = Math.sqrt(3.0);

   public static final double HALF_ROOT_3 = 0.5*ROOT_3;

   public static final double ONE_OVER_ROOT_3 = 1.0/ROOT_3;

   public static final double EPSILON = 1e-6;
}
