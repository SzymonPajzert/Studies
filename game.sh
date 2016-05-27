#!/usr/bin/env bash

function validate_int {
    if [[ $1 == *[^0-9]* ]]; then echo "invalid int $1"; exit 1; fi
}

while (( "$#" )); do
    case $1 in
    -n)
        validate_int $2; n=$2;
        shift 2
        ;;
    -k)
        validate_int $2; k=$2
        shift 2
        ;;
    -s)
        validate_int $2; s=$2
        shift 2
        ;;
    -p1)
        validate_int $2; x1=$2
        validate_int $3; y1=$3
        shift 3
        ;;
    -p2)
        validate_int $2; x2=$2
        validate_int $3; y2=$3
        shift 3
        ;;
    -ai1)
        if [ -x $2 ]; then ai1=$2; else exit 1; fi
        shift 1
        ;;
    -ai2)
        if [ -x $2 ]; then ai2=$2; else exit 1; fi
        shift 1
        ;;
    *) exit 1;;
    esac
done

if [ -z ${n+x} ]; then n=10; fi
if [ -z ${k+x} ]; then k=100; fi
if [ -z ${s+x} ]; then s=1; fi

echo "$n $k $s $x1 $y1 $x2 $y2 $ai1 $ai2"