// Grammaire du langage PROJET
// CMPL L3info 
// Nathalie Girard, Anne Grazon, Veronique Masson
// il convient d'y inserer les appels a {PtGen.pt(k);}
// relancer Antlr apres chaque modification et raffraichir le projet Eclipse le cas echeant

// attention l'analyse est poursuivie apres erreur si l'on supprime la clause rulecatch

grammar projet;

options {
  language=Java; k=1;
 }

@header {           
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileInputStream;
} 


// partie syntaxique :  description de la grammaire //
// les non-terminaux doivent commencer par une minuscule


@members {

 
// variables globales et methodes utiles a placer ici
  
}
// la directive rulecatch permet d'interrompre l'analyse a la premiere erreur de syntaxe
@rulecatch {
catch (RecognitionException e) {reportError (e) ; throw e ; }}


unite  :   unitprog  EOF {PtGen.pt(90); }{ System.out.println("succes, arret de la compilation "); }
      |    unitmodule  EOF {PtGen.pt(90); }{ System.out.println("succes, arret de la compilation "); }
  ;
  
unitprog
  : {PtGen.pt(54);}'programme' {PtGen.pt(51);} ident  ':'   
     declarations  
     corps 
  ;
  
unitmodule
  : {PtGen.pt(54);}'module' ident ':'  {PtGen.pt(52);}  
     declarations {PtGen.pt(58);}
  ;
  
declarations
  : partiedef?  partieref?  consts? vars? decprocs? 
  ;
  
partiedef
  : 'def' ident {PtGen.pt(53);} (',' ident {PtGen.pt(53);} )* ptvg 
   ;
  
partieref: 'ref' specif {PtGen.pt(55);} (','  specif {PtGen.pt(55);})* ptvg
  ;
  
specif  : ident ( 'fixe' '(' type {PtGen.pt(56);}  ( ',' type  {PtGen.pt(56);} )* ')' )? 
                 ( 'mod'  '(' type {PtGen.pt(57);} ( ',' type  {PtGen.pt(57);} )* ')' )?
  ;
  
consts  : 'const' ( ident { PtGen.pt(22); } '=' valeur  { PtGen.pt(23); } ptvg  )+
  ;
  
vars  : 'var' ( type ident { PtGen.pt(22);} { PtGen.pt(24); }( ',' ident { PtGen.pt(22); }{ PtGen.pt(24); } )* ptvg  )+ { PtGen.pt(31); }
  ;
  
type  : 'ent' {PtGen.pt(25);} 
  |     'bool' {PtGen.pt(26);}
  ;
  
decprocs: {PtGen.pt(41);}(decproc ptvg)+ {PtGen.pt(42);}
  ;
  
decproc : 'proc' ident { PtGen.pt(44); } parfixe? parmod? {PtGen.pt(43);} consts? vars? corps { PtGen.pt(47); } 
  ;
  
ptvg  : ';'
  | 
  ;
  
corps : 'debut' instructions  'fin'  
  ;
  
parfixe: 'fixe' '(' pf ( ';' pf)* ')'
  ;
  
pf  : type ident {PtGen.pt(45);} ( ',' ident {PtGen.pt(45);} )*
  ;

parmod  : 'mod' '(' pm ( ';' pm)* ')'
  ;
  
pm  : type ident  {PtGen.pt(46);} ( ',' ident {PtGen.pt(46);} )*
  ;
  
instructions
  : instruction ( ';' instruction)*
  ;
  
instruction
  : inssi
  | inscond
  | boucle
  | lecture
  | ecriture
  | affouappel
  | 
  ;
  
inssi : 'si' expression { PtGen.pt(32); }
                'alors' instructions
                ('sinon' { PtGen.pt(39); } instructions )?
                { PtGen.pt(40); } 'fsi'
  ;
  
inscond : 'cond' { PtGen.pt(35);} expression { PtGen.pt(32); } ':' instructions 
          (',' { PtGen.pt(36); } expression { PtGen.pt(32); } ':' instructions )* 
          (  'aut' { PtGen.pt(36); } instructions | { PtGen.pt(37); } ) 
          'fcond' { PtGen.pt(38); }
  ;
  
boucle  : 'ttq' { PtGen.pt(33); }  expression { PtGen.pt(32); } 'faire' instructions 'fait'{ PtGen.pt(34);}
  ;
  
lecture: 'lire'  '(' ident { PtGen.pt(18); }  ( ',' ident { PtGen.pt(18); }  )* ')' 
  ; 
  
ecriture: 'ecrire'  '('  expression { PtGen.pt(20); } ( ',' expression  { PtGen.pt(20); }  )*      ')'
   ;
  
affouappel
  : ident { PtGen.pt(29); } ( ':=' expression { PtGen.pt(30); }
            |   (effixes (effmods)?)? { PtGen.pt(50); }
          )
  ;
  
effixes : '(' (expression  (',' expression  )*)? ')'
  ;
  
effmods :'(' (ident { PtGen.pt(49);} (',' ident { PtGen.pt(49);} )*)? ')'
  ; 
  
expression: (exp1)  ('ou' { PtGen.pt(6); } exp1 { PtGen.pt(6); }  { PtGen.pt(99); } )*
  ;
  
exp1  : exp2 ('et' { PtGen.pt(6); } exp2 { PtGen.pt(6);  }   { PtGen.pt(9); }  )*
  ;
  
exp2  : 'non' exp2 { PtGen.pt(6); }{ PtGen.pt(70); }
  | exp3  
  ;
  
exp3  : exp4 
  ( '='  { PtGen.pt(2); } exp4 { PtGen.pt(2); }  { PtGen.pt(5); } 
  | '<>' { PtGen.pt(2); } exp4 { PtGen.pt(2); }  { PtGen.pt(10); } 
  | '>'  { PtGen.pt(2); } exp4 { PtGen.pt(2); }  { PtGen.pt(11); }
  | '>=' { PtGen.pt(2); } exp4 { PtGen.pt(2); }  { PtGen.pt(12); } 
  | '<'  { PtGen.pt(2); } exp4 { PtGen.pt(2); }  { PtGen.pt(13); } 
  | '<=' { PtGen.pt(2); } exp4 { PtGen.pt(2); }  { PtGen.pt(14); } 
  ) ?
  ;
  
exp4  : exp5 
        ('+' { PtGen.pt(2); } exp5 { PtGen.pt(2); } { PtGen.pt(8); }
        |'-' { PtGen.pt(2); } exp5 { PtGen.pt(2); } { PtGen.pt(15); }
        )*
  ;
  
exp5  : primaire  
        (    '*'  { PtGen.pt(2); } primaire { PtGen.pt(2); } { PtGen.pt(16); } 
          | 'div' { PtGen.pt(2); } primaire { PtGen.pt(2); } { PtGen.pt(17); } 
        )*
  ;
  
primaire: valeur { PtGen.pt(4); } 
  | ident  { PtGen.pt(1); }
  | '(' expression ')'
  ;
  
valeur  : nbentier { PtGen.pt(7); } 
  | '+' nbentier  { PtGen.pt(7); }
  | '-' nbentier { PtGen.pt(3); } // a verifier
  | 'vrai' {PtGen.pt(27);}
  | 'faux'  {PtGen.pt(28);} ;

// partie lexicale  : cette partie ne doit pas etre modifiee  //
// les unites lexicales de ANTLR doivent commencer par une majuscule
// Attention : ANTLR n'autorise pas certains traitements sur les unites lexicales, 
// il est alors ncessaire de passer par un non-terminal intermediaire 
// exemple : pour l'unit lexicale INT, le non-terminal nbentier a du etre introduit
 
      
nbentier  :   INT { UtilLex.valEnt = Integer.parseInt($INT.text);}; // mise a jour de valEnt

ident : ID  { UtilLex.traiterId($ID.text); } ; // mise a jour de numIdCourant
     // tous les identificateurs seront places dans la table des identificateurs, y compris le nom du programme ou module
     // (NB: la table des symboles n'est pas geree au niveau lexical mais au niveau du compilateur)
        
  
ID  :   ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ; 
     
// zone purement lexicale //

INT :   '0'..'9'+ ;
WS  :   (' '|'\t' |'\r')+ {skip();} ; // definition des "blocs d'espaces"
RC  :   ('\n') {UtilLex.incrementeLigne(); skip() ;} ; // definition d'un unique "passage a la ligne" et comptage des numeros de lignes

COMMENT
  :  '\{' (.)* '\}' {skip();}   // toute suite de caracteres entouree d'accolades est un commentaire
  |  '#' ~( '\r' | '\n' )* {skip();}  // tout ce qui suit un caractere diese sur une ligne est un commentaire
  ;

// commentaires sur plusieurs lignes
ML_COMMENT    :   '/*' (options {greedy=false;} : .)* '*/' {$channel=HIDDEN;}
    ;	   



	   