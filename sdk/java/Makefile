#
# Makefile to compile UTI SDK sources 
#

#
# Commands
#
JAVAC=javac
JAR=jar
JAVADOC=javadoc

#
# Variables
#
JWS_HOME=/opt/java/jwsdp-1.3
SAAJ_JAR=$(JWS_HOME)/saaj/lib/saaj-api.jar
RV_JAR=$(TIB_RV_HOME)/lib/tibrvj.jar

CP=$(CLASSPATH):$(SAAJ_JAR):$(RV_JAR)

TOOLS_SRC=uti/nextgen/tools
ADAPTER_SRC=uti/nextgen/adapter
SOAP_SRC=uti/nextgen/soapclient

JAR_FILE=uti_sdk.jar

#
# Javadoc variables
#
JDOC_SRC_PATH=.
JDOC_WINDOW_TITLE="UTI SDK API Documentation"
JDOC_DOC_TITLE="<h2>UTI SDK API Documentation</h2>"
JDOC_PCKGS=uti.nextgen.tools uti.nextgen.adapter uti.nextgen.soapclient
JDOC_DIR=../doc/api


#
# Targets
#
all: tools adapter soap jar javadoc

tools:
	$(JAVAC) -classpath $(CP) $(TOOLS_SRC)/*.java

adapter:
	$(JAVAC) -classpath $(CP) $(ADAPTER_SRC)/*.java

soap:
	$(JAVAC) -classpath $(CP) $(SOAP_SRC)/*.java

jar:
	$(JAR) cfv $(JAR_FILE) $(TOOLS_SRC)/*.class
	$(JAR) ufv $(JAR_FILE) $(ADAPTER_SRC)/*.class
	$(JAR) ufv $(JAR_FILE) $(SOAP_SRC)/*.class

javadoc:
	-mkdir $(JDOC_DIR)
	$(JAVADOC) -sourcepath $(JDOC_SRC_PATH) -classpath $(CP) \
        -windowtitle $(JDOC_WINDOW_TITLE) -doctitle $(JDOC_DOC_TITLE) \
        -d $(JDOC_DIR) $(JDOC_PCKGS)

clean:
	-rm $(TOOLS_SRC)/*.class
	-rm $(ADAPTER_SRC)/*.class
	-rm $(SOAP_SRC)/*.class
	-rm $(JAR_FILE)
	-rm -r $(JDOC_DIR)
