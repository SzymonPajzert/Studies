exception Cykliczne

let topol (connections:('a * 'a list) list) =
  let i = ref 0 in
  let visited = Array.make (List.length connections) false in
  let map_creator acc (value, dependencies) =
    let identifier = !i in
    i := !i + 1;
    PMap.add value (identifier, dependencies) acc
  in
  let map = List.fold_left map_creator PMap.empty connections in
  let rec dfs ~explode acc value =
    let (index, dependencies) = PMap.find value map in
    if visited.(index) then (if explode then raise Cykliczne else acc)
    else
      (visited.(index) <- true;
      value :: (List.fold_left (dfs ~explode:true) acc dependencies))
  in
    List.fold_left (fun acc x -> dfs ~explode:false acc (fst x)) [] connections
