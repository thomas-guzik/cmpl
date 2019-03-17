// Grammaire du langage PROJET
// COMP L3  
// Hart Maximilian - Morel-Adam - Guzik-Thomas
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


unite  :   unitprog  EOF
      |    unitmodule  EOF
  ;
  
unitprog
  : 'programme' ident ':'  
     declarations  
     corps { System.out.println("succes, arret de la compilation "); }
  ;
  
unitmodule
  : 'module' ident ':' 
     declarations   
  ;
  
declarations
  : partiedef? partieref? consts? vars? decprocs? 
  ;
  
partiedef
  : 'def' ident  (',' ident )* ptvg
  ;
  
partieref: 'ref'  specif (',' specif)* ptvg
  ;
  
specif  : ident  ( 'fixe' '(' type  ( ',' type  )* ')' )? 
                 ( 'mod'  '(' type  ( ',' type  )* ')' )? 
  ;
  
consts  : 'const' ( ident '=' valeur  ptvg {PtGen.pt(40);}  )+ 
  ;
  
vars  : 'var' ( type ident {PtGen.pt(41);} ( ','  ident {PtGen.pt(41);} )* ptvg  )+ {PtGen.pt(42);}
  ;
  
type  : 'ent'  {PtGen.pt(43);}
  |     'bool' {PtGen.pt(44);}
  ;
  
decprocs: (decproc ptvg)+
  ;
  
decproc :  'proc'  ident  parfixe? parmod? consts? vars? corps 
  ;
  
ptvg  : ';'
  | 
  ;
  
corps : 'debut' instructions 'fin' {PtGen.pt(255);}
  ;
  
parfixe: 'fixe' '(' pf ( ';' pf)* ')'
  ;
  
pf  : type ident  ( ',' ident  )*  
  ;

parmod  : 'mod' '(' pm ( ';' pm)* ')'
  ;
  
pm  : type ident  ( ',' ident  )*
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
  
inssi : 'si' expression {PtGen.pt(8);PtGen.pt(50);} 'alors' instructions ('sinon' {PtGen.pt(51);}  instructions)? 'fsi' {PtGen.pt(52);} 
  ;
  
inscond : 'cond' {PtGen.pt(56);}  expression {PtGen.pt(8);PtGen.pt(57);}  ':' instructions
          (',' {PtGen.pt(58);}  expression {PtGen.pt(8);PtGen.pt(57);} ':' instructions )* 
          ('aut' {PtGen.pt(59);}   instructions |  ) 
          'fcond'  {PtGen.pt(60);} 
  ;
  
boucle  : 'ttq' {PtGen.pt(53);}  expression {PtGen.pt(8);PtGen.pt(54);} 'faire' instructions 'fait' {PtGen.pt(55);}
  ;
  
lecture: 'lire' '(' ident {PtGen.pt(32);}  ( ',' ident  {PtGen.pt(32);} )* ')' 
  ;
  
ecriture: 'ecrire' '(' expression {PtGen.pt(33);} ( ',' expression {PtGen.pt(33);} )* ')'
   ;
  
affouappel
  : ident  (    ':=' {PtGen.pt(30);}  expression {PtGen.pt(31);}
            |   (effixes (effmods)?)?  
           )
  ;
  
effixes : '(' (expression  (',' expression  )*)? ')'
  ;
  
effmods :'(' (ident  (',' ident  )*)? ')'
  ; 
  
expression: (exp1) ( 'ou' {PtGen.pt(8);} exp1 {PtGen.pt(8);PtGen.pt(10);} )*
  ;
  
exp1  : exp2 ('et' {PtGen.pt(8);} exp2 {PtGen.pt(8);PtGen.pt(11);} )* 
  ;
  
exp2  : 'non' exp2 {PtGen.pt(8);PtGen.pt(12);} 
  | exp3  
  ;
  
exp3  : exp4 
  ( '='   {PtGen.pt(7);} exp4   {PtGen.pt(7); PtGen.pt(17);}
  | '<>'  {PtGen.pt(7);} exp4  {PtGen.pt(7); PtGen.pt(18);}
  | '>'   {PtGen.pt(7);} exp4  {PtGen.pt(7); PtGen.pt(15);}
  | '>='  {PtGen.pt(7);} exp4  {PtGen.pt(7); PtGen.pt(16);}
  | '<'   {PtGen.pt(7);} exp4  {PtGen.pt(7); PtGen.pt(13);}
  | '<='  {PtGen.pt(7);} exp4  {PtGen.pt(7); PtGen.pt(14);}
  ) ?
  ;
  
exp4  : exp5 
        ('+' {PtGen.pt(7);} exp5  {PtGen.pt(7);PtGen.pt(19);}
        |'-' {PtGen.pt(7);} exp5 {PtGen.pt(7);PtGen.pt(20);}
        )*
  ;
  
exp5  : primaire 
        (    '*' {PtGen.pt(7);}  primaire {PtGen.pt(7);PtGen.pt(21);}
          | 'div'{PtGen.pt(7);}  primaire {PtGen.pt(7);PtGen.pt(22);}
        )*
  ;
  
primaire: valeur {PtGen.pt(5);}
  | ident  {PtGen.pt(6);}
  | '(' expression ')'
  ;
  
valeur  : nbentier {PtGen.pt(1);}
  | '+' nbentier {PtGen.pt(1);}
  | '-' nbentier {PtGen.pt(2);}
  | 'vrai' {PtGen.pt(3);}
  | 'faux' {PtGen.pt(4);}
  ;

// partie lexicale  : cette partie ne doit pas etre modifie  //
// les unites lexicales de ANTLR doivent commencer par une majuscule
// attention : ANTLR n'autorise pas certains traitements sur les unites lexicales, 
// il est alors ncessaire de passer par un non-terminal intermediaire 
// exemple : pour l'unit lexicale INT, le non-terminal nbentier a du etre introduit
 
      
nbentier  :   INT { UtilLex.valNb = Integer.parseInt($INT.text);}; // mise a jour de valNb

ident : ID  { UtilLex.traiterId($ID.text); } ; // mise a jour de numId
     // tous les identificateurs seront places dans la table des identificateurs, y compris le nom du programme ou module
     // la table des symboles n'est pas geree au niveau lexical
        
  
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



	   
