# flowframtk
Vector graphics application that can be used with flowfram.sty LaTeX package

Home page: https://www.dickimaw-books.com/software/flowframtk

The latest release can be obtained from [CTAN](https://ctan.org/pkg/flowframtk).

Under construction (in the process of moving over from a local
non-git repository and changing structure).

FlowframTk is a vector graphics application written in Java. You can use FlowframTk to:

 - Construct shapes using lines, moves and cubic Bézier segments.
 - Edit shapes by changing the defining control points.
 - Add symmetry to shapes.
 - Convert shapes into rotational, scaled or spiral patterns
 - Incorporate text and bitmap images (for annotation and background effects).
 - Display text along a shape.
 - Extract the parameters for TeX's `\parshape` command and for [`\shapepar`](https://ctan.org/pkg/shapepar).
 - Construct frames for use with the [`flowfram`](https://ctan.org/pkg/flowfram) package and export as a class or package.
 - Pictures can be saved in FlowframTk's native open binary format or native open ascii format or can be exported as:
   + a pgfpicture environment for use in LaTeX documents with the pgf package;
   + a complete single-paged LaTeX document that contains the pgf image code;
   + a LaTeX package or class based on the flowfram package;
   + a PNG image file;
   + a PostScript file;
   + a PDF image file;
   + a scalable vector graphics (SVG) image file.
   (Export functions for PS, PDF and SVG all require a TeX distribution.) 
 - Alternative text can be specified to use when exporting to a LaTeX file.
 - Mappings can be used to specify what LaTeX font declarations should be used when exporting to a LaTeX file.
 - Accompanying Applications: 
   + `jdrview`: A lightweight jdr/ajr image viewer
   + `jdrutils`: A suite of command line converters

License GPLv3+: GNU GPL version 3 or later
http://gnu.org/licenses/gpl.html
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.
