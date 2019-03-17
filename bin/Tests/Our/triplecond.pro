programme triplecond:

var bool a,b,c,d,e,f, aa,bb,cc,dd,ee,ff,aaa,jj;	{adresses variables dans la pile d'exécution: 0, 1, 2, 3, 4, 5}
debut
	lire(a,b,c,d,e,f, aa,bb,cc,dd,ee,ff,aaa,jj);
	cond
		a : ecrire(1),
		b : ecrire(2),
		c : cond aa: ecrire(4) fcond,
		e : cond
			bb : ecrire(5),
			cc : ecrire(6) fcond,
		f : cond dd : ecrire(7),
			jj : ecrire(2) fcond
		aut cond 
			ee : ecrire(6),
			ff : ecrire(7)
			aut cond aaa: ecrire(8) fcond
		fcond
	fcond;
fin
