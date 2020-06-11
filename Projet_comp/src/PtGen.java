
/*********************************************************************************
 * VARIABLES ET METHODES FOURNIES PAR LA CLASSE UtilLex (cf libClass_Projet)     *
 *       complement à l'ANALYSEUR LEXICAL produit par ANTLR                      *
 *                                                                               *
 *                                                                               *
 *   nom du programme compile, sans suffixe : String UtilLex.nomSource           *
 *   ------------------------                                                    *
 *                                                                               *
 *   attributs lexicaux (selon items figurant dans la grammaire):                *
 *   ------------------                                                          *
 *     int UtilLex.valEnt = valeur du dernier nombre entier lu (item nbentier)   *
 *     int UtilLex.numIdCourant = code du dernier identificateur lu (item ident) *
 *                                                                               *
 *                                                                               *
 *   methodes utiles :                                                           *
 *   ---------------                                                             *
 *     void UtilLex.messErr(String m)  affichage de m et arret compilation       *
 *     String UtilLex.chaineIdent(int numId) delivre l'ident de codage numId     *
 *     void afftabSymb()  affiche la table des symboles                          *
 *********************************************************************************/

import java.io.*;

/**
 * classe de mise en oeuvre du compilateur
 * ======================================= (verifications semantiques +
 * production du code objet)
 * 
 * @author Girard, Grazon, Masson
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

			// categories possibles des identificateurs :
			CONSTANTE = 1, VARGLOBALE = 2, VARLOCALE = 3, PARAMFIXE = 4, PARAMMOD = 5, PROC = 6, DEF = 7, REF = 8,
			PRIVEE = 9,

			// valeurs possible du vecteur de translation
			TRANSDON = 1, TRANSCODE = 2, REFEXT = 3;

	// utilitaires de controle de type
	// -------------------------------
	/**
	 * verification du type entier de l'expression en cours de compilation (arret de
	 * la compilation sinon)
	 */
	private static void verifEnt() {
		if (tCour != ENT)
			UtilLex.messErr("expression entiere attendue");
	}

	/**
	 * verification du type booleen de l'expression en cours de compilation (arret
	 * de la compilation sinon)
	 */
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
	/**
	 * modification du vecteur de translation associe au code produit +
	 * incrementation attribut nbTransExt du descripteur NB: effectue uniquement si
	 * c'est une reference externe ou si on compile un module
	 * 
	 * @param valeur : TRANSDON, TRANSCODE ou REFEXT
	 */
	private static void modifVecteurTrans(int valeur) {
		if (valeur == REFEXT || desc.getUnite().equals("module")) {
			po.vecteurTrans(valeur);
			desc.incrNbTansExt();
		}
	}

	// descripteur associe a un programme objet (compilation separee)
	private static Descripteur desc;

	// autres variables fournies
	// -------------------------

	// MERCI de renseigner ici un nom pour le trinome, constitue EXCLUSIVEMENT DE
	// LETTRES
	public static String trinome = "KASIM_OUGUEUR_MINIER";

	private static int tCour; // type de l'expression compilee
	private static int vCour; // sert uniquement lors de la compilation d'une valeur (entiere ou boolenne)

	
	private static int nbDef, nbRef;
	// TABLE DES SYMBOLES
	private static int code, nbVar, nbAff, nbParams, categ, paramFouM; // nos variables
	// ------------------
	//
	private static EltTabSymb[] tabSymb = new EltTabSymb[MAXSYMB + 1];

	// it = indice de remplissage de tabSymb
	// bc = bloc courant (=1 si le bloc courant est le programme principal)
	private static int it, bc;
	

	/**
	 * utilitaire de recherche de l'ident courant (ayant pour code
	 * UtilLex.numIdCourant) dans tabSymb
	 * 
	 * @param borneInf : recherche de l'indice it vers borneInf (=1 si recherche
	 *                 dans tout tabSymb)
	 * @return : indice de l'ident courant (de code UtilLex.numIdCourant) dans
	 *         tabSymb (O si absence)
	 */
	private static int presentIdent(int borneInf) {
		int i = it;
		while (i >= borneInf && tabSymb[i].code != UtilLex.numIdCourant)
			i--;
		if (i >= borneInf)
			return i;
		else
			return 0;
	}

	/**
	 * utilitaire de placement des caracteristiques d'un nouvel ident dans tabSymb
	 * 
	 * @param code : UtilLex.numIdCourant de l'ident
	 * @param cat  : categorie de l'ident parmi CONSTANTE, VARGLOBALE, PROC, etc.
	 * @param type : ENT, BOOL ou NEUTRE
	 * @param info : valeur pour une constante, ad d'exécution pour une variable,
	 *             etc.
	 */
	private static void placeIdent(int code, int cat, int type, int info) {
		if (it == MAXSYMB)
			UtilLex.messErr("debordement de la table des symboles");
		it = it + 1;
		tabSymb[it] = new EltTabSymb(code, cat, type, info);
	}

	/**
	 * utilitaire d'affichage de la table des symboles
	 */
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
				System.out.println(" reference NULL");
			else
				System.out.println(" " + tabSymb[i]);
		}
		System.out.println();
	}

	/**
	 * initialisations A COMPLETER SI BESOIN -------------------------------------
	 */
	public static void initialisations() {

		// indices de gestion de la table des symboles
		it = 0;
		bc = 1;

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

	/**
	 * code des points de generation A COMPLETER
	 * -----------------------------------------
	 * 
	 * @param numGen : numero du point de generation a executer
	 */
	public static void pt(int numGen) {

		switch (numGen) {
		case 0:
			initialisations();
			break;
		case 1:
			int indice = presentIdent(1); // System.out.println(" present ident case 1"+indice);
			if (indice == 0) {
				UtilLex.messErr("identificateur non d�fini");
			}
			tCour = tabSymb[indice].type;
			switch (tabSymb[indice].categorie) {

			case CONSTANTE:
				po.produire(EMPILER);
				po.produire(tabSymb[indice].info);
				// System.out.println("case 1 constante");
				break;
			case VARGLOBALE:
				po.produire(CONTENUG);
				po.produire(tabSymb[indice].info);
				
				// compilation separee, construction de vecteurTrans 
				modifVecteurTrans(TRANSDON);
				
				// System.out.println("case 1 varglobale");

				break;
				
			case VARLOCALE:
				po.produire(CONTENUL);
				po.produire(tabSymb[indice].info); 
				po.produire(0);
				break;

			case PARAMFIXE:
				po.produire(CONTENUL);
				po.produire(tabSymb[indice].info); 
				po.produire(0);
				break;

			case PARAMMOD:
				po.produire(CONTENUL);
				po.produire(tabSymb[indice].info); 
				po.produire(1);
				break;
				
			default:
				System.out.println("case 1");
				break;
			}

			break;
		
		case 2:
			verifEnt();
			break;
		case 3:
			tCour = ENT;
			vCour = -UtilLex.valEnt;
			break;
		case 4:
			po.produire(EMPILER);
			po.produire(vCour);
			break;
		case 5:
			tCour = BOOL;
			po.produire(EG);
			break;
		case 6:
			verifBool();
			break;
		case 70: 
			po.produire(NON);
			tCour = BOOL;
			break;
		case 7:
			tCour = ENT;
			vCour = UtilLex.valEnt;
			break;
		case 8:
			po.produire(ADD);
			tCour = ENT;
			break;
		case 9:
			po.produire(ET);
			tCour = BOOL;
			break;
		case 99:
			po.produire(OU);
			tCour = BOOL;
			break;
		case 10:
			tCour = BOOL;
			po.produire(DIFF);
			break;
		case 11:
			tCour = BOOL;
			po.produire(SUP);
			break;
		case 12:
			tCour = BOOL;
			po.produire(SUPEG);
			break;
		case 13:
			tCour = BOOL;
			po.produire(INF);
			break;
		case 14:
			tCour = BOOL;
			po.produire(INFEG);
			break;
		case 15:
			po.produire(SOUS);
			tCour = ENT;
			break;
		case 16:
			po.produire(MUL);
			tCour = ENT;
			break;
		case 17:
			po.produire(DIV);
			tCour = ENT;
			break;

		case 18: // lire
			int indice2 = presentIdent(1);
			if (indice2 == 0) {
				UtilLex.messErr("identificateur non d�fini");
			}
			tCour = tabSymb[indice2].type;
			switch (tabSymb[indice2].categorie) {

			case CONSTANTE:
				UtilLex.messErr("on ne peut pas lire une cst");
				break;
				
			case PARAMFIXE:
				UtilLex.messErr("on ne peut pas lire les parametres fixes");
				break;
				
			case VARGLOBALE:
				if (tCour == BOOL) {
					po.produire(LIREBOOL);
					po.produire(AFFECTERG);
					po.produire(tabSymb[indice2].info);
					modifVecteurTrans(TRANSDON);
				}
				
				if (tCour == ENT) {
					po.produire(LIRENT);
					po.produire(AFFECTERG);
					po.produire(tabSymb[indice2].info);
					modifVecteurTrans(TRANSDON);
				}
				break;

			case VARLOCALE: 
				if (tCour == BOOL) {
					po.produire(LIREBOOL);
					po.produire(AFFECTERL);
					po.produire(tabSymb[indice2].info);
				}
				if (tCour == ENT) {
					po.produire(LIRENT);
					po.produire(AFFECTERL);
					po.produire(tabSymb[indice2].info);
				}
				po.produire(0);
			break;

			case PARAMMOD: 
				if (tCour == BOOL) {
					po.produire(LIREBOOL);
					po.produire(AFFECTERL);
					po.produire(tabSymb[indice2].info);
				}
				if (tCour == ENT) {
					po.produire(LIRENT);
					po.produire(AFFECTERL);
					po.produire(tabSymb[indice2].info);
				}
				po.produire(1);
				break;
			}

			break;

		case 20:
			int index2 = presentIdent(1);
			if (index2 == 0) {
				UtilLex.messErr("identificateur non d�fini");
			}
			switch (tCour) {

				case ENT:
					po.produire(ECRENT);
					break;
				case BOOL:
					po.produire(ECRBOOL); 
					break;
			}

			break;

		case 22:
			code = UtilLex.numIdCourant;
			break;

		case 23: // declarations des constantes 
			if (presentIdent(1) == 0)
				placeIdent(code, CONSTANTE, tCour, vCour);
			else
				UtilLex.messErr("constant deja declaree");
			break;

		case 24: // declarations des var
			if (presentIdent(bc) == 0) {

				// en cas des proc
				if (bc > 1) // placement dans tabsymb
					placeIdent(UtilLex.numIdCourant, VARLOCALE, tCour, nbVar); 
				else
					placeIdent(UtilLex.numIdCourant, VARGLOBALE, tCour, nbVar);

				nbVar++;

    		}
			else UtilLex.messErr("Variable deja declaree.");
			
			break;
			
		case 31:
			
			// Si programme
			if (desc.getUnite().equals("programme")) {
				po.produire(RESERVER);

				// en cas des proc
				if  (bc > 1)
					po.produire(nbVar - (nbParams + 2));
				else
					po.produire(nbVar);
			}

			// Maj descripteur
			desc.setTailleGlobaux(nbVar);
			
			nbVar =0; // reinit nbVar
			break;

		case 25:
			tCour = ENT;
			break;

		case 26:
			tCour = BOOL;
			break;

		case 27:
			tCour = BOOL;
			vCour = VRAI;
			break;

		case 28:
			tCour = BOOL;
			vCour = FAUX;
			break;

		case 29: // Affectation des var
			int indexVar = presentIdent(1); // Index des var
			nbAff = indexVar;
			if (indexVar == 0)
				UtilLex.messErr("identifiant inconnu");
			if ((tabSymb[indexVar].categorie == CONSTANTE) || (tabSymb[indexVar].categorie == PARAMFIXE))
				UtilLex.messErr("L'identifiant n'est pas une variable");

			tCour = tabSymb[indexVar].type;
			categ = tabSymb[indexVar].categorie;
			break;

		case 30: // Affectation 

			switch (categ) {

				case VARGLOBALE:
					
					po.produire(AFFECTERG); 
					po.produire(tabSymb[nbAff].info);

					// compilation separee, construction de vecteurTrans 
					modifVecteurTrans(TRANSDON);
					break;

				case VARLOCALE:
					
					po.produire(AFFECTERL);
					po.produire(tabSymb[nbAff].info);
					po.produire(0);
					break;

				case PARAMMOD:
					po.produire(AFFECTERL);
					po.produire(tabSymb[nbAff].info);
					po.produire(1);
					break;

				default:
					UtilLex.messErr("Erreur d'affectation ");
					break;
			}
			break;

		case 32: // verifier que la derniere expression est une booleenne
			verifBool();
			po.produire(BSIFAUX); 
			po.produire(0); 

			// compilation separee, construction de vecteurTrans 
			modifVecteurTrans(TRANSCODE);
			pileRep.empiler(po.getIpo()); // empiler ipo dans la pile de reprise 
			break;

		// ttq
		case 33: // on retient le début du ttq
			pileRep.empiler(po.getIpo() + 1);
			break;

		case 34:
			po.produire(BINCOND);// fin
			po.modifier(pileRep.depiler(), po.getIpo()+2);
			po.produire(pileRep.depiler());
			
			// compilation separee, construction de vecteurTrans 
			modifVecteurTrans(TRANSCODE);
			
			break;

		// inscond

		case 35:
			pileRep.empiler(0);
			
			break;

		case 36:
			int depBsi = pileRep.depiler();
			int depBin = pileRep.depiler();

			po.produire(BINCOND);
			po.produire(depBin);
			
			// compilation separee, construction de vecteurTrans 
			modifVecteurTrans(TRANSCODE);

			po.modifier(depBsi, po.getIpo() + 1);
			pileRep.empiler(po.getIpo());
			break;
			
		/*case 37: 
			int aut = pileRep.depiler();
			po.produire(BINCOND);
			po.produire(aut);
			pileRep.empiler(po.getIpo());
			break;
*/
		case 37:

			int bsi = pileRep.depiler();			
			po.modifier(bsi, po.getIpo() + 1);
			
			break;		
		case 38:

			int var = pileRep.depiler();
			int tmp =0;
			while (po.getElt(var) != 0) {

				tmp = po.getElt(var);
				po.modifier(var, po.getIpo() + 1);
				var = tmp;
			}
			po.modifier(var, po.getIpo() + 1);

			break;

		// si
		case 39:
			int br = pileRep.depiler();
			po.produire(BINCOND);
			po.produire(0);
			
			// compilation separee, construction de vecteurTrans 
			modifVecteurTrans(TRANSCODE);
			
			pileRep.empiler(po.getIpo());
			po.modifier(br, po.getIpo() + 1);
			break;
					
		case 40:
			int lastBr = pileRep.depiler();
			po.modifier(lastBr, po.getIpo() + 1);

			break;
			
		case 41: // pour les proc, production bincond (Si programme)
			if (desc.getUnite().equals("programme")) {
				po.produire(BINCOND); 
				po.produire(0);
			
				// compilation separee, construction de vecteurTrans 
				modifVecteurTrans(TRANSCODE);
				pileRep.empiler(po.getIpo());
			}
			
			break;
						
		case 42: // maj bincond initial
			if (desc.getUnite().equals("programme")) {
				po.modifier(pileRep.depiler(), po.getIpo()+1);
			}
			break;
			
		case 44:
			// mettre la proc dans tabsybm
            if(presentIdent(1)==0) {
            	placeIdent(UtilLex.numIdCourant,PROC, NEUTRE, po.getIpo()+1);
            	placeIdent(-1, PRIVEE, NEUTRE, 0); // info de privee a changer 
            	bc= it+1; // maj bc
                nbParams=0; // init nbParam
                

            } else { 
                UtilLex.messErr("Procedures déja declarée");
            }
            
        break ;
			
		case 43: 

			tabSymb[bc-1].info = nbParams; //maj info de privee 
			nbVar = nbParams + 2;  // Donnees de liaison
			break;
        
		case 45: // mettre les param fixes dans tabsybm
            placeIdent(UtilLex.numIdCourant,PARAMFIXE, tCour, nbParams);
            nbParams++;
    		
            break;
		
		case 46: // mettre les param mod dans tabsybm
            placeIdent(UtilLex.numIdCourant,PARAMMOD, tCour, nbParams);
            nbParams++;
            
            break;
			
		case 47: // fin des procedures
				po.produire(RETOUR);
			   	po.produire(nbParams);
			   	
			   	it = bc + nbParams-1 ; // maj de it pour supprimer les variables locales
			   	
			   	for(int i=bc; i<=it; i++) { //masquage nom param
                    tabSymb[i].code=-1;
                }

				bc = 1; // maj bc a la fin des procedures 

			   	break;
            
		case 49: // param mod
			int index49 = presentIdent(1);

            switch (tabSymb[index49].categorie) {

                case VARGLOBALE:
                        po.produire(EMPILERADG);
                        po.produire(tabSymb[index49].info);
                        
                     // compilation separee, construction de vecteurTrans 
    					modifVecteurTrans(TRANSDON);

                    break;

                case VARLOCALE:
                        po.produire(EMPILERADL);
                        po.produire(tabSymb[index49].info);
                        po.produire(0);
                    break;

                case PARAMMOD:
                        po.produire(EMPILERADL);
                        po.produire(tabSymb[index49].info);
                        po.produire(1);
                    break;

                default:
                    UtilLex.messErr("Erreur dans le passage des parametres.");
                    break;
            }
            break;
			
		case 50: // procedures calls

			po.produire(APPEL);
			po.produire(tabSymb[nbAff].info); // adproc (nbAff est aussi l'index des var)
			
			// compilation separee, construction de vecteurTrans 
			// Si refext
			if (tabSymb[nbAff+1].categorie == REF)
				modifVecteurTrans(REFEXT);
			
			// Si proc locale
			else
				modifVecteurTrans(TRANSCODE);
			
			po.produire(tabSymb[nbAff+1].info); // nbparam
			break;
								
		case 51:
			desc.setUnite("programme");
			break;
		
		case 52:
			desc.setUnite("module");
			break;
			
		case 53:
			if(presentIdent(1)==0){ 
				desc.ajoutDef(UtilLex.chaineIdent(UtilLex.numIdCourant));
				nbDef++;
			} else {
				UtilLex.messErr(" def deja declaree ");
			}			

			break;
			
		case 55:
            if(presentIdent(1)==0){ 
            	
    			desc.ajoutRef(UtilLex.chaineIdent(UtilLex.numIdCourant));
    			nbRef++; // normalement c'est fait dans ajoutRef :/ 
  				desc.modifRefNbParam(nbRef, nbParams);    			
                placeIdent(UtilLex.numIdCourant, PROC, NEUTRE, nbRef);
  				placeIdent(-1, REF, NEUTRE, nbParams);
  				
  			// ajout param ref dans tabSymb
  				if (paramFouM == 1) { 
  					placeIdent(-1,PARAMFIXE, tCour, -1);
  					paramFouM = 0;
  				}
  				else if (paramFouM == 2) {
  					placeIdent(-1,PARAMMOD, tCour, -1);
  					paramFouM = 0;
  				}

  				
            } else {
            	
                UtilLex.messErr(" Ref déja déclaree ");
            }
            nbParams=0; // reinit nbParams pour chaque ref
 
            break;
			
		case 56:

			paramFouM = 1; // pour determiner si param fixe ou mod
			nbParams++;
            break;

        case 57:
        	
        	paramFouM=2; // pour determiner si param fixe ou mod
            nbParams++;
            break;
            
        case 58: // maj tabDef et tabSymb
        	
        	for (int i = 1; i <= nbDef; i++ ) {
        		for (int j = 1; j <it; j ++) {
        			if ((tabSymb[j].categorie == PROC) && 
        				desc.getDefNomProc(i).equals(UtilLex.chaineIdent(tabSymb[j].code))){
        				// Maj adpo et nbParams dans tabDef
        				desc.modifDefAdPo(i, tabSymb[j].info);
        				desc.modifDefNbParam(i, tabSymb[j+1].info);

		    			// Maj type dans tabSymb
		    			tabSymb[j+1].categorie = DEF;
        				
        			}
        		}
        	}
        	
			break;
	
         case 54: //Reinit entre chaque unite

        	 nbVar=0;
        	 nbRef = 0;
        	 nbDef = 0;
 	    	 desc = new Descripteur();
	    	 
 	    	 break;
		   
		case 90:
			// Si programme 
			if (desc.getUnite().equals("programme")) po.produire(ARRET);

			// Maj descripteur
			desc.setTailleCode(po.getIpo());

			// Creation .obj, .gen et .desc et affichage tabSymb
			afftabSymb();
			po.constObj();
			po.constGen();
			desc.ecrireDesc(UtilLex.nomSource);
			break;

		default:
			System.out.println("Point de generation non prevu dans votre liste  "+ numGen);
			break;

		}
	}
}