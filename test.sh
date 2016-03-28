#!/bin/bash

if [[ S# == 3 ]]
then
    echo Tutaj
    PROGRAM="./$2 -v"
    DIRECTORY="./$3"
    STDERR_CHECK=1
else
    PROGRAM="./$1"
    DIRECTORY="./$2"
    STDERR_CHECK=0
fi

# Zmienna przechowujące informację, czy wszystkie testy przeszły.
OK=1

for inFile in $DIRECTORY/*.in; do
    fileName=${inFile%\.*}
    cat $inFile | $PROGRAM 1> ${fileName}.realout 2> ${fileName}.realerr

    if [[ $? != 0 ]]; then
       echo "NIEPOPRAWNY KOD BŁĘDU DLA WEJŚCIA $fileName"
       OK=0
    fi

    diff ${fileName}.out ${fileName}.realout
    if [[ $? != 0 ]]; then
        echo "NIEPOPRAWNE STANDARDOWE WYJŚCIE DLA $fileName"
        OK=0
    fi

    if [[ $STDERR_CHECK == 1 ]]; then
        diff ${fileName}.err ${fileName}.realerr
        if [[ $? != 0 ]]; then
            echo "NIEPOPRAWNE WYJŚCIE BŁĘDU DLA $fileName"
            OK=0
        fi
    fi

    rm ${fileName}.realout 2> /dev/null
    rm ${fileName}.realerr 2> /dev/null
done

GREEN="\033[0;32m"
RED="\033[0;31m"
DEFAULT="\033[0m"

if [[ $OK == 1 ]]; then
    echo -e "${GREEN}WSZYSTKIE TESTY PRZESZŁY${DEFAULT}"
else
    exit 1
fi
