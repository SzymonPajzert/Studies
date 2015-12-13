(*prints number of passed test*)
let counter = ref 0
let passed () =
  counter := !counter+1;
  print_string "passed test no: ";
  print_int !counter;
  print_newline ()

let empty = (Iset.empty, Naiveset.empty)

exception Inconsistent of (Iset.t * Naiveset.t) * string

(* It should run functions, but I don't know yet how to do it
let run_assertion f1 f2  args =
  let res1, res2 = List.fold_left (fun x y -> ((fst x) y), ((snd x) y) ) (f1, f2) args in
  if (answer1 = answer2) then answer1
  else raise Inconsistent set, "is_empty"
*)

let is_empty set =
  let answer1 = Iset.is_empty (fst set) in
  let answer2 = Naiveset.is_empty (snd set) in
  if (answer1 = answer2) then answer1
  else raise (Inconsistent (set, "is_empty"))

let add i set = (Iset.add i (fst set), Naiveset.add i (snd set))

let remove i set =
  (Iset.remove i (fst set), Naiveset.remove i (snd set))

let mem x set =
  let answer1 = Iset.mem x (fst set) in
  let answer2 = Naiveset.mem x (snd set) in
  if (answer1 = answer2) then answer1
  else raise (Inconsistent (set, "mem"))

let below x set =
  let answer1 = Iset.below x (fst set) in
  let answer2 = Naiveset.below x (snd set) in
  if (answer1 = answer2) then answer1
  else raise (Inconsistent (set, "below"))

let split x set =
  let (answer1l, answer1b, answer1r) = Iset.split x (fst set) in
  let (answer2l, answer2b, answer2r) = Naiveset.split x (snd set) in
  if (answer1b = answer2b) then (answer1l, answer1b, answer1r), (answer2l, answer2b, answer2r)
  else raise (Inconsistent (set, "split"))

let print_set set n =
  for i = 0 to n do
    print_string (if mem i set then "1 " else "0 ");
  done;
  print_newline ()

let set = empty
let () = assert(is_empty set); passed ()

let set = add (1, 3) set
let () =
  print_set set 20;
  passed ()

let set = add (5, 7) set
let () =
  print_set set 20;
  passed ()

let set = add (3, 10) set
let () =
  print_set set 20;
  passed ()

let set = remove (3, 5) set
let () =
  print_set set 20;
  passed ()
