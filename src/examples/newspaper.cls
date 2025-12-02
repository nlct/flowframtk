% Created by FlowframTk version 0.8.9
% 2 Dec 2025, 16:28:11
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{newspaper}
\RequirePackage{flowframtkutils}
\RequirePackage[utf8]{inputenc}
\RequirePackage[T1]{fontenc}
\RequirePackage{lmodern}

\FlowFramTkDeclareKeys{newspaper}
\ExplSyntaxOn
\DeclareUnknownKeyHandler[newspaper]{
  \bool_lazy_or:nnTF
   { \tl_if_empty_p:n { #2 } }
   { \tl_if_novalue_p: { #2 } }
  { \PassOptionsToClass { #1 } {article} }
  { \PassOptionsToClass { #1 = { #2 } } {article} }
}
\ExplSyntaxOff
\FlowFramTkSetKey{pages=relative}
\ProcessKeyOptions[newspaper]
\PassOptionsToClass{12pt}{article}
\LoadClass{article}
\RequirePackage[a4paper,portrait]{geometry}
\geometry{lmargin=58.0pt,rmargin=58.0pt,tmargin=58.0pt,bmargin=58.0pt}
\setlength{\evensidemargin}{\oddsidemargin}
\addtolength{\evensidemargin}{54.0pt}
\RequirePackage{flowfram}
\newstaticframe[all]{479.503784pt}{88.003754pt}{-0.501877pt}{641.544974pt}[title]

\setstaticframe*{title}{valign=c}
\setstaticcontents*{title}{\begin{center}
\bfseries\Huge
Fairy Tale Times
\end{center}
\hfill Issue 2. 7 December 2005.}
\newdynamicframe[all]{233.003738pt}{30.003754pt}{-0.501877pt}{598.044974pt}[leftheading]

\setdynamicframe*{leftheading}{valign=c}
\setdynamiccontents*{leftheading}{\section{Killer Wolf on the Loose}}
\newdynamicframe[all]{233.003754pt}{30.003754pt}{245.998138pt}{598.044974pt}[rightheading]

\setdynamicframe*{rightheading}{valign=c}
\setdynamiccontents*{rightheading}{\section{Tragic Wall Accident}}
\newdynamicframe[all]{479.503784pt}{305.503723pt}{-0.501877pt}{279.045004pt}[lefttext]

\setdynamicframe*{lefttext}{shape={\parshape=22
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 232.0pt 
0.0pt 479.0pt 
0.0pt 479.0pt 
0.0pt 479.0pt 
0.0pt 479.0pt 
0.0pt 479.0pt 
0.0pt 479.0pt 
0.0pt 479.0pt }}
\setdynamicframe*{lefttext}{valign=t}
\setdynamiccontents*{lefttext}{The authorities are warning of a killer wolf on the
loose. He has so far devoured an old grandmother and
two pig brothers. He is described as being furry with
big eyes and big teeth.

On Monday this week he broke into a house, and devoured
an old lady. He then disguised himself as the old lady
in order to deceive her granddaughter. Luckily for the little
girl a woodsman arrived in time to rescue her. Parents are
being cautioned not to let their children wander about on
their own, and to remind them not to talk to strangers.

The next day the wolf struck again, this time targeting two
pig brothers who had most incautiously made their dwellings
on the cheap using inadequate materials. The wolf also made
an attempt on the third pig brother, but was unable to break
into his house.

Police are appealing to the public for witnesses, and remind
people to keep their doors securely fastened at all times.

``Always ask to see identification,'' said one police advisor,
``and invest in improving the general security of your property.''
}
\newstaticframe[all]{71.266251pt}{76.285004pt}{414.8pt}{504.761847pt}[eggimage]

% Border command for frame 'eggimage'
\flowframtkNewFrameBorder{eggimage}{%
\begin{pgfpicture}{0pt}{0pt}{71.266251pt}{76.285004pt}
\pgfputat{\pgfpoint{-0.0pt}{-0.0pt}}{%
\begin{pgfscope}
\pgftransformcm{1.0}{0.0}{0.0}{1.0}{\pgfpoint{0.0pt}{76.285004pt}}
\pgflowlevelsynccm
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[top,left]{\pgfimage[width=71.26625pt,height=76.285pt]{egg}}}
\end{pgfscope}
}
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[left,bottom]{#1}}
\end{pgfpicture}}
\setstaticframe*
{eggimage}{offset=0pt,border={\flowframtkUseFrameBorderCsName{eggimage}}}

\setstaticframe*{eggimage}{valign=c}
\newdynamicframe[all]{233.003754pt}{204.003738pt}{245.998138pt}{380.544989pt}[rightext]

\setdynamicframe*{rightext}{shape={\parshape=14
0.0pt 189.0pt 
0.0pt 189.0pt 
0.0pt 174.0pt 
0.0pt 160.0pt 
0.0pt 174.0pt 
0.0pt 174.0pt 
0.0pt 233.0pt 
0.0pt 233.0pt 
0.0pt 233.0pt 
0.0pt 233.0pt 
0.0pt 233.0pt 
0.0pt 233.0pt 
0.0pt 233.0pt 
0.0pt 233.0pt }}
\setdynamicframe*{rightext}{valign=t}
\setdynamiccontents*{rightext}{An egg person tragically fell from a six foot wall yesterday
afternoon and was smashed to pieces. The king’s cavalry rushed
to the scene, but regretted that they were unable to help him.

Humpty Dumpty was believed to be sitting on the wall when he fell.
Police have ruled out foul play, but
are advising people not to play on high walls, particularly
those vulnerable members of the population suffering from
eggshell syndrome.

\small\em
Exclusive interview with one of the King's men on page 6.
}
\newstaticframe[all]{239.897156pt}{254.953384pt}{238.607849pt}{372.999365pt}[Lbar]

% Border command for frame 'Lbar'
\flowframtkNewFrameBorder{Lbar}{%
\begin{pgfpicture}{0pt}{0pt}{239.897156pt}{254.953384pt}
\pgfputat{\pgfpoint{-0.0pt}{-0.0pt}}{%
\begin{pgfscope}
\pgfsetlinewidth{1.0bp}
\pgfsetrectcap
\pgfsetmiterjoin
\pgfsetmiterlimit{10.0}
\pgfpathmoveto{\pgfpoint{0.502151pt}{254.451235pt}}
\pgfpathlineto{\pgfpoint{0.642151pt}{0.797485pt}}
\pgfpathlineto{\pgfpoint{239.394651pt}{0.502485pt}}
\definecolor{strokepaint}{rgb}{0.0,0.0,0.0}\pgfsetstrokecolor{strokepaint}
\pgfusepath{stroke}
\end{pgfscope}
}
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[left,bottom]{#1}}
\end{pgfpicture}}
\setstaticframe*
{Lbar}{offset=0pt,border={\flowframtkUseFrameBorderCsName{Lbar}}}

\setstaticframe*{Lbar}{valign=c}
\newstaticframe[all]{478.788757pt}{1.003723pt}{-0.284374pt}{271.621252pt}[hline]

% Border command for frame 'hline'
\flowframtkNewFrameBorder{hline}{%
\begin{pgfpicture}{0pt}{0pt}{478.788757pt}{1.003723pt}
\pgfputat{\pgfpoint{-0.0pt}{-0.0pt}}{%
\begin{pgfscope}
\pgfsetlinewidth{1.0bp}
\pgfsetrectcap
\pgfsetmiterjoin
\pgfsetmiterlimit{10.0}
\pgfpathmoveto{\pgfpoint{0.501874pt}{0.501848pt}}
\pgfpathlineto{\pgfpoint{478.286874pt}{0.501848pt}}
\definecolor{strokepaint}{rgb}{0.0,0.0,0.0}\pgfsetstrokecolor{strokepaint}
\pgfusepath{stroke}
\end{pgfscope}
}
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[left,bottom]{#1}}
\end{pgfpicture}}
\setstaticframe*
{hline}{offset=0pt,border={\flowframtkUseFrameBorderCsName{hline}}}

\setstaticframe*{hline}{valign=c}
\newdynamicframe[all]{479.503784pt}{30.003784pt}{-0.501877pt}{235.544958pt}[bottomheading]

\setdynamicframe*{bottomheading}{valign=t}
\setdynamiccontents*{bottomheading}{\section{Relief as Missing Sheep
Finally Return Home}}
\newflowframe[all]{233.003738pt}{218.503784pt}{-0.501877pt}{3.544958pt}[left]

\setflowframe*{left}{margin=outer}
\newflowframe[all]{233.003754pt}{218.503784pt}{245.998138pt}{3.544958pt}[right]

\setflowframe*{right}{margin=outer}
\newstaticframe[all]{58.217499pt}{46.172501pt}{-8.0pt}{184.87435pt}[sheepimage]

% Border command for frame 'sheepimage'
\flowframtkNewFrameBorder{sheepimage}{%
\begin{pgfpicture}{0pt}{0pt}{58.217499pt}{46.172501pt}
\pgfputat{\pgfpoint{-0.0pt}{-0.0pt}}{%
\begin{pgfscope}
\pgftransformcm{1.0}{0.0}{0.0}{1.0}{\pgfpoint{0.0pt}{46.172501pt}}
\pgflowlevelsynccm
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[top,left]{\pgfimage[width=58.2175pt,height=46.1725pt]{sheep}}}
\end{pgfscope}
}
\pgfputat{\pgfpoint{0pt}{0pt}}{\pgftext[left,bottom]{#1}}
\end{pgfpicture}}
\setstaticframe*
{sheepimage}{offset=0pt,border={\flowframtkUseFrameBorderCsName{sheepimage}}}

\setstaticframe*{sheepimage}{valign=c}
\newdynamicframe[all]{30.003784pt}{30.003784pt}{492.498108pt}{-39.955042pt}[footer]

\setdynamicframe*
{footer}{evenx=-40.994018pt,eveny=-39.955042pt}

\setdynamicframe*{footer}{valign=c,style={itshape}}
\flowframtkSetDynamicOddFoot{footer}
\flowframtkMakeDFHeaderFooter
% suppress section numbering
\setcounter{secnumdepth}{0}
% set the paragraph indentation for static
% and dynamic frames
\setlength{\sdfparindent}{\parindent}

\endinput
