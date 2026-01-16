package com.dickimawbooks.jdr.io.svg;

public class CoordPairsRequiredException extends SVGException
{
   public CoordPairsRequiredException(SVGHandler h, String name)
   {
      super(h, String.format("Coord pairs required in: %s", name));

      attributeName = name;
   }

   public CoordPairsRequiredException(SVGAbstractElement elem, String name)
   {
      super(elem, String.format("Coord pairs required in: %s", name));

      attributeName = name;
   }

   public CoordPairsRequiredException(SVGHandler h, String name,
     Throwable cause)
   {
      super(h, String.format( "Coord pairs required in: %s", name), cause);

      attributeName = name;
   }

   public CoordPairsRequiredException(SVGAbstractElement elem, String name,
     Throwable cause)
   {
      super(elem, String.format( "Coord pairs required in: %s", name), cause);

      attributeName = name;
   }

   public String getAttributeName()
   {
      return attributeName;
   }

   @Override
   public String getLocalizedMessage()
   {
      String msg = handler.getMessageWithFallback(
        "error.svg.coord_pairs_expected",
        "co-ordinate pairs x1 y1 x2 y2 required in {0}",
         attributeName);

      if (element != null)
      {
         msg = handler.getMessageWithFallback(
          "error.svg.element_msg_prefix",
          "Element {0}: {1}",
           element.getName(), msg);
      }

      return msg;
   }

   String attributeName;
}
