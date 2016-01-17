module Hasher = Hash.Make(struct let capacities = [|3;5;4|] end)
open Hasher

let print_array a =
  Array.iter (Printf.printf "%d ") a; print_newline ()

let test_hash_dehash x =
  print_array x;
  let h = hash x in
  Printf.printf "%d\n" h;
  h |> dehash |> print_array;
  print_newline ()
;;

let test_next x =
  print_array x;
  let x = x |> hash in
  List.iter (fun x -> x |> dehash |> print_array) (next x);
  print_newline ()

let () =
  test_hash_dehash [|1;2;3|];
  test_hash_dehash [|1;0;0|];
  test_next [|1;2;3|];
