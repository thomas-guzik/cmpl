
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
	// D�finition de la table des symboles
	//
	private static EltTabSymb[] tabSymb = new EltTabSymb[MAXSYMB + 1];

	// it = indice de remplissage de tabSymb
	// bc = bloc courant (=1 si le bloc courant est le programme principal)
	// i = variable a tout faire
	// counterVar = compte le nb de variable lors de la declarations
	// tAff = type de la variable a affecter
	// iAff = placement de la pile de la variable a affecter
	private static int it, bc, i, counterVar, tAff, iAff;

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

	
	
	private static void err(String s) {
		
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
	
	private static final String[] inst = { "", "reserver  ", "empiler   ",
		"contenug  ", "affecterg ", "ou        ", "et        ",
		"non       ", "inf       ", "infeg     ", "sup       ",
		"supeg     ", "eg        ", "diff      ", "add       ",
		"sous      ", "mul       ", "div       ", "bsifaux   ",
		"bincond   ", "lirent    ", "lirebool  ", "ecrent    ",
		"ecrbool   ", "arret     ", "empileradg", "empileradl",
		"contenul  ", "affecterl ", "appel     ", "retour    " };

	
	private static int mnemoCounter = 1;
	
	private static void affMnemoIpo(){
		try { // Peut generer une erreur si aucun po[] est vide
			int diff = po.getIpo() - mnemoCounter;
			if(diff == 0) {
				System.out.println(String.valueOf(po.getIpo()) + " " + inst[po.getElt(po.getIpo())]);
			}
			else if(diff == 1) {
				System.out.println(String.valueOf(po.getIpo()-1) + " " + inst[ po.getElt(po.getIpo()-1) ] +  po.getElt(po.getIpo()));
			}
			else if(diff == 2){
				System.out.println(String.valueOf(po.getIpo()-2) + " " + inst[ po.getElt(po.getIpo()-2) ] +  po.getElt(po.getIpo()-1) + " " + po.getElt(po.getIpo()));
			}
			mnemoCounter = po.getIpo()+1;
		}
		catch (NullPointerException e) {}
	}

	/* FIN DEBUG */

	// initialisations A COMPLETER SI BESOIN
	// -------------------------------------

	public static void initialisations() {

		// indices de gestion de la table des symboles
		it = 0;
		bc = 1;
		counterVar = 0;
		iAff = 0;
		tAff = NEUTRE;

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
		// System.out.println("numGen = " + numGen);
		
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
			
		/* Empiler une valeur brute
		 * primaire -> val {}
		 */
		case 5: // 
			po.produire(EMPILER);
			po.produire(vCour);
			break;
			
		/* Empiler une variable ou constante
		 * primaire -> ident {}	
		 */
		case 6:
			i = presentIdent(bc);
			if (i == 0) {
				UtilLex.messErr(UtilLex.repId(UtilLex.numId) + " non declare");
			}

			tCour = tabSymb[i].type;
			switch (tabSymb[i].categorie) {
			case CONSTANTE:
				po.produire(EMPILER);
				po.produire(tabSymb[i].info);
				break;
			case VARGLOBALE:
				po.produire(CONTENUG);
				po.produire(tabSymb[i].info);
				break;
			}
			break;
     
		/* Verifications du type pour les operations suivantes :
		 * 
         * exp3 -> exp4 = {} exp4 {}
         *         exp4 <= {} exp4 {}
         *         exp4 ... {} exp4 {}
         * 
         * exp4 -> exp5 + {} exp5 {}
         *         exp5 - {} exp5 {}
         * 
         * exp5 -> primaire * {} primaire {}
         *         primaire div {} primaire {}
         */
		case 7:
			verifEnt();
			break;
			
		/* Verifications du type pour les operations suivantes :
		 * 
		 * expr -> exp1 ou {} exp1 {}
		 * exp1 -> exp2 et {} exp2 {}
		 * exp2 -> non exp2 {}
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

		/* AFFECTATION  + Lire 30-32 */
		
		/* On sauvegarde le type (tAff) et le placement dans la pile (iAff) de la var � affecter
		 * AffOuAppel -> x := {} y | Non traite
		 */
		case 30:
			i = presentIdent(bc);
			if (i == 0) {
				UtilLex.messErr(UtilLex.repId(UtilLex.numId) + " non declare");
			}
			
			else if (tabSymb[i].categorie == VARGLOBALE) {
				tAff = tabSymb[i].type;
				iAff = tabSymb[i].info;
			}
			else {
				UtilLex.messErr("Categorie invalide");
			}
			break;
		
		/* Fin de traitement de la partie droite, on compare si les types sont les memes
		 * Et on affecte
		 * AffOuAppel ->  x := y {}	| Non traite
		 */
		case 31:
			if(tCour == tAff) {
				po.produire(AFFECTERG);
				po.produire(iAff);
			}
			else {
				UtilLex.messErr("Affectation : Types incompatibles");
			}
		break;
		
		case 32: // lecture -> lire ( x{}, y{}, ...{} )
			i = presentIdent(bc);
			if (i == 0) {
				UtilLex.messErr(UtilLex.repId(UtilLex.numId) + " non declare");
			}
			
			else if (tabSymb[i].categorie == VARGLOBALE) {
				if (tabSymb[i].type == ENT) {
					po.produire(LIRENT);
				} else if (tabSymb[i].type == BOOL) {
					po.produire(LIREBOOL);
				} else {
					UtilLex.messErr("Type de variable inconnu");
				}
				po.produire(AFFECTERG);
				po.produire(tabSymb[i].info);
			} else {
				UtilLex.messErr("Categorie invalide");
			}
			break;
		
		/* ECRITURE 
		 * ecriture -> ecrire ( x{}, y{}, ...{} )
		 */
		case 33:
			if (tCour == ENT) {
				po.produire(ECRENT);
			} else if (tCour == BOOL) {
				po.produire(ECRBOOL);
			} else {
				UtilLex.messErr("Type de variable inconnu");
			}
		break;

		/* DECLARATIONS 40 */

		/* Declaration constante
		 * consts -> const a=val;{} b=val;{} ...;{}
		 */
		case 40:
			if(presentIdent(bc) == 0) {
				placeIdent(UtilLex.numId, CONSTANTE, tCour, vCour);
			}
			else {
				UtilLex.messErr("Constante " + UtilLex.repId(UtilLex.numId) + " deja declare");
			}
		break;
		
		/* Declaration variable
		 * vars -> var ent a{}, b{}; bool c{}, ...{}; ent ...{};
		 */	
		case 41:
			if(presentIdent(bc) == 0) {
				placeIdent(UtilLex.numId, VARGLOBALE, tCour, counterVar);
				counterVar++;
			}
			else {
				UtilLex.messErr("Variable " + UtilLex.repId(UtilLex.numId) + " deja declare");
			}
		break;
		/* Reservation de place dans la pile
		 * vars -> var (...;)+ {} 	
		 */
		case 42:
			po.produire(RESERVER);
			po.produire(counterVar);
		break;
		/* tCour = type
		 * type -> 'ent'{43} | 'bool' {44} 
		 */
		case 43:
			tCour = ENT;
		break;
		case 44:
			tCour = BOOL;
		break;
		
		/* STRUCTURES CONDITIONNELLES 50 */
			
		/* SI */
			
		// inssi -> 'si' expr {verifBool} {} 'alors' ..
		case 50:
			po.produire(BSIFAUX);
			po.produire(0); // fsi OU sinon
			pileRep.empiler(po.getIpo()); 
		break;
		
		// inssi -> 'si' expr 'alors' expr 'sinon' {}  ..
		// Après sinon, on doit renvoyer pc à la fin du if si if pris
		case 51:
			po.produire(BINCOND);
			po.produire(0); // fsi
			po.modifier(pileRep.depiler(), po.getIpo()+1);
			pileRep.empiler(po.getIpo());
		break;
		
		// inssi -> ... fsi {}
		case 52:
			po.modifier(pileRep.depiler(), po.getIpo()+1);
		break;
		
		/* TTQ */
		// boucle -> 'ttq' {} expr 'faire' 
		case 53:
			pileRep.empiler(po.getIpo()+1); // debut ttq
		break;
		
		// boucle -> 'ttq' expr {verifBool}{} 'faire' ... fait
		case 54:
			po.produire(BSIFAUX);
			po.produire(0); // fait
			pileRep.empiler(po.getIpo());
		break;
		
		// boucle -> 'ttq' expr  ... fait {}
		case 55:
			po.modifier(pileRep.depiler(), po.getIpo()+3); // modif de bsifaux, jump après bincond
			po.produire(BINCOND);
			po.produire(pileRep.depiler()); // debut du ttq
		break;
			
		/* COND */
		
		// inscond ->  'cond' {} ...
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
		// Seulement apres une virgule
		case 58:
			po.modifier(pileRep.depiler(), po.getIpo()+3); // maj bsifaux
			po.produire(BINCOND);
			po.produire(pileRep.depiler());
			pileRep.empiler(po.getIpo());
		break;
		
		//  inscond -> 'cond' expr : ins aut {} 
		case 59:
			po.modifier(pileRep.depiler(), po.getIpo()+1);
			po.produire(BINCOND);
			po.produire(pileRep.depiler());
			pileRep.empiler(po.getIpo());
			pileRep.empiler(0);
		break;
		
		// inscond -> 'fcond' {}
		case 60:
			i = pileRep.depiler();
			if(i != 0) { // bsifaux a mettre a jour
				po.modifier(i, po.getIpo()+1);
			}
			i = pileRep.depiler();
			int elti;
			while (i != 0) { //bcond a mettre a jour
				elti = po.getElt(i);
				po.modifier(i, po.getIpo()+1);
				i = elti;
			}
		break;
			
		
		/* FIN & GENERATIONS DU CODE
		 * corps -> 'debut' instructions 'fin' {}
		*/
		case 255:
			po.produire(ARRET);
			po.constObj();
			po.constGen();
			afftabSymb();
		break;

		default:
			System.out.println("Point de generation non prevu dans votre liste");
		break;
		

		}
		
		//affMnemoIpo();
	}
}
