ifndef JDRSRC
JDRSRC:=$(shell dirname `pwd`)
endif
LIBDIR=$(JDRSRC)/lib
CLASSTREE=com/dickimawbooks

# Java 8 extended support until December 2030
target_version := --release 8 -Xlint:-options

all	: $(LIBDIR)/jdr.jar $(LIBDIR)/jdrresources.jar \
	  $(LIBDIR)/flowframtk.jar $(LIBDIR)/jdrview.jar \
	  $(LIBDIR)/jdrinfo.jar $(LIBDIR)/jdrconverter.jar \
	  $(LIBDIR)/eps2jdr.jar \
	  $(LIBDIR)/jdr2png.jar \
	  $(LIBDIR)/jdr2svg.jar  \
	  $(LIBDIR)/svg2jdr.jar

jdr	: $(LIBDIR)/jdr.jar

jdrresources	: $(LIBDIR)/jdrresources.jar

flowframtk	: $(LIBDIR)/flowframtk.jar

jdrview	: $(LIBDIR)/jdrview.jar

../lib/resources	:
	cd ../lib ; ln -f -s ../resources

../lib/jdr.jar	: $(LIBDIR)/jdr.jar

../lib/jdrresources.jar	: $(LIBDIR)/jdrresources.jar

../lib/jdrview.jar	: $(LIBDIR)/jdrview.jar

../lib/flowframtk.jar	: $(LIBDIR)/flowframtk.jar

../lib/jdrconverter.jar	: $(LIBDIR)/jdrconverter.jar

../lib/jdrinfo.jar	: $(LIBDIR)/jdrinfo.jar

../lib/svg2jdr.jar	: $(LIBDIR)/svg2jdr.jar

../lib/eps2jdr.jar	: $(LIBDIR)/eps2jdr.jar

../lib/jdr2png.jar	: $(LIBDIR)/jdr2png.jar

../lib/jdr2svg.jar	: $(LIBDIR)/jdr2svg.jar

$(LIBDIR)/jdr.jar	: classes/$(CLASSTREE)/jdr/JDRObject.class
	cd classes ; \
	jar cvf $(LIBDIR)/jdr.jar \
	$(CLASSTREE)/jdr/*.class \
	$(CLASSTREE)/jdr/io/*.class \
	$(CLASSTREE)/jdr/io/eps/*.class \
	$(CLASSTREE)/jdr/io/eps/operators/*.class \
	$(CLASSTREE)/jdr/io/svg/*.class \
	$(CLASSTREE)/jdr/exceptions/*.class \
	$(CLASSTREE)/jdr/marker/*.class

$(LIBDIR)/jdrresources.jar	: jdr classes/$(CLASSTREE)/jdrresources/JDRResources.class \
		jdrresources/icons/*.png jdrresources/icons/*/*.png \
		classes/$(CLASSTREE)/jdrresources/icons \
		../lib/resources
		cd classes ; \
		jar cvf $(LIBDIR)/jdrresources.jar \
		$(CLASSTREE)/jdrresources/*.class \
		$(CLASSTREE)/jdrresources/filter/*.class \
		$(CLASSTREE)/jdrresources/numfield/*.class \
		$(CLASSTREE)/jdrresources/icons/*.png \
		$(CLASSTREE)/jdrresources/icons/*/*.png

$(LIBDIR)/flowframtk.jar	: jdr jdrresources classes/$(CLASSTREE)/flowframtk/FlowframTk.class \
		  flowframtk/Manifest.txt \
		  ../images/splashimage.png \
		../lib/resources
		cp -u ../images/splashimage.png classes/
		cd classes ; \
		jar cmf ../flowframtk/Manifest.txt \
	          $(LIBDIR)/flowframtk.jar \
		  splashimage.png \
		  $(CLASSTREE)/flowframtk/*.class \
		  $(CLASSTREE)/flowframtk/dialog/*.class

$(LIBDIR)/jdrview.jar	: jdr jdrresources classes/$(CLASSTREE)/jdrview/JDRView.class \
	jdrview/Manifest.txt \
	../lib/resources
	cd classes ; \
	jar cmf ../jdrview/Manifest.txt \
	   $(LIBDIR)/jdrview.jar \
	   $(CLASSTREE)/jdrview/*.class

$(LIBDIR)/jdrconverter.jar	: classes/$(CLASSTREE)/jdrconverter/JDRConverter.class \
		  jdrconverter/Manifest.txt
		cd classes ; \
		jar cmf ../jdrconverter/Manifest.txt \
	          $(LIBDIR)/jdrconverter.jar \
		  $(CLASSTREE)/jdrconverter/*.class

$(LIBDIR)/jdrinfo.jar	: classes/$(CLASSTREE)/jdrinfo/JdrInfo.class \
		  jdrinfo/Manifest.txt
		cd classes ; \
		jar cmf ../jdrinfo/Manifest.txt \
	          $(LIBDIR)/jdrinfo.jar \
		  $(CLASSTREE)/jdrinfo/*.class

$(LIBDIR)/eps2jdr.jar	: classes/$(CLASSTREE)/eps2jdr/Eps2Jdr.class \
		  eps2jdr/Manifest.txt
		cd classes ; \
		jar cmf ../eps2jdr/Manifest.txt \
	          $(LIBDIR)/eps2jdr.jar \
		  $(CLASSTREE)/eps2jdr/*.class

$(LIBDIR)/jdr2png.jar	: classes/$(CLASSTREE)/jdr2png/Jdr2Png.class \
		  jdr2png/Manifest.txt
		cd classes; \
		jar cmf ../jdr2png/Manifest.txt \
	          $(LIBDIR)/jdr2png.jar \
		  $(CLASSTREE)/jdr2png/*.class

$(LIBDIR)/jdr2svg.jar	: classes/$(CLASSTREE)/jdr2svg/Jdr2Svg.class \
		  jdr2svg/Manifest.txt
		cd classes ; \
		jar cmf ../jdr2svg/Manifest.txt \
	          $(LIBDIR)/jdr2svg.jar \
		  $(CLASSTREE)/jdr2svg/*.class

$(LIBDIR)/svg2jdr.jar	: classes/$(CLASSTREE)/svg2jdr/Svg2Jdr.class \
		  svg2jdr/Manifest.txt
		cd classes ; \
		jar cmf ../svg2jdr/Manifest.txt \
	          $(LIBDIR)/svg2jdr.jar \
		  $(CLASSTREE)/svg2jdr/*.class

classes/$(CLASSTREE)/jdr/JDRObject.class : \
	classes \
	jdr/*.java \
	jdr/marker/*.java \
	jdr/exceptions/*.java \
	jdr/io/*.java \
	jdr/io/eps/*.java \
	jdr/io/eps/operators/*.java  \
	jdr/io/svg/*.java  
	cd jdr ; \
	javac $(target_version) -d ../classes -Xlint:unchecked -Xlint:deprecation \
	  *.java */*.java */*/*.java */*/*/*.java


classes/$(CLASSTREE)/jdrresources/JDRResources.class : \
	classes \
	jdrresources/*.java \
	jdrresources/*/*.java 
	cd jdrresources ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	  -cp $(LIBDIR)/jdr.jar:$(LIBDIR)/texjavahelplib.jar \
	  *.java */*.java

classes/$(CLASSTREE)/flowframtk/FlowframTk.class : \
	jdr jdrresources classes \
	flowframtk/*.java \
	flowframtk/*/*.java
	cd flowframtk ; \
	javac $(target_version) -d ../classes -Xlint:unchecked -Xlint:deprecation \
	  -cp $(LIBDIR)/texjavahelplib.jar:$(LIBDIR)/jdr.jar:$(LIBDIR)/jdrresources.jar \
	  *.java */*.java


classes/$(CLASSTREE)/jdrview/JDRView.class : \
	jdr jdrresources classes \
	jdrview/*.java
	cd jdrview ; \
	javac $(target_version) -d ../classes -Xlint:unchecked  -Xlint:deprecation \
	  -cp $(LIBDIR)/texjavahelplib.jar:$(LIBDIR)/jdr.jar:$(LIBDIR)/jdrresources.jar \
	  *.java

classes/$(CLASSTREE)/jdrconverter/JDRConverter.class : \
	classes \
	jdrconverter/*.java
	cd jdrconverter ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar:$(LIBDIR)/jdrresources.jar:$(LIBDIR)/texjavahelplib.jar \
	*.java

classes/$(CLASSTREE)/jdrinfo/JdrInfo.class : \
	classes \
	jdrinfo/*.java
	cd jdrinfo ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar \
	*.java

classes/$(CLASSTREE)/eps2jdr/Eps2Jdr.class : \
	classes \
	eps2jdr/*.java
	cd eps2jdr ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar:$(LIBDIR)/jdrresources.jar:$(LIBDIR)/texjavahelplib.jar \
	*.java

classes/$(CLASSTREE)/jdr2png/Jdr2Png.class : \
	classes \
	jdr2png/*.java
	cd jdr2png ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar \
	*.java

classes/$(CLASSTREE)/jdr2svg/Jdr2Svg.class : \
	classes \
	jdr2svg/*.java
	cd jdr2svg ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar \
	*.java

classes/$(CLASSTREE)/svg2jdr/Svg2Jdr.class : \
	classes \
	svg2jdr/*.java
	cd svg2jdr ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar \
	*.java

classes	:
	mkdir -p classes

classes/$(CLASSTREE)/jdrresources/icons	:
	mkdir -p classes/$(CLASSTREE)/jdrresources
	cd classes/$(CLASSTREE)/jdrresources; ln -f -s $(JDRSRC)/java/jdrresources/icons


classes/$(CLASSTREE)/jdrview	:
	mkdir -p classes/$(CLASSTREE)/jdrview

classes/$(CLASSTREE)/flowframtk	:
	mkdir -p classes/$(CLASSTREE)/flowframtk

clean	:
	\rm -f $(LIBDIR)/jdr.jar $(LIBDIR)/jdrresources.jar $(LIBDIR)/jdrview.jar
	\rm -f $(LIBDIR)/flowframtk.jar $(LIBDIR)/jdrconverter.jar  $(LIBDIR)/jdrinfo.jar
	\rm -f $(LIBDIR)/eps2jdr.jar $(LIBDIR)/jdr2png.jar
	\rm -f $(LIBDIR)/jdr2svg.jar $(LIBDIR)/svg2jdr.jar
	\rm -f -r classes
