#!/bin/bash
cd "$(dirname "@0")"
export LD_LIBRARY_PATH=lib
java -jar AnemoneActiniaria.jar
