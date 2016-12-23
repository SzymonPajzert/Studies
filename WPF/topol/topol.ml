exception Cykliczne

let topol connections =
  (*List.iter dla danej wartości dodaje do grafu informacje o niedodwiedzeniu i zależności*)
  let graph =
    List.fold_left (fun acc (v, dep) -> PMap.add v (false, dep) acc) PMap.empty connections
  in

  (*dfs dla niedodwiedzonego wierzchołka dokleja jego zależności i jego na początek listy.
  Jeśli był wierzochłek ma zabronioną wartość to zwróci wyjątek gdy flaga explode jest prawdziwa*)
  let rec dfs (explode, ban) (graph, acc) v =
    let (graph, (visit, dependencies)) =
      try (graph, PMap.find v graph) with
      | Not_found -> (PMap.add v (false, []) graph, (false, []))
    in
    if v = ban && explode then raise Cykliczne else
    if visit then (graph, acc)
    else
      let graph = PMap.add v (true, []) graph in
      let (graph, acc_dependencies) = List.fold_left (dfs (true, ban)) (graph, acc) dependencies in
      (graph, v :: acc_dependencies)
  in
  (*conditioner ustawia zabronioną wartość i flagę explode na false*)
  let conditioner acc (value, _) = dfs (false, value) acc value in
  snd (List.fold_left conditioner (graph, []) connections)
