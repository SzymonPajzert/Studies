module Hasher = Hash.Make(struct let capacities = [|3;5;4|] end)
open Hasher

let t x =
  Array.iter (Printf.printf "%d ") x; print_newline ();
  let h = hash x in
  Printf.printf "%d\n" h;
  h |> dehash |> Array.iter (Printf.printf "%d "); print_newline ()

let () =
  t [|0;0;0|];
  t [|1;0;0|];
