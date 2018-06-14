[out:xml][timeout:900][bbox:{{bbox}}];

node(poly:"{{poly}}") -> .ns;
way(bn.ns) -> .ws;
(rel(bn.ns); rel(bw.ws););
(._; rel(br);) -> .rs;

node.ns[name];
convert NODE ::id=id(), ::geom=geom(), version=version(), ::=::;
out qt geom;

way.ws[name](if: is_closed());
convert WAY ::id=id(), ::geom=hull(geom()), version=version(), ::=::;
out qt geom;

way.ws[name](if: !is_closed());
convert WAY ::id=id(), ::geom=trace(geom()), version=version(), ::=::;
out qt geom;

rel.rs[name];
map_to_area; rel(pivot);
convert REL ::id=id(), ::geom=hull(geom()), version=version(), ::=::;
out qt geom;
