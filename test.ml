open Origami

(*testowanie prostokat*)
let p = prostokat (0., 0.) (5., 10.)

let _ = print_endline (string_of_int (p (0., 2.)))
let _ = print_endline (string_of_int (p (1., 6.)))
let _ = print_endline (string_of_int (p (5., 3.)))
let _ = print_endline (string_of_int (p (10., 10.)))
let _ = print_endline (string_of_int (p (6., 2.)))

(*testowanie kolko*)
let o = kolko (1., 5.) 10.

let _ = print_endline (string_of_int (o (0., 2.)))
let _ = print_endline (string_of_int (o (1., 6.)))
let _ = print_endline (string_of_int (o (5., 3.)))
let _ = print_endline (string_of_int (o (10., 10.)))
let _ = print_endline (string_of_int (o (6., 2.)))
