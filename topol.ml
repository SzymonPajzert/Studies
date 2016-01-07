exception Cykliczne

let topol connections =
  (*List.iter dla danej wartości dodaje do grafu informacje o niedodwiedzeniu i zależności*)
  let graph = ref PMap.empty in
  let add key value = graph := PMap.add key value !graph in
  List.iter (fun (v, dep) -> add v (false, dep)) connections;

  (*dfs dla niedodwiedzonego wierzchołka dokleja jego zależności i jego na początek listy.
  Jeśli był wierzochłek ma zabronioną wartość to zwróci wyjątek gdy flaga explode jest prawdziwa*)
  let rec dfs (explode, ban) acc v =
    let (visit, dependencies) =
      try PMap.find v !graph with
      | Not_found -> (add v (false, []); (false, []))
    in
    if v = ban && explode then raise Cykliczne else
    if visit then acc
    else
      (add v (true, []);
      let acc_dependencies = List.fold_left (dfs (true, ban)) acc dependencies in
      v :: acc_dependencies)
  in
  (*conditioner zwiększa numer cyklu oraz usuwa dla dfs nieinteresujące zależności*)
  let conditioner acc (value, _) = dfs (false, value) acc value in
  List.fold_left conditioner [] connections
