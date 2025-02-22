#!/bin/sh

## Execution examples
# ---------------------------------------------------------------------------
# Shell script to run command line tool for ASTAH
# ---------------------------------------------------------------------------
# Command Examples
# ---------------------------------------------------------------------------
# sh astah-command.sh -image er -f ./Sample.asta -t png -o ./
# sh astah-command.sh -image all -f ./Sample.asta -t png -o ./
# ---------------------------------------------------------------------------
# Option Examples
# ---------------------------------------------------------------------------
# usage: Export Image Options
#  -f,--file <target file>    target file
#  -image                     export documents to image
#  -o,--output <output dir>   output dir
#  -t,--type <image type>     png/jpg/emf
# ---------------------------------------------------------------------------

# Define ASTAH_HOME como o diretório onde o script está localizado
ASTAH_HOME=$(dirname "$0")

# Configurações da JVM
INITIAL_HEAP_SIZE=64m
MAXIMUM_HEAP_SIZE=1024m
LIBPATH="$ASTAH_HOME/lib/rlm"

JAVA_OPTS="-Xms$INITIAL_HEAP_SIZE -Xmx$MAXIMUM_HEAP_SIZE -Djava.library.path=$LIBPATH"

# Executa o comando Java
java $JAVA_OPTS -cp "$ASTAH_HOME/lib/*:$ASTAH_HOME/astah-uml.jar" com.change_vision.jude.cmdline.JudeCommandRunner "$@"