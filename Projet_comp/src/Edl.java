import java.io.*;



 
 //TODO : Renseigner le champs auteur : MINIER_Tristan_OUGUEUR_Karim_KASIM_Sarah
 /**
 * 
 * @author MINIER, OUGUEUR, KASIM
 * @version 2020
 *
 */


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

	//TODO : declarations de variables A COMPLETER SI BESOIN
	static int ipo, nMod, nbErr;
	static String nomProg;
	
	
	static Descripteur.EltDef[] dicoDef = new Descripteur.EltDef[60];
	
	static int transDon[] = new int [6];
	static int transCode[] = new int [6];
	static int adFinale[][] = new int [5][10];
	static int nbDefs = 0;
	static String objs[] = new String[MAXMOD];
	static int numRef=0;
	
	
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
		nomProg = s;

		nMod = 0;
		objs[nMod]=s;
		while (!s.equals("") && nMod < MAXMOD) {
			System.out.print("nom de module " + (nMod + 1)
					+ " (RC si termine) ");
			s = Lecture.lireString();
			
			if (!s.equals("")) {
				
				nMod = nMod + 1;
				tabDesc[nMod] = new Descripteur();
				tabDesc[nMod].lireDesc(s);
				objs[nMod]=s;
				if (!tabDesc[nMod].getUnite().equals("module"))
					erreur(FATALE, "module attendu");
				//on ajoute la taille de l'unite precedent
				transCode[nMod] = transCode[nMod-1] + tabDesc[nMod-1].getTailleCode();
				//on ajoute le nb de globaux du precedent
				transDon[nMod] = transDon[nMod-1] + tabDesc[nMod-1].getTailleGlobaux();
				
				//on enregistre chaque procédure du module dans dicoDef
				for(int i=1;i<=tabDesc[nMod].getNbDef();i++) {
					
					
						if(indexDico(tabDesc[nMod].getDefNomProc(i))==0){
							
							//dicoDef[nbDefs+i]= new Descripteur.EltDef("test",0,0);
							dicoDef[nbDefs+i] = tabDesc[nMod].tabDef[i]; // si vous aviez une erreur ici, mettez "changez" tabDef en public 							dicoDef[nbDefs+i].adPo= tabDesc[nMod].getDefAdPo(i) + transCode[nMod];
							//dicoDef[nbDefs+i].nbParam= tabDesc[nMod].getDefNbParam(i);
						}
						else {
							
							erreur(NONFATALE,"deja présent");
						
						}
					
				}
				
				nbDefs += tabDesc[nMod].getNbDef(); //nb total de defs dans dicoDef
				
			}
		}
		
		
		
	}
	
	//retourne l'indice de la proc dans DicoDef 
	static int indexDico(String nom) {
		for(int i=1;i<=nbDefs;i++) {
			if(nom.equals(dicoDef[i].nomProc)){
				
				return i;
			}
		}
		
		return 0;
	}

	static void constMap() {
		// f2 = fichier executable .map construit
		OutputStream f2 = Ecriture.ouvrir(nomProg + ".map");
		if (f2 == null)
			erreur(FATALE, "creation du fichier " + nomProg
					+ ".map impossible");
		// pour construire le code concatene de toutes les unités
		int[] po = new int[(nMod + 1) * MAXOBJ + 1];
		
		ipo=1;
		
		for(int i=0;i<=nMod;i++) {
			InputStream mod = Lecture.ouvrir(objs[i]+".obj");
			//System.out.println(objs[i]);
			if(mod == null) {
				erreur(FATALE, "impossible d'ouvrir " + objs[i]);
			}
			
			int AdPo, X;
			
			int nbExt=1;
			int vTrans[] = new int[1000];
			
			//TransExt dans vTrans
			for(int j=0;j<tabDesc[i].getNbTransExt();j++) {
				
				
				
				AdPo= Lecture.lireInt(mod) ; 
				X = Lecture.lireIntln(mod);
				vTrans[AdPo]=X;
				//System.out.println(AdPo + " "+ X);
			}
			int dernier =tabDesc[i].getTailleCode();
			if(i==nMod) {
				dernier =tabDesc[i].getTailleCode()-1;
			}
			
			for(int j=1;j<=dernier;j++) {
				
				po[ipo]=Lecture.lireIntln(mod);//lecture du code obj 
					
				//si la ligne est dans vTrans
				switch(vTrans[j]) {
				
					case TRANSCODE:
						po[ipo]+= transCode[i];
						break;
					case TRANSDON:
						po[ipo]+= transDon[i];
						break;
					case REFEXT:
						po[ipo] = adFinale[i][nbExt];
						nbExt++;
						break;
					
				}
				
				ipo++;
		}
			Lecture.fermer(mod);
		}
	
		//nb total de variables globales
		po[2] = transDon[nMod] + tabDesc[nMod].getTailleGlobaux();
		
		for(int i=1; i<=ipo;i++) {
			
			Ecriture.ecrireStringln(f2,""+po[i]);
		}
		Ecriture.fermer(f2);

		// creation du fichier en mnemonique correspondant
		Mnemo.creerFichier(ipo, po, nomProg + ".ima");
		
	}

	public static void main(String argv[]) {
		System.out.println("EDITEUR DE LIENS / PROJET LICENCE");
		System.out.println("---------------------------------");
		System.out.println("");
		nbErr = 0;
		
		// Phase 1 de l'edition de liens
		// -----------------------------
		lireDescripteurs();		//TODO : lecture des descripteurs a completer si besoin
		
		//parcours chaque unite 
		for(int i =0; i<=nMod;i++) {
			
			//rangs des ref dans l'unite
			for(int j=1; j<=tabDesc[i].getNbRef();j++) {
				
				
					//on recupere l'indice de la def correspondant à la ref dans dicoDef
					
						numRef = indexDico(tabDesc[i].getRefNomProc(j));
						
					if(numRef!=0) {
						adFinale[i][j]= dicoDef[numRef].adPo;
					}
			}
		}
		
		if (nbErr > 0) {
			System.out.println("programme executable non produit");
			System.exit(1);
		}
		
		// Phase 2 de l'edition de liens
		// -----------------------------
		constMap();				//TODO : ... A COMPLETER ...
		System.out.println("Edition de liens terminee");
	}
}