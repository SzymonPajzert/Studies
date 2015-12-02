open Origami

(*testowanie prostokat*)
let _ = print_endline "testowanie prostokat"
let p = prostokat (0., 0.) (5., 10.)
let _ = assert (p (0., 2.) = 1)
let _ = assert (p (1., 6.) = 1)
let _ = assert (p (5., 3.) = 1)
let _ = assert (p (10., 10.) = 0)
let _ = assert (p (6., 2.) = 0)
testowanie kolko

(*testowanie kolko*)
let _ = print_endline "testowanie kolko"
let o = kolko (1., 5.) 10.
let _ = assert (o (0., 2.) = 1)
let _ = assert (o (1., 6.) = 1)
let _ = assert (o (5., 3.) = 1)
let _ = assert (o (10., 10.) = 0)
let _ = assert (o (6., 2.) = 1)

(*testowanie det*)
let _ = print_endline "testowanie det"
let _ = assert (det (1., 2.) (4., 3.) (3., 4.) > 0.)
let _ = assert (det (1., 2.) (4., 3.) (1., 6.) > 0.)
let _ = assert (det (1., 2.) (4., 3.) ((-1.), 3.) > 0.)
let _ = assert (det (1., 2.) (4., 3.) ((-2.), 1.) = 0.)
let _ = assert (det (0., 0.) (5., 10.) (0., 2.) > 0.)
let _ = assert (det (1., 2.) (4., 3.) ())

(*testowanie zloz*)
let _ = print_endline "testowanie zloz"
let k = prostokat (0., 0.) (5., 10.)
let _ = assert (k (0., 2.) = 1)
let _ = assert (k (1., 6.) = 1)
let _ = assert (k (5., 3.) = 1)
let _ = assert (k (10., 10.) = 0)
let _ = assert (k (6., 2.) = 0)

let k1 = zloz (0., 5.) (2., 5.) k
let _ = assert (k1 (0., 2.) = 0)
let _ = assert (k1 (1., 6.) = 2)
let _ = assert (k1 (5., 3.) = 0)
let _ = assert (k1 (10., 10.) = 0)
let _ = assert (k1 (6., 2.) = 0)
