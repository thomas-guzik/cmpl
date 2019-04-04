
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
	// counter = compteur qui est utilise pour compter les variables, les parametres de fonction,
	// le nombre d'arguments d'un appel, le nombre de reference
	// id_save = numero d'un id qui sera sauvegarde par AffOuAppel ou lecture (simplement pour des raisons d'efficacite) 
	private static int it, bc, a, counter, id_save;

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
	
	// Ajoute a la liste trans une translation TRANSDON/TRANSCODE seulement si on est dans un module
	private static void onlyIfModuleAddVecteurTrans(int x) {
		if(desc.getUnite().equals("module")) {
			po.vecteurTrans(x);
			desc.incrNbTansExt();
		}
	}
	
	// Genere un descripteur de fichier conforme :
	// - Verifie si a pour chaque chaque fonction defini une proceduire lui correspond
	// - Remplie ensuite les champs adPo et NbPram avec les valeurs adequates
	private static void finishDesc() {
		boolean notDefined = true;
		desc.setTailleCode(po.getIpo());
		
		// Mise a jour de tabDef
		String defNom = "";
		for(int i = desc.getNbDef(); i > 0; i--) {
			defNom = desc.getDefNomProc(i);
			for(int j = it; j > 0; j--) {
				if(tabSymb[j].code != -1 && defNom.equals(UtilLex.repId(tabSymb[j].code)) && tabSymb[j].categorie == PROC) {
					desc.modifDefAdPo(i, tabSymb[j].info);
					desc.modifDefNbParam(i, tabSymb[j+1].info);
					notDefined = false;
					break;
				}
			}
			if(notDefined) {
				UtilLex.messErr(defNom + " n'est defini par aucune procedure");
			}
		}
		
		
		desc.ecrireDesc(UtilLex.nomSource);
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
	
	// Permet d'afficher le code Mnemo au fil de la compilation
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

		/* Ajout dans la pile d'execution de la valeur lu
		 * primaire -> val {}
		 */
		case 5: //
			po.produire(EMPILER);
			po.produire(vCour);
			break;

		/* Ajout dans la pile d'execution du contenu de la variable
		 * primaire -> ident {}
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
				onlyIfModuleAddVecteurTrans(TRANSDON);
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

		// Declaration constante : Ajoute la constante a tabSymb
		// consts -> const a=val;{} b=val;{} ...;{}
		case 30:
			if (presentIdent(bc) != 0) {
				UtilLex.messErr("Constante " + UtilLex.repId(UtilLex.numId) + " deja declare");
			} else {
				placeIdent(UtilLex.numId, CONSTANTE, tCour, vCour);
			}
			break;

		/* Declaration variable : Ajoute la variable dans tabSymb */
		
		// Preparation a la lecture des variables, on va devoir compter leur nombre
		// counter = 0
		// declarations -> partiedef? partieref? consts? {} vars? decprocs? 
		// decproc -> 'proc' ident parfixe? parmod? consts? {} vars? corps 
		case 31:
			counter = 0;
			break;
		
		// Ajout dans tabSymb des var	
		// vars -> var ent a{}, b{}; bool c{}, ...{}; ent ...{};
		case 32:
			if (presentIdent(bc) != 0) {
				UtilLex.messErr("Variable " + UtilLex.repId(UtilLex.numId) + " deja declare");
			}

			if (bc == 1) { // Contexte globale
				placeIdent(UtilLex.numId, VARGLOBALE, tCour, counter);
			} else { // Contexte locale
				placeIdent(UtilLex.numId, VARLOCALE, tCour, counter+tabSymb[bc-1].info+2);
			}
			counter++;
			break;
			
		// Produit RESERVER  seulement si on est dans un programme et a cause de la grammaire
		// on est dans un contexte globale
		// declarations -> partiedef? partieref? consts? vars? {} decprocs? 
		case 33:
			if(desc.getUnite().equals("programme")) {
				po.produire(RESERVER);
				po.produire(counter);
			}
			desc.setTailleGlobaux(counter);
			break;
		
		// tCour = type type -> 'ent'{43} | 'bool' {44}
		case 34:
			tCour = ENT;
			break;
		case 35:
			tCour = BOOL;
			break;

		/* AFFECTATION 40 */

		// On sauvegarde l'id lu au début dans id_save
		// AffOuAppel -> ident {} ( := expr | effixes effmodes )
		case 40:
			id_save = presentIdent(1);
			if (id_save == 0) {
				UtilLex.messErr("Affectation : " + UtilLex.repId(UtilLex.numId) + " non declare");
			}
			break;

		// On verifie que l'ident est du meme type que l'expression
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
				onlyIfModuleAddVecteurTrans(TRANSDON);
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

		// Pour des souci de non repetage du code, on rappellera 42 apres d'avoir generer LIRE...	
		// lecture -> lire ( ident{43}{42}, ident{43}{42}, ...{43}{42} )
		case 43:
			id_save = presentIdent(1);
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
			onlyIfModuleAddVecteurTrans(TRANSCODE);
			pileRep.empiler(po.getIpo());
			break;

		// inssi -> 'si' expr 'alors' expr 'sinon' {} ..
		// Après sinon, on doit renvoyer pc à la fin du if si if pris
		case 51:
			po.produire(BINCOND);
			po.produire(0); // fsi
			onlyIfModuleAddVecteurTrans(TRANSCODE);
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
			onlyIfModuleAddVecteurTrans(TRANSCODE);
			pileRep.empiler(po.getIpo());
			break;

		// boucle -> 'ttq' expr ... fait {}
		case 55:
			po.modifier(pileRep.depiler(), po.getIpo() + 3); // modif de bsifaux, jump après bincond
			po.produire(BINCOND);
			po.produire(pileRep.depiler()); // debut du ttq
			onlyIfModuleAddVecteurTrans(TRANSCODE);
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
			onlyIfModuleAddVecteurTrans(TRANSCODE);
			pileRep.empiler(po.getIpo());
			break;

		// inscond -> 'cond' expr : ins,{} expr : ins,{},... ins
		// inscond -> 'cond' expr : ins,{} expr : ins aut {},... ins
		// Seulement apres une virgule
		case 58:
			po.modifier(pileRep.depiler(), po.getIpo() + 3); // maj bsifaux
			po.produire(BINCOND);
			po.produire(pileRep.depiler());
			onlyIfModuleAddVecteurTrans(TRANSCODE);
			pileRep.empiler(po.getIpo());
			break;

		// inscond -> 'cond' expr : ins aut {58}{}
		case 59:
			pileRep.empiler(0);
			break;

		// inscond -> 'fcond' {}
		// La fin d'une lecture d'une condition peut se passer de 2 manieres
		// On a lu un 'aut', le dernier element dans la pile sera alors un 0
		// -> On depile et on fait rien
		// On a fini sur un cond, le dernier element est alors l'ipo jmp d'un BSIFAUX
		// -> On depile et on maj bsifaux
		// Ensuite on remonte le chainage des bincond jusqu'a arriver a 0
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

		/* GESTION BINCOND DEBUT PROCEDURE */
		
		// Genere un bincond seulement si des procedures existent
		// ET si on est dans un programme
		// Empile le jmp du BINCOND en 61 depile en 62
		// decprocs -> {}(decproc ptvg ) + {depilement,62}
		case 61:
			if(desc.getUnite().equals("programme")) {
				po.produire(BINCOND);
				po.produire(0);
				pileRep.empiler(po.getIpo());
			}
			break;
			
		// decprocs -> {empilement, 61}(decproc ptvg ) + {}
		case 62:
			if(desc.getUnite().equals("programme")) {
				po.modifier(pileRep.depiler(), po.getIpo() + 1);
			}
			break;
		
		// Ajoute l'ident dans tabSymb et une ligne pour les parametres
		// decproc -> 'proc' ident {} parfixe? parmod? consts? vars? corps 
		case 63:
			a = presentIdent(1);
			if (a != 0 && tabSymb[a].categorie == PROC) {
				UtilLex.messErr("Procedure " + UtilLex.repId(UtilLex.numId) + " deja declare");
			}
			
			placeIdent(UtilLex.numId, PROC, NEUTRE, po.getIpo() + 1);
			placeIdent(-1, PRIVEE, NEUTRE, 0);
			counter = 0;
			bc = it + 1;
			break;

		// Ajoute les parametres fixes dans tabSymb
		// pf -> type ident {} ( ',' ident {} )*
		case 64:
			if (presentIdent(bc) != 0) {
				UtilLex.messErr("Parametre fixe " + UtilLex.repId(UtilLex.numId) + " deja declare");
			}
			placeIdent(UtilLex.numId, PARAMFIXE, tCour, counter);
			counter++;
			break;

		// Ajoute les parametres modulaires dans tabSymb
		// pm -> type ident {} ( ',' ident {} )*
		case 65:
			if (presentIdent(bc) != 0) {
				UtilLex.messErr("Parametre modulable " + UtilLex.repId(UtilLex.numId) + " deja declare");
			}
			placeIdent(UtilLex.numId, PARAMMOD, tCour, counter);
			counter++;
			break;

		// Fin de la lecture des parametres
		// Mise a jour du nombre de param dns tabSymb
		// decproc -> 'proc' ident parfixe? parmod? {}
		case 66:
			tabSymb[it - counter].info = counter;
			break;
		
		// A present on lit les variables locales de la procedure
		// On initialise notre counter a 0 avec 31
		// decproc -> 'proc' ident parfixe? parmod? consts? {31} vars? corps	
			
		// Produit reserver a la fin de la lecture des variables seulement pour les procedures
		// vars -> 'var' ( type ident ( ','  ident )* ptvg )+ {}
		case 67:
			if(bc > 1) {
				po.produire(RESERVER);
				po.produire(counter);
			}
			break;
		
		// Fin de la lecture de la procedure	
		// Generation du RETOUR
		// Suppression des lignes desormais inutiles dans tabSymb (celles qui correspondent a des varlocales)
		// Masquage des autres lignes
		// decproc -> 'proc' ident parfixe? parmod? const? vars? corps {}
		case 68: 
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
			break;
		
		/* APPEL PROCEDURE */

		// On verifie qu'on appelle bien une procedure
		// AffOuAppel -> ident (... | {} (effixes (effmods)?)? )
		case 70:
			if (tabSymb[id_save].categorie != PROC) {
				UtilLex.messErr(UtilLex.repId(UtilLex.numId) + " n'est pas une procedure");
			}
			counter = 0; // Permet de compter le nombre d'arguement
			break;

		// On lit les arguments fixes et on releve les erreurs
		// effixes -> '(' (expr {} ( , expr {} )* )? ')'
		case 71:
			counter++;
			
			if (counter > tabSymb[id_save+1].info) {
				UtilLex.messErr("Appel : Le nombre de parametres est trop grand");
			}
			if (tabSymb[id_save + counter + 1].categorie != PARAMFIXE) {
				UtilLex.messErr("Appel : le parametre " + counter + " doit etre modulable");
			}
			break;

		// On lit les arguments modulables et on releve les erreurs
		// On genere les instructions d'empilement correspondant aux categories d'arguments
		// effmods -> '(' (ident {} ( , ident {} )* )? ')'
		case 72:
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
				onlyIfModuleAddVecteurTrans(TRANSDON);
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

		// Fin de la lecture de l'appel
		// On regarde si on a le bon nombre de parametre
		// Puis on produit APPEL
		// AffOuAppel -> ident (... | (effixes (effmods)?)? {})
		case 73:
			if(counter != tabSymb[id_save+1].info) {
				UtilLex.messErr("Appel : le nombre de parametres est invalide");
			}
			
			po.produire(APPEL);
			po.produire(tabSymb[id_save].info);
			
			if(desc.presentRef(UtilLex.repId(tabSymb[id_save].code)) != 0) {
				po.vecteurTrans(REFEXT);
				desc.incrNbTansExt();
			}
			
			po.produire(counter);
			break;
		
		/* GESTION DU DESCRIPTEUR */
		
		// unitprog -> 'programme' {} ident ':' declarations corps
		case 80:
			desc.setUnite("programme");
			break;
		
		// unitmodule -> 'module' {} ident ':' declarations
		case 81:
			desc.setUnite("module");
			break;
		
		// Ajout au descripteur des differentes definitions
		// partiedef -> 'def' ident {}  (',' ident {} )*
		case 83:
			if(desc.presentDef(UtilLex.repId(UtilLex.numId)) != 0) {
				UtilLex.messErr("Definition: " + UtilLex.repId(UtilLex.numId) + " deja defini");
			}
			desc.ajoutDef(UtilLex.repId(UtilLex.numId));
			break;
		
		/* PARTIE REF */
		
		// Ajout au descripteur des references
		// On les place egalement dans tabSymb
		// specif -> ident {} ( 'fixe' '(' type  ( ',' type  )* ')' )? 
        //          ( 'mod'  '(' type  ( ',' type  )* ')' )? 
		case 84:
			if(desc.presentRef(UtilLex.repId(UtilLex.numId)) != 0) {
				UtilLex.messErr("Reference " + UtilLex.repId(UtilLex.numId) + " deja declare");
			}
			
			counter = 0;
			desc.ajoutRef(UtilLex.repId(UtilLex.numId));
			placeIdent(UtilLex.numId, PROC, NEUTRE, desc.getNbRef());
			placeIdent(-1, PRIVEE, NEUTRE, counter);
			break;
		
		// On rajoute chaque parametre de la reference dans tabSymb	
		// specif -> ident ( 'fixe' '(' type {} ( ',' type {} )* ')' )? 
        //          ( 'mod'  '(' type ( ',' type )* ')' )? 
		case 85:
			placeIdent(-1, PARAMFIXE, tCour, counter);
			counter++;
			break;
		
		// specif -> ident ( 'fixe' '(' type {} ( ',' type {} )* ')' )? 
        //          ( 'mod'  '(' type {} ( ',' type {} )* ')' )? 
		case 86:
			placeIdent(-1, PARAMMOD, tCour, counter);
			counter++;
			break;	
		
		// On a fini de lire la "declaration" d'une reference, on note son nbParam dans le desc
		// On met aussi son nbParam dans tabSymb
		// partieref -> 'ref' specif {} ( ',' specif {}) * 	
		case 87:
			// desc.getNbRef() est aussi egale au numero de la derniere reference dans tabRef
			desc.modifRefNbParam(desc.getNbRef(), counter);
			tabSymb[it-counter].info = counter;
			break;
		
		// On verifie que le module definit bien des procedures
		// declarations -> partiedef? {} partieref? ...
		case 88:
			if(desc.getUnite().equals("module") && desc.getNbDef() == 0) {
				UtilLex.messErr("Le module definit aucune procedure");
			}
			break;
		
			
		/* FIN & GENERATIONS DU CODE */
		
		// unitprog -> 'programme' ident : declarations corps {254}{255}
		case 254:
			po.produire(ARRET);
			break; // Ce break est inutile quand on y pense
			
		// unitprog -> 'programme' ident : declarations corps {}
		// unitmodule -> 'module' ident : declarations {}
		case 255:
			finishDesc();
			po.constObj();
			po.constGen();
			
			afftabSymb();
			break;

		default:
			System.out.println("Point de generation non prevu dans votre liste");
			break;

		}
		affMnemoIpo();
	}
}
