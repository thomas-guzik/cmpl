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
  : 'programme' {PtGen.pt(80);} ident ':'  
     declarations  
     corps {PtGen.pt(254);PtGen.pt(255);}{ System.out.println("succes, arret de la compilation "); }
  ;
  
unitmodule
  : 'module' {PtGen.pt(81);} ident ':' 
     declarations {PtGen.pt(255);}   
  ;
  
declarations
  : partiedef? {PtGen.pt(88);} partieref? consts? vars? decprocs? 
  ;
  
partiedef
  : 'def' ident {PtGen.pt(83);} (',' ident {PtGen.pt(83);} )* ptvg
  ;
  
partieref: 'ref'  specif {PtGen.pt(87);} (',' specif {PtGen.pt(87);} )* ptvg
  ;
  
specif  : ident {PtGen.pt(84);} ( 'fixe' '(' type {PtGen.pt(85);} ( ',' type {PtGen.pt(85);} )* ')' )? 
                 ( 'mod'  '(' type {PtGen.pt(86);} ( ',' type {PtGen.pt(86);} )* ')' )? 
  ;
  
consts  : 'const' ( ident '=' valeur  ptvg {PtGen.pt(30);}  )+ 
  ;
  
vars  : 'var' ( type ident {PtGen.pt(31);} ( ','  ident {PtGen.pt(31);} )* ptvg  )+ {PtGen.pt(32);}
  ;
  
type  : 'ent'  {PtGen.pt(33);}
  |     'bool' {PtGen.pt(34);}
  ;
  
decprocs: {PtGen.pt(61);}(decproc ptvg)+ {PtGen.pt(62);}
  ;
  
decproc :  'proc'  ident {PtGen.pt(63);} parfixe? parmod? {PtGen.pt(66);} consts? vars? corps {PtGen.pt(67);}
  ;
  
ptvg  : ';'
  | 
  ;
  
corps : 'debut' instructions 'fin'
  ;
  
parfixe: 'fixe' '(' pf ( ';' pf )* ')'
  ;
  
pf  : type ident {PtGen.pt(64);} ( ',' ident {PtGen.pt(64);} )*  
  ;

parmod  : 'mod' '(' pm ( ';' pm)* ')'
  ;
  
pm  : type ident {PtGen.pt(65);} ( ',' ident {PtGen.pt(65);} )*
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
          ('aut' {PtGen.pt(58);PtGen.pt(59);}   instructions |  ) 
          'fcond'  {PtGen.pt(60);} 
  ;
  
boucle  : 'ttq' {PtGen.pt(53);}  expression {PtGen.pt(8);PtGen.pt(54);} 'faire' instructions 'fait' {PtGen.pt(55);}
  ;
  
lecture: 'lire' '(' ident {PtGen.pt(43);PtGen.pt(42);}  ( ',' ident  {PtGen.pt(43);PtGen.pt(42);} )* ')' 
  ;
  
ecriture: 'ecrire' '(' expression {PtGen.pt(44);} ( ',' expression {PtGen.pt(44);} )* ')'
   ;
  
affouappel
  : ident {PtGen.pt(40);} (    ':='  expression {PtGen.pt(41);PtGen.pt(42);}
            | {PtGen.pt(70);}  (effixes (effmods)?)? {PtGen.pt(73);}  
           )
  ;
  
effixes : '(' (expression {PtGen.pt(71);} (',' expression {PtGen.pt(71);} )*)? ')'
  ;
  
effmods :'(' (ident {PtGen.pt(72);} (',' ident {PtGen.pt(72);}  )*)? ')'
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



	   
