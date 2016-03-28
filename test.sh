#!/bin/bash

if [[ $# == 3 ]]
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

# Kolory
GREEN="\033[0;32m"
RED="\033[0;31m"
DEFAULT="\033[0m"

for inFile in $DIRECTORY/*.in; do
    fileName=${inFile%\.*}
    cat $inFile | $PROGRAM 1> ${fileName}.realout 2> ${fileName}.realerr

    if [[ $? != 0 ]]; then
       echo "Niepoprawny kod wyjścia dla ${RED}$fileName${DEFAULT}"
       OK=0
    fi

    diff ${fileName}.out ${fileName}.realout &> /dev/null
    if [[ $? != 0 ]]; then
        echo "Niepoprawne wyjście błędu dla ${RED}$fileName${DEFAULT}"
        OK=0
    fi

    if [[ $STDERR_CHECK == 1 ]]; then
        diff ${fileName}.err ${fileName}.realerr &> /dev/null
        if [[ $? != 0 ]]; then
            echo "Niepoprawne wyjście błędu dla ${RED}$fileName${DEFAULT}"
            OK=0
        fi
    fi

    rm ${fileName}.realout 2> /dev/null
    rm ${fileName}.realerr 2> /dev/null
done

if [[ $OK == 1 ]]; then
    echo -e "${GREEN}WSZYSTKIE TESTY PRZESZŁY${DEFAULT}"
else
    exit 1
fi
