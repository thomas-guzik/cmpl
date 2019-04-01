
/*********************************************************************************
 * VARIABLES ET METHODES FOURNIES PAR LA CLASSE UtilLex (cf libclass)            *
 *       complement à l'ANALYSEUR LEXICAL produit par ANTLR                      *
 *                                                                               *
 *                                                                               *
 *   nom du programme compile, sans suffixe : String UtilLex.nomSource           *
 *   ------------------------                                                    *
 *                                                                               *
 *   attributs lexicaux (selon items figurant dans la grammaire):                *
 *   ------------------                                                          *
 *     int UtilLex.valNb = valeur du dernier nombre entier lu (item nbentier)    *
 *     int UtilLex.numId = code du dernier identificateur lu (item ident)        *
 *                                                                               *
 *                                                                               *
 *   methodes utiles :                                                           *
 *   ---------------                                                             *
 *     void UtilLex.messErr(String m)  affichage de m et arret compilation       *
 *     String UtilLex.repId(int nId) delivre l'ident de codage nId               *
 *     void afftabSymb()  affiche la table des symboles                          *
 *********************************************************************************/

import java.io.*;

// classe de mise en oeuvre du compilateur
// =======================================
// (verifications semantiques + production du code objet)
/**
 * @author Guzik Thomas, Morel Adam, Hart Maximillian
 *
 * 
 * 
 * 
 */
public class PtGen {

	// constantes manipulees par le compilateur
	// ----------------------------------------

	private static final int

	// taille max de la table des symboles
	MAXSYMB = 300,

			// codes MAPILE :
			RESERVER = 1, EMPILER = 2, CONTENUG = 3, AFFECTERG = 4, OU = 5, ET = 6, NON = 7, INF = 8, INFEG = 9,
			SUP = 10, SUPEG = 11, EG = 12, DIFF = 13, ADD = 14, SOUS = 15, MUL = 16, DIV = 17, BSIFAUX = 18,
			BINCOND = 19, LIRENT = 20, LIREBOOL = 21, ECRENT = 22, ECRBOOL = 23, ARRET = 24, EMPILERADG = 25,
			EMPILERADL = 26, CONTENUL = 27, AFFECTERL = 28, APPEL = 29, RETOUR = 30,

			// codes des valeurs vrai/faux
			VRAI = 1, FAUX = 0,

			// types permis :
			ENT = 1, BOOL = 2, NEUTRE = 3,

			// cat�gories possibles des identificateurs :
			CONSTANTE = 1, VARGLOBALE = 2, VARLOCALE = 3, PARAMFIXE = 4, PARAMMOD = 5, PROC = 6, DEF = 7, REF = 8,
			PRIVEE = 9,

			// valeurs possible du vecteur de translation
			TRANSDON = 1, TRANSCODE = 2, REFEXT = 3;

	// utilitaires de controle de type
	// -------------------------------

	private static void verifEnt() {
		if (tCour != ENT)
			UtilLex.messErr("expression entiere attendue");
	}

	private static void verifBool() {
		if (tCour != BOOL)
			UtilLex.messErr("expression booleenne attendue");
	}

	// pile pour gerer les chaines de reprise et les branchements en avant
	// -------------------------------------------------------------------

	private static TPileRep pileRep;

	// production du code objet en memoire
	// -----------------------------------

	private static ProgObjet po;

	// COMPILATION SEPAREE
	// -------------------
	//
	// modification du vecteur de translation associe au code produit
	// + incrementation attribut nbTransExt du descripteur
	// NB: effectue uniquement si c'est une reference externe ou si on compile un
	// module
	private static void modifVecteurTrans(int valeur) {
		if (valeur == REFEXT || desc.getUnite().equals("module")) {
			po.vecteurTrans(valeur);
			desc.incrNbTansExt();
		}
	}

	// descripteur associe a un programme objet
	private static Descripteur desc;

	// autres variables fournies
	// -------------------------
	public static String trinome = "Hart-Maximilian_Morel-Adam_Guzik-Thomas"; // MERCI de renseigner ici un nom pour le
																				// trinome, constitue de exclusivement
																				// de lettres

	private static int tCour; // type de l'expression compilee
	private static int vCour; // valeur de l'expression compilee le cas echeant
	// Definition de la table des symboles
	//
	private static EltTabSymb[] tabSymb = new EltTabSymb[MAXSYMB + 1];

	// it = indice de remplissage de tabSymb
	// bc = bloc courant (=1 si le bloc courant est le programme principal)
	// a = variable a tout faire
	// counter = compteur qui se retrouve partout // A detailler plus
	// nbRef = compte le nombre de reference
	private static int it, bc, a, counter, id_save, nbRef;

	// utilitaire de recherche de l'ident courant (ayant pour code UtilLex.numId)
	// dans tabSymb
	// rend en resultat l'indice de cet ident dans tabSymb (O si absence)
	private static int presentIdent(int binf) {
		int i = it;
		while (i >= binf && tabSymb[i].code != UtilLex.numId)
			i--;
		if (i >= binf)
			return i;
		else
			return 0;
	}

	// utilitaire de placement des caracteristiques d'un nouvel ident dans tabSymb
	//
	private static void placeIdent(int c, int cat, int t, int v) {
		if (it == MAXSYMB)
			UtilLex.messErr("debordement de la table des symboles");
		it = it + 1;
		tabSymb[it] = new EltTabSymb(c, cat, t, v);
	}

	/* DEBUG */
	// utilitaire d'affichage de la table des symboles
	//
	private static void afftabSymb() {
		System.out.println("       code           categorie      type    info");
		System.out.println("      |--------------|--------------|-------|----");
		for (int i = 1; i <= it; i++) {
			if (i == bc) {
				System.out.print("bc=");
				Ecriture.ecrireInt(i, 3);
			} else if (i == it) {
				System.out.print("it=");
				Ecriture.ecrireInt(i, 3);
			} else
				Ecriture.ecrireInt(i, 6);
			if (tabSymb[i] == null)
				System.out.println(" r�f�rence NULL");
			else
				System.out.println(" " + tabSymb[i]);
		}
		System.out.println();
	}

	private static final String[] inst = { "", "reserver  ", "empiler   ", "contenug  ", "affecterg ", "ou        ",
			"et        ", "non       ", "inf       ", "infeg     ", "sup       ", "supeg     ", "eg        ",
			"diff      ", "add       ", "sous      ", "mul       ", "div       ", "bsifaux   ", "bincond   ",
			"lirent    ", "lirebool  ", "ecrent    ", "ecrbool   ", "arret     ", "empileradg", "empileradl",
			"contenul  ", "affecterl ", "appel     ", "retour    " };

	private static int old_ipo = 0;

	private static void affMnemoIpo() {
		if (po.getIpo() > 0) {
			int diff = po.getIpo() - old_ipo;
			if (diff == 1) {
				System.out.println(String.valueOf(po.getIpo()) + " " + inst[po.getElt(po.getIpo())]);
			} else if (diff == 2) {
				System.out.println(String.valueOf(po.getIpo() - 1) + " " + inst[po.getElt(po.getIpo() - 1)]
						+ po.getElt(po.getIpo()));
			} else if (diff == 3) {
				System.out.println(String.valueOf(po.getIpo() - 2) + " " + inst[po.getElt(po.getIpo() - 2)]
						+ po.getElt(po.getIpo() - 1) + " " + po.getElt(po.getIpo()));
			}
		}
		old_ipo = po.getIpo();
	}

	/* FIN DEBUG */

	// initialisations A COMPLETER SI BESOIN
	// -------------------------------------

	public static void initialisations() {

		// indices de gestion de la table des symboles
		it = 0;
		bc = 1;
		counter = 0;
		id_save = 0;
		nbRef = 0;

		// pile des reprises pour compilation des branchements en avant
		pileRep = new TPileRep();
		// programme objet = code Mapile de l'unite en cours de compilation
		po = new ProgObjet();
		// COMPILATION SEPAREE: desripteur de l'unite en cours de compilation
		desc = new Descripteur();

		// initialisation necessaire aux attributs lexicaux
		UtilLex.initialisation();

		// initialisation du type de l'expression courante
		tCour = NEUTRE;

	} // initialisations

	// code des points de generation A COMPLETER
	// -----------------------------------------
	public static void pt(int numGen) {
		System.out.println("numGen = " + numGen);

		switch (numGen) {
		case 0:
			initialisations();
			break;

		/* VALEUR -> nb | +nb | -nb | 'vrai' | 'faux' */
		case 1: // nb
			tCour = ENT;
			vCour = UtilLex.valNb;
			break;

		case 2: // -nb
			tCour = ENT;
			vCour = -UtilLex.valNb;
			break;

		case 3: // 'vrai'
			tCour = BOOL;
			vCour = VRAI;
			break;
		case 4: // 'faux'
			tCour = BOOL;
			vCour = FAUX;
			break;

		/* EXPRESSIONS */

		/*
		 * Empiler une valeur brute primaire -> val {}
		 */
		case 5: //
			po.produire(EMPILER);
			po.produire(vCour);
			break;

		/*
		 * Empiler une variable ou constante primaire -> ident {}
		 */
		case 6:
			a = presentIdent(1);
			if (a == 0) {
				UtilLex.messErr(UtilLex.repId(UtilLex.numId) + " non declare");
			}

			tCour = tabSymb[a].type;
			switch (tabSymb[a].categorie) {
			case CONSTANTE:
				po.produire(EMPILER);
				po.produire(tabSymb[a].info);
				break;
			case VARGLOBALE:
				po.produire(CONTENUG);
				po.produire(tabSymb[a].info);
				break;
			case PARAMFIXE:
			case VARLOCALE:
				po.produire(CONTENUL);
				po.produire(tabSymb[a].info);
				po.produire(0);
				break;
			case PARAMMOD:
				po.produire(CONTENUL);
				po.produire(tabSymb[a].info);
				po.produire(1);
				break;
			default:
				UtilLex.messErr(UtilLex.repId(UtilLex.numId) + " n'est pas de la bonne categorie");
			}
			break;

		/*
		 * Verifications du type pour les operations suivantes :
		 * 
		 * exp3 -> exp4 = {} exp4 {} exp4 <= {} exp4 {} exp4 ... {} exp4 {}
		 * 
		 * exp4 -> exp5 + {} exp5 {} exp5 - {} exp5 {}
		 * 
		 * exp5 -> primaire * {} primaire {} primaire div {} primaire {}
		 */
		case 7:
			verifEnt();
			break;

		/*
		 * Verifications du type pour les operations suivantes :
		 * 
		 * expr -> exp1 ou {} exp1 {} exp1 -> exp2 et {} exp2 {} exp2 -> non exp2 {}
		 */
		case 8:
			verifBool();
			break;

		/* OPERATIONS BOOLEENNES 10-12 */

		case 10: // x ou y {verif}{}
			po.produire(OU);
			break;

		case 11: // x et y {verif}{}
			po.produire(ET);
			break;

		case 12: // non x {verif}{}
			po.produire(NON);
			break;

		/* OPERATIONS DE COMPARAISONS 13-18 */

		case 13: // x < y {verif}{}
			po.produire(INF);
			tCour = BOOL;
			break;

		case 14: // x <= y {verif}{}
			po.produire(INFEG);
			tCour = BOOL;
			break;

		case 15: // x > y {verif}{}
			po.produire(SUP);
			tCour = BOOL;
			break;

		case 16: // x >= y {verif}{}
			po.produire(SUPEG);
			tCour = BOOL;
			break;

		case 17: // x = y {verif}{}
			po.produire(EG);
			tCour = BOOL;
			break;

		case 18: // x <> y {verif}{}
			po.produire(DIFF);
			tCour = BOOL;
			break;

		/* OPERATIONS ARITHMETIQUES 19-22 */

		case 19: // x + y {verif}{}
			po.produire(ADD);
			break;

		case 20: // x - y {verif}{}
			po.produire(SOUS);
			break;

		case 21: // x * y {verif}{}
			po.produire(MUL);
			break;

		case 22: // x div y {verif}{}
			po.produire(DIV);
			break;

		/* FIN EXPRESSION */

		/* DECLARATIONS 30 */

		/*
		 * Declaration constante consts -> const a=val;{} b=val;{} ...;{}
		 */
		case 30:
			if (presentIdent(bc) != 0) {
				UtilLex.messErr("Constante " + UtilLex.repId(UtilLex.numId) + " deja declare");
			} else {
				placeIdent(UtilLex.numId, CONSTANTE, tCour, vCour);
			}
			break;

		/*
		 * Declaration variable vars -> var ent a{}, b{}; bool c{}, ...{}; ent ...{};
		 */
		case 31:
			if (presentIdent(bc) != 0) {
				UtilLex.messErr("Variable " + UtilLex.repId(UtilLex.numId) + " deja declare");
			}

			if (bc == 1) {
				placeIdent(UtilLex.numId, VARGLOBALE, tCour, counter);
			} else {
				placeIdent(UtilLex.numId, VARLOCALE, tCour, counter);
			}
			counter++;
			break;
		/*
		 * Reservation de place dans la pile vars -> var (...;)+ {}
		 * seulement si module == false
		 */
		case 32:
			if(desc.getUnite().equals("programme")) {
				po.produire(RESERVER);
				if(bc==1) {
					po.produire(counter);
					desc.setTailleGlobaux(counter);
				}
				else
					po.produire(counter-tabSymb[bc-1].info-2);
			}
			
			break;
		/*
		 * tCour = type type -> 'ent'{43} | 'bool' {44}
		 */
		case 33:
			tCour = ENT;
			break;
		case 34:
			tCour = BOOL;
			break;

		/* AFFECTATION 40 */

		/*
		 * On sauvegarde l'id lu au début dans id_save AffOuAppel -> ident {} ( := expr
		 * | effixes effmodes )
		 */
		case 40:
			id_save = presentIdent(1);
			if (id_save == 0) {
				UtilLex.messErr(UtilLex.repId(UtilLex.numId) + " non declare");
			}
			
			// System.out.println("p = "+ id_save);
			
			break;

		// AffOuAppel -> ident := expr {}
		case 41:
			if (tCour != tabSymb[id_save].type) {
				UtilLex.messErr("Affectation : Types incompatibles");
			}
			break;

		/*
		 * Fin de traitement de la partie droite, on compare si les types sont les memes
		 * Que les categories sont ok Et on affecte AffOuAppel -> ident := expr
		 * {verifTcour}{} | Non traite
		 */
		case 42:
			switch (tabSymb[id_save].categorie) {
			case VARGLOBALE:
				po.produire(AFFECTERG);
				po.produire(tabSymb[id_save].info);
				break;
			case VARLOCALE:
				po.produire(AFFECTERL);
				po.produire(tabSymb[id_save].info);
				po.produire(0);
				break;
			case PARAMMOD:
				po.produire(AFFECTERL);
				po.produire(tabSymb[id_save].info);
				po.produire(1);
				break;
			default:
				UtilLex.messErr("Affectation : Categorie invalide");
			}
			break;

		// lecture -> lire ( ident{43}{42}, ident{43}{42}, ...{43}{42} )
		case 43:
			id_save = presentIdent(bc);
			if (id_save == 0) {
				UtilLex.messErr(UtilLex.repId(UtilLex.numId) + " non declare");
			}
			switch (tabSymb[id_save].type) {
			case ENT:
				po.produire(LIRENT);
				break;
			case BOOL:
				po.produire(LIREBOOL);
				break;
			default:
				UtilLex.messErr("Lecture : Type de variable inconnu");
			}

			break;

		/*
		 * ECRITURE ecriture -> ecrire ( expr{}, expr{}, ...{} )
		 */
		case 44:
			switch (tCour) {
			case ENT:
				po.produire(ECRENT);
				break;
			case BOOL:
				po.produire(ECRBOOL);
				break;
			default:
				UtilLex.messErr("Ecrire : Type de variable inconnu");
			}
			break;

		/* STRUCTURES CONDITIONNELLES 50 */

		/* SI */

		// inssi -> 'si' expr {verifBool} {} 'alors' ..
		case 50:
			po.produire(BSIFAUX);
			po.produire(0); // fsi OU sinon
			pileRep.empiler(po.getIpo());
			break;

		// inssi -> 'si' expr 'alors' expr 'sinon' {} ..
		// Après sinon, on doit renvoyer pc à la fin du if si if pris
		case 51:
			po.produire(BINCOND);
			po.produire(0); // fsi
			po.modifier(pileRep.depiler(), po.getIpo() + 1);
			pileRep.empiler(po.getIpo());
			break;

		// inssi -> ... fsi {}
		case 52:
			po.modifier(pileRep.depiler(), po.getIpo() + 1);
			break;

		/* TTQ */
		// boucle -> 'ttq' {} expr 'faire'
		case 53:
			pileRep.empiler(po.getIpo() + 1); // debut ttq
			break;

		// boucle -> 'ttq' expr {verifBool}{} 'faire' ... fait
		case 54:
			po.produire(BSIFAUX);
			po.produire(0); // fait
			pileRep.empiler(po.getIpo());
			break;

		// boucle -> 'ttq' expr ... fait {}
		case 55:
			po.modifier(pileRep.depiler(), po.getIpo() + 3); // modif de bsifaux, jump après bincond
			po.produire(BINCOND);
			po.produire(pileRep.depiler()); // debut du ttq
			break;

		/* COND */

		// inscond -> 'cond' {} ...
		case 56:
			pileRep.empiler(0);
			break;

		// inscond -> 'cond' expr {verifBool}{} : ins, expr {verifBool}{} ...
		case 57:
			po.produire(BSIFAUX);
			po.produire(0);
			pileRep.empiler(po.getIpo());
			break;

		// inscond -> 'cond' expr : ins,{} expr : ins,{},... ins
		// inscond -> 'cond' expr : ins,{} expr : ins aut {},... ins
		// Seulement apres une virgule
		case 58:
			po.modifier(pileRep.depiler(), po.getIpo() + 3); // maj bsifaux
			po.produire(BINCOND);
			po.produire(pileRep.depiler());
			pileRep.empiler(po.getIpo());
			break;

		// inscond -> 'cond' expr : ins aut {58}{}
		case 59:
			pileRep.empiler(0);
			break;

		// inscond -> 'fcond' {}
		case 60:
			a = pileRep.depiler();
			if (a != 0) { // bsifaux a mettre a jour
				po.modifier(a, po.getIpo() + 1);
			}
			a = pileRep.depiler();
			int elti;
			while (a != 0) { // bcond a mettre a jour
				elti = po.getElt(a);
				po.modifier(a, po.getIpo() + 1);
				a = elti;
			}
			break;

		/* PROCEDURES */

		// decproc -> 'proc' ident {}
		case 61:
			if (presentIdent(1) != 0) {
				UtilLex.messErr("Procedure " + UtilLex.repId(UtilLex.numId) + " deja declare");
			}
			

			placeIdent(UtilLex.numId, PROC, NEUTRE, po.getIpo() + 1);
			placeIdent(-1, PRIVEE, NEUTRE, 0);
			counter = 0;
			bc = it + 1;
			break;

		// pf -> type ident {} ( ',' ident {} )*
		case 62:
			if (presentIdent(bc) != 0) {
				UtilLex.messErr("Parametre fixe " + UtilLex.repId(UtilLex.numId) + " deja declare");
			}
			placeIdent(UtilLex.numId, PARAMFIXE, tCour, counter);
			counter++;
			break;

		// pm -> type ident {} ( ',' ident {} )*
		case 63:
			if (presentIdent(bc) != 0) {
				UtilLex.messErr("Parametre modulable " + UtilLex.repId(UtilLex.numId) + " deja declare");
			}
			placeIdent(UtilLex.numId, PARAMMOD, tCour, counter);
			counter++;
			break;

		// Mise a jour du nombre de param dns tabSymb
		// decproc -> 'proc' ident parfixe? parmod? {}
		case 64:
			tabSymb[it - counter].info = counter;
			counter += 2;
			break;

		/* APPEL PROCEDURE */

		// AffOuAppel -> ident (... | {} (effixes (effmods)?)? )
		case 65:
			if (tabSymb[id_save].categorie != PROC) {
				UtilLex.messErr(UtilLex.repId(UtilLex.numId) + " n'est pas une procedure");
			}
			counter = 0; // Permet de compter le nombre d'arguement
			break;

		// effixes -> '(' (expr {} ( , expr {} )* )? ')'
		case 66:
			counter++;
			
			if (counter > tabSymb[id_save+1].info) {
				UtilLex.messErr("Appel : Le nombre de parametres est trop grand");
			}
			if (tabSymb[id_save + counter + 1].categorie != PARAMFIXE) {
				UtilLex.messErr("Appel : le parametre " + counter + " doit etre modulable");
			}
			
			break;

		// effmods -> '(' (ident {} ( , ident {} )* )? ')'
		case 67:
			counter++;
			
			if (counter > tabSymb[id_save+1].info) {
				UtilLex.messErr("Appel : Le nombre de parametres est trop grand");
			}
			if (tabSymb[id_save + counter + 1].categorie != PARAMMOD) {
				UtilLex.messErr("Appel : Le parametre " + counter + " doit etre fixe");
			}

			a = presentIdent(1);
			if (a == 0) {
				UtilLex.messErr("Appel : " + UtilLex.repId(UtilLex.numId) + " non declare");
			}
			if (tabSymb[a].type != tabSymb[id_save + counter + 1].type) {
				UtilLex.messErr("Appel : " + UtilLex.repId(UtilLex.numId) + "n'est pas du bon type");
			}

			switch (tabSymb[a].categorie) {
			case VARGLOBALE:
				po.produire(EMPILERADG);
				po.produire(tabSymb[a].info);
				break;
			case VARLOCALE:
				po.produire(EMPILERADL);
				po.produire(tabSymb[a].info);
				po.produire(0);
				break;
			case PARAMMOD:
				po.produire(EMPILERADL);
				po.produire(tabSymb[a].info);
				po.produire(1);
				break;
			default:
				UtilLex.messErr("Appel : " + UtilLex.repId(UtilLex.numId) + "n'est pas de la bonne categorie");
			}

			break;

		// AffOuAppel -> ident (... | (effixes (effmods)?)? {})
		case 68:
			if(counter != tabSymb[id_save+1].info) {
				UtilLex.messErr("Appel : le nombre de parametres est invalide");
			}
			
			po.produire(APPEL);
			po.produire(tabSymb[id_save].info);
			po.produire(counter);
			break;
			
		// decprocs -> {}(decproc ptvg ) + {depilement,70}
		case 69:
			po.produire(BINCOND);
			po.produire(0);
			pileRep.empiler(po.getIpo());
			break;
			
		// decprocs -> {empilement, 69}(decproc ptvg ) + {}
		case 70:
			po.modifier(pileRep.depiler(), po.getIpo() + 1);
			break;
		
		/* GESTION DES MODULES */
		
		// unitprog -> 'programme' {} ident ':' declarations corps
		case 71:
			desc.setUnite("programme");
			break;
		
		// unitmodule -> 'module' {} ident ':' declarations
		case 72:
			desc.setUnite("module");
			break;
		
		// partiedef -> 'def' ident {}  (',' ident {} )*
		case 73:
			desc.ajoutDef(UtilLex.repId(UtilLex.numId));
			break;
		
		/* PARTIE REF */
		
		// specif -> ident {} ( 'fixe' '(' type  ( ',' type  )* ')' )? 
        //          ( 'mod'  '(' type  ( ',' type  )* ')' )? 
		case 74:
			counter = 0;
			desc.ajoutRef(UtilLex.repId(UtilLex.numId));
			break;
		
		// specif -> ident ( 'fixe' '(' type {} ( ',' type {} )* ')' )? 
        //          ( 'mod'  '(' type {} ( ',' type {} )* ')' )? 
		case 75:
			counter++;
			break;
			
		// partieref -> 'ref' specif {} ( ',' specif {}) * 	
		case 76:
			nbRef++;
			desc.modifRefNbParam(nbRef, counter);
			break;
		
		// declarations -> partiedef? {} partieref? ...
		case 77:
			if(desc.getUnite().equals("module") && desc.getNbDef() == 0) {
				UtilLex.messErr("Le module definit aucune procedure");
			}
			break;
		
		
		/*
		 * FIN & GENERATIONS DU CODE corps -> 'debut' instructions 'fin' {}
		 */
		case 255:
			if (bc == 1) {
				po.produire(ARRET);
				desc.setTailleCode(po.getIpo());
				po.constObj();
				po.constGen();
				desc.ecrireDesc(UtilLex.nomSource);
				afftabSymb();
			} else {
				for (int i = it; i >= bc; i--) {
					if (tabSymb[i].categorie == VARLOCALE || tabSymb[i].categorie == CONSTANTE) {
						tabSymb[i] = null; it--;
					} else if (tabSymb[i].categorie == PARAMFIXE || tabSymb[i].categorie == PARAMMOD) {
						tabSymb[i].code = -1;
					} else {
						UtilLex.messErr("Erreur interne avec tabSymb");
					}
				}
				po.produire(RETOUR);
				po.produire(tabSymb[bc - 1].info);
				bc = 1;
			}
			break;

		default:
			System.out.println("Point de generation non prevu dans votre liste");
			break;

		}
		affMnemoIpo();
	}
}
