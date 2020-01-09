\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{flowframtk}[2019/12/14]

\PassOptionsToClass{oneside}{scrbook}
\DeclareOption*{\PassOptionsToClass{\CurrentOption}{scrbook}}

\ProcessOptions

\LoadClass{scrbook}

\RequirePackage{etoolbox}
\RequirePackage{amsthm}
\RequirePackage{graphicx}
\RequirePackage[x11names]{xcolor}
\RequirePackage{pgf}

\PassOptionsToPackage{stylemods={tree,bookindex}}{glossaries-extra}
\PassOptionsToPackage{linkcolor=Blue4,urlcolor=DeepPink4}{hyperref}

\pagenumbering{alph}
\renewcommand{\@author}{}

\publishers{\includepgf{title}}

\newcommand{\Author}[1]{%
  \ifdefempty\@author
  {\def\@author{#1}}%
  {\appto\@author{\and #1}}%
}

% Default definition.
% (Overridden by context label in dictionary file.)
\providecommand{\idxpackagename}{package}

\newcommand{\appfmt}[1]{\texorpdfstring{\textsf{#1}}{#1}}

\newcommand{\clinoindex}[1]{\texorpdfstring{\texttt{#1}}{#1}}

\newcommand{\stynoindex}[1]{\texorpdfstring{\textsf{#1}}{#1}}

\newcommand{\clsnoindex}[1]{\texorpdfstring{\textsf{#1}}{#1}}

\newcommand{\envnoindex}[1]{\texorpdfstring{\textsf{#1}}{#1}}

\newcommand{\extnoindex}[1]{\texorpdfstring{\texttt{.#1}}{.#1}}

\newcommand{\optnoindex}[1]{\texorpdfstring{\texttt-\/-#1}{--#1}}

\newcommand*{\titleref}[2]{\htmlref{#1}{#2}}

\newcommand*{\menu}[1]{\textsf{#1}}
\newcommand*{\mto}{$\to$}

\newrobustcmd*{\dash}{\,---\,}

\newcommand*{\lessthan}{\ifmmode<\else\textless\fi}
\newcommand*{\greaterthan}{\ifmmode>\else\textgreater\fi}
\newcommand*{\nbsp}{~}
\newcommand*{\doublequote}{"}
\newcommand*{\ampersand}{\&}
\newcommand*{\leftbrace}{\{}
\newcommand*{\rightbrace}{\}}
\newcommand*{\underscore}{\_}
\newcommand*{\textpercent}{\%}
\newcommand*{\texthash}{\#}
\newcommand{\floatconts}[3]{%
  \@ifundefined{\@captype conts}{\tableconts{#1}{#2}{#3}}%
  {\csname\@captype conts\endcsname{#1}{#2}{#3}}%
}

\newcommand{\tableconts}[3]{%
  #2\label{#1}\vskip\baselineskip
  {\centering #3\par}%
}

\newcommand{\figureconts}[3]{%
  {\centering #3\par}%
  \vskip\baselineskip
  #2\label{#1}%
}

\newcommand{\continuesymbol}{$\hookleftarrow$}

\newcommand*{\continue@symbol}{%
 \smash{\raisebox{-1.6ex}{\makebox[0pt][r]{\continuesymbol}}}}

\newcommand*{\continueline}{\continue@symbol\newline}

\newcommand*{\sectionautorefname}{Section}

\newcommand*{\dq}[1]{``#1''}

\newcommand*{\meta}[1]{\ensuremath{\langle}\textit{#1}\ensuremath{\rangle}}

\newcommand{\marg}[1]{\texttt{\char`\{#1\char`\}}}

\newcommand{\csnoindex}[1]{\texorpdfstring{\texttt{\char`\\#1}}{\glsbackslash#1}}

\newrobustcmd*{\cs}[1]{\gls{cs_#1}}

\newcommand*{\button}[1]{\textsf{\bfseries #1}}

\newcommand*{\element}[1]{\texttt{\char`\<#1\char`\>}}

\newcommand*{\file}[1]{\texttt{#1}}

% Default definition.
% (Overridden by context label in dictionary file.)
\providecommand{\idxenvname}{environment}

\newenvironment{boilerplate}{\clearpage\thispagestyle{empty}}{\clearpage}

\newtheoremstyle{example}% style name
{2ex}% above space
{2ex}% below space
{}% body font
{}% indent amount
{\bfseries}% head font
{.}% post head punctuation
{\newline}% post head punctuation
{}% head spec 

% Default definition.
% (Overridden by context label in dictionary file.)
\providecommand{\examplename}{Example}

\theoremstyle{example}
\newtheorem{example}{\examplename}

\newcommand{\preresourcehook}{\setabbreviationstyle{long-short-desc}}

\newcommand*{\printmainglossary}{%
 \printunsrtglossary*[style=indexgroup,nonumberlist]
 {%
   \renewcommand{\printunsrtglossaryentryprocesshook}[1]{%
     \ifglshasdesc{##1}{}{\printunsrtglossaryskipentry}%
   }%
   \renewcommand{\glstreeitem}{%
    \parindent0pt\glspar\hangindent20pt
    \everypar{\parindent30pt\hangindent20pt}}%
   \renewcommand{\glstreepredesc}{\endgraf\nopagebreak\glstreeitem\parindent\hangindent}%
   \let\glstreenamefmt\textsf
   \renewcommand*{\glstreePreHeader}[2]{\pdfbookmark[1]{##2}{\currentglossary.##1}}%
   \renewcommand*{\glstreegroupheaderfmt}[1]{\hfill\textit{##1}\hfill}%
 }%
}
\newcommand*{\printmainindex}{%
 \printunsrtglossary*[target=false,title=\indexname,style=bookindex]
 {%
   \renewcommand*{\glstreegroupheaderfmt}[1]{\textit{##1}}%
   \renewcommand*{\glsxtrbookindexname}[1]{%
     \ifglshasdesc{##1}%
     {\glshyperlink[\glossentryname{##1}]{##1}}%
     {\glsdohypertarget{\glolinkprefix##1}{\glossentryname{##1}}}%
   }%
   \renewcommand*{\glsxtrbookindexprelocation}[1]{%
     \glsxtrifhasfield{location}{##1}%
     {\,\textcolor{lightgray}{\dotfill}\,}%
     {\glsxtrprelocation}%
   }%
 }%
}

\newcommand*{\figureref}[1]{%
\hyperlink{#1top}{\figurename~\ref*{#1}}}

\newcommand{\tableref}[1]{\htmlref{\tablename~\ref*{#1}}{#1}}

\newcommand{\fndagmark}{\makebox[0pt][r]{\textsuperscript{\dag}\,}}

\newcommand{\degrees}[1]{\ensuremath{#1^\circ}}

% keys for \includePGF
\define@key{includepgf}{angle}{%
   \sbox\@pgfimagebox{\rotatebox{#1}{\usebox\@pgfimagebox}}}
\define@key{includepgf}{scale}{%
   \sbox\@pgfimagebox{\scalebox{#1}{\usebox\@pgfimagebox}}}
\define@key{includepgf}{width}{%
   \def\@pgfimagebox@w{#1}%
   \protected@edef\@doresize{\noexpand\resizebox{#1}{\@pgfimagebox@h}}%
   \sbox\@pgfimagebox{\@doresize{\usebox\@pgfimagebox}}%
}
\define@key{includepgf}{height}{%
   \def\@pgfimagebox@h{#1}%
   \sbox\@pgfimagebox{\expandafter\resizebox\expandafter
     {\@pgfimagebox@w}{#1}{\usebox\@pgfimagebox}}%
}

\def\@findfile#1#2{%
\def\@tmp{#2}%
\ifx\@tmp\@nnil
  \let\@donext\@gobble
\else
  \IfFileExists{#2/#1}{\def\@findfiledir{#2}%
     \let\@donext\@gobbletonil
   }{\let\@donext\@findfile}%
\fi
\@donext{#1}%
}

\def\@gobbletonil#1\@nil{}

\newsavebox\@pgfimagebox
% input a PGF image
\newcommand*{\includepgf}[2][]{%
\filename@parse{#2}%
\ifx\filename@area\@empty
  \def\@thisfile{#2}%
  \expandafter\@findfile\expandafter\@thisfile\Ginput@path\@nil
  \ifx\@findfiledir\undefined
     \sbox\@pgfimagebox{\input{#2}}%
  \else
     \sbox\@pgfimagebox{\input{\@findfiledir/#2}}%
  \fi
\else
  \sbox\@pgfimagebox{\input{#2}}%
\fi
\def\@pgfimagebox@w{!}%
\def\@pgfimagebox@h{!}%
\setkeys{includepgf}{#1}%
\usebox{\@pgfimagebox}}

% Don't use file extension!
% if tex file located, use that, otherwise assume image file
\newcommand*{\includeimg}[2][]{%
\def\@thisfile{#2}%
\expandafter\@findfile\expandafter\@thisfile\Ginput@path\@nil
\ifx\@findfiledir\undefined
   \vcentre{\includegraphics[#1]{#2}}%
\else
   \vcentre{\includepgf[#1]{\@findfiledir/#2}\drafttag{\@findfiledir/#2.tex}}%
\fi
}

\newcommand*{\vcentre}[1]{\raisebox{-0.5\height}{#1}}

\newcommand*{\includetimg}[2][]{%
\def\@thisfile{#2}%
\expandafter\@findfile\expandafter\@thisfile\Ginput@path\@nil
\ifx\@findfiledir\undefined
   \raisebox{-\height}{\includegraphics[#1]{#2}}%
\else
   \raisebox{-\height}{\includepgf[#1]{\@findfiledir/#2}\drafttag{\@findfiledir/#2.tex}}%
\fi
}

\newcommand*{\includebimg}[2][]{%
\def\@thisfile{#2}%
\expandafter\@findfile\expandafter\@thisfile\Ginput@path\@nil
\ifx\@findfiledir\undefined
   \includegraphics[#1]{#2}%
\else
   \includepgf[#1]{\@findfiledir/#2}\drafttag{\@findfiledir/#2.tex}%
\fi
}

% add a tag if using draft mode
\newcommand{\drafttag}[2][r]{\ifdim\overfullrule>0pt\relax
\makebox[0pt][#1]{{\small\color{gray} #2}}\fi}

\newcommand*\bitem[1]{%
 \item[\raisebox{-12bp}%
 {%
   \@for\@this@img:=#1\do{%
    \includegraphics[trim=3 3 3 3,clip,height=24bp]{\@this@img}%
   }%
 }]
}
\newcommand*{\buttonlistlabel}[1]{\smash{#1}}

\newenvironment{buttonlist}
{%
  \list{}%
  {%
   \setlength\labelwidth{24bp}%
   \setlength\leftmargin{34bp}%
   \setlength{\itemsep}{12.0pt plus 2.0pt minus 1.0pt}%
   \let\makelabel\buttonlistlabel
  }%
}
{
 \endlist
 \smallskip
}

\pagestyle{headings}
\renewcommand*{\sectionmark}[1]{%
\markright{\thesection\quad\MakeUppercase{#1}}}
\renewcommand*{\subsectionmark}[1]{%
\markright{\thesubsection\quad\MakeUppercase{#1}}}

\newcommand*{\radialcoords}[2]{$(#1:#2)$}
\newcommand*{\radiusmark}{\ensuremath{r}}
\newcommand*{\anglemark}{\ensuremath{\theta}}

\newcommand*{\hydash}{-\discretionary{}{}{}}
\newcommand*{\hyslash}{/\discretionary{}{}{}}

\newenvironment{syntax}%
{\par\vskip\baselineskip\par\noindent\ignorespaces}
{\par\vskip\baselineskip\par\noindent\ignorespacesafterend}

\newcommand*\makesubnumberedenums{%
  \renewcommand*{\theenumi}{\arabic{enumi}}%
  \renewcommand*{\labelenumi}{\theenumi.}%
  \renewcommand*{\theenumii}{\theenumi.\arabic{enumii}}%
  \renewcommand*{\labelenumii}{\theenumii.}%
  \renewcommand*{\theenumiii}{\theenumii.\arabic{enumiii}}%
  \renewcommand*{\labelenumiii}{\theenumiii.}%
  \renewcommand*{\theenumiv}{\theenumiii.\arabic{enumiv}}%
  \renewcommand*{\labelenumiv}{\theenumiv.}%
  \renewcommand*{\p@enumi}{}%
  \renewcommand*{\p@enumii}{}%
  \renewcommand*{\p@enumiii}{}%
  \renewcommand*{\p@enumiv}{}%
}

\newcommand*{\restoreenums}{%
  \renewcommand*{\theenumi}{\arabic{enumi}}%
  \renewcommand*{\labelenumi}{\theenumi.}%
  \renewcommand*{\theenumii}{\alph{enumii}}%
  \renewcommand*{\labelenumii}{(\theenumii)}%
  \renewcommand*{\theenumiii}{\roman{enumiii}}%
  \renewcommand*{\labelenumiii}{\theenumiii.}%
  \renewcommand*{\theenumiv}{\Alph{enumiv}}%
  \renewcommand*{\labelenumiv}{\theenumiv.}%
  \renewcommand*{\p@enumi}{}%
  \renewcommand*{\p@enumii}{\theenumi}%
  \renewcommand*{\p@enumiii}{\p@enumii(\thenumii)}%
  \renewcommand*{\p@enumiv}{\p@enumiii\theenumiii}%
}

\newcommand{\sheareq}{%
\begin{displaymath}
\left(
\begin{array}{cc}
1 & s_x\\
s_y & 1
\end{array}
\right)
\left(
\begin{array}{c}
x\\
y
\end{array}
\right)
=
\left(
\begin{array}{l}
x+s_xy\\
y+s_yx
\end{array}
\right)
\end{displaymath}}

\@ifundefined{sharedimagesdir}
{%
   \newcommand*{\sharedimagesdir}{../sharedimages}
   \newcommand*{\imagesdir}{./images-\applang}
}
{}
\providecommand*{\iconsdir}{../../java/jdrresources/icons}
\providecommand*{\buttonsdir}{../../java/jdrresources/icons/buttons}
\graphicspath{{\imagesdir/}{\sharedimagesdir/}{\iconsdir/}{\buttonsdir/}}

\newlength\subcapskip
\setlength\subcapskip{1.5\baselineskip}

\newcommand*{\subfigcapfmt}[1]{(\emph{#1})}

\newcommand*{\subfigcaption}[1]{\rule{0pt}{\subcapskip}\subfigcapfmt{#1}}

% \centfigure[short cap]{label}{image}{caption}
\newcommand{\centfigure}[4][]{%
  \begin{figure}[htbp]%
    \@centfigure@body{#1}{#2}{#3}{#4}%
  \end{figure}%
}

\newcommand{\@centfigure}[4][htbp]{%
  \begin{figure}[htbp]%
    \@centfigure@body{#1}{#2}{#3}{#4}%
  \end{figure}%
}

\newcommand{\@centfigure@body}[4]{%
  \hypertarget{#2top}{}
  \centering
  #3%
  \par
  \def\cap@text{#1}%
  \ifx\cap@text\@empty
    \caption{#4}%
  \else
    {%
     \def\dash{---}%
     \def\LaTeX{LaTeX}%
     \def\TeX{TeX}%
     \let\ \space
     \protected@xdef\cap@text{#1}%
    }%
    \expandafter\caption\expandafter[\cap@text]{#4}%
  \fi
  \label{#2}%
}

\renewcommand{\l@figure}{%
  \@dottedtocline{1}{1.5em}{3.5em}%
}

\renewcommand{\l@table}{%
  \@dottedtocline{1}{1.5em}{3.5em}%
}

% \sidebysidefigs{pre-text}[short cap 1]{label1}{image1}{caption1}[short cap
% 2]{label2}{image2}{caption2}
\newcommand{\sidebysidefigs}[1]{%
  #1%
 \begin{figure}[htbp]
 \@sidebysidefigs
}

\newcommand{\@sidebysidefigs}[4][]{%
  \begin{minipage}{0.45\linewidth}%
    \@centfigure@body{#1}{#2}{#3}{#4}%
  \end{minipage}\hfill
  \@@sidebysidefigs
}

\newcommand{\@@sidebysidefigs}[4][]{%
  \begin{minipage}{0.45\linewidth}%
    \@centfigure@body{#1}{#2}{#3}{#4}%
  \end{minipage}%
  \end{figure}
}

\newlength\iconheight
\newlength\iconwidth
\newsavebox\iconsbox
\newcommand{\iconstartpar}[1][-3]{%
  \def\@iconstartpar@hangafter{#1}%
  \@iconstartpar
}

\newcommand{\@iconstartpar}[2][]{%
  \par\vspace{\baselineskip}%
  \sbox{\iconsbox}{\@for\this@icon:=#2\do{\includegraphics[#1]{\this@icon}}}%
  \settoheight{\iconheight}{\usebox{\iconsbox}}%
  \settowidth{\iconwidth}{\usebox{\iconsbox}}
  \advance\iconheight by -0.5\baselineskip
  \advance\iconwidth by 10pt\relax
  \hangindent=\iconwidth
  \hangafter=\@iconstartpar@hangafter\relax
  \noindent\makebox[0pt][l]{\hspace*{-\iconwidth}\smash{\raisebox{-\iconheight}{\usebox{\iconsbox}}}}%
}

\newcommand{\texrelated}{\iconstartpar{texrelated.png}}
\newcommand{\pgfrelated}{\iconstartpar{pgfrelated.png}}
\newcommand{\flfrelated}{\iconstartpar{flfrelated.png}}
\newcommand{\warning}{\iconstartpar{warning.png}}

\endinput
