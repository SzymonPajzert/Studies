module Hasher = Hash.Make(struct let capacities = [|3;5;4|] end)

let print_array a =
  Array.iter (Printf.printf "%d ") a; print_newline ()

let test_hash_dehash x =
  print_array x;
  let h = Hasher.hash x in
  Printf.printf "%d\n" h;
  h |> Hasher.dehash |> print_array;
  print_newline ()

let test_next x =
  print_array x;
  let x = x |> Hasher.hash in
  List.iter (fun x -> x |> Hasher.dehash |> print_array) (Hasher.next x);
  print_newline ()

let () =
  test_hash_dehash [|1;2;3|];
  test_hash_dehash [|1;0;0|];
  test_next [|0;0;0|]

let () =
  Przelewanka.przelewanka [|3, 1; 5, 2; 4, 1|] |> Printf.printf "%d\n"
