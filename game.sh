#!/usr/bin/env bash

readonly distance=8

function validate_int {
    if [[ $1 == *[^0-9]* ]]; then echo "invalid int $1"; exit 1; fi
}

function extract_int {
    local t=$1
    extract_result_1=${t%,*}
    validate_int ${extract_result_1}
    extract_result_2=${t#*,}
    validate_int ${extract_result_2}
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
        extract_int $2; x1=${extract_result_1}; y1=${extract_result_2}
        shift 2
        ;;
    -p2)
        extract_int $2; x2=${extract_result_1}; y2=${extract_result_2}
        shift 2
        ;;
    -ai1)
        if [ -x $2 ]; then ai1=$2; else exit 1; fi
        shift 1
        ;;
    -ai2)
        if [ -x $2 ]; then ai2=$2; else exit 1; fi
        shift 1
        ;;
    *)
        echo "wrong argument"
        exit 1
        ;;
    esac
done

if [ -z ${n+x} ]; then n=10; fi
if (( ${n} <= ${distance} )); then echo "too small n"; exit 1; fi

if [ -z ${k+x} ]; then k=100; fi
if [ -z ${s+x} ]; then s=1; fi

# If only second pair of coordinates is given, set it as first
swap_1_and_2=0
if [[ -z ${x1+x} && -n ${x2+x} ]]; then
    x1=${x2}; unset x2
    y1=${y2}; unset y2
    swap_1_and_2=1
fi

# We assure that it is possible to find proper x coordinates.
# To make all cases possible, swap randomly x and y coordinates.
# Swap either if it is not currently possible to find good x.
swapped=`shuf -i 0-1 -n 1`
if (( swapped == 1 )) || (( x1 <= distance )) && (( x1 + distance >= n )); then
    if [[ -n ${x1+x} ]]; then t=${x1}; x1=${y1}; y1=${t}; fi
    if [[ -n ${x2+x} ]]; then t=${x2}; x2=${y2}; y2=${t}; fi
fi

# If no coordinates are given, choose pair of non contradictory coordinates
if [ -z ${x1+x} ]; then
    if (( n >= 2*distance )); then
        x1=`shuf -i 1-${n} -n 1`
        y1=`shuf -i 1-${n} -n 1`
    else
        # Eliminate possibilities where first coordinate hinders choice of second
        (( safe_range = 2*n - 2*distance ))
        x1=`shuf -i 1-${safe_range} -n 1`
        if (( 2*x1 > safe_range )); then
            (( x1 += 2*distance - n ))
        fi
        y1=`shuf -i 1-${n} -n 1`
    fi
fi

# If only one pair of coordinates is given, look for second one
if [ -z ${x2+x} ]; then
    (( x2_left_range = x1 > distance ? x1 - distance : 0 ))
    (( x2_right_range = x1 + distance <= n ? n + 1 - x1 - distance : 0))
    (( x2_range = x2_left_range + x2_right_range ))
    if (( x2_range == 0 )); then echo "not possible"; exit 1; fi
    x2=`shuf -i 1-${x2_range} -n 1`
    if (( x2 > x2_left_range )); then (( x2 +=  n - x2_right_range)); fi
    y2=`shuf -i 1-${n} -n 1`
fi

if (( swap_1_and_2 == 1 )); then
    (( x1 = x1 + x2 )); (( x2 = x1 - x2 )); (( x1 = x1 - x2 ))
    (( y1 = y1 + y2 )); (( y2 = y1 - y2 )); (( y1 = y1 - y2 ))
fi

if (( swapped == 1 )); then
    (( x1 = x1 + y1 )); (( y1 = x1 - y1 )); (( x1 = x1 - y1 ))
    (( x2 = x2 + y2 )); (( y2 = x2 - y2 )); (( x2 = x2 - y2 ))
fi

if (( x1 - x2 < distance )) && (( x2 - x1 < distance )) && \
   (( y1 - y2 < distance )) && (( y2 - y1 < distance )); then
    echo "too small distance";
    exit 1;
fi

echo "$n $k $s $x1 $y1 $x2 $y2"

# End of parsing, start of the game.

gui_in=3
gui_out=4
player1_in=4
player1_out=3
player2_in=4
player2_out=3

gui_arguments=""
if [ -z ${ai1+x} ]; then
    gui_arguments="${gui_arguments} -human1";
else
    PIPE=$(mktemp -u)
    mkfifo ${PIPE}
    player1_in=5
    eval "exec ${player1_in}<>${PIPE}"

    PIPE=$(mktemp -u)
    mkfifo ${PIPE}
    player1_in=6
    eval "exec ${player1_out}<>${PIPE}"

    ${ai1} <&${player1_in} >&${player1_out}
fi

if [ -z ${ai2+x} ]; then
    gui_arguments="${gui_arguments} -human2";
else
    PIPE=$(mktemp -u)
    mkfifo ${PIPE}
    player2_in=5
    eval "exec ${player2_in}<>${PIPE}"

    PIPE=$(mktemp -u)
    mkfifo ${PIPE}
    player2_in=6
    eval "exec ${player2_out}<>${PIPE}"

    ${ai2} <&${player2_in} >&${player2_out}
fi

PIPE=$(mktemp -u)
mkfifo ${PIPE}
eval "exec ${gui_in}<>${PIPE}"

PIPE=$(mktemp -u)
mkfifo ${PIPE}
eval "exec ${gui_out}<>${PIPE}"

./sredniowiecze_gui_with_libs.sh ${gui_arguments} <&${gui_in} >&${gui_out} &