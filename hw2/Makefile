SOURCES = $(shell find src -type f -name "*.java")
CLASSES = $(patsubst src/%.java,out/%.class,$(SOURCES))
MAINCLASS = Solution

all: $(CLASSES)

run:
	java -Xmx4G -XX:+UseConcMarkSweepGC -cp out: ${MAINCLASS}

pack:
	zip hw2.zip -r Makefile src

clean:
	rm -rf out

out/%.class: src/%.java out
	javac -cp src: $< -d out

out:
	mkdir -p out
