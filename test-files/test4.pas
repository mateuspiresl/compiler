program Test3; {programa para teste do lexico}
var
   NUMERO  : integer;
   final   : integer;
begin  {tente gerar um erro usando um caracter não permitido.. tipo $}
   NUMERO := 3 * 5 + 7 - 9;
   if (NUMERO >= 20) and (NUMERO <=90) then
      NUMERO := 10 / 3;
   final := NUMERO + 1	  
end.