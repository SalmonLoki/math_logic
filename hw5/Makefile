SOURCES = $(shell find src -type f -name "*.java")
CLASSES = $(patsubst src/%.java,out/%.class,$(SOURCES))
MAINCLASS = Main

all: $(CLASSES)

run:
	java -cp out: ${MAINCLASS}

pack:
	zip hw5.zip -r Makefile src

clean:
	rm -rf out

out/%.class: src/%.java out
	javac -cp src: $< -d out

out:
	mkdir -p out
