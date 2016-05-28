#!/usr/bin/env bash

readonly distance=8

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
swap_coordinates=0
if [[ -z ${x1+x} && -n ${x2+x} ]]; then
    x1=${x2}; unset x2
    y1=${y2}; unset y2
    swap_coordinates=1
fi


# If no coordinates are given, choose pair of non contradictory coordinates
if [ -z ${x1+x} ]; then
    # Eliminate possibilities where first coordinate hinders choice of second
    (( safe_range = n > 2*$distance ? n : 2*n - 2*distance ))
    x1=`shuf -i 1-${safe_range} -n 1`
    (( x1 += n > 2*distance ? 0 : 2*distance - n ))
    y1=`shuf -i 1-${n} -n 1`

    # We assured problem won't happen. To make it more random, swap coordinates.
    should_swap=`shuf -i 0-1 -n 1`
    if (( should_swap == 1 )); then
        (( x1 = x1 + y1 )); (( y1 = x1 - y1 )); (( x1 = x1 - y1 ))
    fi
    unset should_swap
fi

echo "$x1 $y1 $x2 $y2"

# If only one pair of coordinates is given, look for second one
if [ -z ${x2+x} ]; then
    # Values on both sides that assure coordinates are good.
    (( x2_biggest_left = x1 > distance ? x1 - distance : 0 ))
    (( x2_smallest_right = x1 + distance < n ? n - x1 - distance : n+1))

    # Length of ranges on top and bottom
    (( y2_top_range = y1 > distance ? y1 - distance : 0 ))
    (( y2_bottom_range = y1 + distance < n ? n - y1 - distance : 0))
    (( y2_range = y2_top_range + y2_bottom_range ))

    x2=`shuf -i 1-${n} -n 1`
    if (( x2 > x2_biggest_left )) && (( x2 < x2_smallest_right)) ; then
        # If x2 is problematic, find proper y2 - eliminate those in too close
        # neighbourhood to the y1.
        y2=`shuf -i 1-${y2_range} -n 1`
        if (( y2 > y2_top_range )); then (( y2 += 2*distance - 1 )); fi
    else
        y2=`shuf -i 1-${n} -n 1`
    fi
fi

# TODO - wrong first coordinates

echo "$x1 $y1 $x2 $y2"

if (( swap_coordinates == 1 )); then
    (( x1 = x1 + x2 )); (( x2 = x1 - x2 )); (( x1 = x1 - x2 ))
    (( y1 = y1 + y2 )); (( y2 = y1 - y2 )); (( y1 = y1 - y2 ))
    unset swap_coordinates
fi

echo "$x1 $y1 $x2 $y2"


if (( x1 - x2 < distance )) && (( x2 - x1 < distance )) && \
   (( y1 - y2 < distance )) && (( y2 - y1 < distance )); then
    echo "too small distance";
    exit 1;
fi

echo "$x1 $y1 $x2 $y2"