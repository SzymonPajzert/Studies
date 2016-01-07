let () =
  Printf.printf "Enter seed\n%!";
  (Scanf.scanf " %d" (fun i -> i)) |> Random.init;
  Random.self_init ();
  Printf.printf "\nTesting began\n"

let string_of_list ~s_conv l =
  List.fold_left (fun x y -> x  ^ (s_conv y) ^ " ") "" l

(*with given s_conv from 'a type to string creates printable data*)
let string_of_data ~s_conv data =
  let string_creator acc (value, dependencies) =
    acc ^ (s_conv value ^ ": " ^ (string_of_list ~s_conv dependencies) ^ "\n")
  in
  List.fold_left string_creator "" data

let string_of_intlist = string_of_list ~s_conv:string_of_int
let string_of_intdata = string_of_data ~s_conv:string_of_int

let data1 = [1, [2]; 2, []]
let () = data1 |> string_of_intdata |> print_endline

(*creates random list containing numbers in increasing order from min_val including
to max_val excluding*)
let rec random_list min_val max_val =
  if min_val >= max_val then []
  else
    let n = Random.int (max_val - min_val) + min_val in
    n :: random_list (n + 1) max_val

let create_input max_val =
  let rec temp acc i =
    if i = max_val - 1 then (i, []) :: acc
    else
      let new_dep = (i+1) :: random_list (i+2) max_val in
      temp ((i, new_dep) :: acc) (i+1)
  in
    temp [] 0

let validate input =
  let time = Sys.time() in
  let output = input |> Topol.topol in
  Sys.time() -. time |> string_of_float |> print_endline;
  (*graph of connections for given value returns values that should be behind*)
  let graph = List.fold_left (fun map (v, dep) -> PMap.add v dep map) PMap.empty input in
  let rec folder ~setvisited (result, visited) value =
    if result && (not (PMap.mem value visited)) then
      let dependencies = try PMap.find value graph with | Not_found -> [] in
      let newvisited = if setvisited then PMap.add value true visited else visited in
      List.fold_left (folder ~setvisited:false) (result, newvisited) dependencies
    else (false, visited)
  in
  fst (List.fold_left (folder ~setvisited:true) (true, PMap.empty) output)

let () = create_input 100 |> validate |> string_of_bool |> print_endline
let () = [1, [2; 3]; 2, [3]] |> Topol.topol |> string_of_intlist |> print_endline
