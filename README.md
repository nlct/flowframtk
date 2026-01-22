# flowframtk
Vector graphics application that can be used with flowfram.sty LaTeX package

Home page: https://www.dickimaw-books.com/software/flowframtk

Screenshots: https://www.dickimaw-books.com/software/flowframtk/screenshots/

The latest public release can be obtained from [CTAN](https://ctan.org/pkg/flowframtk).
Newer releases available for testing can be found in the [Releases
page](https://github.com/nlct/flowframtk/releases).

FlowframTk is a vector graphics application written in Java. You can use FlowframTk to:

 - Construct shapes using lines, moves and cubic Bézier segments.
 - Edit shapes by changing the defining control points.
 - Add symmetry to shapes.
 - Convert shapes into rotational, scaled or spiral patterns
 - Incorporate text and bitmap images (for annotation and background effects).
 - Display text along a shape.
 - Extract the parameters for TeX's `\parshape` command and for [`\shapepar`](https://ctan.org/pkg/shapepar).
 - Construct frames for use with the [`flowfram`](https://ctan.org/pkg/flowfram) package and export as a class or package.
 - Pictures can be saved in FlowframTk's native open binary format (`.jdr`) or native open plain text format (`.ajr`) or can be exported as:
   + a `pgfpicture` environment for use in LaTeX documents with the [`pgf`](https://ctan.org/pkg/pgf) package;
   + a complete single-paged LaTeX document that contains the `pgf` image code;
   + a LaTeX package or class based on the `flowfram` package;
   + a PNG image file (full-paged or cropped);
   + a PDF image file (full-paged or cropped);
   + a PDF multi-paged document that uses the `flowfram` package for
     the layout;
   + a PostScript (PS) file;
   + a scalable vector graphics (SVG) image file.
   (Export functions for PDF require a TeX distribution.
    Export functions for PNG, PS, SVG may need a TeX distribution,
    depending on the export setting.
    The export to PS or SVG functions now need to be explicitly
    enabled.) 
 - Alternative text can be specified to use when exporting to a LaTeX file.
 - Mappings can be used to specify what LaTeX font declarations should be used when exporting to a LaTeX file.
 - Accompanying Applications: 
   + `jdrview`: A lightweight jdr/ajr image viewer
   + `jdrconverter`: converts to or from jdr/ajr file format

## Source Code

The source code depends on the [TeX Java Help library](https://github.com/nlct/texjavahelp) and the [TeX Parser Library](https://github.com/nlct/texparser).
The `texjavahelplib.jar` and `texparserlib.jar` libraries needs to be added to the `lib`
directory.

The documentation has LaTeX source code but depends on the
applicable language XML files in the `lib/resources/dictionaries`
directory. The TeX Java Help Library comes with some command line
tools that are used to build the documentation:

 - `tjhxml2bib` converts the XML language files to Bib2Gls bib files
   (this allows the documentation to pick up the widget text and
   menu hierarchy);
 - `texjavahelpmk` needs to be run after the PDF is created (so that
   it can pick up the table of contents, cross-references etc)
   and creates the HTML and XML files used for the in-application
   help.

The `version.tex` file just contains `\date` and is automatically
created to pick up the current version and date information from the
Java source:
```bash
echo "\\date{Version " > version.tex
grep 'String APP_VERSION = ' $baseclass | sed "s/public\sstatic\sfinal\sString\sAPP_VERSION\s=//" | tr -d "\"\; " >> version.tex
grep 'String APP_DATE = ' $baseclass | sed "s/public\sstatic\sfinal\sString\sAPP_DATE\s=//" | tr -d "\"\; " >> version.tex
echo "}" >> version.tex
```
where `$baseclass` is the path to `JDRResources.java`.

For example, to create flowframtk-en.pdf:

Create `flowframtk-props-en.bib` from the XML dictionaries
    (where `$dictdir` is the path to `lib/resources/dictionaries`):
```bash
tjhxml2bib --copy-overwrite $dictdir/texjavahelplib-en.xml $dictdir/flowframtk-en.xml $dictdir/jdrcommon-en.xml -o flowframtk-props-en.bib
```

Build the document. This can be done with Arara:
```bash
arara flowframtk-en
```

If successful, the HTML and XML files can now be created (where
`$targetdir` is the path to `lib/resources/helpsets`):
```bash
texjavahelpmk flowframtk-en.tex $targetdir/flowframtk/en
```

## Licence

License GPLv3+: GNU GPL version 3 or later
http://gnu.org/licenses/gpl.html
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.
