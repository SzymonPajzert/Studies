OCAMLC=ocamlc
OCAMLOPT=ocamlopt
OCAMLDEP=ocamldep
INCLUDES=
OCAMLFLAGS=$(INCLUDES) -g
OCAMLOPTFLAGS=$(INCLUDES)

#tu nalezy wpisac wszystkie moduly do skompilowania w kolejnosci zgodnej z zaleznosciami
MODS= naiveset.mli naiveset.ml iset.mli iset.ml
TEST= manualtest.ml

all:
	$(OCAMLOPT) -o test $(OCAMLOPTFLAGS) $(MODS) $(TEST)

lib:
	$(OCAMLOPT) -o iset $(OCAMLOPTFLAGS) $(MODS)

.SUFFIXES: .mll .mly .ml .mli .cmo .cmi .cmx

.mll.ml:
	ocamllex $<

.mly.mli:
	ocamlyacc $<

.mly.ml:
	ocamlyacc $<

.ml.cmo:
	$(OCAMLC) $(OCAMLFLAGS) -c $<

.mli.cmi:
	$(OCAMLC) $(OCAMLFALGS) -c $<

.ml.cmx:
	$(OCAMLOPT) $(OCAMLOPTFLAGS) -c $<

clean:
	rm -f *~ *.cm[oix] *.o *.a res*.txt nkjp.cmxa
