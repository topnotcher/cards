JC=javac
CLASSPATH=.:/home/mario/dev/nhbirc/lib
JFLAGS=-g -Xlint:unchecked -classpath $(CLASSPATH) 
SRC=.
SOURCES=$(shell find $(SRC) -name '*.java')
OBJECTS=$(SOURCES:.java=.class)
DOCDIR="doc/html/"
all: classes
		
.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

classes: $(OBJECTS)

run: all
	java -cp $(CLASSPATH) Launcher

debug: all
	jdb -sourcepath $(CLASSPATH) -classpath $(CLASSPATH) Launcher

jar: classes
	jar cfe client.jar Launcher *.class util/*.class irc/*.class client/*.class

docs:
	$(RM) -r $(DOCDIR)/*
	javadoc -private -classpath $(SRC) -sourcepath $(SRC) -d $(DOCDIR) *.java util irc client

clean:
	$(foreach var, $(shell find . -name '*.class'), $(RM) '$(var)';)

submit: all 
	git archive --format=tar  HEAD --prefix=tmp/ | (ssh gbowser@london.cs.uri.edu ./submitfinal.sh) 
