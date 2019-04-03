import java.io.*;


public class Edl {
	
	// nombre max de modules, taille max d'un code objet d'une unite
	static final int MAXMOD = 5, MAXOBJ = 1000;
	// nombres max de references externes (REF) et de points d'entree (DEF)
	// pour une unite
	private static final int MAXREF = 10, MAXDEF = 10;
	
	// typologie des erreurs
	private static final int FATALE = 0, NONFATALE = 1;
	
	// valeurs possibles du vecteur de translation
	private static final int TRANSDON=1,TRANSCODE=2,REFEXT=3;
	
	// table de tous les descripteurs concernes par l'edl
	static Descripteur[] tabDesc = new Descripteur[MAXMOD + 1];
	
	// declarations de variables A COMPLETER SI BESOIN
	static int ipo, nMod, nbErr, limit_dico;
	// static String nomProg;
	static String nomsProgMod[] = new String[MAXMOD+1];
	
	static int transDon[] = new int[MAXMOD + 1];
	static int transCode[]  = new int[MAXMOD + 1];
	static Descripteur.EltDef[] dicoDef = new Descripteur.EltDef[(MAXMOD + 1)*MAXDEF];
	static int adFinale[][] = new int[MAXMOD+1][MAXREF];
	
	static int vTrans[] = new int[MAXOBJ];
	
	static int[] po = new int[(nMod + 1) * MAXOBJ + 1];

	// utilitaire de traitement des erreurs
	// ------------------------------------
	static void erreur(int te, String m) {
		System.out.println(m);
		if (te == FATALE) {
			System.out.println("ABANDON DE L'EDITION DE LIENS");
			System.exit(1);
		}
		nbErr = nbErr + 1;
	}

	// utilitaire de remplissage de la table des descripteurs tabDesc
	// --------------------------------------------------------------
	static void lireDescripteurs() {
		String s;
		System.out.println("les noms doivent etre fournis sans suffixe");
		System.out.print("nom du programme : ");
		s = Lecture.lireString();
		tabDesc[0] = new Descripteur();
		tabDesc[0].lireDesc(s);
		if (!tabDesc[0].getUnite().equals("programme"))
			erreur(FATALE, "programme attendu");
		nomsProgMod[0] = s;
		
		nMod = 0;
		while (!s.equals("") && nMod < MAXMOD) {
			System.out.print("nom de module " + (nMod + 1)
					+ " (RC si termine) ");
			s = Lecture.lireString();
			if (!s.equals("")) {
				nMod = nMod + 1;
				tabDesc[nMod] = new Descripteur();
				tabDesc[nMod].lireDesc(s);
				if (!tabDesc[nMod].getUnite().equals("module")) {
					erreur(FATALE, "module attendu");
				}
				nomsProgMod[nMod] = s;
			}	
		}
	}

	
	static void constMap() {
		// f2 = fichier executable .map construit
		OutputStream f2 = Ecriture.ouvrir(nomsProgMod[0] + ".map");
		if (f2 == null)
			erreur(FATALE, "creation du fichier " + nomsProgMod[0]
					+ ".map impossible");
		// pour construire le code concatene de toutes les unit�s
		
		for (int i = 1; i <= ipo; i++)
			Ecriture.ecrireStringln(f2, "" + po[i]);
		
		Ecriture.fermer(f2);
		// creation du fichier en mnemonique correspondant
		Mnemo.creerFichier(ipo, po, nomsProgMod[0] + ".ima");
	}

	public static void main(String argv[]) {
		System.out.println("EDITEUR DE LIENS / PROJET LICENCE");
		System.out.println("---------------------------------");
		System.out.println("");
		nbErr = 0;
		
		// Phase 1 de l'edition de liens
		// -----------------------------
		lireDescripteurs();		// lecture des descripteurs a completer si besoin
		
		remplirTransDonEtCode();
		affTrans(TRANSCODE);
		affTrans(TRANSDON);
		remplirDicoDef();
		affDicoDef();
		remplirAdFinale();
		affAdFinale();
		
		if (nbErr > 0) {
			System.out.println("programme executable non produit");
			System.exit(1);
		}
		
		concat();
		
		// Phase 2 de l'edition de liens
		// -----------------------------
		constMap();
		Mnemo.creerFichier(ipo,po, nomsProgMod[0] + ".ima");
		System.out.println("Edition de liens terminee");
	}
	
	public static void remplirTransDonEtCode() {
		int counter_varg = 0;
		int counter_ipo = 0;
		int i = 0;
		
		for(;i <= nMod; i++) {
			transDon[i] = counter_varg;
			counter_varg += tabDesc[i].getTailleGlobaux();
			
			transCode[i] = counter_ipo;
			counter_ipo += tabDesc[i].getTailleCode();
		}
		for(;i <= MAXMOD; i++) {
			transDon[i] = -1;
			transCode[i] = -1;
		}
	}
	
	public static void remplirDicoDef() {
		limit_dico = 1;
		Descripteur gen = new Descripteur();
		String nomProc;
		int adPo;
		int nbParam;
		
		dicoDef[0] = gen.new EltDef("-1", -1, -1);
		
		for(int i = 0; i <= nMod; i++) {
			
			for(int j = 1; j <= tabDesc[i].getNbDef(); j++) {
				nomProc = tabDesc[i].getDefNomProc(j);
				adPo = tabDesc[i].getDefAdPo(j) + transCode[i];
				nbParam = tabDesc[i].getDefNbParam(j);
				
				if(presentInDico(nomProc) != 0) {
					erreur(NONFATALE, nomProc + " doublement defini");
				}
				// dicoDef[limit_dico] = (new Descripteur()).new EltDef(nomProc, adPo, nbParam);
				dicoDef[limit_dico] = gen.new EltDef(nomProc, adPo, nbParam);
				limit_dico++;
			}
		}
		for(int i = limit_dico; i < (MAXMOD + 1)*MAXDEF; i++) {
			dicoDef[i] = gen.new EltDef("-1", -1, -1);
		}
	}
	
	public static int presentInDico(String nomProc) {
		for(int i = 1; i < limit_dico; i++) {
			if(dicoDef[i].nomProc.equals(nomProc)) {
				return i;
			}
		}
		return 0;
	}
	
	public static void remplirAdFinale() {
		String nomProcRef;
		int a;
		for(int i = 0; i <= nMod; i++) {
			for(int j = 1; j <= tabDesc[i].getNbRef(); j++) {
				nomProcRef = tabDesc[i].getRefNomProc(j);
				a = presentInDico(nomProcRef);
				if(a == 0) {
					erreur(NONFATALE, nomProcRef + " a aucune definition associe");
				}
				adFinale[i][j] = dicoDef[a].adPo;
			}
		}
	}
	
	public static void affTrans(int trans) {
		switch(trans) {
		case TRANSDON:
			System.out.println("\nTransDon table:");
			break;
		case TRANSCODE:
			System.out.println("\nTransCode table:");
			break;
		}
		
		for(int i = 0; i <= nMod; i++) {
			switch(trans) {
			case TRANSDON:
				System.out.print(transDon[i] + " ");
				break;
			case TRANSCODE:
				System.out.print(transCode[i] + " ");
				break;
			}
		}
		System.out.println();
	}
	
	public static void affDicoDef() {
		System.out.println("\nDicoDef:\ni\tnomProc\tadPo\tnbParam");
		for(int i = 1; i < limit_dico; i++) {
			System.out.println(""+i+"\t"+dicoDef[i].nomProc+"\t"+dicoDef[i].adPo+"\t"+dicoDef[i].nbParam);
		}
	}
	
	public static void affAdFinale() {
		System.out.println("\nadFinale table:");
		for(int i = 0; i <= nMod; i++) {
			for(int j = 0; j <= tabDesc[i].getNbRef(); j++) {
				System.out.print(adFinale[i][j]+"\t");
			}
			System.out.println();
		}
	}
	
	public static void initvTrans() {		
		for(int i = 0; i < MAXOBJ; i++) {
			vTrans[i] = -1;
		}
	}
	
	public static void concat() {
		ipo = 0;
		for(int i = 0; i <= nMod; i++) {
			InputStream f = Lecture.ouvrir(nomsProgMod[i] + ".obj");
			if (f == null) {
				System.out.println("Fichier " + nomsProgMod[i] + ".obj inexistant");
				System.exit(1);
			}
			
			initvTrans();
			for(int j = 0; j < tabDesc[i].getNbTransExt(); j++) {
				vTrans[Lecture.lireInt(f)] = Lecture.lireInt(f);
			}
			for(int j = 1; j <= tabDesc[i].getTailleCode(); j++) {
				ipo++;
				
				switch(vTrans[j]) {
				case TRANSDON:
					po[ipo] = Lecture.lireInt(f) + transDon[i];
					break;
				case TRANSCODE:
					po[ipo] = Lecture.lireInt(f) + transCode[i];
					break;
				case REFEXT:
					po[ipo] = adFinale[i][Lecture.lireInt(f)];
					break;
				default:
					po[ipo] = Lecture.lireInt(f); 
				}
			}
			Lecture.fermer(f);
		}
		po[2] = sumGlobaux();
	}
	
	public static int sumGlobaux() {
		int sum = 0;
		for(int i = 0; i <= nMod; i++) {
			sum += tabDesc[i].getTailleGlobaux();
		}
		return sum;
	}
}
