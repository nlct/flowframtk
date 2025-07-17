LIBDIR=$(JDRSRC)/lib
CLASSTREE=com/dickimawbooks

# Java 8 extended support until December 2030
target_version := --release 8 -Xlint:-options

all	: $(LIBDIR)/jdr.jar $(LIBDIR)/jdrresources.jar \
	  $(LIBDIR)/flowframtk.jar $(LIBDIR)/jdrview.jar \
	  $(LIBDIR)/ajr2ajr.jar $(LIBDIR)/jdrinfo.jar \
	  $(LIBDIR)/ajr2jdr.jar $(LIBDIR)/ajr2tex.jar \
	  $(LIBDIR)/eps2jdr.jar $(LIBDIR)/jdr2ajr.jar \
	  $(LIBDIR)/jdr2eps.jar $(LIBDIR)/jdr2png.jar \
	  $(LIBDIR)/jdr2svg.jar $(LIBDIR)/jdr2tex.jar \
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

../lib/ajr2ajr.jar	: $(LIBDIR)/ajr2ajr.jar

../lib/jdrinfo.jar	: $(LIBDIR)/jdrinfo.jar

../lib/ajr2jdr.jar	: $(LIBDIR)/ajr2jdr.jar

../lib/svg2jdr.jar	: $(LIBDIR)/svg2jdr.jar

../lib/ajr2tex.jar	: $(LIBDIR)/ajr2tex.jar

../lib/eps2jdr.jar	: $(LIBDIR)/eps2jdr.jar

../lib/jdr2ajr.jar	: $(LIBDIR)/jdr2ajr.jar

../lib/jdr2eps.jar	: $(LIBDIR)/jdr2eps.jar

../lib/jdr2png.jar	: $(LIBDIR)/jdr2png.jar

../lib/jdr2svg.jar	: $(LIBDIR)/jdr2svg.jar

../lib/jdr2tex.jar	: $(LIBDIR)/jdr2tex.jar

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
		  images/splashimage.png \
		../lib/resources
		cp -u images/splashimage.png classes/
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

$(LIBDIR)/ajr2ajr.jar	: classes/$(CLASSTREE)/ajr2ajr/Ajr2Ajr.class \
		  ajr2ajr/Manifest.txt
		cd classes ; \
		jar cmf ../ajr2ajr/Manifest.txt \
	          $(LIBDIR)/ajr2ajr.jar \
		  $(CLASSTREE)/ajr2ajr/*.class

$(LIBDIR)/jdrinfo.jar	: classes/$(CLASSTREE)/jdrinfo/JdrInfo.class \
		  jdrinfo/Manifest.txt
		cd classes ; \
		jar cmf ../jdrinfo/Manifest.txt \
	          $(LIBDIR)/jdrinfo.jar \
		  $(CLASSTREE)/jdrinfo/*.class

$(LIBDIR)/ajr2jdr.jar	: classes/$(CLASSTREE)/ajr2jdr/Ajr2Jdr.class \
		  ajr2jdr/Manifest.txt
		cd classes ; \
		jar cmf ../ajr2jdr/Manifest.txt \
	          $(LIBDIR)/ajr2jdr.jar \
		  $(CLASSTREE)/ajr2jdr/*.class

$(LIBDIR)/ajr2tex.jar	: classes/$(CLASSTREE)/ajr2tex/Ajr2Tex.class \
		  ajr2tex/Manifest.txt
		cd classes ; \
		jar cmf ../ajr2tex/Manifest.txt \
	          $(LIBDIR)/ajr2tex.jar \
		  $(CLASSTREE)/ajr2tex/*.class

$(LIBDIR)/eps2jdr.jar	: classes/$(CLASSTREE)/eps2jdr/Eps2Jdr.class \
		  eps2jdr/Manifest.txt
		cd classes ; \
		jar cmf ../eps2jdr/Manifest.txt \
	          $(LIBDIR)/eps2jdr.jar \
		  $(CLASSTREE)/eps2jdr/*.class

$(LIBDIR)/jdr2ajr.jar	: classes/$(CLASSTREE)/jdr2ajr/Jdr2Ajr.class \
		  jdr2ajr/Manifest.txt
		cd classes ; \
		jar cmf ../jdr2ajr/Manifest.txt \
	          $(LIBDIR)/jdr2ajr.jar \
		  $(CLASSTREE)/jdr2ajr/*.class

$(LIBDIR)/jdr2eps.jar	: classes/$(CLASSTREE)/jdr2eps/Jdr2Eps.class \
		  jdr2eps/Manifest.txt
		cd classes ; \
		jar cmf ../jdr2eps/Manifest.txt \
	          $(LIBDIR)/jdr2eps.jar \
		  $(CLASSTREE)/jdr2eps/*.class

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

$(LIBDIR)/jdr2tex.jar	: classes/$(CLASSTREE)/jdr2tex/Jdr2Tex.class \
		  jdr2tex/Manifest.txt
		cd classes ; \
		jar cmf ../jdr2tex/Manifest.txt \
	          $(LIBDIR)/jdr2tex.jar \
		  $(CLASSTREE)/jdr2tex/*.class

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
	  -cp $(LIBDIR)/jdr.jar:$(LIBDIR)/javahelp2.jar \
	  *.java */*.java

classes/$(CLASSTREE)/flowframtk/FlowframTk.class : \
	jdr jdrresources classes \
	flowframtk/*.java \
	flowframtk/*/*.java
	cd flowframtk ; \
	javac $(target_version) -d ../classes -Xlint:unchecked -Xlint:deprecation \
	  -cp $(LIBDIR)/javahelp2.jar:$(LIBDIR)/jdr.jar:$(LIBDIR)/jdrresources.jar \
	  *.java */*.java


classes/$(CLASSTREE)/jdrview/JDRView.class : \
	jdr jdrresources classes \
	jdrview/*.java
	cd jdrview ; \
	javac $(target_version) -d ../classes -Xlint:unchecked  -Xlint:deprecation \
	  -cp $(LIBDIR)/javahelp2.jar:$(LIBDIR)/jdr.jar:$(LIBDIR)/jdrresources.jar \
	  *.java

classes/$(CLASSTREE)/ajr2ajr/Ajr2Ajr.class : \
	classes \
	ajr2ajr/*.java
	cd ajr2ajr ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar \
	*.java

classes/$(CLASSTREE)/jdrinfo/JdrInfo.class : \
	classes \
	jdrinfo/*.java
	cd jdrinfo ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar \
	*.java

classes/$(CLASSTREE)/ajr2jdr/Ajr2Jdr.class : \
	classes \
	ajr2jdr/*.java
	cd ajr2jdr ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar \
	*.java

classes/$(CLASSTREE)/ajr2tex/Ajr2Tex.class : \
	classes \
	ajr2tex/*.java
	cd ajr2tex ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar \
	*.java

classes/$(CLASSTREE)/eps2jdr/Eps2Jdr.class : \
	classes \
	eps2jdr/*.java
	cd eps2jdr ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar:$(LIBDIR)/jdrresources.jar \
	*.java

classes/$(CLASSTREE)/jdr2ajr/Jdr2Ajr.class : \
	classes \
	jdr2ajr/*.java
	cd jdr2ajr ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar \
	*.java

classes/$(CLASSTREE)/jdr2eps/Jdr2Eps.class : \
	classes \
	jdr2eps/*.java
	cd jdr2eps ; \
	javac $(target_version) -d ../classes -Xlint:deprecation -Xlint:unchecked \
	-cp $(LIBDIR)/jdr.jar \
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

classes/$(CLASSTREE)/jdr2tex/Jdr2Tex.class : \
	classes \
	jdr2tex/*.java
	cd jdr2tex ; \
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
	cd classes/$(CLASSTREE)/jdrresources; ln -f -s $(JDRSRC)/src/jdrresources/icons


classes/$(CLASSTREE)/jdrview	:
	mkdir -p classes/$(CLASSTREE)/jdrview

classes/$(CLASSTREE)/flowframtk	:
	mkdir -p classes/$(CLASSTREE)/flowframtk

clean	:
	\rm -f $(LIBDIR)/jdr.jar $(LIBDIR)/jdrresources.jar $(LIBDIR)/jdrview.jar
	\rm -f $(LIBDIR)/flowframtk.jar $(LIBDIR)/ajr2ajr.jar $(LIBDIR)/ajr2jdr.jar $(LIBDIR)/ajr2tex.jar $(LIBDIR)/jdrinfo.jar
	\rm -f $(LIBDIR)/eps2jdr.jar $(LIBDIR)/jdr2ajr.jar $(LIBDIR)/jdr2eps.jar $(LIBDIR)/jdr2png.jar
	\rm -f $(LIBDIR)/jdr2svg.jar $(LIBDIR)/jdr2tex.jar $(LIBDIR)/svg2jdr.jar
	\rm -f -r classes
