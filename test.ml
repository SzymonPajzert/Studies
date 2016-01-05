let () =
  Printf.printf "Enter seed for randomization:\n%!";
  Random.init (Scanf.scanf " %d" (fun i -> i));
  Printf.printf "Read seed is 42\nTesting began\n"

let string_of_data ~s_conv data =
  let string_of_intlist intlist =
    List.fold_left (fun x y -> x  ^ (s_conv y) ^ " ") "" intlist
  in
  let string_creator acc (value, dependencies) =
    acc ^ (s_conv value ^ ": " ^ (string_of_intlist dependencies) ^ "\n")
  in
  List.fold_left string_creator "" data

let string_of_intdata = string_of_data ~s_conv:string_of_int

let data1 = [1, [2]; 2, [3]; 3, []]
let () = print_endline (string_of_intdata data1)
