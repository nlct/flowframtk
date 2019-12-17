all	: flowframtk-en-GB.pdf flowframtk-en-US.pdf

../../lib/resources/helpsets	:
				 mkdir -p ../../lib/resources/helpsets

../../lib/resources/helpsets/sharedimages	: ../../lib/resources/helpsets
				 cp -r ../sharedimages ../../lib/resources/helpsets

../../lib/resources/helpsets/flowframtk/en-GB	:
				 mkdir -p ../../lib/resources/helpsets/flowframtk/en-GB

../../lib/resources/helpsets/flowframtk/en-GB/images-en-GB	: ../../lib/resources/helpsets/flowframtk/en-GB
				 cp -r images-en-GB ../../lib/resources/helpsets/flowframtk/en-GB/

flowframtk-en-GB.pdf	: flowframtk-en-GB.tex flowframtk.cls
			arara flowframtk-en-GB

flowframtk-en-GB.tex	: createflowframtkdocs flowframtk-en.xml \
			../../lib/resources/helpsets/sharedimages \
			../../lib/resources/helpsets/flowframtk/en-GB/images-en-GB \
			../../lib/resources/dictionaries/flowframtk-en-GB.prop \
			../../java/flowframtk/FlowframTkInvoker.java
			createflowframtkdocs FlowframTk en GB

../../lib/resources/helpsets/flowframtk/en-US	:
				 mkdir -p ../../lib/resources/helpsets/flowframtk/en-US

../../lib/resources/helpsets/flowframtk/en-US/images-en-US	: ../../lib/resources/helpsets/flowframtk/en-US
				 cp -r images-en-US ../../lib/resources/helpsets/flowframtk/en-US/

flowframtk-en-US.pdf	: flowframtk-en-US.tex flowframtk.cls
			arara flowframtk-en-US

flowframtk-en-US.tex	: createflowframtkdocs flowframtk-en.xml \
			../../lib/resources/helpsets/sharedimages \
			../../lib/resources/helpsets/flowframtk/en-US/images-en-US \
			../../lib/resources/dictionaries/flowframtk-en-US.prop \
			../../java/flowframtk/FlowframTkInvoker.java
			createflowframtkdocs FlowframTk en US

clean	:
	flowframtk-en-GB.aux
	flowframtk-en-GB.glg
	flowframtk-en-GB.glstex
	flowframtk-en-GB.log
	flowframtk-en-GB.out
	flowframtk-en-GB.toc
	flowframtk-en-US.aux
	flowframtk-en-US.glg
	flowframtk-en-US.glstex
	flowframtk-en-US.log
	flowframtk-en-US.out
	flowframtk-en-US.toc

squeaky	:
	flowframtk-en-GB.tex
	flowframtk-en-GB.bib
	flowframtk-en-GB.pdf
	flowframtk-en-US.tex
	flowframtk-en-US.bib
	flowframtk-en-US.pdf

