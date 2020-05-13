#
# Makefile to build SDK distribution.
#

#
# Variables
#
DIST_DIR=dist
BASE_DIR=$(DIST_DIR)/uti/sdk
BIN_DIR=$(BASE_DIR)/bin
GLOBAL_BIN_DIR=$(BASE_DIR)/globalbin
DEP_DIR=$(BASE_DIR)/deploy
DOC_DIR=$(BASE_DIR)/doc
ETC_DIR=$(BASE_DIR)/etc
LIB_DIR=$(BASE_DIR)/lib
LOG_DIR=$(BASE_DIR)/log
SRC_DIR=$(BASE_DIR)/src

SDK_JAR=java/uti_sdk.jar
BIN_FILES=bin/*
GLOB_BIN_FILES=globalbin/*
DEP_FILES=deploy/*.sh
DOC_FILES=doc/*
ETC_FILES=etc/*.*

TAR_FILE=uti_sdk.tar.gz
SRC_TAR=src.tar.gz

#
# Targets
#
all: compile dir copy tar

compile:
	cd java; make

dir:
	mkdir $(DIST_DIR)
	mkdir -p $(BASE_DIR)
	mkdir $(BIN_DIR)
	mkdir $(GLOBAL_BIN_DIR)
	mkdir $(DEP_DIR)
	mkdir $(DOC_DIR)
	mkdir $(ETC_DIR)
	mkdir $(LIB_DIR)
	mkdir $(LOG_DIR)
	mkdir $(SRC_DIR)

copy:
	cp $(BIN_FILES) $(BIN_DIR)
	cp $(GLOB_BIN_FILES) $(GLOBAL_BIN_DIR)
	cp $(DEP_FILES) $(DEP_DIR)
	cp -r $(DOC_FILES) $(DOC_DIR)
	cp $(ETC_FILES) $(ETC_DIR)
	chmod u+w $(ETC_DIR)/*.*
	cp $(SDK_JAR) $(LIB_DIR)
	echo 1 > $(LOG_DIR)/active.txt

tar:
	cd java; make clean
	tar cfvz $(SRC_TAR) Makefile bin deploy doc etc globalbin java
	mv $(SRC_TAR) $(SRC_DIR)
	cd $(DIST_DIR); tar cfvz $(TAR_FILE) *
	mv $(DIST_DIR)/$(TAR_FILE) .

clean:
	rm -rf $(DIST_DIR)
	rm $(TAR_FILE)
