type 'a queue =
  Null |
  Node of 'a queue * 'a * int * 'a queue
(*
Queue consists of Null if empty. Otherwise it's made from 4 expressions:
Left son, value, NPL, right son *)

let empty = Null

let rec join a b =
  match (a, b) with
  | (Null, _) -> b
  | (_, Null) -> a
  | (Node (left, aval, anpl, right), Node (_, bval, _, _)) ->
    if (aval > bval) then join b a
    else
      let newright = join right b in
        match (left, newright) with
        | (Null, _) -> Node (newright, aval, 0, empty)
        | (Node (_, _, leftnpl, _), Node (_, _, rightnpl, _)) ->
          if rightnpl < leftnpl then Node (left, aval, rightnpl+1, newright)
          else Node (newright, aval, leftnpl+1, left)

let add x q = join (Node (Null, x, 0, Null)) q

exception Empty

let delete_min q =
  match q with
  | Null -> raise Empty
  | Node (left, qval, _, right) -> (qval, (join left right))

let is_empty q =
  match q with
  | Null -> true
  | Node _ -> false
