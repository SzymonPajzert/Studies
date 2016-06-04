#!/bin/bash -x

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
        if (( $# >= 2 )); then shift 2; else exit 1; fi
        ;;
    -k)
        validate_int $2; k=$2
        if (( $# >= 2 )); then shift 2; else exit 1; fi
        ;;
    -s)
        validate_int $2; s=$2
        if (( $# >= 2 )); then shift 2; else exit 1; fi
        ;;
    -p1)
        extract_int $2; x1=${extract_result_1}; y1=${extract_result_2}
        if (( $# >= 2 )); then shift 2; else exit 1; fi
        ;;
    -p2)
        extract_int $2; x2=${extract_result_1}; y2=${extract_result_2}
        if (( $# >= 2 )); then shift 2; else exit 1; fi
        ;;
    -ai1)
        if [ -x $2 ]; then ai1=$2; else exit 1; fi
        if (( $# >= 2 )); then shift 2; else exit 1; fi
        ;;
    -ai2)
        if [ -x $2 ]; then ai2=$2; else exit 1; fi
        if (( $# >= 2 )); then shift 2; else exit 1; fi
        ;;
    *)
        echo "wrong argument"
        exit 1
        ;;
    esac
done

if [ -z ${n} ]; then n=10; fi
if (( n <= distance )) || (( n >= 2147483648)); then echo "too small n"; exit 1; fi

if [ -z ${k} ]; then k=100; fi
if [ -z ${s} ]; then s=1; fi

# Coordinates randomization.

# If only second pair of coordinates is given, set it as first
swap_1_and_2=0
if [[ -z ${x1} && -n ${x2} ]]; then
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
if [[ -z ${x2} ]]; then
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

echo "INIT ${n} ${k} 1 ${x1} ${y1} ${x2} ${y2}"

# End of parsing, start of the game.
# Human vs Human game

if [[ -z ${ai1} ]] && [[ -z ${ai2} ]]; then
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

# At least one AI games

gui_arguments=""

readonly gui_in=3
readonly gui_out=4
readonly ai1_in=5
readonly ai1_out=6
readonly ai2_in=7
readonly ai2_out=8

cur_ai_in=0
cur_ai_out=0
next_ai_in=0
next_ai_out=0

ai1_pid=0
ai2_pid=0

if [[ -n ${ai1} ]]; then
    make_pipe ${ai1_in}
    make_pipe ${ai1_out}
    ${ai1} <&${ai1_in} >&${ai1_out} &
    ai1_pid=$!
    echo "INIT ${n} ${k} 1 ${x1} ${y1} ${x2} ${y2}" >&${ai1_in}
    cur_ai_in=${ai1_in}
    cur_ai_out=${ai1_out}
else
    cur_ai_in=${gui_in}
    cur_ai_out=${gui_out}
    gui_arguments="${gui_arguments} -human1"
fi

if [[ -n ${ai2} ]]; then
    make_pipe ${ai2_in}
    make_pipe ${ai2_out}
    ${ai2} <&${ai2_in} >&${ai2_out} &
    ai2_pid=$!
    echo "INIT ${n} ${k} 2 ${x1} ${y1} ${x2} ${y2}" >&${ai2_in}
    next_ai_in=${ai2_in}
    next_ai_out=${ai2_out}
else
    next_ai_in=${gui_in}
    next_ai_out=${gui_out}
    gui_arguments="${gui_arguments} -human2"
fi

make_pipe ${gui_in}
if [[ -z ${ai1} ]] || [[ -z ${ai2} ]]; then
    make_pipe ${gui_out}
    ./sredniowiecze_gui_with_libs.sh ${gui_arguments} <&${gui_in} >&${gui_out} &
    notify_gui=0
    gui_pid=$!
    echo "INIT ${n} ${k} 1 ${x1} ${y1} ${x2} ${y2}" >&${gui_in}
    echo "INIT ${n} ${k} 2 ${x1} ${y1} ${x2} ${y2}" >&${gui_in}
else
    ./sredniowiecze_gui_with_libs.sh ${gui_arguments} <&${gui_in} &>/dev/null &
    notify_gui=1
    gui_pid=$!
    echo "INIT ${n} ${k} 1 ${x1} ${y1} ${x2} ${y2}" >&${gui_in}
    echo "INIT ${n} ${k} 2 ${x1} ${y1} ${x2} ${y2}" >&${gui_in}
fi

while kill -0 ${gui_pid} &>/dev/null && \
      ( [[ ${ai1_pid} == 0 ]] || kill -0 ${ai1_pid} &>/dev/null )  && \
      ( [[ ${ai2_pid} == 0 ]] || kill -0 ${ai2_pid} &>/dev/null ); do
    read -t 1 line <&${cur_ai_out}
    if [[ -n ${line} ]]; then
        echo ${line} >&${next_ai_in}
        if (( notify_gui == 1 )); then echo ${line} >&${gui_in}; fi

        if [[ ${line} == "END_TURN" ]]; then
            if [[ ${cur_ai_out} != ${gui_out} ]]; then sleep ${s}; fi

            t=${next_ai_in}
            next_ai_in=${cur_ai_in}
            cur_ai_in=${t}

            t=${next_ai_out}
            next_ai_out=${cur_ai_out}
            cur_ai_out=${t}
        fi
    fi
done

while read -t 1 line <&${cur_ai_out}; do
    if [[ -n ${line} ]]; then
        echo ${line} >&${next_ai_in}
        if (( notify_gui == 1 )); then echo ${line} >&${gui_in}; fi
    fi
done

if (( ai1_pid != 0 )); then kill ${ai1_pid} &>/dev/null; fi
if (( ai2_pid != 0 )); then kill ${ai2_pid} &>/dev/null; fi
kill ${gui_pid} &>/dev/null

ok=1
wait ${gui_pid}; if (( $? != 0 )); then ok=0; fi

if (( ai1_pid != 0 )); then
    wait ${ai1_pid};
    if (( $? == 42 )); then
        ok=0
    fi
fi

if (( ai2_pid != 0 )); then
    wait ${ai2_pid};
    if (( $? == 42 )); then
        ok=0
    fi
fi

if (( ok == 1 )); then exit 0; else exit 1; fi

