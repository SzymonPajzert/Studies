#!/bin/bash

readonly distance=8
readonly unit_number=3

function validate_int {
    if [[ $1 == *[^0-9]* ]]; then echo "invalid int $1"; exit 1; fi
}

extract_result_1=0
extract_result_2=0
function extract_int {
    local t=$1
    extract_result_1=${t%,*}
    validate_int ${extract_result_1}
    extract_result_2=${t#*,}
    validate_int ${extract_result_2}
}

function make_pipe {
    PIPE=$(mktemp -u)
    mkfifo ${PIPE}
    eval "exec $1<>${PIPE}"
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
        shift 2
        ;;
    -ai2)
        if [ -x $2 ]; then ai2=$2; else exit 1; fi
        shift 2
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

# Coordinates randomization.

# If only second pair of coordinates is given, set it as first
swap_1_and_2=0
if [[ -z ${x1+x} && -n ${x2+x} ]]; then
    x1=${x2}; unset x2
    y1=${y2}; unset y2
    swap_1_and_2=1
fi

(( x_safe_range = n - unit_number ))
# If no coordinates are given, choose pair of non contradictory coordinates
if [[ -z ${x1+x} ]]; then
    if (( n >= 2*distance )); then
        x1=`shuf -i 1-${n} -n 1`
        y1=`shuf -i 1-${n} -n 1`
    else
        # Eliminate possibilities where first coordinate hinders choice of second
        (( y_safe_range = 2*n - 2*distance ))
        y1=`shuf -i 1-${y_safe_range} -n 1`
        if (( 2*y1 > y_safe_range )); then
            (( y1 += 2*distance - n ))
        fi
        x1=`shuf -i 1-${x_safe_range} -n 1`
    fi
fi

# If only one pair of coordinates is given, look for second one
if [[ -z ${x2+x} ]]; then
    (( y2_top_range = y1 > distance ? y1 - distance : 0 ))
    (( y2_bottom_range = y1 + distance <= n ? n + 1 - y1 - distance : 0))
    (( y2_range = y2_top_range + y2_bottom_range ))

    if (( y2_range == 0 )); then
        (( x2_left_range = x1 > distance ? x1 - distance : 0 ))
        (( t = x1 + distance + unit_number ))
        (( x2_right_range = t <= n ? n + 1 - t : 0 ))
        (( x2_range = x2_left_range + x2_right_range ))
        if (( x2_range == 0)); then echo "impossible"; exit 1;
        else
            x2=`shuf -i 1-${x2_range} -n 1`
            if (( x2 > x2_left_range ));
                then (( x2 +=  n - x2_right_range - unit_number));
            fi
            y2=`shuf -i 1-${n} -n 1`
        fi
    else
        y2=`shuf -i 1-${y2_range} -n 1`
        if (( y2 > y2_top_range )); then (( y2 +=  n - y2_bottom_range)); fi
        x2=`shuf -i 1-${x_safe_range} -n 1`
    fi
fi

if (( swap_1_and_2 == 1 )); then
    (( x1 = x1 + x2 )); (( x2 = x1 - x2 )); (( x1 = x1 - x2 ))
    (( y1 = y1 + y2 )); (( y2 = y1 - y2 )); (( y1 = y1 - y2 ))
fi

# End of parsing, start of the game.

if [[ -z ${ai1+x} ]] && [[ -z ${ai2+x} ]]; then
    echo "opcja 1"
    make_pipe 3
    gui_in=3
    ./sredniowiecze_gui_with_libs.sh -human1 -human2 <&3 &>/dev/null &
    gui_pid=$!

    echo "INIT ${n} ${k} 1 ${x1} ${y1} ${x2} ${y2}" >&${gui_in}
    echo "INIT ${n} ${k} 2 ${x1} ${y1} ${x2} ${y2}" >&${gui_in}
    wait ${gui_pid}
    exit 0
fi

if [[ -n ${ai1+x} ]] && [[ -z ${ai2+x} ]]; then
    echo "opcja 2"
    for i in `seq 3 6`; do make_pipe ${i}; done
    gui_in=3
    gui_out=4
    ai1_in=5
    ai1_out=6

    cur_ai_in=5
    cur_ai_out=6
    next_ai_in=3
    next_ai_out=4

    ${ai1} <&5 >&6 &
    ai1_pid=$!

    ./sredniowiecze_gui_with_libs.sh -human2 <&3 >&4 &
    gui_pid=$!

    echo "INIT ${n} ${k} 1 ${x1} ${y1} ${x2} ${y2}" >&${gui_in}
    echo "INIT ${n} ${k} 2 ${x1} ${y1} ${x2} ${y2}" >&${gui_in}
    echo "INIT ${n} ${k} 1 ${x1} ${y1} ${x2} ${y2}" >&${ai1_in}

    while kill -0 ${gui_pid} &>/dev/null && \
          kill -0 ${ai1_pid} &>/dev/null ; do
        read line <&${cur_ai_out}
        if [[ -n ${line} ]]; then
            echo ${line} >&${next_ai_in}

            if [[ ${line} == "END_TURN" ]]; then
                t=${next_ai_in}
                next_ai_in=${cur_ai_in}
                cur_ai_in=${t}

                t=${next_ai_out}
                next_ai_out=${cur_ai_out}
                cur_ai_out=${t}

                if [[ ${cur_ai_out} == ${gui_out} ]]; then sleep ${s}; fi
            fi
        fi
    done

    while read -t 1 line <&${cur_ai_out}; do
        echo ${line} >&${next_ai_in}
    done

    kill ${gui_pid} &>/dev/null
    kill ${ai1_pid} &>/dev/null
    kill ${ai1_pid} &>/dev/null
    exit 0
fi

if [[ -n ${ai1+x} ]] && [[ -n ${ai2+x} ]]; then
    echo "opcja 4"
    make_pipe 3
    gui_in=3
    for i in `seq 5 8`; do make_pipe ${i}; done
    cur_ai_in=5
    cur_ai_out=6
    next_ai_in=7
    next_ai_out=8

    ${ai1} <&5 >&6 &
    ai1_pid=$!

    ${ai2} <&7 >&8 &
    ai2_pid=$!

    ./sredniowiecze_gui_with_libs.sh <&3 &>/dev/null &
    gui_pid=$!

    echo "INIT ${n} ${k} 1 ${x1} ${y1} ${x2} ${y2}" >&${gui_in}
    echo "INIT ${n} ${k} 1 ${x1} ${y1} ${x2} ${y2}" >&${cur_ai_in}
    echo "INIT ${n} ${k} 2 ${x1} ${y1} ${x2} ${y2}" >&${gui_in}
    echo "INIT ${n} ${k} 2 ${x1} ${y1} ${x2} ${y2}" >&${next_ai_in}

    while kill -0 ${gui_pid} &>/dev/null && \
          kill -0 ${ai1_pid} &>/dev/null && \
          kill -0 ${ai2_pid} &>/dev/null ; do
        read line <&${cur_ai_out}
        if [[ -n ${line} ]]; then
            echo ${line} >&${gui_in}
            echo ${line} >&${next_ai_in}

            if [[ ${line} == "END_TURN" ]]; then
                t=${next_ai_in}
                next_ai_in=${cur_ai_in}
                cur_ai_in=${t}

                t=${next_ai_out}
                next_ai_out=${cur_ai_out}
                cur_ai_out=${t}

                sleep ${s}
            fi
        fi
    done

    while read -t 1 line <&${cur_ai_out}; do
        echo ${line} >&${gui_in}
        echo ${line} >&${next_ai_in}
    done

    kill ${gui_pid} &>/dev/null
    kill ${ai1_pid} &>/dev/null
    kill ${ai1_pid} &>/dev/null
    exit 0
fi

