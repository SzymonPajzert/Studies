#!/bin/bash

if [[ $# == 3 ]]
then
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

function correctPrint {
    echo -e "${GREEN}$1${DEFAULT}"
}

function wrongPrint {
    echo -e "${RED}$1${DEFAULT}"
}

for inFile in ${DIRECTORY}*.in; do
    fileName=${inFile%\.*}
    cat ${inFile} | ${PROGRAM} 1> ${fileName}.realout 2> ${fileName}.realerr

    if [[ $? != 0  && $? != 42 ]]; then
       wrongPrint "Niepoprawny kod wyjścia dla $fileName"
       OK=0
    fi

    diff -y ${fileName}.out ${fileName}.realout &> /dev/null
    if [[ $? != 0 ]]; then
        wrongPrint "Niepoprawne wyjście dla ${fileName}"

        if [[ ${OK} == 1 ]]; then
            cat ${fileName}.realdiff
        fi

        OK=0
    else
        correctPrint "Poprawne wyjście dla $fileName"
    fi

    if [[ $STDERR_CHECK == 1 ]]; then
        diff -y ${fileName}.err ${fileName}.realerr &> /dev/null
        if [[ $? != 0 ]]; then
            wrongPrint "Niepoprawne wyjście błędu dla $fileName"
            OK=0
        else
            correctPrint "Poprawne wyjście błędu dla $fileName"
        fi
    fi

    rm ${fileName}.realout 2> /dev/null
    rm ${fileName}.realerr 2> /dev/null
done

if [[ $OK == 1 ]]; then
    correctPrint "WSZYSTKIE TESTY PRZESZŁY"
else
    exit 1
fi